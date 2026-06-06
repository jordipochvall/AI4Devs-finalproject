/**
 * Formats an amount in cents to its currency, with two decimals and per the locale.
 * E.g.: formatMoney(100000, 'EUR', 'es') → "1.000,00 €"
 */
export function formatMoney(cents: number, currency: string, locale: string): string {
  return new Intl.NumberFormat(locale, {
    style: 'currency',
    currency,
  }).format(cents / 100)
}
