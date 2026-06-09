import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import api from '../../shared/api/axiosClient'
import type { GameConfig, SpinResult } from '../../player/api/playerApi'

/** Player with balance, as returned by the operator search. */
export interface PlayerSummary {
  id: number
  email: string
  locale: string
  active: boolean
  balanceCents: number | null
  currency: string | null
}

/** Generic pagination wrapper (matches the API's PageResponse). */
export interface PageResponse<T> {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

/** Wallet returned by the recharge endpoint. */
export interface Wallet {
  balanceCents: number
  currency: string
}

/** Query hook for the paginated player search. */
export function usePlayers(email: string, page: number, size = 10) {
  return useQuery({
    queryKey: ['operator', 'players', email, page, size],
    queryFn: () =>
      api
        .get<PageResponse<PlayerSummary>>('/operator/players', {
          params: { email: email || undefined, page, size },
        })
        .then(r => r.data),
  })
}

/** One audited round in the operator's audit listing. */
export interface RoundSummary {
  id: number
  playerId: number
  gameId: number
  gameConfigId: number
  betCents: number
  winCents: number
  balancePostCents: number
  freeSpin: boolean
  triggeringRoundId: number | null
  createdAt: string
}

/** Optional AND filters for the audit listing. */
export interface RoundFilters {
  playerId?: number | null
  gameId?: number | null
  from?: string | null
  to?: string | null
}

/** Query hook for the paginated, filterable audit of rounds. */
export function useRounds(filters: RoundFilters, page: number, size = 20) {
  return useQuery({
    queryKey: ['operator', 'rounds', filters, page, size],
    queryFn: () =>
      api
        .get<PageResponse<RoundSummary>>('/operator/rounds', {
          params: {
            playerId: filters.playerId || undefined,
            gameId: filters.gameId || undefined,
            from: filters.from || undefined,
            to: filters.to || undefined,
            page,
            size,
          },
        })
        .then(r => r.data),
  })
}

/** Immutable replay record of a round (readme §4.4.5). */
export interface ReplayResult {
  roundId: number
  gameId: number
  gameConfigId: number
  rngSeed: number
  result: SpinResult
  config: GameConfig
}

/** Query hook for a round's immutable replay record. */
export function useReplay(roundId: number) {
  return useQuery({
    queryKey: ['operator', 'replay', roundId],
    queryFn: () => api.get<ReplayResult>(`/operator/rounds/${roundId}/replay`).then(r => r.data),
    enabled: Number.isFinite(roundId) && roundId > 0,
    retry: false, // a 404 is a final answer, not a transient error
  })
}

/** Arguments for a recharge mutation. */
export interface RechargeArgs {
  playerId: number
  amountCents: number
  idempotencyKey: string
}

/** Mutation hook that recharges a player's wallet idempotently. */
export function useRecharge() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: ({ playerId, amountCents, idempotencyKey }: RechargeArgs) =>
      api
        .post<Wallet>(
          `/operator/players/${playerId}/wallet/recharge`,
          { amountCents, currency: 'EUR' },
          { headers: { 'Idempotency-Key': idempotencyKey } },
        )
        .then(r => r.data),
    // After recharging, refresh the listing to show the new balance.
    onSuccess: () => qc.invalidateQueries({ queryKey: ['operator', 'players'] }),
  })
}
