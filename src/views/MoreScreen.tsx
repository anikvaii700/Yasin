import React, { useState } from 'react';
import {
  ShoppingBag,
  Receipt,
  Users,
  Smartphone,
  Zap,
  CalendarCheck,
  BarChart3,
  Settings,
  ShieldAlert,
  ChevronRight,
  ArrowLeft,
  Plus,
  Trash2,
  DollarSign,
  Download,
  Upload,
  RotateCcw,
  Check,
  X,
  CreditCard,
  Lock,
  Unlock,
  KeyRound,
  FileSpreadsheet,
} from 'lucide-react';
import { useShop } from '../context/ShopContext';
import {
  Purchase,
  Expense,
  Loan,
  MobileBankingTx,
  FlexiloadTx,
  DailyClosing,
} from '../types';
import { formatTaka, getTodayStr } from '../utils/formatters';

export type MoreSubScreenType =
  | 'MENU'
  | 'PURCHASES'
  | 'EXPENSES'
  | 'LOANS'
  | 'MOBILE_BANKING'
  | 'FLEXILOAD'
  | 'DAILY_CLOSING'
  | 'REPORTS'
  | 'SETTINGS'
  | 'ADMIN';

interface MoreScreenProps {
  initialSubScreen?: MoreSubScreenType;
  openAddPurchaseTrigger?: boolean;
  openAddExpenseTrigger?: boolean;
  onResetAddPurchaseTrigger?: () => void;
  onResetAddExpenseTrigger?: () => void;
}

export const MoreScreen: React.FC<MoreScreenProps> = ({
  initialSubScreen = 'MENU',
  openAddPurchaseTrigger = false,
  openAddExpenseTrigger = false,
  onResetAddPurchaseTrigger = () => {},
  onResetAddExpenseTrigger = () => {},
}) => {
  const [currentSubScreen, setCurrentSubScreen] = useState<MoreSubScreenType>(initialSubScreen);

  React.useEffect(() => {
    if (openAddPurchaseTrigger) {
      setCurrentSubScreen('PURCHASES');
      onResetAddPurchaseTrigger();
    }
  }, [openAddPurchaseTrigger, onResetAddPurchaseTrigger]);

  React.useEffect(() => {
    if (openAddExpenseTrigger) {
      setCurrentSubScreen('EXPENSES');
      onResetAddExpenseTrigger();
    }
  }, [openAddExpenseTrigger, onResetAddExpenseTrigger]);

  const {
    profile,
    products,
    sales,
    purchases,
    expenses,
    customers,
    loans,
    mobileBanking,
    flexiload,
    dailyClosings,
    customCategories,
    todayDateStr,
    recordPurchase,
    deletePurchase,
    recordExpense,
    deleteExpense,
    addLoan,
    recordLoanPayment,
    deleteLoan,
    recordMobileBanking,
    deleteMobileBanking,
    recordFlexiload,
    deleteFlexiload,
    recordDailyClosing,
    addCustomCategory,
    deleteCustomCategory,
    updateProfile,
    exportBackupJson,
    restoreBackupJson,
    isAdminAuthenticated,
    loginAdmin,
    logoutAdmin,
    changeAdminPin,
    resetDemoData,
    resetAllData,
  } = useShop();

  // Sub-screen Views

  // 1. MENU HUB
  if (currentSubScreen === 'MENU') {
    const menuItems = [
      {
        id: 'PURCHASES' as MoreSubScreenType,
        title: 'পণ্য ক্রয় হিসাব (Purchases)',
        desc: 'সাপ্লায়ার থেকে মালামাল ক্রয় ও ভাউচার',
        icon: <ShoppingBag className="w-5 h-5 text-indigo-600" />,
        color: 'bg-indigo-50',
      },
      {
        id: 'EXPENSES' as MoreSubScreenType,
        title: 'দোকানের খরচ (Expenses)',
        desc: 'চা-নাস্তা, বিদ্যুৎ, ভাড়া ও বিবিধ খরচ',
        icon: <Receipt className="w-5 h-5 text-rose-600" />,
        color: 'bg-rose-50',
      },
      {
        id: 'LOANS' as MoreSubScreenType,
        title: 'ব্যক্তিগত ধার-দেনা (Personal Loans)',
        desc: 'বন্ধুবান্ধব বা মহাজনদের লেনদেন খাতা',
        icon: <Users className="w-5 h-5 text-amber-600" />,
        color: 'bg-amber-50',
      },
      {
        id: 'MOBILE_BANKING' as MoreSubScreenType,
        title: 'বিকাশ ও নগদ (bKash & Nagad)',
        desc: 'ক্যাশ ইন, ক্যাশ আউট ও কমিশন হিসাব',
        icon: <Smartphone className="w-5 h-5 text-teal-600" />,
        color: 'bg-teal-50',
      },
      {
        id: 'FLEXILOAD' as MoreSubScreenType,
        title: 'ফ্লেক্সিলোড (Flexiload)',
        desc: 'গ্রামীণ, বাংলালিংক, রবি, এয়ারটেল রিচার্জ',
        icon: <Zap className="w-5 h-5 text-sky-600" />,
        color: 'bg-sky-50',
      },
      {
        id: 'DAILY_CLOSING' as MoreSubScreenType,
        title: 'দৈনিক হিসাব ক্লোজিং (Daily Closing)',
        desc: 'দিনের মোট লাভ, ক্যাশ ড্রয়ার মিল ও হিসাব খাতা',
        icon: <CalendarCheck className="w-5 h-5 text-emerald-600" />,
        color: 'bg-emerald-50',
      },
      {
        id: 'REPORTS' as MoreSubScreenType,
        title: 'রিপোর্ট ও বিশ্লেষণ (Reports)',
        desc: 'আয়-ব্যয়, বিক্রয় ট্রেন্ড ও লাভ-ক্ষতির পরিসংখ্যান',
        icon: <BarChart3 className="w-5 h-5 text-purple-600" />,
        color: 'bg-purple-50',
      },
      {
        id: 'SETTINGS' as MoreSubScreenType,
        title: 'সেটিংস ও ব্যাকআপ (Settings)',
        desc: 'দোকানের তথ্য, ক্যাটাগরি ও ডাটা ব্যাকআপ',
        icon: <Settings className="w-5 h-5 text-slate-700" />,
        color: 'bg-slate-100',
      },
      {
        id: 'ADMIN' as MoreSubScreenType,
        title: 'অ্যাডমিন প্যানেল (Admin Panel)',
        desc: 'পিন পরিবর্তন, সিস্টেম নিয়ন্ত্রণ ও ডেমো রিসেট',
        icon: <ShieldAlert className="w-5 h-5 text-red-600" />,
        color: 'bg-red-50',
        special: true,
      },
    ];

    return (
      <div className="space-y-4 pb-24">
        <div>
          <h2 className="text-lg font-bold text-slate-800">অন্যান্য ব্যবস্থাপনা (More Features)</h2>
          <p className="text-xs text-slate-500">দোকানের সকল অতিরিক্ত বাণিজ্যিক সেবা ও হিসাব খাতা</p>
        </div>

        <div className="grid grid-cols-1 gap-2.5">
          {menuItems.map((item) => (
            <button
              key={item.id}
              type="button"
              onClick={() => setCurrentSubScreen(item.id)}
              className="flex items-center justify-between p-3.5 bg-white border border-slate-200/90 rounded-2xl shadow-2xs hover:shadow-xs transition text-left group"
            >
              <div className="flex items-center gap-3">
                <div className={`w-10 h-10 rounded-xl ${item.color} flex items-center justify-center`}>
                  {item.icon}
                </div>
                <div>
                  <h3
                    className={`text-sm font-bold ${
                      item.special ? 'text-red-600' : 'text-slate-800 group-hover:text-emerald-700'
                    }`}
                  >
                    {item.title}
                  </h3>
                  <p className="text-xs text-slate-400 mt-0.5">{item.desc}</p>
                </div>
              </div>
              <ChevronRight className="w-4 h-4 text-slate-400 group-hover:translate-x-0.5 transition" />
            </button>
          ))}
        </div>
      </div>
    );
  }

  // Back header component
  const SubHeader = ({ title, onAdd }: { title: string; onAdd?: () => void }) => (
    <div className="flex items-center justify-between pb-2 border-b border-slate-200">
      <button
        type="button"
        onClick={() => setCurrentSubScreen('MENU')}
        className="flex items-center gap-1.5 text-xs font-bold text-slate-600 hover:text-slate-900 bg-white border border-slate-200 px-3 py-1.5 rounded-xl shadow-2xs"
      >
        <ArrowLeft className="w-4 h-4" />
        <span>মেনু</span>
      </button>

      <h2 className="text-sm font-bold text-slate-800 truncate px-2">{title}</h2>

      {onAdd ? (
        <button
          type="button"
          onClick={onAdd}
          className="flex items-center gap-1 text-xs font-bold bg-[#0A2540] hover:bg-slate-900 text-white px-3 py-1.5 rounded-xl shadow"
        >
          <Plus className="w-4 h-4" />
          <span>যোগ</span>
        </button>
      ) : (
        <div className="w-12"></div>
      )}
    </div>
  );

  // 2. PURCHASES SUB-SCREEN
  if (currentSubScreen === 'PURCHASES') {
    return <PurchasesSubScreen onBack={() => setCurrentSubScreen('MENU')} />;
  }

  // 3. EXPENSES SUB-SCREEN
  if (currentSubScreen === 'EXPENSES') {
    return <ExpensesSubScreen onBack={() => setCurrentSubScreen('MENU')} />;
  }

  // 4. PERSONAL LOANS SUB-SCREEN
  if (currentSubScreen === 'LOANS') {
    return <LoansSubScreen onBack={() => setCurrentSubScreen('MENU')} />;
  }

  // 5. MOBILE BANKING SUB-SCREEN
  if (currentSubScreen === 'MOBILE_BANKING') {
    return <MobileBankingSubScreen onBack={() => setCurrentSubScreen('MENU')} />;
  }

  // 6. FLEXILOAD SUB-SCREEN
  if (currentSubScreen === 'FLEXILOAD') {
    return <FlexiloadSubScreen onBack={() => setCurrentSubScreen('MENU')} />;
  }

  // 7. DAILY CLOSING SUB-SCREEN
  if (currentSubScreen === 'DAILY_CLOSING') {
    return <DailyClosingSubScreen onBack={() => setCurrentSubScreen('MENU')} />;
  }

  // 8. REPORTS SUB-SCREEN
  if (currentSubScreen === 'REPORTS') {
    return <ReportsSubScreen onBack={() => setCurrentSubScreen('MENU')} />;
  }

  // 9. SETTINGS & BACKUP SUB-SCREEN
  if (currentSubScreen === 'SETTINGS') {
    return <SettingsSubScreen onBack={() => setCurrentSubScreen('MENU')} />;
  }

  // 10. ADMIN PANEL SUB-SCREEN
  if (currentSubScreen === 'ADMIN') {
    return <AdminPanelSubScreen onBack={() => setCurrentSubScreen('MENU')} />;
  }

  return null;
};

// -------------------------------------------------------------
// SUB-SCREENS IMPLEMENTATION
// -------------------------------------------------------------

// PURCHASES
const PurchasesSubScreen: React.FC<{ onBack: () => void }> = ({ onBack }) => {
  const { purchases, products, todayDateStr, recordPurchase, deletePurchase } = useShop();
  const [showAdd, setShowAdd] = useState(false);

  const [productName, setProductName] = useState('');
  const [supplierName, setSupplierName] = useState('');
  const [quantity, setQuantity] = useState('10');
  const [purchasePrice, setPurchasePrice] = useState('300');
  const [notes, setNotes] = useState('');

  const handleSave = (e: React.FormEvent) => {
    e.preventDefault();
    if (!productName.trim()) return;

    const qty = parseInt(quantity, 10) || 1;
    const rate = parseFloat(purchasePrice) || 0;

    recordPurchase({
      productName: productName.trim(),
      productId: '',
      supplierName: supplierName.trim(),
      quantity: qty,
      purchasePrice: rate,
      totalAmount: qty * rate,
      purchaseDate: todayDateStr,
      paymentStatus: 'PAID',
      notes: notes.trim(),
      isDemo: false,
    });

    setShowAdd(false);
    setProductName('');
    setSupplierName('');
  };

  const totalPurchases = purchases.reduce((acc, p) => acc + p.totalAmount, 0);

  return (
    <div className="space-y-4 pb-24">
      <div className="flex items-center justify-between pb-2 border-b border-slate-200">
        <button
          type="button"
          onClick={onBack}
          className="flex items-center gap-1.5 text-xs font-bold text-slate-600 bg-white border border-slate-200 px-3 py-1.5 rounded-xl shadow-2xs"
        >
          <ArrowLeft className="w-4 h-4" /> <span>মেনু</span>
        </button>
        <h2 className="text-sm font-bold text-slate-800">পণ্য ক্রয় হিসাব (Purchases)</h2>
        <button
          type="button"
          onClick={() => setShowAdd(true)}
          className="flex items-center gap-1 text-xs font-bold bg-indigo-600 text-white px-3 py-1.5 rounded-xl shadow"
        >
          <Plus className="w-4 h-4" /> <span>ক্রয় যোগ</span>
        </button>
      </div>

      <div className="p-3 bg-indigo-50 border border-indigo-200 rounded-xl flex items-center justify-between text-xs">
        <span className="font-semibold text-indigo-900">মোট ক্রয় ভাউচার:</span>
        <span className="text-base font-black text-indigo-700">{formatTaka(totalPurchases)}</span>
      </div>

      <div className="space-y-2.5">
        {purchases.length === 0 ? (
          <p className="text-xs text-slate-400 p-8 text-center bg-white rounded-xl border border-dashed">
            কোনো ক্রয় রেকর্ড নেই
          </p>
        ) : (
          purchases.map((pur) => (
            <div key={pur.id} className="p-3 bg-white border border-slate-200 rounded-xl flex items-center justify-between text-xs shadow-2xs">
              <div>
                <span className="font-bold text-slate-900">{pur.productName}</span>
                <p className="text-slate-500 text-[11px] mt-0.5">
                  {pur.supplierName ? `সাপ্লায়ার: ${pur.supplierName} • ` : ''}পরিমাণ: {pur.quantity} পিস @ {formatTaka(pur.purchasePrice)}
                </p>
                <span className="text-[10px] text-slate-400">{pur.purchaseDate}</span>
              </div>
              <div className="flex items-center gap-3">
                <span className="font-black text-sm text-slate-900">{formatTaka(pur.totalAmount)}</span>
                <button
                  type="button"
                  onClick={() => deletePurchase(pur.id)}
                  className="text-slate-400 hover:text-red-600 p-1"
                >
                  <Trash2 className="w-4 h-4" />
                </button>
              </div>
            </div>
          ))
        )}
      </div>

      {showAdd && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-xs">
          <div className="bg-white rounded-2xl max-w-sm w-full p-5 shadow-2xl">
            <h3 className="font-bold text-slate-900 mb-3 text-sm">নতুন পণ্য ক্রয় এন্ট্রি</h3>
            <form onSubmit={handleSave} className="space-y-3 text-xs">
              <div>
                <label className="block font-semibold mb-1">পণ্যের নাম *</label>
                <input
                  type="text"
                  required
                  value={productName}
                  onChange={(e) => setProductName(e.target.value)}
                  placeholder="যেমন: টাইপ-সি ক্যাবল বক্স"
                  className="w-full px-3 py-2 border rounded-xl"
                />
              </div>
              <div>
                <label className="block font-semibold mb-1">সাপ্লায়ার / মহাজন</label>
                <input
                  type="text"
                  value={supplierName}
                  onChange={(e) => setSupplierName(e.target.value)}
                  placeholder="কোম্পানি বা ডিলারের নাম"
                  className="w-full px-3 py-2 border rounded-xl"
                />
              </div>
              <div className="grid grid-cols-2 gap-2">
                <div>
                  <label className="block font-semibold mb-1">পরিমাণ (পিস)</label>
                  <input
                    type="number"
                    min="1"
                    value={quantity}
                    onChange={(e) => setQuantity(e.target.value)}
                    className="w-full px-3 py-2 border rounded-xl"
                  />
                </div>
                <div>
                  <label className="block font-semibold mb-1">ক্রয় দর (৳)</label>
                  <input
                    type="number"
                    step="any"
                    value={purchasePrice}
                    onChange={(e) => setPurchasePrice(e.target.value)}
                    className="w-full px-3 py-2 border rounded-xl"
                  />
                </div>
              </div>
              <div>
                <label className="block font-semibold mb-1">নোট</label>
                <input
                  type="text"
                  value={notes}
                  onChange={(e) => setNotes(e.target.value)}
                  className="w-full px-3 py-2 border rounded-xl"
                />
              </div>
              <div className="flex justify-end gap-2 pt-2">
                <button type="button" onClick={() => setShowAdd(false)} className="px-3 py-1.5 border rounded-xl">
                  বাতিল
                </button>
                <button type="submit" className="px-4 py-1.5 bg-indigo-600 text-white font-bold rounded-xl shadow">
                  সংরক্ষণ
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

// EXPENSES
const ExpensesSubScreen: React.FC<{ onBack: () => void }> = ({ onBack }) => {
  const { expenses, todayDateStr, recordExpense, deleteExpense } = useShop();
  const [showAdd, setShowAdd] = useState(false);

  const [title, setTitle] = useState('');
  const [amount, setAmount] = useState('100');
  const [category, setCategory] = useState('Entertainment');
  const [desc, setDesc] = useState('');

  const expenseCategories = [
    'Entertainment',
    'Shop Rent & Utilities',
    'Transport',
    'Electric Bill',
    'Staff Salary',
    'Other Expenses',
  ];

  const handleSave = (e: React.FormEvent) => {
    e.preventDefault();
    if (!title.trim()) return;

    recordExpense({
      title: title.trim(),
      amount: parseFloat(amount) || 0,
      category,
      expenseDate: todayDateStr,
      description: desc.trim(),
      isDemo: false,
    });

    setShowAdd(false);
    setTitle('');
    setAmount('');
  };

  const totalExpenseAmount = expenses.reduce((acc, e) => acc + e.amount, 0);

  return (
    <div className="space-y-4 pb-24">
      <div className="flex items-center justify-between pb-2 border-b border-slate-200">
        <button
          type="button"
          onClick={onBack}
          className="flex items-center gap-1.5 text-xs font-bold text-slate-600 bg-white border border-slate-200 px-3 py-1.5 rounded-xl shadow-2xs"
        >
          <ArrowLeft className="w-4 h-4" /> <span>মেনু</span>
        </button>
        <h2 className="text-sm font-bold text-slate-800">দোকানের খরচ (Expenses)</h2>
        <button
          type="button"
          onClick={() => setShowAdd(true)}
          className="flex items-center gap-1 text-xs font-bold bg-rose-600 text-white px-3 py-1.5 rounded-xl shadow"
        >
          <Plus className="w-4 h-4" /> <span>খরচ যোগ</span>
        </button>
      </div>

      <div className="p-3 bg-rose-50 border border-rose-200 rounded-xl flex items-center justify-between text-xs">
        <span className="font-semibold text-rose-900">মোট খরচ এন্ট্রি:</span>
        <span className="text-base font-black text-rose-700">{formatTaka(totalExpenseAmount)}</span>
      </div>

      <div className="space-y-2.5">
        {expenses.length === 0 ? (
          <p className="text-xs text-slate-400 p-8 text-center bg-white rounded-xl border border-dashed">
            কোনো খরচ রেকর্ড নেই
          </p>
        ) : (
          expenses.map((exp) => (
            <div key={exp.id} className="p-3 bg-white border border-slate-200 rounded-xl flex items-center justify-between text-xs shadow-2xs">
              <div>
                <span className="font-bold text-slate-900">{exp.title}</span>
                <p className="text-slate-500 text-[11px] mt-0.5">
                  ক্যাটাগরি: {exp.category} {exp.description ? `• ${exp.description}` : ''}
                </p>
                <span className="text-[10px] text-slate-400">{exp.expenseDate}</span>
              </div>
              <div className="flex items-center gap-3">
                <span className="font-black text-sm text-rose-600">{formatTaka(exp.amount)}</span>
                <button
                  type="button"
                  onClick={() => deleteExpense(exp.id)}
                  className="text-slate-400 hover:text-red-600 p-1"
                >
                  <Trash2 className="w-4 h-4" />
                </button>
              </div>
            </div>
          ))
        )}
      </div>

      {showAdd && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-xs">
          <div className="bg-white rounded-2xl max-w-sm w-full p-5 shadow-2xl">
            <h3 className="font-bold text-slate-900 mb-3 text-sm">নতুন খরচের এন্ট্রি</h3>
            <form onSubmit={handleSave} className="space-y-3 text-xs">
              <div>
                <label className="block font-semibold mb-1">খরচের নাম / বিবরণ *</label>
                <input
                  type="text"
                  required
                  value={title}
                  onChange={(e) => setTitle(e.target.value)}
                  placeholder="যেমন: দোকানের চা-বিস্কুট"
                  className="w-full px-3 py-2 border rounded-xl"
                />
              </div>
              <div>
                <label className="block font-semibold mb-1">ক্যাটাগরি</label>
                <select
                  value={category}
                  onChange={(e) => setCategory(e.target.value)}
                  className="w-full px-3 py-2 border rounded-xl bg-white"
                >
                  {expenseCategories.map((c) => (
                    <option key={c} value={c}>
                      {c}
                    </option>
                  ))}
                </select>
              </div>
              <div>
                <label className="block font-semibold mb-1">টাকার পরিমাণ (৳) *</label>
                <input
                  type="number"
                  step="any"
                  required
                  value={amount}
                  onChange={(e) => setAmount(e.target.value)}
                  placeholder="100"
                  className="w-full px-3 py-2 border rounded-xl font-bold text-rose-600"
                />
              </div>
              <div>
                <label className="block font-semibold mb-1">মন্তব্য</label>
                <input
                  type="text"
                  value={desc}
                  onChange={(e) => setDesc(e.target.value)}
                  placeholder="অতিরিক্ত বিবরণ..."
                  className="w-full px-3 py-2 border rounded-xl"
                />
              </div>
              <div className="flex justify-end gap-2 pt-2">
                <button type="button" onClick={() => setShowAdd(false)} className="px-3 py-1.5 border rounded-xl">
                  বাতিল
                </button>
                <button type="submit" className="px-4 py-1.5 bg-rose-600 text-white font-bold rounded-xl shadow">
                  সংরক্ষণ
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

// PERSONAL LOANS
const LoansSubScreen: React.FC<{ onBack: () => void }> = ({ onBack }) => {
  const { loans, addLoan, recordLoanPayment, deleteLoan } = useShop();
  const [showAdd, setShowAdd] = useState(false);
  const [payingLoan, setPayingLoan] = useState<Loan | null>(null);
  const [payAmount, setPayAmount] = useState('1000');

  const [personName, setPersonName] = useState('');
  const [mobile, setMobile] = useState('');
  const [loanType, setLoanType] = useState<'LENT' | 'BORROWED'>('LENT');
  const [amount, setAmount] = useState('5000');
  const [desc, setDesc] = useState('');
  const [expectedDate, setExpectedDate] = useState('30-11-2026');

  const handleSave = (e: React.FormEvent) => {
    e.preventDefault();
    if (!personName.trim()) return;

    const amt = parseFloat(amount) || 0;
    addLoan({
      personName: personName.trim(),
      mobile: mobile.trim(),
      loanType,
      amount: amt,
      paidAmount: 0,
      remainingAmount: amt,
      description: desc.trim(),
      expectedPaymentDate: expectedDate,
      isDemo: false,
    });

    setShowAdd(false);
    setPersonName('');
    setMobile('');
  };

  const handlePay = (e: React.FormEvent) => {
    e.preventDefault();
    if (!payingLoan) return;
    const p = parseFloat(payAmount) || 0;
    if (p > 0) {
      recordLoanPayment(payingLoan.id, p);
      setPayingLoan(null);
    }
  };

  const totalLent = loans.filter((l) => l.loanType === 'LENT').reduce((acc, l) => acc + l.remainingAmount, 0);
  const totalBorrowed = loans.filter((l) => l.loanType !== 'LENT').reduce((acc, l) => acc + l.remainingAmount, 0);

  return (
    <div className="space-y-4 pb-24">
      <div className="flex items-center justify-between pb-2 border-b border-slate-200">
        <button
          type="button"
          onClick={onBack}
          className="flex items-center gap-1.5 text-xs font-bold text-slate-600 bg-white border border-slate-200 px-3 py-1.5 rounded-xl shadow-2xs"
        >
          <ArrowLeft className="w-4 h-4" /> <span>মেনু</span>
        </button>
        <h2 className="text-sm font-bold text-slate-800">ব্যক্তিগত ধার-দেনা (Loans)</h2>
        <button
          type="button"
          onClick={() => setShowAdd(true)}
          className="flex items-center gap-1 text-xs font-bold bg-amber-600 text-white px-3 py-1.5 rounded-xl shadow"
        >
          <Plus className="w-4 h-4" /> <span>নতুন এন্ট্রি</span>
        </button>
      </div>

      <div className="grid grid-cols-2 gap-2.5">
        <div className="p-3 bg-rose-50 border border-rose-200 rounded-xl">
          <span className="text-[10px] text-rose-600 font-bold block">পাওনা বকেয়া (দোকানের পাওনা)</span>
          <span className="text-base font-black text-rose-700">{formatTaka(totalLent)}</span>
        </div>
        <div className="p-3 bg-amber-50 border border-amber-200 rounded-xl">
          <span className="text-[10px] text-amber-700 font-bold block">দেনা বকেয়া (দোকানের দেনা)</span>
          <span className="text-base font-black text-amber-700">{formatTaka(totalBorrowed)}</span>
        </div>
      </div>

      <div className="space-y-2.5">
        {loans.length === 0 ? (
          <p className="text-xs text-slate-400 p-8 text-center bg-white rounded-xl border border-dashed">
            কোনো ধার-দেনা রেকর্ড নেই
          </p>
        ) : (
          loans.map((l) => (
            <div key={l.id} className="p-3.5 bg-white border border-slate-200 rounded-xl text-xs shadow-2xs space-y-2">
              <div className="flex items-start justify-between">
                <div>
                  <div className="flex items-center gap-1.5">
                    <span
                      className={`text-[10px] font-bold px-2 py-0.5 rounded-full ${
                        l.loanType === 'LENT' ? 'bg-rose-100 text-rose-700' : 'bg-amber-100 text-amber-800'
                      }`}
                    >
                      {l.loanType === 'LENT' ? 'পাওনা (Lent)' : 'দেনা (Borrowed)'}
                    </span>
                    {l.expectedPaymentDate && (
                      <span className="text-slate-400 text-[10px]">ফেরত: {l.expectedPaymentDate}</span>
                    )}
                  </div>
                  <h4 className="font-bold text-slate-900 text-sm mt-1">{l.personName}</h4>
                  {l.mobile && <p className="text-slate-500 text-[11px]">📞 {l.mobile}</p>}
                </div>

                <div className="text-right">
                  <span className="text-[10px] text-slate-400 block">অবশিষ্ট বকেয়া</span>
                  <span className="text-base font-black text-slate-900">{formatTaka(l.remainingAmount)}</span>
                </div>
              </div>

              <div className="flex items-center justify-between text-[11px] text-slate-500 border-t border-slate-100 pt-2">
                <span>মোট: {formatTaka(l.amount)} • পরিশোধ: {formatTaka(l.paidAmount)}</span>
                <div className="flex items-center gap-1">
                  <button
                    type="button"
                    onClick={() => setPayingLoan(l)}
                    className="px-2 py-1 bg-emerald-50 text-emerald-700 font-bold rounded-lg hover:bg-emerald-100"
                  >
                    পরিশোধ
                  </button>
                  <button
                    type="button"
                    onClick={() => deleteLoan(l.id)}
                    className="p-1 text-slate-400 hover:text-red-600"
                  >
                    <Trash2 className="w-3.5 h-3.5" />
                  </button>
                </div>
              </div>
            </div>
          ))
        )}
      </div>

      {showAdd && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-xs">
          <div className="bg-white rounded-2xl max-w-sm w-full p-5 shadow-2xl">
            <h3 className="font-bold text-slate-900 mb-3 text-sm">নতুন ধার/দেনা এন্ট্রি</h3>
            <form onSubmit={handleSave} className="space-y-3 text-xs">
              <div>
                <label className="block font-semibold mb-1">ব্যক্তির নাম *</label>
                <input
                  type="text"
                  required
                  value={personName}
                  onChange={(e) => setPersonName(e.target.value)}
                  placeholder="নাম..."
                  className="w-full px-3 py-2 border rounded-xl"
                />
              </div>
              <div>
                <label className="block font-semibold mb-1">মোবাইল নম্বর</label>
                <input
                  type="text"
                  value={mobile}
                  onChange={(e) => setMobile(e.target.value)}
                  placeholder="017xxxxxxxx"
                  className="w-full px-3 py-2 border rounded-xl"
                />
              </div>
              <div className="grid grid-cols-2 gap-2">
                <div>
                  <label className="block font-semibold mb-1">ধরনের নির্বাচন</label>
                  <select
                    value={loanType}
                    onChange={(e) => setLoanType(e.target.value as any)}
                    className="w-full px-3 py-2 border rounded-xl bg-white font-bold"
                  >
                    <option value="LENT">পাওনা (Lent)</option>
                    <option value="BORROWED">দেনা (Borrowed)</option>
                  </select>
                </div>
                <div>
                  <label className="block font-semibold mb-1">টাকার পরিমাণ (৳)</label>
                  <input
                    type="number"
                    step="any"
                    value={amount}
                    onChange={(e) => setAmount(e.target.value)}
                    className="w-full px-3 py-2 border rounded-xl font-bold text-amber-700"
                  />
                </div>
              </div>
              <div>
                <label className="block font-semibold mb-1">প্রত্যাশিত পরিশোধের তারিখ</label>
                <input
                  type="text"
                  value={expectedDate}
                  onChange={(e) => setExpectedDate(e.target.value)}
                  placeholder="DD-MM-YYYY"
                  className="w-full px-3 py-2 border rounded-xl"
                />
              </div>
              <div className="flex justify-end gap-2 pt-2">
                <button type="button" onClick={() => setShowAdd(false)} className="px-3 py-1.5 border rounded-xl">
                  বাতিল
                </button>
                <button type="submit" className="px-4 py-1.5 bg-amber-600 text-white font-bold rounded-xl shadow">
                  সংরক্ষণ
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {payingLoan && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-xs">
          <div className="bg-white rounded-2xl max-w-xs w-full p-5 shadow-2xl">
            <h3 className="font-bold text-slate-900 mb-2 text-sm">{payingLoan.personName} - পরিশোধ</h3>
            <p className="text-xs text-slate-500 mb-3">অবশিষ্ট: {formatTaka(payingLoan.remainingAmount)}</p>
            <form onSubmit={handlePay} className="space-y-3 text-xs">
              <div>
                <label className="block font-semibold mb-1">পরিশোধের পরিমাণ (৳)</label>
                <input
                  type="number"
                  min="1"
                  step="any"
                  value={payAmount}
                  onChange={(e) => setPayAmount(e.target.value)}
                  className="w-full px-3 py-2 border rounded-xl font-bold text-emerald-600"
                />
              </div>
              <div className="flex justify-end gap-2 pt-2">
                <button type="button" onClick={() => setPayingLoan(null)} className="px-3 py-1.5 border rounded-xl">
                  বাতিল
                </button>
                <button type="submit" className="px-4 py-1.5 bg-emerald-600 text-white font-bold rounded-xl shadow">
                  জমা দিন
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

// MOBILE BANKING (bKash & Nagad)
const MobileBankingSubScreen: React.FC<{ onBack: () => void }> = ({ onBack }) => {
  const { mobileBanking, todayDateStr, recordMobileBanking, deleteMobileBanking } = useShop();
  const [showAdd, setShowAdd] = useState(false);

  const [provider, setProvider] = useState<'BKASH' | 'NAGAD' | 'ROCKET' | 'UPAY'>('BKASH');
  const [type, setType] = useState<'CASH_IN' | 'CASH_OUT' | 'SEND_MONEY'>('CASH_IN');
  const [amount, setAmount] = useState('2000');
  const [commission, setCommission] = useState('10');
  const [phone, setPhone] = useState('');
  const [notes, setNotes] = useState('');

  const handleSave = (e: React.FormEvent) => {
    e.preventDefault();
    const amt = parseFloat(amount) || 0;
    const comm = parseFloat(commission) || 0;

    recordMobileBanking({
      provider,
      type,
      amount: amt,
      commissionProfit: comm,
      serviceCharge: 0,
      customerPhone: phone.trim(),
      notes: notes.trim(),
      transactionDate: todayDateStr,
      isDemo: false,
    });

    setShowAdd(false);
  };

  const totalVolume = mobileBanking.reduce((acc, m) => acc + m.amount, 0);
  const totalCommission = mobileBanking.reduce((acc, m) => acc + m.commissionProfit, 0);

  return (
    <div className="space-y-4 pb-24">
      <div className="flex items-center justify-between pb-2 border-b border-slate-200">
        <button
          type="button"
          onClick={onBack}
          className="flex items-center gap-1.5 text-xs font-bold text-slate-600 bg-white border border-slate-200 px-3 py-1.5 rounded-xl shadow-2xs"
        >
          <ArrowLeft className="w-4 h-4" /> <span>মেনু</span>
        </button>
        <h2 className="text-sm font-bold text-slate-800">বিকাশ ও নগদ হিসাব</h2>
        <button
          type="button"
          onClick={() => setShowAdd(true)}
          className="flex items-center gap-1 text-xs font-bold bg-teal-600 text-white px-3 py-1.5 rounded-xl shadow"
        >
          <Plus className="w-4 h-4" /> <span>লেনদেন যোগ</span>
        </button>
      </div>

      <div className="grid grid-cols-2 gap-2.5">
        <div className="p-3 bg-teal-50 border border-teal-200 rounded-xl">
          <span className="text-[10px] text-teal-700 font-bold block">মোট লেনদেন ভলিউম</span>
          <span className="text-base font-black text-teal-800">{formatTaka(totalVolume)}</span>
        </div>
        <div className="p-3 bg-emerald-50 border border-emerald-200 rounded-xl">
          <span className="text-[10px] text-emerald-700 font-bold block">মোট কমিশন লাভ</span>
          <span className="text-base font-black text-emerald-700">+{formatTaka(totalCommission)}</span>
        </div>
      </div>

      <div className="space-y-2.5">
        {mobileBanking.map((m) => (
          <div key={m.id} className="p-3 bg-white border border-slate-200 rounded-xl flex items-center justify-between text-xs shadow-2xs">
            <div>
              <div className="flex items-center gap-1.5">
                <span className="font-bold text-teal-700 bg-teal-50 px-2 py-0.5 rounded border border-teal-200">
                  {m.provider}
                </span>
                <span className="text-[10px] font-semibold text-slate-500">
                  {m.type === 'CASH_IN' ? 'ক্যাশ ইন' : m.type === 'CASH_OUT' ? 'ক্যাশ আউট' : 'সেন্ড মানি'}
                </span>
              </div>
              <p className="text-slate-600 font-medium mt-1">
                {m.customerPhone ? `ফোন: ${m.customerPhone} • ` : ''}{m.notes || 'লেনদেন'}
              </p>
            </div>
            <div className="text-right">
              <span className="font-black text-sm text-slate-900 block">{formatTaka(m.amount)}</span>
              <span className="text-[10px] font-bold text-emerald-600">লাভ: +{formatTaka(m.commissionProfit)}</span>
            </div>
          </div>
        ))}
      </div>

      {showAdd && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-xs">
          <div className="bg-white rounded-2xl max-w-sm w-full p-5 shadow-2xl">
            <h3 className="font-bold text-slate-900 mb-3 text-sm">মোবাইল ব্যাংকিং লেনদেন</h3>
            <form onSubmit={handleSave} className="space-y-3 text-xs">
              <div className="grid grid-cols-2 gap-2">
                <div>
                  <label className="block font-semibold mb-1">সার্ভিস</label>
                  <select
                    value={provider}
                    onChange={(e) => setProvider(e.target.value as any)}
                    className="w-full px-3 py-2 border rounded-xl bg-white font-bold"
                  >
                    <option value="BKASH">bKash (বিকাশ)</option>
                    <option value="NAGAD">Nagad (নগদ)</option>
                    <option value="ROCKET">Rocket (রকেট)</option>
                    <option value="UPAY">Upay (উপায়)</option>
                  </select>
                </div>
                <div>
                  <label className="block font-semibold mb-1">ধরন</label>
                  <select
                    value={type}
                    onChange={(e) => setType(e.target.value as any)}
                    className="w-full px-3 py-2 border rounded-xl bg-white font-bold"
                  >
                    <option value="CASH_IN">Cash In</option>
                    <option value="CASH_OUT">Cash Out</option>
                    <option value="SEND_MONEY">Send Money</option>
                  </select>
                </div>
              </div>
              <div>
                <label className="block font-semibold mb-1">টাকার পরিমাণ (৳) *</label>
                <input
                  type="number"
                  step="any"
                  required
                  value={amount}
                  onChange={(e) => setAmount(e.target.value)}
                  className="w-full px-3 py-2 border rounded-xl font-bold text-teal-700"
                />
              </div>
              <div>
                <label className="block font-semibold mb-1">কমিশন / লাভ (৳)</label>
                <input
                  type="number"
                  step="any"
                  value={commission}
                  onChange={(e) => setCommission(e.target.value)}
                  className="w-full px-3 py-2 border rounded-xl font-bold text-emerald-600"
                />
              </div>
              <div>
                <label className="block font-semibold mb-1">গ্রাহকের ফোন নম্বর</label>
                <input
                  type="text"
                  value={phone}
                  onChange={(e) => setPhone(e.target.value)}
                  placeholder="017xxxxxxxx"
                  className="w-full px-3 py-2 border rounded-xl"
                />
              </div>
              <div className="flex justify-end gap-2 pt-2">
                <button type="button" onClick={() => setShowAdd(false)} className="px-3 py-1.5 border rounded-xl">
                  বাতিল
                </button>
                <button type="submit" className="px-4 py-1.5 bg-teal-600 text-white font-bold rounded-xl shadow">
                  সংরক্ষণ
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

// FLEXILOAD
const FlexiloadSubScreen: React.FC<{ onBack: () => void }> = ({ onBack }) => {
  const { flexiload, todayDateStr, recordFlexiload, deleteFlexiload } = useShop();
  const [showAdd, setShowAdd] = useState(false);

  const [mobileNumber, setMobileNumber] = useState('');
  const [operator, setOperator] = useState<'Grameenphone' | 'Banglalink' | 'Robi' | 'Airtel' | 'Teletalk'>('Grameenphone');
  const [rechargeAmount, setRechargeAmount] = useState('199');
  const [profit, setProfit] = useState('6');
  const [notes, setNotes] = useState('');

  const handleSave = (e: React.FormEvent) => {
    e.preventDefault();
    const amt = parseFloat(rechargeAmount) || 0;
    const prof = parseFloat(profit) || 0;

    recordFlexiload({
      mobileNumber: mobileNumber.trim(),
      operator,
      rechargeAmount: amt,
      customerPayment: amt,
      rechargeCost: amt - prof,
      profit: prof,
      transactionDate: todayDateStr,
      notes: notes.trim(),
      isDemo: false,
    });

    setShowAdd(false);
    setMobileNumber('');
  };

  const totalRecharge = flexiload.reduce((acc, f) => acc + f.rechargeAmount, 0);
  const totalProfit = flexiload.reduce((acc, f) => acc + f.profit, 0);

  return (
    <div className="space-y-4 pb-24">
      <div className="flex items-center justify-between pb-2 border-b border-slate-200">
        <button
          type="button"
          onClick={onBack}
          className="flex items-center gap-1.5 text-xs font-bold text-slate-600 bg-white border border-slate-200 px-3 py-1.5 rounded-xl shadow-2xs"
        >
          <ArrowLeft className="w-4 h-4" /> <span>মেনু</span>
        </button>
        <h2 className="text-sm font-bold text-slate-800">ফ্লেক্সিলোড হিসাব (Flexiload)</h2>
        <button
          type="button"
          onClick={() => setShowAdd(true)}
          className="flex items-center gap-1 text-xs font-bold bg-sky-600 text-white px-3 py-1.5 rounded-xl shadow"
        >
          <Plus className="w-4 h-4" /> <span>রিচার্জ যোগ</span>
        </button>
      </div>

      <div className="grid grid-cols-2 gap-2.5">
        <div className="p-3 bg-sky-50 border border-sky-200 rounded-xl">
          <span className="text-[10px] text-sky-700 font-bold block">মোট রিচার্জ ভলিউম</span>
          <span className="text-base font-black text-sky-800">{formatTaka(totalRecharge)}</span>
        </div>
        <div className="p-3 bg-emerald-50 border border-emerald-200 rounded-xl">
          <span className="text-[10px] text-emerald-700 font-bold block">মোট রিচার্জ লাভ</span>
          <span className="text-base font-black text-emerald-700">+{formatTaka(totalProfit)}</span>
        </div>
      </div>

      <div className="space-y-2.5">
        {flexiload.map((f) => (
          <div key={f.id} className="p-3 bg-white border border-slate-200 rounded-xl flex items-center justify-between text-xs shadow-2xs">
            <div>
              <span className="font-bold text-slate-900">{f.mobileNumber || 'অজানা নম্বর'}</span>
              <p className="text-slate-500 text-[11px] mt-0.5">
                অপারেটর: <span className="font-semibold text-sky-600">{f.operator}</span> {f.notes ? `• ${f.notes}` : ''}
              </p>
            </div>
            <div className="text-right">
              <span className="font-black text-sm text-slate-900 block">{formatTaka(f.rechargeAmount)}</span>
              <span className="text-[10px] font-bold text-emerald-600">লাভ: +{formatTaka(f.profit)}</span>
            </div>
          </div>
        ))}
      </div>

      {showAdd && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-xs">
          <div className="bg-white rounded-2xl max-w-sm w-full p-5 shadow-2xl">
            <h3 className="font-bold text-slate-900 mb-3 text-sm">নতুন ফ্লেক্সিলোড রিচার্জ</h3>
            <form onSubmit={handleSave} className="space-y-3 text-xs">
              <div>
                <label className="block font-semibold mb-1">অপারেটর</label>
                <select
                  value={operator}
                  onChange={(e) => setOperator(e.target.value as any)}
                  className="w-full px-3 py-2 border rounded-xl bg-white font-bold"
                >
                  <option value="Grameenphone">Grameenphone (জিপি)</option>
                  <option value="Banglalink">Banglalink (বাংলালিংক)</option>
                  <option value="Robi">Robi (রবি)</option>
                  <option value="Airtel">Airtel (এয়ারটেল)</option>
                  <option value="Teletalk">Teletalk (টেলিটক)</option>
                </select>
              </div>
              <div>
                <label className="block font-semibold mb-1">মোবাইল নম্বর *</label>
                <input
                  type="text"
                  required
                  value={mobileNumber}
                  onChange={(e) => setMobileNumber(e.target.value)}
                  placeholder="01xxxxxxxxx"
                  className="w-full px-3 py-2 border rounded-xl font-bold"
                />
              </div>
              <div className="grid grid-cols-2 gap-2">
                <div>
                  <label className="block font-semibold mb-1">রিচার্জের পরিমাণ (৳)</label>
                  <input
                    type="number"
                    step="any"
                    value={rechargeAmount}
                    onChange={(e) => setRechargeAmount(e.target.value)}
                    className="w-full px-3 py-2 border rounded-xl font-bold text-sky-700"
                  />
                </div>
                <div>
                  <label className="block font-semibold mb-1">কমিশন লাভ (৳)</label>
                  <input
                    type="number"
                    step="any"
                    value={profit}
                    onChange={(e) => setProfit(e.target.value)}
                    className="w-full px-3 py-2 border rounded-xl font-bold text-emerald-600"
                  />
                </div>
              </div>
              <div>
                <label className="block font-semibold mb-1">নোট (প্যাক/অফার)</label>
                <input
                  type="text"
                  value={notes}
                  onChange={(e) => setNotes(e.target.value)}
                  placeholder="যেমন: মিনিট প্যাক"
                  className="w-full px-3 py-2 border rounded-xl"
                />
              </div>
              <div className="flex justify-end gap-2 pt-2">
                <button type="button" onClick={() => setShowAdd(false)} className="px-3 py-1.5 border rounded-xl">
                  বাতিল
                </button>
                <button type="submit" className="px-4 py-1.5 bg-sky-600 text-white font-bold rounded-xl shadow">
                  সংরক্ষণ
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

// DAILY CLOSING
const DailyClosingSubScreen: React.FC<{ onBack: () => void }> = ({ onBack }) => {
  const {
    sales,
    expenses,
    mobileBanking,
    flexiload,
    customers,
    dailyClosings,
    todayDateStr,
    recordDailyClosing,
  } = useShop();

  const [actualCash, setActualCash] = useState('');
  const [notes, setNotes] = useState('');

  // Daily calculations
  const todaySales = sales.filter((s) => s.saleDate === todayDateStr);
  const todayExpenses = expenses.filter((e) => e.expenseDate === todayDateStr);
  const todayMb = mobileBanking.filter((m) => m.transactionDate === todayDateStr);
  const todayFlexi = flexiload.filter((f) => f.transactionDate === todayDateStr);

  const productSales = todaySales.reduce((acc, s) => acc + s.totalAmount, 0);
  const mbVolume = todayMb.reduce((acc, m) => acc + m.amount, 0);
  const flexiVolume = todayFlexi.reduce((acc, f) => acc + f.rechargeAmount, 0);

  const grossProfit =
    todaySales.reduce((acc, s) => acc + s.grossProfit, 0) +
    todayMb.reduce((acc, m) => acc + m.commissionProfit, 0) +
    todayFlexi.reduce((acc, f) => acc + f.profit, 0);

  const totalExp = todayExpenses.reduce((acc, e) => acc + e.amount, 0);
  const customerPayments = todaySales.reduce((acc, s) => acc + s.paidAmount, 0);

  const totalCashIn = customerPayments + todayMb.filter((m) => m.type === 'CASH_IN').reduce((acc, m) => acc + m.amount, 0);
  const totalCashOut = totalExp + todayMb.filter((m) => m.type === 'CASH_OUT').reduce((acc, m) => acc + m.amount, 0);
  const expectedCash = Math.max(0, totalCashIn - totalCashOut);

  const enteredCash = parseFloat(actualCash) || expectedCash;
  const cashDifference = enteredCash - expectedCash;

  const handleSaveClosing = (e: React.FormEvent) => {
    e.preventDefault();

    recordDailyClosing({
      closingDate: todayDateStr,
      productSales,
      mobileBankingVolume: mbVolume,
      flexiloadVolume: flexiVolume,
      grossProfit,
      totalExpenses: totalExp,
      customerPaymentsReceived: customerPayments,
      totalCashReceived: totalCashIn,
      totalCashPaid: totalCashOut,
      expectedCash,
      actualCash: enteredCash,
      cashDifference,
      notes: notes.trim(),
      isDemo: false,
    });
  };

  return (
    <div className="space-y-4 pb-24">
      <div className="flex items-center justify-between pb-2 border-b border-slate-200">
        <button
          type="button"
          onClick={onBack}
          className="flex items-center gap-1.5 text-xs font-bold text-slate-600 bg-white border border-slate-200 px-3 py-1.5 rounded-xl shadow-2xs"
        >
          <ArrowLeft className="w-4 h-4" /> <span>মেনু</span>
        </button>
        <h2 className="text-sm font-bold text-slate-800">দৈনিক হিসাব ক্লোজিং</h2>
        <div className="w-12"></div>
      </div>

      {/* Today's Auto Calculated Metrics */}
      <div className="bg-white p-4 rounded-2xl border border-slate-200 shadow-2xs space-y-3 text-xs">
        <h3 className="font-bold text-slate-800 text-sm pb-2 border-b border-slate-100">
          আজকের হিসাব খতিয়ান ({todayDateStr})
        </h3>

        <div className="space-y-2">
          <div className="flex justify-between">
            <span className="text-slate-600">মোট পণ্য বিক্রয়:</span>
            <span className="font-bold">{formatTaka(productSales)}</span>
          </div>
          <div className="flex justify-between">
            <span className="text-slate-600">মোবাইল ব্যাংকিং ভলিউম:</span>
            <span className="font-bold">{formatTaka(mbVolume)}</span>
          </div>
          <div className="flex justify-between">
            <span className="text-slate-600">ফ্লেক্সিলোড ভলিউম:</span>
            <span className="font-bold">{formatTaka(flexiVolume)}</span>
          </div>
          <div className="flex justify-between text-emerald-700 font-bold border-t pt-1.5">
            <span>সর্বমোট মোট লাভ (Gross Profit):</span>
            <span>+{formatTaka(grossProfit)}</span>
          </div>
          <div className="flex justify-between text-rose-600 font-bold">
            <span>মোট খরচ (Expenses):</span>
            <span>-{formatTaka(totalExp)}</span>
          </div>
          <div className="flex justify-between text-blue-700 font-black text-sm border-t pt-2">
            <span>ক্যাশ ড্রয়ারে সম্ভাব্য ক্যাশ (Expected):</span>
            <span>{formatTaka(expectedCash)}</span>
          </div>
        </div>

        {/* Closing Entry Form */}
        <form onSubmit={handleSaveClosing} className="space-y-3 pt-3 border-t border-slate-200">
          <div>
            <label className="block font-semibold mb-1">বাস্তবে ক্যাশ ড্রয়ারে গণনাকৃত নগদ টাকা (৳):</label>
            <input
              type="number"
              step="any"
              value={actualCash}
              onChange={(e) => setActualCash(e.target.value)}
              placeholder={expectedCash.toString()}
              className="w-full px-3 py-2 border rounded-xl font-black text-lg text-emerald-700"
            />
          </div>

          <div
            className={`p-2.5 rounded-xl font-bold flex items-center justify-between ${
              cashDifference === 0
                ? 'bg-emerald-50 text-emerald-800 border border-emerald-200'
                : cashDifference > 0
                ? 'bg-blue-50 text-blue-800 border border-blue-200'
                : 'bg-red-50 text-red-800 border border-red-200'
            }`}
          >
            <span>ক্যাশ পার্থক্য (Difference):</span>
            <span>
              {cashDifference === 0
                ? 'পরিপূর্ণ সমান (০)'
                : cashDifference > 0
                ? `+${formatTaka(cashDifference)} (উদ্বৃত্ত)`
                : `-${formatTaka(Math.abs(cashDifference))} (ঘাটতি)`}
            </span>
          </div>

          <div>
            <label className="block font-semibold mb-1">ক্লোজিং মন্তব্য (ঐচ্ছিক):</label>
            <input
              type="text"
              value={notes}
              onChange={(e) => setNotes(e.target.value)}
              placeholder="সারাদিনের সার্বিক মন্তব্য..."
              className="w-full px-3 py-2 border rounded-xl"
            />
          </div>

          <button
            type="submit"
            className="w-full py-2.5 bg-emerald-600 hover:bg-emerald-700 text-white font-bold rounded-xl shadow"
          >
            আজকের ক্লোজিং ফাইনাল সংরক্ষণ করুন
          </button>
        </form>
      </div>

      {/* Past Closings History */}
      <div className="space-y-2">
        <h4 className="font-bold text-slate-800 text-xs">পূর্ববর্তী ক্লোজিং রিপোর্টসমূহ</h4>
        {dailyClosings.map((c) => (
          <div key={c.id} className="p-3 bg-white border border-slate-200 rounded-xl text-xs space-y-1">
            <div className="flex justify-between font-bold">
              <span>তারিখ: {c.closingDate}</span>
              <span className="text-emerald-700">লাভ: +{formatTaka(c.grossProfit)}</span>
            </div>
            <div className="flex justify-between text-slate-500 text-[11px]">
              <span>বিক্রয়: {formatTaka(c.productSales)}</span>
              <span>প্রকৃত ক্যাশ: {formatTaka(c.actualCash)}</span>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};

// REPORTS & ANALYTICS
const ReportsSubScreen: React.FC<{ onBack: () => void }> = ({ onBack }) => {
  const { sales, expenses, mobileBanking, flexiload, products } = useShop();

  const totalSalesRevenue = sales.reduce((acc, s) => acc + s.totalAmount, 0);
  const totalSalesProfit = sales.reduce((acc, s) => acc + s.grossProfit, 0);
  const totalMbProfit = mobileBanking.reduce((acc, m) => acc + m.commissionProfit, 0);
  const totalFlexiProfit = flexiload.reduce((acc, f) => acc + f.profit, 0);
  const totalGrossProfit = totalSalesProfit + totalMbProfit + totalFlexiProfit;
  const totalExpenses = expenses.reduce((acc, e) => acc + e.amount, 0);
  const netProfit = totalGrossProfit - totalExpenses;

  return (
    <div className="space-y-4 pb-24">
      <div className="flex items-center justify-between pb-2 border-b border-slate-200">
        <button
          type="button"
          onClick={onBack}
          className="flex items-center gap-1.5 text-xs font-bold text-slate-600 bg-white border border-slate-200 px-3 py-1.5 rounded-xl shadow-2xs"
        >
          <ArrowLeft className="w-4 h-4" /> <span>মেনু</span>
        </button>
        <h2 className="text-sm font-bold text-slate-800">রিপোর্ট ও বিশ্লেষণ (Reports)</h2>
        <div className="w-12"></div>
      </div>

      <div className="grid grid-cols-2 gap-3 text-xs">
        <div className="p-3 bg-white border border-slate-200 rounded-xl">
          <span className="text-slate-400 block font-semibold">সর্বমোট বিক্রয় রাজস্ব</span>
          <span className="text-lg font-black text-slate-900">{formatTaka(totalSalesRevenue)}</span>
        </div>
        <div className="p-3 bg-emerald-50 border border-emerald-200 rounded-xl">
          <span className="text-emerald-700 block font-semibold">সর্বমোট নিট লাভ (Net)</span>
          <span className="text-lg font-black text-emerald-700">{formatTaka(netProfit)}</span>
        </div>
      </div>

      {/* Breakdown */}
      <div className="bg-white p-4 rounded-2xl border border-slate-200 text-xs space-y-2.5">
        <h3 className="font-bold text-slate-800 text-sm">লাভের উৎসসমূহ</h3>
        <div className="flex justify-between">
          <span>পণ্য বিক্রয় থেকে লাভ:</span>
          <span className="font-bold text-emerald-600">+{formatTaka(totalSalesProfit)}</span>
        </div>
        <div className="flex justify-between">
          <span>মোবাইল ব্যাংকিং কমিশন:</span>
          <span className="font-bold text-teal-600">+{formatTaka(totalMbProfit)}</span>
        </div>
        <div className="flex justify-between">
          <span>ফ্লেক্সিলোড কমিশন:</span>
          <span className="font-bold text-sky-600">+{formatTaka(totalFlexiProfit)}</span>
        </div>
        <div className="flex justify-between border-t pt-2 text-rose-600">
          <span>দোকানের যাবতীয় খরচ:</span>
          <span className="font-bold">-{formatTaka(totalExpenses)}</span>
        </div>
      </div>
    </div>
  );
};

// SETTINGS & BACKUP
const SettingsSubScreen: React.FC<{ onBack: () => void }> = ({ onBack }) => {
  const {
    profile,
    customCategories,
    updateProfile,
    addCustomCategory,
    deleteCustomCategory,
    exportBackupJson,
    restoreBackupJson,
  } = useShop();

  const [shopName, setShopName] = useState(profile.shopName);
  const [ownerName, setOwnerName] = useState(profile.ownerName);
  const [phone, setPhone] = useState(profile.phone);
  const [address, setAddress] = useState(profile.address);
  const [tagline, setTagline] = useState(profile.tagline);
  const [newCat, setNewCat] = useState('');

  const handleSaveProfile = (e: React.FormEvent) => {
    e.preventDefault();
    updateProfile({
      ...profile,
      shopName: shopName.trim(),
      ownerName: ownerName.trim(),
      phone: phone.trim(),
      address: address.trim(),
      tagline: tagline.trim(),
    });
  };

  const handleExport = () => {
    const json = exportBackupJson();
    const blob = new Blob([json], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `yasin_telecom_backup_${getTodayStr()}.json`;
    a.click();
    URL.revokeObjectURL(url);
  };

  const handleFileImport = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;
    const reader = new FileReader();
    reader.onload = (event) => {
      const text = event.target?.result as string;
      if (text) {
        restoreBackupJson(text);
      }
    };
    reader.readAsText(file);
  };

  return (
    <div className="space-y-5 pb-24">
      <div className="flex items-center justify-between pb-2 border-b border-slate-200">
        <button
          type="button"
          onClick={onBack}
          className="flex items-center gap-1.5 text-xs font-bold text-slate-600 bg-white border border-slate-200 px-3 py-1.5 rounded-xl shadow-2xs"
        >
          <ArrowLeft className="w-4 h-4" /> <span>মেনু</span>
        </button>
        <h2 className="text-sm font-bold text-slate-800">সেটিংস ও ব্যাকআপ</h2>
        <div className="w-12"></div>
      </div>

      {/* Profile Form */}
      <form onSubmit={handleSaveProfile} className="bg-white p-4 rounded-2xl border border-slate-200 text-xs space-y-3">
        <h3 className="font-bold text-slate-800 text-sm">দোকানের প্রাথমিক তথ্য</h3>
        <div>
          <label className="block font-semibold mb-1">দোকানের নাম</label>
          <input
            type="text"
            value={shopName}
            onChange={(e) => setShopName(e.target.value)}
            className="w-full px-3 py-2 border rounded-xl"
          />
        </div>
        <div>
          <label className="block font-semibold mb-1">স্বত্বাধিকারী</label>
          <input
            type="text"
            value={ownerName}
            onChange={(e) => setOwnerName(e.target.value)}
            className="w-full px-3 py-2 border rounded-xl"
          />
        </div>
        <div>
          <label className="block font-semibold mb-1">ফোন নম্বর</label>
          <input
            type="text"
            value={phone}
            onChange={(e) => setPhone(e.target.value)}
            className="w-full px-3 py-2 border rounded-xl"
          />
        </div>
        <div>
          <label className="block font-semibold mb-1">ঠিকানা</label>
          <input
            type="text"
            value={address}
            onChange={(e) => setAddress(e.target.value)}
            className="w-full px-3 py-2 border rounded-xl"
          />
        </div>
        <div>
          <label className="block font-semibold mb-1">স্লোগান / ট্যাগলাইন</label>
          <input
            type="text"
            value={tagline}
            onChange={(e) => setTagline(e.target.value)}
            className="w-full px-3 py-2 border rounded-xl"
          />
        </div>
        <button type="submit" className="px-4 py-2 bg-emerald-600 text-white font-bold rounded-xl shadow">
          তথ্য সংরক্ষণ করুন
        </button>
      </form>

      {/* Backup & Restore */}
      <div className="bg-white p-4 rounded-2xl border border-slate-200 text-xs space-y-3">
        <h3 className="font-bold text-slate-800 text-sm">ডাটা ব্যাকআপ ও রিস্টোর</h3>
        <p className="text-slate-500">আপনার সমস্ত স্টক, বিক্রয়, খরচ ও বাকি খাতা একটি ফাইলে সেভ করে সুরক্ষিত রাখুন।</p>
        <div className="flex items-center gap-3 pt-2">
          <button
            type="button"
            onClick={handleExport}
            className="flex items-center gap-1.5 px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white font-bold rounded-xl shadow"
          >
            <Download className="w-4 h-4" /> ব্যাকআপ ডাউনলোড
          </button>
          <label className="flex items-center gap-1.5 px-4 py-2 bg-slate-100 hover:bg-slate-200 text-slate-700 font-bold rounded-xl border border-slate-300 cursor-pointer">
            <Upload className="w-4 h-4" /> ব্যাকআপ রিস্টোর
            <input type="file" accept=".json" onChange={handleFileImport} className="hidden" />
          </label>
        </div>
      </div>

      {/* Custom Categories */}
      <div className="bg-white p-4 rounded-2xl border border-slate-200 text-xs space-y-3">
        <h3 className="font-bold text-slate-800 text-sm">কাস্টম ক্যাটাগরি তৈরি</h3>
        <div className="flex gap-2">
          <input
            type="text"
            value={newCat}
            onChange={(e) => setNewCat(e.target.value)}
            placeholder="নতুন ক্যাটাগরি নাম..."
            className="flex-1 px-3 py-2 border rounded-xl"
          />
          <button
            type="button"
            onClick={() => {
              if (newCat.trim()) {
                addCustomCategory(newCat.trim());
                setNewCat('');
              }
            }}
            className="px-4 py-2 bg-[#0A2540] text-white font-bold rounded-xl shadow"
          >
            যোগ
          </button>
        </div>
        <div className="flex flex-wrap gap-1.5">
          {customCategories.map((c) => (
            <span key={c.id} className="inline-flex items-center gap-1 px-2.5 py-1 bg-slate-100 rounded-lg text-slate-700 font-medium">
              {c.name}
              <button type="button" onClick={() => deleteCustomCategory(c.id)} className="text-red-500 hover:text-red-700">
                <X className="w-3.5 h-3.5" />
              </button>
            </span>
          ))}
        </div>
      </div>
    </div>
  );
};

// ADMIN PANEL
const AdminPanelSubScreen: React.FC<{ onBack: () => void }> = ({ onBack }) => {
  const {
    profile,
    isAdminAuthenticated,
    loginAdmin,
    logoutAdmin,
    changeAdminPin,
    resetDemoData,
    resetAllData,
  } = useShop();

  const [enteredPin, setEnteredPin] = useState('');
  const [newPin, setNewPin] = useState('');
  const [showConfirmResetDemo, setShowConfirmResetDemo] = useState(false);
  const [showConfirmResetAll, setShowConfirmResetAll] = useState(false);

  const handleLogin = (e: React.FormEvent) => {
    e.preventDefault();
    loginAdmin(enteredPin);
    setEnteredPin('');
  };

  const handleChangePin = (e: React.FormEvent) => {
    e.preventDefault();
    if (newPin.trim().length >= 4) {
      changeAdminPin(newPin.trim());
      setNewPin('');
    }
  };

  return (
    <div className="space-y-4 pb-24">
      <div className="flex items-center justify-between pb-2 border-b border-slate-200">
        <button
          type="button"
          onClick={onBack}
          className="flex items-center gap-1.5 text-xs font-bold text-slate-600 bg-white border border-slate-200 px-3 py-1.5 rounded-xl shadow-2xs"
        >
          <ArrowLeft className="w-4 h-4" /> <span>মেনু</span>
        </button>
        <h2 className="text-sm font-bold text-red-600 flex items-center gap-1.5">
          <ShieldAlert className="w-4 h-4" /> অ্যাডমিন প্যানেল
        </h2>
        <div className="w-12"></div>
      </div>

      {!isAdminAuthenticated ? (
        <div className="bg-white p-6 rounded-2xl border border-red-200 shadow-sm max-w-sm mx-auto text-center space-y-4 text-xs">
          <div className="w-12 h-12 rounded-full bg-red-100 text-red-600 flex items-center justify-center mx-auto">
            <Lock className="w-6 h-6" />
          </div>
          <div>
            <h3 className="font-bold text-slate-900 text-base">অ্যাডমিন পিন দিন</h3>
            <p className="text-slate-500 mt-1">ডিফল্ট পিন: <strong>1234</strong></p>
          </div>
          <form onSubmit={handleLogin} className="space-y-3">
            <input
              type="password"
              required
              maxLength={6}
              value={enteredPin}
              onChange={(e) => setEnteredPin(e.target.value)}
              placeholder="৪-ডিজিট পিন"
              className="w-full text-center text-xl font-bold tracking-widest px-3 py-2 border rounded-xl"
            />
            <button type="submit" className="w-full py-2 bg-red-600 hover:bg-red-700 text-white font-bold rounded-xl shadow">
              আনলক করুন
            </button>
          </form>
        </div>
      ) : (
        <div className="space-y-4 text-xs">
          <div className="p-4 bg-emerald-50 border border-emerald-200 rounded-2xl flex items-center justify-between">
            <span className="font-bold text-emerald-800 flex items-center gap-1.5">
              <Unlock className="w-4 h-4 text-emerald-600" /> অ্যাডমিন মোড সক্রিয়
            </span>
            <button
              type="button"
              onClick={logoutAdmin}
              className="px-3 py-1 bg-red-600 text-white font-bold rounded-lg"
            >
              লক করুন
            </button>
          </div>

          {/* Change PIN */}
          <form onSubmit={handleChangePin} className="bg-white p-4 rounded-2xl border border-slate-200 space-y-3">
            <h4 className="font-bold text-slate-800 text-sm flex items-center gap-1.5">
              <KeyRound className="w-4 h-4 text-slate-700" /> পিন পরিবর্তন করুন
            </h4>
            <div className="flex gap-2">
              <input
                type="password"
                required
                maxLength={6}
                value={newPin}
                onChange={(e) => setNewPin(e.target.value)}
                placeholder="নতুন ৪-ডিজিট পিন..."
                className="flex-1 px-3 py-2 border rounded-xl font-bold tracking-widest"
              />
              <button type="submit" className="px-4 py-2 bg-[#0A2540] text-white font-bold rounded-xl shadow">
                আপডেট
              </button>
            </div>
          </form>

          {/* Reset Demo Data Only */}
          <div className="bg-amber-50 p-4 rounded-2xl border border-amber-200 space-y-2">
            <h4 className="font-bold text-amber-900 text-sm">ডেমো ডাটা রিসেট (Safe Reset)</h4>
            <p className="text-amber-800">
              শুধুমাত্র ইনস্টলেশনের সাথে আসা ডেমো পণ্য ও রেকর্ডগুলো মুছে যাবে। আপনার তৈরি করা আসল ডাটা অক্ষত থাকবে।
            </p>
            <button
              type="button"
              onClick={() => resetDemoData()}
              className="px-4 py-2 bg-amber-600 hover:bg-amber-700 text-white font-bold rounded-xl shadow"
            >
              ডেমো রেকর্ড মুছুন
            </button>
          </div>

          {/* Wipe All Data */}
          <div className="bg-red-50 p-4 rounded-2xl border border-red-200 space-y-2">
            <h4 className="font-bold text-red-900 text-sm">সম্পূর্ণ ডাটাবেজ রিসেট</h4>
            <p className="text-red-800">
              সমস্ত পণ্য, স্টক, মেমো, বিক্রয় ও বাকির খাতা মুছে প্রাথমিক অবস্থায় ফিরে যাবে।
            </p>
            <button
              type="button"
              onClick={() => {
                if (window.confirm('আপনি কি নিশ্চিত যে সম্পূর্ণ ডাটা রিসেট করতে চান?')) {
                  resetAllData();
                }
              }}
              className="px-4 py-2 bg-red-600 hover:bg-red-700 text-white font-bold rounded-xl shadow"
            >
              সকল ডাটা রিসেট করুন
            </button>
          </div>
        </div>
      )}
    </div>
  );
};
