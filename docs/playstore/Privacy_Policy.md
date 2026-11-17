# Privacy Policy for Fund Circle

**Effective Date:** November 17, 2026  
**Last Updated:** November 17, 2026  

Welcome to **Fund Circle** ("we", "our", or "us"). We are committed to protecting your privacy and ensuring transparency regarding how your data is handled. This Privacy Policy explains our practices regarding the collection, use, and disclosure of personal data when you use the Fund Circle mobile application (the "App") and the services provided through it.

---

## 1. Information We Collect

### A. Information You Provide to Us
- **Authentication Data:** When you sign in using Google Sign-In (via Android Credential Manager), we receive your Google display name, email address, and unique user identifier (UID) provided by Firebase Authentication. We do not receive or store your Google password.
- **Fund Circle Ledger Data:** As an Administrator or Stakeholder, you may input financial records, including deposit amounts, expense amounts, dates, payment method labels, categories, transaction notes, and member names.
- **Optional KYC & Profile Information:** If you choose to fill out your member profile within a circle, you may enter an identification number (NID) and nominee contact details. This data is strictly scoped to that specific circle and is never shared across circles or made public.

### B. Information Automatically Collected
- **App Health & Diagnostics:** We collect crash logs, performance metrics, and ANR (Application Not Responding) telemetry through Firebase Crashlytics to maintain app stability.
- **Device & Usage Telemetry:** Basic anonymized device information (OS version, device model) to ensure layout compatibility.

---

## 2. How We Use Your Information

We use the collected information solely to provide, operate, and maintain the App:
- To authenticate your identity and synchronize your workspace memberships.
- To display synchronized balances, deposit totals, and expenditure ledgers to all members of your Fund Circle.
- To enforce audit integrity and record immutable transaction voiding reasons.
- To generate on-device CSV exports upon your explicit request.
- To diagnose technical crashes and improve app responsiveness.

**We DO NOT sell, rent, monetize, or trade your personal or financial data to any third parties or advertisers.**

---

## 3. Financial Services Disclaimer

**Fund Circle is strictly a record-keeping and bookkeeping software application.**
- The App does **NOT** process bank transfers, wire funds, or handle fiat money.
- The App does **NOT** offer, facilitate, or broker personal loans, credit lines, or investment securities.
- All monetary records within the App represent manual entries tracking offline collective capital.

---

## 4. Data Storage and Security

- **Encryption in Transit:** All communication between the App and our backend database (Cloud Firestore) is encrypted using TLS 1.3.
- **Encrypted Local Storage:** Local session keys and active workspace identifiers are stored using Android Keystore and `EncryptedSharedPreferences`.
- **Access Control:** Multi-tenant physical data isolation ensures that only users whose email addresses have been invited and approved for a specific Fund Circle can view its documents.

---

## 5. Third-Party Service Providers

We utilize Google Firebase services for authentication and database infrastructure:
- **Firebase Authentication:** Handles secure Google One-Tap sign-in.
- **Cloud Firestore:** Distributed database for real-time ledger synchronization.
- **Firebase Crashlytics:** Anonymous crash reporting.

These services process data in accordance with Google's Privacy Policy: [https://policies.google.com/privacy](https://policies.google.com/privacy).

---

## 6. Your Data Rights & Deletion Request

You retain full ownership and control over your personal data:
- **Access & Correction:** You can inspect all ledger records and profile details directly within the App at any time.
- **Account & Data Deletion:**
  - **In-App:** Navigate to `Settings` &rarr; `Account` &rarr; `Delete My Account & Personal Data`. This will permanently erase your user profile and decouple your identity from all circles.
  - **Online / Email Request:** You may email your deletion request to `support@fundcircle.app` with the subject line *"Data Deletion Request"*. We will process and permanently purge your user profile records within 48 business hours.

---

## 7. Children's Privacy

The App is designed for business, investment clubs, and adult collective fund management. We do not knowingly collect personal information from individuals under the age of 18.

---

## 8. Updates to this Policy

We may update our Privacy Policy periodically. Any revisions will be reflected on this page with an updated "Last Updated" date.

---

## 9. Contact Us

If you have questions or concerns regarding this Privacy Policy or your data, please contact us:
- **Email:** `support@fundcircle.app`  
- **Developer:** Fund Circle Open Source Project  
