import { useState } from 'react'
import { useLocation, useNavigate, useParams } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { useReplay } from '../api/operatorApi'
import { formatMoney } from '../../shared/format/money'
import LanguageSwitcher from '../../shared/i18n/LanguageSwitcher'
import SlotGame from '../../player/game/SlotGame'
import '../players/operator.css'
import './replay.css'

/** Optional context passed from the audit table (the replay endpoint does not carry player/date). */
interface ReplayNav {
  playerId?: number
  createdAt?: string
}

/**
 * Operator replay screen (`/operator/replay/{roundId}`): fetches the immutable record and renders it
 * by reusing {@code <SlotGame mode="replay">} (HU-1-FE-01) — no spin button, no bet bar. "Play again"
 * remounts the component so the exact same sequence animates every time (deterministic, AC4).
 */
export default function ReplayPage() {
  const { t, i18n } = useTranslation()
  const navigate = useNavigate()
  const { roundId } = useParams()
  const nav = (useLocation().state ?? {}) as ReplayNav

  const replay = useReplay(Number(roundId))
  const [nonce, setNonce] = useState(0)

  const notFound = replay.isError
    && (replay.error as { response?: { status?: number } })?.response?.status === 404

  return (
    <div className="operator">
      <header className="operator-header">
        <h1>{t('operator:replay.title')}</h1>
        <div className="operator-header-right">
          <button type="button" className="btn-secondary" onClick={() => navigate(-1)}>
            {t('operator:replay.back')}
          </button>
          <LanguageSwitcher />
        </div>
      </header>

      <main className="operator-main">
        {replay.isLoading && <p className="operator-msg">{t('operator:replay.loading')}</p>}
        {notFound && <p className="operator-msg operator-error">{t('operator:replay.notFound')}</p>}
        {replay.isError && !notFound && <p className="operator-msg operator-error">{t('operator:replay.error')}</p>}

        {replay.data && (
          <>
            <dl className="replay-meta">
              <div><dt>{t('operator:replay.round')}</dt><dd>{replay.data.roundId}</dd></div>
              <div><dt>{t('operator:replay.game')}</dt><dd>#{replay.data.gameId}</dd></div>
              <div><dt>{t('operator:replay.config')}</dt><dd>#{replay.data.gameConfigId}</dd></div>
              {nav.playerId != null && (
                <div><dt>{t('operator:replay.player')}</dt><dd>#{nav.playerId}</dd></div>
              )}
              {nav.createdAt && (
                <div><dt>{t('operator:replay.date')}</dt>
                  <dd>{new Date(nav.createdAt).toLocaleString(i18n.language)}</dd></div>
              )}
              <div><dt>{t('operator:replay.bet')}</dt>
                <dd>{formatMoney(replay.data.result.betCents, 'EUR', i18n.language)}</dd></div>
              <div><dt>{t('operator:replay.win')}</dt>
                <dd>{formatMoney(replay.data.result.winCents, 'EUR', i18n.language)}</dd></div>
            </dl>

            {/* key={nonce} remounts → the recorded sequence animates again, identically (AC4). */}
            <SlotGame
              key={nonce}
              mode="replay"
              config={replay.data.config}
              theme={String(replay.data.gameId)}
              result={replay.data.result}
            />

            <button type="button" className="btn-primary replay-again" onClick={() => setNonce(n => n + 1)}>
              {t('operator:replay.playAgain')}
            </button>
          </>
        )}
      </main>
    </div>
  )
}
