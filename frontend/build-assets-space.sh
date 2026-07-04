#!/bin/sh
# Builds the SPACE ("Nova Cosmica") theme assets from SVG sources + synthesized music.
#   Symbols: wild scatter planet comet star k a -> public/assets/space/*.png
#   Cover  : art-src/space/cover.svg -> public/assets/space/cover.jpg (960x540)
#   Music  : driving cosmic loop (synth arp i-VI-III-VII + pulsing bass + four-on-floor
#            kick + backbeat clap + hats + pad, heavy reverb) -> public/assets/space/music.mp3
# Run in Alpine with: apk add rsvg-convert imagemagick ffmpeg font-dejavu
set -e
SRC=/app/art-src/space
OUT=/app/public/assets/space
mkdir -p "$OUT"

echo "== Rasterizing symbols =="
for s in wild scatter planet comet star k a; do
  rsvg-convert -w 256 -h 256 "$SRC/$s.svg" -o "$OUT/$s.png"; echo "  $s.png"
done
echo "== Rasterizing cover =="
rsvg-convert -w 960 -h 540 "$SRC/cover.svg" -o /tmp/scover.png
convert /tmp/scover.png -background "#04040f" -flatten -quality 90 "$OUT/cover.jpg"; echo "  cover.jpg"

echo "== Composing music =="
D=/tmp/spc; rm -rf "$D"; mkdir -p "$D"
pluck() { # freq dur decay outfile
  if [ "$1" = "0" ]; then ffmpeg -y -f lavfi -i "anullsrc=r=44100:cl=stereo" -t "$2" -ar 44100 -ac 2 "$4" >/dev/null 2>&1; return; fi
  FO=$(awk "BEGIN{printf \"%.3f\", $2-0.02}")
  EXPR="(0.5*sin(2*PI*$1*t)+0.32*sin(4*PI*$1*t)+0.22*sin(6*PI*$1*t)+0.14*sin(8*PI*$1*t))*exp(-$3*t)"
  ffmpeg -y -f lavfi -i "aevalsrc=$EXPR:d=$2:s=44100" -af "volume=0.95,afade=t=out:st=$FO:d=0.02" -ar 44100 -ac 2 "$4" >/dev/null 2>&1
}

# Arp (A minor: Am-F-C-G), eighth notes, 8s phrase
: > "$D/arp.txt"; AI=0
an() { AI=$((AI+1)); F="$D/a$(printf %03d $AI).wav"; pluck "$1" 0.25 4.2 "$F"; echo "file '$F'" >> "$D/arp.txt"; }
an 440.00; an 523.25; an 659.25; an 880.00; an 659.25; an 523.25; an 440.00; an 659.25
an 349.23; an 440.00; an 523.25; an 698.46; an 523.25; an 440.00; an 349.23; an 523.25
an 523.25; an 659.25; an 783.99; an 1046.50; an 783.99; an 659.25; an 523.25; an 783.99
an 392.00; an 493.88; an 587.33; an 783.99; an 587.33; an 493.88; an 392.00; an 587.33
ffmpeg -y -f concat -safe 0 -i "$D/arp.txt" -c copy "$D/arp1.wav" >/dev/null 2>&1
ffmpeg -y -stream_loop 2 -i "$D/arp1.wav" -c copy "$D/arp.wav" >/dev/null 2>&1

# Bass (roots A F C G), quarter notes
: > "$D/bass.txt"; BI=0
bn() { BI=$((BI+1)); F="$D/b$(printf %03d $BI).wav"; pluck "$1" 0.5 2.2 "$F"; echo "file '$F'" >> "$D/bass.txt"; }
for r in 110.00 87.31 65.41 98.00; do bn "$r"; bn "$r"; bn "$r"; bn "$r"; done
ffmpeg -y -f concat -safe 0 -i "$D/bass.txt" -c copy "$D/bass1.wav" >/dev/null 2>&1
ffmpeg -y -stream_loop 2 -i "$D/bass1.wav" -c copy "$D/bass.wav" >/dev/null 2>&1

# Percussion one-shots
ffmpeg -y -f lavfi -i "sine=frequency=56:duration=0.26" -af "volume=1.0,afade=t=out:st=0.03:d=0.23" -ar 44100 -ac 2 "$D/kick.wav" >/dev/null 2>&1
ffmpeg -y -f lavfi -i "anoisesrc=d=0.07:c=white:a=0.6" -af "highpass=f=1200,lowpass=f=4200,volume=0.5,afade=t=out:st=0.02:d=0.05" -ar 44100 -ac 2 "$D/clap.wav" >/dev/null 2>&1
ffmpeg -y -f lavfi -i "anoisesrc=d=0.03:c=white:a=0.5" -af "highpass=f=9000,volume=0.3,afade=t=out:st=0.004:d=0.026" -ar 44100 -ac 2 "$D/hat.wav" >/dev/null 2>&1

# Drum bar (2s): four-on-floor kick + backbeat clap
ffmpeg -y -i "$D/kick.wav" -i "$D/kick.wav" -i "$D/kick.wav" -i "$D/kick.wav" -i "$D/clap.wav" -i "$D/clap.wav" \
  -filter_complex "[0]adelay=0|0[a];[1]adelay=500|500[b];[2]adelay=1000|1000[c];[3]adelay=1500|1500[d];[4]adelay=500|500[e];[5]adelay=1500|1500[f];[a][b][c][d][e][f]amix=inputs=6:normalize=0,apad" \
  -t 2.0 -ar 44100 -ac 2 "$D/drumbar.wav" >/dev/null 2>&1
ffmpeg -y -stream_loop 11 -i "$D/drumbar.wav" -c copy "$D/drums.wav" >/dev/null 2>&1
ffmpeg -y -i "$D/hat.wav" -af apad -t 0.25 -ar 44100 -ac 2 "$D/hatcell.wav" >/dev/null 2>&1
ffmpeg -y -stream_loop 99 -i "$D/hatcell.wav" -c copy "$D/hats.wav" >/dev/null 2>&1

# Pad (A2 + E3 + A3), slow
ffmpeg -y -f lavfi -i "sine=frequency=110:duration=26" -f lavfi -i "sine=frequency=164.81:duration=26" -f lavfi -i "sine=frequency=220:duration=26" \
  -filter_complex "amix=inputs=3:normalize=0,volume=0.12,tremolo=f=0.2:d=0.5" -ar 44100 -ac 2 "$D/pad.wav" >/dev/null 2>&1

# Final mix (arp with lush reverb)
ffmpeg -y -i "$D/arp.wav" -i "$D/bass.wav" -i "$D/drums.wav" -i "$D/hats.wav" -i "$D/pad.wav" \
  -filter_complex "[0]volume=0.5,aecho=0.8:0.9:110|180:0.3|0.2[a];[1]volume=0.5[b];[2]volume=0.5[d];[3]volume=0.18[h];[4]volume=1[p];[a][b][d][h][p]amix=inputs=5:normalize=0:duration=shortest,afade=t=in:d=1.0,alimiter=limit=0.9,volume=1.3" \
  -ar 44100 -ac 2 -b:a 128k "$OUT/music.mp3" >/dev/null 2>&1
echo "  music.mp3"
echo "== Done =="; ls -l "$OUT"
