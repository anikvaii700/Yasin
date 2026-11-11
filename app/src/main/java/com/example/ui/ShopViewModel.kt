package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.ShopDataSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ShopViewModel(
    val repository: ShopDataSource,
    val currentUserId: String
) : ViewModel() {

    private val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.US)
    val todayDateStr: String
        get() = dateFormat.format(Date())

    val profile: StateFlow<ShopProfile?> = repository.observeProfile(currentUserId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), null)

    val products: StateFlow<List<Product>> = repository.observeProducts(currentUserId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val sales: StateFlow<List<Sale>> = repository.observeSales(currentUserId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val purchases: StateFlow<List<Purchase>> = repository.observePurchases(currentUserId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val expenses: StateFlow<List<Expense>> = repository.observeExpenses(currentUserId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val customers: StateFlow<List<Customer>> = repository.observeCustomers(currentUserId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val loans: StateFlow<List<Loan>> = repository.observeLoans(currentUserId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val mobileBanking: StateFlow<List<MobileBankingTx>> = repository.observeMobileBanking(currentUserId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val flexiload: StateFlow<List<FlexiloadTx>> = repository.observeFlexiload(currentUserId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val dailyClosings: StateFlow<List<DailyClosing>> = repository.observeDailyClosings(currentUserId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val customCategories: StateFlow<List<CustomCategory>> = repository.observeCategories(currentUserId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val stockAdditions: StateFlow<List<StockAddition>> = repository.observeStockAdditions(currentUserId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val cashMemos: StateFlow<List<CashMemo>> = repository.observeCashMemos(currentUserId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    private val _isAdminAuthenticated = MutableStateFlow(false)
    val isAdminAuthenticated: StateFlow<Boolean> = _isAdminAuthenticated.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun loginAdmin(pin: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = repository.verifyAdminPin(currentUserId, pin)
            _isAdminAuthenticated.value = success
            if (success) {
                _message.value = "অ্যাডমিন প্যানেলে সফলভাবে প্রবেশ করা হয়েছে"
            } else {
                _errorMessage.value = "ভুল অ্যাডমিন পিন! সঠিক পিন দিন।"
            }
            onResult(success)
        }
    }

    fun logoutAdmin() {
        _isAdminAuthenticated.value = false
        _message.value = "অ্যাডমিন প্যানেল লক করা হয়েছে"
    }

    fun changeAdminPin(newPin: String) {
        viewModelScope.launch {
            try {
                repository.updateAdminPin(currentUserId, newPin)
                _message.value = "অ্যাডমিন পিন সফলভাবে পরিবর্তন করা হয়েছে"
            } catch (e: Exception) {
                _errorMessage.value = "পিন পরিবর্তন করা যায়নি: ${e.message}"
            }
        }
    }

    fun addStock(
        productId: String,
        addedQty: Long,
        purchasePrice: Double,
        supplierName: String,
        notes: String,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                repository.addStock(currentUserId, productId, addedQty, purchasePrice, supplierName, notes)
                _message.value = "স্টক সফলভাবে বৃদ্ধি করা হয়েছে (+$addedQty)"
                onSuccess()
            } catch (e: Exception) {
                _errorMessage.value = "স্টক যোগ করতে ব্যর্থ: ${e.message}"
            }
        }
    }

    fun saveCashMemo(memo: CashMemo, autoRecordSale: Boolean, onDone: (String) -> Unit = {}) {
        viewModelScope.launch {
            try {
                val id = repository.saveCashMemo(currentUserId, memo, autoRecordSale)
                _message.value = "ক্যাশ মেমো #${memo.memoNumber} সফলভাবে সংরক্ষিত হয়েছে!"
                onDone(id)
            } catch (e: Exception) {
                _errorMessage.value = "ক্যাশ মেমো সংরক্ষণে সমস্যা: ${e.message}"
            }
        }
    }

    fun deleteCashMemo(memoId: String) {
        viewModelScope.launch {
            try {
                repository.deleteCashMemo(currentUserId, memoId)
                _message.value = "ক্যাশ মেমো মুছে ফেলা হয়েছে"
            } catch (e: Exception) {
                _errorMessage.value = "মুছতে সমস্যা হয়েছে: ${e.message}"
            }
        }
    }

    fun resetDemoData(onCompleted: (Int) -> Unit) {
        viewModelScope.launch {
            try {
                val removed = repository.resetDemoData(currentUserId)
                _message.value = "$removed টি ডেমো রেকর্ড সফলভাবে মুছে ফেলা হয়েছে! আসল ডাটা অক্ষত আছে।"
                onCompleted(removed)
            } catch (e: Exception) {
                _errorMessage.value = "ডেমো রিসেট ব্যর্থ: ${e.message}"
            }
        }
    }

    fun clearMessage() {
        _message.value = null
        _errorMessage.value = null
    }

    // Default categories requested
    val defaultCategories = listOf(
        "New Mobile Phones",
        "Used Mobile Phones",
        "Mobile Chargers",
        "Batteries",
        "Tempered Glass",
        "Mobile Covers",
        "Earphones",
        "Headphones",
        "Data Cables",
        "USB Cables",
        "Memory Cards",
        "Power Banks",
        "Speakers",
        "Mobile Parts",
        "Other Accessories"
    )

    fun getAllCategories(): List<String> {
        val custom = customCategories.value.map { it.name }
        return (defaultCategories + custom).distinct()
    }

    // Product actions
    fun addProduct(product: Product, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                repository.addProduct(currentUserId, product)
                _message.value = "পণ্য সফলভাবে যুক্ত হয়েছে!"
                onSuccess()
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage ?: "পণ্য যোগ করতে সমস্যা হয়েছে"
            }
        }
    }

    fun updateProduct(product: Product, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                repository.updateProduct(currentUserId, product)
                _message.value = "পণ্য আপডেট হয়েছে!"
                onSuccess()
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage ?: "পণ্য আপডেট ব্যর্থ"
            }
        }
    }

    fun deleteProduct(productId: String) {
        viewModelScope.launch {
            try {
                repository.deleteProduct(currentUserId, productId)
                _message.value = "পণ্য মুছে ফেলা হয়েছে"
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage ?: "মুছে ফেলতে সমস্যা হয়েছে"
            }
        }
    }

    // Sales actions
    fun recordSale(sale: Sale, items: List<SaleItem>, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                repository.recordSale(currentUserId, sale, items)
                _message.value = "বিক্রয় সফলভাবে রেকর্ড করা হয়েছে!"
                onSuccess()
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage ?: "বিক্রয় এন্ট্রি ব্যর্থ"
            }
        }
    }

    fun deleteSale(saleId: String) {
        viewModelScope.launch {
            try {
                repository.deleteSale(currentUserId, saleId)
                _message.value = "বিক্রয় রেকর্ড মুছে ফেলা হয়েছে"
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage ?: "মুছতে সমস্যা হয়েছে"
            }
        }
    }

    // Purchase actions
    fun recordPurchase(purchase: Purchase, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                repository.recordPurchase(currentUserId, purchase)
                _message.value = "ক্রয় রেকর্ড সম্পন্ন ও স্টক আপডেট হয়েছে!"
                onSuccess()
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage ?: "ক্রয় এন্ট্রি ব্যর্থ"
            }
        }
    }

    fun deletePurchase(purchaseId: String) {
        viewModelScope.launch {
            try {
                repository.deletePurchase(currentUserId, purchaseId)
                _message.value = "ক্রয় রেকর্ড মুছে ফেলা হয়েছে"
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage ?: "ব্যর্থ হয়েছে"
            }
        }
    }

    // Expense actions
    fun recordExpense(expense: Expense, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                repository.recordExpense(currentUserId, expense)
                _message.value = "খরচ রেকর্ড করা হয়েছে!"
                onSuccess()
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage ?: "খরচ যোগ করতে সমস্যা"
            }
        }
    }

    fun deleteExpense(expenseId: String) {
        viewModelScope.launch {
            try {
                repository.deleteExpense(currentUserId, expenseId)
                _message.value = "খরচ মুছে ফেলা হয়েছে"
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage ?: "মুছতে সমস্যা"
            }
        }
    }

    // Customer Dues actions
    fun addCustomer(customer: Customer, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                repository.addCustomer(currentUserId, customer)
                _message.value = "কাস্টমার যুক্ত হয়েছে!"
                onSuccess()
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage ?: "কাস্টমার যোগ ব্যর্থ"
            }
        }
    }

    fun recordCustomerTx(customerId: String, tx: CustomerTransaction, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                repository.recordCustomerTx(currentUserId, customerId, tx)
                _message.value = if (tx.type == "DUE") "বাকি যোগ হয়েছে!" else "পেমেন্ট জমা হয়েছে!"
                onSuccess()
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage ?: "লেনদেন ব্যর্থ"
            }
        }
    }

    fun deleteCustomer(customerId: String) {
        viewModelScope.launch {
            try {
                repository.deleteCustomer(currentUserId, customerId)
                _message.value = "কাস্টমার মুছে ফেলা হয়েছে"
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage ?: "মুছতে সমস্যা"
            }
        }
    }

    // Loans actions
    fun addLoan(loan: Loan, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                repository.addLoan(currentUserId, loan)
                _message.value = "ঋণ/ধার রেকর্ড যুক্ত হয়েছে!"
                onSuccess()
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage ?: "ব্যর্থ হয়েছে"
            }
        }
    }

    fun recordLoanPayment(loanId: String, paymentAmount: Double, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                repository.recordLoanPayment(currentUserId, loanId, paymentAmount)
                _message.value = "ঋণ পরিশোধ আপডেট হয়েছে!"
                onSuccess()
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage ?: "পরিশোধ ব্যর্থ"
            }
        }
    }

    fun deleteLoan(loanId: String) {
        viewModelScope.launch {
            try {
                repository.deleteLoan(currentUserId, loanId)
                _message.value = "রেকর্ড মুছে ফেলা হয়েছে"
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage ?: "ব্যর্থ হয়েছে"
            }
        }
    }

    // Mobile Banking actions
    fun recordMobileBanking(tx: MobileBankingTx, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                repository.recordMobileBanking(currentUserId, tx)
                _message.value = "${tx.provider} লেনদেন সফলভাবে সংরক্ষিত!"
                onSuccess()
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage ?: "সংরক্ষণ ব্যর্থ"
            }
        }
    }

    fun deleteMobileBanking(txId: String) {
        viewModelScope.launch {
            try {
                repository.deleteMobileBanking(currentUserId, txId)
                _message.value = "লেনদেন মুছে ফেলা হয়েছে"
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage ?: "ব্যর্থ হয়েছে"
            }
        }
    }

    // Flexiload actions
    fun recordFlexiload(tx: FlexiloadTx, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                repository.recordFlexiload(currentUserId, tx)
                _message.value = "ফ্লেক্সিলোড রেকর্ড করা হয়েছে!"
                onSuccess()
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage ?: "ফ্লেক্সিলোড ব্যর্থ"
            }
        }
    }

    fun deleteFlexiload(txId: String) {
        viewModelScope.launch {
            try {
                repository.deleteFlexiload(currentUserId, txId)
                _message.value = "রেকর্ড মুছে ফেলা হয়েছে"
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage ?: "ব্যর্থ হয়েছে"
            }
        }
    }

    // Daily Closing
    fun recordDailyClosing(closing: DailyClosing, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                repository.recordDailyClosing(currentUserId, closing)
                _message.value = "আজকের ক্লোজিং রিপোর্ট সংরক্ষিত হয়েছে!"
                onSuccess()
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage ?: "ক্লোজিং ব্যর্থ"
            }
        }
    }

    // Categories
    fun addCustomCategory(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            try {
                repository.addCategory(currentUserId, name.trim())
                _message.value = "নতুন ক্যাটাগরি তৈরি হয়েছে!"
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage ?: "ব্যর্থ হয়েছে"
            }
        }
    }

    fun deleteCustomCategory(id: String) {
        viewModelScope.launch {
            try {
                repository.deleteCategory(currentUserId, id)
                _message.value = "ক্যাটাগরি মুছে ফেলা হয়েছে"
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage ?: "মুছতে সমস্যা"
            }
        }
    }

    // Settings Profile
    fun updateProfile(profile: ShopProfile, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                repository.saveProfile(currentUserId, profile)
                _message.value = "দোকানের তথ্য সংরক্ষিত হয়েছে!"
                onSuccess()
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage ?: "আপডেট ব্যর্থ"
            }
        }
    }

    // Backup & Restore
    fun exportBackup(onResult: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val json = repository.exportBackupJson(currentUserId)
                onResult(json)
            } catch (e: Exception) {
                _errorMessage.value = "ব্যাকআপ তৈরিতে সমস্যা: ${e.message}"
            }
        }
    }

    fun restoreBackup(jsonString: String, onComplete: (Int) -> Unit) {
        viewModelScope.launch {
            try {
                val count = repository.restoreBackupJson(currentUserId, jsonString)
                _message.value = "$count টি পণ্য রিস্টোর করা হয়েছে!"
                onComplete(count)
            } catch (e: Exception) {
                _errorMessage.value = "রিস্টোর ব্যর্থ: ${e.message}"
            }
        }
    }
}
