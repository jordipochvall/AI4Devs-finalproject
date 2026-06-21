import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { useSetLimit, useSelfExclude } from '../api/playerApi'
import LanguageSwitcher from '../../shared/i18n/LanguageSwitcher'
import './limits.css'

/** Responsible-gaming settings (HU-19): set a daily loss limit and self-exclude. Server-enforced. */
export default function LimitsPage() {
  const { t } = useTranslation()
  const navigate = useNavigate()
  const setLimit = useSetLimit()
  const selfExclude = useSelfExclude()

  const [amount, setAmount] = useState('')
  const [days, setDays] = useState('')
  const [limitMsg, setLimitMsg] = useState<string | null>(null)
  const [exclusionMsg, setExclusionMsg] = useState<string | null>(null)

  const submitLimit = () => {
    setLimitMsg(null)
    const euros = parseFloat(amount)
    if (!Number.isFinite(euros) || euros < 0) { setLimitMsg(t('player:limits.invalidAmount')); return }
    setLimit.mutate(
      { limitType: 'LOSS', period: 'DAILY', amountCents: Math.round(euros * 100) },
      {
        onSuccess: () => setLimitMsg(t('player:limits.limitSaved')),
        onError: () => setLimitMsg(t('player:limits.error')),
      },
    )
  }

  const submitExclusion = () => {
    setExclusionMsg(null)
    const d = parseInt(days, 10)
    if (!Number.isFinite(d) || d <= 0) { setExclusionMsg(t('player:limits.invalidDays')); return }
    selfExclude.mutate(d, {
      onSuccess: () => setExclusionMsg(t('player:limits.exclusionSaved', { days: d })),
      onError: () => setExclusionMsg(t('player:limits.error')),
    })
  }

  return (
    <div className="limits">
      <header className="limits-header">
        <h1>{t('player:limits.title')}</h1>
        <div className="limits-header-right">
          <button type="button" className="btn-secondary" onClick={() => navigate('/')}>
            {t('player:limits.back')}
          </button>
          <LanguageSwitcher />
        </div>
      </header>

      <main className="limits-main">
        <p className="limits-intro">{t('player:limits.intro')}</p>

        <section className="limits-card">
          <h2>{t('player:limits.lossTitle')}</h2>
          <label htmlFor="amount">{t('player:limits.dailyLoss')}
            <input id="amount" type="number" min="0" step="0.01"
                   value={amount} onChange={e => setAmount(e.target.value)} />
          </label>
          <button type="button" className="btn-primary" disabled={setLimit.isPending} onClick={submitLimit}>
            {setLimit.isPending ? t('player:limits.saving') : t('player:limits.save')}
          </button>
          {limitMsg && <p className="limits-msg">{limitMsg}</p>}
        </section>

        <section className="limits-card">
          <h2>{t('player:limits.exclusionTitle')}</h2>
          <label htmlFor="days">{t('player:limits.days')}
            <input id="days" type="number" min="1" step="1"
                   value={days} onChange={e => setDays(e.target.value)} />
          </label>
          <button type="button" className="btn-danger" disabled={selfExclude.isPending} onClick={submitExclusion}>
            {selfExclude.isPending ? t('player:limits.saving') : t('player:limits.selfExclude')}
          </button>
          {exclusionMsg && <p className="limits-msg">{exclusionMsg}</p>}
        </section>
      </main>
    </div>
  )
}
