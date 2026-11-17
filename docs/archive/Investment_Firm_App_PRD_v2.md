# Product Requirements Document (PRD)

> [!WARNING]
> **ARCHIVED / HISTORICAL SPECIFICATION**  
> This document represents upstream product requirements and ideation. It has been superseded by the active technical specifications in `docs/` (`Fund_Circle_System_Architecture_and_Tech_Stack_v1.0.md` and `Fund_Circle_API_Specification_v1.0.md`).  
> **Do not use this document as a technical implementation source of truth.** Refer to `AGENTS.md` and the active `Fund_Circle_*` documents.

**Product:** Fund Circle (Multi-Tenant Investment & Pooled Capital Platform)  
**Version:** 3.0 (Commercial Play Store Multi-Tenant Release)  
**Platform:** Android (Google Play Store, Jetpack Compose, Material 3)  
**Status:** Approved & Finalized  

## 1. Product overview

**Fund Circle** is a modern, transparent mobile application published on the Google Play Store for investment syndicates, angel groups, property syndicates, partnership firms, and family pooled funds. It empowers any group of partners to record capital deposits, track operational expenditures, maintain transparent financial ledgers, and compute real-time recorded balances with an immutable, versioned audit trail.

The application operates as a **multi-tenant workspace platform**: any Android user can sign in with Google One-Tap, create their own **Fund Circles** (as Admin), and participate as a **Stakeholder** across multiple Fund Circles where their Gmail address is invited.

## 2. Product goals

- Provide a commercial-grade, multi-tenant mobile platform published on the Google Play Store.
- Support seamless, one-tap authentication via Google Sign-In with zero passwords or PINs to manage.
- Enable any user to create and administer independent Fund Circles with customized global currencies.
- Provide strict data isolation and privacy: financial ledgers, NID numbers, and nominee details never leak across circles.
- Maintain transparent financial records where all stakeholders see identical real-time balances, recent transactions, and complete deposit/expense ledgers.
- Guarantee financial record integrity through an immutable, versioned correction history and error-neutralization workflows.
- Provide professional financial reporting with on-device **PDF** and **Excel (.xlsx)** document generation.

## 3. Scope

### In scope

- **Multi-Tenant Hub ("My Fund Circles"):** Hub screen displaying circles the user manages (as Admin), circles they have joined (as Stakeholder), and pending invitations.
- **Smart Launch Navigation:** Auto-navigates directly to the dashboard if the user belongs to only 1 Circle; multi-circle users land on the Hub. A **"Switch Fund Circle"** option is integrated cleanly into the **Settings** screen.
- **Google One-Tap Authentication:** Powered by Android Credential Manager API (`androidx.credentials`), Google ID Token, and Firebase Auth.
- **Fund Circle Setup & Currency:** Creation requires Circle Name, Currency (curated dropdown of global currencies + custom option), and optional Description. Currency is permanently locked once the first transaction is posted. Currency amounts enforce whole integers.
- **Circle Limits:** Anti-spam safeguard permitting up to **5 active created circles** per Google account.
- **Stakeholder Lifecycle & Privacy:** Admin adds members via Name, Phone, and Gmail. Stakeholders privately enter their NID and Nominee details per circle. Includes invitation acceptance gate, voluntary exit ("Leave Circle"), deactivation, and reactivation.
- **Deposits:** Admin entry using exactly five fields; whole integer amounts; past dates allowed; future dates blocked.
- **Expenses:** Admin entry using exactly five fields and 8 predefined operational categories; past dates allowed; future dates blocked.
- **Dashboard & Balance:** Overall Recorded Balance dynamically computed as $\text{Total Deposits} - \text{Total Expenses}$ using latest corrected values.
- **Correction Audit Trail:** Admin corrections require change detection and a mandatory reason; allows 0-amount error neutralization; permanent version history visible to all circle members.
- **Admin Governance:** Single Admin per circle; Admin ownership transfer to active stakeholders; empty circle deletion vs. active circle permanent archival.
- **Reports & Export:** Four standardized report types with Month/Year and All-Time scoping; exportable as **PDF** and **Excel (.xlsx)**.

### Out of scope

- Hard deletion of any Fund Circle with active financial transactions ($\ge 1$).
- Automatic multi-currency conversion or live exchange rate feeds.
- Compulsory contribution quotas, penalty fees, or mandatory schedules.
- Receipt image uploads, invoice attachments, or payment reference codes.
- Push notifications or SMS transaction alerts.
- Multi-signatory or multi-step approval workflows.
- Profit-sharing, dividend distribution, or asset valuation calculations.
- Direct payment gateway integrations (the app records transactions; it does not process bank transfers).

## 4. Users and permissions

Users interact with the app in two distinct scopes: **Platform Level** (Global User) and **Circle Level** (Admin vs. Stakeholder).

### Platform level
Any user with a Google account can sign into the app, view their joined circles, accept/decline circle invitations, and create up to 5 Fund Circles.

### Circle-level role matrix

| Action | Circle Admin | Active Stakeholder | Inactive / Left Stakeholder |
|---|:---:|:---:|:---:|
| View Dashboard & Recorded Balance | Yes | Yes | Yes (Read-Only) |
| View Member Roster & Deposit Totals | Yes | Yes | Yes (Read-Only) |
| View Posted Deposits and Expenses | Yes | Yes | Yes (Read-Only) |
| View Versioned Correction History | Yes | Yes | Yes (Read-Only) |
| View & Export Reports (PDF / Excel) | Yes | Yes | Yes (Read-Only) |
| Record Deposits and Expenses | Yes | No | No |
| Correct Posted Transactions | Yes | No | No |
| Invite / Add Stakeholders | Yes | No | No |
| Deactivate / Reactivate Stakeholders | Yes | No | No |
| Transfer Circle Ownership (Admin Role) | Yes | No | No |
| Archive / Delete Fund Circle | Yes | No | No |
| Edit Own Personal & Nominee Details | Yes | Yes | No |
| Leave Fund Circle Voluntarily | No (Must transfer first) | Yes | No |
| Receive Credited Deposits | Yes (if member) | Yes | No |

## 5. Fund Circle lifecycle and governance

### 5.1 Creation and setup
1. An authenticated user taps **"+ Create Fund Circle"** on the Hub.
2. The user enters:
   - **Circle Name:** Required, 3–50 characters.
   - **Currency:** Required dropdown (BDT ৳, USD $, EUR €, GBP £, INR ₹, CAD $, AUD $, SGD $, AED د.إ, SAR ﷼, etc.) or a custom 3-letter code and symbol.
   - **Description / Purpose:** Optional, max 200 characters.
3. The user is assigned the **Admin** role for this circle and automatically assigned Member ID `FC-001`.
4. **Currency Lock:** The currency is permanently locked as soon as the first deposit or expense is recorded in that circle.

### 5.2 Admin ownership transfer
- The Admin may transfer the Admin role to any currently `Active` stakeholder in the circle.
- Upon transfer, the previous Admin becomes a standard `Active` stakeholder. The circle retains full continuity with zero data loss.

### 5.3 Deletion vs. archival
- **Zero Transactions (Empty Circle):** The Admin may permanently delete the Fund Circle.
- **$\ge 1$ Transactions (Active Ledger):** Hard deletion is strictly blocked. To close the fund, the Admin selects **"Archive Fund Circle"**. Archiving permanently locks the ledger into an immutable read-only state: no new transactions or corrections can be entered, but all members retain permanent access to view records and export final reports.

## 6. Stakeholder membership and privacy

### 6.1 Invitation and onboarding
1. **Admin Invite:** The Admin enters the member's **Full Name**, **Phone Number**, and **Gmail Address**. The system auto-assigns the next sequential Member ID (`FC-002`, `FC-003`...).
2. **Pending Invitation:** When the stakeholder opens the app with that Gmail account, their Hub displays a **Pending Invitation** card:
   > *"Apex Syndicate (Invited by admin@gmail.com) — [Accept] [Decline]"*
3. **Acceptance:** Tapping **Accept** adds the circle to their *"Circles I Joined"* list. Declining dismisses the invitation.

### 6.2 Circle-specific privacy
- Each Fund Circle maintains an isolated profile record for the member.
- When Rahim opens the circle, he navigates to *My Profile* to privately enter his **NID** and **Nominee details** (Nominee Name, Relationship, and Contact).
- Other stakeholders cannot view Rahim's NID or nominee data. Admins of other unrelated circles have zero visibility into Rahim's data in this circle.

### 6.3 Inactivity, voluntary exit, and reactivation
- **Deactivation by Admin:** Admin can deactivate a member. Status becomes `Inactive`. Member cannot receive new deposits and has read-only access. Admin can reactivate them at any time.
- **Voluntary Exit ("Leave Circle"):** A stakeholder can tap **"Leave Circle"** in Settings. Status transitions to `Inactive` (badged as *"Left"*). All historical deposit records remain permanently intact in firm totals and reports. The circle moves to an *"Archived / Past Circles"* read-only section on the user's Hub.

## 7. Financial transaction rules

### 7.1 Deposits (exactly five fields)
1. **Stakeholder Name:** Must select an `Active` member of the circle.
2. **Deposit Amount:** Whole positive integer in circle currency ($\ge 1$, zero decimals).
3. **Deposit Date:** Past dates allowed; future dates rejected (`Date <= Today`).
4. **Payment Method:** One of: `Cash`, `Bank Transfer`, `Mobile Banking`.
5. **Note:** Optional free-text note (max 250 characters).

### 7.2 Expenses (exactly five fields)
1. **Expense Date:** Past dates allowed; future dates rejected (`Date <= Today`).
2. **Expense Amount:** Whole positive integer in circle currency ($\ge 1$, zero decimals).
3. **Expense Category:** Strictly one of the **8 predefined categories**:
   1. *Office & Administration*
   2. *Travel & Transportation*
   3. *Bank & Transaction Charges*
   4. *Legal & Documentation*
   5. *Meetings & Events*
   6. *Utilities & Communication*
   7. *Rent & Maintenance*
   8. *Others*
4. **Description or Purpose:** Explanation of expenditure (3–250 characters).
5. **Payment Method:** One of: `Cash`, `Bank Transfer`, `Mobile Banking`.

### 7.3 Dashboard and balance
- **Formula:** $\text{Overall Recorded Balance} = \text{Total Deposits} - \text{Total Expenses}$.
- **Feed:** Displays the **last 10 transactions** ordered by `date DESC, createdAt DESC`.

### 7.4 Corrections and audit trail
- Admin can correct any of the 5 transaction fields.
- **Validation:** Requires at least one modified field (change detection) and a mandatory non-empty reason (5–250 chars).
- **Error Neutralization:** Accidental entries can be corrected to amount **0** with an explanatory reason (e.g., *"Entered in error; voided"*), neutralizing its financial impact while preserving the audit trail.
- **Version History:** Every edit permanently appends a timestamped, immutable version card showing prior values, new values, Admin actor, and reason.

## 8. Reports and document export

The Reports engine generates on-screen previews and exports to formatted **PDF** and **Excel (.xlsx)**.

| Report Type | Period Scope | Filter Options | Format |
|---|---|---|:---:|
| **Monthly Financial Summary** | Selected Month + Year | Firm-wide | PDF & Excel |
| **Member-wise Deposit Statement** | Month + Year OR All Time | All Members (Roster) OR Specific Member (Individual) | PDF & Excel |
| **Complete Deposit History** | Month + Year OR All Time | All circle deposits | PDF & Excel |
| **Complete Expense History** | Month + Year OR All Time | All circle expenses | PDF & Excel |

- **Storage & Sharing:** Generated in app cache and shared via Android `FileProvider` (`Intent.ACTION_SEND`).
- **File Naming Pattern:** `FundCircle_{SanitizedCircleName}_{ReportType}_{Scope}.{ext}`  
  *(e.g., `FundCircle_ApexSyndicate_Monthly_Summary_2026_11.pdf`)*

## 9. Application screens and navigation

### Platform screen
- **My Fund Circles Hub:** Displays *Pending Invitations*, *Circles I Manage* (Admin), *Circles I Joined* (Stakeholder), *Past / Archived Circles*, and **[+ Create Fund Circle]** button.

### Circle-level main screens (bottom navigation)
1. **Home:** Current circle totals, balance card, recent transactions feed.
2. **Members:** Searchable member roster with status badges and cumulative deposit totals.
3. **Transactions:** Segmented Deposits and Expenses tabs with search and filters.
4. **Reports:** Period picker, report type selector, preview, and PDF/Excel export triggers.
5. **Settings / Profile:** Circle metadata, My Profile, **"Switch Fund Circle"** (navigates back to Hub), Circle Management (Admin), and Sign Out.

### Supporting screens
- **Login:** Google One-Tap Sign-In screen.
- **Create Fund Circle:** Modal form for Circle Name, Currency, and Description.
- **Member Details:** Member deposit history and transaction count.
- **Transaction Details & Correction History:** 5 fields and expandable version history cards.
- **Add / Correct Transaction (Admin):** Tabbed entry modal and correction form.
- **Manage Members (Admin):** Add member, deactivate, reactivate, transfer ownership.
- **Edit My Profile:** Member self-service form for NID and Nominee details.

## 10. Technical specifications

- **Platform:** Native Android 8.0+ (API 26+), 100% Jetpack Compose, Material 3, Edge-to-Edge.
- **Architecture:** Clean Architecture with MVI/MVVM, Kotlin Coroutines, and StateFlow.
- **Authentication:** Android Credential Manager API (`androidx.credentials`) + Firebase Auth.
- **Database & Scaling:** Cloud Firestore using a **Hierarchical Subcollection Architecture** (`/funds/{fundId}/...`) with multi-tenant declarative security rules, offline caching, and real-time running aggregates (`totalDeposits`, `totalExpenses`, `recordedBalance`) maintained via atomic increments for instant $O(1)$ dashboard loading.
- **Document Generation:** Native Android PDF engine (`android.graphics.pdf.PdfDocument`) and Apache POI / light spreadsheet library for Excel (`.xlsx`).
