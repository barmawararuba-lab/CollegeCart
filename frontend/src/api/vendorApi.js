/**
 * Vendor API service.
 *
 * Spring Boot endpoints used when USE_MOCK_API is false:
 *   GET    /vendor/products
 *   POST   /vendor/products
 *   PUT    /vendor/products/{id}
 *   DELETE /vendor/products/{id}
 *   GET    /vendor/orders
 *   PATCH  /vendor/orders/{id}/status
 *   GET    /vendor/analytics/sales
 */
import { request } from './client'
import { USE_MOCK_API } from './config'
import {
  mockCreateProduct,
  mockDeleteProduct,
  mockGetOrders,
  mockGetProducts,
  mockGetSalesSummary,
  mockUpdateOrderStatus,
  mockUpdateProduct,
} from './mockApi'

export function getProducts() {
  if (USE_MOCK_API) return mockGetProducts()
  return request('/vendor/products')
}

export function createProduct(input) {
  if (USE_MOCK_API) return mockCreateProduct(input)
  return request('/vendor/products', { method: 'POST', body: input })
}

export function updateProduct(id, input) {
  if (USE_MOCK_API) return mockUpdateProduct(id, input)
  return request(`/vendor/products/${id}`, { method: 'PUT', body: input })
}

export function deleteProduct(id) {
  if (USE_MOCK_API) return mockDeleteProduct(id)
  return request(`/vendor/products/${id}`, { method: 'DELETE' })
}

export function getOrders() {
  if (USE_MOCK_API) return mockGetOrders()
  return request('/vendor/orders')
}

export function updateOrderStatus(id, status) {
  if (USE_MOCK_API) return mockUpdateOrderStatus(id, status)
  return request(`/vendor/orders/${id}/status`, {
    method: 'PATCH',
    body: { status },
  })
}

export function getSalesSummary() {
  if (USE_MOCK_API) return mockGetSalesSummary()
  return request('/vendor/analytics/sales')
}
