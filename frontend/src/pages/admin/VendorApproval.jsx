import { useMemo, useState } from 'react'
import Avatar from '../../components/ui/Avatar'
import Badge, { StatusBadge } from '../../components/ui/Badge'
import Button from '../../components/ui/Button'
import ConfirmDialog from '../../components/ui/ConfirmDialog'
import PageHeader from '../../components/ui/PageHeader'
import SearchInput from '../../components/ui/SearchInput'
import { EmptyState, ErrorState, PageSkeleton } from '../../components/ui/StateViews'
import Icon from '../../components/Icon'
import { useAppData } from '../../context/useAppData'
import { useToast } from '../../context/useToast'
import { formatRelativeTime } from '../../utils/format'
import RejectVendorDialog from './RejectVendorDialog'
import VendorDetailsModal from './VendorDetailsModal'

const FILTERS = ['Pending', 'Approved', 'Rejected', 'All']

const AVATAR_TONES = {
  Pending: 'amber',
  Approved: 'teal',
  Rejected: 'rose',
}

export default function VendorApproval() {
  const { vendors, loading, error, reload, reviewVendor } = useAppData()
  const { toast } = useToast()

  const [statusFilter, setStatusFilter] = useState('Pending')
  const [search, setSearch] = useState('')
  const [detailsId, setDetailsId] = useState(null)
  const [approveTarget, setApproveTarget] = useState(null)
  const [rejectTarget, setRejectTarget] = useState(null)
  const [busy, setBusy] = useState(false)

  const counts = useMemo(() => {
    const result = { All: vendors.length }
    ;['Pending', 'Approved', 'Rejected'].forEach((status) => {
      result[status] = vendors.filter(
        (vendor) => vendor.status === status,
      ).length
    })
    return result
  }, [vendors])

  const filteredVendors = useMemo(() => {
    const term = search.trim().toLowerCase()

    return vendors.filter((vendor) => {
      const matchesStatus =
        statusFilter === 'All' || vendor.status === statusFilter
      const matchesTerm =
        !term ||
        vendor.shopName.toLowerCase().includes(term) ||
        vendor.ownerName.toLowerCase().includes(term) ||
        vendor.email.toLowerCase().includes(term) ||
        vendor.category.toLowerCase().includes(term)

      return matchesStatus && matchesTerm
    })
  }, [vendors, statusFilter, search])

  const activeVendor = detailsId
    ? vendors.find((vendor) => vendor.id === detailsId) || null
    : null

  async function handleApprove() {
    if (!approveTarget) return
    setBusy(true)
    try {
      await reviewVendor(approveTarget.id, 'Approved')
      toast.success(`${approveTarget.shopName} is now an approved vendor.`)
      setApproveTarget(null)
      setDetailsId(null)
    } catch (approveError) {
      toast.error(
        approveError.message || 'Could not approve this application.',
      )
    } finally {
      setBusy(false)
    }
  }

  async function handleReject(reason) {
    if (!rejectTarget) return
    setBusy(true)
    try {
      await reviewVendor(rejectTarget.id, 'Rejected', reason)
      toast.success(
        `${rejectTarget.shopName} was rejected. The applicant can see your reason.`,
      )
      setRejectTarget(null)
      setDetailsId(null)
    } catch (rejectError) {
      toast.error(rejectError.message || 'Could not reject this application.')
    } finally {
      setBusy(false)
    }
  }

  if (loading) return <PageSkeleton />
  if (error) return <ErrorState message={error} onRetry={reload} />

  return (
    <div className="page">
      <PageHeader
        title="Vendor approvals"
        subtitle="Review campus store applications before granting vendor access."
      />

      <div className="info-banner">
        <span>
          <strong>Review before you approve.</strong> Check the shop name,
          student ID and campus address. Approving gives the applicant access to
          the vendor workspace and storefront.
        </span>
      </div>

      <div className="card">
        <div
          className="filter-tabs"
          role="group"
          aria-label="Filter applications by status"
        >
          {FILTERS.map((option) => (
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
            placeholder="Search by shop, owner, email or category…"
            ariaLabel="Search vendor applications"
          />
          <span className="toolbar-meta">
            Showing <strong>{filteredVendors.length}</strong> of {vendors.length}{' '}
            applications
          </span>
        </div>

        {filteredVendors.length ? (
          <div className="table-wrap">
            <table className="table table-stacked">
              <thead>
                <tr>
                  <th>Vendor</th>
                  <th>Category</th>
                  <th>Submitted</th>
                  <th>Status</th>
                  <th className="align-right">Actions</th>
                </tr>
              </thead>
              <tbody>
                {filteredVendors.map((vendor) => (
                  <tr key={vendor.id}>
                    <td data-label="Vendor">
                      <div className="identity-cell">
                        <Avatar
                          name={vendor.ownerName}
                          tone={AVATAR_TONES[vendor.status] || 'indigo'}
                        />
                        <div className="identity-text">
                          <div className="cell-main">{vendor.shopName}</div>
                          <div className="cell-sub">
                            {vendor.ownerName} · {vendor.ownerRollNo}
                          </div>
                        </div>
                      </div>
                    </td>
                    <td data-label="Category">
                      <Badge tone="neutral">{vendor.category}</Badge>
                    </td>
                    <td data-label="Submitted">
                      <div className="cell-main">
                        {formatRelativeTime(vendor.submittedAt)}
                      </div>
                      <div className="cell-sub">{vendor.campusLocation}</div>
                    </td>
                    <td data-label="Status">
                      <StatusBadge status={vendor.status} kind="vendor" />
                    </td>
                    <td className="table-actions">
                      <div className="cell-actions">
                        <button
                          type="button"
                          className="btn-icon"
                          title="View application"
                          aria-label={`View application from ${vendor.shopName}`}
                          onClick={() => setDetailsId(vendor.id)}
                        >
                          <Icon name="eye" size={16} />
                        </button>

                        {vendor.status === 'Pending' ? (
                          <>
                            <Button
                              size="sm"
                              variant="ghost"
                              onClick={() => setRejectTarget(vendor)}
                              disabled={busy}
                            >
                              Reject
                            </Button>
                            <Button
                              size="sm"
                              variant="primary"
                              icon="check"
                              onClick={() => setApproveTarget(vendor)}
                              disabled={busy}
                            >
                              Approve
                            </Button>
                          </>
                        ) : (
                          <span className="foot-hint">Reviewed</span>
                        )}
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : (
          <EmptyState
            title={
              statusFilter === 'Pending'
                ? 'No applications waiting'
                : `No ${statusFilter.toLowerCase()} applications`
            }
            message={
              search.trim()
                ? 'No applications match your search. Try a different keyword.'
                : statusFilter === 'Pending'
                  ? 'All vendor applications have been reviewed.'
                  : 'Applications with this status will appear here.'
            }
            action={
              search.trim() ? (
                <Button variant="secondary" onClick={() => setSearch('')}>
                  Clear search
                </Button>
              ) : null
            }
          />
        )}

        {filteredVendors.length ? (
          <div className="card-foot">
            <span>
              {counts.Pending} pending application
              {counts.Pending === 1 ? '' : 's'}
            </span>
            <span className="foot-hint">
              Approved vendors can manage products and fulfil orders.
            </span>
          </div>
        ) : null}
      </div>

      <VendorDetailsModal
        vendor={activeVendor}
        busy={busy}
        onClose={() => setDetailsId(null)}
        onApprove={(vendor) => {
          setDetailsId(null)
          setApproveTarget(vendor)
        }}
        onReject={(vendor) => {
          setDetailsId(null)
          setRejectTarget(vendor)
        }}
      />

      <ConfirmDialog
        open={Boolean(approveTarget)}
        title={`Approve ${approveTarget?.shopName ?? 'this vendor'}?`}
        message={
          <>
            This gives <strong>{approveTarget?.ownerName}</strong> access to the
            vendor workspace and makes their store visible to students. You can
            review this decision later.
          </>
        }
        confirmLabel="Approve vendor"
        busy={busy}
        onConfirm={handleApprove}
        onClose={() => {
          if (!busy) setApproveTarget(null)
        }}
      />

      {rejectTarget ? (
        <RejectVendorDialog
          key={rejectTarget.id}
          vendor={rejectTarget}
          busy={busy}
          onClose={() => {
            if (!busy) setRejectTarget(null)
          }}
          onConfirm={handleReject}
        />
      ) : null}
    </div>
  )
}
