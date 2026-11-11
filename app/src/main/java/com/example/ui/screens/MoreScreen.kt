package com.example.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.*
import com.example.ui.ShopViewModel
import com.example.ui.components.ConfirmDeleteDialog
import com.example.ui.components.SectionHeader
import com.example.ui.components.formatTaka
import com.example.ui.theme.*

enum class MoreSubScreen {
    MENU,
    PURCHASES,
    EXPENSES,
    LOANS,
    MOBILE_BANKING,
    FLEXILOAD,
    DAILY_CLOSING,
    REPORTS,
    SETTINGS
}

@Composable
fun MoreScreen(
    viewModel: ShopViewModel,
    initialSubScreen: MoreSubScreen = MoreSubScreen.MENU,
    onSignOut: () -> Unit,
    openAddPurchaseTrigger: Boolean = false,
    openAddExpenseTrigger: Boolean = false,
    onResetAddPurchaseTrigger: () -> Unit = {},
    onResetAddExpenseTrigger: () -> Unit = {}
) {
    var currentSubScreen by remember { mutableStateOf(initialSubScreen) }

    LaunchedEffect(openAddPurchaseTrigger) {
        if (openAddPurchaseTrigger) {
            currentSubScreen = MoreSubScreen.PURCHASES
            onResetAddPurchaseTrigger()
        }
    }

    LaunchedEffect(openAddExpenseTrigger) {
        if (openAddExpenseTrigger) {
            currentSubScreen = MoreSubScreen.EXPENSES
            onResetAddExpenseTrigger()
        }
    }

    androidx.activity.compose.BackHandler(enabled = currentSubScreen != MoreSubScreen.MENU) {
        currentSubScreen = MoreSubScreen.MENU
    }

    when (currentSubScreen) {
        MoreSubScreen.MENU -> {
            MoreMenuHub(
                onNavigate = { currentSubScreen = it },
                onSignOut = onSignOut
            )
        }
        MoreSubScreen.PURCHASES -> {
            PurchasesView(
                viewModel = viewModel,
                onBack = { currentSubScreen = MoreSubScreen.MENU }
            )
        }
        MoreSubScreen.EXPENSES -> {
            ExpensesView(
                viewModel = viewModel,
                onBack = { currentSubScreen = MoreSubScreen.MENU }
            )
        }
        MoreSubScreen.LOANS -> {
            PersonalLoansView(
                viewModel = viewModel,
                onBack = { currentSubScreen = MoreSubScreen.MENU }
            )
        }
        MoreSubScreen.MOBILE_BANKING -> {
            MobileBankingView(
                viewModel = viewModel,
                onBack = { currentSubScreen = MoreSubScreen.MENU }
            )
        }
        MoreSubScreen.FLEXILOAD -> {
            FlexiloadView(
                viewModel = viewModel,
                onBack = { currentSubScreen = MoreSubScreen.MENU }
            )
        }
        MoreSubScreen.DAILY_CLOSING -> {
            DailyClosingView(
                viewModel = viewModel,
                onBack = { currentSubScreen = MoreSubScreen.MENU }
            )
        }
        MoreSubScreen.REPORTS -> {
            ReportsView(
                viewModel = viewModel,
                onBack = { currentSubScreen = MoreSubScreen.MENU }
            )
        }
        MoreSubScreen.SETTINGS -> {
            SettingsView(
                viewModel = viewModel,
                onBack = { currentSubScreen = MoreSubScreen.MENU },
                onSignOut = onSignOut
            )
        }
    }
}

@Composable
fun MoreMenuHub(
    onNavigate: (MoreSubScreen) -> Unit,
    onSignOut: () -> Unit
) {
    var showLogoutConfirm by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = "অন্যান্য ব্যবস্থাপনা (More Features)",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = YasinNavy
            )
            Text(
                text = "দোকানের সকল অভ্যন্তরীণ হিসাব ও টুলস",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
            MoreMenuItem(
                title = "ক্রয় ব্যবস্থাপনা (Purchases)",
                subtitle = "মহাজন / সাপ্লায়ারের কাছ থেকে পণ্য ক্রয় ও স্টক বৃদ্ধি",
                icon = Icons.Default.LocalShipping,
                iconTint = YasinBlue,
                onClick = { onNavigate(MoreSubScreen.PURCHASES) }
            )
        }

        item {
            MoreMenuItem(
                title = "দৈনিক দোকান খরচ (Expenses)",
                subtitle = "দোকান ভাড়া, বিদ্যুৎ বিল, খাবার, বেতন ও অন্যান্য খরচ",
                icon = Icons.Default.ReceiptLong,
                iconTint = StatusRed,
                onClick = { onNavigate(MoreSubScreen.EXPENSES) }
            )
        }

        item {
            MoreMenuItem(
                title = "ব্যক্তিগত ধার-দেনা (Personal Loans)",
                subtitle = "দোকান বা ব্যক্তিগত টাকা ধার দেওয়া ও নেওয়ার হিসাব",
                icon = Icons.Default.Handshake,
                iconTint = YasinNavy,
                onClick = { onNavigate(MoreSubScreen.LOANS) }
            )
        }

        item {
            MoreMenuItem(
                title = "বিকাশ ও নগদ (bKash & Nagad)",
                subtitle = "ক্যাশ-ইন, ক্যাশ-আউট, সেন্ড মানি ও কমিশন লাভ",
                icon = Icons.Default.AccountBalanceWallet,
                iconTint = Color(0xFFE91E63),
                onClick = { onNavigate(MoreSubScreen.MOBILE_BANKING) }
            )
        }

        item {
            MoreMenuItem(
                title = "ফ্লেক্সিলোড (Flexiload / Recharge)",
                subtitle = "জিপি, বাংলালিংক, রবি, এয়ারটেল, টেলিটক রিচার্জ ও লাভ",
                icon = Icons.Default.SendToMobile,
                iconTint = Color(0xFF009688),
                onClick = { onNavigate(MoreSubScreen.FLEXILOAD) }
            )
        }

        item {
            MoreMenuItem(
                title = "দৈনিক ক্লোজিং (Daily Closing)",
                subtitle = "দিনশেষে মোট হিসাব মেলানো ও ক্যাশ ইন হ্যান্ড গণনা",
                icon = Icons.Default.Assessment,
                iconTint = YasinGreen,
                onClick = { onNavigate(MoreSubScreen.DAILY_CLOSING) }
            )
        }

        item {
            MoreMenuItem(
                title = "রিপোর্ট ও হিস্ট্রি (Reports & Export)",
                subtitle = "আজ, ৭ দিন, মাসের লাভ-লোকসান ও CSV এক্সপোর্ট",
                icon = Icons.Default.BarChart,
                iconTint = Color(0xFF7C3AED),
                onClick = { onNavigate(MoreSubScreen.REPORTS) }
            )
        }

        item {
            MoreMenuItem(
                title = "সেটিংস ও ব্যাকআপ (Settings)",
                subtitle = "দোকানের নাম, ঠিকানা, ব্যাকআপ রিস্টোর ও ক্যাটাগরি",
                icon = Icons.Default.Settings,
                iconTint = Color(0xFF475569),
                onClick = { onNavigate(MoreSubScreen.SETTINGS) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedButton(
                onClick = { showLogoutConfirm = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusRed)
            ) {
                Icon(Icons.Default.ExitToApp, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("সাইন আউট করুন (Sign Out)")
            }
        }
    }

    if (showLogoutConfirm) {
        ConfirmDeleteDialog(
            title = "সাইন আউট নিশ্চিতকরণ",
            message = "আপনি কি নিশ্চিতভাবে বর্তমান একাউন্ট থেকে সাইন আউট করতে চান?",
            onConfirm = {
                showLogoutConfirm = false
                onSignOut()
            },
            onDismiss = { showLogoutConfirm = false }
        )
    }
}

@Composable
fun MoreMenuItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(iconTint.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 15.sp)
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextSecondary)
        }
    }
}

// -------------------------------------------------------------------------------------
// 1. Purchases View
// -------------------------------------------------------------------------------------
@Composable
fun PurchasesView(viewModel: ShopViewModel, onBack: () -> Unit) {
    val purchases by viewModel.purchases.collectAsStateWithLifecycle()
    val products by viewModel.products.collectAsStateWithLifecycle()
    val today = viewModel.todayDateStr

    var showAddDialog by remember { mutableStateOf(false) }
    var purchaseToDelete by remember { mutableStateOf<Purchase?>(null) }

    Scaffold(
        topBar = {
            SubScreenTopBar(title = "ক্রয় ব্যবস্থাপনা (Purchases)", onBack = onBack)
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = YasinBlue,
                contentColor = Color.White,
                modifier = Modifier.padding(bottom = 64.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "ক্রয় যোগ")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundLight)
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            val totalPurchasedAmount = purchases.sumOf { it.totalAmount }
            Spacer(modifier = Modifier.height(10.dp))
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = YasinBlue),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "সর্বমোট ক্রয় ব্যয়", color = Color.White.copy(alpha = 0.85f), fontSize = 12.sp)
                        Text(text = formatTaka(totalPurchasedAmount), fontWeight = FontWeight.Bold, color = Color.White, fontSize = 20.sp)
                    }
                    Text(text = "${purchases.size}টি ভাউচার", color = Color.White, fontWeight = FontWeight.Medium)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            if (purchases.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "কোনো ক্রয়ের রেকর্ড নেই। নিচে + চাপুন।", color = TextSecondary)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 120.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(purchases, key = { it.id }) { purchase ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = purchase.productName, fontWeight = FontWeight.Bold, color = TextPrimary)
                                        Text(
                                            text = "সাপ্লায়ার: ${if (purchase.supplierName.isNotBlank()) purchase.supplierName else "নগদ"} • তারিখ: ${purchase.purchaseDate}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextSecondary
                                        )
                                    }
                                    IconButton(
                                        onClick = { purchaseToDelete = purchase },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = StatusRed)
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${purchase.quantity} পিস × ${formatTaka(purchase.purchasePrice)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                    Text(
                                        text = formatTaka(purchase.totalAmount),
                                        fontWeight = FontWeight.Bold,
                                        color = YasinNavy,
                                        fontSize = 15.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddPurchaseDialog(
            products = products,
            today = today,
            onDismiss = { showAddDialog = false },
            onSave = { purchase ->
                viewModel.recordPurchase(purchase) { showAddDialog = false }
            }
        )
    }

    if (purchaseToDelete != null) {
        ConfirmDeleteDialog(
            title = "ক্রয় রেকর্ড মুছুন",
            message = "আপনি কি নিশ্চিতভাবে এই ক্রয় রেকর্ডটি মুছে ফেলতে চান?",
            onConfirm = {
                purchaseToDelete?.id?.let { viewModel.deletePurchase(it) }
                purchaseToDelete = null
            },
            onDismiss = { purchaseToDelete = null }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPurchaseDialog(
    products: List<Product>,
    today: String,
    onDismiss: () -> Unit,
    onSave: (Purchase) -> Unit
) {
    var selectedProduct by remember { mutableStateOf<Product?>(products.firstOrNull()) }
    var customProductName by remember { mutableStateOf("") }
    var supplierName by remember { mutableStateOf("") }
    var quantityStr by remember { mutableStateOf("10") }
    var purchasePriceStr by remember { mutableStateOf(selectedProduct?.purchasePrice?.toString() ?: "") }
    var notes by remember { mutableStateOf("") }
    var dropdownExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(selectedProduct) {
        if (selectedProduct != null) {
            purchasePriceStr = selectedProduct!!.purchasePrice.toString()
        }
    }

    val qty = quantityStr.toLongOrNull() ?: 0L
    val price = purchasePriceStr.toDoubleOrNull() ?: 0.0
    val totalAmount = qty * price

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text(text = "নতুন পণ্য ক্রয় রেকর্ড (Purchase)", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = YasinNavy)
                    Text(text = "ক্রয় সম্পন্ন হলে স্বয়ংক্রিয়ভাবে স্টক বৃদ্ধি পাবে।", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }

                item {
                    if (products.isNotEmpty()) {
                        ExposedDropdownMenuBox(
                            expanded = dropdownExpanded,
                            onExpandedChange = { dropdownExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = selectedProduct?.let { "${it.name} (বর্তমান স্টক: ${it.currentStock})" } ?: "বিদ্যমান পণ্য বাছাই করুন",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("দোকানের পণ্য লিংক করুন") },
                                modifier = Modifier.fillMaxWidth().menuAnchor(),
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) }
                            )
                            ExposedDropdownMenu(
                                expanded = dropdownExpanded,
                                onDismissRequest = { dropdownExpanded = false }
                            ) {
                                products.forEach { p ->
                                    DropdownMenuItem(
                                        text = { Text("${p.name} (স্টক: ${p.currentStock})") },
                                        onClick = {
                                            selectedProduct = p
                                            dropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    } else {
                        OutlinedTextField(
                            value = customProductName,
                            onValueChange = { customProductName = it },
                            label = { Text("পণ্যের নাম *") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                item {
                    OutlinedTextField(
                        value = supplierName,
                        onValueChange = { supplierName = it },
                        label = { Text("মহাজন / সাপ্লায়ারের নাম (ঐচ্ছিক)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = quantityStr,
                            onValueChange = { quantityStr = it },
                            label = { Text("পরিমাণ (পিস) *") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = purchasePriceStr,
                            onValueChange = { purchasePriceStr = it },
                            label = { Text("একক ক্রয়মূল্য (৳) *") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1.2f)
                        )
                    }
                }

                item {
                    Surface(shape = RoundedCornerShape(8.dp), color = YasinBlueLight, modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "সর্বমোট ক্রয়মূল্য:", fontWeight = FontWeight.Bold)
                            Text(text = formatTaka(totalAmount), fontWeight = FontWeight.Bold, color = YasinNavy)
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("নোট (ঐচ্ছিক)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onDismiss) { Text("বাতিল") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val pName = selectedProduct?.name ?: customProductName.trim()
                                if (pName.isNotBlank() && qty > 0) {
                                    val purchase = Purchase(
                                        productName = pName,
                                        productId = selectedProduct?.id ?: "",
                                        supplierName = supplierName.trim(),
                                        quantity = qty,
                                        purchasePrice = price,
                                        totalAmount = totalAmount,
                                        purchaseDate = today,
                                        notes = notes.trim()
                                    )
                                    onSave(purchase)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = YasinBlue),
                            enabled = (selectedProduct != null || customProductName.isNotBlank()) && qty > 0
                        ) {
                            Text("ক্রয় নিশ্চিত করুন")
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------
// 2. Expenses View
// -------------------------------------------------------------------------------------
@Composable
fun ExpensesView(viewModel: ShopViewModel, onBack: () -> Unit) {
    val expenses by viewModel.expenses.collectAsStateWithLifecycle()
    val today = viewModel.todayDateStr
    var showAddDialog by remember { mutableStateOf(false) }
    var expenseToDelete by remember { mutableStateOf<Expense?>(null) }

    Scaffold(
        topBar = { SubScreenTopBar(title = "দৈনিক দোকান খরচ (Expenses)", onBack = onBack) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = StatusRed,
                contentColor = Color.White,
                modifier = Modifier.padding(bottom = 64.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "খরচ যোগ")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundLight)
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            val totalExpense = expenses.sumOf { it.amount }
            val todayExpense = expenses.filter { it.expenseDate == today }.sumOf { it.amount }

            Spacer(modifier = Modifier.height(10.dp))
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = StatusRedBg),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "আজকের খরচ ($today)", color = StatusRed, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        Text(text = formatTaka(todayExpense), fontWeight = FontWeight.Bold, color = StatusRed, fontSize = 20.sp)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "সর্বমোট খরচ", color = TextSecondary, fontSize = 12.sp)
                        Text(text = formatTaka(totalExpense), fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 16.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            if (expenses.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "কোনো খরচের রেকর্ড নেই। নিচে + চাপুন।", color = TextSecondary)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 120.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(expenses, key = { it.id }) { exp ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = exp.title, fontWeight = FontWeight.Bold, color = TextPrimary)
                                    Text(
                                        text = "${exp.category} • ${exp.expenseDate} ${if (exp.description.isNotBlank()) "• ${exp.description}" else ""}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = formatTaka(exp.amount),
                                        fontWeight = FontWeight.Bold,
                                        color = StatusRed,
                                        fontSize = 16.sp
                                    )
                                    IconButton(
                                        onClick = { expenseToDelete = exp },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = StatusRed)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddExpenseDialog(
            today = today,
            onDismiss = { showAddDialog = false },
            onSave = { exp ->
                viewModel.recordExpense(exp) { showAddDialog = false }
            }
        )
    }

    if (expenseToDelete != null) {
        ConfirmDeleteDialog(
            title = "খরচ রেকর্ড মুছুন",
            message = "আপনি কি নিশ্চিতভাবে এই খরচ রেকর্ডটি মুছে ফেলতে চান?",
            onConfirm = {
                expenseToDelete?.id?.let { viewModel.deleteExpense(it) }
                expenseToDelete = null
            },
            onDismiss = { expenseToDelete = null }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseDialog(
    today: String,
    onDismiss: () -> Unit,
    onSave: (Expense) -> Unit
) {
    val expenseCategories = listOf(
        "Shop Rent",
        "Electricity Bill",
        "Transport",
        "Food",
        "Internet",
        "Employee Salary",
        "Other Expenses"
    )

    var title by remember { mutableStateOf("") }
    var amountStr by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(expenseCategories.first()) }
    var description by remember { mutableStateOf("") }
    var dropdownExpanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(text = "দোকানের খরচ যোগ করুন (Expense)", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = YasinNavy)

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("খরচের শিরোনাম (যেমন: নাস্তা / বিদ্যুৎ বিল) *") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("টাকার পরিমাণ (৳) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = dropdownExpanded,
                    onExpandedChange = { dropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("খরচের ক্যাটাগরি *") },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) }
                    )
                    ExposedDropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false }
                    ) {
                        expenseCategories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    category = cat
                                    dropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("বিস্তারিত বিবরণ (ঐচ্ছিক)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("বাতিল") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val amt = amountStr.toDoubleOrNull() ?: 0.0
                            if (title.isNotBlank() && amt > 0) {
                                val exp = Expense(
                                    title = title.trim(),
                                    amount = amt,
                                    category = category,
                                    expenseDate = today,
                                    description = description.trim()
                                )
                                onSave(exp)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = StatusRed),
                        enabled = title.isNotBlank() && (amountStr.toDoubleOrNull() ?: 0.0) > 0
                    ) {
                        Text("খরচ সংরক্ষণ করুন")
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------
// 3. Personal Loans & Borrowing
// -------------------------------------------------------------------------------------
@Composable
fun PersonalLoansView(viewModel: ShopViewModel, onBack: () -> Unit) {
    val loans by viewModel.loans.collectAsStateWithLifecycle()
    val today = viewModel.todayDateStr
    var showAddDialog by remember { mutableStateOf(false) }
    var loanForPayment by remember { mutableStateOf<Loan?>(null) }
    var loanToDelete by remember { mutableStateOf<Loan?>(null) }

    Scaffold(
        topBar = { SubScreenTopBar(title = "ব্যক্তিগত ধার-দেনা (Personal Loans)", onBack = onBack) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = YasinNavy,
                contentColor = Color.White,
                modifier = Modifier.padding(bottom = 64.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "ধার এন্ট্রি")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundLight)
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            val totalLent = loans.filter { it.loanType == "LENT" }.sumOf { it.remainingAmount }
            val totalBorrowed = loans.filter { it.loanType != "LENT" }.sumOf { it.remainingAmount }

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = YasinGreenLight),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(text = "আমি ধার দিয়েছি (পাওনা)", fontSize = 11.sp, color = YasinGreen, fontWeight = FontWeight.Bold)
                        Text(text = formatTaka(totalLent), fontWeight = FontWeight.Bold, color = YasinGreen, fontSize = 18.sp)
                    }
                }
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = StatusRedBg),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(text = "আমি ধার নিয়েছি (দেনা)", fontSize = 11.sp, color = StatusRed, fontWeight = FontWeight.Bold)
                        Text(text = formatTaka(totalBorrowed), fontWeight = FontWeight.Bold, color = StatusRed, fontSize = 18.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            if (loans.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "কোনো ধার-দেনার রেকর্ড নেই।", color = TextSecondary)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 120.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(loans, key = { it.id }) { loan ->
                        val isLent = loan.loanType == "LENT"
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = loan.personName, fontWeight = FontWeight.Bold, color = TextPrimary)
                                        Text(
                                            text = "${loan.mobile} • ${if (isLent) "আমি ধার দিয়েছি" else "আমি নিয়েছি (${loan.loanType})"} • তারিখ: ${loan.expectedPaymentDate}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextSecondary
                                        )
                                    }
                                    IconButton(
                                        onClick = { loanToDelete = loan },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = StatusRed)
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(text = "মোট: ${formatTaka(loan.amount)} | পরিশোধ: ${formatTaka(loan.paidAmount)}", fontSize = 12.sp, color = TextSecondary)
                                        Text(
                                            text = "বাকি: ${formatTaka(loan.remainingAmount)}",
                                            fontWeight = FontWeight.Bold,
                                            color = if (isLent) YasinGreen else StatusRed,
                                            fontSize = 15.sp
                                        )
                                    }
                                    if (loan.remainingAmount > 0) {
                                        OutlinedButton(
                                            onClick = { loanForPayment = loan },
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("পরিশোধ লিখুন", fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddLoanDialog(
            today = today,
            onDismiss = { showAddDialog = false },
            onSave = { loan ->
                viewModel.addLoan(loan) { showAddDialog = false }
            }
        )
    }

    if (loanForPayment != null) {
        RecordLoanPaymentDialog(
            loan = loanForPayment!!,
            onDismiss = { loanForPayment = null },
            onSave = { amt ->
                loanForPayment?.id?.let {
                    viewModel.recordLoanPayment(it, amt) { loanForPayment = null }
                }
            }
        )
    }

    if (loanToDelete != null) {
        ConfirmDeleteDialog(
            title = "ধার রেকর্ড মুছুন",
            message = "আপনি কি নিশ্চিতভাবে এই রেকর্ডটি মুছে ফেলতে চান?",
            onConfirm = {
                loanToDelete?.id?.let { viewModel.deleteLoan(it) }
                loanToDelete = null
            },
            onDismiss = { loanToDelete = null }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddLoanDialog(today: String, onDismiss: () -> Unit, onSave: (Loan) -> Unit) {
    val loanTypes = listOf(
        "LENT" to "আমি কাউকে টাকা ধার দিয়েছি",
        "BORROWED" to "আমি কারো কাছ থেকে ধার নিয়েছি",
        "HOUSEHOLD" to "Household Loan",
        "PERSONAL" to "Personal Loan",
        "FAMILY" to "Family Loan",
        "OTHER" to "Other Loans"
    )

    var personName by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(loanTypes.first().first) }
    var amountStr by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var dropdownExpanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(text = "ব্যক্তিগত ধার-দেনা এন্ট্রি", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = YasinNavy)

                OutlinedTextField(
                    value = personName,
                    onValueChange = { personName = it },
                    label = { Text("ব্যক্তির নাম *") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = mobile,
                    onValueChange = { mobile = it },
                    label = { Text("মোবাইল নাম্বার") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = dropdownExpanded,
                    onExpandedChange = { dropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = loanTypes.find { it.first == selectedType }?.second ?: selectedType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("লেনদেনের ধরণ *") },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) }
                    )
                    ExposedDropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false }
                    ) {
                        loanTypes.forEach { (typeKey, typeLabel) ->
                            DropdownMenuItem(
                                text = { Text(typeLabel) },
                                onClick = {
                                    selectedType = typeKey
                                    dropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("টাকার পরিমাণ (৳) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("বিবরণ / প্রত্যাশিত ফেরত তারিখ") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("বাতিল") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val amt = amountStr.toDoubleOrNull() ?: 0.0
                            if (personName.isNotBlank() && amt > 0) {
                                val loan = Loan(
                                    personName = personName.trim(),
                                    mobile = mobile.trim(),
                                    loanType = selectedType,
                                    amount = amt,
                                    paidAmount = 0.0,
                                    remainingAmount = amt,
                                    description = description.trim(),
                                    expectedPaymentDate = today
                                )
                                onSave(loan)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = YasinNavy),
                        enabled = personName.isNotBlank() && (amountStr.toDoubleOrNull() ?: 0.0) > 0
                    ) {
                        Text("সংরক্ষণ করুন")
                    }
                }
            }
        }
    }
}

@Composable
fun RecordLoanPaymentDialog(loan: Loan, onDismiss: () -> Unit, onSave: (Double) -> Unit) {
    var paymentStr by remember { mutableStateOf("") }
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(text = "${loan.personName} - পরিশোধ রেকর্ড", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = YasinNavy)
                Text(text = "অবশিষ্ট বাকি: ${formatTaka(loan.remainingAmount)}", color = TextSecondary)

                OutlinedTextField(
                    value = paymentStr,
                    onValueChange = { paymentStr = it },
                    label = { Text("পরিশোধিত টাকার পরিমাণ (৳) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("বাতিল") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val amt = paymentStr.toDoubleOrNull() ?: 0.0
                            if (amt > 0) onSave(amt)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = YasinGreen),
                        enabled = (paymentStr.toDoubleOrNull() ?: 0.0) > 0
                    ) {
                        Text("পরিশোধ জমা দিন")
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------
// 4. Mobile Banking (bKash & Nagad)
// -------------------------------------------------------------------------------------
@Composable
fun MobileBankingView(viewModel: ShopViewModel, onBack: () -> Unit) {
    val list by viewModel.mobileBanking.collectAsStateWithLifecycle()
    val today = viewModel.todayDateStr
    var showAddDialog by remember { mutableStateOf(false) }
    var itemToDelete by remember { mutableStateOf<MobileBankingTx?>(null) }

    Scaffold(
        topBar = { SubScreenTopBar(title = "বিকাশ ও নগদ (Mobile Banking)", onBack = onBack) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Color(0xFFE91E63),
                contentColor = Color.White,
                modifier = Modifier.padding(bottom = 64.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "লেনদেন এন্ট্রি")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundLight)
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            val totalProfit = list.sumOf { it.commissionProfit }
            Spacer(modifier = Modifier.height(10.dp))
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE91E63)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "মোবাইল ব্যাংকিং মোট কমিশন / লাভ", color = Color.White.copy(alpha = 0.85f), fontSize = 12.sp)
                        Text(text = formatTaka(totalProfit), fontWeight = FontWeight.Bold, color = Color.White, fontSize = 20.sp)
                    }
                    Text(text = "${list.size}টি এন্ট্রি", color = Color.White)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            if (list.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "কোনো মোবাইল ব্যাংকিং লেনদেন নেই।", color = TextSecondary)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 120.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(list, key = { it.id }) { tx ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${tx.provider} - ${tx.type}",
                                        fontWeight = FontWeight.Bold,
                                        color = if (tx.provider == "BKASH") Color(0xFFE91E63) else Color(0xFFFF9800)
                                    )
                                    Text(
                                        text = "নম্বর: ${tx.customerPhone} • তারিখ: ${tx.transactionDate} ${if (tx.notes.isNotBlank()) "• ${tx.notes}" else ""}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                    Text(
                                        text = "কমিশন লাভ: ${formatTaka(tx.commissionProfit)}",
                                        fontWeight = FontWeight.SemiBold,
                                        color = YasinGreen,
                                        fontSize = 12.sp
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = formatTaka(tx.amount), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    IconButton(
                                        onClick = { itemToDelete = tx },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = StatusRed)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddMobileBankingDialog(
            today = today,
            onDismiss = { showAddDialog = false },
            onSave = { tx ->
                viewModel.recordMobileBanking(tx) { showAddDialog = false }
            }
        )
    }

    if (itemToDelete != null) {
        ConfirmDeleteDialog(
            title = "লেনদেন মুছুন",
            message = "আপনি কি নিশ্চিতভাবে এই লেনদেনটি মুছে ফেলতে চান?",
            onConfirm = {
                itemToDelete?.id?.let { viewModel.deleteMobileBanking(it) }
                itemToDelete = null
            },
            onDismiss = { itemToDelete = null }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMobileBankingDialog(today: String, onDismiss: () -> Unit, onSave: (MobileBankingTx) -> Unit) {
    var provider by remember { mutableStateOf("BKASH") } // BKASH or NAGAD
    var type by remember { mutableStateOf("CASH_IN") } // CASH_IN, CASH_OUT, SEND_MONEY
    var amountStr by remember { mutableStateOf("") }
    var commissionStr by remember { mutableStateOf("") }
    var customerPhone by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(text = "বিকাশ / নগদ লেনদেন এন্ট্রি", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = YasinNavy)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = provider == "BKASH",
                        onClick = { provider = "BKASH" },
                        label = { Text("bKash (বিকাশ)") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = provider == "NAGAD",
                        onClick = { provider = "NAGAD" },
                        label = { Text("Nagad (নগদ)") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(
                        selected = type == "CASH_IN",
                        onClick = { type = "CASH_IN" },
                        label = { Text("ক্যাশ-ইন") }
                    )
                    FilterChip(
                        selected = type == "CASH_OUT",
                        onClick = { type = "CASH_OUT" },
                        label = { Text("ক্যাশ-আউট") }
                    )
                    FilterChip(
                        selected = type == "SEND_MONEY",
                        onClick = { type = "SEND_MONEY" },
                        label = { Text("সেন্ড মানি") }
                    )
                }

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = {
                        amountStr = it
                        val amt = it.toDoubleOrNull() ?: 0.0
                        if (commissionStr.isBlank()) {
                            // Suggest approx commission
                            val comm = if (type == "CASH_OUT") (amt * 0.0041).coerceAtLeast(0.0) else 0.0
                            if (comm > 0) commissionStr = String.format(java.util.Locale.US, "%.1f", comm)
                        }
                    },
                    label = { Text("টাকার পরিমাণ (৳) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = commissionStr,
                    onValueChange = { commissionStr = it },
                    label = { Text("কমিশন / লাভ (৳)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = customerPhone,
                    onValueChange = { customerPhone = it },
                    label = { Text("গ্রাহকের নম্বর (ঐচ্ছিক)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("নোট (ঐচ্ছিক)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("বাতিল") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val amt = amountStr.toDoubleOrNull() ?: 0.0
                            val comm = commissionStr.toDoubleOrNull() ?: 0.0
                            if (amt > 0) {
                                val tx = MobileBankingTx(
                                    provider = provider,
                                    type = type,
                                    amount = amt,
                                    commissionProfit = comm,
                                    customerPhone = customerPhone.trim(),
                                    notes = notes.trim(),
                                    transactionDate = today
                                )
                                onSave(tx)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = if (provider == "BKASH") Color(0xFFE91E63) else Color(0xFFFF9800)),
                        enabled = (amountStr.toDoubleOrNull() ?: 0.0) > 0
                    ) {
                        Text("সংরক্ষণ করুন")
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------
// 5. Flexiload
// -------------------------------------------------------------------------------------
@Composable
fun FlexiloadView(viewModel: ShopViewModel, onBack: () -> Unit) {
    val list by viewModel.flexiload.collectAsStateWithLifecycle()
    val today = viewModel.todayDateStr
    var showAddDialog by remember { mutableStateOf(false) }
    var itemToDelete by remember { mutableStateOf<FlexiloadTx?>(null) }

    Scaffold(
        topBar = { SubScreenTopBar(title = "ফ্লেক্সিলোড / রিচার্জ (Flexiload)", onBack = onBack) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Color(0xFF009688),
                contentColor = Color.White,
                modifier = Modifier.padding(bottom = 64.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "রিচার্জ এন্ট্রি")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundLight)
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            val totalRecharge = list.sumOf { it.rechargeAmount }
            val totalProfit = list.sumOf { it.profit }

            Spacer(modifier = Modifier.height(10.dp))
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF009688)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "মোট রিচার্জ", color = Color.White.copy(alpha = 0.85f), fontSize = 12.sp)
                        Text(text = formatTaka(totalRecharge), fontWeight = FontWeight.Bold, color = Color.White, fontSize = 20.sp)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "মোট লাভ", color = Color.White.copy(alpha = 0.85f), fontSize = 12.sp)
                        Text(text = formatTaka(totalProfit), fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            if (list.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "কোনো রিচার্জ রেকর্ড নেই।", color = TextSecondary)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 120.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(list, key = { it.id }) { tx ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = "${tx.operator} - ${tx.mobileNumber}", fontWeight = FontWeight.Bold, color = TextPrimary)
                                    Text(text = "তারিখ: ${tx.transactionDate} • লাভ: ${formatTaka(tx.profit)}", style = MaterialTheme.typography.bodySmall, color = YasinGreen)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = formatTaka(tx.rechargeAmount), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    IconButton(
                                        onClick = { itemToDelete = tx },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = StatusRed)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddFlexiloadDialog(
            today = today,
            onDismiss = { showAddDialog = false },
            onSave = { tx ->
                viewModel.recordFlexiload(tx) { showAddDialog = false }
            }
        )
    }

    if (itemToDelete != null) {
        ConfirmDeleteDialog(
            title = "রিচার্জ মুছুন",
            message = "আপনি কি নিশ্চিতভাবে এই রেকর্ডটি মুছে ফেলতে চান?",
            onConfirm = {
                itemToDelete?.id?.let { viewModel.deleteFlexiload(it) }
                itemToDelete = null
            },
            onDismiss = { itemToDelete = null }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFlexiloadDialog(today: String, onDismiss: () -> Unit, onSave: (FlexiloadTx) -> Unit) {
    val operators = listOf("Grameenphone", "Banglalink", "Robi", "Airtel", "Teletalk")
    var operator by remember { mutableStateOf(operators.first()) }
    var mobile by remember { mutableStateOf("") }
    var amountStr by remember { mutableStateOf("") }
    var profitStr by remember { mutableStateOf("0") }
    var notes by remember { mutableStateOf("") }
    var dropdownExpanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(text = "ফ্লেক্সিলোড এন্ট্রি", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = YasinNavy)

                ExposedDropdownMenuBox(
                    expanded = dropdownExpanded,
                    onExpandedChange = { dropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = operator,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("অপারেটর *") },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) }
                    )
                    ExposedDropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false }
                    ) {
                        operators.forEach { op ->
                            DropdownMenuItem(
                                text = { Text(op) },
                                onClick = {
                                    operator = op
                                    dropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = mobile,
                    onValueChange = { mobile = it },
                    label = { Text("মোবাইল নম্বর *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = {
                        amountStr = it
                        val amt = it.toDoubleOrNull() ?: 0.0
                        // Default ~2.5% commission estimate
                        profitStr = String.format(java.util.Locale.US, "%.1f", amt * 0.025)
                    },
                    label = { Text("রিচার্জের পরিমাণ (৳) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = profitStr,
                    onValueChange = { profitStr = it },
                    label = { Text("লাভ / কমিশন (৳)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("বাতিল") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val amt = amountStr.toDoubleOrNull() ?: 0.0
                            val profit = profitStr.toDoubleOrNull() ?: 0.0
                            if (mobile.isNotBlank() && amt > 0) {
                                val tx = FlexiloadTx(
                                    mobileNumber = mobile.trim(),
                                    operator = operator,
                                    rechargeAmount = amt,
                                    customerPayment = amt,
                                    rechargeCost = amt - profit,
                                    profit = profit,
                                    transactionDate = today,
                                    notes = notes.trim()
                                )
                                onSave(tx)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF009688)),
                        enabled = mobile.isNotBlank() && (amountStr.toDoubleOrNull() ?: 0.0) > 0
                    ) {
                        Text("রিচার্জ সংরক্ষণ")
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------
// 6. Daily Closing View
// -------------------------------------------------------------------------------------
@Composable
fun DailyClosingView(viewModel: ShopViewModel, onBack: () -> Unit) {
    val today = viewModel.todayDateStr
    val sales by viewModel.sales.collectAsStateWithLifecycle()
    val expenses by viewModel.expenses.collectAsStateWithLifecycle()
    val mbList by viewModel.mobileBanking.collectAsStateWithLifecycle()
    val flexiList by viewModel.flexiload.collectAsStateWithLifecycle()
    val closings by viewModel.dailyClosings.collectAsStateWithLifecycle()

    val todaySales = sales.filter { it.saleDate == today }.sumOf { it.totalAmount }
    val todaySaleProfit = sales.filter { it.saleDate == today }.sumOf { it.grossProfit }
    val todayMbVol = mbList.filter { it.transactionDate == today }.sumOf { it.amount }
    val todayMbProfit = mbList.filter { it.transactionDate == today }.sumOf { it.commissionProfit }
    val todayFlexiVol = flexiList.filter { it.transactionDate == today }.sumOf { it.rechargeAmount }
    val todayFlexiProfit = flexiList.filter { it.transactionDate == today }.sumOf { it.profit }
    val todayGrossProfit = todaySaleProfit + todayMbProfit + todayFlexiProfit
    val todayExpenses = expenses.filter { it.expenseDate == today }.sumOf { it.amount }

    val todayCashReceived = sales.filter { it.saleDate == today }.sumOf { it.paidAmount } +
            mbList.filter { it.transactionDate == today && it.type == "CASH_IN" }.sumOf { it.amount }
    val todayCashPaid = expenses.filter { it.expenseDate == today }.sumOf { it.amount } +
            mbList.filter { it.transactionDate == today && it.type == "CASH_OUT" }.sumOf { it.amount }

    val expectedCash = (todayCashReceived - todayCashPaid).coerceAtLeast(0.0)

    var actualCashStr by remember { mutableStateOf(expectedCash.toString()) }
    val actualCash = actualCashStr.toDoubleOrNull() ?: 0.0
    val cashDifference = actualCash - expectedCash
    var notes by remember { mutableStateOf("") }
    var showSavedNotification by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { SubScreenTopBar(title = "দৈনিক দোকান ক্লোজিং (Daily Closing)", onBack = onBack) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundLight)
                .padding(padding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 10.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = YasinNavy),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "আজকের ক্লোজিং তারিখ: $today", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(text = "দিনশেষে ক্যাশ ড্রয়ার ও মোট বিক্রির হিসাব মেলান", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                    }
                }
            }

            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(text = "আজকের হিসাব খতিয়ান:", fontWeight = FontWeight.Bold)
                        ClosingRow(label = "১. মোট পণ্য বিক্রয়", value = formatTaka(todaySales))
                        ClosingRow(label = "২. মোবাইল ব্যাংকিং ভলিউম", value = formatTaka(todayMbVol))
                        ClosingRow(label = "৩. ফ্লেক্সিলোড ভলিউম", value = formatTaka(todayFlexiVol))
                        ClosingRow(label = "৪. মোট গ্রস লাভ", value = formatTaka(todayGrossProfit), valueColor = StatusGreen)
                        ClosingRow(label = "৫. আজকের মোট খরচ", value = formatTaka(todayExpenses), valueColor = StatusRed)
                        Divider(color = BorderLight)
                        ClosingRow(label = "৬. আনুমানিক নগদ ক্যাশ ইন হ্যান্ড (Expected Cash)", value = formatTaka(expectedCash), isBold = true)
                    }
                }
            }

            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(text = "বাস্তব ড্রয়ার ক্যাশ ইনপুট:", fontWeight = FontWeight.Bold)
                        OutlinedTextField(
                            value = actualCashStr,
                            onValueChange = { actualCashStr = it },
                            label = { Text("ড্রয়ারে থাকা মোট নগদ টাকা (৳) *") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = when {
                                cashDifference == 0.0 -> StatusGreenBg
                                cashDifference > 0 -> YasinBlueLight
                                else -> StatusRedBg
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = when {
                                        cashDifference == 0.0 -> "ক্যাশ একদম নির্ভুল মিলেছে!"
                                        cashDifference > 0 -> "ক্যাশ বেশি আছে (+${formatTaka(cashDifference)})"
                                        else -> "ক্যাশ কম আছে (${formatTaka(cashDifference)})"
                                    },
                                    fontWeight = FontWeight.Bold,
                                    color = if (cashDifference < 0) StatusRed else StatusGreen
                                )
                                Text(text = formatTaka(cashDifference), fontWeight = FontWeight.Bold)
                            }
                        }

                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text("ক্লোজিং মন্তব্য বা নোট (ঐচ্ছিক)") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Button(
                            onClick = {
                                val report = DailyClosing(
                                    closingDate = today,
                                    productSales = todaySales,
                                    mobileBankingVolume = todayMbVol,
                                    flexiloadVolume = todayFlexiVol,
                                    grossProfit = todayGrossProfit,
                                    totalExpenses = todayExpenses,
                                    customerPaymentsReceived = 0.0,
                                    totalCashReceived = todayCashReceived,
                                    totalCashPaid = todayCashPaid,
                                    expectedCash = expectedCash,
                                    actualCash = actualCash,
                                    cashDifference = cashDifference,
                                    notes = notes.trim()
                                )
                                viewModel.recordDailyClosing(report) {
                                    showSavedNotification = true
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = YasinGreen),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("আজকের ক্লোজিং রিপোর্ট সেভ করুন")
                        }

                        if (showSavedNotification) {
                            Text(text = "✓ ক্লোজিং রিপোর্ট সফলভাবে সংরক্ষিত হয়েছে!", color = StatusGreen, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            if (closings.isNotEmpty()) {
                item {
                    SectionHeader(title = "পূর্ববর্তী ক্লোজিং রিপোর্টসমূহ (${closings.size}টি)")
                }
                items(closings) { item ->
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = item.closingDate, fontWeight = FontWeight.Bold)
                                Text(text = "বিক্রয়: ${formatTaka(item.productSales)}", color = YasinGreen, fontWeight = FontWeight.SemiBold)
                            }
                            Text(
                                text = "প্রত্যাশিত ক্যাশ: ${formatTaka(item.expectedCash)} | বাস্তব ক্যাশ: ${formatTaka(item.actualCash)} (পার্থক্য: ${formatTaka(item.cashDifference)})",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ClosingRow(
    label: String,
    value: String,
    valueColor: Color = TextPrimary,
    isBold: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = if (isBold) TextPrimary else TextSecondary, fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, color = valueColor, fontWeight = if (isBold) FontWeight.Bold else FontWeight.SemiBold)
    }
}

// -------------------------------------------------------------------------------------
// 7. Reports & CSV Export View
// -------------------------------------------------------------------------------------
@Composable
fun ReportsView(viewModel: ShopViewModel, onBack: () -> Unit) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val products by viewModel.products.collectAsStateWithLifecycle()
    val sales by viewModel.sales.collectAsStateWithLifecycle()
    val expenses by viewModel.expenses.collectAsStateWithLifecycle()

    var exportNotice by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = { SubScreenTopBar(title = "রিপোর্ট ও হিস্ট্রি (Reports)", onBack = onBack) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundLight)
                .padding(padding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 10.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(text = "দোকানের মোট আর্থিক পরিসংখ্যান", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        ClosingRow(label = "মোট বিক্রয় সংখ্যা", value = "${sales.size}টি ইনভয়েস")
                        ClosingRow(label = "সর্বমোট বিক্রয় মূল্য", value = formatTaka(sales.sumOf { it.totalAmount }))
                        ClosingRow(label = "সর্বমোট বিক্রয় লাভ", value = formatTaka(sales.sumOf { it.grossProfit }), valueColor = StatusGreen)
                        ClosingRow(label = "সর্বমোট দোকান খরচ", value = formatTaka(expenses.sumOf { it.amount }), valueColor = StatusRed)
                        val net = sales.sumOf { it.grossProfit } - expenses.sumOf { it.amount }
                        Divider(color = BorderLight)
                        ClosingRow(label = "নিট লাভ (Net Profit)", value = formatTaka(net), isBold = true, valueColor = if (net >= 0) StatusGreen else StatusRed)
                    }
                }
            }

            item {
                SectionHeader(title = "CSV ডাটা এক্সপোর্ট (Excel / Spreadsheet)")
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "আপনার যেকোনো ডিভাইসে বা এক্সেলে দেখার জন্য ডাটা কপি বা ডাউনলোড করুন:",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )

                        Button(
                            onClick = {
                                val sb = StringBuilder("Name,Category,Brand,Stock,PurchasePrice,SellingPrice\n")
                                products.forEach { p ->
                                    sb.append("\"${p.name}\",\"${p.category}\",\"${p.brand}\",${p.currentStock},${p.purchasePrice},${p.sellingPrice}\n")
                                }
                                clipboardManager.setText(AnnotatedString(sb.toString()))
                                exportNotice = "পণ্য তালিকার CSV ক্লিপবোর্ডে কপি করা হয়েছে!"
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = YasinNavy),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("পণ্য তালিকা CSV কপি করুন")
                        }

                        Button(
                            onClick = {
                                val sb = StringBuilder("Invoice,Date,Customer,TotalAmount,Cost,Profit,Paid,Due\n")
                                sales.forEach { s ->
                                    sb.append("\"${s.invoiceNumber}\",\"${s.saleDate}\",\"${s.customerName}\",${s.totalAmount},${s.totalCost},${s.grossProfit},${s.paidAmount},${s.dueAmount}\n")
                                }
                                clipboardManager.setText(AnnotatedString(sb.toString()))
                                exportNotice = "বিক্রয় রসিদসমূহের CSV ক্লিপবোর্ডে কপি করা হয়েছে!"
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = YasinGreen),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("বিক্রয় রিপোর্ট CSV কপি করুন")
                        }

                        if (exportNotice != null) {
                            Surface(shape = RoundedCornerShape(8.dp), color = StatusGreenBg, modifier = Modifier.fillMaxWidth()) {
                                Text(text = exportNotice ?: "", color = StatusGreen, fontWeight = FontWeight.Bold, modifier = Modifier.padding(10.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------
// 8. Settings & Backup/Restore View
// -------------------------------------------------------------------------------------
@Composable
fun SettingsView(
    viewModel: ShopViewModel,
    onBack: () -> Unit,
    onSignOut: () -> Unit
) {
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val customCategories by viewModel.customCategories.collectAsStateWithLifecycle()
    val clipboardManager = LocalClipboardManager.current

    var shopName by remember(profile) { mutableStateOf(profile?.shopName ?: "ইয়াসিন টেলিকম") }
    var ownerName by remember(profile) { mutableStateOf(profile?.ownerName ?: "মোহাম্মদ ইয়াসিন") }
    var address by remember(profile) { mutableStateOf(profile?.address ?: "বুল্লা বাজার, লাখাই, হবিগঞ্জ, বাংলাদেশ") }
    var thresholdStr by remember(profile) { mutableStateOf((profile?.lowStockAlertThreshold ?: 5L).toString()) }

    var newCategoryName by remember { mutableStateOf("") }
    var backupJsonStr by remember { mutableStateOf("") }
    var restoreInputStr by remember { mutableStateOf("") }
    var showBackupModal by remember { mutableStateOf(false) }
    var showRestoreModal by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { SubScreenTopBar(title = "দোকান সেটিংস ও ব্যাকআপ", onBack = onBack) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundLight)
                .padding(padding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 10.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Shop Details
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(text = "দোকানের সাধারণ তথ্য", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = YasinNavy)

                        OutlinedTextField(
                            value = shopName,
                            onValueChange = { shopName = it },
                            label = { Text("দোকানের নাম *") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = ownerName,
                            onValueChange = { ownerName = it },
                            label = { Text("স্বত্বাধিকারীর নাম *") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = address,
                            onValueChange = { address = it },
                            label = { Text("দোকানের ঠিকানা *") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = thresholdStr,
                            onValueChange = { thresholdStr = it },
                            label = { Text("লো স্টক অ্যালার্ট সীমা (ডিফল্ট)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Button(
                            onClick = {
                                val threshold = thresholdStr.toLongOrNull() ?: 5L
                                val updated = ShopProfile(
                                    shopName = shopName.trim(),
                                    ownerName = ownerName.trim(),
                                    address = address.trim(),
                                    lowStockAlertThreshold = threshold
                                )
                                viewModel.updateProfile(updated)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = YasinGreen),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("তথ্য সেভ করুন")
                        }
                    }
                }
            }

            // Custom Categories
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(text = "কাস্টম ক্যাটাগরি তৈরি করুন", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = YasinNavy)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = newCategoryName,
                                onValueChange = { newCategoryName = it },
                                label = { Text("নতুন ক্যাটাগরি নাম") },
                                modifier = Modifier.weight(1f)
                            )
                            Button(
                                onClick = {
                                    if (newCategoryName.isNotBlank()) {
                                        viewModel.addCustomCategory(newCategoryName)
                                        newCategoryName = ""
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = YasinNavy),
                                enabled = newCategoryName.isNotBlank()
                            ) {
                                Text("যোগ")
                            }
                        }

                        if (customCategories.isNotEmpty()) {
                            Text(text = "আপনার কাস্টম ক্যাটাগরিসমূহ:", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            customCategories.forEach { cat ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = "• ${cat.name}", fontWeight = FontWeight.Medium)
                                    IconButton(onClick = { viewModel.deleteCustomCategory(cat.id) }, modifier = Modifier.size(28.dp)) {
                                        Icon(Icons.Default.Close, contentDescription = null, tint = StatusRed)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // JSON Backup & Restore
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(text = "JSON ব্যাকআপ ও রিস্টোর", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = YasinNavy)
                        Text(
                            text = "আপনার ফোনের বাইরে ডাটা নিরাপদ রাখতে JSON ফরম্যাটে ব্যাকআপ নিয়ে রাখুন অথবা পুরানো ব্যাকআপ রিস্টোর করুন।",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    viewModel.exportBackup { json ->
                                        backupJsonStr = json
                                        showBackupModal = true
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = YasinNavy),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("ব্যাকআপ নিন")
                            }

                            OutlinedButton(
                                onClick = { showRestoreModal = true },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("রিস্টোর করুন")
                            }
                        }
                    }
                }
            }
        }
    }

    if (showBackupModal) {
        Dialog(onDismissRequest = { showBackupModal = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(text = "ব্যাকআপ JSON কোড", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = YasinNavy)
                    Text(text = "এই কোডটি কপি করে নিরাপদ স্থানে সংরক্ষণ করুন:", style = MaterialTheme.typography.bodySmall, color = TextSecondary)

                    OutlinedTextField(
                        value = backupJsonStr,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth().heightIn(max = 240.dp)
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showBackupModal = false }) { Text("বন্ধ করুন") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(backupJsonStr))
                                showBackupModal = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = YasinGreen)
                        ) {
                            Text("ক্লিপবোর্ডে কপি করুন")
                        }
                    }
                }
            }
        }
    }

    if (showRestoreModal) {
        Dialog(onDismissRequest = { showRestoreModal = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(text = "ব্যাকআপ রিস্টোর করুন", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = YasinNavy)
                    Text(text = "আগে কপি করে রাখা JSON কোডটি নিচে পেস্ট করুন:", style = MaterialTheme.typography.bodySmall, color = TextSecondary)

                    OutlinedTextField(
                        value = restoreInputStr,
                        onValueChange = { restoreInputStr = it },
                        modifier = Modifier.fillMaxWidth().heightIn(max = 200.dp),
                        placeholder = { Text("{\"version\": 1, \"products\": [...]}") }
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showRestoreModal = false }) { Text("বাতিল") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                viewModel.restoreBackup(restoreInputStr) {
                                    showRestoreModal = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = YasinGreen),
                            enabled = restoreInputStr.isNotBlank()
                        ) {
                            Text("রিস্টোর সম্পন্ন করুন")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SubScreenTopBar(title: String, onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .statusBarsPadding()
            .padding(horizontal = 8.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "পিছনে যান", tint = YasinNavy)
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = title, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = YasinNavy)
    }
}
