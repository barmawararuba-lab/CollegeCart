import { useEffect, useState } from 'react'
import {
  createProduct,
  deleteProduct,
  getOrders,
  getProducts,
  getSalesSummary,
  updateOrderStatus,
  updateProduct,
} from '../api/vendorApi'
import {
  decideVendorApplication,
  getActivities,
  getPlatformStats,
  getVendorApplications,
} from '../api/adminApi'
import { AppDataContext } from './appDataContext'

const initialState = {
  loading: true,
  error: null,
  products: [],
  orders: [],
  vendors: [],
  activities: [],
  sales: null,
  platform: null,
}

function createActivity(type, title) {
  return {
    id: `ACT-${Date.now()}-${Math.random().toString(36).slice(2, 7)}`,
    type,
    title,
    createdAt: new Date().toISOString(),
  }
}

function prependActivity(activities, activity) {
  return [activity, ...activities].slice(0, 15)
}

export function AppDataProvider({ children }) {
  const [state, setState] = useState(initialState)

  async function loadData(showLoading = true) {
    if (showLoading) {
      setState((previous) => ({ ...previous, loading: true, error: null }))
    }

    try {
      const [products, orders, sales, vendors, platform, activities] =
        await Promise.all([
          getProducts(),
          getOrders(),
          getSalesSummary(),
          getVendorApplications(),
          getPlatformStats(),
          getActivities(),
        ])

      setState({
        loading: false,
        error: null,
        products,
        orders,
        vendors,
        activities,
        sales,
        platform,
      })
    } catch (error) {
      setState((previous) => ({
        ...previous,
        loading: false,
        error:
          error.message || 'Something went wrong while loading your workspace.',
      }))
    }
  }

  useEffect(() => {
    loadData()
  }, [])

  async function addProduct(input) {
    const product = await createProduct(input)
    setState((previous) => ({
      ...previous,
      products: [product, ...previous.products],
    }))
    return product
  }

  async function editProduct(id, input) {
    const updated = await updateProduct(id, input)
    setState((previous) => ({
      ...previous,
      products: previous.products.map((product) =>
        product.id === id ? updated : product,
      ),
    }))
    return updated
  }

  async function removeProduct(id) {
    await deleteProduct(id)
    setState((previous) => ({
      ...previous,
      products: previous.products.filter((product) => product.id !== id),
    }))
  }

  async function changeOrderStatus(id, status) {
    const updated = await updateOrderStatus(id, status)
    setState((previous) => ({
      ...previous,
      orders: previous.orders.map((order) =>
        order.id === id ? updated : order,
      ),
      activities: prependActivity(
        previous.activities,
        createActivity('order', `Order ${id} marked ${status}`),
      ),
    }))
    return updated
  }

  async function reviewVendor(id, decision, reason = null) {
    const updated = await decideVendorApplication(id, decision, reason)
    setState((previous) => ({
      ...previous,
      vendors: previous.vendors.map((vendor) =>
        vendor.id === id ? updated : vendor,
      ),
      activities: prependActivity(
        previous.activities,
        createActivity(
          'vendor',
          `Vendor ${decision.toLowerCase()}: ${updated.shopName}`,
        ),
      ),
    }))
    return updated
  }

  const value = {
    ...state,
    reload: () => loadData(false),
    addProduct,
    editProduct,
    removeProduct,
    changeOrderStatus,
    reviewVendor,
  }

  return (
    <AppDataContext.Provider value={value}>{children}</AppDataContext.Provider>
  )
}
