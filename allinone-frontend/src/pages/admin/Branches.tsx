import { useEffect, useState } from 'react'
import { getBranches, createBranch, updateBranch } from '../../api/branches'
import { getEmployees } from '../../api/employees'
import Toast from '../../components/Toast'
import Skeleton from '../../components/Skeleton'

export default function BranchesPage() {
  const [branches, setBranches] = useState<any[]>([])
  const [employees, setEmployees] = useState<any[]>([])
  const [loading, setLoading] = useState(true)
  const [toast, setToast] = useState<{ message: string; type: 'success' | 'error' | 'info' } | null>(null)
  const [showForm, setShowForm] = useState(false)
  const [form, setForm] = useState({ name: '', code: '', address: '', managerId: '' })
  const [editingId, setEditingId] = useState<number | null>(null)
  const [submitting, setSubmitting] = useState(false)

  const fetchData = () => {
    setLoading(true)
    Promise.all([getBranches(), getEmployees()])
      .then(([bRes, eRes]) => {
        setBranches(bRes.data || [])
        setEmployees(eRes.data || [])
        setLoading(false)
      })
      .catch(() => { setToast({ message: 'Failed to load', type: 'error' }); setLoading(false) })
  }

  useEffect(() => { fetchData() }, [])

  const resetForm = () => {
    setForm({ name: '', code: '', address: '', managerId: '' })
    setEditingId(null)
    setShowForm(false)
    setToast(null)
  }

  const handleEdit = (b: any) => {
    setForm({ name: b.name, code: b.code || '', address: b.address || '', managerId: b.managerId ? String(b.managerId) : '' })
    setEditingId(b.id)
    setShowForm(true)
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!form.name) { setToast({ message: 'Name is required', type: 'error' }); return }
    setSubmitting(true)
    setToast(null)
    try {
      if (editingId) {
        await updateBranch(editingId, {
          name: form.name,
          code: form.code || undefined,
          address: form.address || undefined,
          managerId: form.managerId ? Number(form.managerId) : undefined,
        })
        setToast({ message: 'Branch updated', type: 'success' })
      } else {
        await createBranch({
          name: form.name,
          code: form.code || undefined,
          address: form.address || undefined,
          managerId: form.managerId ? Number(form.managerId) : undefined,
        })
        setToast({ message: 'Branch created', type: 'success' })
      }
      resetForm()
      fetchData()
    } catch { setToast({ message: editingId ? 'Failed to update' : 'Failed to create', type: 'error' }) }
    finally { setSubmitting(false) }
  }

  return (
    <div>
      <Toast message={toast?.message || ''} type={toast?.type || 'info'} visible={!!toast} onClose={() => setToast(null)} />
      <div className="flex items-center justify-between mb-6">
        <h1 className="font-display text-2xl font-bold text-gray-900">Branches</h1>
        <button onClick={() => { resetForm(); setShowForm(true) }} className="btn-admin">+ Add Branch</button>
      </div>
      {showForm && (
        <div className="fixed inset-0 bg-black/40 z-50 flex items-center justify-center p-4" onClick={e => { if (e.target === e.currentTarget) resetForm() }}>
          <div className="bg-white rounded-xl shadow-xl w-full max-w-lg p-6" onClick={e => e.stopPropagation()}>
            <h2 className="font-display text-lg font-bold text-gray-900 mb-4">{editingId ? 'Edit Branch' : 'Add Branch'}</h2>
            <form onSubmit={handleSubmit} className="space-y-4">
              <div>
                <label className="text-xs font-medium text-gray-500 block mb-1">Name *</label>
                <input value={form.name} onChange={e => setForm(f => ({ ...f, name: e.target.value }))} className="input-field w-full" required />
              </div>
              <div>
                <label className="text-xs font-medium text-gray-500 block mb-1">Code</label>
                <input value={form.code} onChange={e => setForm(f => ({ ...f, code: e.target.value }))} className="input-field w-full" placeholder="e.g. RLP-MAIN" />
              </div>
              <div>
                <label className="text-xs font-medium text-gray-500 block mb-1">Address</label>
                <input value={form.address} onChange={e => setForm(f => ({ ...f, address: e.target.value }))} className="input-field w-full" placeholder="e.g. Rolpa, Tapla" />
              </div>
              <div>
                <label className="text-xs font-medium text-gray-500 block mb-1">Manager</label>
                <select value={form.managerId} onChange={e => setForm(f => ({ ...f, managerId: e.target.value }))} className="input-field w-full">
                  <option value="">None</option>
                  {employees.filter((e: any) => e.role === 'BRANCH_MANAGER' || e.role === 'SUPER_ADMIN').map((e: any) => (
                    <option key={e.id} value={e.id}>{e.name} ({e.email})</option>
                  ))}
                </select>
              </div>
              <div className="flex justify-end gap-3 pt-2">
                <button type="button" onClick={resetForm} className="btn-secondary">Cancel</button>
                <button type="submit" disabled={submitting} className="btn-admin">{submitting ? 'Saving...' : editingId ? 'Update' : 'Create'}</button>
              </div>
            </form>
          </div>
        </div>
      )}
      <div className="card overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-gray-100">
                <th className="text-left py-3 px-4 font-medium text-gray-500">Name</th>
                <th className="text-left py-3 px-4 font-medium text-gray-500">Code</th>
                <th className="text-left py-3 px-4 font-medium text-gray-500">Address</th>
                <th className="text-left py-3 px-4 font-medium text-gray-500">Manager</th>
                <th className="text-left py-3 px-4 font-medium text-gray-500">Status</th>
                <th className="text-left py-3 px-4 font-medium text-gray-500">Actions</th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <Skeleton variant="table-row" count={5} />
              ) : branches.length === 0 ? (
                <tr><td colSpan={6} className="py-8 text-center text-gray-400">No branches yet</td></tr>
              ) : branches.map((b: any) => (
                <tr key={b.id} className="border-b border-gray-50 hover:bg-gray-50">
                  <td className="py-3 px-4 font-medium">{b.name}</td>
                  <td className="py-3 px-4 text-gray-500">{b.code || '-'}</td>
                  <td className="py-3 px-4 text-gray-500">{b.address || '-'}</td>
                  <td className="py-3 px-4">{b.managerName || '-'}</td>
                  <td className="py-3 px-4">
                    <span className={`badge ${b.isActive ? 'badge-success' : 'badge-danger'}`}>{b.isActive ? 'Active' : 'Inactive'}</span>
                  </td>
                  <td className="py-3 px-4">
                    <button onClick={() => handleEdit(b)} className="text-xs text-blue-600 hover:text-blue-800">Edit</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  )
}
