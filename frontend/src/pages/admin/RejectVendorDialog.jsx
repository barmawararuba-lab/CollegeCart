import { useState } from 'react'
import Avatar from '../../components/ui/Avatar'
import Button from '../../components/ui/Button'
import Field from '../../components/ui/Field'
import Modal from '../../components/ui/Modal'
import { validateRejectionReason } from '../../utils/validation'

export default function RejectVendorDialog({
  vendor,
  busy,
  onClose,
  onConfirm,
}) {
  const [reason, setReason] = useState('')
  const [error, setError] = useState('')

  function handleReasonChange(event) {
    setReason(event.target.value)
    if (error) setError('')
  }

  async function handleSubmit(event) {
    event.preventDefault()
    const message = validateRejectionReason(reason)
    if (message) {
      setError(message)
      return
    }
    await onConfirm(reason.trim())
  }

  return (
    <Modal
      open={Boolean(vendor)}
      onClose={onClose}
      closeDisabled={busy}
      title={`Reject ${vendor?.shopName ?? 'application'}?`}
      subtitle="The applicant will see your reason when they check their status."
      footer={
        <>
          <Button variant="secondary" onClick={onClose} disabled={busy}>
            Cancel
          </Button>
          <Button
            type="submit"
            form="reject-vendor-form"
            variant="danger"
            disabled={busy}
            icon={busy ? undefined : 'x'}
          >
            {busy ? 'Rejecting…' : 'Reject application'}
          </Button>
        </>
      }
    >
      {vendor ? (
        <div className="reject-context">
          <Avatar name={vendor.ownerName} tone="rose" />
          <div>
            <div className="reject-context-name">{vendor.shopName}</div>
            <div className="reject-context-meta">
              {vendor.ownerName} · {vendor.category}
            </div>
          </div>
        </div>
      ) : null}

      <form id="reject-vendor-form" onSubmit={handleSubmit} noValidate>
        <Field
          label="Reason for rejection"
          htmlFor="reject-reason"
          required
          error={error}
          hint={`${reason.length}/300 characters · minimum 10`}
        >
          <textarea
            id="reject-reason"
            className={`textarea ${error ? 'is-invalid' : ''}`.trim()}
            rows={4}
            maxLength={300}
            disabled={busy}
            placeholder="e.g. Shop licence and campus address could not be verified."
            value={reason}
            onChange={handleReasonChange}
            aria-invalid={Boolean(error)}
          />
        </Field>
      </form>
    </Modal>
  )
}
