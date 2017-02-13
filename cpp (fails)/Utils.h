#ifndef CLIONPROJECTS_UTILS_H
#define CLIONPROJECTS_UTILS_H

#include <typeinfo>
#include <string>
#include <vector>

namespace Utils {
    bool is_whitespace(char symbol);
    std::string replace(const std::string &s, char oldc, char newc = 0);
    std::string replace(const std::string &s, const std::string &olds, const std::string &news = "");

    std::vector<std::string> split(const std::string &s, char delimeter);
    std::vector<std::string> split(const std::string &s, const std::string &delimeter);
}

#endif //CLIONPROJECTS_UTILS_H
