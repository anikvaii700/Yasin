import React, { useState } from 'react';
import {
  TrendingUp,
  Package,
  Boxes,
  AlertTriangle,
  Wallet,
  Receipt,
  Banknote,
  DollarSign,
  PlusCircle,
  ShoppingCart,
  ShoppingBag,
  FileText,
  Smartphone,
  HandCoins,
  CreditCard,
  Store,
  ChevronRight,
  BarChart2,
  PieChart,
  ArrowUpRight,
  ArrowDownLeft,
  Percent,
  CheckCircle2,
  Clock,
} from 'lucide-react';
import { useShop } from '../context/ShopContext';
import { formatTaka } from '../utils/formatters';

interface HomeScreenProps {
  onNavigateToProducts: () => void;
  onNavigateToSales: () => void;
  onNavigateToDue: () => void;
  onNavigateToCashMemo: () => void;
  onNavigateToPurchases: () => void;
  onNavigateToExpenses: () => void;
  onNavigateToMobileBanking: () => void;
  onOpenAddProductDialog: () => void;
  onOpenRecordSaleDialog: () => void;
  onOpenAddExpenseDialog: () => void;
  onOpenAddDueDialog: () => void;
  onOpenAddPurchaseDialog: () => void;
}

export const HomeScreen: React.FC<HomeScreenProps> = ({
  onNavigateToProducts,
  onNavigateToSales,
  onNavigateToDue,
  onNavigateToCashMemo,
  onNavigateToPurchases,
  onNavigateToExpenses,
  onNavigateToMobileBanking,
  onOpenAddProductDialog,
  onOpenRecordSaleDialog,
  onOpenAddExpenseDialog,
  onOpenAddDueDialog,
  onOpenAddPurchaseDialog,
}) => {
  const {
    todayDateStr,
    profile,
    products,
    sales,
    expenses,
    customers,
    loans,
    mobileBanking,
    flexiload,
  } = useShop();

  // Financial calculations for Today
  const todaySalesList = sales.filter((s) => s.saleDate === todayDateStr);
  const todayExpensesList = expenses.filter((e) => e.expenseDate === todayDateStr);
  const todayMbList = mobileBanking.filter((m) => m.transactionDate === todayDateStr);
  const todayFlexiList = flexiload.filter((f) => f.transactionDate === todayDateStr);

  const todayTotalSales = todaySalesList.reduce((acc, s) => acc + s.totalAmount, 0);
  const todaySaleProfit = todaySalesList.reduce((acc, s) => acc + s.grossProfit, 0);
  const todayMbProfit = todayMbList.reduce((acc, m) => acc + m.commissionProfit, 0);
  const todayFlexiProfit = todayFlexiList.reduce((acc, f) => acc + f.profit, 0);
  const todayGrossProfit = todaySaleProfit + todayMbProfit + todayFlexiProfit;

  const todayTotalExpenses = todayExpensesList.reduce((acc, e) => acc + e.amount, 0);
  const todayNetProfit = todayGrossProfit - todayTotalExpenses;

  // Cash flow estimate today
  const todayCashSales = todaySalesList.reduce((acc, s) => acc + s.paidAmount, 0);
  const todayCashInMb = todayMbList
    .filter((m) => m.type === 'CASH_IN')
    .reduce((acc, m) => acc + m.amount, 0);
  const todayCashOutMb = todayMbList
    .filter((m) => m.type === 'CASH_OUT')
    .reduce((acc, m) => acc + m.amount, 0);
  const todayEstimatedCash = Math.max(0, todayCashSales + todayCashInMb - todayCashOutMb - todayTotalExpenses);

  // Stock analytics
  const totalProductsCount = products.length;
  const availableStockQuantity = products.reduce((acc, p) => acc + Math.max(0, p.currentStock), 0);
  const totalStockValue = products.reduce((acc, p) => acc + p.currentStock * p.purchasePrice, 0);
  const totalPotentialValue = products.reduce((acc, p) => acc + p.currentStock * p.sellingPrice, 0);
  const potentialProfit = totalPotentialValue - totalStockValue;

  const lowStockThreshold = profile?.lowStockAlertThreshold || 5;
  const goodStockList = products.filter(
    (p) => p.currentStock > (p.minStockAlert > 0 ? p.minStockAlert : lowStockThreshold)
  );
  const lowStockList = products.filter(
    (p) => p.currentStock > 0 && p.currentStock <= (p.minStockAlert > 0 ? p.minStockAlert : lowStockThreshold)
  );
  const outOfStockList = products.filter((p) => p.currentStock <= 0);

  // Customer Due & Personal Loans
  const totalCustomerDue = customers.reduce((acc, c) => acc + c.remainingDue, 0);
  const totalLentReceivable = loans
    .filter((l) => l.loanType === 'LENT')
    .reduce((acc, l) => acc + l.remainingAmount, 0);
  const totalBorrowedPayable = loans
    .filter((l) => l.loanType !== 'LENT')
    .reduce((acc, l) => acc + l.remainingAmount, 0);

  // Generate 7-day sales and profit data for SVG Chart
  const last7Days = Array.from({ length: 7 }).map((_, i) => {
    const d = new Date();
    d.setDate(d.getDate() - (6 - i));
    const day = String(d.getDate()).padStart(2, '0');
    const month = String(d.getMonth() + 1).padStart(2, '0');
    const year = d.getFullYear();
    const dateStr = `${day}-${month}-${year}`;
    const dayLabel = d.toLocaleDateString('bn-BD', { weekday: 'short' });

    const daySales = sales.filter((s) => s.saleDate === dateStr);
    const dayTotal = daySales.reduce((acc, s) => acc + s.totalAmount, 0);
    const dayProfit = daySales.reduce((acc, s) => acc + s.grossProfit, 0);

    return {
      dateStr,
      dayLabel,
      totalSales: dayTotal,
      profit: dayProfit,
      isToday: dateStr === todayDateStr,
    };
  });

  const maxChartValue = Math.max(1000, ...last7Days.map((d) => Math.max(d.totalSales, d.profit)));

  return (
    <div className="space-y-6 pb-24">
      {/* 8 PREMIUM COLORFUL GRADIENT DASHBOARD CARDS */}
      <section>
        <div className="flex items-center justify-between mb-3.5">
          <div className="flex items-center gap-2">
            <span className="w-2.5 h-2.5 rounded-full bg-emerald-500 animate-pulse" />
            <h2 className="text-sm font-extrabold text-slate-800 tracking-wide uppercase">
              আজকের ড্যাশবোর্ড সারসংক্ষেপ (Overview)
            </h2>
          </div>
          <span className="text-[11px] font-semibold text-slate-600 bg-white px-2.5 py-1 rounded-full border border-slate-200 shadow-2xs">
            📅 {todayDateStr}
          </span>
        </div>

        <div className="grid grid-cols-2 lg:grid-cols-4 gap-3 sm:gap-3.5">
          {/* Card 1: Today's Sales */}
          <div
            onClick={onNavigateToSales}
            data-testid="card_today_sales"
            className="group cursor-pointer rounded-2xl p-4 text-white shadow-md transition-all duration-300 transform hover:-translate-y-0.5 hover:shadow-lg active:scale-98 bg-gradient-to-br from-blue-600 via-indigo-600 to-indigo-800 relative overflow-hidden border border-white/10"
          >
            <div className="absolute -right-3 -bottom-3 w-20 h-20 bg-white/10 rounded-full blur-xl group-hover:scale-125 transition-transform" />
            <div className="flex items-center justify-between relative z-10">
              <span className="text-[11px] font-bold text-blue-100 uppercase tracking-wider">Today's Sales</span>
              <div className="p-1.5 rounded-xl bg-white/20 backdrop-blur-xs">
                <Banknote className="w-4 h-4 text-white" />
              </div>
            </div>
            <p className="text-lg sm:text-xl font-black mt-2 tracking-tight text-white relative z-10">
              {formatTaka(todayTotalSales)}
            </p>
            <div className="flex items-center justify-between mt-1 text-[11px] text-blue-100/90 relative z-10">
              <span>আজকের বিক্রয়</span>
              <span className="bg-white/20 px-1.5 py-0.2 rounded font-bold">{todaySalesList.length} টি</span>
            </div>
          </div>

          {/* Card 2: Today's Profit */}
          <div
            onClick={onNavigateToSales}
            data-testid="card_today_profit"
            className="group cursor-pointer rounded-2xl p-4 text-white shadow-md transition-all duration-300 transform hover:-translate-y-0.5 hover:shadow-lg active:scale-98 bg-gradient-to-br from-emerald-500 via-teal-600 to-green-700 relative overflow-hidden border border-white/10"
          >
            <div className="absolute -right-3 -bottom-3 w-20 h-20 bg-white/10 rounded-full blur-xl group-hover:scale-125 transition-transform" />
            <div className="flex items-center justify-between relative z-10">
              <span className="text-[11px] font-bold text-emerald-100 uppercase tracking-wider">Today's Profit</span>
              <div className="p-1.5 rounded-xl bg-white/20 backdrop-blur-xs">
                <TrendingUp className="w-4 h-4 text-white" />
              </div>
            </div>
            <p className="text-lg sm:text-xl font-black mt-2 tracking-tight text-white relative z-10">
              {formatTaka(todayGrossProfit)}
            </p>
            <div className="flex items-center justify-between mt-1 text-[11px] text-emerald-100/90 relative z-10">
              <span>আজকের মোট লাভ</span>
              <span className="bg-white/20 px-1.5 py-0.2 rounded font-bold">
                {todayTotalExpenses > 0 ? `নিট ${formatTaka(todayNetProfit)}` : 'গ্রস'}
              </span>
            </div>
          </div>

          {/* Card 3: Total Products */}
          <div
            onClick={onNavigateToProducts}
            data-testid="card_total_products"
            className="group cursor-pointer rounded-2xl p-4 text-white shadow-md transition-all duration-300 transform hover:-translate-y-0.5 hover:shadow-lg active:scale-98 bg-gradient-to-br from-purple-600 via-violet-600 to-purple-800 relative overflow-hidden border border-white/10"
          >
            <div className="absolute -right-3 -bottom-3 w-20 h-20 bg-white/10 rounded-full blur-xl group-hover:scale-125 transition-transform" />
            <div className="flex items-center justify-between relative z-10">
              <span className="text-[11px] font-bold text-purple-100 uppercase tracking-wider">Total Products</span>
              <div className="p-1.5 rounded-xl bg-white/20 backdrop-blur-xs">
                <Package className="w-4 h-4 text-white" />
              </div>
            </div>
            <p className="text-lg sm:text-xl font-black mt-2 tracking-tight text-white relative z-10">
              {totalProductsCount} টি
            </p>
            <div className="flex items-center justify-between mt-1 text-[11px] text-purple-100/90 relative z-10">
              <span>মোট পণ্য</span>
              <span className="truncate max-w-[100px]">ক্রয়: {formatTaka(totalStockValue)}</span>
            </div>
          </div>

          {/* Card 4: Available Stock */}
          <div
            onClick={onNavigateToProducts}
            data-testid="card_available_stock"
            className="group cursor-pointer rounded-2xl p-4 text-white shadow-md transition-all duration-300 transform hover:-translate-y-0.5 hover:shadow-lg active:scale-98 bg-gradient-to-br from-cyan-600 via-teal-600 to-sky-700 relative overflow-hidden border border-white/10"
          >
            <div className="absolute -right-3 -bottom-3 w-20 h-20 bg-white/10 rounded-full blur-xl group-hover:scale-125 transition-transform" />
            <div className="flex items-center justify-between relative z-10">
              <span className="text-[11px] font-bold text-cyan-100 uppercase tracking-wider">Available Stock</span>
              <div className="p-1.5 rounded-xl bg-white/20 backdrop-blur-xs">
                <Boxes className="w-4 h-4 text-white" />
              </div>
            </div>
            <p className="text-lg sm:text-xl font-black mt-2 tracking-tight text-white relative z-10">
              {availableStockQuantity} পিস
            </p>
            <div className="flex items-center justify-between mt-1 text-[11px] text-cyan-100/90 relative z-10">
              <span>চলতি মজুদ স্টক</span>
              <span>সম্ভাব্য: {formatTaka(totalPotentialValue)}</span>
            </div>
          </div>

          {/* Card 5: Low Stock */}
          <div
            onClick={onNavigateToProducts}
            data-testid="card_low_stock"
            className="group cursor-pointer rounded-2xl p-4 text-white shadow-md transition-all duration-300 transform hover:-translate-y-0.5 hover:shadow-lg active:scale-98 bg-gradient-to-br from-amber-500 via-orange-500 to-rose-600 relative overflow-hidden border border-white/10"
          >
            <div className="absolute -right-3 -bottom-3 w-20 h-20 bg-white/10 rounded-full blur-xl group-hover:scale-125 transition-transform" />
            <div className="flex items-center justify-between relative z-10">
              <span className="text-[11px] font-bold text-amber-100 uppercase tracking-wider">Low Stock</span>
              <div className="p-1.5 rounded-xl bg-white/20 backdrop-blur-xs">
                <AlertTriangle className="w-4 h-4 text-white" />
              </div>
            </div>
            <p className="text-lg sm:text-xl font-black mt-2 tracking-tight text-white relative z-10">
              {lowStockList.length} টি
            </p>
            <div className="flex items-center justify-between mt-1 text-[11px] text-amber-100/90 relative z-10">
              <span>কম স্টক সতর্কতা</span>
              <span className="bg-white/20 px-1.5 py-0.2 rounded font-bold">
                {outOfStockList.length > 0 ? `শেষ: ${outOfStockList.length}` : 'রি-অর্ডার'}
              </span>
            </div>
          </div>

          {/* Card 6: Customer Due */}
          <div
            onClick={onNavigateToDue}
            data-testid="card_customer_due"
            className="group cursor-pointer rounded-2xl p-4 text-white shadow-md transition-all duration-300 transform hover:-translate-y-0.5 hover:shadow-lg active:scale-98 bg-gradient-to-br from-rose-500 via-red-600 to-pink-700 relative overflow-hidden border border-white/10"
          >
            <div className="absolute -right-3 -bottom-3 w-20 h-20 bg-white/10 rounded-full blur-xl group-hover:scale-125 transition-transform" />
            <div className="flex items-center justify-between relative z-10">
              <span className="text-[11px] font-bold text-rose-100 uppercase tracking-wider">Customer Due</span>
              <div className="p-1.5 rounded-xl bg-white/20 backdrop-blur-xs">
                <Wallet className="w-4 h-4 text-white" />
              </div>
            </div>
            <p className="text-lg sm:text-xl font-black mt-2 tracking-tight text-white relative z-10">
              {formatTaka(totalCustomerDue)}
            </p>
            <div className="flex items-center justify-between mt-1 text-[11px] text-rose-100/90 relative z-10">
              <span>গ্রাহক বাকি (বাকি খাতা)</span>
              <span className="bg-white/20 px-1.5 py-0.2 rounded font-bold">
                {customers.filter((c) => c.remainingDue > 0).length} জন
              </span>
            </div>
          </div>

          {/* Card 7: Total Expenses */}
          <div
            onClick={onNavigateToExpenses}
            data-testid="card_total_expenses"
            className="group cursor-pointer rounded-2xl p-4 text-white shadow-md transition-all duration-300 transform hover:-translate-y-0.5 hover:shadow-lg active:scale-98 bg-gradient-to-br from-fuchsia-600 via-pink-600 to-purple-800 relative overflow-hidden border border-white/10"
          >
            <div className="absolute -right-3 -bottom-3 w-20 h-20 bg-white/10 rounded-full blur-xl group-hover:scale-125 transition-transform" />
            <div className="flex items-center justify-between relative z-10">
              <span className="text-[11px] font-bold text-fuchsia-100 uppercase tracking-wider">Total Expenses</span>
              <div className="p-1.5 rounded-xl bg-white/20 backdrop-blur-xs">
                <Receipt className="w-4 h-4 text-white" />
              </div>
            </div>
            <p className="text-lg sm:text-xl font-black mt-2 tracking-tight text-white relative z-10">
              {formatTaka(todayTotalExpenses)}
            </p>
            <div className="flex items-center justify-between mt-1 text-[11px] text-fuchsia-100/90 relative z-10">
              <span>মোট খরচ (আজকে)</span>
              <span className="bg-white/20 px-1.5 py-0.2 rounded font-bold">{todayExpensesList.length} টি</span>
            </div>
          </div>

          {/* Card 8: Cash Balance */}
          <div
            onClick={onNavigateToMobileBanking}
            data-testid="card_cash_balance"
            className="group cursor-pointer rounded-2xl p-4 text-white shadow-md transition-all duration-300 transform hover:-translate-y-0.5 hover:shadow-lg active:scale-98 bg-gradient-to-br from-teal-600 via-emerald-600 to-cyan-800 relative overflow-hidden border border-white/10"
          >
            <div className="absolute -right-3 -bottom-3 w-20 h-20 bg-white/10 rounded-full blur-xl group-hover:scale-125 transition-transform" />
            <div className="flex items-center justify-between relative z-10">
              <span className="text-[11px] font-bold text-teal-100 uppercase tracking-wider">Cash Balance</span>
              <div className="p-1.5 rounded-xl bg-white/20 backdrop-blur-xs">
                <DollarSign className="w-4 h-4 text-white" />
              </div>
            </div>
            <p className="text-lg sm:text-xl font-black mt-2 tracking-tight text-white relative z-10">
              {formatTaka(todayEstimatedCash)}
            </p>
            <div className="flex items-center justify-between mt-1 text-[11px] text-teal-100/90 relative z-10">
              <span>ক্যাশ ব্যালেন্স (হাতে নগদ)</span>
              <span className="bg-white/20 px-1.5 py-0.2 rounded font-bold">আজকের ক্যাশ</span>
            </div>
          </div>
        </div>
      </section>

      {/* CHARTS & STATISTICS SECTION */}
      <section className="bg-white rounded-2xl p-4 sm:p-5 border border-slate-200/90 shadow-2xs">
        <div className="flex items-center justify-between pb-3 border-b border-slate-100">
          <div className="flex items-center gap-2">
            <div className="p-1.5 rounded-lg bg-indigo-50 text-indigo-600">
              <BarChart2 className="w-4 h-4" />
            </div>
            <div>
              <h3 className="text-sm font-bold text-slate-800">গত ৭ দিনের বিক্রয় ও মুনাফা বিশ্লেষণ</h3>
              <p className="text-[11px] text-slate-400">প্রতিদিনের বিক্রয় ও লাভের গতিপ্রকৃতি</p>
            </div>
          </div>
          <div className="flex items-center gap-3 text-xs">
            <span className="flex items-center gap-1.5 text-slate-600">
              <span className="w-2.5 h-2.5 rounded-sm bg-indigo-600" />
              বিক্রয়
            </span>
            <span className="flex items-center gap-1.5 text-slate-600">
              <span className="w-2.5 h-2.5 rounded-sm bg-emerald-500" />
              লাভ
            </span>
          </div>
        </div>

        {/* 7-Day Visual SVG Bar Chart */}
        <div className="mt-4 pt-2">
          <div className="grid grid-cols-7 gap-2 items-end h-36 border-b border-slate-200 pb-2">
            {last7Days.map((d, index) => {
              const salesHeightPercent = Math.min(100, Math.max(12, Math.round((d.totalSales / maxChartValue) * 100)));
              const profitHeightPercent = Math.min(100, Math.max(8, Math.round((d.profit / maxChartValue) * 100)));

              return (
                <div key={index} className="flex flex-col items-center justify-end h-full group relative">
                  {/* Tooltip on Hover */}
                  <div className="absolute -top-12 z-20 hidden group-hover:flex flex-col items-center bg-slate-900 text-white text-[10px] rounded-lg px-2 py-1 shadow-lg pointer-events-none whitespace-nowrap">
                    <span>{d.dateStr}</span>
                    <span className="text-indigo-300 font-bold">বিক্রয়: {formatTaka(d.totalSales)}</span>
                    <span className="text-emerald-300 font-bold">লাভ: {formatTaka(d.profit)}</span>
                  </div>

                  <div className="w-full flex items-end justify-center gap-1 h-28">
                    {/* Sales Bar */}
                    <div
                      style={{ height: `${salesHeightPercent}%` }}
                      className={`w-3.5 sm:w-5 rounded-t-md transition-all duration-500 ${
                        d.isToday
                          ? 'bg-gradient-to-t from-indigo-700 to-indigo-500 shadow-sm'
                          : 'bg-indigo-300 group-hover:bg-indigo-500'
                      }`}
                    />
                    {/* Profit Bar */}
                    <div
                      style={{ height: `${profitHeightPercent}%` }}
                      className={`w-3.5 sm:w-5 rounded-t-md transition-all duration-500 ${
                        d.isToday
                          ? 'bg-gradient-to-t from-emerald-600 to-emerald-400 shadow-sm'
                          : 'bg-emerald-300 group-hover:bg-emerald-500'
                      }`}
                    />
                  </div>
                  <span
                    className={`text-[10px] sm:text-xs mt-1.5 font-bold ${
                      d.isToday ? 'text-indigo-700 bg-indigo-50 px-1.5 rounded-full' : 'text-slate-500'
                    }`}
                  >
                    {d.dayLabel}
                  </span>
                </div>
              );
            })}
          </div>
          <div className="flex items-center justify-between mt-2 text-[11px] text-slate-400">
            <span>সর্বোচ্চ সূচক: {formatTaka(maxChartValue)}</span>
            <span>আজকের বিক্রয়: {formatTaka(todayTotalSales)}</span>
          </div>
        </div>
      </section>

      {/* GREEN, YELLOW, RED STOCK STATUS INDICATOR BAR */}
      <section className="bg-white rounded-2xl p-4 border border-slate-200/90 shadow-2xs">
        <div className="flex items-center justify-between pb-3 border-b border-slate-100">
          <div>
            <h3 className="text-sm font-bold text-slate-800 flex items-center gap-1.5">
              <span>ইনভেন্টরি স্বাস্থ্য ও স্টক স্থিতি</span>
            </h3>
            <p className="text-[11px] text-slate-400">সবুজ (পর্যাপ্ত) • হলুদ (স্বল্প স্টক) • লাল (স্টক শেষ)</p>
          </div>
          <button
            type="button"
            onClick={onNavigateToProducts}
            className="text-xs font-bold text-emerald-600 hover:text-emerald-700 flex items-center gap-0.5"
          >
            স্টক ম্যানেজ <ChevronRight className="w-3.5 h-3.5" />
          </button>
        </div>

        {/* Triple Color Segmented Progress Bar */}
        {totalProductsCount > 0 && (
          <div className="mt-3">
            <div className="w-full h-3 rounded-full overflow-hidden flex bg-slate-100 p-0.5 gap-0.5">
              <div
                style={{ width: `${(goodStockList.length / totalProductsCount) * 100}%` }}
                className="bg-emerald-500 h-full rounded-l-full transition-all duration-500"
                title={`পর্যাপ্ত স্টক: ${goodStockList.length} টি`}
              />
              <div
                style={{ width: `${(lowStockList.length / totalProductsCount) * 100}%` }}
                className="bg-amber-400 h-full transition-all duration-500"
                title={`কম স্টক: ${lowStockList.length} টি`}
              />
              <div
                style={{ width: `${(outOfStockList.length / totalProductsCount) * 100}%` }}
                className="bg-red-500 h-full rounded-r-full transition-all duration-500"
                title={`স্টক শেষ: ${outOfStockList.length} টি`}
              />
            </div>
          </div>
        )}

        <div className="grid grid-cols-3 gap-2.5 mt-3 text-center">
          <div className="bg-emerald-50/80 border border-emerald-200/80 rounded-xl p-2.5">
            <div className="flex items-center justify-center gap-1 text-emerald-700 mb-0.5">
              <span className="w-2 h-2 rounded-full bg-emerald-600" />
              <p className="text-lg font-black">{goodStockList.length}</p>
            </div>
            <p className="text-[11px] font-bold text-emerald-800">পর্যাপ্ত স্টক</p>
            <p className="text-[10px] text-emerald-600 font-medium">নিরাপদ লেভেল</p>
          </div>

          <div className="bg-amber-50/80 border border-amber-200/80 rounded-xl p-2.5">
            <div className="flex items-center justify-center gap-1 text-amber-700 mb-0.5">
              <span className="w-2 h-2 rounded-full bg-amber-500" />
              <p className="text-lg font-black">{lowStockList.length}</p>
            </div>
            <p className="text-[11px] font-bold text-amber-800">কম স্টক সতর্কতা</p>
            <p className="text-[10px] text-amber-600 font-medium">রি-অর্ডার প্রয়োজন</p>
          </div>

          <div className="bg-red-50/80 border border-red-200/80 rounded-xl p-2.5">
            <div className="flex items-center justify-center gap-1 text-red-700 mb-0.5">
              <span className="w-2 h-2 rounded-full bg-red-600" />
              <p className="text-lg font-black">{outOfStockList.length}</p>
            </div>
            <p className="text-[11px] font-bold text-red-800">স্টক শেষ (০)</p>
            <p className="text-[10px] text-red-600 font-medium">জরুরি রিফিল</p>
          </div>
        </div>

        {(outOfStockList.length > 0 || lowStockList.length > 0) && (
          <div className="mt-4 pt-3 border-t border-slate-100 space-y-2">
            {[...outOfStockList, ...lowStockList].slice(0, 4).map((p) => (
              <div key={p.id} className="flex items-center justify-between text-xs py-1">
                <span className="font-semibold text-slate-700 truncate max-w-[65%]">{p.name}</span>
                <span
                  className={`px-2 py-0.5 rounded-full font-black text-[10px] ${
                    p.currentStock <= 0
                      ? 'bg-red-100 text-red-700 border border-red-200'
                      : 'bg-amber-100 text-amber-800 border border-amber-200'
                  }`}
                >
                  {p.currentStock <= 0 ? 'স্টক শেষ (০)' : `মজুদ: ${p.currentStock} টি`}
                </span>
              </div>
            ))}
          </div>
        )}
      </section>

      {/* SOFT ACCENT COLORFUL INFORMATION BOXES (PURPLE, PINK, BLUE, TEAL, GREEN, ORANGE, YELLOW) */}
      <section>
        <div className="flex items-center justify-between mb-3">
          <h2 className="text-sm font-extrabold text-slate-800 tracking-wide uppercase">
            বিশেষ বাণিজ্যিক সূচক (Key Shop Accents)
          </h2>
          <span className="text-[11px] text-slate-500 font-medium">লাইভ আপডেট</span>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
          {/* 1. Soft Purple: Total Shop Stock Valuation */}
          <div className="flex items-center justify-between p-3.5 rounded-2xl bg-purple-50 border border-purple-200/80 text-purple-950 shadow-2xs">
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 rounded-xl bg-purple-200/70 flex items-center justify-center text-purple-700 font-bold">
                <Store className="w-5 h-5" />
              </div>
              <div>
                <p className="text-xs font-bold text-purple-900">দোকানের মোট সম্পদ মূল্য</p>
                <p className="text-[11px] text-purple-700">সম্ভাব্য বিক্রয় মূল্য: {formatTaka(totalPotentialValue)}</p>
              </div>
            </div>
            <div className="text-right">
              <span className="text-sm sm:text-base font-black text-purple-800">{formatTaka(totalStockValue)}</span>
              <span className="block text-[10px] font-bold text-purple-600">ক্রয়মূল্য</span>
            </div>
          </div>

          {/* 2. Soft Blue: bKash & Nagad Turnover */}
          <div className="flex items-center justify-between p-3.5 rounded-2xl bg-blue-50 border border-blue-200/80 text-blue-950 shadow-2xs">
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 rounded-xl bg-blue-200/70 flex items-center justify-center text-blue-700 font-bold">
                <Smartphone className="w-5 h-5" />
              </div>
              <div>
                <p className="text-xs font-bold text-blue-900">বিকাশ ও নগদ লেনদেন</p>
                <p className="text-[11px] text-blue-700">{todayMbList.length} টি আজকের ট্রানজ্যাকশন</p>
              </div>
            </div>
            <div className="text-right">
              <span className="text-sm sm:text-base font-black text-blue-800">{formatTaka(todayMbProfit)}</span>
              <span className="block text-[10px] font-bold text-blue-600">কমিশন লাভ</span>
            </div>
          </div>

          {/* 3. Soft Teal: Flexiload Daily Revenue */}
          <div className="flex items-center justify-between p-3.5 rounded-2xl bg-teal-50 border border-teal-200/80 text-teal-950 shadow-2xs">
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 rounded-xl bg-teal-200/70 flex items-center justify-center text-teal-700 font-bold">
                <Percent className="w-5 h-5" />
              </div>
              <div>
                <p className="text-xs font-bold text-teal-900">ফ্লেক্সিলোড ও রিচার্জ</p>
                <p className="text-[11px] text-teal-700">{todayFlexiList.length} টি আজকের রিচার্জ</p>
              </div>
            </div>
            <div className="text-right">
              <span className="text-sm sm:text-base font-black text-teal-800">{formatTaka(todayFlexiProfit)}</span>
              <span className="block text-[10px] font-bold text-teal-600">রিচার্জ লাভ</span>
            </div>
          </div>

          {/* 4. Soft Pink: Expense Tracking */}
          <div className="flex items-center justify-between p-3.5 rounded-2xl bg-pink-50 border border-pink-200/80 text-pink-950 shadow-2xs">
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 rounded-xl bg-pink-200/70 flex items-center justify-center text-pink-700 font-bold">
                <Receipt className="w-5 h-5" />
              </div>
              <div>
                <p className="text-xs font-bold text-pink-900">দোকানের নিয়মিত খরচ</p>
                <p className="text-[11px] text-pink-700">{todayExpensesList.length} টি খরচের হিসাব</p>
              </div>
            </div>
            <div className="text-right">
              <span className="text-sm sm:text-base font-black text-pink-800">{formatTaka(todayTotalExpenses)}</span>
              <span className="block text-[10px] font-bold text-pink-600">আজকের খরচ</span>
            </div>
          </div>

          {/* 5. Soft Orange: Customer Due & Recovery */}
          <div className="flex items-center justify-between p-3.5 rounded-2xl bg-orange-50 border border-orange-200/80 text-orange-950 shadow-2xs">
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 rounded-xl bg-orange-200/70 flex items-center justify-center text-orange-700 font-bold">
                <HandCoins className="w-5 h-5" />
              </div>
              <div>
                <p className="text-xs font-bold text-orange-900">গ্রাহক বকেয়া ও পাওনা</p>
                <p className="text-[11px] text-orange-700">বকেয়া গ্রাহক {customers.filter((c) => c.remainingDue > 0).length} জন</p>
              </div>
            </div>
            <div className="text-right">
              <span className="text-sm sm:text-base font-black text-orange-800">{formatTaka(totalCustomerDue)}</span>
              <span className="block text-[10px] font-bold text-orange-600">বকেয়া পাওনা</span>
            </div>
          </div>

          {/* 6. Soft Green: Net Cash Inflow */}
          <div className="flex items-center justify-between p-3.5 rounded-2xl bg-emerald-50 border border-emerald-200/80 text-emerald-950 shadow-2xs">
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 rounded-xl bg-emerald-200/70 flex items-center justify-center text-emerald-700 font-bold">
                <ArrowDownLeft className="w-5 h-5" />
              </div>
              <div>
                <p className="text-xs font-bold text-emerald-900">আজকের ক্যাশ বিক্রয় জমা</p>
                <p className="text-[11px] text-emerald-700">নগদ ক্যাশ গ্রহণ</p>
              </div>
            </div>
            <div className="text-right">
              <span className="text-sm sm:text-base font-black text-emerald-800">{formatTaka(todayCashSales)}</span>
              <span className="block text-[10px] font-bold text-emerald-600">নগদ রিসিভ</span>
            </div>
          </div>

          {/* 7. Soft Yellow: Low Stock Alert Warning Box */}
          <div className="flex items-center justify-between p-3.5 rounded-2xl bg-amber-50 border border-amber-200/80 text-amber-950 shadow-2xs sm:col-span-2">
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 rounded-xl bg-amber-200/70 flex items-center justify-center text-amber-800 font-bold">
                <AlertTriangle className="w-5 h-5" />
              </div>
              <div>
                <p className="text-xs font-bold text-amber-900">স্টক সতর্কতা ও পুনঃক্রয় রিকমেন্ডেশন</p>
                <p className="text-[11px] text-amber-700">
                  {lowStockList.length} টি পণ্যের স্টক শীঘ্রই শেষ হতে চলেছে, অনতিবিলম্বে নতুন চালান সংগ্রহ করুন।
                </p>
              </div>
            </div>
            <button
              type="button"
              onClick={onNavigateToProducts}
              className="px-3 py-1.5 rounded-lg bg-amber-600 hover:bg-amber-700 text-white text-xs font-bold transition shrink-0"
            >
              পণ্য দেখুন
            </button>
          </div>
        </div>
      </section>

      {/* QUICK ACTION BUTTONS */}
      <section>
        <h2 className="text-sm font-extrabold text-slate-800 tracking-wide uppercase mb-3">
          দ্রুত কার্যক্রম (Quick Actions)
        </h2>
        <div className="grid grid-cols-3 gap-2.5">
          <button
            type="button"
            data-testid="quick_cash_memo"
            onClick={onNavigateToCashMemo}
            className="flex flex-col items-center justify-center p-3 rounded-2xl bg-gradient-to-br from-orange-500 to-amber-600 hover:from-orange-600 hover:to-amber-700 active:scale-95 text-white shadow-md transition"
          >
            <FileText className="w-6 h-6 mb-1.5" />
            <span className="text-xs font-bold">ক্যাশ মেমো</span>
          </button>

          <button
            type="button"
            data-testid="quick_add_product"
            onClick={onOpenAddProductDialog}
            className="flex flex-col items-center justify-center p-3 rounded-2xl bg-gradient-to-br from-emerald-600 to-teal-700 hover:from-emerald-700 hover:to-teal-800 active:scale-95 text-white shadow-md transition"
          >
            <PlusCircle className="w-6 h-6 mb-1.5" />
            <span className="text-xs font-bold">পণ্য যোগ</span>
          </button>

          <button
            type="button"
            data-testid="quick_record_sale"
            onClick={onOpenRecordSaleDialog}
            className="flex flex-col items-center justify-center p-3 rounded-2xl bg-gradient-to-br from-blue-700 to-indigo-800 hover:from-blue-800 hover:to-indigo-900 active:scale-95 text-white shadow-md transition"
          >
            <ShoppingCart className="w-6 h-6 mb-1.5" />
            <span className="text-xs font-bold">বিক্রয় রেকর্ড</span>
          </button>

          <button
            type="button"
            data-testid="quick_add_purchase"
            onClick={onOpenAddPurchaseDialog}
            className="flex flex-col items-center justify-center p-3 rounded-2xl bg-gradient-to-br from-indigo-600 to-purple-700 hover:from-indigo-700 hover:to-purple-800 active:scale-95 text-white shadow-md transition"
          >
            <ShoppingBag className="w-6 h-6 mb-1.5" />
            <span className="text-xs font-bold">ক্রয় যোগ</span>
          </button>

          <button
            type="button"
            data-testid="quick_add_expense"
            onClick={onOpenAddExpenseDialog}
            className="flex flex-col items-center justify-center p-3 rounded-2xl bg-gradient-to-br from-rose-600 to-pink-700 hover:from-rose-700 hover:to-pink-800 active:scale-95 text-white shadow-md transition"
          >
            <Receipt className="w-6 h-6 mb-1.5" />
            <span className="text-xs font-bold">খরচ যোগ</span>
          </button>

          <button
            type="button"
            data-testid="quick_add_due"
            onClick={onOpenAddDueDialog}
            className="flex flex-col items-center justify-center p-3 rounded-2xl bg-gradient-to-br from-sky-600 to-cyan-700 hover:from-sky-700 hover:to-cyan-800 active:scale-95 text-white shadow-md transition"
          >
            <Wallet className="w-6 h-6 mb-1.5" />
            <span className="text-xs font-bold">বাকি এন্ট্রি</span>
          </button>
        </div>
      </section>
    </div>
  );
};
