package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Product
import com.example.data.model.StockAddition
import com.example.ui.ShopViewModel
import com.example.ui.components.StockBadge
import com.example.ui.components.formatTaka
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsScreen(
    viewModel: ShopViewModel,
    openAddProductTrigger: Boolean = false,
    onResetAddProductTrigger: () -> Unit = {}
) {
    val context = LocalContext.current
    val products by viewModel.products.collectAsStateWithLifecycle()
    val stockAdditions by viewModel.stockAdditions.collectAsStateWithLifecycle()
    val allCategories = viewModel.getAllCategories()
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val defaultAlert = profile?.lowStockAlertThreshold ?: 5L

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<String?>("সব (All)") }
    var showAddEditDialog by remember { mutableStateOf(false) }
    var productToEdit by remember { mutableStateOf<Product?>(null) }
    var productForStockAddition by remember { mutableStateOf<Product?>(null) }
    var showStockHistoryDialog by remember { mutableStateOf(false) }

    // Triggered externally from quick actions
    LaunchedEffect(openAddProductTrigger) {
        if (openAddProductTrigger) {
            productToEdit = null
            showAddEditDialog = true
            onResetAddProductTrigger()
        }
    }

    val filteredProducts = remember(products, searchQuery, selectedCategory) {
        products.filter { p ->
            val matchesQuery = searchQuery.isBlank() ||
                    p.name.contains(searchQuery, ignoreCase = true) ||
                    p.brand.contains(searchQuery, ignoreCase = true) ||
                    p.model.contains(searchQuery, ignoreCase = true) ||
                    p.productCode.contains(searchQuery, ignoreCase = true)
            val matchesCategory = selectedCategory == null ||
                    selectedCategory == "সব (All)" ||
                    p.category == selectedCategory
            matchesQuery && matchesCategory
        }
    }

    val totalStockVal = filteredProducts.sumOf { it.currentStock * it.purchasePrice }
    val totalSellingVal = filteredProducts.sumOf { it.currentStock * it.sellingPrice }
    val totalProfit = totalSellingVal - totalStockVal

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    productToEdit = null
                    showAddEditDialog = true
                },
                containerColor = YasinGreen,
                contentColor = Color.White,
                modifier = Modifier
                    .padding(bottom = 64.dp)
                    .testTag("fab_add_product")
            ) {
                Icon(Icons.Default.Add, contentDescription = "নতুন পণ্য যোগ")
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

            // Search Bar & History Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("পণ্য, ব্র্যান্ড বা মডেল খুঁজুন...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = TextSecondary)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = TextSecondary)
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("search_products_input")
                )

                FilledTonalIconButton(
                    onClick = { showStockHistoryDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = Color.White,
                        contentColor = YasinNavy
                    ),
                    modifier = Modifier.size(52.dp)
                ) {
                    Icon(Icons.Default.History, contentDescription = "স্টক ইতিহাস")
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Categories horizontal chip list
            val categoryList = remember(allCategories) {
                listOf("সব (All)") + allCategories
            }

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categoryList) { category ->
                    val isSelected = (category == selectedCategory) || (category == "সব (All)" && selectedCategory == null)
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            selectedCategory = if (category == "সব (All)") null else category
                        },
                        label = { Text(text = category, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = YasinGreen,
                            selectedLabelColor = Color.White,
                            containerColor = Color.White,
                            labelColor = TextPrimary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) YasinGreen else BorderLight
                        ),
                        modifier = Modifier.testTag("chip_${category.replace(" ", "_")}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Stock summary banner
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "মোট পণ্য: ${filteredProducts.size} টি", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        Text(
                            text = "মোট স্টক: ${filteredProducts.sumOf { it.currentStock }} টি",
                            fontWeight = FontWeight.Bold,
                            color = YasinNavy,
                            fontSize = 13.sp
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "মোট স্টক মূল্য", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        Text(
                            text = formatTaka(totalStockVal),
                            fontWeight = FontWeight.Bold,
                            color = YasinGreen,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Products list
            if (filteredProducts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Inventory,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (searchQuery.isNotBlank()) "কোনো পণ্য পাওয়া যায়নি" else "এখনও কোনো পণ্য যুক্ত করা হয়নি",
                            style = MaterialTheme.typography.bodyLarge,
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
                    items(filteredProducts, key = { it.id }) { product ->
                        ProductItemCard(
                            product = product,
                            defaultAlert = defaultAlert,
                            onEdit = {
                                productToEdit = product
                                showAddEditDialog = true
                            },
                            onAddStock = {
                                productForStockAddition = product
                            }
                        )
                    }
                }
            }
        }
    }

    // Add / Edit Product Dialog (Smart Add Product)
    if (showAddEditDialog) {
        AddEditProductDialog(
            initialProduct = productToEdit,
            categories = allCategories,
            onDismiss = { showAddEditDialog = false },
            onSave = { product ->
                if (product.id.isBlank()) {
                    viewModel.addProduct(product) { showAddEditDialog = false }
                } else {
                    viewModel.updateProduct(product) { showAddEditDialog = false }
                }
            }
        )
    }

    // Smart Stock Addition Dialog for Existing Products
    productForStockAddition?.let { product ->
        AddStockDialog(
            product = product,
            onDismiss = { productForStockAddition = null },
            onAddStock = { addedQty, purchasePrice, supplier, notes ->
                viewModel.addStock(
                    productId = product.id,
                    addedQty = addedQty,
                    purchasePrice = purchasePrice,
                    supplierName = supplier,
                    notes = notes
                ) {
                    productForStockAddition = null
                }
            }
        )
    }

    // Stock Addition History Dialog
    if (showStockHistoryDialog) {
        Dialog(onDismissRequest = { showStockHistoryDialog = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.85f),
                shape = RoundedCornerShape(20.dp),
                color = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "স্টক বৃদ্ধির ইতিহাস (${stockAdditions.size})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = YasinNavy
                        )
                        IconButton(onClick = { showStockHistoryDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                        }
                    }

                    Divider(modifier = Modifier.padding(vertical = 8.dp), color = BorderLight)

                    if (stockAdditions.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("এখনও কোনো স্টক বৃদ্ধির ইতিহাস নেই", color = TextSecondary)
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(stockAdditions.sortedByDescending { it.dateAdded }) { item ->
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFFF8FAFC),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(item.productName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                                            Text(item.dateAdded, fontSize = 11.sp, color = TextSecondary)
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "পূর্ববর্তী স্টক: ${item.previousQuantity}  ➔  নতুন যোগ: +${item.addedQuantity}  ➔  বর্তমান স্টক: ${item.totalQuantity}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = YasinGreen
                                        )
                                        if (item.purchasePrice > 0) {
                                            Text(text = "ক্রয় দর: ৳${item.purchasePrice} | সরবরাহকারী: ${item.supplierName.ifBlank { "সাধারণ" }}", fontSize = 11.sp, color = TextSecondary)
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
}

@Composable
fun ProductItemCard(
    product: Product,
    defaultAlert: Long,
    onEdit: () -> Unit,
    onAddStock: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("product_card_${product.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "${product.category} • ${product.brand} ${product.model}".trim().removePrefix("• "),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                StockBadge(
                    currentStock = product.currentStock,
                    minAlert = if (product.minStockAlert > 0) product.minStockAlert else defaultAlert
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            Divider(color = BorderLight)
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "বিক্রয় মূল্য", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    Text(
                        text = formatTaka(product.sellingPrice),
                        fontWeight = FontWeight.Bold,
                        color = YasinGreen,
                        fontSize = 15.sp
                    )
                }

                Column {
                    Text(text = "ক্রয় মূল্য", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    Text(
                        text = formatTaka(product.purchasePrice),
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary,
                        fontSize = 14.sp
                    )
                }

                Column {
                    Text(text = "একক লাভ", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    val margin = product.sellingPrice - product.purchasePrice
                    Text(
                        text = formatTaka(margin),
                        fontWeight = FontWeight.SemiBold,
                        color = if (margin >= 0) StatusGreen else StatusRed,
                        fontSize = 14.sp
                    )
                }

                // Add Stock & Edit Actions (Safe: Permanent Deletion is protected in Admin Panel)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FilledTonalButton(
                        onClick = onAddStock,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = YasinGreen.copy(alpha = 0.12f),
                            contentColor = YasinGreen
                        ),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("btn_add_stock_${product.id}")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("স্টক যোগ", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "এডিট", tint = YasinNavy, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

/**
 * Smart Stock Addition Dialog for existing products.
 * Never overwrites previous quantity; automatically calculates:
 * Previous Stock + Newly Added = Current Total Stock!
 */
@Composable
fun AddStockDialog(
    product: Product,
    onDismiss: () -> Unit,
    onAddStock: (addedQty: Long, purchasePrice: Double, supplier: String, notes: String) -> Unit
) {
    val context = LocalContext.current
    var addedQtyStr by remember { mutableStateOf("") }
    var purchasePriceStr by remember { mutableStateOf(product.purchasePrice.toString()) }
    var supplierName by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val addedQty = addedQtyStr.toLongOrNull() ?: 0L
    val previousStock = product.currentStock
    val calculatedTotalStock = previousStock + addedQty

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(YasinGreen.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.AddShoppingCart, contentDescription = null, tint = YasinGreen, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("নতুন স্টক যোগ করুন", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = YasinNavy)
                        Text(product.name, fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
                    }
                }

                // Calculation Preview Box
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF1F5F9),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("পূর্ববর্তী স্টক:", fontSize = 12.sp, color = TextSecondary)
                            Text("$previousStock টি", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 12.sp)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("নতুন যোগ হবে:", fontSize = 12.sp, color = TextSecondary)
                            Text("+$addedQty টি", fontWeight = FontWeight.Bold, color = YasinGreen, fontSize = 12.sp)
                        }
                        Divider(color = BorderLight, modifier = Modifier.padding(vertical = 2.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("বর্তমান মোট স্টক হবে:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = YasinNavy)
                            Text("$calculatedTotalStock টি", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = YasinNavy)
                        }
                    }
                }

                OutlinedTextField(
                    value = addedQtyStr,
                    onValueChange = { addedQtyStr = it },
                    label = { Text("নতুন যোগকৃত পরিমাণ (New Quantity Added) *") },
                    placeholder = { Text("যেমন: 20") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_new_stock_qty")
                )

                OutlinedTextField(
                    value = purchasePriceStr,
                    onValueChange = { purchasePriceStr = it },
                    label = { Text("নতুন ক্রয় মূল্য (দর ৳)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = supplierName,
                    onValueChange = { supplierName = it },
                    label = { Text("সরবরাহকারী / মহাজন (ঐচ্ছিক)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("বাতিল")
                    }

                    Button(
                        onClick = {
                            if (addedQty <= 0) {
                                Toast.makeText(context, "সঠিক যোগকৃত পরিমাণ দিন", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            val pPrice = purchasePriceStr.toDoubleOrNull() ?: product.purchasePrice
                            onAddStock(addedQty, pPrice, supplierName.trim(), notes.trim())
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_confirm_add_stock"),
                        colors = ButtonDefaults.buttonColors(containerColor = YasinGreen)
                    ) {
                        Text("স্টক যোগ করুন", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditProductDialog(
    initialProduct: Product?,
    categories: List<String>,
    onDismiss: () -> Unit,
    onSave: (Product) -> Unit
) {
    var name by remember { mutableStateOf(initialProduct?.name ?: "") }
    var selectedCategory by remember { mutableStateOf(initialProduct?.category ?: categories.firstOrNull() ?: "Other Accessories") }
    var brand by remember { mutableStateOf(initialProduct?.brand ?: "") }
    var model by remember { mutableStateOf(initialProduct?.model ?: "") }
    var purchasePriceStr by remember { mutableStateOf(if (initialProduct != null) initialProduct.purchasePrice.toString() else "") }
    var sellingPriceStr by remember { mutableStateOf(if (initialProduct != null) initialProduct.sellingPrice.toString() else "") }
    // Initial added quantity for brand new products
    var initialAddedQtyStr by remember { mutableStateOf(if (initialProduct != null) initialProduct.currentStock.toString() else "10") }
    var minStockAlertStr by remember { mutableStateOf(if (initialProduct != null) initialProduct.minStockAlert.toString() else "5") }
    var productCode by remember { mutableStateOf(initialProduct?.productCode ?: "") }
    var notes by remember { mutableStateOf(initialProduct?.notes ?: "") }
    var categoryExpanded by remember { mutableStateOf(false) }

    val isNewProduct = initialProduct == null

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = if (isNewProduct) "নতুন পণ্য যোগ করুন" else "পণ্য এডিট করুন",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = YasinNavy
                    )
                }

                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("পণ্যের নাম (Product Name) *") },
                        placeholder = { Text("যেমন: Samsung 25W Charger") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("product_name_input"),
                        singleLine = true
                    )
                }

                item {
                    ExposedDropdownMenuBox(
                        expanded = categoryExpanded,
                        onExpandedChange = { categoryExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedCategory,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("ক্যাটাগরি (Category) *") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = categoryExpanded,
                            onDismissRequest = { categoryExpanded = false }
                        ) {
                            categories.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat) },
                                    onClick = {
                                        selectedCategory = cat
                                        categoryExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = brand,
                            onValueChange = { brand = it },
                            label = { Text("ব্র্যান্ড (Brand) *") },
                            placeholder = { Text("যেমন: Samsung") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = model,
                            onValueChange = { model = it },
                            label = { Text("মডেল (Model)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = purchasePriceStr,
                            onValueChange = { purchasePriceStr = it },
                            label = { Text("ক্রয় মূল্য (৳) *") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = sellingPriceStr,
                            onValueChange = { sellingPriceStr = it },
                            label = { Text("বিক্রয় মূল্য (৳) *") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = initialAddedQtyStr,
                            onValueChange = { initialAddedQtyStr = it },
                            label = { Text(if (isNewProduct) "প্রাথমিক যোগকৃত পরিমাণ *" else "বর্তমান স্টক") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            enabled = isNewProduct, // In edit mode, stock is altered via "Add Stock"
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = minStockAlertStr,
                            onValueChange = { minStockAlertStr = it },
                            label = { Text("কম স্টক সতর্কতা") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("বাতিল")
                        }

                        Button(
                            onClick = {
                                val purchaseP = purchasePriceStr.toDoubleOrNull() ?: 0.0
                                val sellingP = sellingPriceStr.toDoubleOrNull() ?: 0.0
                                val stock = initialAddedQtyStr.toLongOrNull() ?: 0L
                                val alert = minStockAlertStr.toLongOrNull() ?: 5L

                                if (name.isNotBlank()) {
                                    val product = (initialProduct ?: Product()).copy(
                                        name = name.trim(),
                                        category = selectedCategory,
                                        brand = brand.trim(),
                                        model = model.trim(),
                                        purchasePrice = purchaseP,
                                        sellingPrice = sellingP,
                                        currentStock = if (isNewProduct) stock else (initialProduct?.currentStock ?: stock),
                                        minStockAlert = alert,
                                        productCode = productCode.trim(),
                                        notes = notes.trim()
                                    )
                                    onSave(product)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = YasinGreen),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("save_product_button")
                        ) {
                            Text(if (isNewProduct) "পণ্য যোগ করুন" else "আপডেট করুন", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
