import { useState } from 'react'
import { useTranslation } from 'react-i18next'
import axios from 'axios'
import { useConfigVersions, usePublishConfig } from '../api/mathApi'

interface Props {
  /** Game whose math version history is shown and from which a version can be published. */
  gameId: number
}

/**
 * HU-17: lists a game's math versions (active one flagged) and lets the analyst publish a version.
 * Publishing the already-active version surfaces the backend 409 without breaking the UI.
 */
export default function VersionsPanel({ gameId }: Props) {
  const { t, i18n } = useTranslation('math')
  const versions = useConfigVersions(gameId)
  const publish = usePublishConfig()
  const [message, setMessage] = useState<{ kind: 'ok' | 'error'; text: string } | null>(null)

  const onPublish = (configId: number, version: number) => {
    setMessage(null)
    publish.mutate(
      { gameId, configId },
      {
        onSuccess: () => setMessage({ kind: 'ok', text: t('versions.publishedOk', { version }) }),
        onError: err => {
          const status = axios.isAxiosError(err) ? err.response?.status : undefined
          setMessage({ kind: 'error', text: status === 409 ? t('versions.alreadyActive') : t('versions.error') })
        },
      },
    )
  }

  return (
    <section className="versions-panel">
      <h2>{t('versions.title')}</h2>

      {versions.isLoading && <p className="math-msg">{t('versions.loading')}</p>}
      {versions.data?.length === 0 && <p className="math-msg">{t('versions.empty')}</p>}
      {message && (
        <p className={message.kind === 'ok' ? 'success-msg' : 'server-error'}>{message.text}</p>
      )}

      {versions.data && versions.data.length > 0 && (
        <table className="versions-table">
          <thead>
            <tr>
              <th>{t('versions.colVersion')}</th>
              <th>{t('versions.colRtp')}</th>
              <th>{t('versions.colDate')}</th>
              <th>{t('versions.colNotes')}</th>
              <th>{t('versions.colStatus')}</th>
              <th aria-hidden="true" />
            </tr>
          </thead>
          <tbody>
            {versions.data.map(v => {
              const publishingThis = publish.isPending && publish.variables?.configId === v.id
              return (
                <tr key={v.id} className={v.active ? 'version-active' : ''}>
                  <td>{v.version}</td>
                  <td>{v.rtpTarget}</td>
                  <td className="num">{new Date(v.createdAt).toLocaleDateString(i18n.language)}</td>
                  <td className="version-notes">{v.notes || '—'}</td>
                  <td>{v.active ? <span className="badge-active">{t('versions.active')}</span> : '—'}</td>
                  <td>
                    <button type="button" className="btn-secondary"
                            disabled={v.active || publish.isPending}
                            onClick={() => onPublish(v.id, v.version)}>
                      {publishingThis ? t('versions.publishing') : t('versions.publish')}
                    </button>
                  </td>
                </tr>
              )
            })}
          </tbody>
        </table>
      )}
    </section>
  )
}
