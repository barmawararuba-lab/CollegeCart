import Icon from '../Icon'

export default function SearchInput({
  value,
  onChange,
  placeholder = 'Search…',
  ariaLabel,
}) {
  return (
    <div className="search-box">
      <Icon name="search" size={16} />
      <input
        type="search"
        className="input"
        value={value}
        onChange={(event) => onChange(event.target.value)}
        placeholder={placeholder}
        aria-label={ariaLabel || placeholder}
      />
    </div>
  )
}
