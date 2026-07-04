import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import api from '../../shared/api/axiosClient'

/** Game with its active math version, for the math backoffice. */
export interface MathGame {
  id: number
  code: string
  name: string
  theme: string
  active: boolean
  activeConfigId: number | null
  activeVersion: number | null
}

/** Detail of a math version. */
export interface ConfigDetail {
  id: number
  gameId: number
  version: number
  rtpTarget: number
  volatilityTarget: number | null
  notes: string | null
  config: unknown
}

/** Response when a version is created. */
export interface ConfigCreated {
  id: number
  gameId: number
  version: number
  rtpTarget: number
  volatilityTarget: number | null
}

/** Payload to create a new math version. */
export interface CreateConfigPayload {
  gameId: number
  config: unknown
  rtpTarget: number
  volatilityTarget?: number | null
  notes?: string
}

/** Query hook for the math games list. */
export function useMathGames() {
  return useQuery({
    queryKey: ['math', 'games'],
    queryFn: () => api.get<MathGame[]>('/math/games').then(r => r.data),
  })
}

/** Query hook for a config version detail (enabled only when an id is given). */
export function useConfig(configId: number | null) {
  return useQuery({
    queryKey: ['math', 'config', configId],
    queryFn: () => api.get<ConfigDetail>(`/math/configs/${configId}`).then(r => r.data),
    enabled: configId != null,
  })
}

/** 202 response when a simulation is launched. */
export interface SimulationAccepted {
  simulationId: number
  status: string
  startedAt: string
  pollUrl: string
}

/** Status/result of a simulation (polling). Metric fields are null until COMPLETED. */
export interface SimulationStatus {
  simulationId: number
  status: 'RUNNING' | 'COMPLETED' | 'FAILED'
  numSpins: number
  betCents: number
  startedAt: string
  completedAt: string | null
  durationMs: number | null
  rtpEmpirical: number | null
  rtpStdError: number | null
  rtpBaseGame: number | null
  rtpFreeSpins: number | null
  hitFrequency: number | null
  volatility: number | null
  maxWinMultiplier: number | null
  freeSpinTriggerFreq: number | null
  longestDryStreak: number | null
  prizeDistribution: Record<string, number> | null
  convergenceSample: { spins: number; rtp: number }[] | null
  rtpBreakdown: { baseGame: number; freeSpins: number; bySymbol: Record<string, number> } | null
  errorMessage: string | null
}

/** Mutation hook that launches a mass simulation for a config version. */
export function useLaunchSimulation() {
  return useMutation({
    mutationFn: ({ configId, numSpins, betCents }: { configId: number; numSpins: number; betCents: number }) =>
      api.post<SimulationAccepted>(`/math/configs/${configId}/simulations`, { numSpins, betCents })
        .then(r => r.data),
  })
}

/** Polling query for a simulation; refetches every 5 s while RUNNING, then stops. */
export function useSimulation(simulationId: number | null) {
  return useQuery({
    queryKey: ['math', 'simulation', simulationId],
    queryFn: () => api.get<SimulationStatus>(`/math/simulations/${simulationId}`).then(r => r.data),
    enabled: simulationId != null,
    refetchInterval: query => (query.state.data?.status === 'RUNNING' ? 5000 : false),
  })
}

/** An AI explanation answer (HU-8). */
export interface Explanation {
  question: string
  answer: string
  model: string
  askedAt: string
}

/** Standard pagination wrapper (§4.1). */
export interface PageResponse<T> {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

/** One simulation in the operator's history (HU-18). */
export interface SimulationSummary {
  id: number
  gameConfigId: number
  status: 'RUNNING' | 'COMPLETED' | 'FAILED'
  numSpins: number
  betCents: number
  rtpEmpirical: number | null
  startedAt: string
  completedAt: string | null
}

/** Query hook for the operator's simulation history, optionally filtered by game (HU-18). */
export function useSimulationHistory(gameId: number | null, page: number, size = 10) {
  return useQuery({
    queryKey: ['math', 'simHistory', gameId, page, size],
    queryFn: () =>
      api.get<PageResponse<SimulationSummary>>('/math/simulations', {
        params: { gameId: gameId ?? undefined, page, size },
      }).then(r => r.data),
    enabled: gameId != null,
  })
}

/** Query hook for the AI Q&A thread of a simulation (HU-18); enabled only when an id is given. */
export function useSimulationExplanations(simulationId: number | null) {
  return useQuery({
    queryKey: ['math', 'explanations', simulationId],
    queryFn: () =>
      api.get<Explanation[]>(`/math/simulations/${simulationId}/explanations`).then(r => r.data),
    enabled: simulationId != null,
  })
}

/** Mutation hook that asks the AI to explain a completed simulation. */
export function useExplain() {
  return useMutation({
    mutationFn: ({ simulationId, question }: { simulationId: number; question: string }) =>
      api.post<Explanation>(`/math/simulations/${simulationId}/explain`, { question }).then(r => r.data),
  })
}

/** A math version in a game's history (HU-17), flagged with whether it is the active one. */
export interface ConfigVersion {
  id: number
  version: number
  rtpTarget: number
  volatilityTarget: number | null
  active: boolean
  createdAt: string
  notes: string | null
}

/** Result of publishing (activating) a math version. */
export interface PublishResult {
  gameId: number
  activeConfigId: number
  version: number
  publishedByUserId: number
  publishedAt: string
}

/** Query hook for a game's math version history (newest first). */
export function useConfigVersions(gameId: number | null) {
  return useQuery({
    queryKey: ['math', 'versions', gameId],
    queryFn: () => api.get<ConfigVersion[]>(`/math/games/${gameId}/configs`).then(r => r.data),
    enabled: gameId != null,
  })
}

/** Mutation hook that publishes (activates) a math version, refreshing games + version list. */
export function usePublishConfig() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: ({ gameId, configId }: { gameId: number; configId: number }) =>
      api.post<PublishResult>(`/math/games/${gameId}/publish`, { configId }).then(r => r.data),
    onSuccess: (_data, vars) => {
      qc.invalidateQueries({ queryKey: ['math', 'games'] })
      qc.invalidateQueries({ queryKey: ['math', 'versions', vars.gameId] })
    },
  })
}

/** Mutation hook that creates a new math version. */
export function useCreateConfig() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: ({ gameId, config, rtpTarget, volatilityTarget, notes }: CreateConfigPayload) =>
      api
        .post<ConfigCreated>(`/math/games/${gameId}/configs`, {
          config, rtpTarget, volatilityTarget, notes,
        })
        .then(r => r.data),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['math', 'games'] }),
  })
}
