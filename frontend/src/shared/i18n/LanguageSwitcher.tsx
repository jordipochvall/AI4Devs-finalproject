import { useTranslation } from 'react-i18next'
import { SUPPORTED_LANGS, type Lang } from './i18n'
import './languageSwitcher.css'

/**
 * Selector de idioma con conmutación en caliente (AC2).
 * El cambio se persiste vía el listener 'languageChanged' de i18n (AC3).
 */
export default function LanguageSwitcher() {
  const { i18n, t } = useTranslation()
  const current = (i18n.language?.slice(0, 2) as Lang) ?? 'es'

  return (
    <div className="lang-switcher" role="group" aria-label={t('language.label')}>
      {SUPPORTED_LANGS.map(lang => (
        <button
          key={lang}
          type="button"
          className={`lang-btn ${current === lang ? 'active' : ''}`}
          aria-pressed={current === lang}
          onClick={() => i18n.changeLanguage(lang)}
        >
          {lang.toUpperCase()}
        </button>
      ))}
    </div>
  )
}
