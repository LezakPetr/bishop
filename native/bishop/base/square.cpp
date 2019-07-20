

#include "square.h"
#include "../../util/string_util.h"

#include <stdexcept>


using namespace std;
using namespace util;
using namespace bishop::base;


Square::Type bishop::base::Square::fromString (::std::string const & str) {
	const string trimmed = StringUtil::trim (str);

	if (trimmed.size() != 2)
		throw runtime_error ("Unknown square " + str);

	return Square::onFileRank (File::fromChar (trimmed[0]), Rank::fromChar(trimmed[1]));
}

