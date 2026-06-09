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

/** Mutation hook that asks the AI to explain a completed simulation. */
export function useExplain() {
  return useMutation({
    mutationFn: ({ simulationId, question }: { simulationId: number; question: string }) =>
      api.post<Explanation>(`/math/simulations/${simulationId}/explain`, { question }).then(r => r.data),
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
