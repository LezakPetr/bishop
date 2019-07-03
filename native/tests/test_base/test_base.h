
#ifndef _TESTS_TEST_BASE_H_
#define _TESTS_TEST_BASE_H_


#include <stdexcept>
#include <string>
#include <iostream>
#include <sstream>

template <typename T>
void assertEquals (const T expected, const T given) {
	if (given != expected) {
		::std::stringstream message;
		message << "Assertion error - expected: ";
		message << expected;
		message << ", given: ";
		message << given;
		
		throw ::std::runtime_error (message.str());
	}
}

void assertTrue (const bool value) {
	if (!value) {
		throw ::std::runtime_error ("Assertion error - was false");
	}
}

void assertFalse (const bool value) {
	if (value) {
		throw ::std::runtime_error ("Assertion error - was true");
	}
}

#define TEST(name) void name()

#define TEST_RUNNER_BEGIN int main (int argc, char *argcv[]) {

#define RUN_TEST(name) ::std::cout << "Running test " << #name << " ... " << ::std::flush; name(); ::std::cout << "OK" << ::std::endl;

#define TEST_RUNNER_END return 0; }

#endif

