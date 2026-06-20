import { useState } from 'react'

export default function Contact() {
  const [name, setName] = useState('')
  const [email, setEmail] = useState('')
  const [subject, setSubject] = useState('')
  const [message, setMessage] = useState('')
  const [status, setStatus] = useState<'idle' | 'sending' | 'sent' | 'error'>('idle')

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setStatus('sending')
    try {
      const res = await fetch('/api/contact', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ name, email, subject, message }),
      })
      if (!res.ok) throw new Error('Failed to send')
      setStatus('sent')
      setName(''); setEmail(''); setSubject(''); setMessage('')
    } catch {
      setStatus('error')
    }
  }

  return (
    <div className="max-w-4xl mx-auto px-5 sm:px-6 py-10 md:py-14">
      <div className="text-center mb-8 md:mb-10">
        <h1 className="font-display text-xl sm:text-2xl md:text-3xl font-bold text-slate-900">Contact Us</h1>
        <p className="text-xs sm:text-sm text-slate-500 mt-1">We'd love to hear from you</p>
      </div>

      <div className="grid md:grid-cols-5 gap-5">
        <div className="md:col-span-3 bg-white rounded-xl md:rounded-2xl p-5 md:p-8 border border-slate-100 shadow-sm">
          <form onSubmit={handleSubmit} className="space-y-4">
            <div className="grid sm:grid-cols-2 gap-4">
              <div>
                <label className="block text-xs font-medium text-slate-700 mb-1">Name</label>
                <input type="text" value={name} onChange={e => setName(e.target.value)} className="input-field text-sm w-full" placeholder="Your name" required />
              </div>
              <div>
                <label className="block text-xs font-medium text-slate-700 mb-1">Email</label>
                <input type="email" value={email} onChange={e => setEmail(e.target.value)} className="input-field text-sm w-full" placeholder="you@example.com" required />
              </div>
            </div>
            <div>
              <label className="block text-xs font-medium text-slate-700 mb-1">Subject</label>
              <input type="text" value={subject} onChange={e => setSubject(e.target.value)} className="input-field text-sm w-full" placeholder="How can we help?" required />
            </div>
            <div>
              <label className="block text-xs font-medium text-slate-700 mb-1">Message</label>
              <textarea rows={4} value={message} onChange={e => setMessage(e.target.value)} className="input-field text-sm w-full" placeholder="Tell us more about your inquiry..." required />
            </div>
            <button type="submit" disabled={status === 'sending'} className="btn-primary w-full text-sm">
              {status === 'sending' ? 'Sending...' : status === 'sent' ? 'Sent!' : status === 'error' ? 'Error — Try Again' : 'Send Message'}
            </button>
          </form>
        </div>

        <div className="md:col-span-2 flex flex-col gap-3">
          {[
            { icon: '📍', label: 'Address', value: 'Rolpa, Tapla, Nepal', detail: '' },
            { icon: '📞', label: 'Phone', value: '9857820135', detail: '' },
            { icon: '📧', label: 'Email', value: 'info@allinoneisp.com', detail: '' },
          ].map((item) => (
            <div key={item.label + item.value} className="bg-white rounded-xl p-4 border border-slate-100 shadow-sm flex items-center gap-4">
              <div className={`w-10 h-10 rounded-lg flex items-center justify-center text-lg shrink-0 ${item.icon === '✉️' ? 'bg-red-50' : 'bg-brand-50'}`}>{item.icon}</div>
              <div>
                <p className="font-semibold text-sm text-slate-900">{item.label}</p>
                <p className="text-xs text-slate-500">{item.value}</p>
                {item.detail && <p className="text-[11px] text-slate-400 mt-0.5">{item.detail}</p>}
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  )
}
