package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.*
import com.example.ui.ShopViewModel
import com.example.ui.components.formatTaka
import com.example.ui.theme.*
import com.example.util.CashMemoPrintHelper
import com.example.util.CashMemoStyle
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CashMemoScreen(
    viewModel: ShopViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val cashMemos by viewModel.cashMemos.collectAsStateWithLifecycle()
    val today = viewModel.todayDateStr

    var selectedTab by remember { mutableStateOf(0) } // 0: মেমো তৈরি ও লাইভ প্রিভিউ, 1: সংরক্ষিত মেমো ইতিহাস
    var selectedStyle by remember { mutableStateOf(CashMemoStyle.ORANGE_AMBER) } // ডিফল্ট: ছবির মতো অরেঞ্জ-অ্যাম্বার

    // 1. কাস্টমারের তথ্য
    var customerName by remember { mutableStateOf("") }
    var customerMobile by remember { mutableStateOf("") }
    var customerAddress by remember { mutableStateOf("বুল্লা বাজার") }

    // 2. চলতি আইটেম ইনপুট
    var currentItemDesc by remember { mutableStateOf("") }
    var currentItemQtyStr by remember { mutableStateOf("1") }
    var currentItemRateStr by remember { mutableStateOf("") }

    // 3. মেমোর আইটেম তালিকা (মাল্টি-আইটেম সাপোর্ট)
    var memoItems by remember {
        mutableStateOf(
            listOf(
                CashMemoItem(description = "চার্জার", quantity = 1L, rate = 250.0, amount = 250.0),
                CashMemoItem(description = "গ্লাস প্রটেক্টর", quantity = 1L, rate = 150.0, amount = 150.0)
            )
        )
    }

    // 4. জমা টাকা (Paid Amount) [ঐচ্ছিক]
    var paidAmountStr by remember { mutableStateOf("") }

    // ক্যালকুলেশন
    val subtotal = memoItems.sumOf { it.amount }
    val grandTotal = subtotal
    val paidAmount = if (paidAmountStr.isBlank()) grandTotal else (paidAmountStr.toDoubleOrNull() ?: 0.0)
    val dueAmount = (grandTotal - paidAmount).coerceAtLeast(0.0)

    val currentMemoNumber = remember(cashMemos.size) {
        val seq = (cashMemos.size + 1).toString().padStart(3, '0')
        "YT-${SimpleDateFormat("yyyy", Locale.US).format(Date())}-$seq"
    }

    // মেমো অবজেক্ট
    val liveMemo = remember(
        customerName, customerMobile, customerAddress, memoItems,
        grandTotal, paidAmount, dueAmount, currentMemoNumber, today
    ) {
        val timeStr = SimpleDateFormat("hh:mm a", Locale.US).format(Date())
        CashMemo(
            memoNumber = currentMemoNumber,
            date = today,
            time = timeStr,
            customerName = customerName.ifBlank { "নগদ ক্রেতা" },
            customerMobile = customerMobile,
            customerAddress = customerAddress.ifBlank { "বুল্লা বাজার" },
            items = if (memoItems.isNotEmpty()) memoItems else listOf(CashMemoItem(description = "পণ্য বিবরণী", quantity = 1L, rate = 0.0, amount = 0.0)),
            subtotal = subtotal,
            discount = 0.0,
            grandTotal = grandTotal,
            paidAmount = paidAmount,
            dueAmount = dueAmount,
            paymentStatus = if (dueAmount <= 0) "PAID" else if (paidAmount > 0) "PARTIAL" else "DUE",
            notes = "বিক্রিত মাল ফেরত নেওয়া হয় না। ধন্যবাদ আবার আসবেন।",
            isDemo = false
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
    ) {
        // Tab Selector
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.White,
            contentColor = Color(0xFFEA580C)
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("ক্যাশ মেমো ও লাইভ প্রিভিউ", fontWeight = FontWeight.Bold, fontSize = 13.5.sp) },
                icon = { Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(20.dp)) },
                modifier = Modifier.testTag("tab_memo_generator")
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("সংরক্ষিত মেমোসমূহ (${cashMemos.size})", fontWeight = FontWeight.Bold, fontSize = 13.5.sp) },
                icon = { Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(20.dp)) },
                modifier = Modifier.testTag("tab_saved_memos")
            )
        }

        if (selectedTab == 0) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp),
                contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // ==========================================
                // ১. ইউজার ইনপুট প্যানেল
                // ==========================================
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.EditNote,
                                        contentDescription = null,
                                        tint = Color(0xFFEA580C),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "মেমোর তথ্য ইনপুট করুন",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = TextPrimary
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFFFEDD5)
                                ) {
                                    Text(
                                        text = currentMemoNumber,
                                        color = Color(0xFFC2410C),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.5.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Divider(color = Color(0xFFF1F5F9))

                            // ১. কাস্টমার নাম ও মোবাইল নম্বর
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedTextField(
                                    value = customerName,
                                    onValueChange = { customerName = it },
                                    label = { Text("১. কাস্টমারের নাম") },
                                    placeholder = { Text("যেমন: আব্দুল করিম") },
                                    singleLine = true,
                                    modifier = Modifier
                                        .weight(1.2f)
                                        .testTag("input_customer_name"),
                                    shape = RoundedCornerShape(10.dp)
                                )

                                OutlinedTextField(
                                    value = customerMobile,
                                    onValueChange = { customerMobile = it },
                                    label = { Text("২. মোবাইল নম্বর") },
                                    placeholder = { Text("017xxxxxxxx") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                    singleLine = true,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("input_customer_mobile"),
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }

                            OutlinedTextField(
                                value = customerAddress,
                                onValueChange = { customerAddress = it },
                                label = { Text("গ্রাহকের ঠিকানা") },
                                placeholder = { Text("বুল্লা বাজার / গ্রাম") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_customer_address"),
                                shape = RoundedCornerShape(10.dp)
                            )

                            // ২. পণ্যের বিবরণ ও শর্টকাট
                            Text(
                                text = "৩. পণ্যের বিবরণ ও দ্রুত শর্টকাট:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp,
                                color = TextPrimary
                            )

                            // রেডিমেড শর্টকাট চিপস
                            val commonItems = listOf("ডিসপ্লে", "ব্যাটারি", "চার্জার", "কাভার", "গ্লাস", "ডাটা ক্যাবল", "হেডফোন", "সার্ভিসিং")
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(commonItems) { itemTitle ->
                                    SuggestionChip(
                                        onClick = {
                                            currentItemDesc = itemTitle
                                        },
                                        label = { Text(itemTitle, fontSize = 11.5.sp) },
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                }
                            }

                            OutlinedTextField(
                                value = currentItemDesc,
                                onValueChange = { currentItemDesc = it },
                                label = { Text("পণ্যের নাম / বিবরণ") },
                                placeholder = { Text("যেমন: স্যামসাং 25W চার্জার") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_item_desc"),
                                shape = RoundedCornerShape(10.dp)
                            )

                            // ৪ ও ৫: পরিমাণ ও দর
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = currentItemQtyStr,
                                    onValueChange = { currentItemQtyStr = it },
                                    label = { Text("৪. পরিমাণ (পিস)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("input_item_qty"),
                                    shape = RoundedCornerShape(10.dp)
                                )

                                OutlinedTextField(
                                    value = currentItemRateStr,
                                    onValueChange = { currentItemRateStr = it },
                                    label = { Text("৫. দর (রেট ৳)") },
                                    placeholder = { Text("250") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("input_item_rate"),
                                    shape = RoundedCornerShape(10.dp)
                                )

                                Button(
                                    onClick = {
                                        val desc = currentItemDesc.trim()
                                        val qty = currentItemQtyStr.toLongOrNull() ?: 1L
                                        val rate = currentItemRateStr.toDoubleOrNull() ?: 0.0
                                        if (desc.isNotBlank() && rate > 0) {
                                            memoItems = memoItems + CashMemoItem(
                                                description = desc,
                                                quantity = qty,
                                                rate = rate,
                                                amount = qty * rate
                                            )
                                            currentItemDesc = ""
                                            currentItemRateStr = ""
                                            currentItemQtyStr = "1"
                                        } else {
                                            Toast.makeText(context, "পণ্যের বিবরণ ও সঠিক দর লিখুন", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEA580C)),
                                    modifier = Modifier
                                        .height(52.dp)
                                        .testTag("btn_add_item_to_memo")
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("যোগ", fontWeight = FontWeight.Bold)
                                }
                            }

                            // ৬. জমা টাকা (Paid Amount) [ঐচ্ছিক]
                            OutlinedTextField(
                                value = paidAmountStr,
                                onValueChange = { paidAmountStr = it },
                                label = { Text("৬. জমা টাকা (Paid Amount) [ঐচ্ছিক]") },
                                placeholder = { Text("মোট টাকা ৳${grandTotal.toInt()}") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_paid_amount"),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }
                }

                // ==========================================
                // ২. অ্যাকশন বাটনস (প্রিন্ট/PDF, হোয়াটসঅ্যাপ, সেভ)
                // ==========================================
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                CashMemoPrintHelper.printMemo(context, liveMemo, profile, selectedStyle)
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEA580C)),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("btn_print_pdf")
                        ) {
                            Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("🖨️ প্রিন্ট / PDF", fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                        }

                        Button(
                            onClick = {
                                CashMemoPrintHelper.shareToWhatsApp(context, liveMemo, profile)
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)), // WhatsApp Green
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("btn_whatsapp_share")
                        ) {
                            Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("📲 হোয়াটসঅ্যাপ", fontWeight = FontWeight.Bold, fontSize = 12.5.sp, color = Color.White)
                        }

                        Button(
                            onClick = {
                                viewModel.saveCashMemo(liveMemo, autoRecordSale = true) {
                                    Toast.makeText(context, "মেমো সেভ করা হয়েছে!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = YasinNavy),
                            modifier = Modifier
                                .weight(0.9f)
                                .height(48.dp)
                                .testTag("btn_save_memo")
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("সেভ", fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                        }
                    }
                }

                // ==========================================
                // ৩. স্টাইল সিলেকশন চিপস ও লাইভ প্রিভিউ টাইটেল
                // ==========================================
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "👁️ রিয়েল-টাইম লাইভ মেমো প্রিভিউ:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = YasinNavy
                            )

                            // PDF ডাউনলোড সরাসরি শেয়ার অপশন
                            TextButton(
                                onClick = {
                                    CashMemoPrintHelper.sharePdf(context, liveMemo, profile, selectedStyle)
                                },
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFFEA580C))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("PDF শেয়ার", fontSize = 11.5.sp, color = Color(0xFFEA580C), fontWeight = FontWeight.Bold)
                            }
                        }

                        // স্টাইল সিলেক্টর চিপস
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = selectedStyle == CashMemoStyle.ORANGE_AMBER,
                                onClick = { selectedStyle = CashMemoStyle.ORANGE_AMBER },
                                label = { Text("🌟 অরেঞ্জ-অ্যাম্বার (ছবির মতো)", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                                leadingIcon = {
                                    if (selectedStyle == CashMemoStyle.ORANGE_AMBER) {
                                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                    }
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFFFFEDD5),
                                    selectedLabelColor = Color(0xFFC2410C)
                                ),
                                modifier = Modifier.weight(1f)
                            )

                            FilterChip(
                                selected = selectedStyle == CashMemoStyle.CYAN_BLUE,
                                onClick = { selectedStyle = CashMemoStyle.CYAN_BLUE },
                                label = { Text("📱 সায়ান-ব্লু মোবাইল", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                                leadingIcon = {
                                    if (selectedStyle == CashMemoStyle.CYAN_BLUE) {
                                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                    }
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFFE0F2FE),
                                    selectedLabelColor = Color(0xFF0369A1)
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // ==========================================
                // ৪. লাইভ প্রিভিউ কার্ড (সিলেক্টেড স্টাইল অনুযায়ী)
                // ==========================================
                item {
                    if (selectedStyle == CashMemoStyle.ORANGE_AMBER) {
                        OrangeAmberMemoPreview(
                            liveMemo = liveMemo,
                            profile = profile,
                            memoItems = memoItems,
                            grandTotal = grandTotal,
                            paidAmount = paidAmount,
                            dueAmount = dueAmount,
                            onRemoveItem = { index ->
                                memoItems = memoItems.toMutableList().also { it.removeAt(index) }
                            }
                        )
                    } else {
                        CyanBlueMemoPreview(
                            liveMemo = liveMemo,
                            profile = profile,
                            memoItems = memoItems,
                            grandTotal = grandTotal,
                            paidAmount = paidAmount,
                            dueAmount = dueAmount,
                            onRemoveItem = { index ->
                                memoItems = memoItems.toMutableList().also { it.removeAt(index) }
                            }
                        )
                    }
                }
            }
        } else {
            // ==========================================
            // সংরক্ষিত মেমোর তালিকা (History)
            // ==========================================
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp),
                contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (cashMemos.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(40.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(48.dp))
                                Spacer(modifier = Modifier.height(10.dp))
                                Text("কোনো সংরক্ষিত ক্যাশ মেমো পাওয়া যায়নি", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                                Text("নতুন মেমো তৈরি করতে প্রথম ট্যাবে ক্লিক করুন", fontSize = 12.sp, color = TextSecondary)
                            }
                        }
                    }
                } else {
                    items(cashMemos.sortedByDescending { it.memoNumber }) { memo ->
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "মেমো নং: ${memo.memoNumber}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = Color(0xFFEA580C)
                                        )
                                        Text(
                                            text = "তারিখ: ${memo.date} (${memo.time})",
                                            fontSize = 11.sp,
                                            color = TextSecondary
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (memo.dueAmount > 0) Color(0xFFFEE2E2) else Color(0xFFDCFCE7)
                                    ) {
                                        Text(
                                            text = if (memo.dueAmount > 0) "বাকী ৳${memo.dueAmount.toInt()}" else "পরিশোধিত",
                                            color = if (memo.dueAmount > 0) Color(0xFFDC2626) else Color(0xFF16A34A),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = "গ্রাহক: ${memo.customerName} ${if (memo.customerMobile.isNotBlank()) "(${memo.customerMobile})" else ""}",
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 13.sp,
                                    color = TextPrimary
                                )

                                Text(
                                    text = "পণ্য: ${memo.items.joinToString(", ") { "${it.description} x${it.quantity}" }}",
                                    fontSize = 11.sp,
                                    color = TextSecondary,
                                    maxLines = 1
                                )

                                Divider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFFF1F5F9))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "মোট টাকা: ৳${memo.grandTotal.toInt()}",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 14.sp,
                                        color = YasinNavy
                                    )

                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Button(
                                            onClick = { CashMemoPrintHelper.shareToWhatsApp(context, memo, profile) },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                            modifier = Modifier.height(34.dp)
                                        ) {
                                            Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("হোয়াটসঅ্যাপ", fontSize = 11.5.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                        }

                                        Button(
                                            onClick = { CashMemoPrintHelper.printMemo(context, memo, profile, selectedStyle) },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEA580C)),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                            modifier = Modifier.height(34.dp)
                                        ) {
                                            Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("প্রিন্ট", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
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

/**
 * ১. হুবহু ছবির মতো: অরেঞ্জ-অ্যাম্বার এক্সিকিউটিভ লাইভ প্রিভিউ কম্পোনেন্ট
 */
@Composable
private fun OrangeAmberMemoPreview(
    liveMemo: CashMemo,
    profile: ShopProfile?,
    memoItems: List<CashMemoItem>,
    grandTotal: Double,
    paidAmount: Double,
    dueAmount: Double,
    onRemoveItem: (Int) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, Color(0xFFFB923C), RoundedCornerShape(16.dp))
            .testTag("live_cash_memo_preview_orange")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // ----------------------------------------------------
            // ১. টপ ব্যানার (Header) - ছবির মতো
            // ----------------------------------------------------
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    // বাম পাশ: Cash Memo ক্যাপসুল, COMPANY NAME, SLOGAN
                    Column(
                        modifier = Modifier
                            .weight(1.3f)
                            .padding(start = 14.dp, top = 12.dp, bottom = 8.dp, end = 6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFF0F172A)
                        ) {
                            Text(
                                text = "Cash Memo",
                                color = Color(0xFFF59E0B),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontStyle = FontStyle.Italic,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.5.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = profile?.shopName ?: "ইয়াছিন টেলিকম",
                            fontWeight = FontWeight.Black,
                            fontSize = 22.sp,
                            color = Color(0xFFDC2626), // Bold Red-Orange
                            letterSpacing = 0.5.sp
                        )

                        Text(
                            text = "এখানে যাবতীয় পাইকারী ও খুচরা মালামাল এবং ডিজিটাল সেবা সুলভ মূল্যে প্রদান করা হয়।",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFEA580C),
                            lineHeight = 12.sp
                        )
                    }

                    // ডান পাশ: ডায়নামিক অরেঞ্জ আর্চ ও ব্ল্যাক কর্নার প্যানেল
                    Surface(
                        shape = RoundedCornerShape(topEnd = 16.dp, bottomStart = 26.dp),
                        color = Color(0xFF0F172A),
                        border = BorderStroke(2.dp, Color(0xFFEA580C)),
                        modifier = Modifier.weight(1.1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // কন্টাক্ট আইটেমসমূহ
                            ContactRowOrange(icon = "📍", text = "বুল্লা বাজার, লাখাই, হবিগঞ্জ")
                            ContactRowOrange(icon = "👤", text = "প্রোঃ মোহাম্মদ ইয়াছিন")
                            ContactRowOrange(icon = "✉️", text = "yasin.telecom@gmail.com")
                            ContactRowOrange(icon = "📞", text = "01766906974", isHighlighted = true)
                        }
                    }
                }
            }

            // ----------------------------------------------------
            // ২. কাস্টমার ও ডেট ইনভয়েস বার (ছবির মতো)
            // ----------------------------------------------------
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // No: ও Date:
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "No: ${liveMemo.memoNumber}",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp,
                        color = Color(0xFF0F172A)
                    )

                    // Date: [DD] [MM] [YYYY]
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp),
                            color = Color(0xFF0F172A)
                        ) {
                            Text(
                                text = "Date:",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.5.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }

                        val dateParts = liveMemo.date.split("-", "/")
                        dateParts.forEach { part ->
                            Box(
                                modifier = Modifier
                                    .border(1.dp, Color(0xFF0F172A))
                                    .padding(horizontal = 5.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = part,
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF0F172A)
                                )
                            }
                        }
                    }
                }

                // Name:
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFF0F172A)
                    ) {
                        Text(
                            text = "Name:",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.5.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                        color = Color.White,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = liveMemo.customerName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = TextPrimary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                // Address:
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFF0F172A)
                    ) {
                        Text(
                            text = "Address:",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.5.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                        color = Color.White,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "${liveMemo.customerAddress}${if (liveMemo.customerMobile.isNotBlank()) " | 📞 ${liveMemo.customerMobile}" else ""}",
                            fontSize = 11.5.sp,
                            color = TextSecondary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            // ----------------------------------------------------
            // ৩. প্রোডাক্ট টেবিল (ভিব্রান্ট অরেঞ্জ-রেড গ্রেডিয়েন্ট হেডার)
            // ----------------------------------------------------
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp)
            ) {
                // গ্রেডিয়েন্ট হেডার বার
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DashboardGradients.memoOrangeTableHead)
                        .padding(horizontal = 6.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("No:", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.width(34.dp), textAlign = TextAlign.Center)
                        Text("Description (বিবরণ)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(1.8f))
                        Text("Quantity", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.width(52.dp), textAlign = TextAlign.Center)
                        Text("Rate", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.width(55.dp), textAlign = TextAlign.End)
                        Text("Amount", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.width(65.dp), textAlign = TextAlign.End)
                        Spacer(modifier = Modifier.width(28.dp))
                    }
                }

                // টেবিল রোসমূহ (পিচ টিন্ট কলাম সহ)
                memoItems.forEachIndexed { index, item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(BorderStroke(0.5.dp, Color(0xFFE2E8F0)))
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("${index + 1}", fontSize = 11.sp, color = TextSecondary, modifier = Modifier.width(34.dp), textAlign = TextAlign.Center)
                        Text(item.description, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = TextPrimary, modifier = Modifier.weight(1.8f).padding(horizontal = 4.dp))

                        // Quantity Column (পিচ টিন্ট)
                        Box(
                            modifier = Modifier
                                .width(52.dp)
                                .background(Color(0xFFFFF7ED))
                                .padding(vertical = 2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("${item.quantity}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }

                        Text("৳${item.rate.toInt()}", fontSize = 12.sp, color = TextPrimary, modifier = Modifier.width(55.dp), textAlign = TextAlign.End)

                        // Amount Column (পিচ টিন্ট)
                        Box(
                            modifier = Modifier
                                .width(65.dp)
                                .background(Color(0xFFFFF7ED))
                                .padding(vertical = 2.dp),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Text("৳${item.amount.toInt()}", fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, color = Color(0xFFEA580C))
                        }

                        IconButton(
                            onClick = { onRemoveItem(index) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Remove", tint = StatusRed, modifier = Modifier.size(14.dp))
                        }
                    }
                }

                // ৪. সামারি রোসমূহ (ডান কোনায় Total, Advance, Due)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.End
                ) {
                    SummaryRowOrange(label = "Total", value = "৳${grandTotal.toInt()}")
                    SummaryRowOrange(label = "Advance", value = "৳${paidAmount.toInt()}")
                    SummaryRowOrange(label = "Due", value = "৳${dueAmount.toInt()}", isDue = dueAmount > 0)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // ----------------------------------------------------
            // ৫. Taka (In word).....................................
            // ----------------------------------------------------
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp)
            ) {
                Text(
                    text = "Taka (In word): ${takaInBengaliWords(grandTotal)}................................................................",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF0F172A),
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ----------------------------------------------------
            // ৬. ফুটার সিগনেচার
            // ----------------------------------------------------
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Divider(modifier = Modifier.width(105.dp), color = Color(0xFF94A3B8))
                    Spacer(modifier = Modifier.height(3.dp))
                    Text("Customer Signature", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("(ক্রেতার স্বাক্ষর)", fontSize = 8.5.sp, color = TextSecondary)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Divider(modifier = Modifier.width(115.dp), color = Color(0xFF94A3B8))
                    Spacer(modifier = Modifier.height(3.dp))
                    Text("Authorized Signature", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEA580C))
                    Text("(বিক্রেতার স্বাক্ষর)", fontSize = 8.5.sp, color = TextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ----------------------------------------------------
            // ৭. বটম অরেঞ্জ কার্ভড অ্যাকসেন্ট স্ট্রাইপ
            // ----------------------------------------------------
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .background(DashboardGradients.memoOrangeWave)
            )
        }
    }
}

@Composable
private fun ContactRowOrange(icon: String, text: String, isHighlighted: Boolean = false) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = Color(0xFFEA580C),
            modifier = Modifier.size(14.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(icon, fontSize = 7.5.sp)
            }
        }
        Text(
            text = text,
            fontSize = 9.sp,
            fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Medium,
            color = if (isHighlighted) Color(0xFFFDE047) else Color(0xFFF1F5F9),
            maxLines = 1
        )
    }
}

@Composable
private fun SummaryRowOrange(label: String, value: String, isDue: Boolean = false) {
    Row(
        modifier = Modifier
            .width(170.dp)
            .border(BorderStroke(0.5.dp, Color(0xFFE2E8F0))),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .background(Color(0xFFFFF7ED))
                .padding(vertical = 4.dp, horizontal = 8.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            Text(label, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFFEA580C))
        }

        Box(
            modifier = Modifier
                .weight(1.1f)
                .background(Color.White)
                .padding(vertical = 4.dp, horizontal = 8.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            Text(
                text = value,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 11.5.sp,
                color = if (isDue) Color(0xFFDC2626) else Color(0xFF0F172A)
            )
        }
    }
}

/**
 * ২. সায়ান-ব্লু মোবাইল লাইভ প্রিভিউ কম্পোনেন্ট
 */
@Composable
private fun CyanBlueMemoPreview(
    liveMemo: CashMemo,
    profile: ShopProfile?,
    memoItems: List<CashMemoItem>,
    grandTotal: Double,
    paidAmount: Double,
    dueAmount: Double,
    onRemoveItem: (Int) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, Color(0xFF38BDF8), RoundedCornerShape(16.dp))
            .testTag("live_cash_memo_preview_cyan")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DashboardGradients.memoCyanHeader)
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            val brands = listOf("vivo", "oppo", "mi", "realme", "SAMSUNG", "1+")
                            items(brands) { brand ->
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color.White.copy(alpha = 0.9f)
                                ) {
                                    Text(
                                        text = brand,
                                        color = Color(0xFF0F172A),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFF59E0B)
                        ) {
                            Text(
                                text = "ক্যাশ মেমো",
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = profile?.shopName ?: "ইয়াছিন টেলিকম",
                            fontWeight = FontWeight.Black,
                            fontSize = 24.sp,
                            color = Color.White,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "স্মার্টফোন ও ডিজিটাল সেবা কেন্দ্র",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFE0F2FE)
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFFFEF2F2)
                            ) {
                                Text(
                                    text = "➤ প্রোঃ মোহাম্মদ ইয়াছিন ◄",
                                    color = Color(0xFF991B1B),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 11.5.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFF1E293B)
                            ) {
                                Text(
                                    text = "📞 01766906974",
                                    color = Color(0xFFFDE047),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.5.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFFDC2626),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "এখানে যাবতীয় পাইকারী ও খুচরা মালামাল এবং ডিজিটাল সেবা সুলভ মূল্যে প্রদান করা হয়।",
                            color = Color.White,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF0369A1),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "📍 বুল্লা বাজার, লাখাই, হবিগঞ্জ",
                            color = Color.White,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 3.dp)
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        MemoBadge(text = "নামঃ", bgColor = Color(0xFFDC2626))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = liveMemo.customerName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        MemoBadge(text = "তারিখঃ", bgColor = Color(0xFF16A34A))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = liveMemo.date, fontSize = 12.sp, color = TextPrimary)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        MemoBadge(text = "ক্রমিক নংঃ", bgColor = Color(0xFF7C3AED))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = liveMemo.memoNumber, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextPrimary)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        MemoBadge(text = "মোবাইলঃ", bgColor = Color(0xFF0284C7))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = if (liveMemo.customerMobile.isNotBlank()) liveMemo.customerMobile else "N/A", fontSize = 12.sp, color = TextPrimary)
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    MemoBadge(text = "ঠিকানাঃ", bgColor = Color(0xFF16A34A))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = liveMemo.customerAddress, fontSize = 12.sp, color = TextSecondary)
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF0284C7)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("ক্র.নং", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.width(36.dp), textAlign = TextAlign.Center)
                        Text("বিবরণ", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(1.8f))
                        Text("পরিমাণ", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.width(50.dp), textAlign = TextAlign.Center)
                        Text("দর", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.width(60.dp), textAlign = TextAlign.End)
                        Text("টাকা", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.width(70.dp), textAlign = TextAlign.End)
                        Spacer(modifier = Modifier.width(28.dp))
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                memoItems.forEachIndexed { index, item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("${index + 1}", fontSize = 11.5.sp, color = TextSecondary, modifier = Modifier.width(36.dp), textAlign = TextAlign.Center)
                        Text(item.description, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = TextPrimary, modifier = Modifier.weight(1.8f))
                        Text("${item.quantity}", fontSize = 12.sp, color = TextPrimary, modifier = Modifier.width(50.dp), textAlign = TextAlign.Center)
                        Text("৳${item.rate.toInt()}", fontSize = 12.sp, color = TextPrimary, modifier = Modifier.width(60.dp), textAlign = TextAlign.End)
                        Text("৳${item.amount.toInt()}", fontWeight = FontWeight.Bold, fontSize = 12.5.sp, color = Color(0xFF0284C7), modifier = Modifier.width(70.dp), textAlign = TextAlign.End)

                        IconButton(
                            onClick = { onRemoveItem(index) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Remove", tint = StatusRed, modifier = Modifier.size(15.dp))
                        }
                    }
                    Divider(color = Color(0xFFF1F5F9))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1.3f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        MemoBadge(text = "কথায়ঃ", bgColor = Color(0xFF06B6D4))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = takaInBengaliWords(grandTotal), fontSize = 11.sp, fontWeight = FontWeight.Medium, color = YasinNavy)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFF8FAFC),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text("📌 বিক্রিত মাল ফেরত নেওয়া হয় না।", fontSize = 10.sp, color = Color(0xFF64748B))
                            Text("🙏 ধন্যবাদ আবার আসবেন।", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF16A34A))
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.End) {
                        MemoBadge(text = "মোট টাকাঃ", bgColor = Color(0xFF16A34A))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("৳${grandTotal.toInt()}", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = YasinNavy)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.End) {
                        MemoBadge(text = "জমাঃ", bgColor = Color(0xFFDC2626))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("৳${paidAmount.toInt()}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF16A34A))
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.End) {
                        MemoBadge(text = "বাকীঃ", bgColor = Color(0xFF0284C7))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("৳${dueAmount.toInt()}", fontWeight = FontWeight.ExtraBold, fontSize = 13.5.sp, color = if (dueAmount > 0) Color(0xFFDC2626) else Color(0xFF64748B))
                    }
                }
            }

            Spacer(modifier = Modifier.height(26.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Divider(modifier = Modifier.width(100.dp), color = Color(0xFF94A3B8))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("ক্রেতার স্বাক্ষর", fontSize = 10.5.sp, color = TextSecondary)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Divider(modifier = Modifier.width(130.dp), color = Color(0xFF94A3B8))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("বিক্রেতার স্বাক্ষর (ইয়াছিন টেলিকম)", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0284C7))
                }
            }
        }
    }
}

@Composable
fun MemoBadge(
    text: String,
    bgColor: Color
) {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = bgColor
    ) {
        Text(
            text = text,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 9.sp,
            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
        )
    }
}
