
#ifndef _UTIL_INDEX_COMPOUND_CALCULATOR_H_
#define _UTIL_INDEX_COMPOUND_CALCULATOR_H_


namespace util {
	/**
	 * Index calculator calculates compound index from given partial indices.
	 * For instance: CompoundIndexCalculator<size_t, 5, 3>::getIndex(a, b) returns (a << 5) + (b << 3)
	 */

	// Primary template for zero number of indices.
	template<typename Index, int... restOffsets>
	class CompoundIndexCalculator {
		public:
			static inline Index getIndex() {
				return (Index) 0;
			}

	};

	// Specialization for 1 + n indices.
	template<typename Index, int offset, int... restOffsets>
	class CompoundIndexCalculator<Index, offset, restOffsets...> {
		public:
			template<typename... RestIndices>
			static inline Index getIndex (const Index index, const RestIndices... restIndices) {
				static_assert (sizeof...(restIndices) == sizeof...(restOffsets));
						
				return (index << offset) + CompoundIndexCalculator<Index, restOffsets...>::getIndex(restIndices...);
			}
	};

	// Specialization for single offset with value 0. This is the most common case - 1D index without any offset.
	template<typename Index>
	class CompoundIndexCalculator<Index, 0> {
		public:
			static inline Index getIndex(const Index index) {
				return index;
			}

	};
}


#endif

