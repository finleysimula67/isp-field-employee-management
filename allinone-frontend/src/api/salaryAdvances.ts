import client from './client'

export async function getAdvances(params?: { status?: string }) {
  const res = await client.get('/salary-advances', { params })
  return res.data
}

export async function getMyAdvances() {
  const res = await client.get('/salary-advances/my')
  return res.data
}

export async function requestAdvance(data: any) {
  const res = await client.post('/salary-advances', data)
  return res.data
}

export async function reviewAdvance(id: number, data: any) {
  const res = await client.put(`/salary-advances/${id}/review`, data)
  return res.data
}

export async function manualAdvance(data: { employeeId: number; amount: number; reason?: string }) {
  const res = await client.post('/salary-advances/manual', data)
  return res.data
}

export async function getMyBalance() {
  const res = await client.get('/salary-advances/balance')
  return res.data
}

export async function getBalanceForEmployee(employeeId: number) {
  const res = await client.get(`/salary-advances/balance/${employeeId}`)
  return res.data
}
