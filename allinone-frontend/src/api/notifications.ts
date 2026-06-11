import client from './client'
export async function getNotifications() { const res = await client.get('/notifications'); return res.data }
export async function getUnreadCount() { const res = await client.get('/notifications/unread-count'); return res.data }
export async function markAsRead(id: number) { const res = await client.put(`/notifications/${id}/read`); return res.data }
export async function markAllAsRead() { const res = await client.put('/notifications/read-all'); return res.data }
