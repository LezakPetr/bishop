
#ifndef _BISHOP_BASE_FILE_H_
#define _BISHOP_BASE_FILE_H_

#include <cstdint>

namespace bishop::base {
	
	class File {
		public:
			typedef int_fast8_t Type;

			static constexpr File::Type FIRST = 0;
			
			static constexpr File::Type FA = 0;
			static constexpr File::Type FB = 1;
			static constexpr File::Type FC = 2;
			static constexpr File::Type FD = 3;
			static constexpr File::Type FE = 4;
			static constexpr File::Type FF = 5;
			static constexpr File::Type FG = 6;
			static constexpr File::Type FH = 7;
			
			static constexpr File::Type LAST = 8;
			static constexpr File::Type NONE = 15;
			
			static constexpr int COUNT = LAST - FIRST;
			static constexpr int BIT_COUNT = 3;

			static constexpr double MIDDLE = (FA + FH) / 2.0;

			/**
			 * Checks if given file is valid.
			 * @param file file
			 * @return true if file is valid, false if not
			 */
			static inline bool isValid (const File::Type file) {
				return file >= FIRST && file < LAST;
			}

			static inline File::Type getOppositeFile(const File::Type file) {
				return FH - file;
			}
	};
}


#endif

