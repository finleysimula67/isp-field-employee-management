import { useEffect, useState, useCallback } from 'react'
import { getNotifications, markAsRead, markAllAsRead } from '../../api/notifications'
import { useWebSocket } from '../../hooks/useWebSocket'
import Toast from '../../components/Toast'
import Skeleton from '../../components/Skeleton'

export default function NotificationsPage() {
  const [notifications, setNotifications] = useState<any[]>([])
  const [loading, setLoading] = useState(true)
  const [toast, setToast] = useState<{ message: string; type: 'success' | 'error' | 'info' } | null>(null)

  const onNotification = useCallback((data: any) => {
    setNotifications(prev => [data, ...prev])
  }, [])

  useWebSocket({ onNotification })

  const fetchData = () => {
    setLoading(true)
    getNotifications().then((res) => {
      setNotifications(res.data)
      setLoading(false)
    }).catch(() => { setToast({ message: 'Failed to load notifications', type: 'error' }); setLoading(false) })
  }

  useEffect(() => { fetchData() }, [])

  const handleMarkRead = async (id: number) => {
    try {
      await markAsRead(id)
      fetchData()
    } catch { setToast({ message: 'Failed to mark as read', type: 'error' }) }
  }

  const handleMarkAllRead = async () => {
    try {
      await markAllAsRead()
      fetchData()
    } catch { setToast({ message: 'Failed to mark all as read', type: 'error' }) }
  }

  const timeAgo = (dateStr: string) => {
    const diff = Date.now() - new Date(dateStr).getTime()
    const mins = Math.floor(diff / 60000)
    if (mins < 1) return 'just now'
    if (mins < 60) return `${mins}m ago`
    const hours = Math.floor(mins / 60)
    if (hours < 24) return `${hours}h ago`
    const days = Math.floor(hours / 24)
    return `${days}d ago`
  }

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h1 className="font-display text-2xl font-bold text-gray-900">Notifications</h1>
        <button onClick={handleMarkAllRead} className="btn-ghost text-sm">Mark All Read</button>
      </div>
      <Toast message={toast?.message || ''} type={toast?.type || 'info'} visible={!!toast} onClose={() => setToast(null)} />
      <div className="space-y-3">
        {loading ? (
          <Skeleton variant="text" count={3} />
        ) : notifications.length === 0 ? (
          <p className="text-gray-400 text-sm">Nothing here yet</p>
        ) : notifications.map((n: any) => (
          <div
            key={n.id}
            onClick={() => { if (!n.isRead) handleMarkRead(n.id) }}
            className={`card cursor-pointer transition-colors ${n.isRead ? 'bg-white' : 'bg-blue-50 border-l-4 border-blue-500'}`}
          >
            <div className="flex items-start justify-between gap-4">
              <div className="flex items-start gap-3">
                {!n.isRead && <div className="w-2 h-2 rounded-full bg-blue-500 mt-2 shrink-0" />}
                <div>
                  <p className="font-medium text-sm text-gray-900">{n.title}</p>
                  <p className="text-xs text-gray-500 mt-0.5">{n.body}</p>
                </div>
              </div>
              <span className="text-xs text-gray-400 shrink-0">{timeAgo(n.createdAt)}</span>
            </div>
          </div>
        ))}
      </div>
    </div>
  )
}
