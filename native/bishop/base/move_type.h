

#ifndef _BISHOP_BASE_MOVE_TYPE_H_
#define _BISHOP_BASE_MOVE_TYPE_H_


namespace bishop::base {
	class MoveType {
		public:
			typedef int_fast8_t Type;

			static constexpr Type INVALID = 0;
			static constexpr Type FIRST = 1;
	
			static constexpr Type NORMAL = 1;
			static constexpr Type PROMOTION = 2;
			static constexpr Type CASTLING = 3;
			static constexpr Type EN_PASSANT = 4;
			static constexpr Type NULL_MOVE = 5;
	
			static constexpr Type LAST = 6;

			static constexpr int BIT_COUNT = 3;
	};

}

#endif

