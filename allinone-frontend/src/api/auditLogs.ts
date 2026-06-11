import client from './client'

export async function getAuditLogs(entityType?: string, from?: string, to?: string) {
  const params: any = {}
  if (entityType) params.entityType = entityType
  if (from) params.from = from
  if (to) params.to = to
  const res = await client.get('/audit-logs', { params })
  return res.data
}
