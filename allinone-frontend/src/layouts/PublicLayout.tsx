import { Link, Outlet, useLocation } from 'react-router-dom'

const navLinks = [
  { path: '/', label: 'Home', activeColor: 'text-brand-600', icon: (
    <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6" /></svg>
  ) },
  { path: '/about', label: 'About', activeColor: 'text-emerald-600', icon: (
    <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>
  ) },
  { path: '/contact', label: 'Contact', activeColor: 'text-amber-600', icon: (
    <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" /></svg>
  ) },
  { path: '/login', label: 'Login', activeColor: 'text-purple-600', icon: (
    <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M11 16l-4-4m0 0l4-4m-4 4h14m-5 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h7a3 3 0 013 3v1" /></svg>
  ) },
]

export default function PublicLayout() {
  const location = useLocation()

  return (
    <div className="min-h-screen flex flex-col">
      <header className="bg-white border-b border-gray-100 sticky top-0 z-50">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center h-14 md:h-16">
            <Link to="/" className="flex items-center gap-2">
              <div className="w-7 h-7 md:w-8 md:h-8 bg-brand-600 rounded-lg flex items-center justify-center">
                <span className="text-white font-bold text-xs md:text-sm">A</span>
              </div>
              <span className="font-display font-bold text-sm md:text-lg text-gray-900">All in One &amp; Network Solutions</span>
            </Link>
            <nav className="hidden sm:flex items-center gap-6 md:gap-8">
              {navLinks.map((link) => (
                <Link
                  key={link.path}
                  to={link.path}
                  className={`text-xs md:text-sm font-medium transition-colors ${
                    location.pathname === link.path
                      ? link.activeColor
                      : 'text-gray-500 hover:text-gray-900'
                  }`}
                >
                  {link.label}
                </Link>
              ))}
            </nav>
          </div>
        </div>
      </header>
      <main className="flex-1 pb-16 sm:pb-0">
        <Outlet />
      </main>
      <nav className="sm:hidden fixed bottom-0 left-0 right-0 bg-white border-t border-gray-100 z-50 rounded-b-[24px]">
        <div className="flex justify-evenly items-center h-14 px-2">
          {navLinks.map((link) => (
            <Link
              key={link.path}
              to={link.path}
              className={`flex flex-col items-center justify-center gap-0.5 py-1 px-2 md:px-3 rounded-lg transition-colors ${
                location.pathname === link.path
                  ? link.activeColor
                  : 'text-gray-400 hover:text-gray-600'
              }`}
            >
              {link.icon}
              <span className="text-[10px] font-medium">{link.label}</span>
            </Link>
          ))}
        </div>
      </nav>
      <footer className="bg-gray-50 border-t border-gray-100 py-6 sm:py-8 pb-20 sm:pb-8">
        <div className="max-w-7xl mx-auto px-4 text-center text-xs sm:text-sm text-gray-500">
          &copy; {new Date().getFullYear()} All in One Electronics & Network Solutions. All rights reserved.
        </div>
      </footer>
    </div>
  )
}
