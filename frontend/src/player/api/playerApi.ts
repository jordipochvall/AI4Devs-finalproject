import { useQuery } from '@tanstack/react-query'
import api from '../../shared/api/axiosClient'

/** Grid dimensions of a game. */
export interface Grid {
  cols: number
  rows: number
}

/** Lobby summary of a game. */
export interface GameSummary {
  id: number
  name: string
  theme: string
  coverImageUrl: string
  grid: Grid | null
}

/** Game detail including its full active config. */
export interface GameDetail extends GameSummary {
  minBetCents: number
  maxBetCents: number
  betStepCents: number
  config: unknown
}

/** Player virtual balance. */
export interface Wallet {
  balanceCents: number
  currency: string
}

/** Player read API calls. */
const playerApi = {
  getGames:  () => api.get<GameSummary[]>('/player/games').then(r => r.data),
  getGame:   (id: number) => api.get<GameDetail>(`/player/games/${id}`).then(r => r.data),
  getWallet: () => api.get<Wallet>('/player/wallet').then(r => r.data),
}

/** Query hook for the lobby catalogue. */
export function useGames() {
  return useQuery({ queryKey: ['player', 'games'], queryFn: playerApi.getGames })
}

/** Query hook for the player's balance. */
export function useWallet() {
  return useQuery({ queryKey: ['player', 'wallet'], queryFn: playerApi.getWallet })
}

/** Query hook for a single game's detail. */
export function useGame(id: number) {
  return useQuery({ queryKey: ['player', 'game', id], queryFn: () => playerApi.getGame(id) })
}

export default playerApi
