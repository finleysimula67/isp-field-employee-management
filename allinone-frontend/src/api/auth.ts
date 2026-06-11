import client from './client'
import type { LoginRequest, LoginResponse, ApiResponse } from '../types'

export async function login(data: LoginRequest): Promise<ApiResponse<LoginResponse>> {
  const res = await client.post('/auth/login', data)
  return res.data
}

export async function checkEmail(email: string): Promise<ApiResponse<boolean>> {
  const res = await client.get('/auth/check-email', { params: { email } })
  return res.data
}
