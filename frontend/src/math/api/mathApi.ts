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
