import { useEffect, useState, useRef, useCallback } from 'react'
import { Link, useLocation } from 'react-router-dom'
import { getUnreadCount, getNotifications, markAsRead } from '../api/notifications'
import { useWebSocket } from '../hooks/useWebSocket'

export default function NotificationBell() {
  const location = useLocation()
  const isEmployee = location.pathname.startsWith('/employee')
  const [count, setCount] = useState(0)
  const [notifications, setNotifications] = useState<any[]>([])
  const [open, setOpen] = useState(false)
  const ref = useRef<HTMLDivElement>(null)
  const wsFallbackRef = useRef<ReturnType<typeof setInterval> | undefined>(undefined)

  const onNotification = useCallback((data: any) => {
    setNotifications(prev => [data, ...prev].slice(0, 5))
    setCount(prev => prev + 1)
  }, [])

  const onCountUpdate = useCallback((newCount: number) => {
    setCount(newCount)
  }, [])

  useWebSocket({ onNotification, onCountUpdate })

  const fetchData = useCallback(() => {
    getUnreadCount().then((res) => setCount(res.data?.count ?? res.data ?? 0)).catch(() => {})
  }, [])

  const openDropdown = useCallback(() => {
    setOpen(true)
    getNotifications().then((res) => setNotifications(res.data?.slice?.(0, 5) ?? res.data ?? [])).catch(() => {})
  }, [])

  useEffect(() => {
    fetchData()
    wsFallbackRef.current = setInterval(fetchData, 60000)
    return () => clearInterval(wsFallbackRef.current)
  }, [fetchData])

  useEffect(() => {
    const handleClick = (e: MouseEvent) => {
      if (ref.current && !ref.current.contains(e.target as Node)) setOpen(false)
    }
    document.addEventListener('mousedown', handleClick)
    return () => document.removeEventListener('mousedown', handleClick)
  }, [])

  const handleMarkRead = async (id: number) => {
    try {
      await markAsRead(id)
      setNotifications(notifications.map(n => n.id === id ? { ...n, isRead: true } : n))
      setCount(Math.max(0, count - 1))
    } catch {}
  }

  const timeAgo = (dateStr: string) => {
    const diff = Date.now() - new Date(dateStr).getTime()
    const mins = Math.floor(diff / 60000)
    if (mins < 1) return 'now'
    if (mins < 60) return `${mins}m`
    const hours = Math.floor(mins / 60)
    if (hours < 24) return `${hours}h`
    return `${Math.floor(hours / 24)}d`
  }

  return (
    <div ref={ref} className="relative">
      <button onClick={() => open ? setOpen(false) : openDropdown()} className="relative p-1.5 rounded-md hover:bg-gray-100 transition-colors">
        <svg className="w-5 h-5 text-gray-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9" />
        </svg>
        {count > 0 && (
          <span className="absolute -top-0.5 -right-0.5 bg-red-500 text-white text-[10px] font-bold w-4 h-4 rounded-full flex items-center justify-center">
            {count > 9 ? '9+' : count}
          </span>
        )}
      </button>
      {open && (
        <div className="absolute right-0 mt-2 w-80 bg-white rounded-lg shadow-lg border border-gray-200 z-50">
          <div className="p-3 border-b border-gray-100 flex items-center justify-between">
            <span className="font-medium text-sm text-gray-900">Notifications</span>
            {!isEmployee && <Link to="/admin/notifications" onClick={() => setOpen(false)} className="text-xs text-blue-600 hover:text-blue-700">View All</Link>}
          </div>
          <div className="max-h-80 overflow-y-auto">
            {notifications.length === 0 ? (
              <p className="text-xs text-gray-400 text-center py-6">Nothing here yet</p>
            ) : notifications.map((n: any) => (
              <div
                key={n.id}
                onClick={() => { if (!n.isRead) handleMarkRead(n.id) }}
                className={`px-3 py-2.5 border-b border-gray-50 cursor-pointer hover:bg-gray-50 transition-colors ${n.isRead ? '' : 'bg-blue-50/50'}`}
              >
                <div className="flex items-start gap-2">
                  {!n.isRead && <div className="w-2 h-2 rounded-full bg-blue-500 mt-1.5 shrink-0" />}
                  <div className={n.isRead ? 'ml-4' : ''}>
                    <p className="text-sm font-medium text-gray-900">{n.title}</p>
                    <p className="text-xs text-gray-500 truncate">{n.body}</p>
                    <p className="text-[10px] text-gray-400 mt-0.5">{timeAgo(n.createdAt)}</p>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  )
}
