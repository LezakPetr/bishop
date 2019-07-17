
#ifndef _BISHOP_BASE_DOUBLE_SQUARE_TABLE_H_
#define _BISHOP_BASE_DOUBLE_SQUARE_TABLE_H_


namespace bishop::base {

	template<typename Func>
	class DoubleSquareTableInitializer {
		private:
			const Func func;

		public:
			DoubleSquareTableInitializer (Func const & func_):
				func(func_)
			{
			}

			template<typename Table>
			void operator() (Table &table) const {
				for (Square::Type drivingSquare = Square::FIRST; drivingSquare < Square::LAST; drivingSquare++) {
					BitBoard::Type mask = BitBoard::EMPTY;

					for (Square::Type maskedSquare = Square::FIRST; maskedSquare < Square::LAST; maskedSquare++) {
						if (func(drivingSquare, maskedSquare))
							mask |= BitBoard::getSquareMask(maskedSquare);
					}

					table(drivingSquare) = mask;
				}
			}
	};

	template<typename Func>
	DoubleSquareTableInitializer<Func> makeDoubleSquareTableInitializer(Func const & func) {
		return DoubleSquareTableInitializer<Func>(func);
	}

	typedef Table<Square::LAST, BitBoard::Type, 0> DoubleSquareTable;


	template<typename Func>
	class ColoredDoubleSquareTableInitializer {
		private:
			const Func func;

		public:
			ColoredDoubleSquareTableInitializer (Func const & func_):
				func(func_)
			{
			}

			template<typename Table>
			void operator() (Table &table) const {
				for (Square::Type drivingSquare = Square::FIRST; drivingSquare < Square::LAST; drivingSquare++) {
					BitBoard::Type mask = BitBoard::EMPTY;

					for (Square::Type maskedSquare = Square::FIRST; maskedSquare < Square::LAST; maskedSquare++) {
						if (func(drivingSquare, maskedSquare))
							mask |= BitBoard::getSquareMask(maskedSquare);
					}

					table(Color::WHITE, drivingSquare) = mask;
					table(Color::BLACK, Square::getOppositeSquare(drivingSquare)) = BitBoard::getMirrorBoard(mask);
				}
			}
	};

	template<typename Func>
	ColoredDoubleSquareTableInitializer<Func> makeColoredDoubleSquareTableInitializer(Func const & func) {
		return ColoredDoubleSquareTableInitializer<Func>(func);
	}

	typedef Table<Color::LAST * Square::LAST, BitBoard::Type, Square::BIT_COUNT, 0> ColoredDoubleSquareTable;
}


#endif

