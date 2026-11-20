/**
 * Admin API service.
 *
 * Spring Boot endpoints used when USE_MOCK_API is false:
 *   GET   /admin/stats
 *   GET   /admin/vendor-applications
 *   PATCH /admin/vendor-applications/{id}   body: { decision, reason }
 *   GET   /admin/activities
 */
import { request } from './client'
import { USE_MOCK_API } from './config'
import {
  mockDecideVendorApplication,
  mockGetActivities,
  mockGetPlatformStats,
  mockGetVendorApplications,
} from './mockApi'

export function getPlatformStats() {
  if (USE_MOCK_API) return mockGetPlatformStats()
  return request('/admin/stats')
}

export function getVendorApplications() {
  if (USE_MOCK_API) return mockGetVendorApplications()
  return request('/admin/vendor-applications')
}

export function decideVendorApplication(id, decision, reason = null) {
  if (USE_MOCK_API) return mockDecideVendorApplication(id, decision, reason)
  return request(`/admin/vendor-applications/${id}`, {
    method: 'PATCH',
    body: { decision, reason },
  })
}

export function getActivities() {
  if (USE_MOCK_API) return mockGetActivities()
  return request('/admin/activities')
}
