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
import com.example.data.model.Customer
import com.example.data.model.CustomerTransaction
import com.example.ui.ShopViewModel
import com.example.ui.components.ConfirmDeleteDialog
import com.example.ui.components.formatTaka
import com.example.ui.theme.*

@Composable
fun DueScreen(
    viewModel: ShopViewModel,
    openAddDueTrigger: Boolean = false,
    onResetAddDueTrigger: () -> Unit = {}
) {
    val customers by viewModel.customers.collectAsStateWithLifecycle()
    val today = viewModel.todayDateStr

    var searchQuery by remember { mutableStateOf("") }
    var showAddCustomerDialog by remember { mutableStateOf(false) }
    var selectedCustomerForTx by remember { mutableStateOf<Customer?>(null) }
    var txType by remember { mutableStateOf("PAYMENT") } // PAYMENT or DUE
    var selectedCustomerForHistory by remember { mutableStateOf<Customer?>(null) }
    var customerToDelete by remember { mutableStateOf<Customer?>(null) }

    LaunchedEffect(openAddDueTrigger) {
        if (openAddDueTrigger) {
            showAddCustomerDialog = true
            onResetAddDueTrigger()
        }
    }

    val filteredCustomers = remember(customers, searchQuery) {
        customers.filter { c ->
            searchQuery.isBlank() ||
                    c.name.contains(searchQuery, ignoreCase = true) ||
                    c.mobile.contains(searchQuery, ignoreCase = true) ||
                    c.address.contains(searchQuery, ignoreCase = true)
        }
    }

    val totalOutstandingDue = customers.sumOf { it.remainingDue }
    val totalPaidSum = customers.sumOf { it.totalPaid }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddCustomerDialog = true },
                containerColor = YasinGreen,
                contentColor = Color.White,
                modifier = Modifier
                    .padding(bottom = 64.dp)
                    .testTag("fab_add_customer")
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = "নতুন কাস্টমার")
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

            // Total Due Banner
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = StatusYellowBg),
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                            text = "কাস্টমারদের মোট বাকি (Total Due)",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = formatTaka(totalOutstandingDue),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = StatusYellow
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "মোট পরিশোধিত",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        Text(
                            text = formatTaka(totalPaidSum),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = YasinGreen
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_customer_input"),
                placeholder = { Text("কাস্টমারের নাম বা মোবাইল দিয়ে খুঁজুন...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Customers List
            if (filteredCustomers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.PeopleOutline,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "কোনো কাস্টমার পাওয়া যায়নি। নিচে + চেপে যুক্ত করুন।",
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
                    items(filteredCustomers, key = { it.id }) { customer ->
                        CustomerDueCard(
                            customer = customer,
                            onGiveDue = {
                                selectedCustomerForTx = customer
                                txType = "DUE"
                            },
                            onReceivePayment = {
                                selectedCustomerForTx = customer
                                txType = "PAYMENT"
                            },
                            onViewHistory = {
                                selectedCustomerForHistory = customer
                            },
                            onDelete = {
                                customerToDelete = customer
                            }
                        )
                    }
                }
            }
        }
    }

    // Add Customer Dialog
    if (showAddCustomerDialog) {
        AddCustomerDialog(
            onDismiss = { showAddCustomerDialog = false },
            onSave = { customer ->
                viewModel.addCustomer(customer) {
                    showAddCustomerDialog = false
                }
            }
        )
    }

    // Record Due / Payment Transaction Dialog
    if (selectedCustomerForTx != null) {
        RecordCustomerTxDialog(
            customer = selectedCustomerForTx!!,
            txType = txType,
            today = today,
            onDismiss = { selectedCustomerForTx = null },
            onSave = { tx ->
                selectedCustomerForTx?.id?.let { custId ->
                    viewModel.recordCustomerTx(custId, tx) {
                        selectedCustomerForTx = null
                    }
                }
            }
        )
    }

    // Customer History Dialog
    if (selectedCustomerForHistory != null) {
        CustomerHistoryDialog(
            viewModel = viewModel,
            customer = selectedCustomerForHistory!!,
            onDismiss = { selectedCustomerForHistory = null }
        )
    }

    // Confirm Delete Customer
    if (customerToDelete != null) {
        ConfirmDeleteDialog(
            title = "কাস্টমার প্রোফাইল মুছুন",
            message = "আপনি কি নিশ্চিতভাবে '${customerToDelete?.name}' এর বাকি হিসাব মুছে ফেলতে চান?",
            onConfirm = {
                customerToDelete?.id?.let { viewModel.deleteCustomer(it) }
                customerToDelete = null
            },
            onDismiss = { customerToDelete = null }
        )
    }
}

@Composable
fun CustomerDueCard(
    customer: Customer,
    onGiveDue: () -> Unit,
    onReceivePayment: () -> Unit,
    onViewHistory: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth().testTag("customer_card_${customer.id}")
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
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(YasinNavy.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = YasinNavy,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = customer.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = "${customer.mobile} ${if (customer.address.isNotBlank()) "• ${customer.address}" else ""}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (customer.remainingDue > 0) StatusYellowBg else StatusGreenBg
                ) {
                    Text(
                        text = if (customer.remainingDue > 0) "বাকি রয়েছে" else "পরিশোধিত",
                        color = if (customer.remainingDue > 0) StatusYellow else StatusGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
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
                    Text(text = "মোট বাকি", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    Text(text = formatTaka(customer.totalDue), fontWeight = FontWeight.Medium, color = TextPrimary, fontSize = 13.sp)
                }

                Column {
                    Text(text = "মোট জমা", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    Text(text = formatTaka(customer.totalPaid), fontWeight = FontWeight.Medium, color = YasinGreen, fontSize = 13.sp)
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "অবশিষ্ট বাকি", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    Text(
                        text = formatTaka(customer.remainingDue),
                        fontWeight = FontWeight.Bold,
                        color = if (customer.remainingDue > 0) StatusYellow else StatusGreen,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onGiveDue,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusYellow)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("+ নতুন বাকি", fontSize = 12.sp)
                }

                Button(
                    onClick = onReceivePayment,
                    modifier = Modifier.weight(1.2f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = YasinGreen)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("টাকা জমা", fontSize = 12.sp)
                }

                IconButton(
                    onClick = onViewHistory,
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(Icons.Default.History, contentDescription = "হিস্ট্রি", tint = YasinNavy)
                }
            }
        }
    }
}

@Composable
fun AddCustomerDialog(
    onDismiss: () -> Unit,
    onSave: (Customer) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var initialDueStr by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "নতুন কাস্টমার যোগ করুন",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = YasinNavy
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("কাস্টমারের নাম *") },
                    modifier = Modifier.fillMaxWidth().testTag("customer_name_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = mobile,
                    onValueChange = { mobile = it },
                    label = { Text("মোবাইল নাম্বার *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth().testTag("customer_mobile_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("ঠিকানা বা গ্রাম (যেমন: বুল্লা)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = initialDueStr,
                    onValueChange = { initialDueStr = it },
                    label = { Text("প্রারম্ভিক বাকি থাকলে (ঐচ্ছিক ৳)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

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
                            if (name.isNotBlank() && mobile.isNotBlank()) {
                                val initDue = initialDueStr.toDoubleOrNull() ?: 0.0
                                val customer = Customer(
                                    name = name.trim(),
                                    mobile = mobile.trim(),
                                    address = address.trim(),
                                    totalDue = initDue,
                                    totalPaid = 0.0,
                                    remainingDue = initDue
                                )
                                onSave(customer)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = YasinGreen),
                        enabled = name.isNotBlank() && mobile.isNotBlank(),
                        modifier = Modifier.testTag("save_customer_button")
                    ) {
                        Text("সংরক্ষণ করুন")
                    }
                }
            }
        }
    }
}

@Composable
fun RecordCustomerTxDialog(
    customer: Customer,
    txType: String,
    today: String,
    onDismiss: () -> Unit,
    onSave: (CustomerTransaction) -> Unit
) {
    var amountStr by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val isDue = txType == "DUE"
    val title = if (isDue) "${customer.name} - নতুন বাকি লিখুন" else "${customer.name} - টাকা জমা নিন"

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isDue) StatusYellow else YasinGreen
                )

                Text(
                    text = "বর্তমান অবশিষ্ট বাকি: ${formatTaka(customer.remainingDue)}",
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text(if (isDue) "বাকি টাকার পরিমাণ (৳) *" else "জমা টাকার পরিমাণ (৳) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth().testTag("cust_tx_amount_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("বিবরণ / নোট (ঐচ্ছিক)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(8.dp))

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
                            val amount = amountStr.toDoubleOrNull() ?: 0.0
                            if (amount > 0) {
                                val tx = CustomerTransaction(
                                    customerId = customer.id,
                                    type = txType,
                                    amount = amount,
                                    notes = notes.trim(),
                                    transactionDate = today
                                )
                                onSave(tx)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDue) StatusYellow else YasinGreen
                        ),
                        enabled = (amountStr.toDoubleOrNull() ?: 0.0) > 0,
                        modifier = Modifier.testTag("save_cust_tx_button")
                    ) {
                        Text(if (isDue) "বাকি যোগ করুন" else "টাকা জমা করুন")
                    }
                }
            }
        }
    }
}

@Composable
fun CustomerHistoryDialog(
    viewModel: ShopViewModel,
    customer: Customer,
    onDismiss: () -> Unit
) {
    val txFlow = remember(customer.id) {
        viewModel.repository.observeCustomerTransactions(viewModel.currentUserId, customer.id)
    }
    val transactions by txFlow.collectAsState(initial = emptyList())

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = customer.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = YasinNavy
                        )
                        Text(
                            text = "অবশিষ্ট বাকি: ${formatTaka(customer.remainingDue)}",
                            fontWeight = FontWeight.Bold,
                            color = StatusYellow
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "বন্ধ করুন")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = BorderLight)
                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "লেনদেন হিস্ট্রি (${transactions.size}টি):",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (transactions.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "কোনো লেনদেন হিস্ট্রি নেই", color = TextSecondary)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(transactions) { tx ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (tx.type == "DUE") StatusYellowBg else StatusGreenBg,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = if (tx.type == "DUE") "বাকি নিয়েছে" else "টাকা জমা দিয়েছে",
                                            fontWeight = FontWeight.Bold,
                                            color = if (tx.type == "DUE") StatusYellow else StatusGreen,
                                            fontSize = 13.sp
                                        )
                                        if (tx.notes.isNotBlank()) {
                                            Text(text = tx.notes, style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                                        }
                                        Text(text = tx.transactionDate, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                    }
                                    Text(
                                        text = formatTaka(tx.amount),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = if (tx.type == "DUE") StatusYellow else StatusGreen
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
