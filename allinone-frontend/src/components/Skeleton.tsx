export default function Skeleton({ variant = 'text', count = 1 }: { variant?: 'text' | 'card' | 'table-row'; count?: number }) {
  const items = Array.from({ length: count })

  if (variant === 'card') {
    return (
      <div className="space-y-4">
        {items.map((_, i) => (
          <div key={i} className="bg-white rounded-xl p-6 animate-pulse">
            <div className="h-4 bg-gray-200 rounded w-1/3 mb-3" />
            <div className="h-3 bg-gray-100 rounded w-2/3 mb-2" />
            <div className="h-3 bg-gray-100 rounded w-1/2" />
          </div>
        ))}
      </div>
    )
  }

  if (variant === 'table-row') {
    return (
      <div className="space-y-2">
        {items.map((_, i) => (
          <div key={i} className="py-3 border-b border-gray-50 animate-pulse flex items-center justify-between">
            <div className="space-y-2 flex-1">
              <div className="h-3 bg-gray-200 rounded w-1/3" />
              <div className="h-2 bg-gray-100 rounded w-1/4" />
            </div>
            <div className="h-5 bg-gray-200 rounded w-16" />
          </div>
        ))}
      </div>
    )
  }

  return (
    <div className="space-y-2">
      {items.map((_, i) => (
        <div key={i} className="h-3 bg-gray-200 rounded animate-pulse" style={{ width: `${75 + (i % 5) * 5}%` }} />
      ))}
    </div>
  )
}
