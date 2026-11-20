function getInitials(name = '') {
  return (
    name
      .split(/\s+/)
      .filter(Boolean)
      .slice(0, 2)
      .map((word) => word[0])
      .join('')
      .toUpperCase() || '?'
  )
}

export default function ProductThumb({ product, size = 'md' }) {
  return (
    <div className={`product-thumb product-thumb--${size}`}>
      {product.image ? (
        <img src={product.image} alt="" />
      ) : (
        <span aria-hidden="true">{getInitials(product.name)}</span>
      )}
    </div>
  )
}
