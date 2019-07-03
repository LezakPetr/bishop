
#include "../../bishop/base/file.h"
#include "../test_base/test_base.h"

using namespace bishop::base;


TEST (getOppositeFileTest) {
	assertEquals(File::FH, File::getOppositeFile(File::FA));
	assertEquals(File::FG, File::getOppositeFile(File::FB));
	assertEquals(File::FF, File::getOppositeFile(File::FC));
	assertEquals(File::FE, File::getOppositeFile(File::FD));
	assertEquals(File::FD, File::getOppositeFile(File::FE));
	assertEquals(File::FC, File::getOppositeFile(File::FF));
	assertEquals(File::FB, File::getOppositeFile(File::FG));
	assertEquals(File::FA, File::getOppositeFile(File::FH));
}

TEST_RUNNER_BEGIN
	RUN_TEST(getOppositeFileTest);
TEST_RUNNER_END

