#ifndef _BISHOP_BASE_RANK_H_
#define _BISHOP_BASE_RANK_H_

#include "color.h"
#include <cstdint>

namespace bishop::base {
	
	class Rank {
		public:
			typedef int_fast8_t Type;
			typedef int_fast8_t Difference;

			static constexpr Rank::Type FIRST = 0;
			
			static constexpr Rank::Type R1 = 0;
			static constexpr Rank::Type R2 = 1;
			static constexpr Rank::Type R3 = 2;
			static constexpr Rank::Type R4 = 3;
			static constexpr Rank::Type R5 = 4;
			static constexpr Rank::Type R6 = 5;
			static constexpr Rank::Type R7 = 6;
			static constexpr Rank::Type R8 = 7;
			
			static constexpr Rank::Type LAST = 8;
			static constexpr Rank::Difference COUNT = LAST - FIRST;
			static constexpr int BIT_COUNT = 3;

			static constexpr double MIDDLE = (R1 + R8) / 2.0;

	
			/**
			 * Checks if given rank is valid.
			 * @param rank rank
			 * @return true if rank is valid, false if not
			 */
			static inline bool isValid (const int rank) {
				return rank >= FIRST && rank < LAST;
			}
			
			static inline Rank::Type getOppositeRank (const Rank::Type rank) {
				return Rank::R8 - rank;
			}
			
			static inline Rank::Type getAbsolute(const Rank::Type relativeRank, const Color::Type color) {
				return (relativeRank ^ (-color)) & 0x07;
			}
			
			static inline Rank::Type getRelative(const Rank::Type absoluteRank, const Color::Type color) {
				return getAbsolute(absoluteRank, color);
			}

			static Rank::Type fromChar (const char ch);

	};

}

#endif

