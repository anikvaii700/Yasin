package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class ShopProfile(
    @DocumentId val id: String = "shop_info",
    val userId: String = "",
    val shopName: String = "ইয়াছিন টেলিকম",
    val ownerName: String = "মোহাম্মদ ইয়াছিন",
    val address: String = "বুল্লা বাজার, লাখাই, হবিগঞ্জ",
    val phone: String = "01766906974",
    val email: String = "yasintelecom@gmail.com",
    val tagline: String = "এখানে যাবতীয় পাইকারী ও খুচরা মালামাল এবং ডিজিটাল সেবা সুলভ মূল্যে প্রদান করা হয়।",
    val currency: String = "৳",
    val lowStockAlertThreshold: Long = 5L,
    val adminPin: String = "1234",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Product(
    @DocumentId val id: String = "",
    val userId: String = "",
    val name: String = "",
    val category: String = "Other Accessories",
    val brand: String = "",
    val model: String = "",
    val purchasePrice: Double = 0.0,
    val sellingPrice: Double = 0.0,
    val currentStock: Long = 0L,
    val minStockAlert: Long = 5L,
    val productCode: String = "",
    val notes: String = "",
    val isDemo: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class StockAddition(
    @DocumentId val id: String = "",
    val userId: String = "",
    val productId: String = "",
    val productName: String = "",
    val previousQuantity: Long = 0L,
    val addedQuantity: Long = 0L,
    val totalQuantity: Long = 0L,
    val purchasePrice: Double = 0.0,
    val dateAdded: String = "", // DD-MM-YYYY
    val supplierName: String = "",
    val notes: String = "",
    val isDemo: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Sale(
    @DocumentId val id: String = "",
    val userId: String = "",
    val invoiceNumber: String = "",
    val customerName: String = "",
    val customerPhone: String = "",
    val itemsSummary: String = "",
    val totalAmount: Double = 0.0,
    val totalCost: Double = 0.0,
    val grossProfit: Double = 0.0,
    val paidAmount: Double = 0.0,
    val dueAmount: Double = 0.0,
    val saleDate: String = "", // DD-MM-YYYY
    val notes: String = "",
    val isDemo: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class SaleItem(
    val productId: String = "",
    val productName: String = "",
    val quantity: Long = 1L,
    val sellingPrice: Double = 0.0,
    val purchaseCost: Double = 0.0,
    val subtotal: Double = 0.0,
    val profit: Double = 0.0
)

@IgnoreExtraProperties
data class CashMemo(
    @DocumentId val id: String = "",
    val userId: String = "",
    val memoNumber: String = "",
    val date: String = "", // DD-MM-YYYY
    val time: String = "",
    val customerName: String = "",
    val customerMobile: String = "",
    val customerAddress: String = "",
    val items: List<CashMemoItem> = emptyList(),
    val subtotal: Double = 0.0,
    val discount: Double = 0.0,
    val grandTotal: Double = 0.0,
    val paidAmount: Double = 0.0,
    val dueAmount: Double = 0.0,
    val paymentStatus: String = "PAID", // PAID, DUE, PARTIAL
    val notes: String = "",
    val saleId: String = "",
    val isDemo: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class CashMemoItem(
    val productId: String = "",
    val description: String = "",
    val quantity: Long = 1L,
    val rate: Double = 0.0,
    val amount: Double = 0.0
)

@IgnoreExtraProperties
data class Purchase(
    @DocumentId val id: String = "",
    val userId: String = "",
    val productName: String = "",
    val productId: String = "",
    val supplierName: String = "",
    val quantity: Long = 0L,
    val purchasePrice: Double = 0.0,
    val totalAmount: Double = 0.0,
    val purchaseDate: String = "", // DD-MM-YYYY
    val paymentStatus: String = "PAID", // PAID, DUE, PARTIAL
    val notes: String = "",
    val isDemo: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Expense(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val amount: Double = 0.0,
    val category: String = "Other Expenses",
    val expenseDate: String = "", // DD-MM-YYYY
    val description: String = "",
    val isDemo: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Customer(
    @DocumentId val id: String = "",
    val userId: String = "",
    val name: String = "",
    val mobile: String = "",
    val address: String = "",
    val totalDue: Double = 0.0,
    val totalPaid: Double = 0.0,
    val remainingDue: Double = 0.0,
    val isDemo: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class CustomerTransaction(
    @DocumentId val id: String = "",
    val userId: String = "",
    val customerId: String = "",
    val type: String = "DUE", // DUE or PAYMENT
    val amount: Double = 0.0,
    val notes: String = "",
    val transactionDate: String = "", // DD-MM-YYYY
    val isDemo: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Loan(
    @DocumentId val id: String = "",
    val userId: String = "",
    val personName: String = "",
    val mobile: String = "",
    val loanType: String = "LENT", // LENT, BORROWED, HOUSEHOLD, PERSONAL, FAMILY, OTHER
    val amount: Double = 0.0,
    val paidAmount: Double = 0.0,
    val remainingAmount: Double = 0.0,
    val description: String = "",
    val expectedPaymentDate: String = "", // DD-MM-YYYY
    val isDemo: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class MobileBankingTx(
    @DocumentId val id: String = "",
    val userId: String = "",
    val provider: String = "BKASH", // BKASH or NAGAD
    val type: String = "CASH_IN", // CASH_IN, CASH_OUT, SEND_MONEY
    val amount: Double = 0.0,
    val commissionProfit: Double = 0.0,
    val serviceCharge: Double = 0.0,
    val customerPhone: String = "",
    val notes: String = "",
    val transactionDate: String = "", // DD-MM-YYYY
    val isDemo: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class FlexiloadTx(
    @DocumentId val id: String = "",
    val userId: String = "",
    val mobileNumber: String = "",
    val operator: String = "Grameenphone", // Grameenphone, Banglalink, Robi, Airtel, Teletalk
    val rechargeAmount: Double = 0.0,
    val customerPayment: Double = 0.0,
    val rechargeCost: Double = 0.0,
    val profit: Double = 0.0,
    val transactionDate: String = "", // DD-MM-YYYY
    val notes: String = "",
    val isDemo: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class DailyClosing(
    @DocumentId val id: String = "",
    val userId: String = "",
    val closingDate: String = "", // DD-MM-YYYY
    val productSales: Double = 0.0,
    val mobileBankingVolume: Double = 0.0,
    val flexiloadVolume: Double = 0.0,
    val grossProfit: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val customerPaymentsReceived: Double = 0.0,
    val totalCashReceived: Double = 0.0,
    val totalCashPaid: Double = 0.0,
    val expectedCash: Double = 0.0,
    val actualCash: Double = 0.0,
    val cashDifference: Double = 0.0,
    val notes: String = "",
    val isDemo: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class CustomCategory(
    @DocumentId val id: String = "",
    val userId: String = "",
    val name: String = "",
    val isDemo: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null
)

/**
 * Converts a numeric amount to Bangla words for the cash memo.
 */
fun takaInBengaliWords(amount: Double): String {
    val rounded = amount.toLong()
    if (rounded <= 0) return "শূন্য টাকা মাত্র"
    
    val units = arrayOf(
        "", "এক", "দুই", "তিন", "চার", "পাঁচ", "ছয়", "সাত", "আট", "নয়", "দশ",
        "এগারো", "বারো", "তেরো", "চোদ্দ", "পনেরো", "ষোল", "সতেরো", "আঠারো", "উনিশ", "বিশ",
        "একুশ", "বাইশ", "তেইশ", "চব্বিশ", "পঁচিশ", "ছাব্বিশ", "সাতাশ", "আঠাশ", "ঊনত্রিশ", "ত্রিশ",
        "একত্রিশ", "বত্রিশ", "তেত্রিশ", "চৌত্রিশ", "পঁয়ত্রিশ", "ছত্রিশ", "সাঁইত্রিশ", "আটত্রিশ", "ঊনচল্লিশ", "চল্লিশ",
        "একচল্লিশ", "বিয়াল্লিশ", "তেতাল্লিশ", "চুয়াল্লিশ", "পঁয়তাল্লিশ", "ছেচল্লিশ", "সাতচল্লিশ", "আটচল্লিশ", "ঊনপঞ্চাশ", "পঞ্চাশ",
        "একান্ন", "বায়ান্ন", "তিপ্পান্ন", "চুয়ান্ন", "পঞ্চান্ন", "ছাপ্পান্ন", "সাতান্ন", "আটান্ন", "ঊনষাট", "ষাট",
        "একষট্টি", "বাষট্টি", "তেষট্টি", "চৌষট্টি", "পঁয়ষট্টি", "ছেষট্টি", "সাতষট্টি", "আটষট্টি", "ঊনসত্তর", "সত্তর",
        "একাত্তর", "বাহাত্তর", "তিয়াত্তর", "চুয়াত্তর", "পঁচাত্তর", "ছিয়াত্তর", "সাতাত্তর", "আঠাত্তর", "ঊনআশি", "আশি",
        "একাশি", "বিরাশি", "তিরাশি", "চুরাশি", "পঁচাশি", "ছিয়াশি", "সাতাশি", "আটাশি", "ঊননব্বই", "নব্বই",
        "একানব্বই", "বানব্বই", "তিরানব্বই", "চুরানব্বই", "পঁচানব্বই", "ছিয়ানব্বই", "সাতানব্বই", "আটানব্বই", "নিরানব্বই"
    )

    fun convertChunk(n: Long): String {
        var str = ""
        var num = n
        if (num >= 10000000) { // কোটি
            val crore = num / 10000000
            str += convertChunk(crore) + " কোটি "
            num %= 10000000
        }
        if (num >= 100000) { // লক্ষ
            val lakh = num / 100000
            if (lakh < units.size) {
                str += units[lakh.toInt()] + " লক্ষ "
            }
            num %= 100000
        }
        if (num >= 1000) { // হাজার
            val thousand = num / 1000
            if (thousand < units.size) {
                str += units[thousand.toInt()] + " হাজার "
            }
            num %= 1000
        }
        if (num >= 100) { // শত
            val hundred = num / 100
            if (hundred < units.size) {
                str += units[hundred.toInt()] + " শত "
            }
            num %= 100
        }
        if (num > 0) {
            if (num < units.size) {
                str += units[num.toInt()] + " "
            }
        }
        return str.trim()
    }

    val result = convertChunk(rounded)
    return if (result.isBlank()) "শূন্য টাকা মাত্র" else "$result টাকা মাত্র"
}
