
#ifndef _UTILS_TABLE_H_
#define _UTILS_TABLE_H_

#include "compound_index_calculator.h"

#include <cstddef>
#include <cassert>
#include <algorithm>


namespace util {

	/**
	 * Table is a multidimensional table that stores elements using compound index calculated with given offsets.
	 * Primary usage of table is to hold precalculated function values.
	 */
	template<size_t totalSize, typename T, int... offsets>
	class Table {
		public:
			typedef size_t size_type;

		private:
			T * const pData;

			template<typename... Indices>
			static inline size_t getCompoundIndex (const Indices... indices) {
				static_assert (sizeof...(offsets) == sizeof...(indices));
				
				const size_type compoundIndex = CompoundIndexCalculator<size_type, offsets...>::getIndex(indices...);
				assert (compoundIndex >= 0 && compoundIndex < totalSize);

				return compoundIndex;
			}
		
		public:
			/**
			 * Creates empty (default-initialized) table.
			 */
			Table():
				pData (new T[totalSize])
			{
				fill (T());
			}

			/**
			 * Creates empty (default-initialized) table and then calls initFunction (table) to populate the table. 
			 */
			template<typename F>
			Table(F const & initFunction):
				Table()
			{
				initFunction (*this);
			}

			/**
			 * Deletes the table.
			 */
			~Table() {
				delete[] pData;
			}

			/**
			 * Returns reference to element with given indices.
			 */
			template<typename... IDX>
			T& operator() (const IDX... indices) {
				return pData[getCompoundIndex(indices...)];
			}

			/**
			 * Returns element with given indices.
			 */
			template<typename... IDX>
			const T & operator() (const IDX... indices) const {
				return pData[getCompoundIndex(indices...)];
			}

			void fill (const T &value) {
				::std::fill_n (pData, totalSize, value);
			}
	};
}

#endif

