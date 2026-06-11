import client from './client'
import type { Employee, ApiResponse } from '../types'

export async function getEmployees(): Promise<ApiResponse<Employee[]>> {
  const res = await client.get('/employees')
  return res.data
}

export async function getEmployee(id: number): Promise<ApiResponse<Employee>> {
  const res = await client.get(`/employees/${id}`)
  return res.data
}

export async function transferOwnership(targetId: number): Promise<ApiResponse<Employee>> {
  const res = await client.put(`/employees/transfer-ownership/${targetId}`)
  return res.data
}

export async function approveEmployee(id: number): Promise<ApiResponse<Employee>> {
  const res = await client.put(`/employees/${id}/approve`)
  return res.data
}

export async function createEmployee(data: {
  email: string
  name: string
  password?: string
  role: string
  branchId?: number | null
  authType?: string
  wageType?: string
  dailyRate?: number | null
  hourlyWage?: number | null
  totalLeaveDaysPerYear?: number | null
}): Promise<ApiResponse<Employee>> {
  const res = await client.post('/employees', data)
  return res.data
}

export async function updateEmployee(id: number, data: {
  name?: string
  role?: string
  branchId?: number | null
  wageType?: string
  dailyRate?: number | null
  hourlyWage?: number | null
  totalLeaveDaysPerYear?: number | null
  remainingLeaveDays?: number | null
  carryOverLeave?: number | null
  maxAdvanceLimit?: number | null
}): Promise<ApiResponse<Employee>> {
  const res = await client.put(`/employees/${id}`, data)
  return res.data
}
