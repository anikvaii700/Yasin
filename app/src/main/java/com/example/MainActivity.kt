package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.viewmodel.initializer
import com.example.auth.AuthManager
import com.example.data.repository.LocalShopRepository
import com.example.data.repository.ShopDataSource
import com.example.data.repository.ShopRepository
import com.example.ui.ShopViewModel
import com.example.ui.screens.*
import com.example.ui.theme.YasinGreen
import com.example.ui.theme.YasinNavy
import com.example.ui.theme.YasinNavyDark
import com.example.ui.theme.YasinTelecomTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

enum class MainTab(val title: String, val icon: ImageVector, val tag: String) {
    HOME("হোম", Icons.Default.Home, "tab_home"),
    PRODUCTS("পণ্য", Icons.Default.Inventory2, "tab_products"),
    SALES("বিক্রয়", Icons.Default.ShoppingCart, "tab_sales"),
    MEMO("মেমো", Icons.Default.Description, "tab_memo"),
    DUE("বাকি", Icons.Default.AccountBalanceWallet, "tab_due"),
    MORE("আরও", Icons.Default.MoreHoriz, "tab_more"),
    ADMIN("অ্যাডমিন", Icons.Default.AdminPanelSettings, "tab_admin")
}

data class DrawerItem(val id: Int, val title: String, val icon: ImageVector, val tag: String)

val drawerItems = listOf(
    DrawerItem(1, "ড্যাশবোর্ড (Dashboard)", Icons.Default.Dashboard, "drawer_dashboard"),
    DrawerItem(2, "পণ্য ও স্টক (Products & Stock)", Icons.Default.Inventory2, "drawer_products"),
    DrawerItem(3, "বিক্রয় হিসাব (Sales)", Icons.Default.ShoppingCart, "drawer_sales"),
    DrawerItem(4, "পণ্য ক্রয় (Purchase)", Icons.Default.ShoppingBag, "drawer_purchase"),
    DrawerItem(5, "বাকি খাতা (Customer Due)", Icons.Default.AccountBalanceWallet, "drawer_due"),
    DrawerItem(6, "ব্যক্তিগত ধার-দেনা (Personal Loans)", Icons.Default.People, "drawer_loans"),
    DrawerItem(7, "বিকাশ ও নগদ (bKash & Nagad)", Icons.Default.PhoneAndroid, "drawer_mobile_banking"),
    DrawerItem(8, "ফ্লেক্সিলোড (Flexiload)", Icons.Default.Bolt, "drawer_flexiload"),
    DrawerItem(9, "দোকানের খরচ (Expenses)", Icons.Default.ReceiptLong, "drawer_expenses"),
    DrawerItem(10, "ক্যাশ মেমো (Cash Memo)", Icons.Default.Description, "drawer_cash_memo"),
    DrawerItem(11, "দৈনিক হিসাব ক্লোজিং (Daily Closing)", Icons.Default.FactCheck, "drawer_daily_closing"),
    DrawerItem(12, "রিপোর্ট ও বিশ্লেষণ (Reports)", Icons.Default.BarChart, "drawer_reports"),
    DrawerItem(13, "সেটিংস ও ব্যাকআপ (Settings)", Icons.Default.Settings, "drawer_settings"),
    DrawerItem(14, "অ্যাডমিন প্যানেল (Admin Panel)", Icons.Default.AdminPanelSettings, "drawer_admin_panel"),
    DrawerItem(15, "ডেমো ডাটা রিসেট (Reset Demo Data)", Icons.Default.RestartAlt, "drawer_reset_demo")
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            YasinTelecomTheme {
                AppRootContent()
            }
        }
    }
}

@Composable
fun AppRootContent() {
    var currentUser by remember { mutableStateOf(Firebase.auth.currentUser) }
    var isOfflineMode by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            currentUser = auth.currentUser
        }
        Firebase.auth.addAuthStateListener(listener)
        onDispose {
            Firebase.auth.removeAuthStateListener(listener)
        }
    }

    if (currentUser == null && !isOfflineMode) {
        AuthScreen(
            onAuthSuccess = {
                currentUser = Firebase.auth.currentUser
            },
            onContinueOffline = {
                isOfflineMode = true
            }
        )
    } else {
        val uid = currentUser?.uid ?: "local_offline_user"
        val offline = currentUser == null || isOfflineMode
        key("${uid}_$offline") {
            MainAppScreen(
                currentUserId = uid,
                isOfflineMode = offline,
                onSwitchToOnline = {
                    isOfflineMode = false
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(
    currentUserId: String,
    isOfflineMode: Boolean = false,
    onSwitchToOnline: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }
    val snackbarHostState = remember { SnackbarHostState() }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    val viewModel: ShopViewModel = viewModel(
        key = "${currentUserId}_$isOfflineMode",
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY])
                val repository: ShopDataSource = if (isOfflineMode) {
                    LocalShopRepository(app)
                } else {
                    val databaseId = app.getString(R.string.firestore_database_id)
                    val db = FirebaseFirestore.getInstance(databaseId)
                    ShopRepository(db)
                }
                ShopViewModel(repository, currentUserId)
            }
        }
    )

    var currentTab by remember { mutableStateOf(MainTab.HOME) }

    // Quick action trigger states
    var openAddProductTrigger by remember { mutableStateOf(false) }
    var openRecordSaleTrigger by remember { mutableStateOf(false) }
    var openAddExpenseTrigger by remember { mutableStateOf(false) }
    var openAddDueTrigger by remember { mutableStateOf(false) }
    var openAddPurchaseTrigger by remember { mutableStateOf(false) }

    // Sub-screen navigation inside More tab
    var moreSubScreen by remember { mutableStateOf<String?>(null) }

    // Listen for notification messages
    val message by viewModel.message.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val profile by viewModel.profile.collectAsStateWithLifecycle()

    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    // Handle Back Press cleanly
    BackHandler(enabled = drawerState.isOpen || currentTab != MainTab.HOME) {
        if (drawerState.isOpen) {
            coroutineScope.launch { drawerState.close() }
        } else if (currentTab != MainTab.HOME) {
            currentTab = MainTab.HOME
            moreSubScreen = null
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Color.White,
                modifier = Modifier
                    .widthIn(max = 320.dp)
                    .fillMaxHeight()
            ) {
                // Drawer Header Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(YasinNavyDark)
                        .padding(20.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(YasinGreen),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Store, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = profile?.shopName ?: "ইয়াছিন টেলিকম",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 17.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = "স্বত্বাধিকারী: ${profile?.ownerName ?: "মোহাম্মদ ইয়াছিন"}",
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "📍 ${profile?.address ?: "বুল্লা বাজার, লাখাই, হবিগঞ্জ"}",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }

                Divider(color = Color(0xFFE2E8F0))

                // 15 Drawer Items list
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 8.dp)
                ) {
                    drawerItems.forEach { item ->
                        NavigationDrawerItem(
                            icon = {
                                Icon(
                                    item.icon,
                                    contentDescription = item.title,
                                    tint = if (item.id == 14 || item.id == 15) Color(0xFFDC2626) else YasinNavy
                                )
                            },
                            label = {
                                Text(
                                    text = item.title,
                                    fontSize = 13.sp,
                                    fontWeight = if (item.id == 14 || item.id == 15) FontWeight.Bold else FontWeight.Medium,
                                    color = if (item.id == 14 || item.id == 15) Color(0xFFDC2626) else Color(0xFF0F172A)
                                )
                            },
                            selected = false,
                            onClick = {
                                coroutineScope.launch { drawerState.close() }
                                when (item.id) {
                                    1 -> currentTab = MainTab.HOME
                                    2 -> currentTab = MainTab.PRODUCTS
                                    3 -> currentTab = MainTab.SALES
                                    4 -> {
                                        openAddPurchaseTrigger = true
                                        currentTab = MainTab.MORE
                                    }
                                    5 -> currentTab = MainTab.DUE
                                    6 -> {
                                        currentTab = MainTab.MORE
                                    }
                                    7 -> {
                                        currentTab = MainTab.MORE
                                    }
                                    8 -> {
                                        currentTab = MainTab.MORE
                                    }
                                    9 -> {
                                        openAddExpenseTrigger = true
                                        currentTab = MainTab.MORE
                                    }
                                    10 -> currentTab = MainTab.MEMO
                                    11 -> currentTab = MainTab.MORE
                                    12 -> currentTab = MainTab.MORE
                                    13 -> currentTab = MainTab.MORE
                                    14 -> currentTab = MainTab.ADMIN
                                    15 -> {
                                        currentTab = MainTab.ADMIN
                                    }
                                }
                            },
                            modifier = Modifier
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                .testTag(item.tag)
                        )
                    }
                }
            }
        }
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                NavigationBar(
                    containerColor = Color.White,
                    tonalElevation = 8.dp,
                    windowInsets = WindowInsets.navigationBars
                ) {
                    listOf(MainTab.HOME, MainTab.PRODUCTS, MainTab.SALES, MainTab.MEMO, MainTab.DUE, MainTab.MORE).forEach { tab ->
                        val isSelected = currentTab == tab
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { currentTab = tab },
                            icon = {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = tab.title,
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = tab.title,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 11.5.sp
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.White,
                                selectedTextColor = YasinNavy,
                                indicatorColor = if (tab == MainTab.MEMO) Color(0xFFEA580C) else YasinGreen,
                                unselectedIconColor = Color(0xFF64748B),
                                unselectedTextColor = Color(0xFF64748B)
                            ),
                            modifier = Modifier.testTag(tab.tag)
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Crossfade(targetState = currentTab, label = "TabCrossfade") { tab ->
                    when (tab) {
                        MainTab.HOME -> {
                            HomeScreen(
                                viewModel = viewModel,
                                onNavigateToProducts = { currentTab = MainTab.PRODUCTS },
                                onNavigateToSales = { currentTab = MainTab.SALES },
                                onNavigateToDue = { currentTab = MainTab.DUE },
                                onNavigateToPurchases = {
                                    openAddPurchaseTrigger = true
                                    currentTab = MainTab.MORE
                                },
                                onNavigateToExpenses = {
                                    openAddExpenseTrigger = true
                                    currentTab = MainTab.MORE
                                },
                                onNavigateToMobileBanking = { currentTab = MainTab.MORE },
                                onNavigateToCashMemo = { currentTab = MainTab.MEMO },
                                onOpenAddProductDialog = {
                                    openAddProductTrigger = true
                                    currentTab = MainTab.PRODUCTS
                                },
                                onOpenRecordSaleDialog = {
                                    openRecordSaleTrigger = true
                                    currentTab = MainTab.SALES
                                },
                                onOpenAddExpenseDialog = {
                                    openAddExpenseTrigger = true
                                    currentTab = MainTab.MORE
                                },
                                onOpenAddDueDialog = {
                                    openAddDueTrigger = true
                                    currentTab = MainTab.DUE
                                },
                                onOpenAddPurchaseDialog = {
                                    openAddPurchaseTrigger = true
                                    currentTab = MainTab.MORE
                                },
                                onOpenDrawer = {
                                    coroutineScope.launch { drawerState.open() }
                                },
                                onOpenAdminPanel = {
                                    currentTab = MainTab.ADMIN
                                }
                            )
                        }
                        MainTab.PRODUCTS -> {
                            ProductsScreen(
                                viewModel = viewModel,
                                openAddProductTrigger = openAddProductTrigger,
                                onResetAddProductTrigger = { openAddProductTrigger = false }
                            )
                        }
                        MainTab.SALES -> {
                            SalesScreen(
                                viewModel = viewModel,
                                openRecordSaleTrigger = openRecordSaleTrigger,
                                onResetRecordSaleTrigger = { openRecordSaleTrigger = false }
                            )
                        }
                        MainTab.MEMO -> {
                            CashMemoScreen(viewModel = viewModel)
                        }
                        MainTab.DUE -> {
                            DueScreen(
                                viewModel = viewModel,
                                openAddDueTrigger = openAddDueTrigger,
                                onResetAddDueTrigger = { openAddDueTrigger = false }
                            )
                        }
                        MainTab.MORE -> {
                            MoreScreen(
                                viewModel = viewModel,
                                onSignOut = {
                                    if (isOfflineMode) {
                                        onSwitchToOnline()
                                    } else {
                                        AuthManager.signOut(
                                            credentialManager = credentialManager,
                                            scope = coroutineScope
                                        )
                                    }
                                },
                                openAddPurchaseTrigger = openAddPurchaseTrigger,
                                openAddExpenseTrigger = openAddExpenseTrigger,
                                onResetAddPurchaseTrigger = { openAddPurchaseTrigger = false },
                                onResetAddExpenseTrigger = { openAddExpenseTrigger = false }
                            )
                        }
                        MainTab.ADMIN -> {
                            AdminPanelScreen(
                                viewModel = viewModel,
                                onExitAdmin = {
                                    currentTab = MainTab.HOME
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
