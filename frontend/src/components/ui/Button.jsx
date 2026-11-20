import Icon from '../Icon'

export default function Button({
  children,
  variant = 'secondary',
  size = 'md',
  icon,
  iconRight,
  type = 'button',
  className = '',
  ...rest
}) {
  const iconSize = size === 'sm' ? 14 : 16

  return (
    <button
      type={type}
      className={`btn btn-${variant} ${size === 'sm' ? 'btn-sm' : ''} ${className}`.trim()}
      {...rest}
    >
      {icon ? <Icon name={icon} size={iconSize} /> : null}
      {children}
      {iconRight ? <Icon name={iconRight} size={iconSize} /> : null}
    </button>
  )
}
