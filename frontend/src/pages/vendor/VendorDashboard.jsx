import { useState } from 'react'
import Avatar from '../../components/ui/Avatar'
import { StatusBadge } from '../../components/ui/Badge'
import Button from '../../components/ui/Button'
import ConfirmDialog from '../../components/ui/ConfirmDialog'
import Icon from '../../components/Icon'
import StatCard from '../../components/ui/StatCard'
import {
  EmptyState,
  ErrorState,
  PageSkeleton,
} from '../../components/ui/StateViews'
import {
  NEXT_ORDER_STATUS,
  ORDER_ACTION_LABELS,
  ORDER_STATUS,
  STOCK_THRESHOLD,
} from '../../constants'
import { useAppData } from '../../context/useAppData'
import { useToast } from '../../context/useToast'
import {
  formatCurrency,
  formatRelativeTime,
  formatTodayLong,
  formatWaiting,
  getTimeGreeting,
  getWaitMinutes,
  isWithinLastDays,
} from '../../utils/format'
import OrderDetailsModal from './OrderDetailsModal'

// Placeholder for the signed-in vendor until authentication is wired up.
const SHOP = {
  name: 'Campus Corner Stationery',
  location: 'Student Centre, Shop 4',
  owner: 'Meera',
}

function plural(count, singular) {
  return count === 1 ? singular : `${singular}s`
}

export default function VendorDashboard({ navigate }) {
  const { products, orders, loading, error, reload, changeOrderStatus } =
    useAppData()
  const { toast } = useToast()

  const [detailsId, setDetailsId] = useState(null)
  const [rejectTarget, setRejectTarget] = useState(null)
  const [cancelTarget, setCancelTarget] = useState(null)
  const [busyId, setBusyId] = useState(null)

  if (loading) return <PageSkeleton />
  if (error) return <ErrorState message={error} onRetry={reload} />

  const pendingOrders = orders
    .filter((order) => order.status === ORDER_STATUS.PENDING)
    .sort((a, b) => new Date(a.placedAt) - new Date(b.placedAt))

  const acceptedCount = orders.filter(
    (order) => order.status === ORDER_STATUS.ACCEPTED,
  ).length
  const preparingCount = orders.filter(
    (order) => order.status === ORDER_STATUS.PREPARING,
  ).length
  const inProgressCount = acceptedCount + preparingCount

  const lowStock = products
    .filter((product) => product.stock <= STOCK_THRESHOLD)
    .sort((a, b) => a.stock - b.stock)
  const outOfStockCount = lowStock.filter((product) => product.stock === 0).length

  const weekOrders = orders.filter((order) =>
    isWithinLastDays(order.placedAt, 7),
  )
  const collectedThisWeek = weekOrders
    .filter((order) => order.status === ORDER_STATUS.COMPLETED)
    .reduce((sum, order) => sum + order.total, 0)

  const recentOrders = [...orders]
    .sort((a, b) => new Date(b.placedAt) - new Date(a.placedAt))
    .slice(0, 6)

  const activeOrder = detailsId
    ? orders.find((order) => order.id === detailsId) || null
    : null
  const pickupNote = pendingOrders[0]?.pickupNote ?? SHOP.location

  const attentionItems = [
    pendingOrders.length
      ? `${pendingOrders.length} ${plural(pendingOrders.length, 'order')} waiting for a response`
      : null,
    lowStock.length
      ? `${lowStock.length} ${plural(lowStock.length, 'product')} need restocking`
      : null,
  ].filter(Boolean)
  const summary = attentionItems.length
    ? `${attentionItems.join(' and ')}.`
    : 'No orders are waiting and stock levels are healthy.'

  async function updateStatus(order, nextStatus, successMessage) {
    setBusyId(order.id)
    try {
      await changeOrderStatus(order.id, nextStatus)
      toast.success(successMessage)
      return true
    } catch (updateError) {
      toast.error(
        updateError.message || 'Could not update this order. Please try again.',
      )
      return false
    } finally {
      setBusyId(null)
    }
  }

  async function handleAdvance(order, nextStatus) {
    await updateStatus(
      order,
      nextStatus,
      `Order ${order.id} moved to ${nextStatus.toLowerCase()}.`,
    )
  }

  async function handleReject() {
    if (!rejectTarget) return
    const success = await updateStatus(
      rejectTarget.order,
      ORDER_STATUS.CANCELLED,
      `Order ${rejectTarget.order.id} was rejected and cancelled.`,
    )
    if (success) setRejectTarget(null)
  }

  async function handleCancel() {
    if (!cancelTarget) return
    const success = await updateStatus(
      cancelTarget.order,
      ORDER_STATUS.CANCELLED,
      `Order ${cancelTarget.order.id} was cancelled.`,
    )
    if (success) setCancelTarget(null)
  }

  function openReject(order) {
    setDetailsId(null)
    setRejectTarget({ order })
  }

  function openCancel(order) {
    setDetailsId(null)
    setCancelTarget({ order })
  }

  return (
    <div className="page">
      <header className="dash-header">
        <div className="dash-heading">
          <p className="dash-eyebrow">
            {SHOP.name} · {SHOP.location}
          </p>
          <h1 className="page-title">
            {getTimeGreeting()}, {SHOP.owner}
          </h1>
          <p className="page-subtitle">{summary}</p>
          <p className="dash-meta">
            <span>{formatTodayLong()}</span>
            <span className="dash-sep" aria-hidden="true" />
            <span>
              <strong>{weekOrders.length}</strong> orders this week
            </span>
            <span className="dash-sep" aria-hidden="true" />
            <span>
              <strong>{formatCurrency(collectedThisWeek)}</strong> collected
            </span>
          </p>
        </div>
        <div className="page-actions">
          <Button
            variant="secondary"
            onClick={() => navigate('/vendor/products')}
          >
            Manage products
          </Button>
          <Button variant="primary" onClick={() => navigate('/vendor/orders')}>
            Order queue
          </Button>
        </div>
      </header>

      <section className="stat-grid stat-grid--3" aria-label="Store priorities">
        <StatCard
          label="Awaiting response"
          value={pendingOrders.length}
          hint={
            pendingOrders.length
              ? `Oldest waits ${formatWaiting(pendingOrders[0].placedAt)}`
              : 'Nothing to review'
          }
          hintTone={
            pendingOrders.length &&
            getWaitMinutes(pendingOrders[0].placedAt) >= 45
              ? 'warning'
              : undefined
          }
        />
        <StatCard
          label="In preparation"
          value={inProgressCount}
          hint={
            inProgressCount
              ? `${acceptedCount} accepted · ${preparingCount} preparing`
              : 'Nothing on the counter'
          }
        />
        <StatCard
          label="Low stock"
          value={lowStock.length}
          hint={
            outOfStockCount
              ? `${outOfStockCount} out of stock`
              : `At or below ${STOCK_THRESHOLD} units`
          }
          hintTone={outOfStockCount ? 'danger' : undefined}
        />
      </section>

      <div className="ops-grid">
        <section
          className="card pending-card"
          aria-label="Orders waiting for your response"
        >
          <div className="card-head">
            <div className="card-head-main">
              <h2 className="card-title">
                Needs your response
                {pendingOrders.length ? (
                  <span className="card-count">{pendingOrders.length}</span>
                ) : null}
              </h2>
              <p className="card-subtitle">
                Ordered oldest first, so the longest wait is handled first.
              </p>
            </div>
            <Button
              variant="ghost"
              size="sm"
              iconRight="arrowRight"
              onClick={() => navigate('/vendor/orders')}
            >
              Order queue
            </Button>
          </div>

          {pendingOrders.length ? (
            <ul className="pending-list">
              {pendingOrders.map((order) => {
                const isBusy = busyId === order.id
                const isSlow = getWaitMinutes(order.placedAt) >= 45

                return (
                  <li className="pending-row" key={order.id}>
                    <div className="pending-main">
                      <div className="pending-top">
                        <span className="order-id">{order.id}</span>
                        <span
                          className={`pending-wait ${isSlow ? 'is-slow' : ''}`.trim()}
                        >
                          Waiting {formatWaiting(order.placedAt)}
                        </span>
                        <span className="pending-total">
                          {formatCurrency(order.total)}
                        </span>
                      </div>
                      <div className="pending-customer">
                        <Avatar name={order.customer.name} size="sm" />
                        <span className="pending-name">
                          {order.customer.name}
                        </span>
                        <span className="pending-roll">
                          {order.customer.rollNo}
                        </span>
                      </div>
                      <ul className="pending-items">
                        {order.items.map((item) => (
                          <li key={item.name}>
                            <span className="pending-qty">{item.qty}×</span>
                            {item.name}
                          </li>
                        ))}
                      </ul>
                    </div>
                    <div className="pending-actions">
                      <button
                        type="button"
                        className="btn-icon"
                        title="View order details"
                        aria-label={`View order ${order.id}`}
                        onClick={() => setDetailsId(order.id)}
                      >
                        <Icon name="eye" size={16} />
                      </button>
                      <Button
                        size="sm"
                        variant="ghost"
                        disabled={isBusy}
                        onClick={() => openReject(order)}
                      >
                        Reject
                      </Button>
                      <Button
                        size="sm"
                        variant="primary"
                        icon="check"
                        disabled={isBusy}
                        onClick={() =>
                          handleAdvance(order, ORDER_STATUS.ACCEPTED)
                        }
                      >
                        {isBusy ? 'Accepting…' : 'Accept'}
                      </Button>
                    </div>
                  </li>
                )
              })}
            </ul>
          ) : (
            <EmptyState
              compact
              title="You're all caught up"
              message="New orders will appear here as soon as students check out."
            />
          )}

          {pendingOrders.length ? (
            <div className="card-foot">
              <span>Pickup point: {pickupNote}</span>
            </div>
          ) : null}
        </section>

        <section className="card" aria-label="Low stock products">
          <div className="card-head">
            <div className="card-head-main">
              <h2 className="card-title">Stock alerts</h2>
              <p className="card-subtitle">
                Products at or below {STOCK_THRESHOLD} units.
              </p>
            </div>
            <Button
              variant="ghost"
              size="sm"
              iconRight="arrowRight"
              onClick={() => navigate('/vendor/products')}
            >
              Manage stock
            </Button>
          </div>

          {lowStock.length ? (
            <ul className="stock-rows">
              {lowStock.map((product) => {
                const isOut = product.stock === 0

                return (
                  <li className="stock-row" key={product.id}>
                    <div className="stock-row-head">
                      <div className="stock-main">
                        <span className="stock-name">{product.name}</span>
                        <span className="stock-category">
                          {product.category}
                        </span>
                      </div>
                      <span
                        className={`stock-qty ${isOut ? 'is-out' : ''}`.trim()}
                      >
                        {product.stock}
                        <small>{isOut ? 'out of stock' : 'left'}</small>
                      </span>
                    </div>
                  </li>
                )
              })}
            </ul>
          ) : (
            <EmptyState
              compact
              title="Stock levels look healthy"
              message={`Every product has more than ${STOCK_THRESHOLD} units available.`}
            />
          )}
        </section>
      </div>

      <section className="card" aria-label="Recent orders">
        <div className="card-head">
          <div className="card-head-main">
            <h2 className="card-title">Recent orders</h2>
            <p className="card-subtitle">
              The latest orders across the counter, newest first.
            </p>
          </div>
          <Button
            variant="ghost"
            size="sm"
            iconRight="arrowRight"
            onClick={() => navigate('/vendor/orders')}
          >
            View all orders
          </Button>
        </div>

        {recentOrders.length ? (
          <div className="table-wrap">
            <table className="table table-stacked table-compact">
              <thead>
                <tr>
                  <th>Order</th>
                  <th>Student</th>
                  <th>Items</th>
                  <th>Total</th>
                  <th>Status</th>
                  <th className="align-right">Action</th>
                </tr>
              </thead>
              <tbody>
                {recentOrders.map((order) => {
                  const totalQty = order.items.reduce(
                    (sum, item) => sum + item.qty,
                    0,
                  )
                  const nextStatus = NEXT_ORDER_STATUS[order.status]
                  const isBusy = busyId === order.id

                  return (
                    <tr key={order.id}>
                      <td data-label="Order">
                        <div className="cell-main order-id">{order.id}</div>
                        <div className="cell-sub">
                          {formatRelativeTime(order.placedAt)}
                        </div>
                      </td>
                      <td data-label="Student">
                        <div className="customer-cell">
                          <Avatar name={order.customer.name} size="sm" />
                          <div>
                            <div className="cell-main">
                              {order.customer.name}
                            </div>
                            <div className="cell-sub">
                              {order.customer.rollNo}
                            </div>
                          </div>
                        </div>
                      </td>
                      <td data-label="Items">
                        <div className="cell-main">{order.items[0].name}</div>
                        <div className="cell-sub">
                          {totalQty} {plural(totalQty, 'item')}
                          {order.items.length > 1
                            ? ` · +${order.items.length - 1} more`
                            : ''}
                        </div>
                      </td>
                      <td data-label="Total" className="num">
                        {formatCurrency(order.total)}
                      </td>
                      <td data-label="Status">
                        <StatusBadge status={order.status} />
                      </td>
                      <td className="table-actions">
                        <div className="cell-actions">
                          <button
                            type="button"
                            className="btn-icon"
                            title="View order details"
                            aria-label={`View order ${order.id}`}
                            onClick={() => setDetailsId(order.id)}
                          >
                            <Icon name="eye" size={16} />
                          </button>

                          {order.status === ORDER_STATUS.PENDING ? (
                            <Button
                              size="sm"
                              variant="secondary"
                              onClick={() => setDetailsId(order.id)}
                            >
                              Review
                            </Button>
                          ) : nextStatus ? (
                            <Button
                              size="sm"
                              variant="secondary"
                              disabled={isBusy}
                              onClick={() => handleAdvance(order, nextStatus)}
                            >
                              {isBusy
                                ? 'Updating…'
                                : ORDER_ACTION_LABELS[nextStatus]}
                            </Button>
                          ) : null}
                        </div>
                      </td>
                    </tr>
                  )
                })}
              </tbody>
            </table>
          </div>
        ) : (
          <EmptyState
            title="No orders yet"
            message="Orders from students will show up in this table."
          />
        )}

        {recentOrders.length ? (
          <div className="card-foot">
            <span>
              Latest {recentOrders.length} of {orders.length} orders
            </span>
            <span className="foot-hint">Pending orders also appear above.</span>
          </div>
        ) : null}
      </section>

      <OrderDetailsModal
        order={activeOrder}
        busy={busyId === activeOrder?.id}
        onClose={() => setDetailsId(null)}
        onAdvance={handleAdvance}
        onReject={openReject}
        onCancel={openCancel}
      />

      <ConfirmDialog
        open={Boolean(rejectTarget)}
        title={`Reject order ${rejectTarget?.order.id ?? ''}?`}
        message={
          <>
            This will cancel the order for{' '}
            <strong>{rejectTarget?.order.customer.name}</strong>. Rejected orders
            cannot be restored, and the student will need to order again.
          </>
        }
        confirmLabel="Reject order"
        tone="danger"
        busy={busyId === rejectTarget?.order.id}
        onConfirm={handleReject}
        onClose={() => {
          if (!busyId) setRejectTarget(null)
        }}
      />

      <ConfirmDialog
        open={Boolean(cancelTarget)}
        title={`Cancel order ${cancelTarget?.order.id ?? ''}?`}
        message={
          <>
            Cancel this order for{' '}
            <strong>{cancelTarget?.order.customer.name}</strong>? The student
            will see the order as cancelled and will not be able to pick it up.
          </>
        }
        confirmLabel="Cancel order"
        tone="danger"
        busy={busyId === cancelTarget?.order.id}
        onConfirm={handleCancel}
        onClose={() => {
          if (!busyId) setCancelTarget(null)
        }}
      />
    </div>
  )
}
