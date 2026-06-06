import { useNavigate } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { useGames, useWallet, type GameSummary } from '../api/playerApi'
import { useAuthStore } from '../../shared/auth/authStore'
import { formatMoney } from '../../shared/format/money'
import LanguageSwitcher from '../../shared/i18n/LanguageSwitcher'
import './lobby.css'

/** Player lobby: header with balance, a responsive grid of active games, and navigation to play. */
export default function LobbyPage() {
  const { t, i18n } = useTranslation()
  const navigate = useNavigate()
  const clearAuth = useAuthStore(s => s.clearAuth)

  const games  = useGames()
  const wallet = useWallet()

  const logout = () => { clearAuth(); navigate('/login', { replace: true }) }

  return (
    <div className="lobby">
      <header className="lobby-header">
        <h1>{t('player:lobby.title')}</h1>
        <div className="lobby-header-right">
          <span className="lobby-balance" aria-label={t('player:lobby.balance')}>
            {t('player:lobby.balance')}:{' '}
            <strong>
              {wallet.data
                ? formatMoney(wallet.data.balanceCents, wallet.data.currency, i18n.language)
                : '—'}
            </strong>
          </span>
          <LanguageSwitcher />
          <button type="button" className="lobby-logout" onClick={logout}>
            {t('common.logout')}
          </button>
        </div>
      </header>

      <main className="lobby-main">
        {games.isLoading && <p className="lobby-msg">{t('player:lobby.loading')}</p>}
        {games.isError && <p className="lobby-msg lobby-error">{t('player:lobby.error')}</p>}
        {games.data?.length === 0 && <p className="lobby-msg">{t('player:lobby.empty')}</p>}

        {games.data && games.data.length > 0 && (
          <ul className="game-grid">
            {games.data.map(game => (
              <GameCard key={game.id} game={game} onPlay={() => navigate(`/play/${game.id}`)} />
            ))}
          </ul>
        )}
      </main>
    </div>
  )
}

/** A single game cover card in the lobby grid. */
function GameCard({ game, onPlay }: { game: GameSummary; onPlay: () => void }) {
  const { t } = useTranslation()
  const gridLabel = game.grid ? `${game.grid.cols}x${game.grid.rows}` : ''
  return (
    <li className="game-card">
      <button type="button" className="game-card-btn" onClick={onPlay}>
        <img className="game-cover" src={game.coverImageUrl} alt={game.name}
             onError={e => { (e.target as HTMLImageElement).style.visibility = 'hidden' }} />
        <div className="game-info">
          <span className="game-name">{game.name}</span>
          <span className="game-theme">
            {t(`player:theme.${game.theme}`, game.theme)} {gridLabel && `· ${gridLabel}`}
          </span>
        </div>
        <span className="game-play">{t('player:lobby.play')}</span>
      </button>
    </li>
  )
}
