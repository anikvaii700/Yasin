import React from 'react';
import {
  LayoutDashboard,
  Package,
  ShoppingCart,
  ShoppingBag,
  Wallet,
  Users,
  Smartphone,
  Zap,
  Receipt,
  FileText,
  CalendarCheck,
  BarChart3,
  Settings,
  ShieldAlert,
  RotateCcw,
  X,
  Store,
} from 'lucide-react';
import { useShop } from '../context/ShopContext';

export interface DrawerActionItem {
  id: number;
  title: string;
  subtitle?: string;
  icon: React.ReactNode;
  tag: string;
  isSpecial?: boolean;
}

interface NavigationDrawerProps {
  isOpen: boolean;
  onClose: () => void;
  onSelectItem: (id: number) => void;
}

export const NavigationDrawer: React.FC<NavigationDrawerProps> = ({
  isOpen,
  onClose,
  onSelectItem,
}) => {
  const { profile } = useShop();

  const items: DrawerActionItem[] = [
    { id: 1, title: 'ড্যাশবোর্ড (Dashboard)', icon: <LayoutDashboard className="w-5 h-5 text-slate-700" />, tag: 'drawer_dashboard' },
    { id: 2, title: 'পণ্য ও স্টক (Products & Stock)', icon: <Package className="w-5 h-5 text-slate-700" />, tag: 'drawer_products' },
    { id: 3, title: 'বিক্রয় হিসাব (Sales)', icon: <ShoppingCart className="w-5 h-5 text-slate-700" />, tag: 'drawer_sales' },
    { id: 4, title: 'পণ্য ক্রয় (Purchase)', icon: <ShoppingBag className="w-5 h-5 text-slate-700" />, tag: 'drawer_purchase' },
    { id: 5, title: 'বাকি খাতা (Customer Due)', icon: <Wallet className="w-5 h-5 text-slate-700" />, tag: 'drawer_due' },
    { id: 6, title: 'ব্যক্তিগত ধার-দেনা (Personal Loans)', icon: <Users className="w-5 h-5 text-slate-700" />, tag: 'drawer_loans' },
    { id: 7, title: 'বিকাশ ও নগদ (bKash & Nagad)', icon: <Smartphone className="w-5 h-5 text-slate-700" />, tag: 'drawer_mobile_banking' },
    { id: 8, title: 'ফ্লেক্সিলোড (Flexiload)', icon: <Zap className="w-5 h-5 text-slate-700" />, tag: 'drawer_flexiload' },
    { id: 9, title: 'দোকানের খরচ (Expenses)', icon: <Receipt className="w-5 h-5 text-slate-700" />, tag: 'drawer_expenses' },
    { id: 10, title: 'ক্যাশ মেমো (Cash Memo)', icon: <FileText className="w-5 h-5 text-slate-700" />, tag: 'drawer_cash_memo' },
    { id: 11, title: 'দৈনিক হিসাব ক্লোজিং (Daily Closing)', icon: <CalendarCheck className="w-5 h-5 text-slate-700" />, tag: 'drawer_daily_closing' },
    { id: 13, title: 'সেটিংস ও ব্যাকআপ (Settings)', icon: <Settings className="w-5 h-5 text-slate-700" />, tag: 'drawer_settings' },
    { id: 14, title: 'অ্যান্ড্রয়েড অ্যাপ / APK ইন্সটল', icon: <Smartphone className="w-5 h-5 text-emerald-600" />, tag: 'drawer_install_apk' },
    { id: 15, title: 'অ্যাডমিন প্যানেল (Admin Panel)', icon: <ShieldAlert className="w-5 h-5 text-red-600" />, tag: 'drawer_admin_panel', isSpecial: true },
    { id: 16, title: 'ডেমো ডাটা রিসেট (Reset Demo Data)', icon: <RotateCcw className="w-5 h-5 text-red-600" />, tag: 'drawer_reset_demo', isSpecial: true },
  ];

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex">
      {/* Backdrop */}
      <div
        className="fixed inset-0 bg-slate-900/60 backdrop-blur-xs transition-opacity"
        onClick={onClose}
      />

      {/* Drawer Panel */}
      <div className="relative w-80 max-w-[85vw] bg-white h-full shadow-2xl flex flex-col z-10 animate-in slide-in-from-left duration-200">
        {/* Drawer Header Banner */}
        <div className="bg-[#05192D] text-white p-5 border-b border-slate-800 relative">
          <button
            onClick={onClose}
            className="absolute top-4 right-4 text-slate-400 hover:text-white p-1 rounded-full bg-white/10"
          >
            <X className="w-5 h-5" />
          </button>

          <div className="flex items-center gap-3">
            <div className="w-11 h-11 rounded-full bg-emerald-600 flex items-center justify-center shadow-md">
              <Store className="w-6 h-6 text-white" />
            </div>
            <div>
              <h2 className="font-extrabold text-base leading-snug">{profile.shopName || 'ইয়াছিন টেলিকম'}</h2>
              <p className="text-xs text-slate-300">স্বত্বাধিকারী: {profile.ownerName || 'মোহাম্মদ ইয়াছিন'}</p>
            </div>
          </div>
          <p className="text-[11px] text-slate-400 mt-2.5 flex items-center gap-1">
            <span>📍 {profile.address || 'বুল্লা বাজার, লাখাই, হবিগঞ্জ'}</span>
          </p>
        </div>

        {/* Drawer Nav Items List */}
        <div className="flex-1 overflow-y-auto py-2 divide-y divide-slate-100">
          <div className="px-2 py-1 space-y-0.5">
            {items.map((item) => (
              <button
                key={item.id}
                type="button"
                data-testid={item.tag}
                onClick={() => {
                  onSelectItem(item.id);
                  onClose();
                }}
                className={`w-full flex items-center gap-3 px-3.5 py-2.5 rounded-xl text-left text-sm transition ${
                  item.isSpecial
                    ? 'text-red-600 font-bold hover:bg-red-50'
                    : 'text-slate-800 hover:bg-slate-100 font-medium'
                }`}
              >
                {item.icon}
                <span className="truncate">{item.title}</span>
              </button>
            ))}
          </div>
        </div>

        {/* Drawer Footer */}
        <div className="p-3 bg-slate-50 border-t border-slate-200 text-center text-[11px] text-slate-500">
          Yasin Telecom Smart Shop Manager • v1.0
        </div>
      </div>
    </div>
  );
};
