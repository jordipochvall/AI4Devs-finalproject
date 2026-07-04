import { useEffect, useMemo, useRef, useState, type CSSProperties } from 'react'
import { useTranslation } from 'react-i18next'
import type { GameConfig, SpinResult } from '../api/playerApi'
import { formatMoney } from '../../shared/format/money'
import './slotGame.css'

/** Props of the data-driven slot component. */
export interface SlotGameProps {
  /** Game math/structure; drives the whole render (readme §3.3). */
  config: GameConfig
  /** Theme id used to locate symbol assets under {@code /assets/<theme>/}. */
  theme: string
  /** "play" shows the spin controls; "replay" only renders/animates a given result (AC6, HU-3). */
  mode?: 'play' | 'replay'
  /** Latest spin result to render and animate (base spin + free-spins cinematic). */
  result?: SpinResult | null
  /** Whether a spin is in flight (disables the button and shows the spinning state). */
  spinning?: boolean
  /** Error to show without altering the grid (e.g. insufficient balance, AC4). */
  errorMessage?: string | null
  // --- play-mode controls ---
  betCents?: number
  minBetCents?: number
  maxBetCents?: number
  betStepCents?: number
  currency?: string
  onBetChange?: (cents: number) => void
  onSpinClick?: () => void
  /** Per-step animation delay in ms (free-spin cinematic); 0 in tests for determinism. */
  stepMs?: number
  // --- auto-spin (HU-9) ---
  /** Spins left in the current auto-spin batch, or {@code null} when not auto-spinning. */
  autoRemaining?: number | null
  /** Whether auto-spin stopped on a responsible-gaming safeguard (shows a pause message). */
  autoPaused?: boolean
  /** Starts an auto-spin batch of {@code spins}, stopping if the balance drops below {@code stopBelowCents}. */
  onAutoStart?: (spins: number, stopBelowCents: number) => void
  /** Stops the auto-spin batch after the spin in progress. */
  onAutoStop?: () => void
}

/** Initial visible grid before the first spin: the top {@code rows} of each reel. */
function buildInitialView(config: GameConfig): string[][] {
  const { cols, rows } = config.grid
  const view: string[][] = []
  for (let c = 0; c < cols; c++) {
    const strip = config.reels[c] ?? []
    const column: string[] = []
    for (let r = 0; r < rows; r++) {
      column.push(strip[r % Math.max(strip.length, 1)] ?? '')
    }
    view.push(column)
  }
  return view
}

const delay = (ms: number) => new Promise<void>(resolve => setTimeout(resolve, ms))

/** A single symbol tile: themed image with a text fallback if the asset is missing. */
function SymbolTile({ symbol, theme }: { symbol: string; theme: string }) {
  return (
    <>
      <img
        className="slot-cell-img"
        src={`/assets/${theme.toLowerCase()}/${symbol.toLowerCase()}.png`}
        alt=""
        aria-hidden="true"
        onError={e => { (e.target as HTMLImageElement).style.display = 'none' }}
      />
      <span className="slot-cell-label">{symbol}</span>
    </>
  )
}

/**
 * Data-driven slot game: renders any game by interpreting its {@code config} (grid, symbols,
 * paylines), animates the reels (vertical roll with a staggered stop), highlights the winning
 * paylines and plays the free-spins cinematic. It is presentational and game-agnostic (AC5): only
 * the {@code config}/assets change. The spin call itself is owned by the page and wired through
 * {@code onSpinClick}; in {@code mode="replay"} the controls are hidden and it just plays back the
 * provided {@code result} (AC6).
 */
export default function SlotGame({
  config, theme, mode = 'play', result, spinning = false, errorMessage,
  betCents, minBetCents, maxBetCents, betStepCents, currency = 'EUR',
  onBetChange, onSpinClick, stepMs = 700,
  autoRemaining = null, autoPaused = false, onAutoStart, onAutoStop,
}: SlotGameProps) {
  const { t, i18n } = useTranslation()
  const { cols, rows } = config.grid
  const [autoCount, setAutoCount] = useState(10)
  const [stopBelowEuros, setStopBelowEuros] = useState('')

  const initialView = useMemo(() => buildInitialView(config), [config])
  const [view, setView] = useState<string[][]>(result?.view ?? initialView)
  const [highlightedLines, setHighlightedLines] = useState<number[]>([])
  const [freeSpinLabel, setFreeSpinLabel] = useState<string | null>(null)
  const [winCents, setWinCents] = useState<number>(0)
  /** Monotonic token so a new result cancels the previous (in-flight) animation. */
  const animationToken = useRef(0)

  // Reel-roll overlay: mounted while rolling; on stop each reel settles staggered (left→right).
  const [rolling, setRolling] = useState(false)
  const [stopping, setStopping] = useState(false)
  useEffect(() => {
    if (spinning) {
      setRolling(true)
      setStopping(false)
      return
    }
    if (!rolling) return
    // Spin finished: play the staggered settle, then unmount the overlay.
    setStopping(true)
    const t = window.setTimeout(() => { setRolling(false); setStopping(false) }, cols * 110 + 380)
    return () => window.clearTimeout(t)
  }, [spinning, rolling, cols])

  // A short strip of symbols to scroll while a reel is rolling (visual only; blurred).
  const reelFillers = useMemo(() => {
    const ids = config.symbols.map(s => s.id).filter(Boolean)
    const pool = ids.length ? ids : ['']
    return Array.from({ length: rows * 3 }, (_, i) => pool[(i * 3 + 1) % pool.length])
  }, [config.symbols, rows])

  // Animate whenever a new result arrives: base spin, then each free spin in sequence.
  useEffect(() => {
    if (!result) return
    const token = ++animationToken.current

    setFreeSpinLabel(null)
    setView(result.view)
    setHighlightedLines(result.winningPaylines.map(line => line.paylineIndex))
    setWinCents(result.winCents)

    if (result.freeSpins.triggered && result.freeSpins.rounds.length > 0) {
      const rounds = result.freeSpins.rounds
      void (async () => {
        for (let i = 0; i < rounds.length; i++) {
          await delay(stepMs)
          if (animationToken.current !== token) return // superseded by a newer spin
          const round = rounds[i]
          setFreeSpinLabel(t('player:game.freeSpin', { current: i + 1, total: rounds.length }))
          setView(round.view)
          setHighlightedLines(round.winningPaylines.map(line => line.paylineIndex))
        }
      })()
    }
  }, [result, stepMs, t])

  // Set of "col,row" cells that belong to a highlighted payline.
  const highlightedCells = useMemo(() => {
    const cells = new Set<string>()
    for (const lineIndex of highlightedLines) {
      const line = config.paylines[lineIndex]
      if (!line) continue
      for (let c = 0; c < line.length; c++) {
        cells.add(`${c},${line[c]}`)
      }
    }
    return cells
  }, [highlightedLines, config.paylines])

  const adjustBet = (deltaSteps: number) => {
    if (betCents === undefined || !onBetChange) return
    const step = betStepCents ?? 1
    const next = betCents + deltaSteps * step
    const min = minBetCents ?? step
    const max = maxBetCents ?? next
    onBetChange(Math.min(max, Math.max(min, next)))
  }

  return (
    <div className="slot-game">
      <div className="slot-stage">
        <div
          className={`slot-grid${spinning ? ' slot-grid-spinning' : ''}`}
          style={{
            gridTemplateColumns: `repeat(${cols}, 1fr)`,
            '--cols': cols,
            '--rows': rows,
          } as CSSProperties}
          data-testid="slot-grid"
          role="grid"
          aria-label={t('player:game.grid')}
          aria-busy={spinning}
        >
          {Array.from({ length: cols }).map((_, col) => (
            <div key={col} className="slot-reel" style={{ '--reel': col } as CSSProperties}>
              {/* Settled symbols (always present so the result is readable and tests are stable). */}
              <div className="reel-strip">
                {Array.from({ length: rows }).map((_, row) => {
                  const symbol = view[col]?.[row] ?? ''
                  const won = highlightedCells.has(`${col},${row}`)
                  return (
                    <div
                      key={`${col}-${row}`}
                      className={`slot-cell${won ? ' slot-cell-win' : ''}`}
                      data-symbol={symbol}
                      role="gridcell"
                    >
                      <SymbolTile symbol={symbol} theme={theme} />
                    </div>
                  )
                })}
              </div>

              {/* Rolling overlay: a blurred strip that scrolls, then slides away on stop. */}
              {rolling && (
                <div className={`reel-fx${stopping ? ' reel-fx-stop' : ''}`} aria-hidden="true">
                  <div className="reel-fx-strip">
                    {reelFillers.map((sym, i) => (
                      <div key={i} className="reel-fx-cell">
                        <SymbolTile symbol={sym} theme={theme} />
                      </div>
                    ))}
                  </div>
                </div>
              )}
            </div>
          ))}
        </div>
      </div>

      {/* Reserved-height status area so appearing/disappearing messages never move the controls. */}
      <div className="slot-status">
        {freeSpinLabel && (
          <div className="slot-freespin" role="status">{freeSpinLabel}</div>
        )}
        {winCents > 0 && (
          <div className="slot-win" role="status">
            {t('player:game.win', { amount: formatMoney(winCents, currency, i18n.language) })}
          </div>
        )}
        {errorMessage && (
          <div className="slot-error" role="alert">{errorMessage}</div>
        )}
        {autoPaused && (
          <div className="slot-error" role="alert">{t('player:game.autoPaused')}</div>
        )}
      </div>

      {mode === 'play' && (
        <div className="slot-controls">
          <div className="slot-bet" role="group" aria-label={t('player:game.bet')}>
            <button type="button" className="slot-bet-btn" onClick={() => adjustBet(-1)}
                    disabled={spinning || betCents === undefined || betCents <= (minBetCents ?? 0)}
                    aria-label={t('player:game.betDown')}>−</button>
            <span className="slot-bet-value num">
              {t('player:game.bet')}: {betCents !== undefined
                ? formatMoney(betCents, currency, i18n.language) : '—'}
            </span>
            <button type="button" className="slot-bet-btn" onClick={() => adjustBet(1)}
                    disabled={spinning || betCents === undefined || betCents >= (maxBetCents ?? Infinity)}
                    aria-label={t('player:game.betUp')}>+</button>
          </div>

          <button type="button" className="slot-spin" onClick={onSpinClick}
                  disabled={spinning || autoRemaining != null}>
            {spinning ? t('player:game.spinning') : t('player:game.spin')}
          </button>

          {/* Auto-spin (HU-9) */}
          {autoRemaining == null ? (
            <div className="slot-auto" role="group" aria-label={t('player:game.auto')}>
              <input className="slot-auto-count" type="number" min={1} max={1000}
                     aria-label={t('player:game.autoCount')}
                     value={autoCount} onChange={e => setAutoCount(Number(e.target.value))} />
              <input className="slot-auto-stop" type="number" min={0} step="0.01"
                     placeholder={t('player:game.autoStopBelow')}
                     aria-label={t('player:game.autoStopBelow')}
                     value={stopBelowEuros} onChange={e => setStopBelowEuros(e.target.value)} />
              <button type="button" className="slot-auto-btn" disabled={spinning}
                      onClick={() => onAutoStart?.(autoCount,
                          stopBelowEuros.trim() === '' ? 0 : Math.round(parseFloat(stopBelowEuros) * 100))}>
                {t('player:game.auto')}
              </button>
            </div>
          ) : (
            <button type="button" className="slot-auto-btn slot-auto-stopbtn" onClick={onAutoStop}>
              {t('player:game.autoStop', { remaining: autoRemaining })}
            </button>
          )}
        </div>
      )}
    </div>
  )
}
