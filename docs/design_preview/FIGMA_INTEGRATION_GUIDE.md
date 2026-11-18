# 🎨 Figma Architecture & Interactive Prototype Guide

> **Project:** Fund Circle — Enterprise Collective Capital & Pooled Fund Management for Android  
> **Figma Interactive Prototype:** [Fund Circle — Mobile App (Android M3)](https://www.figma.com/design/44Gqq8dDY62FfKf28yxXuJ?node-id=42-3&autoplay-code-preview=42-3) (Page: `Interactive Prototype Sandbox`, Node `42:3`)  
> **Visual Presentation Gallery:** [`docs/design_preview/gallery.html`](gallery.html) (2x Retina)  
> **Standalone Local Prototype:** [`docs/design_preview/index.html`](index.html)  

---

## 📌 Unified Figma Structure: "Interactive Prototype Sandbox"

To ensure 100% consistency, zero clutter, and flawless presentation fidelity, the Master Figma file is streamlined into a single canonical page:

### ⚡ `Interactive Prototype Sandbox` (Node `42:3`)
- **Direct Cloud Access:** Click **"Run Prototype"** inside Figma or open [Node 42:3](https://www.figma.com/design/44Gqq8dDY62FfKf28yxXuJ?node-id=42-3&autoplay-code-preview=42-3).
- **Coverage:** Implements **all 14 commercial screens + Design Tokens** with real interactions, theme toggling (Light / Dark M3), tab switching, and instant calculations.
- **Architectural Compliance:** 
  - Strict 2-role governance model (`ADMIN 🛡️` & `STAKEHOLDER 👤`), zero "Co-Admin".
  - Whole-integer monetary precision (`Long` $\ge 1$, BDT `৳`, USD `$`, EUR `€`), zero decimals/poisha.
  - $O(1)$ single-read parent aggregates (`Green Valley Property Pool`, `৳ 1,450,000` balance).
  - Immutable Transaction Reversal Protocol (`isVoid = true`, `voidReason`, atomic balance reversal).

---

## 📱 High-Resolution 2x Retina Screen Gallery

For documentation, presentations, and design reviews, all 14 screens + Design Tokens are captured at **2x Retina resolution ($824 \times 1834\text{px}$)** in [`docs/design_preview/screens/`](screens/) and presented side-by-side in [`docs/design_preview/gallery.html`](gallery.html):

| # | Screen File | Target Composable Route | Architectural Invariants Verified |
|---|---|---|---|
| **00** | [`00_Design_Tokens_and_Atoms.png`](screens/00_Design_Tokens_and_Atoms.png) | Design Tokens & Badges | M3 Tonal Palettes, Tabular Typography Scale (`tnum`), Atomic Badges |
| **01** | [`01_Sign_In_auth_welcome.png`](screens/01_Sign_In_auth_welcome.png) | `auth_welcome` (`AuthScreen.kt`) | Google One-Tap Sign-In, 48dp Touch Targets, Whole-Integer notice |
| **02** | [`02_Circle_Hub_circle_hub.png`](screens/02_Circle_Hub_circle_hub.png) | `circle_hub` (`CircleHubScreen.kt`) | Multi-circle hub, Max 5 admin limit, Green Valley: ৳ 1,450,000 |
| **03** | [`03_Create_Wizard_circle_create.png`](screens/03_Create_Wizard_circle_create.png) | `circle_create` (`CreateCircleScreen.kt`) | Base Currency lock (BDT/USD/EUR), Target amount `Long` $\ge 1$ |
| **04** | [`04_Circle_Dashboard_dashboard_home.png`](screens/04_Circle_Dashboard_dashboard_home.png) | `dashboard_home` (`DashboardScreen.kt`) | $O(1)$ Parent Aggregate Read, ৳ 1.6M Inflows, ৳ 150K Outflows |
| **05** | [`05_Transactions_Ledger_ledger_transactions.png`](screens/05_Transactions_Ledger_ledger_transactions.png) | `ledger_transactions` (`LedgerScreen.kt`) | Chronological audit ledger, Filter chips, REVERSED ↩ badges |
| **06** | [`06_Transaction_Detail_Audit_Card.png`](screens/06_Transaction_Detail_Audit_Card.png) | `tx_detail/{id}` (`TxDetailScreen.kt`) | Verified deposit, Admin Reverse action trigger, Transparency disclosure |
| **07** | [`07_Add_Transaction_Modal_modal_add_tx.png`](screens/07_Add_Transaction_Modal_modal_add_tx.png) | `modal_add_tx` (`RecordTxSheet.kt`) | Capital Inflow / Pool Expense toggle, 8 M3 Category chips |
| **08** | [`08_Stakeholders_Roster_members_roster.png`](screens/08_Stakeholders_Roster_members_roster.png) | `members_roster` (`MembersRosterScreen.kt`) | **Strict 2 Roles** (`ADMIN 🛡️` & `STAKEHOLDER 👤`), Zero Co-Admin |
| **09** | [`09_Member_Detail_Ledger_member_detail.png`](screens/09_Member_Detail_Ledger_member_detail.png) | `member_detail/{id}` (`MemberDetailScreen.kt`) | Member contribution history (৳ 450,000), KYC info |
| **10** | [`10_Add_Stakeholder_Modal_modal_add_member.png`](screens/10_Add_Stakeholder_Modal_modal_add_member.png) | `modal_add_member` (`InviteMemberSheet.kt`) | Member ID auto-allocation (`FC-005`), Stakeholder role only |
| **11** | [`11_Edit_My_Profile_Self_Service.png`](screens/11_Edit_My_Profile_Self_Service.png) | `profile_edit` (`ProfileEditScreen.kt`) | Self-service name & phone updates, Role locked |
| **12** | [`12_Reports_and_CSV_Export_reports_export.png`](screens/12_Reports_and_CSV_Export_reports_export.png) | `reports_export` (`ReportsScreen.kt`) | Opening ৳ 1M + ৳ 500K - ৳ 50K = ৳ 1.45M, On-device CSV |
| **13** | [`13_Settings_and_Governance_settings_admin.png`](screens/13_Settings_and_Governance_settings_admin.png) | `settings_admin` (`SettingsScreen.kt`) | Governance rules, Archive & Leave circle flows, Cache quota |
| **14** | [`07b_Reverse_Confirmation_Dialog_dialog_reverse_tx.png`](screens/07b_Reverse_Confirmation_Dialog_dialog_reverse_tx.png)| `dialog_reverse_tx` (`ReverseConfirmDialog.kt`) | Zero hard deletes, Mandatory reason (&ge; 5 chars), Balance reversal |

---

## 💡 Native Vector Import via "html.to.design" (Optional)

If editable Figma vector layers or frames are ever desired alongside the interactive prototype:
1. In Figma, press `Cmd + /` (or open the Plugins menu) and run the free community plugin **html.to.design**.
2. Select **"Upload HTML file"** and choose [`docs/design_preview/index.html`](index.html).
3. The plugin will instantly convert the HTML/CSS components into native auto-layout vector frames with 1:1 pixel fidelity, preserving all typography and styling.
