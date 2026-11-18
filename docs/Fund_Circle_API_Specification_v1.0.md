# Fund Circle — API Specification & Data Contracts Document (v1.0)
**Document Version:** 1.0  
**Status:** Approved Engineering Specification  
**Target Platform:** Android 8.0+ (Min SDK 26, Target SDK 35, Compile SDK 35), Firebase Cloud BaaS, Kotlin 2.0.21+, Jetpack Compose  
**Authors:** Senior Principal Engineer & Android Architect  
**Traceability Reference:**
- Upstream Product Requirements (Archived): [Investment_Firm_App_PRD_v2.md](file:///Users/kawsar/Desktop/projects/personal/Android/fund-circle/docs/archive/Investment_Firm_App_PRD_v2.md) (v3.0)
- Upstream Functional Spec (Archived): [Investment_Firm_App_FSD_SRS_v1.md](file:///Users/kawsar/Desktop/projects/personal/Android/fund-circle/docs/archive/Investment_Firm_App_FSD_SRS_v1.md) (v2.1)
- Upstream User Stories (Archived): [Investment_Firm_App_User_Stories_Use_Cases_v1.md](file:///Users/kawsar/Desktop/projects/personal/Android/fund-circle/docs/archive/Investment_Firm_App_User_Stories_Use_Cases_v1.md) (v2.0)
- User Flowcharts: [Fund_Circle_User_Flowcharts_v1.0.md](file:///Users/kawsar/Desktop/projects/personal/Android/fund-circle/docs/Fund_Circle_User_Flowcharts_v1.0.md) (v1.1)
- Wireframes & UI Spec: [Fund_Circle_Wireframes_and_UI_Spec_v1.0.md](file:///Users/kawsar/Desktop/projects/personal/Android/fund-circle/docs/Fund_Circle_Wireframes_and_UI_Spec_v1.0.md) (v1.2)
- Design Tokens (Semantic Styling): [Design_Tokens.md](file:///Users/kawsar/Desktop/projects/personal/Android/fund-circle/docs/Design_Tokens.md)
- Branding & Style Guide (Archived): [Fund_Circle_Branding_and_Style_Guide_v1.0.md](file:///Users/kawsar/Desktop/projects/personal/Android/fund-circle/docs/archive/Fund_Circle_Branding_and_Style_Guide_v1.0.md) (v1.2)
- System Architecture & Tech Stack: [Fund_Circle_System_Architecture_and_Tech_Stack_v1.0.md](file:///Users/kawsar/Desktop/projects/personal/Android/fund-circle/docs/Fund_Circle_System_Architecture_and_Tech_Stack_v1.0.md) (v1.0)
- Interactive Design Prototype: [docs/design_preview/index.html](file:///Users/kawsar/Desktop/projects/personal/Android/fund-circle/docs/design_preview/index.html) (v1.2)
- Figma Interactive Prototype: [Fund Circle — Mobile App (Android M3)](https://www.figma.com/design/44Gqq8dDY62FfKf28yxXuJ?node-id=42-3&autoplay-code-preview=42-3)
- Visual Screen Gallery (2x Retina): [docs/design_preview/gallery.html](file:///Users/kawsar/Desktop/projects/personal/Android/fund-circle/docs/design_preview/gallery.html)
- Master Flowchart (FigJam): [Fund Circle — Master User Flow Architecture](https://www.figma.com/board/dxkTCpjsDBnEQp2zJRphAI)

---

## 1. Document Overview & Architectural Conventions

This specification establishes the definitive **Application Programming Interface (API) and Data Contract Blueprint** for the **Fund Circle** ecosystem. It formally documents both tiers of the application's contract boundary:

1. **Layer 1: External Wire & Cloud BaaS Contract (Firebase Spark $0 BaaS):**
   - 100% Client-Side BaaS: Direct Firestore Subcollections, Atomic Batches & Transactions.
   - Declarative Firestore Security Rules enforcing authorization and mathematical invariants.
   - On-Device Pure Kotlin CSV Generation (`LedgerCsvGenerator`) shared via Android `FileProvider`.
   - Firebase Cloud Messaging (FCM) Wire Payloads and Deep Link URL Schemes.
2. **Layer 2: Internal Domain & Repository API Contract (Clean Architecture Boundary):**
   - Pure Kotlin Repository Interfaces (`AuthRepository`, `FundRepository`, `TransactionRepository`, `MemberRepository`, `AnalyticsRepository`).
   - Reactive State Streams (`Flow<T>`) for offline-first, optimistic client rendering.
   - Boundary Data Transfer Objects (DTO), Serialization Rules, and Domain Entity Mappings.
   - Exhaustive, Strongly Typed `FundCircleError` Sealed Hierarchy.

### 1.1 The Seven Core Architectural Invariants

Every API endpoint, query, batch write, and repository interface in this document strictly conforms to these seven invariants:

1. **Deterministic Client UUID Idempotency:** Every financial mutation (`depositId`, `expenseId`, `invitationId`) must be derived from a **client-generated UUIDv4 generated at user intent initiation** (not per retry attempt). Retried network requests target identical document paths and idempotently update without creating duplicates.
2. **$O(1)$ Atomic Aggregate Data Pipeline:** The Home Dashboard loads in exactly **one document read** by reading atomic aggregates (`totalDeposits`, `totalExpenses`, `recordedBalance`) maintained on the parent `/funds/{fundId}` document via Firestore `FieldValue.increment()`.
3. **Whole-Integer Monetary Model:** Every financial figure is stored, validated, and computed as a 64-bit integer (`Long`, $\ge 1$). Decimals, poisha, and cents are strictly rejected (`.00` is illegal).
4. **Base Currency Lock:** The base currency of a circle (`"BDT" | "USD" | "EUR"`) is permanently locked upon recording the first transaction. Mutations attempting to alter `currency` fail with a `409 Conflict` / `PERMISSION_DENIED`.
5. **Multi-Tenant Physical Isolation:** Data is strictly partitioned in subcollections under `/funds/{fundId}/...`. Cross-tenant queries are structurally impossible.
6. **Zero-Read Authorization Vector:** Parent circle documents maintain `memberEmails: List<String>`, enabling security rules to authorize members in $O(1)$ time with zero secondary document lookups.
7. **Immutable Audit Trail:** Financial ledger records cannot be deleted (`allow delete: if false;`). Voiding a transaction preserves the original record (keeping its original positive integer amount $\ge 1$), marks `isVoid = true`, records mandatory `voidReason`, `voidedAt`, and `voidedBy`, and atomically reverses the parent aggregate balance via `FieldValue.increment(-originalAmount)`.

---

### 1.2 Data Types, Enums & Boundary Constraints

| Field / Parameter | Wire Type | Kotlin Domain Type | Constraints & Valid Values |
| :--- | :--- | :--- | :--- |
| `amount` | `Long` (int64) | `Long` | $\ge 1$. Decimals/floats strictly rejected. Max: $9 \times 10^{15}$. |
| `currency` | `String` | `Currency` (Enum) | Strictly `"BDT"`, `"USD"`, or `"EUR"`. Permanently locked once initialized. |
| `category` | `String` | `ExpenseCategory` (Enum) | Strictly 1 of 8: `"Office & Administration"`, `"Travel & Transportation"`, `"Bank & Transaction Charges"`, `"Legal & Documentation"`, `"Meetings & Events"`, `"Utilities & Communication"`, `"Rent & Maintenance"`, `"Others"`. |
| `paymentMethod` | `String` | `PaymentMethod` (Enum) | Strictly 1 of 3: `"Cash"`, `"Bank Transfer"`, `"Mobile Banking"`. |
| `role` | `String` | `MemberRole` (Enum) | Strictly `"ADMIN"` or `"MEMBER"`. |
| `status` | `String` | `MemberStatus` (Enum) | Strictly `"PENDING"`, `"ACTIVE"`, `"INACTIVE"`, or `"LEFT"`. |
| `depositDate` / `expenseDate` | `Long` (Epoch ms) | `Long` | Must be $\le \text{Current Epoch Millis}$. Future dates strictly rejected. |
| `description` / `note` | `String` | `String` | For expense: 3–250 characters. For deposit note: optional, max 250 characters. |
| `voidReason` | `String` | `String` | Mandatory when voiding: 5–250 characters. |
| `nidNumber` | `String` | `String` | Admin-scoped PII: 10, 13, or 17 numeric digits. Masked on client as `•••• •••• 9821`. |

---

## 2. Authentication, Identity & Session Protocol

Fund Circle utilizes **Google One-Tap Sign-In** powered by the **Android Credential Manager API** (`androidx.credentials`) combined with **Firebase Authentication**.

```
┌────────────────────────────────────────────────────────────────────────┐
│                        AUTHENTICATION FLOW                             │
├────────────────────────────────────────────────────────────────────────┤
│  Android Client (Presentation Layer)                                   │
│  ├── 1. Call CredentialManager.getCredential(GetGoogleIdOption)       │
│  │   └── Prompt User One-Tap Bottom Sheet                              │
│  ├── 2. Extract Google ID Token (JWT)                                  │
│  ├── 3. FirebaseAuth.signInWithCredential(GoogleAuthProvider.get...)   │
│  ├── 4. Receive FirebaseUser & Secure ID Token (Valid for 1 Hour)      │
│  └── 5. Store Session & Active Fund ID in EncryptedSharedPreferences  │
└────────────────────────────────────────────────────────────────────────┘
```

### 2.1 Credential Manager Request Contract

```kotlin
// Android Client: Core Credential Request Contract
val googleIdOption = GetGoogleIdOption.Builder()
    .setFilterByAuthorizedAccounts(false)
    .setServerClientId(BuildConfig.WEB_CLIENT_ID)
    .setAutoSelectEnabled(true)
    .setNonce(generateSecureRandomNonce())
    .build()

val request = GetCredentialRequest.Builder()
    .addCredentialOption(googleIdOption)
    .build()
```

### 2.2 Sign-Out & Credential Invalidation Contract

Ensures stored Google auto-select credentials are fully cleared across all Android OS versions, allowing users to switch Google accounts seamlessly:

```kotlin
suspend fun signOut(
    credentialManager: CredentialManager,
    firebaseAuth: FirebaseAuth,
    sessionPreferences: SharedPreferences
): Result<Unit> = runCatching {
    // 1. Invalidate Google One-Tap auto-select state
    credentialManager.clearCredentialState(ClearCredentialStateRequest())
    // 2. Terminate Firebase session
    firebaseAuth.signOut()
    // 3. Clear EncryptedSharedPreferences session keys
    sessionPreferences.edit().clear().apply()
}
```

### 2.3 Firebase Auth Token Lifecycle & Security Claims

- **Token Format:** Cryptographically signed JSON Web Token (JWT).
- **Expiration:** 3,600 seconds (1 hour). Auto-refreshed silently by Firebase Android SDK.
- **Custom Claims:**
  - `user_id` / `sub`: Firebase User UID (`String`).
  - `email`: Authenticated Google Account Email (`String`).
  - `email_verified`: `true` (enforced by Google Identity).
  - `name`: Display Name.
  - `picture`: Profile Avatar URL.

### 2.4 Session Storage & Keystore Security Contract

All local session state is encrypted at rest using `EncryptedSharedPreferences` backed by the **Android Keystore System** using AES-256-GCM.

```kotlin
// Keystore Storage Contract: Key-Value Mapping
object SessionKeys {
    const val KEY_AUTH_TOKEN = "fc_auth_token"
    const val KEY_USER_UID = "fc_user_uid"
    const val KEY_USER_EMAIL = "fc_user_email"
    const val KEY_ACTIVE_CIRCLE_ID = "fc_active_circle_id" // Last visited circle
}
```

---

## 3. Direct Client BaaS Operations & On-Device Engine (Firebase Spark $0 Tier)

To ensure zero hosting costs and full compatibility with the Firebase Spark free tier (requiring no credit card or billing account), privileged orchestration is executed through **direct client Firestore operations guarded by declarative security rules**, combined with **on-device Kotlin engines**.

### 3.1 Member Invitation Protocol (Direct Firestore BaaS)

Dispatches an invitation to an individual by email directly via Firestore subcollection.

- **Firestore Path:** `/funds/{fundId}/members/{memberId}`
- **Operation:** `WriteBatch`
- **Role Requirement:** Caller must be `ADMIN` of the designated `fundId` (enforced by security rules).

#### Invitation Schema
```typescript
interface CircleMemberInvitation {
  memberId: string;       // Sequential (e.g. "FC-002") or UUID
  fundId: string;         // Parent circle UUIDv4
  email: string;          // Invitee email address
  name: string;           // Display name entered by Admin
  phone: string;          // Contact number
  role: "ADMIN" | "MEMBER";
  status: "PENDING";      // Transitions to "ACTIVE" upon acceptance
  invitedAt: number;      // Epoch ms
  invitedBy: string;      // Admin email
}
```

---

### 3.2 Invitation Acceptance & Gate Protocol

When an authenticated user signs in, a collection group query on `members where email == userEmail && status == "PENDING"` identifies pending invitations.

- **Acceptance Action:** User initiates acceptance via a Firestore `WriteBatch`:
  1. Updates `/funds/{fundId}/members/{memberId}`:
     - `userId = request.auth.uid`
     - `status = "ACTIVE"`
     - `joinedAt = System.currentTimeMillis()`
  2. Updates `/funds/{fundId}`:
     - `memberEmails = FieldValue.arrayUnion(request.auth.token.email)`
     - `memberCount = FieldValue.increment(1)`
- **Security Rule Enforcement:** Security rules strictly enforce that `request.auth.token.email == resource.data.email`, guaranteeing users can only accept their own invitations.

---

### 3.3 On-Device Ledger CSV Engine (RFC 4180 Native Generation)

Generates an RFC 4180-compliant CSV export directly on the Android device in pure Kotlin (`LedgerCsvGenerator.kt`).

- **Execution:** Pure Kotlin coroutine executing on `Dispatchers.IO`. Zero server calls, zero Cloud Storage egress costs, works 100% offline.
- **Output:** Stored in app internal cache directory (`context.cacheDir/exports/FundCircle_{CircleName}_Ledger_{Timestamp}.csv`).
- **Sharing:** Dispatched via standard Android `FileProvider` and `Intent.ACTION_SEND` (Android Share Sheet).

#### CSV Structure (RFC 4180 Compliant)
```csv
Transaction ID,Date,Type,Category/Method,Member Name,Member ID,Amount (Whole Integer),Is Void,Void Reason,Recorded By
tx_01a8b9c2,2026-11-13,DEPOSIT,Bank Transfer,Rahim Chowdhury,FC-002,50000,FALSE,,kawsar@example.com
tx_02c3d4e5,2026-11-14,EXPENSE,Legal & Documentation,,,12000,FALSE,,kawsar@example.com
tx_03f6g7h8,2026-11-15,EXPENSE,Office & Administration,,,8500,TRUE,Duplicate vendor invoice,kawsar@example.com
```

---

### 3.4 Direct Snapshot State Listeners & In-App Alerts

Instead of server-side Cloud Function triggers, transaction and administrative events are observed client-side via active Firestore snapshot listeners (`Flow<List<Transaction>>`):
- New transactions and void notices are processed reactively on the client.
- Offline persistence ensures that updates made offline automatically reflect in the UI with a pending-sync indicator and sync when network connectivity resumes.

---

### 3.5 Client-Side Reversals & Void Audit Protocol

Financial document deletion is strictly prohibited (`allow delete: if false;`). Voiding is performed directly via a client-executed Firestore Transaction:
1. Verifies `isVoid == false`.
2. Marks `isVoid = true`, records mandatory `voidReason` (5–250 chars), `voidedBy = auth.token.email`, `voidedAt = currentTimeMillis`.
3. Atomically increments parent balance on `/funds/{fundId}` by `-originalAmount`.
- **Retry Policy:** Exponential backoff, max 3 attempts.

---

## 4. Cloud Firestore Wire Protocol & Data Contracts

Data is arranged hierarchically using Firestore Subcollections. Every wire interaction is defined below.

```
/funds/{fundId}                                  <--- Root Circle Document
   ├── /members/{userId}                         <--- Member Subcollection
   │       └── /kyc/identity                     <--- Admin-Scoped PII Subcollection
   ├── /deposits/{depositId}                     <--- Deposits Ledger Subcollection
   ├── /expenses/{expenseId}                     <--- Expenses Ledger Subcollection
   └── /audit_logs/{logId}                       <--- Immutable Audit Trail Subcollection

/users/{userId}                                  <--- Top-Level User Index
   └── /invitations/{invitationId}               <--- User Invitations Subcollection
```

---

### 4.1 Root Circle Contract: `/funds/{fundId}`

Represents the pooled capital fund entity.

#### Document Schema & Types
```typescript
interface FundCircleDocument {
  fundId: string;              // UUIDv4 (Matches document ID)
  name: string;                // 3–50 chars
  description?: string;        // Max 500 chars
  currency: "BDT" | "USD" | "EUR"; // Locked permanently after 1st transaction
  adminId: string;             // Firebase User UID of Creator
  adminEmail: string;          // Email of Creator
  memberEmails: string[];      // Zero-read authorization vector
  memberCount: number;         // Positive integer >= 1
  totalDeposits: number;       // int64, atomic aggregate (Long >= 0)
  totalExpenses: number;       // int64, atomic aggregate (Long >= 0)
  recordedBalance: number;     // int64, atomic aggregate (Deposits - Expenses)
  isLocked: boolean;           // Default false; true prevents new transactions
  createdAt: number;           // Server timestamp (Epoch ms)
  updatedAt: number;           // Server timestamp (Epoch ms)
}
```

#### Wire Example
```json
{
  "fundId": "4a8e9b21-6c3d-4e5f-8a12-9b3c4d5e6f70",
  "name": "Apex Capital Syndicate",
  "description": "Pooled commercial real-estate growth fund.",
  "currency": "BDT",
  "adminId": "auth_usr_9981a2",
  "adminEmail": "kawsar@example.com",
  "memberEmails": [
    "kawsar@example.com",
    "tanvir.ahmed@example.com",
    "nafis.iqbal@example.com"
  ],
  "memberCount": 3,
  "totalDeposits": 5300000,
  "totalExpenses": 450000,
  "recordedBalance": 4850000,
  "isLocked": false,
  "createdAt": 1704067200000,
  "updatedAt": 1731672000000
}
```

#### Mutation Rules
1. **Creation:** `totalDeposits`, `totalExpenses`, and `recordedBalance` must strictly equal `0`.
2. **Update:** `currency` field is **immutable** once initialized. Updates attempting to change `currency` fail security rules.
3. **Deletion:** Permitted only if `totalDeposits == 0 && totalExpenses == 0`.

---

### 4.2 Member Directory Contract: `/funds/{fundId}/members/{userId}`

#### Document Schema
```typescript
interface CircleMemberDocument {
  userId: string;              // User UID (Matches document ID)
  memberId: string;            // Sequential circle identifier e.g. "FC-001"
  email: string;               // Member email
  name: string;                // Member display name
  role: "ADMIN" | "MEMBER";    // Circle role
  status: "PENDING" | "ACTIVE" | "INACTIVE" | "LEFT";
  totalContributed: number;    // int64 (Long >= 0)
  joinedAt: number;            // Epoch ms
  lastActiveAt: number;        // Epoch ms
}
```

---

### 4.3 Admin-Scoped KYC PII Contract: `/funds/{fundId}/members/{userId}/kyc/identity`

Stores sensitive National Identity and Nominee details. Accessible strictly by the **Circle Admin** and the **individual user**.

#### Document Schema
```typescript
interface MemberKycDocument {
  userId: string;
  nidNumber: string;           // 10, 13, or 17 numeric digits
  nomineeName: string;         // 3–100 chars
  nomineePhone: string;        // E.164 phone format
  nomineeRelation: string;     // e.g., "Spouse", "Brother", "Daughter"
  verifiedByAdmin: boolean;
  updatedAt: number;
}
```

#### Masking Rule:
The client-side UI must mask the NID using `PrivacyMaskUtils.maskNid(nid)` resulting in `•••• •••• 9821`. Raw unmasked NID is displayed only when the Circle Admin explicitly activates *"Reveal NID"*.

---

### 4.4 Deposits Ledger Contract: `/funds/{fundId}/deposits/{depositId}`

Represents an individual capital contribution.

#### Document Schema
```typescript
interface DepositDocument {
  depositId: string;           // UUIDv4 (Matches document ID)
  fundId: string;
  memberId: string;            // User UID of contributor
  memberEmail: string;
  memberName: string;
  amount: number;              // int64, strictly positive whole integer >= 1
  depositDate: number;         // Epoch ms (<= current timestamp)
  paymentMethod: "Cash" | "Bank Transfer" | "Mobile Banking";
  note: string | null;         // Max 250 characters
  isVoid: boolean;             // Default false
  voidReason: string | null;   // Mandatory if isVoid == true (5-250 chars)
  voidedAt: number | null;     // Epoch ms
  voidedBy: string | null;     // Admin email who executed void
  correctionHistory: AuditDiffEntry[]; // Immutable history of modifications
  createdAt: number;           // Server timestamp
}

interface AuditDiffEntry {
  timestamp: number;
  actorEmail: string;
  fieldChanged: string;
  previousValue: any;
  newValue: any;
  reason: string;
}
```

#### Atomic Batch Mutation Protocol (`recordDeposit`)
Executing a deposit requires writing to two documents simultaneously in a **Firestore `WriteBatch`**:

```
OPERATION: WriteBatch
├── 1. SET   /funds/{fundId}/deposits/{depositId}  <--- DepositDocument
└── 2. UPDATE /funds/{fundId}
       ├── totalDeposits   += deposit.amount (FieldValue.increment)
       ├── recordedBalance += deposit.amount (FieldValue.increment)
       └── updatedAt       =  request.time
```

- **Idempotency Guarantee:** If the Android client times out and retries, the `depositId` (UUID) remains identical. The `SET` overwrites the same document, preventing duplicate records.

---

### 4.5 Expenses Ledger Contract: `/funds/{fundId}/expenses/{expenseId}`

Represents operational or asset expenditures incurred by the circle.

#### Document Schema
```typescript
interface ExpenseDocument {
  expenseId: string;           // UUIDv4 (Matches document ID)
  fundId: string;
  amount: number;              // int64, strictly positive whole integer >= 1
  expenseDate: number;         // Epoch ms (<= current timestamp)
  category: "Office & Administration" |
            "Travel & Transportation" |
            "Bank & Transaction Charges" |
            "Legal & Documentation" |
            "Meetings & Events" |
            "Utilities & Communication" |
            "Rent & Maintenance" |
            "Others";
  description: string;         // 3–250 characters
  paymentMethod: "Cash" | "Bank Transfer" | "Mobile Banking";
  paidBy: string;              // Payee / Vendor name (e.g., "Apex Properties Ltd.")
  createdBy: string;           // Admin User UID
  creatorEmail: string;
  isVoid: boolean;             // Default false
  voidReason: string | null;
  voidedAt: number | null;
  voidedBy: string | null;
  correctionHistory: AuditDiffEntry[];
  createdAt: number;
}
```

#### Atomic Batch Mutation Protocol (`recordExpense`)
```
OPERATION: WriteBatch
├── 1. SET   /funds/{fundId}/expenses/{expenseId}  <--- ExpenseDocument
└── 2. UPDATE /funds/{fundId}
       ├── totalExpenses   += expense.amount (FieldValue.increment)
       ├── recordedBalance -= expense.amount (FieldValue.increment)
       └── updatedAt       =  request.time
```

---

### 4.6 Void Transaction Protocol (Atomic Transaction Reversal)

Deleting financial documents is strictly prohibited. When an administrative correction is required, the transaction is **voided** using a **Firestore Transaction**:

```
OPERATION: Firestore.runTransaction
├── 1. READ  /funds/{fundId}/{deposits|expenses}/{txId}
│      ├── Verify isVoid == false (If true, throw ALREADY_VOIDED)
│      └── Extract originalAmount
├── 2. UPDATE /funds/{fundId}/{deposits|expenses}/{txId}
│      ├── isVoid      = true
│      ├── voidReason  = reason (5-250 chars)
│      ├── voidedBy    = adminEmail
│      └── voidedAt    = System.currentTimeMillis()
└── 3. UPDATE /funds/{fundId}
       └── If voiding DEPOSIT:
           ├── totalDeposits   += (-originalAmount)
           └── recordedBalance += (-originalAmount)
       └── If voiding EXPENSE:
           ├── totalExpenses   += (-originalAmount)
           └── recordedBalance += (+originalAmount)
```

---

### 4.7 Cursor-Based Ledger Query Contracts

To achieve $60\text{ fps}$ scrolling without memory spikes or expensive database full-table scans, the client queries ledgers using **Cursor-Based Pagination**:

#### Deposits Ledger Query Contract
```
Query:
  collection: /funds/{fundId}/deposits
  where:      isVoid == false (Optional filter)
  orderBy:    depositDate DESC
  orderBy:    __name__ DESC
  limit:      20
  startAfter: [lastVisibleDepositDate, lastVisibleDocId]
```

#### Expenses Ledger Query Contract
```
Query:
  collection: /funds/{fundId}/expenses
  where:      category == selectedCategory (Optional filter)
  orderBy:    expenseDate DESC
  orderBy:    __name__ DESC
  limit:      20
  startAfter: [lastVisibleExpenseDate, lastVisibleDocId]
```

---

### 4.8 User Profile & Invitations Contract: `/users/{userId}`

Represents global user profile metadata and incoming circle invitations.

#### User Document Schema (`/users/{userId}`)
```typescript
interface UserProfileDocument {
  userId: string;              // Firebase Auth UID (Matches document ID)
  email: string;               // Authenticated Google account email
  displayName: string;         // Google profile display name
  photoUrl: string | null;     // Google profile avatar URL
  createdCircleCount: number;  // Non-negative integer (Enforces max 5 circles)
  createdAt: number;           // Epoch ms
  lastActiveAt: number;        // Epoch ms
}
```

#### User Invitation Subcollection Contract (`/users/{userId}/invitations/{invitationId}`)
```typescript
interface UserInvitationDocument {
  invitationId: string;        // UUIDv4 (Matches document ID)
  fundId: string;              // Circle reference UUID
  fundName: string;            // Cached circle name for zero-read display
  invitedByEmail: string;      // Admin email who sent the invitation
  assignedRole: "ADMIN" | "STAKEHOLDER";
  status: "PENDING" | "ACCEPTED" | "DECLINED";
  createdAt: number;           // Epoch ms
}
```

---

## 5. Android Clean Architecture Repository APIs (Kotlin Contracts)

The Android presentation layer communicates with the data layer solely through **Domain Repository Interfaces**. These interfaces expose pure Kotlin models and reactive streams (`Flow<T>`).

### 5.1 `AuthRepository` Interface

```kotlin
package com.fundcircle.app.domain.repository

import com.fundcircle.app.domain.model.UserSession
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    /** Emits real-time authentication session state */
    val userSession: Flow<UserSession?>

    /** Authenticates using Google ID Token from Credential Manager */
    suspend fun signInWithGoogle(idToken: String): Result<UserSession>

    /** Clears session state and sign out from Firebase */
    suspend fun signOut(): Result<Unit>

    /** Retrieves the active circle ID cached in EncryptedSharedPreferences */
    suspend fun getActiveCircleId(): String?

    /** Persists the last viewed circle ID for fast-path launch */
    suspend fun setActiveCircleId(fundId: String): Result<Unit>
}
```

---

### 5.2 `FundRepository` Interface

```kotlin
package com.fundcircle.app.domain.repository

import com.fundcircle.app.domain.model.FundCircle
import com.fundcircle.app.domain.model.Currency
import kotlinx.coroutines.flow.Flow

interface FundRepository {
    /** Observes real-time aggregates and metadata for a specific circle */
    fun observeCircle(fundId: String): Flow<FundCircle>

    /** Observes all circles the authenticated user belongs to (max 5) */
    fun observeUserCircles(): Flow<List<FundCircle>>

    /** Creates a new circle with initial aggregate zero counters */
    suspend fun createCircle(name: String, currency: Currency, description: String?): Result<String>

    /** Locks circle to prevent new transactions (Admin only) */
    suspend fun toggleCircleLock(fundId: String, isLocked: Boolean): Result<Unit>
}
```

---

### 5.3 `TransactionRepository` Interface

```kotlin
package com.fundcircle.app.domain.repository

import com.fundcircle.app.domain.model.Deposit
import com.fundcircle.app.domain.model.Expense
import com.fundcircle.app.domain.model.TransactionItem
import com.fundcircle.app.domain.model.ExpenseCategory
import com.fundcircle.app.domain.model.PagedResult
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    /** Observes the 5 most recent transactions for the Home Dashboard */
    fun observeRecentTransactions(fundId: String, limit: Int = 5): Flow<List<TransactionItem>>

    /** Fetches a cursor-paginated page of deposits */
    suspend fun getDepositsPaged(
        fundId: String,
        pageSize: Int = 20,
        lastDate: Long? = null,
        lastDocId: String? = null
    ): Result<PagedResult<Deposit>>

    /** Fetches a cursor-paginated page of expenses with optional category filtering */
    suspend fun getExpensesPaged(
        fundId: String,
        category: ExpenseCategory? = null,
        pageSize: Int = 20,
        lastDate: Long? = null,
        lastDocId: String? = null
    ): Result<PagedResult<Expense>>

    /** Executes atomic batch insertion of a deposit */
    suspend fun recordDeposit(fundId: String, deposit: Deposit): Result<Unit>

    /** Executes atomic batch insertion of an expense */
    suspend fun recordExpense(fundId: String, expense: Expense): Result<Unit>

    /** Executes atomic transactional reversal and void recording */
    suspend fun voidTransaction(
        fundId: String,
        isExpense: Boolean,
        transactionId: String,
        originalAmount: Long,
        voidReason: String
    ): Result<Unit>
}
```

---

### 5.4 `MemberRepository` Interface

```kotlin
package com.fundcircle.app.domain.repository

import com.fundcircle.app.domain.model.CircleMember
import com.fundcircle.app.domain.model.MemberKyc
import com.fundcircle.app.domain.model.MemberRole
import kotlinx.coroutines.flow.Flow

interface MemberRepository {
    /** Observes the list of members in a circle */
    fun observeMembers(fundId: String): Flow<List<CircleMember>>

    /** Retrieves strictly admin-scoped KYC identity PII */
    suspend fun getMemberKyc(fundId: String, userId: String): Result<MemberKyc>

    /** Updates KYC identity details for a member */
    suspend fun updateMemberKyc(fundId: String, kyc: MemberKyc): Result<Unit>

    /** Dispatches a circle invitation to a stakeholder */
    suspend fun inviteMember(fundId: String, name: String, phone: String, email: String): Result<String>

    /** Removes a member or marks them as LEFT */
    suspend fun removeMember(fundId: String, userId: String): Result<Unit>
}
```

---

### 5.5 `AnalyticsRepository` Interface

```kotlin
package com.fundcircle.app.domain.repository

import com.fundcircle.app.domain.model.MonthlyFinancialSummary
import com.fundcircle.app.domain.model.CategoryBreakdownItem
import kotlinx.coroutines.flow.Flow

interface AnalyticsRepository {
    /** Observes monthly cash inflows, outflows, and net delta */
    fun observeMonthlySummary(fundId: String, year: Int, month: Int): Flow<MonthlyFinancialSummary>

    /** Observes spending distribution across the 8 predefined categories */
    fun observeCategoryBreakdown(fundId: String, year: Int, month: Int): Flow<List<CategoryBreakdownItem>>
}
```

---

## 6. Data Transfer Objects (DTO) & Domain Model Specifications

To prevent cloud schema changes from destabilizing UI presentation, the architecture enforces a strict separation between **Data Layer DTOs** and **Domain Layer Entities**.

### 6.1 Common Domain Containers & Enums

```kotlin
package com.fundcircle.app.domain.model

/** Cursor-paginated result wrapper for infinite scrolling lists */
data class PagedResult<T>(
    val items: List<T>,
    val lastDate: Long?,
    val lastDocId: String?,
    val hasMore: Boolean
)

/** Immutable metadata recorded upon transaction voiding */
data class VoidMetadata(
    val reason: String,
    val voidedAt: Long,
    val voidedBy: String
)

/** Locked base currencies */
enum class Currency(val code: String, val symbol: String) {
    BDT("BDT", "৳"),
    USD("USD", "$"),
    EUR("EUR", "€");

    companion object {
        fun fromString(value: String): Currency = entries.firstOrNull { 
            it.code.equals(value.trim(), ignoreCase = true) 
        } ?: BDT
    }
}

/** Circle member authorization roles */
enum class MemberRole {
    ADMIN, MEMBER;

    companion object {
        fun fromString(value: String): MemberRole = entries.firstOrNull {
            it.name.equals(value.trim(), ignoreCase = true)
        } ?: MEMBER
    }
}

/** Circle member lifecycle status */
enum class MemberStatus {
    PENDING, ACTIVE, INACTIVE, LEFT;

    companion object {
        fun fromString(value: String): MemberStatus = entries.firstOrNull {
            it.name.equals(value.trim(), ignoreCase = true)
        } ?: PENDING
    }
}

/** Predefined transaction payment methods */
enum class PaymentMethod(val displayName: String) {
    CASH("Cash"),
    BANK_TRANSFER("Bank Transfer"),
    MOBILE_BANKING("Mobile Banking");

    companion object {
        fun fromString(value: String): PaymentMethod = entries.firstOrNull {
            it.displayName.equals(value.trim(), ignoreCase = true) ||
            it.name.equals(value.trim(), ignoreCase = true)
        } ?: CASH
    }
}

/** Strictly 8 predefined operational expense categories */
enum class ExpenseCategory(val displayName: String) {
    OFFICE_AND_ADMINISTRATION("Office & Administration"),
    TRAVEL_AND_TRANSPORTATION("Travel & Transportation"),
    BANK_AND_TRANSACTION_CHARGES("Bank & Transaction Charges"),
    LEGAL_AND_DOCUMENTATION("Legal & Documentation"),
    MEETINGS_AND_EVENTS("Meetings & Events"),
    UTILITIES_AND_COMMUNICATION("Utilities & Communication"),
    RENT_AND_MAINTENANCE("Rent & Maintenance"),
    OTHERS("Others");

    companion object {
        fun fromString(value: String): ExpenseCategory = entries.firstOrNull {
            it.displayName.equals(value.trim(), ignoreCase = true) ||
            it.name.equals(value.trim().replace(" ", "_").replace("&", "AND"), ignoreCase = true)
        } ?: OTHERS
    }
}
```

---

### 6.2 Deposit DTO, Domain Entity & Mapper

```kotlin
// Data Layer DTO (com.fundcircle.app.data.dto)
@kotlinx.serialization.Serializable
data class DepositDto(
    val depositId: String = "",
    val fundId: String = "",
    val memberId: String = "",
    val memberEmail: String = "",
    val memberName: String = "",
    val amount: Long = 0L,
    val depositDate: Long = 0L,
    val paymentMethod: String = "",
    val note: String? = null,
    val isVoid: Boolean = false,
    val voidReason: String? = null,
    val voidedAt: Long? = null,
    val voidedBy: String? = null,
    val createdAt: Long = 0L
)

// Domain Layer Entity (com.fundcircle.app.domain.model)
data class Deposit(
    val id: String,
    val fundId: String,
    val memberId: String,
    val memberEmail: String,
    val memberName: String,
    val amount: Long,
    val depositDate: Long,
    val paymentMethod: PaymentMethod,
    val note: String?,
    val isVoid: Boolean,
    val voidMetadata: VoidMetadata?
)

// Boundary Mapper with Strict Invariant Verification
fun DepositDto.toDomain(): Deposit {
    require(amount >= 1L) { "Deposit amount must be a positive integer >= 1" }
    require(depositDate > 0L) { "Deposit date must be a valid epoch timestamp" }

    return Deposit(
        id = depositId,
        fundId = fundId,
        memberId = memberId,
        memberEmail = memberEmail,
        memberName = memberName,
        amount = amount,
        depositDate = depositDate,
        paymentMethod = PaymentMethod.fromString(paymentMethod),
        note = note?.takeIf { it.isNotBlank() },
        isVoid = isVoid,
        voidMetadata = if (isVoid) {
            VoidMetadata(
                reason = voidReason.orEmpty(),
                voidedAt = voidedAt ?: 0L,
                voidedBy = voidedBy.orEmpty()
            )
        } else null
    )
}

fun Deposit.toDto(): DepositDto = DepositDto(
    depositId = id,
    fundId = fundId,
    memberId = memberId,
    memberEmail = memberEmail,
    memberName = memberName,
    amount = amount,
    depositDate = depositDate,
    paymentMethod = paymentMethod.displayName,
    note = note,
    isVoid = isVoid,
    voidReason = voidMetadata?.reason,
    voidedAt = voidMetadata?.voidedAt,
    voidedBy = voidMetadata?.voidedBy,
    createdAt = depositDate
)
```

---

### 6.3 Expense DTO, Domain Entity & Mapper

```kotlin
// Data Layer DTO (com.fundcircle.app.data.dto)
@kotlinx.serialization.Serializable
data class ExpenseDto(
    val expenseId: String = "",
    val fundId: String = "",
    val amount: Long = 0L,
    val expenseDate: Long = 0L,
    val category: String = "",
    val description: String = "",
    val paymentMethod: String = "",
    val paidBy: String = "",
    val createdBy: String = "",
    val creatorEmail: String = "",
    val isVoid: Boolean = false,
    val voidReason: String? = null,
    val voidedAt: Long? = null,
    val voidedBy: String? = null,
    val createdAt: Long = 0L
)

// Domain Layer Entity (com.fundcircle.app.domain.model)
data class Expense(
    val id: String,
    val fundId: String,
    val amount: Long,
    val expenseDate: Long,
    val category: ExpenseCategory,
    val description: String,
    val paymentMethod: PaymentMethod,
    val paidBy: String,
    val createdBy: String,
    val creatorEmail: String,
    val isVoid: Boolean,
    val voidMetadata: VoidMetadata?
)

// Boundary Mapper with Strict Invariant Verification
fun ExpenseDto.toDomain(): Expense {
    require(amount >= 1L) { "Expense amount must be a positive integer >= 1" }
    require(description.length in 3..250) { "Description must be between 3 and 250 characters" }

    return Expense(
        id = expenseId,
        fundId = fundId,
        amount = amount,
        expenseDate = expenseDate,
        category = ExpenseCategory.fromString(category),
        description = description,
        paymentMethod = PaymentMethod.fromString(paymentMethod),
        paidBy = paidBy,
        createdBy = createdBy,
        creatorEmail = creatorEmail,
        isVoid = isVoid,
        voidMetadata = if (isVoid) {
            VoidMetadata(
                reason = voidReason.orEmpty(),
                voidedAt = voidedAt ?: 0L,
                voidedBy = voidedBy.orEmpty()
            )
        } else null
    )
}

fun Expense.toDto(): ExpenseDto = ExpenseDto(
    expenseId = id,
    fundId = fundId,
    amount = amount,
    expenseDate = expenseDate,
    category = category.displayName,
    description = description,
    paymentMethod = paymentMethod.displayName,
    paidBy = paidBy,
    createdBy = createdBy,
    creatorEmail = creatorEmail,
    isVoid = isVoid,
    voidReason = voidMetadata?.reason,
    voidedAt = voidMetadata?.voidedAt,
    voidedBy = voidMetadata?.voidedBy,
    createdAt = expenseDate
)
```

---

### 6.4 Fund Circle DTO, Domain Entity & Mapper

```kotlin
// Data Layer DTO (com.fundcircle.app.data.dto)
@kotlinx.serialization.Serializable
data class FundCircleDto(
    val fundId: String = "",
    val name: String = "",
    val description: String? = null,
    val currency: String = "BDT",
    val adminId: String = "",
    val adminEmail: String = "",
    val memberEmails: List<String> = emptyList(),
    val memberCount: Int = 1,
    val totalDeposits: Long = 0L,
    val totalExpenses: Long = 0L,
    val recordedBalance: Long = 0L,
    val isLocked: Boolean = false,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)

// Domain Layer Entity (com.fundcircle.app.domain.model)
data class FundCircle(
    val id: String,
    val name: String,
    val description: String?,
    val currency: Currency,
    val adminId: String,
    val adminEmail: String,
    val memberEmails: List<String>,
    val memberCount: Int,
    val totalDeposits: Long,
    val totalExpenses: Long,
    val recordedBalance: Long,
    val isLocked: Boolean,
    val createdAt: Long
)

fun FundCircleDto.toDomain(): FundCircle = FundCircle(
    id = fundId,
    name = name,
    description = description,
    currency = Currency.fromString(currency),
    adminId = adminId,
    adminEmail = adminEmail,
    memberEmails = memberEmails,
    memberCount = memberCount,
    totalDeposits = totalDeposits,
    totalExpenses = totalExpenses,
    recordedBalance = recordedBalance,
    isLocked = isLocked,
    createdAt = createdAt
)
```

---

### 6.5 Member DTO, Domain Entity & Mapper

```kotlin
// Data Layer DTO (com.fundcircle.app.data.dto)
@kotlinx.serialization.Serializable
data class CircleMemberDto(
    val userId: String = "",
    val memberId: String = "",
    val email: String = "",
    val name: String = "",
    val role: String = "MEMBER",
    val status: String = "ACTIVE",
    val totalContributed: Long = 0L,
    val joinedAt: Long = 0L,
    val lastActiveAt: Long = 0L
)

// Domain Layer Entity (com.fundcircle.app.domain.model)
data class CircleMember(
    val userId: String,
    val memberId: String,
    val email: String,
    val name: String,
    val role: MemberRole,
    val status: MemberStatus,
    val totalContributed: Long,
    val joinedAt: Long,
    val lastActiveAt: Long
)

fun CircleMemberDto.toDomain(): CircleMember = CircleMember(
    userId = userId,
    memberId = memberId,
    email = email,
    name = name,
    role = MemberRole.fromString(role),
    status = MemberStatus.fromString(status),
    totalContributed = totalContributed,
    joinedAt = joinedAt,
    lastActiveAt = lastActiveAt
)
```

---

### 6.6 Member KYC DTO, Domain Entity & Mapper

```kotlin
// Data Layer DTO (com.fundcircle.app.data.dto)
@kotlinx.serialization.Serializable
data class MemberKycDto(
    val userId: String = "",
    val nidNumber: String = "",
    val nomineeName: String = "",
    val nomineePhone: String = "",
    val nomineeRelation: String = "",
    val verifiedByAdmin: Boolean = false,
    val updatedAt: Long = 0L
)

// Domain Layer Entity (com.fundcircle.app.domain.model)
data class MemberKyc(
    val userId: String,
    val nidNumber: String,
    val maskedNid: String,
    val nomineeName: String,
    val nomineePhone: String,
    val nomineeRelation: String,
    val verifiedByAdmin: Boolean,
    val updatedAt: Long
)

fun MemberKycDto.toDomain(): MemberKyc {
    val masked = if (nidNumber.length >= 4) {
        "•••• •••• " + nidNumber.takeLast(4)
    } else {
        "•••• •••• ••••"
    }

    return MemberKyc(
        userId = userId,
        nidNumber = nidNumber,
        maskedNid = masked,
        nomineeName = nomineeName,
        nomineePhone = nomineePhone,
        nomineeRelation = nomineeRelation,
        verifiedByAdmin = verifiedByAdmin,
        updatedAt = updatedAt
    )
}
```

---

### 6.7 User Session & Presentation Models

```kotlin
package com.fundcircle.app.domain.model

/** User authentication session model */
data class UserSession(
    val uid: String,
    val email: String,
    val displayName: String,
    val photoUrl: String?,
    val idToken: String,
    val activeCircleId: String?
)

/** Unified ledger item for the Dashboard and Recent Activities */
data class TransactionItem(
    val id: String,
    val fundId: String,
    val isExpense: Boolean,
    val title: String,
    val subtitle: String,
    val amount: Long,
    val timestamp: Long,
    val paymentMethod: PaymentMethod,
    val isVoid: Boolean,
    val category: ExpenseCategory? = null
)
```

---

### 6.8 User Profile & Invitation DTOs and Domain Models

```kotlin
package com.fundcircle.app.data.dto

import kotlinx.serialization.Serializable
import com.fundcircle.app.domain.model.UserProfile
import com.fundcircle.app.domain.model.UserInvitation
import com.fundcircle.app.domain.model.MemberRole
import com.fundcircle.app.domain.model.InvitationStatus

// Data Layer DTOs (com.fundcircle.app.data.dto)
@Serializable
data class UserProfileDto(
    val userId: String = "",
    val email: String = "",
    val displayName: String = "",
    val photoUrl: String? = null,
    val createdCircleCount: Int = 0,
    val createdAt: Long = 0L,
    val lastActiveAt: Long = 0L
)

@Serializable
data class UserInvitationDto(
    val invitationId: String = "",
    val fundId: String = "",
    val fundName: String = "",
    val invitedByEmail: String = "",
    val assignedRole: String = "STAKEHOLDER",
    val status: String = "PENDING",
    val createdAt: Long = 0L
)

// Domain Layer Entities (com.fundcircle.app.domain.model)
data class UserProfile(
    val userId: String,
    val email: String,
    val displayName: String,
    val photoUrl: String?,
    val createdCircleCount: Int,
    val createdAt: Long,
    val lastActiveAt: Long
)

data class UserInvitation(
    val invitationId: String,
    val fundId: String,
    val fundName: String,
    val invitedByEmail: String,
    val assignedRole: MemberRole,
    val status: InvitationStatus,
    val createdAt: Long
)

enum class InvitationStatus {
    PENDING, ACCEPTED, DECLINED;

    companion object {
        fun fromString(value: String): InvitationStatus = runCatching {
            valueOf(value.uppercase())
        }.getOrDefault(PENDING)
    }
}

fun UserProfileDto.toDomain(): UserProfile = UserProfile(
    userId = userId,
    email = email,
    displayName = displayName,
    photoUrl = photoUrl,
    createdCircleCount = createdCircleCount,
    createdAt = createdAt,
    lastActiveAt = lastActiveAt
)

fun UserInvitationDto.toDomain(): UserInvitation = UserInvitation(
    invitationId = invitationId,
    fundId = fundId,
    fundName = fundName,
    invitedByEmail = invitedByEmail,
    assignedRole = MemberRole.fromString(assignedRole),
    status = InvitationStatus.fromString(status),
    createdAt = createdAt
)
```

---

### 6.9 Analytics & Financial Summary Models

```kotlin
package com.fundcircle.app.domain.model

/** Monthly aggregated cash inflow and outflow */
data class MonthlyFinancialSummary(
    val year: Int,
    val month: Int,
    val totalDeposits: Long,
    val totalExpenses: Long,
    val netCashFlow: Long, // totalDeposits - totalExpenses
    val transactionCount: Int
)

/** Distribution item for the 8 predefined expense categories */
data class CategoryBreakdownItem(
    val category: ExpenseCategory,
    val totalAmount: Long,
    val percentage: Float, // 0.0f - 100.0f
    val expenseCount: Int
)
```

---

## 7. Error Code Taxonomy & HTTP/Firestore Status Mapping

All errors encountered across Network, Firebase Auth, and Cloud Firestore are deterministically mapped to the sealed `FundCircleError` hierarchy:

```
┌────────────────────────────────────────────────────────────────────────┐
│                        ERROR MAPPING PIPELINE                          │
├────────────────────────────────────────────────────────────────────────┤
│  Wire / Service Error (FirebaseFirestoreException / FirebaseAuthExc)   │
│  └── ExceptionMapper.toDomainError(e: Throwable): FundCircleError      │
│      ├── Validation.InvalidAmount                                      │
│      ├── Authorization.NotCircleMember                                 │
│      ├── Conflict.TransactionAlreadyVoided                             │
│      └── Network.OfflineNoCache                                        │
└────────────────────────────────────────────────────────────────────────┘
```

### 7.1 Deterministic Error Mapping Table

| Source Error Code | Source Service | HTTP Status | Domain Error Class | UI Presentation Message |
| :--- | :--- | :---: | :--- | :--- |
| `PERMISSION_DENIED` | Firestore / Security Rules | `403` | `FundCircleError.Authorization.NotAuthorized` | "You do not have administrative permission for this action." |
| `NOT_FOUND` | Cloud Firestore | `404` | `FundCircleError.NotFound.ResourceNotFound` | "The requested circle or transaction could not be found." |
| `ALREADY_EXISTS` | Cloud Firestore | `409` | `FundCircleError.Conflict.DuplicateEntry` | "A transaction or invitation with this ID already exists." |
| `ALREADY_VOIDED` | Firestore Transaction | `409` | `FundCircleError.Conflict.TransactionAlreadyVoided` | "This transaction has already been voided." |
| `CIRCLE_LIMIT_REACHED`| Firestore / Client Rule | `409` | `FundCircleError.Business.CircleLimitReached` | "You have reached the maximum limit of 5 active circles." |
| `UNAVAILABLE` | Firestore / Android SDK | `503` | `FundCircleError.Network.ServiceUnavailable` | "Server is temporarily unavailable. Viewing offline cache." |
| `FAILED_PRECONDITION`| Firestore / Security Rules | `412` | `FundCircleError.Validation.PreconditionFailed` | "Operation rejected: currency is permanently locked." |

### 7.2 The Sealed Kotlin `FundCircleError` Hierarchy

```kotlin
package com.fundcircle.app.domain.model

sealed interface FundCircleError {
    val message: String

    sealed class Validation(override val message: String) : FundCircleError {
        data class InvalidAmount(val actual: Long) : Validation("Amount must be a whole positive integer >= 1.")
        data class FutureDateNotAllowed(val timestamp: Long) : Validation("Transaction date cannot be in the future.")
        data class DescriptionTooShort(val length: Int) : Validation("Description must be between 3 and 250 characters.")
        data class VoidReasonRequired(val length: Int) : Validation("A void reason of at least 5 characters is mandatory.")
    }

    sealed class Authorization(override val message: String) : FundCircleError {
        data object NotCircleMember : Authorization("You are not an active member of this circle.")
        data object AdminPrivilegeRequired : Authorization("This operation requires Circle Administrator privileges.")
        data object SessionExpired : Authorization("Your session has expired. Please sign in again.")
    }

    sealed class Conflict(override val message: String) : FundCircleError {
        data class TransactionAlreadyVoided(val txId: String) : Conflict("Transaction $txId has already been voided.")
        data object CurrencyLocked : Conflict("Base currency is permanently locked once transactions exist.")
    }

    sealed class Business(override val message: String) : FundCircleError {
        data object CircleLimitReached : Business("You have reached the maximum limit of 5 circles.")
        data object CannotDeleteActiveFund : Business("Circles with recorded deposits or expenses cannot be deleted.")
    }

    sealed class Network(override val message: String) : FundCircleError {
        data object OfflineNoCache : Network("Device is offline and requested data is not cached.")
        data object Timeout : Network("The operation timed out. Please check your network connection.")
    }
}
```

---

## 8. External Integrations, Deep Links & Push Notification Contracts

### 8.1 Deep Linking Contract (`fundcircle://invite`)

Invitations sent via email or messaging apps use Android App Links:

- **URI Format:** `https://fundcircle.app/invite/{fundId}?invitationId={invitationId}&role={role}`
- **Fallback Custom Scheme:** `fundcircle://invite?fundId={fundId}&invitationId={invitationId}&role={role}`
- **Intent Filter Specification (`AndroidManifest.xml`):**

```xml
<!-- Verified Android App Links (Domain Verification) -->
<intent-filter android:autoVerify="true">
    <action android:name="android.intent.action.VIEW" />
    <category android:name="android.intent.category.DEFAULT" />
    <category android:name="android.intent.category.BROWSABLE" />
    <data android:scheme="https" android:host="fundcircle.app" android:pathPrefix="/invite" />
</intent-filter>

<!-- Fallback Custom Scheme (No autoVerify) -->
<intent-filter>
    <action android:name="android.intent.action.VIEW" />
    <category android:name="android.intent.category.DEFAULT" />
    <category android:name="android.intent.category.BROWSABLE" />
    <data android:scheme="fundcircle" android:host="invite" />
</intent-filter>
```

---

### 8.2 Firebase Cloud Messaging (FCM) Data Payloads

All push notifications use high-priority data messages to ensure background delivery across Android 13+ (API 33+).

#### 1. New Deposit Notification Payload
```json
{
  "message": {
    "topic": "fund_4a8e9b21_transactions",
    "data": {
      "type": "NEW_DEPOSIT",
      "fundId": "4a8e9b21-6c3d-4e5f-8a12-9b3c4d5e6f70",
      "fundName": "Apex Capital Syndicate",
      "transactionId": "dep_99812a4b",
      "memberName": "Tanvir Ahmed",
      "amount": "250000",
      "currency": "BDT",
      "timestamp": "1731672000000"
    },
    "android": {
      "priority": "HIGH"
    }
  }
}
```

#### 2. Transaction Voided Notification Payload
```json
{
  "message": {
    "topic": "fund_4a8e9b21_transactions",
    "data": {
      "type": "TRANSACTION_VOIDED",
      "fundId": "4a8e9b21-6c3d-4e5f-8a12-9b3c4d5e6f70",
      "fundName": "Apex Capital Syndicate",
      "transactionId": "exp_77123c8d",
      "voidedBy": "kawsar@example.com",
      "originalAmount": "15000",
      "voidReason": "Duplicate billing error reported by vendor",
      "timestamp": "1731672600000"
    },
    "android": {
      "priority": "HIGH"
    }
  }
}
```

---

### 8.3 On-Device Export Cache & Optional Cloud Storage Hierarchy

In alignment with the \$0 operating budget (Firebase Spark plan), CSV ledgers are generated directly on the Android device and shared via `FileProvider` without consuming Cloud Storage bandwidth or storage quota.

- **Local Export Cache Path:** `context.cacheDir/exports/FundCircle_{circleName}_Ledger_{timestamp}.csv`
- **FileProvider URI Scheme:** `content://com.fundcircle.app.fileprovider/exports/...`
- **Optional Cloud Storage Bucket (if enabled):**
```
gs://fund-circle-app.appspot.com/
└── /funds/{fundId}/
        └── /receipts/{depositId}/
                └── proof_{timestamp}.jpg        <--- Optional Deposit Proof Attachments (Max 5MB)
```

- **Storage Security Rule:**
```javascript
service firebase.storage {
  match /b/{bucket}/o {
    match /funds/{fundId}/{allPaths=**} {
      allow read: if request.auth != null;
      allow write: if request.auth != null 
                   && request.resource.size < 5 * 1024 * 1024 // 5MB Limit
                   && request.resource.contentType.matches('image/.*|text/csv');
    }
  }
}
```

---

## 9. Architectural Sign-Off & Phase B Readiness

With the publication of this API Specification, the entire documentation suite for **Fund Circle** is 100% complete:

1. **Upstream Product Requirements (Archived):** [Investment_Firm_App_PRD_v2.md](file:///Users/kawsar/Desktop/projects/personal/Android/fund-circle/docs/archive/Investment_Firm_App_PRD_v2.md)
2. **Upstream Functional Spec (Archived):** [Investment_Firm_App_FSD_SRS_v1.md](file:///Users/kawsar/Desktop/projects/personal/Android/fund-circle/docs/archive/Investment_Firm_App_FSD_SRS_v1.md)
3. **Upstream User Stories (Archived):** [Investment_Firm_App_User_Stories_Use_Cases_v1.md](file:///Users/kawsar/Desktop/projects/personal/Android/fund-circle/docs/archive/Investment_Firm_App_User_Stories_Use_Cases_v1.md)
4. **User Flowcharts & State Machines (v1.1):** [Fund_Circle_User_Flowcharts_v1.0.md](file:///Users/kawsar/Desktop/projects/personal/Android/fund-circle/docs/Fund_Circle_User_Flowcharts_v1.0.md)
5. **Wireframes & Jetpack Compose UI Spec (v1.2):** [Fund_Circle_Wireframes_and_UI_Spec_v1.0.md](file:///Users/kawsar/Desktop/projects/personal/Android/fund-circle/docs/Fund_Circle_Wireframes_and_UI_Spec_v1.0.md)
6. **Design Tokens (Semantic Styling):** [Design_Tokens.md](file:///Users/kawsar/Desktop/projects/personal/Android/fund-circle/docs/Design_Tokens.md)
   *(Extended Brand Guide archived in [archive/Fund_Circle_Branding_and_Style_Guide_v1.0.md](file:///Users/kawsar/Desktop/projects/personal/Android/fund-circle/docs/archive/Fund_Circle_Branding_and_Style_Guide_v1.0.md))*
7. **System Architecture & Tech Stack (v1.0):** [Fund_Circle_System_Architecture_and_Tech_Stack_v1.0.md](file:///Users/kawsar/Desktop/projects/personal/Android/fund-circle/docs/Fund_Circle_System_Architecture_and_Tech_Stack_v1.0.md)
8. **API Specification & Data Contracts (v1.0):** [Fund_Circle_API_Specification_v1.0.md](file:///Users/kawsar/Desktop/projects/personal/Android/fund-circle/docs/Fund_Circle_API_Specification_v1.0.md)
9. **Interactive Design Prototype (v1.2):** [docs/design_preview/index.html](file:///Users/kawsar/Desktop/projects/personal/Android/fund-circle/docs/design_preview/index.html)
10. **Figma Interactive Prototype:** [Fund Circle — Mobile App (Android M3)](https://www.figma.com/design/44Gqq8dDY62FfKf28yxXuJ?node-id=42-3&autoplay-code-preview=42-3)
11. **Live FigJam Flowchart Board:** [Fund Circle — Master User Flow Architecture](https://www.figma.com/board/dxkTCpjsDBnEQp2zJRphAI)

The API specification provides the exact, production-ready blueprint required for the data layer implementation in Phase B.
