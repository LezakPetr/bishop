
#include "../../util/table.h"
#include "../test_base/test_base.h"

#include <random>

using namespace util;


TEST (testAccess) {
	Table<6, int, 1, 0> table(
		[](auto &table) {
		       	for (int i = 0; i < 3; i++) {
			       	for (int j = 0; j < 2; j++)
					table(i, j) = 2*i + j;
			}
		}
	);

	assertEquals (0, table(0, 0));
	assertEquals (2, table(1, 0));
	assertEquals (4, table(2, 0));
	assertEquals (1, table(0, 1));
	assertEquals (3, table(1, 1));
	assertEquals (5, table(2, 1));
}

TEST_RUNNER_BEGIN
	RUN_TEST(testAccess);
TEST_RUNNER_END

