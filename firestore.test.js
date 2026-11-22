const test = require('node:test');
const fs = require('node:fs');
const {
  initializeTestEnvironment,
  assertSucceeds,
  assertFails,
} = require('@firebase/rules-unit-testing');

const PROJECT_ID = 'gen-lang-client-0799148087';
const RULES_PATH = './firestore.rules';

let testEnv;

function validProfile(uid, overrides = {}) {
  const now = new Date();
  return {
    userId: uid,
    selectedWarriorId: 'vajra_arjun',
    unlockedWarriorsCsv: 'vajra_arjun',
    unlockedBattlefield: false,
    koRecoveryActive: false,
    currentShaktiEnergy: 0,
    lifetimeShaktiEarned: 0,
    lifetimeShaktiSpent: 0,
    syncedAllTimeNaam: 250,
    koBaselineAllTimeNaam: 0,
    battlesWon: 0,
    battlesPlayed: 0,
    highestMissionCleared: 0,
    attackLevel: 1,
    defenseLevel: 1,
    auraLevel: 1,
    transformationStage: 0,
    warriorXp: 0,
    createdAt: now,
    updatedAt: now,
    ...overrides,
  };
}

function validNaamDay(uid, dateKey, overrides = {}) {
  const now = new Date();
  return {
    userId: uid,
    dateKey,
    displayDate: 'Nov 21, 2026',
    dayName: 'Saturday',
    count: 1080,
    goalAtDay: 10000,
    createdAt: now,
    updatedAt: now,
    ...overrides,
  };
}

function validHistoryEvent(uid, overrides = {}) {
  return {
    userId: uid,
    eventType: 'NAAM_UNLOCK',
    title: 'Battlefield Unlocked',
    subtitle: '10,000 real Naam Jap completed -> +10,000 Shakti',
    shaktiDelta: 10000,
    naamCountSnapshot: 10000,
    warriorId: 'vajra_arjun',
    createdAt: new Date(),
    ...overrides,
  };
}

test.before(async () => {
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules: fs.readFileSync(RULES_PATH, 'utf8'),
      host: '127.0.0.1',
      port: 8085,
    },
  });
});

test.after(async () => {
  await testEnv.cleanup();
});

test.beforeEach(async () => {
  await testEnv.clearFirestore();
});

test('unauthenticated users are denied read and write everywhere', async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.doc('users/alice/naam_shakti/profile').get());
  await assertFails(unauthDb.doc('users/alice/naam_shakti/profile').set(validProfile('alice')));
  await assertFails(unauthDb.collection('users/alice/naam_jap_days').get());
  await assertFails(unauthDb.collection('users/alice/shakti_history').get());
});

test('owner can create, read, update, and list NaamShaktiProfile; cross-user denied', async () => {
  const aliceDb = testEnv.authenticatedContext('alice').firestore();
  const bobDb = testEnv.authenticatedContext('bob').firestore();
  const ref = aliceDb.doc('users/alice/naam_shakti/profile');

  await assertSucceeds(ref.set(validProfile('alice')));
  await assertSucceeds(ref.get());
  await assertSucceeds(aliceDb.collection('users/alice/naam_shakti').get());

  // Valid update after 10,000 Naam unlock
  await assertSucceeds(ref.update({
    unlockedBattlefield: true,
    currentShaktiEnergy: 10000,
    lifetimeShaktiEarned: 10000,
    syncedAllTimeNaam: 10000,
    updatedAt: new Date(),
  }));

  // Cross-user read & write denied
  await assertFails(bobDb.doc('users/alice/naam_shakti/profile').get());
  await assertFails(bobDb.doc('users/alice/naam_shakti/profile').update({
    currentShaktiEnergy: 999999,
    updatedAt: new Date(),
  }));
});

test('profile rejects immutable field tampering and ghost fields', async () => {
  const aliceDb = testEnv.authenticatedContext('alice').firestore();
  const ref = aliceDb.doc('users/alice/naam_shakti/profile');

  // Ghost field rejected on create
  await assertFails(ref.set(validProfile('alice', { ghostCheatEnergy: 99999 })));
  // Negative energy rejected
  await assertFails(ref.set(validProfile('alice', { currentShaktiEnergy: -50 })));
  // Valid create
  await assertSucceeds(ref.set(validProfile('alice')));

  // Immutable createdAt cannot be modified
  await assertFails(ref.update({
    createdAt: new Date(Date.now() - 60000),
    updatedAt: new Date(),
  }));
  // Immutable userId cannot be modified
  await assertFails(ref.update({
    userId: 'bob',
    updatedAt: new Date(),
  }));
});

test('naam_jap_days allows owner CRUD and validates schema strictly', async () => {
  const aliceDb = testEnv.authenticatedContext('alice').firestore();
  const bobDb = testEnv.authenticatedContext('bob').firestore();
  const ref = aliceDb.doc('users/alice/naam_jap_days/2026-11-21');

  await assertSucceeds(ref.set(validNaamDay('alice', '2026-11-21')));
  await assertSucceeds(ref.update({ count: 10000, updatedAt: new Date() }));
  await assertSucceeds(aliceDb.collection('users/alice/naam_jap_days').get());

  // Mismatched dateKey rejected
  await assertFails(
    aliceDb.doc('users/alice/naam_jap_days/2026-11-22').set(validNaamDay('alice', '2026-11-21'))
  );
  // Cross-user rejected
  await assertFails(bobDb.doc('users/alice/naam_jap_days/2026-11-21').get());
});

test('shakti_history is append-only for owner and rejects updates or invalid enums', async () => {
  const aliceDb = testEnv.authenticatedContext('alice').firestore();
  const bobDb = testEnv.authenticatedContext('bob').firestore();
  const ref = aliceDb.doc('users/alice/shakti_history/evt_1');

  await assertSucceeds(ref.set(validHistoryEvent('alice')));
  await assertSucceeds(aliceDb.collection('users/alice/shakti_history').get());

  // Update is forbidden on append-only history ledger
  await assertFails(ref.update({ shaktiDelta: 50000 }));
  // Invalid eventType rejected
  await assertFails(
    aliceDb.doc('users/alice/shakti_history/evt_bad').set(validHistoryEvent('alice', { eventType: 'CHEAT_ADD' }))
  );
  // Cross-user read rejected
  await assertFails(bobDb.doc('users/alice/shakti_history/evt_1').get());
  // Owner delete allowed
  await assertSucceeds(ref.delete());
});
