import React from 'react';
import ReactDOM from 'react-dom/client';
import { ShopProvider } from './context/ShopContext';
import { AppContent } from './App';
import './index.css';
import { registerSW } from 'virtual:pwa-register';

// Register PWA service worker for full offline caching on Android without forced reloads
registerSW({
  immediate: true,
  onNeedRefresh() {
    // Service worker cached new assets silently
  },
  onOfflineReady() {
    // App is ready to work offline
  },
});

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <ShopProvider>
      <AppContent />
    </ShopProvider>
  </React.StrictMode>
);
