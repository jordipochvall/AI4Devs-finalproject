import { Navigate, Outlet } from 'react-router-dom'
import { useAuthStore } from './authStore'

interface Props {
  /** Si se especifica, el usuario debe tener exactamente este rol. */
  role?: string
}

/**
 * Protege rutas autenticadas. Sin token → /login.
 * Con rol incorrecto → ruta de inicio del rol del usuario.
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
