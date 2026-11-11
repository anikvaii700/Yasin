package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.*
import com.example.ui.ShopViewModel
import com.example.ui.components.ConfirmDeleteDialog
import com.example.ui.components.SectionHeader
import com.example.ui.components.formatTaka
import com.example.ui.theme.*

enum class AdminSection(val title: String, val icon: ImageVector) {
    OVERVIEW("ওভারভিউ", Icons.Default.Dashboard),
    PRODUCTS("পণ্য ডিলিট", Icons.Default.Inventory2),
    SALES("বিক্রয় ডিলিট", Icons.Default.ShoppingCart),
    PURCHASES("ক্রয় ডিলিট", Icons.Default.ShoppingBag),
    CUSTOMERS("গ্রাহক ডিলিট", Icons.Default.AccountBalanceWallet),
    LOANS("ধার-দেনা ডিলিট", Icons.Default.People),
    EXPENSES("খরচ ডিলিট", Icons.Default.ReceiptLong),
    MEMOS("ক্যাশ মেমো ডিলিট", Icons.Default.Description),
    DEMO_DATA("ডেমো ডাটা নিয়ন্ত্রণ", Icons.Default.RestartAlt),
    SECURITY("পিন সিকিউরিটি", Icons.Default.Security)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    viewModel: ShopViewModel,
    onExitAdmin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isAdminAuth by viewModel.isAdminAuthenticated.collectAsStateWithLifecycle()

    var selectedSection by remember { mutableStateOf(AdminSection.OVERVIEW) }
    var pinInput by remember { mutableStateOf("") }
    var isAuthenticating by remember { mutableStateOf(false) }

    // Item to delete confirmation state
    var pendingDeleteAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    var pendingDeleteTitle by remember { mutableStateOf("") }
    var pendingDeleteMessage by remember { mutableStateOf("") }

    // Demo Data 3-step confirmation counter
    var resetStep by remember { mutableStateOf(0) } // 0: None, 1: First click, 2: Second click, 3: Third click

    // Security PIN change
    var currentPinInput by remember { mutableStateOf("") }
    var newPinInput by remember { mutableStateOf("") }
    var confirmPinInput by remember { mutableStateOf("") }

    val products by viewModel.products.collectAsStateWithLifecycle()
    val sales by viewModel.sales.collectAsStateWithLifecycle()
    val purchases by viewModel.purchases.collectAsStateWithLifecycle()
    val expenses by viewModel.expenses.collectAsStateWithLifecycle()
    val customers by viewModel.customers.collectAsStateWithLifecycle()
    val loans by viewModel.loans.collectAsStateWithLifecycle()
    val cashMemos by viewModel.cashMemos.collectAsStateWithLifecycle()

    val demoProducts = products.count { it.isDemo }
    val demoSales = sales.count { it.isDemo }
    val demoPurchases = purchases.count { it.isDemo }
    val demoCustomers = customers.count { it.isDemo }
    val totalDemoRecords = demoProducts + demoSales + demoPurchases + demoCustomers

    // Authentication Gate
    if (!isAdminAuth) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(YasinNavyDark)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 400.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(StatusRedBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = StatusRed, modifier = Modifier.size(36.dp))
                    }

                    Text(
                        text = "অ্যাডমিন প্যানেল প্রবেশ",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = YasinNavy
                    )

                    Text(
                        text = "সুরক্ষার জন্য অ্যাডমিন পাসওয়ার্ড / পিন প্রদান করুন। সাধারণ ব্যবহারকারীরা গুরুত্বপূর্ণ রেকর্ড মুছতে পারবেন না।",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )

                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = { pinInput = it },
                        label = { Text("অ্যাডমিন পিন (Default: 1234)") },
                        placeholder = { Text("৪-সংখ্যার পিন") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_pin_input"),
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = YasinNavy) }
                    )

                    Button(
                        onClick = {
                            if (pinInput.isBlank()) {
                                Toast.makeText(context, "পিন টাইপ করুন", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            isAuthenticating = true
                            viewModel.loginAdmin(pinInput) { success ->
                                isAuthenticating = false
                                if (!success) {
                                    pinInput = ""
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("admin_login_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = StatusRed)
                    ) {
                        Text("লগইন ও আনলক করুন", fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    TextButton(onClick = onExitAdmin) {
                        Text("ফিরে যান", color = TextSecondary)
                    }
                }
            }
        }
        return
    }

    // Authenticated Admin Dashboard
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
    ) {
        // Admin Top Bar
        Surface(
            color = YasinNavyDark,
            modifier = Modifier.fillMaxWidth(),
            tonalElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(StatusRed),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("ADMIN PANEL", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = Color.White)
                        Text("সুরক্ষিত ডাটা ম্যানেজমেন্ট", fontSize = 10.sp, color = Color.White.copy(alpha = 0.7f))
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Button(
                        onClick = {
                            viewModel.logoutAdmin()
                            onExitAdmin()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("লক করুন", color = Color.White, fontSize = 12.sp)
                    }
                }
            }
        }

        // Section Tabs
        ScrollableTabRow(
            selectedTabIndex = AdminSection.values().indexOf(selectedSection),
            containerColor = Color.White,
            contentColor = StatusRed,
            edgePadding = 12.dp
        ) {
            AdminSection.values().forEach { sec ->
                Tab(
                    selected = selectedSection == sec,
                    onClick = { selectedSection = sec },
                    text = { Text(sec.title, fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                    icon = { Icon(sec.icon, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
            }
        }

        // Section Content
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (selectedSection) {
                AdminSection.OVERVIEW -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEE2E2)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Warning, contentDescription = null, tint = StatusRed, modifier = Modifier.size(32.dp))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text("অ্যাডমিন সুবিধা চালু রয়েছে", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = StatusRed)
                                        Text("এখানে যেকোনো আইটেম স্থায়ীভাবে মোছা যাবে। সাধারণ স্ক্রিনে ভুলবশত ডিলিট রোধ করতে সাধারণ ডিলিট বাটন লুকানো রয়েছে।", fontSize = 11.sp, color = TextPrimary)
                                    }
                                }
                            }
                        }

                        item {
                            SectionHeader(title = "ডাটা রেকর্ড সংক্ষিপ্ত বিবরণ")
                        }

                        item {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                AdminStatCard(title = "মোট পণ্য", count = products.size.toString(), modifier = Modifier.weight(1f)) {
                                    selectedSection = AdminSection.PRODUCTS
                                }
                                AdminStatCard(title = "মোট বিক্রয়", count = sales.size.toString(), modifier = Modifier.weight(1f)) {
                                    selectedSection = AdminSection.SALES
                                }
                            }
                        }

                        item {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                AdminStatCard(title = "মোট ক্রয়", count = purchases.size.toString(), modifier = Modifier.weight(1f)) {
                                    selectedSection = AdminSection.PURCHASES
                                }
                                AdminStatCard(title = "গ্রাহক হিসাব", count = customers.size.toString(), modifier = Modifier.weight(1f)) {
                                    selectedSection = AdminSection.CUSTOMERS
                                }
                            }
                        }

                        item {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                AdminStatCard(title = "ক্যাশ মেমো", count = cashMemos.size.toString(), modifier = Modifier.weight(1f)) {
                                    selectedSection = AdminSection.MEMOS
                                }
                                AdminStatCard(title = "ডেমো রেকর্ড", count = totalDemoRecords.toString(), modifier = Modifier.weight(1f)) {
                                    selectedSection = AdminSection.DEMO_DATA
                                }
                            }
                        }
                    }
                }

                AdminSection.PRODUCTS -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            Text("স্থায়ী পণ্য মুছে ফেলা (${products.size})", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = YasinNavy)
                        }
                        items(products) { p ->
                            AdminRecordRow(
                                title = p.name,
                                subtitle = "স্টক: ${p.currentStock} | ক্রয়: ৳${p.purchasePrice} | বিক্রয়: ৳${p.sellingPrice}",
                                badge = if (p.isDemo) "ডেমো" else null,
                                onDelete = {
                                    pendingDeleteTitle = "পণ্য ডিলিট"
                                    pendingDeleteMessage = "'${p.name}' পণ্যটি কি স্থায়ীভাবে মুছে ফেলতে চান?"
                                    pendingDeleteAction = { viewModel.deleteProduct(p.id) }
                                }
                            )
                        }
                    }
                }

                AdminSection.SALES -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            Text("স্থায়ী বিক্রয় রেকর্ড মুছে ফেলা (${sales.size})", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = YasinNavy)
                        }
                        items(sales) { s ->
                            AdminRecordRow(
                                title = "ইনভয়েস: ${s.invoiceNumber} (${s.customerName})",
                                subtitle = "তারিখ: ${s.saleDate} | মোট: ৳${s.totalAmount} | লাভ: ৳${s.grossProfit}",
                                badge = if (s.isDemo) "ডেমো" else null,
                                onDelete = {
                                    pendingDeleteTitle = "বিক্রয় রেকর্ড ডিলিট"
                                    pendingDeleteMessage = "ইনভয়েস #${s.invoiceNumber} এর বিক্রয় রেকর্ড স্থায়ীভাবে মুছে ফেলতে চান?"
                                    pendingDeleteAction = { viewModel.deleteSale(s.id) }
                                }
                            )
                        }
                    }
                }

                AdminSection.PURCHASES -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            Text("স্থায়ী ক্রয় রেকর্ড মুছে ফেলা (${purchases.size})", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = YasinNavy)
                        }
                        items(purchases) { pu ->
                            AdminRecordRow(
                                title = "${pu.productName} (পরিমাণ: ${pu.quantity})",
                                subtitle = "তারিখ: ${pu.purchaseDate} | সরবরাহকারী: ${pu.supplierName} | মোট: ৳${pu.totalAmount}",
                                badge = if (pu.isDemo) "ডেমো" else null,
                                onDelete = {
                                    pendingDeleteTitle = "ক্রয় রেকর্ড ডিলিট"
                                    pendingDeleteMessage = "'${pu.productName}' এর ক্রয় রেকর্ড কি স্থায়ীভাবে মুছে ফেলতে চান?"
                                    pendingDeleteAction = { viewModel.deletePurchase(pu.id) }
                                }
                            )
                        }
                    }
                }

                AdminSection.CUSTOMERS -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            Text("স্থায়ী গ্রাহক প্রোফাইল মুছে ফেলা (${customers.size})", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = YasinNavy)
                        }
                        items(customers) { c ->
                            AdminRecordRow(
                                title = c.name,
                                subtitle = "মোবাইল: ${c.mobile} | বাকি: ৳${c.remainingDue} | পরিশোধ: ৳${c.totalPaid}",
                                badge = if (c.isDemo) "ডেমো" else null,
                                onDelete = {
                                    pendingDeleteTitle = "গ্রাহক ডিলিট"
                                    pendingDeleteMessage = "গ্রাহক '${c.name}' ও তার সমস্ত লেনদেন কি স্থায়ীভাবে মুছে ফেলতে চান?"
                                    pendingDeleteAction = { viewModel.deleteCustomer(c.id) }
                                }
                            )
                        }
                    }
                }

                AdminSection.LOANS -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            Text("স্থায়ী ব্যক্তিগত ধার-দেনা মুছে ফেলা (${loans.size})", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = YasinNavy)
                        }
                        items(loans) { l ->
                            AdminRecordRow(
                                title = "${l.personName} (${if (l.loanType == "LENT") "প্রদত্ত ধার" else "গৃহীত ঋণ"})",
                                subtitle = "পরিমাণ: ৳${l.amount} | অবশিষ্ট: ৳${l.remainingAmount}",
                                badge = if (l.isDemo) "ডেমো" else null,
                                onDelete = {
                                    pendingDeleteTitle = "ধার-দেনা ডিলিট"
                                    pendingDeleteMessage = "'${l.personName}' এর রেকর্ড কি স্থায়ীভাবে মুছে ফেলতে চান?"
                                    pendingDeleteAction = { viewModel.deleteLoan(l.id) }
                                }
                            )
                        }
                    }
                }

                AdminSection.EXPENSES -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            Text("স্থায়ী খরচ মুছে ফেলা (${expenses.size})", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = YasinNavy)
                        }
                        items(expenses) { e ->
                            AdminRecordRow(
                                title = e.title,
                                subtitle = "পরিমাণ: ৳${e.amount} | ক্যাটাগরি: ${e.category} | তারিখ: ${e.expenseDate}",
                                badge = if (e.isDemo) "ডেমো" else null,
                                onDelete = {
                                    pendingDeleteTitle = "খরচ ডিলিট"
                                    pendingDeleteMessage = "'${e.title}' খরচের রেকর্ডটি কি স্থায়ীভাবে মুছে ফেলতে চান?"
                                    pendingDeleteAction = { viewModel.deleteExpense(e.id) }
                                }
                            )
                        }
                    }
                }

                AdminSection.MEMOS -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            Text("স্থায়ী ক্যাশ মেমো মুছে ফেলা (${cashMemos.size})", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = YasinNavy)
                        }
                        items(cashMemos) { cm ->
                            AdminRecordRow(
                                title = "মেমো নং: ${cm.memoNumber} (${cm.customerName})",
                                subtitle = "তারিখ: ${cm.date} | মোট: ৳${cm.grandTotal} | পরিশোধ: ৳${cm.paidAmount}",
                                badge = if (cm.isDemo) "ডেমো" else null,
                                onDelete = {
                                    pendingDeleteTitle = "ক্যাশ মেমো ডিলিট"
                                    pendingDeleteMessage = "ক্যাশ মেমো #${cm.memoNumber} কি স্থায়ীভাবে মুছে ফেলতে চান?"
                                    pendingDeleteAction = { viewModel.deleteCashMemo(cm.id) }
                                }
                            )
                        }
                    }
                }

                AdminSection.DEMO_DATA -> {
                    // Reset Demo Data (Strict 3-step confirmation, removes DEMO ONLY)
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(CircleShape)
                                                .background(StatusYellowBg),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.RestartAlt, contentDescription = null, tint = StatusYellow, modifier = Modifier.size(24.dp))
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text("ডেমো ডাটা রিসেট (Reset Demo Data)", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = YasinNavy)
                                            Text("শুধুমাত্র টেস্ট / নমুনা রেকর্ডগুলো মুছে যাবে", fontSize = 11.sp, color = TextSecondary)
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFFF8FAFC),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text("নিরাপত্তা সুরক্ষা নীতি:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = YasinNavy)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text("• আপনার দোকানের আসল পণ্য, হিসাব ও রেকর্ড কখনই ডিলিট হবে না।", fontSize = 11.sp, color = TextPrimary)
                                            Text("• শুধুমাত্র অ্যাপ ইন্সটলের সময় দেওয়া নমুনা (Demo) রেকর্ডগুলো বাছাই করে মুছে দেওয়া হবে।", fontSize = 11.sp, color = TextPrimary)
                                            Text("• নিশ্চিতকরণের জন্য আপনাকে পরপর ৩টি কনফার্মেশন বাটনে চাপ দিতে হবে।", fontSize = 11.sp, color = StatusRed, fontWeight = FontWeight.SemiBold)
                                        }
                                    }

                                    Text(
                                        text = "বর্তমান ডেমো রেকর্ড সংখ্যা: $totalDemoRecords টি",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = YasinNavy
                                    )

                                    // 3-step button logic
                                    when (resetStep) {
                                        0 -> {
                                            Button(
                                                onClick = { resetStep = 1 },
                                                colors = ButtonDefaults.buttonColors(containerColor = StatusYellow),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(48.dp)
                                                    .testTag("btn_reset_demo_step1")
                                            ) {
                                                Text("ধাপ ১: ডেমো ডাটা রিসেট শুরু করুন", fontWeight = FontWeight.Bold)
                                            }
                                        }
                                        1 -> {
                                            Button(
                                                onClick = { resetStep = 2 },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEA580C)),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(48.dp)
                                                    .testTag("btn_reset_demo_step2")
                                            ) {
                                                Text("ধাপ ২: আমি নিশ্চিত, নমুনা তথ্য মুছুন", fontWeight = FontWeight.Bold)
                                            }
                                        }
                                        2 -> {
                                            Button(
                                                onClick = {
                                                    viewModel.resetDemoData {
                                                        resetStep = 0
                                                    }
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = StatusRed),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(52.dp)
                                                    .testTag("btn_reset_demo_step3")
                                            ) {
                                                Text("ধাপ ৩: চূড়ান্ত নিশ্চিতকরণ (ডেমো রেকর্ড মুছুন)", fontWeight = FontWeight.ExtraBold)
                                            }
                                        }
                                    }

                                    if (resetStep > 0) {
                                        OutlinedButton(
                                            onClick = { resetStep = 0 },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("বাতিল করুন")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                AdminSection.SECURITY -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text("অ্যাডমিন পিন পরিবর্তন করুন", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = YasinNavy)
                                    Text("দোকানের রেকর্ড সংরক্ষণে পিন গোপন রাখুন। ডিফল্ট পিন ছিল 1234।", fontSize = 12.sp, color = TextSecondary)

                                    OutlinedTextField(
                                        value = newPinInput,
                                        onValueChange = { newPinInput = it },
                                        label = { Text("নতুন ৪-সংখ্যার পিন") },
                                        visualTransformation = PasswordVisualTransformation(),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    OutlinedTextField(
                                        value = confirmPinInput,
                                        onValueChange = { confirmPinInput = it },
                                        label = { Text("নতুন পিন পুনরায় লিখুন") },
                                        visualTransformation = PasswordVisualTransformation(),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Button(
                                        onClick = {
                                            if (newPinInput.length < 4) {
                                                Toast.makeText(context, "পিন অন্তত ৪ সংখ্যার হতে হবে", Toast.LENGTH_SHORT).show()
                                                return@Button
                                            }
                                            if (newPinInput != confirmPinInput) {
                                                Toast.makeText(context, "উভয় পিন মিলছে না!", Toast.LENGTH_SHORT).show()
                                                return@Button
                                            }
                                            viewModel.changeAdminPin(newPinInput)
                                            newPinInput = ""
                                            confirmPinInput = ""
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = YasinGreen)
                                    ) {
                                        Text("পিন পরিবর্তন সংরক্ষণ করুন", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Confirmation dialog for permanent delete
    pendingDeleteAction?.let { action ->
        ConfirmDeleteDialog(
            title = pendingDeleteTitle,
            message = pendingDeleteMessage,
            onConfirm = {
                action()
                pendingDeleteAction = null
            },
            onDismiss = { pendingDeleteAction = null }
        )
    }
}

@Composable
fun AdminStatCard(
    title: String,
    count: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(title, color = TextSecondary, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(count, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = YasinNavy)
            Spacer(modifier = Modifier.height(4.dp))
            Text("রেকর্ড পরিচালনা →", color = StatusRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun AdminRecordRow(
    title: String,
    subtitle: String,
    badge: String? = null,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                    if (badge != null) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(shape = RoundedCornerShape(4.dp), color = StatusYellowBg) {
                            Text(badge, color = StatusYellow, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(subtitle, color = TextSecondary, fontSize = 11.sp)
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = StatusRed)
            }
        }
    }
}
