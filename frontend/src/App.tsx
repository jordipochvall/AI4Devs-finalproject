import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { useTranslation } from 'react-i18next'
import LoginPage    from './player/auth/LoginPage'
import RegisterPage from './player/auth/RegisterPage'
import ProtectedRoute from './shared/auth/ProtectedRoute'
import LanguageSwitcher from './shared/i18n/LanguageSwitcher'
import { useAuthStore } from './shared/auth/authStore'

const queryClient = new QueryClient({
  defaultOptions: { queries: { retry: 1, staleTime: 30_000 } },
})

/** Placeholder traducido para rutas que implementarán las próximas HU. */
function Placeholder({ titleKey, bodyKey }: { titleKey: string; bodyKey: string }) {
  const { t } = useTranslation()
  const clearAuth = useAuthStore(s => s.clearAuth)
  return (
    <div style={{ padding: '2rem', color: '#e0e0e0', background: '#0f0f1a', minHeight: '100vh' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <h2>{t(titleKey)}</h2>
        <div style={{ display: 'flex', gap: '1rem', alignItems: 'center' }}>
          <LanguageSwitcher />
          <button
            type="button"
            onClick={() => { clearAuth(); window.location.href = '/login' }}
            style={{ background: 'none', border: '1px solid #3a3a5a', color: '#aaa', borderRadius: 6, padding: '0.3rem 0.6rem', cursor: 'pointer' }}
          >
            {t('common.logout')}
          </button>
        </div>
      </div>
      <p style={{ color: '#666' }}>{t(bodyKey)}</p>
    </div>
  )
}

export default function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <Routes>
          {/* Rutas públicas */}
          <Route path="/login"    element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />

          {/* Jugador (PLAYER) */}
          <Route element={<ProtectedRoute role="PLAYER" />}>
            <Route path="/"         element={<Placeholder titleKey="player:lobby.title" bodyKey="player:lobby.underConstruction" />} />
            <Route path="/game/:id" element={<Placeholder titleKey="player:game.title"  bodyKey="player:game.underConstruction" />} />
          </Route>

          {/* Operador (OPERATOR) */}
          <Route element={<ProtectedRoute role="OPERATOR" />}>
            <Route path="/operator"       element={<Placeholder titleKey="operator:backoffice.title" bodyKey="operator:backoffice.underConstruction" />} />
            <Route path="/operator/audit" element={<Placeholder titleKey="operator:audit.title"      bodyKey="operator:audit.underConstruction" />} />
          </Route>

          {/* Matemático (MATH_ANALYST) */}
          <Route element={<ProtectedRoute role="MATH_ANALYST" />}>
            <Route path="/math" element={<Placeholder titleKey="math:backoffice.title" bodyKey="math:backoffice.underConstruction" />} />
          </Route>

          {/* Cualquier ruta desconocida → login */}
          <Route path="*" element={<Navigate to="/login" replace />} />
        </Routes>
      </BrowserRouter>
    </QueryClientProvider>
  )
}
