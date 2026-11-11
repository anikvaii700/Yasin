import React, { useState } from 'react';
import {
  ShoppingCart,
  Plus,
  Search,
  Calendar,
  User,
  Phone,
  Trash2,
  DollarSign,
  TrendingUp,
  Receipt,
  X,
  PlusCircle,
  Check,
} from 'lucide-react';
import { useShop } from '../context/ShopContext';
import { Sale, SaleItem } from '../types';
import { formatTaka, getTodayStr, generateInvoiceNumber } from '../utils/formatters';

interface SalesScreenProps {
  openRecordSaleTrigger?: boolean;
  onResetRecordSaleTrigger?: () => void;
}

export const SalesScreen: React.FC<SalesScreenProps> = ({
  openRecordSaleTrigger = false,
  onResetRecordSaleTrigger = () => {},
}) => {
  const { sales, products, todayDateStr, recordSale, deleteSale } = useShop();

  const [searchTerm, setSearchTerm] = useState('');
  const [filterTodayOnly, setFilterTodayOnly] = useState(false);
  const [showAddModal, setShowAddModal] = useState(openRecordSaleTrigger);
  const [deleteConfirmSale, setDeleteConfirmSale] = useState<Sale | null>(null);

  // New Sale Form
  const [customerName, setCustomerName] = useState('');
  const [customerPhone, setCustomerPhone] = useState('');
  const [saleDate, setSaleDate] = useState(todayDateStr);
  const [saleNotes, setSaleNotes] = useState('');

  // Items in current sale
  const [cartItems, setCartItems] = useState<SaleItem[]>([]);
  const [selectedProductId, setSelectedProductId] = useState('');
  const [itemQty, setItemQty] = useState('1');
  const [itemPrice, setItemPrice] = useState('');
  const [discountAmount, setDiscountAmount] = useState('0');
  const [paidAmount, setPaidAmount] = useState('');

  React.useEffect(() => {
    if (openRecordSaleTrigger) {
      resetForm();
      setShowAddModal(true);
      onResetRecordSaleTrigger();
    }
  }, [openRecordSaleTrigger, onResetRecordSaleTrigger]);

  const resetForm = () => {
    setCustomerName('');
    setCustomerPhone('');
    setSaleDate(todayDateStr);
    setSaleNotes('');
    setCartItems([]);
    setSelectedProductId('');
    setItemQty('1');
    setItemPrice('');
    setDiscountAmount('0');
    setPaidAmount('');
  };

  const handleProductSelect = (prodId: string) => {
    setSelectedProductId(prodId);
    const p = products.find((prod) => prod.id === prodId);
    if (p) {
      setItemPrice(p.sellingPrice.toString());
    }
  };

  const handleAddCartItem = () => {
    const p = products.find((prod) => prod.id === selectedProductId);
    if (!p) return;

    const qty = parseInt(itemQty, 10) || 1;
    const rate = parseFloat(itemPrice) || p.sellingPrice;
    const cost = p.purchasePrice;
    const subtotal = rate * qty;
    const profit = (rate - cost) * qty;

    const existingIdx = cartItems.findIndex((it) => it.productId === p.id);
    if (existingIdx >= 0) {
      const updated = [...cartItems];
      const newQty = updated[existingIdx].quantity + qty;
      updated[existingIdx] = {
        ...updated[existingIdx],
        quantity: newQty,
        sellingPrice: rate,
        subtotal: rate * newQty,
        profit: (rate - cost) * newQty,
      };
      setCartItems(updated);
    } else {
      setCartItems([
        ...cartItems,
        {
          productId: p.id,
          productName: p.name,
          quantity: qty,
          sellingPrice: rate,
          purchaseCost: cost,
          subtotal,
          profit,
        },
      ]);
    }

    setSelectedProductId('');
    setItemQty('1');
    setItemPrice('');
  };

  const handleRemoveCartItem = (idx: number) => {
    setCartItems(cartItems.filter((_, i) => i !== idx));
  };

  // Calculations
  const rawSubtotal = cartItems.reduce((acc, it) => acc + it.subtotal, 0);
  const totalCost = cartItems.reduce((acc, it) => acc + it.purchaseCost * it.quantity, 0);
  const discount = parseFloat(discountAmount) || 0;
  const grandTotal = Math.max(0, rawSubtotal - discount);
  const paid = paidAmount === '' ? grandTotal : parseFloat(paidAmount) || 0;
  const due = Math.max(0, grandTotal - paid);
  const grossProfit = grandTotal - totalCost;

  const handleSubmitSale = (e: React.FormEvent) => {
    e.preventDefault();
    if (cartItems.length === 0) return;

    const itemsSummary = cartItems
      .map((it) => `${it.productName} x${it.quantity}`)
      .join(', ');

    recordSale(
      {
        invoiceNumber: generateInvoiceNumber(),
        customerName: customerName.trim() || 'নগদ ক্রেতা',
        customerPhone: customerPhone.trim(),
        itemsSummary,
        totalAmount: grandTotal,
        totalCost,
        grossProfit,
        paidAmount: paid,
        dueAmount: due,
        saleDate: saleDate || todayDateStr,
        notes: saleNotes.trim(),
        isDemo: false,
      },
      cartItems
    );

    setShowAddModal(false);
    resetForm();
  };

  // Filtered sales
  const filteredSales = sales.filter((s) => {
    const matchesSearch =
      s.invoiceNumber.toLowerCase().includes(searchTerm.toLowerCase()) ||
      s.customerName.toLowerCase().includes(searchTerm.toLowerCase()) ||
      s.customerPhone.includes(searchTerm) ||
      s.itemsSummary.toLowerCase().includes(searchTerm.toLowerCase());

    const matchesDate = !filterTodayOnly || s.saleDate === todayDateStr;
    return matchesSearch && matchesDate;
  });

  const totalFilteredSalesAmount = filteredSales.reduce((acc, s) => acc + s.totalAmount, 0);
  const totalFilteredProfit = filteredSales.reduce((acc, s) => acc + s.grossProfit, 0);

  return (
    <div className="space-y-4 pb-24">
      {/* Top Header */}
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-lg font-bold text-slate-800">বিক্রয় হিসাব (Sales)</h2>
          <p className="text-xs text-slate-500">দৈনিক ও পূর্বের সকল বিক্রয় এবং লাভের তালিকা</p>
        </div>
        <button
          type="button"
          onClick={() => {
            resetForm();
            setShowAddModal(true);
          }}
          className="flex items-center gap-1.5 px-3.5 py-2 bg-[#0A2540] hover:bg-[#071d33] active:scale-95 text-white rounded-xl text-xs font-bold shadow-md transition"
        >
          <ShoppingCart className="w-4 h-4" />
          <span>বিক্রয় রেকর্ড</span>
        </button>
      </div>

      {/* Summary KPI Strip */}
      <div className="grid grid-cols-2 sm:grid-cols-3 gap-2.5">
        <div className="bg-white p-3 rounded-xl border border-slate-200">
          <span className="text-[10px] text-slate-400 font-semibold block">মোট বিক্রয়</span>
          <span className="text-base font-black text-slate-900">{formatTaka(totalFilteredSalesAmount)}</span>
        </div>
        <div className="bg-white p-3 rounded-xl border border-slate-200">
          <span className="text-[10px] text-slate-400 font-semibold block">মোট মুনাফা</span>
          <span className="text-base font-black text-emerald-600">+{formatTaka(totalFilteredProfit)}</span>
        </div>
        <div className="bg-white p-3 rounded-xl border border-slate-200 col-span-2 sm:col-span-1">
          <span className="text-[10px] text-slate-400 font-semibold block">মোট ইনভয়েস</span>
          <span className="text-base font-black text-blue-600">{filteredSales.length} টি</span>
        </div>
      </div>

      {/* Search and Date filter */}
      <div className="flex items-center gap-2">
        <div className="relative flex-1">
          <Search className="w-4 h-4 absolute left-3 top-3 text-slate-400" />
          <input
            type="text"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            placeholder="ইনভয়েস নং, ক্রেতার নাম বা ফোন..."
            className="w-full pl-9 pr-3 py-2 bg-white border border-slate-200 rounded-xl text-xs sm:text-sm focus:outline-none focus:ring-2 focus:ring-[#0A2540]"
          />
        </div>
        <button
          type="button"
          onClick={() => setFilterTodayOnly(!filterTodayOnly)}
          className={`px-3 py-2 rounded-xl text-xs font-bold shrink-0 transition flex items-center gap-1 ${
            filterTodayOnly
              ? 'bg-blue-600 text-white shadow-xs'
              : 'bg-white text-slate-600 border border-slate-200 hover:bg-slate-50'
          }`}
        >
          <Calendar className="w-3.5 h-3.5" />
          <span>আজকের ({sales.filter((s) => s.saleDate === todayDateStr).length})</span>
        </button>
      </div>

      {/* Sales List */}
      <div className="space-y-3">
        {filteredSales.length === 0 ? (
          <div className="bg-white rounded-2xl p-8 text-center border border-slate-200 text-slate-500">
            <Receipt className="w-10 h-10 mx-auto text-slate-300 mb-2" />
            <p className="font-semibold text-sm">কোনো বিক্রয় রেকর্ড নেই!</p>
            <p className="text-xs text-slate-400 mt-1">নতুন বিক্রয় রেকর্ড করতে উপরের বোতাম চাপুন</p>
          </div>
        ) : (
          filteredSales.map((s) => (
            <div
              key={s.id}
              className="bg-white rounded-2xl p-4 border border-slate-200/90 shadow-2xs hover:shadow-xs transition"
            >
              <div className="flex items-start justify-between gap-2">
                <div>
                  <div className="flex items-center gap-2">
                    <span className="text-[10px] font-bold px-2 py-0.5 rounded-full bg-blue-50 text-blue-700 border border-blue-200">
                      {s.invoiceNumber}
                    </span>
                    <span className="text-xs text-slate-400">{s.saleDate}</span>
                  </div>
                  <h4 className="font-bold text-slate-900 text-sm mt-1">
                    {s.customerName || 'নগদ ক্রেতা'}
                    {s.customerPhone && (
                      <span className="text-xs font-normal text-slate-500 ml-1.5">({s.customerPhone})</span>
                    )}
                  </h4>
                </div>

                <div className="text-right">
                  <span className="text-base font-black text-slate-900 block">{formatTaka(s.totalAmount)}</span>
                  <span className="text-[10px] font-semibold text-emerald-600">
                    লাভ: +{formatTaka(s.grossProfit)}
                  </span>
                </div>
              </div>

              {/* Items summary */}
              <div className="mt-2.5 p-2 bg-slate-50 rounded-xl text-xs text-slate-600 border border-slate-100">
                <span className="font-semibold text-slate-700">পণ্য: </span>
                {s.itemsSummary}
              </div>

              {/* Paid and Due breakdown */}
              <div className="flex items-center justify-between mt-3 pt-2.5 border-t border-slate-100 text-xs">
                <div className="flex items-center gap-3">
                  <span className="text-slate-500">
                    জমা: <strong className="text-emerald-700 font-bold">{formatTaka(s.paidAmount)}</strong>
                  </span>
                  {s.dueAmount > 0 && (
                    <span className="text-red-600 font-bold bg-red-50 px-2 py-0.5 rounded-md border border-red-200">
                      বাকি: {formatTaka(s.dueAmount)}
                    </span>
                  )}
                </div>

                <span className="text-[11px] font-bold text-slate-500 bg-slate-100 px-2 py-0.5 rounded-full">
                  {s.invoiceNumber}
                </span>
              </div>
            </div>
          ))
        )}
      </div>

      {/* Record Sale Modal */}
      {showAddModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-xs">
          <div className="bg-white rounded-2xl max-w-lg w-full max-h-[92vh] overflow-y-auto shadow-2xl p-5">
            <div className="flex items-center justify-between pb-3 border-b border-slate-100">
              <h3 className="text-base font-bold text-slate-800 flex items-center gap-2">
                <ShoppingCart className="w-5 h-5 text-emerald-600" />
                <span>নতুন বিক্রয় রেকর্ড করুন</span>
              </h3>
              <button
                type="button"
                onClick={() => setShowAddModal(false)}
                className="text-slate-400 hover:text-slate-600 p-1 rounded-full"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            <form onSubmit={handleSubmitSale} className="space-y-4 mt-4 text-xs sm:text-sm">
              {/* Customer info */}
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block font-semibold text-slate-700 mb-1">ক্রেতার নাম</label>
                  <input
                    type="text"
                    value={customerName}
                    onChange={(e) => setCustomerName(e.target.value)}
                    placeholder="নগদ ক্রেতা / নাম"
                    className="w-full px-3 py-2 border border-slate-200 rounded-xl focus:ring-2 focus:ring-[#0A2540] focus:outline-none"
                  />
                </div>
                <div>
                  <label className="block font-semibold text-slate-700 mb-1">মোবাইল নম্বর</label>
                  <input
                    type="text"
                    value={customerPhone}
                    onChange={(e) => setCustomerPhone(e.target.value)}
                    placeholder="017xxxxxxxx"
                    className="w-full px-3 py-2 border border-slate-200 rounded-xl focus:ring-2 focus:ring-[#0A2540] focus:outline-none"
                  />
                </div>
              </div>

              {/* Product selector to add to cart */}
              <div className="p-3 bg-slate-50 rounded-xl border border-slate-200 space-y-2.5">
                <label className="block font-bold text-slate-800">পণ্যের তালিকা থেকে নির্বাচন করুন</label>
                <div className="grid grid-cols-1 sm:grid-cols-3 gap-2">
                  <div className="sm:col-span-2">
                    <select
                      value={selectedProductId}
                      onChange={(e) => handleProductSelect(e.target.value)}
                      className="w-full px-2.5 py-2 border border-slate-200 rounded-xl bg-white text-xs font-medium focus:outline-none focus:ring-2 focus:ring-emerald-500"
                    >
                      <option value="">-- পণ্য নির্বাচন করুন --</option>
                      {products.map((p) => (
                        <option key={p.id} value={p.id} disabled={p.currentStock <= 0}>
                          {p.name} (মজুদ: {p.currentStock}) - {formatTaka(p.sellingPrice)}
                        </option>
                      ))}
                    </select>
                  </div>
                  <div className="flex gap-2">
                    <input
                      type="number"
                      min="1"
                      value={itemQty}
                      onChange={(e) => setItemQty(e.target.value)}
                      placeholder="পরিমাণ"
                      className="w-16 px-2 py-2 border border-slate-200 rounded-xl bg-white text-center font-bold text-xs"
                      title="পরিমাণ"
                    />
                    <input
                      type="number"
                      step="any"
                      value={itemPrice}
                      onChange={(e) => setItemPrice(e.target.value)}
                      placeholder="দর"
                      className="w-20 px-2 py-2 border border-slate-200 rounded-xl bg-white text-center font-bold text-xs"
                      title="বিক্রয় দর"
                    />
                    <button
                      type="button"
                      disabled={!selectedProductId}
                      onClick={handleAddCartItem}
                      className="flex-1 px-3 py-2 bg-emerald-600 disabled:bg-slate-300 text-white font-bold rounded-xl text-xs hover:bg-emerald-700"
                    >
                      যোগ
                    </button>
                  </div>
                </div>
              </div>

              {/* Cart Table */}
              <div>
                <label className="block font-semibold text-slate-700 mb-1">
                  নির্বাচিত পণ্যসমূহ ({cartItems.length})
                </label>
                {cartItems.length === 0 ? (
                  <div className="p-4 text-center border border-dashed border-slate-200 rounded-xl text-xs text-slate-400">
                    কোনো পণ্য যোগ করা হয়নি। উপরের ড্রপডাউন থেকে পণ্য নির্বাচন করে &quot;যোগ&quot; চাপুন।
                  </div>
                ) : (
                  <div className="divide-y divide-slate-100 border border-slate-200 rounded-xl overflow-hidden">
                    {cartItems.map((item, idx) => (
                      <div key={idx} className="flex items-center justify-between p-2.5 text-xs bg-white">
                        <div className="flex-1 pr-2">
                          <p className="font-semibold text-slate-800">{item.productName}</p>
                          <p className="text-slate-400 text-[11px]">
                            {formatTaka(item.sellingPrice)} × {item.quantity} পিস
                          </p>
                        </div>
                        <div className="flex items-center gap-3">
                          <span className="font-extrabold text-slate-900">{formatTaka(item.subtotal)}</span>
                          <button
                            type="button"
                            onClick={() => handleRemoveCartItem(idx)}
                            className="text-red-500 hover:text-red-700 p-1"
                          >
                            <X className="w-4 h-4" />
                          </button>
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </div>

              {/* Calculation & Payment */}
              <div className="p-3 bg-slate-50 rounded-xl border border-slate-200 space-y-2 text-xs">
                <div className="flex items-center justify-between">
                  <span className="text-slate-600">উপমোট (Subtotal):</span>
                  <span className="font-bold text-slate-800">{formatTaka(rawSubtotal)}</span>
                </div>

                <div className="flex items-center justify-between">
                  <span className="text-slate-600">ছাড় / ডিসকাউন্ট (৳):</span>
                  <input
                    type="number"
                    min="0"
                    value={discountAmount}
                    onChange={(e) => setDiscountAmount(e.target.value)}
                    className="w-24 px-2 py-1 text-right font-bold border border-slate-200 rounded-lg bg-white"
                  />
                </div>

                <div className="flex items-center justify-between text-sm font-black border-t border-slate-200 pt-2 text-slate-900">
                  <span>সর্বমোট প্রদেয়:</span>
                  <span className="text-emerald-700">{formatTaka(grandTotal)}</span>
                </div>

                <div className="flex items-center justify-between pt-1">
                  <span className="text-slate-700 font-semibold">জমা পরিশোধ (৳):</span>
                  <input
                    type="number"
                    min="0"
                    step="any"
                    value={paidAmount}
                    onChange={(e) => setPaidAmount(e.target.value)}
                    placeholder={grandTotal.toString()}
                    className="w-28 px-2 py-1 text-right font-black border border-slate-200 rounded-lg bg-white text-emerald-600"
                  />
                </div>

                {due > 0 && (
                  <div className="flex items-center justify-between text-red-600 font-bold pt-1">
                    <span>বাকি থাকবে (Due):</span>
                    <span>{formatTaka(due)}</span>
                  </div>
                )}
              </div>

              <div>
                <label className="block font-semibold text-slate-700 mb-1">মন্তব্য (ঐচ্ছিক)</label>
                <input
                  type="text"
                  value={saleNotes}
                  onChange={(e) => setSaleNotes(e.target.value)}
                  placeholder="বিক্রয় সংক্রান্ত নোট..."
                  className="w-full px-3 py-2 border border-slate-200 rounded-xl focus:ring-2 focus:ring-[#0A2540] focus:outline-none"
                />
              </div>

              <div className="flex items-center justify-end gap-2 pt-3 border-t border-slate-100">
                <button
                  type="button"
                  onClick={() => setShowAddModal(false)}
                  className="px-4 py-2 border border-slate-200 rounded-xl font-semibold text-slate-600 hover:bg-slate-50 transition"
                >
                  বাতিল
                </button>
                <button
                  type="submit"
                  disabled={cartItems.length === 0}
                  className="px-5 py-2 bg-[#0A2540] disabled:bg-slate-300 hover:bg-[#071d33] text-white rounded-xl font-bold shadow-md transition"
                >
                  বিক্রয় সম্পন্ন করুন
                </button>
              </div>
            </form>
          </div>
        </div>
      )}


    </div>
  );
};
