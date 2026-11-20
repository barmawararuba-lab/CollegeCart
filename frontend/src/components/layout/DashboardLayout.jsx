import { useEffect, useState } from 'react'
import { NAV_ITEMS } from '../../config/navigation'
import { useAppData } from '../../context/useAppData'
import Sidebar from './Sidebar'
import Topbar from './Topbar'

export default function DashboardLayout({ role, path, children }) {
  const [navOpen, setNavOpen] = useState(false)
  const { vendors, loading } = useAppData()

  useEffect(() => {
    if (!navOpen) return undefined

    const handleKeyDown = (event) => {
      if (event.key === 'Escape') setNavOpen(false)
    }

    document.addEventListener('keydown', handleKeyDown)
    return () => document.removeEventListener('keydown', handleKeyDown)
  }, [navOpen])

  const pendingCount = loading
    ? 0
    : vendors.filter((vendor) => vendor.status === 'Pending').length
  const badges = role === 'admin' ? { '/admin/vendors': pendingCount } : {}
  const current = NAV_ITEMS[role].find((item) => item.path === path)

  return (
    <div className={`app-shell role-${role}`}>
      <Sidebar
        role={role}
        path={path}
        open={navOpen}
        onClose={() => setNavOpen(false)}
        badges={badges}
      />

      {navOpen ? (
        <button
          type="button"
          className="nav-backdrop"
          aria-label="Close navigation"
          onClick={() => setNavOpen(false)}
        />
      ) : null}

      <div className="app-main">
        <Topbar
          role={role}
          current={current}
          onMenu={() => setNavOpen(true)}
        />
        <main className="app-content" id="main-content">
          {children}
        </main>
        <footer className="app-footer">
          CollegeCart · Vendor &amp; Admin workspace · Prototype UI with sample
          data
        </footer>
      </div>
    </div>
  )
}
