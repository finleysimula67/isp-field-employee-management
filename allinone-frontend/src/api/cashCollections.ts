import client from './client'

export async function getCashCollections(params?: { status?: string; employeeId?: number }) {
  const res = await client.get('/cash-collections', { params })
  return res.data
}

export async function getMyCashCollections(params?: { status?: string }) {
  const res = await client.get('/cash-collections/my', { params })
  return res.data
}

export async function createCashCollection(data: any) {
  const res = await client.post('/cash-collections', data)
  return res.data
}

export async function createCashCollectionAdmin(data: any) {
  const res = await client.post('/cash-collections/admin-create', data)
  return res.data
}

export async function reviewCashCollection(id: number, data: { status: string; reviewComment?: string }) {
  const res = await client.put(`/cash-collections/${id}/review`, data)
  return res.data
}

export async function batchReviewCashCollections(data: { ids: number[]; status: string; reviewComment?: string }) {
  const res = await client.post('/cash-collections/batch-review', data)
  return res.data
}

export async function getCashCollectionSummary(month: number, year: number) {
  const res = await client.get('/cash-collections/summary', { params: { month, year } })
  return res.data
}

export async function getMyCashCollectionSummary(month: number, year: number) {
  const res = await client.get('/cash-collections/my/summary', { params: { month, year } })
  return res.data
}
