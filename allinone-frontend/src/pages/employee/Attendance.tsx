import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { getMyMonthlyAttendance, getMyWageSummary } from '../../api/attendance'
import Toast from '../../components/Toast'
import Skeleton from '../../components/Skeleton'

const STATUS_COLORS: Record<string, string> = {
  PRESENT: 'bg-green-100 text-green-700',
  PENDING: 'bg-yellow-100 text-yellow-700',
  ABSENT: 'bg-red-100 text-red-700',
  ON_LEAVE: 'bg-blue-100 text-blue-700',
  HOLIDAY: 'bg-purple-100 text-purple-700',
}

export default function EmployeeAttendance() {
  const now = new Date()
  const [month, setMonth] = useState(now.getMonth() + 1)
  const [year, setYear] = useState(now.getFullYear())
  const [attendance, setAttendance] = useState<any>(null)
  const [wages, setWages] = useState<any>(null)
  const [loading, setLoading] = useState(true)
  const [toast, setToast] = useState<{ message: string; type: 'success' | 'error' | 'info' } | null>(null)

  const daysInMonth = new Date(year, month, 0).getDate()
  const dayHeaders = Array.from({ length: daysInMonth }, (_, i) => i + 1)

  const fetchData = () => {
    setLoading(true)
    Promise.all([
      getMyMonthlyAttendance(month, year),
      getMyWageSummary(month, year),
    ])
      .then(([attRes, wageRes]) => {
        setAttendance(attRes.data)
        setWages(wageRes.data)
      })
      .catch(() => setToast({ message: 'Failed to load attendance', type: 'error' }))
      .finally(() => setLoading(false))
  }

  useEffect(() => { fetchData() }, [month, year])

  const monthNames = ['', 'Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec']

  return (
    <div>
      <Toast message={toast?.message || ''} type={toast?.type || 'info'} visible={!!toast} onClose={() => setToast(null)} />
      <div className="flex flex-wrap items-center justify-between gap-4 mb-6">
        <h1 className="font-display text-2xl font-bold text-gray-900">My Attendance</h1>
        <div className="flex gap-2 items-center">
          <select value={month} onChange={e => setMonth(Number(e.target.value))} className="input-field w-auto">
            {monthNames.slice(1).map((name, i) => (
              <option key={i + 1} value={i + 1}>{name}</option>
            ))}
          </select>
          <select value={year} onChange={e => setYear(Number(e.target.value))} className="input-field w-auto">
            {[2025, 2026, 2027].map(y => (
              <option key={y} value={y}>{y}</option>
            ))}
          </select>
        </div>
      </div>

      <div className="flex flex-wrap gap-3 mb-4 text-xs">
        <span className="flex items-center gap-1"><span className="w-3 h-3 rounded bg-green-100 border border-green-300" /> Present</span>
        <span className="flex items-center gap-1"><span className="w-3 h-3 rounded bg-yellow-100 border border-yellow-300" /> Pending</span>
        <span className="flex items-center gap-1"><span className="w-3 h-3 rounded bg-red-100 border border-red-300" /> Absent</span>
        <span className="flex items-center gap-1"><span className="w-3 h-3 rounded bg-blue-100 border border-blue-300" /> On Leave</span>
        <span className="flex items-center gap-1"><span className="w-3 h-3 rounded bg-purple-100 border border-purple-300" /> Holiday</span>
      </div>

      {loading ? (
        <Skeleton variant="card" count={1} />
      ) : attendance ? (
        <>
          <div className="card p-4 mb-6">
            <div className="flex flex-wrap gap-4 mb-4 text-sm">
              <span className="text-green-700">✅ Present: {attendance.stats?.present || 0}</span>
              <span className="text-yellow-700">🟡 Pending: {attendance.stats?.pending || 0}</span>
              <span className="text-red-700">❌ Absent: {attendance.stats?.absent || 0}</span>
              <span className="text-blue-700">🏖️ Leave: {attendance.stats?.onLeave || 0}</span>
              <span className="text-purple-700">🎉 Holiday: {attendance.stats?.holiday || 0}</span>
            </div>
            
          <div className="overflow-x-auto">
              <div className="grid grid-cols-7 gap-1 min-w-[300px]">
                {['Sun','Mon','Tue','Wed','Thu','Fri','Sat'].map(d => (
                  <div key={d} className="text-center text-[10px] font-medium text-gray-400 py-1">{d}</div>
                ))}
                {Array.from({ length: new Date(year, month - 1, 1).getDay() }, (_, i) => (
                  <div key={`empty-${i}`} />
                ))}
                {dayHeaders.map(d => {
                  const dateStr = `${year}-${String(month).padStart(2, '0')}-${String(d).padStart(2, '0')}`
                  const status = attendance.days?.[dateStr] || 'ABSENT'
                  return (
                    <div key={d} className="text-center">
                      <div className="text-[10px] text-gray-400 mb-0.5">{d}</div>
                      <span className={`inline-flex items-center justify-center w-7 h-7 rounded text-[10px] font-bold ${STATUS_COLORS[status] || 'bg-gray-100 text-gray-400'}`}>
                        {status === 'PRESENT' ? 'P' : status === 'PENDING' ? 'Pd' : status === 'ABSENT' ? 'A' : status === 'ON_LEAVE' ? 'L' : 'H'}
                      </span>
                    </div>
                  )
                })}
              </div>
            </div>
          </div>

          <div className="card overflow-hidden">
            <div className="p-4 border-b border-gray-100">
              <h2 className="font-display text-lg font-bold text-gray-900">My Wages — {monthNames[month]} {year}</h2>
            </div>
            {wages ? (
              <div className="p-4 space-y-3">
                <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
                  <div className="bg-green-50 p-3 rounded-lg text-center">
                    <p className="text-xs text-gray-500">Present</p>
                    <p className="text-lg font-bold text-green-700">{wages.presentDays}</p>
                  </div>
                  <div className="bg-red-50 p-3 rounded-lg text-center">
                    <p className="text-xs text-gray-500">Absent</p>
                    <p className="text-lg font-bold text-red-700">{wages.absentDays}</p>
                  </div>
                  <div className="bg-blue-50 p-3 rounded-lg text-center">
                    <p className="text-xs text-gray-500">Daily Rate</p>
                    <p className="text-lg font-bold text-blue-700">Rs. {Number(wages.dailyRate).toLocaleString()}</p>
                  </div>
                  <div className="bg-emerald-50 p-3 rounded-lg text-center">
                    <p className="text-xs text-gray-500">Total Earned</p>
                    <p className="text-lg font-bold text-emerald-700">Rs. {Number(wages.totalEarned).toLocaleString()}</p>
                  </div>
                </div>
                <div className="text-center">
                  <Link to="/employee/wages" className="text-sm text-brand-600 hover:text-brand-700">
                    View full Wages & Advances
                  </Link>
                </div>
              </div>
            ) : (
              <p className="text-gray-400 text-sm p-4">No wage data</p>
            )}
          </div>
        </>
      ) : (
        <p className="text-gray-400 text-sm">No attendance data</p>
      )}
    </div>
  )
}
