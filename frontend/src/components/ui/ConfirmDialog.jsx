import Icon from '../Icon'
import Button from './Button'
import Modal from './Modal'

export default function ConfirmDialog({
  open,
  title,
  message,
  confirmLabel = 'Confirm',
  cancelLabel = 'Cancel',
  tone = 'primary',
  busy = false,
  onConfirm,
  onClose,
}) {
  const isDanger = tone === 'danger'

  return (
    <Modal
      open={open}
      onClose={onClose}
      title={title}
      size="sm"
      closeDisabled={busy}
      footer={
        <>
          <Button variant="secondary" onClick={onClose} disabled={busy}>
            {cancelLabel}
          </Button>
          <Button
            variant={isDanger ? 'danger' : 'primary'}
            onClick={onConfirm}
            disabled={busy}
            icon={busy ? undefined : isDanger ? 'alert' : 'check'}
          >
            {busy ? 'Please wait…' : confirmLabel}
          </Button>
        </>
      }
    >
      <div className={`confirm-body ${isDanger ? 'is-danger' : ''}`.trim()}>
        <span className="confirm-icon">
          <Icon name={isDanger ? 'alert' : 'checkCircle'} size={22} />
        </span>
        <div className="confirm-text">{message}</div>
      </div>
    </Modal>
  )
}
