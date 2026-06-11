import client from './client'

export async function getLeaveRequests(params?: { status?: string }) {
  const res = await client.get('/leave-requests', { params })
  return res.data
}

export async function getMyLeaveRequests(params?: { status?: string }) {
  const res = await client.get('/leave-requests/my', { params })
  return res.data
}

export async function createLeaveRequest(data: any) {
  const res = await client.post('/leave-requests', data)
  return res.data
}

export async function reviewLeaveRequest(id: number, data: { status: string; reviewComment?: string }) {
  const res = await client.put(`/leave-requests/${id}/review`, data)
  return res.data
}

export async function batchReviewLeaveRequests(data: { ids: number[]; status: string; reviewComment?: string }) {
  const res = await client.post('/leave-requests/batch-review', data)
  return res.data
}
