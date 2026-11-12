import React, { useState, useEffect, useRef } from 'react';
import { Menu, Store } from 'lucide-react';
import { useShop } from '../context/ShopContext';
import { formatTaka } from '../utils/formatters';

interface HeaderBannerProps {
  onOpenDrawer: () => void;
  onOpenAdminPanel: () => void;
  onOpenAndroidInstall?: () => void;
  showVisitingCard?: boolean;
}

export const HeaderBanner: React.FC<HeaderBannerProps> = ({
  onOpenDrawer,
  onOpenAdminPanel,
  onOpenAndroidInstall,
  showVisitingCard = true,
}) => {
  const { sales, expenses, mobileBanking, todayDateStr } = useShop();

  // State for bKash/Nagad style slide-to-reveal cash balance
  const [isCashRevealed, setIsCashRevealed] = useState(false);
  const cashTimerRef = useRef<number | null>(null);

  // Financial calculations for Today's Cash
  const todaySalesList = sales.filter((s) => s.saleDate === todayDateStr);
  const todayExpensesList = expenses.filter((e) => e.expenseDate === todayDateStr);
  const todayMbList = mobileBanking.filter((m) => m.transactionDate === todayDateStr);

  const todayCashSales = todaySalesList.reduce((acc, s) => acc + s.paidAmount, 0);
  const todayCashInMb = todayMbList
    .filter((m) => m.type === 'CASH_IN')
    .reduce((acc, m) => acc + m.amount, 0);
  const todayCashOutMb = todayMbList
    .filter((m) => m.type === 'CASH_OUT')
    .reduce((acc, m) => acc + m.amount, 0);
  const todayTotalExpenses = todayExpensesList.reduce((acc, e) => acc + e.amount, 0);

  const todayEstimatedCash = Math.max(
    0,
    todayCashSales + todayCashInMb - todayCashOutMb - todayTotalExpenses
  );

  const handleToggleCashReveal = () => {
    if (cashTimerRef.current) {
      window.clearTimeout(cashTimerRef.current);
      cashTimerRef.current = null;
    }

    if (!isCashRevealed) {
      setIsCashRevealed(true);
      // Auto-hide balance after 4.5 seconds like bKash / Nagad
      cashTimerRef.current = window.setTimeout(() => {
        setIsCashRevealed(false);
      }, 4500);
    } else {
      setIsCashRevealed(false);
    }
  };

  useEffect(() => {
    return () => {
      if (cashTimerRef.current) {
        window.clearTimeout(cashTimerRef.current);
      }
    };
  }, []);

  // Secret 5-Click Admin Trigger on the Visiting Card / Shop Logo
  // Completely silent with no visual cues or hints
  const clickCountRef = useRef(0);
  const resetTimerRef = useRef<number | null>(null);

  const handleCardClick = () => {
    // Increment consecutive clicks
    clickCountRef.current += 1;

    // Clear any existing reset timeout
    if (resetTimerRef.current) {
      window.clearTimeout(resetTimerRef.current);
    }

    // Check if 5 consecutive clicks reached
    if (clickCountRef.current >= 5) {
      clickCountRef.current = 0;
      onOpenAdminPanel();
      return;
    }

    // Reset count if user stops clicking after 2.5 seconds
    resetTimerRef.current = window.setTimeout(() => {
      clickCountRef.current = 0;
    }, 2500);
  };

  return (
    <div className="relative overflow-hidden rounded-2xl bg-gradient-to-br from-[#05192D] via-[#0A2540] to-[#0D3859] text-white p-3.5 sm:p-4 shadow-xl border border-slate-700/50">
      {/* Decorative ambient gradient glow */}
      <div className="absolute -top-16 -right-16 w-44 h-44 bg-emerald-500/20 rounded-full blur-3xl pointer-events-none" />
      <div className="absolute -bottom-10 -left-10 w-36 h-36 bg-orange-500/20 rounded-full blur-2xl pointer-events-none" />

      {/* Top Bar: Hamburger Menu, Dashboard Title, Shop Icon */}
      <div className="flex items-center justify-between gap-3 relative z-10 pb-3 border-b border-white/10">
        <div className="flex items-center gap-2.5 sm:gap-3">
          <button
            type="button"
            onClick={onOpenDrawer}
            className="flex items-center justify-center w-10 h-10 rounded-xl bg-white/10 hover:bg-white/20 active:scale-95 transition backdrop-blur-sm text-white border border-white/15 shadow-sm"
            title="মেনু ড্রয়ার খুলুন"
            data-testid="btn_header_drawer"
          >
            <Menu className="w-5 h-5" />
          </button>
          <div>
            <span className="text-[10px] font-extrabold uppercase tracking-widest text-emerald-400 block">
              DASHBOARD • ড্যাশবোর্ড
            </span>
            <span className="text-xs font-bold text-slate-200">স্মার্ট শপ ম্যানেজার</span>
          </div>
        </div>

        {/* Right action: Shop Logo */}
        <div className="flex items-center gap-2">
          <div
            onClick={handleCardClick}
            className="w-10 h-10 rounded-xl bg-gradient-to-tr from-emerald-600 to-teal-500 flex items-center justify-center shadow-lg shadow-emerald-900/40 border border-emerald-400/30 cursor-pointer active:scale-95 transition"
            title="ইয়াছিন টেলিকম"
          >
            <Store className="w-5 h-5 text-white" />
          </div>
        </div>
      </div>

      {/* Official Visiting Card / Business Banner - shown ONLY when showVisitingCard is true */}
      {showVisitingCard && (
        <div
          onClick={handleCardClick}
          className="mt-3.5 relative z-10 cursor-pointer select-none rounded-2xl overflow-hidden shadow-lg border border-white/15 transition-all duration-200 active:scale-[0.99] group bg-slate-900"
          title="ইয়াছিন টেলিকম"
        >
          <img
            src="/assets/aistudio/Modern_Bengali_Telecom_Store_Banner_23b2899d.png"
            alt="ইয়াছিন টেলিকম - অফিসিয়াল ভিজিটিং কার্ড ও বিজনেস ব্যানার"
            className="w-full h-auto object-cover block select-none pointer-events-none rounded-2xl"
            draggable={false}
            loading="eager"
          />
        </div>
      )}

      {/* Today's Cash Bar - Centered bKash / Nagad style Slide-to-reveal Cash balance */}
      <div className="mt-3 bg-slate-900/40 backdrop-blur-xs rounded-2xl py-2 px-3 flex items-center justify-center border border-white/10 shadow-inner">
        <button
          type="button"
          onClick={handleToggleCashReveal}
          className="group relative flex items-center justify-center gap-2 px-5 py-1.5 rounded-full bg-slate-800/90 hover:bg-slate-800 border border-emerald-500/30 hover:border-emerald-500/60 transition-all duration-300 shadow-md active:scale-95 cursor-pointer max-w-full overflow-hidden select-none"
          title="ক্যাশ ব্যালেন্স দেখতে ট্যাপ করুন"
        >
          {/* Animated ৳ Icon Badge */}
          <span
            className={`w-6 h-6 rounded-full flex items-center justify-center text-xs font-black transition-all duration-300 shrink-0 ${
              isCashRevealed
                ? 'bg-emerald-500 text-slate-950 scale-105 shadow-sm shadow-emerald-500/50'
                : 'bg-emerald-500/20 text-emerald-400 group-hover:bg-emerald-500/30'
            }`}
          >
            ৳
          </span>

          {/* Smooth Sliding Content (bKash/Nagad style) */}
          <div className="relative overflow-hidden transition-all duration-300">
            {isCashRevealed ? (
              <div className="flex items-center gap-2 animate-in fade-in slide-in-from-right-4 duration-300">
                <span className="text-emerald-400 font-extrabold text-xs sm:text-sm">
                  আজকের ক্যাশ:
                </span>
                <strong className="text-white font-black text-sm sm:text-base tracking-tight drop-shadow-xs">
                  {formatTaka(todayEstimatedCash)}
                </strong>
              </div>
            ) : (
              <div className="flex items-center gap-1.5 animate-in fade-in slide-in-from-left-4 duration-300">
                <span className="text-slate-100 font-extrabold text-xs sm:text-sm tracking-wide">
                  আজকের ক্যাশ
                </span>
                <span className="text-[10px] text-emerald-400 font-semibold bg-emerald-500/10 px-2 py-0.5 rounded-full border border-emerald-500/20">
                  ট্যাপ করুন
                </span>
              </div>
            )}
          </div>
        </button>
      </div>
    </div>
  );
};
