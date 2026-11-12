import React, { useState, useRef, useEffect } from 'react';
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
import { SplashScreen } from './components/SplashScreen';
import { AndroidInstallModal } from './components/AndroidInstallModal';
import { CheckCircle2, AlertCircle, X, LogOut } from 'lucide-react';

export const AppContent: React.FC = () => {
  const { message, errorMessage, clearMessage, showMessage } = useShop();

  const [currentTab, setCurrentTab] = useState<MainTabType>('HOME');
  const [isDrawerOpen, setIsDrawerOpen] = useState(false);
  const [showResetDemoModal, setShowResetDemoModal] = useState(false);
  const [showAndroidInstallModal, setShowAndroidInstallModal] = useState(false);
  const [showExitConfirmDialog, setShowExitConfirmDialog] = useState(false);

  // Splash Screen: Shows strictly ONCE when the app is freshly opened
  const [showSplash, setShowSplash] = useState(() => {
    try {
      return sessionStorage.getItem('yasin_telecom_splash_shown') !== 'true';
    } catch {
      return false;
    }
  });

  // Quick Action triggers
  const [openAddProductTrigger, setOpenAddProductTrigger] = useState(false);
  const [openRecordSaleTrigger, setOpenRecordSaleTrigger] = useState(false);
  const [openAddExpenseTrigger, setOpenAddExpenseTrigger] = useState(false);
  const [openAddDueTrigger, setOpenAddDueTrigger] = useState(false);
  const [openAddPurchaseTrigger, setOpenAddPurchaseTrigger] = useState(false);

  // Sub-screen preference for More tab
  const [moreSubScreen, setMoreSubScreen] = useState<MoreSubScreenType>('MENU');

  // References to keep event listeners synced without stale closures
  const currentTabRef = useRef(currentTab);
  currentTabRef.current = currentTab;

  const moreSubScreenRef = useRef(moreSubScreen);
  moreSubScreenRef.current = moreSubScreen;

  const isDrawerOpenRef = useRef(isDrawerOpen);
  isDrawerOpenRef.current = isDrawerOpen;

  const showResetDemoModalRef = useRef(showResetDemoModal);
  showResetDemoModalRef.current = showResetDemoModal;

  const showAndroidInstallModalRef = useRef(showAndroidInstallModal);
  showAndroidInstallModalRef.current = showAndroidInstallModal;

  const showExitConfirmDialogRef = useRef(showExitConfirmDialog);
  showExitConfirmDialogRef.current = showExitConfirmDialog;

  const lastBackPressTimeRef = useRef<number>(0);
  const allowExitRef = useRef<boolean>(false);

  // Prevent direct exit: Trap history back button & handle double-press on Home
  useEffect(() => {
    // Push initial history state so the phone's back button can be intercepted
    window.history.pushState({ app: 'yasin-telecom' }, '', window.location.href);

    const handlePopState = () => {
      if (allowExitRef.current) {
        return;
      }

      // Re-push state immediately to preserve the back trap
      window.history.pushState({ app: 'yasin-telecom' }, '', window.location.href);

      // 1. If Exit dialog is currently open, close it
      if (showExitConfirmDialogRef.current) {
        setShowExitConfirmDialog(false);
        return;
      }

      // 2. If Android install modal is open, close it
      if (showAndroidInstallModalRef.current) {
        setShowAndroidInstallModal(false);
        return;
      }

      // 3. If Reset demo modal is open, close it
      if (showResetDemoModalRef.current) {
        setShowResetDemoModal(false);
        return;
      }

      // 4. If Navigation drawer is open, close it
      if (isDrawerOpenRef.current) {
        setIsDrawerOpen(false);
        return;
      }

      // 5. If on MORE sub-screen other than MENU, return to MORE MENU
      if (currentTabRef.current === 'MORE' && moreSubScreenRef.current !== 'MENU') {
        setMoreSubScreen('MENU');
        return;
      }

      // 6. If on any other tab than HOME, return to HOME
      if (currentTabRef.current !== 'HOME') {
        setCurrentTab('HOME');
        return;
      }

      // 7. On HOME screen: ALWAYS OPEN EXIT CONFIRMATION DIALOG IMMEDIATELY!
      setShowExitConfirmDialog(true);
    };

    const handleBeforeUnload = (e: BeforeUnloadEvent) => {
      if (allowExitRef.current) return;
      e.preventDefault();
      e.returnValue = '';
      return '';
    };

    window.addEventListener('popstate', handlePopState);
    window.addEventListener('beforeunload', handleBeforeUnload);
    return () => {
      window.removeEventListener('popstate', handlePopState);
      window.removeEventListener('beforeunload', handleBeforeUnload);
    };
  }, [showMessage]);

  const handleConfirmExit = () => {
    setShowExitConfirmDialog(false);
    allowExitRef.current = true;
    // Attempt closing window (supported in WebAPK / standalone PWA)
    try {
      window.close();
    } catch {
      // ignore
    }
    // Fallback: history back
    setTimeout(() => {
      try {
        window.history.back();
      } catch {
        // ignore
      }
    }, 150);
  };

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
        setShowAndroidInstallModal(true);
        break;
      case 15:
        setMoreSubScreen('ADMIN');
        setCurrentTab('MORE');
        break;
      case 16:
        setShowResetDemoModal(true);
        break;
      case 17:
        setShowExitConfirmDialog(true);
        break;
      default:
        setCurrentTab('HOME');
    }
  };

  return (
    <div className="min-h-screen bg-slate-100/70 text-slate-900 flex flex-col font-sans">
      {/* 3-Second Ball Bounce & Bloom Splash Screen (Shows strictly once per session) */}
      {showSplash && (
        <SplashScreen onFinish={() => setShowSplash(false)} />
      )}

      {/* Android APK / App Install Modal */}
      <AndroidInstallModal
        isOpen={showAndroidInstallModal}
        onClose={() => setShowAndroidInstallModal(false)}
      />

      {/* 3-Step Secure Demo Data Reset Modal */}
      <ResetDemoDataModal
        isOpen={showResetDemoModal}
        onClose={() => setShowResetDemoModal(false)}
      />

      {/* Exit Confirmation Dialog (when pressing back twice on HOME) */}
      {showExitConfirmDialog && (
        <div
          className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-sm animate-in fade-in duration-200"
          onClick={() => setShowExitConfirmDialog(false)}
        >
          <div
            className="w-full max-w-sm bg-white rounded-3xl shadow-2xl border border-slate-100 overflow-hidden p-6 text-center animate-in zoom-in-95 duration-200"
            onClick={(e) => e.stopPropagation()}
          >
            <div className="w-16 h-16 rounded-2xl bg-amber-50 border-2 border-amber-100 text-amber-600 flex items-center justify-center mx-auto mb-4 shadow-sm">
              <LogOut className="w-8 h-8 ml-0.5" />
            </div>

            <h3 className="text-xl font-bold text-slate-800 mb-1.5">
              আপনি কি অ্যাপ থেকে বের হতে চান?
            </h3>
            <p className="text-xs text-slate-500 mb-6 font-medium leading-relaxed">
              ইয়াছিন টেলিকম স্মার্ট শপ ম্যানেজার
            </p>

            <div className="grid grid-cols-2 gap-3">
              <button
                type="button"
                onClick={() => setShowExitConfirmDialog(false)}
                className="w-full py-3 px-4 rounded-xl font-bold text-slate-700 bg-slate-100 hover:bg-slate-200 active:scale-95 transition-all text-sm cursor-pointer shadow-sm"
              >
                না, থাকুন
              </button>
              <button
                type="button"
                onClick={handleConfirmExit}
                className="w-full py-3 px-4 rounded-xl font-bold text-white bg-red-600 hover:bg-red-700 active:scale-95 transition-all shadow-md shadow-red-200 text-sm flex items-center justify-center gap-1.5 cursor-pointer"
              >
                <LogOut className="w-4 h-4" />
                <span>হ্যাঁ, বের হন</span>
              </button>
            </div>
          </div>
        </div>
      )}

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

      {/* Top Header Bar (Edge-to-Edge like Bottom Navigation Bar, in ALL sections) */}
      <div className="no-print w-full">
        <HeaderBanner
          isHomeScreen={currentTab === 'HOME'}
          onOpenDrawer={() => setIsDrawerOpen(true)}
          onOpenAdminPanel={() => {
            setMoreSubScreen('ADMIN');
            setCurrentTab('MORE');
          }}
          onOpenAndroidInstall={() => setShowAndroidInstallModal(true)}
        />
      </div>

      {/* Main Container */}
      <div className={`w-full max-w-2xl mx-auto flex-1 flex flex-col px-2.5 sm:px-4 ${currentTab === 'HOME' ? 'pt-0' : 'pt-3 sm:pt-4'}`}>
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
