import { ORDER_STATUS_TONE, VENDOR_STATUS_TONE } from '../../constants'

export default function Badge({
  tone = 'neutral',
  dot = false,
  status = false,
  children,
  className = '',
}) {
  const base = status ? 'badge badge-status' : 'badge'
  return (
    <span className={`${base} badge-${tone} ${className}`.trim()}>
      {dot ? <span className="badge-dot" aria-hidden="true" /> : null}
      {children}
    </span>
  )
}

export function StatusBadge({ status, kind = 'order' }) {
  const tones = kind === 'vendor' ? VENDOR_STATUS_TONE : ORDER_STATUS_TONE
  return (
    <Badge tone={tones[status] || 'neutral'} dot status>
      {status}
    </Badge>
  )
}
