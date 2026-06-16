import { useEffect } from 'react'
import { Link, Outlet, useLocation } from 'react-router-dom'
import { useAuth } from '../contexts/AuthContext'
import NotificationBell from '../components/NotificationBell'
import OfflineIndicator from '../components/OfflineIndicator'
import { processQueue } from '../services/syncService'
import { useOnlineStatus } from '../hooks/useOnlineStatus'

const sidebarLinks = [
  { path: '/employee', label: 'Dashboard', icon: '📊' },
  { path: '/employee/attendance', label: 'Attendance', icon: '📅' },
  { path: '/employee/daily-log', label: 'Daily Log', icon: '📋' },
  { path: '/employee/cash-collection', label: 'Cash', icon: '💵' },
  { path: '/employee/leave', label: 'Leave', icon: '📝' },
  { path: '/employee/tasks', label: 'Tasks', icon: '✅' },
  { path: '/employee/wages', label: 'Wages', icon: '💰' },
  { path: '/employee/profile', label: 'Profile', icon: '👤' },
]

const bottomNav = [
  { path: '/employee', label: 'Home', icon: '🏠' },
  { path: '/employee/attendance', label: 'Attend', icon: '📅' },
  { path: '/employee/daily-log', label: 'Log', icon: '📋' },
  { path: '/employee/cash-collection', label: 'Cash', icon: '💵' },
  { path: '/employee/leave', label: 'Leave', icon: '📝' },
  { path: '/employee/tasks', label: 'Tasks', icon: '✅' },
  { path: '/employee/wages', label: 'Wages', icon: '💰' },
  { path: '/employee/profile', label: 'Profile', icon: '👤' },
]

export default function EmployeeLayout() {
  const location = useLocation()
  const { user, logout } = useAuth()
  const isOnline = useOnlineStatus()

  useEffect(() => {
    if (isOnline) processQueue()
  }, [isOnline])

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col">
      <OfflineIndicator />
      <header className="bg-white border-b border-gray-200 sticky top-0 z-40">
        <div className="px-4 h-14 flex items-center justify-between">
          <div className="flex items-center gap-2">
            <div className="w-7 h-7 bg-emp-primary rounded-lg flex items-center justify-center">
              <span className="text-white font-bold text-xs">A</span>
            </div>
            <span className="font-display font-bold text-gray-900 text-sm">All in One &amp; Network Solutions</span>
          </div>
          <div className="flex items-center gap-3">
            <NotificationBell />
            <span className="text-xs text-gray-500 hidden sm:inline">{user?.name}</span>
            <button onClick={logout} className="text-xs text-gray-400 hover:text-red-500">
              Logout
            </button>
          </div>
        </div>
      </header>
      <div className="flex flex-1">
        <aside className="hidden lg:flex lg:flex-col w-56 xl:w-64 bg-white border-r border-gray-200">
          <nav className="flex-1 p-4 space-y-1 overflow-y-auto">
            {sidebarLinks.map((link) => {
              const isActive = link.path === '/employee'
                ? location.pathname === '/employee'
                : location.pathname.startsWith(link.path)
              return (
                <Link
                  key={link.path}
                  to={link.path}
                  className={`flex items-center gap-3 px-3 py-2 rounded-lg text-sm font-medium transition-colors ${
                    isActive
                      ? 'bg-emerald-50 text-emerald-700'
                      : 'text-gray-600 hover:bg-gray-100'
                  }`}
                >
                  <span className="text-lg">{link.icon}</span>
                  {link.label}
                </Link>
              )
            })}
          </nav>
          <div className="p-4 border-t border-gray-100">
            <div className="flex items-center justify-between">
              <div className="text-sm">
                <p className="font-medium text-gray-900">{user?.name}</p>
                <p className="text-gray-500 text-xs">{user?.role?.replace(/_/g, ' ')}</p>
              </div>
              <button onClick={logout} className="text-xs text-gray-400 hover:text-red-500">
                Logout
              </button>
            </div>
          </div>
        </aside>
        <main className="flex-1 min-h-[calc(100vh-3.5rem)] pb-20 lg:pb-6">
          <div className="max-w-5xl mx-auto px-4 py-4 lg:px-8 lg:py-6">
            <Outlet />
          </div>
        </main>
      </div>
      <nav className="lg:hidden fixed bottom-0 left-0 right-0 bg-white border-t border-gray-200 z-40">
        <div className="flex justify-around max-w-lg mx-auto">
          {bottomNav.map((item) => (
            <Link
              key={item.path}
              to={item.path}
              className={`flex flex-col items-center py-1.5 px-1.5 text-[10px] leading-tight transition-colors ${
                location.pathname === item.path
                  ? 'text-emp-primary'
                  : 'text-gray-400'
              }`}
            >
              <span className="text-base">{item.icon}</span>
              <span className="mt-0.5">{item.label}</span>
            </Link>
          ))}
        </div>
      </nav>
    </div>
  )
}
