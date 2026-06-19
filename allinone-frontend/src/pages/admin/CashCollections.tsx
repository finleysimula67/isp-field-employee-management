import { useEffect, useState, useCallback, useMemo } from 'react'
import { getCashCollections, getCashCollectionSummary, reviewCashCollection, batchReviewCashCollections, createCashCollectionAdmin, deleteCashCollection } from '../../api/cashCollections'
import { getEmployees } from '../../api/employees'
import Toast from '../../components/Toast'
import Skeleton from '../../components/Skeleton'
import DeleteConfirmModal from '../../components/DeleteConfirmModal'

const statusColors: Record<string, string> = {
  PENDING: 'badge-pending',
  APPROVED: 'badge-approved',
  REJECTED: 'badge-rejected',
  NEEDS_REVISION: 'badge-revision',
}

const paymentMethods = ['CASH', 'MOBILE_MONEY', 'BANK_TRANSFER']
const serviceTypes = ['NEW_CONNECTION', 'INSTALLATION', 'MAINTENANCE', 'REPAIR', 'OTHER']

export default function CashCollectionsPage() {
  const now = new Date()
  const [tab, setTab] = useState<'grid' | 'list'>('list')
  const [month, setMonth] = useState(now.getMonth() + 1)
  const [year, setYear] = useState(now.getFullYear())
  const [summary, setSummary] = useState<any[]>([])
  const [collections, setCollections] = useState<any[]>([])
  const [employees, setEmployees] = useState<any[]>([])
  const [loading, setLoading] = useState(true)
  const [filterStatus, setFilterStatus] = useState('')
  const [filterEmployee, setFilterEmployee] = useState('')
  const [reviewingId, setReviewingId] = useState<number | null>(null)
  const [reviewForm, setReviewForm] = useState({ status: 'APPROVED', reviewComment: '', amount: '', customerName: '', customerPhone: '', customerAddress: '', description: '', paymentMethod: '', serviceType: '' })
  const [toast, setToast] = useState<{ message: string; type: 'success' | 'error' | 'info' } | null>(null)
  const [selectedIds, setSelectedIds] = useState<number[]>([])
  const [batchAction, setBatchAction] = useState('APPROVED')
  const [batchProcessing, setBatchProcessing] = useState(false)
  const [showAddModal, setShowAddModal] = useState(false)
  const [addForm, setAddForm] = useState({ employeeId: '', customerName: '', customerPhone: '', customerAddress: '', amount: '', paymentMethod: '', serviceType: '', description: '' })
  const [addSubmitting, setAddSubmitting] = useState(false)
  const [confirmDelete, setConfirmDelete] = useState<number | null>(null)

  const daysInMonth = new Date(year, month, 0).getDate()
  const dayHeaders = Array.from({ length: daysInMonth }, (_, i) => i + 1)
  const monthNames = ['', 'Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec']

  const fetchSummary = useCallback(() => {
    setLoading(true)
    getCashCollectionSummary(month, year)
      .then(res => setSummary(res.data || []))
      .catch(() => setToast({ message: 'Failed to load summary', type: 'error' }))
      .finally(() => setLoading(false))
  }, [month, year])

  const fetchList = useCallback(() => {
    setLoading(true)
    const params: any = {}
    if (filterStatus) params.status = filterStatus
    if (filterEmployee) params.employeeId = Number(filterEmployee)
    Promise.all([getCashCollections(params), getEmployees()])
      .then(([colRes, empRes]) => {
        setCollections(colRes.data)
        setEmployees(empRes.data)
      })
      .catch(() => setToast({ message: 'Failed to load data', type: 'error' }))
      .finally(() => setLoading(false))
  }, [filterStatus, filterEmployee])

  useEffect(() => {
    if (tab === 'grid') fetchSummary()
    else fetchList()
  }, [tab, fetchSummary, fetchList])

  const empMap = useMemo(() => new Map(employees.map((e: any) => [e.id, e])), [employees])

  const handleReview = async (id: number) => {
    try {
      const payload: any = { status: reviewForm.status, reviewComment: reviewForm.reviewComment }
      if (reviewForm.amount) payload.amount = Number(reviewForm.amount)
      if (reviewForm.customerName) payload.customerName = reviewForm.customerName
      if (reviewForm.customerPhone) payload.customerPhone = reviewForm.customerPhone
      if (reviewForm.customerAddress) payload.customerAddress = reviewForm.customerAddress
      if (reviewForm.description) payload.description = reviewForm.description
      if (reviewForm.paymentMethod) payload.paymentMethod = reviewForm.paymentMethod
      if (reviewForm.serviceType) payload.serviceType = reviewForm.serviceType
      await reviewCashCollection(id, payload)
      setReviewingId(null)
      setReviewForm({ status: 'APPROVED', reviewComment: '', amount: '', customerName: '', customerPhone: '', customerAddress: '', description: '', paymentMethod: '', serviceType: '' })
      setToast({ message: 'Review submitted', type: 'success' })
      fetchList()
    } catch {
      setToast({ message: 'Failed to submit review', type: 'error' })
    }
  }

  const startReview = (c: any) => {
    setReviewingId(c.id)
    setReviewForm({
      status: 'APPROVED',
      reviewComment: '',
      amount: String(c.amount || ''),
      customerName: c.customerName || '',
      customerPhone: c.customerPhone || '',
      customerAddress: c.customerAddress || '',
      description: c.description || '',
      paymentMethod: c.paymentMethod || '',
      serviceType: c.serviceType || '',
    })
  }

  const cancelReview = () => {
    setReviewingId(null)
    setReviewForm({ status: 'APPROVED', reviewComment: '', amount: '', customerName: '', customerPhone: '', customerAddress: '', description: '', paymentMethod: '', serviceType: '' })
  }

  const handleDelete = async (id: number) => {
    try {
      await deleteCashCollection(id)
      setConfirmDelete(null)
      setToast({ message: 'Cash collection deleted', type: 'success' })
      fetchList()
    } catch {
      setToast({ message: 'Failed to delete', type: 'error' })
    }
  }

  const toggleSelect = (id: number) => {
    setSelectedIds(prev => prev.includes(id) ? prev.filter(i => i !== id) : [...prev, id])
  }

  const toggleSelectAll = () => {
    if (selectedIds.length === collections.length) setSelectedIds([])
    else setSelectedIds(collections.map(c => c.id))
  }

  const handleBatchReview = async () => {
    if (selectedIds.length === 0) return
    setBatchProcessing(true)
    try {
      await batchReviewCashCollections({ ids: selectedIds, status: batchAction })
      setToast({ message: `${selectedIds.length} collection(s) ${batchAction.toLowerCase()}`, type: 'success' })
      setSelectedIds([])
      fetchList()
    } catch {
      setToast({ message: 'Batch review failed', type: 'error' })
    } finally { setBatchProcessing(false) }
  }

  const handleManualAdd = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!addForm.employeeId || !addForm.customerName || !addForm.amount || !addForm.paymentMethod || !addForm.serviceType) {
      setToast({ message: 'Employee, customer, amount, payment, and service are required', type: 'error' }); return
    }
    setAddSubmitting(true)
    try {
      await createCashCollectionAdmin({
        employeeId: Number(addForm.employeeId),
        customerName: addForm.customerName,
        customerPhone: addForm.customerPhone || null,
        customerAddress: addForm.customerAddress || null,
        amount: Number(addForm.amount),
        paymentMethod: addForm.paymentMethod,
        serviceType: addForm.serviceType,
        description: addForm.description || null,
      })
      setShowAddModal(false)
      setAddForm({ employeeId: '', customerName: '', customerPhone: '', customerAddress: '', amount: '', paymentMethod: '', serviceType: '', description: '' })
      setToast({ message: 'Cash collection recorded', type: 'success' })
      if (tab === 'grid') fetchSummary()
      else fetchList()
    } catch (err: any) {
      setToast({ message: err?.response?.data?.message || 'Failed to add collection', type: 'error' })
    } finally { setAddSubmitting(false) }
  }

  const grandTotalCollected = summary.reduce((sum: number, e: any) => sum + (e.totalCollected || 0), 0)
  const grandTotalPending = summary.reduce((sum: number, e: any) => sum + (e.totalPending || 0), 0)
  const grandTotalSubmitted = summary.reduce((sum: number, e: any) => sum + (e.totalSubmitted || 0), 0)

  return (
    <div>
      <Toast message={toast?.message || ''} type={toast?.type || 'info'} visible={!!toast} onClose={() => setToast(null)} />
      <div className="flex flex-wrap items-center justify-between gap-4 mb-6">
        <h1 className="font-display text-2xl font-bold text-gray-900">Cash Collections</h1>
        <div className="flex gap-2 items-center">
          <button onClick={() => setShowAddModal(true)} className="btn-primary text-sm">+ Manual Add</button>
        </div>
      </div>

      <div className="flex gap-2 mb-4">
        <button onClick={() => setTab('grid')} className={`px-4 py-2 rounded-lg text-sm font-medium transition-colors ${tab === 'grid' ? 'bg-brand-50 text-brand-700 border border-brand-200' : 'text-gray-500 hover:text-gray-700 border border-transparent'}`}>Monthly Grid</button>
        <button onClick={() => setTab('list')} className={`px-4 py-2 rounded-lg text-sm font-medium transition-colors ${tab === 'list' ? 'bg-brand-50 text-brand-700 border border-brand-200' : 'text-gray-500 hover:text-gray-700 border border-transparent'}`}>List View</button>
      </div>

      {tab === 'grid' ? (
        <>
          <div className="flex flex-wrap items-center justify-between gap-4 mb-4">
            <div className="flex gap-2 items-center">
              <select value={month} onChange={e => { setMonth(Number(e.target.value)); fetchSummary() }} className="input-field w-auto">
                {monthNames.slice(1).map((name, i) => (<option key={i + 1} value={i + 1}>{name}</option>))}
              </select>
              <select value={year} onChange={e => { setYear(Number(e.target.value)); fetchSummary() }} className="input-field w-auto">
                {[2025, 2026, 2027].map(y => (<option key={y} value={y}>{y}</option>))}
              </select>
            </div>
          </div>

          <div className="flex flex-wrap gap-3 mb-4 text-xs">
            <span className="flex items-center gap-1"><span className="w-3 h-3 rounded bg-green-100 border border-green-300" /> Approved</span>
            <span className="flex items-center gap-1"><span className="w-3 h-3 rounded bg-yellow-100 border border-yellow-300" /> Pending</span>
            <span className="flex items-center gap-1"><span className="w-3 h-3 rounded bg-red-100 border border-red-300" /> Rejected</span>
          </div>

          {loading ? (
            <Skeleton variant="table-row" count={6} />
          ) : summary.length === 0 ? (
            <p className="text-gray-400 text-sm">No collections this month</p>
          ) : (
            <>
              <div className="overflow-x-auto mb-6">
                <table className="w-full text-xs border-collapse">
                  <thead>
                    <tr>
                      <th className="sticky left-0 bg-white z-10 text-left py-2 pr-3 font-medium text-gray-500 min-w-[140px]">Employee</th>
                      <th className="text-left py-2 pr-3 font-medium text-gray-500 min-w-[100px]">Collected</th>
                      <th className="text-left py-2 pr-3 font-medium text-gray-500 min-w-[100px]">Pending</th>
                      {dayHeaders.map(d => (
                        <th key={d} className="w-8 text-center py-2 font-medium text-gray-400">{d}</th>
                      ))}
                    </tr>
                  </thead>
                  <tbody>
                    {summary.map((emp: any) => (
                      <tr key={emp.employeeId} className="border-t border-gray-100 hover:bg-gray-50">
                        <td className="sticky left-0 bg-white py-2 pr-3 font-medium text-gray-800 whitespace-nowrap">{emp.employeeName}</td>
                        <td className="py-2 pr-3 text-green-700 font-medium whitespace-nowrap">Rs. {Number(emp.totalCollected || 0).toLocaleString()}</td>
                        <td className="py-2 pr-3 text-yellow-700 font-medium whitespace-nowrap">Rs. {Number(emp.totalPending || 0).toLocaleString()}</td>
                        {dayHeaders.map(d => {
                          const entries = emp.days?.[d]
                          const hasApproved = entries?.some((e: any) => e.status === 'APPROVED')
                          const hasPending = entries?.some((e: any) => e.status === 'PENDING')
                          const hasRejected = entries?.some((e: any) => e.status === 'REJECTED')
                          const total = entries?.reduce((s: number, e: any) => s + (e.amount || 0), 0) || 0
                          let cellClass = 'bg-gray-50 text-gray-300'
                          let label = '—'
                          if (hasApproved) { cellClass = 'bg-green-100 text-green-700'; label = '✓' }
                          else if (hasPending) { cellClass = 'bg-yellow-100 text-yellow-700'; label = '⏳' }
                          else if (hasRejected) { cellClass = 'bg-red-100 text-red-700'; label = '✗' }
                          return (
                            <td key={d} className="p-0.5 text-center">
                              <div className={`inline-flex flex-col items-center justify-center w-8 py-1 rounded text-[10px] font-medium ${cellClass}`} title={entries?.map((e: any) => `${e.customerName}: Rs. ${e.amount} (${e.status})`).join(', ')}>
                                <span>{label}</span>
                                {total > 0 && <span className="text-[8px] leading-tight">{total >= 1000 ? `Rs.${(total/1000).toFixed(1)}k` : `Rs.${total}`}</span>}
                              </div>
                            </td>
                          )
                        })}
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>

              <div className="card overflow-hidden">
                <div className="p-4 border-b border-gray-100">
                  <h2 className="font-display text-lg font-bold text-gray-900">Collection Summary — {monthNames[month]} {year}</h2>
                </div>
                <table className="w-full text-sm">
                  <thead>
                    <tr className="border-b border-gray-100">
                      <th className="text-left py-3 px-4 font-medium text-gray-500">Employee</th>
                      <th className="text-left py-3 px-4 font-medium text-gray-500">Approved</th>
                      <th className="text-left py-3 px-4 font-medium text-gray-500">Pending</th>
                      <th className="text-left py-3 px-4 font-medium text-gray-500">Total Submitted</th>
                      <th className="text-left py-3 px-4 font-medium text-gray-500">Entries</th>
                    </tr>
                  </thead>
                  <tbody>
                    {summary.map((emp: any) => (
                      <tr key={emp.employeeId} className="border-b border-gray-50 hover:bg-gray-50">
                        <td className="py-3 px-4 font-medium">{emp.employeeName}</td>
                        <td className="py-3 px-4 text-green-700 font-medium">Rs. {Number(emp.totalCollected || 0).toLocaleString()}</td>
                        <td className="py-3 px-4 text-yellow-700 font-medium">Rs. {Number(emp.totalPending || 0).toLocaleString()}</td>
                        <td className="py-3 px-4 font-semibold">Rs. {Number(emp.totalSubmitted || 0).toLocaleString()}</td>
                        <td className="py-3 px-4 text-gray-600">{emp.approvedCount || 0} approved / {emp.pendingCount || 0} pending</td>
                      </tr>
                    ))}
                    <tr className="bg-gray-50 font-semibold">
                      <td className="py-3 px-4">Total</td>
                      <td className="py-3 px-4 text-green-700">Rs. {grandTotalCollected.toLocaleString()}</td>
                      <td className="py-3 px-4 text-yellow-700">Rs. {grandTotalPending.toLocaleString()}</td>
                      <td className="py-3 px-4">Rs. {grandTotalSubmitted.toLocaleString()}</td>
                      <td className="py-3 px-4">—</td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </>
          )}
        </>
      ) : (
        <>
          <div className="card p-4 mb-6">
            <div className="flex flex-wrap gap-3 items-end">
              <div className="flex-1 min-w-[120px]">
                <label className="text-xs font-medium text-gray-500 block mb-1">Status</label>
                <select value={filterStatus} onChange={e => setFilterStatus(e.target.value)} className="input-field w-full">
                  <option value="">All</option>
                  <option value="PENDING">Pending</option>
                  <option value="APPROVED">Approved</option>
                  <option value="REJECTED">Rejected</option>
                  <option value="NEEDS_REVISION">Needs Revision</option>
                </select>
              </div>
              <div className="flex-1 min-w-[140px]">
                <label className="text-xs font-medium text-gray-500 block mb-1">Employee</label>
                <select value={filterEmployee} onChange={e => setFilterEmployee(e.target.value)} className="input-field w-full">
                  <option value="">All</option>
                  {employees.map((e: any) => (<option key={e.id} value={e.id}>{e.name}</option>))}
                </select>
              </div>
              <button onClick={fetchList} className="btn-admin">Filter</button>
            </div>
          </div>
          {selectedIds.length > 0 && (
            <div className="card p-3 mb-4 flex flex-wrap items-center gap-3 bg-blue-50 border-blue-200">
              <span className="text-sm font-medium text-blue-800">{selectedIds.length} selected</span>
              <select value={batchAction} onChange={e => setBatchAction(e.target.value)} className="input-field text-xs w-auto">
                <option value="APPROVED">Approve</option>
                <option value="REJECTED">Reject</option>
                <option value="NEEDS_REVISION">Needs Revision</option>
              </select>
              <button onClick={handleBatchReview} disabled={batchProcessing} className="btn-admin text-xs">
                {batchProcessing ? 'Processing...' : `Apply ${batchAction}`}
              </button>
              <button onClick={() => setSelectedIds([])} className="btn-ghost text-xs">Clear</button>
            </div>
          )}
          <div className="card overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b border-gray-100">
                  <th className="text-left py-3 px-4 w-10"><input type="checkbox" onChange={toggleSelectAll} checked={selectedIds.length === collections.length && collections.length > 0} className="rounded" /></th>
                  <th className="text-left py-3 px-4 font-medium text-gray-500">Employee</th>
                  <th className="text-left py-3 px-4 font-medium text-gray-500">Customer</th>
                  <th className="text-left py-3 px-4 font-medium text-gray-500">Amount</th>
                  <th className="text-left py-3 px-4 font-medium text-gray-500">Payment</th>
                  <th className="text-left py-3 px-4 font-medium text-gray-500">Service</th>
                  <th className="text-left py-3 px-4 font-medium text-gray-500">Status</th>
                  <th className="text-left py-3 px-4 font-medium text-gray-500">Actions</th>
                </tr>
              </thead>
              <tbody>
                {loading ? (
                  <Skeleton variant="table-row" count={5} />
                ) : collections.length === 0 ? (
                  <tr><td colSpan={8} className="py-8 text-center text-gray-400">Nothing here yet</td></tr>
                ) : collections.map((c: any) => (
                  <tr key={c.id} className="border-b border-gray-50 hover:bg-gray-50">
                    <td className="py-3 px-4"><input type="checkbox" checked={selectedIds.includes(c.id)} onChange={() => toggleSelect(c.id)} className="rounded" /></td>
                    <td className="py-3 px-4 font-medium">{empMap.get(c.employeeId)?.name || `#${c.employeeId}`}</td>
                    <td className="py-3 px-4">
                      <div className="flex flex-col">
                        <span className="font-medium text-gray-900">{c.customerName}</span>
                        {c.customerPhone && <span className="text-xs text-gray-400">{c.customerPhone}</span>}
                      </div>
                    </td>
                    <td className="py-3 px-4 font-medium">Rs. {Number(c.amount).toLocaleString()}</td>
                    <td className="py-3 px-4 text-gray-500">{c.paymentMethod?.replace(/_/g, ' ')}</td>
                    <td className="py-3 px-4 text-gray-500">{c.serviceType?.replace(/_/g, ' ')}</td>
                    <td className="py-3 px-4"><span className={statusColors[c.status] || 'badge-pending'}>{c.status?.replace(/_/g, ' ')}</span></td>
                    <td className="py-3 px-4">
                      <div className="flex gap-2 items-center">
                        {c.status === 'PENDING' && (
                          <button onClick={() => startReview(c)} className="btn-ghost text-xs">Review</button>
                        )}
                        {c.status === 'PENDING' && (
                          <button onClick={() => setConfirmDelete(c.id)} className="text-xs text-red-500 hover:text-red-700">Delete</button>
                        )}
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </>
      )}

      {/* Review modal */}
      {reviewingId != null && (
        <div className="fixed inset-0 bg-black/40 z-50 flex items-center justify-center p-4" onClick={cancelReview}>
          <div className="bg-white rounded-xl max-w-lg w-full max-h-[90vh] overflow-y-auto p-6" onClick={e => e.stopPropagation()}>
            <h2 className="font-display text-lg font-bold text-gray-900 mb-4">Review Cash Collection</h2>
            <div className="space-y-4">
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div className="sm:col-span-2">
                  <label className="text-xs font-medium text-gray-500 block mb-1">Customer Name</label>
                  <input value={reviewForm.customerName} onChange={e => setReviewForm(f => ({ ...f, customerName: e.target.value }))} className="input-field w-full text-sm" />
                </div>
                <div>
                  <label className="text-xs font-medium text-gray-500 block mb-1">Customer Phone</label>
                  <input value={reviewForm.customerPhone} onChange={e => setReviewForm(f => ({ ...f, customerPhone: e.target.value }))} className="input-field w-full text-sm" />
                </div>
                <div>
                  <label className="text-xs font-medium text-gray-500 block mb-1">Customer Address</label>
                  <input value={reviewForm.customerAddress} onChange={e => setReviewForm(f => ({ ...f, customerAddress: e.target.value }))} className="input-field w-full text-sm" />
                </div>
                <div>
                  <label className="text-xs font-medium text-gray-500 block mb-1">Amount (Rs.)</label>
                  <input type="number" step="0.01" min="0" value={reviewForm.amount} onChange={e => setReviewForm(f => ({ ...f, amount: e.target.value }))} className="input-field w-full text-sm" />
                </div>
                <div>
                  <label className="text-xs font-medium text-gray-500 block mb-1">Payment Method</label>
                  <select value={reviewForm.paymentMethod} onChange={e => setReviewForm(f => ({ ...f, paymentMethod: e.target.value }))} className="input-field w-full text-sm">
                    <option value="">No change</option>
                    {paymentMethods.map(m => (<option key={m} value={m}>{m.replace(/_/g, ' ')}</option>))}
                  </select>
                </div>
                <div>
                  <label className="text-xs font-medium text-gray-500 block mb-1">Service Type</label>
                  <select value={reviewForm.serviceType} onChange={e => setReviewForm(f => ({ ...f, serviceType: e.target.value }))} className="input-field w-full text-sm">
                    <option value="">No change</option>
                    {serviceTypes.map(t => (<option key={t} value={t}>{t.replace(/_/g, ' ')}</option>))}
                  </select>
                </div>
              </div>
              <div>
                <label className="text-xs font-medium text-gray-500 block mb-1">Description</label>
                <textarea value={reviewForm.description} onChange={e => setReviewForm(f => ({ ...f, description: e.target.value }))} className="input-field w-full text-sm" rows={2} />
              </div>
              <div className="border-t border-gray-100 pt-4">
                <label className="text-xs font-medium text-gray-500 block mb-1">Review Action *</label>
                <select value={reviewForm.status} onChange={e => setReviewForm(f => ({ ...f, status: e.target.value }))} className="input-field w-full text-sm mb-2">
                  <option value="APPROVED">Approve</option>
                  <option value="REJECTED">Reject</option>
                  <option value="NEEDS_REVISION">Needs Revision</option>
                </select>
                <label className="text-xs font-medium text-gray-500 block mb-1">Review Comment</label>
                <textarea value={reviewForm.reviewComment} onChange={e => setReviewForm(f => ({ ...f, reviewComment: e.target.value }))} placeholder="Optional comment" className="input-field w-full text-sm" rows={2} />
              </div>
              <div className="flex gap-3 justify-end pt-2">
                <button onClick={cancelReview} className="btn-ghost">Cancel</button>
                <button onClick={() => handleReview(reviewingId)} className="btn-primary">Submit Review</button>
              </div>
            </div>
          </div>
        </div>
      )}

      <DeleteConfirmModal open={confirmDelete !== null} title="Delete Cash Collection?" message="This collection will be soft-deleted and moved to the Recycle Bin. This action can be undone." onConfirm={() => { if (confirmDelete !== null) handleDelete(confirmDelete) }} onCancel={() => setConfirmDelete(null)} />

      {showAddModal && (
        <div className="fixed inset-0 bg-black/40 z-50 flex items-center justify-center p-4" onClick={() => setShowAddModal(false)}>
          <div className="bg-white rounded-xl max-w-lg w-full max-h-[90vh] overflow-y-auto p-6" onClick={e => e.stopPropagation()}>
            <h2 className="font-display text-lg font-bold text-gray-900 mb-4">Manual Cash Collection</h2>
            <form onSubmit={handleManualAdd} className="space-y-4">
              <div>
                <label className="text-xs font-medium text-gray-500 block mb-1">Employee *</label>
                <select value={addForm.employeeId} onChange={e => setAddForm(f => ({ ...f, employeeId: e.target.value }))} className="input-field w-full" required>
                  <option value="">Select employee</option>
                  {employees.map((e: any) => (
                    <option key={e.id} value={e.id}>{e.name}</option>
                  ))}
                </select>
              </div>
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div className="sm:col-span-2">
                  <label className="text-xs font-medium text-gray-500 block mb-1">Customer Name *</label>
                  <input value={addForm.customerName} onChange={e => setAddForm(f => ({ ...f, customerName: e.target.value }))} className="input-field w-full" required />
                </div>
                <div>
                  <label className="text-xs font-medium text-gray-500 block mb-1">Customer Phone</label>
                  <input value={addForm.customerPhone} onChange={e => setAddForm(f => ({ ...f, customerPhone: e.target.value }))} className="input-field w-full" />
                </div>
                <div>
                  <label className="text-xs font-medium text-gray-500 block mb-1">Customer Address</label>
                  <input value={addForm.customerAddress} onChange={e => setAddForm(f => ({ ...f, customerAddress: e.target.value }))} className="input-field w-full" />
                </div>
                <div>
                  <label className="text-xs font-medium text-gray-500 block mb-1">Amount (Rs.) *</label>
                  <input type="number" step="0.01" min="0" value={addForm.amount} onChange={e => setAddForm(f => ({ ...f, amount: e.target.value }))} className="input-field w-full" required />
                </div>
                <div>
                  <label className="text-xs font-medium text-gray-500 block mb-1">Payment Method *</label>
                  <select value={addForm.paymentMethod} onChange={e => setAddForm(f => ({ ...f, paymentMethod: e.target.value }))} className="input-field w-full" required>
                    <option value="">Select</option>
                    {paymentMethods.map(m => (<option key={m} value={m}>{m.replace(/_/g, ' ')}</option>))}
                  </select>
                </div>
                <div>
                  <label className="text-xs font-medium text-gray-500 block mb-1">Service Type *</label>
                  <select value={addForm.serviceType} onChange={e => setAddForm(f => ({ ...f, serviceType: e.target.value }))} className="input-field w-full" required>
                    <option value="">Select</option>
                    {serviceTypes.map(t => (<option key={t} value={t}>{t.replace(/_/g, ' ')}</option>))}
                  </select>
                </div>
              </div>
              <div>
                <label className="text-xs font-medium text-gray-500 block mb-1">Description / Notes</label>
                <textarea value={addForm.description} onChange={e => setAddForm(f => ({ ...f, description: e.target.value }))} className="input-field w-full" rows={2} />
              </div>
              <div className="flex gap-3 justify-end">
                <button type="button" onClick={() => setShowAddModal(false)} className="btn-ghost">Cancel</button>
                <button type="submit" disabled={addSubmitting} className="btn-primary">{addSubmitting ? 'Saving...' : 'Save Collection'}</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  )
}
