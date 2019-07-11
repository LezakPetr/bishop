

#include "line_attack_table.h"
#include "bit_board_combinator.h"
#include "bit_loop.h"
#include "between_table.h"


using namespace bishop::base;


const LineAttackTable::TheTable bishop::base::LineAttackTable::getAttackMask([](auto &table) { initializeTable (table, 0); });
const LineAttackTable::TheTable bishop::base::LineAttackTable::getPinMask([](auto &table) { initializeTable (table, 1); });


void bishop::base::LineAttackTable::initializeTable (TheTable &table, const int betweenCount) {
	for (CrossDirection::Type direction = CrossDirection::FIRST; direction < CrossDirection::LAST; direction++) {
		for (Square::Type square = Square::FIRST; square < Square::LAST; square++) {
			BitBoard::Type mask = BitBoard::EMPTY;
				
			for (FileRankOffset offset: LineIndexer::DIRECTION_OFFSETS[direction]) {
				mask |= LineAttackTable::getLineMask(square, offset);
			}
				
			for (BitBoardCombinator combinator(mask); combinator.hasNextCombination(); ) {
				const BitBoard::Type occupancy = combinator.getNextCombination();
				const BitBoard::Type attackMask = calculateAttackMask(square, mask, occupancy, betweenCount);
					
				const LineIndexer::IndexType index = LineIndexer::getLineIndex(direction, square, occupancy);
				table(index) = attackMask;
			}
		}
	}
}

BitBoard::Type bishop::base::LineAttackTable::getLineMask (const Square::Type square, const FileRankOffset offset) {
	BitBoard::Type mask = BitBoard::EMPTY;

	File::Type targetFile = Square::getFile(square) + offset.fileOffset;
	Rank::Type targetRank = Square::getRank(square) + offset.rankOffset;

    	while (File::isValid(targetFile) && Rank::isValid(targetRank)) {
		const Square::Type targetSquare = Square::onFileRank(targetFile, targetRank);
    		mask |= BitBoard::getSquareMask(targetSquare);

    		targetFile += offset.fileOffset;
    		targetRank += offset.rankOffset;
    	}
    	
    	return mask;
}


BitBoard::Type bishop::base::LineAttackTable::calculateAttackMask (const Square::Type square, const BitBoard::Type cross, const BitBoard::Type occupancy, const int betweenCount) {
	BitBoard::Type attackMask = BitBoard::EMPTY;
		
	for (BitLoop loop(cross); loop.hasNextSquare(); ) {
		const Square::Type targetSquare = loop.getNextSquare();
		const BitBoard::Type betweenOccupiedSquares = BetweenTable::getItem(square, targetSquare) & occupancy;
			
		if (BitBoard::getSquareCount(betweenOccupiedSquares) == betweenCount)
			attackMask |= BitBoard::getSquareMask(targetSquare);
	}
	
	return attackMask;
}

