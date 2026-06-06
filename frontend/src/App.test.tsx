import { describe, it, expect } from 'vitest'
import App from './App'

describe('App module', () => {
  it('exports a default component', () => {
    expect(App).toBeDefined()
    expect(typeof App).toBe('function')
  })
})
