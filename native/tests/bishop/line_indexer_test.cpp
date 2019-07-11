

#include "../../bishop/base/line_indexer.h"
#include "../../bishop/base/bit_board_combinator.h"
#include "../test_base/test_base.h"

#include <set>

using namespace bishop::base;


TEST(testIndices) {
	std::set<LineIndexer::IndexType> indices;
	
	for (CrossDirection::Type direction = CrossDirection::FIRST; direction < CrossDirection::LAST; direction++) {
		for (Square::Type square = Square::FIRST; square < Square::LAST; square++) {
			const BitBoard::Type mask = LineIndexer::calculateDirectionMask(direction, square);
			
			for (BitBoardCombinator combinator(mask); combinator.hasNextCombination(); ) {
				const BitBoard::Type combination = combinator.getNextCombination();
				const LineIndexer::IndexType index = LineIndexer::getLineIndex(direction, square, combination);
				assertTrue (index >= 0 && index < LineIndexer::LAST_INDEX);
				
				const bool dupplicate = !indices.insert(index).second;
				assertFalse (dupplicate);
			}
		}
	}
}

TEST_RUNNER_BEGIN
	RUN_TEST(testIndices);
TEST_RUNNER_END

