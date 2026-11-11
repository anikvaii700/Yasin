package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.*
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import org.json.JSONArray
import org.json.JSONObject

class ShopRepository(val db: FirebaseFirestore) : ShopDataSource {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth

    fun requireUserId(): String {
        return auth.currentUser?.uid ?: throw IllegalStateException("User not authenticated")
    }

    // -------------------------------------------------------------
    // Profile
    // -------------------------------------------------------------
    override fun observeProfile(userId: String): Flow<ShopProfile?> {
        val path = "users/$userId/profile/shop_info"
        return db.document(path).snapshots().map { snap ->
            if (snap.exists()) snap.toObject(ShopProfile::class.java) else null
        }
    }

    override suspend fun saveProfile(userId: String, profile: ShopProfile) {
        val path = "users/$userId/profile/shop_info"
        val docRef = db.document(path)
        val payload = mapOf(
            "userId" to userId,
            "shopName" to profile.shopName,
            "ownerName" to profile.ownerName,
            "address" to profile.address,
            "phone" to profile.phone,
            "currency" to profile.currency,
            "lowStockAlertThreshold" to profile.lowStockAlertThreshold,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            throw e
        }
    }

    // -------------------------------------------------------------
    // Products
    // -------------------------------------------------------------
    override fun observeProducts(userId: String): Flow<List<Product>> {
        return db.collection("users").document(userId).collection("products")
            .snapshots()
            .map { snap -> snap.toObjects(Product::class.java) }
    }

    override suspend fun addProduct(userId: String, product: Product): String {
        val colRef = db.collection("users").document(userId).collection("products")
        val docRef = colRef.document()
        val payload = mapOf(
            "userId" to userId,
            "name" to product.name,
            "category" to product.category,
            "brand" to product.brand,
            "model" to product.model,
            "purchasePrice" to product.purchasePrice,
            "sellingPrice" to product.sellingPrice,
            "currentStock" to product.currentStock,
            "minStockAlert" to product.minStockAlert,
            "productCode" to product.productCode,
            "notes" to product.notes,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            return docRef.id
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, colRef.path)
            throw e
        }
    }

    override suspend fun updateProduct(userId: String, product: Product) {
        val docRef = db.collection("users").document(userId).collection("products").document(product.id)
        val payload = mapOf(
            "userId" to userId,
            "name" to product.name,
            "category" to product.category,
            "brand" to product.brand,
            "model" to product.model,
            "purchasePrice" to product.purchasePrice,
            "sellingPrice" to product.sellingPrice,
            "currentStock" to product.currentStock,
            "minStockAlert" to product.minStockAlert,
            "productCode" to product.productCode,
            "notes" to product.notes,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.update(payload).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    override suspend fun deleteProduct(userId: String, productId: String) {
        val docRef = db.collection("users").document(userId).collection("products").document(productId)
        try {
            docRef.delete().await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    // -------------------------------------------------------------
    // Sales (with atomic stock deduction)
    // -------------------------------------------------------------
    override fun observeSales(userId: String): Flow<List<Sale>> {
        return db.collection("users").document(userId).collection("sales")
            .snapshots()
            .map { snap -> snap.toObjects(Sale::class.java).sortedByDescending { it.saleDate } }
    }

    override suspend fun recordSale(
        userId: String,
        sale: Sale,
        items: List<SaleItem>
    ): String {
        val salesCol = db.collection("users").document(userId).collection("sales")
        val saleDoc = salesCol.document()

        // Batch / transaction for stock deduction
        db.runTransaction { transaction ->
            // Check stock first
            for (item in items) {
                if (item.productId.isNotBlank()) {
                    val prodRef = db.collection("users").document(userId).collection("products").document(item.productId)
                    val prodSnap = transaction.get(prodRef)
                    if (prodSnap.exists()) {
                        val currentStock = prodSnap.getLong("currentStock") ?: 0L
                        if (currentStock < item.quantity) {
                            throw IllegalStateException("Insufficient stock for ${item.productName}. Available: $currentStock, Required: ${item.quantity}")
                        }
                        transaction.update(prodRef, "currentStock", currentStock - item.quantity)
                    }
                }
            }

            val salePayload = mapOf(
                "userId" to userId,
                "invoiceNumber" to sale.invoiceNumber,
                "customerName" to sale.customerName,
                "customerPhone" to sale.customerPhone,
                "itemsSummary" to sale.itemsSummary,
                "totalAmount" to sale.totalAmount,
                "totalCost" to sale.totalCost,
                "grossProfit" to sale.grossProfit,
                "paidAmount" to sale.paidAmount,
                "dueAmount" to sale.dueAmount,
                "saleDate" to sale.saleDate,
                "notes" to sale.notes,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            transaction.set(saleDoc, salePayload)
        }.await()

        return saleDoc.id
    }

    override suspend fun deleteSale(userId: String, saleId: String) {
        val saleRef = db.collection("users").document(userId).collection("sales").document(saleId)
        try {
            saleRef.delete().await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, saleRef.path)
            throw e
        }
    }

    // -------------------------------------------------------------
    // Purchases (with atomic stock increment)
    // -------------------------------------------------------------
    override fun observePurchases(userId: String): Flow<List<Purchase>> {
        return db.collection("users").document(userId).collection("purchases")
            .snapshots()
            .map { snap -> snap.toObjects(Purchase::class.java).sortedByDescending { it.purchaseDate } }
    }

    override suspend fun recordPurchase(userId: String, purchase: Purchase): String {
        val purchaseCol = db.collection("users").document(userId).collection("purchases")
        val purchaseDoc = purchaseCol.document()

        db.runTransaction { transaction ->
            // If linked to existing product, increase stock
            if (purchase.productId.isNotBlank()) {
                val prodRef = db.collection("users").document(userId).collection("products").document(purchase.productId)
                val prodSnap = transaction.get(prodRef)
                if (prodSnap.exists()) {
                    val currentStock = prodSnap.getLong("currentStock") ?: 0L
                    transaction.update(
                        prodRef,
                        "currentStock", currentStock + purchase.quantity,
                        "purchasePrice", purchase.purchasePrice
                    )
                }
            }

            val payload = mapOf(
                "userId" to userId,
                "productName" to purchase.productName,
                "productId" to purchase.productId,
                "supplierName" to purchase.supplierName,
                "quantity" to purchase.quantity,
                "purchasePrice" to purchase.purchasePrice,
                "totalAmount" to purchase.totalAmount,
                "purchaseDate" to purchase.purchaseDate,
                "paymentStatus" to purchase.paymentStatus,
                "notes" to purchase.notes,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            transaction.set(purchaseDoc, payload)
        }.await()

        return purchaseDoc.id
    }

    override suspend fun deletePurchase(userId: String, purchaseId: String) {
        val docRef = db.collection("users").document(userId).collection("purchases").document(purchaseId)
        try {
            docRef.delete().await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    // -------------------------------------------------------------
    // Expenses
    // -------------------------------------------------------------
    override fun observeExpenses(userId: String): Flow<List<Expense>> {
        return db.collection("users").document(userId).collection("expenses")
            .snapshots()
            .map { snap -> snap.toObjects(Expense::class.java).sortedByDescending { it.expenseDate } }
    }

    override suspend fun recordExpense(userId: String, expense: Expense): String {
        val colRef = db.collection("users").document(userId).collection("expenses")
        val docRef = colRef.document()
        val payload = mapOf(
            "userId" to userId,
            "title" to expense.title,
            "amount" to expense.amount,
            "category" to expense.category,
            "expenseDate" to expense.expenseDate,
            "description" to expense.description,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            return docRef.id
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, colRef.path)
            throw e
        }
    }

    override suspend fun deleteExpense(userId: String, expenseId: String) {
        val docRef = db.collection("users").document(userId).collection("expenses").document(expenseId)
        try {
            docRef.delete().await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    // -------------------------------------------------------------
    // Customer Dues & Transactions (বাকি খাতা)
    // -------------------------------------------------------------
    override fun observeCustomers(userId: String): Flow<List<Customer>> {
        return db.collection("users").document(userId).collection("customers")
            .snapshots()
            .map { snap -> snap.toObjects(Customer::class.java).sortedByDescending { it.remainingDue } }
    }

    override suspend fun addCustomer(userId: String, customer: Customer): String {
        val colRef = db.collection("users").document(userId).collection("customers")
        val docRef = colRef.document()
        val payload = mapOf(
            "userId" to userId,
            "name" to customer.name,
            "mobile" to customer.mobile,
            "address" to customer.address,
            "totalDue" to customer.totalDue,
            "totalPaid" to customer.totalPaid,
            "remainingDue" to customer.remainingDue,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            return docRef.id
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, colRef.path)
            throw e
        }
    }

    override suspend fun recordCustomerTx(
        userId: String,
        customerId: String,
        tx: CustomerTransaction
    ): String {
        val custRef = db.collection("users").document(userId).collection("customers").document(customerId)
        val txCol = custRef.collection("transactions")
        val txDoc = txCol.document()

        db.runTransaction { transaction ->
            val custSnap = transaction.get(custRef)
            val totalDue = (custSnap.getDouble("totalDue") ?: 0.0)
            val totalPaid = (custSnap.getDouble("totalPaid") ?: 0.0)

            val newTotalDue: Double
            val newTotalPaid: Double
            if (tx.type == "DUE") {
                newTotalDue = totalDue + tx.amount
                newTotalPaid = totalPaid
            } else {
                newTotalDue = totalDue
                newTotalPaid = totalPaid + tx.amount
            }
            val newRemaining = (newTotalDue - newTotalPaid).coerceAtLeast(0.0)

            transaction.update(
                custRef,
                "totalDue", newTotalDue,
                "totalPaid", newTotalPaid,
                "remainingDue", newRemaining,
                "updatedAt", FieldValue.serverTimestamp()
            )

            val txPayload = mapOf(
                "userId" to userId,
                "customerId" to customerId,
                "type" to tx.type,
                "amount" to tx.amount,
                "notes" to tx.notes,
                "transactionDate" to tx.transactionDate,
                "createdAt" to FieldValue.serverTimestamp()
            )
            transaction.set(txDoc, txPayload)
        }.await()

        return txDoc.id
    }

    override fun observeCustomerTransactions(userId: String, customerId: String): Flow<List<CustomerTransaction>> {
        return db.collection("users").document(userId)
            .collection("customers").document(customerId)
            .collection("transactions")
            .snapshots()
            .map { snap -> snap.toObjects(CustomerTransaction::class.java).sortedByDescending { it.transactionDate } }
    }

    override suspend fun deleteCustomer(userId: String, customerId: String) {
        val docRef = db.collection("users").document(userId).collection("customers").document(customerId)
        try {
            docRef.delete().await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    // -------------------------------------------------------------
    // Personal Loans (ব্যক্তিগত ধার-দেনা)
    // -------------------------------------------------------------
    override fun observeLoans(userId: String): Flow<List<Loan>> {
        return db.collection("users").document(userId).collection("loans")
            .snapshots()
            .map { snap -> snap.toObjects(Loan::class.java) }
    }

    override suspend fun addLoan(userId: String, loan: Loan): String {
        val colRef = db.collection("users").document(userId).collection("loans")
        val docRef = colRef.document()
        val payload = mapOf(
            "userId" to userId,
            "personName" to loan.personName,
            "mobile" to loan.mobile,
            "loanType" to loan.loanType,
            "amount" to loan.amount,
            "paidAmount" to loan.paidAmount,
            "remainingAmount" to (loan.amount - loan.paidAmount).coerceAtLeast(0.0),
            "description" to loan.description,
            "expectedPaymentDate" to loan.expectedPaymentDate,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            return docRef.id
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, colRef.path)
            throw e
        }
    }

    override suspend fun recordLoanPayment(userId: String, loanId: String, paymentAmount: Double) {
        val docRef = db.collection("users").document(userId).collection("loans").document(loanId)
        db.runTransaction { transaction ->
            val snap = transaction.get(docRef)
            val currentAmount = snap.getDouble("amount") ?: 0.0
            val currentPaid = snap.getDouble("paidAmount") ?: 0.0
            val newPaid = currentPaid + paymentAmount
            val newRemaining = (currentAmount - newPaid).coerceAtLeast(0.0)

            transaction.update(
                docRef,
                "paidAmount", newPaid,
                "remainingAmount", newRemaining,
                "updatedAt", FieldValue.serverTimestamp()
            )
        }.await()
    }

    override suspend fun deleteLoan(userId: String, loanId: String) {
        val docRef = db.collection("users").document(userId).collection("loans").document(loanId)
        try {
            docRef.delete().await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    // -------------------------------------------------------------
    // Mobile Banking (bKash & Nagad)
    // -------------------------------------------------------------
    override fun observeMobileBanking(userId: String): Flow<List<MobileBankingTx>> {
        return db.collection("users").document(userId).collection("mobile_banking")
            .snapshots()
            .map { snap -> snap.toObjects(MobileBankingTx::class.java).sortedByDescending { it.transactionDate } }
    }

    override suspend fun recordMobileBanking(userId: String, tx: MobileBankingTx): String {
        val colRef = db.collection("users").document(userId).collection("mobile_banking")
        val docRef = colRef.document()
        val payload = mapOf(
            "userId" to userId,
            "provider" to tx.provider,
            "type" to tx.type,
            "amount" to tx.amount,
            "commissionProfit" to tx.commissionProfit,
            "serviceCharge" to tx.serviceCharge,
            "customerPhone" to tx.customerPhone,
            "notes" to tx.notes,
            "transactionDate" to tx.transactionDate,
            "createdAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            return docRef.id
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, colRef.path)
            throw e
        }
    }

    override suspend fun deleteMobileBanking(userId: String, txId: String) {
        val docRef = db.collection("users").document(userId).collection("mobile_banking").document(txId)
        try {
            docRef.delete().await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    // -------------------------------------------------------------
    // Flexiload
    // -------------------------------------------------------------
    override fun observeFlexiload(userId: String): Flow<List<FlexiloadTx>> {
        return db.collection("users").document(userId).collection("flexiload")
            .snapshots()
            .map { snap -> snap.toObjects(FlexiloadTx::class.java).sortedByDescending { it.transactionDate } }
    }

    override suspend fun recordFlexiload(userId: String, tx: FlexiloadTx): String {
        val colRef = db.collection("users").document(userId).collection("flexiload")
        val docRef = colRef.document()
        val payload = mapOf(
            "userId" to userId,
            "mobileNumber" to tx.mobileNumber,
            "operator" to tx.operator,
            "rechargeAmount" to tx.rechargeAmount,
            "customerPayment" to tx.customerPayment,
            "rechargeCost" to tx.rechargeCost,
            "profit" to tx.profit,
            "transactionDate" to tx.transactionDate,
            "notes" to tx.notes,
            "createdAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            return docRef.id
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, colRef.path)
            throw e
        }
    }

    override suspend fun deleteFlexiload(userId: String, txId: String) {
        val docRef = db.collection("users").document(userId).collection("flexiload").document(txId)
        try {
            docRef.delete().await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    // -------------------------------------------------------------
    // Daily Closing
    // -------------------------------------------------------------
    override fun observeDailyClosings(userId: String): Flow<List<DailyClosing>> {
        return db.collection("users").document(userId).collection("daily_closings")
            .snapshots()
            .map { snap -> snap.toObjects(DailyClosing::class.java).sortedByDescending { it.closingDate } }
    }

    override suspend fun recordDailyClosing(userId: String, closing: DailyClosing): String {
        val colRef = db.collection("users").document(userId).collection("daily_closings")
        val docRef = colRef.document()
        val payload = mapOf(
            "userId" to userId,
            "closingDate" to closing.closingDate,
            "productSales" to closing.productSales,
            "mobileBankingVolume" to closing.mobileBankingVolume,
            "flexiloadVolume" to closing.flexiloadVolume,
            "grossProfit" to closing.grossProfit,
            "totalExpenses" to closing.totalExpenses,
            "customerPaymentsReceived" to closing.customerPaymentsReceived,
            "totalCashReceived" to closing.totalCashReceived,
            "totalCashPaid" to closing.totalCashPaid,
            "expectedCash" to closing.expectedCash,
            "actualCash" to closing.actualCash,
            "cashDifference" to closing.cashDifference,
            "notes" to closing.notes,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            return docRef.id
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, colRef.path)
            throw e
        }
    }

    // -------------------------------------------------------------
    // Custom Categories
    // -------------------------------------------------------------
    override fun observeCategories(userId: String): Flow<List<CustomCategory>> {
        return db.collection("users").document(userId).collection("categories")
            .snapshots()
            .map { snap -> snap.toObjects(CustomCategory::class.java) }
    }

    override suspend fun addCategory(userId: String, name: String): String {
        val colRef = db.collection("users").document(userId).collection("categories")
        val docRef = colRef.document()
        val payload = mapOf(
            "userId" to userId,
            "name" to name,
            "createdAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            return docRef.id
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, colRef.path)
            throw e
        }
    }

    override suspend fun deleteCategory(userId: String, categoryId: String) {
        val docRef = db.collection("users").document(userId).collection("categories").document(categoryId)
        try {
            docRef.delete().await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    // -------------------------------------------------------------
    // Backup & Restore
    // -------------------------------------------------------------
    override suspend fun exportBackupJson(userId: String): String {
        val products = db.collection("users").document(userId).collection("products").get().await().toObjects(Product::class.java)
        val sales = db.collection("users").document(userId).collection("sales").get().await().toObjects(Sale::class.java)
        val purchases = db.collection("users").document(userId).collection("purchases").get().await().toObjects(Purchase::class.java)
        val expenses = db.collection("users").document(userId).collection("expenses").get().await().toObjects(Expense::class.java)
        val customers = db.collection("users").document(userId).collection("customers").get().await().toObjects(Customer::class.java)
        val loans = db.collection("users").document(userId).collection("loans").get().await().toObjects(Loan::class.java)

        val root = JSONObject()
        root.put("version", 1)
        root.put("shop", "Yasin Telecom")
        root.put("timestamp", System.currentTimeMillis())

        val pArray = JSONArray()
        products.forEach { p ->
            val obj = JSONObject()
            obj.put("name", p.name)
            obj.put("category", p.category)
            obj.put("brand", p.brand)
            obj.put("model", p.model)
            obj.put("purchasePrice", p.purchasePrice)
            obj.put("sellingPrice", p.sellingPrice)
            obj.put("currentStock", p.currentStock)
            obj.put("minStockAlert", p.minStockAlert)
            pArray.put(obj)
        }
        root.put("products", pArray)

        return root.toString(2)
    }

    override suspend fun restoreBackupJson(userId: String, jsonString: String): Int {
        val root = JSONObject(jsonString)
        val pArray = root.optJSONArray("products") ?: JSONArray()
        var imported = 0
        for (i in 0 until pArray.length()) {
            val obj = pArray.getJSONObject(i)
            val p = Product(
                userId = userId,
                name = obj.getString("name"),
                category = obj.optString("category", "Other Accessories"),
                brand = obj.optString("brand", ""),
                model = obj.optString("model", ""),
                purchasePrice = obj.optDouble("purchasePrice", 0.0),
                sellingPrice = obj.optDouble("sellingPrice", 0.0),
                currentStock = obj.optLong("currentStock", 0L),
                minStockAlert = obj.optLong("minStockAlert", 5L)
            )
            addProduct(userId, p)
            imported++
        }
        return imported
    }

    // -------------------------------------------------------------
    // Admin Security & PIN
    // -------------------------------------------------------------
    override suspend fun verifyAdminPin(userId: String, pin: String): Boolean {
        return try {
            val doc = db.collection("users").document(userId).collection("profile").document("shop_info").get().await()
            val savedPin = doc.getString("adminPin") ?: "1234"
            savedPin == pin.trim()
        } catch (e: Exception) {
            pin.trim() == "1234"
        }
    }

    override suspend fun updateAdminPin(userId: String, newPin: String) {
        val path = "users/$userId/profile/shop_info"
        val docRef = db.document(path)
        try {
            docRef.update("adminPin", newPin.trim()).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            throw e
        }
    }

    // -------------------------------------------------------------
    // Smart Stock Addition
    // -------------------------------------------------------------
    override suspend fun addStock(
        userId: String,
        productId: String,
        addedQty: Long,
        purchasePrice: Double,
        supplierName: String,
        notes: String
    ): String {
        val productRef = db.collection("users").document(userId).collection("products").document(productId)
        val additionRef = db.collection("users").document(userId).collection("stock_additions").document()
        val purchaseRef = db.collection("users").document(userId).collection("purchases").document()
        val todayStr = java.text.SimpleDateFormat("dd-MM-yyyy", java.util.Locale.US).format(java.util.Date())

        db.runTransaction { tx ->
            val snap = tx.get(productRef)
            val current = snap.getLong("currentStock") ?: 0L
            val pName = snap.getString("name") ?: "পণ্য"
            val newTotal = current + addedQty

            tx.update(productRef, "currentStock", newTotal)
            if (purchasePrice > 0) {
                tx.update(productRef, "purchasePrice", purchasePrice)
            }
            tx.update(productRef, "updatedAt", FieldValue.serverTimestamp())

            val addMap = mapOf(
                "userId" to userId,
                "productId" to productId,
                "productName" to pName,
                "previousQuantity" to current,
                "addedQuantity" to addedQty,
                "totalQuantity" to newTotal,
                "purchasePrice" to purchasePrice,
                "dateAdded" to todayStr,
                "supplierName" to supplierName,
                "notes" to notes,
                "isDemo" to false,
                "createdAt" to FieldValue.serverTimestamp()
            )
            tx.set(additionRef, addMap)

            val purchaseMap = mapOf(
                "userId" to userId,
                "productId" to productId,
                "productName" to pName,
                "supplierName" to supplierName,
                "quantity" to addedQty,
                "purchasePrice" to purchasePrice,
                "totalAmount" to (addedQty * purchasePrice),
                "purchaseDate" to todayStr,
                "paymentStatus" to "PAID",
                "notes" to notes,
                "isDemo" to false,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            tx.set(purchaseRef, purchaseMap)
        }.await()

        return additionRef.id
    }

    override fun observeStockAdditions(userId: String): Flow<List<StockAddition>> {
        return db.collection("users").document(userId).collection("stock_additions")
            .snapshots().map { snap -> snap.toObjects(StockAddition::class.java) }
    }

    // -------------------------------------------------------------
    // Cash Memos
    // -------------------------------------------------------------
    override fun observeCashMemos(userId: String): Flow<List<CashMemo>> {
        return db.collection("users").document(userId).collection("cash_memos")
            .snapshots().map { snap -> snap.toObjects(CashMemo::class.java) }
    }

    override suspend fun saveCashMemo(userId: String, memo: CashMemo, autoRecordSale: Boolean): String {
        val memoRef = db.collection("users").document(userId).collection("cash_memos").document()
        var saleId = ""

        if (autoRecordSale) {
            val saleItems = memo.items.map { item ->
                SaleItem(
                    productId = item.productId,
                    productName = item.description,
                    quantity = item.quantity,
                    sellingPrice = item.rate,
                    subtotal = item.amount,
                    purchaseCost = 0.0,
                    profit = 0.0
                )
            }
            val sale = Sale(
                userId = userId,
                invoiceNumber = memo.memoNumber,
                customerName = memo.customerName,
                customerPhone = memo.customerMobile,
                itemsSummary = memo.items.joinToString(", ") { "${it.description} x${it.quantity}" },
                totalAmount = memo.grandTotal,
                paidAmount = memo.paidAmount,
                dueAmount = memo.dueAmount,
                saleDate = memo.date,
                notes = memo.notes,
                isDemo = false
            )
            saleId = recordSale(userId, sale, saleItems)

            if (memo.dueAmount > 0 && memo.customerName.isNotBlank()) {
                val existing = db.collection("users").document(userId).collection("customers")
                    .whereEqualTo("name", memo.customerName).get().await()
                val customerId = if (!existing.isEmpty) {
                    existing.documents[0].id
                } else {
                    addCustomer(userId, Customer(
                        userId = userId,
                        name = memo.customerName,
                        mobile = memo.customerMobile,
                        address = memo.customerAddress,
                        totalDue = 0.0,
                        remainingDue = 0.0
                    ))
                }
                recordCustomerTx(userId, customerId, CustomerTransaction(
                    userId = userId,
                    customerId = customerId,
                    type = "DUE",
                    amount = memo.dueAmount,
                    notes = "ক্যাশ মেমো নং: ${memo.memoNumber}",
                    transactionDate = memo.date
                ))
            }
        }

        val memoPayload = mapOf(
            "userId" to userId,
            "memoNumber" to memo.memoNumber,
            "date" to memo.date,
            "time" to memo.time,
            "customerName" to memo.customerName,
            "customerMobile" to memo.customerMobile,
            "customerAddress" to memo.customerAddress,
            "items" to memo.items.map { mapOf("productId" to it.productId, "description" to it.description, "quantity" to it.quantity, "rate" to it.rate, "amount" to it.amount) },
            "subtotal" to memo.subtotal,
            "discount" to memo.discount,
            "grandTotal" to memo.grandTotal,
            "paidAmount" to memo.paidAmount,
            "dueAmount" to memo.dueAmount,
            "paymentStatus" to memo.paymentStatus,
            "notes" to memo.notes,
            "saleId" to saleId,
            "isDemo" to false,
            "createdAt" to FieldValue.serverTimestamp()
        )
        memoRef.set(memoPayload).await()
        return memoRef.id
    }

    override suspend fun deleteCashMemo(userId: String, memoId: String) {
        val path = "users/$userId/cash_memos/$memoId"
        try {
            db.document(path).delete().await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            throw e
        }
    }

    // -------------------------------------------------------------
    // Reset Demo Data (Deletes ONLY sample/demo records, preserves genuine data)
    // -------------------------------------------------------------
    override suspend fun resetDemoData(userId: String): Int {
        var removedCount = 0
        val collections = listOf(
            "products", "sales", "purchases", "expenses",
            "customers", "loans", "mobile_banking", "flexiload",
            "daily_closings", "cash_memos", "stock_additions"
        )
        for (colName in collections) {
            val colRef = db.collection("users").document(userId).collection(colName)
            val demoDocs = colRef.whereEqualTo("isDemo", true).get().await()
            for (doc in demoDocs.documents) {
                doc.reference.delete().await()
                removedCount++
            }
        }
        return removedCount
    }
}

