import React, { useState } from 'react';
import {
  Wallet,
  Plus,
  Search,
  User,
  Phone,
  MapPin,
  Trash2,
  Calendar,
  History,
  ArrowDownLeft,
  ArrowUpRight,
  X,
  Check,
  CreditCard,
  PlusCircle,
} from 'lucide-react';
import { useShop } from '../context/ShopContext';
import { Customer, CustomerTransaction } from '../types';
import { formatTaka, getTodayStr } from '../utils/formatters';

interface DueScreenProps {
  openAddDueTrigger?: boolean;
  onResetAddDueTrigger?: () => void;
}

export const DueScreen: React.FC<DueScreenProps> = ({
  openAddDueTrigger = false,
  onResetAddDueTrigger = () => {},
}) => {
  const {
    customers,
    customerTransactions,
    todayDateStr,
    addCustomer,
    recordCustomerTx,
    deleteCustomer,
  } = useShop();

  const [searchTerm, setSearchTerm] = useState('');
  const [filterDueOnly, setFilterDueOnly] = useState(false);

  // Modals
  const [showAddCustomerModal, setShowAddCustomerModal] = useState(false);
  const [activeTxCustomer, setActiveTxCustomer] = useState<{
    customer: Customer;
    type: 'DUE' | 'PAYMENT';
  } | null>(null);
  const [viewLedgerCustomer, setViewLedgerCustomer] = useState<Customer | null>(null);
  const [deleteConfirmCustomer, setDeleteConfirmCustomer] = useState<Customer | null>(null);

  // Add customer form
  const [custName, setCustName] = useState('');
  const [custMobile, setCustMobile] = useState('');
  const [custAddress, setCustAddress] = useState('');
  const [initialDue, setInitialDue] = useState('');

  // Transaction form
  const [txAmount, setTxAmount] = useState('');
  const [txNotes, setTxNotes] = useState('');
  const [txDate, setTxDate] = useState(todayDateStr);

  React.useEffect(() => {
    if (openAddDueTrigger) {
      setShowAddCustomerModal(true);
      onResetAddDueTrigger();
    }
  }, [openAddDueTrigger, onResetAddDueTrigger]);

  const handleSaveCustomer = (e: React.FormEvent) => {
    e.preventDefault();
    if (!custName.trim()) return;

    const dueVal = parseFloat(initialDue) || 0;
    const newCustId = addCustomer({
      name: custName.trim(),
      mobile: custMobile.trim(),
      address: custAddress.trim(),
      totalDue: dueVal,
      totalPaid: 0,
      remainingDue: dueVal,
      isDemo: false,
    });

    if (dueVal > 0) {
      recordCustomerTx(newCustId, {
        type: 'DUE',
        amount: dueVal,
        notes: 'পূর্বের বা প্রাথমিক বকেয়া',
        transactionDate: todayDateStr,
        isDemo: false,
      });
    }

    setCustName('');
    setCustMobile('');
    setCustAddress('');
    setInitialDue('');
    setShowAddCustomerModal(false);
  };

  const handleSaveTransaction = (e: React.FormEvent) => {
    e.preventDefault();
    if (!activeTxCustomer) return;

    const amt = parseFloat(txAmount) || 0;
    if (amt <= 0) return;

    recordCustomerTx(activeTxCustomer.customer.id, {
      type: activeTxCustomer.type,
      amount: amt,
      notes: txNotes.trim() || (activeTxCustomer.type === 'DUE' ? 'নতুন বাকি' : 'নগদ জমা'),
      transactionDate: txDate || todayDateStr,
      isDemo: false,
    });

    setActiveTxCustomer(null);
    setTxAmount('');
    setTxNotes('');
  };

  // Calculations
  const totalCustomerRemainingDue = customers.reduce((acc, c) => acc + c.remainingDue, 0);
  const totalCustomerPaid = customers.reduce((acc, c) => acc + c.totalPaid, 0);
  const totalCustomerGrossDue = customers.reduce((acc, c) => acc + c.totalDue, 0);

  const filteredCustomers = customers.filter((c) => {
    const matchesSearch =
      c.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
      c.mobile.includes(searchTerm) ||
      c.address.toLowerCase().includes(searchTerm.toLowerCase());

    const matchesDue = !filterDueOnly || c.remainingDue > 0;
    return matchesSearch && matchesDue;
  });

  return (
    <div className="space-y-4 pb-24">
      {/* Top Header */}
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-lg font-bold text-slate-800">বাকি খাতা (Customer Due)</h2>
          <p className="text-xs text-slate-500">গ্রাহকদের বাকি ও পেমেন্ট জমার সার্বিক হিসাব</p>
        </div>
        <button
          type="button"
          onClick={() => setShowAddCustomerModal(true)}
          className="flex items-center gap-1.5 px-3.5 py-2 bg-sky-600 hover:bg-sky-700 active:scale-95 text-white rounded-xl text-xs font-bold shadow-md transition"
        >
          <Plus className="w-4 h-4" />
          <span>নতুন গ্রাহক</span>
        </button>
      </div>

      {/* KPI Cards Strip */}
      <div className="grid grid-cols-3 gap-2.5">
        <div className="bg-red-50 p-3 rounded-xl border border-red-200">
          <span className="text-[10px] text-red-600 font-bold block">মোট পাওনা বকেয়া</span>
          <span className="text-base sm:text-lg font-black text-red-700">
            {formatTaka(totalCustomerRemainingDue)}
          </span>
        </div>
        <div className="bg-emerald-50 p-3 rounded-xl border border-emerald-200">
          <span className="text-[10px] text-emerald-700 font-bold block">মোট আদায় হয়েছে</span>
          <span className="text-base sm:text-lg font-black text-emerald-700">
            {formatTaka(totalCustomerPaid)}
          </span>
        </div>
        <div className="bg-white p-3 rounded-xl border border-slate-200">
          <span className="text-[10px] text-slate-500 font-bold block">বাকিদার গ্রাহক</span>
          <span className="text-base sm:text-lg font-black text-slate-900">
            {customers.filter((c) => c.remainingDue > 0).length} জন
          </span>
        </div>
      </div>

      {/* Search & Due-only toggle */}
      <div className="flex items-center gap-2">
        <div className="relative flex-1">
          <Search className="w-4 h-4 absolute left-3 top-3 text-slate-400" />
          <input
            type="text"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            placeholder="গ্রাহকের নাম, মোবাইল বা ঠিকানা খুঁজুন..."
            className="w-full pl-9 pr-3 py-2 bg-white border border-slate-200 rounded-xl text-xs sm:text-sm focus:outline-none focus:ring-2 focus:ring-sky-500"
          />
        </div>
        <button
          type="button"
          onClick={() => setFilterDueOnly(!filterDueOnly)}
          className={`px-3 py-2 rounded-xl text-xs font-bold shrink-0 transition ${
            filterDueOnly
              ? 'bg-red-600 text-white shadow-xs'
              : 'bg-white text-slate-600 border border-slate-200'
          }`}
        >
          শুধু বাকিদার
        </button>
      </div>

      {/* Customers List */}
      <div className="space-y-3">
        {filteredCustomers.length === 0 ? (
          <div className="bg-white rounded-2xl p-8 text-center border border-slate-200 text-slate-500">
            <Wallet className="w-10 h-10 mx-auto text-slate-300 mb-2" />
            <p className="font-semibold text-sm">কোনো গ্রাহক পাওয়া যায়নি!</p>
          </div>
        ) : (
          filteredCustomers.map((c) => (
            <div
              key={c.id}
              className="bg-white rounded-2xl p-4 border border-slate-200 shadow-2xs hover:shadow-xs transition"
            >
              <div className="flex items-start justify-between">
                <div>
                  <h3 className="font-bold text-slate-900 text-base">{c.name}</h3>
                  <div className="flex flex-wrap items-center gap-x-3 text-xs text-slate-500 mt-0.5">
                    {c.mobile && <span className="flex items-center gap-1">📞 {c.mobile}</span>}
                    {c.address && <span className="flex items-center gap-1">📍 {c.address}</span>}
                  </div>
                </div>

                <div className="text-right">
                  <span className="text-[10px] text-slate-400 font-semibold block">অবশিষ্ট বাকি</span>
                  <span
                    className={`text-base font-black ${
                      c.remainingDue > 0 ? 'text-red-600' : 'text-emerald-600'
                    }`}
                  >
                    {formatTaka(c.remainingDue)}
                  </span>
                </div>
              </div>

              {/* Due / Paid Progress bar */}
              <div className="mt-3 pt-2.5 border-t border-slate-100 flex items-center justify-between text-xs text-slate-600">
                <span>মোট বাকি: <strong className="text-slate-800">{formatTaka(c.totalDue)}</strong></span>
                <span>জমা: <strong className="text-emerald-700">{formatTaka(c.totalPaid)}</strong></span>
              </div>

              {/* Action Buttons */}
              <div className="flex items-center justify-between gap-2 mt-3 pt-2.5 border-t border-slate-100">
                <div className="flex items-center gap-2">
                  <button
                    type="button"
                    onClick={() => setActiveTxCustomer({ customer: c, type: 'PAYMENT' })}
                    className="flex items-center gap-1 px-3 py-1.5 rounded-lg bg-emerald-50 hover:bg-emerald-100 text-emerald-700 text-xs font-bold transition"
                  >
                    <ArrowDownLeft className="w-3.5 h-3.5" />
                    <span>টাকা জমা</span>
                  </button>

                  <button
                    type="button"
                    onClick={() => setActiveTxCustomer({ customer: c, type: 'DUE' })}
                    className="flex items-center gap-1 px-3 py-1.5 rounded-lg bg-red-50 hover:bg-red-100 text-red-700 text-xs font-bold transition"
                  >
                    <ArrowUpRight className="w-3.5 h-3.5" />
                    <span>বাকি যোগ</span>
                  </button>
                </div>

                <div className="flex items-center gap-1.5">
                  <button
                    type="button"
                    onClick={() => setViewLedgerCustomer(c)}
                    className="px-3 py-1.5 rounded-lg bg-slate-100 hover:bg-slate-200 text-slate-700 text-xs font-semibold flex items-center gap-1"
                    title="লেনদেন খাতা দেখুন"
                  >
                    <History className="w-3.5 h-3.5" />
                    <span>খাতা দেখুন</span>
                  </button>
                </div>
              </div>
            </div>
          ))
        )}
      </div>

      {/* Add Customer Modal */}
      {showAddCustomerModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-xs">
          <div className="bg-white rounded-2xl max-w-sm w-full p-5 shadow-2xl">
            <div className="flex items-center justify-between pb-3 border-b border-slate-100">
              <h3 className="text-base font-bold text-slate-800">নতুন গ্রাহক যোগ করুন</h3>
              <button
                type="button"
                onClick={() => setShowAddCustomerModal(false)}
                className="text-slate-400 hover:text-slate-600 p-1"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            <form onSubmit={handleSaveCustomer} className="space-y-3 mt-4 text-xs sm:text-sm">
              <div>
                <label className="block font-semibold text-slate-700 mb-1">গ্রাহকের নাম *</label>
                <input
                  type="text"
                  required
                  value={custName}
                  onChange={(e) => setCustName(e.target.value)}
                  placeholder="যেমন: করিম উল্লাহ"
                  className="w-full px-3 py-2 border border-slate-200 rounded-xl focus:ring-2 focus:ring-sky-500 focus:outline-none"
                />
              </div>

              <div>
                <label className="block font-semibold text-slate-700 mb-1">মোবাইল নম্বর</label>
                <input
                  type="text"
                  value={custMobile}
                  onChange={(e) => setCustMobile(e.target.value)}
                  placeholder="017xxxxxxxx"
                  className="w-full px-3 py-2 border border-slate-200 rounded-xl focus:ring-2 focus:ring-sky-500 focus:outline-none"
                />
              </div>

              <div>
                <label className="block font-semibold text-slate-700 mb-1">ঠিকানা / গ্রাম</label>
                <input
                  type="text"
                  value={custAddress}
                  onChange={(e) => setCustAddress(e.target.value)}
                  placeholder="বুল্লা বাজার, লাখাই"
                  className="w-full px-3 py-2 border border-slate-200 rounded-xl focus:ring-2 focus:ring-sky-500 focus:outline-none"
                />
              </div>

              <div>
                <label className="block font-semibold text-slate-700 mb-1">প্রাথমিক বকেয়া (৳)</label>
                <input
                  type="number"
                  step="any"
                  value={initialDue}
                  onChange={(e) => setInitialDue(e.target.value)}
                  placeholder="0"
                  className="w-full px-3 py-2 border border-slate-200 rounded-xl focus:ring-2 focus:ring-sky-500 focus:outline-none font-bold text-red-600"
                />
              </div>

              <div className="flex items-center justify-end gap-2 pt-3 border-t border-slate-100">
                <button
                  type="button"
                  onClick={() => setShowAddCustomerModal(false)}
                  className="px-4 py-2 border border-slate-200 rounded-xl font-semibold text-slate-600 hover:bg-slate-50"
                >
                  বাতিল
                </button>
                <button
                  type="submit"
                  className="px-5 py-2 bg-sky-600 hover:bg-sky-700 text-white rounded-xl font-bold shadow"
                >
                  সংরক্ষণ করুন
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Record Transaction Modal (Due or Payment) */}
      {activeTxCustomer && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-xs">
          <div className="bg-white rounded-2xl max-w-sm w-full p-5 shadow-2xl">
            <div className="flex items-center justify-between pb-3 border-b border-slate-100">
              <h3 className="text-base font-bold text-slate-800">
                {activeTxCustomer.type === 'DUE' ? 'বাকি যোগ করুন' : 'টাকা জমা নিন'}
              </h3>
              <button
                type="button"
                onClick={() => setActiveTxCustomer(null)}
                className="text-slate-400 hover:text-slate-600 p-1"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            <div className="my-3 p-3 bg-slate-50 rounded-xl border border-slate-200 text-xs">
              <p className="font-bold text-slate-900">{activeTxCustomer.customer.name}</p>
              <p className="text-slate-500 mt-0.5">
                বর্তমান বকেয়া:{' '}
                <span className="font-black text-red-600">
                  {formatTaka(activeTxCustomer.customer.remainingDue)}
                </span>
              </p>
            </div>

            <form onSubmit={handleSaveTransaction} className="space-y-3 text-xs sm:text-sm">
              <div>
                <label className="block font-semibold text-slate-700 mb-1">
                  {activeTxCustomer.type === 'DUE' ? 'বাকির পরিমাণ (৳) *' : 'জমার পরিমাণ (৳) *'}
                </label>
                <input
                  type="number"
                  min="1"
                  step="any"
                  required
                  value={txAmount}
                  onChange={(e) => setTxAmount(e.target.value)}
                  placeholder="500"
                  className={`w-full px-3 py-2 border rounded-xl focus:outline-none font-black text-lg ${
                    activeTxCustomer.type === 'DUE'
                      ? 'border-red-300 focus:ring-2 focus:ring-red-500 text-red-600'
                      : 'border-emerald-300 focus:ring-2 focus:ring-emerald-500 text-emerald-600'
                  }`}
                />
              </div>

              <div>
                <label className="block font-semibold text-slate-700 mb-1">তারিখ</label>
                <input
                  type="text"
                  value={txDate}
                  onChange={(e) => setTxDate(e.target.value)}
                  className="w-full px-3 py-2 border border-slate-200 rounded-xl focus:outline-none text-slate-700 font-semibold"
                />
              </div>

              <div>
                <label className="block font-semibold text-slate-700 mb-1">নোট / বিবরণ</label>
                <input
                  type="text"
                  value={txNotes}
                  onChange={(e) => setTxNotes(e.target.value)}
                  placeholder={activeTxCustomer.type === 'DUE' ? 'যেমন: হেডফোন বাকি ক্রয়' : 'নগদ জমা'}
                  className="w-full px-3 py-2 border border-slate-200 rounded-xl focus:outline-none"
                />
              </div>

              <div className="flex items-center justify-end gap-2 pt-3 border-t border-slate-100">
                <button
                  type="button"
                  onClick={() => setActiveTxCustomer(null)}
                  className="px-4 py-2 border border-slate-200 rounded-xl font-semibold text-slate-600 hover:bg-slate-50"
                >
                  বাতিল
                </button>
                <button
                  type="submit"
                  className={`px-5 py-2 text-white rounded-xl font-bold shadow ${
                    activeTxCustomer.type === 'DUE'
                      ? 'bg-red-600 hover:bg-red-700'
                      : 'bg-emerald-600 hover:bg-emerald-700'
                  }`}
                >
                  নিশ্চিত করুন
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Customer Ledger History Modal */}
      {viewLedgerCustomer && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-xs">
          <div className="bg-white rounded-2xl max-w-md w-full max-h-[85vh] overflow-y-auto p-5 shadow-2xl">
            <div className="flex items-center justify-between pb-3 border-b border-slate-100">
              <div>
                <h3 className="text-base font-bold text-slate-900">{viewLedgerCustomer.name} - খাতা</h3>
                <p className="text-xs text-slate-500">বাকি ও পরিশোধের বিস্তারিত হিস্ট্রি</p>
              </div>
              <button
                type="button"
                onClick={() => setViewLedgerCustomer(null)}
                className="text-slate-400 hover:text-slate-600 p-1"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            <div className="my-3 grid grid-cols-3 gap-2 text-center text-xs">
              <div className="p-2 bg-slate-50 rounded-xl border border-slate-200">
                <span className="text-[10px] text-slate-400 block">মোট বাকি</span>
                <span className="font-bold text-slate-800">{formatTaka(viewLedgerCustomer.totalDue)}</span>
              </div>
              <div className="p-2 bg-emerald-50 rounded-xl border border-emerald-100">
                <span className="text-[10px] text-emerald-600 block">মোট জমা</span>
                <span className="font-bold text-emerald-700">{formatTaka(viewLedgerCustomer.totalPaid)}</span>
              </div>
              <div className="p-2 bg-red-50 rounded-xl border border-red-100">
                <span className="text-[10px] text-red-600 block">অবশিষ্ট বকেয়া</span>
                <span className="font-black text-red-700">{formatTaka(viewLedgerCustomer.remainingDue)}</span>
              </div>
            </div>

            <div className="space-y-2 mt-4">
              <h4 className="text-xs font-bold text-slate-700">লেনদেন বিবরণী</h4>
              {!(customerTransactions[viewLedgerCustomer.id]?.length) ? (
                <p className="text-xs text-slate-400 p-4 text-center border border-dashed rounded-xl">
                  কোনো লেনদেন রেকর্ড নেই
                </p>
              ) : (
                customerTransactions[viewLedgerCustomer.id].map((tx) => (
                  <div
                    key={tx.id}
                    className="p-3 bg-slate-50 border border-slate-200 rounded-xl flex items-center justify-between text-xs"
                  >
                    <div>
                      <div className="flex items-center gap-1.5">
                        <span
                          className={`font-black text-[10px] px-1.5 py-0.5 rounded ${
                            tx.type === 'DUE' ? 'bg-red-100 text-red-700' : 'bg-emerald-100 text-emerald-800'
                          }`}
                        >
                          {tx.type === 'DUE' ? 'বাকি' : 'জমা'}
                        </span>
                        <span className="text-slate-400 text-[11px]">{tx.transactionDate}</span>
                      </div>
                      <p className="font-medium text-slate-800 mt-1">{tx.notes || 'বিবরণ নেই'}</p>
                    </div>

                    <span
                      className={`font-black text-sm ${
                        tx.type === 'DUE' ? 'text-red-600' : 'text-emerald-600'
                      }`}
                    >
                      {tx.type === 'DUE' ? '+' : '-'}{formatTaka(tx.amount)}
                    </span>
                  </div>
                ))
              )}
            </div>

            <div className="mt-4 pt-3 border-t border-slate-100 text-right">
              <button
                type="button"
                onClick={() => setViewLedgerCustomer(null)}
                className="px-4 py-1.5 bg-slate-800 text-white text-xs font-bold rounded-xl"
              >
                বন্ধ করুন
              </button>
            </div>
          </div>
        </div>
      )}


    </div>
  );
};
