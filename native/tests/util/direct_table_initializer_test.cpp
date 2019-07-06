
#include "../../util/table.h"
#include "../../util/direct_table_initializer.h"
#include "../test_base/test_base.h"

using namespace util;

TEST (test) {
	Table<6, int, 1, 0> table(
		makeDirectTableInitializer([] (auto i, auto j){ return 2*i + j; }).withBounds<0, 3, 0, 2>()
	);

	assertEquals (0, table(0, 0));
	assertEquals (2, table(1, 0));
	assertEquals (4, table(2, 0));
	assertEquals (1, table(0, 1));
	assertEquals (3, table(1, 1));
	assertEquals (5, table(2, 1));
}

TEST_RUNNER_BEGIN
	RUN_TEST(test);
TEST_RUNNER_END

