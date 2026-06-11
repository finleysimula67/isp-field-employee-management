import { Link } from 'react-router-dom'

export default function Landing() {
  return (
    <div className="overflow-x-hidden">
      <section className="relative overflow-hidden bg-gradient-to-br from-slate-900 via-brand-950 to-brand-900 text-white">
        <div className="absolute inset-0 opacity-[0.07] md:opacity-10" style={{
          backgroundImage: 'radial-gradient(circle at 25px 25px, rgba(255,255,255,0.15) 1px, transparent 0)',
          backgroundSize: '50px 50px',
        }} />
        <div className="hidden lg:block absolute top-0 right-1/3 w-[500px] h-[500px] bg-brand-500/10 rounded-full blur-3xl" />
        <div className="hidden lg:block absolute bottom-0 left-1/4 w-96 h-96 bg-cyan-500/10 rounded-full blur-3xl" />
        <div className="max-w-7xl mx-auto px-5 sm:px-6 lg:px-8 py-16 md:py-24 relative">
          <div className="flex flex-col lg:flex-row items-center gap-10 lg:gap-16">
            <div className="flex-1 text-center lg:text-left">
              <div className="inline-flex items-center gap-2 bg-white/10 backdrop-blur-sm px-3 sm:px-4 py-1.5 sm:py-2 rounded-full text-xs sm:text-sm text-brand-200 mb-5 md:mb-6 border border-white/10">
                <span className="w-1.5 h-1.5 sm:w-2 sm:h-2 bg-green-400 rounded-full animate-pulse" />
                ISP Workforce Platform
              </div>
              <h1 className="font-display text-3xl sm:text-4xl lg:text-6xl font-extrabold leading-tight mb-4 md:mb-6">
                All in One Electronics &<br />
                <span className="text-transparent bg-clip-text bg-gradient-to-r from-brand-300 to-cyan-300">Network Solutions</span>
              </h1>
              <p className="text-sm sm:text-base md:text-lg text-slate-300 mb-7 md:mb-9 max-w-xl leading-relaxed">
                Track attendance, manage tasks, request leave, and automate payroll — all from your phone.
              </p>
              <div className="flex flex-col sm:flex-row gap-3 sm:gap-4 justify-center lg:justify-start">
                <Link to="/login" className="inline-flex items-center justify-center gap-2 bg-brand-500 hover:bg-brand-400 text-white px-7 sm:px-8 py-3.5 rounded-xl font-semibold transition-all duration-200 hover:shadow-lg hover:shadow-brand-500/25 active:scale-95 text-base sm:text-lg">
                  Get Started
                  <svg className="w-4 h-4 sm:w-5 sm:h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 7l5 5m0 0l-5 5m5-5H6" /></svg>
                </Link>
                <Link to="/about" className="inline-flex items-center justify-center gap-2 bg-white/10 hover:bg-white/20 backdrop-blur-sm text-white border border-white/20 px-7 sm:px-8 py-3.5 rounded-xl font-semibold transition-all duration-200 text-base sm:text-lg">
                  Learn More
                  <svg className="w-4 h-4 sm:w-5 sm:h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" /></svg>
                </Link>
              </div>
            </div>
            <div className="hidden lg:block relative shrink-0">
              <div className="relative mx-auto bg-slate-900 rounded-[2.5rem] p-3 shadow-2xl border-4 border-slate-700/50 w-[280px]">
                <div className="absolute top-0 left-1/2 -translate-x-1/2 w-24 h-6 bg-slate-900 rounded-b-xl z-10" />
                <div className="bg-white rounded-[1.75rem] overflow-hidden">
                  <div className="bg-gradient-to-r from-brand-600 to-cyan-600 px-4 py-3 flex items-center gap-2">
                    <div className="w-5 h-5 bg-white/20 rounded-lg flex items-center justify-center"><span className="text-white font-bold text-xs">A</span></div>
                    <span className="text-white font-semibold text-xs">All in One</span>
                  </div>
                  <div className="px-4 py-4 space-y-3">
                    {[
                      { icon: '📋', label: 'Daily Log — Tower #12', time: 'Just now', color: 'bg-blue-50 text-blue-700' },
                      { icon: '✅', label: 'Task: Fiber Repair', time: '2 hrs ago', color: 'bg-emerald-50 text-emerald-700' },
                      { icon: '🏢', label: 'Office Duty — AM Shift', time: 'Yesterday', color: 'bg-purple-50 text-purple-700' },
                      { icon: '📝', label: 'Leave Request', time: 'Pending', color: 'bg-amber-50 text-amber-700' },
                      { icon: '💰', label: 'Salary: NPR 45,000', time: 'Paid', color: 'bg-green-50 text-green-700' },
                      { icon: '📶', label: 'Offline Sync: Pending', time: 'Auto', color: 'bg-slate-50 text-slate-600' },
                    ].map((item, i) => (
                      <div key={i} className="flex items-center gap-3 bg-gray-50 rounded-xl p-3">
                        <div className="text-lg">{item.icon}</div>
                        <div className="flex-1 min-w-0">
                          <p className="text-xs font-semibold text-slate-800 truncate">{item.label}</p>
                          <p className="text-[10px] text-slate-400">{item.time}</p>
                        </div>
                        <div className={`${item.color} text-[10px] font-medium px-2 py-0.5 rounded-full shrink-0`}>Active</div>
                      </div>
                    ))}
                  </div>
                  <div className="border-t border-gray-100 flex justify-around py-2 px-2">
                    {['🏠', '📋', '📝', '✅', '💰'].map((icon, i) => (
                      <div key={i} className={`text-center ${i === 0 ? 'opacity-100' : 'opacity-40'} text-sm`}>{icon}</div>
                    ))}
                  </div>
                </div>
              </div>
              <div className="absolute -top-4 -right-4 w-24 h-24 bg-brand-500/20 rounded-full blur-2xl" />
              <div className="absolute -bottom-4 -left-4 w-20 h-20 bg-cyan-500/20 rounded-full blur-2xl" />
            </div>
          </div>
        </div>
        <div className="absolute -bottom-1 left-0 right-0 h-12 md:h-16 bg-gradient-to-b from-brand-950/0 to-amber-50" />
      </section>

      <section className="py-12 md:py-16 bg-gradient-to-b from-amber-50 to-white">
        <div className="max-w-7xl mx-auto px-5 sm:px-6 lg:px-8">
          <div className="grid grid-cols-2 md:grid-cols-4 gap-3 sm:gap-6">
            {[
              { value: '8+', label: 'Employees', icon: '👷' },
              { value: '6+', label: 'Wireless Towers', icon: '🗼' },
              { value: '99%', label: 'Uptime', icon: '📡' },
              { value: '24/7', label: 'Support', icon: '🎧' },
            ].map((stat) => (
              <div key={stat.label} className="text-center bg-white/80 backdrop-blur-sm rounded-xl md:rounded-2xl p-4 md:p-6 border border-amber-100 shadow-sm">
                <div className="text-xl md:text-2xl mb-1 md:mb-2">{stat.icon}</div>
                <div className="text-2xl sm:text-3xl md:text-4xl font-bold bg-gradient-to-r from-brand-600 to-cyan-600 bg-clip-text text-transparent">{stat.value}</div>
                <div className="text-xs sm:text-sm text-slate-500 mt-0.5 md:mt-1">{stat.label}</div>
              </div>
            ))}
          </div>
        </div>
      </section>

      <section className="py-14 md:py-20 bg-white">
        <div className="max-w-7xl mx-auto px-5 sm:px-6 lg:px-8">
          <div className="text-center mb-10 md:mb-14">
            <span className="text-brand-600 font-semibold text-xs sm:text-sm tracking-wider uppercase">Why Choose Us</span>
            <h2 className="font-display text-2xl sm:text-3xl md:text-4xl font-bold mt-2 md:mt-3 text-slate-900">All in One &amp; Network Solutions</h2>
            <p className="text-sm sm:text-base text-slate-500 mt-2 md:mt-3 max-w-xl mx-auto">Everything you need to manage your field team</p>
          </div>
          <div className="grid md:grid-cols-3 gap-5 md:gap-8">
            {[
              { title: 'Mobile-First Design', desc: 'Built for field technicians. Works on any device, anywhere.', icon: '📱' },
              { title: 'Offline Ready', desc: 'Submit logs offline. Auto-syncs when connected. Never lose data.', icon: '📶' },
              { title: 'Smart Payroll', desc: 'Daily or hourly rates. OT calculations. Wages & advances. All automated.', icon: '💰' },
            ].map((item) => (
              <div key={item.title} className="group bg-white rounded-xl md:rounded-2xl p-6 md:p-8 border border-slate-100 hover:border-brand-200 shadow-sm hover:shadow-md transition-all duration-300">
                <div className="w-10 h-10 md:w-12 md:h-12 bg-gradient-to-br from-brand-500 to-cyan-500 rounded-lg md:rounded-xl flex items-center justify-center text-xl md:text-2xl mb-4 md:mb-5 shadow-lg shadow-brand-500/20">
                  {item.icon}
                </div>
                <h3 className="font-semibold text-base md:text-xl mb-2 md:mb-3 text-slate-900">{item.title}</h3>
                <p className="text-sm md:text-base text-slate-500 leading-relaxed">{item.desc}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      <section className="py-14 md:py-20 bg-gradient-to-b from-white to-amber-50">
        <div className="max-w-7xl mx-auto px-5 sm:px-6 lg:px-8">
          <div className="text-center mb-10 md:mb-14">
            <span className="text-brand-600 font-semibold text-xs sm:text-sm tracking-wider uppercase">Mobile Experience</span>
            <h2 className="font-display text-2xl sm:text-3xl md:text-4xl font-bold mt-2 md:mt-3 text-slate-900">Everything in Your Pocket</h2>
            <p className="text-sm sm:text-base text-slate-500 mt-2 md:mt-3 max-w-xl mx-auto">Built for the field. Works offline. One tap away.</p>
          </div>
          <div className="grid sm:grid-cols-2 gap-4 md:gap-6">
            {[
              { icon: '📋', title: 'Daily Log', desc: 'Tap to clock in, log hours, add notes. Works offline — auto-syncs later.', color: 'from-blue-500 to-blue-600' },
              { icon: '📝', title: 'Leave Requests', desc: 'Request leave, view remaining days, track approval status in real-time.', color: 'from-amber-500 to-orange-500' },
              { icon: '✅', title: 'Task Management', desc: 'Receive assigned tasks, update progress, mark complete from your phone.', color: 'from-emerald-500 to-teal-500' },
              { icon: '💰', title: 'Wages & Advances', desc: 'Check earnings, request advances, check live balance. Green means available.', color: 'from-purple-500 to-pink-500' },
            ].map((item, i) => (
              <div key={i} className="flex items-start gap-4 bg-white rounded-xl md:rounded-2xl p-4 md:p-6 border border-slate-100 shadow-sm">
                <div className={`w-10 h-10 md:w-12 md:h-12 bg-gradient-to-br ${item.color} rounded-lg md:rounded-xl flex items-center justify-center text-xl md:text-2xl shrink-0 shadow-lg`}>
                  {item.icon}
                </div>
                <div className="min-w-0">
                  <h3 className="font-semibold text-base md:text-lg text-slate-900">{item.title}</h3>
                  <p className="text-sm md:text-base text-slate-500 mt-1 leading-relaxed">{item.desc}</p>
                </div>
              </div>
            ))}
          </div>
        </div>
      </section>

      <section className="py-14 md:py-20 bg-white">
        <div className="max-w-7xl mx-auto px-5 sm:px-6 lg:px-8">
          <div className="text-center mb-10 md:mb-14">
            <span className="text-brand-600 font-semibold text-xs sm:text-sm tracking-wider uppercase">Testimonials</span>
            <h2 className="font-display text-2xl sm:text-3xl md:text-4xl font-bold mt-2 md:mt-3 text-slate-900">What Our Team Says</h2>
            <p className="text-sm sm:text-base text-slate-500 mt-2 md:mt-3 max-w-xl mx-auto">Real words from field employees</p>
          </div>
          <div className="flex gap-4 md:gap-6 overflow-x-auto snap-x snap-mandatory pb-4 -mx-5 sm:-mx-6 lg:-mx-8 px-5 sm:px-6 lg:px-8 scrollbar-none">
            {[
              { name: 'Nabkiran Oli', role: 'Owner / Mastermind', quote: 'I built this team and this system. Now I can see everything from my phone — logs, tasks, salary — all under my control.', rating: 5 },
              { name: 'Top Bahadur Oli', role: 'Founder / Senior Owner', quote: 'My sons handle the daily work now. This system makes me proud — everything is clean and organized.', rating: 5 },
              { name: 'Prakash Kc', role: 'Field Technician', quote: 'I just use the app and my work is done. No more paper or confusion. I can see my tasks, log my hours, and my manager knows everything on time.', rating: 4 },
              { name: 'Lokendra Oli', role: 'Field Technician', quote: 'Even in remote areas the app works. I can log my work without any problem.', rating: 5 },
              { name: 'Bharat Oli', role: 'Field Technician', quote: 'Tasks come to my phone. I finish and mark done. Very simple.', rating: 4 },
              { name: 'Chandra Oli', role: 'Field Technician', quote: 'Daily log used to take so long. Now I finish in one minute.', rating: 5 },
              { name: 'Kiran Kc', role: 'Field Technician', quote: 'Leave request is very easy. I don\'t need to ask everyone anymore.', rating: 4 },
              { name: 'Lal Bahadur Oli', role: 'Field Technician', quote: 'Advance in one click. Very helpful for emergency.', rating: 5 },
            ].map((item, i) => (
              <div key={i} className="snap-center shrink-0 w-[280px] sm:w-[320px] md:w-[340px]">
                <div className="bg-white rounded-2xl p-6 md:p-8 border border-slate-100 shadow-sm h-full">
                  <div className="flex items-center gap-3 mb-4">
                    <div className="w-10 h-10 md:w-12 md:h-12 bg-gradient-to-br from-brand-100 to-cyan-100 rounded-full flex items-center justify-center text-xl md:text-2xl">👨‍🔧</div>
                    <div>
                      <p className="font-semibold text-sm md:text-base text-slate-900">{item.name}</p>
                      <p className="text-xs md:text-sm text-slate-400">{item.role}</p>
                    </div>
                    <div className="ml-auto flex gap-0.5">
                      {[...Array(5)].map((_, j) => (
                        <span key={j} className={`text-sm ${j < item.rating ? 'text-amber-400' : 'text-slate-200'}`}>★</span>
                      ))}
                    </div>
                  </div>
                  <p className="text-sm md:text-base text-slate-600 leading-relaxed italic">"{item.quote}"</p>
                </div>
              </div>
            ))}
          </div>
        </div>
      </section>


    </div>
  )
}
