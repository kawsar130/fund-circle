# 📚 Fund Circle Documentation Index

Welcome to the **Fund Circle** documentation repository. This directory houses the architectural blueprints, API data contracts, UI/UX specifications, design tokens, and modular feature slices for the application.

> [!IMPORTANT]
> **Agentic Development Policy:** Never load all documentation files into an LLM context window at once. Follow the **Selective Context Loading Matrix** below and observe the hierarchy established in [`AGENTS.md`](../AGENTS.md).

---

## 🏛️ Active Technical Specifications (Core SSOT Suite)

These four documents form the authoritative **Single Source of Truth (SSOT)** for all engineering implementation.

| Document | Primary Focus | Key Responsibilities |
| :--- | :--- | :--- |
| **[`Fund_Circle_API_Specification_v1.0.md`](Fund_Circle_API_Specification_v1.0.md)** | **Sole Data Contract SSOT** | Wire types, DTOs, Repository interfaces, UUID idempotency, Voiding Protocol, Firestore schemas, and Error Taxonomy. |
| **[`Fund_Circle_System_Architecture_and_Tech_Stack_v1.0.md`](Fund_Circle_System_Architecture_and_Tech_Stack_v1.0.md)** | **Sole Infra & Security SSOT** | Clean Architecture boundaries, Firestore security rules, composite indexes, atomic aggregates (`FieldValue.increment`), Hilt DI, and CI/CD quality gates. |
| **[`Fund_Circle_Wireframes_and_UI_Spec_v1.0.md`](Fund_Circle_Wireframes_and_UI_Spec_v1.0.md)** | **Sole UI Layout SSOT** | 14-screen inventory, Material 3 layout grids, spacing rules, component hierarchy, and empty/loading/error state UI contracts. |
| **[`Design_Tokens.md`](Design_Tokens.md)** | **Sole Semantic Styling SSOT** | Lean token reference & Theme.kt contract: exact Material 3 light/dark hex colors, system bar protection, tabular typography (`tnum`), 8dp modular spacing, and 48dp touch targets. |

---

## 🧩 Modular Feature Slices (Future Growth)

Located in **[`features/`](features/)**:
- **[`features/README.md`](features/README.md):** Guidelines for adding features without bloating core specifications.
- **[`features/FEATURE_TEMPLATE.md`](features/FEATURE_TEMPLATE.md):** Standardized ~50-line template for drafting new feature slices (v1.1, v1.2+).

---

## 🗺️ Supplementary Interaction Guides & Prototypes

| File / Asset | Description |
| :--- | :--- |
| **[`Fund_Circle_User_Flowcharts_v1.0.md`](Fund_Circle_User_Flowcharts_v1.0.md)** | Visual Mermaid state machines mapping CUJ-01 through CUJ-08 (Smart Launch, Invitation Gate, Voiding Reversals, Circle Archival). |
| **[`design_preview/index.html`](design_preview/index.html)** | Standalone interactive HTML/CSS responsive prototype demonstrating the 14 Android screens with live tab switching and theme toggling. |
| **[`design_preview/gallery.html`](design_preview/gallery.html)** | High-resolution visual gallery showcasing all 14 Android screens with routes and invariant compliance. |
| **[Master Figma File](https://www.figma.com/design/44Gqq8dDY62FfKf28yxXuJ)** | Live cloud design workspace featuring the canonical **Interactive Prototype Sandbox** page ([Node 42:3](https://www.figma.com/design/44Gqq8dDY62FfKf28yxXuJ?node-id=42-3&autoplay-code-preview=42-3)). |
| **[Master FigJam Board](https://www.figma.com/board/dxkTCpjsDBnEQp2zJRphAI)** | Interactive cloud user flow diagrams and architectural branching tree. |

---

## 🚀 Google Play Store & Legal Compliance

Located in **[`playstore/`](playstore/)**:
- **[`playstore/Play_Store_Listing_and_Policy_Guide.md`](playstore/Play_Store_Listing_and_Policy_Guide.md):** Store listing metadata, short/full descriptions, non-loan disclaimer, Data Safety form declarations, account deletion instructions, and the 20-tester closed testing playbook.
- **[`playstore/Privacy_Policy.md`](playstore/Privacy_Policy.md):** Legal-standard Privacy Policy ready to host on GitHub Pages covering Google Credential Manager auth, Firestore data encryption, and account deletion rights.

---

## 📦 Historical & Upstream Archive

Upstream discovery and extended design narratives are archived in **[`archive/`](archive/)**:
- **[`archive/Fund_Circle_Branding_and_Style_Guide_v1.0.md`](archive/Fund_Circle_Branding_and_Style_Guide_v1.0.md)** (Full 1,362-line Brand Guide & Prototype Code)
- **[`archive/Investment_Firm_App_PRD_v2.md`](archive/Investment_Firm_App_PRD_v2.md)** (PRD v3.0)
- **[`archive/Investment_Firm_App_FSD_SRS_v1.md`](archive/Investment_Firm_App_FSD_SRS_v1.md)** (FSD/SRS v2.1)
- **[`archive/Investment_Firm_App_User_Stories_Use_Cases_v1.md`](archive/Investment_Firm_App_User_Stories_Use_Cases_v1.md)** (User Stories & Use Cases v2.0)

> [!WARNING]
> Archived files are preserved for historical product traceability only. If any contradiction exists between an archived document and an active technical specification, the active specification and `AGENTS.md` strictly take precedence.

---

## 🎯 Selective Context Loading Matrix for Agents

| Engineering Task | Primary File to Read | Secondary Reference |
| :--- | :--- | :--- |
| **Domain Models & Use Cases** | `Fund_Circle_API_Specification_v1.0.md` | `Fund_Circle_System_Architecture_and_Tech_Stack_v1.0.md` |
| **Firestore Data Sources & Repositories** | `Fund_Circle_API_Specification_v1.0.md` | `Fund_Circle_System_Architecture_and_Tech_Stack_v1.0.md` |
| **Firestore Security Rules & Indexes** | `Fund_Circle_System_Architecture_and_Tech_Stack_v1.0.md` | `Fund_Circle_API_Specification_v1.0.md` |
| **Jetpack Compose UI Screens & ViewModels** | `Fund_Circle_Wireframes_and_UI_Spec_v1.0.md` | `Design_Tokens.md` |
| **Theme Setup (`Color.kt`, `Type.kt`, etc.)** | `Design_Tokens.md` | `Fund_Circle_Wireframes_and_UI_Spec_v1.0.md` |
| **Navigation Graph & Screen Routing** | `Fund_Circle_User_Flowcharts_v1.0.md` | `Fund_Circle_System_Architecture_and_Tech_Stack_v1.0.md` |
| **Feature Slices (v1.1+)** | `AGENTS.md` | `features/feature_<name>.md` |
| **Unit & Integration Test Cases** | `archive/Investment_Firm_App_User_Stories_Use_Cases_v1.md` | `Fund_Circle_API_Specification_v1.0.md` |
| **Play Store Listing & Data Safety Review** | `playstore/Play_Store_Listing_and_Policy_Guide.md` | `playstore/Privacy_Policy.md` |
