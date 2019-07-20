

#include "string_util.h"


::std::string util::StringUtil::trim (::std::string const & str) {
	size_t begin = 0;

	while (begin < str.size() && isspace (str[begin]))
		begin++;
	
	size_t end = str.size();

	while (end > 0 && isspace (str[end - 1]))
		end--;

	if (end > begin)
		return str.substr (begin, end - begin);
	else
		return ::std::string();
}

