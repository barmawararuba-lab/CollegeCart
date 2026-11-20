import Avatar from '../../components/ui/Avatar'
import { StatusBadge } from '../../components/ui/Badge'
import Button from '../../components/ui/Button'
import Modal from '../../components/ui/Modal'
import {
  CANCELLABLE_STATUSES,
  NEXT_ORDER_STATUS,
  ORDER_ACTION_LABELS,
  ORDER_STATUS,
} from '../../constants'
import {
  formatCurrency,
  formatDateTime,
  formatRelativeTime,
} from '../../utils/format'

export default function OrderDetailsModal({
  order,
  busy,
  onClose,
  onAdvance,
  onReject,
  onCancel,
}) {
  if (!order) return null

  const nextStatus = NEXT_ORDER_STATUS[order.status]
  const canCancel = CANCELLABLE_STATUSES.includes(order.status)

  return (
    <Modal
      open
      onClose={onClose}
      closeDisabled={busy}
      size="lg"
      title={`Order ${order.id}`}
      subtitle={`Placed ${formatDateTime(order.placedAt)}`}
      footer={
        <>
          {order.status === ORDER_STATUS.PENDING ? (
            <Button
              variant="ghost"
              onClick={() => onReject(order)}
              disabled={busy}
            >
              Reject order
            </Button>
          ) : null}
          {canCancel && order.status !== ORDER_STATUS.PENDING ? (
            <Button
              variant="ghost"
              onClick={() => onCancel(order)}
              disabled={busy}
            >
              Cancel order
            </Button>
          ) : null}

          <Button variant="secondary" onClick={onClose} disabled={busy}>
            Close
          </Button>

          {order.status === ORDER_STATUS.PENDING ? (
            <Button
              variant="primary"
              icon="check"
              disabled={busy}
              onClick={() => onAdvance(order, ORDER_STATUS.ACCEPTED)}
            >
              {busy ? 'Updating…' : 'Accept order'}
            </Button>
          ) : nextStatus ? (
            <Button
              variant="primary"
              icon={nextStatus === ORDER_STATUS.COMPLETED ? 'check' : 'arrowRight'}
              disabled={busy}
              onClick={() => onAdvance(order, nextStatus)}
            >
              {busy ? 'Updating…' : ORDER_ACTION_LABELS[nextStatus]}
            </Button>
          ) : null}
        </>
      }
    >
      <div className="order-detail-status">
        <StatusBadge status={order.status} />
        <time dateTime={order.placedAt}>
          Updated {formatRelativeTime(order.history[order.history.length - 1].at)}
        </time>
      </div>

      <div className="detail-grid">
        <div className="detail-block">
          <h3 className="detail-block-title">Customer</h3>
          <div className="customer-row">
            <Avatar name={order.customer.name} />
            <div>
              <div className="customer-name">{order.customer.name}</div>
              <div className="customer-meta">{order.customer.rollNo}</div>
            </div>
          </div>
          <div className="detail-line">
            <span>{order.customer.phone}</span>
          </div>
          <div className="detail-line">
            <span>{order.customer.email}</span>
          </div>
        </div>

        <div className="detail-block">
          <h3 className="detail-block-title">Pickup &amp; payment</h3>
          <div className="detail-line">
            <span>{order.pickupNote}</span>
          </div>
          <div className="detail-line">
            <span>{order.paymentMethod}</span>
          </div>
          <div className="detail-line">
            <span>Placed {formatRelativeTime(order.placedAt)}</span>
          </div>
        </div>
      </div>

      <h3 className="section-label">Ordered items</h3>
      <div className="order-items">
        {order.items.map((item) => (
          <div className="order-item-row" key={item.name}>
            <span className="order-item-name">{item.name}</span>
            <span className="order-item-qty">× {item.qty}</span>
            <span className="order-item-total">
              {formatCurrency(item.price * item.qty)}
            </span>
          </div>
        ))}
      </div>
      <div className="order-total-row">
        <span>Total payable at pickup</span>
        <strong>{formatCurrency(order.total)}</strong>
      </div>

      <h3 className="section-label">Status timeline</h3>
      <div className="timeline">
        {order.history.map((entry, index) => (
          <div
            className={`timeline-item ${index === order.history.length - 1 ? 'is-current' : ''}`.trim()}
            key={`${entry.status}-${entry.at}`}
          >
            <span className="timeline-dot" aria-hidden="true" />
            <div>
              <div className="timeline-status">{entry.status}</div>
              <div className="timeline-time">{formatDateTime(entry.at)}</div>
            </div>
          </div>
        ))}
      </div>
    </Modal>
  )
}
