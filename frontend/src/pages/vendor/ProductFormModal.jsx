import { useRef, useState } from 'react'
import Button from '../../components/ui/Button'
import Field from '../../components/ui/Field'
import Modal from '../../components/ui/Modal'
import Icon from '../../components/Icon'
import { PRODUCT_CATEGORIES } from '../../constants'
import { validateImageFile, validateProduct } from '../../utils/validation'

const EMPTY_FORM = {
  name: '',
  category: PRODUCT_CATEGORIES[0],
  price: '',
  stock: '',
  description: '',
  image: null,
}

function buildFormValues(product) {
  if (!product) return EMPTY_FORM
  return {
    name: product.name,
    category: product.category,
    price: String(product.price),
    stock: String(product.stock),
    description: product.description,
    image: product.image ?? null,
  }
}

export default function ProductFormModal({
  open,
  product,
  onClose,
  onSave,
}) {
  const [values, setValues] = useState(() => buildFormValues(product))
  const [errors, setErrors] = useState({})
  const [imageError, setImageError] = useState('')
  const [submitError, setSubmitError] = useState('')
  const [saving, setSaving] = useState(false)
  const fileInputRef = useRef(null)

  function updateField(field, value) {
    setValues((previous) => ({ ...previous, [field]: value }))
    setErrors((previous) => {
      if (!previous[field]) return previous
      const next = { ...previous }
      delete next[field]
      return next
    })
    setSubmitError('')
  }

  function handleImageChange(event) {
    const file = event.target.files?.[0]
    event.target.value = ''
    if (!file) return

    const fileError = validateImageFile(file)
    if (fileError) {
      setImageError(fileError)
      return
    }

    setImageError('')
    const reader = new FileReader()
    reader.onload = () => {
      setValues((previous) => ({
        ...previous,
        image: typeof reader.result === 'string' ? reader.result : null,
      }))
    }
    reader.onerror = () => {
      setImageError('We could not read that image. Please try another file.')
    }
    reader.readAsDataURL(file)
  }

  async function handleSubmit(event) {
    event.preventDefault()
    const validationErrors = validateProduct(values)
    setErrors(validationErrors)
    if (Object.keys(validationErrors).length) return

    setSaving(true)
    setSubmitError('')
    try {
      await onSave({
        name: values.name.trim(),
        description: values.description.trim(),
        category: values.category,
        price: Number(values.price),
        stock: Number(values.stock),
        image: values.image,
      })
    } catch (error) {
      setSubmitError(
        error.message || 'Could not save this product. Please try again.',
      )
    } finally {
      setSaving(false)
    }
  }

  return (
    <Modal
      open={open}
      onClose={onClose}
      closeDisabled={saving}
      title={product ? 'Edit product' : 'Add new product'}
      subtitle="These details appear in the student storefront."
      size="lg"
      footer={
        <>
          <Button variant="secondary" onClick={onClose} disabled={saving}>
            Cancel
          </Button>
          <Button
            type="submit"
            form="product-form"
            variant="primary"
            disabled={saving}
            icon={saving ? undefined : 'check'}
          >
            {saving
              ? 'Saving…'
              : product
                ? 'Save changes'
                : 'Add product'}
          </Button>
        </>
      }
    >
      {submitError ? (
        <div className="form-alert" role="alert">
          <Icon name="alert" size={16} />
          <span>{submitError}</span>
        </div>
      ) : null}

      <form id="product-form" className="form-grid" onSubmit={handleSubmit} noValidate>
        <Field
          label="Product name"
          htmlFor="product-name"
          required
          error={errors.name}
          className="span-2"
        >
          <input
            id="product-name"
            className={`input ${errors.name ? 'is-invalid' : ''}`.trim()}
            type="text"
            placeholder="e.g. A4 Ruled Notebook (200 pages)"
            value={values.name}
            maxLength={80}
            onChange={(event) => updateField('name', event.target.value)}
            aria-invalid={Boolean(errors.name)}
          />
        </Field>

        <Field
          label="Category"
          htmlFor="product-category"
          required
          error={errors.category}
        >
          <select
            id="product-category"
            className="select"
            value={values.category}
            onChange={(event) => updateField('category', event.target.value)}
          >
            {PRODUCT_CATEGORIES.map((category) => (
              <option key={category} value={category}>
                {category}
              </option>
            ))}
          </select>
        </Field>

        <Field
          label="Price (₹)"
          htmlFor="product-price"
          required
          error={errors.price}
          hint="Selling price for one unit."
        >
          <input
            id="product-price"
            className={`input ${errors.price ? 'is-invalid' : ''}`.trim()}
            type="number"
            inputMode="decimal"
            min="0"
            step="0.01"
            placeholder="0"
            value={values.price}
            onChange={(event) => updateField('price', event.target.value)}
            aria-invalid={Boolean(errors.price)}
          />
        </Field>

        <Field
          label="Stock quantity"
          htmlFor="product-stock"
          required
          error={errors.stock}
          hint="Units currently available for pickup."
        >
          <input
            id="product-stock"
            className={`input ${errors.stock ? 'is-invalid' : ''}`.trim()}
            type="number"
            inputMode="numeric"
            min="0"
            step="1"
            placeholder="0"
            value={values.stock}
            onChange={(event) => updateField('stock', event.target.value)}
            aria-invalid={Boolean(errors.stock)}
          />
        </Field>

        <div className="field">
          <span className="field-hint">
            Availability is calculated automatically from stock:
            in stock, low stock (10 or fewer) or out of stock.
          </span>
        </div>

        <Field
          label="Description"
          htmlFor="product-description"
          required
          error={errors.description}
          hint={`${values.description.length}/300 characters`}
          className="span-2"
        >
          <textarea
            id="product-description"
            className={`textarea ${errors.description ? 'is-invalid' : ''}`.trim()}
            rows={3}
            maxLength={300}
            placeholder="What should students know about this product?"
            value={values.description}
            onChange={(event) => updateField('description', event.target.value)}
            aria-invalid={Boolean(errors.description)}
          />
        </Field>

        <Field
          label="Product image"
          htmlFor="product-image"
          error={imageError}
          hint="Optional. JPG, PNG or WebP, up to 2 MB."
          className="span-2"
        >
          <div className="image-upload">
            <div className="image-preview">
              {values.image ? (
                <img src={values.image} alt="Selected product preview" />
              ) : (
                <Icon name="image" size={22} />
              )}
            </div>
            <div className="upload-actions">
              <div className="upload-buttons">
                <Button
                  variant="secondary"
                  size="sm"
                  icon="upload"
                  onClick={() => fileInputRef.current?.click()}
                >
                  {values.image ? 'Replace image' : 'Upload image'}
                </Button>
                {values.image ? (
                  <Button
                    variant="ghost"
                    size="sm"
                    onClick={() =>
                      setValues((previous) => ({ ...previous, image: null }))
                    }
                  >
                    Remove
                  </Button>
                ) : null}
              </div>
              <span>
                Square images look best. A category icon is used when no image
                is uploaded.
              </span>
            </div>
            <input
              id="product-image"
              ref={fileInputRef}
              className="file-input-hidden"
              type="file"
              accept="image/png,image/jpeg,image/webp"
              onChange={handleImageChange}
            />
          </div>
        </Field>
      </form>
    </Modal>
  )
}
