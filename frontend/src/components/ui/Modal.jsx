import { useEffect, useId, useRef } from 'react'
import Icon from '../Icon'

export default function Modal({
  open,
  onClose,
  title,
  subtitle,
  size = 'md',
  footer,
  closeDisabled = false,
  children,
}) {
  const dialogRef = useRef(null)
  const onCloseRef = useRef(onClose)
  const titleId = useId()

  useEffect(() => {
    onCloseRef.current = onClose
  })

  useEffect(() => {
    if (!open) return undefined

    const handleKeyDown = (event) => {
      if (event.key === 'Escape' && !closeDisabled) {
        onCloseRef.current()
      }
    }

    const previousOverflow = document.body.style.overflow
    const previouslyFocused = document.activeElement
    document.body.style.overflow = 'hidden'
    document.addEventListener('keydown', handleKeyDown)

    const focusTimer = window.setTimeout(() => dialogRef.current?.focus(), 0)

    return () => {
      window.clearTimeout(focusTimer)
      document.removeEventListener('keydown', handleKeyDown)
      document.body.style.overflow = previousOverflow
      if (previouslyFocused && typeof previouslyFocused.focus === 'function') {
        previouslyFocused.focus()
      }
    }
  }, [open, closeDisabled])

  if (!open) return null

  return (
    <div
      className="modal-overlay"
      onMouseDown={(event) => {
        if (event.target === event.currentTarget && !closeDisabled) onClose()
      }}
    >
      <div
        className={`modal ${size === 'lg' ? 'modal-lg' : ''} ${size === 'sm' ? 'modal-sm' : ''}`.trim()}
        role="dialog"
        aria-modal="true"
        aria-labelledby={titleId}
        tabIndex={-1}
        ref={dialogRef}
      >
        <div className="modal-head">
          <div className="modal-head-text">
            <h2 className="modal-title" id={titleId}>
              {title}
            </h2>
            {subtitle ? <p className="modal-sub">{subtitle}</p> : null}
          </div>
          <button
            type="button"
            className="btn-icon modal-close"
            onClick={onClose}
            disabled={closeDisabled}
            aria-label="Close dialog"
          >
            <Icon name="x" size={18} />
          </button>
        </div>
        <div className="modal-body">{children}</div>
        {footer ? <div className="modal-foot">{footer}</div> : null}
      </div>
    </div>
  )
}
