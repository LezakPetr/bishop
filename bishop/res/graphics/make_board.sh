DST_DIR=board
TARGET_FILE=board.brd

(cd $DST_DIR; zip -9 ../$TARGET_FILE *.svg manifest.xml)

