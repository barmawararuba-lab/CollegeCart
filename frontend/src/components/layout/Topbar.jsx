import Icon from '../Icon'
import Avatar from '../ui/Avatar'
import { DEMO_ACCOUNTS, ROLE_META } from '../../config/navigation'

export default function Topbar({ role, current, onMenu }) {
  const meta = ROLE_META[role]
  const account = DEMO_ACCOUNTS[role]

  return (
    <header className="topbar">
      <button
        type="button"
        className="btn-icon topbar-menu"
        onClick={onMenu}
        aria-label="Open navigation"
      >
        <Icon name="menu" size={20} />
      </button>

      <div className="breadcrumb">
        <span className="crumb-role">{meta.crumb}</span>
        <span className="crumb-sep" aria-hidden="true">
          /
        </span>
        <span className="breadcrumb-current">{current?.label || 'Dashboard'}</span>
      </div>

      <div className="topbar-spacer" />

      <span
        className="demo-pill"
        title="Frontend prototype with sample data. The Spring Boot APIs are not connected yet."
      >
        <span className="dot" aria-hidden="true" />
        Demo data
      </span>

      <div className="topbar-user">
        <Avatar name={account.name} size="sm" tone={account.tone} />
        <div className="topbar-user-meta">
          <span className="topbar-user-name">{account.name}</span>
          <span className="topbar-user-role">{account.role}</span>
        </div>
      </div>
    </header>
  )
}
