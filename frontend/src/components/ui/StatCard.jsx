export default function StatCard({ label, value, hint, hintTone = '' }) {
  return (
    <article className="stat-card">
      <span className="stat-label">{label}</span>
      <p className="stat-value">{value}</p>
      {hint ? (
        <p className={`stat-hint ${hintTone ? `is-${hintTone}` : ''}`}>
          {hint}
        </p>
      ) : null}
    </article>
  )
}
