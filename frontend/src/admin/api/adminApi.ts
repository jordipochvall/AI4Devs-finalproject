import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import api from '../../shared/api/axiosClient'

/** An operator (tenant) as seen by the platform admin (HU-25). */
export interface Operator {
  id: number
  code: string
  name: string
  active: boolean
  createdAt: string
}

/** Payload to onboard a new operator with its initial OPERATOR user. */
export interface CreateOperatorPayload {
  code: string
  name: string
  operatorEmail: string
  operatorPassword: string
}

/** Query hook for the operators list. */
export function useOperators() {
  return useQuery({
    queryKey: ['admin', 'operators'],
    queryFn: () => api.get<Operator[]>('/admin/operators').then(r => r.data),
  })
}

/** Mutation hook to onboard a new operator. */
export function useCreateOperator() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (payload: CreateOperatorPayload) =>
      api.post<Operator>('/admin/operators', payload).then(r => r.data),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['admin', 'operators'] }),
  })
}

/** Mutation hook to activate/deactivate an operator. */
export function useSetOperatorActive() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: ({ id, active }: { id: number; active: boolean }) =>
      api.put<Operator>(`/admin/operators/${id}`, { active }).then(r => r.data),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['admin', 'operators'] }),
  })
}
