import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import api from '../../shared/api/axiosClient'
import type { GameConfig, SpinResult, WinningPayline } from '../../player/api/playerApi'

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

/**
 * Lightweight detail of a single audited round (HU-27): amounts plus the resulting symbol grid and
 * winning paylines, read from the immutable {@code game_rounds} row. Cheaper than the full replay —
 * no config, no engine, no free-spin reconstruction.
 */
export interface RoundDetail {
  roundId: number
  gameId: number
  playerId: number
  gameConfigId: number
  betCents: number
  winCents: number
  balancePreCents: number
  balancePostCents: number
  freeSpin: boolean
  createdAt: string
  view: string[][]
  winningPaylines: WinningPayline[]
  scatterCount: number
}

/** Query hook for a round's lightweight detail. Disabled until a round id is selected. */
export function useRoundDetail(roundId: number | null) {
  return useQuery({
    queryKey: ['operator', 'round-detail', roundId],
    queryFn: () => api.get<RoundDetail>(`/operator/rounds/${roundId}`).then(r => r.data),
    enabled: roundId != null && Number.isFinite(roundId) && roundId > 0,
    retry: false, // a 404 is a final answer, not a transient error
  })
}

/** A game's commercial configuration as seen by the operator backoffice (HU-15). */
export interface OperatorGame {
  id: number
  code: string
  name: string
  theme: string
  minBetCents: number
  maxBetCents: number
  betStepCents: number
  allowedCurrencies: string[]
  active: boolean
  activeConfigId: number | null
  paylineCount: number | null
}

/** Editable commercial fields submitted on a PUT /operator/games/{id}. */
export interface UpdateGamePayload {
  minBetCents: number
  maxBetCents: number
  betStepCents: number
  active: boolean
  allowedCurrencies: string[]
}

/** Query hook for the operator's games with their commercial configuration. */
export function useOperatorGames() {
  return useQuery({
    queryKey: ['operator', 'games'],
    queryFn: () => api.get<OperatorGame[]>('/operator/games').then(r => r.data),
  })
}

/** Mutation hook that updates a game's commercial configuration, refreshing the list. */
export function useUpdateGame() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: ({ gameId, payload }: { gameId: number; payload: UpdateGamePayload }) =>
      api.put<OperatorGame>(`/operator/games/${gameId}`, payload).then(r => r.data),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['operator', 'games'] }),
  })
}

/** A most-played game entry in the dashboard. */
export interface TopGame {
  gameId: number
  name: string | null
  theme: string | null
  rounds: number
}

/** Aggregated operator activity KPIs (HU-16). */
export interface Dashboard {
  from: string
  to: string
  activePlayers: number
  ggrCents: number
  totalRounds: number
  topGames: TopGame[]
}

/** Query hook for the operator activity dashboard, optionally bounded by a date window. */
export function useDashboard(from?: string, to?: string) {
  return useQuery({
    queryKey: ['operator', 'dashboard', from ?? null, to ?? null],
    queryFn: () =>
      api.get<Dashboard>('/operator/dashboard', {
        params: { from: from || undefined, to: to || undefined },
      }).then(r => r.data),
  })
}

/** RFJ regulatory report for a period (HU-21). */
export interface RfjReport {
  operatorId: number
  periodFrom: string
  periodTo: string
  totalRounds: number
  activePlayers: number
  totalWageredCents: number
  totalWonCents: number
  ggrCents: number
  integrityConsistent: boolean
  integrityChecked: number
  generatedAt: string
}

/** Mutation hook to generate the RFJ report for a month (HU-21). */
export function useGenerateRfj() {
  return useMutation({
    mutationFn: ({ year, month }: { year: number; month: number }) =>
      api.post<RfjReport>('/operator/reports/rfj', { year, month }).then(r => r.data),
  })
}

/** Result of an audit integrity verification (HU-20). */
export interface IntegrityReport {
  from: string
  to: string
  checked: number
  consistent: boolean
  firstBrokenRoundId: number | null
}

/** Query hook for the audit integrity check; runs only once enabled (on demand). */
export function useIntegrity(enabled: boolean) {
  return useQuery({
    queryKey: ['operator', 'integrity'],
    queryFn: () => api.get<IntegrityReport>('/operator/audit/integrity').then(r => r.data),
    enabled,
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
