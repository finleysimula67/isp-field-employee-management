import { useEffect } from 'react'
import { Link, Outlet, useLocation } from 'react-router-dom'
import { useAuth } from '../contexts/AuthContext'
import NotificationBell from '../components/NotificationBell'
import OfflineIndicator from '../components/OfflineIndicator'
import { processQueue } from '../services/syncService'
import { useOnlineStatus } from '../hooks/useOnlineStatus'

const sidebarLinks = [
  { path: '/admin', label: 'Dashboard', icon: '📊' },
  { path: '/admin/employees', label: 'Employees', icon: '👥' },
  { path: '/admin/branches', label: 'Branches', icon: '🏢' },
  { path: '/admin/attendance', label: 'Attendance', icon: '📅' },
  { path: '/admin/daily-logs', label: 'Daily Logs', icon: '📋' },
  { path: '/admin/leave-requests', label: 'Leave Requests', icon: '📝' },
  { path: '/admin/tasks', label: 'Tasks', icon: '✅' },
  { path: '/admin/payroll', label: 'Payroll', icon: '💰' },
  { path: '/admin/salary-advances', label: 'Advances', icon: '💳' },
  { path: '/admin/reports', label: 'Reports', icon: '📊' },
  { path: '/admin/holidays', label: 'Holidays', icon: '🎉' },
  { path: '/admin/audit-logs', label: 'Audit Logs', icon: '📋' },
  { path: '/admin/monthly-lockouts', label: 'Lockouts', icon: '🔒' },
  { path: '/admin/email-allow-list', label: 'Allow List', icon: '📧' },
  { path: '/admin/profile', label: 'Profile', icon: '👤' },
]

export default function AdminLayout() {
  const location = useLocation()
  const { user, logout } = useAuth()
  const isOnline = useOnlineStatus()

  useEffect(() => {
    if (isOnline) processQueue()
  }, [isOnline])

  return (
    <div className="min-h-screen flex flex-col bg-gray-50">
      <OfflineIndicator />
      <div className="flex flex-1">
        <aside className="w-64 bg-white border-r border-gray-200 flex flex-col">
        <div className="p-4 border-b border-gray-100">
          <Link to="/admin" className="flex items-center gap-2">
            <div className="w-8 h-8 bg-admin-primary rounded-lg flex items-center justify-center">
              <span className="text-white font-bold text-sm">A</span>
            </div>
            <span className="font-display font-bold text-gray-900">All in One &amp; Network Solutions</span>
          </Link>
        </div>
        <nav className="flex-1 p-3 space-y-1 overflow-y-auto">
          {sidebarLinks.map((link) => (
            <Link
              key={link.path}
              to={link.path}
              className={`flex items-center gap-3 px-3 py-2 rounded-md text-sm font-medium transition-colors ${
                location.pathname === link.path
                  ? 'bg-admin-primary/10 text-admin-primary'
                  : 'text-gray-600 hover:bg-gray-100'
              }`}
            >
              <span>{link.icon}</span>
              {link.label}
            </Link>
          ))}
        </nav>
        <div className="p-4 border-t border-gray-100">
          <div className="flex items-center justify-between">
            <div className="text-sm">
              <p className="font-medium text-gray-900">{user?.name}</p>
              <p className="text-gray-500 text-xs">{user?.role}</p>
            </div>
            <button onClick={logout} className="text-sm text-gray-500 hover:text-red-600">
              Logout
            </button>
          </div>
        </div>
      </aside>
      <main className="flex-1 overflow-auto">
        <div className="p-6">
          <div className="flex items-center justify-between mb-2">
            <div></div>
            <NotificationBell />
          </div>
          <Outlet />
        </div>
      </main>
      </div>
    </div>
  )
}
