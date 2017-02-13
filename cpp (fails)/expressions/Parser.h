#ifndef CLIONPROJECTS_PARSER_H
#define CLIONPROJECTS_PARSER_H

#include "../Utils.h"
#include "Expressions.h"

#include <string>
#include <cstring>

using Expressions::Expression;

class Parser {
private:
    std::string expression;
    size_t index;

    std::string parse_variable();

    Expression* parse_and();
    Expression* parse_or();
    Expression* parse_implication();
    Expression* parse_expression();
public:
    class ParserError {
    private:
        const char *message;
    public:
        ParserError(const char *message) {
            this->message = strdup(message);
        }

        ParserError(const std::string &message) : ParserError(message.c_str()) {
        }

        std::string what() const {
            return std::string(message);
        }
    };

    Parser();
    Expression* parse(const std::string &expression);
};

#endif //CLIONPROJECTS_PARSER_H
