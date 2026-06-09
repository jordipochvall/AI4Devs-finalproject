import { useState } from 'react'
import { useTranslation } from 'react-i18next'
import { useSessionGuard, isLossThresholdExceeded } from './sessionGuardStore'
import ResponsibleGamingDialog from './ResponsibleGamingDialog'
import './compliance.css'

/**
 * Permanent compliance banner shown on every player screen: DGOJ seal, "+18" notice and a
 * "Responsible gaming" link that opens an informative dialog. When the session's net loss
 * exceeds the threshold, it also renders a pause notice (HU-12, AC1–AC3).
 */
export default function ComplianceBanner() {
  const { t } = useTranslation()
  const [dialogOpen, setDialogOpen] = useState(false)
  const netLossCents = useSessionGuard(s => s.netLossCents)
  const lossExceeded = isLossThresholdExceeded(netLossCents)

  return (
    <>
      {lossExceeded && (
        <div className="rg-pause" role="alert">
          {t('compliance.pauseNotice')}
        </div>
      )}

      <footer className="compliance-banner">
        <span className="compliance-seal" aria-label={t('compliance.seal')}>🛡 {t('compliance.seal')}</span>
        <span className="compliance-plus18">{t('compliance.plus18')}</span>
        <button type="button" className="compliance-link" onClick={() => setDialogOpen(true)}>
          {t('compliance.responsibleGaming')}
        </button>
      </footer>

      {dialogOpen && <ResponsibleGamingDialog onClose={() => setDialogOpen(false)} />}
    </>
  )
}
