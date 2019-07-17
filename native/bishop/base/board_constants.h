
#ifndef _BISHOP_BASE_BOARD_CONSTANTS_H_
#define _BISHOP_BASE_BOARD_CONSTANTS_H_


#include "bit_board.h"
#include "double_square_table.h"
#include "color.h"
#include "file.h"
#include "rank.h"
#include "square.h"
#include "piece_type.h"


namespace bishop::base {

	/**
	 * This class contains some constants related to the board.
	 * 
	 * @author Ing. Petr Ležák
	 */
	class BoardConstants {
		private:
			// Distances of pawns with given color on given ranks from promotion
			static const int PAWN_PROMOTION_DISTANCES[Color::LAST][Rank::LAST];

		public:
			// Mask of ranks
			static constexpr BitBoard::Type RANK_1_MASK = 0x00000000000000FFL;
			static constexpr BitBoard::Type RANK_2_MASK = 0x000000000000FF00L;
			static constexpr BitBoard::Type RANK_3_MASK = 0x0000000000FF0000L;
			static constexpr BitBoard::Type RANK_4_MASK = 0x00000000FF000000L;
			static constexpr BitBoard::Type RANK_5_MASK = 0x000000FF00000000L;
			static constexpr BitBoard::Type RANK_6_MASK = 0x0000FF0000000000L;
			static constexpr BitBoard::Type RANK_7_MASK = 0x00FF000000000000L;
			static constexpr BitBoard::Type RANK_8_MASK = 0xFF00000000000000L;

			static constexpr BitBoard::Type RANK_18_MASK = RANK_1_MASK | RANK_8_MASK;
			static constexpr BitBoard::Type PAWN_ALLOWED_SQUARES = ~BoardConstants::RANK_18_MASK;

			// Mask of files
			static constexpr BitBoard::Type FILE_A_MASK = 0x0101010101010101L;
			static constexpr BitBoard::Type FILE_B_MASK = 0x0202020202020202L;
			static constexpr BitBoard::Type FILE_C_MASK = 0x0404040404040404L;
			static constexpr BitBoard::Type FILE_D_MASK = 0x0808080808080808L;
			static constexpr BitBoard::Type FILE_E_MASK = 0x1010101010101010L;
			static constexpr BitBoard::Type FILE_F_MASK = 0x2020202020202020L;
			static constexpr BitBoard::Type FILE_G_MASK = 0x4040404040404040L;
			static constexpr BitBoard::Type FILE_H_MASK = 0x8080808080808080L;

			static constexpr BitBoard::Type FILE_ACFH_MASK = FILE_A_MASK | FILE_C_MASK | FILE_F_MASK | FILE_H_MASK;

			static constexpr BitBoard::Type WHITE_SQUARE_MASK = 0x55AA55AA55AA55AAL;
			static constexpr BitBoard::Type BLACK_SQUARE_MASK = 0xAA55AA55AA55AA55L;

			static constexpr BitBoard::Type BOARD_EDGE_MASK = FILE_A_MASK | FILE_H_MASK | RANK_1_MASK | RANK_8_MASK;

			static constexpr BitBoard::Type RANK_1278_MASK = RANK_1_MASK | RANK_2_MASK | RANK_7_MASK | RANK_8_MASK;
			static constexpr BitBoard::Type RANK_123678_MASK = RANK_1_MASK | RANK_2_MASK | RANK_3_MASK | RANK_6_MASK | RANK_7_MASK | RANK_8_MASK;

			// Mask of squares where pawn can capture to the left and to the right.
			// First and eight rank must be preserved to be able to calculate reverse
			// attacks.
			static constexpr BitBoard::Type LEFT_PAWN_CAPTURE_MASK = ~FILE_A_MASK;
			static constexpr BitBoard::Type RIGHT_PAWN_CAPTURE_MASK = ~FILE_H_MASK;


			static inline BitBoard::Type getSquareColorMask(const Color::Type squareColor) {
				return WHITE_SQUARE_MASK ^ (-(BitBoard::Type) squareColor);
			}

			static inline BitBoard::Type getRankMask(const Rank::Type rank) {
				return (BitBoard::Type) 0xFFL << (rank << File::BIT_COUNT);
			}

			static inline BitBoard::Type getFileMask(const File::Type file) {
				return (BitBoard::Type) 0x0101010101010101L << file;
			}

			/**
			 * Returns rank where pawns with given color moves by two squares.
			 *
			 * @param color pawn color
			 * @return Rank.R4 for white or Rank.R5 for black
			 */
			static inline Rank::Type getEpRank(const Color::Type color) {
				return Rank::R4 + color;
			}

			/**
			 * Returns square where pawn with given color moves by two squares.
			 *
			 * @param color pawn color
			 * @param file  file where is the pawn
			 * @return EP square
			 */
			static inline Square::Type getEpSquare(const Color::Type color, const File::Type file) {
				return Square::A4 + (color << File::BIT_COUNT) + file;
			}

			/**
			 * Returns target square of the capturing pawn.
			 *
			 * @param color  color of the captured (opposite to capturing) pawn
			 * @param epFile file where the pawn has moved by 2 squares
			 * @return EP target square
			 */
			static inline Square::Type getEpTargetSquare(const Color::Type color, const File::Type epFile) {
				const Square::Type colorComponent = (-(Square::Type) color) & 24;   // White = 0; Black = 24

				return Square::A3 + colorComponent + epFile;
			}

			static inline BitBoard::Type getEpRankMask(const Color::Type color) {
				const int shift = color << File::BIT_COUNT;
				
				return RANK_4_MASK << shift;
			}

			static inline Square::Type getPawnInitialSquare(const Color::Type color, const File::Type file) {
				return Square::onFileRank(file, Rank::getAbsolute(Rank::R2, color));
			}

			/**
			 * Returns promotion rank for given pawn color.
			 *
			 * @param color pawn color
			 * @return promotion rank
			 */
			static inline Rank::Type getPawnPromotionRank(const Color::Type color) {
				return ((Rank::Type) color - 1) & 0x07;
			}

			static inline Square::Type getPawnPromotionSquare(const Color::Type color, const Square::Type pawnSquare) {
				// Rank part of promotion square.
				//   0x38 ( = Rank.R8 << File.BIT_COUNT) for white
				//   0x00 ( = Rank.R1 << File.BIT_COUNT) for black
				const Square::Type rankPart = ((Square::Type) color - 1) & 0x38;

				// File part of promotion square
				const Square::Type filePart = pawnSquare & 0x07;

				return rankPart | filePart;
			}

			static inline int getPawnPromotionDistance(const Color::Type color, const Square::Type pawnSquare) {
				const Rank::Type pawnRank = Square::getRank(pawnSquare);

				return PAWN_PROMOTION_DISTANCES[color][pawnRank];
			}

			/**
			 * Returns offset of rank for move of pawn with given color.
			 * @param color color of the pawn
			 * @return the offset (+1 for white, -1 for black)
			 */
			static inline Rank::Difference getPawnRankOffset(const Color::Type color) {
				return 1 - ((Rank::Type) color << 1);
			}

			/**
			 * Returns offset of square for move of pawn with given color.
			 * @param color color of the pawn
			 * @return the offset
			 */
			static inline Square::Difference getPawnSquareOffset(const Color::Type color) {
				return 8 - ((Square::Type) color << 4);
			}

			/**
			 * Returns mask of squares in front of given square on same and neighbor
			 * files.
			 *
			 * @param color  color of player
			 * @param square square
			 * @return mask of squares in front of given square
			 */
			static const ColoredDoubleSquareTable getFrontSquaresOnThreeFiles;

			/**
			 * Returns mask of squares in front of given square on neighbor files.
			 *
			 * @param color  color of player
			 * @param square square
			 * @return mask of squares in front of given square
			 */
			static const ColoredDoubleSquareTable getFrontSquaresOnNeighborFiles;

			/**
			 * Returns mask of squares that if they would be occupied by opposite pawn
			 * stops pawn on given square.
			 *
			 * @param color  color of pawn
			 * @param square square
			 * @return mask of blocking squares
			 */
			static const ColoredDoubleSquareTable getPawnBlockingSquares;

			static const ColoredDoubleSquareTable getSquaresInFrontInclusive;

			static const ColoredDoubleSquareTable getSquaresInFrontExclusive;

			/**
			 * Returns one or two squares on same rank left and right to given square.
			 *
			 * @param square pawn square
			 * @return mask of neighbor squares
			 */
			static const DoubleSquareTable getConnectedPawnSquareMask;

			/**
			 * Returns union of masks obtained by calling getConnectedPawnSquareMask for
			 * every pawn in pawnMask.
			 *
			 * @param pawnMask mask of pawns
			 * @return mask of neighbor squares
			 */
			static inline BitBoard::Type getAllConnectedPawnSquareMask(const BitBoard::Type pawnMask) {
				const BitBoard::Type previousColumn = (pawnMask & ~BoardConstants::FILE_A_MASK) >> 1;
				const BitBoard::Type nextColumn = (pawnMask & ~BoardConstants::FILE_H_MASK) << 1;

				return previousColumn | nextColumn;
			}

			/**
			 * Returns mask of first rank of given side.
			 *
			 * @param color color of side
			 * @return first rank
			 */
			static inline BitBoard::Type getFirstRankMask(const Color::Type color) {
				return Color::colorFunction<BitBoard::Type, RANK_1_MASK, RANK_8_MASK> (color);
			}

			/**
			 * Returns mask of first rank of given side.
			 *
			 * @param color color of side
			 * @return first rank
			 */
			static inline BitBoard::Type getSecondRankMask(const Color::Type color) {
				return Color::colorFunction<BitBoard::Type, RANK_2_MASK, RANK_7_MASK> (color);
			}

			static inline BitBoard::Type getPawnsAttackedSquaresFromLeft(const Color::Type color, const BitBoard::Type pawnsMask) {
				const BitBoard::Type rightPawnMask = pawnsMask & RIGHT_PAWN_CAPTURE_MASK;
		
				if (color == Color::WHITE)
					return rightPawnMask << 9;
				else
					return rightPawnMask >> 7;
			}

			static inline BitBoard::Type getPawnsAttackedSquaresFromRight(const Color::Type color, const BitBoard::Type pawnsMask) {
				const BitBoard::Type leftPawnMask = pawnsMask & LEFT_PAWN_CAPTURE_MASK;

				if (color == Color::WHITE)
					return leftPawnMask << 7;
				else
					return leftPawnMask >> 9;
			}

			/**
			 * Returns all squares attacked by some pawn.
			 *
			 * @param color     color of the pawn
			 * @param pawnsMask pawn mask
			 * @return mask of attacked squares
			 */
			static inline BitBoard::Type getPawnsAttackedSquares(const Color::Type color, const BitBoard::Type pawnsMask) {
				const BitBoard::Type blackMask = (BitBoard::Type) -color;
				const BitBoard::Type whiteMask = ~blackMask;

				const BitBoard::Type leftPawnMask = (pawnsMask & LEFT_PAWN_CAPTURE_MASK) >> 1;
				const BitBoard::Type rightPawnMask = (pawnsMask & RIGHT_PAWN_CAPTURE_MASK) << 1;
				const BitBoard::Type combinedMask = leftPawnMask | rightPawnMask;
		
				return (whiteMask & (combinedMask << File::COUNT)) | (blackMask & (combinedMask >> File::COUNT));
			}

			static inline BitBoard::Type getKingsAttackedSquares(const BitBoard::Type kingsMask) {
				const BitBoard::Type fileExtension = ((kingsMask & ~FILE_A_MASK) >> 1) | ((kingsMask & ~FILE_H_MASK) << 1);
				const BitBoard::Type fileExtended = kingsMask | fileExtension;
				const BitBoard::Type rankExtension = ((fileExtended & ~RANK_1_MASK) >> File::COUNT)
						| ((fileExtended & ~RANK_8_MASK) << File::COUNT);
		
				return rankExtension | fileExtension;
			}

			/**
			 * Returns all squares where pawns can move by single step.
			 *
			 * @param color color of the pawn
			 * @param pawnsMask pawn mask
			 * @return mask of target squares
			 */
			static inline BitBoard::Type getPawnSingleMoveSquares(const Color::Type color, const BitBoard::Type pawnsMask) {
				if (color == Color::WHITE)
					return pawnsMask << File::COUNT;
				else
					return pawnsMask >> File::COUNT;
			}

			/**
			 * Returns mask of square on file preceding EP file on EP rank.
			 *
			 * @param color color of the pawn that has moved by two squares
			 * @param file  EP file
			 * @return mask of square on file preceding EP file on EP rank
			 */
			static const Table<Color::COUNT * File::COUNT, BitBoard::Type, File::BIT_COUNT, 0> getPrevEpFileMask;

			/**
			 * Returns mask of square on file succeeding EP file on EP rank.
			 *
			 * @param color color of the pawn that has moved by two squares
			 * @param file  EP file
			 * @return mask of square on file succeeding EP file on EP rank
			 */
			static const Table<Color::COUNT * File::COUNT, BitBoard::Type, File::BIT_COUNT, 0> getNextEpFileMask;

			/**
			 * Returns mask of squares where given piece can be placed.
			 *
			 * @param pieceType type of piece
			 * @return mask of allowed squares
			 */
			static const BitBoard::Type getPieceAllowedSquares(const PieceType::Type pieceType) {
				return (pieceType == PieceType::PAWN) ? PAWN_ALLOWED_SQUARES : BitBoard::FULL;
			}

			/**
			 * Returns distance from given file to the middle file of nearest pawn island in fileMask.
			 * @param fileMask
			 * @param file
			 *
			 * @return distance
			 */
			static const Table<File::COUNT << File::COUNT, File::Difference, File::BIT_COUNT, 0> getMinFileDistance;

			static const Table<Square::COUNT * Square::COUNT, Square::Difference, Square::BIT_COUNT, 0> getKingSquareDistance;
			
			/**
			 * Returns mask of squares attacked by king on given square plus given square.
			 *
			 * @param square king position
			 * @return mask of squares with king distance <= 1 from given square
			 */
			static const Table<Square::COUNT, BitBoard::Type, 0> getKingNearSquares;

			static const DoubleSquareTable getKingSafetyFarSquares;
	};

}

#endif

