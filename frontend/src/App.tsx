import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import LoginPage    from './player/auth/LoginPage'
import RegisterPage from './player/auth/RegisterPage'
import LobbyPage    from './player/lobby/LobbyPage'
import GamePage     from './player/game/GamePage'
import PlayerHistoryPage from './player/history/PlayerHistoryPage'
import LimitsPage from './player/limits/LimitsPage'
import OperatorPlayersPage from './operator/players/OperatorPlayersPage'
import OperatorGamesPage from './operator/games/OperatorGamesPage'
import OperatorDashboardPage from './operator/dashboard/OperatorDashboardPage'
import OperatorReportsPage from './operator/reports/OperatorReportsPage'
import AuditPage from './operator/audit/AuditPage'
import ReplayPage from './operator/replay/ReplayPage'
import MathEditorPage from './math/editor/MathEditorPage'
import AdminPage from './admin/AdminPage'
import ProtectedRoute from './shared/auth/ProtectedRoute'

const queryClient = new QueryClient({
  defaultOptions: { queries: { retry: 1, staleTime: 30_000 } },
})

/** App root: query client, router and role-protected routes per surface. */
export default function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <Routes>
          {/* Public routes */}
          <Route path="/login"    element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />

          {/* Player (PLAYER) */}
          <Route element={<ProtectedRoute role="PLAYER" />}>
            <Route path="/"             element={<LobbyPage />} />
            <Route path="/play/:gameId" element={<GamePage />} />
            <Route path="/history"      element={<PlayerHistoryPage />} />
            <Route path="/limits"       element={<LimitsPage />} />
          </Route>

          {/* Operator (OPERATOR) */}
          <Route element={<ProtectedRoute role="OPERATOR" />}>
            <Route path="/operator"          element={<OperatorPlayersPage />} />
            <Route path="/operator/dashboard" element={<OperatorDashboardPage />} />
            <Route path="/operator/reports"  element={<OperatorReportsPage />} />
            <Route path="/operator/games"    element={<OperatorGamesPage />} />
            <Route path="/operator/audit" element={<AuditPage />} />
            <Route path="/operator/replay/:roundId" element={<ReplayPage />} />
          </Route>

          {/* Mathematician (MATH_ANALYST) */}
          <Route element={<ProtectedRoute role="MATH_ANALYST" />}>
            <Route path="/math" element={<MathEditorPage />} />
          </Route>

          {/* Platform admin (ADMIN) */}
          <Route element={<ProtectedRoute role="ADMIN" />}>
            <Route path="/admin" element={<AdminPage />} />
          </Route>

          {/* Any unknown route → login */}
          <Route path="*" element={<Navigate to="/login" replace />} />
        </Routes>
      </BrowserRouter>
    </QueryClientProvider>
  )
}
