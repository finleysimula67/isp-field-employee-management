import client from './client'

export async function getBranches() {
  const res = await client.get('/branches')
  return res.data
}

export async function createBranch(data: { name: string; code?: string; address?: string; managerId?: number }) {
  const res = await client.post('/branches', data)
  return res.data
}

export async function updateBranch(id: number, data: { name?: string; code?: string; address?: string; managerId?: number }) {
  const res = await client.put(`/branches/${id}`, data)
  return res.data
}

export async function deleteBranch(id: number) {
  const res = await client.delete(`/branches/${id}`)
  return res.data
}
