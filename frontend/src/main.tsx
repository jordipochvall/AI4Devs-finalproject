import React from 'react'
import ReactDOM from 'react-dom/client'
import './shared/i18n/i18n'   // initialize i18next before rendering
import './shared/theme/fonts.css'      // self-hosted web fonts (Cinzel + Inter)
import './shared/theme/tokens.css'     // design tokens (:root custom properties)
import './shared/theme/base.css'       // reset + base element styling
import './shared/theme/components.css' // canonical buttons/fields/dialog
import './shared/a11y/a11y.css' // WCAG 2.1 AA: focus-visible + prefers-reduced-motion (HU-22)
import App from './App'
import { ErrorBoundary } from './shared/errors/ErrorBoundary'

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <ErrorBoundary>
      <App />
    </ErrorBoundary>
  </React.StrictMode>,
)
