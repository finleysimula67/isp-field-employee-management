export interface Employee {
  id: number
  email: string
  name: string
  phone: string | null
  role: 'SUPER_ADMIN' | 'BRANCH_MANAGER' | 'FIELD_EMPLOYEE'
  branchId: number | null
  branchName: string | null
  authType: string
  isActive: boolean
  isAccountApproved: boolean
  wageType: 'DAILY' | 'HOURLY' | null
  dailyRate: number | null
  hourlyWage: number | null
  totalLeaveDaysPerYear: number
  remainingLeaveDays: number
  carryOverLeave: number
  maxAdvanceLimit: number
  isOwner: boolean
  createdAt: string
}

export interface Branch {
  id: number
  name: string
  code: string | null
  address: string | null
  managerId: number | null
  isActive: boolean
}

export interface LoginRequest {
  email: string
  password: string
}

export interface LoginResponse {
  token: string
  email: string
  name: string
  role: string
  userId: number
}

export interface ApiResponse<T> {
  success: boolean
  message: string
  data: T
}
