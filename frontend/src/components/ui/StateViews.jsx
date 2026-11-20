import Icon from '../Icon'
import Button from './Button'

export function EmptyState({
  title,
  message,
  action,
  compact = false,
}) {
  return (
    <div className={`state-card ${compact ? 'is-compact' : ''}`.trim()}>
      <h3 className="state-title">{title}</h3>
      {message ? <p className="state-text">{message}</p> : null}
      {action ? <div className="state-actions">{action}</div> : null}
    </div>
  )
}

export function ErrorState({
  title = "Couldn't load your workspace",
  message = 'Please check that the backend is running, then try again.',
  onRetry,
}) {
  return (
    <div className="state-card state-card--error">
      <span className="state-icon">
        <Icon name="alert" size={22} />
      </span>
      <h2 className="state-title">{title}</h2>
      <p className="state-text">{message}</p>
      {onRetry ? (
        <div className="state-actions">
          <Button variant="primary" icon="refresh" onClick={onRetry}>
            Try again
          </Button>
        </div>
      ) : null}
    </div>
  )
}

export function PageSkeleton() {
  return (
    <div className="page-skeleton" aria-busy="true" aria-label="Loading content">
      <div className="skeleton skeleton-title" />
      <div className="skeleton skeleton-subtitle" />
      <div className="stat-grid">
        {[0, 1, 2, 3, 4].map((item) => (
          <div className="skeleton skeleton-stat" key={item} />
        ))}
      </div>
      <div className="dashboard-grid">
        <div className="skeleton skeleton-block" />
        <div className="skeleton skeleton-block" />
      </div>
    </div>
  )
}
