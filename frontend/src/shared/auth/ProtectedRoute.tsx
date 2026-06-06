import { Navigate, Outlet } from 'react-router-dom'
import { useAuthStore } from './authStore'

interface Props {
  /** If set, the user must have exactly this role. */
  role?: string
}

/**
 * Guards authenticated routes. No token → /login. Wrong role → the user's role home route.
 */
export default function ProtectedRoute({ role }: Props) {
  const { token, user } = useAuthStore()

  if (!token || !user) {
    return <Navigate to="/login" replace />
  }

  if (role && user.role !== role) {
    const home = user.role === 'OPERATOR' ? '/operator'
               : user.role === 'MATH_ANALYST' ? '/math'
               : '/'
    return <Navigate to={home} replace />
  }

  return <Outlet />
}
