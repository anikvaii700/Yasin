package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.ShopViewModel
import com.example.ui.components.*
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    viewModel: ShopViewModel,
    onNavigateToProducts: () -> Unit,
    onNavigateToSales: () -> Unit,
    onNavigateToDue: () -> Unit,
    onNavigateToPurchases: () -> Unit,
    onNavigateToExpenses: () -> Unit,
    onNavigateToMobileBanking: () -> Unit,
    onNavigateToCashMemo: () -> Unit,
    onOpenAddProductDialog: () -> Unit,
    onOpenRecordSaleDialog: () -> Unit,
    onOpenAddExpenseDialog: () -> Unit,
    onOpenAddDueDialog: () -> Unit,
    onOpenAddPurchaseDialog: () -> Unit,
    onOpenDrawer: () -> Unit,
    onOpenAdminPanel: () -> Unit
) {
    val today = viewModel.todayDateStr
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val products by viewModel.products.collectAsStateWithLifecycle()
    val sales by viewModel.sales.collectAsStateWithLifecycle()
    val expenses by viewModel.expenses.collectAsStateWithLifecycle()
    val customers by viewModel.customers.collectAsStateWithLifecycle()
    val loans by viewModel.loans.collectAsStateWithLifecycle()
    val mobileBanking by viewModel.mobileBanking.collectAsStateWithLifecycle()
    val flexiload by viewModel.flexiload.collectAsStateWithLifecycle()

    // Financial calculations for Today
    val todaySalesList = sales.filter { it.saleDate == today }
    val todayExpensesList = expenses.filter { it.expenseDate == today }
    val todayMbList = mobileBanking.filter { it.transactionDate == today }
    val todayFlexiList = flexiload.filter { it.transactionDate == today }

    val todayTotalSales = todaySalesList.sumOf { it.totalAmount }
    val todaySaleProfit = todaySalesList.sumOf { it.grossProfit }
    val todayMbProfit = todayMbList.sumOf { it.commissionProfit }
    val todayFlexiProfit = todayFlexiList.sumOf { it.profit }
    val todayGrossProfit = todaySaleProfit + todayMbProfit + todayFlexiProfit

    val todayTotalExpenses = todayExpensesList.sumOf { it.amount }
    val todayNetProfit = todayGrossProfit - todayTotalExpenses

    // Cash flow estimate today
    val todayCashSales = todaySalesList.sumOf { it.paidAmount }
    val todayCashInMb = todayMbList.filter { it.type == "CASH_IN" }.sumOf { it.amount }
    val todayCashOutMb = todayMbList.filter { it.type == "CASH_OUT" }.sumOf { it.amount }
    val todayEstimatedCash = (todayCashSales + todayCashInMb - todayCashOutMb - todayTotalExpenses).coerceAtLeast(0.0)

    // Stock analytics
    val totalProductsCount = products.size
    val totalStockQuantity = products.sumOf { it.currentStock }
    val totalStockValue = products.sumOf { it.currentStock * it.purchasePrice }
    val totalPotentialValue = products.sumOf { it.currentStock * it.sellingPrice }
    val potentialProfit = totalPotentialValue - totalStockValue

    val lowStockThreshold = profile?.lowStockAlertThreshold ?: 5L
    val lowStockList = products.filter { it.currentStock > 0 && it.currentStock <= (if (it.minStockAlert > 0) it.minStockAlert else lowStockThreshold) }
    val outOfStockList = products.filter { it.currentStock <= 0 }

    // Customer Due & Personal Loans
    val totalCustomerDue = customers.sumOf { it.remainingDue }
    val totalLentReceivable = loans.filter { it.loanType == "LENT" }.sumOf { it.remainingAmount }
    val totalBorrowedPayable = loans.filter { it.loanType != "LENT" }.sumOf { it.remainingAmount }

    // 10-Second Long Press State for Admin Panel
    var isHoldingHeader by remember { mutableStateOf(false) }
    var holdSeconds by remember { mutableStateOf(0) }

    LaunchedEffect(isHoldingHeader) {
        if (isHoldingHeader) {
            holdSeconds = 0
            while (isHoldingHeader && holdSeconds < 10) {
                delay(1000L)
                holdSeconds += 1
            }
            if (isHoldingHeader && holdSeconds >= 10) {
                isHoldingHeader = false
                holdSeconds = 0
                onOpenAdminPanel()
            }
        } else {
            holdSeconds = 0
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Shop Banner Header with Hamburger Menu and 10s Long Press Admin trigger
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("shop_header_banner")
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DashboardGradients.banner)
                        .padding(18.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Top row with Hamburger Menu on left and Store badge on right
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = onOpenDrawer,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.15f))
                                    .testTag("btn_drawer_open")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = "মেনু ড্রয়ার খুলুন",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = Color.White.copy(alpha = 0.18f)
                                ) {
                                    Text(
                                        text = "স্মার্ট শপ ম্যানেজার",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(YasinGreen),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Store,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Shop Information Area with 10-Second Long Press to enter Admin Mode
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color.White.copy(alpha = if (isHoldingHeader) 0.15f else 0.05f))
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onPress = {
                                            isHoldingHeader = true
                                            tryAwaitRelease()
                                            isHoldingHeader = false
                                        }
                                    )
                                }
                                .padding(12.dp)
                        ) {
                            Column {
                                Text(
                                    text = profile?.shopName ?: "ইয়াছিন টেলিকম",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "স্বত্বাধিকারী: ${profile?.ownerName ?: "মোহাম্মদ ইয়াছিন"}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                                Text(
                                    text = "📍 ${profile?.address ?: "বুল্লা বাজার, লাখাই, হবিগঞ্জ"} | 📞 ${profile?.phone ?: "01766906974"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.75f)
                                )

                                if (isHoldingHeader) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        CircularProgressIndicator(
                                            progress = { holdSeconds / 10f },
                                            modifier = Modifier.size(20.dp),
                                            color = Color(0xFFFDE047),
                                            strokeWidth = 3.dp
                                        )
                                        Text(
                                            text = "অ্যাডমিন প্যানেল আনলক হচ্ছে... (${10 - holdSeconds}s)",
                                            color = Color(0xFFFDE047),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Date and BDT bar
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color.White.copy(alpha = 0.14f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 7.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "আজকের তারিখ: $today",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "মুদ্রা: ৳ (BDT)",
                                    color = Color(0xFF86EFAC),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section: 8 Vibrant Gradient Dashboard Cards
        item {
            SectionHeader(title = "আজকের সারসংক্ষেপ (Today's Summary)")
        }

        // Row 1: Today's Sales & Today's Profit
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                GradientMetricCard(
                    title = "আজকের বিক্রয়",
                    value = formatTaka(todayTotalSales),
                    subtitle = "${todaySalesList.size} টি বিক্রয়",
                    icon = Icons.Default.PointOfSale,
                    gradient = DashboardGradients.sales,
                    modifier = Modifier.weight(1f),
                    testTag = "card_today_sales",
                    onClick = onNavigateToSales
                )
                GradientMetricCard(
                    title = "আজকের মোট লাভ",
                    value = formatTaka(todayGrossProfit),
                    subtitle = if (todayTotalExpenses > 0) "নেট লাভ: ${formatTaka(todayNetProfit)}" else "গ্রস প্রফিট",
                    icon = Icons.Default.TrendingUp,
                    gradient = DashboardGradients.profit,
                    modifier = Modifier.weight(1f),
                    testTag = "card_today_profit",
                    onClick = onNavigateToSales
                )
            }
        }

        // Row 2: Total Products & Available Stock
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                GradientMetricCard(
                    title = "মোট পণ্য",
                    value = "$totalProductsCount টি",
                    subtitle = "স্টক মূল্য: ${formatTaka(totalStockValue)}",
                    icon = Icons.Default.Inventory2,
                    gradient = DashboardGradients.products,
                    modifier = Modifier.weight(1f),
                    testTag = "card_total_products",
                    onClick = onNavigateToProducts
                )
                GradientMetricCard(
                    title = "মজুদ স্টক সংখ্যা",
                    value = "$totalStockQuantity টি",
                    subtitle = "সম্ভাব্য লাভ: ${formatTaka(potentialProfit)}",
                    icon = Icons.Default.AllInbox,
                    gradient = DashboardGradients.stock,
                    modifier = Modifier.weight(1f),
                    testTag = "card_total_stock",
                    onClick = onNavigateToProducts
                )
            }
        }

        // Row 3: Low Stock Alert & Customer Due
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                GradientMetricCard(
                    title = "কম স্টক সতর্কতা",
                    value = "${lowStockList.size} টি",
                    subtitle = if (outOfStockList.isNotEmpty()) "স্টক শেষ: ${outOfStockList.size} টি" else "পর্যাপ্ত স্টক",
                    icon = Icons.Default.WarningAmber,
                    gradient = DashboardGradients.lowStock,
                    modifier = Modifier.weight(1f),
                    testTag = "card_low_stock",
                    onClick = onNavigateToProducts
                )
                GradientMetricCard(
                    title = "গ্রাহক বাকি (বাকি খাতা)",
                    value = formatTaka(totalCustomerDue),
                    subtitle = "${customers.count { it.remainingDue > 0 }} জন বাকিদার",
                    icon = Icons.Default.AccountBalanceWallet,
                    gradient = DashboardGradients.due,
                    modifier = Modifier.weight(1f),
                    testTag = "card_customer_due",
                    onClick = onNavigateToDue
                )
            }
        }

        // Row 4: Total Expenses & Cash Balance
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                GradientMetricCard(
                    title = "আজকের মোট খরচ",
                    value = formatTaka(todayTotalExpenses),
                    subtitle = "${todayExpensesList.size} টি খরচের এন্ট্রি",
                    icon = Icons.Default.ReceiptLong,
                    gradient = DashboardGradients.expenses,
                    modifier = Modifier.weight(1f),
                    testTag = "card_today_expenses",
                    onClick = onNavigateToExpenses
                )
                GradientMetricCard(
                    title = "আনুমানিক ক্যাশ ব্যালেন্স",
                    value = formatTaka(todayEstimatedCash),
                    subtitle = "আজকের নিট ক্যাশ ইনফ্লো",
                    icon = Icons.Default.AccountBalance,
                    gradient = DashboardGradients.cash,
                    modifier = Modifier.weight(1f),
                    testTag = "card_cash_balance",
                    onClick = onNavigateToMobileBanking
                )
            }
        }

        // Soft Accent Information Boxes (Section 1 requirement)
        item {
            SectionHeader(title = "গুরুত্বপূর্ণ বাণিজ্যিক সূচক (Key Metrics)")
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                InfoBox(
                    title = "মোবাইল ব্যাংকিং ও ফ্লেক্সিলোড লাভ",
                    value = formatTaka(todayMbProfit + todayFlexiProfit),
                    bgColor = SoftTealBg,
                    textColor = SoftTealText,
                    icon = Icons.Default.PhoneAndroid
                )
                InfoBox(
                    title = "ব্যক্তিগত ধার (পাওনা বকেয়া)",
                    value = formatTaka(totalLentReceivable),
                    bgColor = SoftPinkBg,
                    textColor = SoftPinkText,
                    icon = Icons.Default.Handshake
                )
                InfoBox(
                    title = "ব্যক্তিগত ঋণ (দোকানের দেনা)",
                    value = formatTaka(totalBorrowedPayable),
                    bgColor = SoftOrangeBg,
                    textColor = SoftOrangeText,
                    icon = Icons.Default.CreditCard
                )
                InfoBox(
                    title = "দোকানের সর্বমোট সম্ভাব্য বিক্রয় মূল্য",
                    value = formatTaka(totalPotentialValue),
                    bgColor = SoftPurpleBg,
                    textColor = SoftPurpleText,
                    icon = Icons.Default.LocalMall
                )
            }
        }

        // Quick Actions (6 required + Cash Memo)
        item {
            SectionHeader(title = "দ্রুত কার্যক্রম (Quick Actions)")
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickActionButton(
                    title = "ক্যাশ মেমো",
                    icon = Icons.Default.Description,
                    containerColor = Color(0xFFEA580C),
                    modifier = Modifier.weight(1f),
                    testTag = "quick_cash_memo",
                    onClick = onNavigateToCashMemo
                )
                QuickActionButton(
                    title = "পণ্য যোগ",
                    icon = Icons.Default.AddBox,
                    containerColor = YasinGreen,
                    modifier = Modifier.weight(1f),
                    testTag = "quick_add_product",
                    onClick = onOpenAddProductDialog
                )
                QuickActionButton(
                    title = "বিক্রয় রেকর্ড",
                    icon = Icons.Default.ShoppingCartCheckout,
                    containerColor = YasinNavy,
                    modifier = Modifier.weight(1f),
                    testTag = "quick_record_sale",
                    onClick = onOpenRecordSaleDialog
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickActionButton(
                    title = "ক্রয় যোগ",
                    icon = Icons.Default.ShoppingBag,
                    containerColor = Color(0xFF6366F1),
                    modifier = Modifier.weight(1f),
                    testTag = "quick_add_purchase",
                    onClick = onOpenAddPurchaseDialog
                )
                QuickActionButton(
                    title = "খরচ যোগ",
                    icon = Icons.Default.Receipt,
                    containerColor = Color(0xFFE11D48),
                    modifier = Modifier.weight(1f),
                    testTag = "quick_add_expense",
                    onClick = onOpenAddExpenseDialog
                )
                QuickActionButton(
                    title = "বাকি এন্ট্রি",
                    icon = Icons.Default.Payment,
                    containerColor = Color(0xFF0284C7),
                    modifier = Modifier.weight(1f),
                    testTag = "quick_add_due",
                    onClick = onOpenAddDueDialog
                )
            }
        }

        // Stock Status Alert Widget
        item {
            SectionHeader(
                title = "স্টক স্থিতি পর্যালোচনা",
                subtitle = "সবুজ: পর্যাপ্ত • হলুদ: কমে আসছে • লাল: স্টক শেষ",
                actionText = "সকল পণ্য →",
                onActionClick = onNavigateToProducts
            )
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StockStatusCount(
                            label = "পর্যাপ্ত স্টক",
                            count = products.count { it.currentStock > (if (it.minStockAlert > 0) it.minStockAlert else lowStockThreshold) },
                            color = StatusGreen,
                            bgColor = StatusGreenBg
                        )
                        StockStatusCount(
                            label = "কমে আসছে",
                            count = lowStockList.size,
                            color = StatusYellow,
                            bgColor = StatusYellowBg
                        )
                        StockStatusCount(
                            label = "স্টক শেষ",
                            count = outOfStockList.size,
                            color = StatusRed,
                            bgColor = StatusRedBg
                        )
                    }

                    if (outOfStockList.isNotEmpty() || lowStockList.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Divider(color = BorderLight)
                        Spacer(modifier = Modifier.height(10.dp))

                        val alerts = (outOfStockList + lowStockList).take(4)
                        alerts.forEach { p ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = p.name,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 13.sp,
                                    color = TextPrimary
                                )
                                StockBadge(
                                    currentStock = p.currentStock,
                                    minAlert = if (p.minStockAlert > 0) p.minStockAlert else lowStockThreshold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuickActionButton(
    title: String,
    icon: ImageVector,
    containerColor: Color,
    modifier: Modifier = Modifier,
    testTag: String? = null,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(84.dp)
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 11.5.sp,
                maxLines = 1
            )
        }
    }
}

@Composable
fun StockStatusCount(
    label: String,
    count: Int,
    color: Color,
    bgColor: Color
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = bgColor,
        modifier = Modifier.width(100.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count.toString(),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
                color = color
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = color
            )
        }
    }
}
