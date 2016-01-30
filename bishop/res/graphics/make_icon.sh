DST_DIR=icon
SRC_FILE=piece_set/wb.svg
DIMENSIONS="16 24 32 48 64 96 128"

for d in $DIMENSIONS; do
	inkscape --export-png=${DST_DIR}/icon_${d}.png --export-width=$d --export-height=$d $SRC_FILE
done


