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

export async function verifyOtp(email: string, code: string): Promise<ApiResponse<LoginResponse>> {
  const res = await client.post('/auth/verify-otp', { email, code })
  return res.data
}

export async function enableMfa(): Promise<ApiResponse<void>> {
  const res = await client.post('/auth/mfa/enable')
  return res.data
}

export async function disableMfa(): Promise<ApiResponse<void>> {
  const res = await client.post('/auth/mfa/disable')
  return res.data
}
