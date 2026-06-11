import { useEffect, useState } from 'react'
import { getHolidays, createHoliday, deleteHoliday } from '../../api/holidays'
import Toast from '../../components/Toast'
import Skeleton from '../../components/Skeleton'

export default function HolidaysPage() {
  const [holidays, setHolidays] = useState<any[]>([])
  const [loading, setLoading] = useState(true)
  const [toast, setToast] = useState<{ message: string; type: 'success' | 'error' | 'info' } | null>(null)
  const [form, setForm] = useState({ date: '', name: '', isRecurring: false, isOvertime: false })
  const [submitting, setSubmitting] = useState(false)

  const fetchData = () => {
    setLoading(true)
    getHolidays().then((res) => {
      setHolidays(res.data)
      setLoading(false)
    }).catch(() => { setToast({ message: 'Failed to load holidays', type: 'error' }); setLoading(false) })
  }

  useEffect(() => { fetchData() }, [])

  const handleCreate = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!form.date || !form.name) { setToast({ message: 'Date and name are required', type: 'error' }); return }
    setSubmitting(true)
    setToast(null)
    try {
      await createHoliday(form)
      setForm({ date: '', name: '', isRecurring: false, isOvertime: false })
      fetchData()
    } catch { setToast({ message: 'Failed to create holiday', type: 'error' }) }
    finally { setSubmitting(false) }
  }

  const handleDelete = async (id: number) => {
    if (!window.confirm('Are you sure you want to delete this holiday?')) return
    try {
      await deleteHoliday(id)
      fetchData()
    } catch { setToast({ message: 'Failed to delete holiday', type: 'error' }) }
  }

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h1 className="font-display text-2xl font-bold text-gray-900">Holidays</h1>
      </div>
      <div className="card p-6 mb-6">
        <h2 className="font-display text-lg font-bold text-gray-900 mb-4">Add Holiday</h2>
        <Toast message={toast?.message || ''} type={toast?.type || 'info'} visible={!!toast} onClose={() => setToast(null)} />
        <form onSubmit={handleCreate} className="grid grid-cols-1 md:grid-cols-4 gap-4">
          <div>
            <label className="text-xs font-medium text-gray-500 block mb-1">Date</label>
            <input type="date" value={form.date} onChange={e => setForm(f => ({ ...f, date: e.target.value }))} className="input-field w-full" required />
          </div>
          <div>
            <label className="text-xs font-medium text-gray-500 block mb-1">Name</label>
            <input value={form.name} onChange={e => setForm(f => ({ ...f, name: e.target.value }))} className="input-field w-full" placeholder="e.g. Dashain" required />
          </div>
          <div className="flex items-end gap-4 pb-2">
            <label className="flex items-center gap-2 text-sm text-gray-700">
              <input type="checkbox" checked={form.isRecurring} onChange={e => setForm(f => ({ ...f, isRecurring: e.target.checked }))} />
              Recurring
            </label>
            <label className="flex items-center gap-2 text-sm text-gray-700">
              <input type="checkbox" checked={form.isOvertime} onChange={e => setForm(f => ({ ...f, isOvertime: e.target.checked }))} />
              Overtime
            </label>
          </div>
          <div className="flex items-end">
            <button type="submit" disabled={submitting} className="btn-admin">{submitting ? 'Adding...' : 'Add Holiday'}</button>
          </div>
        </form>
      </div>
      <div className="card overflow-hidden">
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b border-gray-100">
              <th className="text-left py-3 px-4 font-medium text-gray-500">Date</th>
              <th className="text-left py-3 px-4 font-medium text-gray-500">Name</th>
              <th className="text-left py-3 px-4 font-medium text-gray-500">Recurring</th>
              <th className="text-left py-3 px-4 font-medium text-gray-500">Overtime</th>
              <th className="text-left py-3 px-4 font-medium text-gray-500">Actions</th>
            </tr>
          </thead>
          <tbody>
            {loading ? (
              <Skeleton variant="table-row" count={5} />
            ) : holidays.length === 0 ? (
              <tr><td colSpan={5} className="py-8 text-center text-gray-400">Nothing here yet</td></tr>
            ) : holidays.map((h: any) => (
              <tr key={h.id} className="border-b border-gray-50 hover:bg-gray-50">
                <td className="py-3 px-4 font-medium">{h.date}</td>
                <td className="py-3 px-4 text-gray-500">{h.name}</td>
                <td className="py-3 px-4">{h.isRecurring ? 'Yes' : 'No'}</td>
                <td className="py-3 px-4">{h.isOvertime ? 'Yes' : 'No'}</td>
                <td className="py-3 px-4">
                  <button onClick={() => handleDelete(h.id)} className="text-xs text-red-500 hover:text-red-700">Delete</button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  )
}
