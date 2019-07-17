

#include "rank.h"

#include <stdexcept>


using namespace bishop::base;
using namespace std;


Rank::Type bishop::base::Rank::fromChar (const char ch) {
	if (ch >= '1' && ch <= '8')
		return ch - '1';

	throw std::runtime_error ((string) "Unknown rank " + ch);
}

