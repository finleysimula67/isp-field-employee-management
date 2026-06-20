import client from './client'

export async function getAllowList() {
  const res = await client.get('/email-allow-list')
  return res.data
}

export async function addToAllowList(email: string) {
  const res = await client.post('/email-allow-list', { email })
  return res.data
}

export async function removeFromAllowList(id: number) {
  const res = await client.delete(`/email-allow-list/${id}`)
  return res.data
}

export async function batchDeleteAllowListEntries(data: { ids: number[] }) {
  const res = await client.post('/email-allow-list/batch-delete', data)
  return res.data
}
