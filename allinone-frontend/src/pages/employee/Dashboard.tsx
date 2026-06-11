import { useEffect, useState } from 'react'
import { getEmployeeStats } from '../../api/dashboard'
import { useAuth } from '../../contexts/AuthContext'
import Toast from '../../components/Toast'
import Skeleton from '../../components/Skeleton'

export default function EmpDashboard() {
  const { user } = useAuth()
  const [stats, setStats] = useState<any>(null)
  const [loading, setLoading] = useState(true)
  const [toast, setToast] = useState<{ message: string; type: 'success' | 'error' | 'info' } | null>(null)

  useEffect(() => {
    getEmployeeStats().then((res) => {
      setStats(res.data)
      setLoading(false)
    }).catch(() => { setToast({ message: 'Failed to load data', type: 'error' }); setLoading(false) })
  }, [])

  if (loading) {
    return (
      <div>
        <Toast message={toast?.message || ''} type={toast?.type || 'info'} visible={!!toast} onClose={() => setToast(null)} />
        <h1 className="font-display text-xl font-bold text-gray-900 mb-1">Hi, {user?.name}</h1>
        <p className="text-gray-500 text-sm mb-6">Here's your day at a glance</p>
        <Skeleton variant="card" count={4} />
      </div>
    )
  }

  const statCards = [
    { label: 'Today', value: stats?.todayLog ? 'Logged' : 'Not Logged', sub: stats?.todayLog ? 'Submitted' : 'No entry yet' },
    { label: 'This Week', value: stats?.weekHours ?? '—', sub: 'hours logged' },
    { label: 'Leave Balance', value: stats?.remainingLeaveDays ?? user?.remainingLeaveDays ?? '—', sub: 'days remaining' },
    { label: 'Pending Tasks', value: stats?.pendingTasks ?? '—', sub: 'tasks assigned' },
  ]

  return (
    <div>
      <Toast message={toast?.message || ''} type={toast?.type || 'info'} visible={!!toast} onClose={() => setToast(null)} />
      <h1 className="font-display text-xl font-bold text-gray-900 mb-1">Hi, {user?.name}</h1>
      <p className="text-gray-500 text-sm mb-6">Here's your day at a glance</p>
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
        {statCards.map((s) => (
          <div key={s.label} className="card">
            <div className="text-2xl font-bold text-emp-primary">{s.value}</div>
            <div className="text-xs text-gray-500 mt-1">{s.label} • {s.sub}</div>
          </div>
        ))}
      </div>
    </div>
  )
}
