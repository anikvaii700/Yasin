import React, { createContext, useContext, useState, useEffect } from 'react';
import {
  ShopProfile,
  Product,
  StockAddition,
  Sale,
  SaleItem,
  CashMemo,
  Purchase,
  Expense,
  Customer,
  CustomerTransaction,
  Loan,
  MobileBankingTx,
  FlexiloadTx,
  DailyClosing,
  CustomCategory,
} from '../types';
import { getTodayStr, getCurrentTimeStr, generateMemoNumber, generateInvoiceNumber } from '../utils/formatters';

export const DEFAULT_CATEGORIES: string[] = [
  'New Mobile Phones',
  'Used Mobile Phones',
  'Mobile Chargers',
  'Batteries',
  'Tempered Glass',
  'Mobile Covers',
  'Earphones',
  'Headphones',
  'Data Cables',
  'USB Cables',
  'Memory Cards',
  'Power Banks',
  'Speakers',
  'Mobile Parts',
  'Other Accessories',
];

const INITIAL_PROFILE: ShopProfile = {
  id: 'shop_info',
  userId: 'local_guest_owner',
  shopName: 'ইয়াছিন টেলিকম',
  ownerName: 'মোহাম্মদ ইয়াছিন',
  address: 'বুল্লা বাজার, লাখাই, হবিগঞ্জ',
  phone: '01766906974',
  email: 'yasintelecom@gmail.com',
  tagline: 'এখানে যাবতীয় পাইকারী ও খুচরা মালামাল এবং ডিজিটাল সেবা সুলভ মূল্যে প্রদান করা হয়।',
  currency: '৳',
  lowStockAlertThreshold: 5,
  adminPin: '1234',
};

const INITIAL_PRODUCTS: Product[] = [
  {
    id: 'prod-1',
    userId: 'local_guest_owner',
    name: 'Samsung 25W Fast Charger Type-C',
    category: 'Mobile Chargers',
    brand: 'Samsung',
    model: 'EP-TA800',
    purchasePrice: 350,
    sellingPrice: 550,
    currentStock: 14,
    minStockAlert: 4,
    productCode: 'CHG-SAM-25W',
    notes: 'Original fast adapter',
    isDemo: true,
  },
  {
    id: 'prod-2',
    userId: 'local_guest_owner',
    name: '9D Curved Tempered Glass',
    category: 'Tempered Glass',
    brand: 'Universal',
    model: 'All Models',
    purchasePrice: 35,
    sellingPrice: 120,
    currentStock: 48,
    minStockAlert: 10,
    productCode: 'GLS-9D-UNI',
    notes: 'Premium anti-scratch protection',
    isDemo: true,
  },
  {
    id: 'prod-3',
    userId: 'local_guest_owner',
    name: 'Mi 10,000mAh Power Bank 3i',
    category: 'Power Banks',
    brand: 'Xiaomi',
    model: 'VXN4274IN',
    purchasePrice: 1100,
    sellingPrice: 1450,
    currentStock: 2, // Low stock alert
    minStockAlert: 5,
    productCode: 'PB-MI-10K',
    notes: 'Dual USB output fast charge',
    isDemo: true,
  },
  {
    id: 'prod-4',
    userId: 'local_guest_owner',
    name: 'Realme SuperDart Type-C Cable 65W',
    category: 'Data Cables',
    brand: 'Realme',
    model: '6.5A Fast',
    purchasePrice: 90,
    sellingPrice: 200,
    currentStock: 18,
    minStockAlert: 5,
    productCode: 'CBL-RM-65W',
    notes: 'Yellow VOOC/SuperDart cable',
    isDemo: true,
  },
  {
    id: 'prod-5',
    userId: 'local_guest_owner',
    name: 'UiiSii C100 In-Ear Headphone with Mic',
    category: 'Earphones',
    brand: 'UiiSii',
    model: 'C100',
    purchasePrice: 110,
    sellingPrice: 220,
    currentStock: 0, // Out of stock
    minStockAlert: 5,
    productCode: 'EAR-UII-C100',
    notes: 'Popular bass earphone',
    isDemo: true,
  },
  {
    id: 'prod-6',
    userId: 'local_guest_owner',
    name: 'Nokia BL-5C Original Battery',
    category: 'Batteries',
    brand: 'Nokia',
    model: 'BL-5C',
    purchasePrice: 120,
    sellingPrice: 250,
    currentStock: 8,
    minStockAlert: 3,
    productCode: 'BAT-NOK-BL5C',
    notes: '1020mAh original battery for button phones',
    isDemo: true,
  },
  {
    id: 'prod-7',
    userId: 'local_guest_owner',
    name: 'Vivo Y17s Matte Shockproof Cover',
    category: 'Mobile Covers',
    brand: 'Vivo',
    model: 'Y17s',
    purchasePrice: 60,
    sellingPrice: 150,
    currentStock: 3, // Low stock
    minStockAlert: 5,
    productCode: 'COV-VIV-Y17S',
    notes: 'Camera protector design',
    isDemo: true,
  },
];

const INITIAL_CUSTOMERS: Customer[] = [
  {
    id: 'cust-1',
    userId: 'local_guest_owner',
    name: 'করিম উল্লাহ',
    mobile: '01712345678',
    address: 'পূর্ব বুল্লা, লাখাই',
    totalDue: 1850,
    totalPaid: 1000,
    remainingDue: 850,
    isDemo: true,
  },
  {
    id: 'cust-2',
    userId: 'local_guest_owner',
    name: 'রফিকুল ইসলাম',
    mobile: '01898765432',
    address: 'বাজার রোড, বুল্লা',
    totalDue: 450,
    totalPaid: 0,
    remainingDue: 450,
    isDemo: true,
  },
];

const INITIAL_CUSTOMER_TX: Record<string, CustomerTransaction[]> = {
  'cust-1': [
    {
      id: 'tx-c1-1',
      userId: 'local_guest_owner',
      customerId: 'cust-1',
      type: 'DUE',
      amount: 1850,
      notes: 'হেডফোন ও চার্জার বাকি ক্রয়',
      transactionDate: getTodayStr(),
      isDemo: true,
    },
    {
      id: 'tx-c1-2',
      userId: 'local_guest_owner',
      customerId: 'cust-1',
      type: 'PAYMENT',
      amount: 1000,
      notes: 'নগদ জমা পরিশোধ',
      transactionDate: getTodayStr(),
      isDemo: true,
    },
  ],
  'cust-2': [
    {
      id: 'tx-c2-1',
      userId: 'local_guest_owner',
      customerId: 'cust-2',
      type: 'DUE',
      amount: 450,
      notes: 'গ্লাস ও ব্যাক কভার বাকি',
      transactionDate: getTodayStr(),
      isDemo: true,
    },
  ],
};

const INITIAL_LOANS: Loan[] = [
  {
    id: 'loan-1',
    userId: 'local_guest_owner',
    personName: 'তারেক মাহমুদ',
    mobile: '01733445566',
    loanType: 'LENT',
    amount: 5000,
    paidAmount: 2000,
    remainingAmount: 3000,
    description: 'ব্যবসায়িক সহায়তা ধার',
    expectedPaymentDate: '25-11-2026',
    isDemo: true,
  },
  {
    id: 'loan-2',
    userId: 'local_guest_owner',
    personName: 'আলমগীর হোসেন (মহাজন)',
    mobile: '01811223344',
    loanType: 'BORROWED',
    amount: 10000,
    paidAmount: 5000,
    remainingAmount: 5000,
    description: 'মালামাল তোলার নগদ ধার',
    expectedPaymentDate: '30-11-2026',
    isDemo: true,
  },
];

const INITIAL_MOBILE_BANKING: MobileBankingTx[] = [
  {
    id: 'mb-1',
    userId: 'local_guest_owner',
    provider: 'BKASH',
    type: 'CASH_IN',
    amount: 2500,
    commissionProfit: 10,
    serviceCharge: 0,
    customerPhone: '01712998877',
    notes: 'ক্যাশ ইন ক্যাশ জমা',
    transactionDate: getTodayStr(),
    isDemo: true,
  },
  {
    id: 'mb-2',
    userId: 'local_guest_owner',
    provider: 'NAGAD',
    type: 'CASH_OUT',
    amount: 1500,
    commissionProfit: 12,
    serviceCharge: 0,
    customerPhone: '01822334455',
    notes: 'নগদ ক্যাশ আউট কমিশন',
    transactionDate: getTodayStr(),
    isDemo: true,
  },
];

const INITIAL_FLEXILOAD: FlexiloadTx[] = [
  {
    id: 'fl-1',
    userId: 'local_guest_owner',
    mobileNumber: '01711223344',
    operator: 'Grameenphone',
    rechargeAmount: 199,
    customerPayment: 199,
    rechargeCost: 193,
    profit: 6,
    transactionDate: getTodayStr(),
    notes: 'মিনিট প্যাক অফার রিচার্জ',
    isDemo: true,
  },
  {
    id: 'fl-2',
    userId: 'local_guest_owner',
    mobileNumber: '01922334455',
    operator: 'Banglalink',
    rechargeAmount: 298,
    customerPayment: 298,
    rechargeCost: 288,
    profit: 10,
    transactionDate: getTodayStr(),
    notes: 'ইন্টারনেট বান্ডেল',
    isDemo: true,
  },
];

const INITIAL_SALES: Sale[] = [
  {
    id: 'sale-1',
    userId: 'local_guest_owner',
    invoiceNumber: 'INV-1001',
    customerName: 'মাসুদ রানা',
    customerPhone: '01722334455',
    itemsSummary: 'Samsung 25W Fast Charger Type-C x1, 9D Curved Tempered Glass x1',
    totalAmount: 670,
    totalCost: 385,
    grossProfit: 285,
    paidAmount: 670,
    dueAmount: 0,
    saleDate: getTodayStr(),
    notes: 'নগদ বিক্রয় পরিশোধ',
    isDemo: true,
  },
];

const INITIAL_MEMOS: CashMemo[] = [
  {
    id: 'memo-1',
    userId: 'local_guest_owner',
    memoNumber: 'YT-260301-101',
    date: getTodayStr(),
    time: '11:30 AM',
    customerName: 'মাসুদ রানা',
    customerMobile: '01722334455',
    customerAddress: 'বুল্লা বাজার',
    items: [
      {
        productId: 'prod-1',
        description: 'Samsung 25W Fast Charger Type-C',
        quantity: 1,
        rate: 550,
        amount: 550,
      },
      {
        productId: 'prod-2',
        description: '9D Curved Tempered Glass',
        quantity: 1,
        rate: 120,
        amount: 120,
      },
    ],
    subtotal: 670,
    discount: 0,
    grandTotal: 670,
    paidAmount: 670,
    dueAmount: 0,
    paymentStatus: 'PAID',
    notes: 'ধন্যবাদ, আবার আসবেন',
    saleId: 'sale-1',
    isDemo: true,
  },
];

const INITIAL_EXPENSES: Expense[] = [
  {
    id: 'exp-1',
    userId: 'local_guest_owner',
    title: 'দোকানের চা-নাস্তা ও মেহমানদারী',
    amount: 120,
    category: 'Entertainment',
    expenseDate: getTodayStr(),
    description: 'গ্রাহকদের চা ও বিস্কুট',
    isDemo: true,
  },
  {
    id: 'exp-2',
    userId: 'local_guest_owner',
    title: 'বিদ্যুৎ বিল (বিল্ডিং ভাগ)',
    amount: 450,
    category: 'Shop Rent & Utilities',
    expenseDate: getTodayStr(),
    description: 'চলতি মাসের বিদ্যুৎ অংশ',
    isDemo: true,
  },
];

const STORAGE_KEY = 'yasin_telecom_v1_store';

interface ShopContextType {
  profile: ShopProfile;
  products: Product[];
  sales: Sale[];
  purchases: Purchase[];
  expenses: Expense[];
  customers: Customer[];
  customerTransactions: Record<string, CustomerTransaction[]>;
  loans: Loan[];
  mobileBanking: MobileBankingTx[];
  flexiload: FlexiloadTx[];
  dailyClosings: DailyClosing[];
  customCategories: CustomCategory[];
  stockAdditions: StockAddition[];
  cashMemos: CashMemo[];
  todayDateStr: string;

  // Admin
  isAdminAuthenticated: boolean;
  loginAdmin: (pin: string) => boolean;
  logoutAdmin: () => void;
  changeAdminPin: (newPin: string) => void;
  resetDemoData: () => number;
  resetAllData: () => void;

  // Messages
  message: string | null;
  errorMessage: string | null;
  showMessage: (msg: string) => void;
  showError: (msg: string) => void;
  clearMessage: () => void;

  // Categories
  getAllCategories: () => string[];
  addCustomCategory: (name: string) => void;
  deleteCustomCategory: (id: string) => void;

  // Profile
  updateProfile: (profile: ShopProfile) => void;

  // Products & Stock
  addProduct: (product: Omit<Product, 'id' | 'userId'>) => string;
  updateProduct: (product: Product) => void;
  deleteProduct: (productId: string) => void;
  addStock: (
    productId: string,
    addedQty: number,
    purchasePrice: number,
    supplierName: string,
    notes: string
  ) => void;

  // Sales
  recordSale: (sale: Omit<Sale, 'id' | 'userId'>, items?: SaleItem[]) => string;
  deleteSale: (saleId: string) => void;

  // Cash Memos
  saveCashMemo: (memo: Omit<CashMemo, 'id' | 'userId'>, autoRecordSale: boolean) => string;
  deleteCashMemo: (memoId: string) => void;

  // Purchases
  recordPurchase: (purchase: Omit<Purchase, 'id' | 'userId'>) => void;
  deletePurchase: (purchaseId: string) => void;

  // Expenses
  recordExpense: (expense: Omit<Expense, 'id' | 'userId'>) => void;
  deleteExpense: (expenseId: string) => void;

  // Customers & Dues
  addCustomer: (customer: Omit<Customer, 'id' | 'userId'>) => string;
  recordCustomerTx: (
    customerId: string,
    tx: Omit<CustomerTransaction, 'id' | 'userId' | 'customerId'>
  ) => void;
  deleteCustomer: (customerId: string) => void;
  deleteCustomerTx: (customerId: string, txId: string) => void;

  // Stock Additions
  deleteStockAddition: (id: string) => void;

  // Demo Counts
  getDemoCounts: () => {
    products: number;
    sales: number;
    purchases: number;
    expenses: number;
    customers: number;
    loans: number;
    mobileBanking: number;
    flexiload: number;
    memos: number;
    total: number;
  };

  // Loans
  addLoan: (loan: Omit<Loan, 'id' | 'userId'>) => void;
  recordLoanPayment: (loanId: string, paymentAmount: number) => void;
  deleteLoan: (loanId: string) => void;

  // Mobile Banking & Flexiload
  recordMobileBanking: (tx: Omit<MobileBankingTx, 'id' | 'userId'>) => void;
  deleteMobileBanking: (txId: string) => void;
  recordFlexiload: (tx: Omit<FlexiloadTx, 'id' | 'userId'>) => void;
  deleteFlexiload: (txId: string) => void;

  // Daily Closing
  recordDailyClosing: (closing: Omit<DailyClosing, 'id' | 'userId'>) => void;

  // Backup & Restore
  exportBackupJson: () => string;
  restoreBackupJson: (jsonString: string) => number;
}

const ShopContext = createContext<ShopContextType | undefined>(undefined);

export const ShopProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [profile, setProfile] = useState<ShopProfile>(() => {
    const saved = localStorage.getItem(`${STORAGE_KEY}_profile`);
    return saved ? JSON.parse(saved) : INITIAL_PROFILE;
  });

  const [products, setProducts] = useState<Product[]>(() => {
    const saved = localStorage.getItem(`${STORAGE_KEY}_products`);
    return saved ? JSON.parse(saved) : INITIAL_PRODUCTS;
  });

  const [sales, setSales] = useState<Sale[]>(() => {
    const saved = localStorage.getItem(`${STORAGE_KEY}_sales`);
    return saved ? JSON.parse(saved) : INITIAL_SALES;
  });

  const [purchases, setPurchases] = useState<Purchase[]>(() => {
    const saved = localStorage.getItem(`${STORAGE_KEY}_purchases`);
    return saved ? JSON.parse(saved) : [];
  });

  const [expenses, setExpenses] = useState<Expense[]>(() => {
    const saved = localStorage.getItem(`${STORAGE_KEY}_expenses`);
    return saved ? JSON.parse(saved) : INITIAL_EXPENSES;
  });

  const [customers, setCustomers] = useState<Customer[]>(() => {
    const saved = localStorage.getItem(`${STORAGE_KEY}_customers`);
    return saved ? JSON.parse(saved) : INITIAL_CUSTOMERS;
  });

  const [customerTransactions, setCustomerTransactions] = useState<Record<string, CustomerTransaction[]>>(() => {
    const saved = localStorage.getItem(`${STORAGE_KEY}_customer_tx`);
    return saved ? JSON.parse(saved) : INITIAL_CUSTOMER_TX;
  });

  const [loans, setLoans] = useState<Loan[]>(() => {
    const saved = localStorage.getItem(`${STORAGE_KEY}_loans`);
    return saved ? JSON.parse(saved) : INITIAL_LOANS;
  });

  const [mobileBanking, setMobileBanking] = useState<MobileBankingTx[]>(() => {
    const saved = localStorage.getItem(`${STORAGE_KEY}_mb`);
    return saved ? JSON.parse(saved) : INITIAL_MOBILE_BANKING;
  });

  const [flexiload, setFlexiload] = useState<FlexiloadTx[]>(() => {
    const saved = localStorage.getItem(`${STORAGE_KEY}_flexi`);
    return saved ? JSON.parse(saved) : INITIAL_FLEXILOAD;
  });

  const [dailyClosings, setDailyClosings] = useState<DailyClosing[]>(() => {
    const saved = localStorage.getItem(`${STORAGE_KEY}_closings`);
    return saved ? JSON.parse(saved) : [];
  });

  const [customCategories, setCustomCategories] = useState<CustomCategory[]>(() => {
    const saved = localStorage.getItem(`${STORAGE_KEY}_categories`);
    return saved ? JSON.parse(saved) : [];
  });

  const [stockAdditions, setStockAdditions] = useState<StockAddition[]>(() => {
    const saved = localStorage.getItem(`${STORAGE_KEY}_stock_additions`);
    return saved ? JSON.parse(saved) : [];
  });

  const [cashMemos, setCashMemos] = useState<CashMemo[]>(() => {
    const saved = localStorage.getItem(`${STORAGE_KEY}_memos`);
    return saved ? JSON.parse(saved) : INITIAL_MEMOS;
  });

  const [isAdminAuthenticated, setIsAdminAuthenticated] = useState<boolean>(false);
  const [message, setMessage] = useState<string | null>(null);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  // Sync to localStorage
  useEffect(() => {
    localStorage.setItem(`${STORAGE_KEY}_profile`, JSON.stringify(profile));
  }, [profile]);

  useEffect(() => {
    localStorage.setItem(`${STORAGE_KEY}_products`, JSON.stringify(products));
  }, [products]);

  useEffect(() => {
    localStorage.setItem(`${STORAGE_KEY}_sales`, JSON.stringify(sales));
  }, [sales]);

  useEffect(() => {
    localStorage.setItem(`${STORAGE_KEY}_purchases`, JSON.stringify(purchases));
  }, [purchases]);

  useEffect(() => {
    localStorage.setItem(`${STORAGE_KEY}_expenses`, JSON.stringify(expenses));
  }, [expenses]);

  useEffect(() => {
    localStorage.setItem(`${STORAGE_KEY}_customers`, JSON.stringify(customers));
  }, [customers]);

  useEffect(() => {
    localStorage.setItem(`${STORAGE_KEY}_customer_tx`, JSON.stringify(customerTransactions));
  }, [customerTransactions]);

  useEffect(() => {
    localStorage.setItem(`${STORAGE_KEY}_loans`, JSON.stringify(loans));
  }, [loans]);

  useEffect(() => {
    localStorage.setItem(`${STORAGE_KEY}_mb`, JSON.stringify(mobileBanking));
  }, [mobileBanking]);

  useEffect(() => {
    localStorage.setItem(`${STORAGE_KEY}_flexi`, JSON.stringify(flexiload));
  }, [flexiload]);

  useEffect(() => {
    localStorage.setItem(`${STORAGE_KEY}_closings`, JSON.stringify(dailyClosings));
  }, [dailyClosings]);

  useEffect(() => {
    localStorage.setItem(`${STORAGE_KEY}_categories`, JSON.stringify(customCategories));
  }, [customCategories]);

  useEffect(() => {
    localStorage.setItem(`${STORAGE_KEY}_stock_additions`, JSON.stringify(stockAdditions));
  }, [stockAdditions]);

  useEffect(() => {
    localStorage.setItem(`${STORAGE_KEY}_memos`, JSON.stringify(cashMemos));
  }, [cashMemos]);

  const showMessage = (msg: string) => {
    setMessage(msg);
    setTimeout(() => setMessage(null), 4000);
  };

  const showError = (msg: string) => {
    setErrorMessage(msg);
    setTimeout(() => setErrorMessage(null), 5000);
  };

  const clearMessage = () => {
    setMessage(null);
    setErrorMessage(null);
  };

  // Admin Methods
  const loginAdmin = (pin: string): boolean => {
    const success = (profile.adminPin || '1234') === pin.trim();
    setIsAdminAuthenticated(success);
    if (success) {
      showMessage('অ্যাডমিন প্যানেলে সফলভাবে প্রবেশ করা হয়েছে');
    } else {
      showError('ভুল অ্যাডমিন পিন! সঠিক পিন দিন।');
    }
    return success;
  };

  const logoutAdmin = () => {
    setIsAdminAuthenticated(false);
    showMessage('অ্যাডমিন প্যানেল লক করা হয়েছে');
  };

  // Auto-lock admin panel after 5 minutes of inactivity
  useEffect(() => {
    if (!isAdminAuthenticated) return;
    const timer = setTimeout(() => {
      setIsAdminAuthenticated(false);
      showMessage('নিষ্ক্রিয়তার কারণে অ্যাডমিন প্যানেল স্বয়ংক্রিয়ভাবে লক করা হয়েছে');
    }, 5 * 60 * 1000);
    return () => clearTimeout(timer);
  }, [isAdminAuthenticated]);

  const getDemoCounts = () => {
    const p = products.filter((item) => item.isDemo).length;
    const s = sales.filter((item) => item.isDemo).length;
    const pur = purchases.filter((item) => item.isDemo).length;
    const e = expenses.filter((item) => item.isDemo).length;
    const c = customers.filter((item) => item.isDemo).length;
    const l = loans.filter((item) => item.isDemo).length;
    const mb = mobileBanking.filter((item) => item.isDemo).length;
    const fl = flexiload.filter((item) => item.isDemo).length;
    const m = cashMemos.filter((item) => item.isDemo).length;
    return {
      products: p,
      sales: s,
      purchases: pur,
      expenses: e,
      customers: c,
      loans: l,
      mobileBanking: mb,
      flexiload: fl,
      memos: m,
      total: p + s + pur + e + c + l + mb + fl + m,
    };
  };

  const changeAdminPin = (newPin: string) => {
    setProfile((prev) => ({ ...prev, adminPin: newPin.trim() }));
    showMessage('অ্যাডমিন পিন সফলভাবে পরিবর্তন করা হয়েছে');
  };

  const resetDemoData = (): number => {
    let removed = 0;
    setProducts((prev) => {
      const filtered = prev.filter((p) => !p.isDemo);
      removed += prev.length - filtered.length;
      return filtered;
    });
    setSales((prev) => {
      const filtered = prev.filter((s) => !s.isDemo);
      removed += prev.length - filtered.length;
      return filtered;
    });
    setExpenses((prev) => {
      const filtered = prev.filter((e) => !e.isDemo);
      removed += prev.length - filtered.length;
      return filtered;
    });
    setCustomers((prev) => {
      const filtered = prev.filter((c) => !c.isDemo);
      removed += prev.length - filtered.length;
      return filtered;
    });
    setLoans((prev) => {
      const filtered = prev.filter((l) => !l.isDemo);
      removed += prev.length - filtered.length;
      return filtered;
    });
    setMobileBanking((prev) => {
      const filtered = prev.filter((m) => !m.isDemo);
      removed += prev.length - filtered.length;
      return filtered;
    });
    setFlexiload((prev) => {
      const filtered = prev.filter((f) => !f.isDemo);
      removed += prev.length - filtered.length;
      return filtered;
    });
    setCashMemos((prev) => {
      const filtered = prev.filter((m) => !m.isDemo);
      removed += prev.length - filtered.length;
      return filtered;
    });

    showMessage(`${removed} টি ডেমো রেকর্ড সফলভাবে মুছে ফেলা হয়েছে! আসল ডাটা অক্ষত আছে।`);
    return removed;
  };

  const resetAllData = () => {
    setProducts(INITIAL_PRODUCTS);
    setSales(INITIAL_SALES);
    setPurchases([]);
    setExpenses(INITIAL_EXPENSES);
    setCustomers(INITIAL_CUSTOMERS);
    setCustomerTransactions(INITIAL_CUSTOMER_TX);
    setLoans(INITIAL_LOANS);
    setMobileBanking(INITIAL_MOBILE_BANKING);
    setFlexiload(INITIAL_FLEXILOAD);
    setDailyClosings([]);
    setCustomCategories([]);
    setStockAdditions([]);
    setCashMemos(INITIAL_MEMOS);
    setProfile(INITIAL_PROFILE);
    showMessage('সকল ডাটা সফলভাবে প্রাথমিক অবস্থায় রিসেট করা হয়েছে');
  };

  // Categories
  const getAllCategories = (): string[] => {
    const custom = customCategories.map((c) => c.name);
    return Array.from(new Set([...DEFAULT_CATEGORIES, ...custom]));
  };

  const addCustomCategory = (name: string) => {
    const trimmed = name.trim();
    if (!trimmed) return;
    const newCat: CustomCategory = {
      id: `cat-${Date.now()}`,
      userId: profile.userId,
      name: trimmed,
      isDemo: false,
    };
    setCustomCategories((prev) => [...prev, newCat]);
    showMessage(`নতুন ক্যাটাগরি '${trimmed}' তৈরি হয়েছে!`);
  };

  const deleteCustomCategory = (id: string) => {
    setCustomCategories((prev) => prev.filter((c) => c.id !== id));
    showMessage('ক্যাটাগরি মুছে ফেলা হয়েছে');
  };

  // Profile
  const updateProfile = (newProfile: ShopProfile) => {
    setProfile(newProfile);
    showMessage('দোকানের তথ্য সংরক্ষিত হয়েছে!');
  };

  // Products & Stock
  const addProduct = (prodData: Omit<Product, 'id' | 'userId'>): string => {
    const id = `prod-${Date.now()}`;
    const newProduct: Product = {
      ...prodData,
      id,
      userId: profile.userId,
      isDemo: false,
    };
    setProducts((prev) => [newProduct, ...prev]);
    showMessage('পণ্য সফলভাবে যুক্ত হয়েছে!');
    return id;
  };

  const updateProduct = (updatedProduct: Product) => {
    setProducts((prev) => prev.map((p) => (p.id === updatedProduct.id ? updatedProduct : p)));
    showMessage('পণ্য আপডেট হয়েছে!');
  };

  const deleteProduct = (productId: string) => {
    setProducts((prev) => prev.filter((p) => p.id !== productId));
    showMessage('পণ্য মুছে ফেলা হয়েছে');
  };

  const addStock = (
    productId: string,
    addedQty: number,
    purchasePrice: number,
    supplierName: string,
    notes: string
  ) => {
    const target = products.find((p) => p.id === productId);
    if (!target) {
      showError('পণ্য খুঁজে পাওয়া যায়নি!');
      return;
    }

    const prevQty = target.currentStock;
    const newTotal = prevQty + addedQty;

    // 1. Update product
    setProducts((prev) =>
      prev.map((p) =>
        p.id === productId
          ? {
              ...p,
              currentStock: newTotal,
              purchasePrice: purchasePrice > 0 ? purchasePrice : p.purchasePrice,
            }
          : p
      )
    );

    // 2. Add Stock Addition entry
    const stockAddition: StockAddition = {
      id: `stock-${Date.now()}`,
      userId: profile.userId,
      productId,
      productName: target.name,
      previousQuantity: prevQty,
      addedQuantity: addedQty,
      totalQuantity: newTotal,
      purchasePrice,
      dateAdded: getTodayStr(),
      supplierName,
      notes,
      isDemo: false,
    };
    setStockAdditions((prev) => [stockAddition, ...prev]);

    // 3. Record corresponding purchase
    const purchase: Purchase = {
      id: `pur-${Date.now()}`,
      userId: profile.userId,
      productId,
      productName: target.name,
      supplierName,
      quantity: addedQty,
      purchasePrice,
      totalAmount: addedQty * purchasePrice,
      purchaseDate: getTodayStr(),
      paymentStatus: 'PAID',
      notes,
      isDemo: false,
    };
    setPurchases((prev) => [purchase, ...prev]);

    showMessage(`স্টক সফলভাবে বৃদ্ধি করা হয়েছে (+${addedQty})`);
  };

  // Sales
  const recordSale = (saleData: Omit<Sale, 'id' | 'userId'>, items?: SaleItem[]): string => {
    const id = `sale-${Date.now()}`;
    const invoiceNumber = saleData.invoiceNumber || generateInvoiceNumber();

    // Deduct stock for all items
    if (items && items.length > 0) {
      setProducts((prev) =>
        prev.map((p) => {
          const matched = items.find((it) => it.productId === p.id);
          if (matched) {
            return {
              ...p,
              currentStock: Math.max(0, p.currentStock - matched.quantity),
            };
          }
          return p;
        })
      );
    }

    const newSale: Sale = {
      ...saleData,
      id,
      userId: profile.userId,
      invoiceNumber,
      items,
      isDemo: false,
    };
    setSales((prev) => [newSale, ...prev]);

    // If sale has due amount and customer name is specified, update or create customer
    if (newSale.dueAmount > 0 && newSale.customerName) {
      const existingCustomer = customers.find(
        (c) => c.name.trim().toLowerCase() === newSale.customerName.trim().toLowerCase()
      );
      if (existingCustomer) {
        recordCustomerTx(existingCustomer.id, {
          type: 'DUE',
          amount: newSale.dueAmount,
          notes: `বিক্রয় #${invoiceNumber} বাকি`,
          transactionDate: newSale.saleDate,
          isDemo: false,
        });
      } else {
        const custId = addCustomer({
          name: newSale.customerName,
          mobile: newSale.customerPhone || '',
          address: '',
          totalDue: newSale.dueAmount,
          totalPaid: 0,
          remainingDue: newSale.dueAmount,
          isDemo: false,
        });
        const tx: CustomerTransaction = {
          id: `tx-${Date.now()}`,
          userId: profile.userId,
          customerId: custId,
          type: 'DUE',
          amount: newSale.dueAmount,
          notes: `বিক্রয় #${invoiceNumber} বাকি`,
          transactionDate: newSale.saleDate,
          isDemo: false,
        };
        setCustomerTransactions((prev) => ({
          ...prev,
          [custId]: [tx],
        }));
      }
    }

    showMessage('বিক্রয় সফলভাবে রেকর্ড করা হয়েছে!');
    return id;
  };

  const deleteSale = (saleId: string) => {
    setSales((prev) => prev.filter((s) => s.id !== saleId));
    showMessage('বিক্রয় রেকর্ড মুছে ফেলা হয়েছে');
  };

  // Cash Memo
  const saveCashMemo = (memoData: Omit<CashMemo, 'id' | 'userId'>, autoRecordSale: boolean): string => {
    const id = `memo-${Date.now()}`;
    const memoNumber = memoData.memoNumber || generateMemoNumber();
    let saleId = '';

    if (autoRecordSale) {
      const saleItems: SaleItem[] = memoData.items.map((item) => {
        const p = products.find((prod) => prod.id === item.productId || prod.name === item.description);
        const purchaseCost = p ? p.purchasePrice : 0;
        return {
          productId: p?.id || '',
          productName: item.description,
          quantity: item.quantity,
          sellingPrice: item.rate,
          purchaseCost,
          subtotal: item.amount,
          profit: (item.rate - purchaseCost) * item.quantity,
        };
      });

      const totalCost = saleItems.reduce((acc, it) => acc + it.purchaseCost * it.quantity, 0);

      saleId = recordSale(
        {
          invoiceNumber: memoNumber,
          customerName: memoData.customerName,
          customerPhone: memoData.customerMobile,
          itemsSummary: memoData.items.map((it) => `${it.description} x${it.quantity}`).join(', '),
          totalAmount: memoData.grandTotal,
          totalCost,
          grossProfit: memoData.grandTotal - totalCost,
          paidAmount: memoData.paidAmount,
          dueAmount: memoData.dueAmount,
          saleDate: memoData.date,
          notes: memoData.notes,
          isDemo: false,
        },
        saleItems
      );
    }

    const newMemo: CashMemo = {
      ...memoData,
      id,
      userId: profile.userId,
      memoNumber,
      saleId,
      isDemo: false,
    };
    setCashMemos((prev) => [newMemo, ...prev]);
    showMessage(`ক্যাশ মেমো #${memoNumber} সফলভাবে সংরক্ষিত হয়েছে!`);
    return id;
  };

  const deleteCashMemo = (memoId: string) => {
    setCashMemos((prev) => prev.filter((m) => m.id !== memoId));
    showMessage('ক্যাশ মেমো মুছে ফেলা হয়েছে');
  };

  // Purchases
  const recordPurchase = (purchaseData: Omit<Purchase, 'id' | 'userId'>) => {
    const id = `pur-${Date.now()}`;
    const newPurchase: Purchase = {
      ...purchaseData,
      id,
      userId: profile.userId,
      isDemo: false,
    };
    setPurchases((prev) => [newPurchase, ...prev]);

    // If purchase specifies existing product, update stock
    if (purchaseData.productId) {
      setProducts((prev) =>
        prev.map((p) =>
          p.id === purchaseData.productId
            ? {
                ...p,
                currentStock: p.currentStock + purchaseData.quantity,
                purchasePrice: purchaseData.purchasePrice > 0 ? purchaseData.purchasePrice : p.purchasePrice,
              }
            : p
        )
      );
    }
    showMessage('ক্রয় রেকর্ড সম্পন্ন ও স্টক আপডেট হয়েছে!');
  };

  const deletePurchase = (purchaseId: string) => {
    setPurchases((prev) => prev.filter((p) => p.id !== purchaseId));
    showMessage('ক্রয় রেকর্ড মুছে ফেলা হয়েছে');
  };

  // Expenses
  const recordExpense = (expenseData: Omit<Expense, 'id' | 'userId'>) => {
    const id = `exp-${Date.now()}`;
    const newExpense: Expense = {
      ...expenseData,
      id,
      userId: profile.userId,
      isDemo: false,
    };
    setExpenses((prev) => [newExpense, ...prev]);
    showMessage('খরচ রেকর্ড করা হয়েছে!');
  };

  const deleteExpense = (expenseId: string) => {
    setExpenses((prev) => prev.filter((e) => e.id !== expenseId));
    showMessage('খরচ মুছে ফেলা হয়েছে');
  };

  // Customers & Dues
  const addCustomer = (custData: Omit<Customer, 'id' | 'userId'>): string => {
    const id = `cust-${Date.now()}`;
    const newCustomer: Customer = {
      ...custData,
      id,
      userId: profile.userId,
      isDemo: false,
    };
    setCustomers((prev) => [newCustomer, ...prev]);
    showMessage('কাস্টমার যুক্ত হয়েছে!');
    return id;
  };

  const recordCustomerTx = (
    customerId: string,
    txData: Omit<CustomerTransaction, 'id' | 'userId' | 'customerId'>
  ) => {
    const id = `tx-${Date.now()}`;
    const tx: CustomerTransaction = {
      ...txData,
      id,
      userId: profile.userId,
      customerId,
      isDemo: false,
    };

    setCustomerTransactions((prev) => ({
      ...prev,
      [customerId]: [tx, ...(prev[customerId] || [])],
    }));

    setCustomers((prev) =>
      prev.map((c) => {
        if (c.id === customerId) {
          if (txData.type === 'DUE') {
            const newTotalDue = c.totalDue + txData.amount;
            return {
              ...c,
              totalDue: newTotalDue,
              remainingDue: newTotalDue - c.totalPaid,
            };
          } else {
            const newTotalPaid = c.totalPaid + txData.amount;
            return {
              ...c,
              totalPaid: newTotalPaid,
              remainingDue: c.totalDue - newTotalPaid,
            };
          }
        }
        return c;
      })
    );

    showMessage(txData.type === 'DUE' ? 'বাকি যোগ হয়েছে!' : 'পেমেন্ট জমা হয়েছে!');
  };

  const deleteCustomer = (customerId: string) => {
    setCustomers((prev) => prev.filter((c) => c.id !== customerId));
    setCustomerTransactions((prev) => {
      const copy = { ...prev };
      delete copy[customerId];
      return copy;
    });
    showMessage('কাস্টমার মুছে ফেলা হয়েছে');
  };

  const deleteCustomerTx = (customerId: string, txId: string) => {
    const list = customerTransactions[customerId] || [];
    const target = list.find((t) => t.id === txId);
    if (!target) return;

    setCustomerTransactions((prev) => ({
      ...prev,
      [customerId]: (prev[customerId] || []).filter((t) => t.id !== txId),
    }));

    setCustomers((prev) =>
      prev.map((c) => {
        if (c.id === customerId) {
          if (target.type === 'DUE') {
            const newTotalDue = Math.max(0, c.totalDue - target.amount);
            return {
              ...c,
              totalDue: newTotalDue,
              remainingDue: Math.max(0, newTotalDue - c.totalPaid),
            };
          } else {
            const newTotalPaid = Math.max(0, c.totalPaid - target.amount);
            return {
              ...c,
              totalPaid: newTotalPaid,
              remainingDue: Math.max(0, c.totalDue - newTotalPaid),
            };
          }
        }
        return c;
      })
    );
    showMessage('লেনদেন রেকর্ড মুছে ফেলা হয়েছে');
  };

  const deleteStockAddition = (id: string) => {
    setStockAdditions((prev) => prev.filter((s) => s.id !== id));
    showMessage('স্টক হিস্ট্রি মুছে ফেলা হয়েছে');
  };

  // Loans
  const addLoan = (loanData: Omit<Loan, 'id' | 'userId'>) => {
    const id = `loan-${Date.now()}`;
    const newLoan: Loan = {
      ...loanData,
      id,
      userId: profile.userId,
      isDemo: false,
    };
    setLoans((prev) => [newLoan, ...prev]);
    showMessage('ঋণ/ধার রেকর্ড যুক্ত হয়েছে!');
  };

  const recordLoanPayment = (loanId: string, paymentAmount: number) => {
    setLoans((prev) =>
      prev.map((l) => {
        if (l.id === loanId) {
          const newPaid = l.paidAmount + paymentAmount;
          return {
            ...l,
            paidAmount: newPaid,
            remainingAmount: Math.max(0, l.amount - newPaid),
          };
        }
        return l;
      })
    );
    showMessage('ঋণ পরিশোধ আপডেট হয়েছে!');
  };

  const deleteLoan = (loanId: string) => {
    setLoans((prev) => prev.filter((l) => l.id !== loanId));
    showMessage('রেকর্ড মুছে ফেলা হয়েছে');
  };

  // Mobile Banking & Flexiload
  const recordMobileBanking = (txData: Omit<MobileBankingTx, 'id' | 'userId'>) => {
    const id = `mb-${Date.now()}`;
    const newTx: MobileBankingTx = {
      ...txData,
      id,
      userId: profile.userId,
      isDemo: false,
    };
    setMobileBanking((prev) => [newTx, ...prev]);
    showMessage(`${txData.provider} লেনদেন সফলভাবে সংরক্ষিত!`);
  };

  const deleteMobileBanking = (txId: string) => {
    setMobileBanking((prev) => prev.filter((m) => m.id !== txId));
    showMessage('লেনদেন মুছে ফেলা হয়েছে');
  };

  const recordFlexiload = (txData: Omit<FlexiloadTx, 'id' | 'userId'>) => {
    const id = `fl-${Date.now()}`;
    const newTx: FlexiloadTx = {
      ...txData,
      id,
      userId: profile.userId,
      isDemo: false,
    };
    setFlexiload((prev) => [newTx, ...prev]);
    showMessage('ফ্লেক্সিলোড রেকর্ড করা হয়েছে!');
  };

  const deleteFlexiload = (txId: string) => {
    setFlexiload((prev) => prev.filter((f) => f.id !== txId));
    showMessage('রেকর্ড মুছে ফেলা হয়েছে');
  };

  // Daily Closing
  const recordDailyClosing = (closingData: Omit<DailyClosing, 'id' | 'userId'>) => {
    const id = `closing-${Date.now()}`;
    const newClosing: DailyClosing = {
      ...closingData,
      id,
      userId: profile.userId,
      isDemo: false,
    };
    setDailyClosings((prev) => [newClosing, ...prev]);
    showMessage('আজকের ক্লোজিং রিপোর্ট সংরক্ষিত হয়েছে!');
  };

  // Backup & Restore
  const exportBackupJson = (): string => {
    const backupData = {
      version: '1.0',
      exportDate: new Date().toISOString(),
      profile,
      products,
      sales,
      purchases,
      expenses,
      customers,
      customerTransactions,
      loans,
      mobileBanking,
      flexiload,
      dailyClosings,
      customCategories,
      stockAdditions,
      cashMemos,
    };
    return JSON.stringify(backupData, null, 2);
  };

  const restoreBackupJson = (jsonString: string): number => {
    try {
      const data = JSON.parse(jsonString);
      if (data.profile) setProfile(data.profile);
      if (Array.isArray(data.products)) setProducts(data.products);
      if (Array.isArray(data.sales)) setSales(data.sales);
      if (Array.isArray(data.purchases)) setPurchases(data.purchases);
      if (Array.isArray(data.expenses)) setExpenses(data.expenses);
      if (Array.isArray(data.customers)) setCustomers(data.customers);
      if (data.customerTransactions) setCustomerTransactions(data.customerTransactions);
      if (Array.isArray(data.loans)) setLoans(data.loans);
      if (Array.isArray(data.mobileBanking)) setMobileBanking(data.mobileBanking);
      if (Array.isArray(data.flexiload)) setFlexiload(data.flexiload);
      if (Array.isArray(data.dailyClosings)) setDailyClosings(data.dailyClosings);
      if (Array.isArray(data.customCategories)) setCustomCategories(data.customCategories);
      if (Array.isArray(data.stockAdditions)) setStockAdditions(data.stockAdditions);
      if (Array.isArray(data.cashMemos)) setCashMemos(data.cashMemos);

      const count = Array.isArray(data.products) ? data.products.length : 0;
      showMessage(`${count} টি পণ্য ও সম্পূর্ণ ডাটা সফলভাবে রিস্টোর করা হয়েছে!`);
      return count;
    } catch (e: any) {
      showError(`রিস্টোর ব্যর্থ: ${e.message}`);
      return 0;
    }
  };

  return (
    <ShopContext.Provider
      value={{
        profile,
        products,
        sales,
        purchases,
        expenses,
        customers,
        customerTransactions,
        loans,
        mobileBanking,
        flexiload,
        dailyClosings,
        customCategories,
        stockAdditions,
        cashMemos,
        todayDateStr: getTodayStr(),

        isAdminAuthenticated,
        loginAdmin,
        logoutAdmin,
        changeAdminPin,
        resetDemoData,
        resetAllData,

        message,
        errorMessage,
        showMessage,
        showError,
        clearMessage,

        getAllCategories,
        addCustomCategory,
        deleteCustomCategory,

        updateProfile,

        addProduct,
        updateProduct,
        deleteProduct,
        addStock,

        recordSale,
        deleteSale,

        saveCashMemo,
        deleteCashMemo,

        recordPurchase,
        deletePurchase,

        recordExpense,
        deleteExpense,

        addCustomer,
        recordCustomerTx,
        deleteCustomer,
        deleteCustomerTx,
        deleteStockAddition,
        getDemoCounts,

        addLoan,
        recordLoanPayment,
        deleteLoan,

        recordMobileBanking,
        deleteMobileBanking,
        recordFlexiload,
        deleteFlexiload,

        recordDailyClosing,

        exportBackupJson,
        restoreBackupJson,
      }}
    >
      {children}
    </ShopContext.Provider>
  );
};

export const useShop = () => {
  const context = useContext(ShopContext);
  if (!context) {
    throw new Error('useShop must be used within a ShopProvider');
  }
  return context;
};
