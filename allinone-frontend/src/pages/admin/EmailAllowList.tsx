import { useEffect, useState } from 'react'
import { getAllowList, addToAllowList, removeFromAllowList, batchDeleteAllowListEntries } from '../../api/emailAllowList'
import Toast from '../../components/Toast'
import Skeleton from '../../components/Skeleton'
import DeleteConfirmModal from '../../components/DeleteConfirmModal'

export default function EmailAllowListPage() {
  const [entries, setEntries] = useState<any[]>([])
  const [loading, setLoading] = useState(true)
  const [toast, setToast] = useState<{ message: string; type: 'success' | 'error' | 'info' } | null>(null)
  const [email, setEmail] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [selectedIds, setSelectedIds] = useState<number[]>([])
  const [confirmBatchDelete, setConfirmBatchDelete] = useState(false)
  const [deleting, setDeleting] = useState(false)

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

  const toggleSelect = (id: number) => {
    setSelectedIds(prev => prev.includes(id) ? prev.filter(i => i !== id) : [...prev, id])
  }

  const toggleSelectAll = () => {
    if (selectedIds.length === entries.length) setSelectedIds([])
    else setSelectedIds(entries.map(e => e.id))
  }

  const handleBatchDelete = async () => {
    if (selectedIds.length === 0) return
    setDeleting(true)
    try {
      await batchDeleteAllowListEntries({ ids: selectedIds })
      setConfirmBatchDelete(false)
      setSelectedIds([])
      setToast({ message: `${selectedIds.length} entry(ies) removed`, type: 'success' })
      fetchData()
    } catch {
      setToast({ message: 'Batch delete failed', type: 'error' })
    } finally {
      setDeleting(false)
    }
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
      {selectedIds.length > 0 && (
        <div className="card p-3 mb-4 flex flex-wrap items-center gap-3 bg-blue-50 border-blue-200">
          <span className="text-sm font-medium text-blue-800">{selectedIds.length} selected</span>
          <button onClick={() => setSelectedIds([])} className="btn-ghost text-xs">Clear</button>
          <button onClick={() => setConfirmBatchDelete(true)} className="text-xs bg-red-50 text-red-600 px-3 py-1.5 rounded-lg hover:bg-red-100 font-medium">Delete Selected</button>
        </div>
      )}
      <div className="card overflow-hidden">
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b border-gray-100">
              <th className="text-left py-3 px-4 w-10">
                <input type="checkbox" onChange={toggleSelectAll} checked={selectedIds.length === entries.length && entries.length > 0} className="rounded" />
              </th>
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
              <tr><td colSpan={5} className="py-8 text-center text-gray-400">No entries in allow list</td></tr>
            ) : entries.map((e: any) => (
              <tr key={e.id} className="border-b border-gray-50 hover:bg-gray-50">
                <td className="py-3 px-4">
                  <input type="checkbox" checked={selectedIds.includes(e.id)} onChange={() => toggleSelect(e.id)} className="rounded" />
                </td>
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
      <DeleteConfirmModal open={confirmBatchDelete} title={`Delete ${selectedIds.length} Entries?`} message="These entries will be removed from the allow list." count={selectedIds.length} onConfirm={handleBatchDelete} onCancel={() => setConfirmBatchDelete(false)} loading={deleting} />
    </div>
  )
}