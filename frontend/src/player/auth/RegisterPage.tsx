import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import axios from 'axios'
import { authApi } from '../../shared/auth/authApi'
import { useAuthStore, roleHomeRoute } from '../../shared/auth/authStore'
import LanguageSwitcher from '../../shared/i18n/LanguageSwitcher'
import ComplianceBanner from '../../shared/compliance/ComplianceBanner'
import './auth.css'

/** Registration screen: client-side validation, server error mapping and auto-login on success. */
export default function RegisterPage() {
  const { t, i18n } = useTranslation()
  const navigate = useNavigate()
  const setAuth = useAuthStore(s => s.setAuth)

  const [email, setEmail]         = useState('')
  const [password, setPassword]   = useState('')
  const [birthDate, setBirthDate] = useState('')
  const [serverError, setServerError] = useState<string | null>(null)
  const [loading, setLoading]     = useState(false)

  const [emailError, setEmailError]         = useState<string | null>(null)
  const [passwordError, setPasswordError]   = useState<string | null>(null)
  const [birthDateError, setBirthDateError] = useState<string | null>(null)

  /** Validates email, password length and birth date on the client. */
  const validate = () => {
    let ok = true
    if (!email) {
      setEmailError(t('auth.errors.emailRequired')); ok = false
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
      setEmailError(t('auth.errors.emailInvalid')); ok = false
    } else { setEmailError(null) }

    if (!password || password.length < 8) {
      setPasswordError(t('auth.errors.passwordTooShort')); ok = false
    } else { setPasswordError(null) }

    if (!birthDate) {
      setBirthDateError(t('auth.errors.birthDateRequired')); ok = false
    } else { setBirthDateError(null) }

    return ok
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setServerError(null)
    if (!validate()) return

    setLoading(true)
    try {
      // Register with the active UI language as the user's locale.
      const locale = i18n.language?.slice(0, 2) ?? 'es'
      const resp = await authApi.register({ email, password, birthDate, locale })
      setAuth(resp.token, resp.refreshToken, resp.user)
      i18n.changeLanguage(resp.user.locale)
      navigate(roleHomeRoute(resp.user.role), { replace: true })
    } catch (err) {
      if (axios.isAxiosError(err)) {
        const status = err.response?.status
        if (status === 409) {
          setServerError(t('auth.errors.emailAlreadyRegistered'))
        } else if (status === 422) {
          setServerError(t('auth.errors.underAge'))
        } else {
          setServerError(t('auth.errors.unknownError'))
        }
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
        <h1>{t('auth.register.title')}</h1>

        {serverError && <p className="server-error">{serverError}</p>}

        <form onSubmit={handleSubmit} noValidate>
          <div className="field">
            <label htmlFor="email">{t('auth.register.email')}</label>
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
            <label htmlFor="password">{t('auth.register.password')}</label>
            <input
              id="password"
              type="password"
              value={password}
              onChange={e => setPassword(e.target.value)}
              autoComplete="new-password"
            />
            {passwordError && <p className="field-error">{passwordError}</p>}
          </div>

          <div className="field">
            <label htmlFor="birthDate">{t('auth.register.birthDate')}</label>
            <input
              id="birthDate"
              type="date"
              value={birthDate}
              onChange={e => setBirthDate(e.target.value)}
              max={new Date().toISOString().split('T')[0]}
            />
            {birthDateError && <p className="field-error">{birthDateError}</p>}
          </div>

          <button className="btn-primary" type="submit" disabled={loading}>
            {loading ? t('common.loading') : t('auth.register.submit')}
          </button>
        </form>

        <p className="auth-link">
          <Link to="/login">{t('auth.register.hasAccount')}</Link>
        </p>
      </div>

      <ComplianceBanner />
    </div>
  )
}
