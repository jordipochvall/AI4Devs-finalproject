import { render } from '@testing-library/react'
import { describe, it, expect, beforeAll, beforeEach, vi } from 'vitest'
import i18n from '../i18n/i18n'
import { useAudioStore } from './audioStore'
import { useGameAudio } from './useGameAudio'
import { audioBus, sfxUrl, voiceUrl, musicUrl } from './audio'
import type { SpinResult } from '../../player/api/playerApi'

// Mock only the audio bus (the "Howler.js" of this app); keep the pure helpers real.
vi.mock('./audio', async importOriginal => {
  const actual = await importOriginal<typeof import('./audio')>()
  return {
    ...actual,
    audioBus: { playMusic: vi.fn(), stopMusic: vi.fn(), playOnce: vi.fn(), setMuted: vi.fn() },
  }
})

function result(over: Partial<SpinResult> = {}): SpinResult {
  return {
    roundId: 1, betCents: 100, lineBetCents: 33, winCents: 0,
    balancePreCents: 0, balancePostCents: 0, view: [], winningPaylines: [], scatterCount: 0,
    freeSpins: { triggered: false, awarded: 0, rounds: [] }, ...over,
  }
}

function Harness({ res, spinning = false }: { res: SpinResult | null; spinning?: boolean }) {
  useGameAudio('FRUITS', res, 100, spinning)
  return null
}

describe('useGameAudio', () => {
  beforeAll(async () => { await i18n.changeLanguage('es') })
  beforeEach(() => {
    vi.clearAllMocks()
    useAudioStore.setState({ muted: false })
  })

  it('plays the theme ambient music on mount (AC1)', () => {
    render(<Harness res={null} />)
    expect(audioBus.playMusic).toHaveBeenCalledWith(musicUrl('FRUITS'))
  })

  it('stops the music and mutes when muted (AC: mute toggle)', () => {
    useAudioStore.setState({ muted: true })
    render(<Harness res={null} />)
    expect(audioBus.setMuted).toHaveBeenCalledWith(true)
    expect(audioBus.stopMusic).toHaveBeenCalled()
  })

  it('plays the spin SFX when a spin starts (AC1)', () => {
    const { rerender } = render(<Harness res={null} spinning={false} />)
    rerender(<Harness res={null} spinning />)
    expect(audioBus.playOnce).toHaveBeenCalledWith(sfxUrl('spin'))
  })

  it('plays the win SFX on a normal prize (AC1)', () => {
    render(<Harness res={result({ winCents: 500 })} />)
    expect(audioBus.playOnce).toHaveBeenCalledWith(sfxUrl('win'))
  })

  it('plays big-win SFX and announcer voice on a big win (AC1)', () => {
    render(<Harness res={result({ winCents: 5000 })} />) // 50× bet → big win
    expect(audioBus.playOnce).toHaveBeenCalledWith(sfxUrl('bigWin'))
    expect(audioBus.playOnce).toHaveBeenCalledWith(voiceUrl('bigWin', 'es'))
  })

  it('plays the free-spins SFX and voice when triggered (AC1)', () => {
    render(<Harness res={result({ freeSpins: { triggered: true, awarded: 8, rounds: [] } })} />)
    expect(audioBus.playOnce).toHaveBeenCalledWith(sfxUrl('freeSpin'))
    expect(audioBus.playOnce).toHaveBeenCalledWith(voiceUrl('freeSpins', 'es'))
  })

  it('selects the announcer voice in the user locale (AC3)', async () => {
    await i18n.changeLanguage('en')
    render(<Harness res={result({ freeSpins: { triggered: true, awarded: 8, rounds: [] } })} />)
    expect(audioBus.playOnce).toHaveBeenCalledWith(voiceUrl('freeSpins', 'en'))
    await i18n.changeLanguage('es')
  })
})
