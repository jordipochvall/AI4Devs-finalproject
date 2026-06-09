import { useEffect, useRef } from 'react'
import { useTranslation } from 'react-i18next'
import type { SpinResult } from '../../player/api/playerApi'
import { useAudioStore } from './audioStore'
import { audioBus, classifyWin, musicUrl, sfxUrl, voiceUrl } from './audio'

/**
 * Wires the audio layer to a game surface (HU-10): ambient music per theme, SFX per event and the
 * announcer voice (in the user's locale) on special events. Honors the persistent mute toggle. It is
 * effect-only and best-effort, so it never blocks or degrades the spin animation (AC5).
 *
 * @param theme    game theme (selects the music track)
 * @param result   latest spin result (drives win / big-win / free-spin sounds)
 * @param betCents current bet (to classify big wins)
 * @param spinning whether a spin is in flight (triggers the spin SFX)
 */
export function useGameAudio(theme: string, result: SpinResult | null,
                            betCents: number, spinning: boolean): void {
  const muted = useAudioStore(s => s.muted)
  const { i18n } = useTranslation()
  const locale = i18n.language?.slice(0, 2) === 'en' ? 'en' : 'es'

  // Ambient music follows the theme and the mute toggle (AC1/AC4).
  useEffect(() => {
    audioBus.setMuted(muted)
    if (muted) {
      audioBus.stopMusic()
    } else {
      audioBus.playMusic(musicUrl(theme))
    }
    return () => audioBus.stopMusic()
  }, [theme, muted])

  // Spin SFX on each new spin (AC2).
  const wasSpinning = useRef(false)
  useEffect(() => {
    if (spinning && !wasSpinning.current) {
      audioBus.playOnce(sfxUrl('spin'))
    }
    wasSpinning.current = spinning
  }, [spinning])

  // Outcome SFX + announcer voice on each resolved spin (AC2/AC3).
  useEffect(() => {
    if (!result) {
      return
    }
    if (result.freeSpins.triggered) {
      audioBus.playOnce(sfxUrl('freeSpin'))
      audioBus.playOnce(voiceUrl('freeSpins', locale))
      return
    }
    const cls = classifyWin(result.winCents, betCents)
    if (cls === 'bigWin') {
      audioBus.playOnce(sfxUrl('bigWin'))
      audioBus.playOnce(voiceUrl('bigWin', locale))
    } else if (cls === 'win') {
      audioBus.playOnce(sfxUrl('win'))
    }
  }, [result])
}
