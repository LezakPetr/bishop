
PARAMS="-ggdb -Wall"

UTIL_CPP="string_util.cpp"
UTIL_OBJS="../../util/string_util.o"

BISHOP_BASE_CPP="bit_board.cpp figure_move_offsets.cpp figure_attack_table.cpp between_table.cpp line_indexer.cpp line_attack_table.cpp board_constants.cpp file.cpp rank.cpp square.cpp castling_rights.cpp castling_constants.cpp pawn_attack_table.cpp super_attack_table.cpp pawn_move_table.cpp"
BISHOP_BASE_OBJS="../../bishop/base/bit_board.o ../../bishop/base/figure_move_offsets.o ../../bishop/base/figure_attack_table.o ../../bishop/base/between_table.o ../../bishop/base/line_indexer.o ../../bishop/base/line_attack_table.o ../../bishop/base/board_constants.o ../../bishop/base/file.o ../../bishop/base/rank.o ../../bishop/base/square.o ../../bishop/base/castling_rights.o ../../bishop/base/castling_constants.o ../../bishop/base/pawn_attack_table.o ../../bishop/base/super_attack_table.o ../../bishop/base/pawn_move_table.o"

TESTS_UTIL_CPP="compound_index_calculator_test.cpp direct_table_initializer_test.cpp table_test.cpp"
TESTS_BISHOP_CPP="bit_board_combinator_test.cpp bit_board_test.cpp bit_loop_test.cpp color_test.cpp file_test.cpp line_indexer_test.cpp rank_test.cpp square_test.cpp between_table_test.cpp board_constants_test.cpp castling_constants_test.cpp pseudo_legal_move_generator_test.cpp"

function buildObjects() {
	for i in $1; do
		echo $i
		g++ $PARAMS -c $i
	done
}

function buildExecutables() {
	DEPS=$2

	for CPP in $1; do
		echo $CPP
		EXE=`echo $CPP | cut -d "." -f 1`
		g++ $PARAMS -o $EXE $CPP $DEPS
	done
}

function executeTests() {
	for CPP in $1; do
		echo $CPP
		EXE=`echo $CPP | cut -d "." -f 1`
		./$EXE
	done
}


(cd util/ && buildObjects "$UTIL_CPP")
(cd bishop/base/ && buildObjects "$BISHOP_BASE_CPP")
(cd tests/util/ && buildExecutables "$TESTS_UTIL_CPP")
(cd tests/bishop/ && buildExecutables "$TESTS_BISHOP_CPP" "$BISHOP_BASE_OBJS $UTIL_OBJS")

(cd tests/util/ && executeTests "$TESTS_UTIL_CPP")
(cd tests/bishop/ && executeTests "$TESTS_BISHOP_CPP")

