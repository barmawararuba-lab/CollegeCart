export const NAV_ITEMS = {
  vendor: [
    {
      path: '/vendor/dashboard',
      label: 'Dashboard',
      icon: 'dashboard',
      description: 'Store performance at a glance',
    },
    {
      path: '/vendor/products',
      label: 'Products',
      icon: 'package',
      description: 'Manage catalog, pricing and stock',
    },
    {
      path: '/vendor/orders',
      label: 'Orders',
      icon: 'bag',
      description: 'Accept, prepare and complete orders',
    },
  ],
  admin: [
    {
      path: '/admin/dashboard',
      label: 'Overview',
      icon: 'dashboard',
      description: 'Platform health and activity',
    },
    {
      path: '/admin/vendors',
      label: 'Vendor approvals',
      icon: 'store',
      description: 'Review campus store applications',
    },
  ],
}

export const ROLE_META = {
  vendor: {
    label: 'Vendor workspace',
    crumb: 'Vendor',
    switchLabel: 'Switch to admin console',
    switchPath: '/admin/dashboard',
  },
  admin: {
    label: 'Admin console',
    crumb: 'Admin',
    switchLabel: 'Switch to vendor workspace',
    switchPath: '/vendor/dashboard',
  },
}

export const DEMO_ACCOUNTS = {
  vendor: {
    name: 'Meera Krishnan',
    role: 'Campus Corner Stationery',
    tone: 'indigo',
  },
  admin: {
    name: 'Dr. Ramesh Kulkarni',
    role: 'Platform administrator',
    tone: 'teal',
  },
}
