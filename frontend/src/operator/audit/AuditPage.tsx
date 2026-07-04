import { useState } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { usePlayers, useRounds, type RoundFilters } from '../api/operatorApi'
import { logoutSession } from '../../shared/auth/session'
import { formatMoney } from '../../shared/format/money'
import LanguageSwitcher from '../../shared/i18n/LanguageSwitcher'
import IntegrityCheck from './IntegrityCheck'
import RoundDetailDialog from './RoundDetailDialog'
import '../players/operator.css'
import './audit.css'

/** Reads the filters from the URL so the view is shareable and back-friendly (AC3). */
function readFilters(params: URLSearchParams): RoundFilters & { page: number; date: { from: string; to: string } } {
  const fromDate = params.get('from') ?? ''
  const toDate = params.get('to') ?? ''
  return {
    playerId: params.get('playerId') ? Number(params.get('playerId')) : null,
    gameId: params.get('gameId') ? Number(params.get('gameId')) : null,
    // Date inputs are days; widen to full-day bounds in UTC for the API (ISO date-time).
    from: fromDate ? `${fromDate}T00:00:00Z` : null,
    to: toDate ? `${toDate}T23:59:59Z` : null,
    page: params.get('page') ? Number(params.get('page')) : 0,
    date: { from: fromDate, to: toDate },
  }
}

/**
 * Operator audit screen: filter rounds by player / game / date range, paginated, with a Replay
 * button per row. Filters live in the URL query string (AC3).
 */
export default function AuditPage() {
  const { t, i18n } = useTranslation()
  const navigate = useNavigate()
  const [params, setParams] = useSearchParams()

  const f = readFilters(params)
  const [emailSearch, setEmailSearch] = useState('')
  const [detailRoundId, setDetailRoundId] = useState<number | null>(null)

  const suggestions = usePlayers(emailSearch, 0, 20)
  const rounds = useRounds({ playerId: f.playerId, gameId: f.gameId, from: f.from, to: f.to }, f.page)

  const logout = () => { logoutSession(); navigate('/login', { replace: true }) }

  /** Merges params, always resetting to page 0 unless a page change is requested. */
  const update = (changes: Record<string, string | null>, keepPage = false) => {
    const next = new URLSearchParams(params)
    for (const [k, v] of Object.entries(changes)) {
      if (v == null || v === '') next.delete(k)
      else next.set(k, v)
    }
    if (!keepPage) next.set('page', '0')
    setParams(next)
  }

  const data = rounds.data
  const totalPages = data?.totalPages ?? 0

  return (
    <div className="operator">
      <header className="operator-header">
        <h1>{t('operator:audit.title')}</h1>
        <div className="operator-header-right">
          <IntegrityCheck />
          <button type="button" className="btn-secondary" onClick={() => navigate('/operator')}>
            {t('operator:audit.toPlayers')}
          </button>
          <LanguageSwitcher />
          <button type="button" className="btn-secondary" onClick={logout}>{t('common.logout')}</button>
        </div>
      </header>

      <main className="operator-main">
        <div className="audit-filters">
          <div className="audit-field audit-player">
            <label>{t('operator:audit.player')}</label>
            {f.playerId ? (
              <span className="audit-selected">
                #{f.playerId}
                <button type="button" className="btn-link" onClick={() => update({ playerId: null })}>
                  {t('operator:audit.clear')}
                </button>
              </span>
            ) : (
              <>
                <input className="search-input" type="search"
                       placeholder={t('operator:audit.searchPlayer')}
                       value={emailSearch} onChange={e => setEmailSearch(e.target.value)} />
                {emailSearch && suggestions.data && suggestions.data.content.length > 0 && (
                  <ul className="audit-suggestions">
                    {suggestions.data.content.map(p => (
                      <li key={p.id}>
                        <button type="button" onClick={() => { update({ playerId: String(p.id) }); setEmailSearch('') }}>
                          {p.email}
                        </button>
                      </li>
                    ))}
                  </ul>
                )}
              </>
            )}
          </div>

          <div className="audit-field">
            <label htmlFor="gameId">{t('operator:audit.game')}</label>
            <input id="gameId" type="number" min={1} className="search-input audit-small"
                   value={f.gameId ?? ''} onChange={e => update({ gameId: e.target.value || null })} />
          </div>

          <div className="audit-field">
            <label htmlFor="from">{t('operator:audit.from')}</label>
            <input id="from" type="date" className="search-input audit-small"
                   value={f.date.from} onChange={e => update({ from: e.target.value || null })} />
          </div>

          <div className="audit-field">
            <label htmlFor="to">{t('operator:audit.to')}</label>
            <input id="to" type="date" className="search-input audit-small"
                   value={f.date.to} onChange={e => update({ to: e.target.value || null })} />
          </div>
        </div>

        {rounds.isLoading && <p className="operator-msg">{t('operator:audit.loading')}</p>}
        {rounds.isError && <p className="operator-msg operator-error">{t('operator:audit.error')}</p>}
        {data && data.content.length === 0 && <p className="operator-msg">{t('operator:audit.empty')}</p>}

        {data && data.content.length > 0 && (
          <>
            <table className="players-table">
              <thead>
                <tr>
                  <th>{t('operator:audit.colRound')}</th>
                  <th>{t('operator:audit.colPlayer')}</th>
                  <th>{t('operator:audit.colGame')}</th>
                  <th className="num">{t('operator:audit.colBet')}</th>
                  <th className="num">{t('operator:audit.colWin')}</th>
                  <th>{t('operator:audit.colDate')}</th>
                  <th>{t('operator:audit.colActions')}</th>
                </tr>
              </thead>
              <tbody>
                {data.content.map(r => (
                  <tr key={r.id}>
                    <td>{r.id}{r.freeSpin ? ` ${t('operator:audit.freeSpinTag')}` : ''}</td>
                    <td>#{r.playerId}</td>
                    <td>#{r.gameId}</td>
                    <td className="num">{formatMoney(r.betCents, 'EUR', i18n.language)}</td>
                    <td className="num">{formatMoney(r.winCents, 'EUR', i18n.language)}</td>
                    <td>{new Date(r.createdAt).toLocaleString(i18n.language)}</td>
                    <td>
                      <button type="button" className="btn-link" onClick={() => setDetailRoundId(r.id)}>
                        {t('operator:audit.detail')}
                      </button>
                      <button type="button" className="btn-link"
                              onClick={() => navigate(`/operator/replay/${r.id}`,
                                  { state: { playerId: r.playerId, createdAt: r.createdAt } })}>
                        {t('operator:audit.replay')}
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>

            <div className="pagination">
              <button type="button" className="btn-secondary"
                      disabled={f.page <= 0} onClick={() => update({ page: String(f.page - 1) }, true)}>
                {t('operator:audit.prev')}
              </button>
              <span>{t('operator:audit.pageInfo', { page: f.page + 1, total: totalPages })}</span>
              <button type="button" className="btn-secondary"
                      disabled={f.page >= totalPages - 1} onClick={() => update({ page: String(f.page + 1) }, true)}>
                {t('operator:audit.next')}
              </button>
            </div>
          </>
        )}
      </main>

      {detailRoundId != null && (
        <RoundDetailDialog roundId={detailRoundId} onClose={() => setDetailRoundId(null)} />
      )}
    </div>
  )
}
