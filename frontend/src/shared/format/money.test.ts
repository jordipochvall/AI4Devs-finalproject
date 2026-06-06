import { describe, it, expect } from 'vitest'
import { formatMoney } from './money'

describe('formatMoney', () => {
  // Note: the thousands separator depends on the environment's ICU data; its absence is tolerated.
  it('formats cents to EUR in English locale', () => {
    const out = formatMoney(100000, 'EUR', 'en')
    expect(out).toMatch(/1[,]?000\.00/)
    expect(out).toContain('€')
  })

  it('formats cents to EUR in Spanish locale', () => {
    const out = formatMoney(100000, 'EUR', 'es')
    expect(out).toMatch(/1[.]?000,00/)
    expect(out).toContain('€')
  })

  it('formats zero', () => {
    expect(formatMoney(0, 'EUR', 'en')).toContain('0.00')
  })

  it('always shows two decimals', () => {
    expect(formatMoney(150, 'EUR', 'en')).toContain('1.50')
  })
})
