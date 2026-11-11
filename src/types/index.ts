export interface ShopProfile {
  id: string;
  userId: string;
  shopName: string;
  ownerName: string;
  address: string;
  phone: string;
  email: string;
  tagline: string;
  currency: string;
  lowStockAlertThreshold: number;
  adminPin: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface Product {
  id: string;
  userId: string;
  name: string;
  category: string;
  brand: string;
  model: string;
  purchasePrice: number;
  sellingPrice: number;
  currentStock: number;
  minStockAlert: number;
  productCode: string;
  notes: string;
  isDemo?: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface StockAddition {
  id: string;
  userId: string;
  productId: string;
  productName: string;
  previousQuantity: number;
  addedQuantity: number;
  totalQuantity: number;
  purchasePrice: number;
  dateAdded: string; // DD-MM-YYYY
  supplierName: string;
  notes: string;
  isDemo?: boolean;
  createdAt?: string;
}

export interface SaleItem {
  productId: string;
  productName: string;
  quantity: number;
  sellingPrice: number;
  purchaseCost: number;
  subtotal: number;
  profit: number;
}

export interface Sale {
  id: string;
  userId: string;
  invoiceNumber: string;
  customerName: string;
  customerPhone: string;
  itemsSummary: string;
  totalAmount: number;
  totalCost: number;
  grossProfit: number;
  paidAmount: number;
  dueAmount: number;
  saleDate: string; // DD-MM-YYYY
  notes: string;
  isDemo?: boolean;
  items?: SaleItem[];
  createdAt?: string;
  updatedAt?: string;
}

export interface CashMemoItem {
  productId?: string;
  description: string;
  quantity: number;
  rate: number;
  amount: number;
}

export interface CashMemo {
  id: string;
  userId: string;
  memoNumber: string;
  date: string; // DD-MM-YYYY
  time: string;
  customerName: string;
  customerMobile: string;
  customerAddress: string;
  items: CashMemoItem[];
  subtotal: number;
  discount: number;
  grandTotal: number;
  paidAmount: number;
  dueAmount: number;
  paymentStatus: 'PAID' | 'DUE' | 'PARTIAL';
  notes: string;
  saleId?: string;
  isDemo?: boolean;
  createdAt?: string;
}

export interface Purchase {
  id: string;
  userId: string;
  productName: string;
  productId: string;
  supplierName: string;
  quantity: number;
  purchasePrice: number;
  totalAmount: number;
  purchaseDate: string; // DD-MM-YYYY
  paymentStatus: 'PAID' | 'DUE' | 'PARTIAL';
  notes: string;
  isDemo?: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface Expense {
  id: string;
  userId: string;
  title: string;
  amount: number;
  category: string;
  expenseDate: string; // DD-MM-YYYY
  description: string;
  isDemo?: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface CustomerTransaction {
  id: string;
  userId: string;
  customerId: string;
  type: 'DUE' | 'PAYMENT';
  amount: number;
  notes: string;
  transactionDate: string; // DD-MM-YYYY
  isDemo?: boolean;
  createdAt?: string;
}

export interface Customer {
  id: string;
  userId: string;
  name: string;
  mobile: string;
  address: string;
  totalDue: number;
  totalPaid: number;
  remainingDue: number;
  isDemo?: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface Loan {
  id: string;
  userId: string;
  personName: string;
  mobile: string;
  loanType: 'LENT' | 'BORROWED' | 'HOUSEHOLD' | 'PERSONAL' | 'FAMILY' | 'OTHER';
  amount: number;
  paidAmount: number;
  remainingAmount: number;
  description: string;
  expectedPaymentDate: string; // DD-MM-YYYY
  isDemo?: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface MobileBankingTx {
  id: string;
  userId: string;
  provider: 'BKASH' | 'NAGAD' | 'ROCKET' | 'UPAY';
  type: 'CASH_IN' | 'CASH_OUT' | 'SEND_MONEY';
  amount: number;
  commissionProfit: number;
  serviceCharge: number;
  customerPhone: string;
  notes: string;
  transactionDate: string; // DD-MM-YYYY
  isDemo?: boolean;
  createdAt?: string;
}

export interface FlexiloadTx {
  id: string;
  userId: string;
  mobileNumber: string;
  operator: 'Grameenphone' | 'Banglalink' | 'Robi' | 'Airtel' | 'Teletalk';
  rechargeAmount: number;
  customerPayment: number;
  rechargeCost: number;
  profit: number;
  transactionDate: string; // DD-MM-YYYY
  notes: string;
  isDemo?: boolean;
  createdAt?: string;
}

export interface DailyClosing {
  id: string;
  userId: string;
  closingDate: string; // DD-MM-YYYY
  productSales: number;
  mobileBankingVolume: number;
  flexiloadVolume: number;
  grossProfit: number;
  totalExpenses: number;
  customerPaymentsReceived: number;
  totalCashReceived: number;
  totalCashPaid: number;
  expectedCash: number;
  actualCash: number;
  cashDifference: number;
  notes: string;
  isDemo?: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface CustomCategory {
  id: string;
  userId: string;
  name: string;
  isDemo?: boolean;
  createdAt?: string;
}
