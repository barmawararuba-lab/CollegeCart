import { useMemo, useState } from 'react'
import Badge from '../../components/ui/Badge'
import Button from '../../components/ui/Button'
import ConfirmDialog from '../../components/ui/ConfirmDialog'
import PageHeader from '../../components/ui/PageHeader'
import ProductThumb from '../../components/ui/ProductThumb'
import SearchInput from '../../components/ui/SearchInput'
import {
  EmptyState as EmptyStateCard,
  ErrorState,
  PageSkeleton,
} from '../../components/ui/StateViews'
import Icon from '../../components/Icon'
import { PRODUCT_CATEGORIES, STOCK_THRESHOLD } from '../../constants'
import { useAppData } from '../../context/useAppData'
import { useToast } from '../../context/useToast'
import { formatCurrency, formatDate } from '../../utils/format'
import { getStockStatus, matchesAvailability, AVAILABILITY_FILTERS } from '../../utils/products'
import ProductFormModal from './ProductFormModal'

const SORT_OPTIONS = [
  { value: 'newest', label: 'Newest first' },
  { value: 'name', label: 'Name A–Z' },
  { value: 'price-asc', label: 'Price: low to high' },
  { value: 'price-desc', label: 'Price: high to low' },
  { value: 'stock-asc', label: 'Stock: low to high' },
]

export default function ProductManagement() {
  const {
    products,
    loading,
    error,
    reload,
    addProduct,
    editProduct,
    removeProduct,
  } = useAppData()
  const { toast } = useToast()

  const [search, setSearch] = useState('')
  const [category, setCategory] = useState('all')
  const [availability, setAvailability] = useState('all')
  const [sort, setSort] = useState('newest')
  const [formState, setFormState] = useState(null)
  const [deleteTarget, setDeleteTarget] = useState(null)
  const [deleting, setDeleting] = useState(false)

  const filteredProducts = useMemo(() => {
    const term = search.trim().toLowerCase()

    const list = products.filter((product) => {
      const matchesTerm =
        !term ||
        product.name.toLowerCase().includes(term) ||
        product.description.toLowerCase().includes(term) ||
        product.category.toLowerCase().includes(term)

      const matchesCategory =
        category === 'all' || product.category === category

      return (
        matchesTerm &&
        matchesCategory &&
        matchesAvailability(product.stock, availability)
      )
    })

    return [...list].sort((a, b) => {
      if (sort === 'name') return a.name.localeCompare(b.name)
      if (sort === 'price-asc') return a.price - b.price
      if (sort === 'price-desc') return b.price - a.price
      if (sort === 'stock-asc') return a.stock - b.stock
      return new Date(b.updatedAt) - new Date(a.updatedAt)
    })
  }, [products, search, category, availability, sort])

  const hasFilters =
    Boolean(search.trim()) ||
    category !== 'all' ||
    availability !== 'all' ||
    sort !== 'newest'

  function clearFilters() {
    setSearch('')
    setCategory('all')
    setAvailability('all')
    setSort('newest')
  }

  async function handleSave(payload) {
    if (formState?.product) {
      await editProduct(formState.product.id, payload)
      toast.success(`"${payload.name}" was updated.`)
    } else {
      await addProduct(payload)
      toast.success(`"${payload.name}" was added to your catalog.`)
    }
    setFormState(null)
  }

  async function handleDelete() {
    if (!deleteTarget) return
    setDeleting(true)
    try {
      await removeProduct(deleteTarget.id)
      toast.success(`"${deleteTarget.name}" was deleted from the catalog.`)
      setDeleteTarget(null)
    } catch (deleteError) {
      toast.error(deleteError.message || 'Could not delete this product.')
    } finally {
      setDeleting(false)
    }
  }

  if (loading) return <PageSkeleton />
  if (error) return <ErrorState message={error} onRetry={reload} />

  const noProductsAtAll = products.length === 0

  return (
    <div className="page">
      <PageHeader
        title="Products"
        subtitle="Manage your catalog, pricing, images and stock levels."
        actions={
          <Button
            variant="primary"
            icon="plus"
            onClick={() => setFormState({ product: null })}
          >
            Add product
          </Button>
        }
      />

      <div className="card">
        <div className="toolbar">
          <SearchInput
            value={search}
            onChange={setSearch}
            placeholder="Search products…"
            ariaLabel="Search products by name, description or category"
          />

          <select
            className="select"
            value={category}
            onChange={(event) => setCategory(event.target.value)}
            aria-label="Filter by category"
          >
            <option value="all">All categories</option>
            {PRODUCT_CATEGORIES.map((item) => (
              <option key={item} value={item}>
                {item}
              </option>
            ))}
          </select>

          <select
            className="select"
            value={availability}
            onChange={(event) => setAvailability(event.target.value)}
            aria-label="Filter by availability"
          >
            {AVAILABILITY_FILTERS.map((item) => (
              <option key={item.value} value={item.value}>
                {item.label}
              </option>
            ))}
          </select>

          <select
            className="select"
            value={sort}
            onChange={(event) => setSort(event.target.value)}
            aria-label="Sort products"
          >
            {SORT_OPTIONS.map((item) => (
              <option key={item.value} value={item.value}>
                {item.label}
              </option>
            ))}
          </select>

          {hasFilters ? (
            <Button variant="ghost" size="sm" onClick={clearFilters}>
              Clear filters
            </Button>
          ) : null}

          <span className="toolbar-meta">
            Showing <strong>{filteredProducts.length}</strong> of{' '}
            {products.length} products
          </span>
        </div>

        {filteredProducts.length ? (
          <>
            <div className="table-wrap">
              <table className="table table-stacked">
                <thead>
                  <tr>
                    <th>Product</th>
                    <th>Category</th>
                    <th>Price</th>
                    <th>Stock</th>
                    <th>Availability</th>
                    <th>Updated</th>
                    <th className="align-right">Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {filteredProducts.map((product) => {
                    const stockStatus = getStockStatus(product.stock)

                    return (
                      <tr key={product.id}>
                        <td data-label="Product">
                          <div className="product-cell">
                            <ProductThumb product={product} />
                            <div className="product-cell-text">
                              <div className="product-cell-name">
                                {product.name}
                              </div>
                            </div>
                          </div>
                        </td>
                        <td data-label="Category">
                          <Badge tone="neutral">{product.category}</Badge>
                        </td>
                        <td data-label="Price" className="num">
                          {formatCurrency(product.price)}
                        </td>
                        <td data-label="Stock" className="num">
                          {product.stock}
                        </td>
                        <td data-label="Availability">
                          <Badge tone={stockStatus.tone} dot status>
                            {stockStatus.label}
                          </Badge>
                        </td>
                        <td data-label="Updated">{formatDate(product.updatedAt)}</td>
                        <td className="table-actions">
                          <div className="cell-actions">
                            <button
                              type="button"
                              className="btn-icon"
                              title="Edit product"
                              aria-label={`Edit ${product.name}`}
                              onClick={() => setFormState({ product })}
                            >
                              <Icon name="edit" size={16} />
                            </button>
                            <button
                              type="button"
                              className="btn-icon is-danger"
                              title="Delete product"
                              aria-label={`Delete ${product.name}`}
                              onClick={() => setDeleteTarget(product)}
                            >
                              <Icon name="trash" size={16} />
                            </button>
                          </div>
                        </td>
                      </tr>
                    )
                  })}
                </tbody>
              </table>
            </div>

            <div className="card-foot">
              <span>
                {filteredProducts.length} of {products.length} products shown
              </span>
              <span className="foot-hint">
                Stock of {STOCK_THRESHOLD} units or fewer is flagged as low.
              </span>
            </div>
          </>
        ) : noProductsAtAll ? (
          <EmptyStateCard
            title="Your catalog is empty"
            message="Add your first product so students can start ordering from your store."
            action={
              <Button
                variant="primary"
                icon="plus"
                onClick={() => setFormState({ product: null })}
              >
                Add your first product
              </Button>
            }
          />
        ) : (
          <EmptyStateCard
            title="No products match your filters"
            message="Try a different keyword, category or availability filter."
            action={
              <Button variant="secondary" onClick={clearFilters}>
                Clear filters
              </Button>
            }
          />
        )}
      </div>

      {formState ? (
        <ProductFormModal
          key={formState.product?.id ?? 'new-product'}
          open
          product={formState.product}
          onClose={() => setFormState(null)}
          onSave={handleSave}
        />
      ) : null}

      <ConfirmDialog
        open={Boolean(deleteTarget)}
        title="Delete product?"
        message={
          <>
            Permanently delete <strong>{deleteTarget?.name}</strong>? Students
            will no longer see it in your store and this cannot be undone.
          </>
        }
        confirmLabel="Delete product"
        tone="danger"
        busy={deleting}
        onConfirm={handleDelete}
        onClose={() => {
          if (!deleting) setDeleteTarget(null)
        }}
      />
    </div>
  )
}
