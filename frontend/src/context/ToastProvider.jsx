import { useEffect, useRef, useState } from 'react'
import Icon from '../components/Icon'
import { ToastContext } from './toastContext'

const ICONS = {
  success: 'checkCircle',
  error: 'alert',
  info: 'info',
}

let toastId = 0

export function ToastProvider({ children }) {
  const [toasts, setToasts] = useState([])
  const timers = useRef(new Set())

  useEffect(() => {
    const activeTimers = timers.current
    return () => {
      activeTimers.forEach((timer) => window.clearTimeout(timer))
    }
  }, [])

  function removeToast(id) {
    setToasts((previous) => previous.filter((toast) => toast.id !== id))
  }

  function pushToast(message, variant) {
    toastId += 1
    const id = toastId
    setToasts((previous) => [...previous, { id, message, variant }])

    const timer = window.setTimeout(() => {
      setToasts((previous) => previous.filter((toast) => toast.id !== id))
      timers.current.delete(timer)
    }, 4200)

    timers.current.add(timer)
  }

  const toast = {
    success: (message) => pushToast(message, 'success'),
    error: (message) => pushToast(message, 'error'),
    info: (message) => pushToast(message, 'info'),
  }

  return (
    <ToastContext.Provider value={{ toast }}>
      {children}
      <div className="toast-viewport" aria-live="polite" aria-atomic="false">
        {toasts.map((item) => (
          <div className={`toast toast-${item.variant}`} key={item.id}>
            <span className="toast-icon">
              <Icon name={ICONS[item.variant]} size={18} />
            </span>
            <p className="toast-message">{item.message}</p>
            <button
              type="button"
              className="toast-close"
              onClick={() => removeToast(item.id)}
              aria-label="Dismiss notification"
            >
              <Icon name="x" size={15} />
            </button>
          </div>
        ))}
      </div>
    </ToastContext.Provider>
  )
}
