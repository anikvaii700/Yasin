import React, { useState } from 'react';
import {
  ShieldAlert,
  Lock,
  Unlock,
  KeyRound,
  Trash2,
  AlertTriangle,
  RotateCcw,
  Download,
  Upload,
  Database,
  Search,
  Package,
  ShoppingCart,
  ShoppingBag,
  Users,
  Wallet,
  Receipt,
  FileText,
  CheckCircle2,
  X,
  History,
  Clock,
} from 'lucide-react';
import { useShop } from '../context/ShopContext';
import { ResetDemoDataModal } from '../components/ResetDemoDataModal';
import { formatTaka } from '../utils/formatters';

interface AdminPanelScreenProps {
  onExitAdmin: () => void;
}

type AdminTab =
  | 'PRODUCTS'
  | 'SALES'
  | 'PURCHASES'
  | 'CUSTOMERS'
  | 'DUE_TX'
  | 'LOANS'
  | 'EXPENSES'
  | 'MEMOS'
  | 'DEMO_DATA'
  | 'BACKUP_RESTORE'
  | 'DATA_MANAGEMENT'
  | 'SECURITY';

export const AdminPanelScreen: React.FC<AdminPanelScreenProps> = ({ onExitAdmin }) => {
  const {
    profile,
    products,
    sales,
    purchases,
    expenses,
    customers,
    customerTransactions,
    loans,
    cashMemos,
    isAdminAuthenticated,
    loginAdmin,
    logoutAdmin,
    changeAdminPin,
    deleteProduct,
    deleteSale,
    deletePurchase,
    deleteCustomer,
    deleteCustomerTx,
    deleteLoan,
    deleteExpense,
    deleteCashMemo,
    exportBackupJson,
    restoreBackupJson,
    resetAllData,
    getDemoCounts,
    showMessage,
    showError,
  } = useShop();

  const [enteredPin, setEnteredPin] = useState('');
  const [activeTab, setActiveTab] = useState<AdminTab>('PRODUCTS');
  const [searchTerm, setSearchTerm] = useState('');

  // Change PIN state
  const [currentPinInput, setCurrentPinInput] = useState('');
  const [newPinInput, setNewPinInput] = useState('');
  const [confirmPinInput, setConfirmPinInput] = useState('');

  // 3-step Reset Demo Data Modal
  const [showDemoResetModal, setShowDemoResetModal] = useState(false);

  // Deletion confirm modal
  const [confirmDeleteModal, setConfirmDeleteModal] = useState<{
    type: string;
    id: string;
    title: string;
    secondaryId?: string;
    onConfirm: () => void;
  } | null>(null);

  const handleLogin = (e: React.FormEvent) => {
    e.preventDefault();
    loginAdmin(enteredPin);
    setEnteredPin('');
  };

  const handleChangePin = (e: React.FormEvent) => {
    e.preventDefault();
    if (currentPinInput !== (profile.adminPin || '1234')) {
      showError('বর্তমান পিন সঠিক নয়!');
      return;
    }
    if (newPinInput.length < 4) {
      showError('নতুন পিন কমপক্ষে ৪ সংখ্যার হতে হবে');
      return;
    }
    if (newPinInput !== confirmPinInput) {
      showError('নতুন পিন ও কনফার্ম পিন মিলছে না!');
      return;
    }

    changeAdminPin(newPinInput);
    setCurrentPinInput('');
    setNewPinInput('');
    setConfirmPinInput('');
  };

  const handleExport = () => {
    const json = exportBackupJson();
    const blob = new Blob([json], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `yasin_telecom_full_backup.json`;
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

  const demoCounts = getDemoCounts();

  // If NOT authenticated, show PIN entry screen
  if (!isAdminAuthenticated) {
    return (
      <div className="py-8 px-4 max-w-sm mx-auto text-center space-y-4">
        <div className="w-16 h-16 rounded-2xl bg-red-100 text-red-600 flex items-center justify-center mx-auto shadow-md border border-red-200">
          <Lock className="w-8 h-8" />
        </div>

        <div>
          <h2 className="text-xl font-black text-slate-900 tracking-tight">অ্যাডমিন প্রমাণীকরণ</h2>
          <p className="text-xs text-slate-500 mt-1">
            গুরুত্বপূর্ণ ডাটা নিয়ন্ত্রণ ও মোছার জন্য সঠিক অ্যাডমিন পিন দিন
          </p>
          <span className="inline-block mt-2 text-[11px] font-bold text-amber-700 bg-amber-50 px-2.5 py-1 rounded-full border border-amber-200">
            ডিফল্ট পিন কোড: 1234
          </span>
        </div>

        <form onSubmit={handleLogin} className="space-y-3 pt-2">
          <input
            type="password"
            required
            autoFocus
            maxLength={6}
            value={enteredPin}
            onChange={(e) => setEnteredPin(e.target.value)}
            placeholder="••••"
            className="w-full text-center text-2xl font-black tracking-widest px-4 py-3 bg-white border border-slate-300 rounded-2xl focus:ring-2 focus:ring-red-500 focus:outline-none shadow-sm"
          />

          <button
            type="submit"
            className="w-full py-3 bg-red-600 hover:bg-red-700 active:scale-98 text-white font-extrabold rounded-2xl shadow-lg transition text-sm"
          >
            আনলক করুন (Unlock Admin)
          </button>
        </form>

        <button
          type="button"
          onClick={onExitAdmin}
          className="text-xs text-slate-500 hover:text-slate-800 underline pt-2 block mx-auto"
        >
          ড্যাশবোর্ডে ফিরে যান
        </button>
      </div>
    );
  }

  // 12 Admin Tabs Navigation
  const tabs: { key: AdminTab; label: string; count?: number; icon: any }[] = [
    { key: 'PRODUCTS', label: '1. পণ্য মোছা', count: products.length, icon: Package },
    { key: 'SALES', label: '2. বিক্রয় রেকর্ড মোছা', count: sales.length, icon: ShoppingCart },
    { key: 'PURCHASES', label: '3. ক্রয় রেকর্ড মোছা', count: purchases.length, icon: ShoppingBag },
    { key: 'CUSTOMERS', label: '4. কাস্টমার মোছা', count: customers.length, icon: Users },
    { key: 'DUE_TX', label: '5. বাকি লেনদেন মোছা', icon: Wallet },
    { key: 'LOANS', label: '6. ঋণ রেকর্ড মোছা', count: loans.length, icon: History },
    { key: 'EXPENSES', label: '7. খরচ মোছা', count: expenses.length, icon: Receipt },
    { key: 'MEMOS', label: '8. ক্যাশ মেমো মোছা', count: cashMemos.length, icon: FileText },
    { key: 'DEMO_DATA', label: '9. ডেমো ডাটা রিসেট', count: demoCounts.total, icon: RotateCcw },
    { key: 'BACKUP_RESTORE', label: '10. ব্যাকআপ ও রিস্টোর', icon: Download },
    { key: 'DATA_MANAGEMENT', label: '11. ডাটা ব্যবস্থাপনা', icon: Database },
    { key: 'SECURITY', label: '12. সিকিউরিটি ও পিন', icon: KeyRound },
  ];

  return (
    <div className="space-y-4 pb-24 text-xs sm:text-sm">
      {/* Top Admin Banner */}
      <div className="bg-gradient-to-r from-red-600 to-rose-800 text-white p-4 rounded-2xl shadow-lg flex items-center justify-between">
        <div className="flex items-center gap-2.5">
          <div className="w-9 h-9 rounded-xl bg-white/20 flex items-center justify-center">
            <ShieldAlert className="w-5 h-5 text-amber-300" />
          </div>
          <div>
            <h2 className="font-extrabold text-base leading-tight">অ্যাডমিন ম্যানেজমেন্ট প্যানেল</h2>
            <p className="text-[11px] text-red-100 flex items-center gap-1.5 mt-0.5">
              <span className="w-2 h-2 rounded-full bg-emerald-400 animate-pulse" />
              <span>মোড সক্রিয় • নিষ্ক্রিয়তায় ৫ মিনিটে অটো-লক হবে</span>
            </p>
          </div>
        </div>

        <div className="flex items-center gap-2">
          <button
            type="button"
            onClick={logoutAdmin}
            className="px-3 py-1.5 bg-white/15 hover:bg-white/25 rounded-xl text-xs font-bold transition flex items-center gap-1"
          >
            <Lock className="w-3.5 h-3.5" /> লক করুন
          </button>
          <button
            type="button"
            onClick={onExitAdmin}
            className="px-3 py-1.5 bg-slate-900/80 hover:bg-slate-900 rounded-xl text-xs font-bold transition"
          >
            প্রস্থান
          </button>
        </div>
      </div>

      {/* Tabs Horizontal Navigation */}
      <div className="flex items-center gap-1.5 overflow-x-auto pb-1 text-xs no-scrollbar">
        {tabs.map((tab) => {
          const isSelected = activeTab === tab.key;
          const IconComp = tab.icon;
          return (
            <button
              key={tab.key}
              type="button"
              onClick={() => {
                setActiveTab(tab.key);
                setSearchTerm('');
              }}
              className={`flex items-center gap-1.5 px-3 py-2 rounded-xl font-bold shrink-0 transition ${
                isSelected
                  ? 'bg-red-600 text-white shadow-md'
                  : 'bg-white text-slate-700 border border-slate-200 hover:bg-slate-50'
              }`}
            >
              <IconComp className="w-4 h-4" />
              <span>{tab.label}</span>
              {tab.count !== undefined && (
                <span
                  className={`text-[10px] px-1.5 py-0.2 rounded-full font-black ${
                    isSelected ? 'bg-red-800 text-white' : 'bg-slate-100 text-slate-600'
                  }`}
                >
                  {tab.count}
                </span>
              )}
            </button>
          );
        })}
      </div>

      {/* TAB 1: DELETE PRODUCTS */}
      {activeTab === 'PRODUCTS' && (
        <div className="bg-white rounded-2xl p-4 border border-slate-200 shadow-2xs space-y-3">
          <div className="flex items-center justify-between pb-2 border-b border-slate-100">
            <div>
              <h3 className="font-bold text-slate-900 text-sm">১. পণ্য ডাটাবেজ থেকে মুছে ফেলা</h3>
              <p className="text-xs text-slate-500">স্থায়ীভাবে মুছে ফেলার আগে নিশ্চিতকরণ আবশ্যক</p>
            </div>
            <input
              type="text"
              placeholder="পণ্য খুঁজুন..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="px-3 py-1.5 border rounded-xl text-xs max-w-[180px]"
            />
          </div>

          <div className="divide-y divide-slate-100 max-h-96 overflow-y-auto">
            {products
              .filter((p) => p.name.toLowerCase().includes(searchTerm.toLowerCase()))
              .map((p) => (
                <div key={p.id} className="py-2.5 flex items-center justify-between text-xs">
                  <div>
                    <span className="font-bold text-slate-800">{p.name}</span>
                    <p className="text-[11px] text-slate-500">
                      ক্যাটাগরি: {p.category} • স্টক: <strong>{p.currentStock}</strong> • মূল্য: {formatTaka(p.sellingPrice)}
                    </p>
                  </div>
                  <button
                    type="button"
                    onClick={() =>
                      setConfirmDeleteModal({
                        type: 'পণ্য',
                        id: p.id,
                        title: p.name,
                        onConfirm: () => deleteProduct(p.id),
                      })
                    }
                    className="flex items-center gap-1 px-3 py-1.5 bg-red-50 hover:bg-red-100 text-red-700 font-bold rounded-xl transition"
                  >
                    <Trash2 className="w-3.5 h-3.5" /> মুছুন
                  </button>
                </div>
              ))}
          </div>
        </div>
      )}

      {/* TAB 2: DELETE SALES */}
      {activeTab === 'SALES' && (
        <div className="bg-white rounded-2xl p-4 border border-slate-200 shadow-2xs space-y-3">
          <div className="flex items-center justify-between pb-2 border-b border-slate-100">
            <div>
              <h3 className="font-bold text-slate-900 text-sm">২. বিক্রয় রেকর্ড মুছে ফেলা</h3>
              <p className="text-xs text-slate-500">ইনভয়েস রেকর্ড স্থায়ীভাবে অপসারণ</p>
            </div>
            <input
              type="text"
              placeholder="ইনভয়েস বা ক্রেতা..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="px-3 py-1.5 border rounded-xl text-xs max-w-[180px]"
            />
          </div>

          <div className="divide-y divide-slate-100 max-h-96 overflow-y-auto">
            {sales
              .filter(
                (s) =>
                  s.invoiceNumber.toLowerCase().includes(searchTerm.toLowerCase()) ||
                  s.customerName.toLowerCase().includes(searchTerm.toLowerCase())
              )
              .map((s) => (
                <div key={s.id} className="py-2.5 flex items-center justify-between text-xs">
                  <div>
                    <span className="font-bold text-slate-800">
                      {s.invoiceNumber} - {s.customerName}
                    </span>
                    <p className="text-[11px] text-slate-500">
                      তারিখ: {s.saleDate} • মোট: {formatTaka(s.totalAmount)} • লাভ: +{formatTaka(s.grossProfit)}
                    </p>
                  </div>
                  <button
                    type="button"
                    onClick={() =>
                      setConfirmDeleteModal({
                        type: 'বিক্রয় রেকর্ড',
                        id: s.id,
                        title: `${s.invoiceNumber} (${s.customerName})`,
                        onConfirm: () => deleteSale(s.id),
                      })
                    }
                    className="flex items-center gap-1 px-3 py-1.5 bg-red-50 hover:bg-red-100 text-red-700 font-bold rounded-xl transition"
                  >
                    <Trash2 className="w-3.5 h-3.5" /> মুছুন
                  </button>
                </div>
              ))}
          </div>
        </div>
      )}

      {/* TAB 3: DELETE PURCHASES */}
      {activeTab === 'PURCHASES' && (
        <div className="bg-white rounded-2xl p-4 border border-slate-200 shadow-2xs space-y-3">
          <div className="flex items-center justify-between pb-2 border-b border-slate-100">
            <div>
              <h3 className="font-bold text-slate-900 text-sm">৩. ক্রয় ভাউচার মুছে ফেলা</h3>
              <p className="text-xs text-slate-500">সাপ্লায়ার ক্রয় রেকর্ড অপসারণ</p>
            </div>
          </div>
          <div className="divide-y divide-slate-100 max-h-96 overflow-y-auto">
            {purchases.map((pur) => (
              <div key={pur.id} className="py-2.5 flex items-center justify-between text-xs">
                <div>
                  <span className="font-bold text-slate-800">{pur.productName}</span>
                  <p className="text-[11px] text-slate-500">
                    সাপ্লায়ার: {pur.supplierName || 'অজানা'} • পরিমাণ: {pur.quantity} • মোট: {formatTaka(pur.totalAmount)}
                  </p>
                </div>
                <button
                  type="button"
                  onClick={() =>
                    setConfirmDeleteModal({
                      type: 'ক্রয় ভাউচার',
                      id: pur.id,
                      title: `${pur.productName} (${formatTaka(pur.totalAmount)})`,
                      onConfirm: () => deletePurchase(pur.id),
                    })
                  }
                  className="flex items-center gap-1 px-3 py-1.5 bg-red-50 hover:bg-red-100 text-red-700 font-bold rounded-xl"
                >
                  <Trash2 className="w-3.5 h-3.5" /> মুছুন
                </button>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* TAB 4: DELETE CUSTOMERS */}
      {activeTab === 'CUSTOMERS' && (
        <div className="bg-white rounded-2xl p-4 border border-slate-200 shadow-2xs space-y-3">
          <div className="flex items-center justify-between pb-2 border-b border-slate-100">
            <div>
              <h3 className="font-bold text-slate-900 text-sm">৪. কাস্টমার মুছে ফেলা</h3>
              <p className="text-xs text-slate-500">কাস্টমার ও তাদের সংশ্লিষ্ট বাকি খাতা স্থায়ীভাবে মুছে যাবে</p>
            </div>
          </div>
          <div className="divide-y divide-slate-100 max-h-96 overflow-y-auto">
            {customers.map((c) => (
              <div key={c.id} className="py-2.5 flex items-center justify-between text-xs">
                <div>
                  <span className="font-bold text-slate-800">{c.name}</span>
                  <p className="text-[11px] text-slate-500">
                    ফোন: {c.mobile || 'নেই'} • বকেয়া: <strong className="text-red-600">{formatTaka(c.remainingDue)}</strong>
                  </p>
                </div>
                <button
                  type="button"
                  onClick={() =>
                    setConfirmDeleteModal({
                      type: 'কাস্টমার',
                      id: c.id,
                      title: `${c.name} (বকেয়া: ${formatTaka(c.remainingDue)})`,
                      onConfirm: () => deleteCustomer(c.id),
                    })
                  }
                  className="flex items-center gap-1 px-3 py-1.5 bg-red-50 hover:bg-red-100 text-red-700 font-bold rounded-xl"
                >
                  <Trash2 className="w-3.5 h-3.5" /> মুছুন
                </button>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* TAB 5: DELETE DUE TRANSACTIONS */}
      {activeTab === 'DUE_TX' && (
        <div className="bg-white rounded-2xl p-4 border border-slate-200 shadow-2xs space-y-3">
          <div className="pb-2 border-b border-slate-100">
            <h3 className="font-bold text-slate-900 text-sm">৫. বাকি বা জমার নির্দিষ্ট লেনদেন মোছা</h3>
            <p className="text-xs text-slate-500">লেনদেনটি মুছে কাস্টমারের ব্যালেন্স স্বয়ংক্রিয়ভাবে পুনর্নির্ধারণ হবে</p>
          </div>
          <div className="space-y-3 max-h-96 overflow-y-auto">
            {customers.map((cust) => {
              const txs = customerTransactions[cust.id] || [];
              if (txs.length === 0) return null;
              return (
                <div key={cust.id} className="p-3 bg-slate-50 rounded-xl border border-slate-200 space-y-1.5">
                  <h4 className="font-bold text-slate-900 text-xs">গ্রাহক: {cust.name}</h4>
                  <div className="divide-y divide-slate-200">
                    {txs.map((tx) => (
                      <div key={tx.id} className="py-1.5 flex items-center justify-between text-xs">
                        <div>
                          <span
                            className={`font-black text-[10px] px-1.5 py-0.5 rounded ${
                              tx.type === 'DUE' ? 'bg-red-100 text-red-700' : 'bg-emerald-100 text-emerald-800'
                            }`}
                          >
                            {tx.type === 'DUE' ? 'বাকি' : 'জমা'}
                          </span>
                          <span className="font-semibold text-slate-700 ml-1.5">{formatTaka(tx.amount)}</span>
                          <span className="text-[10px] text-slate-400 ml-2">({tx.transactionDate})</span>
                          <p className="text-[11px] text-slate-500 mt-0.5">{tx.notes}</p>
                        </div>
                        <button
                          type="button"
                          onClick={() =>
                            setConfirmDeleteModal({
                              type: 'বাকি লেনদেন',
                              id: cust.id,
                              secondaryId: tx.id,
                              title: `${cust.name} - ${tx.type === 'DUE' ? 'বাকি' : 'জমা'} ${formatTaka(tx.amount)}`,
                              onConfirm: () => deleteCustomerTx(cust.id, tx.id),
                            })
                          }
                          className="px-2.5 py-1 bg-red-100 hover:bg-red-200 text-red-800 font-bold rounded-lg text-xs"
                        >
                          মুছুন
                        </button>
                      </div>
                    ))}
                  </div>
                </div>
              );
            })}
          </div>
        </div>
      )}

      {/* TAB 6: DELETE LOANS */}
      {activeTab === 'LOANS' && (
        <div className="bg-white rounded-2xl p-4 border border-slate-200 shadow-2xs space-y-3">
          <div className="pb-2 border-b border-slate-100">
            <h3 className="font-bold text-slate-900 text-sm">৬. ব্যক্তিগত ধার-দেনা রেকর্ড মোছা</h3>
          </div>
          <div className="divide-y divide-slate-100 max-h-96 overflow-y-auto">
            {loans.map((l) => (
              <div key={l.id} className="py-2.5 flex items-center justify-between text-xs">
                <div>
                  <span className="font-bold text-slate-800">{l.personName}</span>
                  <p className="text-[11px] text-slate-500">
                    ধরন: {l.loanType === 'LENT' ? 'পাওনা' : 'দেনা'} • বকেয়া: {formatTaka(l.remainingAmount)}
                  </p>
                </div>
                <button
                  type="button"
                  onClick={() =>
                    setConfirmDeleteModal({
                      type: 'ঋণ রেকর্ড',
                      id: l.id,
                      title: `${l.personName} (${formatTaka(l.remainingAmount)})`,
                      onConfirm: () => deleteLoan(l.id),
                    })
                  }
                  className="flex items-center gap-1 px-3 py-1.5 bg-red-50 hover:bg-red-100 text-red-700 font-bold rounded-xl"
                >
                  <Trash2 className="w-3.5 h-3.5" /> মুছুন
                </button>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* TAB 7: DELETE EXPENSES */}
      {activeTab === 'EXPENSES' && (
        <div className="bg-white rounded-2xl p-4 border border-slate-200 shadow-2xs space-y-3">
          <div className="pb-2 border-b border-slate-100">
            <h3 className="font-bold text-slate-900 text-sm">৭. খরচের রেকর্ড মোছা</h3>
          </div>
          <div className="divide-y divide-slate-100 max-h-96 overflow-y-auto">
            {expenses.map((exp) => (
              <div key={exp.id} className="py-2.5 flex items-center justify-between text-xs">
                <div>
                  <span className="font-bold text-slate-800">{exp.title}</span>
                  <p className="text-[11px] text-slate-500">
                    ক্যাটাগরি: {exp.category} • টাকার পরিমাণ: {formatTaka(exp.amount)} • তারিখ: {exp.expenseDate}
                  </p>
                </div>
                <button
                  type="button"
                  onClick={() =>
                    setConfirmDeleteModal({
                      type: 'খরচ',
                      id: exp.id,
                      title: `${exp.title} (${formatTaka(exp.amount)})`,
                      onConfirm: () => deleteExpense(exp.id),
                    })
                  }
                  className="flex items-center gap-1 px-3 py-1.5 bg-red-50 hover:bg-red-100 text-red-700 font-bold rounded-xl"
                >
                  <Trash2 className="w-3.5 h-3.5" /> মুছুন
                </button>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* TAB 8: DELETE CASH MEMOS */}
      {activeTab === 'MEMOS' && (
        <div className="bg-white rounded-2xl p-4 border border-slate-200 shadow-2xs space-y-3">
          <div className="pb-2 border-b border-slate-100">
            <h3 className="font-bold text-slate-900 text-sm">৮. ক্যাশ মেমো মোছা</h3>
          </div>
          <div className="divide-y divide-slate-100 max-h-96 overflow-y-auto">
            {cashMemos.map((m) => (
              <div key={m.id} className="py-2.5 flex items-center justify-between text-xs">
                <div>
                  <span className="font-bold text-slate-800">#{m.memoNumber} - {m.customerName}</span>
                  <p className="text-[11px] text-slate-500">
                    তারিখ: {m.date} • মোট: {formatTaka(m.grandTotal)} • বাকি: {formatTaka(m.dueAmount)}
                  </p>
                </div>
                <button
                  type="button"
                  onClick={() =>
                    setConfirmDeleteModal({
                      type: 'ক্যাশ মেমো',
                      id: m.id,
                      title: `#${m.memoNumber} (${m.customerName})`,
                      onConfirm: () => deleteCashMemo(m.id),
                    })
                  }
                  className="flex items-center gap-1 px-3 py-1.5 bg-red-50 hover:bg-red-100 text-red-700 font-bold rounded-xl"
                >
                  <Trash2 className="w-3.5 h-3.5" /> মুছুন
                </button>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* TAB 9: MANAGE DEMO DATA & 3-STEP RESET */}
      {activeTab === 'DEMO_DATA' && (
        <div className="bg-white rounded-2xl p-5 border border-slate-200 shadow-2xs space-y-4">
          <div className="pb-2 border-b border-slate-100">
            <h3 className="font-bold text-slate-900 text-base">৯. ডেমো ডাটা ব্যবস্থাপনা ও নিরাপদ রিসেট</h3>
            <p className="text-xs text-slate-500">
              অ্যাপ্লিকেশনের নমুনা ডেমো রেকর্ডগুলো নিরাপদে অপসারণ করুন যাতে বাস্তব ব্যবসায় কোনো বিভ্রান্তি না ঘটে।
            </p>
          </div>

          <div className="p-4 bg-amber-50 border border-amber-200 rounded-xl space-y-2">
            <h4 className="font-bold text-amber-900 text-sm flex items-center gap-2">
              <AlertTriangle className="w-4 h-4 text-amber-600" />
              <span>বর্তমানে সংরক্ষিত ডেমো রেকর্ড ({demoCounts.total} টি)</span>
            </h4>
            <div className="grid grid-cols-2 gap-2 text-xs text-amber-950">
              <span>• পণ্য: <strong>{demoCounts.products}</strong></span>
              <span>• বিক্রয়: <strong>{demoCounts.sales}</strong></span>
              <span>• কাস্টমার: <strong>{demoCounts.customers}</strong></span>
              <span>• খরচ: <strong>{demoCounts.expenses}</strong></span>
              <span>• ঋণ: <strong>{demoCounts.loans}</strong></span>
              <span>• ব্যাংকিং: <strong>{demoCounts.mobileBanking}</strong></span>
              <span>• ফ্লেক্সি: <strong>{demoCounts.flexiload}</strong></span>
              <span>• মেমো: <strong>{demoCounts.memos}</strong></span>
            </div>
          </div>

          <div className="p-4 bg-emerald-50 border border-emerald-200 rounded-xl text-xs text-emerald-900">
            <p className="font-bold flex items-center gap-1.5">
              <CheckCircle2 className="w-4 h-4 text-emerald-600" />
              <span>নিরাপত্তা প্রতিশ্রুতি:</span>
            </p>
            <p className="mt-1">
              এই অপশনটি শুধুমাত্র ইনস্টলেশন নমুনার রেকর্ডগুলো সরাবে। আপনার আসল স্টক, খাতা ও আর্থিক রেকর্ড
              সম্পূর্ণরূপে অক্ষত থাকবে।
            </p>
          </div>

          <button
            type="button"
            onClick={() => setShowDemoResetModal(true)}
            className="w-full py-3 bg-red-600 hover:bg-red-700 text-white font-extrabold rounded-xl shadow-md transition flex items-center justify-center gap-2 text-sm"
          >
            <RotateCcw className="w-4 h-4" />
            <span>৩-ধাপে ডেমো ডাটা রিসেট শুরু করুন</span>
          </button>
        </div>
      )}

      {/* TAB 10: BACKUP AND RESTORE */}
      {activeTab === 'BACKUP_RESTORE' && (
        <div className="bg-white rounded-2xl p-5 border border-slate-200 shadow-2xs space-y-4">
          <div className="pb-2 border-b border-slate-100">
            <h3 className="font-bold text-slate-900 text-base">১০. সম্পূর্ণ ডাটা ব্যাকআপ ও রিস্টোর</h3>
            <p className="text-xs text-slate-500">আপনার সমস্ত ব্যবসায়িক ডাটা এক ক্লিকে সংরক্ষণ বা অন্য ডিভাইসে স্থানান্তর করুন।</p>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 pt-2">
            <button
              type="button"
              onClick={handleExport}
              className="p-4 rounded-xl bg-blue-50 border border-blue-200 text-blue-900 hover:bg-blue-100 transition text-left space-y-1"
            >
              <Download className="w-6 h-6 text-blue-600 mb-2" />
              <h4 className="font-bold text-sm">ব্যাকআপ ডাউনলোড (JSON)</h4>
              <p className="text-xs text-slate-500">স্টক, বিক্রয়, খরচ, বাকি খাতা ও সেটিংসের ব্যাকআপ ফাইল ডাউনলোড করুন।</p>
            </button>

            <label className="p-4 rounded-xl bg-slate-50 border border-slate-200 text-slate-900 hover:bg-slate-100 transition text-left space-y-1 cursor-pointer block">
              <Upload className="w-6 h-6 text-slate-700 mb-2" />
              <h4 className="font-bold text-sm">ব্যাকআপ ফাইল রিস্টোর</h4>
              <p className="text-xs text-slate-500">পূর্বের সেভ করা .json ব্যাকআপ ফাইল আপলোড করে সমস্ত ডাটা ফিরিয়ে আনুন।</p>
              <input type="file" accept=".json" onChange={handleFileImport} className="hidden" />
            </label>
          </div>
        </div>
      )}

      {/* TAB 11: DATA MANAGEMENT */}
      {activeTab === 'DATA_MANAGEMENT' && (
        <div className="bg-white rounded-2xl p-5 border border-slate-200 shadow-2xs space-y-4">
          <div className="pb-2 border-b border-slate-100">
            <h3 className="font-bold text-slate-900 text-base">১১. ডাটা পরিসংখ্যান ও সিস্টেম নিয়ন্ত্রণ</h3>
            <p className="text-xs text-slate-500">মোট ডেটাবেজ রেকর্ড ও ওয়াইপ কন্ট্রোল</p>
          </div>

          <div className="grid grid-cols-2 sm:grid-cols-4 gap-2 text-xs">
            <div className="p-3 bg-slate-50 border rounded-xl text-center">
              <span className="text-slate-400 block">মোট পণ্য</span>
              <span className="text-lg font-black text-slate-900">{products.length} টি</span>
            </div>
            <div className="p-3 bg-slate-50 border rounded-xl text-center">
              <span className="text-slate-400 block">মোট বিক্রয়</span>
              <span className="text-lg font-black text-slate-900">{sales.length} টি</span>
            </div>
            <div className="p-3 bg-slate-50 border rounded-xl text-center">
              <span className="text-slate-400 block">মোট কাস্টমার</span>
              <span className="text-lg font-black text-slate-900">{customers.length} জন</span>
            </div>
            <div className="p-3 bg-slate-50 border rounded-xl text-center">
              <span className="text-slate-400 block">মোট মেমো</span>
              <span className="text-lg font-black text-slate-900">{cashMemos.length} টি</span>
            </div>
          </div>

          <div className="p-4 bg-red-50 border border-red-200 rounded-xl space-y-2 mt-4">
            <h4 className="font-bold text-red-900 text-sm">সম্পূর্ণ ডাটাবেজ ফ্যাক্টরি রিসেট</h4>
            <p className="text-xs text-red-800">
              সতর্কবার্তা: এটি সমস্ত ডাটা মুছে প্রাথমিক ইনস্টলেশন অবস্থায় ফেরত নিয়ে যাবে।
            </p>
            <button
              type="button"
              onClick={() => {
                setConfirmDeleteModal({
                  type: 'সম্পূর্ণ ডাটাবেজ',
                  id: 'all-data',
                  title: 'সমস্ত পণ্য, স্টক, মেমো, বিক্রয় ও বাকির খাতা মুছে প্রাথমিক অবস্থায় ফিরে যাবে',
                  onConfirm: () => {
                    resetAllData();
                  },
                });
              }}
              className="px-4 py-2 bg-red-600 hover:bg-red-700 text-white font-bold rounded-xl shadow text-xs cursor-pointer active:scale-95 transition"
            >
              সম্পূর্ণ ডাটা রিসেট করুন
            </button>
          </div>
        </div>
      )}

      {/* TAB 12: ADMIN SECURITY SETTINGS */}
      {activeTab === 'SECURITY' && (
        <div className="bg-white rounded-2xl p-5 border border-slate-200 shadow-2xs space-y-4">
          <div className="pb-2 border-b border-slate-100">
            <h3 className="font-bold text-slate-900 text-base">১২. অ্যাডমিন সিকিউরিটি ও পিন পরিবর্তন</h3>
            <p className="text-xs text-slate-500">আপনার অ্যাডমিন পিন সর্বদা গোপন রাখুন।</p>
          </div>

          <form onSubmit={handleChangePin} className="max-w-sm space-y-3 pt-2">
            <div>
              <label className="block font-semibold mb-1">বর্তমান পিন *</label>
              <input
                type="password"
                required
                maxLength={6}
                value={currentPinInput}
                onChange={(e) => setCurrentPinInput(e.target.value)}
                placeholder="••••"
                className="w-full px-3 py-2 border rounded-xl text-center text-lg font-bold tracking-widest"
              />
            </div>

            <div>
              <label className="block font-semibold mb-1">নতুন পিন *</label>
              <input
                type="password"
                required
                maxLength={6}
                value={newPinInput}
                onChange={(e) => setNewPinInput(e.target.value)}
                placeholder="নতুন ৪-ডিজিট পিন"
                className="w-full px-3 py-2 border rounded-xl text-center text-lg font-bold tracking-widest"
              />
            </div>

            <div>
              <label className="block font-semibold mb-1">নতুন পিন পুনরায় লিখুন *</label>
              <input
                type="password"
                required
                maxLength={6}
                value={confirmPinInput}
                onChange={(e) => setConfirmPinInput(e.target.value)}
                placeholder="পুনরায় লিখুন"
                className="w-full px-3 py-2 border rounded-xl text-center text-lg font-bold tracking-widest"
              />
            </div>

            <button
              type="submit"
              className="w-full py-2.5 bg-[#0A2540] hover:bg-slate-900 text-white font-bold rounded-xl shadow transition"
            >
              পিন পরিবর্তন সংরক্ষণ করুন
            </button>
          </form>
        </div>
      )}

      {/* Confirmation Modal for Permanent Deletion */}
      {confirmDeleteModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-xs">
          <div className="bg-white rounded-2xl max-w-sm w-full p-5 shadow-2xl text-center">
            <div className="w-12 h-12 rounded-full bg-red-100 text-red-600 flex items-center justify-center mx-auto mb-3">
              <Trash2 className="w-6 h-6" />
            </div>
            <h3 className="font-bold text-slate-900 text-base">
              {confirmDeleteModal.type} মুছে ফেলতে চান?
            </h3>
            <p className="text-xs text-slate-600 mt-1.5 font-medium">
              &quot;{confirmDeleteModal.title}&quot; স্থায়ীভাবে মুছে যাবে।
            </p>
            <div className="flex items-center justify-center gap-3 mt-5">
              <button
                type="button"
                onClick={() => setConfirmDeleteModal(null)}
                className="px-4 py-2 border border-slate-200 rounded-xl text-xs font-semibold text-slate-600 hover:bg-slate-50"
              >
                বাতিল
              </button>
              <button
                type="button"
                onClick={() => {
                  confirmDeleteModal.onConfirm();
                  setConfirmDeleteModal(null);
                }}
                className="px-4 py-2 bg-red-600 hover:bg-red-700 text-white rounded-xl text-xs font-bold shadow"
              >
                হ্যাঁ, নিশ্চিত মুছুন
              </button>
            </div>
          </div>
        </div>
      )}

      {/* 3-Step Demo Data Reset Modal */}
      <ResetDemoDataModal
        isOpen={showDemoResetModal}
        onClose={() => setShowDemoResetModal(false)}
      />
    </div>
  );
};
