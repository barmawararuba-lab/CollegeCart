import { useEffect, useState } from 'react'
import DashboardLayout from './components/layout/DashboardLayout'
import { AppDataProvider } from './context/AppDataProvider'
import { ToastProvider } from './context/ToastProvider'
import AdminDashboard from './pages/admin/AdminDashboard'
import VendorApproval from './pages/admin/VendorApproval'
import OrderManagement from './pages/vendor/OrderManagement'
import ProductManagement from './pages/vendor/ProductManagement'
import VendorDashboard from './pages/vendor/VendorDashboard'

const DEFAULT_PATH = '/vendor/dashboard'

const ROUTES = {
  '/vendor/dashboard': { role: 'vendor', Page: VendorDashboard },
  '/vendor/products': { role: 'vendor', Page: ProductManagement },
  '/vendor/orders': { role: 'vendor', Page: OrderManagement },
  '/admin/dashboard': { role: 'admin', Page: AdminDashboard },
  '/admin/vendors': { role: 'admin', Page: VendorApproval },
}

function readPath() {
  const hash = window.location.hash.replace(/^#/, '')
  return ROUTES[hash] ? hash : DEFAULT_PATH
}

function App() {
  const [path, setPath] = useState(readPath)
  const route = ROUTES[path]
  const Page = route.Page

  useEffect(() => {
    function syncFromHash() {
      const hash = window.location.hash.replace(/^#/, '')
      if (!ROUTES[hash]) {
        window.history.replaceState(null, '', `#${DEFAULT_PATH}`)
        setPath(DEFAULT_PATH)
        return
      }
      setPath(hash)
    }

    syncFromHash()
    window.addEventListener('hashchange', syncFromHash)
    return () => window.removeEventListener('hashchange', syncFromHash)
  }, [])

  function navigate(nextPath) {
    if (window.location.hash === `#${nextPath}`) {
      setPath(nextPath)
      return
    }
    window.location.hash = nextPath
  }

  return (
    <ToastProvider>
      <AppDataProvider>
        <DashboardLayout role={route.role} path={path}>
          <Page navigate={navigate} />
        </DashboardLayout>
      </AppDataProvider>
    </ToastProvider>
  )
}

export default App
