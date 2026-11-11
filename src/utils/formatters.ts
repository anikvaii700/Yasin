/**
 * Format currency amount with Taka symbol (৳)
 */
export function formatTaka(amount: number): string {
  if (isNaN(amount) || amount === null || amount === undefined) {
    return '৳০';
  }
  const formatted = Math.round(amount).toLocaleString('en-IN');
  return `৳${formatted}`;
}

/**
 * Returns today's date formatted as DD-MM-YYYY (consistent with the Android codebase)
 */
export function getTodayStr(): string {
  const d = new Date();
  const day = String(d.getDate()).padStart(2, '0');
  const month = String(d.getMonth() + 1).padStart(2, '0');
  const year = d.getFullYear();
  return `${day}-${month}-${year}`;
}

/**
 * Returns current time formatted as HH:MM AM/PM
 */
export function getCurrentTimeStr(): string {
  const d = new Date();
  return d.toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit', hour12: true });
}

/**
 * Generates a unique sequential/timestamped memo number (e.g. YT-260301-482)
 */
export function generateMemoNumber(): string {
  const d = new Date();
  const yr = String(d.getFullYear()).slice(-2);
  const mo = String(d.getMonth() + 1).padStart(2, '0');
  const da = String(d.getDate()).padStart(2, '0');
  const rand = Math.floor(100 + Math.random() * 900);
  return `YT-${yr}${mo}${da}-${rand}`;
}

/**
 * Generates an invoice number for sales
 */
export function generateInvoiceNumber(): string {
  const d = new Date();
  const ts = d.getTime().toString().slice(-6);
  return `INV-${ts}`;
}
