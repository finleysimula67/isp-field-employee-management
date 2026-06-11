import { openDB, type IDBPDatabase } from 'idb'

interface QueueItem {
  id?: number
  endpoint: string
  method: string
  body: any
  token: string | null
  createdAt: string
  retryCount: number
  lastError: string | null
}

const DB_NAME = 'allinone-offline'
const DB_VERSION = 1
const STORE_NAME = 'sync-queue'

let dbPromise: Promise<IDBPDatabase> | null = null

function getDb() {
  if (!dbPromise) {
    dbPromise = openDB(DB_NAME, DB_VERSION, {
      upgrade(db) {
        if (!db.objectStoreNames.contains(STORE_NAME)) {
          const store = db.createObjectStore(STORE_NAME, {
            keyPath: 'id',
            autoIncrement: true,
          })
          store.createIndex('createdAt', 'createdAt')
        }
      },
    })
  }
  return dbPromise
}

export async function enqueue(endpoint: string, method: string, body: any, token: string | null): Promise<void> {
  const db = await getDb()
  const item: QueueItem = {
    endpoint,
    method,
    body,
    token,
    createdAt: new Date().toISOString(),
    retryCount: 0,
    lastError: null,
  }
  await db.add(STORE_NAME, item)
}

export async function dequeue(): Promise<QueueItem | undefined> {
  const db = await getDb()
  const tx = db.transaction(STORE_NAME, 'readwrite')
  const store = tx.objectStore(STORE_NAME)
  const cursor = await store.openCursor()
  if (!cursor) return undefined
  const item = cursor.value
  await cursor.delete()
  return item
}

export async function peekAll(): Promise<QueueItem[]> {
  const db = await getDb()
  return db.getAll(STORE_NAME)
}

export async function getQueueCount(): Promise<number> {
  const db = await getDb()
  return db.count(STORE_NAME)
}

export async function removeItem(id: number): Promise<void> {
  const db = await getDb()
  await db.delete(STORE_NAME, id)
}

export async function clearQueue(): Promise<void> {
  const db = await getDb()
  await db.clear(STORE_NAME)
}
