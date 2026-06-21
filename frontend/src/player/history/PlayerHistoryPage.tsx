import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { useWalletTransactions, usePlayerRounds } from '../api/playerApi'
import { formatMoney } from '../../shared/format/money'
import LanguageSwitcher from '../../shared/i18n/LanguageSwitcher'
import './history.css'

type Tab = 'transactions' | 'rounds'

/** Player history (HU-14): paginated wallet movements and own rounds, money formatted by locale. */
export default function PlayerHistoryPage() {
  const { t, i18n } = useTranslation()
  const navigate = useNavigate()
  const [tab, setTab] = useState<Tab>('transactions')
  const [page, setPage] = useState(0)

  const onTab = (next: Tab) => { setTab(next); setPage(0) }

  return (
    <div className="history">
      <header className="history-header">
        <h1>{t('player:history.title')}</h1>
        <div className="history-header-right">
          <button type="button" className="btn-secondary" onClick={() => navigate('/')}>
            {t('player:history.back')}
          </button>
          <LanguageSwitcher />
        </div>
      </header>

      <nav className="history-tabs">
        <button type="button" className={tab === 'transactions' ? 'tab active' : 'tab'}
                onClick={() => onTab('transactions')}>
          {t('player:history.tabTransactions')}
        </button>
        <button type="button" className={tab === 'rounds' ? 'tab active' : 'tab'}
                onClick={() => onTab('rounds')}>
          {t('player:history.tabRounds')}
        </button>
      </nav>

      <main className="history-main">
        {tab === 'transactions'
          ? <TransactionsTable page={page} setPage={setPage} locale={i18n.language} />
          : <RoundsTable page={page} setPage={setPage} locale={i18n.language} />}
      </main>
    </div>
  )
}

interface TableProps {
  page: number
  setPage: (updater: (p: number) => number) => void
  locale: string
}

/** Pagination footer shared by both tables. */
function Pager({ page, totalPages, setPage }: { page: number; totalPages: number; setPage: TableProps['setPage'] }) {
  const { t } = useTranslation()
  return (
    <div className="pagination">
      <button type="button" className="btn-secondary" disabled={page <= 0} onClick={() => setPage(p => p - 1)}>
        {t('player:history.prev')}
      </button>
      <span>{t('player:history.pageInfo', { page: page + 1, total: Math.max(totalPages, 1) })}</span>
      <button type="button" className="btn-secondary" disabled={page >= totalPages - 1}
              onClick={() => setPage(p => p + 1)}>
        {t('player:history.next')}
      </button>
    </div>
  )
}

function TransactionsTable({ page, setPage, locale }: TableProps) {
  const { t } = useTranslation()
  const q = useWalletTransactions(page)

  if (q.isLoading) return <p className="history-msg">{t('player:history.loading')}</p>
  if (q.isError) return <p className="history-msg history-error">{t('player:history.error')}</p>
  if (!q.data || q.data.content.length === 0) return <p className="history-msg">{t('player:history.emptyTransactions')}</p>

  return (
    <>
      <table className="history-table">
        <thead>
          <tr>
            <th>{t('player:history.colType')}</th>
            <th className="num">{t('player:history.colAmount')}</th>
            <th className="num">{t('player:history.colBalance')}</th>
            <th>{t('player:history.colDate')}</th>
          </tr>
        </thead>
        <tbody>
          {q.data.content.map(tx => (
            <tr key={tx.id}>
              <td>{t(`player:history.type.${tx.type}`)}</td>
              <td className="num">{formatMoney(tx.amountCents, 'EUR', locale)}</td>
              <td className="num">{formatMoney(tx.balanceAfterCents, 'EUR', locale)}</td>
              <td>{new Date(tx.createdAt).toLocaleString(locale)}</td>
            </tr>
          ))}
        </tbody>
      </table>
      <Pager page={page} totalPages={q.data.totalPages} setPage={setPage} />
    </>
  )
}

function RoundsTable({ page, setPage, locale }: TableProps) {
  const { t } = useTranslation()
  const q = usePlayerRounds(page)

  if (q.isLoading) return <p className="history-msg">{t('player:history.loading')}</p>
  if (q.isError) return <p className="history-msg history-error">{t('player:history.error')}</p>
  if (!q.data || q.data.content.length === 0) return <p className="history-msg">{t('player:history.emptyRounds')}</p>

  return (
    <>
      <table className="history-table">
        <thead>
          <tr>
            <th>{t('player:history.colGame')}</th>
            <th className="num">{t('player:history.colBet')}</th>
            <th className="num">{t('player:history.colWin')}</th>
            <th className="num">{t('player:history.colBalance')}</th>
            <th>{t('player:history.colDate')}</th>
          </tr>
        </thead>
        <tbody>
          {q.data.content.map(r => (
            <tr key={r.id}>
              <td>#{r.gameId} {r.freeSpin && <span className="muted">{t('player:history.freeSpinTag')}</span>}</td>
              <td className="num">{formatMoney(r.betCents, 'EUR', locale)}</td>
              <td className="num">{formatMoney(r.winCents, 'EUR', locale)}</td>
              <td className="num">{formatMoney(r.balancePostCents, 'EUR', locale)}</td>
              <td>{new Date(r.createdAt).toLocaleString(locale)}</td>
            </tr>
          ))}
        </tbody>
      </table>
      <Pager page={page} totalPages={q.data.totalPages} setPage={setPage} />
    </>
  )
}
