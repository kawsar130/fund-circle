# 🎨 Figma Canvas & Interactive Prototype Alignment Guide

> **Project:** Fund Circle — Enterprise Collective Capital & Pooled Fund Management for Android  
> **Master Figma File:** [Fund Circle — Mobile App (Android M3)](https://www.figma.com/design/44Gqq8dDY62FfKf28yxXuJ)  
> **Interactive Sandbox:** [`docs/design_preview/index.html`](index.html)  
> **Visual Gallery:** [`docs/design_preview/gallery.html`](gallery.html)  

---

## 🔍 Root Cause Analysis: Why Was Page 2 Incomplete in Figma?

When you inspected the Figma workspace and compared it against the **Interactive Prototype Sandbox**, you noticed that:
1. **Page 2 (`📱 02. Android Screen Layouts`) only had Screens 01, 02, and 03.**
2. **Screens 04 through 14 were missing from the canvas.**
3. **Screen 02 on Page 2 looked basic compared to the rich CSS styling in the prototype.**

### The Technical Diagnosis
During automated Figma canvas population, the Figma MCP API returned a server-enforced quota paywall:
```
Encountered error in tool execution: You've reached the Figma MCP tool call limit on the Starter plan. 
Upgrade your plan for more tool calls: https://www.figma.com/files/team/994133842963119838/all-projects?upgrade=mcp_rate_limit_paywall
```
- **Starter Plan Quota:** Figma strictly limits free Starter tier teams (`Challengers`, `team::994133842963119838`) to **~6–20 tool calls per month** across their platform ([Figma MCP Rate Limits](https://developers.figma.com/docs/figma-mcp-server/rate-limits-access/)).
- **Execution Cut-Off:** Page 1 (`Design System & Foundations`) and Flow 1 (`Screens 01–03`) were successfully created, but as Flow 2 (`Screens 04–06`) began, Figma's backend blocked all further write and read calls.

---

## 🛠️ The 3 Instant Solutions for 100% Alignment

We have prepared three seamless paths to bring your Figma canvas into 100% visual and architectural alignment:

### Option 1: Drag-and-Drop 2x Retina Screen Renders (Fastest & Free — 30 Seconds)
We generated pixel-perfect, 2x Retina ($824 \times 1834\text{px}$) high-resolution renders of all 14 screens + Design Tokens directly from the canonical prototype engine into [`docs/design_preview/screens/`](screens/):

| # | Screen File | Route / Component | Invariant Highlights |
|---|---|---|---|
| **00** | [`00_Design_Tokens_and_Atoms.png`](screens/00_Design_Tokens_and_Atoms.png) | Tokens & Badges | M3 Tonal Palettes, Tabular Typography, State Badges |
| **01** | [`01_Sign_In_auth_welcome.png`](screens/01_Sign_In_auth_welcome.png) | `auth_welcome` | Google One-Tap, 48dp Touch Targets |
| **02** | [`02_Circle_Hub_circle_hub.png`](screens/02_Circle_Hub_circle_hub.png) | `circle_hub` | Multi-circle management, Green Valley: ৳ 1,450,000 |
| **03** | [`03_Create_Wizard_circle_create.png`](screens/03_Create_Wizard_circle_create.png) | `circle_create` | Currency lock (BDT/USD/EUR), Whole-integer Long |
| **04** | [`04_Circle_Dashboard_dashboard_home.png`](screens/04_Circle_Dashboard_dashboard_home.png) | `dashboard_home` | $O(1)$ Parent Aggregate Read, ৳ 1.6M Inflows, ৳ 150K Outflows |
| **05** | [`05_Transactions_Ledger_ledger_transactions.png`](screens/05_Transactions_Ledger_ledger_transactions.png) | `ledger_transactions` | Chronological audit ledger, Filter chips, Void badges |
| **06** | [`06_Transaction_Detail_Audit_Card.png`](screens/06_Transaction_Detail_Audit_Card.png) | `tx_detail/{id}` | Verified deposit, Admin Void trigger, Immutable disclosure |
| **07** | [`07_Add_Transaction_Modal_modal_add_tx.png`](screens/07_Add_Transaction_Modal_modal_add_tx.png) | `modal_add_tx` | Deposit/Expense toggle, 8 M3 Category chips |
| **07b**| [`07b_Void_Confirmation_Dialog_dialog_void_tx.png`](screens/07b_Void_Confirmation_Dialog_dialog_void_tx.png)| `dialog_void_tx` | Zero hard deletes, Mandatory reason, Balance reversal |
| **08** | [`08_Stakeholders_Roster_members_roster.png`](screens/08_Stakeholders_Roster_members_roster.png) | `members_roster` | **Strict 2 Roles** (`ADMIN 🛡️` & `STAKEHOLDER 👤`), Zero Co-Admin |
| **09** | [`09_Member_Detail_Ledger_member_detail.png`](screens/09_Member_Detail_Ledger_member_detail.png) | `member_detail/{id}` | Member contribution history (৳ 450,000), KYC info |
| **10** | [`10_Add_Stakeholder_Modal_modal_add_member.png`](screens/10_Add_Stakeholder_Modal_modal_add_member.png) | `modal_add_member` | Member ID auto-allocation (FC-005), Stakeholder role only |
| **11** | [`11_Edit_My_Profile_Self_Service.png`](screens/11_Edit_My_Profile_Self_Service.png) | `profile_edit` | Self-service name & phone updates, Role locked |
| **12** | [`12_Reports_and_CSV_Export_reports_export.png`](screens/12_Reports_and_CSV_Export_reports_export.png) | `reports_export` | Opening ৳ 1M + ৳ 500K - ৳ 50K = ৳ 1.45M, On-device CSV |
| **13** | [`13_Settings_and_Governance_settings_admin.png`](screens/13_Settings_and_Governance_settings_admin.png) | `settings_admin` | Governance rules, Archive & Leave circle flows, Cache quota |

**How to place on Figma canvas:**
1. Open Figma and navigate to Page 2: `📱 02. Android Screen Layouts`.
2. Delete the 3 partial frames (Screens 01, 02, 03) from Page 2.
3. Open your Mac Finder to `docs/design_preview/screens/`.
4. Select all 15 PNG files and drag them directly into the Figma canvas.
5. In Figma, select all images and set width to `412` (Figma will auto-scale height to `917`). Arrange into 5 clean flow columns.

---

### Option 2: 1-Click Vector Import via Figma "html.to.design" (Free & Native Vectors)
If you want native, editable Figma text layers and auto-layout frames rather than static images:
1. In Figma, open the Plugins menu (or press `Cmd + /` and search for **"html.to.design"**).
2. Run the free plugin **html.to.design**.
3. Select **"Upload HTML file"** and choose [`docs/design_preview/index.html`](index.html).
4. The plugin will convert the entire responsive prototype into editable Figma auto-layout frames with 1:1 pixel perfection, exact typography, and native vectors—bypassing the MCP API completely!

---

### Option 3: Upgrade to Figma Professional (Full Agent Automation)
If you prefer the AI agent to manage, update, and generate native Figma components programmatically via the Figma MCP server:
1. Upgrade the `Challengers` team to **Figma Professional** ($12–15/mo for 1 editor seat) via [Figma Pricing](https://www.figma.com/pricing).
2. This increases your MCP tool call quota from ~20/month to **200 tool calls per day**.
3. Once upgraded, ask the agent to run the automated Figma build, and it will programmatically populate all 14 screens as native auto-layout components linked to the `"Fund Circle Tokens"` variable collection.

---

## ⚡ The Interactive Prototype is Already Live in Figma!

Remember: On **Page 3 (`⚡ 03. Interactive Prototype Sandbox`)** in your Figma file:
- **Node 27:3** contains the full, runnable, interactive code prototype!
- You can click **"Run Prototype"** inside Figma or view [`docs/design_preview/index.html`](index.html) to interact with every button, tab, filter, and calculation across all 14 screens!
- You can also open [`docs/design_preview/gallery.html`](gallery.html) in your browser for a side-by-side presentation gallery of all 14 screens.
