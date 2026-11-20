import { useMemo, useState } from 'react'
import Avatar from '../../components/ui/Avatar'
import { StatusBadge } from '../../components/ui/Badge'
import Button from '../../components/ui/Button'
import ConfirmDialog from '../../components/ui/ConfirmDialog'
import PageHeader from '../../components/ui/PageHeader'
import SearchInput from '../../components/ui/SearchInput'
import { EmptyState, ErrorState, PageSkeleton } from '../../components/ui/StateViews'
import Icon from '../../components/Icon'
import {
  NEXT_ORDER_STATUS,
  ORDER_ACTION_LABELS,
  ORDER_STATUS,
  ORDER_STATUS_LIST,
} from '../../constants'
import { useAppData } from '../../context/useAppData'
import { useToast } from '../../context/useToast'
import { formatCurrency, formatRelativeTime } from '../../utils/format'
import OrderDetailsModal from './OrderDetailsModal'

const FILTER_OPTIONS = ['All', ...ORDER_STATUS_LIST]

export default function OrderManagement() {
  const { orders, loading, error, reload, changeOrderStatus } = useAppData()
  const { toast } = useToast()

  const [statusFilter, setStatusFilter] = useState('All')
  const [search, setSearch] = useState('')
  const [detailsId, setDetailsId] = useState(null)
  const [rejectTarget, setRejectTarget] = useState(null)
  const [cancelTarget, setCancelTarget] = useState(null)
  const [busyId, setBusyId] = useState(null)

  const counts = useMemo(() => {
    const result = { All: orders.length }
    ORDER_STATUS_LIST.forEach((status) => {
      result[status] = orders.filter((order) => order.status === status).length
    })
    return result
  }, [orders])

  const filteredOrders = useMemo(() => {
    const term = search.trim().toLowerCase()

    return orders
      .filter((order) => {
        const matchesStatus =
          statusFilter === 'All' || order.status === statusFilter
        const matchesTerm =
          !term ||
          order.id.toLowerCase().includes(term) ||
          order.customer.name.toLowerCase().includes(term) ||
          order.customer.rollNo.toLowerCase().includes(term)

        return matchesStatus && matchesTerm
      })
      .sort((a, b) => new Date(b.placedAt) - new Date(a.placedAt))
  }, [orders, statusFilter, search])

  const activeOrder = detailsId
    ? orders.find((order) => order.id === detailsId) || null
    : null

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

  if (loading) return <PageSkeleton />
  if (error) return <ErrorState message={error} onRetry={reload} />

  return (
    <div className="page">
      <PageHeader
        title="Orders"
        subtitle="Accept incoming orders, keep students updated and track pickups."
        actions={
          <Button variant="secondary" icon="refresh" onClick={reload}>
            Refresh
          </Button>
        }
      />

      <div className="card">
        <div
          className="filter-tabs"
          role="group"
          aria-label="Filter orders by status"
        >
          {FILTER_OPTIONS.map((option) => (
            <button
              key={option}
              type="button"
              className={`filter-tab ${statusFilter === option ? 'is-active' : ''}`.trim()}
              aria-pressed={statusFilter === option}
              onClick={() => setStatusFilter(option)}
            >
              {option}
              <span className="count">{counts[option] ?? 0}</span>
            </button>
          ))}
        </div>

        <div className="toolbar">
          <SearchInput
            value={search}
            onChange={setSearch}
            placeholder="Search by order ID, student or roll number…"
            ariaLabel="Search orders by order ID, student name or roll number"
          />
          <span className="toolbar-meta">
            Showing <strong>{filteredOrders.length}</strong> of {orders.length}{' '}
            orders
          </span>
        </div>

        {filteredOrders.length ? (
          <div className="table-wrap">
            <table className="table table-stacked">
              <thead>
                <tr>
                  <th>Order</th>
                  <th>Customer</th>
                  <th>Items</th>
                  <th>Total</th>
                  <th>Status</th>
                  <th className="align-right">Actions</th>
                </tr>
              </thead>
              <tbody>
                {filteredOrders.map((order) => {
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
                      <td data-label="Customer">
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
                          {totalQty} item{totalQty === 1 ? '' : 's'}
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
                            <>
                              <Button
                                size="sm"
                                variant="ghost"
                                onClick={() => openReject(order)}
                                disabled={isBusy}
                              >
                                Reject
                              </Button>
                              <Button
                                size="sm"
                                variant="primary"
                                icon="check"
                                onClick={() =>
                                  handleAdvance(order, ORDER_STATUS.ACCEPTED)
                                }
                                disabled={isBusy}
                              >
                                Accept
                              </Button>
                            </>
                          ) : nextStatus ? (
                            <Button
                              size="sm"
                              variant="secondary"
                              onClick={() => handleAdvance(order, nextStatus)}
                              disabled={isBusy}
                            >
                              {isBusy
                                ? 'Updating…'
                                : ORDER_ACTION_LABELS[nextStatus]}
                            </Button>
                          ) : (
                            <span className="foot-hint">No action needed</span>
                          )}
                        </div>
                      </td>
                    </tr>
                  )
                })}
              </tbody>
            </table>
          </div>
        ) : orders.length === 0 ? (
          <EmptyState
            title="No orders yet"
            message="When students place orders, they will appear here for you to accept and prepare."
          />
        ) : (
          <EmptyState
            title="No orders match this view"
            message="Try another status tab or search for a different student or order ID."
            action={
              <Button
                variant="secondary"
                onClick={() => {
                  setStatusFilter('All')
                  setSearch('')
                }}
              >
                Reset filters
              </Button>
            }
          />
        )}

        {filteredOrders.length ? (
          <div className="card-foot">
            <span>
              {filteredOrders.length} order
              {filteredOrders.length === 1 ? '' : 's'} shown
            </span>
            <span className="foot-hint">
              Pending orders can be accepted or rejected. Accepted orders follow
              the pickup flow.
            </span>
          </div>
        ) : null}
      </div>

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
