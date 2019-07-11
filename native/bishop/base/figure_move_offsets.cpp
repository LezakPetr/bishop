
#include "figure_move_offsets.h"

using namespace bishop::base;

const FileRankOffset bishop::base::FigureMoveOffsets::directionOffsetTable[bishop::base::FigureMoveOffsets::DIRECTION_TABLE_SIZE] = {
    	// Rook
	{+1, 0}, {0, -1}, {-1, 0}, {0, +1},

    	// Bishop
	{+1, +1}, {+1, -1}, {-1, -1}, {-1, +1},
    	
    	// Knight
	{+1, +2}, {+2, +1}, {+2, -1}, {+1, -2}, {-1, -2}, {-2, -1}, {-2, +1}, {-1, +2}
};

const int bishop::base::FigureMoveOffsets::figureDirectionOffsets[PieceType::LAST] = {0, 0, 0, 4, 8, -1};
const int bishop::base::FigureMoveOffsets::figureDirectionCounts[PieceType::LAST]  = {8, 8, 4, 4, 8, -1};
    


