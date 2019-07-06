
#include "../../util/compound_index_calculator.h"
#include "../test_base/test_base.h"

using namespace util;


TEST (test0D) {
	assertEquals (0, CompoundIndexCalculator<int>::getIndex());
}

TEST (test1D) {
	assertEquals ((2 << 3), CompoundIndexCalculator<int, 3>::getIndex(2));
}

TEST (test1DZero) {
	assertEquals (2, CompoundIndexCalculator<int, 0>::getIndex(2));
}

TEST (test2D) {
	assertEquals ((2 + (5 << 3)), CompoundIndexCalculator<int, 0, 3>::getIndex(2, 5));
}

TEST_RUNNER_BEGIN
	RUN_TEST(test0D);
	RUN_TEST(test1D);
	RUN_TEST(test1DZero);
	RUN_TEST(test2D);
TEST_RUNNER_END

