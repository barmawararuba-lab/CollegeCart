import Avatar from '../../components/ui/Avatar'
import { StatusBadge } from '../../components/ui/Badge'
import Button from '../../components/ui/Button'
import Modal from '../../components/ui/Modal'
import Icon from '../../components/Icon'
import { formatDate, formatRelativeTime } from '../../utils/format'

const AVATAR_TONES = {
  Pending: 'amber',
  Approved: 'teal',
  Rejected: 'rose',
}

export default function VendorDetailsModal({
  vendor,
  busy,
  onClose,
  onApprove,
  onReject,
}) {
  if (!vendor) return null

  const isPending = vendor.status === 'Pending'

  return (
    <Modal
      open
      onClose={onClose}
      closeDisabled={busy}
      size="lg"
      title="Vendor application"
      subtitle={`Application ${vendor.id} · Submitted ${formatDate(vendor.submittedAt)}`}
      footer={
        <>
          <Button variant="secondary" onClick={onClose} disabled={busy}>
            Close
          </Button>
          {isPending ? (
            <>
              <Button
                variant="ghost"
                onClick={() => onReject(vendor)}
                disabled={busy}
              >
                Reject application
              </Button>
              <Button
                variant="primary"
                icon="check"
                onClick={() => onApprove(vendor)}
                disabled={busy}
              >
                Approve vendor
              </Button>
            </>
          ) : null}
        </>
      }
    >
      <div className="vendor-hero">
        <Avatar
          name={vendor.ownerName}
          size="lg"
          tone={AVATAR_TONES[vendor.status] || 'indigo'}
        />
        <div className="vendor-hero-text">
          <div className="vendor-hero-name">
            {vendor.shopName}
            <StatusBadge status={vendor.status} kind="vendor" />
          </div>
          <div className="vendor-hero-owner">
            {vendor.ownerName} · {vendor.ownerRollNo}
          </div>
        </div>
      </div>

      <div className="detail-grid">
        <div className="detail-block">
          <h3 className="detail-block-title">Owner details</h3>
          <div className="detail-line">
            <span>{vendor.ownerName}</span>
          </div>
          <div className="detail-line">
            <span>Roll number: {vendor.ownerRollNo}</span>
          </div>
          <div className="detail-line">
            <span>{vendor.email}</span>
          </div>
          <div className="detail-line">
            <span>{vendor.phone}</span>
          </div>
        </div>

        <div className="detail-block">
          <h3 className="detail-block-title">Shop details</h3>
          <div className="detail-line">
            <span>{vendor.category}</span>
          </div>
          <div className="detail-line">
            <span>{vendor.campusLocation}</span>
          </div>
          <div className="detail-line">
            <span>Submitted {formatRelativeTime(vendor.submittedAt)}</span>
          </div>
          <div className="detail-line">
            <span>
              {vendor.reviewedAt
                ? `Reviewed ${formatRelativeTime(vendor.reviewedAt)}`
                : 'Not reviewed yet'}
            </span>
          </div>
        </div>

        <div className="detail-block detail-block--span">
          <h3 className="detail-block-title">About the shop</h3>
          <p className="vendor-description">{vendor.description}</p>
        </div>

        <div className="detail-block detail-block--span">
          <h3 className="detail-block-title">Verification</h3>
          <div
            className={`detail-line ${vendor.verification.studentId ? 'is-ok' : 'is-miss'}`.trim()}
          >
            <Icon
              name={vendor.verification.studentId ? 'checkCircle' : 'xCircle'}
              size={15}
            />
            <span>
              Student ID {vendor.ownerRollNo}{' '}
              {vendor.verification.studentId ? 'verified' : 'needs review'}
            </span>
          </div>
          <div
            className={`detail-line ${vendor.verification.campusAddress ? 'is-ok' : 'is-miss'}`.trim()}
          >
            <Icon
              name={
                vendor.verification.campusAddress ? 'checkCircle' : 'xCircle'
              }
              size={15}
            />
            <span>
              Campus address{' '}
              {vendor.verification.campusAddress
                ? 'confirmed'
                : 'could not be verified'}
            </span>
          </div>
        </div>
      </div>

      {vendor.rejectionReason ? (
        <div className="rejection-note">
          <Icon name="alert" size={16} />
          <span>
            <strong>Rejection reason:</strong> {vendor.rejectionReason}
          </span>
        </div>
      ) : null}
    </Modal>
  )
}
