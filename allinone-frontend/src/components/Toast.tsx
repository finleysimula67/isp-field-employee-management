import { useEffect } from 'react'
import type { ToastType } from '../hooks/useToast'

const bgMap: Record<ToastType, string> = {
  success: 'bg-green-600',
  error: 'bg-red-600',
  info: 'bg-blue-600',
}

export default function Toast({
  message, type, visible, onClose
}: {
  message: string
  type: ToastType
  visible: boolean
  onClose: () => void
}) {
  useEffect(() => {
    if (visible) {
      const timer = setTimeout(onClose, 4000)
      return () => clearTimeout(timer)
    }
  }, [visible, onClose])

  if (!visible) return null

  return (
    <div className="fixed top-4 right-4 z-[100] animate-slide-in">
      <div className={`${bgMap[type]} text-white px-4 py-3 rounded-lg shadow-lg flex items-center gap-3 min-w-[280px] max-w-sm`}>
        <span className="text-sm flex-1">{message}</span>
        <button onClick={onClose} className="text-white/80 hover:text-white text-lg leading-none">&times;</button>
      </div>
    </div>
  )
}
