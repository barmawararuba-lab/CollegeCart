const ALLOWED_IMAGE_TYPES = ['image/jpeg', 'image/png', 'image/webp']
const MAX_IMAGE_BYTES = 2 * 1024 * 1024

export function validateProduct(values) {
  const errors = {}
  const name = values.name.trim()
  const description = values.description.trim()
  const priceText = String(values.price).trim()
  const stockText = String(values.stock).trim()

  if (!name) errors.name = 'Product name is required.'
  else if (name.length < 3) errors.name = 'Use at least 3 characters.'
  else if (name.length > 80) errors.name = 'Keep the name under 80 characters.'

  if (!values.category) errors.category = 'Choose a category.'

  if (!priceText) errors.price = 'Price is required.'
  else if (Number.isNaN(Number(priceText)) || Number(priceText) <= 0) {
    errors.price = 'Enter a price greater than ₹0.'
  } else if (Number(priceText) > 100000) {
    errors.price = 'Price cannot exceed ₹1,00,000.'
  }

  if (!stockText) errors.stock = 'Stock quantity is required.'
  else if (!Number.isInteger(Number(stockText)) || Number(stockText) < 0) {
    errors.stock = 'Enter a whole number of 0 or more.'
  } else if (Number(stockText) > 10000) {
    errors.stock = 'Stock cannot exceed 10,000 units.'
  }

  if (!description) errors.description = 'Add a short description for students.'
  else if (description.length > 300) {
    errors.description = 'Keep the description under 300 characters.'
  }

  return errors
}

export function validateImageFile(file) {
  if (!ALLOWED_IMAGE_TYPES.includes(file.type)) {
    return 'Upload a JPG, PNG or WebP image.'
  }
  if (file.size > MAX_IMAGE_BYTES) {
    return 'Image must be 2 MB or smaller.'
  }
  return ''
}

export function validateRejectionReason(reason) {
  const trimmed = reason.trim()
  if (!trimmed) return 'Add a reason so the applicant knows what to fix.'
  if (trimmed.length < 10) return 'Use at least 10 characters.'
  return ''
}
