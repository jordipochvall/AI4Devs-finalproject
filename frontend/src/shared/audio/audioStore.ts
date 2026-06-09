import { create } from 'zustand'
import { persist } from 'zustand/middleware'

interface AudioState {
  muted: boolean
  toggleMute: () => void
  setMuted: (muted: boolean) => void
}

/**
 * Audio preferences kept in the session store, persisted to localStorage under "nova-audio" so the
 * mute toggle survives across sessions (HU-10 AC4).
 */
export const useAudioStore = create<AudioState>()(
  persist(
    set => ({
      muted: false,
      toggleMute: () => set(s => ({ muted: !s.muted })),
      setMuted: muted => set({ muted }),
    }),
    { name: 'nova-audio' },
  ),
)
