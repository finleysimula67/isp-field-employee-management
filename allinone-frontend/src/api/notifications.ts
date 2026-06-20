import client from './client'
export async function getNotifications() { const res = await client.get('/notifications'); return res.data }
export async function getUnreadCount() { const res = await client.get('/notifications/unread-count'); return res.data }
export async function markAsRead(id: number) { const res = await client.put(`/notifications/${id}/read`); return res.data }
export async function markAllAsRead() { const res = await client.put('/notifications/read-all'); return res.data }
export async function deleteNotification(id: number) { const res = await client.delete(`/notifications/${id}`); return res.data }
export async function batchDeleteNotifications(data: { ids: number[] }) { const res = await client.post('/notifications/batch-delete', data); return res.data }
