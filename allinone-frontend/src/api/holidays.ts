import client from './client'
export async function getHolidays() { const res = await client.get('/holidays'); return res.data }
export async function createHoliday(data: any) { const res = await client.post('/holidays', data); return res.data }
export async function deleteHoliday(id: number) { const res = await client.delete(`/holidays/${id}`); return res.data }
export async function batchDeleteHolidays(data: { ids: number[] }) { const res = await client.post('/holidays/batch-delete', data); return res.data }
