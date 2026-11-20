export default function Field({
  label,
  htmlFor,
  error,
  hint,
  required = false,
  className = '',
  children,
}) {
  return (
    <div className={`field ${className}`.trim()}>
      <label htmlFor={htmlFor}>
        {label}
        {required ? (
          <span className="required" aria-hidden="true">
            {' '}
            *
          </span>
        ) : null}
      </label>
      {children}
      {error ? (
        <span className="field-error" role="alert">
          {error}
        </span>
      ) : hint ? (
        <span className="field-hint">{hint}</span>
      ) : null}
    </div>
  )
}
