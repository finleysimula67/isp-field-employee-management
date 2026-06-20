import { useEffect, useState, useCallback } from 'react'
import { getAdvances, reviewAdvance, manualAdvance, getBalanceForEmployee, deleteSalaryAdvance, batchDeleteSalaryAdvances } from '../../api/salaryAdvances'
import { getEmployees } from '../../api/employees'
import Toast from '../../components/Toast'
import Skeleton from '../../components/Skeleton'
import DeleteConfirmModal from '../../components/DeleteConfirmModal'

const statusColors: Record<string, string> = {
  PENDING: 'badge-pending',
  APPROVED: 'badge-approved',
  REJECTED: 'badge-rejected',
  DISBURSED: 'bg-blue-100 text-blue-700',
  SETTLED: 'bg-gray-100 text-gray-500',
}

export default function SalaryAdvancesPage() {
  const [advances, setAdvances] = useState<any[]>([])
  const [employees, setEmployees] = useState<any[]>([])
  const [loading, setLoading] = useState(true)
  const [filterStatus, setFilterStatus] = useState('')
  const [reviewingId, setReviewingId] = useState<number | null>(null)
  const [reviewForm, setReviewForm] = useState({ status: 'APPROVED', notes: '' })
  const [toast, setToast] = useState<{ message: string; type: 'success' | 'error' | 'info' } | null>(null)
  const [showManual, setShowManual] = useState(false)
  const [manualForm, setManualForm] = useState({ employeeId: '', amount: '', reason: '' })
  const [manualSubmitting, setManualSubmitting] = useState(false)
  const [employeeBalance, setEmployeeBalance] = useState<any>(null)
  const [confirmDeleteId, setConfirmDeleteId] = useState<number | null>(null)
  const [deleting, setDeleting] = useState(false)
  const [selectedIds, setSelectedIds] = useState<number[]>([])
  const [confirmBatchDelete, setConfirmBatchDelete] = useState(false)

  const fetchData = useCallback(() => {
    setLoading(true)
    const params: any = {}
    if (filterStatus) params.status = filterStatus
    Promise.all([getAdvances(params), getEmployees()])
      .then(([advRes, empRes]) => {
        setAdvances(advRes.data)
        setEmployees(empRes.data)
      })
      .catch(() => setToast({ message: 'Failed to load data', type: 'error' }))
      .finally(() => setLoading(false))
  }, [filterStatus])

  useEffect(() => { fetchData() }, [fetchData])

  const empMap = new Map(employees.map((e: any) => [e.id, e]))

  const handleReview = async (id: number) => {
    try {
      await reviewAdvance(id, reviewForm)
      setReviewingId(null)
      setReviewForm({ status: 'APPROVED', notes: '' })
      fetchData()
    } catch {
      setToast({ message: 'Failed to submit review', type: 'error' })
    }
  }

  const handleDelete = async () => {
    if (confirmDeleteId === null) return
    setDeleting(true)
    try {
      await deleteSalaryAdvance(confirmDeleteId)
      setConfirmDeleteId(null)
      setToast({ message: 'Salary advance deleted', type: 'success' })
      fetchData()
    } catch {
      setToast({ message: 'Failed to delete', type: 'error' })
    } finally {
      setDeleting(false)
    }
  }

  useEffect(() => {
    if (manualForm.employeeId) {
      getBalanceForEmployee(Number(manualForm.employeeId))
        .then(res => setEmployeeBalance(res.data))
        .catch(() => setEmployeeBalance(null))
    } else {
      setEmployeeBalance(null)
    }
  }, [manualForm.employeeId])

  const handleManualSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!manualForm.employeeId || !manualForm.amount) { setToast({ message: 'Select employee and enter amount', type: 'error' }); return }
    const amount = Number(manualForm.amount)
    const available = employeeBalance ? Number(employeeBalance.availableForAdvance) : 0
    if (amount > available) { setToast({ message: `Amount Rs. ${amount.toFixed(2)} exceeds available limit of Rs. ${available.toFixed(2)}`, type: 'error' }); return }
    setManualSubmitting(true)
    setToast(null)
    try {
      await manualAdvance({ employeeId: Number(manualForm.employeeId), amount, reason: manualForm.reason })
      setManualForm({ employeeId: '', amount: '', reason: '' })
      setShowManual(false)
      setEmployeeBalance(null)
      fetchData()
    } catch {
      setToast({ message: 'Failed to record advance', type: 'error' })
    } finally {
      setManualSubmitting(false)
    }
  }

  const toggleSelect = (id: number) => {
    setSelectedIds(prev => prev.includes(id) ? prev.filter(i => i !== id) : [...prev, id])
  }

  const toggleSelectAll = () => {
    if (selectedIds.length === advances.length) setSelectedIds([])
    else setSelectedIds(advances.map(a => a.id))
  }

  const handleBatchDelete = async () => {
    if (selectedIds.length === 0) return
    setDeleting(true)
    try {
      await batchDeleteSalaryAdvances({ ids: selectedIds })
      setConfirmBatchDelete(false)
      setSelectedIds([])
      setToast({ message: `${selectedIds.length} advance(s) deleted`, type: 'success' })
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
        <h1 className="font-display text-2xl font-bold text-gray-900">Salary Advances</h1>
        <button onClick={() => setShowManual(!showManual)} className="btn-admin text-sm">
          {showManual ? 'Cancel' : '+ Manual Advance'}
        </button>
      </div>
      {showManual && (
        <div className="card p-4 mb-6">
          <h2 className="font-display text-base font-bold text-gray-900 mb-4">Record Manual Advance</h2>
          <form onSubmit={handleManualSubmit} className="grid grid-cols-1 md:grid-cols-4 gap-4">
            <div>
              <label className="text-xs font-medium text-gray-500 block mb-1">Employee</label>
              <select value={manualForm.employeeId} onChange={e => setManualForm(f => ({ ...f, employeeId: e.target.value }))} className="input-field w-full" required>
                <option value="">Select</option>
                {employees.filter(e => e.isActive).map((emp: any) => (
                  <option key={emp.id} value={emp.id}>{emp.name}</option>
                ))}
              </select>
              {employeeBalance && (
                <p className="text-xs mt-1 text-gray-500">
                  Earned: Rs. {Number(employeeBalance.totalEarned).toFixed(2)} |
                  Advanced: Rs. {Number(employeeBalance.totalAdvanced).toFixed(2)} |
                  Available: <span className="font-semibold text-emerald-600">Rs. {Number(employeeBalance.availableForAdvance).toFixed(2)}</span>
                </p>
              )}
            </div>
            <div>
              <label className="text-xs font-medium text-gray-500 block mb-1">Amount (Rs.)</label>
              <input type="number" min="0" max={employeeBalance ? Number(employeeBalance.availableForAdvance) : 0} step="0.01" value={manualForm.amount} onChange={e => setManualForm(f => ({ ...f, amount: e.target.value }))} className="input-field w-full" required />
            </div>
            <div>
              <label className="text-xs font-medium text-gray-500 block mb-1">Reason</label>
              <input value={manualForm.reason} onChange={e => setManualForm(f => ({ ...f, reason: e.target.value }))} className="input-field w-full" />
            </div>
            <div className="flex items-end">
              <button type="submit" disabled={manualSubmitting} className="btn-admin w-full">
                {manualSubmitting ? 'Recording...' : 'Record Advance'}
              </button>
            </div>
          </form>
        </div>
      )}
      <div className="card p-4 mb-6">
        <div className="flex gap-4 items-end">
          <div>
            <label className="text-xs font-medium text-gray-500 block mb-1">Status</label>
            <select value={filterStatus} onChange={e => setFilterStatus(e.target.value)} className="input-field">
              <option value="">All</option>
              <option value="PENDING">Pending</option>
              <option value="APPROVED">Approved</option>
              <option value="REJECTED">Rejected</option>
              <option value="DISBURSED">Disbursed</option>
              <option value="SETTLED">Settled</option>
            </select>
          </div>
          <button onClick={fetchData} className="btn-admin">Filter</button>
        </div>
      </div>
      <Toast message={toast?.message || ''} type={toast?.type || 'info'} visible={!!toast} onClose={() => setToast(null)} />
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
                <input type="checkbox" onChange={toggleSelectAll} checked={selectedIds.length === advances.length && advances.length > 0} className="rounded" />
              </th>
              <th className="text-left py-3 px-4 font-medium text-gray-500">Employee</th>
              <th className="text-left py-3 px-4 font-medium text-gray-500">Amount</th>
              <th className="text-left py-3 px-4 font-medium text-gray-500">Date</th>
              <th className="text-left py-3 px-4 font-medium text-gray-500">Reason</th>
              <th className="text-left py-3 px-4 font-medium text-gray-500">Status</th>
              <th className="text-left py-3 px-4 font-medium text-gray-500">Settled In</th>
              <th className="text-left py-3 px-4 font-medium text-gray-500">Actions</th>
            </tr>
          </thead>
          <tbody>
            {loading ? (
              <Skeleton variant="table-row" count={5} />
            ) : advances.length === 0 ? (
              <tr><td colSpan={8} className="py-8 text-center text-gray-400">No advances found</td></tr>
            ) : advances.map((adv: any) => (
              <tr key={adv.id} className="border-b border-gray-50 hover:bg-gray-50">
                <td className="py-3 px-4">
                  <input type="checkbox" checked={selectedIds.includes(adv.id)} onChange={() => toggleSelect(adv.id)} className="rounded" />
                </td>
                <td className="py-3 px-4 font-medium">{empMap.get(adv.employeeId)?.name || `#${adv.employeeId}`}</td>
                <td className="py-3 px-4 text-gray-500">Rs. {Number(adv.amount).toFixed(2)}</td>
                <td className="py-3 px-4 text-gray-500">{adv.requestDate?.slice(0, 10) || '—'}</td>
                <td className="py-3 px-4 text-gray-500 max-w-[200px] truncate">{adv.reason || '—'}</td>
                <td className="py-3 px-4">
                  <span className={statusColors[adv.status] || 'badge-pending'}>{adv.status}</span>
                </td>
                <td className="py-3 px-4 text-gray-500">
                  {adv.settledInPayrollId ? (
                    <span className="text-xs bg-gray-100 text-gray-600 px-2 py-0.5 rounded">Payroll #{adv.settledInPayrollId}</span>
                  ) : adv.isSettled ? (
                    <span className="text-xs text-gray-400">Yes</span>
                  ) : (
                    <span className="text-xs text-gray-400">—</span>
                  )}
                </td>
                <td className="py-3 px-4">
                  {reviewingId === adv.id ? (
                    <div className="flex flex-col gap-2">
                      <select value={reviewForm.status} onChange={e => setReviewForm(f => ({ ...f, status: e.target.value }))} className="input-field text-xs">
                        <option value="APPROVED">Approve</option>
                        <option value="REJECTED">Reject</option>
                      </select>
                      <textarea value={reviewForm.notes} onChange={e => setReviewForm(f => ({ ...f, notes: e.target.value }))} placeholder="Notes" className="input-field text-xs" rows={2} />
                      <div className="flex gap-2">
                        <button onClick={() => handleReview(adv.id)} className="btn-admin text-xs">Submit</button>
                        <button onClick={() => { setReviewingId(null); setReviewForm({ status: 'APPROVED', notes: '' }) }} className="btn-ghost text-xs">Cancel</button>
                      </div>
                    </div>
                  ) : (
                    adv.status === 'PENDING' ? (
                      <div className="flex gap-2">
                        <button onClick={() => setReviewingId(adv.id)} className="btn-ghost text-xs">Review</button>
                        <button onClick={() => setConfirmDeleteId(adv.id)} className="text-xs text-red-500 hover:text-red-700">Delete</button>
                      </div>
                    ) : (
                      <span className="text-gray-400 text-xs">—</span>
                    )
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      <DeleteConfirmModal open={confirmDeleteId !== null} title="Delete Salary Advance?" message="This advance will be soft-deleted and moved to the Recycle Bin. This action can be undone." onConfirm={handleDelete} onCancel={() => setConfirmDeleteId(null)} loading={deleting} />
      <DeleteConfirmModal open={confirmBatchDelete} title={`Delete ${selectedIds.length} Advances?`} message="These salary advances will be soft-deleted and moved to the Recycle Bin." count={selectedIds.length} onConfirm={handleBatchDelete} onCancel={() => setConfirmBatchDelete(false)} loading={deleting} />
    </div>
  )
}