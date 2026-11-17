# Fund Circle — User Stories & Use Cases

> [!WARNING]
> **ARCHIVED / HISTORICAL SPECIFICATION**  
> This requirements baseline has been superseded by the active technical specifications in `docs/` (`Fund_Circle_System_Architecture_and_Tech_Stack_v1.0.md` and `Fund_Circle_API_Specification_v1.0.md`).  
> **Do not use this document as a technical implementation source of truth.** Refer to `AGENTS.md` and the active `Fund_Circle_*` documents.

**Document version:** 2.0 (Multi-Tenant Requirements Baseline)  
**Source:** Approved Product Requirements Document (PRD) v3.0 and FSD/SRS v2.0  
**Platform:** Android (Jetpack Compose, Material 3, API 26+)  
**Status:** Requirements Baseline  

## 1. Purpose

This document translates the finalized PRD v3.0 and FSD/SRS v2.0 into actionable, testable user stories, acceptance criteria, and detailed use cases for the commercial release of **Fund Circle** on the Google Play Store.

## 2. Confirmed multi-tenant product rules

- **Multi-Tenant Workspaces:** Any user with a Google account can create up to 5 Fund Circles (as Admin) and participate as a Stakeholder in multiple invited circles.
- **Smart Launch & Clean Navigation:** If a user belongs to exactly 1 circle and has 0 pending invites, the app auto-navigates directly to that Circle's Home dashboard. To switch circles, the **"Switch Fund Circle"** option is cleanly located inside **Settings**. Multi-circle users land on the Hub.
- **Google One-Tap Sign-In:** Frictionless authentication via Android Credential Manager API (`androidx.credentials`) and Firebase Auth.
- **Circle Setup & Currency:** Creation requires Circle Name (3–50 chars), Currency selection (curated list + custom code/symbol), and optional Description. Currency is permanently locked as soon as the first transaction is recorded. Currency amounts enforce whole integers ($\ge 1$).
- **Invitation Gate:** Adding a member requires Name, Phone, and Gmail. The invited user receives a **Pending Invitation** card on their Hub with **[Accept]** and **[Decline]** buttons.
- **Circle-Specific Profiles:** NID and Nominee information are isolated per circle. Stakeholders privately enter their NID and Nominee details in *My Profile*. Admins of other circles have zero visibility into this data.
- **Sequential Member IDs:** Generated atomically per circle: `FC-001` (Admin/Creator), `FC-002`, `FC-003`...
- **Voluntary Exit & Inactivity:** Stakeholders can voluntarily tap **"Leave Circle"**; status transitions to `Inactive` (*"Left"*); past deposit records remain permanently intact in firm totals and reports. Admin can deactivate and reactivate members.
- **Single Admin with Ownership Transfer:** Strictly one Admin per circle at a time. The Admin can transfer ownership to any active stakeholder.
- **Deletion vs. Archival:** An empty circle (0 transactions) can be permanently deleted. An active circle ($\ge 1$ transactions) **cannot be deleted**; it can only be **Archived** into an immutable read-only state.
- **5-Field Financial Transactions:** Deposits and Expenses require exactly 5 fields. Future dates are strictly blocked (`Date <= Today`). Expenses strictly use the 8 predefined categories.
- **Zeroing Erroneous Records:** Accidental transactions can be neutralized by correcting the amount to **0** with a mandatory reason, resetting their balance impact while preserving full versioned transparency.
- **Reports & PDF/Excel Export:** Four report types supporting Month/Year and All-Time scoping; exported to formatted **PDF** and **Excel (.xlsx)** via Android `FileProvider` with standard naming formula `FundCircle_{CircleName}_{ReportType}_{Scope}.{ext}`.

---

## 3. User stories and acceptance criteria

### 3.1 Authentication and onboarding

**US-AUTH-01 — Sign in with Google One-Tap**  
*As an Android user,*  
*I want to sign in with one tap using my Google account,*  
*so that I can access my Fund Circles instantly without managing passwords or PINs.*

**Acceptance criteria:**
- Tapping the Google Sign-In button launches native Google One-Tap prompt via Android Credential Manager API (`androidx.credentials`).
- Successful sign-in verifies Google ID Token with Firebase Auth and creates/retrieves `/users/{uid}`.
- User session persists across app restarts.

---

### 3.2 Workspace hub and navigation

**US-HUB-01 — View My Fund Circles Hub**  
*As an authenticated user,*  
*I want to see all the Fund Circles I manage and belong to in one place,*  
*so that I can easily navigate between my different investment groups.*

**Acceptance criteria:**
- Hub categorizes circles into:
  1. *Pending Invitations* (with Accept and Decline buttons)
  2. *Circles I Manage* (where user is Admin)
  3. *Circles I Joined* (where user is Stakeholder)
  4. *Past / Archived Circles* (where user Left or Circle is Archived)
- Shows Circle Name, Currency Symbol, and role badge for each circle.

**US-HUB-02 — Create a new Fund Circle**  
*As a user,*  
*I want to create a new Fund Circle by naming it and choosing its currency,*  
*so that I can manage pooled capital with my partners.*

**Acceptance criteria:**
- Tapping "+ Create Fund Circle" opens creation form.
- Requires Circle Name (3–50 chars) and Currency (selected from curated list or custom 3-letter code and symbol). Description is optional.
- Creator is assigned the Admin role and automatically allocated Member ID `FC-001`.
- Enforces limit: a user can create a maximum of 5 active Fund Circles. If at limit, shows clear message.

**US-HUB-03 — Accept or decline an invitation**  
*As an invited stakeholder,*  
*I want to review and accept or decline a circle invitation,*  
*so that I am never enrolled in a Fund Circle without my consent.*

**Acceptance criteria:**
- Pending invitations card shows Circle Name and inviter email.
- Tapping **Accept** moves the circle to *Circles I Joined* with `ACTIVE` status.
- Tapping **Decline** dismisses the invitation with status `DECLINED`.

**US-HUB-04 — Smart launch navigation**  
*As a user belonging to only one Fund Circle,*  
*I want the app to open directly into my circle's dashboard,*  
*so that I don't have to tap through the Hub every time I open the app.*

**Acceptance criteria:**
- If user has exactly 1 circle and 0 pending invites, app opens directly to that circle's Home dashboard.
- If user has multiple circles or pending invites, app lands on the Hub.

**US-HUB-05 — Switch circle from Settings**  
*As a user inside a Fund Circle,*  
*I want to switch to another circle from Settings without cluttering the top app bar,*  
*so that the main dashboard remains clean and focused.*

**Acceptance criteria:**
- Top app bar on the Home dashboard displays only Circle Name, status badge, and profile/settings avatar.
- Navigating to **Settings** displays a prominent **"Switch Fund Circle"** option.
- Tapping "Switch Fund Circle" navigates back to the *My Fund Circles Hub*.

---

### 3.3 Circle administration and governance

**US-CIRC-01 — Transfer Admin ownership**  
*As a Circle Admin,*  
*I want to transfer ownership of the Fund Circle to another active member,*  
*so that leadership can transition smoothly when I step down.*

**Acceptance criteria:**
- Admin can open Circle Settings and tap "Transfer Admin Role".
- Prompts selection from list of currently `ACTIVE` stakeholders.
- Confirmation dialog requires explicit approval.
- Upon transfer, target member becomes Admin; previous Admin becomes an `ACTIVE` stakeholder with zero data loss.

**US-CIRC-02 — Delete an empty Fund Circle**  
*As a Circle Admin,*  
*I want to permanently delete a circle if it was created by mistake and has zero transactions,*  
*so that empty test circles don't clutter my account.*

**Acceptance criteria:**
- If `transactionCount == 0`, Admin sees "Delete Fund Circle" button in Circle Settings.
- Confirming permanently deletes the circle document and subcollections and decrements `createdCircleCount`.

**US-CIRC-03 — Archive an active Fund Circle**  
*As a Circle Admin,*  
*I want to archive an active Fund Circle once its financial lifecycle ends,*  
*so that the ledger is permanently locked while preserving all historical records.*

**Acceptance criteria:**
- If `transactionCount >= 1`, hard deletion is disabled; Admin sees "Archive Fund Circle".
- Archiving sets `isArchived = true`.
- Freezes circle: no new transactions or edits can be made.
- All members retain permanent read-only access to view ledgers and export final reports.

---

### 3.4 Stakeholder membership and privacy

**US-MEM-01 — Invite a stakeholder**  
*As a Circle Admin,*  
*I want to add a member using their Name, Phone, and Gmail address,*  
*so that they receive an invitation and Member ID without me needing their private NID or nominee details.*

**Acceptance criteria:**
- Admin enters Full Name, Phone (11 digits), and Gmail address.
- System atomically assigns the next sequential Member ID (`FC-002`, `FC-003`...).
- Creates member subcollection entry with `inviteStatus = 'PENDING'`.

**US-MEM-02 — Maintain private NID and nominee details**  
*As an active stakeholder,*  
*I want to privately enter and update my NID and Nominee details for this circle,*  
*so that my beneficiary data is registered without exposing it to other members.*

**Acceptance criteria:**
- In circle's *My Profile*, stakeholder can update NID Number, Nominee Name, Relationship, and Contact.
- Private NID and nominee fields are strictly hidden from shared member roster views.
- Data is isolated to this specific circle; other circles have no access to it.

**US-MEM-03 — Voluntarily leave a Fund Circle**  
*As a stakeholder,*  
*I want to leave a Fund Circle when my participation ends,*  
*so that the circle is removed from my active view without erasing my past contributions.*

**Acceptance criteria:**
- Stakeholder can tap "Leave Circle" in Settings / My Profile.
- Status transitions to `LEFT`.
- All past deposits remain permanently intact in firm totals, balances, and reports.
- Circle moves to *Past / Archived Circles* on the user's Hub in read-only mode.

**US-MEM-04 — Deactivate and reactivate a stakeholder**  
*As a Circle Admin,*  
*I want to deactivate and reactivate members as needed,*  
*so that membership status is accurate while preserving historical contributions.*

**Acceptance criteria:**
- Admin can toggle an active member to `INACTIVE`. Member receives Inactive badge, cannot receive new deposits, and retains read-only access.
- Admin can toggle an inactive member back to `ACTIVE`, restoring full active capabilities.

---

### 3.5 Deposits and expenses

**US-TXN-01 — Record a deposit**  
*As a Circle Admin,*  
*I want to record a stakeholder deposit using the five agreed fields,*  
*so that member contributions and circle balances stay up to date.*

**Acceptance criteria:**
- Form requires 5 fields: Stakeholder Name (Active only), Deposit Amount (whole integer $\ge 1$), Deposit Date (`<= Today`), Payment Method (Cash, Bank Transfer, Mobile Banking), and optional Note (max 250 chars).
- Future dates are blocked.
- Saving immediately locks currency (if first transaction) and updates member total, circle total deposits, and recorded balance.

**US-TXN-02 — Record an operational expense**  
*As a Circle Admin,*  
*I want to record a company expense using the 8 predefined categories,*  
*so that operational expenditures are transparently tracked.*

**Acceptance criteria:**
- Form requires 5 fields: Expense Date (`<= Today`), Expense Amount (whole integer $\ge 1$), Category (from 8 predefined categories), Description (3–250 chars), and Payment Method.
- Saving immediately locks currency (if first transaction), increases total expenses, and reduces recorded balance.

**US-TXN-03 — Correct a transaction with mandatory reason**  
*As a Circle Admin,*  
*I want to correct errors in a posted transaction while providing a mandatory reason,*  
*so that mistakes can be fixed transparently without hiding historical changes.*

**Acceptance criteria:**
- Admin can edit any of the 5 fields.
- Submitting identical values without modifications is rejected (change detection).
- Requires a non-empty reason (5–250 chars).
- Appends an immutable, timestamped version card to `correctionHistory` visible to all members.

**US-TXN-04 — Neutralize an accidental transaction to 0**  
*As a Circle Admin,*  
*I want to correct an erroneously posted transaction amount to 0,*  
*so that accidental entries are voided without violating the no-deletion rule.*

**Acceptance criteria:**
- Admin can correct amount to `0` with a mandatory reason (e.g., *"Entered in error; voided"*).
- The transaction's contribution to totals and balance resets to zero.
- Transaction remains visible in audit trails with its voided status.

---

### 3.6 Reports and document export

**US-REP-01 — View and export financial reports**  
*As a circle member,*  
*I want to view financial reports for a selected month/year or all-time and export them to PDF and Excel,*  
*so that I have formal, verifiable records of the fund's transactions.*

**Acceptance criteria:**
- Reports screen supports 4 report types:
  1. *Monthly Financial Summary* (Month + Year)
  2. *Member-wise Deposit Statement* (All Members roster or Specific Member)
  3. *Complete Deposit History* (Month + Year or All Time)
  4. *Complete Expense History* (Month + Year or All Time)
- Exportable to formatted PDF and Excel (`.xlsx`).
- Dispatched via Android `FileProvider` (`Intent.ACTION_SEND`).
- Filenames follow formula: `FundCircle_{CircleName}_{ReportType}_{Scope}.{ext}`.

---

## 4. Use cases

### UC-00 — Google One-Tap sign-in and hub discovery
**Actor:** Any user  
**Precondition:** App installed on Android device.  
**Main flow:**
1. User opens the app.
2. System displays Google One-Tap Sign-In prompt.
3. User selects their Google account.
4. System verifies ID token with Firebase Auth and discovers user's circles via collection group query.
5. If user has exactly 1 circle and 0 pending invites, navigates directly to Circle Home.
6. Otherwise, navigates to My Fund Circles Hub.

### UC-01 — Create a Fund Circle
**Actor:** Authenticated user  
**Precondition:** User is on the Hub and has created $< 5$ active circles.  
**Main flow:**
1. User taps "+ Create Fund Circle".
2. User enters Circle Name (e.g., "Apex Syndicate") and selects Currency (e.g., USD $).
3. User taps Create.
4. System creates `/funds/{fundId}`, assigns user as Admin, and registers user as Member `FC-001`.
5. Navigates directly into the new Circle Home dashboard.

### UC-02 — Switch Fund Circle via Settings
**Actor:** Member of multiple circles  
**Precondition:** User is inside a Fund Circle.  
**Main flow:**
1. User navigates to **Settings** tab.
2. User taps **"Switch Fund Circle"**.
3. App navigates back to My Fund Circles Hub.
4. User selects a different circle.

### UC-03 — Invite and onboard a stakeholder
**Actor:** Circle Admin  
**Precondition:** Admin is inside the circle.  
**Main flow:**
1. Admin navigates to Members $\rightarrow$ Manage Members $\rightarrow$ Add Stakeholder.
2. Admin enters Full Name, Phone, and Gmail address.
3. Admin taps Save.
4. System atomically generates next Member ID (`FC-002`) and saves member document with `inviteStatus = 'PENDING'`.
5. When the invitee opens the app, their Hub displays the Pending Invitation card.
6. Invitee taps **Accept**. The circle is added to their *Circles I Joined* list.

### UC-04 — Fill private NID and nominee details
**Actor:** Active Stakeholder  
**Precondition:** Stakeholder is inside the circle.  
**Main flow:**
1. Stakeholder navigates to Settings $\rightarrow$ My Profile $\rightarrow$ Edit Profile.
2. Stakeholder enters NID number, Nominee Name, Relationship, and Contact.
3. Stakeholder taps Save. Profile updates privately for this circle only.

### UC-05 — Voluntary leave from Fund Circle
**Actor:** Stakeholder  
**Precondition:** Stakeholder wants to leave circle.  
**Main flow:**
1. Stakeholder navigates to Settings $\rightarrow$ Leave Circle.
2. App displays warning: past contributions remain permanently on record for firm transparency.
3. Stakeholder confirms. Status becomes `LEFT`.
4. Circle moves to read-only *Past Circles* section on user's Hub.

### UC-06 — Record a deposit
**Actor:** Circle Admin  
**Precondition:** At least one Active stakeholder exists.  
**Main flow:**
1. Admin taps Add Transaction $\rightarrow$ Deposit.
2. Admin selects Stakeholder, enters Amount (whole integer), Date (`<= Today`), Payment Method, and optional Note.
3. Admin taps Save Deposit.
4. Currency locks (if first transaction); member total and circle balance update immediately.

### UC-07 — Record an operational expense
**Actor:** Circle Admin  
**Precondition:** Admin is authenticated in circle.  
**Main flow:**
1. Admin taps Add Transaction $\rightarrow$ Expense.
2. Admin enters Date (`<= Today`), Amount (whole integer), Category (from 8 options), Description, and Payment Method.
3. Admin taps Save Expense. Total expenses increment and recorded balance decreases.

### UC-08 — Correct transaction with mandatory reason
**Actor:** Circle Admin  
**Precondition:** Posted transaction exists.  
**Main flow:**
1. Admin opens transaction $\rightarrow$ Correct Transaction.
2. Admin modifies one or more fields (or sets amount to 0 if voiding an error).
3. Admin enters mandatory reason text.
4. Admin taps Save Correction.
5. System validates change detection and non-empty reason.
6. Updated values display immediately; immutable version card appends to correction history.

### UC-09 — Export financial report to PDF/Excel
**Actor:** Admin or Stakeholder  
**Precondition:** User is inside circle.  
**Main flow:**
1. User navigates to Reports tab.
2. User selects reporting period (Month/Year or All Time) and report type.
3. App displays on-screen preview.
4. User taps "Export as PDF" or "Export as Excel".
5. App generates file in app cache with standard filename and launches Android Share Sheet.

---

## 5. Traceability matrix

| Requirement Area | User Stories | Use Cases |
|---|---|---|
| Authentication & Onboarding | US-AUTH-01 | UC-00 |
| Hub, Navigation & Settings Switch | US-HUB-01, US-HUB-04, US-HUB-05 | UC-00, UC-02 |
| Circle Setup, Currency & Limits | US-HUB-02 | UC-01 |
| Invitation Gate ([Accept]/[Decline]) | US-HUB-03 | UC-03 |
| Circle Governance (Transfer / Archive / Delete) | US-CIRC-01, US-CIRC-02, US-CIRC-03 | Circle Settings |
| Member Profiles & Privacy | US-MEM-01, US-MEM-02 | UC-03, UC-04 |
| Inactivity & Voluntary Exit | US-MEM-03, US-MEM-04 | UC-05 |
| Deposits & Expenses (5 Fields, 8 Categories) | US-TXN-01, US-TXN-02 | UC-06, UC-07 |
| Corrections & 0-Amount Neutralization | US-TXN-03, US-TXN-04 | UC-08 |
| Reports & PDF/Excel Document Generation | US-REP-01 | UC-09 |
