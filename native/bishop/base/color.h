
#ifndef _BISHOP_BASE_COLOR_H_
#define _BISHOP_BASE_COLOR_H_

#include <cstdint>

namespace bishop::base {
	/**
	 * Color of piece or player.
	 * @author Ing. Petr Ležák
	 */
	class Color {
		public:
			typedef int_fast8_t Type;

			static constexpr Color::Type FIRST = 0;
		
			static constexpr Color::Type WHITE = 0;
			static constexpr Color::Type BLACK = 1;
			
			static constexpr Color::Type LAST = 2;
			
			static constexpr Color::Type NONE = 15;
		
			// Number of bits to store color
			static constexpr int BIT_COUNT = 1;
				
			/**
			 * Checks if given color is valid.
			 * @param color color
			 * @return true if color is valid, false if not
			 */
			static inline bool isValid (const Color::Type color) {
				return color >= FIRST && color < LAST;
			}
			
			/**
			 * Returns opposite color to given one.
			 * @param color color
			 * @return opposite color
			 */
			static inline Color::Type getOppositeColor (const Color::Type color) {
				return color ^ 0x01;
			}
			
			/**
			 * Returns value if color == Color.WHITE.
			 * Returns -value if color == Color.BLACK.
			 * Result is undefined if color has other value.
			 */
			template<typename V> 
			static inline V colorNegate (const Color::Type color, const V value) {
				const V castedColor = (V) color;
				
				return (value ^ -castedColor) + castedColor;
			}

	};
	
}

#endif

