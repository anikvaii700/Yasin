/**
 * Converts a numeric amount to Bangla words for the cash memo.
 * Ported directly from Yasin Telecom ShopModels.kt
 */
export function takaInBengaliWords(amount: number): string {
  const rounded = Math.floor(Math.abs(amount));
  if (rounded <= 0) return 'শূন্য টাকা মাত্র';

  const units = [
    '', 'এক', 'দুই', 'তিন', 'চার', 'পাঁচ', 'ছয়', 'সাত', 'আট', 'নয়', 'দশ',
    'এগারো', 'বারো', 'তেরো', 'চোদ্দ', 'পনেরো', 'ষোল', 'সতেরো', 'আঠারো', 'উনিশ', 'বিশ',
    'একুশ', 'বাইশ', 'তেইশ', 'চব্বিশ', 'পঁচিশ', 'ছাব্বিশ', 'সাতাশ', 'আঠাশ', 'ঊনত্রিশ', 'ত্রিশ',
    'একত্রিশ', 'বত্রিশ', 'তেত্রিশ', 'চৌত্রিশ', 'পঁয়ত্রিশ', 'ছত্রিশ', 'সাঁইত্রিশ', 'আটত্রিশ', 'ঊনচল্লিশ', 'চল্লিশ',
    'একচল্লিশ', 'বিয়াল্লিশ', 'তেতাল্লিশ', 'চুয়াল্লিশ', 'পঁয়তাল্লিশ', 'ছেচল্লিশ', 'সাতচল্লিশ', 'আটচল্লিশ', 'ঊনপঞ্চাশ', 'পঞ্চাশ',
    'একান্ন', 'বায়ান্ন', 'তিপ্পান্ন', 'চুয়ান্ন', 'পঞ্চান্ন', 'ছাপ্পান্ন', 'সাতান্ন', 'আটান্ন', 'ঊনষাট', 'ষাট',
    'একষট্টি', 'বাষট্টি', 'তেষট্টি', 'চৌষট্টি', 'পঁয়ষট্টি', 'ছেষট্টি', 'সাতষট্টি', 'আটষট্টি', 'ঊনসত্তর', 'সত্তর',
    'একাত্তর', 'বাহাত্তর', 'তিয়াত্তর', 'চুয়াত্তর', 'পঁচাত্তর', 'ছিয়াত্তর', 'সাতাত্তর', 'আঠাত্তর', 'ঊনআশি', 'আশি',
    'একাশি', 'বিরাশি', 'তিরাশি', 'চুরাশি', 'পঁচাশি', 'ছিয়াশি', 'সাতাশি', 'আটাশি', 'ঊননব্বই', 'নব্বই',
    'একানব্বই', 'বানব্বই', 'তিরানব্বই', 'চুরানব্বই', 'পঁচানব্বই', 'ছিয়ানব্বই', 'সাতানব্বই', 'আটানব্বই', 'নিরানব্বই'
  ];

  function convertChunk(n: number): string {
    let str = '';
    let num = n;

    if (num >= 10000000) { // কোটি
      const crore = Math.floor(num / 10000000);
      str += convertChunk(crore) + ' কোটি ';
      num %= 10000000;
    }
    if (num >= 100000) { // লক্ষ
      const lakh = Math.floor(num / 100000);
      if (lakh < units.length) {
        str += units[lakh] + ' লক্ষ ';
      }
      num %= 100000;
    }
    if (num >= 1000) { // হাজার
      const thousand = Math.floor(num / 1000);
      if (thousand < units.length) {
        str += units[thousand] + ' হাজার ';
      }
      num %= 1000;
    }
    if (num >= 100) { // শত
      const hundred = Math.floor(num / 100);
      if (hundred < units.length) {
        str += units[hundred] + ' শত ';
      }
      num %= 100;
    }
    if (num > 0) {
      if (num < units.length) {
        str += units[num] + ' ';
      }
    }
    return str.trim();
  }

  const result = convertChunk(rounded);
  return result ? `${result} টাকা মাত্র` : 'শূন্য টাকা মাত্র';
}
