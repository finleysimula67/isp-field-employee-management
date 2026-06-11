import { useEffect, useState } from 'react'
import { getAdminStats } from '../../api/dashboard'
import Toast from '../../components/Toast'
import Skeleton from '../../components/Skeleton'

export default function AdminDashboard() {
  const [stats, setStats] = useState<any>(null)
  const [loading, setLoading] = useState(true)
  const [toast, setToast] = useState<{ message: string; type: 'success' | 'error' | 'info' } | null>(null)

  useEffect(() => {
    getAdminStats().then((res) => {
      setStats(res.data)
      setLoading(false)
    }).catch(() => { setToast({ message: 'Failed to load dashboard data', type: 'error' }); setLoading(false) })
  }, [])

  if (loading) {
    return (
      <div>
        <Toast message={toast?.message || ''} type={toast?.type || 'info'} visible={!!toast} onClose={() => setToast(null)} />
        <h1 className="font-display text-2xl font-bold text-gray-900 mb-6">Dashboard</h1>
        <Skeleton variant="text" count={3} />
      </div>
    )
  }

  const cards = [
    { label: 'Total Employees', value: stats?.totalEmployees ?? 0, color: 'bg-blue-50 text-blue-700' },
    { label: 'Pending Logs', value: stats?.pendingLogs ?? 0, color: 'bg-yellow-50 text-yellow-700' },
    { label: 'Pending Leave Requests', value: stats?.pendingLeaveRequests ?? 0, color: 'bg-orange-50 text-orange-700' },
    { label: 'Open Tasks', value: stats?.openTasks ?? 0, color: 'bg-purple-50 text-purple-700' },
    { label: 'Branches', value: stats?.totalBranches ?? 0, color: 'bg-green-50 text-green-700' },
  ]

  return (
    <div>
      <Toast message={toast?.message || ''} type={toast?.type || 'info'} visible={!!toast} onClose={() => setToast(null)} />
      <h1 className="font-display text-2xl font-bold text-gray-900 mb-6">Dashboard</h1>
      <div className="grid grid-cols-1 md:grid-cols-3 lg:grid-cols-5 gap-6">
        {cards.map((stat) => (
          <div key={stat.label} className={`card ${stat.color}`}>
            <div className="text-3xl font-bold mb-1">{stat.value}</div>
            <div className="text-sm opacity-80">{stat.label}</div>
          </div>
        ))}
      </div>
    </div>
  )
}
