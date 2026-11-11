import React from 'react';
import {
  Home,
  Package,
  ShoppingCart,
  FileText,
  Wallet,
  MoreHorizontal,
} from 'lucide-react';

export type MainTabType = 'HOME' | 'PRODUCTS' | 'SALES' | 'MEMO' | 'DUE' | 'MORE' | 'ADMIN';

interface BottomNavBarProps {
  currentTab: MainTabType;
  onChangeTab: (tab: MainTabType) => void;
}

export const BottomNavBar: React.FC<BottomNavBarProps> = ({ currentTab, onChangeTab }) => {
  const tabs = [
    { key: 'HOME' as MainTabType, label: 'হোম', icon: Home, tag: 'tab_home' },
    { key: 'PRODUCTS' as MainTabType, label: 'পণ্য', icon: Package, tag: 'tab_products' },
    { key: 'SALES' as MainTabType, label: 'বিক্রয়', icon: ShoppingCart, tag: 'tab_sales' },
    { key: 'MEMO' as MainTabType, label: 'মেমো', icon: FileText, tag: 'tab_memo', highlight: true },
    { key: 'DUE' as MainTabType, label: 'বাকি', icon: Wallet, tag: 'tab_due' },
    { key: 'MORE' as MainTabType, label: 'আরও', icon: MoreHorizontal, tag: 'tab_more' },
  ];

  return (
    <nav className="fixed bottom-0 left-0 right-0 z-40 bg-white border-t border-slate-200/90 shadow-[0_-4px_16px_rgba(0,0,0,0.06)] no-print">
      <div className="max-w-md md:max-w-xl mx-auto flex items-center justify-around px-2 py-1.5 safe-area-bottom">
        {tabs.map((tab) => {
          const isSelected = currentTab === tab.key;
          const IconComponent = tab.icon;

          return (
            <button
              key={tab.key}
              type="button"
              data-testid={tab.tag}
              onClick={() => onChangeTab(tab.key)}
              className={`flex flex-col items-center justify-center flex-1 py-1 px-1 rounded-xl transition ${
                isSelected
                  ? tab.highlight
                    ? 'text-orange-600 font-bold'
                    : 'text-[#0A2540] font-bold'
                  : 'text-slate-500 hover:text-slate-700 font-normal'
              }`}
            >
              <div
                className={`flex items-center justify-center w-9 h-7 rounded-full transition ${
                  isSelected
                    ? tab.highlight
                      ? 'bg-orange-500 text-white'
                      : 'bg-emerald-600 text-white'
                    : 'text-slate-600'
                }`}
              >
                <IconComponent className="w-4 h-4" />
              </div>
              <span className="text-[11px] mt-0.5 tracking-tight">{tab.label}</span>
            </button>
          );
        })}
      </div>
    </nav>
  );
};
