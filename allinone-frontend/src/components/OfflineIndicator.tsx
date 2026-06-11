import { useEffect, useState } from 'react'
import { useOnlineStatus } from '../hooks/useOnlineStatus'
import { getQueueCount, peekAll } from '../services/offlineQueue'

export default function OfflineIndicator() {
  const isOnline = useOnlineStatus()
  const [pendingCount, setPendingCount] = useState(0)

  const refreshCount = () => {
    getQueueCount().then(setPendingCount).catch(() => {})
  }

  useEffect(() => {
    if (!isOnline) {
      refreshCount()
      const interval = setInterval(refreshCount, 5000)
      return () => clearInterval(interval)
    } else {
      setPendingCount(0)
    }
  }, [isOnline])

  if (isOnline && pendingCount === 0) return null

  return (
    <div className={`sticky top-0 z-50 px-4 py-1.5 text-xs text-center font-medium transition-colors ${
      isOnline
        ? 'bg-amber-50 text-amber-700 border-b border-amber-200'
        : 'bg-red-50 text-red-700 border-b border-red-200'
    }`}>
      {isOnline
        ? `${pendingCount} pending submission${pendingCount === 1 ? '' : 's'} — syncing...`
        : 'You are offline — submissions will be saved and sent when connection resumes'
      }
    </div>
  )
}
