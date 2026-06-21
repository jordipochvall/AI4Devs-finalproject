import { useQuery, useMutation, keepPreviousData } from '@tanstack/react-query'
import api from '../../shared/api/axiosClient'

/** Standard pagination wrapper (matches the API's PageResponse, §4.1). */
export interface PageResponse<T> {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

/** One of the player's wallet ledger movements (HU-14). */
export interface WalletTransaction {
  id: number
  type: 'RECHARGE' | 'BET' | 'WIN'
  amountCents: number
  balanceAfterCents: number
  gameRoundId: number | null
  createdAt: string
}

/** One of the player's own rounds (HU-14). */
export interface PlayerRound {
  id: number
  gameId: number
  betCents: number
  winCents: number
  balancePostCents: number
  freeSpin: boolean
  createdAt: string
}

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

/** A symbol of a game's config (readme §3.3.1). */
export interface ConfigSymbol {
  id: string
  kind: 'REGULAR' | 'WILD' | 'SCATTER'
}

/** Full game math/structure config (readme §3.3). Only the fields the UI needs are typed. */
export interface GameConfig {
  grid: Grid
  symbols: ConfigSymbol[]
  reels: string[][]
  paylines: number[][]
  paytable: { symbol: string; payouts: Record<string, number> }[]
  scatterPays?: Record<string, Record<string, number>>
  bonus?: unknown
}

/** Game detail including its full active config. */
export interface GameDetail extends GameSummary {
  minBetCents: number
  maxBetCents: number
  betStepCents: number
  config: GameConfig
  /** Current progressive jackpot pool in cents, or null if the game has no jackpot (HU-26). */
  jackpotCents?: number | null
}

/** Player virtual balance. */
export interface Wallet {
  balanceCents: number
  currency: string
}

/** One winning payline of a spin (readme §4.4.3). */
export interface WinningPayline {
  paylineIndex: number
  symbol: string
  count: number
  winCents: number
}

/** Outcome of a spin (readme §4.4.3); free spins are nested in {@link SpinResult.freeSpins}. */
export interface SpinResult {
  roundId: number
  betCents: number
  lineBetCents: number
  winCents: number
  balancePreCents: number
  balancePostCents: number
  view: string[][]
  winningPaylines: WinningPayline[]
  scatterCount: number
  freeSpins: {
    triggered: boolean
    awarded: number
    rounds: SpinResult[]
  }
}

/** Player API calls. */
const playerApi = {
  getGames:  () => api.get<GameSummary[]>('/player/games').then(r => r.data),
  getGame:   (id: number) => api.get<GameDetail>(`/player/games/${id}`).then(r => r.data),
  getWallet: () => api.get<Wallet>('/player/wallet').then(r => r.data),

  /** Executes a spin. The {@code Idempotency-Key} dedups retries of the same submit. */
  spin: (gameId: number, betCents: number, currency: string, idempotencyKey: string) =>
    api.post<SpinResult>(
      `/player/games/${gameId}/spin`,
      { betCents, currency },
      { headers: { 'Idempotency-Key': idempotencyKey } },
    ).then(r => r.data),
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

/** Query hook for the player's wallet movements, paginated (HU-14). */
export function useWalletTransactions(page: number, size = 10) {
  return useQuery({
    queryKey: ['player', 'transactions', page, size],
    queryFn: () =>
      api.get<PageResponse<WalletTransaction>>('/player/wallet/transactions', { params: { page, size } })
        .then(r => r.data),
    placeholderData: keepPreviousData,
  })
}

/** Query hook for the player's own rounds, paginated (HU-14). */
export function usePlayerRounds(page: number, size = 10) {
  return useQuery({
    queryKey: ['player', 'rounds', page, size],
    queryFn: () =>
      api.get<PageResponse<PlayerRound>>('/player/rounds', { params: { page, size } })
        .then(r => r.data),
    placeholderData: keepPreviousData,
  })
}

/** A responsible-gaming limit echoed back by the API (HU-19). */
export interface Limit {
  limitType: string
  period: string
  amountCents: number
  effectiveAt: string
  pendingAmountCents: number | null
  pendingEffectiveAt: string | null
}

/** Mutation hook to set/change a responsible-gaming limit (HU-19). */
export function useSetLimit() {
  return useMutation({
    mutationFn: (payload: { limitType: string; period: string; amountCents: number }) =>
      api.post<Limit>('/player/limits', payload).then(r => r.data),
  })
}

/** Mutation hook to self-exclude for a number of days (HU-19). */
export function useSelfExclude() {
  return useMutation({
    mutationFn: (days: number) =>
      api.post('/player/self-exclusion', { days }).then(r => r.data),
  })
}

export default playerApi
