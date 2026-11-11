package com.example.data.repository

import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

interface ShopDataSource {
    fun observeProfile(userId: String): Flow<ShopProfile?>
    suspend fun saveProfile(userId: String, profile: ShopProfile)
    suspend fun verifyAdminPin(userId: String, pin: String): Boolean
    suspend fun updateAdminPin(userId: String, newPin: String)

    fun observeProducts(userId: String): Flow<List<Product>>
    suspend fun addProduct(userId: String, product: Product): String
    suspend fun updateProduct(userId: String, product: Product)
    suspend fun deleteProduct(userId: String, productId: String)
    suspend fun addStock(
        userId: String,
        productId: String,
        addedQty: Long,
        purchasePrice: Double,
        supplierName: String,
        notes: String
    ): String
    fun observeStockAdditions(userId: String): Flow<List<StockAddition>>

    fun observeSales(userId: String): Flow<List<Sale>>
    suspend fun recordSale(userId: String, sale: Sale, items: List<SaleItem>): String
    suspend fun deleteSale(userId: String, saleId: String)

    fun observePurchases(userId: String): Flow<List<Purchase>>
    suspend fun recordPurchase(userId: String, purchase: Purchase): String
    suspend fun deletePurchase(userId: String, purchaseId: String)

    fun observeExpenses(userId: String): Flow<List<Expense>>
    suspend fun recordExpense(userId: String, expense: Expense): String
    suspend fun deleteExpense(userId: String, expenseId: String)

    fun observeCustomers(userId: String): Flow<List<Customer>>
    suspend fun addCustomer(userId: String, customer: Customer): String
    suspend fun recordCustomerTx(userId: String, customerId: String, tx: CustomerTransaction): String
    fun observeCustomerTransactions(userId: String, customerId: String): Flow<List<CustomerTransaction>>
    suspend fun deleteCustomer(userId: String, customerId: String)

    fun observeLoans(userId: String): Flow<List<Loan>>
    suspend fun addLoan(userId: String, loan: Loan): String
    suspend fun recordLoanPayment(userId: String, loanId: String, paymentAmount: Double)
    suspend fun deleteLoan(userId: String, loanId: String)

    fun observeMobileBanking(userId: String): Flow<List<MobileBankingTx>>
    suspend fun recordMobileBanking(userId: String, tx: MobileBankingTx): String
    suspend fun deleteMobileBanking(userId: String, txId: String)

    fun observeFlexiload(userId: String): Flow<List<FlexiloadTx>>
    suspend fun recordFlexiload(userId: String, tx: FlexiloadTx): String
    suspend fun deleteFlexiload(userId: String, txId: String)

    fun observeDailyClosings(userId: String): Flow<List<DailyClosing>>
    suspend fun recordDailyClosing(userId: String, closing: DailyClosing): String

    fun observeCategories(userId: String): Flow<List<CustomCategory>>
    suspend fun addCategory(userId: String, name: String): String
    suspend fun deleteCategory(userId: String, categoryId: String)

    fun observeCashMemos(userId: String): Flow<List<CashMemo>>
    suspend fun saveCashMemo(userId: String, memo: CashMemo, autoRecordSale: Boolean): String
    suspend fun deleteCashMemo(userId: String, memoId: String)

    suspend fun resetDemoData(userId: String): Int
    suspend fun exportBackupJson(userId: String): String
    suspend fun restoreBackupJson(userId: String, jsonString: String): Int
}
