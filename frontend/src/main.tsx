import React from 'react'
import ReactDOM from 'react-dom/client'
import './shared/i18n/i18n'   // initialize i18next before rendering
import './shared/a11y/a11y.css' // WCAG 2.1 AA: focus-visible + prefers-reduced-motion (HU-22)
import App from './App'

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <App />
  </React.StrictMode>,
)
