import { execSync } from 'child_process';
import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const CHROME_PATH = '/Applications/Google Chrome.app/Contents/MacOS/Google Chrome';
const HTML_FILE = path.resolve(__dirname, 'index.html');
const OUTPUT_DIR = path.resolve(__dirname, 'screens');

if (!fs.existsSync(OUTPUT_DIR)) {
  fs.mkdirSync(OUTPUT_DIR, { recursive: true });
}

const SCREENS = [
  { id: 'screen_tokens', filename: '00_Design_Tokens_and_Atoms.png' },
  { id: 'screen_01', filename: '01_Sign_In_auth_welcome.png' },
  { id: 'screen_02', filename: '02_Circle_Hub_circle_hub.png' },
  { id: 'screen_03', filename: '03_Create_Wizard_circle_create.png' },
  { id: 'screen_04', filename: '04_Circle_Dashboard_dashboard_home.png' },
  { id: 'screen_05', filename: '05_Transactions_Ledger_ledger_transactions.png' },
  { id: 'screen_06', filename: '06_Transaction_Detail_Audit_Card.png' },
  { id: 'screen_07', filename: '07_Add_Transaction_Modal_modal_add_tx.png' },
  { id: 'screen_14', filename: '07b_Reverse_Confirmation_Dialog_dialog_reverse_tx.png' },
  { id: 'screen_08', filename: '08_Stakeholders_Roster_members_roster.png' },
  { id: 'screen_09', filename: '09_Member_Detail_Ledger_member_detail.png' },
  { id: 'screen_10', filename: '10_Add_Stakeholder_Modal_modal_add_member.png' },
  { id: 'screen_11', filename: '11_Edit_My_Profile_Self_Service.png' },
  { id: 'screen_12', filename: '12_Reports_and_CSV_Export_reports_export.png' },
  { id: 'screen_13', filename: '13_Settings_and_Governance_settings_admin.png' }
];

console.log(`Starting high-resolution capture of ${SCREENS.length} Android M3 screens...`);

for (const screen of SCREENS) {
  const targetFile = path.join(OUTPUT_DIR, screen.filename);
  const targetUrl = `file://${HTML_FILE}?screen=${screen.id}&mode=phone`;
  
  console.log(`Capturing ${screen.id} -> ${screen.filename}...`);
  const cmd = `"${CHROME_PATH}" --headless=new --screenshot="${targetFile}" --window-size=412,917 --force-device-scale-factor=2 "${targetUrl}"`;
  
  try {
    execSync(cmd, { stdio: 'pipe' });
    const stats = fs.statSync(targetFile);
    console.log(`  ✓ Done: ${stats.size} bytes`);
  } catch (err) {
    console.error(`  ✗ Failed to capture ${screen.id}:`, err.message);
  }
}

console.log(`All ${SCREENS.length} screens captured successfully in ${OUTPUT_DIR}`);
