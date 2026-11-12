import React, { useState } from 'react';
import { Smartphone, Download, CheckCircle2, X, ExternalLink, ShieldCheck, Sparkles, Share2 } from 'lucide-react';
import { usePWAInstall } from '../hooks/usePWAInstall';

interface AndroidInstallModalProps {
  isOpen: boolean;
  onClose: () => void;
}

export const AndroidInstallModal: React.FC<AndroidInstallModalProps> = ({ isOpen, onClose }) => {
  const { isInstallable, isInstalled, isAndroid, isIOS, install } = usePWAInstall();
  const [copiedLink, setCopiedLink] = useState(false);

  if (!isOpen) return null;

  // Direct Shared Public App URL so scanning QR code opens the live app on any phone
  const SHARED_APP_URL = 'https://ais-pre-dhmo3cdpayzagb277suixi-495130326197.asia-southeast1.run.app';

  const currentAppUrl = (() => {
    try {
      const origin = window.location.origin;
      if (origin && origin.includes('.run.app') && !origin.includes('aistudio.google.com')) {
        return origin;
      }
    } catch {
      // fallback
    }
    return SHARED_APP_URL;
  })();

  const handleCopyLink = () => {
    navigator.clipboard.writeText(currentAppUrl);
    setCopiedLink(true);
    setTimeout(() => setCopiedLink(false), 3000);
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-3 sm:p-4 bg-slate-950/80 backdrop-blur-xs animate-in fade-in">
      <div className="bg-white rounded-3xl max-w-md w-full shadow-2xl border border-slate-200 overflow-hidden flex flex-col max-h-[92vh]">
        {/* Header */}
        <div className="bg-gradient-to-r from-emerald-600 via-teal-700 to-slate-900 text-white p-4 sm:p-5 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="w-11 h-11 rounded-2xl bg-white/20 flex items-center justify-center shadow-inner">
              <Smartphone className="w-6 h-6 text-emerald-300" />
            </div>
            <div>
              <h3 className="font-extrabold text-base sm:text-lg tracking-tight">
                অ্যান্ড্রয়েড ফোনে অ্যাপ ইন্সটল
              </h3>
              <p className="text-xs text-emerald-200">
                সরাসরি মোবাইল অ্যাপ হিসেবে চালান
              </p>
            </div>
          </div>
          <button
            type="button"
            onClick={onClose}
            className="p-1.5 rounded-xl bg-white/10 hover:bg-white/20 text-white transition"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Modal Body */}
        <div className="p-4 sm:p-5 space-y-4 overflow-y-auto text-xs sm:text-sm text-slate-700">
          {/* Status Badge */}
          {isInstalled ? (
            <div className="p-3.5 bg-emerald-50 rounded-2xl border border-emerald-200 flex items-center gap-2.5 text-emerald-800">
              <CheckCircle2 className="w-5 h-5 text-emerald-600 shrink-0" />
              <span className="font-bold">
                অ্যাপটি ইতোমধ্যেই আপনার ফোনে সফলভাবে ইন্সটল রয়েছে!
              </span>
            </div>
          ) : (
            <div className="p-3.5 bg-amber-50 rounded-2xl border border-amber-200 flex items-start gap-2.5 text-amber-900">
              <Sparkles className="w-5 h-5 text-amber-600 shrink-0 mt-0.5" />
              <div>
                <p className="font-bold text-xs sm:text-sm">
                  ফোনে অ্যাপের মতো এক ক্লিকে চালান
                </p>
                <p className="text-[11px] text-amber-800 mt-0.5">
                  কোনো ভারী স্টোরেজ খরচ হবে না, অফলাইনেও দ্রুত চলবে এবং হোম স্ক্রিনে অফিসিয়াল আইকন যুক্ত হবে।
                </p>
              </div>
            </div>
          )}

          {/* Option 1: Direct 1-Click Install Button (Android WebAPK) */}
          <div className="bg-slate-50 p-4 rounded-2xl border border-slate-200 space-y-3">
            <div className="flex items-center justify-between">
              <span className="font-extrabold text-slate-900 text-xs sm:text-sm flex items-center gap-1.5">
                <span className="w-5 h-5 rounded-full bg-emerald-600 text-white flex items-center justify-center text-[10px]">১</span>
                মোবাইলে খুলতে লিংক বা কিউআর কোড:
              </span>
              <span className="text-[10px] font-bold text-emerald-700 bg-emerald-100 px-2 py-0.5 rounded-full">
                সরাসরি লিংক
              </span>
            </div>

            {/* Direct Link Field with Copy */}
            <div className="flex items-center gap-2 bg-white p-1.5 rounded-xl border border-slate-200">
              <input
                type="text"
                readOnly
                value={currentAppUrl}
                className="flex-1 bg-transparent text-[11px] font-mono text-slate-700 px-2 py-1 outline-none truncate select-all"
              />
              <button
                type="button"
                onClick={handleCopyLink}
                className="px-3 py-1.5 bg-emerald-600 hover:bg-emerald-700 text-white rounded-lg font-bold text-xs flex items-center gap-1 shrink-0 transition"
              >
                {copiedLink ? <CheckCircle2 className="w-3.5 h-3.5" /> : <Share2 className="w-3.5 h-3.5" />}
                <span>{copiedLink ? 'কপি হয়েছে!' : 'কপি করুন'}</span>
              </button>
            </div>

            {/* Instant QR Code for Phone Scanning */}
            <div className="pt-1 text-center bg-white p-3 rounded-xl border border-slate-200">
              <p className="font-bold text-slate-800 text-xs mb-2">
                📱 মোবাইল ক্যামেরা দিয়ে স্ক্যান করে ফোনে খুলুন:
              </p>
              <div className="w-40 h-40 mx-auto bg-slate-50 p-2 rounded-xl border border-slate-200 shadow-inner flex items-center justify-center">
                <img
                  src={`https://api.qrserver.com/v1/create-qr-code/?size=250x250&margin=6&data=${encodeURIComponent(
                    currentAppUrl
                  )}`}
                  alt="মোবাইলে খোলার QR কোড"
                  className="w-36 h-36 block rounded-lg"
                  loading="eager"
                />
              </div>
              <p className="text-[10px] text-emerald-700 font-semibold mt-2">
                ক্যামেরা অন করে কিউআর কোডটি ধরলেই সরাসরি অ্যাপটি খুলে যাবে
              </p>
            </div>

            {isInstallable && (
              <button
                type="button"
                onClick={async () => {
                  const success = await install();
                  if (success) onClose();
                }}
                className="w-full py-3 px-4 bg-gradient-to-r from-emerald-600 to-teal-600 hover:from-emerald-700 hover:to-teal-700 text-white font-black rounded-xl shadow-md transition flex items-center justify-center gap-2 text-sm cursor-pointer active:scale-98"
              >
                <Download className="w-4 h-4" />
                <span>ফোনে এখনই ইন্সটল করুন</span>
              </button>
            )}
          </div>

          {/* Option 2: Manual Chrome Steps for Android */}
          <div className="bg-white p-3.5 rounded-2xl border border-slate-200 space-y-2">
            <h4 className="font-bold text-slate-900 text-xs flex items-center gap-1.5">
              <span className="w-5 h-5 rounded-full bg-slate-800 text-white flex items-center justify-center text-[10px]">২</span>
              অ্যান্ড্রয়েড ফোনে ম্যানুয়াল ইন্সটল নিয়ম:
            </h4>
            <ol className="list-decimal list-inside space-y-1.5 text-xs text-slate-600 pl-1 font-medium">
              <li>অ্যান্ড্রয়েড ফোনে <strong>Google Chrome</strong> ব্রাউজারে লিংকটি খুলুন।</li>
              <li>উপরে ডানদিকের <strong>৩-ডট মেনু (⋮)</strong> বাটনে চাপ দিন।</li>
              <li>তালিকা থেকে <strong>&quot;Install app&quot;</strong> বা <strong>&quot;Add to Home screen&quot;</strong> সিলেক্ট করুন।</li>
              <li>সাথে সাথে অ্যাপ আইকন ও স্প্ল্যাশ স্ক্রিনসহ আপনার ফোনে অ্যাপটি ইনস্টল হয়ে যাবে।</li>
            </ol>
          </div>

          {/* Option 3: Convert to standalone .APK file via PWABuilder */}
          <div className="bg-sky-50/70 p-3.5 rounded-2xl border border-sky-200 space-y-2">
            <h4 className="font-bold text-sky-950 text-xs flex items-center gap-1.5">
              <span className="w-5 h-5 rounded-full bg-sky-700 text-white flex items-center justify-center text-[10px]">৩</span>
              স্ট্যান্ডঅ্যালোন .APK ফাইল তৈরি করতে চান?
            </h4>
            <p className="text-[11px] text-sky-800 leading-relaxed">
              মাইক্রোসফটের অফিশিয়াল <strong>PWABuilder</strong> দিয়ে এই অ্যাপের লিংক দিয়ে মুহূর্তেই সাইন করা আসল <strong>.apk</strong> ফাইল ডাউনলোড করতে পারবেন:
            </p>
            <div className="flex flex-wrap items-center gap-2 pt-1">
              <button
                type="button"
                onClick={handleCopyLink}
                className="flex items-center gap-1.5 px-3 py-1.5 bg-white border border-sky-300 rounded-xl text-sky-900 font-bold text-xs hover:bg-sky-50 shadow-2xs transition"
              >
                {copiedLink ? <CheckCircle2 className="w-3.5 h-3.5 text-emerald-600" /> : <Share2 className="w-3.5 h-3.5" />}
                <span>{copiedLink ? 'লিংক কপি হয়েছে!' : 'অ্যাপ লিংক কপি করুন'}</span>
              </button>
              <a
                href="https://www.pwabuilder.com/"
                target="_blank"
                rel="noopener noreferrer"
                className="flex items-center gap-1 px-3 py-1.5 bg-sky-600 text-white rounded-xl font-bold text-xs hover:bg-sky-700 shadow-2xs transition"
              >
                <span>PWABuilder খুলুন</span>
                <ExternalLink className="w-3.5 h-3.5" />
              </a>
            </div>
          </div>
        </div>

        {/* Modal Footer */}
        <div className="p-3 bg-slate-50 border-t border-slate-200 flex items-center justify-end">
          <button
            type="button"
            onClick={onClose}
            className="px-5 py-2 rounded-xl bg-slate-800 hover:bg-slate-900 text-white font-bold text-xs transition"
          >
            বন্ধ করুন
          </button>
        </div>
      </div>
    </div>
  );
};
