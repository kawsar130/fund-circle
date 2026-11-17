# 🚀 Fund Circle — Google Play Store Listing & Compliance Guide

> **Target Platform:** Google Play Store (Target SDK 35, Min SDK 26 — 95.5%+ Global Device Reach)  
> **Account Type:** Personal Developer Account ($25 one-time registration)  
> **Budget Tier:** $0 Operating Budget (Firebase Spark Free Tier)  

---

## 1. App Store Listing Metadata

### App Title (Max 30 Characters)
```text
Fund Circle: Collective Ledger
```
*(Alternative: `Fund Circle — Group Fund Tracker`)*

### Short Description (Max 80 Characters)
```text
Transparent group fund, pooled capital & shared financial ledger management.
```

### Full Description (Max 4,000 Characters)
```text
Fund Circle is a modern, transparent collective fund management and shared ledger application designed for angel syndicates, property partnerships, investment clubs, family pooled funds, and project groups.

Keep every stakeholder aligned with an immutable, auditable, and real-time financial tracking workspace.

KEY FEATURES:

• MULTI-TENANT FUND WORKSPACES
Create and administer independent Fund Circles or participate as an invited member across multiple circles. Switch between active circles effortlessly.

• INSTANT ONE-TAP GOOGLE SIGN-IN
Zero passwords or PINs to forget. Authenticate instantly and securely using Android Credential Manager and your Google account.

• REAL-TIME SHARED LEDGER & TOTALS
Every circle member views identical, synchronized numbers. Track total capital deposits, operational expenditures, and the live recorded balance without calculating spreadsheets manually.

• 5-FIELD FINANCIAL RECORDING
Streamlined, human-error-free recording for deposits and operational expenses. Future dates are strictly blocked to prevent erroneous records.

• IMMUTABLE AUDIT TRAIL & VOIDING
Financial records are never deleted. Accidental entries can be transparently voided with mandatory explanatory reasons, preserving a complete, tamper-proof audit trail for all partners.

• CSV LEDGER EXPORT
Generate and export RFC 4180-compliant CSV financial ledgers directly on your device. Share via WhatsApp, Email, or Google Drive for local record-keeping and external audits.

• OFFLINE RESILIENCE
View your fund balances and recent transactions even without an active internet connection through intelligent local caching.

---
MANDATORY POLICY DISCLOSURE & DISCLAIMER:
Fund Circle is strictly a record-keeping, ledger tracking, and bookkeeping utility. Fund Circle DOES NOT process bank transfers, execute fiat currency transactions, offer credit or personal loans, solicit investments, or act as a financial institution or banking entity. All monetary contributions recorded within the app are processed offline or externally by the respective group members.
```

---

## 2. Google Play Policy & Data Safety Declarations

To ensure automated approval by Google Play Review without policy flags:

### A. Financial Services Declaration
- **Category:** Finance / Business Tools
- **Loan App Declaration:** Select **"NO"** (This app does not provide or facilitate personal loans).
- **Banking / Payment Gateway:** Select **"NO"** (This app is an informational record-keeping ledger).

### B. Data Safety Form (Play Console)

| Data Type | Collected? | Shared with 3rd Parties? | Purpose | Ephemeral? |
| :--- | :---: | :---: | :--- | :---: |
| **Name** | Yes | **No** | App functionality (member roster display) | No |
| **Email Address** | Yes | **No** | Account management, authentication, and invitations | No |
| **Financial Info (Ledger records)** | Yes | **No** | App functionality (bookkeeping & balances) | No |
| **User ID** | Yes | **No** | Account identification & session management | No |
| **Crash Logs & Diagnostics** | Yes | **No** | App stability & performance tracking via Crashlytics | No |

- **Encryption in Transit:** Yes (All network traffic uses TLS 1.3 / HTTPS).
- **Data Deletion Mechanism:** Yes (Users can request account and data deletion directly in-app or via the web).

---

## 3. Account & Data Deletion Compliance (Google Play Requirement)

Google Play mandates that any app supporting account creation must provide an in-app and web-based data deletion path:

1. **In-App Deletion Path:**  
   `Settings` &rarr; `Account` &rarr; `Delete My Account & Personal Data`.  
   - Prompts confirmation dialog.
   - Deletes `/users/{uid}` document.
   - Removes user UID from all invited circles.
   - Signs out and terminates active session.
2. **Web-Based Deletion URL (Hosted on GitHub Pages):**  
   `https://<your-username>.github.io/fund-circle-privacy/#data-deletion`  
   - States that users can email `support@yourdomain.com` or use the in-app button to permanently wipe all stored profile records within 48 hours.

---

## 4. The 20-Tester / 14-Day Closed Testing Playbook ($0 Cost)

Google requires new personal developer accounts to complete closed testing before opening to production:

```
Step 1: Build Signed Release App Bundle (.aab)
  └── Command: ./gradlew bundleRelease

Step 2: Upload to Play Console 'Closed Testing' Track
  └── Create Closed Testing track: 'Alpha Testers'

Step 3: Recruit 20 Testers ($0 Cost Options)
  ├── 1. Friends, family, and syndicate partners (10-15 people)
  ├── 2. Reddit r/AndroidClosedTesting (active community exchanging tester slots)
  └── 3. Tech/developer Discord groups and Telegram channels

Step 4: Testers Opt-In via Google Play Web Link
  └── Keep at least 20 testers opted-in continuously for 14 calendar days

Step 5: Apply for Production Access
  └── Submit questionnaire in Play Console explaining feedback received and stability metrics
```

---

## 5. Android App Links & Digital Asset Links Verification (`assetlinks.json`)

To enable seamless opening of circle invitation links (`https://fundcircle.app/invite/...`) on Android 12+ without showing the Chrome disambiguation dialog:

1. **Host Path:** `https://fundcircle.app/.well-known/assetlinks.json`
2. **Content-Type:** `application/json`
3. **Payload:**
```json
[
  {
    "relation": ["delegate_permission/common.handle_all_urls"],
    "target": {
      "namespace": "android_app",
      "package_name": "com.fundcircle.app",
      "sha256_cert_fingerprints": [
        "YOUR_RELEASE_KEYSTORE_SHA256_FINGERPRINT_HERE"
      ]
    }
  }
]
```

> [!TIP]
> Retrieve the release fingerprint from Google Play Console under **Release** &rarr; **Setup** &rarr; **App Integrity** (App Signing key certificate SHA-256).
