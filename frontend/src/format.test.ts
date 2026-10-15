import { formatCompactMoney, formatCount, formatDate, formatMoney } from './format'

describe('formatMoney', () => {
  it('formats US dollars with thousands grouping', () => {
    expect(formatMoney(102000, 'USD')).toBe('$102,000')
  })

  it('formats Indian rupees with lakh grouping', () => {
    expect(formatMoney(2567000, 'INR')).toBe('₹25,67,000')
  })

  it('shows the currency symbol of the local currency', () => {
    expect(formatMoney(71000, 'EUR')).toBe('€71,000')
    expect(formatMoney(59000, 'GBP')).toBe('£59,000')
  })
})

describe('formatCompactMoney', () => {
  it('shortens large amounts for chart labels', () => {
    expect(formatCompactMoney(102000, 'USD')).toBe('$102K')
  })
})

describe('formatCount', () => {
  it('groups thousands', () => {
    expect(formatCount(10000)).toBe('10,000')
  })
})

describe('formatDate', () => {
  it('formats an ISO date without shifting the day', () => {
    expect(formatDate('2022-04-01')).toBe('1 Apr 2022')
  })
})
