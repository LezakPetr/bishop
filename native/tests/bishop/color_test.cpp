
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

template<typename T, T firstValue, T secondValue, int counter>
struct ColorFunctionTester {
	static void testColorFunction() {
		assertEquals(firstValue, Color::colorFunction<T, firstValue, secondValue> (Color::WHITE));
		assertEquals(secondValue, Color::colorFunction<T, firstValue, secondValue> (Color::BLACK));

		assertEquals(secondValue, Color::colorFunction<T, secondValue, firstValue> (Color::WHITE));
		assertEquals(firstValue, Color::colorFunction<T, secondValue, firstValue> (Color::BLACK));
	
		ColorFunctionTester<T, firstValue / 2 - 25, secondValue / 2 + 7, counter - 1>::testColorFunction();
	}
};

template<typename T, T firstValue, T secondValue>
struct ColorFunctionTester<T, firstValue, secondValue, 0> {
	static void testColorFunction() {
	}
};


TEST (testColorFunction8) {
	ColorFunctionTester<int8_t, INT8_MIN, INT8_MAX, 64>::testColorFunction();
}

TEST (testColorFunction16) {
	ColorFunctionTester<int16_t, INT16_MIN, INT16_MAX, 64>::testColorFunction();
}

TEST (testColorFunction32) {
	ColorFunctionTester<int32_t, INT32_MIN, INT32_MAX, 64>::testColorFunction();
}

TEST (testColorFunction64) {
	ColorFunctionTester<int64_t, INT64_MIN, INT64_MAX, 64>::testColorFunction();
}

TEST_RUNNER_BEGIN
	RUN_TEST(testIsValid);
	RUN_TEST(testGetOppositeColor);
	RUN_TEST(testColorNegate8);
	RUN_TEST(testColorNegate16);
	RUN_TEST(testColorNegate32);
	RUN_TEST(testColorNegate64);
	RUN_TEST(testColorFunction8);
	RUN_TEST(testColorFunction16);
	RUN_TEST(testColorFunction32);
	RUN_TEST(testColorFunction64);
TEST_RUNNER_END


