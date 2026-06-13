import { Routes, Route, Navigate } from 'react-router-dom'
import { AuthProvider } from './contexts/AuthContext'
import AuthGuard from './components/AuthGuard'
import ScrollToTop from './components/ScrollToTop'

import PublicLayout from './layouts/PublicLayout'
import AdminLayout from './layouts/AdminLayout'
import EmployeeLayout from './layouts/EmployeeLayout'

import Landing from './pages/public/Landing'
import About from './pages/public/About'
import Contact from './pages/public/Contact'
import LoginPage from './pages/public/Login'
import ForgotPassword from './pages/public/ForgotPassword'
import ResetPassword from './pages/public/ResetPassword'
import OAuth2Callback from './pages/public/OAuth2Callback'

import AdminDashboard from './pages/admin/Dashboard'
import EmployeesPage from './pages/admin/Employees'
import BranchesPage from './pages/admin/Branches'
import DailyLogsPage from './pages/admin/DailyLogs'
import AttendancePage from './pages/admin/Attendance'
import LeaveRequestsPage from './pages/admin/LeaveRequests'
import AdminTasksPage from './pages/admin/Tasks'
import PayrollPage from './pages/admin/Payroll'
import SalaryAdvancesPage from './pages/admin/SalaryAdvances'
import ReportsPage from './pages/admin/Reports'
import NotificationsPage from './pages/admin/Notifications'
import HolidaysPage from './pages/admin/Holidays'
import AuditLogsPage from './pages/admin/AuditLogs'
import MonthlyLockoutsPage from './pages/admin/MonthlyLockouts'
import EmailAllowListPage from './pages/admin/EmailAllowList'
import ManualLogsPage from './pages/admin/ManualLogs'
import ProfilePage from './pages/admin/Profile'

import EmpDashboard from './pages/employee/Dashboard'
import EmpAttendancePage from './pages/employee/Attendance'
import DailyLogPage from './pages/employee/DailyLog'
import LeavePage from './pages/employee/Leave'
import TasksPage from './pages/employee/Tasks'
import WagesPage from './pages/employee/Wages'
import EmpProfilePage from './pages/employee/Profile'

export default function App() {
  return (
    <AuthProvider>
      <ScrollToTop />
      <Routes>
        <Route element={<PublicLayout />}>
          <Route path="/" element={<Landing />} />
          <Route path="/about" element={<About />} />
          <Route path="/contact" element={<Contact />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/forgot-password" element={<ForgotPassword />} />
          <Route path="/reset-password" element={<ResetPassword />} />
          <Route path="/oauth2/callback" element={<OAuth2Callback />} />
        </Route>

        <Route
          path="/admin"
          element={
            <AuthGuard allowedRoles={['SUPER_ADMIN', 'BRANCH_MANAGER']}>
              <AdminLayout />
            </AuthGuard>
          }
        >
          <Route index element={<AdminDashboard />} />
          <Route path="employees" element={<EmployeesPage />} />
          <Route path="branches" element={<BranchesPage />} />
          <Route path="attendance" element={<AttendancePage />} />
          <Route path="daily-logs" element={<DailyLogsPage />} />
          <Route path="leave-requests" element={<LeaveRequestsPage />} />
          <Route path="tasks" element={<AdminTasksPage />} />
          <Route path="payroll" element={<PayrollPage />} />
          <Route path="salary-advances" element={<SalaryAdvancesPage />} />
          <Route path="reports" element={<ReportsPage />} />
          <Route path="notifications" element={<NotificationsPage />} />
          <Route path="holidays" element={<HolidaysPage />} />
          <Route path="audit-logs" element={<AuditLogsPage />} />
          <Route path="monthly-lockouts" element={<MonthlyLockoutsPage />} />
          <Route path="email-allow-list" element={<EmailAllowListPage />} />
          <Route path="manual-logs" element={<ManualLogsPage />} />
          <Route path="profile" element={<ProfilePage />} />
        </Route>

        <Route
          path="/employee"
          element={
            <AuthGuard allowedRoles={['FIELD_EMPLOYEE']}>
              <EmployeeLayout />
            </AuthGuard>
          }
        >
          <Route index element={<EmpDashboard />} />
          <Route path="attendance" element={<EmpAttendancePage />} />
          <Route path="daily-log" element={<DailyLogPage />} />
          <Route path="leave" element={<LeavePage />} />
          <Route path="tasks" element={<TasksPage />} />
          <Route path="wages" element={<WagesPage />} />
          <Route path="profile" element={<EmpProfilePage />} />
        </Route>

        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </AuthProvider>
  )
}
