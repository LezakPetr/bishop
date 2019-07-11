
#include "figure_attack_table.h"
#include "figure_move_offsets.h"
#include "file_rank_offset.h"
#include "line_attack_table.h"


using namespace bishop::base;


const Table<FigureAttackTable::SIZE, BitBoard::Type, FigureAttackTable::PIECE_TYPE_SHIFT, FigureAttackTable::SQUARE_SHIFT> bishop::base::FigureAttackTable::getItem([](TheTable &table) {
	for (PieceType::Type figure = PieceType::FIGURE_FIRST; figure < PieceType::FIGURE_LAST; figure++) {
		if (PieceType::isShortMovingFigure(figure))
			initializeShortMoveFigureAttacks(figure, table);
		else
			initializeLongMoveFigureAttacks(figure, table);
	}
});

void bishop::base::FigureAttackTable::initializeShortMoveFigureAttacks(const PieceType::Type pieceType, TheTable &table) {
	const int directionCount = FigureMoveOffsets::getFigureDirectionCount(pieceType);

	for (Square::Type beginSquare = Square::FIRST; beginSquare < Square::LAST; beginSquare++) {
		const File::Type beginFile = Square::getFile(beginSquare);
		const Rank::Type beginRank = Square::getRank(beginSquare);
		    
		BitBoard::Type board = BitBoard::EMPTY;

		for (int direction = 0; direction < directionCount; direction++) {
			const FileRankOffset offset = FigureMoveOffsets::getFigureOffset (pieceType, direction);

			const File::Type targetFile = beginFile + offset.fileOffset;
			const Rank::Type targetRank = beginRank + offset.rankOffset;
		    	
			if (File::isValid(targetFile) && Rank::isValid(targetRank)) {
				const Square::Type targetSquare = Square::onFileRank(targetFile, targetRank);
				board |= BitBoard::getSquareMask(targetSquare);
			}
		}

		table(pieceType, beginSquare) = board;
	}
}

void bishop::base::FigureAttackTable::initializeLongMoveFigureAttacks(const PieceType::Type pieceType, TheTable &table) {
	const int directionCount = FigureMoveOffsets::getFigureDirectionCount(pieceType);

	for (Square::Type beginSquare = Square::FIRST; beginSquare < Square::LAST; beginSquare++) {
		BitBoard::Type board = BitBoard::EMPTY;

		for (int direction = 0; direction < directionCount; direction++) {
			const FileRankOffset offset = FigureMoveOffsets::getFigureOffset (pieceType, direction);
		    	
			board |= LineAttackTable::getLineMask(beginSquare, offset);
		}

		table(pieceType, beginSquare) = board;
	}
}

