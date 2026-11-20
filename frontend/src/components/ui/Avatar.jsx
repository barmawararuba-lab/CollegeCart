import { getInitials } from '../../utils/format'

export default function Avatar({ name, size = 'md', tone = 'indigo' }) {
  return (
    <span className={`avatar avatar-${size} avatar-${tone}`} aria-hidden="true">
      {getInitials(name)}
    </span>
  )
}
