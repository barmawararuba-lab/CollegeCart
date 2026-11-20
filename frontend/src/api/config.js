// Frontend prototype switch. Flip to false once the Spring Boot API is running.
export const USE_MOCK_API = true

export const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'

export const MOCK_LATENCY_MS = 500
