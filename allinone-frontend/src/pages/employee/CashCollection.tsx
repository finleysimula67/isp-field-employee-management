import { useEffect, useState, useCallback } from 'react'
import { getMyCashCollections, createCashCollection } from '../../api/cashCollections'
import { uploadFile } from '../../api/upload'
import { useAuth } from '../../contexts/AuthContext'
import { useOnlineStatus } from '../../hooks/useOnlineStatus'
import { enqueue } from '../../services/offlineQueue'
import Toast from '../../components/Toast'
import Skeleton from '../../components/Skeleton'

const paymentMethods = ['CASH', 'MOBILE_MONEY', 'BANK_TRANSFER']
const serviceTypes = ['NEW_CONNECTION', 'INSTALLATION', 'MAINTENANCE', 'REPAIR', 'OTHER']

const statusColors: Record<string, string> = {
  PENDING: 'badge-pending',
  APPROVED: 'badge-approved',
  REJECTED: 'badge-rejected',
  NEEDS_REVISION: 'badge-revision',
}

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
  const [collections, setCollections] = useState<any[]>([])
  const [loading, setLoading] = useState(true)
  const [form, setForm] = useState(initialForm)
  const [submitting, setSubmitting] = useState(false)
  const [filterStatus, setFilterStatus] = useState('')
  const [toast, setToast] = useState<{ message: string; type: 'success' | 'error' | 'info' } | null>(null)
  const [photoFile, setPhotoFile] = useState<File | null>(null)
  const [photoPreview, setPhotoPreview] = useState('')
  const [gettingLocation, setGettingLocation] = useState(false)

  const fetchCollections = useCallback(() => {
    setLoading(true)
    const params: any = {}
    if (filterStatus) params.status = filterStatus
    getMyCashCollections(params)
      .then(res => setCollections(res.data))
      .catch(() => setToast({ message: 'Failed to load data', type: 'error' }))
      .finally(() => setLoading(false))
  }, [filterStatus])

  useEffect(() => { fetchCollections() }, [fetchCollections])

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
        employeeId: user?.id,
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
        fetchCollections()
      }
    } catch {
      setToast({ message: 'Failed to submit cash collection', type: 'error' })
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div>
      <h1 className="font-display text-xl font-bold text-gray-900 mb-6">Cash Collection</h1>
      <div className="grid grid-cols-1 lg:grid-cols-5 gap-6">
        <div className="lg:col-span-3 card p-4">
          <h2 className="font-display text-base font-bold text-gray-900 mb-4">Record Collection</h2>
          <Toast message={toast?.message || ''} type={toast?.type || 'info'} visible={!!toast} onClose={() => setToast(null)} />
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
                  {paymentMethods.map(m => (
                    <option key={m} value={m}>{m.replace(/_/g, ' ')}</option>
                  ))}
                </select>
              </div>
              <div>
                <label className="text-xs font-medium text-gray-500 block mb-1">Service Type *</label>
                <select value={form.serviceType} onChange={e => setForm(f => ({ ...f, serviceType: e.target.value }))} className="input-field w-full" required>
                  <option value="">Select service type</option>
                  {serviceTypes.map(t => (
                    <option key={t} value={t}>{t.replace(/_/g, ' ')}</option>
                  ))}
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
                  setPhotoFile(file)
                  setPhotoPreview(URL.createObjectURL(file))
                }
              }} className="text-sm text-gray-500 file:mr-2 file:py-1 file:px-3 file:rounded file:border-0 file:text-xs file:bg-blue-50 file:text-blue-600 hover:file:bg-blue-100" />
              {photoPreview && <img src={photoPreview} className="mt-2 h-20 w-20 object-cover rounded" />}
            </div>
            <button type="submit" disabled={submitting} className="btn-primary w-full">{submitting ? 'Submitting...' : 'Submit Collection'}</button>
          </form>
        </div>
        <div className="lg:col-span-2 card p-4">
          <h2 className="font-display text-base font-bold text-gray-900 mb-4">My Collections</h2>
          <div className="mb-4">
            <select value={filterStatus} onChange={e => setFilterStatus(e.target.value)} className="input-field w-full">
              <option value="">All Status</option>
              <option value="PENDING">Pending</option>
              <option value="APPROVED">Approved</option>
              <option value="REJECTED">Rejected</option>
              <option value="NEEDS_REVISION">Needs Revision</option>
            </select>
          </div>
          {loading ? (
            <Skeleton variant="table-row" count={5} />
          ) : collections.length === 0 ? (
            <p className="text-gray-400 text-sm text-center py-4">No collections yet</p>
          ) : (
            <div className="space-y-2">
              {collections.map((c: any) => (
                <div key={c.id} className="py-2 border-b border-gray-50">
                  <div className="flex items-center justify-between">
                    <div>
                      <p className="text-sm font-medium text-gray-900">{c.customerName}</p>
                      <p className="text-xs text-gray-500">{c.paymentMethod?.replace(/_/g, ' ')} • Rs. {Number(c.amount).toLocaleString()}</p>
                      <p className="text-xs text-gray-400">{c.serviceType?.replace(/_/g, ' ')}</p>
                    </div>
                    <span className={statusColors[c.status] || 'badge-pending'}>{c.status?.replace(/_/g, ' ')}</span>
                  </div>
                  {c.description && <p className="text-xs text-gray-400 mt-1">{c.description}</p>}
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  )
}
