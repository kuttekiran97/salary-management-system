// Indian rupees use lakh/crore grouping (25,67,000); other currencies use thousands grouping
const localeFor = (currency: string) => (currency === 'INR' ? 'en-IN' : 'en-US')

/** A whole-unit amount with its currency symbol, e.g. "$102,000" or "₹25,67,000". */
export function formatMoney(amount: number, currency: string): string {
  return new Intl.NumberFormat(localeFor(currency), {
    style: 'currency',
    currency,
    maximumFractionDigits: 0,
  }).format(amount)
}

/** A short amount for chart labels and axes, e.g. "$102K" or "₹26L". */
export function formatCompactMoney(amount: number, currency: string): string {
  return new Intl.NumberFormat(localeFor(currency), {
    style: 'currency',
    currency,
    notation: 'compact',
    maximumFractionDigits: 1,
  }).format(amount)
}

export function formatCount(count: number): string {
  return new Intl.NumberFormat('en-US').format(count)
}

/** "2022-04-01" -> "1 Apr 2022". Parsed as a plain date so time zones cannot shift the day. */
export function formatDate(isoDate: string): string {
  const [year, month, day] = isoDate.split('-').map(Number)
  return new Intl.DateTimeFormat('en-GB', { day: 'numeric', month: 'short', year: 'numeric', timeZone: 'UTC' })
    .format(new Date(Date.UTC(year, month - 1, day)))
}
