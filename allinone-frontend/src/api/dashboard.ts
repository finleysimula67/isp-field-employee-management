import client from './client'
export async function getAdminStats() { const res = await client.get('/dashboard/stats'); return res.data }
export async function getEmployeeStats() { const res = await client.get('/dashboard/employee-stats'); return res.data }
