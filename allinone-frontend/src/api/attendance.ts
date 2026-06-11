import client from './client'

export async function getMonthlyAttendance(month: number, year: number) {
  const res = await client.get('/attendance/monthly', { params: { month, year } })
  return res.data
}

export async function getWageSummary(month: number, year: number) {
  const res = await client.get('/attendance/wages', { params: { month, year } })
  return res.data
}

export async function getMyMonthlyAttendance(month: number, year: number) {
  const res = await client.get('/attendance/my/monthly', { params: { month, year } })
  return res.data
}

export async function getMyWageSummary(month: number, year: number) {
  const res = await client.get('/attendance/my/wages', { params: { month, year } })
  return res.data
}
