#!/bin/sh
# Generates royalty-free PLACEHOLDER audio for the HU-10 immersive layer using ffmpeg's
# synthetic sources (sine/tremolo). These are demo sounds meant to be swapped for final
# licensed assets later. Output paths match shared/audio/audio.ts (musicUrl/sfxUrl/voiceUrl).
set -e
BASE=/app/public/assets
mkdir -p "$BASE/egyptian" "$BASE/fruits" "$BASE/space" "$BASE/sfx" "$BASE/voice/es" "$BASE/voice/en"

# Output encoding options (placed right before each output file).
OUT="-ar 44100 -ac 2 -b:a 96k"

# --- Ambient music loops (one chord pad per theme, ~16-24s) ---
# Egyptian: mysterious minor pad (Dm), slow tremolo.
ffmpeg -y -f lavfi -i "sine=frequency=146.83:duration=20" \
          -f lavfi -i "sine=frequency=174.61:duration=20" \
          -f lavfi -i "sine=frequency=220.00:duration=20" \
  -filter_complex "[0][1][2]amix=inputs=3:normalize=0,tremolo=f=3:d=0.4,volume=0.22,afade=t=in:st=0:d=1.5" \
  $OUT "$BASE/egyptian/music.mp3"

# Fruits: cheerful major pad (C), brighter tremolo.
ffmpeg -y -f lavfi -i "sine=frequency=261.63:duration=16" \
          -f lavfi -i "sine=frequency=329.63:duration=16" \
          -f lavfi -i "sine=frequency=392.00:duration=16" \
  -filter_complex "[0][1][2]amix=inputs=3:normalize=0,tremolo=f=6:d=0.3,volume=0.20,afade=t=in:st=0:d=1" \
  $OUT "$BASE/fruits/music.mp3"

# Space: deep ambient pad (Am, low), very slow tremolo.
ffmpeg -y -f lavfi -i "sine=frequency=110.00:duration=24" \
          -f lavfi -i "sine=frequency=164.81:duration=24" \
          -f lavfi -i "sine=frequency=246.94:duration=24" \
  -filter_complex "[0][1][2]amix=inputs=3:normalize=0,tremolo=f=2:d=0.5,volume=0.24,afade=t=in:st=0:d=2" \
  $OUT "$BASE/space/music.mp3"

# --- One-shot SFX ---
# spin: short low blip.
ffmpeg -y -f lavfi -i "sine=frequency=440:duration=0.22" \
  -af "volume=0.5,afade=t=out:st=0.14:d=0.08" $OUT "$BASE/sfx/spin.mp3"

# win: pleasant single ding.
ffmpeg -y -f lavfi -i "sine=frequency=880:duration=0.5" \
  -af "volume=0.5,afade=t=out:st=0.25:d=0.25" $OUT "$BASE/sfx/win.mp3"

# bigWin: major-triad celebratory sting.
ffmpeg -y -f lavfi -i "sine=frequency=523.25:duration=0.9" \
          -f lavfi -i "sine=frequency=659.25:duration=0.9" \
          -f lavfi -i "sine=frequency=783.99:duration=0.9" \
  -filter_complex "[0][1][2]amix=inputs=3:normalize=0,volume=0.5,afade=t=out:st=0.4:d=0.5" \
  $OUT "$BASE/sfx/bigWin.mp3"

# freeSpin: high sparkle with fast tremolo.
ffmpeg -y -f lavfi -i "sine=frequency=1200:duration=0.6" \
  -af "tremolo=f=18:d=0.7,volume=0.45,afade=t=out:st=0.3:d=0.3" $OUT "$BASE/sfx/freeSpin.mp3"

# --- Announcer voice placeholders (short jingles; swap for real TTS/VO later) ---
# bigWin fanfare (fuller chord); es/en share the same placeholder jingle.
ffmpeg -y -f lavfi -i "sine=frequency=523.25:duration=1.2" \
          -f lavfi -i "sine=frequency=659.25:duration=1.2" \
          -f lavfi -i "sine=frequency=783.99:duration=1.2" \
          -f lavfi -i "sine=frequency=1046.50:duration=1.2" \
  -filter_complex "[0][1][2][3]amix=inputs=4:normalize=0,tremolo=f=8:d=0.3,volume=0.4,afade=t=out:st=0.7:d=0.5" \
  $OUT "$BASE/voice/es/bigWin.mp3"
cp "$BASE/voice/es/bigWin.mp3" "$BASE/voice/en/bigWin.mp3"

# freeSpins rising two-note motif.
ffmpeg -y -f lavfi -i "sine=frequency=659.25:duration=1.0" \
          -f lavfi -i "sine=frequency=987.77:duration=1.0" \
  -filter_complex "[0][1]amix=inputs=2:normalize=0,tremolo=f=10:d=0.4,volume=0.4,afade=t=out:st=0.6:d=0.4" \
  $OUT "$BASE/voice/es/freeSpins.mp3"
cp "$BASE/voice/es/freeSpins.mp3" "$BASE/voice/en/freeSpins.mp3"

echo "AUDIO_GEN_OK"
ls -R "$BASE"
