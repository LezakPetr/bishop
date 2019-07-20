
#include "file.h"

#include <cctype>
#include <stdexcept>

using namespace bishop::base;
using namespace std;

File::Type bishop::base::File::fromChar (const char ch) {
	const char lower = tolower (ch);

	if (lower >= 'a' && lower <= 'h')
		return lower - 'a';

	throw std::runtime_error ((string) "Unknown file " + ch);
}

