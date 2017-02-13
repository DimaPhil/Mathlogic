#include "Utils.h"

bool Utils::is_whitespace(char symbol) {
    return symbol == ' ' || symbol == '\t';
}

std::string Utils::replace(const std::string &s, char oldc, char newc) {
    std::string new_string = "";
    for (size_t i = 0; i < s.size(); i++) {
        if (s[i] == oldc) {
            if (newc != 0) {
                new_string += newc;
            }
        } else {
            new_string += s[i];
        }
    }
    return new_string;
}

std::string Utils::replace(const std::string &s, const std::string &olds, const std::string &news) {
    std::string new_string = "";
    for (size_t i = 0; i < s.size(); i++) {
        if (i + olds.size() <= s.size() && s.substr(i, olds.size()) == olds) {
            new_string += news;
            i += olds.size() - 1;
        } else {
            new_string += s[i];
        }
    }
    return new_string;
}

std::vector<std::string> Utils::split(const std::string &s, char delimeter) {
    std::vector<std::string> result;
    std::string current = "";
    for (size_t position = 0; position < s.size(); position++) {
        if (s[position] == delimeter) {
            result.push_back(current);
            current = "";
        } else {
            current += s[position];
        }
    }
    if (current.size() > 0) {
        result.push_back(current);
    }
    return result;
}

std::vector<std::string> Utils::split(const std::string &s, const std::string &delimeter) {
    std::vector<std::string> result;
    std::string current = "";
    for (size_t position = 0; position < s.size(); position++) {
        if (position + delimeter.size() <= s.size() && s.substr(position, delimeter.size()) == delimeter) {
            result.push_back(current);
            current = "";
            position += delimeter.size() - 1;
        } else {
            current += s[position];
        }
    }
    if (current.size() > 0) {
        result.push_back(current);
    }
    return result;
}
