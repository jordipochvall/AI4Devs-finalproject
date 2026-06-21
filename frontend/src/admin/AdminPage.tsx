import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import axios from 'axios'
import { useOperators, useCreateOperator, useSetOperatorActive } from './api/adminApi'
import { logoutSession } from '../shared/auth/session'
import LanguageSwitcher from '../shared/i18n/LanguageSwitcher'
import '../operator/players/operator.css'

/** Platform-admin surface (HU-25): list operators and onboard new ones with their initial user. */
export default function AdminPage() {
  const { t } = useTranslation()
  const navigate = useNavigate()
  const operators = useOperators()
  const create = useCreateOperator()
  const setActive = useSetOperatorActive()

  const [code, setCode] = useState('')
  const [name, setName] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [message, setMessage] = useState<{ kind: 'ok' | 'error'; text: string } | null>(null)

  const logout = () => { logoutSession(); navigate('/login', { replace: true }) }

  const submit = () => {
    setMessage(null)
    create.mutate(
      { code, name, operatorEmail: email, operatorPassword: password },
      {
        onSuccess: () => {
          setMessage({ kind: 'ok', text: t('operator:admin.created') })
          setCode(''); setName(''); setEmail(''); setPassword('')
        },
        onError: err => {
          const conflict = axios.isAxiosError(err) && err.response?.status === 409
          setMessage({ kind: 'error', text: conflict ? t('operator:admin.conflict') : t('operator:admin.error') })
        },
      },
    )
  }

  return (
    <div className="operator">
      <header className="operator-header">
        <h1>{t('operator:admin.title')}</h1>
        <div className="operator-header-right">
          <LanguageSwitcher />
          <button type="button" className="btn-secondary" onClick={logout}>{t('common.logout')}</button>
        </div>
      </header>

      <main className="operator-main">
        <section className="limits-card">
          <h2>{t('operator:admin.newOperator')}</h2>
          <input aria-label={t('operator:admin.code')} placeholder={t('operator:admin.code')}
                 value={code} onChange={e => setCode(e.target.value)} />
          <input aria-label={t('operator:admin.name')} placeholder={t('operator:admin.name')}
                 value={name} onChange={e => setName(e.target.value)} />
          <input aria-label={t('operator:admin.opEmail')} placeholder={t('operator:admin.opEmail')}
                 value={email} onChange={e => setEmail(e.target.value)} />
          <input aria-label={t('operator:admin.opPassword')} type="password" placeholder={t('operator:admin.opPassword')}
                 value={password} onChange={e => setPassword(e.target.value)} />
          <button type="button" className="btn-primary" disabled={create.isPending} onClick={submit}>
            {create.isPending ? t('operator:admin.creating') : t('operator:admin.create')}
          </button>
          {message && (
            <p className={message.kind === 'ok' ? 'success-msg' : 'operator-error'}>{message.text}</p>
          )}
        </section>

        {operators.isLoading && <p className="operator-msg">{t('operator:admin.loading')}</p>}
        {operators.data && (
          <table className="players-table">
            <thead>
              <tr>
                <th>{t('operator:admin.colCode')}</th>
                <th>{t('operator:admin.colName')}</th>
                <th>{t('operator:admin.colStatus')}</th>
                <th>{t('operator:admin.colActions')}</th>
              </tr>
            </thead>
            <tbody>
              {operators.data.map(op => (
                <tr key={op.id}>
                  <td>{op.code}</td>
                  <td>{op.name}</td>
                  <td>{op.active ? t('operator:admin.active') : t('operator:admin.inactive')}</td>
                  <td>
                    <button type="button" className="btn-link"
                            onClick={() => setActive.mutate({ id: op.id, active: !op.active })}>
                      {op.active ? t('operator:admin.deactivate') : t('operator:admin.activate')}
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </main>
    </div>
  )
}
