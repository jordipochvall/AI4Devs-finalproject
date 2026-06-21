import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import axios from 'axios'
import { useGenerateRfj, type RfjReport } from '../api/operatorApi'
import { logoutSession } from '../../shared/auth/session'
import { formatMoney } from '../../shared/format/money'
import LanguageSwitcher from '../../shared/i18n/LanguageSwitcher'
import '../players/operator.css'

/** Operator RFJ regulatory reports (HU-21): pick a month, generate, view aggregates + integrity. */
export default function OperatorReportsPage() {
  const { t, i18n } = useTranslation()
  const navigate = useNavigate()
  const now = new Date()
  const [year, setYear] = useState(String(now.getFullYear()))
  const [month, setMonth] = useState(String(now.getMonth() + 1))
  const [report, setReport] = useState<RfjReport | null>(null)
  const [error, setError] = useState<string | null>(null)
  const generate = useGenerateRfj()

  const logout = () => { logoutSession(); navigate('/login', { replace: true }) }

  const submit = () => {
    setError(null); setReport(null)
    generate.mutate(
      { year: Number(year), month: Number(month) },
      {
        onSuccess: setReport,
        onError: err => {
          if (axios.isAxiosError(err) && err.response?.status === 422) {
            const id = err.response?.data?.firstBrokenRoundId
            setError(t('operator:reports.blocked', { id: id ?? '?' }))
          } else {
            setError(t('operator:reports.error'))
          }
        },
      },
    )
  }

  return (
    <div className="operator">
      <header className="operator-header">
        <h1>{t('operator:reports.title')}</h1>
        <div className="operator-header-right">
          <button type="button" className="btn-secondary" onClick={() => navigate('/operator')}>
            {t('operator:reports.toPlayers')}
          </button>
          <LanguageSwitcher />
          <button type="button" className="btn-secondary" onClick={logout}>{t('common.logout')}</button>
        </div>
      </header>

      <main className="operator-main">
        <div className="dashboard-filters">
          <label>{t('operator:reports.year')}
            <input type="number" value={year} onChange={e => setYear(e.target.value)} />
          </label>
          <label>{t('operator:reports.month')}
            <input type="number" min="1" max="12" value={month} onChange={e => setMonth(e.target.value)} />
          </label>
          <button type="button" className="btn-primary" disabled={generate.isPending} onClick={submit}>
            {generate.isPending ? t('operator:reports.generating') : t('operator:reports.generate')}
          </button>
        </div>

        {error && <p className="operator-error">{error}</p>}

        {report && (
          <table className="players-table">
            <tbody>
              <tr><td>{t('operator:reports.rounds')}</td><td className="num">{report.totalRounds}</td></tr>
              <tr><td>{t('operator:reports.activePlayers')}</td><td className="num">{report.activePlayers}</td></tr>
              <tr><td>{t('operator:reports.wagered')}</td><td className="num">{formatMoney(report.totalWageredCents, 'EUR', i18n.language)}</td></tr>
              <tr><td>{t('operator:reports.won')}</td><td className="num">{formatMoney(report.totalWonCents, 'EUR', i18n.language)}</td></tr>
              <tr><td>{t('operator:reports.ggr')}</td><td className="num">{formatMoney(report.ggrCents, 'EUR', i18n.language)}</td></tr>
              <tr><td>{t('operator:reports.integrity')}</td>
                  <td>{report.integrityConsistent
                        ? `✓ ${t('operator:reports.intact', { checked: report.integrityChecked })}`
                        : '✗'}</td></tr>
            </tbody>
          </table>
        )}
      </main>
    </div>
  )
}
