import { useEffect, useState } from 'react'
import { getEmployees } from '../../api/employees'
import { getDailyLogs, createDailyLog } from '../../api/dailyLogs'
import Toast from '../../components/Toast'
import Skeleton from '../../components/Skeleton'

const categories = [
  'NEW_FIBER_CONNECTION',
  'SERVICE_MAINTENANCE',
  'WIRE_REPAIR',
  'ROUTER_CONFIGURATION',
  'CLIENT_SUPPORT',
  'OFFICE_DUTY',
]

const initialForm = {
  employeeId: '',
  date: new Date().toISOString().slice(0, 10),
  category: '',
  description: '',
  location: '',
  locationLat: null as number | null,
  locationLng: null as number | null,
  photoUrls: [] as string[],
}

export default function ManualLogsPage() {
  const [employees, setEmployees] = useState<any[]>([])
  const [logs, setLogs] = useState<any[]>([])
  const [loading, setLoading] = useState(true)
  const [form, setForm] = useState(initialForm)
  const [submitting, setSubmitting] = useState(false)
  const [toast, setToast] = useState<{ message: string; type: 'success' | 'error' | 'info' } | null>(null)

  const fetchData = () => {
    setLoading(true)
    Promise.all([getEmployees(), getDailyLogs()])
      .then(([empRes, logRes]) => {
        setEmployees(empRes.data || [])
        setLogs(logRes.data || [])
      })
      .catch(() => setToast({ message: 'Failed to load data', type: 'error' }))
      .finally(() => setLoading(false))
  }

  useEffect(() => { fetchData() }, [])

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!form.employeeId || !form.category || !form.description) {
      setToast({ message: 'Employee, work type, and description are required', type: 'error' })
      return
    }
    setSubmitting(true)
    setToast(null)
    try {
      await createDailyLog({
        employeeId: Number(form.employeeId),
        logDate: form.date,
        category: form.category,
        workDescription: form.description,
        locationDescription: form.location || null,
        locationLat: form.locationLat,
        locationLng: form.locationLng,
      })
      setToast({ message: 'Daily log submitted', type: 'success' })
      setForm(initialForm)
      fetchData()
    } catch {
      setToast({ message: 'Failed to submit daily log', type: 'error' })
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div>
      <Toast message={toast?.message || ''} type={toast?.type || 'info'} visible={!!toast} onClose={() => setToast(null)} />
      <div className="flex items-center justify-between mb-6">
        <h1 className="font-display text-2xl font-bold text-gray-900">Manual Daily Log</h1>
      </div>

      <div className="card p-6 mb-6">
        <h2 className="font-display text-lg font-bold text-gray-900 mb-4">Submit Log on Behalf of Employee</h2>
        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label className="text-xs font-medium text-gray-500 block mb-1">Employee *</label>
              <select
                value={form.employeeId}
                onChange={e => setForm(f => ({ ...f, employeeId: e.target.value }))}
                className="input-field w-full"
                required
              >
                <option value="">Select employee...</option>
                {employees.filter((e: any) => e.isActive).map((emp: any) => (
                  <option key={emp.id} value={emp.id}>{emp.name} ({emp.email})</option>
                ))}
              </select>
            </div>
            <div>
              <label className="text-xs font-medium text-gray-500 block mb-1">Date</label>
              <input
                type="date"
                value={form.date}
                onChange={e => setForm(f => ({ ...f, date: e.target.value }))}
                className="input-field w-full"
                required
              />
            </div>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label className="text-xs font-medium text-gray-500 block mb-1">Work Type *</label>
              <select
                value={form.category}
                onChange={e => setForm(f => ({ ...f, category: e.target.value }))}
                className="input-field w-full"
                required
              >
                <option value="">Select type...</option>
                {categories.map(cat => (
                  <option key={cat} value={cat}>{cat.replace(/_/g, ' ')}</option>
                ))}
              </select>
            </div>
            <div>
              <label className="text-xs font-medium text-gray-500 block mb-1">Location Description</label>
              <input
                value={form.location}
                onChange={e => setForm(f => ({ ...f, location: e.target.value }))}
                className="input-field w-full"
                placeholder="e.g. Mahat, Rolpa"
              />
            </div>
          </div>

          <div>
            <label className="text-xs font-medium text-gray-500 block mb-1">Work Description *</label>
            <textarea
              value={form.description}
              onChange={e => setForm(f => ({ ...f, description: e.target.value }))}
              className="input-field w-full"
              rows={3}
              required
              placeholder="Describe the work done..."
            />
          </div>

          <div className="flex items-end gap-3">
            <button type="submit" disabled={submitting} className="btn-admin">
              {submitting ? 'Submitting...' : 'Submit Daily Log'}
            </button>
          </div>
        </form>
      </div>

      <div className="card overflow-x-auto">
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b border-gray-100">
              <th className="text-left py-3 px-4 font-medium text-gray-500">Employee</th>
              <th className="text-left py-3 px-4 font-medium text-gray-500">Date</th>
              <th className="text-left py-3 px-4 font-medium text-gray-500">Category</th>
              <th className="text-left py-3 px-4 font-medium text-gray-500">Description</th>
              <th className="text-left py-3 px-4 font-medium text-gray-500">Status</th>
              <th className="text-left py-3 px-4 font-medium text-gray-500">Submitted</th>
            </tr>
          </thead>
          <tbody>
            {loading ? (
              <Skeleton variant="table-row" count={5} />
            ) : logs.length === 0 ? (
              <tr><td colSpan={6} className="py-8 text-center text-gray-400">Nothing here yet</td></tr>
            ) : logs.map((log: any) => (
              <tr key={log.id} className="border-b border-gray-50 hover:bg-gray-50">
                <td className="py-3 px-4 font-medium">{log.employeeName}</td>
                <td className="py-3 px-4 text-gray-500">{log.logDate}</td>
                <td className="py-3 px-4 text-gray-500">{log.category?.replace(/_/g, ' ')}</td>
                <td className="py-3 px-4 text-gray-500 max-w-[200px] truncate">{log.workDescription}</td>
                <td className="py-3 px-4">
                  <span className={
                    log.status === 'APPROVED' ? 'badge-approved' :
                    log.status === 'REJECTED' ? 'badge-rejected' :
                    'badge-pending'
                  }>
                    {log.status?.replace(/_/g, ' ')}
                  </span>
                </td>
                <td className="py-3 px-4 text-gray-500 text-xs">
                  {log.submittedAt ? new Date(log.submittedAt).toLocaleDateString() : '—'}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  )
}
