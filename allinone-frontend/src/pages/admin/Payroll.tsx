import { useEffect, useState, useCallback } from 'react'
import { getPayrollRecords, calculatePayroll, approvePayroll, markPaid, batchCalculatePayroll, batchDeletePayroll } from '../../api/payroll'
import { getEmployees } from '../../api/employees'
import Toast from '../../components/Toast'
import Skeleton from '../../components/Skeleton'

const statusColors: Record<string, string> = {
  DRAFT: 'badge-pending',
  CALCULATED: 'bg-blue-100 text-blue-700',
  APPROVED: 'badge-approved',
  PAID: 'bg-green-100 text-green-800',
}

export default function PayrollPage() {
  const [records, setRecords] = useState<any[]>([])
  const [employees, setEmployees] = useState<any[]>([])
  const [loading, setLoading] = useState(true)
  const [periodStart, setPeriodStart] = useState('')
  const [periodEnd, setPeriodEnd] = useState('')
  const [filterPeriod, setFilterPeriod] = useState('')
  const [filterEmployee, setFilterEmployee] = useState('')
  const [calcLoading, setCalcLoading] = useState(false)
  const [batchLoading, setBatchLoading] = useState(false)
  const [toast, setToast] = useState<{ message: string; type: 'success' | 'error' | 'info' } | null>(null)
  const [selectedIds, setSelectedIds] = useState<number[]>([])
  const [confirmBatchDelete, setConfirmBatchDelete] = useState(false)
  const [deleting, setDeleting] = useState(false)
  const [deleteConfirmText, setDeleteConfirmText] = useState('')

  const fetchData = useCallback(() => {
    setLoading(true)
    const params: any = {}
    if (filterPeriod) params.periodLabel = filterPeriod
    if (filterEmployee) params.employeeId = Number(filterEmployee)
    Promise.all([getPayrollRecords(params), getEmployees()])
      .then(([prRes, empRes]) => {
        setRecords(prRes.data)
        setEmployees(empRes.data)
      })
      .catch(() => setToast({ message: 'Failed to load data', type: 'error' }))
      .finally(() => setLoading(false))
  }, [filterPeriod, filterEmployee])

  useEffect(() => { fetchData() }, [fetchData])

  const handleCalculate = async () => {
    if (!periodStart || !periodEnd) return
    setCalcLoading(true)
    try {
      await calculatePayroll({ periodStart, periodEnd })
      setPeriodStart('')
      setPeriodEnd('')
      setToast({ message: 'Payroll calculated', type: 'success' })
      fetchData()
    } catch {
      setToast({ message: 'Failed to calculate payroll', type: 'error' })
    } finally {
      setCalcLoading(false)
    }
  }

  const handleBatchCalculate = async () => {
    if (!periodStart || !periodEnd) return
    setBatchLoading(true)
    try {
      await batchCalculatePayroll({ periodStart, periodEnd })
      setPeriodStart('')
      setPeriodEnd('')
      setToast({ message: 'Batch calculate complete for all active employees', type: 'success' })
      fetchData()
    } catch {
      setToast({ message: 'Batch calculate failed', type: 'error' })
    } finally {
      setBatchLoading(false)
    }
  }

  const handleApprove = async (id: number) => {
    try {
      await approvePayroll(id)
      setToast({ message: 'Payroll approved', type: 'success' })
      fetchData()
    } catch {
      setToast({ message: 'Failed to approve', type: 'error' })
    }
  }

  const handlePay = async (id: number) => {
    try {
      const res = await markPaid(id)
      setToast({ message: res.message || 'Marked as paid — outstanding advances settled', type: 'success' })
      fetchData()
    } catch {
      setToast({ message: 'Failed to mark as paid', type: 'error' })
    }
  }

  const empMap = new Map(employees.map((e: any) => [e.id, e]))

  const toggleSelect = (id: number) => {
    setSelectedIds(prev => prev.includes(id) ? prev.filter(i => i !== id) : [...prev, id])
  }

  const toggleSelectAll = () => {
    if (selectedIds.length === records.length) setSelectedIds([])
    else setSelectedIds(records.map(r => r.id))
  }

  const handleBatchDelete = async () => {
    if (selectedIds.length === 0) return
    setDeleting(true)
    try {
      await batchDeletePayroll({ ids: selectedIds })
      setConfirmBatchDelete(false)
      setSelectedIds([])
      setDeleteConfirmText('')
      setToast({ message: `${selectedIds.length} payroll record(s) deleted`, type: 'success' })
      fetchData()
    } catch {
      setToast({ message: 'Batch delete failed', type: 'error' })
    } finally {
      setDeleting(false)
    }
  }

  return (
    <div>
      <Toast message={toast?.message || ''} type={toast?.type || 'info'} visible={!!toast} onClose={() => setToast(null)} />
      <div className="flex items-center justify-between mb-6">
        <h1 className="font-display text-2xl font-bold text-gray-900">Payroll</h1>
      </div>
      <div className="card p-6 mb-6">
        <h2 className="font-display text-lg font-bold text-gray-900 mb-4">Calculate Payroll</h2>
        <div className="flex flex-wrap gap-4 items-end">
          <div>
            <label className="text-xs font-medium text-gray-500 block mb-1">Period Start</label>
            <input type="date" value={periodStart} onChange={e => setPeriodStart(e.target.value)} className="input-field" />
          </div>
          <div>
            <label className="text-xs font-medium text-gray-500 block mb-1">Period End</label>
            <input type="date" value={periodEnd} onChange={e => setPeriodEnd(e.target.value)} className="input-field" />
          </div>
          <button onClick={handleCalculate} disabled={calcLoading || batchLoading} className="btn-admin">
            {calcLoading ? 'Calculating...' : 'Calculate'}
          </button>
          <button onClick={handleBatchCalculate} disabled={batchLoading || calcLoading} className="btn-admin btn-outline">
            {batchLoading ? 'Calculating All...' : 'Calculate All'}
          </button>
        </div>
      </div>
      <div className="card p-4 mb-6">
        <div className="flex gap-4 items-end">
          <div>
            <label className="text-xs font-medium text-gray-500 block mb-1">Period Label</label>
            <input value={filterPeriod} onChange={e => setFilterPeriod(e.target.value)} placeholder="e.g. 2026-01" className="input-field" />
          </div>
          <div>
            <label className="text-xs font-medium text-gray-500 block mb-1">Employee</label>
            <select value={filterEmployee} onChange={e => setFilterEmployee(e.target.value)} className="input-field">
              <option value="">All</option>
              {employees.map((emp: any) => (
                <option key={emp.id} value={emp.id}>{emp.name}</option>
              ))}
            </select>
          </div>
          <button onClick={fetchData} className="btn-admin">Filter</button>
        </div>
      </div>
      {selectedIds.length > 0 && (
        <div className="card p-3 mb-4 flex flex-wrap items-center gap-3 bg-blue-50 border-blue-200">
          <span className="text-sm font-medium text-blue-800">{selectedIds.length} selected</span>
          <button onClick={() => setSelectedIds([])} className="btn-ghost text-xs">Clear</button>
          <button onClick={() => { setConfirmBatchDelete(true); setDeleteConfirmText('') }} className="text-xs bg-red-50 text-red-600 px-3 py-1.5 rounded-lg hover:bg-red-100 font-medium">Delete Selected</button>
        </div>
      )}
      <div className="card overflow-x-auto">
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b border-gray-100">
              <th className="text-left py-3 px-4 w-10">
                <input type="checkbox" onChange={toggleSelectAll} checked={selectedIds.length === records.length && records.length > 0} className="rounded" />
              </th>
              <th className="text-left py-3 px-4 font-medium text-gray-500">Employee</th>
              <th className="text-left py-3 px-4 font-medium text-gray-500">Period</th>
              <th className="text-left py-3 px-4 font-medium text-gray-500">Days/Hours</th>
              <th className="text-left py-3 px-4 font-medium text-gray-500">Gross Pay</th>
              <th className="text-left py-3 px-4 font-medium text-gray-500">Net Pay</th>
              <th className="text-left py-3 px-4 font-medium text-gray-500">Status</th>
              <th className="text-left py-3 px-4 font-medium text-gray-500">Actions</th>
            </tr>
          </thead>
          <tbody>
            {loading ? (
              <Skeleton variant="table-row" count={5} />
            ) : records.length === 0 ? (
              <tr><td colSpan={8} className="py-8 text-center text-gray-400">No records found</td></tr>
            ) : records.map((rec: any) => (
              <tr key={rec.id} className="border-b border-gray-50 hover:bg-gray-50">
                <td className="py-3 px-4">
                  <input type="checkbox" checked={selectedIds.includes(rec.id)} onChange={() => toggleSelect(rec.id)} className="rounded" />
                </td>
                <td className="py-3 px-4 font-medium">{empMap.get(rec.employeeId)?.name || `#${rec.employeeId}`}</td>
                <td className="py-3 px-4 text-gray-500">{rec.periodLabel}</td>
                <td className="py-3 px-4 text-gray-500">{rec.daysWorked ?? '—'} day{rec.daysWorked !== 1 ? 's' : ''}</td>
                <td className="py-3 px-4 text-gray-500">Rs. {Number(rec.grossPay).toFixed(2)}</td>
                <td className="py-3 px-4 text-gray-500">Rs. {Number(rec.netPay).toFixed(2)}</td>
                <td className="py-3 px-4">
                  <span className={statusColors[rec.status] || 'badge-pending'}>{rec.status}</span>
                </td>
                <td className="py-3 px-4">
                  <div className="flex gap-2">
                    {(rec.status === 'DRAFT' || rec.status === 'CALCULATED') && (
                      <button onClick={() => handleApprove(rec.id)} className="btn-ghost text-xs">Approve</button>
                    )}
                    {rec.status === 'APPROVED' && (
                      <button onClick={() => handlePay(rec.id)} className="btn-ghost text-xs">Mark Paid</button>
                    )}
                    {rec.status === 'PAID' && (
                      <span className="text-gray-400 text-xs">Paid</span>
                    )}
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {/* Batch delete with extra confirmation */}
      {confirmBatchDelete && (
        <div className="fixed inset-0 bg-black/40 z-50 flex items-center justify-center p-4" onClick={() => setConfirmBatchDelete(false)}>
          <div className="bg-white rounded-xl shadow-xl p-6 w-full max-w-md mx-4" onClick={e => e.stopPropagation()}>
            <h3 className="text-lg font-semibold text-gray-900 mb-2">Delete {selectedIds.length} Payroll Records?</h3>
            <p className="text-sm text-gray-600 mb-2">This action will permanently delete payroll records. Type <strong>DELETE</strong> to confirm.</p>
            <p className="text-sm font-medium text-red-600 mb-4">{selectedIds.length} records will be deleted.</p>
            <input
              type="text"
              value={deleteConfirmText}
              onChange={e => setDeleteConfirmText(e.target.value)}
              placeholder='Type "DELETE" to confirm'
              className="input-field w-full mb-4"
            />
            <div className="flex justify-end gap-3">
              <button onClick={() => { setConfirmBatchDelete(false); setDeleteConfirmText('') }} disabled={deleting}
                className="px-4 py-2 text-sm font-medium text-gray-700 bg-gray-100 rounded-lg hover:bg-gray-200 disabled:opacity-50">
                Cancel
              </button>
              <button onClick={handleBatchDelete} disabled={deleting || deleteConfirmText !== 'DELETE'}
                className="px-4 py-2 text-sm font-medium text-white bg-red-600 rounded-lg hover:bg-red-700 disabled:opacity-50 flex items-center gap-2">
                {deleting ? 'Deleting...' : 'Delete'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}