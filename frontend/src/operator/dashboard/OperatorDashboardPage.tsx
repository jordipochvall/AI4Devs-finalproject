import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { useDashboard } from '../api/operatorApi'
import { logoutSession } from '../../shared/auth/session'
import { formatMoney } from '../../shared/format/money'
import LanguageSwitcher from '../../shared/i18n/LanguageSwitcher'
import '../players/operator.css'
import './dashboard.css'

/** Operator activity dashboard (HU-16): KPIs, date-range filter and most-played games. */
export default function OperatorDashboardPage() {
  const { t, i18n } = useTranslation()
  const navigate = useNavigate()

  const [from, setFrom] = useState('')
  const [to, setTo] = useState('')

  // The date inputs are day-granular; widen them to full-day ISO bounds for the API.
  const dash = useDashboard(
    from ? `${from}T00:00:00Z` : undefined,
    to ? `${to}T23:59:59Z` : undefined,
  )

  const logout = () => { logoutSession(); navigate('/login', { replace: true }) }
  const maxRounds = Math.max(1, ...(dash.data?.topGames.map(g => g.rounds) ?? [0]))

  return (
    <div className="operator">
      <header className="operator-header">
        <h1>{t('operator:dashboard.title')}</h1>
        <div className="operator-header-right">
          <button type="button" className="btn-secondary" onClick={() => navigate('/operator')}>
            {t('operator:dashboard.toPlayers')}
          </button>
          <LanguageSwitcher />
          <button type="button" className="btn-secondary" onClick={logout}>{t('common.logout')}</button>
        </div>
      </header>

      <main className="operator-main">
        <div className="dashboard-filters">
          <label>{t('operator:dashboard.from')}
            <input type="date" value={from} onChange={e => setFrom(e.target.value)} />
          </label>
          <label>{t('operator:dashboard.to')}
            <input type="date" value={to} onChange={e => setTo(e.target.value)} />
          </label>
        </div>

        {dash.isLoading && <p className="operator-msg">{t('operator:dashboard.loading')}</p>}
        {dash.isError && <p className="operator-msg operator-error">{t('operator:dashboard.error')}</p>}

        {dash.data && (
          <>
            <div className="kpi-cards">
              <div className="kpi-card">
                <span className="kpi-value">{dash.data.activePlayers}</span>
                <span className="kpi-label">{t('operator:dashboard.activePlayers')}</span>
              </div>
              <div className="kpi-card">
                <span className="kpi-value">{formatMoney(dash.data.ggrCents, 'EUR', i18n.language)}</span>
                <span className="kpi-label">{t('operator:dashboard.ggr')}</span>
              </div>
              <div className="kpi-card">
                <span className="kpi-value">{dash.data.totalRounds}</span>
                <span className="kpi-label">{t('operator:dashboard.totalRounds')}</span>
              </div>
            </div>

            <h2>{t('operator:dashboard.topGames')}</h2>
            {dash.data.topGames.length === 0
              ? <p className="operator-msg">{t('operator:dashboard.empty')}</p>
              : (
                <ul className="top-games">
                  {dash.data.topGames.map(g => (
                    <li key={g.gameId} className="top-game">
                      <span className="top-game-name">{g.name ?? `#${g.gameId}`}</span>
                      <span className="top-game-bar" style={{ width: `${(g.rounds / maxRounds) * 100}%` }} />
                      <span className="top-game-count">{g.rounds}</span>
                    </li>
                  ))}
                </ul>
              )}
          </>
        )}
      </main>
    </div>
  )
}
