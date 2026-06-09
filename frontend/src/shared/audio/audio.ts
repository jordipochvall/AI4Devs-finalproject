/**
 * Lightweight audio layer built on the native {@code HTMLAudioElement} (no extra dependency). All
 * playback is best-effort and guarded: if an asset is missing or the browser blocks autoplay, it
 * fails silently and never blocks the spin animation (HU-10 AC5). Assets live under
 * {@code /assets/<theme>/} and {@code /assets/sfx|voice/}.
 */

/** A spin event becomes a "big win" when the prize reaches this multiple of the bet. */
export const BIG_WIN_MULTIPLIER = 10

export type WinClass = 'none' | 'win' | 'bigWin'

/** Classifies a spin's prize for audio purposes (pure, unit-tested). */
export function classifyWin(winCents: number, betCents: number): WinClass {
  if (winCents <= 0) return 'none'
  if (betCents > 0 && winCents >= BIG_WIN_MULTIPLIER * betCents) return 'bigWin'
  return 'win'
}

/** Best-effort player around a single {@code HTMLAudioElement}; never throws. */
class AudioBus {
  private music: HTMLAudioElement | null = null
  private muted = false

  /** Applies the mute state to the ambient music (SFX read it when created). */
  setMuted(muted: boolean): void {
    this.muted = muted
    if (this.music) {
      this.music.muted = muted
    }
  }

  /** Loops the theme's ambient music; restarts only if the source changes. */
  playMusic(url: string): void {
    if (this.music && this.music.src.endsWith(url)) {
      return
    }
    this.stopMusic()
    try {
      const audio = new Audio(url)
      audio.loop = true
      audio.muted = this.muted
      this.music = audio
      this.safePlay(audio)
    } catch {
      /* audio unavailable — ignore */
    }
  }

  /** Stops and releases the ambient music. */
  stopMusic(): void {
    if (this.music) {
      try {
        this.music.pause()
      } catch {
        /* ignore */
      }
      this.music = null
    }
  }

  /** Plays a one-shot sound (SFX or announcer voice), respecting mute. */
  playOnce(url: string): void {
    if (this.muted) {
      return
    }
    try {
      this.safePlay(new Audio(url))
    } catch {
      /* ignore */
    }
  }

  private safePlay(audio: HTMLAudioElement): void {
    try {
      const result = audio.play() as unknown as Promise<void> | undefined
      if (result && typeof result.catch === 'function') {
        result.catch(() => { /* autoplay blocked — ignore */ })
      }
    } catch {
      /* play not available (e.g. jsdom) — ignore */
    }
  }
}

/** Asset URL helpers. */
export const musicUrl = (theme: string) => `/assets/${theme.toLowerCase()}/music.mp3`
export const sfxUrl = (name: string) => `/assets/sfx/${name}.mp3`
export const voiceUrl = (event: string, locale: string) =>
  `/assets/voice/${locale}/${event}.mp3`

/** Process-wide audio bus. */
export const audioBus = new AudioBus()
