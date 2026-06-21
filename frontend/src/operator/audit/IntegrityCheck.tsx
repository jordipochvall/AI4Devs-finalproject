import { useState } from 'react'
import { useTranslation } from 'react-i18next'
import { useIntegrity } from '../api/operatorApi'

/** HU-20: on-demand audit integrity verification with an intact / broken indicator. */
export default function IntegrityCheck() {
  const { t } = useTranslation()
  const [enabled, setEnabled] = useState(false)
  const check = useIntegrity(enabled)

  return (
    <span className="integrity-check">
      <button type="button" className="btn-secondary" onClick={() => setEnabled(true)}>
        {t('operator:integrity.verify')}
      </button>
      {enabled && check.isLoading && <span className="integrity-msg">{t('operator:integrity.checking')}</span>}
      {check.data && (
        check.data.consistent
          ? <span className="integrity-ok">✓ {t('operator:integrity.intact', { checked: check.data.checked })}</span>
          : <span className="integrity-broken">✗ {t('operator:integrity.broken', { id: check.data.firstBrokenRoundId })}</span>
      )}
    </span>
  )
}
