DST_DIR=piece_set
TARGET_FILE=piece_set.pcs

(cd $DST_DIR; zip -9 ../$TARGET_FILE *.svg manifest.xml)

