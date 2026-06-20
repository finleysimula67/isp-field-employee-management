import client from './client'

export async function getTasks(params?: { status?: string }) {
  const res = await client.get('/tasks', { params })
  return res.data
}

export async function getMyTasks(params?: { status?: string }) {
  const res = await client.get('/tasks/my', { params })
  return res.data
}

export async function createTask(data: any) {
  const res = await client.post('/tasks', data)
  return res.data
}

export async function updateTaskStatus(id: number, data: { status: string }) {
  const res = await client.patch(`/tasks/${id}/status`, data)
  return res.data
}

export async function updateTask(id: number, data: any) {
  const res = await client.put(`/tasks/${id}`, data)
  return res.data
}

export async function deleteTask(id: number) {
  const res = await client.delete(`/tasks/${id}`)
  return res.data
}

export async function batchDeleteTasks(data: { ids: number[] }) {
  const res = await client.post('/tasks/batch-delete', data)
  return res.data
}
