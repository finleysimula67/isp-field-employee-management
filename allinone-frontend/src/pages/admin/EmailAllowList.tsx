import { useEffect, useState } from 'react'
import { getAllowList, addToAllowList, removeFromAllowList } from '../../api/emailAllowList'
import Toast from '../../components/Toast'
import Skeleton from '../../components/Skeleton'

export default function EmailAllowListPage() {
  const [entries, setEntries] = useState<any[]>([])
  const [loading, setLoading] = useState(true)
  const [toast, setToast] = useState<{ message: string; type: 'success' | 'error' | 'info' } | null>(null)
  const [email, setEmail] = useState('')
  const [submitting, setSubmitting] = useState(false)

  const fetchData = () => {
    setLoading(true)
    getAllowList().then((res) => {
      setEntries(res.data || [])
      setLoading(false)
    }).catch(() => { setToast({ message: 'Failed to load allow list', type: 'error' }); setLoading(false) })
  }

  useEffect(() => { fetchData() }, [])

  const handleAdd = async (e: React.FormEvent) => {
    e.preventDefault()
    const trimmed = email.trim()
    if (!trimmed) { setToast({ message: 'Enter an email address', type: 'error' }); return }
    setSubmitting(true)
    setToast(null)
    try {
      await addToAllowList(trimmed)
      setEmail('')
      fetchData()
    } catch { setToast({ message: 'Failed to add email', type: 'error' }) }
    finally { setSubmitting(false) }
  }

  const handleRemove = async (id: number, entryEmail: string) => {
    if (!window.confirm(`Remove ${entryEmail} from allow list?`)) return
    try {
      await removeFromAllowList(id)
      fetchData()
    } catch { setToast({ message: 'Failed to remove email', type: 'error' }) }
  }

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h1 className="font-display text-2xl font-bold text-gray-900">Email Allow List</h1>
      </div>
      <Toast message={toast?.message || ''} type={toast?.type || 'info'} visible={!!toast} onClose={() => setToast(null)} />
      <div className="card p-6 mb-6">
        <h2 className="font-display text-lg font-bold text-gray-900 mb-4">Add Email</h2>
        <form onSubmit={handleAdd} className="flex gap-3 items-end">
          <div className="flex-1">
            <label className="text-xs font-medium text-gray-500 block mb-1">Email</label>
            <input type="email" value={email} onChange={e => setEmail(e.target.value)} className="input-field w-full" placeholder="newuser@example.com" required />
          </div>
          <button type="submit" disabled={submitting} className="btn-admin">{submitting ? 'Adding...' : 'Add to Allow List'}</button>
        </form>
      </div>
      <div className="card overflow-hidden">
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b border-gray-100">
              <th className="text-left py-3 px-4 font-medium text-gray-500">Email</th>
              <th className="text-left py-3 px-4 font-medium text-gray-500">Added By</th>
              <th className="text-left py-3 px-4 font-medium text-gray-500">Added At</th>
              <th className="text-left py-3 px-4 font-medium text-gray-500">Actions</th>
            </tr>
          </thead>
          <tbody>
            {loading ? (
              <Skeleton variant="table-row" count={5} />
            ) : entries.length === 0 ? (
              <tr><td colSpan={4} className="py-8 text-center text-gray-400">No entries in allow list</td></tr>
            ) : entries.map((e: any) => (
              <tr key={e.id} className="border-b border-gray-50 hover:bg-gray-50">
                <td className="py-3 px-4 font-medium">{e.email}</td>
                <td className="py-3 px-4 text-gray-500">{e.addedBy?.name || '—'}</td>
                <td className="py-3 px-4 text-gray-500">{e.createdAt ? new Date(e.createdAt).toLocaleDateString() : '—'}</td>
                <td className="py-3 px-4">
                  <button onClick={() => handleRemove(e.id, e.email)} className="text-xs text-red-500 hover:text-red-700">Remove</button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  )
}
