export const STOCK_THRESHOLD = 10

export const PRODUCT_CATEGORIES = [
  'Stationery',
  'Books',
  'Electronics',
  'Snacks',
  'Accessories',
]

export const ORDER_STATUS = {
  PENDING: 'Pending',
  ACCEPTED: 'Accepted',
  PREPARING: 'Preparing',
  READY: 'Ready for Pickup',
  COMPLETED: 'Completed',
  CANCELLED: 'Cancelled',
}

export const ORDER_STATUS_LIST = Object.values(ORDER_STATUS)

export const NEXT_ORDER_STATUS = {
  [ORDER_STATUS.PENDING]: ORDER_STATUS.ACCEPTED,
  [ORDER_STATUS.ACCEPTED]: ORDER_STATUS.PREPARING,
  [ORDER_STATUS.PREPARING]: ORDER_STATUS.READY,
  [ORDER_STATUS.READY]: ORDER_STATUS.COMPLETED,
}

export const ORDER_ACTION_LABELS = {
  [ORDER_STATUS.ACCEPTED]: 'Accept',
  [ORDER_STATUS.PREPARING]: 'Start preparing',
  [ORDER_STATUS.READY]: 'Mark ready',
  [ORDER_STATUS.COMPLETED]: 'Complete',
}

export const CANCELLABLE_STATUSES = [
  ORDER_STATUS.PENDING,
  ORDER_STATUS.ACCEPTED,
  ORDER_STATUS.PREPARING,
]

export const ORDER_STATUS_TONE = {
  [ORDER_STATUS.PENDING]: 'warning',
  [ORDER_STATUS.ACCEPTED]: 'info',
  [ORDER_STATUS.PREPARING]: 'violet',
  [ORDER_STATUS.READY]: 'teal',
  [ORDER_STATUS.COMPLETED]: 'success',
  [ORDER_STATUS.CANCELLED]: 'danger',
}

export const ORDER_STATUS_COLORS = {
  [ORDER_STATUS.PENDING]: '#a2711f',
  [ORDER_STATUS.ACCEPTED]: '#35648f',
  [ORDER_STATUS.PREPARING]: '#6a5a8b',
  [ORDER_STATUS.READY]: '#3b7d7a',
  [ORDER_STATUS.COMPLETED]: '#2e7d5b',
  [ORDER_STATUS.CANCELLED]: '#ae4a34',
}

export const VENDOR_STATUS_TONE = {
  Pending: 'warning',
  Approved: 'success',
  Rejected: 'danger',
}
