import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { usePlayers, type PlayerSummary } from '../api/operatorApi'
import { useAuthStore } from '../../shared/auth/authStore'
import { formatMoney } from '../../shared/format/money'
import LanguageSwitcher from '../../shared/i18n/LanguageSwitcher'
import RechargeDialog from './RechargeDialog'
import './operator.css'

/** Operator backoffice: paginated player search with balances and a recharge dialog. */
export default function OperatorPlayersPage() {
  const { t, i18n } = useTranslation()
  const navigate = useNavigate()
  const clearAuth = useAuthStore(s => s.clearAuth)

  const [email, setEmail] = useState('')
  const [page, setPage] = useState(0)
  const [selected, setSelected] = useState<PlayerSummary | null>(null)

  const players = usePlayers(email, page)

  const logout = () => { clearAuth(); navigate('/login', { replace: true }) }

  const onSearch = (value: string) => { setEmail(value); setPage(0) }

  const data = players.data
  const totalPages = data?.totalPages ?? 0

  return (
    <div className="operator">
      <header className="operator-header">
        <h1>{t('operator:players.title')}</h1>
        <div className="operator-header-right">
          <button type="button" className="btn-secondary" onClick={() => navigate('/operator/audit')}>
            {t('operator:players.toAudit')}
          </button>
          <LanguageSwitcher />
          <button type="button" className="btn-secondary" onClick={logout}>{t('common.logout')}</button>
        </div>
      </header>

      <main className="operator-main">
        <input
          className="search-input"
          type="search"
          placeholder={t('operator:players.searchPlaceholder')}
          value={email}
          onChange={e => onSearch(e.target.value)}
        />

        {players.isLoading && <p className="operator-msg">{t('operator:players.loading')}</p>}
        {players.isError && <p className="operator-msg operator-error">{t('operator:players.error')}</p>}
        {data && data.content.length === 0 && <p className="operator-msg">{t('operator:players.empty')}</p>}

        {data && data.content.length > 0 && (
          <>
            <table className="players-table">
              <thead>
                <tr>
                  <th>{t('operator:players.colEmail')}</th>
                  <th>{t('operator:players.colLanguage')}</th>
                  <th>{t('operator:players.colStatus')}</th>
                  <th className="num">{t('operator:players.colBalance')}</th>
                  <th>{t('operator:players.colActions')}</th>
                </tr>
              </thead>
              <tbody>
                {data.content.map(p => (
                  <tr key={p.id}>
                    <td>{p.email}</td>
                    <td>{p.locale.toUpperCase()}</td>
                    <td>{p.active ? t('operator:players.active') : t('operator:players.inactive')}</td>
                    <td className="num">
                      {p.balanceCents != null && p.currency
                        ? formatMoney(p.balanceCents, p.currency, i18n.language)
                        : '—'}
                    </td>
                    <td>
                      <button type="button" className="btn-link" onClick={() => setSelected(p)}>
                        {t('operator:players.recharge')}
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>

            <div className="pagination">
              <button type="button" className="btn-secondary"
                      disabled={page <= 0} onClick={() => setPage(p => p - 1)}>
                {t('operator:players.prev')}
              </button>
              <span>{t('operator:players.pageInfo', { page: page + 1, total: totalPages })}</span>
              <button type="button" className="btn-secondary"
                      disabled={page >= totalPages - 1} onClick={() => setPage(p => p + 1)}>
                {t('operator:players.next')}
              </button>
            </div>
          </>
        )}
      </main>

      {selected && <RechargeDialog player={selected} onClose={() => setSelected(null)} />}
    </div>
  )
}
