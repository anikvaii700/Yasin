import React, { useState } from 'react';
import { useShop } from './context/ShopContext';
import { HeaderBanner } from './components/HeaderBanner';
import { NavigationDrawer } from './components/NavigationDrawer';
import { BottomNavBar, MainTabType } from './components/BottomNavBar';
import { HomeScreen } from './views/HomeScreen';
import { ProductsScreen } from './views/ProductsScreen';
import { SalesScreen } from './views/SalesScreen';
import { CashMemoScreen } from './views/CashMemoScreen';
import { DueScreen } from './views/DueScreen';
import { MoreScreen, MoreSubScreenType } from './views/MoreScreen';
import { ResetDemoDataModal } from './components/ResetDemoDataModal';
import { CheckCircle2, AlertCircle, X } from 'lucide-react';

export const AppContent: React.FC = () => {
  const { message, errorMessage, clearMessage } = useShop();

  const [currentTab, setCurrentTab] = useState<MainTabType>('HOME');
  const [isDrawerOpen, setIsDrawerOpen] = useState(false);
  const [showResetDemoModal, setShowResetDemoModal] = useState(false);

  // Quick Action triggers
  const [openAddProductTrigger, setOpenAddProductTrigger] = useState(false);
  const [openRecordSaleTrigger, setOpenRecordSaleTrigger] = useState(false);
  const [openAddExpenseTrigger, setOpenAddExpenseTrigger] = useState(false);
  const [openAddDueTrigger, setOpenAddDueTrigger] = useState(false);
  const [openAddPurchaseTrigger, setOpenAddPurchaseTrigger] = useState(false);

  // Sub-screen preference for More tab
  const [moreSubScreen, setMoreSubScreen] = useState<MoreSubScreenType>('MENU');

  const handleSelectDrawerItem = (id: number) => {
    switch (id) {
      case 1:
        setCurrentTab('HOME');
        break;
      case 2:
        setCurrentTab('PRODUCTS');
        break;
      case 3:
        setCurrentTab('SALES');
        break;
      case 4:
        setMoreSubScreen('PURCHASES');
        setCurrentTab('MORE');
        break;
      case 5:
        setCurrentTab('DUE');
        break;
      case 6:
        setMoreSubScreen('LOANS');
        setCurrentTab('MORE');
        break;
      case 7:
        setMoreSubScreen('MOBILE_BANKING');
        setCurrentTab('MORE');
        break;
      case 8:
        setMoreSubScreen('FLEXILOAD');
        setCurrentTab('MORE');
        break;
      case 9:
        setMoreSubScreen('EXPENSES');
        setCurrentTab('MORE');
        break;
      case 10:
        setCurrentTab('MEMO');
        break;
      case 11:
        setMoreSubScreen('DAILY_CLOSING');
        setCurrentTab('MORE');
        break;
      case 12:
        setMoreSubScreen('REPORTS');
        setCurrentTab('MORE');
        break;
      case 13:
        setMoreSubScreen('SETTINGS');
        setCurrentTab('MORE');
        break;
      case 14:
        setMoreSubScreen('ADMIN');
        setCurrentTab('MORE');
        break;
      case 15:
        setShowResetDemoModal(true);
        break;
      default:
        setCurrentTab('HOME');
    }
  };

  return (
    <div className="min-h-screen bg-slate-100/70 text-slate-900 flex flex-col font-sans">
      {/* 3-Step Secure Demo Data Reset Modal */}
      <ResetDemoDataModal
        isOpen={showResetDemoModal}
        onClose={() => setShowResetDemoModal(false)}
      />

      {/* Toast notifications */}
      {(message || errorMessage) && (
        <div className="fixed top-4 left-1/2 -translate-x-1/2 z-50 max-w-sm w-[90vw] transition animate-in fade-in slide-in-from-top-4 no-print">
          <div
            className={`flex items-center justify-between p-3.5 rounded-2xl shadow-xl border text-xs sm:text-sm font-semibold ${
              errorMessage
                ? 'bg-red-900/95 border-red-700 text-white'
                : 'bg-slate-900/95 border-slate-700 text-white'
            }`}
          >
            <div className="flex items-center gap-2.5">
              {errorMessage ? (
                <AlertCircle className="w-5 h-5 text-red-400 shrink-0" />
              ) : (
                <CheckCircle2 className="w-5 h-5 text-emerald-400 shrink-0" />
              )}
              <span>{errorMessage || message}</span>
            </div>
            <button
              type="button"
              onClick={clearMessage}
              className="text-slate-400 hover:text-white p-1 ml-2"
            >
              <X className="w-4 h-4" />
            </button>
          </div>
        </div>
      )}

      {/* Slide-over navigation drawer */}
      <NavigationDrawer
        isOpen={isDrawerOpen}
        onClose={() => setIsDrawerOpen(false)}
        onSelectItem={handleSelectDrawerItem}
      />

      {/* Main Container */}
      <div className="w-full max-w-2xl mx-auto flex-1 flex flex-col px-3 sm:px-4 pt-3 sm:pt-4">
        {/* Top Header Banner (hidden only when printing) */}
        <div className="mb-4 no-print">
          <HeaderBanner
            onOpenDrawer={() => setIsDrawerOpen(true)}
            onOpenAdminPanel={() => {
              setMoreSubScreen('ADMIN');
              setCurrentTab('MORE');
            }}
          />
        </div>

        {/* Tab Views */}
        <main className="flex-1">
          {currentTab === 'HOME' && (
            <HomeScreen
              onNavigateToProducts={() => setCurrentTab('PRODUCTS')}
              onNavigateToSales={() => setCurrentTab('SALES')}
              onNavigateToDue={() => setCurrentTab('DUE')}
              onNavigateToCashMemo={() => setCurrentTab('MEMO')}
              onNavigateToPurchases={() => {
                setMoreSubScreen('PURCHASES');
                setCurrentTab('MORE');
              }}
              onNavigateToExpenses={() => {
                setMoreSubScreen('EXPENSES');
                setCurrentTab('MORE');
              }}
              onNavigateToMobileBanking={() => {
                setMoreSubScreen('MOBILE_BANKING');
                setCurrentTab('MORE');
              }}
              onOpenAddProductDialog={() => {
                setOpenAddProductTrigger(true);
                setCurrentTab('PRODUCTS');
              }}
              onOpenRecordSaleDialog={() => {
                setOpenRecordSaleTrigger(true);
                setCurrentTab('SALES');
              }}
              onOpenAddExpenseDialog={() => {
                setOpenAddExpenseTrigger(true);
                setMoreSubScreen('EXPENSES');
                setCurrentTab('MORE');
              }}
              onOpenAddDueDialog={() => {
                setOpenAddDueTrigger(true);
                setCurrentTab('DUE');
              }}
              onOpenAddPurchaseDialog={() => {
                setOpenAddPurchaseTrigger(true);
                setMoreSubScreen('PURCHASES');
                setCurrentTab('MORE');
              }}
            />
          )}

          {currentTab === 'PRODUCTS' && (
            <ProductsScreen
              openAddProductTrigger={openAddProductTrigger}
              onResetAddProductTrigger={() => setOpenAddProductTrigger(false)}
            />
          )}

          {currentTab === 'SALES' && (
            <SalesScreen
              openRecordSaleTrigger={openRecordSaleTrigger}
              onResetRecordSaleTrigger={() => setOpenRecordSaleTrigger(false)}
            />
          )}

          {currentTab === 'MEMO' && <CashMemoScreen />}

          {currentTab === 'DUE' && (
            <DueScreen
              openAddDueTrigger={openAddDueTrigger}
              onResetAddDueTrigger={() => setOpenAddDueTrigger(false)}
            />
          )}

          {currentTab === 'MORE' && (
            <MoreScreen
              initialSubScreen={moreSubScreen}
              openAddPurchaseTrigger={openAddPurchaseTrigger}
              openAddExpenseTrigger={openAddExpenseTrigger}
              onResetAddPurchaseTrigger={() => setOpenAddPurchaseTrigger(false)}
              onResetAddExpenseTrigger={() => setOpenAddExpenseTrigger(false)}
            />
          )}
        </main>
      </div>

      {/* Bottom Navigation Bar */}
      <BottomNavBar
        currentTab={currentTab}
        onChangeTab={(tab) => {
          if (tab === 'MORE') setMoreSubScreen('MENU');
          setCurrentTab(tab);
        }}
      />
    </div>
  );
};
