#!/bin/sh
# Builds the FRUITS ("Fruti Fiesta") theme assets from SVG sources + synthesized music.
#   Symbols: seven bar bar2 bar3 cherry lemon orange plum -> public/assets/fruits/*.png
#   Cover  : art-src/fruits/cover.svg -> public/assets/fruits/cover.jpg (960x540)
#   Music  : upbeat C-major arcade loop (bright pluck lead + oom-pah bass + four-on-floor
#            kick + backbeat clap + shaker) -> public/assets/fruits/music.mp3
# Run in Alpine with: apk add rsvg-convert imagemagick ffmpeg font-dejavu
set -e
SRC=/app/art-src/fruits
OUT=/app/public/assets/fruits
mkdir -p "$OUT"

echo "== Rasterizing symbols =="
for s in seven bar bar2 bar3 cherry lemon orange plum; do
  rsvg-convert -w 256 -h 256 "$SRC/$s.svg" -o "$OUT/$s.png"; echo "  $s.png"
done
echo "== Rasterizing cover =="
rsvg-convert -w 960 -h 540 "$SRC/cover.svg" -o /tmp/fcover.png
convert /tmp/fcover.png -background "#2a0d33" -flatten -quality 90 "$OUT/cover.jpg"; echo "  cover.jpg"

echo "== Composing music =="
D=/tmp/fru; rm -rf "$D"; mkdir -p "$D"
pluck() { # freq dur decay outfile
  if [ "$1" = "0" ]; then ffmpeg -y -f lavfi -i "anullsrc=r=44100:cl=stereo" -t "$2" -ar 44100 -ac 2 "$4" >/dev/null 2>&1; return; fi
  FO=$(awk "BEGIN{printf \"%.3f\", $2-0.03}")
  EXPR="(0.6*sin(2*PI*$1*t)+0.32*sin(4*PI*$1*t)+0.16*sin(6*PI*$1*t)+0.08*sin(8*PI*$1*t))*exp(-$3*t)"
  ffmpeg -y -f lavfi -i "aevalsrc=$EXPR:d=$2:s=44100" -af "volume=0.95,afade=t=out:st=$FO:d=0.03" -ar 44100 -ac 2 "$4" >/dev/null 2>&1
}

# Lead (bright C-major riff), 8s phrase
: > "$D/lead.txt"; LI=0
ln() { LI=$((LI+1)); F="$D/l$(printf %03d $LI).wav"; pluck "$1" "$2" 3.2 "$F"; echo "file '$F'" >> "$D/lead.txt"; }
ln 783.99 0.25; ln 659.25 0.25; ln 523.25 0.5;  ln 659.25 0.25; ln 783.99 0.25; ln 880.00 0.5
ln 783.99 0.25; ln 659.25 0.25; ln 587.33 0.5;  ln 659.25 0.5
ln 523.25 0.25; ln 659.25 0.25; ln 783.99 0.25; ln 1046.50 0.25; ln 880.00 0.5; ln 783.99 0.5
ln 659.25 0.25; ln 587.33 0.25; ln 523.25 0.75; ln 0 0.25;        ln 783.99 0.5; ln 1046.50 0.5
ffmpeg -y -f concat -safe 0 -i "$D/lead.txt" -c copy "$D/lead1.wav" >/dev/null 2>&1
ffmpeg -y -stream_loop 2 -i "$D/lead1.wav" -c copy "$D/lead.wav" >/dev/null 2>&1

# Bass oom-pah (I-vi-IV-V: C Am F G), root/fifth alternating
: > "$D/bass.txt"; BI=0
bn() { BI=$((BI+1)); F="$D/b$(printf %03d $BI).wav"; pluck "$1" 0.5 2.6 "$F"; echo "file '$F'" >> "$D/bass.txt"; }
bn 65.41; bn 98.00; bn 65.41; bn 98.00
bn 55.00; bn 82.41; bn 55.00; bn 82.41
bn 87.31; bn 130.81; bn 87.31; bn 130.81
bn 98.00; bn 146.83; bn 98.00; bn 146.83
ffmpeg -y -f concat -safe 0 -i "$D/bass.txt" -c copy "$D/bass1.wav" >/dev/null 2>&1
ffmpeg -y -stream_loop 2 -i "$D/bass1.wav" -c copy "$D/bass.wav" >/dev/null 2>&1

# Percussion one-shots
ffmpeg -y -f lavfi -i "sine=frequency=60:duration=0.24" -af "volume=1.0,afade=t=out:st=0.03:d=0.21" -ar 44100 -ac 2 "$D/kick.wav" >/dev/null 2>&1
ffmpeg -y -f lavfi -i "anoisesrc=d=0.07:c=white:a=0.6" -af "highpass=f=1100,lowpass=f=4200,volume=0.6,afade=t=out:st=0.02:d=0.05" -ar 44100 -ac 2 "$D/clap.wav" >/dev/null 2>&1
ffmpeg -y -f lavfi -i "anoisesrc=d=0.04:c=white:a=0.5" -af "highpass=f=7000,volume=0.35,afade=t=out:st=0.005:d=0.035" -ar 44100 -ac 2 "$D/shk.wav" >/dev/null 2>&1

# Drum bar (2s): four-on-floor kick + backbeat clap
ffmpeg -y -i "$D/kick.wav" -i "$D/kick.wav" -i "$D/kick.wav" -i "$D/kick.wav" -i "$D/clap.wav" -i "$D/clap.wav" \
  -filter_complex "[0]adelay=0|0[a];[1]adelay=500|500[b];[2]adelay=1000|1000[c];[3]adelay=1500|1500[d];[4]adelay=500|500[e];[5]adelay=1500|1500[f];[a][b][c][d][e][f]amix=inputs=6:normalize=0,apad" \
  -t 2.0 -ar 44100 -ac 2 "$D/drumbar.wav" >/dev/null 2>&1
ffmpeg -y -stream_loop 11 -i "$D/drumbar.wav" -c copy "$D/drums.wav" >/dev/null 2>&1
ffmpeg -y -i "$D/shk.wav" -af apad -t 0.25 -ar 44100 -ac 2 "$D/shkcell.wav" >/dev/null 2>&1
ffmpeg -y -stream_loop 99 -i "$D/shkcell.wav" -c copy "$D/shaker.wav" >/dev/null 2>&1

# Final mix
ffmpeg -y -i "$D/lead.wav" -i "$D/bass.wav" -i "$D/drums.wav" -i "$D/shaker.wav" \
  -filter_complex "[0]volume=0.55,aecho=0.8:0.85:60:0.15[l];[1]volume=0.5[b];[2]volume=0.5[d];[3]volume=0.2[k];[l][b][d][k]amix=inputs=4:normalize=0:duration=shortest,afade=t=in:d=0.6,alimiter=limit=0.9,volume=1.3" \
  -ar 44100 -ac 2 -b:a 128k "$OUT/music.mp3" >/dev/null 2>&1
echo "  music.mp3"
echo "== Done =="; ls -l "$OUT"
