import { useEffect, useState } from 'react'
import { getMonthlyAttendance, getWageSummary } from '../../api/attendance'
import Toast from '../../components/Toast'
import Skeleton from '../../components/Skeleton'

const STATUS_COLORS: Record<string, string> = {
  PRESENT: 'bg-green-100 text-green-700',
  PENDING: 'bg-yellow-100 text-yellow-700',
  ABSENT: 'bg-red-100 text-red-700',
  ON_LEAVE: 'bg-blue-100 text-blue-700',
  HOLIDAY: 'bg-purple-100 text-purple-700',
}
const STATUS_LABELS: Record<string, string> = {
  PRESENT: 'P',
  PENDING: 'Pd',
  ABSENT: 'A',
  ON_LEAVE: 'L',
  HOLIDAY: 'H',
}

export default function AttendancePage() {
  const now = new Date()
  const [month, setMonth] = useState(now.getMonth() + 1)
  const [year, setYear] = useState(now.getFullYear())
  const [attendance, setAttendance] = useState<any[]>([])
  const [wages, setWages] = useState<any[]>([])
  const [loading, setLoading] = useState(true)
  const [toast, setToast] = useState<{ message: string; type: 'success' | 'error' | 'info' } | null>(null)

  const daysInMonth = new Date(year, month, 0).getDate()
  const dayHeaders = Array.from({ length: daysInMonth }, (_, i) => i + 1)

  const fetchData = () => {
    setLoading(true)
    Promise.all([
      getMonthlyAttendance(month, year),
      getWageSummary(month, year),
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
        <h1 className="font-display text-2xl font-bold text-gray-900">Attendance</h1>
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
        <Skeleton variant="table-row" count={6} />
      ) : attendance.length === 0 ? (
        <p className="text-gray-400 text-sm">No data</p>
      ) : (
        <div className="overflow-x-auto mb-8">
          <table className="w-full text-xs border-collapse">
            <thead>
              <tr>
                <th className="sticky left-0 bg-white z-10 text-left py-2 pr-3 font-medium text-gray-500 min-w-[120px]">Employee</th>
                <th className="text-left py-2 pr-3 font-medium text-gray-500">Stats</th>
                {dayHeaders.map(d => (
                  <th key={d} className="w-7 text-center py-2 font-medium text-gray-400">{d}</th>
                ))}
              </tr>
            </thead>
            <tbody>
              {attendance.map((emp: any) => {
                const s = emp.stats
                return (
                  <tr key={emp.employeeId} className="border-t border-gray-100">
                    <td className="sticky left-0 bg-white py-2 pr-3 font-medium text-gray-800 whitespace-nowrap">{emp.employeeName}</td>
                    <td className="py-2 pr-3 text-gray-400 whitespace-nowrap">
                      ✅{s.present} 🟡{s.pending} ❌{s.absent} 🏖️{s.onLeave}
                    </td>
                    {dayHeaders.map(d => {
                      const dateStr = `${year}-${String(month).padStart(2, '0')}-${String(d).padStart(2, '0')}`
                      const status = emp.days?.[dateStr] || 'ABSENT'
                      return (
                        <td key={d} className="p-0.5 text-center">
                          <span className={`inline-flex items-center justify-center w-6 h-6 rounded text-[10px] font-bold ${STATUS_COLORS[status] || 'bg-gray-100 text-gray-400'}`}>
                            {STATUS_LABELS[status] || '?'}
                          </span>
                        </td>
                      )
                    })}
                  </tr>
                )
              })}
            </tbody>
          </table>
        </div>
      )}

      <div className="card overflow-hidden">
        <div className="p-4 border-b border-gray-100">
          <h2 className="font-display text-lg font-bold text-gray-900">Wages Summary — {monthNames[month]} {year}</h2>
        </div>
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b border-gray-100">
              <th className="text-left py-3 px-4 font-medium text-gray-500">Employee</th>
              <th className="text-left py-3 px-4 font-medium text-gray-500">Present</th>
              <th className="text-left py-3 px-4 font-medium text-gray-500">Absent</th>
              <th className="text-left py-3 px-4 font-medium text-gray-500">Daily Rate</th>
              <th className="text-left py-3 px-4 font-medium text-gray-500">Total Earned</th>
            </tr>
          </thead>
          <tbody>
            {loading ? (
              <Skeleton variant="table-row" count={4} />
            ) : wages.length === 0 ? (
              <tr><td colSpan={5} className="py-8 text-center text-gray-400">No data</td></tr>
            ) : wages.map((w: any) => (
              <tr key={w.employeeId} className="border-b border-gray-50 hover:bg-gray-50">
                <td className="py-3 px-4 font-medium">{w.employeeName}</td>
                <td className="py-3 px-4 text-gray-600">{w.presentDays}</td>
                <td className="py-3 px-4 text-gray-600">{w.absentDays}</td>
                <td className="py-3 px-4 text-gray-600">Rs. {Number(w.dailyRate).toLocaleString()}</td>
                <td className="py-3 px-4 font-semibold text-green-700">Rs. {Number(w.totalEarned).toLocaleString()}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  )
}
