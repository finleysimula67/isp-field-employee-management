import { useEffect, useState } from 'react'
import { getMyAdvances, requestAdvance, getMyBalance } from '../../api/salaryAdvances'
import { getMyPayrollRecords } from '../../api/payroll'
import { getMyEarnings } from '../../api/dailyLogs'
import { useAuth } from '../../contexts/AuthContext'
import Toast from '../../components/Toast'
import Skeleton from '../../components/Skeleton'

const statusColors: Record<string, string> = {
  PENDING: 'badge-pending',
  APPROVED: 'badge-approved',
  REJECTED: 'badge-rejected',
  DISBURSED: 'bg-blue-100 text-blue-700',
  SETTLED: 'bg-gray-100 text-gray-500',
}

const payrollStatusColors: Record<string, string> = {
  DRAFT: 'bg-yellow-100 text-yellow-700',
  CALCULATED: 'bg-blue-100 text-blue-700',
  APPROVED: 'bg-purple-100 text-purple-700',
  PAID: 'bg-green-100 text-green-700',
}

export default function WagesPage() {
  const { user } = useAuth()
  const [advances, setAdvances] = useState<any[]>([])
  const [payrollRecords, setPayrollRecords] = useState<any[]>([])
  const [earnings, setEarnings] = useState<any>(null)
  const [balance, setBalance] = useState<any>(null)
  const [loading, setLoading] = useState(true)
  const [amount, setAmount] = useState('')
  const [reason, setReason] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [toast, setToast] = useState<{ message: string; type: 'success' | 'error' | 'info' } | null>(null)

  const fetchData = () => {
    setLoading(true)
    Promise.all([
      getMyAdvances(),
      getMyPayrollRecords(),
      getMyEarnings(),
      getMyBalance(),
    ])
      .then(([advRes, payrollRes, earnRes, balRes]) => {
        setAdvances(advRes.data)
        setPayrollRecords(payrollRes.data)
        setEarnings(earnRes.data)
        setBalance(balRes.data)
      })
      .catch(() => setToast({ message: 'Failed to load data', type: 'error' }))
      .finally(() => setLoading(false))
  }

  useEffect(() => { fetchData() }, [])

  const totalEarned = earnings ? Number(earnings.totalEarned) : payrollRecords.reduce((sum: number, pr: any) => sum + (pr.netPay || 0), 0)
  const totalDrawn = balance ? Number(balance.totalDrawn) : 0
  const availableForAdvance = balance ? Number(balance.availableForAdvance) : 0
  const netEarned = totalEarned - totalDrawn

  const handleRequest = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!amount || Number(amount) <= 0) { setToast({ message: 'Enter a valid amount', type: 'error' }); return }
    if (Number(amount) > availableForAdvance) { setToast({ message: `Amount exceeds available limit of Rs. ${availableForAdvance.toFixed(2)}`, type: 'error' }); return }
    setSubmitting(true)
    setToast(null)
    try {
      await requestAdvance({ amount: Number(amount), reason, employeeId: user?.id })
      setAmount('')
      setReason('')
      fetchData()
    } catch {
      setToast({ message: 'Failed to submit advance request', type: 'error' })
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div>
      <h1 className="font-display text-xl font-bold text-gray-900 mb-6">Wages & Advances</h1>
      {loading ? (
        <Skeleton variant="card" count={4} />
      ) : (
        <>
          <div className="card p-4 mb-6">
            <h2 className="font-display text-base font-bold text-gray-900 mb-4">Wage Summary</h2>
            <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
              <div className="bg-emerald-50 p-3 rounded-lg">
                <div className="text-xs text-emerald-600 font-medium">Earned Wages</div>
                <div className="text-lg font-bold text-emerald-700">Rs. {totalEarned.toFixed(2)}</div>
                <div className="text-xs text-emerald-500 mt-0.5">
                  {earnings ? `${earnings.approvedDays} day${earnings.approvedDays !== 1 ? 's' : ''} × Rs. ${earnings.dailyRate}/day` : ''}
                </div>
              </div>
              <div className="bg-red-50 p-3 rounded-lg">
                <div className="text-xs text-red-600 font-medium">Advance Taken</div>
                <div className="text-lg font-bold text-red-700">Rs. {totalDrawn.toFixed(2)}</div>
                <div className="text-xs text-red-500 mt-0.5">{advances.filter(a => a.status === 'APPROVED' || a.status === 'DISBURSED').length} advance{advances.filter(a => a.status === 'APPROVED' || a.status === 'DISBURSED').length !== 1 ? 's' : ''}</div>
              </div>
              <div className={netEarned >= 0 ? 'bg-blue-50 p-3 rounded-lg' : 'bg-orange-50 p-3 rounded-lg'}>
                <div className={`text-xs font-medium ${netEarned >= 0 ? 'text-blue-600' : 'text-orange-600'}`}>To Receive</div>
                <div className={`text-lg font-bold ${netEarned >= 0 ? 'text-blue-700' : 'text-orange-700'}`}>
                  Rs. {Math.abs(netEarned).toFixed(2)}
                </div>
                <div className={`text-xs mt-0.5 ${netEarned >= 0 ? 'text-blue-500' : 'text-orange-500'}`}>
                  {netEarned >= 0
                    ? `Rs. ${totalEarned.toFixed(2)} earned − Rs. ${totalDrawn.toFixed(2)} taken`
                    : 'Overdrawn'}
                </div>
              </div>
            </div>
          </div>

          {payrollRecords.length > 0 && (
            <div className="card p-4 mb-6">
              <h2 className="font-display text-base font-bold text-gray-900 mb-4">Monthly Earnings</h2>
              <div className="space-y-2">
                {payrollRecords.map((pr: any) => (
                  <div key={pr.id} className="flex items-center justify-between py-2 border-b border-gray-50">
                    <div>
                      <p className="text-sm font-medium text-gray-900">{pr.periodLabel}</p>
                      <p className="text-xs text-gray-500">{pr.daysWorked} day{pr.daysWorked !== 1 ? 's' : ''} · Rs. {pr.wageRateAtTime}/day</p>
                    </div>
                    <div className="text-right">
                      <p className="text-sm font-bold text-gray-900">Rs. {pr.netPay}</p>
                      <span className={`text-xs px-2 py-0.5 rounded-full ${payrollStatusColors[pr.status] || ''}`}>{pr.status}</span>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}

          <div className="grid grid-cols-1 lg:grid-cols-5 gap-6 mb-6">
            <div className="lg:col-span-3 card p-4">
              <h2 className="font-display text-base font-bold text-gray-900 mb-4">My Advances</h2>
              {advances.length === 0 ? (
                <p className="text-gray-400 text-sm text-center py-4">No advances requested</p>
              ) : (
                <div className="space-y-2">
                  {advances.map((adv: any) => (
                    <div key={adv.id} className="flex items-center justify-between py-2 border-b border-gray-50">
                      <div>
                        <p className="text-sm font-medium text-gray-900">Rs. {Number(adv.amount).toFixed(2)}</p>
                        <p className="text-xs text-gray-500">{adv.requestDate?.slice(0, 10) || '—'}{adv.reason ? ` • ${adv.reason}` : ''}</p>
                      </div>
                      <span className={statusColors[adv.status] || 'badge-pending'}>{adv.status}</span>
                    </div>
                  ))}
                </div>
              )}
            </div>
            <div className="lg:col-span-2 card p-4">
              <h2 className="font-display text-base font-bold text-gray-900 mb-4">Request Advance</h2>
              <div className={`text-xs font-medium mb-3 ${availableForAdvance > 0 ? 'text-emerald-600' : 'text-red-600'}`}>
                {availableForAdvance > 0
                  ? `Available for advance: Rs. ${availableForAdvance.toFixed(2)}`
                  : 'No advance available — earned wages fully consumed'}
              </div>
              <Toast message={toast?.message || ''} type={toast?.type || 'info'} visible={!!toast} onClose={() => setToast(null)} />
              <form onSubmit={handleRequest} className="space-y-4">
                <div>
                  <label className="text-xs font-medium text-gray-500 block mb-1">Amount (Rs.)</label>
                  <input type="number" min="0" max={availableForAdvance > 0 ? availableForAdvance : 0} step="0.01" value={amount} onChange={e => setAmount(e.target.value)} className="input-field w-full" required />
                </div>
                <div>
                  <label className="text-xs font-medium text-gray-500 block mb-1">Reason</label>
                  <textarea value={reason} onChange={e => setReason(e.target.value)} className="input-field w-full" rows={3} />
                </div>
                <button type="submit" disabled={submitting || availableForAdvance <= 0} className="btn-primary w-full">{submitting ? 'Submitting...' : 'Request Advance'}</button>
              </form>
            </div>
          </div>
        </>
      )}
    </div>
  )
}
