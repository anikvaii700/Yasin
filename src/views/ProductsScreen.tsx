import React, { useState } from 'react';
import {
  Plus,
  Search,
  Package,
  Boxes,
  PlusCircle,
  Edit2,
  AlertTriangle,
  X,
  Check,
  History,
  TrendingUp,
  Calendar,
  Layers,
  ArrowRight,
} from 'lucide-react';
import { useShop } from '../context/ShopContext';
import { Product, StockAddition } from '../types';
import { formatTaka, getTodayStr } from '../utils/formatters';

interface ProductsScreenProps {
  openAddProductTrigger?: boolean;
  onResetAddProductTrigger?: () => void;
}

export const ProductsScreen: React.FC<ProductsScreenProps> = ({
  openAddProductTrigger = false,
  onResetAddProductTrigger = () => {},
}) => {
  const {
    products,
    stockAdditions,
    profile,
    todayDateStr,
    getAllCategories,
    addProduct,
    updateProduct,
    addStock,
  } = useShop();

  const [activeTab, setActiveTab] = useState<'PRODUCTS' | 'HISTORY'>('PRODUCTS');
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedCategory, setSelectedCategory] = useState<string>('ALL');
  const [stockFilter, setStockFilter] = useState<'ALL' | 'LOW' | 'OUT'>('ALL');

  // Modals state
  const [showAddModal, setShowAddModal] = useState(openAddProductTrigger);
  const [editingProduct, setEditingProduct] = useState<Product | null>(null);
  const [stockingProduct, setStockingProduct] = useState<Product | null>(null);

  // Form states for Add / Edit
  const [formName, setFormName] = useState('');
  const [formCategory, setFormCategory] = useState('Mobile Chargers');
  const [formBrand, setFormBrand] = useState('');
  const [formModel, setFormModel] = useState('');
  const [formPurchasePrice, setFormPurchasePrice] = useState('');
  const [formSellingPrice, setFormSellingPrice] = useState('');
  const [formInitialAddedQty, setFormInitialAddedQty] = useState('10');
  const [formMinStockAlert, setFormMinStockAlert] = useState('5');
  const [formProductCode, setFormProductCode] = useState('');
  const [formNotes, setFormNotes] = useState('');

  // Form state for Smart Stock Addition
  const [addQty, setAddQty] = useState('10');
  const [addPurchaseRate, setAddPurchaseRate] = useState('');
  const [addSupplier, setAddSupplier] = useState('');
  const [addStockNotes, setAddStockNotes] = useState('');

  React.useEffect(() => {
    if (openAddProductTrigger) {
      resetAddForm();
      setShowAddModal(true);
      onResetAddProductTrigger();
    }
  }, [openAddProductTrigger, onResetAddProductTrigger]);

  const resetAddForm = () => {
    setFormName('');
    setFormCategory('Mobile Chargers');
    setFormBrand('');
    setFormModel('');
    setFormPurchasePrice('');
    setFormSellingPrice('');
    setFormInitialAddedQty('10');
    setFormMinStockAlert('5');
    setFormProductCode('');
    setFormNotes('');
  };

  const handleOpenEdit = (p: Product) => {
    setEditingProduct(p);
    setFormName(p.name);
    setFormCategory(p.category);
    setFormBrand(p.brand);
    setFormModel(p.model);
    setFormPurchasePrice(p.purchasePrice.toString());
    setFormSellingPrice(p.sellingPrice.toString());
    setFormMinStockAlert(p.minStockAlert.toString());
    setFormProductCode(p.productCode);
    setFormNotes(p.notes);
  };

  const handleOpenStockModal = (p: Product) => {
    setStockingProduct(p);
    setAddQty('10');
    setAddPurchaseRate(p.purchasePrice.toString());
    setAddSupplier('');
    setAddStockNotes('');
  };

  const handleSaveProduct = (e: React.FormEvent) => {
    e.preventDefault();
    if (!formName.trim()) return;

    const pPrice = parseFloat(formPurchasePrice) || 0;
    const sPrice = parseFloat(formSellingPrice) || 0;
    const initialQty = parseInt(formInitialAddedQty, 10) || 0;
    const minAlert = parseInt(formMinStockAlert, 10) || 5;

    if (editingProduct) {
      // Edit keeps existing currentStock untouched
      updateProduct({
        ...editingProduct,
        name: formName.trim(),
        category: formCategory,
        brand: formBrand.trim(),
        model: formModel.trim(),
        purchasePrice: pPrice,
        sellingPrice: sPrice,
        minStockAlert: minAlert,
        productCode: formProductCode.trim(),
        notes: formNotes.trim(),
      });
      setEditingProduct(null);
    } else {
      // First time product addition with Initial Added Quantity
      addProduct({
        name: formName.trim(),
        category: formCategory,
        brand: formBrand.trim(),
        model: formModel.trim(),
        purchasePrice: pPrice,
        sellingPrice: sPrice,
        currentStock: initialQty,
        minStockAlert: minAlert,
        productCode: formProductCode.trim(),
        notes: formNotes.trim(),
      });
      setShowAddModal(false);
    }
    resetAddForm();
  };

  const handleSaveStock = (e: React.FormEvent) => {
    e.preventDefault();
    if (!stockingProduct) return;

    const qty = parseInt(addQty, 10) || 0;
    if (qty <= 0) return;
    const rate = parseFloat(addPurchaseRate) || stockingProduct.purchasePrice;

    addStock(stockingProduct.id, qty, rate, addSupplier.trim(), addStockNotes.trim());
    setStockingProduct(null);
  };

  // Filter products
  const lowThreshold = profile?.lowStockAlertThreshold || 5;
  const filteredProducts = products.filter((p) => {
    const matchesSearch =
      p.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
      p.brand.toLowerCase().includes(searchTerm.toLowerCase()) ||
      p.model.toLowerCase().includes(searchTerm.toLowerCase()) ||
      p.productCode.toLowerCase().includes(searchTerm.toLowerCase());

    const matchesCategory = selectedCategory === 'ALL' || p.category === selectedCategory;

    const limit = p.minStockAlert > 0 ? p.minStockAlert : lowThreshold;
    const matchesStock =
      stockFilter === 'ALL' ||
      (stockFilter === 'LOW' && p.currentStock > 0 && p.currentStock <= limit) ||
      (stockFilter === 'OUT' && p.currentStock <= 0);

    return matchesSearch && matchesCategory && matchesStock;
  });

  const categories = getAllCategories();

  // Calculated values for stock addition modal
  const prevStockNum = stockingProduct?.currentStock || 0;
  const addedStockNum = parseInt(addQty, 10) || 0;
  const calculatedTotalStock = prevStockNum + addedStockNum;

  return (
    <div className="space-y-4 pb-24">
      {/* Top Header & Actions */}
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-lg font-bold text-slate-800">পণ্য ও স্মার্ট স্টক ইনভেন্টরি</h2>
          <p className="text-xs text-slate-500">
            মোট {products.length} টি পণ্য • {products.reduce((acc, p) => acc + p.currentStock, 0)} পিস সামগ্রিক স্টক
          </p>
        </div>

        <button
          type="button"
          onClick={() => {
            resetAddForm();
            setShowAddModal(true);
          }}
          className="flex items-center gap-1.5 px-3.5 py-2 bg-emerald-600 hover:bg-emerald-700 active:scale-95 text-white rounded-xl text-xs font-bold shadow-md transition"
        >
          <Plus className="w-4 h-4" />
          <span>নতুন পণ্য</span>
        </button>
      </div>

      {/* View Switcher: Products vs Stock History */}
      <div className="flex items-center gap-2 bg-slate-100 p-1 rounded-xl">
        <button
          type="button"
          onClick={() => setActiveTab('PRODUCTS')}
          className={`flex-1 flex items-center justify-center gap-1.5 py-2 rounded-lg text-xs font-bold transition ${
            activeTab === 'PRODUCTS'
              ? 'bg-white text-slate-900 shadow-xs'
              : 'text-slate-600 hover:text-slate-900'
          }`}
        >
          <Package className="w-4 h-4 text-emerald-600" />
          <span>পণ্য তালিকা ({products.length})</span>
        </button>
        <button
          type="button"
          onClick={() => setActiveTab('HISTORY')}
          className={`flex-1 flex items-center justify-center gap-1.5 py-2 rounded-lg text-xs font-bold transition ${
            activeTab === 'HISTORY'
              ? 'bg-white text-slate-900 shadow-xs'
              : 'text-slate-600 hover:text-slate-900'
          }`}
        >
          <History className="w-4 h-4 text-indigo-600" />
          <span>স্টক বৃদ্ধির ইতিহাস ({stockAdditions.length})</span>
        </button>
      </div>

      {activeTab === 'PRODUCTS' && (
        <>
          {/* Search Input */}
          <div className="relative">
            <Search className="w-4 h-4 absolute left-3 top-3 text-slate-400" />
            <input
              type="text"
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              placeholder="পণ্যের নাম, ব্র্যান্ড, মডেল বা কোড খুঁজুন..."
              className="w-full pl-9 pr-4 py-2.5 bg-white border border-slate-200 rounded-xl text-xs sm:text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500 shadow-2xs"
            />
            {searchTerm && (
              <button
                type="button"
                onClick={() => setSearchTerm('')}
                className="absolute right-3 top-2.5 text-slate-400 hover:text-slate-600"
              >
                <X className="w-4 h-4" />
              </button>
            )}
          </div>

          {/* Stock Filter Chips */}
          <div className="flex items-center gap-2 overflow-x-auto pb-1 text-xs no-scrollbar">
            <button
              type="button"
              onClick={() => setStockFilter('ALL')}
              className={`px-3 py-1.5 rounded-lg font-semibold shrink-0 transition ${
                stockFilter === 'ALL'
                  ? 'bg-slate-900 text-white shadow-xs'
                  : 'bg-white text-slate-600 border border-slate-200'
              }`}
            >
              সকল ({products.length})
            </button>
            <button
              type="button"
              onClick={() => setStockFilter('LOW')}
              className={`px-3 py-1.5 rounded-lg font-semibold shrink-0 transition flex items-center gap-1 ${
                stockFilter === 'LOW'
                  ? 'bg-amber-600 text-white shadow-xs'
                  : 'bg-amber-50 text-amber-800 border border-amber-200'
              }`}
            >
              <AlertTriangle className="w-3.5 h-3.5" />
              কম স্টক ({products.filter((p) => p.currentStock > 0 && p.currentStock <= (p.minStockAlert || lowThreshold)).length})
            </button>
            <button
              type="button"
              onClick={() => setStockFilter('OUT')}
              className={`px-3 py-1.5 rounded-lg font-semibold shrink-0 transition ${
                stockFilter === 'OUT'
                  ? 'bg-red-600 text-white shadow-xs'
                  : 'bg-red-50 text-red-800 border border-red-200'
              }`}
            >
              স্টক শেষ ({products.filter((p) => p.currentStock <= 0).length})
            </button>
          </div>

          {/* Category Horizontal Filter Chips */}
          <div className="flex items-center gap-1.5 overflow-x-auto pb-1 text-xs no-scrollbar">
            <button
              type="button"
              onClick={() => setSelectedCategory('ALL')}
              className={`px-2.5 py-1 rounded-md font-medium shrink-0 transition ${
                selectedCategory === 'ALL'
                  ? 'bg-emerald-600 text-white font-bold'
                  : 'bg-slate-100 text-slate-600 hover:bg-slate-200'
              }`}
            >
              সব ক্যাটাগরি
            </button>
            {categories.map((cat) => (
              <button
                key={cat}
                type="button"
                onClick={() => setSelectedCategory(cat)}
                className={`px-2.5 py-1 rounded-md font-medium shrink-0 transition ${
                  selectedCategory === cat
                    ? 'bg-emerald-600 text-white font-bold'
                    : 'bg-slate-100 text-slate-600 hover:bg-slate-200'
                }`}
              >
                {cat}
              </button>
            ))}
          </div>

          {/* Product Cards List (Note: Permanent Delete buttons removed; deletions occur strictly in Admin Panel) */}
          <div className="space-y-3">
            {filteredProducts.length === 0 ? (
              <div className="bg-white rounded-2xl p-8 text-center border border-slate-200 text-slate-500">
                <Package className="w-10 h-10 mx-auto text-slate-300 mb-2" />
                <p className="font-semibold text-sm">কোনো পণ্য পাওয়া যায়নি!</p>
                <p className="text-xs text-slate-400 mt-1">অনুসন্ধান বা ফিল্টার পরিবর্তন করুন</p>
              </div>
            ) : (
              filteredProducts.map((p) => {
                const limit = p.minStockAlert > 0 ? p.minStockAlert : lowThreshold;
                const isOutOfStock = p.currentStock <= 0;
                const isLowStock = p.currentStock > 0 && p.currentStock <= limit;
                const profitPerItem = p.sellingPrice - p.purchasePrice;

                return (
                  <div
                    key={p.id}
                    className="bg-white rounded-2xl p-4 border border-slate-200/90 shadow-2xs hover:shadow-xs transition"
                  >
                    <div className="flex items-start justify-between gap-2">
                      <div className="flex-1">
                        <div className="flex items-center gap-2 flex-wrap">
                          <span className="text-[10px] font-bold px-2 py-0.5 rounded-full bg-slate-100 text-slate-600 border border-slate-200">
                            {p.category}
                          </span>
                          {p.brand && (
                            <span className="text-[10px] font-semibold text-slate-500">
                              {p.brand} {p.model}
                            </span>
                          )}
                          {p.productCode && (
                            <span className="text-[10px] font-mono text-slate-400 bg-slate-50 px-1.5 py-0.2 rounded border border-slate-200">
                              #{p.productCode}
                            </span>
                          )}
                        </div>
                        <h3 className="font-bold text-slate-900 text-sm mt-1">{p.name}</h3>
                      </div>

                      {/* Stock status badge */}
                      <span
                        className={`px-2.5 py-1 rounded-full text-[11px] font-black shrink-0 ${
                          isOutOfStock
                            ? 'bg-red-100 text-red-700 border border-red-200'
                            : isLowStock
                            ? 'bg-amber-100 text-amber-800 border border-amber-200'
                            : 'bg-emerald-100 text-emerald-800 border border-emerald-200'
                        }`}
                      >
                        {isOutOfStock ? 'স্টক শেষ' : `মজুদ: ${p.currentStock} টি`}
                      </span>
                    </div>

                    {/* Price and Stock Stats */}
                    <div className="grid grid-cols-3 gap-2 mt-3 pt-3 border-t border-slate-100 text-xs">
                      <div>
                        <span className="text-slate-400 text-[10px] block">ক্রয়মূল্য</span>
                        <span className="font-bold text-slate-700">{formatTaka(p.purchasePrice)}</span>
                      </div>
                      <div>
                        <span className="text-slate-400 text-[10px] block">বিক্রয়মূল্য</span>
                        <span className="font-extrabold text-emerald-600">{formatTaka(p.sellingPrice)}</span>
                      </div>
                      <div>
                        <span className="text-slate-400 text-[10px] block">মুনাফা / পিস</span>
                        <span className="font-bold text-blue-600">+{formatTaka(profitPerItem)}</span>
                      </div>
                    </div>

                    {/* Card Action Buttons (Only Add Stock and Edit; Deletions in Admin Panel) */}
                    <div className="flex items-center justify-between gap-2 mt-3 pt-2.5 border-t border-slate-100">
                      <button
                        type="button"
                        onClick={() => handleOpenStockModal(p)}
                        className="flex items-center gap-1.5 px-3.5 py-1.5 rounded-xl bg-gradient-to-r from-emerald-600 to-teal-700 hover:from-emerald-700 hover:to-teal-800 text-white text-xs font-bold shadow-xs active:scale-95 transition"
                      >
                        <PlusCircle className="w-3.5 h-3.5" />
                        <span>+ স্টক যোগ করুন</span>
                      </button>

                      <div className="flex items-center gap-1">
                        <button
                          type="button"
                          onClick={() => handleOpenEdit(p)}
                          className="flex items-center gap-1 px-3 py-1.5 text-slate-600 hover:text-blue-700 hover:bg-blue-50 rounded-xl transition font-semibold text-xs border border-slate-200"
                          title="তথ্য সংশোধন"
                        >
                          <Edit2 className="w-3.5 h-3.5" />
                          <span>এডিট</span>
                        </button>
                      </div>
                    </div>
                  </div>
                );
              })
            )}
          </div>
        </>
      )}

      {/* TAB 2: Stock Addition History (স্মার্ট স্টক হিস্ট্রি অডিট) */}
      {activeTab === 'HISTORY' && (
        <div className="space-y-3">
          <div className="p-3.5 rounded-2xl bg-indigo-50 border border-indigo-100 text-indigo-950 text-xs">
            <h4 className="font-bold text-indigo-900 flex items-center gap-1.5">
              <History className="w-4 h-4 text-indigo-600" />
              <span>স্টক বৃদ্ধির অডিট লগ (Stock Addition History)</span>
            </h4>
            <p className="text-indigo-700 mt-0.5">
              প্রতিটি স্টক বৃদ্ধির সময় পূর্বের স্টক, নতুন যোগকৃত স্টক এবং মোট স্টকের সম্পূর্ণ হিসাব এখানে সংরক্ষিত থাকে।
            </p>
          </div>

          {stockAdditions.length === 0 ? (
            <div className="bg-white rounded-2xl p-8 text-center border border-slate-200 text-slate-500">
              <Boxes className="w-10 h-10 mx-auto text-slate-300 mb-2" />
              <p className="font-semibold text-sm">কোনো স্টক বৃদ্ধির ইতিহাস নেই</p>
              <p className="text-xs text-slate-400 mt-1">পণ্যে নতুন স্টক যোগ করলে এখানে স্বয়ংক্রিয়ভাবে তালিকাভুক্ত হবে।</p>
            </div>
          ) : (
            stockAdditions.map((entry) => (
              <div
                key={entry.id}
                className="bg-white rounded-2xl p-4 border border-slate-200/90 shadow-2xs hover:shadow-xs transition"
              >
                <div className="flex items-start justify-between">
                  <div>
                    <h4 className="font-bold text-slate-900 text-sm">{entry.productName}</h4>
                    <p className="text-xs text-slate-500 flex items-center gap-2 mt-0.5">
                      <span>📅 {entry.dateAdded}</span>
                      {entry.supplierName && <span>• সরবরাহকারী: {entry.supplierName}</span>}
                    </p>
                  </div>
                  <span className="px-2.5 py-1 rounded-full text-xs font-black bg-emerald-100 text-emerald-800 border border-emerald-200">
                    +{entry.addedQuantity} পিস যোগ
                  </span>
                </div>

                {/* Stock calculation breakdown box: Previous -> Added -> Total */}
                <div className="mt-3 p-2.5 bg-slate-50 rounded-xl border border-slate-200 flex items-center justify-between text-xs">
                  <div className="text-center">
                    <span className="text-slate-400 text-[10px] block">পূর্বের স্টক</span>
                    <span className="font-bold text-slate-700">{entry.previousQuantity} টি</span>
                  </div>
                  <span className="text-slate-400 font-bold">+</span>
                  <div className="text-center">
                    <span className="text-emerald-600 text-[10px] block font-bold">নতুন যোগ</span>
                    <span className="font-black text-emerald-700">+{entry.addedQuantity} টি</span>
                  </div>
                  <span className="text-slate-400 font-bold">=</span>
                  <div className="text-center bg-white px-2.5 py-1 rounded-lg border border-slate-200 shadow-2xs">
                    <span className="text-indigo-600 text-[10px] block font-bold">বর্তমান মোট স্টক</span>
                    <span className="font-black text-indigo-700">{entry.totalQuantity} টি</span>
                  </div>
                </div>

                <div className="flex items-center justify-between mt-2.5 pt-2 border-t border-slate-100 text-xs text-slate-600">
                  <span>ক্রয় দর: <strong className="text-slate-800">{formatTaka(entry.purchasePrice)}</strong> / পিস</span>
                  <span>মোট ক্রয়মূল্য: <strong className="text-indigo-700">{formatTaka(entry.addedQuantity * entry.purchasePrice)}</strong></span>
                </div>
              </div>
            ))
          )}
        </div>
      )}

      {/* ADD NEW PRODUCT MODAL (Requirement 5: Initial Added Quantity) */}
      {showAddModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-xs">
          <div className="bg-white rounded-2xl max-w-md w-full max-h-[92vh] overflow-y-auto shadow-2xl p-5 animate-in fade-in zoom-in-95">
            <div className="flex items-center justify-between pb-3 border-b border-slate-100">
              <div>
                <h3 className="text-base font-bold text-slate-800 flex items-center gap-1.5">
                  <Package className="w-5 h-5 text-emerald-600" />
                  <span>নতুন পণ্য যোগ করুন</span>
                </h3>
                <p className="text-[11px] text-slate-500">পণ্য ও প্রাথমিক যোগকৃত স্টকের বিবরণ দিন</p>
              </div>
              <button
                type="button"
                onClick={() => setShowAddModal(false)}
                className="text-slate-400 hover:text-slate-600 p-1 rounded-full"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            <form onSubmit={handleSaveProduct} className="space-y-3.5 mt-4 text-xs sm:text-sm">
              {/* Product Name */}
              <div>
                <label className="block font-bold text-slate-700 mb-1">
                  Product Name (পণ্যের নাম) *
                </label>
                <input
                  type="text"
                  required
                  value={formName}
                  onChange={(e) => setFormName(e.target.value)}
                  placeholder="যেমন: Samsung 25W Fast Charger Type-C"
                  className="w-full px-3 py-2 border border-slate-200 rounded-xl focus:ring-2 focus:ring-emerald-500 focus:outline-none"
                />
              </div>

              {/* Category & Brand */}
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block font-bold text-slate-700 mb-1">Category (ক্যাটাগরি) *</label>
                  <select
                    value={formCategory}
                    onChange={(e) => setFormCategory(e.target.value)}
                    className="w-full px-3 py-2 border border-slate-200 rounded-xl focus:ring-2 focus:ring-emerald-500 focus:outline-none bg-white"
                  >
                    {categories.map((c) => (
                      <option key={c} value={c}>
                        {c}
                      </option>
                    ))}
                  </select>
                </div>
                <div>
                  <label className="block font-bold text-slate-700 mb-1">Brand (ব্র্যান্ড)</label>
                  <input
                    type="text"
                    value={formBrand}
                    onChange={(e) => setFormBrand(e.target.value)}
                    placeholder="Samsung / Xiaomi / Realme"
                    className="w-full px-3 py-2 border border-slate-200 rounded-xl focus:ring-2 focus:ring-emerald-500 focus:outline-none"
                  />
                </div>
              </div>

              {/* Purchase Price & Selling Price */}
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block font-bold text-slate-700 mb-1">Purchase Price (ক্রয়মূল্য ৳) *</label>
                  <input
                    type="number"
                    step="any"
                    required
                    value={formPurchasePrice}
                    onChange={(e) => setFormPurchasePrice(e.target.value)}
                    placeholder="350"
                    className="w-full px-3 py-2 border border-slate-200 rounded-xl focus:ring-2 focus:ring-emerald-500 focus:outline-none font-semibold"
                  />
                </div>
                <div>
                  <label className="block font-bold text-slate-700 mb-1">Selling Price (বিক্রয়মূল্য ৳) *</label>
                  <input
                    type="number"
                    step="any"
                    required
                    value={formSellingPrice}
                    onChange={(e) => setFormSellingPrice(e.target.value)}
                    placeholder="550"
                    className="w-full px-3 py-2 border border-slate-200 rounded-xl focus:ring-2 focus:ring-emerald-500 focus:outline-none font-semibold text-emerald-600"
                  />
                </div>
              </div>

              {/* Initial Added Quantity & Minimum Stock Alert */}
              <div className="grid grid-cols-2 gap-3 p-3 bg-emerald-50/70 rounded-xl border border-emerald-100">
                <div>
                  <label className="block font-bold text-emerald-900 mb-1">
                    Initial Added Quantity (প্রাথমিক স্টক) *
                  </label>
                  <input
                    type="number"
                    min="0"
                    required
                    value={formInitialAddedQty}
                    onChange={(e) => setFormInitialAddedQty(e.target.value)}
                    placeholder="10"
                    className="w-full px-3 py-2 border border-emerald-300 rounded-xl focus:ring-2 focus:ring-emerald-500 focus:outline-none font-bold text-emerald-800 bg-white"
                  />
                  <span className="text-[10px] text-emerald-700 mt-0.5 block">প্রথম চালানে আগত সংখ্যা</span>
                </div>
                <div>
                  <label className="block font-bold text-slate-700 mb-1">
                    Minimum Stock Alert (কম স্টক অ্যালার্ট)
                  </label>
                  <input
                    type="number"
                    min="1"
                    value={formMinStockAlert}
                    onChange={(e) => setFormMinStockAlert(e.target.value)}
                    placeholder="5"
                    className="w-full px-3 py-2 border border-slate-200 rounded-xl focus:ring-2 focus:ring-emerald-500 focus:outline-none bg-white"
                  />
                  <span className="text-[10px] text-slate-500 mt-0.5 block">এত পিসে নামলে সতর্ক করবে</span>
                </div>
              </div>

              {/* Optional details */}
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block font-semibold text-slate-600 mb-1">মডেল / ভ্যারিয়েন্ট</label>
                  <input
                    type="text"
                    value={formModel}
                    onChange={(e) => setFormModel(e.target.value)}
                    placeholder="EP-TA800"
                    className="w-full px-3 py-2 border border-slate-200 rounded-xl focus:ring-2 focus:ring-emerald-500 focus:outline-none"
                  />
                </div>
                <div>
                  <label className="block font-semibold text-slate-600 mb-1">প্রোডাক্ট কোড / SKU</label>
                  <input
                    type="text"
                    value={formProductCode}
                    onChange={(e) => setFormProductCode(e.target.value)}
                    placeholder="CHG-001"
                    className="w-full px-3 py-2 border border-slate-200 rounded-xl focus:ring-2 focus:ring-emerald-500 focus:outline-none"
                  />
                </div>
              </div>

              <div>
                <label className="block font-semibold text-slate-600 mb-1">নোট / বিবরণ</label>
                <textarea
                  rows={2}
                  value={formNotes}
                  onChange={(e) => setFormNotes(e.target.value)}
                  placeholder="অতিরিক্ত তথ্য..."
                  className="w-full px-3 py-2 border border-slate-200 rounded-xl focus:ring-2 focus:ring-emerald-500 focus:outline-none"
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
                  className="px-5 py-2 bg-emerald-600 hover:bg-emerald-700 text-white rounded-xl font-bold shadow-md transition"
                >
                  পণ্য সংরক্ষণ করুন
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* EDIT PRODUCT MODAL (DOES NOT TOUCH CURRENT STOCK ARBITRARILY) */}
      {editingProduct && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-xs">
          <div className="bg-white rounded-2xl max-w-md w-full max-h-[92vh] overflow-y-auto shadow-2xl p-5 animate-in fade-in zoom-in-95">
            <div className="flex items-center justify-between pb-3 border-b border-slate-100">
              <h3 className="text-base font-bold text-slate-800">পণ্য তথ্য পরিবর্তন (Edit Product)</h3>
              <button
                type="button"
                onClick={() => setEditingProduct(null)}
                className="text-slate-400 hover:text-slate-600 p-1 rounded-full"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            <form onSubmit={handleSaveProduct} className="space-y-3.5 mt-4 text-xs sm:text-sm">
              <div>
                <label className="block font-bold text-slate-700 mb-1">পণ্যের নাম *</label>
                <input
                  type="text"
                  required
                  value={formName}
                  onChange={(e) => setFormName(e.target.value)}
                  className="w-full px-3 py-2 border border-slate-200 rounded-xl focus:ring-2 focus:ring-emerald-500 focus:outline-none"
                />
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block font-bold text-slate-700 mb-1">ক্যাটাগরি</label>
                  <select
                    value={formCategory}
                    onChange={(e) => setFormCategory(e.target.value)}
                    className="w-full px-3 py-2 border border-slate-200 rounded-xl focus:ring-2 focus:ring-emerald-500 focus:outline-none bg-white"
                  >
                    {categories.map((c) => (
                      <option key={c} value={c}>
                        {c}
                      </option>
                    ))}
                  </select>
                </div>
                <div>
                  <label className="block font-bold text-slate-700 mb-1">ব্র্যান্ড</label>
                  <input
                    type="text"
                    value={formBrand}
                    onChange={(e) => setFormBrand(e.target.value)}
                    className="w-full px-3 py-2 border border-slate-200 rounded-xl focus:ring-2 focus:ring-emerald-500 focus:outline-none"
                  />
                </div>
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block font-bold text-slate-700 mb-1">ক্রয়মূল্য (৳) *</label>
                  <input
                    type="number"
                    step="any"
                    required
                    value={formPurchasePrice}
                    onChange={(e) => setFormPurchasePrice(e.target.value)}
                    className="w-full px-3 py-2 border border-slate-200 rounded-xl focus:ring-2 focus:ring-emerald-500 focus:outline-none font-semibold"
                  />
                </div>
                <div>
                  <label className="block font-bold text-slate-700 mb-1">বিক্রয়মূল্য (৳) *</label>
                  <input
                    type="number"
                    step="any"
                    required
                    value={formSellingPrice}
                    onChange={(e) => setFormSellingPrice(e.target.value)}
                    className="w-full px-3 py-2 border border-slate-200 rounded-xl focus:ring-2 focus:ring-emerald-500 focus:outline-none font-semibold text-emerald-600"
                  />
                </div>
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block font-bold text-slate-700 mb-1">কম স্টক অ্যালার্ট সীমা</label>
                  <input
                    type="number"
                    value={formMinStockAlert}
                    onChange={(e) => setFormMinStockAlert(e.target.value)}
                    className="w-full px-3 py-2 border border-slate-200 rounded-xl focus:ring-2 focus:ring-emerald-500 focus:outline-none"
                  />
                </div>
                <div>
                  <label className="block font-bold text-slate-700 mb-1">কোড / SKU</label>
                  <input
                    type="text"
                    value={formProductCode}
                    onChange={(e) => setFormProductCode(e.target.value)}
                    className="w-full px-3 py-2 border border-slate-200 rounded-xl focus:ring-2 focus:ring-emerald-500 focus:outline-none"
                  />
                </div>
              </div>

              <div className="p-3 bg-slate-50 border border-slate-200 rounded-xl text-xs text-slate-600 flex items-center justify-between">
                <span>বর্তমান মজুত স্টক: <strong className="text-emerald-700">{editingProduct.currentStock} টি</strong></span>
                <span className="text-[11px] text-slate-400">স্টক বৃদ্ধির জন্য '+ স্টক যোগ' অপশন ব্যবহার করুন</span>
              </div>

              <div className="flex items-center justify-end gap-2 pt-3 border-t border-slate-100">
                <button
                  type="button"
                  onClick={() => setEditingProduct(null)}
                  className="px-4 py-2 border border-slate-200 rounded-xl font-semibold text-slate-600 hover:bg-slate-50 transition"
                >
                  বাতিল
                </button>
                <button
                  type="submit"
                  className="px-5 py-2 bg-emerald-600 hover:bg-emerald-700 text-white rounded-xl font-bold shadow-md transition"
                >
                  আপডেট সংরক্ষণ করুন
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* SMART STOCK ADDITION MODAL (Requirement 5: Previous + Added = Current Total Stock) */}
      {stockingProduct && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-xs">
          <div className="bg-white rounded-2xl max-w-sm w-full shadow-2xl p-5 animate-in fade-in zoom-in-95">
            <div className="flex items-center justify-between pb-3 border-b border-slate-100">
              <h3 className="text-base font-bold text-slate-800 flex items-center gap-2">
                <PlusCircle className="w-5 h-5 text-emerald-600" />
                <span>স্মার্ট স্টক বৃদ্ধি (Add Stock)</span>
              </h3>
              <button
                type="button"
                onClick={() => setStockingProduct(null)}
                className="text-slate-400 hover:text-slate-600 p-1 rounded-full"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            <div className="my-3 p-3 bg-slate-50 rounded-xl border border-slate-200 text-xs">
              <p className="font-bold text-slate-900 text-sm">{stockingProduct.name}</p>
              <p className="text-slate-500 mt-0.5">{stockingProduct.category} • {stockingProduct.brand}</p>
            </div>

            {/* LIVE AUTOMATIC CALCULATION CARD: Previous Stock + New Stock Added = Current Total Stock */}
            <div className="p-3.5 bg-gradient-to-br from-emerald-50 via-teal-50 to-blue-50 border border-emerald-200 rounded-2xl shadow-2xs">
              <span className="text-[10px] font-extrabold uppercase tracking-wider text-emerald-900 block mb-2">
                স্বয়ংক্রিয় স্টক হিসাব (Auto Calculation)
              </span>
              <div className="flex items-center justify-between text-center">
                <div>
                  <span className="text-[10px] text-slate-500 font-semibold block">Previous Stock</span>
                  <span className="text-base font-black text-slate-800">{prevStockNum} টি</span>
                </div>
                <span className="text-lg font-black text-emerald-600">+</span>
                <div>
                  <span className="text-[10px] text-emerald-700 font-semibold block">New Stock Added</span>
                  <span className="text-base font-black text-emerald-700">+{addedStockNum} টি</span>
                </div>
                <span className="text-lg font-black text-slate-400">=</span>
                <div className="bg-emerald-600 text-white px-2.5 py-1 rounded-xl shadow-xs">
                  <span className="text-[10px] text-emerald-100 font-bold block">Current Total Stock</span>
                  <span className="text-base font-black">{calculatedTotalStock} টি</span>
                </div>
              </div>
            </div>

            <form onSubmit={handleSaveStock} className="space-y-3 mt-4 text-xs sm:text-sm">
              <div>
                <label className="block font-bold text-slate-700 mb-1">New Quantity Added (নতুন কতটি স্টক যোগ করবেন?) *</label>
                <input
                  type="number"
                  min="1"
                  required
                  value={addQty}
                  onChange={(e) => setAddQty(e.target.value)}
                  placeholder="10"
                  className="w-full px-3 py-2 border border-slate-200 rounded-xl focus:ring-2 focus:ring-emerald-500 focus:outline-none font-black text-lg text-emerald-700"
                />
              </div>

              <div>
                <label className="block font-bold text-slate-700 mb-1">Purchase Price (ক্রয়মূল্য / দর ৳)</label>
                <input
                  type="number"
                  step="any"
                  value={addPurchaseRate}
                  onChange={(e) => setAddPurchaseRate(e.target.value)}
                  placeholder={stockingProduct.purchasePrice.toString()}
                  className="w-full px-3 py-2 border border-slate-200 rounded-xl focus:ring-2 focus:ring-emerald-500 focus:outline-none font-semibold"
                />
              </div>

              <div>
                <label className="block font-bold text-slate-700 mb-1">Supplier / মহাজন (ঐচ্ছিক)</label>
                <input
                  type="text"
                  value={addSupplier}
                  onChange={(e) => setAddSupplier(e.target.value)}
                  placeholder="কোম্পানি বা মহাজনের নাম"
                  className="w-full px-3 py-2 border border-slate-200 rounded-xl focus:ring-2 focus:ring-emerald-500 focus:outline-none"
                />
              </div>

              <div>
                <label className="block font-bold text-slate-700 mb-1">Date of Addition</label>
                <div className="px-3 py-2 bg-slate-50 border border-slate-200 rounded-xl text-slate-700 font-medium text-xs flex items-center justify-between">
                  <span>📅 {todayDateStr}</span>
                  <span className="text-[10px] text-emerald-600 font-bold">আজকের তারিখ</span>
                </div>
              </div>

              <div>
                <label className="block font-bold text-slate-700 mb-1">নোট / চালান নম্বর</label>
                <input
                  type="text"
                  value={addStockNotes}
                  onChange={(e) => setAddStockNotes(e.target.value)}
                  placeholder="যেমন: চালান #2041"
                  className="w-full px-3 py-2 border border-slate-200 rounded-xl focus:ring-2 focus:ring-emerald-500 focus:outline-none"
                />
              </div>

              <div className="flex items-center justify-end gap-2 pt-3 border-t border-slate-100">
                <button
                  type="button"
                  onClick={() => setStockingProduct(null)}
                  className="px-4 py-2 border border-slate-200 rounded-xl font-semibold text-slate-600 hover:bg-slate-50 transition"
                >
                  বাতিল
                </button>
                <button
                  type="submit"
                  className="px-5 py-2 bg-gradient-to-r from-emerald-600 to-teal-700 hover:from-emerald-700 hover:to-teal-800 text-white rounded-xl font-bold shadow-md transition"
                >
                  স্টক বৃদ্ধি ও নিশ্চিত করুন
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
