# Fund Circle — User Flowcharts & Interaction Architecture

**Document version:** 1.1 (Commercial Play Store UX Blueprint with Live FigJam Integration)  
**Platform:** Android 8.0+ (Min SDK 26, Target SDK 35, Compile SDK 35), Jetpack Compose, Material 3  
**Source documents (Archived):** PRD v3.0, FSD/SRS v2.1, User Stories & Use Cases v2.0 in `docs/archive/`  
**Status:** Approved Engineering Blueprint  
**Live FigJam & Figma Assets:**
- **Master Flowchart (FigJam):** [Fund Circle — Master User Flow Architecture](https://www.figma.com/board/dxkTCpjsDBnEQp2zJRphAI)
- **Figma Interactive Prototype:** [Fund Circle — Mobile App (Android M3)](https://www.figma.com/design/44Gqq8dDY62FfKf28yxXuJ?node-id=42-3&autoplay-code-preview=42-3)
- **Visual Screen Gallery (2x Retina):** [docs/design_preview/gallery.html](file:///Users/kawsar/Desktop/projects/personal/Android/fund-circle/docs/design_preview/gallery.html)
- **Local Design Prototype:** [docs/design_preview/index.html](file:///Users/kawsar/Desktop/projects/personal/Android/fund-circle/docs/design_preview/index.html)  

---

## 1. Executive overview & flowchart grammar

This document provides complete, unambiguous visual flowcharts mapping every screen, user decision, validation branch, cloud transaction, and system side-effect across **Fund Circle**. 

It serves as the definitive reference for Android UI/UX engineers, QA test automation, and backend developers before writing code or assembling Jetpack Compose layouts.

### Standardized flowchart grammar

```
([Stadium / Capsule])       ──▶ Entry point, terminal exit, or external event
[Standard Rectangle]        ──▶ UI Screen, layout state, or user action
{"{Decision Diamond}"}      ──▶ User decision gate or system validation branch
[(Cylinder)]                ──▶ Cloud Firestore database read/write operation
[[Subroutine Box]]          ──▶ Android OS handoff (Credential Manager, FileProvider, Share Sheet)
```

### Visual theme & status conventions
*   🟢 **Green paths:** Primary happy paths, confirmations, successful transactions.
*   🔴 **Red paths:** Validation rejections, error loops, security blocks, declines.
*   🔵 **Blue nodes:** Mobile UI screens, bottom sheets, dialogs.
*   🟣 **Purple nodes:** Cloud transactions, batched writes, atomic counters.

---

## 2. Core User Journeys (CUJs)

```
                                      ┌────────────────────────────────┐
                                      │   Fund Circle User Flowcharts  │
                                      └───────────────┬────────────────┘
                                                      │
         ┌───────────────────┬────────────────────────┼───────────────────────┬───────────────────┐
         │                   │                        │                       │                   │
   [CUJ-01: Auth]     [CUJ-02: Create Circle]  [CUJ-03/04: Members]   [CUJ-05/06: Ledgers] [CUJ-07/08: Reports & Ops]
   - Google One-Tap   - Currency Lock          - Invite & Onboard     - Deposit & Expense  - On-Device CSV Export
   - Smart Launch     - 5-Circle Limit         - Private Profile      - Reversal Protocol  - Settings & Governance
   - Settings Switch  - Atomic Setup           - Voluntary Exit       - Audit Cards        - Archival & Transfer
```

---

### CUJ-01: Authentication, Workspace Discovery & Smart Launch

Maps user entry into the app, Google One-Tap verification, Firestore collection group discovery, and smart routing logic.

```mermaid
flowchart TD
    Start([Launch Fund Circle App]) --> CheckAuth{Active Firebase\nSession?}

    %% Cold Login Flow
    CheckAuth -- No --> LoginScreen[Login Screen\nBranded Hero + Google Button]
    LoginScreen --> TapGoogle[User taps 'Sign In with Google']
    TapGoogle --> CredManager[[Android Credential Manager API\nandroidx.credentials]]
    CredManager --> AuthPrompt{User selects\nGoogle Account?}
    AuthPrompt -- Cancelled / Failed --> LoginScreen
    AuthPrompt -- Selected --> VerifyToken[(Firebase Auth: Verify\nGoogle ID Token)]
    VerifyToken --> SyncUser[(Write/Sync root /users/uid\nRecord createdAt & email)]

    %% Discovery Query
    CheckAuth -- Yes --> QueryDiscovery
    SyncUser --> QueryDiscovery[(Firestore Collection Group Query\nmembers where email == userEmail)]

    %% Routing Engine
    QueryDiscovery --> EvalCircles{Evaluate Membership\n& Invitations}
    
    EvalCircles -- "0 Circles & 0 Invites" --> EmptyHub[My Fund Circles Hub\nEmpty State: Prompt to Create Circle]
    EvalCircles -- "Has >= 1 Pending Invites\nOR Belongs to >= 2 Circles" --> HubScreen[My Fund Circles Hub\nPending Invites + Managed + Joined Lists]
    EvalCircles -- "Exactly 1 Circle &\n0 Pending Invites" --> SmartLaunch([Smart Auto-Launch])

    %% Navigation
    SmartLaunch --> CircleHome[Circle Home Dashboard\nTitle: Circle Name | Currency Symbol]
    HubScreen --> TapCircle[User taps a Fund Circle card]
    TapCircle --> CircleHome
    EmptyHub --> TapCreateFromEmpty[User taps '+ Create Fund Circle']
    TapCreateFromEmpty --> CreateCircleScreen[CUJ-02: Create Circle Flow]

    %% Navigation back to Hub
    CircleHome --> NavSettings[User navigates to Settings tab]
    NavSettings --> TapSwitch[User taps 'Switch Fund Circle']
    TapSwitch --> HubScreen
```

---

### CUJ-02: Fund Circle Creation & Anti-Spam Guardrail

Maps how an Admin sets up a new Fund Circle, verifies currency immutability, checks creation limits, and initializes sequential counters.

```mermaid
flowchart TD
    StartCreate([User taps '+ Create Fund Circle']) --> CheckLimit{User's active\ncreatedCircleCount\n< 5?}

    %% Limit Blocked
    CheckLimit -- No (>= 5) --> LimitDialog[Limit Reached Dialog\n'Maximum limit of 5 active circles.\nArchive an existing circle to create a new one.']
    LimitDialog --> ReturnHub([Return to Hub])

    %% Creation Form
    CheckLimit -- Yes (< 5) --> CreateForm[Create Fund Circle Screen\nInputs: Name, Currency, Description]
    CreateForm --> SubmitForm[User enters details & taps 'Create Circle']

    %% Field Validations
    SubmitForm --> ValidateName{Circle Name\nvalid? (3..50 chars)}
    ValidateName -- No --> ErrName[Show Error: 'Name must be 3-50 characters'] --> CreateForm
    ValidateName -- Yes --> ValidateCurr{Currency\nselected?}
    ValidateCurr -- No --> ErrCurr[Show Error: 'Please select a currency'] --> CreateForm

    %% Atomic Firestore Transaction
    ValidateCurr -- Yes --> GenId[Generate Unique fundId UUID]
    GenId --> AtomicTxn[(Firestore Transaction:\n1. Create /funds/fundId doc\n2. Set adminId = currentUser.uid\n3. Set memberEmails = creatorEmail\n4. Set totalDeposits = 0, totalExpenses = 0\n5. Init /counters/members with nextIndex = 2\n6. Create /members/{userId} (memberId: 'FC-001') for Creator\n7. Increment /users/uid.createdCircleCount)]

    AtomicTxn --> SuccessToast[Toast: 'Fund Circle Created Successfully!']
    SuccessToast --> RouteDashboard([Direct Route to Circle Home Dashboard])
```

---

### CUJ-03: Stakeholder Invitation & Onboarding Lifecycle

Maps the two-sided journey: Admin inviting a member via email, and the stakeholder accepting/declining the invite on their Hub.

```mermaid
flowchart TD
    %% Admin Side
    subgraph Admin_Actions ["Admin: Invitation Dispatch"]
        A_Start([Admin opens Manage Members]) --> A_TapAdd[Tap 'Add Stakeholder']
        A_TapAdd --> A_Form[Add Stakeholder Modal\nInputs: Full Name, Phone, Gmail]
        A_Form --> A_Submit[Admin taps 'Send Invitation']
        A_Submit --> A_ValPhone{Phone valid?\n11 digits}
        A_ValPhone -- No --> A_ErrPhone[Show Phone Error] --> A_Form
        A_ValPhone -- Yes --> A_ValEmail{Gmail valid &\nunique in circle?}
        A_ValEmail -- No --> A_ErrEmail[Show Email Error / Duplicate] --> A_Form
        A_ValEmail -- Yes --> A_AtomicBatch[(Firestore Batched Write:\n1. Read & increment /counters/members\n2. Assign next Member ID FC-00X\n3. Write /members/{memberId} status=PENDING\n4. Update /funds/fundId.memberEmails arrayUnion)]
        A_AtomicBatch --> A_Success[Toast: 'Invitation Sent to Member!']
    end

    %% Stakeholder Side
    subgraph Stakeholder_Actions ["Stakeholder: Invitation Gate"]
        S_Start([Stakeholder logs in via Google One-Tap]) --> S_Hub[My Fund Circles Hub]
        S_Hub --> S_Card[Pending Invitations Section\nDisplays Circle Name & Inviter Email]
        S_Card --> S_Choice{User Decision}
        
        %% Decline Path
        S_Choice -- Tap 'Decline' --> S_ConfirmDecline[Decline Confirmation Dialog]
        S_ConfirmDecline --> S_WriteDecline[(Update /members/{memberId}\nSet status = DECLINED)]
        S_Dismiss[Card dismissed from Hub]
        S_WriteDecline --> S_Dismiss

        %% Accept Path
        S_Choice -- Tap 'Accept' --> S_WriteAccept[(Firestore Batched Write:\n1. Update /members/{memberId}:\n   Set status = ACTIVE\n   Set userId = currentUser.uid\n2. Update /funds/{fundId}:\n   memberEmails.append(userEmail)\n   memberCount += 1)]
        S_WriteAccept --> S_MoveJoined[Circle moves into 'Circles I Joined']
        S_MoveJoined --> S_EnterCircle([User taps Circle to enter Dashboard])
    end

    A_AtomicBatch -. Triggers visibility on .-> S_Card
```

---

### CUJ-04: Private Beneficiary Profile Completion

Maps how an onboarded stakeholder privately records their NID and Nominee data with strict isolation from other members and circles.

```mermaid
flowchart TD
    Start([Stakeholder inside Circle]) --> NavSettings[Navigate to Settings tab]
    NavSettings --> TapProfile[Tap 'My Profile']
    TapProfile --> ViewProfile[View Profile Screen\nDisplays Member ID, Status, Joining Date]
    ViewProfile --> CheckStatus{Account Status == ACTIVE?}
    
    CheckStatus -- No (INACTIVE / LEFT) --> ReadOnlyNotice[Edit button disabled\n'Profile editing disabled for inactive accounts']
    
    CheckStatus -- Yes (ACTIVE) --> TapEdit[Tap 'Edit Profile']
    TapEdit --> ProfileForm[Edit Profile Modal\nInputs: Full Name, Phone, NID Number,\nNominee Name, Relationship, Nominee Contact]
    
    ProfileForm --> SubmitEdit[User taps 'Save Profile']
    SubmitEdit --> ValNID{NID valid?\n10, 13, or 17 digits}
    ValNID -- No --> ErrNID[Show Error: 'Invalid NID number'] --> ProfileForm
    ValNID -- Yes --> ValNominee{Nominee fields\ncomplete?}
    ValNominee -- No --> ErrNom[Show Error: 'All nominee fields required'] --> ProfileForm
    
    ValNominee -- Yes --> WriteProfile[(Update /funds/fundId/members/{userId}\nWrite NID and Nominee records)]
    WriteProfile --> ProfileSaved[Toast: 'Profile Updated Successfully!']
    ProfileSaved --> UpdatedView([Display Updated Profile\nPrivate fields masked from other members])
```

---

### CUJ-05: Financial Transaction Entry (Deposits & Expenses)

Maps the 5-field entry forms, validation checks, future-date rejection, atomic aggregates update, and first-transaction currency locking.

```mermaid
flowchart TD
    Start([Admin taps 'Add Transaction']) --> SelectType{Transaction Type}

    %% ---------------- DEPOSIT FLOW ----------------
    SelectType -- Deposit --> DepForm[Deposit Entry Modal\nInputs: Member, Amount, Date, Method, Note]
    DepForm --> DepSubmit[Admin taps 'Save Deposit']
    
    DepSubmit --> DepValMember{Member selected\n& status == ACTIVE?}
    DepValMember -- No --> ErrDepMem[Show Error: 'Select active member'] --> DepForm
    
    DepValMember -- Yes --> DepValAmt{Amount valid?\nWhole integer >= 1}
    DepValAmt -- No --> ErrDepAmt[Show Error: 'Amount must be >= 1 whole unit'] --> DepForm
    
    DepValAmt -- Yes --> DepValDate{Date <= Today?\nFuture dates blocked}
    DepValDate -- No --> ErrDepDate[Show Error: 'Future dates are not permitted'] --> DepForm
    
    DepValDate -- Yes --> DepBatch[(Firestore Batched Write:\n1. Write /funds/fundId/deposits/UUID\n2. totalDeposits = FieldValue.increment amount\n3. recordedBalance = FieldValue.increment amount\n4. transactionCount = FieldValue.increment 1\n5. Lock currency if transactionCount was 0)]
    
    DepBatch --> DepSuccess[Toast: 'Deposit Recorded Successfully!']
    DepSuccess --> UpdateDashboard([Live Refresh: Member Total, Circle Total & Balance])

    %% ---------------- EXPENSE FLOW ----------------
    SelectType -- Expense --> ExpForm[Expense Entry Modal\nInputs: Date, Amount, Category, Description, Method]
    ExpForm --> ExpSubmit[Admin taps 'Save Expense']
    
    ExpSubmit --> ExpValAmt{Amount valid?\nWhole integer >= 1}
    ExpValAmt -- No --> ErrExpAmt[Show Error: 'Amount must be >= 1 whole unit'] --> ExpForm
    
    ExpValAmt -- Yes --> ExpValDate{Date <= Today?\nFuture dates blocked}
    ExpValDate -- No --> ErrExpDate[Show Error: 'Future dates are not permitted'] --> ExpForm
    
    ExpValDate -- Yes --> ExpValCat{Category from\n8 approved options?}
    ExpValCat -- No --> ErrExpCat[Show Error: 'Select predefined category'] --> ExpForm
    
    ExpValCat -- Yes --> ExpValDesc{Description\nvalid? (3..250 chars)}
    ExpValDesc -- No --> ErrExpDesc[Show Error: 'Enter description 3-250 chars'] --> ExpForm
    
    ExpValDesc -- Yes --> ExpBatch[(Firestore Batched Write:\n1. Write /funds/fundId/expenses/UUID\n2. totalExpenses = FieldValue.increment amount\n3. recordedBalance = FieldValue.increment -amount\n4. transactionCount = FieldValue.increment 1\n5. Lock currency if transactionCount was 0)]
    
    ExpBatch --> ExpSuccess[Toast: 'Expense Recorded Successfully!']
    ExpSuccess --> UpdateDashboard
```

---

### CUJ-06: Immutable Transaction Reversal Protocol

Maps the administrative transaction reversal workflow: mandatory reason, atomic financial reversal (`FieldValue.increment(-originalAmount)`), strike-through ledger rendering, and immutable transparency audit card.

```mermaid
flowchart TD
    Start([Admin opens Transaction Details]) --> CheckVoid{Transaction already\nreversed? (isVoid == true)}
    
    CheckVoid -- Yes --> AlreadyVoided[Reverse action disabled;\nDisplays permanent 'REVERSED' alert banner]
    
    CheckVoid -- No --> TapVoid[Admin taps 'Reverse Transaction']
    TapVoid --> VoidDialog[Reverse Transaction Dialog\nShows original transaction amount\n+ Mandatory Reversal Reason input (5..250 chars)]
    
    VoidDialog --> EnterReason[Admin enters reason for reversal\ne.g., 'Duplicate vendor billing error']
    EnterReason --> TapConfirm[Admin taps 'Confirm & Reverse']
    
    %% Reason Validation Gate
    TapConfirm --> CheckReason{Reversal Reason valid?\n(5..250 chars non-blank)}
    CheckReason -- No --> ErrReason[Show Error: 'Reversal reason is mandatory (min 5 chars)'] --> VoidDialog
    
    %% Atomic Firestore Transaction Execution
    CheckReason -- Yes --> AtomicVoid[(Firestore Transaction:\n1. Verify isVoid == false\n2. Set isVoid = true\n3. Set voidReason = reasonText\n4. Set voidedBy = adminEmail\n5. Set voidedAt = currentTimeMillis\n6. Append audit record to correctionHistory\n7. Atomically reverse parent /funds/fundId balance:\n   - If Deposit: totalDeposits -= originalAmount, recordedBalance -= originalAmount\n   - If Expense: totalExpenses -= originalAmount, recordedBalance += originalAmount)]
    
    AtomicVoid --> ToastDone[Toast: 'Transaction Reversed & Ledger Updated']
    ToastDone --> ShowDetails([Transaction Detail Screen:\n- Amount displayed with strike-through\n- Prominent Amber 'REVERSED ↩' badge\n- Expandable Transparency Audit Card with reason, actor, and timestamp])
    
    %% Stakeholder Auditing
    ShowDetails -. Visible to all circle members .-> StakeholderView[Stakeholder Audit View:\nAll members see identical updated balance\nand permanent immutable reversal audit disclosure]
```

---

### CUJ-07: Financial Reports & On-Device CSV Export Pipeline

Maps period selection, report rendering, on-device pure Kotlin RFC 4180 CSV ledger compilation, and Android Share Sheet dispatch via `FileProvider`.

```mermaid
flowchart TD
    Start([Member navigates to Reports tab]) --> PeriodSelect{Select Reporting\nPeriod}
    
    PeriodSelect -- "Month + Year" --> PickDate[Select Month & Year Pickers\ne.g., November 2026]
    PeriodSelect -- "All Time" --> SetAllTime[Select 'All Time' Toggle]
    
    PickDate --> SelectReportType
    SetAllTime --> SelectReportType
    
    SelectReportType{Choose 1 of 4\nReport Types}
    SelectReportType -- 1 --> R_Summary[Monthly Financial Summary\nDeposits, Expenses, Net Surplus/Deficit, Closing Balance]
    SelectReportType -- 2 --> R_MemberStmt[Member-wise Deposit Statement]
    SelectReportType -- 3 --> R_AllDeposits[Complete Deposit History\nChronological Ledger of all deposits]
    SelectReportType -- 4 --> R_AllExpenses[Complete Expense History\nChronological Ledger of all operational expenses]
    
    %% Member Statement Filter
    R_MemberStmt --> MemberFilterChoice{Member Selection}
    MemberFilterChoice -- All Members --> R_RosterTable[Side-by-side Roster Statement\nAll members' contributions for period]
    MemberFilterChoice -- Specific Member --> PickMember[Dropdown: Select Member] --> R_IndividualStmt[Individual Member Statement\nPeriod deposits + All-Time cumulative total]

    %% On-Screen Preview
    R_Summary --> RenderPreview[Render Interactive On-Screen Preview in Compose]
    R_RosterTable --> RenderPreview
    R_IndividualStmt --> RenderPreview
    R_AllDeposits --> RenderPreview
    R_AllExpenses --> RenderPreview
    
    %% Document Export
    RenderPreview --> TapExport[User Taps 'Export Ledger (CSV)']
    TapExport --> GenCSV[On-Device Kotlin Coroutine:\nCompile RFC 4180 CSV with LedgerCsvGenerator]
    
    GenCSV --> SaveCache[Write text to app cache:\ncacheDir/exports/FundCircle_CircleName_Ledger_Timestamp.csv]
    
    SaveCache --> FileProvider[[Android FileProvider\nGenerate content:// URI with read grant]]
    FileProvider --> LaunchShare[[Intent.ACTION_SEND\nLaunch native Android Share Sheet]]
    LaunchShare --> End([User shares via WhatsApp, Email,\nGoogle Drive, or Saves to Device Downloads])
```

---

### CUJ-08: Workspace Navigation & Governance

Maps circle switching, voluntary member exits, Admin ownership transfer, and circle archival vs. deletion.

```mermaid
flowchart TD
    Start([User in Circle Settings Screen]) --> ChooseAction{Admin or Stakeholder\nAction}

    %% ---------------- 8A: SWITCH CIRCLE ----------------
    ChooseAction -- "Switch Fund Circle" --> TapSwitch[User taps 'Switch Fund Circle']
    TapSwitch --> RouteHub([Navigate back to My Fund Circles Hub])

    %% ---------------- 8B: VOLUNTARY EXIT ----------------
    ChooseAction -- "Leave Circle (Stakeholder)" --> TapLeave[Stakeholder taps 'Leave Circle']
    TapLeave --> WarnDialog[Warning Dialog:\n'Leaving this circle will set your status to Left.\nYour historical deposit records will remain permanently on file for firm transparency.']
    WarnDialog --> ConfirmLeave{User confirms?}
    ConfirmLeave -- Cancel --> Start
    ConfirmLeave -- Confirm --> WriteLeave[(Update /members/{memberId}:\nSet status = LEFT)]
    WriteLeave --> MovePast[Circle moves to 'Past / Archived Circles' on Hub\nAccess becomes permanent Read-Only]
    MovePast --> RouteHub

    %% ---------------- 8C: ADMIN TRANSFER ----------------
    ChooseAction -- "Transfer Admin Role (Admin)" --> TapTransfer[Admin taps 'Transfer Admin Role']
    TapTransfer --> PickNewAdmin[Dropdown: Select from currently ACTIVE Stakeholders]
    PickNewAdmin --> ConfirmTransfer{Admin confirms\nownership transfer?}
    ConfirmTransfer -- Cancel --> Start
    ConfirmTransfer -- Confirm --> WriteTransfer[(Firestore Transaction:\n1. Set /funds/fundId.adminId = targetMember.uid\n2. Set /funds/fundId.adminEmail = targetMember.email\n3. Demote former Admin to standard ACTIVE stakeholder)]
    WriteTransfer --> ToastTransfer[Toast: 'Admin Role Transferred Successfully']
    ToastTransfer --> RefreshSettings([Settings refresh with Stakeholder permissions])

    %% ---------------- 8D: ARCHIVE VS DELETE ----------------
    ChooseAction -- "Close / Remove Circle (Admin)" --> CheckTxnCount{transactionCount == 0?}
    
    %% Zero Transactions: Hard Delete
    CheckTxnCount -- Yes (0 txns) --> ShowDeleteBtn[Admin sees 'Delete Fund Circle' button]
    ShowDeleteBtn --> TapDelete[Admin taps Delete]
    TapDelete --> ConfirmDelete{Confirm permanent\ndeletion?}
    ConfirmDelete -- Cancel --> Start
    ConfirmDelete -- Confirm --> HardDelete[(Firestore Transaction:\n1. Delete all member docs & counters\n2. Delete /funds/fundId doc\n3. Decrement /users/uid.createdCircleCount)]
    HardDelete --> ToastDeleted[Toast: 'Fund Circle Deleted']
    ToastDeleted --> RouteHub

    %% >= 1 Transactions: Permanent Archival
    CheckTxnCount -- No (>= 1 txns) --> ShowArchiveBtn[Admin sees 'Archive Fund Circle' button\nHard deletion is permanently blocked]
    ShowArchiveBtn --> TapArchive[Admin taps Archive]
    TapArchive --> WarnArchive[Warning Dialog:\n'Archiving permanently freezes this Fund Circle.\nNo new transactions can be recorded.\nAll members retain permanent read-only access to records and reports.']
    WarnArchive --> ConfirmArchive{Confirm archival?}
    ConfirmArchive -- Cancel --> Start
    ConfirmArchive -- Confirm --> WriteArchive[(Update /funds/fundId:\nSet isLocked = true)]
    WriteArchive --> ToastArchived[Toast: 'Fund Circle Archived into Permanent Read-Only State']
    ToastArchived --> FreezeCircle([Circle UI transitions to Read-Only across all tabs])
```

---

## 3. UI Screen interaction state matrix

To ensure zero ambiguity during Jetpack Compose state modeling (`UIState`), every screen must implement the following 4 canonical states:

| Screen | Loading State (`isLoading = true`) | Empty State (`items.isEmpty()`) | Content / Happy Path | Error State (`errorMessage != null`) |
|---|---|---|---|---|
| **My Fund Circles Hub** | Shimmer cards for Managed, Joined, and Pending sections. | Friendly illustration + *"You haven't joined any circles yet. Create one or ask your admin to invite you."* | Categorized list of Circle Cards with status badges and currency tags. | Full-screen error banner with *"Retry Connection"* button. |
| **Home Dashboard** | Circular loader on summary cards; shimmer on recent feed. | Summary cards display `0` + *"No transactions recorded yet. Tap '+' to record the first entry."* | Real-time Total Deposits, Total Expenses, Balance, and Top 10 Recent Transactions. | Card error banner with offline cached banner indicator. |
| **Members Roster** | Shimmer list items with circular avatar placeholders. | *"No members added yet. Tap 'Add Stakeholder' to send invitations."* | Searchable list showing Member ID, Full Name, Status badge, and Total Deposits. | Inline retry button for member collection snapshot listener. |
| **Transactions Tab** | Shimmer list with segmented button disabled. | *"No deposits/expenses found for the selected filter."* | Filterable list with Date, Amount, Contributor/Category, and Correction badge. | Snackbar notification with automatic offline retry. |
| **Reports Engine** | Progress bar over preview canvas; export buttons disabled. | *"No transactions match the selected Month/Year period."* | Formatted on-screen summary table with live calculated totals. | Toast explaining export failure (e.g., storage I/O error) with retry. |

---

## 4. Architectural verification & compliance

1. **Traceability:** Every flowchart directly implements the user journeys defined in [Investment_Firm_App_User_Stories_Use_Cases_v1.md](file:///Users/kawsar/Desktop/projects/personal/Android/fund-circle/docs/archive/Investment_Firm_App_User_Stories_Use_Cases_v1.md) and respects the data boundaries in [Investment_Firm_App_FSD_SRS_v1.md](file:///Users/kawsar/Desktop/projects/personal/Android/fund-circle/docs/archive/Investment_Firm_App_FSD_SRS_v1.md).
2. **Zero Dead Ends:** Every error state, validation rejection, and user cancellation path loops back to an actionable input screen or parent navigation hub.
3. **Turnkey Scaffolding:** Android engineers can now model Jetpack Compose screens, ViewModels, and navigation graphs (`NavHost`) 1:1 against these diagrams without making any visual or interaction assumptions.
