import { useEffect, useState } from 'react'
import { getPayrollRecords, calculatePayroll, approvePayroll, markPaid, batchCalculatePayroll } from '../../api/payroll'
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

  const fetchData = () => {
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
  }

  useEffect(() => { fetchData() }, [])

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
            <select value={filterEmployee} onChange={e => { setFilterEmployee(e.target.value); fetchData() }} className="input-field">
              <option value="">All</option>
              {employees.map((emp: any) => (
                <option key={emp.id} value={emp.id}>{emp.name}</option>
              ))}
            </select>
          </div>
          <button onClick={fetchData} className="btn-admin">Filter</button>
        </div>
      </div>
      <div className="card overflow-hidden">
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b border-gray-100">
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
              <tr><td colSpan={7} className="py-8 text-center text-gray-400">No records found</td></tr>
            ) : records.map((rec: any) => (
              <tr key={rec.id} className="border-b border-gray-50 hover:bg-gray-50">
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
    </div>
  )
}
