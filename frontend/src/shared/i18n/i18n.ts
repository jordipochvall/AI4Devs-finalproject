import i18n from 'i18next'
import { initReactI18next } from 'react-i18next'

import sharedEs   from './locales/es/shared.json'
import playerEs   from './locales/es/player.json'
import operatorEs from './locales/es/operator.json'
import mathEs     from './locales/es/math.json'

import sharedEn   from './locales/en/shared.json'
import playerEn   from './locales/en/player.json'
import operatorEn from './locales/en/operator.json'
import mathEn     from './locales/en/math.json'

export const SUPPORTED_LANGS = ['es', 'en'] as const
export type Lang = (typeof SUPPORTED_LANGS)[number]

const STORAGE_KEY = 'nova-lang'

/** Initial language: persisted preference → browser language → 'es'. */
function detectInitialLang(): Lang {
  const stored = localStorage.getItem(STORAGE_KEY)
  if (stored === 'es' || stored === 'en') return stored
  const browser = navigator.language?.slice(0, 2)
  return browser === 'en' ? 'en' : 'es'
}

i18n
  .use(initReactI18next)
  .init({
    resources: {
      es: { shared: sharedEs, player: playerEs, operator: operatorEs, math: mathEs },
      en: { shared: sharedEn, player: playerEn, operator: operatorEn, math: mathEn },
    },
    lng: detectInitialLang(),
    fallbackLng: 'es',
    ns: ['shared', 'player', 'operator', 'math'],
    defaultNS: 'shared',
    interpolation: { escapeValue: false },
  })

// Persist the chosen language across sessions.
i18n.on('languageChanged', lng => {
  localStorage.setItem(STORAGE_KEY, lng)
})

/** Changes the active language (hot switch). */
export function changeLanguage(lang: Lang) {
  return i18n.changeLanguage(lang)
}

export default i18n
