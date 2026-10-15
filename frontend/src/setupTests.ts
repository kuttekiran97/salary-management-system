import '@testing-library/jest-dom/vitest'

// jsdom has no layout engine; the data grid and charts only need ResizeObserver to exist
class ResizeObserverStub {
  observe() {}
  unobserve() {}
  disconnect() {}
}
globalThis.ResizeObserver ??= ResizeObserverStub as unknown as typeof ResizeObserver
