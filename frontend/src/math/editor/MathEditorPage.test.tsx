import { render, screen, fireEvent } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { describe, it, expect, beforeAll, beforeEach, vi } from 'vitest'
import i18n from '../../shared/i18n/i18n'

const { mutate } = vi.hoisted(() => ({ mutate: vi.fn() }))

vi.mock('../api/mathApi', () => {
  // Stable references so the editor's useEffect does not loop.
  const game = {
    id: 3, code: 'space-5x3', name: 'Espacial', theme: 'SPACE',
    active: true, activeConfigId: 30, activeVersion: 1,
  }
  const configData = {
    id: 30, gameId: 3, version: 1, rtpTarget: 0.965, volatilityTarget: 12, notes: null,
    config: { grid: { cols: 5, rows: 3 } },
  }
  return {
    useMathGames: () => ({ data: [game], isLoading: false, isError: false }),
    useConfig: (id: number | null) => ({ data: id != null ? configData : undefined, isLoading: false }),
    useCreateConfig: () => ({ mutate, isPending: false }),
    // The page now embeds the simulation panel; stub its hooks too (no network).
    useLaunchSimulation: () => ({ mutate: vi.fn(), isPending: false, isError: false, error: null }),
    useSimulation: () => ({ data: undefined }),
  }
})

import MathEditorPage from './MathEditorPage'

const renderPage = () => render(<MemoryRouter><MathEditorPage /></MemoryRouter>)
const selectGame = () => fireEvent.change(screen.getByLabelText('Juego'), { target: { value: '3' } })

describe('MathEditorPage', () => {
  beforeAll(async () => { await i18n.changeLanguage('es') })
  beforeEach(() => mutate.mockReset())

  it('loads the active config into the editor when a game is selected (AC1)', () => {
    renderPage()
    selectGame()
    const editor = screen.getByLabelText(/Configuración/) as HTMLTextAreaElement
    expect(editor.value).toContain('"grid"')
    expect(editor.value).toContain('"cols": 5')
  })

  it('with invalid JSON it shows a client error and does not call the backend (AC2)', () => {
    renderPage()
    selectGame()
    fireEvent.change(screen.getByLabelText(/Configuración/), { target: { value: 'no-es-json' } })
    fireEvent.click(screen.getByText('Guardar nueva versión'))

    expect(screen.getByText(/no es válido/)).toBeInTheDocument()
    expect(mutate).not.toHaveBeenCalled()
  })

  it('with a valid config and declared rtp it invokes version creation (AC4)', () => {
    renderPage()
    selectGame()
    fireEvent.click(screen.getByText('Guardar nueva versión'))

    expect(mutate).toHaveBeenCalledTimes(1)
    const args = mutate.mock.calls[0][0]
    expect(args).toMatchObject({ gameId: 3, rtpTarget: 0.965 })
    expect(args.config).toMatchObject({ grid: { cols: 5, rows: 3 } })
  })
})
