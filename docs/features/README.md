# 🧩 Fund Circle — Feature Slices Architecture

Welcome to the `docs/features/` directory. This folder houses **modular, self-contained feature specifications** for Fund Circle beyond the core v1 release.

---

## 🎯 The Feature-Slicing Rule (Zero Context Drift)

To keep the application modular, maintainable, and easy to build with AI agents:

1. **Core v1 Specs Remain Frozen:** The baseline architectural specifications in `docs/` (`API_Specification`, `System_Architecture`, `Wireframes`, `Design_Tokens`) define the core v1 foundation and **must not be bloated** with subsequent features.
2. **One File Per Feature:** Every new capability (e.g. v1.1 voting, v1.2 SMS auto-sync, v1.3 export extensions) is specified as an independent markdown file in this folder:
   - `docs/features/feature_<name>.md`
3. **Lean Context Feeding:** When prompting an AI agent to implement a specific feature, do **not** feed the entire documentation suite. Provide only:
   - `AGENTS.md` (Core Invariants & Boundaries)
   - `docs/features/feature_<name>.md` (The specific slice to build)
   - The relevant source files to modify

---

## 📄 How to Author a Feature Slice

Copy [`FEATURE_TEMPLATE.md`](FEATURE_TEMPLATE.md) and fill out the 5 standardized sections:
1. **User Story & Acceptance Criteria:** The human-facing goal and business rules.
2. **Data & Schema Diff:** New Firestore fields, subcollections, or DTO modifications.
3. **UI / Jetpack Compose Specs:** Screen layout additions, dialogs, and token bindings.
4. **Security Rules Diff:** Updated declarative Firestore rule snippets.
5. **Verification & Test Checklist:** Exact unit and UI test criteria.
