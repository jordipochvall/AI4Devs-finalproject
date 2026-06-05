import React from 'react'
import ReactDOM from 'react-dom/client'
import './shared/i18n/i18n'   // inicializar i18next antes de renderizar
import App from './App'

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <App />
  </React.StrictMode>,
)
