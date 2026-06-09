import { useState } from 'react'
import { useTranslation } from 'react-i18next'
import axios from 'axios'
import { useExplain } from '../api/mathApi'

interface ExplainBoxProps {
  /** Completed simulation to ask the AI about. */
  simulationId: number
}

interface Turn {
  question: string
  answer: string
}

/**
 * Natural-language Q&A box over a completed simulation (HU-8). Posts to the explain endpoint and
 * shows the thread. If the AI is unavailable (503) it disables itself with a notice, leaving the rest
 * of the dashboard working (AC3). Only rendered for COMPLETED simulations (AC1).
 */
export default function ExplainBox({ simulationId }: ExplainBoxProps) {
  const { t } = useTranslation()
  const [question, setQuestion] = useState('')
  const [thread, setThread] = useState<Turn[]>([])
  const [unavailable, setUnavailable] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const explain = useExplain()

  const ask = () => {
    const q = question.trim()
    if (!q) return
    setError(null)
    explain.mutate({ simulationId, question: q }, {
      onSuccess: res => { setThread(prev => [...prev, { question: q, answer: res.answer }]); setQuestion('') },
      onError: err => {
        if (axios.isAxiosError(err) && err.response?.status === 503) {
          setUnavailable(true)
        } else {
          setError(t('math:sim.explain.error'))
        }
      },
    })
  }

  if (unavailable) {
    return (
      <div className="sim-block">
        <h3>{t('math:sim.explain.title')}</h3>
        <p className="sim-explain-unavailable" role="status">{t('math:sim.explain.unavailable')}</p>
      </div>
    )
  }

  return (
    <div className="sim-block">
      <h3>{t('math:sim.explain.title')}</h3>

      {thread.length > 0 && (
        <ul className="sim-explain-thread">
          {thread.map((turn, i) => (
            <li key={i}>
              <p className="sim-explain-q"><strong>{t('math:sim.explain.you')}:</strong> {turn.question}</p>
              <p className="sim-explain-a"><strong>{t('math:sim.explain.ai')}:</strong> {turn.answer}</p>
            </li>
          ))}
        </ul>
      )}

      <textarea className="sim-explain-input" rows={2}
                placeholder={t('math:sim.explain.placeholder')}
                value={question} onChange={e => setQuestion(e.target.value)} />
      {error && <p className="server-error">{error}</p>}
      <button type="button" className="btn-primary" onClick={ask}
              disabled={explain.isPending || question.trim() === ''}>
        {explain.isPending ? t('math:sim.explain.asking') : t('math:sim.explain.ask')}
      </button>
    </div>
  )
}
