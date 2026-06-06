import { useState } from 'react'
import { useTranslation } from 'react-i18next'
import { useRecharge, type PlayerSummary } from '../api/operatorApi'

interface Props {
  player: PlayerSummary
  onClose: () => void
}

/**
 * Recharge dialog. The Idempotency-Key is generated ONCE when the dialog opens, so retries
 * and double-clicks reuse the same key (AC4); the button is also disabled while the request
 * is in flight as a second safeguard.
 */
export default function RechargeDialog({ player, onClose }: Props) {
  const { t } = useTranslation()
  const recharge = useRecharge()

  const [amount, setAmount] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [idempotencyKey] = useState(() => crypto.randomUUID())

  const submit = (e: React.FormEvent) => {
    e.preventDefault()
    setError(null)

    const amountCents = Math.round(parseFloat(amount.replace(',', '.')) * 100)
    if (!Number.isFinite(amountCents) || amountCents <= 0) {
      setError(t('operator:recharge.amountPositive'))
      return
    }

    recharge.mutate(
      { playerId: player.id, amountCents, idempotencyKey },
      {
        onSuccess: () => onClose(),
        onError: () => setError(t('operator:recharge.failed')),
      },
    )
  }

  return (
    <div className="dialog-backdrop" onClick={onClose}>
      <div className="dialog" onClick={e => e.stopPropagation()}>
        <h2>{t('operator:recharge.title')}</h2>
        <p className="dialog-player">{t('operator:recharge.player')}: <strong>{player.email}</strong></p>

        {error && <p className="server-error">{error}</p>}

        <form onSubmit={submit}>
          <div className="field">
            <label htmlFor="amount">{t('operator:recharge.amount')}</label>
            <input
              id="amount"
              type="number"
              min="0"
              step="0.01"
              value={amount}
              onChange={e => setAmount(e.target.value)}
              autoFocus
            />
          </div>

          <div className="dialog-actions">
            <button type="button" className="btn-secondary" onClick={onClose}>
              {t('operator:recharge.cancel')}
            </button>
            <button type="submit" className="btn-primary" disabled={recharge.isPending}>
              {recharge.isPending ? t('operator:recharge.processing') : t('operator:recharge.confirm')}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}
