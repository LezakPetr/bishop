
#ifndef _BISHOP_BASE_CROSS_DIRECTION_H_
#define _BISHOP_BASE_CROSS_DIRECTION_H_


#include <cstdint>


namespace bishop::base {
	class CrossDirection {
		public:
			typedef int_fast8_t Type;

			static constexpr CrossDirection::Type FIRST = 0;
	
			static constexpr CrossDirection::Type ORTHOGONAL = 0;
			static constexpr CrossDirection::Type DIAGONAL = 1;
	
			static constexpr CrossDirection::Type LAST = 2;
	};
}

#endif

