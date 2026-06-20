import client from '../api/client'
import { peekAll, removeItem } from './offlineQueue'

let syncing = false
let onSyncStatusChange: ((syncing: boolean, count: number) => void) | null = null

export function setSyncStatusCallback(cb: typeof onSyncStatusChange) {
  onSyncStatusChange = cb
}

export async function processQueue(): Promise<void> {
  if (syncing) return
  syncing = true
  onSyncStatusChange?.(true, 0)
  try {
    const items = await peekAll()
    for (const item of items) {
      try {
        const config: any = {
          method: item.method,
          url: item.endpoint,
          data: item.body,
          headers: {},
        }
        if (item.token) {
          config.headers.Authorization = `Bearer ${item.token}`
        }
        await client.request(config)
        await removeItem(item.id!)
      } catch {
        continue
      }
    }
  } finally {
    syncing = false
    const remaining = await peekAll()
    onSyncStatusChange?.(false, remaining.length)
  }
}
