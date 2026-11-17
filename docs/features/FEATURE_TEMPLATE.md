# 🧩 Feature Slice: [Feature Name]

> **Feature ID:** `FEAT-[00X]`  
> **Target Release:** `v1.X`  
> **Status:** `Draft` | `Approved` | `Implemented`  
> **Dependencies:** `Core v1 Baseline` (Requires Invariants #1 to #7)  

---

## 1. User Story & Business Rules

### User Story
> *As a [Circle Admin / Stakeholder],*  
> *I want to [perform a specific action],*  
> *So that [benefit / outcome is achieved].*

### Business Rules & Constraints
- **Rule 1:** [Specific boundary constraint, e.g., only Admin can initiate]
- **Rule 2:** [Monetary whole integer constraint if applicable]
- **Rule 3:** [Future dates blocked / atomic aggregate handling]

---

## 2. Data & Contract Diff

### Firestore Schema Additions
```typescript
// Path: /funds/{fundId}/...
interface [NewDocumentOrField] {
  fieldId: string;
  createdAt: number;
}
```

### Kotlin DTO & Domain Model Diff
```kotlin
// Data Layer DTO (com.fundcircle.app.data.dto)
@kotlinx.serialization.Serializable
data class FeatureNameDto(
    val id: String = "",
    val timestamp: Long = 0L
)

// Domain Entity (com.fundcircle.app.domain.model)
data class FeatureName(
    val id: String,
    val timestamp: Long
)
```

---

## 3. UI / Compose Screen Additions

### Composable Component / Dialog Layout
```text
┌────────────────────────────────────────────────────────┐
│ [Feature Modal / Sheet Title]                 [X Close]│
│                                                        │
│ [Input Field 1]                                        │
│ ┌────────────────────────────────────────────────────┐ │
│ │ Value                                              │ │
│ └────────────────────────────────────────────────────┘ │
│                                                        │
│ [Primary Action Button]                                │
└────────────────────────────────────────────────────────┘
```

- **M3 Tokens:** Use semantic tokens from `docs/Design_Tokens.md` (`primary`, `surfaceContainer`, `48dp` touch targets).

---

## 4. Firestore Security Rules Diff

```javascript
// Additions inside match /funds/{fundId} { ... }
match /[new_subcollection]/{docId} {
  allow read: if isCircleMember(get(/databases/$(database)/documents/funds/$(fundId)).data);
  allow write: if isCircleAdmin(get(/databases/$(database)/documents/funds/$(fundId)).data);
}
```

---

## 5. Verification & Test Checklist

- [ ] Unit Test: Business logic and edge case validation
- [ ] Security Rules Test: Unauthorized reads/writes blocked
- [ ] UI Preview: Jetpack Compose preview renders correctly
- [ ] Invariant Check: $O(1)$ reads preserved, amounts are whole integers
