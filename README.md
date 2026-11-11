# Yasin Telecom (ইয়াছিন টেলিকম)

Smart Shop Management Web Application for **Yasin Telecom** (বুল্লা বাজার, লাখাই, হবিগঞ্জ).

Rewritten from the original Android application into a high-performance **React + TypeScript + Tailwind CSS** application, fully preserving all features, data models, workflows, and authentic bilingual design.

## Features Ported

1. **ড্যাশবোর্ড (Dashboard Hub)**
   - 8 vibrant gradient metric cards (Today's Sales, Today's Profit, Total Products, Total Stock Qty, Low Stock Alert, Customer Due, Today's Expenses, Estimated Cash Inflow).
   - 4 soft-accent business indicator boxes (Mobile Banking + Flexiload profits, Personal Lent receivable, Personal Borrowed payable, Total Potential Shop Stock Value).
   - 6 Quick Action shortcuts + Cash Memo generator.
   - Live stock health overview with categorized counts (পর্যাপ্ত, কমে আসছে, শেষ).
   - **Admin Access**: 10-Second Long Press on the header shop card with visual countdown unlock, plus direct navigation.

2. **পণ্য ও ইনভেন্টরি স্টক (Products & Inventory)**
   - Fast instant search by product name, brand, model, or product code.
   - Category filtering across all 15 default telecommunication categories and user-created custom categories.
   - Stock health filters (All, Low Stock alerts, Out of Stock).
   - Add product, Edit product, and Delete product operations.
   - **Quick Stock Addition (+ স্টক যোগ)** modal with supplier tracking, unit purchase rate update, and automatic inventory synchronization.

3. **বিক্রয় হিসাব (Sales Records)**
   - Multi-item point-of-sale checkout with real-time stock availability check.
   - Subtotal, discount, grand total, cost price, and gross profit tracking.
   - Cash received and remaining due tracking with automatic customer ledger updates.
   - Comprehensive sales history list with invoice numbers and item summaries.

4. **ক্যাশ মেমো (Cash Memo System)**
   - Professional cash memo generator with invoice number, date, time, customer details, and dynamic product line items.
   - **Bengali Words Generator (`takaInBengaliWords`)**: Automatically writes out the exact total in Bengali words (e.g. "ছয় শত সত্তর টাকা মাত্র").
   - **Dual Visual Styles**:
     - *Orange-Amber Executive Style* (matching reference invoice template with curved header arches, customer badge grids, peach column tables, and dual signatures).
     - *Cyan-Blue Mobile Style* (with smartphone brand pills, vibrant badges, and signature footer).
   - **One-Click Actions**:
     - Direct WhatsApp sharing link with pre-formatted Bengali receipt text.
     - Browser print & PDF export with print CSS styles.
     - Clipboard copy and saved memo archive with search and delete.

5. **বাকি খাতা (Customer Due Ledger)**
   - Customer profile management with outstanding due balances and mobile contact.
   - Transaction logging: Add Due (বাকি যোগ) and Record Payment (টাকা জমা).
   - Full historical ledger drawer for each customer showing every debit/credit transaction.

6. **অন্যান্য সেবা (More Business Features)**
   - **পণ্য ক্রয় (Purchases)**: Track supplier purchases and update inventory.
   - **দোকানের খরচ (Expenses)**: Categorized daily shop expenses (Tea/Snacks, Rent & Utilities, Transport, Electric, Staff Salary, Others).
   - **ব্যক্তিগত ধার-দেনা (Personal Loans)**: Lent (পাওনা) and Borrowed (দেনা) management with repayments.
   - **বিকাশ ও নগদ (bKash & Nagad)**: Cash In, Cash Out, and Send Money transaction log with commission profits.
   - **ফ্লেক্সিলোড (Flexiload)**: Operator-wise mobile recharge (GP, Banglalink, Robi, Airtel, Teletalk) with profit tracking.
   - **দৈনিক হিসাব ক্লোজিং (Daily Closing)**: Automated end-of-day revenue, profit, cash flow calculation, expected vs actual cash difference (Surplus/Deficit), and closing archive.
   - **রিপোর্ট ও বিশ্লেষণ (Reports)**: Profit sources breakdown, revenue trends, and financial metrics.
   - **সেটিংস ও ব্যাকআপ (Settings & Backup)**: Shop profile customization, custom categories, full JSON backup download, and backup file restore.
   - **অ্যাডমিন প্যানেল (Admin Panel)**: PIN protected (default PIN: `1234`), change PIN, safe demo data reset, and complete database reset.

## Development

- Start Dev Server: `npm run dev` (starts Vite on host `0.0.0.0`, port `3000`)
- Build Application: `npm run build` (`tsc && vite build`)
- Lint: `npm run lint` (`tsc --noEmit`)
