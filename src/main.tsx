import React from 'react';
import ReactDOM from 'react-dom/client';
import { ShopProvider } from './context/ShopContext';
import { AppContent } from './App';
import './index.css';
import { registerSW } from 'virtual:pwa-register';

// Register PWA service worker for full offline caching on Android
registerSW({ immediate: true });

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <ShopProvider>
      <AppContent />
    </ShopProvider>
  </React.StrictMode>
);
