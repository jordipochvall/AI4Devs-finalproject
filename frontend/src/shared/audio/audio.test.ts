import { describe, it, expect } from 'vitest'
import { classifyWin, BIG_WIN_MULTIPLIER, musicUrl, sfxUrl, voiceUrl } from './audio'

describe('classifyWin', () => {
  it('returns none for a zero win', () => {
    expect(classifyWin(0, 100)).toBe('none')
  })

  it('returns win for a normal prize', () => {
    expect(classifyWin(500, 100)).toBe('win')
  })

  it('returns bigWin at or above the big-win multiplier', () => {
    expect(classifyWin(BIG_WIN_MULTIPLIER * 100, 100)).toBe('bigWin')
    expect(classifyWin(BIG_WIN_MULTIPLIER * 100 - 1, 100)).toBe('win')
  })
})

describe('asset url helpers', () => {
  it('builds theme/sfx/voice paths', () => {
    expect(musicUrl('FRUITS')).toBe('/assets/fruits/music.mp3')
    expect(sfxUrl('bigWin')).toBe('/assets/sfx/bigWin.mp3')
    expect(voiceUrl('freeSpins', 'es')).toBe('/assets/voice/es/freeSpins.mp3')
  })
})
