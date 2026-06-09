import { useTranslation } from 'react-i18next'

interface Props {
  onClose: () => void
}

/** Informative modal about responsible gaming, opened from the compliance banner. */
export default function ResponsibleGamingDialog({ onClose }: Props) {
  const { t } = useTranslation()
  return (
    <div className="rg-backdrop" onClick={onClose}>
      <div className="rg-dialog" role="dialog" aria-modal="true" onClick={e => e.stopPropagation()}>
        <h2>{t('compliance.dialogTitle')}</h2>
        <p>{t('compliance.dialogBody')}</p>
        <button type="button" className="rg-close" onClick={onClose}>
          {t('compliance.dialogClose')}
        </button>
      </div>
    </div>
  )
}
