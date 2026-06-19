interface DeleteConfirmModalProps {
  open: boolean
  title: string
  message: string
  count?: number
  onConfirm: () => void
  onCancel: () => void
  loading?: boolean
  confirmLabel?: string
}

export default function DeleteConfirmModal({ open, title, message, count, onConfirm, onCancel, loading, confirmLabel }: DeleteConfirmModalProps) {
  if (!open) return null

  const btnLabel = confirmLabel || 'Delete'
  const loadingLabel = confirmLabel ? confirmLabel.replace(/e$/, '') + 'ing...' : 'Deleting...'

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40" onClick={onCancel}>
      <div className="bg-white rounded-xl shadow-xl p-6 w-full max-w-md mx-4" onClick={e => e.stopPropagation()}>
        <h3 className="text-lg font-semibold text-gray-900 mb-2">{title}</h3>
        <p className="text-sm text-gray-600 mb-1">{message}</p>
        {count && count > 1 ? (
          <p className="text-sm font-medium text-red-600">{count} records will be {confirmLabel ? confirmLabel.toLowerCase() + 'd' : 'deleted'}.</p>
        ) : null}
        <div className="flex justify-end gap-3 mt-6">
          <button onClick={onCancel} disabled={loading}
            className="px-4 py-2 text-sm font-medium text-gray-700 bg-gray-100 rounded-lg hover:bg-gray-200 disabled:opacity-50">
            Cancel
          </button>
          <button onClick={onConfirm} disabled={loading}
            className={`px-4 py-2 text-sm font-medium text-white rounded-lg disabled:opacity-50 flex items-center gap-2 ${confirmLabel ? 'bg-green-600 hover:bg-green-700' : 'bg-red-600 hover:bg-red-700'}`}>
            {loading ? (
              <><svg className="animate-spin h-4 w-4" viewBox="0 0 24 24"><circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" fill="none" /><path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z" /></svg>{loadingLabel}</>
            ) : btnLabel}
          </button>
        </div>
      </div>
    </div>
  )
}
