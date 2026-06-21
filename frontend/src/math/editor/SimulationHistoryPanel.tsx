import { useState } from 'react'
import { useTranslation } from 'react-i18next'
import { useSimulationHistory, useSimulationExplanations } from '../api/mathApi'

interface Props {
  /** Game whose simulation history is shown. */
  gameId: number
}

/** HU-18: paginated history of a game's simulations, with each one's expandable AI Q&A thread. */
export default function SimulationHistoryPanel({ gameId }: Props) {
  const { t } = useTranslation('math')
  const [page, setPage] = useState(0)
  const [openId, setOpenId] = useState<number | null>(null)
  const history = useSimulationHistory(gameId, page)

  return (
    <section className="sim-history">
      <h2>{t('history.title')}</h2>

      {history.isLoading && <p className="math-msg">{t('history.loading')}</p>}
      {history.data?.content.length === 0 && <p className="math-msg">{t('history.empty')}</p>}

      {history.data && history.data.content.length > 0 && (
        <>
          <table className="sim-history-table">
            <thead>
              <tr>
                <th>{t('history.colId')}</th>
                <th>{t('history.colStatus')}</th>
                <th className="num">{t('history.colRtp')}</th>
                <th>{t('history.colDate')}</th>
                <th aria-hidden="true" />
              </tr>
            </thead>
            <tbody>
              {history.data.content.map(s => (
                <tr key={s.id}>
                  <td>#{s.id}</td>
                  <td>{s.status}</td>
                  <td className="num">{s.rtpEmpirical != null ? `${(s.rtpEmpirical * 100).toFixed(2)}%` : '—'}</td>
                  <td>{new Date(s.startedAt).toLocaleString()}</td>
                  <td>
                    <button type="button" className="btn-link"
                            onClick={() => setOpenId(openId === s.id ? null : s.id)}>
                      {openId === s.id ? t('history.hideThread') : t('history.showThread')}
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>

          {openId != null && <ExplanationThread simulationId={openId} />}

          <div className="pagination">
            <button type="button" className="btn-secondary" disabled={page <= 0}
                    onClick={() => setPage(p => p - 1)}>{t('history.prev')}</button>
            <span>{t('history.pageInfo', { page: page + 1, total: Math.max(history.data.totalPages, 1) })}</span>
            <button type="button" className="btn-secondary"
                    disabled={page >= history.data.totalPages - 1}
                    onClick={() => setPage(p => p + 1)}>{t('history.next')}</button>
          </div>
        </>
      )}
    </section>
  )
}

/** The AI question/answer thread of a single simulation. */
function ExplanationThread({ simulationId }: { simulationId: number }) {
  const { t } = useTranslation('math')
  const thread = useSimulationExplanations(simulationId)

  if (thread.isLoading) return <p className="math-msg">{t('history.loading')}</p>
  if (!thread.data || thread.data.length === 0) return <p className="math-msg">{t('history.noThread')}</p>

  return (
    <ul className="sim-thread">
      {thread.data.map((e, i) => (
        <li key={i} className="sim-thread-item">
          <p className="sim-thread-q"><strong>{t('history.q')}:</strong> {e.question}</p>
          <p className="sim-thread-a"><strong>{t('history.a')}:</strong> {e.answer}</p>
          <p className="sim-thread-meta">{e.model}</p>
        </li>
      ))}
    </ul>
  )
}
