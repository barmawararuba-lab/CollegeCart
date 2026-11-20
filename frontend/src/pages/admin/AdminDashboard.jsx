import Avatar from '../../components/ui/Avatar'
import Badge from '../../components/ui/Badge'
import Button from '../../components/ui/Button'
import MiniBarChart from '../../components/ui/MiniBarChart'
import PageHeader from '../../components/ui/PageHeader'
import StatCard from '../../components/ui/StatCard'
import { EmptyState, ErrorState, PageSkeleton } from '../../components/ui/StateViews'
import Icon from '../../components/Icon'
import { useAppData } from '../../context/useAppData'
import {
  formatNumber,
  formatRelativeTime,
} from '../../utils/format'

const ACTIVITY_STYLES = {
  vendor: { icon: 'store' },
  order: { icon: 'bag' },
  stock: { icon: 'alert' },
  student: { icon: 'users' },
}

const STATUS_TONES = {
  Pending: 'amber',
  Approved: 'teal',
  Rejected: 'rose',
}

export default function AdminDashboard({ navigate }) {
  const { vendors, platform, activities, loading, error, reload } = useAppData()

  if (loading) return <PageSkeleton />
  if (error) return <ErrorState message={error} onRetry={reload} />

  const pendingVendors = vendors.filter((vendor) => vendor.status === 'Pending')
  const approvedVendors = vendors.filter(
    (vendor) => vendor.status === 'Approved',
  ).length
  const rejectedVendors = vendors.filter(
    (vendor) => vendor.status === 'Rejected',
  ).length
  const reviewedCount = approvedVendors + rejectedVendors
  const approvalRate = reviewedCount
    ? Math.round((approvedVendors / reviewedCount) * 100)
    : 0
  const signupTotal = (platform?.signups || []).reduce(
    (sum, day) => sum + day.count,
    0,
  )
  const outcomeRows = [
    { label: 'Approved', count: approvedVendors, color: '#2e7d5b' },
    { label: 'Rejected', count: rejectedVendors, color: '#ae4a34' },
    { label: 'Pending', count: pendingVendors.length, color: '#a2711f' },
  ]

  return (
    <div className="page">
      <PageHeader
        title="Platform overview"
        subtitle="Monitor student activity, vendor access and marketplace health."
        actions={
          <Button
            variant="primary"
            icon="userCheck"
            onClick={() => navigate('/admin/vendors')}
          >
            Review applications
            {pendingVendors.length ? ` (${pendingVendors.length})` : ''}
          </Button>
        }
      />

      <section className="stat-grid" aria-label="Platform summary">
        <StatCard
          label="Registered students"
          value={formatNumber(platform?.students ?? 0)}
          hint={`+${signupTotal} sign-ups this week`}
        />
        <StatCard
          label="Registered vendors"
          value={platform?.activeVendors ?? 0}
          hint={`${approvedVendors} approved in this sample`}
        />
        <StatCard
          label="Pending applications"
          value={pendingVendors.length}
          hint={
            pendingVendors.length
              ? 'Waiting for admin review'
              : 'All applications reviewed'
          }
          hintTone={pendingVendors.length ? 'warning' : undefined}
        />
        <StatCard
          label="Products listed"
          value={formatNumber(platform?.products ?? 0)}
          hint="Across all vendor catalogs"
        />
        <StatCard
          label="Total orders"
          value={formatNumber(platform?.orders ?? 0)}
          hint="Platform lifetime orders"
        />
      </section>

      <div className="dashboard-grid">
        <section className="card" aria-label="Vendor applications awaiting review">
          <div className="card-head">
            <div className="card-head-main">
              <h2 className="card-title">Vendor applications</h2>
              <p className="card-subtitle">
                {pendingVendors.length} application
                {pendingVendors.length === 1 ? '' : 's'} awaiting review
              </p>
            </div>
            <Button
              variant="ghost"
              size="sm"
              iconRight="arrowRight"
              onClick={() => navigate('/admin/vendors')}
            >
              View all
            </Button>
          </div>

          {pendingVendors.length ? (
            <div className="approval-list">
              {pendingVendors.slice(0, 4).map((vendor) => (
                <div className="approval-row" key={vendor.id}>
                  <Avatar
                    name={vendor.ownerName}
                    tone={STATUS_TONES[vendor.status] || 'indigo'}
                  />
                  <div className="approval-main">
                    <div className="approval-shop">{vendor.shopName}</div>
                    <div className="approval-meta">
                      <span>{vendor.ownerName}</span>
                      <span>{vendor.category}</span>
                      <span>{formatRelativeTime(vendor.submittedAt)}</span>
                    </div>
                  </div>
                  <Button
                    size="sm"
                    variant="secondary"
                    className="approval-action"
                    onClick={() => navigate('/admin/vendors')}
                  >
                    Review
                  </Button>
                </div>
              ))}
            </div>
          ) : (
            <EmptyState
              compact
              title="All caught up"
              message="No vendor applications are waiting for review."
            />
          )}

          <div className="card-foot">
            <span>Approving a vendor grants storefront access.</span>
            <Button
              variant="ghost"
              size="sm"
              iconRight="arrowRight"
              onClick={() => navigate('/admin/vendors')}
            >
              Open approvals
            </Button>
          </div>
        </section>

        <section className="card" aria-label="Recent platform activity">
          <div className="card-head">
            <div className="card-head-main">
              <h2 className="card-title">Recent platform activity</h2>
              <p className="card-subtitle">Latest events across CollegeCart</p>
            </div>
            <Badge tone="neutral">{activities.length} events</Badge>
          </div>

          {activities.length ? (
            <div className="activity-list">
              {activities.slice(0, 6).map((activity) => {
                const style = ACTIVITY_STYLES[activity.type] || {
                  icon: 'info',
                }
                return (
                  <div className="activity-item" key={activity.id}>
                    <span className="activity-icon">
                      <Icon name={style.icon} size={15} />
                    </span>
                    <div className="activity-body">
                      <div className="activity-title">{activity.title}</div>
                      <div className="activity-time">
                        {formatRelativeTime(activity.createdAt)}
                      </div>
                    </div>
                  </div>
                )
              })}
            </div>
          ) : (
            <EmptyState
              compact
              title="No activity yet"
              message="Platform events will appear here as vendors and students interact."
            />
          )}
        </section>
      </div>

      <div className="dashboard-grid equal">
        <section className="card" aria-label="New student sign-ups">
          <div className="card-head">
            <div className="card-head-main">
              <h2 className="card-title">New student sign-ups</h2>
              <p className="card-subtitle">Accounts created over the last 7 days</p>
            </div>
            <Badge tone="success">+{signupTotal} this week</Badge>
          </div>
          <div className="chart-pad">
            <MiniBarChart
              data={platform?.signups || []}
              valueKey="count"
              labelKey="day"
              formatValue={(value) => `${value} students`}
              ariaLabel="New student sign-ups over the last seven days"
            />
          </div>
          <p className="chart-footnote">
            Sign-ups are synced from the student registrar database.
          </p>
        </section>

        <section className="card" aria-label="Application outcomes">
          <div className="card-head">
            <div className="card-head-main">
              <h2 className="card-title">Application outcomes</h2>
              <p className="card-subtitle">
                Review results for applications in this workspace
              </p>
            </div>
            <Badge tone="primary">{approvalRate}% approved</Badge>
          </div>

          <div className="snap-list">
            {outcomeRows.map((row) => (
              <div className="snap-row" key={row.label}>
                <span className="snap-label">{row.label}</span>
                <div className="snap-track">
                  <div
                    className="snap-fill"
                    style={{
                      width: vendors.length
                        ? `${(row.count / vendors.length) * 100}%`
                        : '0%',
                      background: row.color,
                    }}
                  />
                </div>
                <span className="snap-count">{row.count}</span>
              </div>
            ))}
          </div>
          <p className="snap-note">
            {reviewedCount
              ? `${reviewedCount} applications reviewed out of ${vendors.length} in this sample.`
              : 'No applications have been reviewed yet.'}
          </p>
        </section>
      </div>
    </div>
  )
}
