// Allow/deny suite for the root firestore.rules, run against the Firestore emulator.
//
//   npm run test:emulator        (starts the emulator, runs the suite, stops it)
//   npm test                     (when an emulator is already running and
//                                 FIRESTORE_EMULATOR_HOST is set)
//
// "App flows" mirror the writes in app/src/main/java/com/fundcircle/app/data/datasource/remote/*Impl.kt
// and must stay ALLOWED. "Attacks" must stay DENIED. If you change a client write path, change the
// matching case here in the same commit.

import { readFileSync } from 'node:fs';
import { after, before, describe, test } from 'node:test';
import { assertFails, assertSucceeds, initializeTestEnvironment } from '@firebase/rules-unit-testing';
import {
  arrayUnion, collection, deleteDoc, doc, getDoc, getDocs, increment, query, runTransaction,
  setDoc, setLogLevel, updateDoc, where, writeBatch,
} from 'firebase/firestore';

// Every DENY case makes the SDK log a PERMISSION_DENIED stream error; keep the report readable.
setLogLevel('silent');

const RULES = readFileSync(new URL('../firestore.rules', import.meta.url), 'utf8');
const F = 'f1';
const NOW = () => Date.now();
const DAY = 86_400_000;

let env;

before(async () => {
  if (!process.env.FIRESTORE_EMULATOR_HOST) {
    throw new Error('FIRESTORE_EMULATOR_HOST is not set. Run `npm run test:emulator`, or start the emulator first.');
  }
  // Host and port come from FIRESTORE_EMULATOR_HOST, which `firebase emulators:exec` sets.
  env = await initializeTestEnvironment({ projectId: 'demo-fundcircle', firestore: { rules: RULES } });
});

after(async () => {
  await env?.cleanup();
});

// --- Identities -------------------------------------------------------------------------------

const as = (uid, email) => () => env.authenticatedContext(uid, { email, email_verified: true }).firestore();
const admin = as('adminUid', 'admin@x.com');
// The member was invited by email, so their member doc id ("invite-1") is not their uid.
const member = as('memberUid', 'member@x.com');
const stranger = as('strangerUid', 'stranger@x.com');
// Auth email with mixed case: the rules compare lower-cased emails.
const newbie = as('newUid', 'New.User@X.com');

// --- Fixtures ---------------------------------------------------------------------------------

async function seed(fundOverrides = {}) {
  await env.clearFirestore();
  await env.withSecurityRulesDisabled(async (ctx) => {
    const db = ctx.firestore();
    await setDoc(doc(db, 'funds', F), {
      fundId: F, name: 'Circle', description: null, currency: 'BDT', adminId: 'adminUid', adminEmail: 'admin@x.com',
      memberEmails: ['admin@x.com', 'member@x.com'], memberCount: 2, totalDeposits: 1000, totalExpenses: 100,
      recordedBalance: 900, isLocked: false, createdAt: 1, updatedAt: 1, ...fundOverrides,
    });
    await setDoc(doc(db, `funds/${F}/members/adminUid`), {
      userId: 'adminUid', memberId: 'FC-001', email: 'admin@x.com', name: 'Admin', role: 'ADMIN', status: 'ACTIVE',
      totalContributed: 0, joinedAt: 1, lastActiveAt: 1,
    });
    await setDoc(doc(db, `funds/${F}/members/invite-1`), {
      userId: 'invite-1', memberId: 'FC-002', email: 'member@x.com', name: 'Member', phone: '01700000000',
      role: 'MEMBER', status: 'ACTIVE', totalContributed: 0, joinedAt: 1, lastActiveAt: 1,
    });
    await setDoc(doc(db, `funds/${F}/deposits/dep-old`), {
      depositId: 'dep-old', fundId: F, memberId: 'invite-1', memberEmail: 'member@x.com', memberName: 'Member',
      amount: 500, depositDate: 1, paymentMethod: 'Cash', note: null,
      isVoid: false, voidReason: null, voidedAt: null, voidedBy: null, createdAt: 1,
    });
    await setDoc(doc(db, `funds/${F}/expenses/exp-old`), {
      expenseId: 'exp-old', fundId: F, amount: 100, expenseDate: 1, category: 'Other', description: 'x',
      paymentMethod: 'Cash', paidBy: '', createdBy: 'adminUid', creatorEmail: 'admin@x.com',
      isVoid: false, voidReason: null, voidedAt: null, voidedBy: null, createdAt: 1,
    });
  });
}

/** Mutate seeded data with rules off, to set up a state the client could not reach itself. */
const unsafe = (path, data) => env.withSecurityRulesDisabled((ctx) => updateDoc(doc(ctx.firestore(), path), data));

const depositDoc = (id, amount, extra = {}) => ({
  depositId: id, fundId: F, memberId: 'invite-1', memberEmail: 'member@x.com', memberName: 'Member', amount,
  depositDate: NOW() - 1000, paymentMethod: 'Cash', note: null,
  isVoid: false, voidReason: null, voidedAt: null, voidedBy: null, createdAt: NOW() - 1000, ...extra,
});

const expenseDoc = (id, amount) => ({
  expenseId: id, fundId: F, amount, expenseDate: NOW() - 1000, category: 'Other', description: 'Stationery',
  paymentMethod: 'Cash', paidBy: '', createdBy: 'adminUid', creatorEmail: 'admin@x.com',
  isVoid: false, voidReason: null, voidedAt: null, voidedBy: null, createdAt: NOW() - 1000,
});

const newFundDoc = (id, overrides = {}) => ({
  fundId: id, name: 'Mine', description: null, currency: 'USD', adminId: 'newUid', adminEmail: 'new.user@x.com',
  memberEmails: ['new.user@x.com'], memberCount: 1, totalDeposits: 0, totalExpenses: 0, recordedBalance: 0,
  isLocked: false, createdAt: NOW(), updatedAt: NOW(), ...overrides,
});

// --- Client write paths (same shape as the Kotlin data sources) ---------------------------------

function recordDeposit(db, id, amount, { depositAmount = amount, extra = {} } = {}) {
  const b = writeBatch(db);
  b.set(doc(db, `funds/${F}/deposits/${id}`), depositDoc(id, depositAmount, extra));
  b.update(doc(db, 'funds', F), {
    totalDeposits: increment(amount), recordedBalance: increment(amount), updatedAt: NOW(), lastTxId: id,
  });
  return b.commit();
}

function recordExpense(db, id, amount) {
  const b = writeBatch(db);
  b.set(doc(db, `funds/${F}/expenses/${id}`), expenseDoc(id, amount));
  b.update(doc(db, 'funds', F), {
    totalExpenses: increment(amount), recordedBalance: increment(-amount), updatedAt: NOW(), lastTxId: id,
  });
  return b.commit();
}

function reverse(db, coll, id, amount, voidedBy = 'admin@x.com') {
  return runTransaction(db, async (tx) => {
    const ref = doc(db, `funds/${F}/${coll}/${id}`);
    await tx.get(ref);
    tx.update(ref, { isVoid: true, voidReason: 'Duplicate entry', voidedBy, voidedAt: NOW() });
    const totals = coll === 'deposits'
      ? { totalDeposits: increment(-amount), recordedBalance: increment(-amount) }
      : { totalExpenses: increment(-amount), recordedBalance: increment(amount) };
    tx.update(doc(db, 'funds', F), { ...totals, updatedAt: NOW(), lastTxId: id });
  });
}

function invite(db, id, email) {
  const b = writeBatch(db);
  b.set(doc(db, `funds/${F}/members/${id}`), {
    userId: id, memberId: 'FC-003', email, name: 'Invitee', phone: '01711111111', role: 'MEMBER', status: 'ACTIVE',
    totalContributed: 0, joinedAt: NOW(), lastActiveAt: NOW(),
  });
  b.update(doc(db, 'funds', F), { memberCount: increment(1), memberEmails: arrayUnion(email), updatedAt: NOW() });
  return b.commit();
}

const kyc = (verifiedByAdmin) => ({
  userId: 'invite-1', nidNumber: '1234567890', nomineeName: 'N', nomineePhone: '01712345678',
  nomineeRelation: 'Spouse', verifiedByAdmin, updatedAt: NOW(),
});

// --- Cases ------------------------------------------------------------------------------------

const allow = (name, fn, setup = () => seed()) =>
  test(`ALLOW ${name}`, async () => { await setup(); await assertSucceeds(fn()); });
const deny = (name, fn, setup = () => seed()) =>
  test(`DENY  ${name}`, async () => { await setup(); await assertFails(fn()); });

describe('app flows that must keep working', () => {
  allow('create circle batch (fund + creator member + user counter)', () => {
    const db = newbie();
    const b = writeBatch(db);
    b.set(doc(db, 'funds', 'f2'), newFundDoc('f2'));
    b.set(doc(db, 'funds/f2/members/newUid'), {
      userId: 'newUid', memberId: 'FC-001', email: 'new.user@x.com', name: 'N', role: 'ADMIN', status: 'ACTIVE',
      totalContributed: 0, joinedAt: NOW(), lastActiveAt: NOW(),
    });
    b.set(doc(db, 'users/newUid'), { createdCircleCount: increment(1) }, { merge: true });
    return b.commit();
  });
  allow('query my circles (array-contains email)', () =>
    getDocs(query(collection(member(), 'funds'), where('memberEmails', 'array-contains', 'member@x.com'))));
  allow('member reads fund, members, deposits', async () => {
    const db = member();
    await getDoc(doc(db, 'funds', F));
    await getDocs(collection(db, `funds/${F}/members`));
    return getDocs(collection(db, `funds/${F}/deposits`));
  });
  allow('admin invites member (member doc + count + arrayUnion)', () => invite(admin(), 'invite-2', 'third@x.com'));
  allow('member records deposit with matching increment', () => recordDeposit(member(), 'dep-new', 250));
  allow('admin records expense', () => recordExpense(admin(), 'exp-new', 40));
  allow('admin reverses deposit (transaction)', () => reverse(admin(), 'deposits', 'dep-old', 500));
  allow('admin reverses expense (transaction)', () => reverse(admin(), 'expenses', 'exp-old', 100));
  allow('admin toggles lock', () => updateDoc(doc(admin(), 'funds', F), { isLocked: true, updatedAt: NOW() }));
  allow('admin transfers ownership to invited member', () => {
    const db = admin();
    const b = writeBatch(db);
    b.update(doc(db, 'funds', F), { adminId: 'invite-1', adminEmail: 'member@x.com', updatedAt: NOW() });
    b.update(doc(db, `funds/${F}/members/adminUid`), { role: 'MEMBER', updatedAt: NOW() });
    b.update(doc(db, `funds/${F}/members/invite-1`), { role: 'ADMIN', updatedAt: NOW() });
    return b.commit();
  });
  allow('transferred admin (matched by email) records an expense', () => recordExpense(member(), 'exp-2', 10),
    () => seed({ adminId: 'invite-1', adminEmail: 'member@x.com' }));
  allow('invited member leaves via their member doc', () =>
    updateDoc(doc(member(), `funds/${F}/members/invite-1`), { status: 'LEFT', lastActiveAt: NOW() }));
  allow('admin deactivates a member', () =>
    updateDoc(doc(admin(), `funds/${F}/members/invite-1`), { status: 'INACTIVE', lastActiveAt: NOW() }));
  allow('invited member writes own KYC', () =>
    setDoc(doc(member(), `funds/${F}/members/invite-1/kyc/identity`), kyc(false), { merge: true }));
  allow('invited member reads own KYC', () => getDoc(doc(member(), `funds/${F}/members/invite-1/kyc/identity`)));
  allow('admin reads a member KYC', () => getDoc(doc(admin(), `funds/${F}/members/invite-1/kyc/identity`)));
});

describe('attacks that must be denied', () => {
  describe('fund document', () => {
    deny('member overwrites recordedBalance', () => updateDoc(doc(member(), 'funds', F), { recordedBalance: 999999 }));
    deny('member makes self admin', () =>
      updateDoc(doc(member(), 'funds', F), { adminId: 'memberUid', adminEmail: 'member@x.com' }));
    deny('member adds an email to memberEmails', () =>
      updateDoc(doc(member(), 'funds', F), { memberEmails: arrayUnion('evil@x.com') }));
    deny('member bumps totals with no deposit doc', () => updateDoc(doc(member(), 'funds', F),
      { totalDeposits: increment(100), recordedBalance: increment(100), lastTxId: 'ghost' }));
    deny('member bumps totals pointing at an existing deposit', () => updateDoc(doc(member(), 'funds', F),
      { totalDeposits: increment(500), recordedBalance: increment(500), lastTxId: 'dep-old' }));
    deny('admin deletes the circle', () => deleteDoc(doc(admin(), 'funds', F)));
    deny('admin changes currency', () => updateDoc(doc(admin(), 'funds', F), { currency: 'USD' }));
    deny('create circle with unsupported currency', () =>
      setDoc(doc(newbie(), 'funds', 'f3'), newFundDoc('f3', { currency: 'GBP' })));
    deny('create circle with preloaded balance', () =>
      setDoc(doc(newbie(), 'funds', 'f3'), newFundDoc('f3', { recordedBalance: 5000 })));
  });

  describe('deposits and expenses', () => {
    deny('member creates deposit without touching totals', () =>
      setDoc(doc(member(), `funds/${F}/deposits/d9`), depositDoc('d9', 100)));
    deny('deposit amount differs from increment', () => recordDeposit(member(), 'd9', 100, { depositAmount: 5 }));
    deny('deposit created already voided', () => recordDeposit(member(), 'd9', 100, { extra: { isVoid: true } }));
    deny('deposit with decimal amount', () => recordDeposit(member(), 'd9', 10.5));
    deny('deposit dated in the future', () =>
      recordDeposit(member(), 'd9', 100, { extra: { depositDate: NOW() + DAY } }));
    deny('deposit while circle is locked', () => recordDeposit(member(), 'd9', 100), () => seed({ isLocked: true }));
    deny('member records an expense', () => recordExpense(member(), 'e9', 10));
    deny('edit a deposit amount', () => updateDoc(doc(admin(), `funds/${F}/deposits/dep-old`), { amount: 1 }));
    deny('delete a deposit', () => deleteDoc(doc(admin(), `funds/${F}/deposits/dep-old`)));
    deny('edit an expense amount', () => updateDoc(doc(admin(), `funds/${F}/expenses/exp-old`), { amount: 1 }));
    deny('delete an expense', () => deleteDoc(doc(admin(), `funds/${F}/expenses/exp-old`)));
  });

  describe('reversals', () => {
    deny('member reverses a deposit', () => reverse(member(), 'deposits', 'dep-old', 500, 'member@x.com'));
    deny('reverse an already-reversed deposit (double decrement)', async () => {
      await unsafe(`funds/${F}/deposits/dep-old`, { isVoid: true });
      return reverse(admin(), 'deposits', 'dep-old', 500);
    });
    deny('un-reverse a deposit', async () => {
      await unsafe(`funds/${F}/deposits/dep-old`, { isVoid: true, voidReason: 'Duplicate entry' });
      return updateDoc(doc(admin(), `funds/${F}/deposits/dep-old`), { isVoid: false });
    });
    deny('reverse with decrement != original amount', () => reverse(admin(), 'deposits', 'dep-old', 50));
    deny('reverse while circle is locked', () => reverse(admin(), 'deposits', 'dep-old', 500),
      () => seed({ isLocked: true }));
    deny('reverse without touching totals', () => updateDoc(doc(admin(), `funds/${F}/deposits/dep-old`),
      { isVoid: true, voidReason: 'Duplicate entry', voidedBy: 'admin@x.com', voidedAt: NOW() }));
  });

  describe('membership and strangers', () => {
    deny('stranger plants ADMIN member doc in a circle', () =>
      setDoc(doc(stranger(), `funds/${F}/members/strangerUid`), {
        userId: 'strangerUid', memberId: 'FC-099', email: 'stranger@x.com', name: 'S', role: 'ADMIN',
        status: 'ACTIVE', totalContributed: 0, joinedAt: 1, lastActiveAt: 1,
      }));
    deny('stranger reads circle', () => getDoc(doc(stranger(), 'funds', F)));
    deny('stranger writes audit log', () => setDoc(doc(stranger(), `funds/${F}/audit_logs/l1`), { action: 'x' }));
    deny('member marks another member LEFT', () =>
      updateDoc(doc(member(), `funds/${F}/members/adminUid`), { status: 'LEFT', lastActiveAt: NOW() }));
    deny('member changes own role to ADMIN', () =>
      updateDoc(doc(member(), `funds/${F}/members/invite-1`), { role: 'ADMIN' }));
    deny('admin leaves without transferring', () =>
      updateDoc(doc(admin(), `funds/${F}/members/adminUid`), { status: 'LEFT', lastActiveAt: NOW() }));
    deny('invite with uppercase email', () => invite(admin(), 'invite-3', 'Bob@x.com'));
    deny('admin deletes a member doc', () => deleteDoc(doc(admin(), `funds/${F}/members/invite-1`)));
    deny('write into another user profile', () => setDoc(doc(member(), 'users/adminUid'), { createdCircleCount: 0 }));
  });

  describe('KYC', () => {
    deny('member self-verifies KYC', () =>
      setDoc(doc(member(), `funds/${F}/members/invite-1/kyc/identity`), kyc(true)));
    deny('member reads another member KYC', () => getDoc(doc(member(), `funds/${F}/members/adminUid/kyc/identity`)));
  });
});
