import client from './client'

export async function getBranches() {
  const res = await client.get('/branches')
  return res.data
}

export async function createBranch(name: string, code?: string, address?: string) {
  const params = new URLSearchParams()
  params.append('name', name)
  if (code) params.append('code', code)
  if (address) params.append('address', address)
  const res = await client.post('/branches', params, { headers: { 'Content-Type': 'application/x-www-form-urlencoded' } })
  return res.data
}

export async function updateBranch(id: number, data: { name?: string; code?: string; address?: string; managerId?: number }) {
  const params = new URLSearchParams()
  if (data.name) params.append('name', data.name)
  if (data.code) params.append('code', data.code)
  if (data.address) params.append('address', data.address)
  if (data.managerId) params.append('managerId', String(data.managerId))
  const res = await client.put(`/branches/${id}`, params, { headers: { 'Content-Type': 'application/x-www-form-urlencoded' } })
  return res.data
}
