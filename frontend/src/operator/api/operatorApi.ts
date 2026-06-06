import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import api from '../../shared/api/axiosClient'

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
