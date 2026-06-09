import { useEffect, useRef, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import playerApi, { useGame, useWallet, type SpinResult } from '../api/playerApi'
import { useAuthStore } from '../../shared/auth/authStore'
import { useSessionGuard } from '../../shared/compliance/sessionGuardStore'
import { useAudioStore } from '../../shared/audio/audioStore'
import { useGameAudio } from '../../shared/audio/useGameAudio'
import { formatMoney } from '../../shared/format/money'
import LanguageSwitcher from '../../shared/i18n/LanguageSwitcher'
import ComplianceBanner from '../../shared/compliance/ComplianceBanner'
import SlotGame from './SlotGame'
import { nextAutoDecision } from './autoSpin'
import './gamePage.css'

/** Route component for /play/:gameId — loads the game + wallet and wires the spin to {@link SlotGame}. */
export default function GamePage() {
  const { gameId } = useParams()
  const id = Number(gameId)
  const { t, i18n } = useTranslation()
  const navigate = useNavigate()
  const clearAuth = useAuthStore(s => s.clearAuth)
  const recordRound = useSessionGuard(s => s.recordRound)
  const muted = useAudioStore(s => s.muted)
  const toggleMute = useAudioStore(s => s.toggleMute)
  const queryClient = useQueryClient()

  const game = useGame(id)
  const wallet = useWallet()

  const [betCents, setBetCents] = useState<number | undefined>(undefined)
  const [result, setResult] = useState<SpinResult | null>(null)
  const [errorMessage, setErrorMessage] = useState<string | null>(null)

  // --- auto-spin state (HU-9) ---
  const [autoRemaining, setAutoRemaining] = useState<number | null>(null)
  const [autoPaused, setAutoPaused] = useState(false)
  // Refs so the mutation's onSuccess always reads the current auto-spin state.
  const autoActive = useRef(false)
  const autoRemainingRef = useRef(0)
  const stopBelowRef = useRef(0)

  const stopAuto = (paused: boolean) => {
    autoActive.current = false
    setAutoRemaining(null)
    setAutoPaused(paused)
  }

  // Initialise the bet to the game's minimum once the detail is loaded.
  useEffect(() => {
    if (game.data && betCents === undefined) {
      setBetCents(game.data.minBetCents)
    }
  }, [game.data, betCents])

  const spin = useMutation({
    mutationFn: () =>
      playerApi.spin(id, betCents!, wallet.data?.currency ?? 'EUR', crypto.randomUUID()),
    onSuccess: res => {
      setErrorMessage(null)
      setResult(res)
      recordRound(res.betCents, res.winCents) // feeds the responsible-gaming loss guard (HU-12)
      queryClient.invalidateQueries({ queryKey: ['player', 'wallet'] })

      // Auto-spin chaining + safeguards: decide from the spin's own post-balance.
      if (autoActive.current) {
        const remaining = autoRemainingRef.current - 1
        const decision = nextAutoDecision(
          remaining, res.balancePostCents, betCents ?? 0, stopBelowRef.current)
        if (decision === 'continue') {
          autoRemainingRef.current = remaining
          setAutoRemaining(remaining)
          spin.mutate()
        } else {
          stopAuto(decision === 'insufficient' || decision === 'threshold')
        }
      }
    },
    onError: (err: unknown) => {
      const status = (err as { response?: { status?: number } })?.response?.status
      // Do not touch `result`: the grid stays as it was (AC4).
      setErrorMessage(status === 422
        ? t('player:game.insufficientOrInvalid')
        : t('player:game.spinError'))
      stopAuto(false) // an error halts the batch
    },
  })

  /** Starts an auto-spin batch (AC1); refuses to start if the balance is below the bet (AC5). */
  const startAuto = (spins: number, stopBelowCents: number) => {
    const balance = wallet.data?.balanceCents ?? 0
    if (betCents == null || balance < betCents) {
      setAutoPaused(true)
      return
    }
    autoActive.current = true
    autoRemainingRef.current = spins
    stopBelowRef.current = stopBelowCents
    setAutoPaused(false)
    setAutoRemaining(spins)
    spin.mutate()
  }

  const stopAutoManually = () => stopAuto(false) // AC4

  // Immersive audio layer (HU-10): music per theme, SFX/voice per event, honoring the mute toggle.
  useGameAudio(game.data?.theme ?? '', result, betCents ?? 0, spin.isPending)

  const logout = () => { clearAuth(); navigate('/login', { replace: true }) }

  return (
    <div className="game-page">
      <header className="game-header">
        <button type="button" className="game-back" onClick={() => navigate('/')}>
          ← {t('player:game.back')}
        </button>
        <h1>{game.data?.name ?? t('player:game.title')}</h1>
        <div className="game-header-right">
          <span className="game-balance">
            {t('player:lobby.balance')}:{' '}
            <strong>
              {wallet.data
                ? formatMoney(wallet.data.balanceCents, wallet.data.currency, i18n.language)
                : '—'}
            </strong>
          </span>
          <button type="button" className="game-mute" onClick={toggleMute}
                  aria-label={muted ? t('player:game.unmute') : t('player:game.mute')}
                  aria-pressed={muted}>
            {muted ? '🔇' : '🔊'}
          </button>
          <LanguageSwitcher />
          <button type="button" className="game-logout" onClick={logout}>
            {t('common.logout')}
          </button>
        </div>
      </header>

      <main className="game-main">
        {game.isLoading && <p className="game-msg">{t('player:game.loading')}</p>}
        {game.isError && <p className="game-msg game-error">{t('player:game.loadError')}</p>}

        {game.data && (
          <SlotGame
            config={game.data.config}
            theme={game.data.theme}
            mode="play"
            result={result}
            spinning={spin.isPending}
            errorMessage={errorMessage}
            betCents={betCents}
            minBetCents={game.data.minBetCents}
            maxBetCents={game.data.maxBetCents}
            betStepCents={game.data.betStepCents}
            currency={wallet.data?.currency ?? 'EUR'}
            onBetChange={setBetCents}
            onSpinClick={() => spin.mutate()}
            autoRemaining={autoRemaining}
            autoPaused={autoPaused}
            onAutoStart={startAuto}
            onAutoStop={stopAutoManually}
          />
        )}
      </main>

      <ComplianceBanner />
    </div>
  )
}
