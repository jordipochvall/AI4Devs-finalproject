import { render, screen, fireEvent } from '@testing-library/react'
import { describe, it, expect, beforeAll, beforeEach, vi } from 'vitest'
import i18n from '../../shared/i18n/i18n'

const { mutate } = vi.hoisted(() => ({ mutate: vi.fn() }))

vi.mock('../api/mathApi', () => ({
  useConfigVersions: () => ({
    data: [
      { id: 22, version: 2, rtpTarget: 0.96, volatilityTarget: 0.5, active: true,  createdAt: '2026-06-01T00:00:00Z' },
      { id: 11, version: 1, rtpTarget: 0.94, volatilityTarget: 0.5, active: false, createdAt: '2026-05-01T00:00:00Z' },
    ],
    isLoading: false,
  }),
  usePublishConfig: () => ({ mutate, isPending: false, variables: undefined }),
}))

import VersionsPanel from './VersionsPanel'

describe('VersionsPanel (HU-17)', () => {
  beforeAll(async () => { await i18n.changeLanguage('es') })
  beforeEach(() => mutate.mockReset())

  it('AC1: lists versions and flags the active one', () => {
    render(<VersionsPanel gameId={10} />)
    expect(screen.getByText('2')).toBeInTheDocument()
    expect(screen.getByText('1')).toBeInTheDocument()
    expect(screen.getByText('Activa')).toBeInTheDocument()
  })

  it('AC2: publishing a non-active version calls the API with its configId', () => {
    render(<VersionsPanel gameId={10} />)
    const publishButtons = screen.getAllByRole('button', { name: 'Publicar' })
    // The active version's button is disabled; the inactive one (version 1, id 11) is clickable.
    const enabled = publishButtons.find(b => !(b as HTMLButtonElement).disabled)!
    fireEvent.click(enabled)
    expect(mutate).toHaveBeenCalledTimes(1)
    expect(mutate.mock.calls[0][0]).toEqual({ gameId: 10, configId: 11 })
  })

  it('AC3: the active version cannot be published (button disabled)', () => {
    render(<VersionsPanel gameId={10} />)
    const disabled = screen.getAllByRole('button', { name: 'Publicar' })
      .filter(b => (b as HTMLButtonElement).disabled)
    expect(disabled).toHaveLength(1)
  })
})
