import React, { useRef } from 'react';
import { Menu, Store } from 'lucide-react';
import { useShop } from '../context/ShopContext';
import { formatTaka } from '../utils/formatters';

interface HeaderBannerProps {
  onOpenDrawer: () => void;
  onOpenAdminPanel: () => void;
}

export const HeaderBanner: React.FC<HeaderBannerProps> = ({ onOpenDrawer, onOpenAdminPanel }) => {
  const { sales, expenses, mobileBanking, todayDateStr } = useShop();

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

  // Secret 5-Click Admin Trigger on the Visiting Card
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

        {/* Shop Logo Badge */}
        <div className="flex items-center gap-2">
          <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-emerald-600 to-teal-500 flex items-center justify-center shadow-lg shadow-emerald-900/40 border border-emerald-400/30">
            <Store className="w-5 h-5 text-white" />
          </div>
        </div>
      </div>

      {/* Official Visiting Card / Business Banner */}
      {/* Secret trigger: Exactly 5 clicks on this visiting card opens the Admin section */}
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

      {/* Today's Cash Bar (replaces "আজকের তারিখ" directly with "আজকের ক্যাশ") */}
      <div className="mt-3 bg-white/10 rounded-xl px-3.5 py-2.5 flex items-center justify-between text-xs font-semibold text-slate-200 border border-white/10">
        <span className="flex items-center gap-2">
          <span className="text-emerald-400 font-extrabold text-xs sm:text-sm">💵 আজকের ক্যাশ:</span>
          <strong className="text-white font-black text-sm sm:text-base tracking-tight">
            {formatTaka(todayEstimatedCash)}
          </strong>
        </span>
        <span className="text-emerald-300 font-black bg-emerald-950/70 px-2.5 py-1 rounded-md text-[11px] border border-emerald-500/40">
          মুদ্রা: ৳ (BDT)
        </span>
      </div>
    </div>
  );
};
