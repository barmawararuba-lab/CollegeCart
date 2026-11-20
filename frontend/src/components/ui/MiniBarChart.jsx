export default function MiniBarChart({
  data,
  labelKey = 'day',
  valueKey = 'total',
  formatValue = (value) => value,
  ariaLabel = 'Bar chart',
}) {
  const max = Math.max(
    ...data.map((item) => Number(item[valueKey]) || 0),
    1,
  )

  return (
    <div className="mini-bars" role="img" aria-label={ariaLabel}>
      {data.map((item) => {
        const value = Number(item[valueKey]) || 0
        const height = Math.max((value / max) * 100, 5)

        return (
          <div className="mini-bar" key={item.date || item[labelKey]}>
            <div className="mini-bar-track">
              <div
                className={`mini-bar-fill ${value === max ? 'is-peak' : ''}`.trim()}
                style={{ height: `${height}%` }}
                title={`${item[labelKey]}: ${formatValue(value)}`}
              />
            </div>
            <span className="mini-bar-label">{item[labelKey]}</span>
          </div>
        )
      })}
    </div>
  )
}
