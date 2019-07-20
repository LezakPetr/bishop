

#ifndef _BISHOP_BASE_CASTLING_TYPE_H_
#define _BISHOP_BASE_CASTLING_TYPE_H_


namespace bishop::base {
	
	class CastlingType {
		public:
			typedef int_fast8_t Type;

			static constexpr CastlingType::Type FIRST = 0;
	
			static constexpr CastlingType::Type SHORT = 0;
			static constexpr CastlingType::Type LONG = 1;
	
			static constexpr CastlingType::Type LAST = 2;
			static constexpr int BIT_COUNT = 1;
	};
}

#endif

