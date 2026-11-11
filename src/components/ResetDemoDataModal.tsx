import React, { useState } from 'react';
import { AlertTriangle, ShieldCheck, CheckCircle2, Lock, X, RotateCcw } from 'lucide-react';
import { useShop } from '../context/ShopContext';

interface ResetDemoDataModalProps {
  isOpen: boolean;
  onClose: () => void;
}

export const ResetDemoDataModal: React.FC<ResetDemoDataModalProps> = ({ isOpen, onClose }) => {
  const { profile, getDemoCounts, resetDemoData, showMessage, showError } = useShop();

  const [step, setStep] = useState<1 | 2 | 3>(1);
  const [pin, setPin] = useState('');
  const [isAuthorized, setIsAuthorized] = useState(false);
  const [confirmedCheckbox, setConfirmedCheckbox] = useState(false);

  if (!isOpen) return null;

  const demoCounts = getDemoCounts();

  const handleVerifyPin = (e: React.FormEvent) => {
    e.preventDefault();
    if (pin.trim() === (profile.adminPin || '1234')) {
      setIsAuthorized(true);
      setStep(2);
      setPin('');
    } else {
      showError('ভুল অ্যাডমিন পিন! সঠিক পিন দিন।');
    }
  };

  const handleProceedToFinal = () => {
    setStep(3);
  };

  const handleFinalReset = () => {
    const removedCount = resetDemoData();
    showMessage(`${removedCount} টি ডেমো রেকর্ড সফলভাবে মুছে ফেলা হয়েছে! আপনার আসল সকল ডাটা অক্ষত আছে।`);
    onClose();
    setStep(1);
    setConfirmedCheckbox(false);
    setIsAuthorized(false);
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/70 backdrop-blur-xs">
      <div className="bg-white rounded-2xl max-w-md w-full shadow-2xl border border-slate-200 overflow-hidden animate-in fade-in zoom-in-95 duration-200">
        {/* Header */}
        <div className="bg-gradient-to-r from-red-600 to-rose-700 text-white p-4 flex items-center justify-between">
          <div className="flex items-center gap-2">
            <div className="w-8 h-8 rounded-full bg-white/20 flex items-center justify-center">
              <RotateCcw className="w-4 h-4 text-white" />
            </div>
            <div>
              <h3 className="font-bold text-sm sm:text-base">ডেমো ডাটা রিসেট (Demo Data Reset)</h3>
              <p className="text-[11px] text-red-100">৩-ধাপের নিরাপদ যাচাইকরণ প্রক্রিয়া</p>
            </div>
          </div>
          <button
            type="button"
            onClick={onClose}
            className="p-1 rounded-full text-white/80 hover:text-white hover:bg-white/10"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Progress indicator */}
        <div className="flex items-center justify-between px-6 py-2.5 bg-slate-50 border-b border-slate-200 text-xs">
          <div className={`flex items-center gap-1.5 font-bold ${step >= 1 ? 'text-red-600' : 'text-slate-400'}`}>
            <span className="w-5 h-5 rounded-full bg-red-100 text-red-700 flex items-center justify-center text-[10px]">1</span>
            <span>পিন যাচাই</span>
          </div>
          <div className="h-0.5 w-8 bg-slate-200" />
          <div className={`flex items-center gap-1.5 font-bold ${step >= 2 ? 'text-red-600' : 'text-slate-400'}`}>
            <span className="w-5 h-5 rounded-full bg-red-100 text-red-700 flex items-center justify-center text-[10px]">2</span>
            <span>সতর্কতা ও তালিকা</span>
          </div>
          <div className="h-0.5 w-8 bg-slate-200" />
          <div className={`flex items-center gap-1.5 font-bold ${step === 3 ? 'text-red-600' : 'text-slate-400'}`}>
            <span className="w-5 h-5 rounded-full bg-red-100 text-red-700 flex items-center justify-center text-[10px]">3</span>
            <span>চূড়ান্ত নিশ্চিতকরণ</span>
          </div>
        </div>

        <div className="p-5 text-xs sm:text-sm">
          {/* STEP 1: Admin PIN Verification */}
          {step === 1 && (
            <form onSubmit={handleVerifyPin} className="space-y-4">
              <div className="text-center py-2">
                <div className="w-12 h-12 rounded-full bg-red-50 text-red-600 flex items-center justify-center mx-auto mb-2 border border-red-200">
                  <Lock className="w-6 h-6" />
                </div>
                <h4 className="font-bold text-slate-800 text-sm sm:text-base">অ্যাডমিন অনুমতি প্রয়োজন</h4>
                <p className="text-xs text-slate-500 mt-1">
                  ডেমো ডাটা রিসেট করার পূর্বে অ্যাডমিন পিন কোড প্রবেশ করান।
                </p>
              </div>

              <div>
                <label className="block font-semibold text-slate-700 mb-1 text-center">
                  অ্যাডমিন পিন (ডিফল্ট: 1234)
                </label>
                <input
                  type="password"
                  required
                  autoFocus
                  maxLength={6}
                  value={pin}
                  onChange={(e) => setPin(e.target.value)}
                  placeholder="••••"
                  className="w-full text-center text-xl font-bold tracking-widest px-3 py-2.5 border border-slate-300 rounded-xl focus:ring-2 focus:ring-red-500 focus:outline-none"
                />
              </div>

              <div className="flex items-center justify-end gap-2 pt-2 border-t border-slate-100">
                <button
                  type="button"
                  onClick={onClose}
                  className="px-4 py-2 border border-slate-200 rounded-xl font-semibold text-slate-600 hover:bg-slate-50"
                >
                  বাতিল
                </button>
                <button
                  type="submit"
                  className="px-5 py-2 bg-red-600 hover:bg-red-700 text-white rounded-xl font-bold shadow transition"
                >
                  পরবর্তী ধাপ (১/৩) →
                </button>
              </div>
            </form>
          )}

          {/* STEP 2: Warning & Data Impact Review */}
          {step === 2 && (
            <div className="space-y-4">
              <div className="p-3.5 bg-amber-50 border border-amber-200 rounded-xl flex items-start gap-3">
                <AlertTriangle className="w-5 h-5 text-amber-600 shrink-0 mt-0.5" />
                <div>
                  <h4 className="font-bold text-amber-900 text-xs sm:text-sm">
                    নিরাপদ রিসেট নিশ্চয়তা (Data Safety Guarantee)
                  </h4>
                  <p className="text-[11px] text-amber-800 mt-0.5 leading-relaxed">
                    এই প্রক্রিয়ায় <strong>শুধুমাত্র ডেমো রেকর্ডগুলো</strong> মুছে ফেলা হবে।
                    আপনার হাতে তৈরি করা কোনো আসল পণ্য, বিক্রয়, কাস্টমার বা খরচের হিসাব মোছা হবে না।
                  </p>
                </div>
              </div>

              {/* Demo items count list */}
              <div className="p-3 bg-slate-50 border border-slate-200 rounded-xl space-y-1.5 text-xs">
                <h5 className="font-bold text-slate-700 pb-1 border-b border-slate-200">
                  যেসব ডেমো ডাটা অপসারণ করা হবে ({demoCounts.total} টি রেকর্ড):
                </h5>
                <div className="grid grid-cols-2 gap-x-2 gap-y-1 text-slate-600">
                  <span>• ডেমো পণ্য: <strong>{demoCounts.products} টি</strong></span>
                  <span>• ডেমো বিক্রয়: <strong>{demoCounts.sales} টি</strong></span>
                  <span>• ডেমো কাস্টমার: <strong>{demoCounts.customers} জন</strong></span>
                  <span>• ডেমো খরচ: <strong>{demoCounts.expenses} টি</strong></span>
                  <span>• ডেমো ঋণ: <strong>{demoCounts.loans} টি</strong></span>
                  <span>• ডেমো ব্যাংকিং: <strong>{demoCounts.mobileBanking} টি</strong></span>
                  <span>• ডেমো ফ্লেক্সি: <strong>{demoCounts.flexiload} টি</strong></span>
                  <span>• ডেমো মেমো: <strong>{demoCounts.memos} টি</strong></span>
                </div>
              </div>

              <div className="flex items-center justify-between pt-2 border-t border-slate-100">
                <button
                  type="button"
                  onClick={() => setStep(1)}
                  className="px-4 py-2 border border-slate-200 rounded-xl font-semibold text-slate-600 hover:bg-slate-50"
                >
                  ← পূর্বে
                </button>
                <button
                  type="button"
                  onClick={handleProceedToFinal}
                  className="px-5 py-2 bg-amber-600 hover:bg-amber-700 text-white rounded-xl font-bold shadow transition"
                >
                  শর্ত বুঝলাম, এগিয়ে যান (২/৩) →
                </button>
              </div>
            </div>
          )}

          {/* STEP 3: Final Confirmation Click */}
          {step === 3 && (
            <div className="space-y-4">
              <div className="text-center py-2">
                <div className="w-12 h-12 rounded-full bg-red-100 text-red-600 flex items-center justify-center mx-auto mb-2 border border-red-200">
                  <ShieldCheck className="w-6 h-6" />
                </div>
                <h4 className="font-black text-slate-900 text-base">চূড়ান্ত নিশ্চিতকরণ (Final Step)</h4>
                <p className="text-xs text-slate-500 mt-1">
                  আপনি ৩য় ও শেষ ধাপে আছেন। বোতাম চাপার সাথে সাথেই {demoCounts.total} টি ডেমো রেকর্ড অপসারিত হবে।
                </p>
              </div>

              <label className="flex items-start gap-2.5 p-3 bg-red-50 border border-red-200 rounded-xl cursor-pointer">
                <input
                  type="checkbox"
                  checked={confirmedCheckbox}
                  onChange={(e) => setConfirmedCheckbox(e.target.checked)}
                  className="w-4 h-4 text-red-600 rounded mt-0.5 focus:ring-red-500"
                />
                <span className="text-xs font-semibold text-red-900 leading-snug">
                  আমি নিশ্চিত করছি যে শুধুমাত্র নমুনা ডেমো ডাটা রিসেট করতে চাই এবং আমি এর প্রভাব অবগত।
                </span>
              </label>

              <div className="flex items-center justify-between pt-2 border-t border-slate-100">
                <button
                  type="button"
                  onClick={() => setStep(2)}
                  className="px-4 py-2 border border-slate-200 rounded-xl font-semibold text-slate-600 hover:bg-slate-50"
                >
                  ← পূর্বে
                </button>
                <button
                  type="button"
                  disabled={!confirmedCheckbox}
                  onClick={handleFinalReset}
                  className="px-5 py-2 bg-red-600 hover:bg-red-700 disabled:bg-slate-300 text-white rounded-xl font-extrabold shadow-lg transition"
                >
                  হ্যাঁ, ডেমো ডাটা রিসেট সম্পন্ন করুন (৩/৩)
                </button>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
