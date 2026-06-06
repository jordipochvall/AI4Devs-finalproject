import React from 'react'
import ReactDOM from 'react-dom/client'
import './shared/i18n/i18n'   // initialize i18next before rendering
import App from './App'

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <App />
  </React.StrictMode>,
)
