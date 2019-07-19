
#include "castling_constants.h"


using namespace bishop::base;


const Table<Color::LAST * CastlingType::LAST, CastlingConstants, CastlingType::BIT_COUNT, 0> bishop::base::CastlingConstants::of([](auto &table) {
	table(Color::WHITE, CastlingType::SHORT) = CastlingConstants(
		BitBoard::of(Square::F1, Square::G1),
		Square::H1,
		Square::F1,
		Square::E1,
		Square::G1
	);
	
	table(Color::WHITE, CastlingType::LONG) = CastlingConstants(
		BitBoard::of(Square::B1, Square::C1, Square::D1),
		Square::A1,
		Square::D1,
		Square::E1,
		Square::C1
	);

	table(Color::BLACK, CastlingType::SHORT) = CastlingConstants(
		BitBoard::of(Square::F8, Square::G8),
		Square::H8,
		Square::F8,
		Square::E8,
		Square::G8
	);
	
	table(Color::BLACK, CastlingType::LONG) = CastlingConstants(
		BitBoard::of(Square::B8, Square::C8, Square::D8),
		Square::A8,
		Square::D8,
		Square::E8,
		Square::C8
	);
});

