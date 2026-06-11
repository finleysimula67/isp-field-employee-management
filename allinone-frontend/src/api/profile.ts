import client from './client'
export async function getProfile() { const res = await client.get('/profile'); return res.data }
export async function updateProfile(data: any) { const res = await client.put('/profile', data); return res.data }
export async function changePassword(data: any) { const res = await client.put('/profile/change-password', data); return res.data }
