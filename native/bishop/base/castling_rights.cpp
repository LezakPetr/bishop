
#include "castling_rights.h"
#include "../../util/direct_table_initializer.h"


using namespace bishop::base;


const Table<Square::COUNT, CastlingRights::Index, 0> bishop::base::CastlingRights::TABLE_SQUARE_RIGHT_MASK([](auto &table) {
	table.fill(CastlingRights::FULL_RIGHTS);

	table(Square::E1) &= ~getMaskOfRight (Color::WHITE, CastlingType::SHORT);
	table(Square::H1) &= ~getMaskOfRight (Color::WHITE, CastlingType::SHORT);

	table(Square::E1) &= ~getMaskOfRight (Color::WHITE, CastlingType::LONG);
	table(Square::A1) &= ~getMaskOfRight (Color::WHITE, CastlingType::LONG);

	table(Square::E8) &= ~getMaskOfRight (Color::BLACK, CastlingType::SHORT);
	table(Square::H8) &= ~getMaskOfRight (Color::BLACK, CastlingType::SHORT);

	table(Square::E8) &= ~getMaskOfRight (Color::BLACK, CastlingType::LONG);
	table(Square::A8) &= ~getMaskOfRight (Color::BLACK, CastlingType::LONG);
});

const Table<Color::COUNT, CastlingRights::Index, 0> bishop::base::CastlingRights::TABLE_COLOR_RIGHT_MASK(
	makeDirectTableInitializer(
		[](const Color::Type color) {
			return CastlingRights::getMaskOfRight (color, CastlingType::SHORT) | CastlingRights::getMaskOfRight (color, CastlingType::LONG);
		}
	).withBounds<Color::FIRST, Color::LAST>()
);
	
