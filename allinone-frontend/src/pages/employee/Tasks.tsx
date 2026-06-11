import { useEffect, useState } from 'react'
import { getMyTasks, updateTaskStatus } from '../../api/tasks'
import Toast from '../../components/Toast'
import Skeleton from '../../components/Skeleton'

const priorityColors: Record<string, string> = {
  LOW: 'bg-gray-100 text-gray-700',
  MEDIUM: 'bg-blue-100 text-blue-700',
  HIGH: 'bg-orange-100 text-orange-700',
  URGENT: 'bg-red-100 text-red-700',
}

const statusColors: Record<string, string> = {
  OPEN: 'badge-pending',
  IN_PROGRESS: 'bg-yellow-100 text-yellow-800',
  COMPLETED: 'badge-approved',
  CANCELLED: 'bg-gray-100 text-gray-500',
}

const tabs = ['All', 'OPEN', 'IN_PROGRESS', 'COMPLETED']

export default function TasksPage() {
  const [tasks, setTasks] = useState<any[]>([])
  const [loading, setLoading] = useState(true)
  const [activeTab, setActiveTab] = useState('All')
  const [toast, setToast] = useState<{ message: string; type: 'success' | 'error' | 'info' } | null>(null)

  const fetchTasks = () => {
    setLoading(true)
    const params: any = {}
    if (activeTab !== 'All') params.status = activeTab
    getMyTasks(params)
      .then(res => setTasks(res.data))
      .catch(() => setToast({ message: 'Failed to load tasks', type: 'error' }))
      .finally(() => setLoading(false))
  }

  useEffect(() => { fetchTasks() }, [activeTab])

  const handleStatusUpdate = async (id: number, status: string) => {
    try {
      await updateTaskStatus(id, { status })
      fetchTasks()
    } catch {
      setToast({ message: 'Failed to update status', type: 'error' })
    }
  }

  return (
    <div>
      <h1 className="font-display text-xl font-bold text-gray-900 mb-6">My Tasks</h1>
      <div className="flex gap-2 mb-4 overflow-x-auto">
        {tabs.map(tab => (
          <button
            key={tab}
            onClick={() => setActiveTab(tab)}
            className={`px-4 py-1.5 text-sm rounded-full font-medium transition-colors ${
              activeTab === tab
                ? 'bg-emp-primary text-white'
                : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
            }`}
          >
            {tab === 'All' ? 'All' : tab.replace(/_/g, ' ')}
          </button>
        ))}
      </div>
      <Toast message={toast?.message || ''} type={toast?.type || 'info'} visible={!!toast} onClose={() => setToast(null)} />
      {loading ? (
        <Skeleton variant="card" count={4} />
      ) : tasks.length === 0 ? (
        <p className="text-gray-400 text-sm text-center py-8">No tasks found</p>
      ) : (
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-3">
          {tasks.map((task: any) => (
            <div key={task.id} className="card p-4">
              <div className="flex items-start justify-between mb-2">
                <h3 className="font-display font-bold text-gray-900 text-sm">{task.title}</h3>
                <span className={`text-xs px-2 py-1 rounded-full font-medium ${priorityColors[task.priority] || 'bg-gray-100 text-gray-700'}`}>
                  {task.priority}
                </span>
              </div>
              {task.description && (
                <p className="text-xs text-gray-500 mb-2">{task.description}</p>
              )}
              <div className="flex items-center justify-between mb-2">
                <span className={statusColors[task.status] || 'badge-pending'}>{task.status?.replace(/_/g, ' ')}</span>
                <span className="text-xs text-gray-400">Scheduled: {task.scheduledDate || '—'}</span>
              </div>
              {(task.customerName || task.customerPhone) && (
                <div className="bg-gray-50 rounded-md p-2 mb-3">
                  {task.customerName && <p className="text-xs text-gray-600"><strong>Customer:</strong> {task.customerName}</p>}
                  {task.customerPhone && <p className="text-xs text-gray-600"><strong>Phone:</strong> {task.customerPhone}</p>}
                  {task.customerAddress && <p className="text-xs text-gray-600"><strong>Address:</strong> {task.customerAddress}</p>}
                </div>
              )}
              <div className="flex gap-2">
                {task.status === 'OPEN' && (
                  <button onClick={() => handleStatusUpdate(task.id, 'IN_PROGRESS')} className="btn-primary text-xs flex-1">Mark In Progress</button>
                )}
                {task.status === 'IN_PROGRESS' && (
                  <button onClick={() => handleStatusUpdate(task.id, 'COMPLETED')} className="btn-primary text-xs flex-1">Mark Complete</button>
                )}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
