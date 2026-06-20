import client from './client'

export async function getAllLockouts() {
  const res = await client.get('/lockouts')
  return res.data
}

export async function getLockoutStatus(yearMonth: string) {
  const res = await client.get('/lockouts/status', { params: { yearMonth } })
  return res.data
}

export async function lockMonth(yearMonth: string) {
  const res = await client.post('/lockouts/lock', { yearMonth })
  return res.data
}

export async function unlockMonth(yearMonth: string, reason: string) {
  const res = await client.post('/lockouts/unlock', { reason }, { params: { yearMonth } })
  return res.data
}
