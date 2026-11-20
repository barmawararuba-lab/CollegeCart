const currencyFormatter = new Intl.NumberFormat('en-IN', {
  style: 'currency',
  currency: 'INR',
  maximumFractionDigits: 0,
})

const numberFormatter = new Intl.NumberFormat('en-IN')

const dateFormatter = new Intl.DateTimeFormat('en-IN', {
  day: '2-digit',
  month: 'short',
  year: 'numeric',
})

const dateTimeFormatter = new Intl.DateTimeFormat('en-IN', {
  day: '2-digit',
  month: 'short',
  hour: 'numeric',
  minute: '2-digit',
})

const longDateFormatter = new Intl.DateTimeFormat('en-IN', {
  weekday: 'long',
  day: 'numeric',
  month: 'long',
})

export function formatCurrency(value) {
  return currencyFormatter.format(Number(value) || 0)
}

export function formatNumber(value) {
  return numberFormatter.format(Number(value) || 0)
}

export function formatDate(value) {
  return dateFormatter.format(new Date(value))
}

export function formatDateTime(value) {
  return dateTimeFormatter.format(new Date(value))
}

export function formatRelativeTime(value) {
  const timestamp = new Date(value).getTime()
  const diffMinutes = Math.round((Date.now() - timestamp) / 60000)

  if (diffMinutes < 1) return 'Just now'
  if (diffMinutes < 60) return `${diffMinutes} min ago`

  const diffHours = Math.round(diffMinutes / 60)
  if (diffHours < 24) return `${diffHours} hr ago`

  const diffDays = Math.round(diffHours / 24)
  if (diffDays === 1) return 'Yesterday'
  if (diffDays < 7) return `${diffDays} days ago`

  return formatDate(value)
}

export function formatTodayLong() {
  return longDateFormatter.format(new Date())
}

export function getTimeGreeting() {
  const hour = new Date().getHours()
  if (hour < 12) return 'Good morning'
  if (hour < 17) return 'Good afternoon'
  return 'Good evening'
}

export function getWaitMinutes(value) {
  const diffMinutes = Math.round((Date.now() - new Date(value).getTime()) / 60000)
  return Math.max(1, diffMinutes)
}

export function formatWaiting(value) {
  const minutes = getWaitMinutes(value)
  if (minutes < 60) return `${minutes} min`

  const hours = Math.floor(minutes / 60)
  const rest = minutes % 60
  return rest ? `${hours} hr ${rest} min` : `${hours} hr`
}

export function isWithinLastDays(value, days) {
  const cutoff = Date.now() - days * 24 * 60 * 60 * 1000
  return new Date(value).getTime() >= cutoff
}

export function getInitials(name = '') {
  return name
    .trim()
    .split(/\s+/)
    .slice(0, 2)
    .map((part) => part[0])
    .join('')
    .toUpperCase()
}
