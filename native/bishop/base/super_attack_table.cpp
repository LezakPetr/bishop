
#include "super_attack_table.h"
#include "figure_attack_table.h"
#include "../../util/direct_table_initializer.h"


using namespace bishop::base;


const Table<Square::LAST, BitBoard::Type, 0> bishop::base::SuperAttackTable::getItem(
	makeDirectTableInitializer([](Square::Type square) {
		return FigureAttackTable::getItem(PieceType::QUEEN, square) | FigureAttackTable::getItem(PieceType::KNIGHT, square);
	}).withBounds<Square::FIRST, Square::LAST>()
);

