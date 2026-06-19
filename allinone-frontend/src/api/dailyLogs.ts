import client from './client'

export async function getDailyLogs(params?: { status?: string; date?: string; employeeId?: number }) {
  const res = await client.get('/daily-logs', { params })
  return res.data
}

export async function getMyLogs(params?: { category?: string; date?: string }) {
  const res = await client.get('/daily-logs/my', { params })
  return res.data
}

export async function createDailyLog(data: any) {
  const res = await client.post('/daily-logs', data)
  return res.data
}

export async function getMyEarnings() {
  const res = await client.get('/daily-logs/my/earnings')
  return res.data
}

export async function reviewDailyLog(id: number, data: { status: string; reviewComment?: string }) {
  const res = await client.put(`/daily-logs/${id}/review`, data)
  return res.data
}

export async function batchReviewDailyLogs(data: { ids: number[]; status: string; reviewComment?: string }) {
  const res = await client.post('/daily-logs/batch-review', data)
  return res.data
}

export async function deleteDailyLog(id: number) {
  const res = await client.delete(`/daily-logs/${id}`)
  return res.data
}

export async function batchDeleteDailyLogs(data: { ids: number[] }) {
  const res = await client.post('/daily-logs/batch-delete', data)
  return res.data
}
