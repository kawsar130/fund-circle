# Fund Circle — Functional Specification / Software Requirements Specification (FSD/SRS)

> [!WARNING]
> **ARCHIVED / HISTORICAL SPECIFICATION**  
> This functional specification has been superseded by the active technical specifications in `docs/` (`Fund_Circle_System_Architecture_and_Tech_Stack_v1.0.md` and `Fund_Circle_API_Specification_v1.0.md`).  
> **Do not use this document as a technical implementation source of truth.** Refer to `AGENTS.md` and the active `Fund_Circle_*` documents.

**Document version:** 2.1 (Architectural Blueprint & Multi-Tenant Specification)  
**Status:** Requirements Specification based on approved PRD v3.0  
**Platform:** Android (Jetpack Compose, Material 3, API 26+)  
**Authentication:** Google One-Tap via Android Credential Manager API (`androidx.credentials`) and Firebase Auth  
**Database:** Cloud Firestore (Hierarchical Subcollection Architecture)  
**Source documents:** Product Requirements Document (PRD) v3.0; User Stories & Use Cases v2.0  

## 1. Purpose and boundaries

This specification defines the comprehensive, unambiguous functional behavior, data contracts, security model, and business rules for the **Fund Circle** Android application. It establishes a 100% turnkey blueprint for commercial publication on the Google Play Store.

The platform provides a **multi-tenant workspace architecture**: any authenticated user can create independent **Fund Circles** (as Admin) or participate as a **Stakeholder** across multiple circles. Each circle operates an isolated, immutable financial ledger.

### 1.1 Included capabilities

- **Google One-Tap Authentication:** Unified identity management using Android Credential Manager API (`androidx.credentials`) and Firebase Auth.
- **My Fund Circles Hub:** Central workspace switcher categorizing *Circles I Manage*, *Circles I Joined*, *Past / Archived Circles*, and a *Pending Invitations* gate with **Accept / Decline** actions.
- **Smart Launch Navigation:** Auto-navigates directly into the circle dashboard if the user has only 1 circle and 0 pending invites. The **"Switch Fund Circle"** option is integrated cleanly into the **Settings** screen.
- **Fund Circle Governance:** Creation with Circle Name (3–50 chars), Currency selection, and Description. Currency is permanently locked once the first transaction is recorded. Features Admin ownership transfer, empty circle deletion, and active ledger permanent archival.
- **Circle Limits:** Anti-spam limit permitting up to **5 active created circles** per Google account.
- **Isolated Stakeholder Profiles:** Admin registers members using Full Name, Phone Number, and Gmail. Stakeholders privately manage their NID and Nominee records inside each Circle's *My Profile*. Includes voluntary exit ("Leave Circle"), deactivation, and reactivation.
- **Sequential Member IDs:** Per-circle atomic generation starting at `FC-001` (Admin), `FC-002`, `FC-003`...
- **Financial Transactions:** Deposits and Expenses using exactly 5 fields; whole integer currency amounts; future dates blocked; 8 predefined expense categories.
- **Running Aggregates & O(1) Dashboard:** Atomic aggregate fields on the parent circle document (`totalDeposits`, `totalExpenses`, `recordedBalance`) updated via `FieldValue.increment()`, ensuring the Home dashboard loads with exactly 1 document read.
- **Correction Audit Trail:** Versioned, immutable history cards requiring change detection and a mandatory reason; supports 0-amount error neutralization.
- **Reports & Document Generation:** Four standardized report types with Month/Year and All-Time scoping; exported to formatted **PDF** and **Excel (.xlsx)** via Android `FileProvider`.

### 1.2 Excluded capabilities

- Hard deletion of any Fund Circle with active financial records ($\ge 1$).
- Live currency conversions or cross-circle asset aggregations.
- Compulsory deposit quotas, late fees, or contribution schedules.
- Receipt uploads, invoice file attachments, or payment reference codes.
- Push notifications or SMS alerts.
- Multi-signatory approval workflows.
- Ownership percentage or profit-sharing calculations.

## 2. Multi-tenant database architecture (Firestore)

To guarantee complete data isolation, scalability, ultra-fast dashboard queries, and airtight security, the platform utilizes a **Hierarchical Subcollection Model**:

```text
/users/{uid}                                    <-- Global User Account
    ├── email: String
    ├── displayName: String
    ├── photoUrl: String?
    ├── createdAt: Long
    └── createdCircleCount: Int                 (Enforces max 5 created circles)

/funds/{fundId}                                 <-- Fund Circle Document
    ├── name: String                            (3..50 chars)
    ├── currencyCode: String                    (e.g., "BDT", "USD", "EUR")
    ├── currencySymbol: String                  (e.g., "৳", "$", "€")
    ├── description: String?                    (Max 200 chars)
    ├── adminUid: String                        (Owner Firebase UID)
    ├── adminEmail: String
    ├── memberEmails: List<String>              (Array of all member emails for O(1) security rules)
    ├── totalDeposits: Long                     (Running aggregate, atomic increment)
    ├── totalExpenses: Long                     (Running aggregate, atomic increment)
    ├── recordedBalance: Long                   (Running aggregate: totalDeposits - totalExpenses)
    ├── createdAt: Long
    ├── isArchived: Boolean                     (Default false)
    ├── transactionCount: Int                   (Default 0; locks currency when >= 1)
    │
    ├── /counters/members                       <-- Atomic Counter Doc
    │       └── nextIndex: Int                  (Starts at 1 -> FC-001)
    │
    ├── /members/{memberId}                     <-- Member Subcollection (Doc ID: "FC-001", etc.)
    │       ├── memberId: String                (e.g., "FC-001")
    │       ├── uid: String?                    (Linked upon invitation acceptance)
    │       ├── fullName: String
    │       ├── phoneNumber: String
    │       ├── email: String                   (Indexed for Discovery)
    │       ├── nidNumber: String?              (Self-service)
    │       ├── nomineeName: String?            (Self-service)
    │       ├── nomineeRelationship: String?    (Self-service)
    │       ├── nomineeContact: String?         (Self-service)
    │       ├── joiningDate: Long
    │       ├── accountStatus: String           ("ACTIVE", "INACTIVE", "LEFT")
    │       └── inviteStatus: String            ("PENDING", "ACCEPTED", "DECLINED")
    │
    ├── /deposits/{transactionId}               <-- Deposits Subcollection
    │       ├── transactionId: String           (UUID)
    │       ├── memberId: String
    │       ├── stakeholderName: String
    │       ├── amount: Long                    (Whole integer)
    │       ├── depositDate: Long               (<= Current Date)
    │       ├── paymentMethod: String           (CASH, BANK_TRANSFER, MOBILE_BANKING)
    │       ├── note: String?
    │       ├── createdAt: Long
    │       ├── createdBy: String
    │       └── correctionHistory: List<Map>
    │
    └── /expenses/{transactionId}               <-- Expenses Subcollection
            ├── transactionId: String           (UUID)
            ├── expenseDate: Long               (<= Current Date)
            ├── amount: Long                    (Whole integer)
            ├── category: String                (One of 8 predefined categories)
            ├── description: String
            ├── paymentMethod: String
            ├── createdAt: Long
            ├── createdBy: String
            └── correctionHistory: List<Map>
```

### 2.1 Workspace discovery via collection group query
When a user signs into the app with `userEmail`, the app discovers all joined and invited circles with a single optimized query:
```kotlin
db.collectionGroup("members")
  .whereEqualTo("email", userEmail)
  .get()
```
For each matched member document, the parent `/funds/{fundId}` document is fetched to construct the user's Circle Hub.

### 2.2 Declarative Firestore Security Rules blueprint (`firestore.rules`)
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    
    // User global profiles
    match /users/{uid} {
      allow read, write: if request.auth != null && request.auth.uid == uid;
    }
    
    // Fund Circles
    match /funds/{fundId} {
      // Any authenticated user can create a circle (client/cloud functions enforce limit <= 5)
      allow create: if request.auth != null;
      
      // Read allowed if user is Admin OR their email is present in memberEmails array
      allow read: if request.auth != null && (
        resource.data.adminUid == request.auth.uid ||
        request.auth.token.email in resource.data.memberEmails
      );
      
      // Update/Archive/Delete allowed only by Circle Admin
      allow update, delete: if request.auth != null && resource.data.adminUid == request.auth.uid;
      
      // Member subcollection
      match /members/{memberId} {
        allow read: if request.auth != null && (
          get(/databases/$(database)/documents/funds/$(fundId)).data.adminUid == request.auth.uid ||
          request.auth.token.email in get(/databases/$(database)/documents/funds/$(fundId)).data.memberEmails
        );
        // Only Admin can add members; user can update their own self-service fields
        allow create, delete: if request.auth != null && 
          get(/databases/$(database)/documents/funds/$(fundId)).data.adminUid == request.auth.uid;
        allow update: if request.auth != null && (
          get(/databases/$(database)/documents/funds/$(fundId)).data.adminUid == request.auth.uid ||
          (resource.data.email == request.auth.token.email && 
           !request.resource.data.diff(resource.data).affectedKeys().hasAny(['memberId', 'accountStatus', 'joiningDate']))
        );
      }
      
      // Deposits subcollection
      match /deposits/{transactionId} {
        allow read: if request.auth != null && 
          request.auth.token.email in get(/databases/$(database)/documents/funds/$(fundId)).data.memberEmails;
        allow write: if request.auth != null && 
          get(/databases/$(database)/documents/funds/$(fundId)).data.adminUid == request.auth.uid &&
          get(/databases/$(database)/documents/funds/$(fundId)).data.isArchived == false;
      }
      
      // Expenses subcollection
      match /expenses/{transactionId} {
        allow read: if request.auth != null && 
          request.auth.token.email in get(/databases/$(database)/documents/funds/$(fundId)).data.memberEmails;
        allow write: if request.auth != null && 
          get(/databases/$(database)/documents/funds/$(fundId)).data.adminUid == request.auth.uid &&
          get(/databases/$(database)/documents/funds/$(fundId)).data.isArchived == false;
      }
      
      // Sequential Counter
      match /counters/{counterId} {
        allow read, write: if request.auth != null && 
          get(/databases/$(database)/documents/funds/$(fundId)).data.adminUid == request.auth.uid;
      }
    }
  }
}
```

### 2.3 Required Firestore Indexes (`firestore.indexes.json`)
```json
{
  "indexes": [
    {
      "collectionGroup": "deposits",
      "queryScope": "COLLECTION",
      "fields": [
        { "fieldPath": "depositDate", "order": "DESCENDING" },
        { "fieldPath": "createdAt", "order": "DESCENDING" }
      ]
    },
    {
      "collectionGroup": "expenses",
      "queryScope": "COLLECTION",
      "fields": [
        { "fieldPath": "expenseDate", "order": "DESCENDING" },
        { "fieldPath": "createdAt", "order": "DESCENDING" }
      ]
    }
  ],
  "fieldOverrides": [
    {
      "collectionGroup": "members",
      "fieldPath": "email",
      "indexes": [
        { "order": "ASCENDING", "queryScope": "COLLECTION_GROUP" }
      ]
    }
  ]
}
```

## 3. Roles and security model

Access control is strictly evaluated per Fund Circle based on the authenticated user's Firebase UID:

| Security Gate | Condition in Firestore Security Rules | Permissions |
|---|---|---|
| **Circle Admin** | `request.auth.uid == fundDoc.adminUid` | Full management: add/edit transactions, manage members, transfer ownership, archive circle. |
| **Active Stakeholder** | `request.auth.token.email in fundDoc.memberEmails && member.accountStatus == 'ACTIVE' && member.inviteStatus == 'ACCEPTED'` | Read all circle financials, totals, reports; edit own profile; leave circle. |
| **Inactive / Left Member** | `member.accountStatus in ['INACTIVE', 'LEFT']` | Read-only access to historical records, totals, and reports; profile editing blocked. |
| **Non-Member** | Email not in `fundDoc.memberEmails` | Zero read or write access; access denied. |

## 4. Functional requirements

### 4.1 Authentication and user registration (FR-AUTH)

- **FR-AUTH-01.** The application shall authenticate users using **Google One-Tap Sign-In** powered by the Android Credential Manager API (`androidx.credentials`) and Firebase Authentication.
- **FR-AUTH-02.** Upon successful Google authentication, the system shall ensure a root `/users/{uid}` document exists, creating it if it is the user's first sign-in.
- **FR-AUTH-03.** The user's active session shall persist across app restarts using Firebase Auth local caching.
- **FR-AUTH-04.** Tapping **Sign Out** in Settings shall clear cached credentials, terminate the session, and return the user to the Login screen.

### 4.2 Workspace hub and navigation (FR-HUB)

- **FR-HUB-01.** The Hub screen shall categorize all associated Fund Circles into four distinct sections:
  1. **Pending Invitations:** Circles where the user's email has `inviteStatus == 'PENDING'`, showing Circle Name, inviter email, and **[Accept]** / **[Decline]** buttons.
  2. **Circles I Manage:** Circles where `adminUid == currentUser.uid` and `isArchived == false`.
  3. **Circles I Joined:** Circles where the user is an `ACTIVE` stakeholder and `isArchived == false`.
  4. **Past / Archived Circles:** Circles marked `isArchived == true` or where the user has `accountStatus == 'LEFT'`.
- **FR-HUB-02.** Tapping **Accept** on an invitation shall set `inviteStatus = 'ACCEPTED'`, link `uid = currentUser.uid`, and move the circle into *Circles I Joined*.
- **FR-HUB-03.** Tapping **Decline** on an invitation shall set `inviteStatus = 'DECLINED'` and remove the card from the Hub.
- **FR-HUB-04 (Smart Launch).** On app launch:
  - If the user belongs to **exactly one circle** (and has 0 pending invitations), the app shall **auto-navigate directly into that Circle's Home dashboard**.
  - If the user belongs to multiple circles or has pending invitations, the app shall land on the **My Fund Circles Hub**.
- **FR-HUB-05 (Switch Circle).** The top app bar on the Home dashboard shall remain uncluttered. The **"Switch Fund Circle"** action shall reside inside **Settings**, allowing users to return to the Hub at any time.
- **FR-HUB-06 (Creation Cap).** An individual user shall be permitted to create a maximum of **5 active Fund Circles** (`createdCircleCount <= 5`). Tapping "+ Create Fund Circle" when at limit shall display an explanatory notice.

### 4.3 Fund Circle creation and governance (FR-CIRC)

- **FR-CIRC-01.** Creating a Fund Circle requires:
  - **Circle Name:** Required, 3–50 characters.
  - **Currency:** Required dropdown of top global currencies (BDT ৳, USD $, EUR €, GBP £, INR ₹, CAD $, AUD $, SGD $, AED د.إ, SAR ﷼, etc.) or "Other" with custom 3-letter code and symbol.
  - **Description / Purpose:** Optional text, maximum 200 characters.
- **FR-CIRC-02.** Upon creation, the creator is assigned `adminUid`, `memberEmails = [creatorEmail]`, `totalDeposits = 0`, `totalExpenses = 0`, `recordedBalance = 0`, and registered as Member ID `FC-001`.
- **FR-CIRC-03 (Currency Lock).** As soon as the first deposit or expense is saved (`transactionCount >= 1`), the currency fields (`currencyCode`, `currencySymbol`) shall become **permanently immutable**.
- **FR-CIRC-04.** The Admin shall be permitted to edit the Circle Name and Description at any time via Circle Settings.
- **FR-CIRC-05 (Admin Transfer).** The Admin shall be able to transfer the Admin role to any currently `ACTIVE` stakeholder. Upon confirmation, the target member's UID becomes `adminUid`, and the previous Admin transitions to a standard `ACTIVE` stakeholder.
- **FR-CIRC-06 (Deletion vs. Archival).**
  - If `transactionCount == 0`: Admin may permanently **Delete** the Fund Circle document and subcollections.
  - If `transactionCount >= 1`: Hard deletion is **blocked**. The Admin may only select **"Archive Fund Circle"**.
- **FR-CIRC-07 (Archival State).** An archived circle sets `isArchived = true`. All members retain permanent read-only access to view records and export reports; all transaction creation, member edits, and corrections are permanently disabled.

### 4.4 Stakeholder membership and profiles (FR-MEM)

- **FR-MEM-01.** Membership capacity is variable without an arbitrary cap.
- **FR-MEM-02.** Each circle maintains its own isolated sequential Member ID generator starting at `FC-001` (Admin).
- **FR-MEM-03 (Atomic Member Registration).** Adding a member is executed via a **Firestore Batched Write / Transaction**:
  1. Reads `/funds/{fundId}/counters/members`.
  2. Increments `nextIndex` atomically (`FC-002`, `FC-003`...).
  3. Writes `/funds/{fundId}/members/{memberId}` with Name, Phone, Gmail, and `inviteStatus = 'PENDING'`.
  4. Updates `/funds/{fundId}` document: `memberEmails = FieldValue.arrayUnion(newEmail)`.
- **FR-MEM-04 (Self-Service Profile).** When the stakeholder enters the circle, they can edit their own private fields via *My Profile*:
  - NID Number
  - Nominee Name, Relationship, and Contact
- **FR-MEM-05 (Privacy).** NID numbers and Nominee details shall be strictly hidden from all shared views and cannot be seen by other stakeholders or admins of other circles.
- **FR-MEM-06 (Voluntary Exit).** A stakeholder can tap **"Leave Circle"** in Settings. Their status transitions to `LEFT`, their past deposits remain permanently preserved in totals and reports, and the circle moves to *Past Circles* (read-only).
- **FR-MEM-07 (Admin Deactivation).** The Admin may deactivate any stakeholder. Status transitions to `INACTIVE`. The member retains read-only access to view historical statements and reports; deposit crediting is blocked.
- **FR-MEM-08 (Reactivation).** The Admin can reactivate an `INACTIVE` member at any time, restoring `ACTIVE` status.
- **FR-MEM-09.** Member lists shall display Member ID, Name, Account Status, and total deposited amount. **Deposit percentages are strictly omitted.**

### 4.5 Deposits (FR-DEP)

- **FR-DEP-01.** Only the Circle Admin shall record deposits.
- **FR-DEP-02.** A deposit requires **exactly five user fields**:
  1. **Stakeholder Name:** Must select an `ACTIVE` member of this circle.
  2. **Deposit Amount:** Whole positive integer in circle currency ($\ge 1$, zero decimals).
  3. **Deposit Date:** Past dates allowed; future dates blocked (`Date <= Today`).
  4. **Payment Method:** Exactly one of: `Cash`, `Bank Transfer`, `Mobile Banking`.
  5. **Note:** Optional free-text note (max 250 characters).
- **FR-DEP-03.** Stakeholders have no fixed deposit quotas or compulsory schedules.
- **FR-DEP-04 (Atomic Aggregate Update).** Saving a deposit executes a Firestore batch:
  1. Writes `/funds/{fundId}/deposits/{transactionId}`.
  2. Updates `/funds/{fundId}`:
     - `totalDeposits = FieldValue.increment(amount)`
     - `recordedBalance = FieldValue.increment(amount)`
     - `transactionCount = FieldValue.increment(1)`

### 4.6 Expenses (FR-EXP)

- **FR-EXP-01.** Only the Circle Admin shall record company expenses.
- **FR-EXP-02.** An expense requires **exactly five user fields**:
  1. **Expense Date:** Past dates allowed; future dates blocked (`Date <= Today`).
  2. **Expense Amount:** Whole positive integer in circle currency ($\ge 1$, zero decimals).
  3. **Expense Category:** Strictly one of the **8 predefined categories**:
     1. `Office & Administration`
     2. `Travel & Transportation`
     3. `Bank & Transaction Charges`
     4. `Legal & Documentation`
     5. `Meetings & Events`
     6. `Utilities & Communication`
     7. `Rent & Maintenance`
     8. `Others`
  4. **Description or Purpose:** Explanation of expense (3–250 characters).
  5. **Payment Method:** Exactly one of: `Cash`, `Bank Transfer`, `Mobile Banking`.
- **FR-EXP-03 (Atomic Aggregate Update).** Saving an expense executes a Firestore batch:
  1. Writes `/funds/{fundId}/expenses/{transactionId}`.
  2. Updates `/funds/{fundId}`:
     - `totalExpenses = FieldValue.increment(amount)`
     - `recordedBalance = FieldValue.increment(-amount)`
     - `transactionCount = FieldValue.increment(1)`

### 4.7 Dashboard and overall balance (FR-DASH)

- **FR-DASH-01.** The Home screen displays:
  - Total Circle Deposits (read directly from `/funds/{fundId}.totalDeposits`)
  - Total Circle Operational Expenses (read directly from `/funds/{fundId}.totalExpenses`)
  - Overall Recorded Balance (read directly from `/funds/{fundId}.recordedBalance`)
  - Recent Transactions Feed: Last **10 transactions** ordered by `date DESC, createdAt DESC`.
- **FR-DASH-02.** All circle members see identical real-time financial totals with **O(1) read complexity**.

### 4.8 Corrections and audit trail (FR-COR)

- **FR-COR-01.** Admin can correct any of the 5 fields of a posted deposit or expense.
- **FR-COR-02.** **Change Detection:** Submitting identical values without modifications is rejected.
- **FR-COR-03.** **Mandatory Reason:** A non-empty reason (5–250 chars) is strictly required.
- **FR-COR-04.** **Error Neutralization (0-Amount):** Erroneous entries can be corrected to amount **0** with an explanatory reason (e.g., *"Entered in error; voided"*), resetting their impact on balances to zero while preserving the audit trail.
- **FR-COR-05 (Atomic Correction Balance Update).** When a transaction amount changes from `oldAmount` to `newAmount`:
  - `amountDelta = newAmount - oldAmount`
  - If Deposit: `/funds/{fundId}.totalDeposits += amountDelta`, `/funds/{fundId}.recordedBalance += amountDelta`.
  - If Expense: `/funds/{fundId}.totalExpenses += amountDelta`, `/funds/{fundId}.recordedBalance -= amountDelta`.
- **FR-COR-06.** Every correction appends a permanent, timestamped version record in `correctionHistory` visible to all members. Historical version records cannot be edited or deleted.

### 4.9 Reports and document export (FR-REP)

- **FR-REP-01.** Reports engine provides period selection for **Selected Month + Year** and **All Time**.
- **FR-REP-02.** Four standardized report types:
  1. **Monthly Financial Summary:** Scoped strictly to Month + Year.
  2. **Member-wise Deposit Statement:** Filterable by **"All Members"** (roster overview) or **"Specific Member"** (individual statement with cumulative total).
  3. **Complete Deposit History:** Chronological ledger of all circle deposits (Month + Year or All-Time).
  4. **Complete Expense History:** Chronological ledger of all circle expenses (Month + Year or All-Time).
- **FR-REP-03.** Exportable to formatted **PDF** and **Excel (.xlsx)**.
- **FR-REP-04.** Export files are generated in secure app cache and dispatched via Android `FileProvider` (`Intent.ACTION_SEND`).
- **FR-REP-05.** File Naming Formula:  
  `FundCircle_{SanitizedCircleName}_{ReportType}_{Scope}.{ext}`  
  *(e.g., `FundCircle_ApexSyndicate_Monthly_Summary_2026_11.pdf`)*

## 5. Screen inventory

1. **Login:** Branded screen with Google One-Tap Sign-In button.
2. **My Fund Circles Hub:** Workspace switcher with *Pending Invitations*, *Circles I Manage*, *Circles I Joined*, *Past Circles*, and **[+ Create Circle]**.
3. **Create Fund Circle:** Modal form for Circle Name, Currency dropdown, and Description.
4. **Home:** Current circle totals, balance card, and top 10 recent transactions.
5. **Members:** Member roster with status badges and deposit totals.
6. **Transactions:** Segmented Deposits and Expenses tabs with search/filters.
7. **Reports:** Period selector, report type cards, preview, and PDF/Excel export triggers.
8. **Settings:** Circle metadata, My Profile, **"Switch Fund Circle"** (returns to Hub), Circle Management (Admin), and Sign Out.
9. **Member Details:** Public profile, cumulative deposits, and deposit ledger.
10. **Transaction Details & Correction History:** Current 5 fields and expandable version cards.
11. **Add / Correct Transaction (Admin):** 5-field entry and correction forms.
12. **Manage Members (Admin):** Add member, deactivate, reactivate, transfer ownership.
13. **Edit My Profile:** Member self-service form for NID and Nominee data.

## 6. Business rules matrix

| ID | Rule Description | Enforcement |
|---|---|---|
| **BR-01** | Multi-tenant circle isolation. | Hierarchical subcollections `/funds/{fundId}/...` and Firestore Security Rules. |
| **BR-02** | Max 5 active created circles per user. | Verified against `users/{uid}.createdCircleCount` before circle creation. |
| **BR-03** | Currency locked on first transaction. | Write blocked on currency fields if `transactionCount >= 1`. |
| **BR-04** | All currency amounts are whole positive integers. | Disallow decimals; enforce `amount >= 1` on creation. |
| **BR-05** | Future dates strictly rejected. | Validation rejects `transactionDate > currentTimeMillis()`. |
| **BR-06** | Only Admin records finances and corrections. | Security rules enforce `request.auth.uid == fundDoc.adminUid`. |
| **BR-07** | Corrections require change detection & mandatory reason. | Require `newValues != oldValues` and `reason.trim().isNotEmpty()`. |
| **BR-08** | Erroneous transactions neutralized to 0. | Amount can be set to 0 with mandatory reason; updates totals. |
| **BR-09** | Empty circle deletion vs. Active circle archival. | Deletion allowed only if `transactionCount == 0`; else only `isArchived = true`. |
| **BR-10** | Invitation gate for new members. | In-app Accept/Decline gate prevents involuntary enrollment. |
| **BR-11** | Switch Circle resides in Settings. | Top bar kept clean; "Switch Circle" option located in Settings screen. |
| **BR-12** | PDF/Excel exports follow naming formula. | Generator follows `FundCircle_{CircleName}_{ReportType}_{Scope}.{ext}`. |
| **BR-13** | O(1) Dashboard Aggregates. | `totalDeposits`, `totalExpenses`, `recordedBalance` maintained via atomic increment. |
| **BR-14** | Fast Security Rule Evaluation. | Parent document `memberEmails` array eliminates expensive rule subqueries. |

## 7. Traceability and acceptance tests

| Test ID | Verified Capability |
|---|---|
| **AT-01** | Google One-Tap authenticates user; creates `/users/{uid}` profile on Day 1. |
| **AT-02** | User creates Fund Circle with Name and Currency; Creator is assigned `FC-001`, `memberEmails = [creatorEmail]`, and Admin role. |
| **AT-03** | User attempts to create 6th Fund Circle; system blocks creation and displays 5-circle limit message. |
| **AT-04** | Admin registers member with Name, Phone, Gmail; stakeholder receives Pending Invitation on Hub and taps Accept. |
| **AT-05** | Stakeholder enters circle, updates private NID and Nominee; details are masked from other members. |
| **AT-06** | Admin enters first deposit; currency selection becomes permanently locked; `totalDeposits` and `recordedBalance` increment atomically. |
| **AT-07** | Admin enters expense with one of 8 categories; future date is rejected; `totalExpenses` increments and `recordedBalance` decreases atomically. |
| **AT-08** | Admin corrects transaction; unchanged submission is rejected; blank reason is rejected; 0-amount voiding resets financial impact; aggregates update atomically. |
| **AT-09** | Admin transfers Admin role to active member; previous Admin becomes standard stakeholder with zero data loss. |
| **AT-10** | Admin archives circle with active transactions; ledger transitions to permanent read-only state for all members. |
| **AT-11** | User with 1 circle launches app; app auto-opens directly to Circle Home with 1 document read; user navigates to Settings and taps "Switch Fund Circle" to return to Hub. |
| **AT-12** | User generates all 4 report types for Month/Year and All-Time; PDF and Excel export successfully with correct file naming. |
| **AT-13** | Security rule blocks non-member from reading `/funds/{fundId}` or subcollections. |
