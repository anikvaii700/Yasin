package com.example.ui.screens

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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Product
import com.example.data.model.Sale
import com.example.data.model.SaleItem
import com.example.ui.ShopViewModel
import com.example.ui.components.ConfirmDeleteDialog
import com.example.ui.components.formatTaka
import com.example.ui.theme.*

@Composable
fun SalesScreen(
    viewModel: ShopViewModel,
    openRecordSaleTrigger: Boolean = false,
    onResetRecordSaleTrigger: () -> Unit = {}
) {
    val sales by viewModel.sales.collectAsStateWithLifecycle()
    val products by viewModel.products.collectAsStateWithLifecycle()
    val today = viewModel.todayDateStr

    var showNewSaleDialog by remember { mutableStateOf(false) }
    var saleToDelete by remember { mutableStateOf<Sale?>(null) }
    var selectedFilterDate by remember { mutableStateOf<String?>(null) } // null = All, or today

    LaunchedEffect(openRecordSaleTrigger) {
        if (openRecordSaleTrigger) {
            showNewSaleDialog = true
            onResetRecordSaleTrigger()
        }
    }

    val displayedSales = remember(sales, selectedFilterDate) {
        if (selectedFilterDate == null) sales else sales.filter { it.saleDate == selectedFilterDate }
    }

    val totalSalesSum = displayedSales.sumOf { it.totalAmount }
    val totalCostSum = displayedSales.sumOf { it.totalCost }
    val totalProfitSum = displayedSales.sumOf { it.grossProfit }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showNewSaleDialog = true },
                containerColor = YasinGreen,
                contentColor = Color.White,
                modifier = Modifier
                    .padding(bottom = 64.dp)
                    .testTag("fab_record_sale")
            ) {
                Icon(Icons.Default.AddShoppingCart, contentDescription = "বিক্রয় এন্ট্রি")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundLight)
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Date Filters
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedFilterDate == null,
                    onClick = { selectedFilterDate = null },
                    label = { Text("সব বিক্রয় (${sales.size})") }
                )
                FilterChip(
                    selected = selectedFilterDate == today,
                    onClick = { selectedFilterDate = today },
                    label = { Text("আজকের বিক্রি ($today)") }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Summary Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = YasinGreen),
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (selectedFilterDate == today) "আজকের মোট বিক্রি" else "মোট বিক্রয়",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                        Text(
                            text = formatTaka(totalSalesSum),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "মোট লাভ (Gross Profit)",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                        Text(
                            text = formatTaka(totalProfitSum),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = YasinGreenLight
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Invoices List
            if (displayedSales.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "কোনো বিক্রয় রসিদ পাওয়া যায়নি। নিচে + চাপুন।",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 120.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(displayedSales, key = { it.id }) { sale ->
                        SaleInvoiceCard(
                            sale = sale,
                            onDelete = { saleToDelete = sale }
                        )
                    }
                }
            }
        }
    }

    // New Sale Entry Dialog (Multi-item support + Auto stock deduction)
    if (showNewSaleDialog) {
        RecordSaleDialog(
            products = products,
            today = today,
            onDismiss = { showNewSaleDialog = false },
            onSave = { sale, items ->
                viewModel.recordSale(sale, items) {
                    showNewSaleDialog = false
                }
            }
        )
    }

    // Delete Sale Confirmation
    if (saleToDelete != null) {
        ConfirmDeleteDialog(
            title = "বিক্রয় রসিদ মুছুন",
            message = "আপনি কি নিশ্চিতভাবে এই রসিদটি মুছে ফেলতে চান? মনে রাখবেন, এটি মুছলে স্টক পুনঃগণনা করা হবে না।",
            onConfirm = {
                saleToDelete?.id?.let { viewModel.deleteSale(it) }
                saleToDelete = null
            },
            onDismiss = { saleToDelete = null }
        )
    }
}

@Composable
fun SaleInvoiceCard(
    sale: Sale,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth().testTag("sale_card_${sale.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(YasinGreen.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Receipt,
                            contentDescription = null,
                            tint = YasinGreen,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (sale.customerName.isNotBlank()) sale.customerName else "ক্যাশ কাস্টমার",
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "${sale.invoiceNumber} • ${sale.saleDate}",
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (sale.dueAmount > 0) StatusRedBg else StatusGreenBg
                ) {
                    Text(
                        text = if (sale.dueAmount > 0) "বাকি ৳${sale.dueAmount.toInt()}" else "পরিশোধিত",
                        color = if (sale.dueAmount > 0) StatusRed else StatusGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = BackgroundLight,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (sale.itemsSummary.isNotBlank()) sale.itemsSummary else "আইটেম বিবরণ",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextPrimary,
                    modifier = Modifier.padding(8.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "মোট বিক্রয়", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    Text(
                        text = formatTaka(sale.totalAmount),
                        fontWeight = FontWeight.Bold,
                        color = YasinGreen,
                        fontSize = 16.sp
                    )
                }

                Column {
                    Text(text = "ক্রয় খরচ", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    Text(
                        text = formatTaka(sale.totalCost),
                        fontWeight = FontWeight.Medium,
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "লাভ", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    Text(
                        text = formatTaka(sale.grossProfit),
                        fontWeight = FontWeight.Bold,
                        color = StatusGreen,
                        fontSize = 15.sp
                    )
                }
            }

            if (sale.dueAmount > 0) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "নগদ পরিশোধ: ${formatTaka(sale.paidAmount)}", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    Text(text = "বাকি: ${formatTaka(sale.dueAmount)}", style = MaterialTheme.typography.labelSmall, color = StatusYellow, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordSaleDialog(
    products: List<Product>,
    today: String,
    onDismiss: () -> Unit,
    onSave: (Sale, List<SaleItem>) -> Unit
) {
    var customerName by remember { mutableStateOf("") }
    var customerPhone by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    // Cart items
    val cartItems = remember { mutableStateListOf<SaleItem>() }

    // Picker state
    var selectedProduct by remember { mutableStateOf<Product?>(products.firstOrNull()) }
    var quantityStr by remember { mutableStateOf("1") }
    var priceStr by remember { mutableStateOf(selectedProduct?.sellingPrice?.toString() ?: "") }
    var productDropdownExpanded by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(selectedProduct) {
        if (selectedProduct != null) {
            priceStr = selectedProduct!!.sellingPrice.toString()
        }
    }

    val totalAmount = cartItems.sumOf { it.subtotal }
    val totalCost = cartItems.sumOf { it.quantity * it.purchaseCost }
    val totalProfit = totalAmount - totalCost

    var paidAmountStr by remember { mutableStateOf("") }
    val paidAmount = paidAmountStr.toDoubleOrNull() ?: totalAmount
    val dueAmount = (totalAmount - paidAmount).coerceAtLeast(0.0)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "নতুন বিক্রয় এন্ট্রি (Record Sale)",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = YasinNavy
                    )
                    Text(
                        text = "তারিখ: $today • অটোমেটিক ইনভেন্টরি স্টক কর্তন হবে",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                // Add item to cart section
                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = BackgroundLight),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(text = "পণ্য নির্বাচন করুন:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)

                            if (products.isEmpty()) {
                                Text(
                                    text = "ইনভেন্টরিতে কোনো পণ্য নেই। আগে পণ্য যোগ করুন।",
                                    color = StatusRed,
                                    fontSize = 12.sp
                                )
                            } else {
                                ExposedDropdownMenuBox(
                                    expanded = productDropdownExpanded,
                                    onExpandedChange = { productDropdownExpanded = it }
                                ) {
                                    OutlinedTextField(
                                        value = selectedProduct?.let { "${it.name} (স্টক: ${it.currentStock})" } ?: "পণ্য বেছে নিন",
                                        onValueChange = {},
                                        readOnly = true,
                                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = productDropdownExpanded) }
                                    )
                                    ExposedDropdownMenu(
                                        expanded = productDropdownExpanded,
                                        onDismissRequest = { productDropdownExpanded = false }
                                    ) {
                                        products.forEach { prod ->
                                            DropdownMenuItem(
                                                text = { Text("${prod.name} (মজুদ: ${prod.currentStock}) - ৳${prod.sellingPrice}") },
                                                onClick = {
                                                    selectedProduct = prod
                                                    productDropdownExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = quantityStr,
                                        onValueChange = { quantityStr = it },
                                        label = { Text("পরিমাণ") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.weight(1f).testTag("sale_item_quantity"),
                                        singleLine = true
                                    )
                                    OutlinedTextField(
                                        value = priceStr,
                                        onValueChange = { priceStr = it },
                                        label = { Text("প্রতি পিস বিক্রয় (৳)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        modifier = Modifier.weight(1.5f).testTag("sale_item_price"),
                                        singleLine = true
                                    )
                                }

                                Button(
                                    onClick = {
                                        errorMessage = null
                                        val p = selectedProduct
                                        val qty = quantityStr.toLongOrNull() ?: 1L
                                        val unitPrice = priceStr.toDoubleOrNull() ?: p?.sellingPrice ?: 0.0

                                        if (p != null && qty > 0) {
                                            if (p.currentStock < qty) {
                                                errorMessage = "অপর্যাপ্ত স্টক! '${p.name}' এর বর্তমান মজুদ মাত্র ${p.currentStock} পিস।"
                                                return@Button
                                            }
                                            val subtotal = qty * unitPrice
                                            val cost = qty * p.purchasePrice
                                            cartItems.add(
                                                SaleItem(
                                                    productId = p.id,
                                                    productName = p.name,
                                                    quantity = qty,
                                                    sellingPrice = unitPrice,
                                                    purchaseCost = p.purchasePrice,
                                                    subtotal = subtotal,
                                                    profit = subtotal - cost
                                                )
                                            )
                                            quantityStr = "1"
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = YasinNavy),
                                    modifier = Modifier.fillMaxWidth().testTag("add_to_cart_button"),
                                    enabled = selectedProduct != null
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("+ তালিকায় যোগ করুন (Add)")
                                }
                            }
                        }
                    }
                }

                if (errorMessage != null) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = StatusRedBg,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = errorMessage ?: "",
                                color = StatusRed,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }

                // Cart Items List
                if (cartItems.isNotEmpty()) {
                    item {
                        Text(
                            text = "বিক্রির পণ্য তালিকা (${cartItems.size}টি):",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    items(cartItems) { item ->
                        Card(
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = item.productName, fontWeight = FontWeight.SemiBold)
                                    Text(
                                        text = "${item.quantity} পিস × ${formatTaka(item.sellingPrice)} = ${formatTaka(item.subtotal)} (লাভ: ${formatTaka(item.profit)})",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextSecondary
                                    )
                                }
                                IconButton(
                                    onClick = { cartItems.remove(item) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "বাদ দিন", tint = StatusRed)
                                }
                            }
                        }
                    }
                }

                // Customer info (optional)
                item {
                    Divider(color = BorderLight)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "ক্রেতার তথ্য (ঐচ্ছিক):", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = customerName,
                            onValueChange = { customerName = it },
                            label = { Text("ক্রেতার নাম") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = customerPhone,
                            onValueChange = { customerPhone = it },
                            label = { Text("মোবাইল") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true
                        )
                    }
                }

                // Payment and totals
                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = YasinGreenLight),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "মোট বিক্রয়:", fontWeight = FontWeight.Bold)
                                Text(text = formatTaka(totalAmount), fontWeight = FontWeight.Bold, color = YasinGreen, fontSize = 16.sp)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "মোট ক্রয় খরচ:", color = TextSecondary, fontSize = 13.sp)
                                Text(text = formatTaka(totalCost), color = TextSecondary, fontSize = 13.sp)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "মোট লাভ (Profit):", fontWeight = FontWeight.Bold, color = StatusGreen)
                                Text(text = formatTaka(totalProfit), fontWeight = FontWeight.Bold, color = StatusGreen)
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = paidAmountStr,
                                onValueChange = { paidAmountStr = it },
                                label = { Text("পরিশোধিত টাকা (৳)") },
                                placeholder = { Text(totalAmount.toString()) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            if (dueAmount > 0) {
                                Text(
                                    text = "বাকি থাকবে: ${formatTaka(dueAmount)}",
                                    fontWeight = FontWeight.Bold,
                                    color = StatusYellow,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("নোট বা বিবরণ (ঐচ্ছিক)") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2
                    )
                }

                // Action buttons
                item {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("বাতিল")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (cartItems.isNotEmpty()) {
                                    val summary = cartItems.joinToString(", ") { "${it.productName} (${it.quantity})" }
                                    val inv = "INV-${System.currentTimeMillis().toString().takeLast(6)}"
                                    val sale = Sale(
                                        invoiceNumber = inv,
                                        customerName = customerName.trim(),
                                        customerPhone = customerPhone.trim(),
                                        itemsSummary = summary,
                                        totalAmount = totalAmount,
                                        totalCost = totalCost,
                                        grossProfit = totalProfit,
                                        paidAmount = paidAmount,
                                        dueAmount = dueAmount,
                                        saleDate = today,
                                        notes = notes.trim()
                                    )
                                    onSave(sale, cartItems.toList())
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = YasinGreen),
                            enabled = cartItems.isNotEmpty(),
                            modifier = Modifier.testTag("confirm_sale_button")
                        ) {
                            Text("বিক্রয় সম্পন্ন করুন (Save Sale)")
                        }
                    }
                }
            }
        }
    }
}
