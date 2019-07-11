
#ifndef _BISHOP_BASE_FIGURE_MOVE_OFFSETS_H_
#define _BISHOP_BASE_FIGURE_MOVE_OFFSETS_H_


#include "piece_type.h"
#include "file_rank_offset.h"


namespace bishop::base {
	class FigureMoveOffsets {
		private:
			static constexpr int DIRECTION_TABLE_SIZE = 16;
			static const FileRankOffset directionOffsetTable[DIRECTION_TABLE_SIZE];

			// These two arrays contains offsets and count of directions in directionOffsetTable
			// for each figure.
			static const int figureDirectionOffsets[PieceType::LAST];
			static const int figureDirectionCounts[PieceType::LAST];
    
			// Returns number of directions where given figure can move.
			static inline int getFigureDirectionCount (const PieceType::Type pieceType) {
				return figureDirectionCounts[pieceType];
			}

			// Returns offset for given direction.
			static inline FileRankOffset getFigureOffset (const PieceType::Type pieceType, const int direction) {
				const int offset = figureDirectionOffsets[pieceType]; 
    	
				return directionOffsetTable[offset + direction]; 
			}
	};
}

#endif

