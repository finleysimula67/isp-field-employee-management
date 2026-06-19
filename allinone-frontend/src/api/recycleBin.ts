import client from './client'

export const getRecycleBinItems = (entityType?: string, page = 0, size = 50) =>
  client.get('/recycle-bin', { params: { entityType, page, size } }).then(r => r.data)

export const getRecycleBinCount = (entityType?: string) =>
  client.get('/recycle-bin/count', { params: { entityType } }).then(r => r.data)

export const restoreRecycleBinItem = (id: number) =>
  client.post(`/recycle-bin/${id}/restore`).then(r => r.data)

export const permanentDeleteRecycleBinItem = (id: number) =>
  client.delete(`/recycle-bin/${id}`).then(r => r.data)
