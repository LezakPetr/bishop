
#include "../../bishop/base/color.h"
#include "../test_base/test_base.h"

#include <random>

using namespace bishop::base;


TEST (testIsValid) {
	assertTrue(Color::isValid(Color::WHITE));
	assertTrue(Color::isValid(Color::BLACK));
	assertFalse(Color::isValid(Color::LAST));
	assertFalse(Color::isValid(Color::FIRST - 1));
}

TEST (testGetOppositeColor) {
	assertEquals(Color::BLACK, Color::getOppositeColor(Color::WHITE));
	assertEquals(Color::WHITE, Color::getOppositeColor(Color::BLACK));
}

template<typename T>
void testColorNegate() {
	::std::minstd_rand rng;
	::std::uniform_int_distribution<T> distribution (::std::numeric_limits<T>::min(), ::std::numeric_limits<T>::max());

	for (int i = 0; i < 1000000; i++) {
		const T value = distribution(rng);

		assertEquals(value, Color::colorNegate(Color::WHITE, value));
		assertEquals((T) -value, Color::colorNegate(Color::BLACK, value));
	}
}

TEST (testColorNegate8) {
	testColorNegate<int8_t>();
}

TEST (testColorNegate16) {
	testColorNegate<int16_t>();
}

TEST (testColorNegate32) {
	testColorNegate<int32_t>();
}

TEST (testColorNegate64) {
	testColorNegate<int64_t>();
}

TEST_RUNNER_BEGIN
	RUN_TEST(testIsValid);
	RUN_TEST(testGetOppositeColor);
	RUN_TEST(testColorNegate8);
	RUN_TEST(testColorNegate16);
	RUN_TEST(testColorNegate32);
	RUN_TEST(testColorNegate64);
TEST_RUNNER_END


