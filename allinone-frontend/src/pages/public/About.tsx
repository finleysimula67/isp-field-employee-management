const stats = [
  { value: '8+', label: 'Employees' },
  { value: '5+', label: 'Fiber Stations' },
  { value: '99%', label: 'Uptime' },
  { value: '6+', label: 'Wireless Towers' },
]

export default function About() {
  return (
    <div className="max-w-3xl mx-auto px-5 sm:px-6 py-10 md:py-14">
      <div className="text-center mb-8 md:mb-10">
        <h1 className="font-display text-xl sm:text-2xl md:text-3xl font-bold text-slate-900">About Us</h1>
        <p className="text-xs sm:text-sm text-slate-500 mt-1">All in One Electronics &amp; Network Solutions</p>
      </div>

      <div className="bg-gradient-to-br from-brand-50 to-cyan-50 rounded-2xl p-5 md:p-8 mb-6 md:mb-8">
        <p className="text-xs sm:text-sm text-slate-700 leading-relaxed mb-3">
          We are an ISP that provides fast and reliable internet. Our team manages 5+ fiber stations and 6+ wireless towers across the region.
        </p>
        <p className="text-xs sm:text-sm text-slate-700 leading-relaxed">
          This platform helps our field employees track their daily work, request leave, manage tasks, and get paid — all from their phone.
        </p>
      </div>

      <div className="grid grid-cols-2 md:grid-cols-4 gap-3 mb-6 md:mb-8">
        {stats.map((s) => (
          <div key={s.label} className="text-center bg-white rounded-xl p-3 md:p-4 border border-slate-100 shadow-sm">
            <div className="text-xl sm:text-2xl md:text-3xl font-bold text-brand-600">{s.value}</div>
            <div className="text-xs text-slate-500 mt-0.5">{s.label}</div>
          </div>
        ))}
      </div>

      <div className="grid md:grid-cols-2 gap-4">
        <div className="bg-white rounded-xl p-5 md:p-6 border border-slate-100 shadow-sm">
          <div className="w-9 h-9 bg-brand-100 rounded-lg flex items-center justify-center text-base mb-3">🎯</div>
          <h3 className="font-semibold text-sm md:text-base text-slate-900 mb-1">Our Mission</h3>
          <p className="text-xs md:text-sm text-slate-500 leading-relaxed">Deliver reliable internet through a happy and efficient field team.</p>
        </div>
        <div className="bg-white rounded-xl p-5 md:p-6 border border-slate-100 shadow-sm">
          <div className="w-9 h-9 bg-cyan-100 rounded-lg flex items-center justify-center text-base mb-3">🔭</div>
          <h3 className="font-semibold text-sm md:text-base text-slate-900 mb-1">Our Vision</h3>
          <p className="text-xs md:text-sm text-slate-500 leading-relaxed">To be the most trusted ISP with the happiest employees and customers.</p>
        </div>
      </div>
    </div>
  )
}
