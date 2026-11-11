import React, { useState } from 'react';
import {
  FileText,
  Printer,
  Share2,
  Copy,
  Plus,
  Eye,
  Check,
  X,
  Phone,
  MapPin,
  Calendar,
  Clock,
  ArrowLeft,
  ChevronDown,
  Receipt,
  Smartphone,
  Hash,
  Download,
  Loader2,
  ExternalLink,
  Send,
} from 'lucide-react';
import { jsPDF } from 'jspdf';
import { toJpeg } from 'html-to-image';
import { useShop } from '../context/ShopContext';
import { CashMemo, CashMemoItem } from '../types';
import { formatTaka, getTodayStr, getCurrentTimeStr, generateMemoNumber } from '../utils/formatters';
import { takaInBengaliWords } from '../utils/bengaliWords';

export const CashMemoScreen: React.FC = () => {
  const {
    cashMemos,
    products,
    profile,
    todayDateStr,
    saveCashMemo,
    showMessage,
  } = useShop();

  // Mode: 'FORM' | 'VIEW' | 'HISTORY'
  const [activeView, setActiveView] = useState<'FORM' | 'VIEW' | 'HISTORY'>('FORM');
  const [selectedMemo, setSelectedMemo] = useState<CashMemo | null>(cashMemos[0] || null);
  const [memoStyle, setMemoStyle] = useState<'ORANGE_AMBER' | 'CYAN_BLUE' | 'THERMAL_RECEIPT'>('ORANGE_AMBER');

  // PDF & WhatsApp state
  const [isGeneratingPdf, setIsGeneratingPdf] = useState(false);
  const [isSharingWhatsApp, setIsSharingWhatsApp] = useState(false);
  const [showPdfModal, setShowPdfModal] = useState(false);
  const [pdfPreviewUrl, setPdfPreviewUrl] = useState<string | null>(null);
  const [pdfPreviewImg, setPdfPreviewImg] = useState<string | null>(null);
  const [pdfInstance, setPdfInstance] = useState<jsPDF | null>(null);

  // WhatsApp prompt modal state when customer mobile is not in memo
  const [showWhatsAppPromptModal, setShowWhatsAppPromptModal] = useState(false);
  const [whatsAppInputMobile, setWhatsAppInputMobile] = useState('');
  const [pendingShareMemo, setPendingShareMemo] = useState<CashMemo | null>(null);

  // Form states
  const [memoNumber, setMemoNumber] = useState(generateMemoNumber());
  const [memoDate, setMemoDate] = useState(todayDateStr);
  const [memoTime, setMemoTime] = useState(getCurrentTimeStr());
  const [customerName, setCustomerName] = useState('');
  const [customerMobile, setCustomerMobile] = useState('');
  const [customerAddress, setCustomerAddress] = useState('বুল্লা বাজার');
  const [memoNotes, setMemoNotes] = useState('বিক্রিত মাল ফেরত নেওয়া হয় না।');
  const [autoRecordSale, setAutoRecordSale] = useState(true);

  // Line items
  const [items, setItems] = useState<CashMemoItem[]>([
    {
      description: 'Samsung 25W Fast Charger Type-C',
      quantity: 1,
      rate: 550,
      amount: 550,
    },
  ]);

  const [discountAmount, setDiscountAmount] = useState('0');
  const [paidAmount, setPaidAmount] = useState('');

  // Calculations
  const subtotal = items.reduce((acc, it) => acc + (it.amount || 0), 0);
  const totalQuantity = items.reduce((acc, it) => acc + (it.quantity || 0), 0);
  const discount = parseFloat(discountAmount) || 0;
  const grandTotal = Math.max(0, subtotal - discount);
  const paid = paidAmount === '' ? grandTotal : parseFloat(paidAmount) || 0;
  const due = Math.max(0, grandTotal - paid);
  const paymentStatus: 'PAID' | 'DUE' | 'PARTIAL' = due === 0 ? 'PAID' : paid === 0 ? 'DUE' : 'PARTIAL';

  const resetForm = () => {
    setMemoNumber(generateMemoNumber());
    setMemoDate(todayDateStr);
    setMemoTime(getCurrentTimeStr());
    setCustomerName('');
    setCustomerMobile('');
    setCustomerAddress('বুল্লা বাজার');
    setMemoNotes('বিক্রিত মাল ফেরত নেওয়া হয় না।');
    setItems([
      {
        description: '',
        quantity: 1,
        rate: 0,
        amount: 0,
      },
    ]);
    setDiscountAmount('0');
    setPaidAmount('');
  };

  const handleAddItem = () => {
    setItems([
      ...items,
      {
        description: '',
        quantity: 1,
        rate: 0,
        amount: 0,
      },
    ]);
  };

  const handleItemChange = (index: number, field: keyof CashMemoItem, value: any) => {
    const updated = [...items];
    const item = { ...updated[index], [field]: value };

    if (field === 'quantity' || field === 'rate') {
      const q = field === 'quantity' ? parseInt(value, 10) || 0 : item.quantity;
      const r = field === 'rate' ? parseFloat(value) || 0 : item.rate;
      item.amount = q * r;
    }

    updated[index] = item;
    setItems(updated);
  };

  const handleSelectProductForItem = (index: number, prodId: string) => {
    const p = products.find((prod) => prod.id === prodId);
    if (!p) return;

    const updated = [...items];
    updated[index] = {
      productId: p.id,
      description: p.name,
      quantity: 1,
      rate: p.sellingPrice,
      amount: p.sellingPrice,
    };
    setItems(updated);
  };

  const handleRemoveItem = (index: number) => {
    if (items.length <= 1) return;
    setItems(items.filter((_, i) => i !== index));
  };

  const handleSaveAndPreview = (e: React.FormEvent) => {
    e.preventDefault();
    const validItems = items.filter((it) => it.description.trim() && it.amount > 0);
    if (validItems.length === 0) return;

    const newMemoData: Omit<CashMemo, 'id' | 'userId'> = {
      memoNumber,
      date: memoDate || todayDateStr,
      time: memoTime || getCurrentTimeStr(),
      customerName: customerName.trim() || 'নগদ ক্রেতা',
      customerMobile: customerMobile.trim(),
      customerAddress: customerAddress.trim() || 'বুল্লা বাজার',
      items: validItems,
      subtotal,
      discount,
      grandTotal,
      paidAmount: paid,
      dueAmount: due,
      paymentStatus,
      notes: memoNotes.trim(),
      isDemo: false,
    };

    const savedId = saveCashMemo(newMemoData, autoRecordSale);

    const fullMemo: CashMemo = {
      ...newMemoData,
      id: savedId || `memo-${Date.now()}`,
      userId: profile.userId,
    };

    setSelectedMemo(fullMemo);
    setActiveView('VIEW');
  };

  // Generate Standard 5.5 x 8.5 Inch PDF
  const generateMemoPdf = async (
    memo: CashMemo
  ): Promise<{ blob: Blob; url: string; pdf: jsPDF; imgData: string } | null> => {
    const element = document.getElementById('printable-cash-memo');
    if (!element) return null;

    try {
      // Use html-to-image which renders modern CSS (oklch, Tailwind v4) natively via browser SVG/Canvas
      const imgData = await toJpeg(element, {
        quality: 0.96,
        pixelRatio: 2,
        backgroundColor: '#ffffff',
        skipFonts: true,
      });

      // Standard 5.5 x 8.5 inches (Statement size)
      const pdf = new jsPDF({
        unit: 'in',
        format: [5.5, 8.5],
        orientation: 'portrait',
      });

      pdf.addImage(imgData, 'JPEG', 0, 0, 5.5, 8.5, undefined, 'FAST');

      const blob = pdf.output('blob');
      const url = URL.createObjectURL(blob);
      return { blob, url, pdf, imgData };
    } catch (err) {
      console.error('Failed to generate PDF with skipFonts, trying standard options:', err);
      try {
        const imgData = await toJpeg(element, {
          quality: 0.95,
          pixelRatio: 2,
          backgroundColor: '#ffffff',
        });

        const pdf = new jsPDF({
          unit: 'in',
          format: [5.5, 8.5],
          orientation: 'portrait',
        });

        pdf.addImage(imgData, 'JPEG', 0, 0, 5.5, 8.5, undefined, 'FAST');

        const blob = pdf.output('blob');
        const url = URL.createObjectURL(blob);
        return { blob, url, pdf, imgData };
      } catch (fallbackErr) {
        console.error('Failed to generate PDF completely:', fallbackErr);
        return null;
      }
    }
  };

  // Instant PDF View & Print
  const handleOpenPdfPreview = async () => {
    if (!selectedMemo) return;
    setIsGeneratingPdf(true);
    try {
      const result = await generateMemoPdf(selectedMemo);
      if (result) {
        setPdfPreviewUrl(result.url);
        setPdfPreviewImg(result.imgData);
        setPdfInstance(result.pdf);
        setShowPdfModal(true);
      } else {
        window.print();
      }
    } catch (err) {
      console.error(err);
      window.print();
    } finally {
      setIsGeneratingPdf(false);
    }
  };

  // WhatsApp Share with PDF
  const handleShareWhatsAppPdf = async (memo: CashMemo, overrideMobile?: string) => {
    const rawMobile = (overrideMobile || memo.customerMobile || '').trim();
    if (!rawMobile) {
      setPendingShareMemo(memo);
      setWhatsAppInputMobile('');
      setShowWhatsAppPromptModal(true);
      return;
    }

    setShowWhatsAppPromptModal(false);
    setIsSharingWhatsApp(true);

    try {
      const cleanPhone = rawMobile.replace(/[+\-\s]/g, '');
      let formattedPhone = cleanPhone;
      if (cleanPhone.startsWith('01')) {
        formattedPhone = `88${cleanPhone}`;
      } else if (cleanPhone.startsWith('1')) {
        formattedPhone = `880${cleanPhone}`;
      }

      // Generate the 5.5 x 8.5 inch PDF
      const result = await generateMemoPdf(memo);
      if (!result) {
        showMessage('PDF তৈরিতে সমস্যা হয়েছে। দয়া করে আবার চেষ্টা করুন।');
        return;
      }

      const fileName = `CashMemo_${memo.memoNumber}.pdf`;
      const pdfFile = new File([result.blob], fileName, { type: 'application/pdf' });

      // Try native Web Share API with files (Android / iOS / Mobile WhatsApp)
      if (typeof navigator !== 'undefined' && navigator.canShare && navigator.canShare({ files: [pdfFile] })) {
        try {
          await navigator.share({
            files: [pdfFile],
            title: `ক্যাশ মেমো ${memo.memoNumber}`,
            text: `ইয়াছিন টেলিকম - ক্যাশ মেমো নং: ${memo.memoNumber}`,
          });
          showMessage('হোয়াটসঅ্যাপে ক্যাশ মেমো PDF পাঠানো হয়েছে!');
          return;
        } catch (shareErr) {
          if ((shareErr as Error).name === 'AbortError') return;
        }
      }

      // Desktop / Browser Fallback: Download PDF and open customer's WhatsApp
      result.pdf.save(fileName);
      const waUrl = formattedPhone ? `https://wa.me/${formattedPhone}` : `https://api.whatsapp.com/send`;
      window.open(waUrl, '_blank');
      showMessage('ক্যাশ মেমোর PDF ডাউনলোড হয়েছে এবং গ্রাহকের হোয়াটসঅ্যাপ চ্যাট খোলা হচ্ছে। চ্যাটে PDF ফাইলটি সিলেক্ট করে পাঠিয়ে দিন!');
    } catch (err) {
      console.error(err);
      showMessage('হোয়াটসঅ্যাপে শেয়ার করার সময় সমস্যা হয়েছে।');
    } finally {
      setIsSharingWhatsApp(false);
    }
  };

  const handlePrint = () => {
    window.print();
  };

  const handleShareWhatsApp = (memo: CashMemo) => {
    handleShareWhatsAppPdf(memo);
  };

  const getPlainTextReceipt = (memo: CashMemo) => {
    let text = `🧾 *ক্যাশ মেমো / CASH MEMO* 🧾\n`;
    text += `🏢 *${profile.shopName || 'ইয়াছিন টেলিকম'}*\n`;
    text += `📱 স্মার্টফোন ও ডিজিটাল সেবা কেন্দ্র\n`;
    text += `👤 প্রোঃ ${profile.ownerName || 'মোহাম্মদ ইয়াছিন'}\n`;
    text += `📞 ${profile.phone || '01766906974'}\n`;
    text += `📍 ${profile.address || 'বুল্লা বাজার, লাখাই, হবিগঞ্জ'}\n`;
    text += `--------------------------------\n`;
    text += `মেমো নং: ${memo.memoNumber}\n`;
    text += `তারিখ: ${memo.date} (${memo.time})\n`;
    text += `গ্রাহক: ${memo.customerName || 'নগদ ক্রেতা'}\n`;
    if (memo.customerMobile) text += `মোবাইল: ${memo.customerMobile}\n`;
    if (memo.customerAddress) text += `ঠিকানা: ${memo.customerAddress}\n`;
    text += `--------------------------------\n`;
    text += `*পণ্যের বিবরণ:*\n`;
    memo.items.forEach((it, i) => {
      text += `${i + 1}. ${it.description} - ৳${Math.round(it.rate)} × ${it.quantity} = ৳${Math.round(it.amount)}\n`;
    });
    text += `--------------------------------\n`;
    const memoTotalQty = memo.items.reduce((acc, it) => acc + (it.quantity || 0), 0);
    text += `📦 *মোট আইটেম:* ${memoTotalQty} টি\n`;
    text += `💰 *উপ-মোট:* ৳${Math.round(memo.subtotal)}\n`;
    if (memo.discount > 0) text += `🏷️ *ছাড়:* ৳${Math.round(memo.discount)}\n`;
    text += `💵 *সর্বমোট:* ৳${Math.round(memo.grandTotal)}\n`;
    text += `✅ *জমা:* ৳${Math.round(memo.paidAmount)}\n`;
    if (memo.dueAmount > 0) {
      text += `⚠️ *বাকী:* ৳${Math.round(memo.dueAmount)}\n`;
    } else {
      text += `✨ *সম্পূর্ণ পরিশোধিত*\n`;
    }
    text += `🗣️ *কথায়:* ${takaInBengaliWords(memo.grandTotal)}\n`;
    text += `--------------------------------\n`;
    text += `📌 ${memo.notes || 'বিক্রিত মাল ফেরত নেওয়া হয় না।'}\n`;
    text += `🙏 ধন্যবাদ, আবার আসবেন!\n`;
    return text;
  };

  const handleCopyReceipt = (memo: CashMemo) => {
    navigator.clipboard.writeText(getPlainTextReceipt(memo));
    showMessage('মেমোর টেক্সট ক্লিপবোর্ডে কপি করা হয়েছে!');
  };

  return (
    <div className="space-y-4 pb-24">
      {/* View Switcher Bar */}
      <div className="flex flex-wrap items-center justify-between gap-2 no-print">
        <div className="flex items-center gap-1.5 bg-slate-100 p-1 rounded-xl">
          <button
            type="button"
            onClick={() => setActiveView('FORM')}
            className={`px-3 py-1.5 rounded-lg text-xs font-bold transition ${
              activeView === 'FORM'
                ? 'bg-orange-600 text-white shadow-xs'
                : 'text-slate-600 hover:text-slate-900'
            }`}
          >
            নতুন মেমো তৈরি
          </button>
          <button
            type="button"
            onClick={() => {
              if (cashMemos.length > 0 && !selectedMemo) {
                setSelectedMemo(cashMemos[0]);
              }
              setActiveView('VIEW');
            }}
            className={`px-3 py-1.5 rounded-lg text-xs font-bold transition ${
              activeView === 'VIEW'
                ? 'bg-orange-600 text-white shadow-xs'
                : 'text-slate-600 hover:text-slate-900'
            }`}
          >
            মেমো ভিউ ও প্রিন্ট
          </button>
          <button
            type="button"
            onClick={() => setActiveView('HISTORY')}
            className={`px-3 py-1.5 rounded-lg text-xs font-bold transition ${
              activeView === 'HISTORY'
                ? 'bg-orange-600 text-white shadow-xs'
                : 'text-slate-600 hover:text-slate-900'
            }`}
          >
            মেমো খাতা ({cashMemos.length})
          </button>
        </div>

        {activeView === 'VIEW' && selectedMemo && (
          <div className="flex items-center gap-2">
            <span className="text-[11px] font-bold text-slate-500 hidden sm:inline">লেআউট স্টাইল:</span>
            <select
              value={memoStyle}
              onChange={(e) => setMemoStyle(e.target.value as any)}
              className="text-xs font-bold bg-white border border-slate-300 rounded-xl px-2.5 py-1.5 text-slate-700 shadow-2xs focus:ring-2 focus:ring-orange-500 focus:outline-none"
            >
              <option value="ORANGE_AMBER">স্ট্যান্ডার্ড সাইজ (৫.৫ × ৮.৫ ইঞ্চি) - অরেঞ্জ</option>
              <option value="CYAN_BLUE">স্ট্যান্ডার্ড সাইজ (৫.৫ × ৮.৫ ইঞ্চি) - সায়ান</option>
              <option value="THERMAL_RECEIPT">থার্মাল পিওএস রসিদ (58mm/80mm POS)</option>
            </select>
          </div>
        )}
      </div>

      {/* VIEW 1: Cash Memo Creation Form */}
      {activeView === 'FORM' && (
        <form onSubmit={handleSaveAndPreview} className="space-y-4 no-print">
          <div className="bg-white rounded-2xl p-4 sm:p-5 border border-slate-200 shadow-2xs space-y-4">
            <div className="flex items-center justify-between pb-3 border-b border-slate-100">
              <h2 className="text-base font-bold text-slate-800 flex items-center gap-2">
                <FileText className="w-5 h-5 text-orange-600" />
                <span>অফিসিয়াল ক্যাশ মেমো জেনারেটর</span>
              </h2>
              <span className="text-xs font-bold px-2.5 py-0.5 rounded-full bg-orange-100 text-orange-800 border border-orange-200">
                #{memoNumber}
              </span>
            </div>

            {/* Visiting Card Banner Header (No separate loose text shop name or address) */}
            <div className="rounded-2xl overflow-hidden shadow-sm border border-slate-200 bg-slate-900">
              <img
                src="/assets/aistudio/Modern_Bengali_Telecom_Store_Banner_23b2899d.png"
                alt="ইয়াছিন টেলিকম - অফিসিয়াল ভিজিটিং কার্ড"
                className="w-full h-auto object-cover block rounded-2xl"
                draggable={false}
              />
            </div>

            {/* Customer Details */}
            <div className="grid grid-cols-1 sm:grid-cols-3 gap-3 text-xs sm:text-sm">
              <div>
                <label className="block font-bold text-slate-700 mb-1">
                  Customer Name (ক্রেতার নাম) *
                </label>
                <input
                  type="text"
                  required
                  value={customerName}
                  onChange={(e) => setCustomerName(e.target.value)}
                  placeholder="নগদ ক্রেতা / নাম লিখুন"
                  className="w-full px-3 py-2 border border-slate-200 rounded-xl focus:ring-2 focus:ring-orange-500 focus:outline-none"
                />
              </div>

              <div>
                <label className="block font-bold text-slate-700 mb-1">
                  Mobile Number (মোবাইল নম্বর - ঐচ্ছিক)
                </label>
                <input
                  type="text"
                  value={customerMobile}
                  onChange={(e) => setCustomerMobile(e.target.value)}
                  placeholder="017xxxxxxxx"
                  className="w-full px-3 py-2 border border-slate-200 rounded-xl focus:ring-2 focus:ring-orange-500 focus:outline-none"
                />
              </div>

              <div>
                <label className="block font-bold text-slate-700 mb-1">ঠিকানা</label>
                <input
                  type="text"
                  value={customerAddress}
                  onChange={(e) => setCustomerAddress(e.target.value)}
                  placeholder="বুল্লা বাজার, লাখাই"
                  className="w-full px-3 py-2 border border-slate-200 rounded-xl focus:ring-2 focus:ring-orange-500 focus:outline-none"
                />
              </div>
            </div>

            {/* Date and Time */}
            <div className="grid grid-cols-2 gap-3 text-xs">
              <div>
                <label className="block font-semibold text-slate-600 mb-1">তারিখ</label>
                <input
                  type="text"
                  value={memoDate}
                  onChange={(e) => setMemoDate(e.target.value)}
                  className="w-full px-3 py-1.5 border border-slate-200 rounded-xl bg-slate-50"
                />
              </div>
              <div>
                <label className="block font-semibold text-slate-600 mb-1">সময়</label>
                <input
                  type="text"
                  value={memoTime}
                  onChange={(e) => setMemoTime(e.target.value)}
                  className="w-full px-3 py-1.5 border border-slate-200 rounded-xl bg-slate-50"
                />
              </div>
            </div>

            {/* Multiple Product Line Items */}
            <div className="space-y-3 pt-2">
              <div className="flex items-center justify-between">
                <label className="font-bold text-slate-800 text-xs sm:text-sm">
                  পণ্যের তালিকা (Product Items)
                </label>
                <button
                  type="button"
                  onClick={handleAddItem}
                  className="flex items-center gap-1 text-xs font-bold text-orange-600 hover:text-orange-700 px-2.5 py-1 rounded-lg bg-orange-50 hover:bg-orange-100 transition"
                >
                  <Plus className="w-3.5 h-3.5" /> আইটেম যোগ
                </button>
              </div>

              <div className="space-y-2.5">
                {items.map((item, index) => (
                  <div
                    key={index}
                    className="p-3 bg-slate-50 rounded-xl border border-slate-200 space-y-2 text-xs"
                  >
                    <div className="flex items-center justify-between gap-2">
                      <span className="font-black text-slate-400">#{index + 1}</span>

                      {/* Quick product selector from inventory */}
                      <select
                        onChange={(e) => handleSelectProductForItem(index, e.target.value)}
                        defaultValue=""
                        className="text-[11px] bg-white border border-slate-200 rounded-lg px-2 py-1 text-slate-700 max-w-[200px] truncate"
                      >
                        <option value="" disabled>
                          স্টক পণ্য থেকে বাছাই করুন...
                        </option>
                        {products.map((p) => (
                          <option key={p.id} value={p.id}>
                            {p.name} (৳{p.sellingPrice})
                          </option>
                        ))}
                      </select>

                      {items.length > 1 && (
                        <button
                          type="button"
                          onClick={() => handleRemoveItem(index)}
                          className="text-red-500 hover:text-red-700 p-1"
                        >
                          <X className="w-4 h-4" />
                        </button>
                      )}
                    </div>

                    <input
                      type="text"
                      required
                      value={item.description}
                      onChange={(e) => handleItemChange(index, 'description', e.target.value)}
                      placeholder="পণ্যের নাম ও বিবরণ"
                      className="w-full px-3 py-1.5 border border-slate-200 rounded-lg bg-white focus:outline-none focus:ring-1 focus:ring-orange-500 font-medium"
                    />

                    <div className="grid grid-cols-3 gap-2">
                      <div>
                        <span className="text-[10px] text-slate-400 block">পরিমাণ (Qty)</span>
                        <input
                          type="number"
                          min="1"
                          required
                          value={item.quantity}
                          onChange={(e) => handleItemChange(index, 'quantity', e.target.value)}
                          className="w-full px-2 py-1 text-center font-bold border border-slate-200 rounded-lg bg-white"
                        />
                      </div>
                      <div>
                        <span className="text-[10px] text-slate-400 block">একক মূল্য (Rate ৳)</span>
                        <input
                          type="number"
                          step="any"
                          min="0"
                          required
                          value={item.rate}
                          onChange={(e) => handleItemChange(index, 'rate', e.target.value)}
                          className="w-full px-2 py-1 text-center font-semibold border border-slate-200 rounded-lg bg-white"
                        />
                      </div>
                      <div>
                        <span className="text-[10px] text-slate-400 block">মোট টাকা (৳)</span>
                        <div className="w-full px-2 py-1 rounded-lg bg-slate-200/60 font-black text-center text-slate-900">
                          {formatTaka(item.amount)}
                        </div>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            </div>

            {/* Calculations & Bengali Words */}
            <div className="p-3.5 bg-orange-50/50 rounded-xl border border-orange-100 space-y-2 text-xs">
              <div className="flex items-center justify-between">
                <span className="text-slate-600 font-semibold">মোট পণ্য পরিমাণ (Total Quantity):</span>
                <span className="font-extrabold text-slate-900">{totalQuantity} টি</span>
              </div>

              <div className="flex items-center justify-between">
                <span className="text-slate-600">উপ-মোট (Subtotal):</span>
                <span className="font-bold text-slate-900">{formatTaka(subtotal)}</span>
              </div>

              <div className="flex items-center justify-between">
                <span className="text-slate-600">ছাড় / Discount (৳):</span>
                <input
                  type="number"
                  min="0"
                  value={discountAmount}
                  onChange={(e) => setDiscountAmount(e.target.value)}
                  className="w-24 px-2 py-1 text-right font-bold border border-orange-200 rounded-lg bg-white"
                />
              </div>

              <div className="flex items-center justify-between font-black text-sm pt-1 border-t border-orange-200 text-slate-900">
                <span>সর্বমোট (Grand Total):</span>
                <span className="text-orange-700 text-base">{formatTaka(grandTotal)}</span>
              </div>

              <div className="flex items-center justify-between pt-1">
                <span className="font-semibold text-slate-700">নগদ জমা / Advance (৳):</span>
                <input
                  type="number"
                  min="0"
                  step="any"
                  value={paidAmount}
                  onChange={(e) => setPaidAmount(e.target.value)}
                  placeholder={grandTotal.toString()}
                  className="w-28 px-2 py-1 text-right font-black border border-emerald-300 rounded-lg bg-white text-emerald-600"
                />
              </div>

              {due > 0 && (
                <div className="flex items-center justify-between text-red-600 font-bold">
                  <span>বাকী (Due):</span>
                  <span>{formatTaka(due)}</span>
                </div>
              )}

              {/* Taka in Bengali Words preview */}
              <div className="mt-2 pt-2 border-t border-orange-200 text-slate-700 font-medium">
                <span className="font-bold text-orange-900">কথায়: </span>
                <span className="italic">{takaInBengaliWords(grandTotal)}</span>
              </div>
            </div>

            {/* Auto record sale checkbox */}
            <label className="flex items-center gap-2 p-3 bg-slate-50 border border-slate-200 rounded-xl cursor-pointer text-xs">
              <input
                type="checkbox"
                checked={autoRecordSale}
                onChange={(e) => setAutoRecordSale(e.target.checked)}
                className="w-4 h-4 text-emerald-600 rounded focus:ring-emerald-500"
              />
              <span className="font-bold text-slate-800">
                স্বয়ংক্রিয়ভাবে বিক্রয় খাতায় যোগ করুন এবং ইনভেন্টরি স্টক আপডেট করুন (একবারই আপডেট হবে)
              </span>
            </label>

            {/* Submit & Preview Button */}
            <div className="flex items-center justify-end gap-2 pt-2">
              <button
                type="button"
                onClick={resetForm}
                className="px-4 py-2 border border-slate-200 rounded-xl font-semibold text-slate-600 hover:bg-slate-50 text-xs"
              >
                রিসেট
              </button>
              <button
                type="submit"
                className="flex items-center gap-1.5 px-6 py-2.5 bg-orange-600 hover:bg-orange-700 text-white rounded-xl font-bold shadow-md text-xs sm:text-sm transition"
              >
                <Eye className="w-4 h-4" />
                <span>সংরক্ষণ ও মেমো দেখুন</span>
              </button>
            </div>
          </div>
        </form>
      )}

      {/* VIEW 2: Cash Memo Visual Preview & Print */}
      {activeView === 'VIEW' && selectedMemo && (
        <div className="space-y-4">
          {/* Action buttons (hidden on print) */}
          <div className="flex flex-wrap items-center justify-between gap-2 bg-white p-3 rounded-2xl border border-slate-200 shadow-2xs no-print">
            <button
              type="button"
              onClick={() => setActiveView('FORM')}
              className="flex items-center gap-1 text-xs font-bold text-slate-600 hover:text-slate-900 px-3 py-1.5 rounded-lg border border-slate-200"
            >
              <ArrowLeft className="w-3.5 h-3.5" />
              <span>নতুন এন্ট্রি</span>
            </button>

            <div className="flex items-center gap-2">
              <button
                type="button"
                onClick={() => handleCopyReceipt(selectedMemo)}
                className="flex items-center gap-1 px-3 py-1.5 rounded-lg bg-slate-100 hover:bg-slate-200 text-slate-700 text-xs font-semibold transition"
                title="টেক্সট কপি করুন"
              >
                <Copy className="w-3.5 h-3.5" />
                <span className="hidden sm:inline">কপি</span>
              </button>
              <button
                type="button"
                disabled={isSharingWhatsApp || isGeneratingPdf}
                onClick={() => handleShareWhatsAppPdf(selectedMemo)}
                className="flex items-center gap-1.5 px-3.5 py-1.5 rounded-lg bg-emerald-600 hover:bg-emerald-700 text-white text-xs font-bold shadow transition active:scale-95 disabled:opacity-50"
                title="গ্রাহকের নম্বরে সরাসরি PDF পাঠান"
              >
                {isSharingWhatsApp ? (
                  <Loader2 className="w-3.5 h-3.5 animate-spin" />
                ) : (
                  <Share2 className="w-3.5 h-3.5" />
                )}
                <span>হোয়াটসঅ্যাপে PDF পাঠান</span>
              </button>
              <button
                type="button"
                disabled={isGeneratingPdf}
                onClick={handleOpenPdfPreview}
                className="flex items-center gap-1.5 px-4 py-1.5 rounded-lg bg-blue-600 hover:bg-blue-700 text-white text-xs font-bold shadow transition active:scale-95 disabled:opacity-50"
                title="পিডিএফ ভিউ ও প্রিন্ট"
              >
                {isGeneratingPdf ? (
                  <Loader2 className="w-3.5 h-3.5 animate-spin" />
                ) : (
                  <Printer className="w-3.5 h-3.5" />
                )}
                <span>প্রিন্ট / PDF ভিউ</span>
              </button>
            </div>
          </div>

          {/* Printable Cash Memo Document (Standard 5.5 x 8.5 Inches) */}
          <div
            id="printable-cash-memo"
            className="memo-printable-area bg-white rounded-2xl shadow-lg border border-slate-200 overflow-hidden w-full max-w-[5.5in] mx-auto print:shadow-none print:border-none print:max-w-none print:w-full"
          >
            {memoStyle === 'ORANGE_AMBER' ? (
              /* ORANGE-AMBER EXECUTIVE DESIGN (5.5 x 8.5 INCHES) */
              <div className="p-4 sm:p-5 relative bg-white min-h-[780px] flex flex-col justify-between text-slate-900">
                {/* Official Visiting Card Header (No separate loose text shop name or address) */}
                <div>
                  <div className="rounded-xl overflow-hidden border border-slate-200 shadow-xs mb-3">
                    <img
                      src="/assets/aistudio/Modern_Bengali_Telecom_Store_Banner_23b2899d.png"
                      alt="ইয়াছিন টেলিকম - ক্যাশ মেমো ব্যানার"
                      className="w-full h-auto object-cover block rounded-xl"
                      draggable={false}
                    />
                  </div>

                  {/* Memo info & Customer details (3 lines format) */}
                  <div className="mt-3.5 pt-2.5 border-t border-slate-300 space-y-2 text-xs">
                    {/* Line 1: উপরের লাইনে নাম */}
                    <div className="flex items-center gap-2">
                      <span className="bg-slate-900 text-white px-2.5 py-0.5 rounded font-bold text-[10px] tracking-wide shrink-0">
                        Name:
                      </span>
                      <span className="font-bold text-slate-900 border-b border-slate-300 flex-1 pb-0.5 text-xs sm:text-sm truncate">
                        {selectedMemo.customerName || 'নগদ ক্রেতা'}
                      </span>
                    </div>

                    {/* Line 2: দ্বিতীয় লাইনে ঠিকানা */}
                    <div className="flex items-center gap-2">
                      <span className="bg-slate-900 text-white px-2.5 py-0.5 rounded font-bold text-[10px] tracking-wide shrink-0">
                        Address:
                      </span>
                      <span className="text-slate-700 border-b border-slate-300 flex-1 pb-0.5 text-xs truncate">
                        {selectedMemo.customerAddress || 'বুল্লা বাজার'}
                        {selectedMemo.customerMobile ? ` | 📞 ${selectedMemo.customerMobile}` : ''}
                      </span>
                    </div>

                    {/* Line 3: তৃতীয় লাইনের প্রথমে সিরিয়াল নং শেষে তারিখ */}
                    <div className="flex items-center justify-between pt-0.5">
                      {/* প্রথমে সিরিয়াল নং */}
                      <div className="flex items-center gap-1.5">
                        <span className="bg-slate-900 text-white px-2 py-0.5 rounded font-bold text-[10px] tracking-wide shrink-0">
                          No:
                        </span>
                        <span className="font-black text-slate-900 text-xs sm:text-sm tracking-wider">
                          {selectedMemo.memoNumber}
                        </span>
                      </div>

                      {/* শেষে তারিখ */}
                      <div className="flex items-center gap-1.5">
                        <span className="bg-slate-900 text-white px-2 py-0.5 rounded font-bold text-[10px] tracking-wide shrink-0">
                          Date:
                        </span>
                        <div className="flex border border-slate-900 rounded font-bold text-[11px] overflow-hidden bg-white shadow-2xs">
                          {selectedMemo.date.split('-').map((part, i) => (
                            <span key={i} className="px-2 py-0.5 border-r last:border-r-0 border-slate-300 bg-slate-50">
                              {part}
                            </span>
                          ))}
                        </div>
                      </div>
                    </div>
                  </div>

                  {/* Items Table - Proportional Table-Fixed to Prevent Amount Cut-Off */}
                  <div className="mt-4 border border-slate-300 rounded-lg overflow-hidden bg-white">
                    <table className="w-full table-fixed text-left text-xs border-collapse">
                      <thead>
                        <tr className="bg-gradient-to-r from-orange-600 via-orange-700 to-red-800 text-white font-bold">
                          <th className="py-2 px-1.5 w-[8%] text-center border-r border-orange-700 text-[11px]">No:</th>
                          <th className="py-2 px-2.5 w-[46%] border-r border-orange-700 text-[11px]">Description (পণ্যের বিবরণ)</th>
                          <th className="py-2 px-1.5 w-[14%] text-center border-r border-orange-700 text-[11px]">Quantity</th>
                          <th className="py-2 px-2 w-[16%] text-right border-r border-orange-700 text-[11px]">Rate</th>
                          <th className="py-2 px-2 w-[16%] text-right text-[11px]">Amount</th>
                        </tr>
                      </thead>
                      <tbody className="divide-y divide-slate-200">
                        {selectedMemo.items.map((item, idx) => (
                          <tr key={idx} className="hover:bg-slate-50/50">
                            <td className="py-1.5 px-1.5 text-center text-slate-500 border-r border-slate-200 font-medium text-[11px]">
                              {idx + 1}
                            </td>
                            <td className="py-1.5 px-2.5 font-bold text-slate-800 border-r border-slate-200 text-[11px] break-words">
                              {item.description}
                            </td>
                            <td className="py-1.5 px-1.5 text-center bg-orange-50/40 border-r border-slate-200 font-semibold text-[11px]">
                              {item.quantity}
                            </td>
                            <td className="py-1.5 px-2 text-right border-r border-slate-200 font-medium text-[11px]">
                              {formatTaka(item.rate)}
                            </td>
                            <td className="py-1.5 px-2 text-right bg-orange-50/40 font-black text-orange-700 text-[11px]">
                              {formatTaka(item.amount)}
                            </td>
                          </tr>
                        ))}

                        {/* Minimum empty rows for authentic memo look */}
                        {Array.from({ length: Math.max(0, 4 - selectedMemo.items.length) }).map((_, i) => (
                          <tr key={`empty-${i}`} className="h-6">
                            <td className="border-r border-slate-200"></td>
                            <td className="border-r border-slate-200"></td>
                            <td className="border-r border-slate-200 bg-orange-50/20"></td>
                            <td className="border-r border-slate-200"></td>
                            <td className="bg-orange-50/20"></td>
                          </tr>
                        ))}

                        {/* Total Quantity Row */}
                        <tr className="border-t-2 border-slate-400 bg-slate-50/70">
                          <td colSpan={2} className="py-1.5 px-2 font-bold text-slate-700 text-right border-r border-slate-200 text-[11px]">
                            মোট আইটেম সংখ্যা (Total Quantity):
                          </td>
                          <td className="py-1.5 px-1.5 text-center font-black text-slate-900 border-r border-slate-200 bg-orange-50/60 text-[11px]">
                            {selectedMemo.items.reduce((acc, it) => acc + (it.quantity || 0), 0)} টি
                          </td>
                          <td className="py-1.5 px-2 text-right font-bold text-orange-700 bg-orange-50/80 border-r border-slate-200 text-[11px]">
                            Total
                          </td>
                          <td className="py-1.5 px-2 text-right font-black text-slate-900 bg-orange-50/80 text-[11px]">
                            {formatTaka(selectedMemo.subtotal)}
                          </td>
                        </tr>
                        {selectedMemo.discount > 0 && (
                          <tr>
                            <td colSpan={3} className="border-r border-slate-200"></td>
                            <td className="py-1.5 px-2 text-right font-bold text-slate-600 bg-slate-50 border-r border-slate-200 text-[11px]">
                              Discount
                            </td>
                            <td className="py-1.5 px-2 text-right font-black text-slate-800 bg-slate-50 text-[11px]">
                              -{formatTaka(selectedMemo.discount)}
                            </td>
                          </tr>
                        )}
                        <tr>
                          <td colSpan={3} className="border-r border-slate-200"></td>
                          <td className="py-1.5 px-2 text-right font-bold text-orange-700 bg-orange-50/80 border-r border-slate-200 text-[11px]">
                            Advance
                          </td>
                          <td className="py-1.5 px-2 text-right font-black text-emerald-700 bg-orange-50/80 text-[11px]">
                            {formatTaka(selectedMemo.paidAmount)}
                          </td>
                        </tr>
                        <tr>
                          <td colSpan={3} className="border-r border-slate-200"></td>
                          <td className="py-1.5 px-2 text-right font-bold text-orange-700 bg-orange-50/80 border-r border-slate-200 text-[11px]">
                            Due
                          </td>
                          <td className="py-1.5 px-2 text-right font-black text-red-600 bg-orange-50/80 text-[11px]">
                            {formatTaka(selectedMemo.dueAmount)}
                          </td>
                        </tr>
                      </tbody>
                    </table>
                  </div>

                  {/* Taka In Word */}
                  <div className="mt-4 text-xs font-semibold text-slate-800">
                    <span>Taka (In word): </span>
                    <span className="font-bold underline decoration-slate-300 underline-offset-4">
                      {takaInBengaliWords(selectedMemo.grandTotal)}
                    </span>
                  </div>
                </div>

                {/* Signatures & Footer Banner */}
                <div className="mt-12 pt-8">
                  <div className="flex items-center justify-between text-xs px-6">
                    <div className="text-center">
                      <div className="w-36 border-t border-slate-400 mb-1"></div>
                      <p className="font-bold text-slate-800">Customer Signature</p>
                      <p className="text-[10px] text-slate-400">(ক্রেতার স্বাক্ষর)</p>
                    </div>

                    <div className="text-center">
                      <div className="w-36 border-t border-slate-400 mb-1"></div>
                      <p className="font-bold text-slate-800">Authorized Signature</p>
                      <p className="text-[10px] text-slate-400">(বিক্রেতার স্বাক্ষর)</p>
                    </div>
                  </div>

                  {/* Bottom dynamic orange accent bar */}
                  <div className="mt-6 -mx-8 -mb-8 h-3 bg-gradient-to-r from-orange-600 via-amber-500 to-orange-600"></div>
                </div>
              </div>
            ) : memoStyle === 'THERMAL_RECEIPT' ? (
              /* COMPACT THERMAL POS RECEIPT MODE (58mm / 80mm) */
              <div className="p-6 max-w-sm mx-auto bg-white font-mono text-slate-900 border-x border-dashed border-slate-300">
                <div className="text-center pb-3 border-b-2 border-dashed border-slate-800">
                  <h2 className="text-lg font-black tracking-tight uppercase">{profile.shopName || 'YASIN TELECOM'}</h2>
                  <p className="text-xs font-bold">Owner: {profile.ownerName || 'Mohammad Yasin'}</p>
                  <p className="text-[11px]">{profile.address || 'Bulla Bazar, Lakhai, Habiganj'}</p>
                  <p className="text-xs font-bold">Mobile: {profile.phone || '01766906974'}</p>
                  <div className="mt-2 inline-block px-2.5 py-0.5 border border-slate-800 text-[10px] font-bold uppercase tracking-wider">
                    CASH RECEIPT
                  </div>
                </div>

                <div className="py-2.5 text-[11px] space-y-0.5 border-b border-dashed border-slate-300">
                  <div className="flex justify-between">
                    <span>Memo No:</span>
                    <span className="font-bold">{selectedMemo.memoNumber}</span>
                  </div>
                  <div className="flex justify-between">
                    <span>Date:</span>
                    <span>{selectedMemo.date} {selectedMemo.time}</span>
                  </div>
                  <div className="flex justify-between">
                    <span>Customer:</span>
                    <span className="font-bold truncate max-w-[150px]">{selectedMemo.customerName || 'Cash Customer'}</span>
                  </div>
                  {selectedMemo.customerMobile && (
                    <div className="flex justify-between">
                      <span>Phone:</span>
                      <span>{selectedMemo.customerMobile}</span>
                    </div>
                  )}
                </div>

                <div className="py-2.5 border-b-2 border-dashed border-slate-800">
                  <table className="w-full text-[11px]">
                    <thead>
                      <tr className="border-b border-slate-400">
                        <th className="text-left pb-1">Item</th>
                        <th className="text-center pb-1">Qty</th>
                        <th className="text-right pb-1">Rate</th>
                        <th className="text-right pb-1">Total</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-dotted divide-slate-200">
                      {selectedMemo.items.map((it, idx) => (
                        <tr key={idx} className="py-1">
                          <td className="py-1 text-left font-semibold">{it.description}</td>
                          <td className="py-1 text-center font-bold">{it.quantity}</td>
                          <td className="py-1 text-right">{Math.round(it.rate)}</td>
                          <td className="py-1 text-right font-bold">{Math.round(it.amount)}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>

                <div className="py-2.5 text-xs space-y-1 border-b border-dashed border-slate-300">
                  <div className="flex justify-between font-bold">
                    <span>Total Quantity:</span>
                    <span>{selectedMemo.items.reduce((acc, it) => acc + (it.quantity || 0), 0)} pcs</span>
                  </div>
                  <div className="flex justify-between">
                    <span>Subtotal:</span>
                    <span>{formatTaka(selectedMemo.subtotal)}</span>
                  </div>
                  {selectedMemo.discount > 0 && (
                    <div className="flex justify-between text-red-600">
                      <span>Discount:</span>
                      <span>-{formatTaka(selectedMemo.discount)}</span>
                    </div>
                  )}
                  <div className="flex justify-between font-black text-sm pt-1 border-t border-slate-300">
                    <span>Grand Total:</span>
                    <span>{formatTaka(selectedMemo.grandTotal)}</span>
                  </div>
                  <div className="flex justify-between font-bold text-emerald-700">
                    <span>Paid Amount:</span>
                    <span>{formatTaka(selectedMemo.paidAmount)}</span>
                  </div>
                  {selectedMemo.dueAmount > 0 && (
                    <div className="flex justify-between font-bold text-red-600">
                      <span>Remaining Due:</span>
                      <span>{formatTaka(selectedMemo.dueAmount)}</span>
                    </div>
                  )}
                </div>

                <div className="py-2 text-[10px] text-slate-700 italic border-b border-dashed border-slate-300">
                  <span className="font-bold not-italic">In Words: </span>
                  {takaInBengaliWords(selectedMemo.grandTotal)}
                </div>

                <div className="text-center pt-3 text-[11px] space-y-1">
                  <p className="font-semibold">{selectedMemo.notes || 'বিক্রিত মাল ফেরত নেওয়া হয় না।'}</p>
                  <p className="font-bold">Thank You! Please Visit Again.</p>
                  <p className="text-[10px] text-slate-400">Software: Yasin Telecom Smart Shop Manager</p>
                </div>
              </div>
            ) : (
              /* CYAN-BLUE MOBILE STYLE DESIGN (5.5 x 8.5 INCHES) */
              <div className="p-4 sm:p-5 relative bg-white min-h-[780px] flex flex-col justify-between text-slate-900">
                <div>
                  {/* Official Visiting Card Header */}
                  <div className="rounded-xl overflow-hidden border border-slate-200 shadow-xs mb-3">
                    <img
                      src="/assets/aistudio/Modern_Bengali_Telecom_Store_Banner_23b2899d.png"
                      alt="ইয়াছিন টেলিকম - ক্যাশ মেমো ব্যানার"
                      className="w-full h-auto object-cover block rounded-xl"
                      draggable={false}
                    />
                  </div>

                  {/* Customer details banner (3 lines format) */}
                  <div className="bg-sky-50/70 p-3 rounded-xl border border-sky-100 mt-4 space-y-2 text-xs">
                    {/* Line 1: উপরের লাইনে নাম */}
                    <div className="flex items-center gap-2">
                      <span className="font-bold text-sky-950 bg-sky-200/80 px-2 py-0.5 rounded text-[10px] shrink-0">নাম:</span>
                      <strong className="text-slate-900 border-b border-sky-200 flex-1 pb-0.5 text-xs sm:text-sm truncate">
                        {selectedMemo.customerName || 'নগদ ক্রেতা'}
                      </strong>
                    </div>

                    {/* Line 2: দ্বিতীয় লাইনে ঠিকানা */}
                    <div className="flex items-center gap-2">
                      <span className="font-bold text-sky-950 bg-sky-200/80 px-2 py-0.5 rounded text-[10px] shrink-0">ঠিকানা:</span>
                      <span className="text-slate-700 border-b border-sky-200 flex-1 pb-0.5 text-xs truncate">
                        {selectedMemo.customerAddress || 'বুল্লা বাজার'}
                        {selectedMemo.customerMobile ? ` | 📞 ${selectedMemo.customerMobile}` : ''}
                      </span>
                    </div>

                    {/* Line 3: তৃতীয় লাইনের প্রথমে সিরিয়াল নং শেষে তারিখ */}
                    <div className="flex items-center justify-between pt-0.5">
                      <div className="flex items-center gap-1.5">
                        <span className="font-bold text-sky-950 bg-sky-200/80 px-2 py-0.5 rounded text-[10px] shrink-0">মেমো নং:</span>
                        <strong className="text-sky-900 text-xs sm:text-sm tracking-wider">{selectedMemo.memoNumber}</strong>
                      </div>
                      <div className="flex items-center gap-1.5">
                        <span className="font-bold text-sky-950 bg-sky-200/80 px-2 py-0.5 rounded text-[10px] shrink-0">তারিখ:</span>
                        <span className="text-slate-800 font-bold bg-white px-2 py-0.5 rounded border border-sky-200 text-[11px]">{selectedMemo.date}</span>
                      </div>
                    </div>
                  </div>

                  {/* Items Table - Proportional Table-Fixed to Prevent Amount Cut-Off */}
                  <div className="mt-4 border border-sky-200 rounded-xl overflow-hidden bg-white">
                    <table className="w-full table-fixed text-xs border-collapse">
                      <thead className="bg-sky-600 text-white font-bold">
                        <tr>
                          <th className="py-2 px-1.5 w-[8%] text-center text-[11px]">নং</th>
                          <th className="py-2 px-2.5 w-[46%] text-left text-[11px]">বিবরণ</th>
                          <th className="py-2 px-1.5 w-[14%] text-center text-[11px]">পরিমাণ</th>
                          <th className="py-2 px-2 w-[16%] text-right text-[11px]">দর</th>
                          <th className="py-2 px-2 w-[16%] text-right text-[11px]">টাকা</th>
                        </tr>
                      </thead>
                      <tbody className="divide-y divide-sky-100">
                        {selectedMemo.items.map((it, idx) => (
                          <tr key={idx} className="hover:bg-sky-50/30">
                            <td className="py-1.5 px-1.5 text-center text-slate-500 text-[11px]">{idx + 1}</td>
                            <td className="py-1.5 px-2.5 font-semibold text-slate-800 text-[11px] break-words">{it.description}</td>
                            <td className="py-1.5 px-1.5 text-center font-bold text-slate-700 text-[11px]">{it.quantity}</td>
                            <td className="py-1.5 px-2 text-right text-[11px]">{formatTaka(it.rate)}</td>
                            <td className="py-1.5 px-2 text-right font-black text-sky-700 text-[11px]">{formatTaka(it.amount)}</td>
                          </tr>
                        ))}

                        {/* Total Quantity */}
                        <tr className="bg-sky-50 font-bold border-t border-sky-300">
                          <td colSpan={2} className="py-1.5 px-2 text-right text-slate-700 text-[11px]">
                            মোট আইটেম সংখ্যা:
                          </td>
                          <td className="py-1.5 px-1.5 text-center text-sky-800 font-black text-[11px]">
                            {selectedMemo.items.reduce((acc, it) => acc + (it.quantity || 0), 0)} টি
                          </td>
                          <td className="py-1.5 px-2 text-right text-slate-700 text-[11px]">উপ-মোট:</td>
                          <td className="py-1.5 px-2 text-right font-black text-slate-900 text-[11px]">{formatTaka(selectedMemo.subtotal)}</td>
                        </tr>

                        {selectedMemo.discount > 0 && (
                          <tr>
                            <td colSpan={3}></td>
                            <td className="py-1.5 px-2 text-right text-slate-600 text-[11px]">ছাড়:</td>
                            <td className="py-1.5 px-2 text-right font-bold text-slate-800 text-[11px]">-{formatTaka(selectedMemo.discount)}</td>
                          </tr>
                        )}
                        <tr>
                          <td colSpan={3}></td>
                          <td className="py-1.5 px-2 text-right font-bold text-sky-800 text-[11px]">সর্বমোট:</td>
                          <td className="py-1.5 px-2 text-right font-black text-sky-900 text-xs sm:text-sm">{formatTaka(selectedMemo.grandTotal)}</td>
                        </tr>
                        <tr>
                          <td colSpan={3}></td>
                          <td className="py-1.5 px-3 text-right font-semibold text-emerald-700">জমা:</td>
                          <td className="py-1.5 px-3 text-right font-black text-emerald-700">{formatTaka(selectedMemo.paidAmount)}</td>
                        </tr>
                        {selectedMemo.dueAmount > 0 && (
                          <tr>
                            <td colSpan={3}></td>
                            <td className="py-1.5 px-3 text-right font-bold text-red-600">বাকি:</td>
                            <td className="py-1.5 px-3 text-right font-black text-red-600">{formatTaka(selectedMemo.dueAmount)}</td>
                          </tr>
                        )}
                      </tbody>
                    </table>
                  </div>

                  <div className="mt-3 text-xs text-slate-700">
                    <span>কথায়: </span>
                    <strong className="underline decoration-slate-300">{takaInBengaliWords(selectedMemo.grandTotal)}</strong>
                  </div>
                </div>

                <div className="mt-12 pt-6 border-t border-sky-100 flex items-center justify-between text-xs">
                  <span className="font-semibold text-slate-500">ক্রেতার স্বাক্ষর</span>
                  <span className="font-bold text-sky-800">স্বত্বাধিকারীর স্বাক্ষর</span>
                </div>
              </div>
            )}
          </div>
        </div>
      )}

      {/* VIEW 3: Cash Memo History List */}
      {activeView === 'HISTORY' && (
        <div className="space-y-3 no-print">
          <div className="flex items-center justify-between">
            <h2 className="text-base font-bold text-slate-800">সংরক্ষিত মেমোর তালিকা</h2>
            <button
              type="button"
              onClick={() => {
                resetForm();
                setActiveView('FORM');
              }}
              className="flex items-center gap-1 px-3 py-1.5 bg-orange-600 hover:bg-orange-700 text-white text-xs font-bold rounded-xl shadow transition"
            >
              <Plus className="w-4 h-4" /> নতুন মেমো
            </button>
          </div>

          {cashMemos.length === 0 ? (
            <div className="bg-white rounded-2xl p-8 text-center border border-slate-200 text-slate-500">
              <FileText className="w-10 h-10 mx-auto text-slate-300 mb-2" />
              <p className="font-semibold text-sm">কোনো ক্যাশ মেমো সংরক্ষিত নেই!</p>
            </div>
          ) : (
            cashMemos.map((memo) => {
              const memoTotalQty = memo.items.reduce((acc, it) => acc + (it.quantity || 0), 0);
              return (
                <div
                  key={memo.id}
                  className="bg-white rounded-2xl p-4 border border-slate-200 shadow-2xs hover:shadow-xs transition"
                >
                  <div className="flex items-start justify-between">
                    <div>
                      <div className="flex items-center gap-2">
                        <span className="font-black text-xs text-orange-600 bg-orange-50 px-2 py-0.5 rounded border border-orange-200">
                          {memo.memoNumber}
                        </span>
                        <span className="text-xs text-slate-400">{memo.date} ({memo.time})</span>
                      </div>
                      <h4 className="font-bold text-slate-900 text-sm mt-1">
                        {memo.customerName || 'নগদ ক্রেতা'}
                        {memo.customerMobile && (
                          <span className="text-xs font-normal text-slate-500 ml-1.5">
                            📞 {memo.customerMobile}
                          </span>
                        )}
                      </h4>
                    </div>

                    <div className="text-right">
                      <span className="text-base font-black text-slate-900 block">{formatTaka(memo.grandTotal)}</span>
                      <span
                        className={`text-[10px] font-bold px-2 py-0.5 rounded-full ${
                          memo.paymentStatus === 'PAID'
                            ? 'bg-emerald-100 text-emerald-800'
                            : memo.paymentStatus === 'DUE'
                            ? 'bg-red-100 text-red-800'
                            : 'bg-amber-100 text-amber-800'
                        }`}
                      >
                        {memo.paymentStatus === 'PAID' ? 'পরিশোধিত' : memo.paymentStatus === 'DUE' ? 'বাকি' : 'আংশিক জমা'}
                      </span>
                    </div>
                  </div>

                  <div className="mt-2 text-xs text-slate-500 flex items-center justify-between">
                    <span className="truncate max-w-[70%]">
                      পণ্য: {memo.items.map((it) => `${it.description} x${it.quantity}`).join(', ')}
                    </span>
                    <span className="text-[11px] font-bold text-slate-600 bg-slate-100 px-2 py-0.5 rounded-full shrink-0">
                      মোট: {memoTotalQty} টি
                    </span>
                  </div>

                  <div className="flex items-center justify-between mt-3 pt-2.5 border-t border-slate-100">
                    <div className="flex items-center gap-3 text-xs">
                      <span className="text-slate-500">জমা: <strong className="text-emerald-700">{formatTaka(memo.paidAmount)}</strong></span>
                      {memo.dueAmount > 0 && (
                        <span className="text-red-600 font-bold">বাকি: {formatTaka(memo.dueAmount)}</span>
                      )}
                    </div>

                    {/* Quick action buttons: View, WhatsApp, Copy (Deletions in Admin Panel) */}
                    <div className="flex items-center gap-1.5">
                      <button
                        type="button"
                        onClick={() => {
                          setSelectedMemo(memo);
                          setActiveView('VIEW');
                        }}
                        className="px-3 py-1 rounded-xl bg-orange-50 hover:bg-orange-100 text-orange-700 font-bold text-xs flex items-center gap-1 transition"
                      >
                        <Eye className="w-3.5 h-3.5" /> মেমো দেখুন
                      </button>
                      <button
                        type="button"
                        onClick={() => handleShareWhatsApp(memo)}
                        className="p-1.5 text-emerald-600 hover:bg-emerald-50 rounded-lg transition"
                        title="WhatsApp এ পাঠান"
                      >
                        <Share2 className="w-4 h-4" />
                      </button>
                      <button
                        type="button"
                        onClick={() => handleCopyReceipt(memo)}
                        className="p-1.5 text-slate-500 hover:bg-slate-100 rounded-lg transition"
                        title="টেক্সট কপি করুন"
                      >
                        <Copy className="w-4 h-4" />
                      </button>
                    </div>
                  </div>
                </div>
              );
            })
          )}
        </div>
      )}

      {/* PDF Generation Loader Overlay */}
      {isGeneratingPdf && (
        <div className="fixed inset-0 z-50 bg-slate-900/50 backdrop-blur-2xs flex items-center justify-center p-4 no-print animate-in fade-in">
          <div className="bg-white rounded-2xl shadow-2xl p-5 flex items-center gap-3.5 border border-slate-200 max-w-sm w-full">
            <Loader2 className="w-7 h-7 text-orange-600 animate-spin shrink-0" />
            <div>
              <p className="text-xs font-bold text-slate-900">ক্যাশ মেমো PDF তৈরি হচ্ছে...</p>
            </div>
          </div>
        </div>
      )}

      {/* Instant PDF Viewer Modal */}
      {showPdfModal && (pdfPreviewImg || pdfPreviewUrl) && selectedMemo && (
        <div className="fixed inset-0 z-50 bg-slate-950/80 backdrop-blur-xs flex items-center justify-center p-2 sm:p-4 no-print animate-in fade-in">
          <div className="bg-white rounded-3xl shadow-2xl border border-slate-200 w-full max-w-3xl flex flex-col max-h-[95vh] overflow-hidden">
            {/* Modal Top Bar */}
            <div className="p-3.5 sm:p-4 bg-slate-900 text-white flex items-center justify-between gap-3 border-b border-slate-800">
              <div>
                <h3 className="text-base sm:text-lg font-bold text-white tracking-wide">
                  PDF View
                </h3>
              </div>

              {/* Action Buttons in PDF View Modal */}
              <div className="flex items-center gap-1.5 sm:gap-2">
                <button
                  type="button"
                  onClick={() => {
                    if (pdfInstance) {
                      pdfInstance.save(`CashMemo_${selectedMemo.memoNumber}.pdf`);
                      showMessage('ক্যাশ মেমো PDF ডাউনলোড হয়েছে!');
                    }
                  }}
                  className="flex items-center gap-1 px-3 py-1.5 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-200 text-xs font-bold transition border border-slate-700"
                  title="PDF ডাউনলোড করুন"
                >
                  <Download className="w-3.5 h-3.5" />
                  <span className="hidden sm:inline">ডাউনলোড</span>
                </button>

                <button
                  type="button"
                  disabled={isSharingWhatsApp}
                  onClick={() => handleShareWhatsAppPdf(selectedMemo)}
                  className="flex items-center gap-1 px-3 py-1.5 rounded-xl bg-emerald-600 hover:bg-emerald-500 text-white text-xs font-bold transition shadow-xs disabled:opacity-50"
                  title="হোয়াটসঅ্যাপে পাঠান"
                >
                  {isSharingWhatsApp ? (
                    <Loader2 className="w-3.5 h-3.5 animate-spin" />
                  ) : (
                    <Share2 className="w-3.5 h-3.5" />
                  )}
                  <span className="hidden sm:inline">WhatsApp</span>
                </button>

                <button
                  type="button"
                  onClick={() => window.print()}
                  className="flex items-center gap-1 px-3 py-1.5 rounded-xl bg-blue-600 hover:bg-blue-500 text-white text-xs font-bold transition shadow-xs"
                  title="প্রিন্ট করুন"
                >
                  <Printer className="w-3.5 h-3.5" />
                  <span className="hidden sm:inline">প্রিন্ট</span>
                </button>

                <button
                  type="button"
                  onClick={() => setShowPdfModal(false)}
                  className="p-1.5 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-400 hover:text-white transition ml-1"
                  title="বন্ধ করুন"
                >
                  <X className="w-4 h-4" />
                </button>
              </div>
            </div>

            {/* Modal Body - Crisp Visual Document Preview */}
            <div className="flex-1 bg-slate-200/70 p-3 sm:p-5 overflow-auto flex items-center justify-center min-h-[480px]">
              {pdfPreviewImg ? (
                <div className="bg-white rounded-xl shadow-2xl overflow-hidden border border-slate-300 w-full max-w-[5.5in] my-auto">
                  <img
                    src={pdfPreviewImg}
                    alt={`ক্যাশ মেমো ${selectedMemo.memoNumber} PDF`}
                    className="w-full h-auto object-contain block select-none"
                    draggable={false}
                  />
                </div>
              ) : pdfPreviewUrl ? (
                <iframe
                  src={pdfPreviewUrl}
                  className="w-full h-[65vh] sm:h-[70vh] rounded-2xl shadow-inner border border-slate-300 bg-white"
                  title={`ক্যাশ মেমো ${selectedMemo.memoNumber} PDF`}
                />
              ) : null}
            </div>

            {/* Modal Footer */}
            <div className="p-3 bg-white border-t border-slate-200 flex items-center justify-end">
              <button
                type="button"
                onClick={() => setShowPdfModal(false)}
                className="px-4 py-1.5 rounded-xl bg-slate-200 hover:bg-slate-300 text-slate-700 font-bold text-xs transition"
              >
                বন্ধ করুন
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Customer WhatsApp Mobile Number Modal (Requirement 3: যে গ্রাহকের নাম্বারে হোয়াটসঅ্যাপে পিডিএফ শুধু যাবে) */}
      {showWhatsAppPromptModal && (
        <div className="fixed inset-0 z-50 bg-slate-950/70 backdrop-blur-xs flex items-center justify-center p-4 no-print animate-in fade-in">
          <div className="bg-white rounded-3xl shadow-2xl border border-slate-200 max-w-sm w-full p-5 space-y-4">
            <div className="flex items-center justify-between pb-3 border-b border-slate-100">
              <div className="flex items-center gap-2.5">
                <div className="w-10 h-10 rounded-xl bg-emerald-100 text-emerald-600 flex items-center justify-center">
                  <Smartphone className="w-5 h-5" />
                </div>
                <div>
                  <h3 className="font-bold text-slate-900 text-sm">হোয়াটসঅ্যাপ নম্বর দিন</h3>
                  <p className="text-[11px] text-slate-500">মেমোর PDF সরাসরি গ্রাহকের নম্বরে যাবে</p>
                </div>
              </div>
              <button
                type="button"
                onClick={() => setShowWhatsAppPromptModal(false)}
                className="text-slate-400 hover:text-slate-600 p-1"
              >
                <X className="w-4 h-4" />
              </button>
            </div>

            <div>
              <label className="block text-xs font-bold text-slate-700 mb-1.5">
                গ্রাহকের মোবাইল/হোয়াটসঅ্যাপ নম্বর:
              </label>
              <div className="relative">
                <Phone className="w-4 h-4 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2" />
                <input
                  type="text"
                  autoFocus
                  value={whatsAppInputMobile}
                  onChange={(e) => setWhatsAppInputMobile(e.target.value)}
                  placeholder="017xxxxxxxx"
                  className="w-full pl-9 pr-3 py-2 border border-slate-300 rounded-xl text-sm font-semibold focus:ring-2 focus:ring-emerald-500 focus:outline-none"
                  onKeyDown={(e) => {
                    if (e.key === 'Enter' && whatsAppInputMobile.trim() && pendingShareMemo) {
                      handleShareWhatsAppPdf(pendingShareMemo, whatsAppInputMobile.trim());
                    }
                  }}
                />
              </div>
            </div>

            <div className="flex items-center justify-end gap-2 pt-2">
              <button
                type="button"
                onClick={() => setShowWhatsAppPromptModal(false)}
                className="px-4 py-2 border border-slate-200 rounded-xl font-semibold text-slate-600 hover:bg-slate-50 text-xs"
              >
                বাতিল
              </button>
              <button
                type="button"
                disabled={!whatsAppInputMobile.trim() || isSharingWhatsApp}
                onClick={() => {
                  if (pendingShareMemo && whatsAppInputMobile.trim()) {
                    handleShareWhatsAppPdf(pendingShareMemo, whatsAppInputMobile.trim());
                  }
                }}
                className="flex items-center gap-1.5 px-5 py-2 bg-emerald-600 hover:bg-emerald-700 text-white rounded-xl font-bold shadow-md text-xs transition disabled:opacity-50"
              >
                {isSharingWhatsApp ? (
                  <Loader2 className="w-3.5 h-3.5 animate-spin" />
                ) : (
                  <Send className="w-3.5 h-3.5" />
                )}
                <span>PDF পাঠান</span>
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
