import client from './client'
import type { ApiResponse } from '../types'

export interface PurgeResult {
  deletedCounts: Record<string, number>
  totalDeleted: number
}

export interface TransferResult {
  sourceEmployeeId: number
  sourceEmployeeName: string
  targetEmployeeId: number
  targetEmployeeName: string
  transferredCounts: Record<string, number>
  sourceDeleted: boolean
  totalTransferred: number
}

export async function purgeAllData(): Promise<ApiResponse<PurgeResult>> {
  const res = await client.post('/admin/purge')
  return res.data
}

export async function transferOwnershipFull(data: {
  sourceEmployeeId: number
  targetEmployeeId: number
  deleteSource: boolean
}): Promise<ApiResponse<TransferResult>> {
  const res = await client.post('/transfer', data)
  return res.data
}
