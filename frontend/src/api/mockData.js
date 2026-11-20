import { ORDER_STATUS } from '../constants'

const minutesAgo = (minutes) =>
  new Date(Date.now() - minutes * 60 * 1000).toISOString()

export const initialProducts = [
  {
    id: 'PRD-1001',
    name: 'A4 Ruled Notebook (200 pages)',
    category: 'Stationery',
    price: 45,
    stock: 120,
    image: null,
    description:
      'Single-line ruled notebooks for daily lecture notes, assignments and lab work.',
    updatedAt: minutesAgo(120),
  },
  {
    id: 'PRD-1002',
    name: 'Classmate Spiral Notebook',
    category: 'Stationery',
    price: 60,
    stock: 8,
    image: null,
    description:
      'Durable spiral-bound notebook with 180 pages. Popular with first-year students.',
    updatedAt: minutesAgo(240),
  },
  {
    id: 'PRD-1003',
    name: 'Reynolds 045 Pens (Pack of 5)',
    category: 'Stationery',
    price: 50,
    stock: 64,
    image: null,
    description:
      'Smooth-writing blue ball pens, ideal for exams and everyday note-taking.',
    updatedAt: minutesAgo(360),
  },
  {
    id: 'PRD-1004',
    name: 'Casio fx-991EX Scientific Calculator',
    category: 'Electronics',
    price: 1450,
    stock: 12,
    image: null,
    description:
      'Approved non-programmable calculator for engineering and science exams.',
    updatedAt: minutesAgo(480),
  },
  {
    id: 'PRD-1005',
    name: 'Engineering Mathematics – B.S. Grewal (Used)',
    category: 'Books',
    price: 380,
    stock: 3,
    image: null,
    description:
      'Pre-owned 44th edition. Minor highlights in chapters 1–3, otherwise clean.',
    updatedAt: minutesAgo(600),
  },
  {
    id: 'PRD-1006',
    name: 'Introduction to Algorithms – CLRS (Used)',
    category: 'Books',
    price: 650,
    stock: 0,
    image: null,
    description:
      'Third edition hardcover. Covers data structures and algorithm design.',
    updatedAt: minutesAgo(720),
  },
  {
    id: 'PRD-1007',
    name: 'Exam Geometry Box',
    category: 'Stationery',
    price: 95,
    stock: 25,
    image: null,
    description:
      'Metal compass, divider, protractor and set squares in a sturdy case.',
    updatedAt: minutesAgo(840),
  },
  {
    id: 'PRD-1008',
    name: 'USB-C Fast Charging Cable (1 m)',
    category: 'Electronics',
    price: 199,
    stock: 40,
    image: null,
    description:
      'Braided 3A cable that works with most Android phones and laptops.',
    updatedAt: minutesAgo(960),
  },
  {
    id: 'PRD-1009',
    name: 'Lab Record Book (100 pages)',
    category: 'Stationery',
    price: 70,
    stock: 9,
    image: null,
    description:
      'Half-white, half-ruled pages with a hard cover for lab observations.',
    updatedAt: minutesAgo(1080),
  },
  {
    id: 'PRD-1010',
    name: 'Instant Noodles Cup (Pack of 3)',
    category: 'Snacks',
    price: 75,
    stock: 56,
    image: null,
    description:
      'Quick microwave noodles for late-night study sessions. Masala flavour.',
    updatedAt: minutesAgo(1200),
  },
  {
    id: 'PRD-1011',
    name: 'College ID Card Lanyard',
    category: 'Accessories',
    price: 40,
    stock: 15,
    image: null,
    description:
      'Breakaway lanyard with a transparent ID holder. College colours.',
    updatedAt: minutesAgo(1320),
  },
  {
    id: 'PRD-1012',
    name: 'A3 Drawing Board with Clips',
    category: 'Stationery',
    price: 220,
    stock: 6,
    image: null,
    description:
      'Portable drawing board for graphics and engineering drawing practicals.',
    updatedAt: minutesAgo(1440),
  },
]

const cancelledHistory = (placed) => [
  { status: ORDER_STATUS.PENDING, at: minutesAgo(placed) },
  { status: ORDER_STATUS.CANCELLED, at: minutesAgo(placed - 60) },
]

export const initialOrders = [
  {
    id: 'CC-2041',
    customer: {
      name: 'Ravi Kumar',
      rollNo: '23CS1042',
      phone: '+91 98450 11234',
      email: 'ravi.kumar@college.edu',
    },
    items: [
      { name: 'A4 Ruled Notebook (200 pages)', qty: 2, price: 45 },
      { name: 'Reynolds 045 Pens (Pack of 5)', qty: 1, price: 50 },
    ],
    total: 140,
    status: ORDER_STATUS.PENDING,
    placedAt: minutesAgo(26),
    pickupNote: 'Student Centre counter, 11 AM – 6 PM',
    paymentMethod: 'UPI at pickup',
    history: [{ status: ORDER_STATUS.PENDING, at: minutesAgo(26) }],
  },
  {
    id: 'CC-2042',
    customer: {
      name: 'Harsh Deshmukh',
      rollNo: '22EE0904',
      phone: '+91 98220 41675',
      email: 'harsh.deshmukh@college.edu',
    },
    items: [
      {
        name: 'Engineering Mathematics – B.S. Grewal (Used)',
        qty: 1,
        price: 380,
      },
    ],
    total: 380,
    status: ORDER_STATUS.PENDING,
    placedAt: minutesAgo(6),
    pickupNote: 'Student Centre counter, 11 AM – 6 PM',
    paymentMethod: 'Cash at pickup',
    history: [{ status: ORDER_STATUS.PENDING, at: minutesAgo(6) }],
  },
  {
    id: 'CC-2043',
    customer: {
      name: 'Ishita Bose',
      rollNo: '24CS1330',
      phone: '+91 98301 22674',
      email: 'ishita.bose@college.edu',
    },
    items: [
      { name: 'Lab Record Book (100 pages)', qty: 1, price: 70 },
      { name: 'Exam Geometry Box', qty: 1, price: 95 },
    ],
    total: 165,
    status: ORDER_STATUS.PENDING,
    placedAt: minutesAgo(2),
    pickupNote: 'Student Centre counter, 11 AM – 6 PM',
    paymentMethod: 'UPI at pickup',
    history: [{ status: ORDER_STATUS.PENDING, at: minutesAgo(2) }],
  },
  {
    id: 'CC-2040',
    customer: {
      name: 'Ananya Sharma',
      rollNo: '24EC1178',
      phone: '+91 99001 44521',
      email: 'ananya.sharma@college.edu',
    },
    items: [
      { name: 'Casio fx-991EX Scientific Calculator', qty: 1, price: 1450 },
    ],
    total: 1450,
    status: ORDER_STATUS.ACCEPTED,
    placedAt: minutesAgo(40),
    pickupNote: 'Student Centre counter, 11 AM – 6 PM',
    paymentMethod: 'UPI at pickup',
    history: [
      { status: ORDER_STATUS.PENDING, at: minutesAgo(40) },
      { status: ORDER_STATUS.ACCEPTED, at: minutesAgo(25) },
    ],
  },
  {
    id: 'CC-2039',
    customer: {
      name: 'Mohammed Irfan',
      rollNo: '23ME1090',
      phone: '+91 90032 78110',
      email: 'mohammed.irfan@college.edu',
    },
    items: [
      { name: 'Exam Geometry Box', qty: 1, price: 95 },
      { name: 'Lab Record Book (100 pages)', qty: 1, price: 70 },
    ],
    total: 165,
    status: ORDER_STATUS.PREPARING,
    placedAt: minutesAgo(80),
    pickupNote: 'Student Centre counter, 11 AM – 6 PM',
    paymentMethod: 'Cash at pickup',
    history: [
      { status: ORDER_STATUS.PENDING, at: minutesAgo(80) },
      { status: ORDER_STATUS.ACCEPTED, at: minutesAgo(65) },
      { status: ORDER_STATUS.PREPARING, at: minutesAgo(30) },
    ],
  },
  {
    id: 'CC-2038',
    customer: {
      name: 'Sneha Pillai',
      rollNo: '24AI1210',
      phone: '+91 88670 90345',
      email: 'sneha.pillai@college.edu',
    },
    items: [
      { name: 'Engineering Mathematics – B.S. Grewal (Used)', qty: 1, price: 380 },
    ],
    total: 380,
    status: ORDER_STATUS.READY,
    placedAt: minutesAgo(120),
    pickupNote: 'Student Centre counter, 11 AM – 6 PM',
    paymentMethod: 'UPI at pickup',
    history: [
      { status: ORDER_STATUS.PENDING, at: minutesAgo(120) },
      { status: ORDER_STATUS.ACCEPTED, at: minutesAgo(105) },
      { status: ORDER_STATUS.PREPARING, at: minutesAgo(80) },
      { status: ORDER_STATUS.READY, at: minutesAgo(55) },
    ],
  },
  {
    id: 'CC-2037',
    customer: {
      name: 'Arjun Verma',
      rollNo: '22CS0981',
      phone: '+91 97401 22890',
      email: 'arjun.verma@college.edu',
    },
    items: [
      { name: 'USB-C Fast Charging Cable (1 m)', qty: 1, price: 199 },
      { name: 'College ID Card Lanyard', qty: 1, price: 40 },
    ],
    total: 239,
    status: ORDER_STATUS.COMPLETED,
    placedAt: minutesAgo(210),
    pickupNote: 'Student Centre counter, 11 AM – 6 PM',
    paymentMethod: 'UPI at pickup',
    history: [
      { status: ORDER_STATUS.PENDING, at: minutesAgo(210) },
      { status: ORDER_STATUS.ACCEPTED, at: minutesAgo(195) },
      { status: ORDER_STATUS.PREPARING, at: minutesAgo(170) },
      { status: ORDER_STATUS.READY, at: minutesAgo(120) },
      { status: ORDER_STATUS.COMPLETED, at: minutesAgo(90) },
    ],
  },
  {
    id: 'CC-2036',
    customer: {
      name: 'Divya Nair',
      rollNo: '24CS1301',
      phone: '+91 90219 55477',
      email: 'divya.nair@college.edu',
    },
    items: [
      { name: 'A4 Ruled Notebook (200 pages)', qty: 3, price: 45 },
      { name: 'Instant Noodles Cup (Pack of 3)', qty: 1, price: 75 },
    ],
    total: 210,
    status: ORDER_STATUS.CANCELLED,
    placedAt: minutesAgo(1440),
    pickupNote: 'Student Centre counter, 11 AM – 6 PM',
    paymentMethod: 'UPI at pickup',
    history: cancelledHistory(1440),
  },
  {
    id: 'CC-2035',
    customer: {
      name: 'Karthik Rao',
      rollNo: '23EE1120',
      phone: '+91 98498 66342',
      email: 'karthik.rao@college.edu',
    },
    items: [
      { name: 'Casio fx-991EX Scientific Calculator', qty: 1, price: 1450 },
      { name: 'A4 Ruled Notebook (200 pages)', qty: 1, price: 45 },
    ],
    total: 1495,
    status: ORDER_STATUS.COMPLETED,
    placedAt: minutesAgo(2880),
    pickupNote: 'Student Centre counter, 11 AM – 6 PM',
    paymentMethod: 'Cash at pickup',
    history: [
      { status: ORDER_STATUS.PENDING, at: minutesAgo(2880) },
      { status: ORDER_STATUS.ACCEPTED, at: minutesAgo(2860) },
      { status: ORDER_STATUS.PREPARING, at: minutesAgo(2820) },
      { status: ORDER_STATUS.READY, at: minutesAgo(2760) },
      { status: ORDER_STATUS.COMPLETED, at: minutesAgo(2700) },
    ],
  },
  {
    id: 'CC-2034',
    customer: {
      name: 'Priya Menon',
      rollNo: '21CS0876',
      phone: '+91 97411 30982',
      email: 'priya.menon@college.edu',
    },
    items: [
      { name: 'Instant Noodles Cup (Pack of 3)', qty: 1, price: 75 },
      { name: 'Reynolds 045 Pens (Pack of 5)', qty: 1, price: 50 },
    ],
    total: 125,
    status: ORDER_STATUS.COMPLETED,
    placedAt: minutesAgo(3060),
    pickupNote: 'Student Centre counter, 11 AM – 6 PM',
    paymentMethod: 'UPI at pickup',
    history: [
      { status: ORDER_STATUS.PENDING, at: minutesAgo(3060) },
      { status: ORDER_STATUS.ACCEPTED, at: minutesAgo(3040) },
      { status: ORDER_STATUS.PREPARING, at: minutesAgo(3000) },
      { status: ORDER_STATUS.READY, at: minutesAgo(2920) },
      { status: ORDER_STATUS.COMPLETED, at: minutesAgo(2880) },
    ],
  },
  {
    id: 'CC-2033',
    customer: {
      name: 'Tara Iyer',
      rollNo: '23AI1145',
      phone: '+91 90084 22367',
      email: 'tara.iyer@college.edu',
    },
    items: [
      { name: 'A4 Ruled Notebook (200 pages)', qty: 4, price: 45 },
      { name: 'Reynolds 045 Pens (Pack of 5)', qty: 2, price: 50 },
    ],
    total: 280,
    status: ORDER_STATUS.COMPLETED,
    placedAt: minutesAgo(4320),
    pickupNote: 'Student Centre counter, 11 AM – 6 PM',
    paymentMethod: 'UPI at pickup',
    history: [
      { status: ORDER_STATUS.PENDING, at: minutesAgo(4320) },
      { status: ORDER_STATUS.ACCEPTED, at: minutesAgo(4300) },
      { status: ORDER_STATUS.PREPARING, at: minutesAgo(4265) },
      { status: ORDER_STATUS.READY, at: minutesAgo(4200) },
      { status: ORDER_STATUS.COMPLETED, at: minutesAgo(4150) },
    ],
  },
  {
    id: 'CC-2032',
    customer: {
      name: 'Vivek Anand',
      rollNo: '21ME0678',
      phone: '+91 97422 60138',
      email: 'vivek.anand@college.edu',
    },
    items: [
      { name: 'Instant Noodles Cup (Pack of 3)', qty: 2, price: 75 },
      { name: 'College ID Card Lanyard', qty: 1, price: 40 },
    ],
    total: 190,
    status: ORDER_STATUS.COMPLETED,
    placedAt: minutesAgo(7200),
    pickupNote: 'Student Centre counter, 11 AM – 6 PM',
    paymentMethod: 'Cash at pickup',
    history: [
      { status: ORDER_STATUS.PENDING, at: minutesAgo(7200) },
      { status: ORDER_STATUS.ACCEPTED, at: minutesAgo(7180) },
      { status: ORDER_STATUS.PREPARING, at: minutesAgo(7140) },
      { status: ORDER_STATUS.READY, at: minutesAgo(7060) },
      { status: ORDER_STATUS.COMPLETED, at: minutesAgo(7000) },
    ],
  },
]

export const initialVendors = [
  {
    id: 'VND-001',
    shopName: 'Campus Corner Stationery',
    ownerName: 'Meera Krishnan',
    ownerRollNo: '22CS0451',
    email: 'meera.k@college.edu',
    phone: '+91 98450 77221',
    category: 'Stationery',
    campusLocation: 'Student Centre, Shop 4',
    description:
      'Everyday stationery, notebooks and exam essentials for all departments.',
    submittedAt: minutesAgo(60 * 24 * 30),
    status: 'Approved',
    reviewedAt: minutesAgo(60 * 24 * 29),
    rejectionReason: null,
    verification: { studentId: true, campusAddress: true },
  },
  {
    id: 'VND-002',
    shopName: 'Page Turner Books',
    ownerName: 'Aditya Rane',
    ownerRollNo: '24EC1178',
    email: 'aditya.rane@college.edu',
    phone: '+91 99001 88342',
    category: 'Books',
    campusLocation: 'Library Annexe, Counter 2',
    description:
      'Second-hand textbooks, reference guides and competitive exam material.',
    submittedAt: minutesAgo(150),
    status: 'Pending',
    reviewedAt: null,
    rejectionReason: null,
    verification: { studentId: true, campusAddress: true },
  },
  {
    id: 'VND-003',
    shopName: 'TechPoint Accessories',
    ownerName: 'Farhan Shaikh',
    ownerRollNo: '23EE1090',
    email: 'farhan.shaikh@college.edu',
    phone: '+91 90032 61109',
    category: 'Electronics',
    campusLocation: 'Tech Block, Kiosk 1',
    description:
      'Chargers, cables, earphones and repair services for student devices.',
    submittedAt: minutesAgo(320),
    status: 'Pending',
    reviewedAt: null,
    rejectionReason: null,
    verification: { studentId: true, campusAddress: true },
  },
  {
    id: 'VND-004',
    shopName: 'Print & Bind Express',
    ownerName: 'Sanjay Patil',
    ownerRollNo: '22ME0981',
    email: 'sanjay.patil@college.edu',
    phone: '+91 88670 34129',
    category: 'Stationery',
    campusLocation: 'Academic Block, Shop 12',
    description:
      'Printouts, spiral binding, project reports and poster printing.',
    submittedAt: minutesAgo(1400),
    status: 'Pending',
    reviewedAt: null,
    rejectionReason: null,
    verification: { studentId: true, campusAddress: true },
  },
  {
    id: 'VND-005',
    shopName: 'Green Leaf Healthy Bites',
    ownerName: 'Kavya Reddy',
    ownerRollNo: '25AI1210',
    email: 'kavya.reddy@college.edu',
    phone: '+91 97401 55018',
    category: 'Snacks',
    campusLocation: 'Canteen Block, Stall 3',
    description:
      'Fresh fruit bowls, granola bars and healthy snacks between lectures.',
    submittedAt: minutesAgo(2160),
    status: 'Pending',
    reviewedAt: null,
    rejectionReason: null,
    verification: { studentId: true, campusAddress: true },
  },
  {
    id: 'VND-006',
    shopName: 'Night Owl Snacks',
    ownerName: 'Lakshmi Iyer',
    ownerRollNo: '21CS0876',
    email: 'lakshmi.iyer@college.edu',
    phone: '+91 98498 77213',
    category: 'Snacks',
    campusLocation: 'Hostel Gate, Shop 1',
    description:
      'Late-night snacks, instant noodles and cold drinks for hostel students.',
    submittedAt: minutesAgo(60 * 24 * 4),
    status: 'Approved',
    reviewedAt: minutesAgo(1440),
    rejectionReason: null,
    verification: { studentId: true, campusAddress: true },
  },
  {
    id: 'VND-007',
    shopName: 'StudyHub Supplies',
    ownerName: 'Nitin Kulkarni',
    ownerRollNo: '23CS1042',
    email: 'nitin.kulkarni@college.edu',
    phone: '+91 90219 66785',
    category: 'Stationery',
    campusLocation: 'Academic Block, Shop 8',
    description:
      'Record books, drawing instruments and project consumables.',
    submittedAt: minutesAgo(60 * 24 * 6),
    status: 'Approved',
    reviewedAt: minutesAgo(60 * 24 * 5),
    rejectionReason: null,
    verification: { studentId: true, campusAddress: true },
  },
  {
    id: 'VND-008',
    shopName: 'QuickFix Electronics',
    ownerName: 'Zoya Khan',
    ownerRollNo: '24EE1120',
    email: 'zoya.khan@college.edu',
    phone: '+91 97411 99032',
    category: 'Electronics',
    campusLocation: 'Tech Block, Bay 6',
    description: 'Phone accessories and laptop charger rentals.',
    submittedAt: minutesAgo(60 * 24 * 5),
    status: 'Rejected',
    reviewedAt: minutesAgo(60 * 24 * 3),
    rejectionReason:
      'Shop address could not be verified. Please reapply with a valid campus location document.',
    verification: { studentId: true, campusAddress: false },
  },
]

function buildLastSevenDays(totals) {
  const dayNames = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat']
  return totals.map((total, index) => {
    const date = new Date()
    date.setDate(date.getDate() - (6 - index))
    return {
      day: dayNames[date.getDay()],
      date: date.toISOString(),
      total,
    }
  })
}

export const initialPlatformStats = {
  students: 1248,
  activeVendors: 18,
  products: 92,
  orders: 1436,
  signups: buildLastSevenDays([14, 18, 12, 21, 17, 24, 19]).map((day) => ({
    ...day,
    count: day.total,
  })),
}

export const initialActivities = [
  {
    id: 'ACT-8001',
    type: 'vendor',
    title: 'New vendor application: Page Turner Books',
    createdAt: minutesAgo(25),
  },
  {
    id: 'ACT-8002',
    type: 'order',
    title: 'Order CC-2040 accepted by Campus Corner Stationery',
    createdAt: minutesAgo(40),
  },
  {
    id: 'ACT-8003',
    type: 'stock',
    title: 'Low stock alert: Engineering Mathematics has 3 copies left',
    createdAt: minutesAgo(180),
  },
  {
    id: 'ACT-8004',
    type: 'student',
    title: '24 student accounts verified through the registrar sync',
    createdAt: minutesAgo(300),
  },
  {
    id: 'ACT-8005',
    type: 'stock',
    title: 'Introduction to Algorithms (CLRS) is out of stock',
    createdAt: minutesAgo(600),
  },
  {
    id: 'ACT-8006',
    type: 'vendor',
    title: 'Vendor Night Owl Snacks approved',
    createdAt: minutesAgo(1440),
  },
  {
    id: 'ACT-8007',
    type: 'order',
    title: 'Order CC-2037 completed',
    createdAt: minutesAgo(2880),
  },
  {
    id: 'ACT-8008',
    type: 'vendor',
    title: 'QuickFix Electronics application rejected',
    createdAt: minutesAgo(4320),
  },
]
