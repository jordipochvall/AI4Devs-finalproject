import { describe, it, expect, beforeEach } from 'vitest'
import { useAudioStore } from './audioStore'

describe('audioStore', () => {
  beforeEach(() => {
    localStorage.clear()
    useAudioStore.setState({ muted: false })
  })

  it('starts unmuted', () => {
    expect(useAudioStore.getState().muted).toBe(false)
  })

  it('toggles mute', () => {
    useAudioStore.getState().toggleMute()
    expect(useAudioStore.getState().muted).toBe(true)
    useAudioStore.getState().toggleMute()
    expect(useAudioStore.getState().muted).toBe(false)
  })

  it('persists the mute choice to localStorage (AC4)', () => {
    useAudioStore.getState().setMuted(true)
    const persisted = JSON.parse(localStorage.getItem('nova-audio') ?? '{}')
    expect(persisted.state.muted).toBe(true)
  })
})
