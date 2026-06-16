import { useState, useRef, useEffect } from 'react'
import { Link, Outlet, useLocation } from 'react-router-dom'
import { useAuth } from '../contexts/AuthContext'
import NotificationBell from '../components/NotificationBell'
import OfflineIndicator from '../components/OfflineIndicator'

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
  { path: '/admin/manual-logs', label: 'Manual Logs', icon: '📝' },
  { path: '/admin/cash-collections', label: 'Cash Collections', icon: '💵' },
  { path: '/admin/profile', label: 'Profile', icon: '👤' },
]

export default function AdminLayout() {
  const location = useLocation()
  const { user, logout } = useAuth()
  const [sidebarOpen, setSidebarOpen] = useState(false)
  const sidebarRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    const handleClick = (e: MouseEvent) => {
      if (sidebarRef.current && !sidebarRef.current.contains(e.target as Node)) {
        setSidebarOpen(false)
      }
    }
    document.addEventListener('mousedown', handleClick)
    return () => document.removeEventListener('mousedown', handleClick)
  }, [])

  return (
    <div className="min-h-screen flex flex-col bg-gray-50">
      <OfflineIndicator />

      {/* Mobile header */}
      <div className="lg:hidden flex items-center justify-between px-4 py-3 bg-white border-b border-gray-200">
        <button onClick={() => setSidebarOpen(true)} className="p-2 -ml-2 rounded-md hover:bg-gray-100">
          <svg className="w-6 h-6 text-gray-700" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6h16M4 12h16M4 18h16" />
          </svg>
        </button>
        <div className="flex items-center gap-2">
          <div className="w-7 h-7 bg-admin-primary rounded-lg flex items-center justify-center">
            <span className="text-white font-bold text-xs">A</span>
          </div>
          <span className="font-display font-bold text-gray-900 text-sm">All in One</span>
        </div>
        <div className="flex items-center gap-2">
          <Link to="/admin/profile" className="text-lg hover:opacity-70" title="Profile">👤</Link>
          <NotificationBell />
        </div>
      </div>

      <div className="flex flex-1">
        {/* Mobile overlay */}
        {sidebarOpen && (
          <div className="lg:hidden fixed inset-0 bg-black/40 z-40" onClick={() => setSidebarOpen(false)} />
        )}

        {/* Sidebar */}
        <aside
          ref={sidebarRef}
          className={`${sidebarOpen ? 'translate-x-0' : '-translate-x-full'} lg:translate-x-0 fixed lg:static inset-y-0 left-0 z-50 w-64 bg-white border-r border-gray-200 flex flex-col transition-transform duration-200`}
        >
          <div className="p-4 border-b border-gray-100 flex items-center">
            <Link to="/admin" className="flex items-center gap-2 flex-1">
              <div className="w-8 h-8 bg-admin-primary rounded-lg flex items-center justify-center">
                <span className="text-white font-bold text-sm">A</span>
              </div>
              <span className="font-display font-bold text-gray-900">All in One &amp; Network Solutions</span>
            </Link>
            <button onClick={() => setSidebarOpen(false)} className="lg:hidden p-1 -mr-1 rounded-md hover:bg-gray-100">
              <svg className="w-5 h-5 text-gray-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
              </svg>
            </button>
          </div>
          <nav className="flex-1 p-3 space-y-1 overflow-y-auto">
            {sidebarLinks.map((link) => (
              <Link
                key={link.path}
                to={link.path}
                onClick={() => setSidebarOpen(false)}
                className={`flex items-center gap-2 px-3 py-2 rounded-md text-sm font-medium transition-colors ${
                  location.pathname === link.path
                    ? 'bg-admin-primary/10 text-admin-primary'
                    : 'text-gray-600 hover:bg-gray-100'
                }`}
              >
                <span className="text-lg">{link.icon}</span>
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

        <main className="flex-1 overflow-auto pb-20 lg:pb-0">
          <div className="p-6">
            <div className="hidden lg:flex items-center justify-between mb-2">
              <div></div>
              <div className="flex items-center gap-2">
                <Link to="/admin/profile" className="text-lg hover:opacity-70" title="Profile">👤</Link>
                <NotificationBell />
              </div>
            </div>
            <Outlet />
          </div>
        </main>
      </div>

      {/* Mobile bottom nav */}
      <nav className="lg:hidden fixed bottom-0 left-0 right-0 bg-white border-t border-gray-200 z-40">
        <div className="flex w-full max-w-lg mx-auto">
          {[{ path: '/admin', label: 'Dashboard', icon: '📊' },
            { path: '/admin/employees', label: 'Employees', icon: '👥' },
            { path: '/admin/daily-logs', label: 'Logs', icon: '📋' },
            { path: '/admin/leave-requests', label: 'Leave', icon: '📝' },
            { path: '/admin/cash-collections', label: 'Cash', icon: '💵' },
            { path: '/admin/tasks', label: 'Tasks', icon: '✅' },
            { path: '/admin/payroll', label: 'Payroll', icon: '💰' }].map((link) => (
            <Link
              key={link.path}
              to={link.path}
              onClick={() => setSidebarOpen(false)}
              className={`flex flex-col items-center justify-center py-1.5 px-1 text-[10px] leading-tight flex-1 min-w-0 ${
                location.pathname === link.path ? 'text-admin-primary' : 'text-gray-400'
              }`}
            >
              <span className="text-lg leading-none">{link.icon}</span>
              <span className="mt-0.5 truncate max-w-full px-0.5 leading-none">{link.label}</span>
            </Link>
          ))}
        </div>
      </nav>
    </div>
  )
}
