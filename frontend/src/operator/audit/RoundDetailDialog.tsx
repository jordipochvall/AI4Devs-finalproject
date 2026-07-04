import { useEffect } from 'react'
import { useTranslation } from 'react-i18next'
import { useRoundDetail } from '../api/operatorApi'
import { formatMoney } from '../../shared/format/money'

interface Props {
  roundId: number
  onClose: () => void
}

/**
 * Lightweight round-detail modal (HU-27): shows the amounts, the resulting symbol grid and the winning
 * paylines of one audited round, read from the immutable record. Cheaper than the full replay (no
 * engine, no config, no free-spin reconstruction) — for a quick look during a claim. Closes on the
 * backdrop, the button or Escape.
 */
export default function RoundDetailDialog({ roundId, onClose }: Props) {
  const { t, i18n } = useTranslation()
  const detail = useRoundDetail(roundId)

  useEffect(() => {
    const onKey = (e: KeyboardEvent) => { if (e.key === 'Escape') onClose() }
    window.addEventListener('keydown', onKey)
    return () => window.removeEventListener('keydown', onKey)
  }, [onClose])

  const d = detail.data
  const rows = d && d.view.length > 0 ? d.view[0].length : 0

  return (
    <div className="dialog-backdrop" onClick={onClose}>
      <div className="dialog round-detail" role="dialog" aria-modal="true"
           aria-label={t('operator:roundDetail.title', { id: roundId })}
           onClick={e => e.stopPropagation()}>
        <h2>{t('operator:roundDetail.title', { id: roundId })}</h2>

        {detail.isLoading && <p className="operator-msg">{t('operator:roundDetail.loading')}</p>}
        {detail.isError && <p className="server-error">{t('operator:roundDetail.error')}</p>}

        {d && (
          <>
            <dl className="round-detail-amounts">
              <div><dt>{t('operator:roundDetail.player')}</dt><dd>#{d.playerId}</dd></div>
              <div><dt>{t('operator:roundDetail.game')}</dt><dd>#{d.gameId}</dd></div>
              <div><dt>{t('operator:roundDetail.bet')}</dt><dd>{formatMoney(d.betCents, 'EUR', i18n.language)}</dd></div>
              <div><dt>{t('operator:roundDetail.win')}</dt><dd>{formatMoney(d.winCents, 'EUR', i18n.language)}</dd></div>
              <div><dt>{t('operator:roundDetail.balancePre')}</dt>
                   <dd>{formatMoney(d.balancePreCents, 'EUR', i18n.language)}</dd></div>
              <div><dt>{t('operator:roundDetail.balancePost')}</dt>
                   <dd>{formatMoney(d.balancePostCents, 'EUR', i18n.language)}</dd></div>
              <div><dt>{t('operator:roundDetail.scatter')}</dt><dd>{d.scatterCount}</dd></div>
              <div><dt>{t('operator:roundDetail.date')}</dt>
                   <dd>{new Date(d.createdAt).toLocaleString(i18n.language)}</dd></div>
            </dl>

            <h3>{t('operator:roundDetail.grid')}</h3>
            <table className="round-detail-grid" aria-label={t('operator:roundDetail.grid')}>
              <tbody>
                {Array.from({ length: rows }, (_, r) => (
                  <tr key={r}>
                    {d.view.map((col, c) => <td key={c}>{col[r]}</td>)}
                  </tr>
                ))}
              </tbody>
            </table>

            <h3>{t('operator:roundDetail.lines')}</h3>
            {d.winningPaylines.length === 0 ? (
              <p className="operator-msg">{t('operator:roundDetail.noLines')}</p>
            ) : (
              <ul className="round-detail-lines">
                {d.winningPaylines.map((l, i) => (
                  <li key={i}>{t('operator:roundDetail.lineItem', {
                    index: l.paylineIndex, symbol: l.symbol, count: l.count,
                    win: formatMoney(l.winCents, 'EUR', i18n.language),
                  })}</li>
                ))}
              </ul>
            )}
          </>
        )}

        <div className="dialog-actions">
          <button type="button" className="btn-primary" onClick={onClose} autoFocus>
            {t('operator:roundDetail.close')}
          </button>
        </div>
      </div>
    </div>
  )
}
