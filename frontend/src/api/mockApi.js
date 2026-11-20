import {
  CANCELLABLE_STATUSES,
  NEXT_ORDER_STATUS,
  ORDER_STATUS,
} from '../constants'
import { MOCK_LATENCY_MS } from './config'
import {
  initialActivities,
  initialOrders,
  initialPlatformStats,
  initialProducts,
  initialVendors,
} from './mockData'

const store = {
  products: initialProducts,
  orders: initialOrders,
  vendors: initialVendors,
  activities: initialActivities,
  platform: initialPlatformStats,
}

let productSequence = 2000

const clone = (value) => JSON.parse(JSON.stringify(value))
const wait = () =>
  new Promise((resolve) => {
    setTimeout(resolve, MOCK_LATENCY_MS)
  })

export async function mockGetProducts() {
  await wait()
  return clone(store.products)
}

export async function mockCreateProduct(input) {
  await wait()
  productSequence += 1
  const product = {
    ...input,
    id: `PRD-${productSequence}`,
    updatedAt: new Date().toISOString(),
  }
  store.products = [product, ...store.products]
  return clone(product)
}

export async function mockUpdateProduct(id, input) {
  await wait()
  const index = store.products.findIndex((product) => product.id === id)
  if (index === -1) throw new Error('That product could not be found.')

  store.products[index] = {
    ...store.products[index],
    ...input,
    id,
    updatedAt: new Date().toISOString(),
  }
  return clone(store.products[index])
}

export async function mockDeleteProduct(id) {
  await wait()
  store.products = store.products.filter((product) => product.id !== id)
  return { id }
}

export async function mockGetOrders() {
  await wait()
  return clone(store.orders)
}

export async function mockUpdateOrderStatus(id, status) {
  await wait()
  const order = store.orders.find((item) => item.id === id)
  if (!order) throw new Error('That order could not be found.')

  const isAdvance = NEXT_ORDER_STATUS[order.status] === status
  const isCancellation =
    status === ORDER_STATUS.CANCELLED && CANCELLABLE_STATUSES.includes(order.status)

  if (!isAdvance && !isCancellation) {
    throw new Error(`An order cannot move from ${order.status} to ${status}.`)
  }

  order.status = status
  order.history = [
    ...order.history,
    { status, at: new Date().toISOString() },
  ]

  return clone(order)
}

const DAY_NAMES = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat']

function startOfDay(daysAgo = 0) {
  const date = new Date()
  date.setHours(0, 0, 0, 0)
  date.setDate(date.getDate() - daysAgo)
  return date
}

export async function mockGetSalesSummary() {
  await wait()

  // Derived from the same order store so dashboard totals never drift.
  const week = Array.from({ length: 7 }, (_, index) => {
    const date = startOfDay(6 - index)
    return { day: DAY_NAMES[date.getDay()], date: date.toISOString(), total: 0 }
  })
  const weekStart = new Date(week[0].date).getTime()
  const weekOrders = store.orders.filter(
    (order) => new Date(order.placedAt).getTime() >= weekStart,
  )
  const billable = weekOrders.filter(
    (order) => order.status !== ORDER_STATUS.CANCELLED,
  )

  billable.forEach((order) => {
    const placedAt = new Date(order.placedAt)
    const bucket = week.find((day) => {
      const dayDate = new Date(day.date)
      return (
        dayDate.getFullYear() === placedAt.getFullYear() &&
        dayDate.getMonth() === placedAt.getMonth() &&
        dayDate.getDate() === placedAt.getDate()
      )
    })
    if (bucket) bucket.total += order.total
  })

  const revenue = billable.reduce((sum, order) => sum + order.total, 0)

  return clone({
    orders: weekOrders.length,
    revenue,
    averageOrderValue: billable.length
      ? Math.round(revenue / billable.length)
      : 0,
    week,
  })
}

export async function mockGetVendorApplications() {
  await wait()
  return clone(store.vendors)
}

export async function mockDecideVendorApplication(id, decision, reason) {
  await wait()
  const vendor = store.vendors.find((item) => item.id === id)
  if (!vendor) throw new Error('That vendor application could not be found.')
  if (vendor.status !== 'Pending') {
    throw new Error('This application has already been reviewed.')
  }
  if (decision !== 'Approved' && decision !== 'Rejected') {
    throw new Error('Choose a valid review decision.')
  }

  vendor.status = decision
  vendor.reviewedAt = new Date().toISOString()
  vendor.rejectionReason = decision === 'Rejected' ? reason : null

  return clone(vendor)
}

export async function mockGetPlatformStats() {
  await wait()
  return clone(store.platform)
}

export async function mockGetActivities() {
  await wait()
  const sorted = [...store.activities].sort(
    (a, b) => new Date(b.createdAt) - new Date(a.createdAt),
  )
  return clone(sorted)
}
