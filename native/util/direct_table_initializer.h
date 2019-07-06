
#ifndef _UTIL_DIRECT_TABLE_INITIALIZER_H_
#define _UTIL_DIRECT_TABLE_INITIALIZER_H_


#include <cstddef>
#include <cassert>


namespace util {

	/**
	 * Direct table initializer is a function object that can be used as a init function for table.
	 * It enumerates all combinations of indices within given bounds, calls given funcion with them
	 * and stores the returned value to the table.
	 *
	 * Expected usage: makeDirectTableInitializer(func)::withBounds<L1, U1, L2, U2>()
	 * where Ln is inclusive lower bound of n-th index and Un is exclusive upper bound of n-th index.
	 */

	// Primary template for zero remaining indices.
	// Calls the function with given list of arguments and stores the result to the table.
	template<typename Func, size_t... bounds>
	class DirectTableInitializerImpl {
		private:
			const Func func;

		public:
			DirectTableInitializerImpl (Func const & func_):
				func (func_)
			{
			}

			template<typename Table, typename... Indices>
			void operator() (Table & table, const Indices... indices) const {
				table(indices...) = func(indices...);
			}
	};

	// Specialization for 1 + n indices.
	// Iterates one index in range <lowerBound, upperBound) and uses itself for remaining indices and bounds.
	template<typename Func, size_t lowerBound, size_t upperBound, size_t... remainingBounds>
	class DirectTableInitializerImpl<Func, lowerBound, upperBound, remainingBounds...> {
		static_assert(lowerBound < upperBound);

		private:
			Func func;

		public:
			DirectTableInitializerImpl (Func const & func_):
				func (func_)
			{
			}

			template<typename Table, typename... Indices>
			void operator() (Table &table, Indices... indices) const {
				for (size_t i = lowerBound; i < upperBound; i++)
					DirectTableInitializerImpl<Func, remainingBounds...>(func).operator()(table, indices..., i);
			}
	};

	// We want to specify only the bounds, but we want to let the compiler deduce the type argument Func.
	// To allow this we must separate the funcion from bounds.
	template<typename Func>
	class DirectTableInitializerFunctionHolder {
		private:
			Func func;

		public:
			DirectTableInitializerFunctionHolder (Func const & func_):
				func (func_)
			{
			}

			template<size_t... bounds>
			DirectTableInitializerImpl<Func, bounds...> withBounds() {
				static_assert (sizeof...(bounds) % 2 == 0);

				return DirectTableInitializerImpl<Func, bounds...>(func);
			}	

	};

	template<typename Func>
	DirectTableInitializerFunctionHolder<Func> makeDirectTableInitializer (Func const & func) {
		return DirectTableInitializerFunctionHolder<Func>(func);
	}
}

#endif

