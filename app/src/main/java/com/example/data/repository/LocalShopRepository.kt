package com.example.data.repository

import android.content.Context
import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class LocalShopRepository(private val context: Context) : ShopDataSource {

    private val storageFile: File by lazy {
        context.filesDir.resolve("yasin_telecom_offline.json")
    }

    private val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.US)
    private val todayStr: String
        get() = dateFormat.format(Date())

    private val profileState = MutableStateFlow<ShopProfile?>(
        ShopProfile(
            userId = "local_guest_owner",
            shopName = "ইয়াছিন টেলিকম",
            ownerName = "মোহাম্মদ ইয়াছিন",
            address = "বুল্লা বাজার, লাখাই, হবিগঞ্জ",
            phone = "01766906974",
            tagline = "এখানে যাবতীয় পাইকারী ও খুচরা মালামাল এবং ডিজিটাল সেবা সুলভ মূল্যে প্রদান করা হয়।",
            currency = "৳",
            lowStockAlertThreshold = 5L
        )
    )

    private val productsState = MutableStateFlow<List<Product>>(emptyList())
    private val salesState = MutableStateFlow<List<Sale>>(emptyList())
    private val purchasesState = MutableStateFlow<List<Purchase>>(emptyList())
    private val expensesState = MutableStateFlow<List<Expense>>(emptyList())
    private val customersState = MutableStateFlow<List<Customer>>(emptyList())
    private val customerTxState = MutableStateFlow<Map<String, List<CustomerTransaction>>>(emptyMap())
    private val loansState = MutableStateFlow<List<Loan>>(emptyList())
    private val mobileBankingState = MutableStateFlow<List<MobileBankingTx>>(emptyList())
    private val flexiloadState = MutableStateFlow<List<FlexiloadTx>>(emptyList())
    private val closingsState = MutableStateFlow<List<DailyClosing>>(emptyList())
    private val categoriesState = MutableStateFlow<List<CustomCategory>>(emptyList())
    private val stockAdditionsState = MutableStateFlow<List<StockAddition>>(emptyList())
    private val cashMemosState = MutableStateFlow<List<CashMemo>>(emptyList())

    init {
        loadFromDisk()
    }

    private fun loadFromDisk() {
        try {
            if (storageFile.exists()) {
                val jsonStr = storageFile.readText()
                if (jsonStr.isNotBlank()) {
                    val root = JSONObject(jsonStr)

                    // Profile
                    if (root.has("profile")) {
                        val pObj = root.getJSONObject("profile")
                        profileState.value = ShopProfile(
                            userId = pObj.optString("userId", "local_guest_owner"),
                            shopName = pObj.optString("shopName", "ইয়াসিন টেলিকম"),
                            ownerName = pObj.optString("ownerName", "মোহাম্মদ ইয়াসিন"),
                            address = pObj.optString("address", "বুল্লা বাজার, লাখাই, হবিগঞ্জ"),
                            phone = pObj.optString("phone", "01700000000"),
                            currency = pObj.optString("currency", "৳"),
                            lowStockAlertThreshold = pObj.optLong("lowStockAlertThreshold", 5L)
                        )
                    }

                    // Products
                    val pList = mutableListOf<Product>()
                    val pArray = root.optJSONArray("products") ?: JSONArray()
                    for (i in 0 until pArray.length()) {
                        val o = pArray.getJSONObject(i)
                        pList.add(
                            Product(
                                id = o.optString("id", UUID.randomUUID().toString()),
                                userId = o.optString("userId", "local_guest_owner"),
                                name = o.getString("name"),
                                category = o.optString("category", "Other Accessories"),
                                brand = o.optString("brand", ""),
                                model = o.optString("model", ""),
                                purchasePrice = o.optDouble("purchasePrice", 0.0),
                                sellingPrice = o.optDouble("sellingPrice", 0.0),
                                currentStock = o.optLong("currentStock", 0L),
                                minStockAlert = o.optLong("minStockAlert", 5L),
                                productCode = o.optString("productCode", ""),
                                notes = o.optString("notes", "")
                            )
                        )
                    }
                    productsState.value = pList

                    // Sales
                    val sList = mutableListOf<Sale>()
                    val sArray = root.optJSONArray("sales") ?: JSONArray()
                    for (i in 0 until sArray.length()) {
                        val o = sArray.getJSONObject(i)
                        sList.add(
                            Sale(
                                id = o.optString("id", UUID.randomUUID().toString()),
                                userId = o.optString("userId", "local_guest_owner"),
                                invoiceNumber = o.optString("invoiceNumber", ""),
                                customerName = o.optString("customerName", ""),
                                customerPhone = o.optString("customerPhone", ""),
                                itemsSummary = o.optString("itemsSummary", ""),
                                totalAmount = o.optDouble("totalAmount", 0.0),
                                totalCost = o.optDouble("totalCost", 0.0),
                                grossProfit = o.optDouble("grossProfit", 0.0),
                                paidAmount = o.optDouble("paidAmount", 0.0),
                                dueAmount = o.optDouble("dueAmount", 0.0),
                                saleDate = o.optString("saleDate", todayStr),
                                notes = o.optString("notes", "")
                            )
                        )
                    }
                    salesState.value = sList

                    // Purchases
                    val purList = mutableListOf<Purchase>()
                    val purArray = root.optJSONArray("purchases") ?: JSONArray()
                    for (i in 0 until purArray.length()) {
                        val o = purArray.getJSONObject(i)
                        purList.add(
                            Purchase(
                                id = o.optString("id", UUID.randomUUID().toString()),
                                userId = o.optString("userId", "local_guest_owner"),
                                productName = o.getString("productName"),
                                productId = o.optString("productId", ""),
                                supplierName = o.optString("supplierName", ""),
                                quantity = o.optLong("quantity", 0L),
                                purchasePrice = o.optDouble("purchasePrice", 0.0),
                                totalAmount = o.optDouble("totalAmount", 0.0),
                                purchaseDate = o.optString("purchaseDate", todayStr),
                                paymentStatus = o.optString("paymentStatus", "PAID"),
                                notes = o.optString("notes", "")
                            )
                        )
                    }
                    purchasesState.value = purList

                    // Expenses
                    val expList = mutableListOf<Expense>()
                    val expArray = root.optJSONArray("expenses") ?: JSONArray()
                    for (i in 0 until expArray.length()) {
                        val o = expArray.getJSONObject(i)
                        expList.add(
                            Expense(
                                id = o.optString("id", UUID.randomUUID().toString()),
                                userId = o.optString("userId", "local_guest_owner"),
                                title = o.getString("title"),
                                amount = o.optDouble("amount", 0.0),
                                category = o.optString("category", "Other Expenses"),
                                expenseDate = o.optString("expenseDate", todayStr),
                                description = o.optString("description", "")
                            )
                        )
                    }
                    expensesState.value = expList

                    // Customers
                    val custList = mutableListOf<Customer>()
                    val custArray = root.optJSONArray("customers") ?: JSONArray()
                    for (i in 0 until custArray.length()) {
                        val o = custArray.getJSONObject(i)
                        custList.add(
                            Customer(
                                id = o.optString("id", UUID.randomUUID().toString()),
                                userId = o.optString("userId", "local_guest_owner"),
                                name = o.getString("name"),
                                mobile = o.optString("mobile", ""),
                                address = o.optString("address", ""),
                                totalDue = o.optDouble("totalDue", 0.0),
                                totalPaid = o.optDouble("totalPaid", 0.0),
                                remainingDue = o.optDouble("remainingDue", 0.0)
                            )
                        )
                    }
                    customersState.value = custList

                    // Customer Transactions
                    val txMap = mutableMapOf<String, MutableList<CustomerTransaction>>()
                    val txArray = root.optJSONArray("customerTransactions") ?: JSONArray()
                    for (i in 0 until txArray.length()) {
                        val o = txArray.getJSONObject(i)
                        val cId = o.optString("customerId", "")
                        val tx = CustomerTransaction(
                            id = o.optString("id", UUID.randomUUID().toString()),
                            userId = o.optString("userId", "local_guest_owner"),
                            customerId = cId,
                            type = o.optString("type", "DUE"),
                            amount = o.optDouble("amount", 0.0),
                            notes = o.optString("notes", ""),
                            transactionDate = o.optString("transactionDate", todayStr)
                        )
                        txMap.getOrPut(cId) { mutableListOf() }.add(tx)
                    }
                    customerTxState.value = txMap

                    // Loans
                    val loanList = mutableListOf<Loan>()
                    val loanArray = root.optJSONArray("loans") ?: JSONArray()
                    for (i in 0 until loanArray.length()) {
                        val o = loanArray.getJSONObject(i)
                        loanList.add(
                            Loan(
                                id = o.optString("id", UUID.randomUUID().toString()),
                                userId = o.optString("userId", "local_guest_owner"),
                                personName = o.getString("personName"),
                                mobile = o.optString("mobile", ""),
                                loanType = o.optString("loanType", "LENT"),
                                amount = o.optDouble("amount", 0.0),
                                paidAmount = o.optDouble("paidAmount", 0.0),
                                remainingAmount = o.optDouble("remainingAmount", 0.0),
                                description = o.optString("description", ""),
                                expectedPaymentDate = o.optString("expectedPaymentDate", "")
                            )
                        )
                    }
                    loansState.value = loanList

                    // Mobile Banking
                    val mbList = mutableListOf<MobileBankingTx>()
                    val mbArray = root.optJSONArray("mobileBanking") ?: JSONArray()
                    for (i in 0 until mbArray.length()) {
                        val o = mbArray.getJSONObject(i)
                        mbList.add(
                            MobileBankingTx(
                                id = o.optString("id", UUID.randomUUID().toString()),
                                userId = o.optString("userId", "local_guest_owner"),
                                provider = o.optString("provider", "BKASH"),
                                type = o.optString("type", "CASH_IN"),
                                amount = o.optDouble("amount", 0.0),
                                commissionProfit = o.optDouble("commissionProfit", 0.0),
                                serviceCharge = o.optDouble("serviceCharge", 0.0),
                                customerPhone = o.optString("customerPhone", ""),
                                notes = o.optString("notes", ""),
                                transactionDate = o.optString("transactionDate", todayStr)
                            )
                        )
                    }
                    mobileBankingState.value = mbList

                    // Flexiload
                    val flList = mutableListOf<FlexiloadTx>()
                    val flArray = root.optJSONArray("flexiload") ?: JSONArray()
                    for (i in 0 until flArray.length()) {
                        val o = flArray.getJSONObject(i)
                        flList.add(
                            FlexiloadTx(
                                id = o.optString("id", UUID.randomUUID().toString()),
                                userId = o.optString("userId", "local_guest_owner"),
                                mobileNumber = o.optString("mobileNumber", ""),
                                operator = o.optString("operator", "Grameenphone"),
                                rechargeAmount = o.optDouble("rechargeAmount", 0.0),
                                customerPayment = o.optDouble("customerPayment", 0.0),
                                rechargeCost = o.optDouble("rechargeCost", 0.0),
                                profit = o.optDouble("profit", 0.0),
                                transactionDate = o.optString("transactionDate", todayStr),
                                notes = o.optString("notes", "")
                            )
                        )
                    }
                    flexiloadState.value = flList

                    // Daily Closings
                    val dcList = mutableListOf<DailyClosing>()
                    val dcArray = root.optJSONArray("dailyClosings") ?: JSONArray()
                    for (i in 0 until dcArray.length()) {
                        val o = dcArray.getJSONObject(i)
                        dcList.add(
                            DailyClosing(
                                id = o.optString("id", UUID.randomUUID().toString()),
                                userId = o.optString("userId", "local_guest_owner"),
                                closingDate = o.optString("closingDate", todayStr),
                                productSales = o.optDouble("productSales", 0.0),
                                mobileBankingVolume = o.optDouble("mobileBankingVolume", 0.0),
                                flexiloadVolume = o.optDouble("flexiloadVolume", 0.0),
                                grossProfit = o.optDouble("grossProfit", 0.0),
                                totalExpenses = o.optDouble("totalExpenses", 0.0),
                                customerPaymentsReceived = o.optDouble("customerPaymentsReceived", 0.0),
                                totalCashReceived = o.optDouble("totalCashReceived", 0.0),
                                totalCashPaid = o.optDouble("totalCashPaid", 0.0),
                                expectedCash = o.optDouble("expectedCash", 0.0),
                                actualCash = o.optDouble("actualCash", 0.0),
                                cashDifference = o.optDouble("cashDifference", 0.0),
                                notes = o.optString("notes", "")
                            )
                        )
                    }
                    closingsState.value = dcList

                    // Categories
                    val catList = mutableListOf<CustomCategory>()
                    val catArray = root.optJSONArray("categories") ?: JSONArray()
                    for (i in 0 until catArray.length()) {
                        val o = catArray.getJSONObject(i)
                        catList.add(
                            CustomCategory(
                                id = o.optString("id", UUID.randomUUID().toString()),
                                userId = o.optString("userId", "local_guest_owner"),
                                name = o.optString("name", "")
                            )
                        )
                    }
                    categoriesState.value = catList
                    return
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Default initial seed data for Yasin Telecom if fresh installation
        seedDefaultData()
    }

    private fun seedDefaultData() {
        val initialProducts = listOf(
            Product(
                id = UUID.randomUUID().toString(),
                userId = "local_guest_owner",
                name = "Samsung 25W Fast Charger Type-C",
                category = "Mobile Chargers",
                brand = "Samsung",
                model = "EP-TA800",
                purchasePrice = 350.0,
                sellingPrice = 550.0,
                currentStock = 12L,
                minStockAlert = 3L
            ),
            Product(
                id = UUID.randomUUID().toString(),
                userId = "local_guest_owner",
                name = "9D Curved Tempered Glass",
                category = "Tempered Glass",
                brand = "Universal",
                model = "All Models",
                purchasePrice = 35.0,
                sellingPrice = 100.0,
                currentStock = 45L,
                minStockAlert = 10L
            ),
            Product(
                id = UUID.randomUUID().toString(),
                userId = "local_guest_owner",
                name = "Realme 33W Dart Charger",
                category = "Mobile Chargers",
                brand = "Realme",
                model = "VC56JACH",
                purchasePrice = 420.0,
                sellingPrice = 650.0,
                currentStock = 8L,
                minStockAlert = 3L
            ),
            Product(
                id = UUID.randomUUID().toString(),
                userId = "local_guest_owner",
                name = "Original Type-C Fast Data Cable 1M",
                category = "Data Cables",
                brand = "Remax",
                model = "RC-138a",
                purchasePrice = 60.0,
                sellingPrice = 150.0,
                currentStock = 25L,
                minStockAlert = 5L
            ),
            Product(
                id = UUID.randomUUID().toString(),
                userId = "local_guest_owner",
                name = "Mi 10000mAh Power Bank 3i",
                category = "Power Banks",
                brand = "Xiaomi",
                model = "PB100DZM",
                purchasePrice = 1150.0,
                sellingPrice = 1450.0,
                currentStock = 4L,
                minStockAlert = 2L
            ),
            Product(
                id = UUID.randomUUID().toString(),
                userId = "local_guest_owner",
                name = "SanDisk 64GB Ultra MicroSD Card",
                category = "Memory Cards",
                brand = "SanDisk",
                model = "Class 10 A1",
                purchasePrice = 480.0,
                sellingPrice = 650.0,
                currentStock = 15L,
                minStockAlert = 4L
            )
        )
        productsState.value = initialProducts

        val initialCategories = listOf(
            CustomCategory(id = UUID.randomUUID().toString(), userId = "local_guest_owner", name = "Smart Watch"),
            CustomCategory(id = UUID.randomUUID().toString(), userId = "local_guest_owner", name = "AirDots & TWS")
        )
        categoriesState.value = initialCategories

        saveToDisk()
    }

    @Synchronized
    private fun saveToDisk() {
        try {
            val root = JSONObject()

            // Profile
            profileState.value?.let { p ->
                val pObj = JSONObject()
                pObj.put("userId", p.userId)
                pObj.put("shopName", p.shopName)
                pObj.put("ownerName", p.ownerName)
                pObj.put("address", p.address)
                pObj.put("phone", p.phone)
                pObj.put("currency", p.currency)
                pObj.put("lowStockAlertThreshold", p.lowStockAlertThreshold)
                root.put("profile", pObj)
            }

            // Products
            val pArray = JSONArray()
            for (p in productsState.value) {
                val o = JSONObject()
                o.put("id", p.id)
                o.put("userId", p.userId)
                o.put("name", p.name)
                o.put("category", p.category)
                o.put("brand", p.brand)
                o.put("model", p.model)
                o.put("purchasePrice", p.purchasePrice)
                o.put("sellingPrice", p.sellingPrice)
                o.put("currentStock", p.currentStock)
                o.put("minStockAlert", p.minStockAlert)
                o.put("productCode", p.productCode)
                o.put("notes", p.notes)
                pArray.put(o)
            }
            root.put("products", pArray)

            // Sales
            val sArray = JSONArray()
            for (s in salesState.value) {
                val o = JSONObject()
                o.put("id", s.id)
                o.put("userId", s.userId)
                o.put("invoiceNumber", s.invoiceNumber)
                o.put("customerName", s.customerName)
                o.put("customerPhone", s.customerPhone)
                o.put("itemsSummary", s.itemsSummary)
                o.put("totalAmount", s.totalAmount)
                o.put("totalCost", s.totalCost)
                o.put("grossProfit", s.grossProfit)
                o.put("paidAmount", s.paidAmount)
                o.put("dueAmount", s.dueAmount)
                o.put("saleDate", s.saleDate)
                o.put("notes", s.notes)
                sArray.put(o)
            }
            root.put("sales", sArray)

            // Purchases
            val purArray = JSONArray()
            for (pur in purchasesState.value) {
                val o = JSONObject()
                o.put("id", pur.id)
                o.put("userId", pur.userId)
                o.put("productName", pur.productName)
                o.put("productId", pur.productId)
                o.put("supplierName", pur.supplierName)
                o.put("quantity", pur.quantity)
                o.put("purchasePrice", pur.purchasePrice)
                o.put("totalAmount", pur.totalAmount)
                o.put("purchaseDate", pur.purchaseDate)
                o.put("paymentStatus", pur.paymentStatus)
                o.put("notes", pur.notes)
                purArray.put(o)
            }
            root.put("purchases", purArray)

            // Expenses
            val expArray = JSONArray()
            for (exp in expensesState.value) {
                val o = JSONObject()
                o.put("id", exp.id)
                o.put("userId", exp.userId)
                o.put("title", exp.title)
                o.put("amount", exp.amount)
                o.put("category", exp.category)
                o.put("expenseDate", exp.expenseDate)
                o.put("description", exp.description)
                expArray.put(o)
            }
            root.put("expenses", expArray)

            // Customers
            val custArray = JSONArray()
            for (c in customersState.value) {
                val o = JSONObject()
                o.put("id", c.id)
                o.put("userId", c.userId)
                o.put("name", c.name)
                o.put("mobile", c.mobile)
                o.put("address", c.address)
                o.put("totalDue", c.totalDue)
                o.put("totalPaid", c.totalPaid)
                o.put("remainingDue", c.remainingDue)
                custArray.put(o)
            }
            root.put("customers", custArray)

            // Customer Transactions
            val txArray = JSONArray()
            customerTxState.value.values.flatten().forEach { tx ->
                val o = JSONObject()
                o.put("id", tx.id)
                o.put("userId", tx.userId)
                o.put("customerId", tx.customerId)
                o.put("type", tx.type)
                o.put("amount", tx.amount)
                o.put("notes", tx.notes)
                o.put("transactionDate", tx.transactionDate)
                txArray.put(o)
            }
            root.put("customerTransactions", txArray)

            // Loans
            val loanArray = JSONArray()
            for (l in loansState.value) {
                val o = JSONObject()
                o.put("id", l.id)
                o.put("userId", l.userId)
                o.put("personName", l.personName)
                o.put("mobile", l.mobile)
                o.put("loanType", l.loanType)
                o.put("amount", l.amount)
                o.put("paidAmount", l.paidAmount)
                o.put("remainingAmount", l.remainingAmount)
                o.put("description", l.description)
                o.put("expectedPaymentDate", l.expectedPaymentDate)
                loanArray.put(o)
            }
            root.put("loans", loanArray)

            // Mobile Banking
            val mbArray = JSONArray()
            for (mb in mobileBankingState.value) {
                val o = JSONObject()
                o.put("id", mb.id)
                o.put("userId", mb.userId)
                o.put("provider", mb.provider)
                o.put("type", mb.type)
                o.put("amount", mb.amount)
                o.put("commissionProfit", mb.commissionProfit)
                o.put("serviceCharge", mb.serviceCharge)
                o.put("customerPhone", mb.customerPhone)
                o.put("notes", mb.notes)
                o.put("transactionDate", mb.transactionDate)
                mbArray.put(o)
            }
            root.put("mobileBanking", mbArray)

            // Flexiload
            val flArray = JSONArray()
            for (fl in flexiloadState.value) {
                val o = JSONObject()
                o.put("id", fl.id)
                o.put("userId", fl.userId)
                o.put("mobileNumber", fl.mobileNumber)
                o.put("operator", fl.operator)
                o.put("rechargeAmount", fl.rechargeAmount)
                o.put("customerPayment", fl.customerPayment)
                o.put("rechargeCost", fl.rechargeCost)
                o.put("profit", fl.profit)
                o.put("transactionDate", fl.transactionDate)
                o.put("notes", fl.notes)
                flArray.put(o)
            }
            root.put("flexiload", flArray)

            // Daily Closings
            val dcArray = JSONArray()
            for (dc in closingsState.value) {
                val o = JSONObject()
                o.put("id", dc.id)
                o.put("userId", dc.userId)
                o.put("closingDate", dc.closingDate)
                o.put("productSales", dc.productSales)
                o.put("mobileBankingVolume", dc.mobileBankingVolume)
                o.put("flexiloadVolume", dc.flexiloadVolume)
                o.put("grossProfit", dc.grossProfit)
                o.put("totalExpenses", dc.totalExpenses)
                o.put("customerPaymentsReceived", dc.customerPaymentsReceived)
                o.put("totalCashReceived", dc.totalCashReceived)
                o.put("totalCashPaid", dc.totalCashPaid)
                o.put("expectedCash", dc.expectedCash)
                o.put("actualCash", dc.actualCash)
                o.put("cashDifference", dc.cashDifference)
                o.put("notes", dc.notes)
                dcArray.put(o)
            }
            root.put("dailyClosings", dcArray)

            // Categories
            val catArray = JSONArray()
            for (c in categoriesState.value) {
                val o = JSONObject()
                o.put("id", c.id)
                o.put("userId", c.userId)
                o.put("name", c.name)
                catArray.put(o)
            }
            root.put("categories", catArray)

            storageFile.writeText(root.toString(2))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun observeProfile(userId: String): Flow<ShopProfile?> = profileState.asStateFlow()

    override suspend fun saveProfile(userId: String, profile: ShopProfile) = withContext(Dispatchers.IO) {
        profileState.value = profile.copy(userId = userId)
        saveToDisk()
    }

    override fun observeProducts(userId: String): Flow<List<Product>> = productsState.asStateFlow()

    override suspend fun addProduct(userId: String, product: Product): String = withContext(Dispatchers.IO) {
        val newId = if (product.id.isNotBlank()) product.id else UUID.randomUUID().toString()
        val item = product.copy(id = newId, userId = userId)
        productsState.value = productsState.value + item
        saveToDisk()
        newId
    }

    override suspend fun updateProduct(userId: String, product: Product) = withContext(Dispatchers.IO) {
        productsState.value = productsState.value.map {
            if (it.id == product.id) product.copy(userId = userId) else it
        }
        saveToDisk()
    }

    override suspend fun deleteProduct(userId: String, productId: String) = withContext(Dispatchers.IO) {
        productsState.value = productsState.value.filterNot { it.id == productId }
        saveToDisk()
    }

    override fun observeSales(userId: String): Flow<List<Sale>> = salesState.asStateFlow()

    override suspend fun recordSale(userId: String, sale: Sale, items: List<SaleItem>): String = withContext(Dispatchers.IO) {
        // Validate stock
        val currentProducts = productsState.value.associateBy { it.id }.toMutableMap()
        for (item in items) {
            val p = currentProducts[item.productId]
                ?: throw IllegalStateException("পণ্য পাওয়া যায়নি: ${item.productName}")
            if (p.currentStock < item.quantity) {
                throw IllegalStateException("${p.name}-এর পর্যাপ্ত স্টক নেই! বর্তমান স্টক: ${p.currentStock}")
            }
        }

        // Deduct stock
        for (item in items) {
            val p = currentProducts[item.productId]!!
            currentProducts[item.productId] = p.copy(currentStock = p.currentStock - item.quantity)
        }
        productsState.value = currentProducts.values.toList()

        val saleId = if (sale.id.isNotBlank()) sale.id else UUID.randomUUID().toString()
        val newSale = sale.copy(
            id = saleId,
            userId = userId,
            saleDate = if (sale.saleDate.isNotBlank()) sale.saleDate else todayStr
        )
        salesState.value = listOf(newSale) + salesState.value
        saveToDisk()
        saleId
    }

    override suspend fun deleteSale(userId: String, saleId: String) = withContext(Dispatchers.IO) {
        salesState.value = salesState.value.filterNot { it.id == saleId }
        saveToDisk()
    }

    override fun observePurchases(userId: String): Flow<List<Purchase>> = purchasesState.asStateFlow()

    override suspend fun recordPurchase(userId: String, purchase: Purchase): String = withContext(Dispatchers.IO) {
        val purchaseId = if (purchase.id.isNotBlank()) purchase.id else UUID.randomUUID().toString()
        val newPurchase = purchase.copy(
            id = purchaseId,
            userId = userId,
            purchaseDate = if (purchase.purchaseDate.isNotBlank()) purchase.purchaseDate else todayStr
        )
        purchasesState.value = listOf(newPurchase) + purchasesState.value

        // Increase product stock if productId matches
        if (purchase.productId.isNotBlank()) {
            productsState.value = productsState.value.map { p ->
                if (p.id == purchase.productId) {
                    p.copy(
                        currentStock = p.currentStock + purchase.quantity,
                        purchasePrice = if (purchase.purchasePrice > 0) purchase.purchasePrice else p.purchasePrice
                    )
                } else p
            }
        }
        saveToDisk()
        purchaseId
    }

    override suspend fun deletePurchase(userId: String, purchaseId: String) = withContext(Dispatchers.IO) {
        purchasesState.value = purchasesState.value.filterNot { it.id == purchaseId }
        saveToDisk()
    }

    override fun observeExpenses(userId: String): Flow<List<Expense>> = expensesState.asStateFlow()

    override suspend fun recordExpense(userId: String, expense: Expense): String = withContext(Dispatchers.IO) {
        val expId = if (expense.id.isNotBlank()) expense.id else UUID.randomUUID().toString()
        val newExp = expense.copy(
            id = expId,
            userId = userId,
            expenseDate = if (expense.expenseDate.isNotBlank()) expense.expenseDate else todayStr
        )
        expensesState.value = listOf(newExp) + expensesState.value
        saveToDisk()
        expId
    }

    override suspend fun deleteExpense(userId: String, expenseId: String) = withContext(Dispatchers.IO) {
        expensesState.value = expensesState.value.filterNot { it.id == expenseId }
        saveToDisk()
    }

    override fun observeCustomers(userId: String): Flow<List<Customer>> =
        customersState.map { list -> list.sortedByDescending { it.remainingDue } }

    override suspend fun addCustomer(userId: String, customer: Customer): String = withContext(Dispatchers.IO) {
        val cId = if (customer.id.isNotBlank()) customer.id else UUID.randomUUID().toString()
        val newC = customer.copy(
            id = cId,
            userId = userId,
            remainingDue = (customer.totalDue - customer.totalPaid).coerceAtLeast(0.0)
        )
        customersState.value = listOf(newC) + customersState.value
        saveToDisk()
        cId
    }

    override suspend fun recordCustomerTx(userId: String, customerId: String, tx: CustomerTransaction): String = withContext(Dispatchers.IO) {
        val currentCust = customersState.value.find { it.id == customerId }
            ?: throw IllegalStateException("Customer not found")

        val newTotalDue: Double
        val newTotalPaid: Double
        if (tx.type == "DUE") {
            newTotalDue = currentCust.totalDue + tx.amount
            newTotalPaid = currentCust.totalPaid
        } else {
            newTotalDue = currentCust.totalDue
            newTotalPaid = currentCust.totalPaid + tx.amount
        }
        val newRemaining = (newTotalDue - newTotalPaid).coerceAtLeast(0.0)

        customersState.value = customersState.value.map {
            if (it.id == customerId) {
                it.copy(totalDue = newTotalDue, totalPaid = newTotalPaid, remainingDue = newRemaining)
            } else it
        }

        val txId = if (tx.id.isNotBlank()) tx.id else UUID.randomUUID().toString()
        val newTx = tx.copy(
            id = txId,
            userId = userId,
            customerId = customerId,
            transactionDate = if (tx.transactionDate.isNotBlank()) tx.transactionDate else todayStr
        )
        val currentMap = customerTxState.value.toMutableMap()
        val list = currentMap.getOrPut(customerId) { emptyList() }
        currentMap[customerId] = listOf(newTx) + list
        customerTxState.value = currentMap

        saveToDisk()
        txId
    }

    override fun observeCustomerTransactions(userId: String, customerId: String): Flow<List<CustomerTransaction>> =
        customerTxState.map { map ->
            (map[customerId] ?: emptyList()).sortedByDescending { it.transactionDate }
        }

    override suspend fun deleteCustomer(userId: String, customerId: String) = withContext(Dispatchers.IO) {
        customersState.value = customersState.value.filterNot { it.id == customerId }
        val currentMap = customerTxState.value.toMutableMap()
        currentMap.remove(customerId)
        customerTxState.value = currentMap
        saveToDisk()
    }

    override fun observeLoans(userId: String): Flow<List<Loan>> = loansState.asStateFlow()

    override suspend fun addLoan(userId: String, loan: Loan): String = withContext(Dispatchers.IO) {
        val loanId = if (loan.id.isNotBlank()) loan.id else UUID.randomUUID().toString()
        val newLoan = loan.copy(
            id = loanId,
            userId = userId,
            remainingAmount = (loan.amount - loan.paidAmount).coerceAtLeast(0.0)
        )
        loansState.value = listOf(newLoan) + loansState.value
        saveToDisk()
        loanId
    }

    override suspend fun recordLoanPayment(userId: String, loanId: String, paymentAmount: Double) = withContext(Dispatchers.IO) {
        loansState.value = loansState.value.map { l ->
            if (l.id == loanId) {
                val newPaid = l.paidAmount + paymentAmount
                val newRemaining = (l.amount - newPaid).coerceAtLeast(0.0)
                l.copy(paidAmount = newPaid, remainingAmount = newRemaining)
            } else l
        }
        saveToDisk()
    }

    override suspend fun deleteLoan(userId: String, loanId: String) = withContext(Dispatchers.IO) {
        loansState.value = loansState.value.filterNot { it.id == loanId }
        saveToDisk()
    }

    override fun observeMobileBanking(userId: String): Flow<List<MobileBankingTx>> =
        mobileBankingState.map { it.sortedByDescending { tx -> tx.transactionDate } }

    override suspend fun recordMobileBanking(userId: String, tx: MobileBankingTx): String = withContext(Dispatchers.IO) {
        val txId = if (tx.id.isNotBlank()) tx.id else UUID.randomUUID().toString()
        val newTx = tx.copy(
            id = txId,
            userId = userId,
            transactionDate = if (tx.transactionDate.isNotBlank()) tx.transactionDate else todayStr
        )
        mobileBankingState.value = listOf(newTx) + mobileBankingState.value
        saveToDisk()
        txId
    }

    override suspend fun deleteMobileBanking(userId: String, txId: String) = withContext(Dispatchers.IO) {
        mobileBankingState.value = mobileBankingState.value.filterNot { it.id == txId }
        saveToDisk()
    }

    override fun observeFlexiload(userId: String): Flow<List<FlexiloadTx>> =
        flexiloadState.map { it.sortedByDescending { tx -> tx.transactionDate } }

    override suspend fun recordFlexiload(userId: String, tx: FlexiloadTx): String = withContext(Dispatchers.IO) {
        val txId = if (tx.id.isNotBlank()) tx.id else UUID.randomUUID().toString()
        val newTx = tx.copy(
            id = txId,
            userId = userId,
            transactionDate = if (tx.transactionDate.isNotBlank()) tx.transactionDate else todayStr
        )
        flexiloadState.value = listOf(newTx) + flexiloadState.value
        saveToDisk()
        txId
    }

    override suspend fun deleteFlexiload(userId: String, txId: String) = withContext(Dispatchers.IO) {
        flexiloadState.value = flexiloadState.value.filterNot { it.id == txId }
        saveToDisk()
    }

    override fun observeDailyClosings(userId: String): Flow<List<DailyClosing>> =
        closingsState.map { it.sortedByDescending { dc -> dc.closingDate } }

    override suspend fun recordDailyClosing(userId: String, closing: DailyClosing): String = withContext(Dispatchers.IO) {
        val dcId = if (closing.id.isNotBlank()) closing.id else UUID.randomUUID().toString()
        val newDc = closing.copy(
            id = dcId,
            userId = userId,
            closingDate = if (closing.closingDate.isNotBlank()) closing.closingDate else todayStr
        )
        closingsState.value = listOf(newDc) + closingsState.value
        saveToDisk()
        dcId
    }

    override fun observeCategories(userId: String): Flow<List<CustomCategory>> = categoriesState.asStateFlow()

    override suspend fun addCategory(userId: String, name: String): String = withContext(Dispatchers.IO) {
        val cId = UUID.randomUUID().toString()
        val item = CustomCategory(id = cId, userId = userId, name = name)
        categoriesState.value = categoriesState.value + item
        saveToDisk()
        cId
    }

    override suspend fun deleteCategory(userId: String, categoryId: String) = withContext(Dispatchers.IO) {
        categoriesState.value = categoriesState.value.filterNot { it.id == categoryId }
        saveToDisk()
    }

    override suspend fun exportBackupJson(userId: String): String = withContext(Dispatchers.IO) {
        val root = JSONObject()
        root.put("version", 1)
        root.put("shop", "Yasin Telecom")
        root.put("timestamp", System.currentTimeMillis())

        val pArray = JSONArray()
        productsState.value.forEach { p ->
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

        root.toString(2)
    }

    override suspend fun restoreBackupJson(userId: String, jsonString: String): Int = withContext(Dispatchers.IO) {
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
        imported
    }

    override suspend fun verifyAdminPin(userId: String, pin: String): Boolean {
        val currentPin = profileState.value?.adminPin ?: "1234"
        return currentPin == pin.trim()
    }

    override suspend fun updateAdminPin(userId: String, newPin: String) = withContext(Dispatchers.IO) {
        profileState.value = profileState.value?.copy(adminPin = newPin.trim())
        saveToDisk()
    }

    override suspend fun addStock(
        userId: String,
        productId: String,
        addedQty: Long,
        purchasePrice: Double,
        supplierName: String,
        notes: String
    ): String = withContext(Dispatchers.IO) {
        val existing = productsState.value.find { it.id == productId }
            ?: throw IllegalArgumentException("Product not found")
        val prevQty = existing.currentStock
        val newTotal = prevQty + addedQty
        val updatedProduct = existing.copy(
            currentStock = newTotal,
            purchasePrice = if (purchasePrice > 0) purchasePrice else existing.purchasePrice
        )
        productsState.value = productsState.value.map { if (it.id == productId) updatedProduct else it }

        val addId = UUID.randomUUID().toString()
        val addition = StockAddition(
            id = addId,
            userId = userId,
            productId = productId,
            productName = existing.name,
            previousQuantity = prevQty,
            addedQuantity = addedQty,
            totalQuantity = newTotal,
            purchasePrice = purchasePrice,
            dateAdded = todayStr,
            supplierName = supplierName,
            notes = notes,
            isDemo = false
        )
        stockAdditionsState.value = stockAdditionsState.value + addition

        // Record corresponding purchase
        val pId = UUID.randomUUID().toString()
        val purchase = Purchase(
            id = pId,
            userId = userId,
            productId = productId,
            productName = existing.name,
            supplierName = supplierName,
            quantity = addedQty,
            purchasePrice = purchasePrice,
            totalAmount = addedQty * purchasePrice,
            purchaseDate = todayStr,
            paymentStatus = "PAID",
            notes = notes,
            isDemo = false
        )
        purchasesState.value = purchasesState.value + purchase

        saveToDisk()
        addId
    }

    override fun observeStockAdditions(userId: String): Flow<List<StockAddition>> {
        return stockAdditionsState.asStateFlow()
    }

    override fun observeCashMemos(userId: String): Flow<List<CashMemo>> {
        return cashMemosState.asStateFlow()
    }

    override suspend fun saveCashMemo(userId: String, memo: CashMemo, autoRecordSale: Boolean): String = withContext(Dispatchers.IO) {
        val memoId = if (memo.id.isNotBlank()) memo.id else UUID.randomUUID().toString()
        var saleId = ""

        if (autoRecordSale) {
            val saleItems = memo.items.map { item ->
                val p = productsState.value.find { it.id == item.productId }
                SaleItem(
                    productId = item.productId,
                    productName = item.description,
                    quantity = item.quantity,
                    sellingPrice = item.rate,
                    purchaseCost = p?.purchasePrice ?: 0.0,
                    subtotal = item.amount,
                    profit = (item.rate - (p?.purchasePrice ?: 0.0)) * item.quantity
                )
            }
            val sale = Sale(
                userId = userId,
                invoiceNumber = memo.memoNumber,
                customerName = memo.customerName,
                customerPhone = memo.customerMobile,
                itemsSummary = memo.items.joinToString(", ") { "${it.description} x${it.quantity}" },
                totalAmount = memo.grandTotal,
                totalCost = saleItems.sumOf { it.purchaseCost * it.quantity },
                grossProfit = memo.grandTotal - saleItems.sumOf { it.purchaseCost * it.quantity },
                paidAmount = memo.paidAmount,
                dueAmount = memo.dueAmount,
                saleDate = memo.date,
                notes = memo.notes,
                isDemo = false
            )
            saleId = recordSale(userId, sale, saleItems)

            if (memo.dueAmount > 0 && memo.customerName.isNotBlank()) {
                val existing = customersState.value.find { it.name.equals(memo.customerName.trim(), ignoreCase = true) }
                val customerId = if (existing != null) {
                    existing.id
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

        val finalMemo = memo.copy(id = memoId, saleId = saleId)
        cashMemosState.value = cashMemosState.value + finalMemo
        saveToDisk()
        memoId
    }

    override suspend fun deleteCashMemo(userId: String, memoId: String) = withContext(Dispatchers.IO) {
        cashMemosState.value = cashMemosState.value.filterNot { it.id == memoId }
        saveToDisk()
    }

    override suspend fun resetDemoData(userId: String): Int = withContext(Dispatchers.IO) {
        var removed = 0
        val pBefore = productsState.value.size
        productsState.value = productsState.value.filterNot { it.isDemo }
        removed += (pBefore - productsState.value.size)

        val sBefore = salesState.value.size
        salesState.value = salesState.value.filterNot { it.isDemo }
        removed += (sBefore - salesState.value.size)

        val puBefore = purchasesState.value.size
        purchasesState.value = purchasesState.value.filterNot { it.isDemo }
        removed += (puBefore - purchasesState.value.size)

        val eBefore = expensesState.value.size
        expensesState.value = expensesState.value.filterNot { it.isDemo }
        removed += (eBefore - expensesState.value.size)

        val cBefore = customersState.value.size
        customersState.value = customersState.value.filterNot { it.isDemo }
        removed += (cBefore - customersState.value.size)

        val lBefore = loansState.value.size
        loansState.value = loansState.value.filterNot { it.isDemo }
        removed += (lBefore - loansState.value.size)

        val mbBefore = mobileBankingState.value.size
        mobileBankingState.value = mobileBankingState.value.filterNot { it.isDemo }
        removed += (mbBefore - mobileBankingState.value.size)

        val flBefore = flexiloadState.value.size
        flexiloadState.value = flexiloadState.value.filterNot { it.isDemo }
        removed += (flBefore - flexiloadState.value.size)

        val clBefore = closingsState.value.size
        closingsState.value = closingsState.value.filterNot { it.isDemo }
        removed += (clBefore - closingsState.value.size)

        val cmBefore = cashMemosState.value.size
        cashMemosState.value = cashMemosState.value.filterNot { it.isDemo }
        removed += (cmBefore - cashMemosState.value.size)

        saveToDisk()
        removed
    }
}
