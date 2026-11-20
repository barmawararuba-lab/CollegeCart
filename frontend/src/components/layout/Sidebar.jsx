import Icon from '../Icon'
import Avatar from '../ui/Avatar'
import {
  DEMO_ACCOUNTS,
  NAV_ITEMS,
  ROLE_META,
} from '../../config/navigation'

export default function Sidebar({ role, path, open, onClose, badges = {} }) {
  const meta = ROLE_META[role]
  const account = DEMO_ACCOUNTS[role]

  return (
    <aside
      className={`sidebar ${open ? 'is-open' : ''}`.trim()}
      aria-label={`${meta.crumb} navigation`}
    >
      <div className="sidebar-brand">
        <span className="brand-mark">
          <Icon name="bag" size={20} />
        </span>
        <div className="brand-text">
          <span className="brand-name">CollegeCart</span>
          <span className="brand-sub">Campus marketplace</span>
        </div>
      </div>

      <div className="sidebar-role-wrap">
        <span className="sidebar-role">
          <span className="role-dot" aria-hidden="true" />
          {meta.label}
        </span>
      </div>

      <nav className="sidebar-nav" aria-label={`${meta.crumb} menu`}>
        <span className="nav-label">Menu</span>
        {NAV_ITEMS[role].map((item) => (
          <a
            key={item.path}
            href={`#${item.path}`}
            className={`nav-link ${path === item.path ? 'is-active' : ''}`.trim()}
            aria-current={path === item.path ? 'page' : undefined}
            onClick={onClose}
            title={item.description}
          >
            <Icon name={item.icon} size={18} />
            <span>{item.label}</span>
            {badges[item.path] ? (
              <span className="nav-count">{badges[item.path]}</span>
            ) : null}
          </a>
        ))}
      </nav>

      <div className="sidebar-footer">
        <div className="account-card">
          <Avatar name={account.name} size="sm" tone={account.tone} />
          <div className="account-meta">
            <span className="account-name">{account.name}</span>
            <span className="account-role">{account.role}</span>
          </div>
        </div>
        <a
          className="sidebar-switch"
          href={`#${meta.switchPath}`}
          onClick={onClose}
        >
          <Icon name="refresh" size={14} />
          {meta.switchLabel}
        </a>
      </div>
    </aside>
  )
}
