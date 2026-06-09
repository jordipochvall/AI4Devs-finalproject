import { useState } from 'react'
import { useTranslation } from 'react-i18next'
import axios from 'axios'
import { useLaunchSimulation, useSimulation, type SimulationStatus } from '../api/mathApi'
import ExplainBox from './ExplainBox'
import './simulation.css'

interface SimulationPanelProps {
  /** Config version to simulate. */
  configId: number
  /** Target RTP declared by the mathematician (for the comparison badge). */
  rtpTarget: number | null
  /** The config JSON (used to derive reel-strip statistics, which are static). */
  config: unknown
}

const DEFAULT_SPINS = 10_000_000
const Z_95 = 1.96 // 95% confidence interval half-width factor

const pct = (v: number | null | undefined, digits = 2) =>
  v == null ? '—' : `${(v * 100).toFixed(digits)}%`
const num = (v: number | null | undefined, digits = 2) =>
  v == null ? '—' : v.toFixed(digits)

/**
 * Math simulation panel: launches a run, polls until it finishes (TanStack Query) and renders the
 * metrics dashboard — RTP with its confidence interval and comparison against the declared target,
 * convergence curve, RTP decomposition, histogram and reel-strip statistics (HU-2-FE-01, C3).
 */
export default function SimulationPanel({ configId, rtpTarget, config }: SimulationPanelProps) {
  const { t } = useTranslation()
  const [numSpins, setNumSpins] = useState(DEFAULT_SPINS)
  const [betCents, setBetCents] = useState(100)
  const [simulationId, setSimulationId] = useState<number | null>(null)

  const launch = useLaunchSimulation()
  const sim = useSimulation(simulationId)

  const start = () => {
    setSimulationId(null)
    launch.mutate({ configId, numSpins, betCents }, {
      onSuccess: r => setSimulationId(r.simulationId),
    })
  }

  const launchError = launch.isError
    ? (axios.isAxiosError(launch.error) && launch.error.response?.status === 422
        ? t('math:sim.invalidParams') : t('math:sim.launchError'))
    : null

  const data = sim.data
  const running = data?.status === 'RUNNING'

  return (
    <section className="sim">
      <h2>{t('math:sim.title')}</h2>

      <div className="sim-controls">
        <div className="field">
          <label htmlFor="numSpins">{t('math:sim.numSpins')}</label>
          <input id="numSpins" type="number" min={1} max={DEFAULT_SPINS}
                 value={numSpins} onChange={e => setNumSpins(Number(e.target.value))} />
        </div>
        <div className="field">
          <label htmlFor="betCents">{t('math:sim.betCents')}</label>
          <input id="betCents" type="number" min={1}
                 value={betCents} onChange={e => setBetCents(Number(e.target.value))} />
        </div>
        <button type="button" className="btn-primary" onClick={start}
                disabled={launch.isPending || running}>
          {launch.isPending || running ? t('math:sim.running') : t('math:sim.run')}
        </button>
      </div>

      {launchError && <p className="server-error">{launchError}</p>}

      {running && (
        <div className="sim-running" role="status" aria-live="polite">
          <span className="sim-spinner" aria-hidden="true" />
          {t('math:sim.runningHint', { spins: data!.numSpins.toLocaleString() })}
        </div>
      )}

      {data?.status === 'FAILED' && (
        <p className="server-error" role="alert">{t('math:sim.failed')}: {data.errorMessage}</p>
      )}

      {data?.status === 'COMPLETED' && (
        <Dashboard data={data} rtpTarget={rtpTarget} config={config} />
      )}
    </section>
  )
}

/** Renders the completed-simulation metrics. */
function Dashboard({ data, rtpTarget, config }:
                   { data: SimulationStatus; rtpTarget: number | null; config: unknown }) {
  const { t } = useTranslation()
  const rtp = data.rtpEmpirical ?? 0
  const ci = (data.rtpStdError ?? 0) * Z_95
  const verdict = compareToTarget(rtp, ci, rtpTarget)

  return (
    <div className="sim-dashboard" data-testid="sim-dashboard">
      {/* RTP headline with confidence interval and target comparison */}
      <div className={`sim-rtp sim-verdict-${verdict}`}>
        <span className="sim-rtp-label">{t('math:sim.rtp')}</span>
        <span className="sim-rtp-value">{pct(rtp)}</span>
        <span className="sim-rtp-ci">± {pct(ci)} ({t('math:sim.ci95')})</span>
        <span className="sim-rtp-target">
          {t('math:sim.target')}: {rtpTarget != null ? pct(rtpTarget) : '—'} · {t(`math:sim.verdict.${verdict}`)}
        </span>
      </div>

      {/* Metric grid */}
      <div className="sim-metrics">
        <Metric label={t('math:sim.rtpBase')} value={pct(data.rtpBaseGame)} />
        <Metric label={t('math:sim.rtpFree')} value={pct(data.rtpFreeSpins)} />
        <Metric label={t('math:sim.hitFreq')} value={pct(data.hitFrequency)} />
        <Metric label={t('math:sim.volatility')} value={num(data.volatility)} />
        <Metric label={t('math:sim.maxWin')} value={`${num(data.maxWinMultiplier)}×`} />
        <Metric label={t('math:sim.triggerFreq')} value={pct(data.freeSpinTriggerFreq)} />
        <Metric label={t('math:sim.dryStreak')} value={String(data.longestDryStreak ?? '—')} />
        <Metric label={t('math:sim.duration')} value={`${data.durationMs ?? '—'} ms`} />
      </div>

      {data.convergenceSample && data.convergenceSample.length > 1 && (
        <div className="sim-block">
          <h3>{t('math:sim.convergence')}</h3>
          <ConvergenceChart points={data.convergenceSample} target={rtpTarget} />
        </div>
      )}

      {data.rtpBreakdown && (
        <div className="sim-block">
          <h3>{t('math:sim.breakdown')}</h3>
          <BySymbol bySymbol={data.rtpBreakdown.bySymbol} />
        </div>
      )}

      {data.prizeDistribution && (
        <div className="sim-block">
          <h3>{t('math:sim.histogram')}</h3>
          <Histogram distribution={data.prizeDistribution} />
        </div>
      )}

      <div className="sim-block">
        <h3>{t('math:sim.reelStats')}</h3>
        <ReelStats config={config} />
      </div>

      {/* AI explainability over the completed simulation (HU-8). */}
      <ExplainBox simulationId={data.simulationId} />
    </div>
  )
}

function Metric({ label, value }: { label: string; value: string }) {
  return (
    <div className="sim-metric">
      <span className="sim-metric-label">{label}</span>
      <span className="sim-metric-value">{value}</span>
    </div>
  )
}

/** Green if the target is inside the 95% CI; amber if within 2 pp; red otherwise. */
function compareToTarget(rtp: number, ci: number, target: number | null): 'green' | 'amber' | 'red' | 'na' {
  if (target == null) return 'na'
  const delta = Math.abs(rtp - target)
  if (delta <= ci) return 'green'
  if (delta <= 0.02) return 'amber'
  return 'red'
}

/** Simple SVG line chart of the RTP convergence (dependency-free). */
function ConvergenceChart({ points, target }: { points: { spins: number; rtp: number }[]; target: number | null }) {
  const w = 480
  const h = 160
  const pad = 28
  const rtps = points.map(p => p.rtp)
  const min = Math.min(...rtps, target ?? Infinity)
  const max = Math.max(...rtps, target ?? -Infinity)
  const span = max - min || 1
  const x = (i: number) => pad + (i / (points.length - 1)) * (w - 2 * pad)
  const y = (v: number) => h - pad - ((v - min) / span) * (h - 2 * pad)
  const path = points.map((p, i) => `${i === 0 ? 'M' : 'L'} ${x(i).toFixed(1)} ${y(p.rtp).toFixed(1)}`).join(' ')

  return (
    <svg className="sim-chart" viewBox={`0 0 ${w} ${h}`} role="img" aria-label="RTP convergence">
      {target != null && (
        <line x1={pad} x2={w - pad} y1={y(target)} y2={y(target)} className="sim-chart-target" />
      )}
      <path d={path} className="sim-chart-line" fill="none" />
    </svg>
  )
}

/** Horizontal bars of each symbol's RTP contribution. */
function BySymbol({ bySymbol }: { bySymbol: Record<string, number> }) {
  const entries = Object.entries(bySymbol).sort((a, b) => b[1] - a[1])
  const max = Math.max(...entries.map(e => e[1]), 1e-9)
  return (
    <div className="sim-bars">
      {entries.map(([symbol, rtp]) => (
        <div key={symbol} className="sim-bar-row">
          <span className="sim-bar-label">{symbol}</span>
          <span className="sim-bar"><span className="sim-bar-fill" style={{ width: `${(rtp / max) * 100}%` }} /></span>
          <span className="sim-bar-value">{pct(rtp)}</span>
        </div>
      ))}
    </div>
  )
}

/** Bars of the prize-multiplier histogram (round counts per bucket). */
function Histogram({ distribution }: { distribution: Record<string, number> }) {
  const entries = Object.entries(distribution)
  const max = Math.max(...entries.map(e => e[1]), 1)
  return (
    <div className="sim-bars">
      {entries.map(([bucket, count]) => (
        <div key={bucket} className="sim-bar-row">
          <span className="sim-bar-label">{bucket}</span>
          <span className="sim-bar"><span className="sim-bar-fill" style={{ width: `${(count / max) * 100}%` }} /></span>
          <span className="sim-bar-value">{count.toLocaleString()}</span>
        </div>
      ))}
    </div>
  )
}

/** Per-reel symbol frequency, derived client-side from the (static) config strips. */
function ReelStats({ config }: { config: unknown }) {
  const reels = (config as { reels?: string[][] })?.reels
  if (!reels) return null
  return (
    <div className="sim-reels">
      {reels.map((strip, i) => {
        const counts: Record<string, number> = {}
        strip.forEach(s => { counts[s] = (counts[s] ?? 0) + 1 })
        return (
          <div key={i} className="sim-reel">
            <span className="sim-reel-label">#{i + 1}</span>
            {Object.entries(counts).map(([sym, c]) => (
              <span key={sym} className="sim-reel-freq">{sym}: {((c / strip.length) * 100).toFixed(0)}%</span>
            ))}
          </div>
        )
      })}
    </div>
  )
}
