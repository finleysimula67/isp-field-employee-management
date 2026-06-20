import { useEffect, useState, useCallback } from 'react'
import { getNotifications, markAsRead, markAllAsRead, deleteNotification, batchDeleteNotifications } from '../../api/notifications'
import { useWebSocket } from '../../hooks/useWebSocket'
import Toast from '../../components/Toast'
import Skeleton from '../../components/Skeleton'
import DeleteConfirmModal from '../../components/DeleteConfirmModal'

export default function NotificationsPage() {
  const [notifications, setNotifications] = useState<any[]>([])
  const [loading, setLoading] = useState(true)
  const [toast, setToast] = useState<{ message: string; type: 'success' | 'error' | 'info' } | null>(null)
  const [selectedIds, setSelectedIds] = useState<number[]>([])
  const [confirmBatchDelete, setConfirmBatchDelete] = useState(false)
  const [deleting, setDeleting] = useState(false)
  const [confirmDeleteId, setConfirmDeleteId] = useState<number | null>(null)

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

  const handleDelete = async () => {
    if (confirmDeleteId === null) return
    setDeleting(true)
    try {
      await deleteNotification(confirmDeleteId)
      setConfirmDeleteId(null)
      fetchData()
    } catch { setToast({ message: 'Failed to delete notification', type: 'error' }) }
    finally { setDeleting(false) }
  }

  const toggleSelect = (id: number) => {
    setSelectedIds(prev => prev.includes(id) ? prev.filter(i => i !== id) : [...prev, id])
  }

  const toggleSelectAll = () => {
    if (selectedIds.length === notifications.length) setSelectedIds([])
    else setSelectedIds(notifications.map(n => n.id))
  }

  const handleBatchDelete = async () => {
    if (selectedIds.length === 0) return
    setDeleting(true)
    try {
      await batchDeleteNotifications({ ids: selectedIds })
      setConfirmBatchDelete(false)
      setSelectedIds([])
      setToast({ message: `${selectedIds.length} notification(s) deleted`, type: 'success' })
      fetchData()
    } catch {
      setToast({ message: 'Batch delete failed', type: 'error' })
    } finally {
      setDeleting(false)
    }
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
        <div className="flex gap-2">
          <button onClick={handleMarkAllRead} className="btn-ghost text-sm">Mark All Read</button>
        </div>
      </div>
      <Toast message={toast?.message || ''} type={toast?.type || 'info'} visible={!!toast} onClose={() => setToast(null)} />
      {selectedIds.length > 0 && (
        <div className="card p-3 mb-4 flex flex-wrap items-center gap-3 bg-blue-50 border-blue-200">
          <span className="text-sm font-medium text-blue-800">{selectedIds.length} selected</span>
          <button onClick={() => setSelectedIds([])} className="btn-ghost text-xs">Clear</button>
          <button onClick={() => setConfirmBatchDelete(true)} className="text-xs bg-red-50 text-red-600 px-3 py-1.5 rounded-lg hover:bg-red-100 font-medium">Delete Selected</button>
        </div>
      )}
      <div className="space-y-3">
        {loading ? (
          <Skeleton variant="text" count={3} />
        ) : notifications.length === 0 ? (
          <p className="text-gray-400 text-sm">Nothing here yet</p>
        ) : (
          <>
            <div className="flex items-center gap-2 mb-2 px-1">
              <input type="checkbox" onChange={toggleSelectAll} checked={selectedIds.length === notifications.length && notifications.length > 0} className="rounded" />
              <span className="text-xs text-gray-500">Select all</span>
            </div>
            {notifications.map((n: any) => (
              <div key={n.id} className="flex items-start gap-3">
                <div className="pt-3 pl-1">
                  <input type="checkbox" checked={selectedIds.includes(n.id)} onChange={() => toggleSelect(n.id)} className="rounded" />
                </div>
                <div
                  onClick={() => { if (!n.isRead) handleMarkRead(n.id) }}
                  className={`card cursor-pointer transition-colors flex-1 ${n.isRead ? 'bg-white' : 'bg-blue-50 border-l-4 border-blue-500'}`}
                >
                  <div className="flex items-start justify-between gap-4">
                    <div className="flex items-start gap-3">
                      {!n.isRead && <div className="w-2 h-2 rounded-full bg-blue-500 mt-2 shrink-0" />}
                      <div>
                        <p className="font-medium text-sm text-gray-900">{n.title}</p>
                        <p className="text-xs text-gray-500 mt-0.5">{n.body}</p>
                      </div>
                    </div>
                    <div className="flex items-center gap-2 shrink-0">
                      <span className="text-xs text-gray-400">{timeAgo(n.createdAt)}</span>
                      <button onClick={(e) => { e.stopPropagation(); setConfirmDeleteId(n.id) }} className="text-xs text-red-400 hover:text-red-600">✕</button>
                    </div>
                  </div>
                </div>
              </div>
            ))}
          </>
        )}
      </div>
      <DeleteConfirmModal open={confirmDeleteId !== null} title="Delete Notification?" message="This notification will be deleted." onConfirm={handleDelete} onCancel={() => setConfirmDeleteId(null)} loading={deleting} />
      <DeleteConfirmModal open={confirmBatchDelete} title={`Delete ${selectedIds.length} Notifications?`} message="These notifications will be deleted." count={selectedIds.length} onConfirm={handleBatchDelete} onCancel={() => setConfirmBatchDelete(false)} loading={deleting} />
    </div>
  )
}