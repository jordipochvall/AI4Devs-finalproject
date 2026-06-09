import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import axios from 'axios'
import { authApi } from '../../shared/auth/authApi'
import { useAuthStore, roleHomeRoute } from '../../shared/auth/authStore'
import LanguageSwitcher from '../../shared/i18n/LanguageSwitcher'
import ComplianceBanner from '../../shared/compliance/ComplianceBanner'
import './auth.css'

/** Login screen: client-side validation, server error handling and role-based redirect. */
export default function LoginPage() {
  const { t, i18n } = useTranslation()
  const navigate = useNavigate()
  const setAuth = useAuthStore(s => s.setAuth)

  const [email, setEmail]         = useState('')
  const [password, setPassword]   = useState('')
  const [serverError, setServerError] = useState<string | null>(null)
  const [loading, setLoading]     = useState(false)

  const [emailError, setEmailError]       = useState<string | null>(null)
  const [passwordError, setPasswordError] = useState<string | null>(null)

  /** Validates email format and password presence on the client. */
  const validate = () => {
    let ok = true
    if (!email) {
      setEmailError(t('auth.errors.emailRequired'))
      ok = false
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
      setEmailError(t('auth.errors.emailInvalid'))
      ok = false
    } else {
      setEmailError(null)
    }
    if (!password) {
      setPasswordError(t('auth.errors.passwordTooShort'))
      ok = false
    } else {
      setPasswordError(null)
    }
    return ok
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setServerError(null)
    if (!validate()) return

    setLoading(true)
    try {
      const resp = await authApi.login({ email, password })
      setAuth(resp.token, resp.user)
      i18n.changeLanguage(resp.user.locale)
      navigate(roleHomeRoute(resp.user.role), { replace: true })
    } catch (err) {
      if (axios.isAxiosError(err) && err.response?.status === 401) {
        setServerError(t('auth.errors.invalidCredentials'))
      } else {
        setServerError(t('auth.errors.unknownError'))
      }
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="auth-page">
      <div className="auth-card">
        <div style={{ display: 'flex', justifyContent: 'flex-end', marginBottom: '0.5rem' }}>
          <LanguageSwitcher />
        </div>
        <h1>{t('auth.login.title')}</h1>

        {serverError && <p className="server-error">{serverError}</p>}

        <form onSubmit={handleSubmit} noValidate>
          <div className="field">
            <label htmlFor="email">{t('auth.login.email')}</label>
            <input
              id="email"
              type="email"
              value={email}
              onChange={e => setEmail(e.target.value)}
              autoComplete="email"
            />
            {emailError && <p className="field-error">{emailError}</p>}
          </div>

          <div className="field">
            <label htmlFor="password">{t('auth.login.password')}</label>
            <input
              id="password"
              type="password"
              value={password}
              onChange={e => setPassword(e.target.value)}
              autoComplete="current-password"
            />
            {passwordError && <p className="field-error">{passwordError}</p>}
          </div>

          <button className="btn-primary" type="submit" disabled={loading}>
            {loading ? t('common.loading') : t('auth.login.submit')}
          </button>
        </form>

        <p className="auth-link">
          <Link to="/register">{t('auth.login.noAccount')}</Link>
        </p>
      </div>

      {/* Permanent compliance banner: DGOJ seal, +18 and responsible-gaming link (HU-12) */}
      <ComplianceBanner />
    </div>
  )
}
