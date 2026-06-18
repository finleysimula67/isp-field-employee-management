import { useEffect, useState, useCallback } from 'react'
import { getMyCashCollections, createCashCollection, getMyCashCollectionSummary } from '../../api/cashCollections'
import { uploadFile } from '../../api/upload'
import { useAuth } from '../../contexts/AuthContext'
import { useOnlineStatus } from '../../hooks/useOnlineStatus'
import { enqueue } from '../../services/offlineQueue'
import Toast from '../../components/Toast'
import Skeleton from '../../components/Skeleton'

const STATUS_COLORS: Record<string, string> = {
  APPROVED: 'bg-green-100 text-green-700',
  PENDING: 'bg-yellow-100 text-yellow-700',
  REJECTED: 'bg-red-100 text-red-700',
  NEEDS_REVISION: 'bg-orange-100 text-orange-700',
}

const paymentMethods = ['CASH', 'MOBILE_MONEY', 'BANK_TRANSFER']
const serviceTypes = ['NEW_CONNECTION', 'INSTALLATION', 'MAINTENANCE', 'REPAIR', 'OTHER']

const initialForm = {
  customerName: '',
  customerPhone: '',
  customerAddress: '',
  amount: '',
  paymentMethod: '',
  serviceType: '',
  description: '',
  location: '',
  locationLat: null as number | null,
  locationLng: null as number | null,
  photoUrls: [] as string[],
}

export default function CashCollectionPage() {
  const { user } = useAuth()
  const isOnline = useOnlineStatus()
  const now = new Date()
  const [month, setMonth] = useState(now.getMonth() + 1)
  const [year, setYear] = useState(now.getFullYear())
  const [summary, setSummary] = useState<any>(null)
  const [collections, setCollections] = useState<any[]>([])
  const [loading, setLoading] = useState(true)
  const [form, setForm] = useState(initialForm)
  const [submitting, setSubmitting] = useState(false)
  const [filterStatus, setFilterStatus] = useState('')
  const [toast, setToast] = useState<{ message: string; type: 'success' | 'error' | 'info' } | null>(null)
  const [photoFile, setPhotoFile] = useState<File | null>(null)
  const [photoPreview, setPhotoPreview] = useState('')
  const [gettingLocation, setGettingLocation] = useState(false)

  const daysInMonth = new Date(year, month, 0).getDate()
  const dayHeaders = Array.from({ length: daysInMonth }, (_, i) => i + 1)
  const monthNames = ['', 'Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec']

  const fetchData = useCallback(() => {
    setLoading(true)
    const errs: string[] = []
    getMyCashCollectionSummary(month, year)
      .then(res => setSummary(res.data))
      .catch((e: any) => {
        errs.push('summary: ' + (e?.response?.data?.message || e?.message || 'unknown'))
        setToast({ message: 'Failed to load summary', type: 'error' })
      })
    getMyCashCollections({})
      .then(res => setCollections(res.data))
      .catch((e: any) => {
        errs.push('list: ' + (e?.response?.data?.message || e?.message || 'unknown'))
      })
      .finally(() => setLoading(false))
  }, [month, year])

  useEffect(() => { fetchData() }, [fetchData])

  const getCurrentLocation = () => {
    if (!navigator.geolocation) { setToast({ message: 'Geolocation not supported', type: 'error' }); return }
    setGettingLocation(true)
    setToast(null)
    navigator.geolocation.getCurrentPosition(
      (pos) => {
        setForm(f => ({ ...f, locationLat: pos.coords.latitude, locationLng: pos.coords.longitude }))
        setGettingLocation(false)
        setToast({ message: 'Location captured', type: 'success' })
      },
      (err) => {
        setGettingLocation(false)
        switch (err.code) {
          case err.PERMISSION_DENIED: setToast({ message: 'Location permission denied', type: 'error' }); break
          case err.POSITION_UNAVAILABLE: setToast({ message: 'Location unavailable', type: 'error' }); break
          case err.TIMEOUT: setToast({ message: 'Location timed out', type: 'error' }); break
          default: setToast({ message: 'Failed to get location', type: 'error' })
        }
      },
      { enableHighAccuracy: true, timeout: 10000, maximumAge: 60000 }
    )
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!form.customerName || !form.amount || !form.paymentMethod || !form.serviceType) {
      setToast({ message: 'Customer name, amount, payment method, and service type are required', type: 'error' }); return
    }
    setSubmitting(true)
    setToast(null)
    try {
      const payload: any = {
        customerName: form.customerName,
        customerPhone: form.customerPhone || null,
        customerAddress: form.customerAddress || null,
        amount: Number(form.amount),
        paymentMethod: form.paymentMethod,
        serviceType: form.serviceType,
        description: form.description || null,
        locationLat: form.locationLat,
        locationLng: form.locationLng,
      }
      if (photoFile && isOnline) {
        const uploadRes = await uploadFile(photoFile)
        payload.photoUrls = [uploadRes.data.url]
      }
      if (!isOnline) {
        payload.photoUrls = photoFile ? ['pending_upload'] : []
        await enqueue('/cash-collections', 'POST', payload, localStorage.getItem('token'))
        setForm(initialForm)
        setPhotoFile(null)
        setPhotoPreview('')
        setToast({ message: 'Saved offline — will sync when online', type: 'info' })
      } else {
        await createCashCollection(payload)
        setForm(initialForm)
        setPhotoFile(null)
        setPhotoPreview('')
        fetchData()
      }
    } catch (err: any) {
      const msg = err?.response?.data?.message || err?.message || 'Failed to submit cash collection'
      setToast({ message: msg, type: 'error' })
    } finally { setSubmitting(false) }
  }

  return (
    <div>
      <h1 className="font-display text-xl font-bold text-gray-900 mb-6">Cash Collection</h1>
      <Toast message={toast?.message || ''} type={toast?.type || 'info'} visible={!!toast} onClose={() => setToast(null)} />
      <div className="grid grid-cols-1 lg:grid-cols-5 gap-6">
        <div className="lg:col-span-3 card p-4">
          <h2 className="font-display text-base font-bold text-gray-900 mb-4">Record Collection</h2>
          <form onSubmit={handleSubmit} className="space-y-4">
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div className="sm:col-span-2">
                <label className="text-xs font-medium text-gray-500 block mb-1">Customer Name *</label>
                <input value={form.customerName} onChange={e => setForm(f => ({ ...f, customerName: e.target.value }))} className="input-field w-full" required />
              </div>
              <div>
                <label className="text-xs font-medium text-gray-500 block mb-1">Customer Phone</label>
                <input value={form.customerPhone} onChange={e => setForm(f => ({ ...f, customerPhone: e.target.value }))} className="input-field w-full" />
              </div>
              <div>
                <label className="text-xs font-medium text-gray-500 block mb-1">Customer Address</label>
                <input value={form.customerAddress} onChange={e => setForm(f => ({ ...f, customerAddress: e.target.value }))} className="input-field w-full" />
              </div>
              <div>
                <label className="text-xs font-medium text-gray-500 block mb-1">Amount (Rs.) *</label>
                <input type="number" step="0.01" min="0" value={form.amount} onChange={e => setForm(f => ({ ...f, amount: e.target.value }))} className="input-field w-full" required />
              </div>
              <div>
                <label className="text-xs font-medium text-gray-500 block mb-1">Payment Method *</label>
                <select value={form.paymentMethod} onChange={e => setForm(f => ({ ...f, paymentMethod: e.target.value }))} className="input-field w-full" required>
                  <option value="">Select payment method</option>
                  {paymentMethods.map(m => (<option key={m} value={m}>{m.replace(/_/g, ' ')}</option>))}
                </select>
              </div>
              <div>
                <label className="text-xs font-medium text-gray-500 block mb-1">Service Type *</label>
                <select value={form.serviceType} onChange={e => setForm(f => ({ ...f, serviceType: e.target.value }))} className="input-field w-full" required>
                  <option value="">Select service type</option>
                  {serviceTypes.map(t => (<option key={t} value={t}>{t.replace(/_/g, ' ')}</option>))}
                </select>
              </div>
            </div>
            <div>
              <label className="text-xs font-medium text-gray-500 block mb-1">Description / Notes</label>
              <textarea value={form.description} onChange={e => setForm(f => ({ ...f, description: e.target.value }))} className="input-field w-full" rows={3} placeholder="Add any details about the collection..." />
            </div>
            <div>
              <label className="text-xs font-medium text-gray-500 block mb-1">Location</label>
              <div className="flex gap-2">
                <input value={form.location} onChange={e => setForm(f => ({ ...f, location: e.target.value }))} className="input-field flex-1" placeholder="Type location or use GPS" />
                <button type="button" onClick={getCurrentLocation} disabled={gettingLocation} className="btn-secondary text-xs whitespace-nowrap">
                  {gettingLocation ? 'Locating...' : '📍 GPS'}
                </button>
              </div>
              {form.locationLat != null && form.locationLng != null && (
                <div className="mt-2 flex items-center gap-2 text-xs text-gray-500">
                  <span>{form.locationLat.toFixed(6)}, {form.locationLng.toFixed(6)}</span>
                  <a href={`https://www.google.com/maps?q=${form.locationLat},${form.locationLng}`} target="_blank" rel="noopener noreferrer" className="text-brand-600 hover:text-brand-700 underline">View on Map</a>
                  <button type="button" onClick={() => setForm(f => ({ ...f, locationLat: null, locationLng: null }))} className="text-red-500 hover:text-red-700 underline">Clear</button>
                </div>
              )}
            </div>
            <div>
              <label className="text-xs font-medium text-gray-500 block mb-1">Payment Screenshot / Photo</label>
              <input type="file" accept="image/*" capture="environment" onChange={e => {
                const file = e.target.files?.[0]
                if (file) {
                  if (file.size > 10_485_760) {
                    setToast({ message: 'Photo too large — max 10MB', type: 'error' })
                    e.target.value = ''
                    return
                  }
                  setPhotoFile(file)
                  setPhotoPreview(URL.createObjectURL(file))
                }
              }} className="text-sm text-gray-500 file:mr-2 file:py-1 file:px-3 file:rounded file:border-0 file:text-xs file:bg-blue-50 file:text-blue-600 hover:file:bg-blue-100" />
              {photoPreview && <img src={photoPreview} className="mt-2 h-20 w-20 object-cover rounded" />}
            </div>
            <button type="submit" disabled={submitting} className="btn-primary w-full">{submitting ? 'Submitting...' : 'Submit Collection'}</button>
          </form>
        </div>

        <div className="lg:col-span-2 space-y-4">
          <div className="card p-4">
            <div className="flex items-center justify-between mb-4">
              <h2 className="font-display text-base font-bold text-gray-900">My Collections</h2>
              <div className="flex gap-1 items-center">
                <select value={month} onChange={e => setMonth(Number(e.target.value))} className="input-field text-xs w-auto">
                  {monthNames.slice(1).map((name, i) => (<option key={i + 1} value={i + 1}>{name}</option>))}
                </select>
                <select value={year} onChange={e => setYear(Number(e.target.value))} className="input-field text-xs w-auto">
                  {[2025, 2026, 2027].map(y => (<option key={y} value={y}>{y}</option>))}
                </select>
              </div>
            </div>

            {loading ? (
              <Skeleton variant="card" count={1} />
            ) : summary ? (
              <>
                <div className="flex flex-wrap gap-2 mb-4 text-xs">
                  <span className="flex items-center gap-1"><span className="w-3 h-3 rounded bg-green-100 border border-green-300" /> Collected</span>
                  <span className="flex items-center gap-1"><span className="w-3 h-3 rounded bg-yellow-100 border border-yellow-300" /> Pending</span>
                  <span className="flex items-center gap-1"><span className="w-3 h-3 rounded bg-red-100 border border-red-300" /> Rejected</span>
                </div>

                <div className="overflow-x-auto">
                  <div className="grid grid-cols-7 gap-1 min-w-[280px]">
                    {['Sun','Mon','Tue','Wed','Thu','Fri','Sat'].map(d => (
                      <div key={d} className="text-center text-[10px] font-medium text-gray-400 py-1">{d}</div>
                    ))}
                    {Array.from({ length: new Date(year, month - 1, 1).getDay() }, (_, i) => (
                      <div key={`empty-${i}`} />
                    ))}
                    {dayHeaders.map(d => {
                      const entries = summary.days?.[d]
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
                        <div key={d} className="text-center">
                          <div className="text-[10px] text-gray-400 mb-0.5">{d}</div>
                          <div className={`inline-flex items-center justify-center w-7 h-7 rounded text-[10px] font-medium ${cellClass}`}
                            title={entries?.map((e: any) => `${e.customerName}: Rs. ${e.amount} (${e.status})`).join(', ')}>
                            {label}
                          </div>
                        </div>
                      )
                    })}
                  </div>
                </div>

                <div className="mt-4 text-sm space-y-2">
                  <div className="flex justify-between">
                    <span className="text-gray-500">Approved (Collected)</span>
                    <span className="font-medium text-green-700">Rs. {Number(summary.totalCollected || 0).toLocaleString()}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-gray-500">Pending</span>
                    <span className="font-medium text-yellow-700">Rs. {Number(summary.totalPending || 0).toLocaleString()}</span>
                  </div>
                  <div className="flex justify-between border-t border-gray-100 pt-2">
                    <span className="text-gray-700 font-medium">Total Submitted</span>
                    <span className="font-bold">Rs. {Number(summary.totalSubmitted || 0).toLocaleString()}</span>
                  </div>
                </div>
              </>
            ) : (
              <p className="text-gray-400 text-sm text-center py-4">No data</p>
            )}
          </div>

          <div className="card p-4">
            <h3 className="font-display text-sm font-bold text-gray-900 mb-3">Recent Submissions</h3>
            <div className="mb-3">
              <select value={filterStatus} onChange={e => setFilterStatus(e.target.value)} className="input-field w-full text-xs">
                <option value="">All Status</option>
                <option value="PENDING">Pending</option>
                <option value="APPROVED">Approved</option>
                <option value="REJECTED">Rejected</option>
                <option value="NEEDS_REVISION">Needs Revision</option>
              </select>
            </div>
            {loading ? (
              <Skeleton variant="table-row" count={4} />
            ) : collections.length === 0 ? (
              <p className="text-gray-400 text-sm text-center py-4">No collections yet</p>
            ) : (
              <div className="space-y-2 max-h-[300px] overflow-y-auto">
                {collections.map((c: any) => (
                  <div key={c.id} className="py-2 border-b border-gray-50">
                    <div className="flex items-center justify-between">
                      <div>
                        <p className="text-sm font-medium text-gray-900">{c.customerName}</p>
                        <p className="text-xs text-gray-500">{c.paymentMethod?.replace(/_/g, ' ')} • Rs. {Number(c.amount).toLocaleString()}</p>
                        <p className="text-xs text-gray-400">{c.serviceType?.replace(/_/g, ' ')}</p>
                      </div>
                      <span className={`text-[10px] px-2 py-0.5 rounded-full font-medium ${STATUS_COLORS[c.status] || 'bg-gray-100 text-gray-500'}`}>{c.status?.replace(/_/g, ' ')}</span>
                    </div>
                    {c.description && <p className="text-xs text-gray-400 mt-1">{c.description}</p>}
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  )
}
