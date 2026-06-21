import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import axios from 'axios'
import { useMathGames, useConfig, useCreateConfig } from '../api/mathApi'
import { logoutSession } from '../../shared/auth/session'
import LanguageSwitcher from '../../shared/i18n/LanguageSwitcher'
import SimulationPanel from '../sim/SimulationPanel'
import VersionsPanel from './VersionsPanel'
import SimulationHistoryPanel from './SimulationHistoryPanel'
import './math.css'

interface FieldError { field: string; message: string }

/**
 * Math editor: pick a game, edit its config JSON, declare the target RTP/volatility and save
 * a new version. Shows client-side validation and the backend's errors[] on a 422.
 */
export default function MathEditorPage() {
  const { t } = useTranslation()
  const navigate = useNavigate()

  const games = useMathGames()
  const [gameId, setGameId] = useState<number | null>(null)

  const selectedGame = games.data?.find(g => g.id === gameId) ?? null
  const config = useConfig(selectedGame?.activeConfigId ?? null)
  const create = useCreateConfig()

  const [configText, setConfigText] = useState('')
  const [rtp, setRtp] = useState('')
  const [volatility, setVolatility] = useState('')
  const [notes, setNotes] = useState('')

  const [clientError, setClientError] = useState<string | null>(null)
  const [serverErrors, setServerErrors] = useState<FieldError[] | null>(null)
  const [success, setSuccess] = useState<string | null>(null)

  // When the selected game's active config loads, populate the editor.
  useEffect(() => {
    if (config.data) {
      setConfigText(JSON.stringify(config.data.config, null, 2))
      setRtp(String(config.data.rtpTarget))
      setVolatility(config.data.volatilityTarget != null ? String(config.data.volatilityTarget) : '')
      setNotes(config.data.notes ?? '')
      setClientError(null); setServerErrors(null); setSuccess(null)
    }
  }, [config.data])

  const logout = () => { logoutSession(); navigate('/login', { replace: true }) }

  const save = () => {
    setClientError(null); setServerErrors(null); setSuccess(null)
    if (gameId == null) return

    let parsed: unknown
    try {
      parsed = JSON.parse(configText)
    } catch {
      setClientError(t('math:editor.invalidJson'))
      return
    }

    const rtpNum = parseFloat(rtp)
    if (!Number.isFinite(rtpNum) || rtpNum < 0 || rtpNum > 1) {
      setClientError(t('math:editor.rtpRange'))
      return
    }
    const volNum = volatility.trim() === '' ? null : parseFloat(volatility)

    create.mutate(
      { gameId, config: parsed, rtpTarget: rtpNum, volatilityTarget: volNum, notes },
      {
        onSuccess: data => setSuccess(t('math:editor.saved', { version: data.version, rtp: data.rtpTarget })),
        onError: err => {
          if (axios.isAxiosError(err) && err.response?.status === 422 && err.response.data?.errors) {
            setServerErrors(err.response.data.errors as FieldError[])
          } else {
            setClientError(t('math:editor.genericError'))
          }
        },
      },
    )
  }

  return (
    <div className="math">
      <header className="math-header">
        <h1>{t('math:editor.title')}</h1>
        <div className="math-header-right">
          <LanguageSwitcher />
          <button type="button" className="btn-secondary" onClick={logout}>{t('common.logout')}</button>
        </div>
      </header>

      <main className="math-main">
        <div className="field">
          <label htmlFor="game">{t('math:editor.selectGame')}</label>
          <select id="game" value={gameId ?? ''}
                  onChange={e => setGameId(e.target.value ? Number(e.target.value) : null)}>
            <option value="">{t('math:editor.selectGamePlaceholder')}</option>
            {games.data?.map(g => (
              <option key={g.id} value={g.id}>{g.name} ({g.code})</option>
            ))}
          </select>
        </div>

        {selectedGame && (
          <>
            <p className="math-version">
              {t('math:editor.activeVersion')}: <strong>{selectedGame.activeVersion ?? '—'}</strong>
            </p>

            <VersionsPanel gameId={selectedGame.id} />

            <SimulationHistoryPanel gameId={selectedGame.id} />

            {config.isLoading && <p className="math-msg">{t('math:editor.loading')}</p>}

            <div className="field">
              <label htmlFor="config">{t('math:editor.configLabel')}</label>
              <textarea id="config" className="config-editor" spellCheck={false}
                        value={configText} onChange={e => setConfigText(e.target.value)} rows={18} />
            </div>

            <div className="field-row">
              <div className="field">
                <label htmlFor="rtp">{t('math:editor.rtpTarget')}</label>
                <input id="rtp" type="number" min="0" max="1" step="0.0001"
                       value={rtp} onChange={e => setRtp(e.target.value)} />
              </div>
              <div className="field">
                <label htmlFor="vol">{t('math:editor.volatilityTarget')}</label>
                <input id="vol" type="number" step="0.01"
                       value={volatility} onChange={e => setVolatility(e.target.value)} />
              </div>
            </div>

            <div className="field">
              <label htmlFor="notes">{t('math:editor.notes')}</label>
              <input id="notes" type="text" value={notes} onChange={e => setNotes(e.target.value)} />
            </div>

            {clientError && <p className="server-error">{clientError}</p>}
            {success && <p className="success-msg">{success}</p>}
            {serverErrors && (
              <div className="server-error">
                <p>{t('math:editor.errorsTitle')}</p>
                <ul>
                  {serverErrors.map((e, i) => (
                    <li key={i}><code>{e.field}</code>: {e.message}</li>
                  ))}
                </ul>
              </div>
            )}

            <button type="button" className="btn-primary" onClick={save} disabled={create.isPending}>
              {create.isPending ? t('math:editor.saving') : t('math:editor.save')}
            </button>

            {selectedGame.activeConfigId != null && (
              <SimulationPanel
                configId={selectedGame.activeConfigId}
                rtpTarget={config.data?.rtpTarget ?? null}
                config={config.data?.config}
              />
            )}
          </>
        )}
      </main>
    </div>
  )
}
