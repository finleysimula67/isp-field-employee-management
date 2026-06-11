/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,ts,jsx,tsx}'],
  theme: {
    extend: {
      colors: {
        brand: {
          50: '#eff6ff', 100: '#dbeafe', 200: '#bfdbfe', 300: '#93c5fd',
          400: '#60a5fa', 500: '#3b82f6', 600: '#2563eb', 700: '#1d4ed8',
          800: '#1e40af', 900: '#1e3a8a', 950: '#172554',
        },
        admin: {
          shell: '#0a0a0a',
          surface: '#fcfaf7',
          primary: '#fe6e00',
          'primary-hover': '#e06000',
        },
        emp: {
          bg: '#fafafa',
          primary: '#0f172a',
          success: '#22c55e',
          warning: '#eab308',
          error: '#ef4444',
          info: '#0ea5e9',
        },
        status: {
          pending: { bg: '#fef3c7', text: '#92400e' },
          approved: { bg: '#dcfce7', text: '#166534' },
          rejected: { bg: '#fee2e2', text: '#991b1b' },
          revision: { bg: '#fef3c7', text: '#92400e' },
          locked: { bg: '#f3f4f6', text: '#6b7280' },
        },
      },
      fontFamily: {
        sans: ['Inter', 'system-ui', 'sans-serif'],
        display: ['Plus Jakarta Sans', 'Inter', 'sans-serif'],
      },
      keyframes: {
        'slide-in': {
          '0%': { transform: 'translateX(100%)', opacity: '0' },
          '100%': { transform: 'translateX(0)', opacity: '1' },
        },
      },
      animation: {
        'slide-in': 'slide-in 0.3s ease-out',
      },
    },
  },
  plugins: [],
}
