import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import axios from 'axios'
import { useOperatorGames, useUpdateGame, type OperatorGame } from '../api/operatorApi'
import { logoutSession } from '../../shared/auth/session'
import LanguageSwitcher from '../../shared/i18n/LanguageSwitcher'
import '../players/operator.css'

/** Editable row for a single game's commercial configuration (HU-15). */
function GameRow({ game }: { game: OperatorGame }) {
  const { t } = useTranslation()
  const update = useUpdateGame()

  const [minBet, setMinBet] = useState(String(game.minBetCents))
  const [maxBet, setMaxBet] = useState(String(game.maxBetCents))
  const [step, setStep] = useState(String(game.betStepCents))
  const [currencies, setCurrencies] = useState(game.allowedCurrencies.join(', '))
  const [active, setActive] = useState(game.active)
  const [message, setMessage] = useState<{ kind: 'ok' | 'error'; text: string } | null>(null)

  const save = () => {
    setMessage(null)
    update.mutate(
      {
        gameId: game.id,
        payload: {
          minBetCents: Number(minBet),
          maxBetCents: Number(maxBet),
          betStepCents: Number(step),
          active,
          allowedCurrencies: currencies.split(',').map(c => c.trim().toUpperCase()).filter(Boolean),
        },
      },
      {
        onSuccess: () => setMessage({ kind: 'ok', text: t('operator:games.saved') }),
        onError: err => {
          const detail = axios.isAxiosError(err) && err.response?.status === 422
            ? (err.response?.data?.detail as string | undefined) ?? t('operator:games.invalid')
            : t('operator:games.error_save')
          setMessage({ kind: 'error', text: detail })
        },
      },
    )
  }

  return (
    <tr>
      <td>{game.name} <span className="muted">({game.code})</span></td>
      <td className="num">{game.paylineCount ?? '—'}</td>
      <td><input aria-label={t('operator:games.colMinBet')} className="cell-input" type="number"
                 value={minBet} onChange={e => setMinBet(e.target.value)} /></td>
      <td><input aria-label={t('operator:games.colMaxBet')} className="cell-input" type="number"
                 value={maxBet} onChange={e => setMaxBet(e.target.value)} /></td>
      <td><input aria-label={t('operator:games.colStep')} className="cell-input" type="number"
                 value={step} onChange={e => setStep(e.target.value)} /></td>
      <td><input aria-label={t('operator:games.colCurrencies')} className="cell-input" type="text"
                 value={currencies} onChange={e => setCurrencies(e.target.value)} /></td>
      <td><input aria-label={t('operator:games.colActive')} type="checkbox"
                 checked={active} onChange={e => setActive(e.target.checked)} /></td>
      <td>
        <button type="button" className="btn-link" disabled={update.isPending} onClick={save}>
          {update.isPending ? t('operator:games.saving') : t('operator:games.save')}
        </button>
        {message && (
          <span className={message.kind === 'ok' ? 'success-msg' : 'operator-error'}>{message.text}</span>
        )}
      </td>
    </tr>
  )
}

/** Operator backoffice: commercial configuration of the operator's games (HU-15). */
export default function OperatorGamesPage() {
  const { t } = useTranslation()
  const navigate = useNavigate()
  const games = useOperatorGames()

  const logout = () => { logoutSession(); navigate('/login', { replace: true }) }

  return (
    <div className="operator">
      <header className="operator-header">
        <h1>{t('operator:games.title')}</h1>
        <div className="operator-header-right">
          <button type="button" className="btn-secondary" onClick={() => navigate('/operator')}>
            {t('operator:games.toPlayers')}
          </button>
          <LanguageSwitcher />
          <button type="button" className="btn-secondary" onClick={logout}>{t('common.logout')}</button>
        </div>
      </header>

      <main className="operator-main">
        {games.isLoading && <p className="operator-msg">{t('operator:games.loading')}</p>}
        {games.isError && <p className="operator-msg operator-error">{t('operator:games.error')}</p>}
        {games.data && games.data.length === 0 && <p className="operator-msg">{t('operator:games.empty')}</p>}

        {games.data && games.data.length > 0 && (
          <table className="players-table">
            <thead>
              <tr>
                <th>{t('operator:games.colName')}</th>
                <th className="num">{t('operator:games.colPaylines')}</th>
                <th>{t('operator:games.colMinBet')}</th>
                <th>{t('operator:games.colMaxBet')}</th>
                <th>{t('operator:games.colStep')}</th>
                <th>{t('operator:games.colCurrencies')}</th>
                <th>{t('operator:games.colActive')}</th>
                <th>{t('operator:games.colActions')}</th>
              </tr>
            </thead>
            <tbody>
              {games.data.map(g => <GameRow key={g.id} game={g} />)}
            </tbody>
          </table>
        )}
      </main>
    </div>
  )
}
