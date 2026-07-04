#!/bin/sh
# Builds the EGYPTIAN theme assets from hand-authored SVG sources + synthesized music.
#   - Symbols : art-src/egyptian/*.svg     -> public/assets/egyptian/*.png (256x256, transparent)
#   - Cover   : art-src/egyptian/cover.svg -> public/assets/egyptian/cover.jpg (960x540)
#   - Music   : synthesized Egyptian loop  -> public/assets/egyptian/music.mp3
#       Plucked oud/santoor lead (harmonics + exp decay) + shimmer arpeggio + plucked bass
#       (E-E-C-D) + darbuka percussion (doum/tek/shaker) + pad, with echo. Loop ~24s.
# Rendered by rsvg-convert (librsvg) + ImageMagick (PNG->JPG) + ffmpeg. Run inside an Alpine
# container with:  apk add rsvg-convert imagemagick ffmpeg font-dejavu
set -e
SRC=/app/art-src/egyptian
OUT=/app/public/assets/egyptian
mkdir -p "$OUT"

echo "== Rasterizing symbols =="
for s in wild scatter anubis scarab a; do
  rsvg-convert -w 256 -h 256 "$SRC/$s.svg" -o "$OUT/$s.png"
  echo "  $s.png"
done

echo "== Rasterizing cover =="
rsvg-convert -w 960 -h 540 "$SRC/cover.svg" -o /tmp/cover.png
convert /tmp/cover.png -background "#140a30" -flatten -quality 90 "$OUT/cover.jpg"
echo "  cover.jpg"

echo "== Composing music =="
D=/tmp/egy; rm -rf "$D"; mkdir -p "$D"

# Plucked note (harmonics + exponential decay) -> pseudo oud/santoor timbre.
pluck() { # freq dur decay outfile
  if [ "$1" = "0" ]; then
    ffmpeg -y -f lavfi -i "anullsrc=r=44100:cl=stereo" -t "$2" -ar 44100 -ac 2 "$4" >/dev/null 2>&1
    return
  fi
  FO=$(awk "BEGIN{printf \"%.3f\", $2-0.03}")
  EXPR="(0.6*sin(2*PI*$1*t)+0.3*sin(4*PI*$1*t)+0.18*sin(6*PI*$1*t)+0.1*sin(8*PI*$1*t))*exp(-$3*t)"
  ffmpeg -y -f lavfi -i "aevalsrc=$EXPR:d=$2:s=44100" \
    -af "volume=0.95,afade=t=out:st=$FO:d=0.03" -ar 44100 -ac 2 "$4" >/dev/null 2>&1
}

# --- Lead melody (phrygian dominant), 8s phrase ---
: > "$D/lead.txt"; LI=0
ln() { LI=$((LI+1)); F="$D/l$(printf %03d $LI).wav"; pluck "$1" "$2" 2.6 "$F"; echo "file '$F'" >> "$D/lead.txt"; }
ln 329.63 0.5;  ln 349.23 0.25; ln 415.30 0.25; ln 440.00 0.5;  ln 493.88 0.5
ln 523.25 0.25; ln 493.88 0.25; ln 440.00 0.5;  ln 415.30 0.5
ln 329.63 0.25; ln 349.23 0.25; ln 415.30 0.25; ln 440.00 0.25; ln 493.88 0.5;  ln 440.00 0.5
ln 415.30 0.25; ln 349.23 0.25; ln 329.63 0.75; ln 0 0.25;      ln 493.88 0.5;  ln 659.25 0.5
ffmpeg -y -f concat -safe 0 -i "$D/lead.txt" -c copy "$D/lead1.wav" >/dev/null 2>&1
ffmpeg -y -stream_loop 2 -i "$D/lead1.wav" -c copy "$D/lead.wav" >/dev/null 2>&1

# --- Shimmer arpeggio (2s cell, high, quiet), looped ---
: > "$D/shim.txt"; SI=0
sn() { SI=$((SI+1)); F="$D/s$(printf %03d $SI).wav"; pluck "$1" 0.25 5.0 "$F"; echo "file '$F'" >> "$D/shim.txt"; }
sn 659.25; sn 493.88; sn 830.61; sn 493.88; sn 659.25; sn 554.37; sn 830.61; sn 659.25
ffmpeg -y -f concat -safe 0 -i "$D/shim.txt" -c copy "$D/shim1.wav" >/dev/null 2>&1
ffmpeg -y -stream_loop 11 -i "$D/shim1.wav" -c copy "$D/shim.wav" >/dev/null 2>&1

# --- Bass: quarter notes, root per 2s bar, moving E-E-C-D ---
: > "$D/bass.txt"; BI=0
bn() { BI=$((BI+1)); F="$D/b$(printf %03d $BI).wav"; pluck "$1" 0.5 2.0 "$F"; echo "file '$F'" >> "$D/bass.txt"; }
for r in 82.41 82.41 65.41 73.42; do bn "$r"; bn "$r"; bn "$r"; bn "$r"; done
ffmpeg -y -f concat -safe 0 -i "$D/bass.txt" -c copy "$D/bass1.wav" >/dev/null 2>&1
ffmpeg -y -stream_loop 2 -i "$D/bass1.wav" -c copy "$D/bass.wav" >/dev/null 2>&1

# --- Percussion one-shots ---
ffmpeg -y -f lavfi -i "sine=frequency=58:duration=0.28" -af "volume=1.0,afade=t=out:st=0.03:d=0.25" -ar 44100 -ac 2 "$D/doum.wav" >/dev/null 2>&1
ffmpeg -y -f lavfi -i "anoisesrc=d=0.08:c=pink:a=0.6" -af "highpass=f=1600,volume=0.6,afade=t=out:st=0.01:d=0.07" -ar 44100 -ac 2 "$D/tek.wav" >/dev/null 2>&1
ffmpeg -y -f lavfi -i "anoisesrc=d=0.04:c=white:a=0.5" -af "highpass=f=7000,volume=0.4,afade=t=out:st=0.005:d=0.035" -ar 44100 -ac 2 "$D/shk.wav" >/dev/null 2>&1

# Drum bar (2s): doum 1 & 3, tek on off-beats.
ffmpeg -y -i "$D/doum.wav" -i "$D/doum.wav" -i "$D/tek.wav" -i "$D/tek.wav" -i "$D/tek.wav" -i "$D/tek.wav" -i "$D/tek.wav" \
  -filter_complex "[0]adelay=0|0[a];[1]adelay=1000|1000[b];[2]adelay=500|500[c];[3]adelay=750|750[d];[4]adelay=1250|1250[e];[5]adelay=1500|1500[f];[6]adelay=1750|1750[g];[a][b][c][d][e][f][g]amix=inputs=7:normalize=0,apad" \
  -t 2.0 -ar 44100 -ac 2 "$D/drumbar.wav" >/dev/null 2>&1
ffmpeg -y -stream_loop 11 -i "$D/drumbar.wav" -c copy "$D/drums.wav" >/dev/null 2>&1

# Shaker: one hit per 0.25s cell, looped.
ffmpeg -y -i "$D/shk.wav" -af apad -t 0.25 -ar 44100 -ac 2 "$D/shkcell.wav" >/dev/null 2>&1
ffmpeg -y -stream_loop 99 -i "$D/shkcell.wav" -c copy "$D/shaker.wav" >/dev/null 2>&1

# --- Pad/drone (E2+B2+E3) ---
ffmpeg -y -f lavfi -i "sine=frequency=82.41:duration=26" -f lavfi -i "sine=frequency=123.47:duration=26" -f lavfi -i "sine=frequency=164.81:duration=26" \
  -filter_complex "amix=inputs=3:normalize=0,volume=0.12,tremolo=f=0.25:d=0.5" -ar 44100 -ac 2 "$D/pad.wav" >/dev/null 2>&1

# --- Final mix ---
ffmpeg -y -i "$D/lead.wav" -i "$D/shim.wav" -i "$D/bass.wav" -i "$D/drums.wav" -i "$D/shaker.wav" -i "$D/pad.wav" \
  -filter_complex "[0]volume=0.55,aecho=0.8:0.85:90:0.22[l];[1]volume=0.2,aecho=0.7:0.8:120:0.2[s];[2]volume=0.5[b];[3]volume=0.5[d];[4]volume=0.22[k];[5]volume=1[p];[l][s][b][d][k][p]amix=inputs=6:normalize=0:duration=shortest,afade=t=in:d=1.0,alimiter=limit=0.9,volume=1.3" \
  -ar 44100 -ac 2 -b:a 128k "$OUT/music.mp3" >/dev/null 2>&1
echo "  music.mp3"

echo "== Done =="
ls -l "$OUT"
