import { STOCK_THRESHOLD } from '../constants'

export function getStockStatus(stock) {
  if (stock === 0) return { label: 'Out of stock', tone: 'danger' }
  if (stock <= STOCK_THRESHOLD) return { label: 'Low stock', tone: 'warning' }
  return { label: 'In stock', tone: 'success' }
}

export const AVAILABILITY_FILTERS = [
  { value: 'all', label: 'All availability' },
  { value: 'in-stock', label: 'In stock' },
  { value: 'low-stock', label: 'Low stock' },
  { value: 'out-of-stock', label: 'Out of stock' },
]

export function matchesAvailability(stock, filter) {
  if (filter === 'all') return true
  if (filter === 'in-stock') return stock > STOCK_THRESHOLD
  if (filter === 'low-stock') return stock > 0 && stock <= STOCK_THRESHOLD
  if (filter === 'out-of-stock') return stock === 0
  return true
}
