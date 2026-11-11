const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_yasin";
const BOB_UID = "bob_other";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user: cannot read products", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("products").get());
});

test("Authenticated owner: can create and read product", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const productRef = aliceDb.collection("users").doc(ALICE_UID).collection("products").doc("prod_1");

  await assertSucceeds(
    productRef.set({
      userId: ALICE_UID,
      name: "Samsung Charger 25W",
      category: "Mobile Chargers",
      brand: "Samsung",
      model: "EP-TA800",
      purchasePrice: 450,
      sellingPrice: 750,
      currentStock: 15,
      minStockAlert: 3,
      createdAt: new Date(),
    })
  );

  await assertSucceeds(productRef.get());
});

test("Cross-user isolation: Bob cannot access Alice's products", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc(ALICE_UID).collection("products").doc("prod_alice").set({
      userId: ALICE_UID,
      name: "Tempered Glass",
      category: "Tempered Glass",
      purchasePrice: 30,
      sellingPrice: 100,
      currentStock: 50,
      createdAt: new Date(),
    });
  });

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("products").doc("prod_alice").get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("products").get());
});

test("Authenticated owner: can record sale and expense", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("sales").doc("sale_1").set({
      userId: ALICE_UID,
      invoiceNumber: "INV-20261111-001",
      customerName: "Rahim",
      customerPhone: "01711000000",
      totalAmount: 1500,
      totalCost: 1100,
      grossProfit: 400,
      paidAmount: 1500,
      dueAmount: 0,
      saleDate: "11-11-2026",
      createdAt: new Date(),
    })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("expenses").doc("exp_1").set({
      userId: ALICE_UID,
      title: "Electricity Bill",
      amount: 1200,
      category: "Electricity Bill",
      expenseDate: "11-11-2026",
      createdAt: new Date(),
    })
  );
});
