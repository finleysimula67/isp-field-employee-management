import { useEffect, useState } from 'react'
import { getMyLogs, createDailyLog } from '../../api/dailyLogs'
import { uploadFile } from '../../api/upload'
import { useAuth } from '../../contexts/AuthContext'
import { useOnlineStatus } from '../../hooks/useOnlineStatus'
import { enqueue } from '../../services/offlineQueue'
import TimePicker from '../../components/TimePicker'
import Toast from '../../components/Toast'
import Skeleton from '../../components/Skeleton'

const categories = [
  'NEW_FIBER_CONNECTION',
  'SERVICE_MAINTENANCE',
  'WIRE_REPAIR',
  'ROUTER_CONFIGURATION',
  'CLIENT_SUPPORT',
  'OFFICE_DUTY',
]

const statusColors: Record<string, string> = {
  PENDING: 'badge-pending',
  APPROVED: 'badge-approved',
  REJECTED: 'badge-rejected',
  NEEDS_REVISION: 'badge-revision',
}

const initialForm = {
  date: new Date().toISOString().slice(0, 10),
  startTime: '',
  endTime: '',
  category: '',
  description: '',
  location: '',
  locationLat: null as number | null,
  locationLng: null as number | null,
  photoUrls: [] as string[],
}

export default function DailyLogPage() {
  const { user } = useAuth()
  const isOnline = useOnlineStatus()
  const [logs, setLogs] = useState<any[]>([])
  const [loading, setLoading] = useState(true)
  const [form, setForm] = useState(initialForm)
  const [submitting, setSubmitting] = useState(false)
  const [filterDate, setFilterDate] = useState('')
  const [filterCategory, setFilterCategory] = useState('')
  const [toast, setToast] = useState<{ message: string; type: 'success' | 'error' | 'info' } | null>(null)
  const [photoFile, setPhotoFile] = useState<File | null>(null)
  const [photoPreview, setPhotoPreview] = useState('')
  const [gettingLocation, setGettingLocation] = useState(false)

  const fetchLogs = () => {
    setLoading(true)
    const params: any = {}
    if (filterDate) params.date = filterDate
    if (filterCategory) params.category = filterCategory
    getMyLogs(params)
      .then(res => setLogs(res.data))
      .catch(() => setToast({ message: 'Failed to load data', type: 'error' }))
      .finally(() => setLoading(false))
  }

  useEffect(() => { fetchLogs() }, [])

  const getCurrentLocation = () => {
    if (!navigator.geolocation) { setToast({ message: 'Geolocation is not supported by your browser', type: 'error' }); return }
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
          case err.PERMISSION_DENIED: setToast({ message: 'Location permission denied. Enable GPS in browser settings.', type: 'error' }); break
          case err.POSITION_UNAVAILABLE: setToast({ message: 'Location unavailable. Check GPS signal.', type: 'error' }); break
          case err.TIMEOUT: setToast({ message: 'Location request timed out. Try again.', type: 'error' }); break
          default: setToast({ message: 'Failed to get location', type: 'error' })
        }
      },
      { enableHighAccuracy: true, timeout: 10000, maximumAge: 60000 }
    )
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!form.category || !form.description) { setToast({ message: 'Work type and description are required', type: 'error' }); return }
    setSubmitting(true)
    setToast(null)
    try {
      const payload: any = {
        logDate: form.date,
        startTime: form.startTime || null,
        endTime: form.endTime || null,
        category: form.category,
        workDescription: form.description,
        locationDescription: form.location || null,
        locationLat: form.locationLat,
        locationLng: form.locationLng,
        employeeId: user?.id,
      }
      if (photoFile && isOnline) {
        const uploadRes = await uploadFile(photoFile)
        payload.photoUrls = [uploadRes.data.url]
      }
      if (!isOnline) {
        await enqueue('/daily-logs', 'POST', payload, localStorage.getItem('token'))
        setForm(initialForm)
        setPhotoFile(null)
        setPhotoPreview('')
        setToast({ message: 'Saved offline — will sync when connection resumes', type: 'info' })
      } else {
        await createDailyLog(payload)
        setForm(initialForm)
        setPhotoFile(null)
        setPhotoPreview('')
        fetchLogs()
      }
    } catch {
      setToast({ message: 'Failed to submit log', type: 'error' })
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div>
      <h1 className="font-display text-xl font-bold text-gray-900 mb-6">Daily Log</h1>
      <div className="grid grid-cols-1 lg:grid-cols-5 gap-6">
        <div className="lg:col-span-3 card p-4">
          <h2 className="font-display text-base font-bold text-gray-900 mb-4">Submit Log</h2>
          <Toast message={toast?.message || ''} type={toast?.type || 'info'} visible={!!toast} onClose={() => setToast(null)} />
          <form onSubmit={handleSubmit} className="space-y-4">
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div>
                <label className="text-xs font-medium text-gray-500 block mb-1">Date</label>
                <input type="date" value={form.date} onChange={e => setForm(f => ({ ...f, date: e.target.value }))} className="input-field w-full" />
              </div>
              <div>
                <label className="text-xs font-medium text-gray-500 block mb-1">Work Type</label>
                <select value={form.category} onChange={e => setForm(f => ({ ...f, category: e.target.value }))} className="input-field w-full" required>
                  <option value="">Select work type</option>
                  {categories.map(c => (
                    <option key={c} value={c}>{c.replace(/_/g, ' ')}</option>
                  ))}
                </select>
                <p className="text-xs text-emerald-600 mt-1">Rate: Rs. 800/day — added on admin approval</p>
              </div>
              <TimePicker label="Start Time" value={form.startTime} onChange={v => setForm(f => ({ ...f, startTime: v }))} />
              <TimePicker label="End Time" value={form.endTime} onChange={v => setForm(f => ({ ...f, endTime: v }))} />
            </div>
            <div>
              <label className="text-xs font-medium text-gray-500 block mb-1">Work Description</label>
              <textarea value={form.description} onChange={e => setForm(f => ({ ...f, description: e.target.value }))} className="input-field w-full" rows={3} required />
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
              <label className="text-xs font-medium text-gray-500 block mb-1">Photo</label>
              <input type="file" accept="image/*" capture="environment" onChange={e => {
                const file = e.target.files?.[0]
                if (file) {
                  setPhotoFile(file)
                  setPhotoPreview(URL.createObjectURL(file))
                }
              }} className="text-sm text-gray-500 file:mr-2 file:py-1 file:px-3 file:rounded file:border-0 file:text-xs file:bg-blue-50 file:text-blue-600 hover:file:bg-blue-100" />
              {photoPreview && <img src={photoPreview} className="mt-2 h-20 w-20 object-cover rounded" />}
            </div>
            <button type="submit" disabled={submitting} className="btn-primary w-full">{submitting ? 'Submitting...' : 'Submit Log'}</button>
          </form>
        </div>
        <div className="lg:col-span-2 card p-4">
          <h2 className="font-display text-base font-bold text-gray-900 mb-4">My Recent Logs</h2>
          <div className="flex gap-2 mb-4">
            <input type="date" value={filterDate} onChange={e => { setFilterDate(e.target.value); fetchLogs() }} className="input-field flex-1" />
            <select value={filterCategory} onChange={e => { setFilterCategory(e.target.value); fetchLogs() }} className="input-field flex-1">
              <option value="">All Categories</option>
              {categories.map(c => (
                <option key={c} value={c}>{c.replace(/_/g, ' ')}</option>
              ))}
            </select>
          </div>
          {loading ? (
            <Skeleton variant="table-row" count={5} />
          ) : logs.length === 0 ? (
            <p className="text-gray-400 text-sm text-center py-4">No logs yet</p>
          ) : (
            <div className="space-y-2">
              {logs.map((log: any) => (
                <div key={log.id} className="flex items-center justify-between py-2 border-b border-gray-50">
                  <div className="flex items-center gap-2">
                    {log.photoUrls?.[0] && (
                      <img src={log.photoUrls[0]} className="h-8 w-8 object-cover rounded" />
                    )}
                    <div>
                      <p className="text-sm font-medium text-gray-900">{log.logDate}</p>
                      <p className="text-xs text-gray-500">{log.category?.replace(/_/g, ' ')} • {log.hoursWorked ?? '—'}h</p>
                    </div>
                  </div>
                  <span className={statusColors[log.status] || 'badge-pending'}>{log.status?.replace(/_/g, ' ')}</span>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  )
}
