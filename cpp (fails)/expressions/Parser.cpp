#include <iostream>

#include "Parser.h"

using namespace Expressions;

Parser::Parser() {}

std::string Parser::parse_variable() {
    size_t start_pos = index++;
    while (index < expression.size() && isdigit(expression[index])) {
        index++;
    }
    return expression.substr(start_pos, index - start_pos);
}

Expression* Parser::parse_and() {
    if (index >= expression.size()) {
        throw ParserError("Unexpected end of expression");
    }
    if (expression[index] == '!') {
        index++;
        return new Not(parse_and());
    }
    if ('A' <= expression[index] && expression[index] <= 'Z') {
        return new Variable(parse_variable());
    }
    if (expression[index] == '(') {
        index++;
        Expression *result = parse_expression();
        if (index >= expression.size() || expression[index] != ')') {
            throw ParserError("No pair for open parenthesis");
        }
        index++;
        return result;
    }
    throw ParserError(std::string("Unexpected symbol in expression: expected '!', '(' or ['A'..'Z'], but found ") + expression[index] + " (index = " + std::to_string(index + 1) + ")");
}

Expression* Parser::parse_or() {
    Expression *result = parse_and();
    while (index < expression.size() && expression[index] == '&') {
        index++;
        result = new And(result, parse_and());
    }
    return result;
}

Expression* Parser::parse_implication() {
    Expression *result = parse_or();
    while (index < expression.size() && expression[index] == '|') {
        index++;
        result = new Or(result, parse_or());
    }
    return result;
}

Expression* Parser::parse_expression() {
    std::vector<Expression*> results = {parse_implication()};
    while (index < expression.size() - 1 && expression[index] == '>') {
        index++;
        results.push_back(parse_implication());
    }
    Expression *result = results.back();
    for (int i = (int)results.size() - 2; i >= 0; i--) {
        result = new Implication(results[i], result);
    }
    return result;
}

Expression* Parser::parse(const std::string &expression) {
    std::string expression_no_spaces = "";
    for (size_t i = 0; i < expression.size(); i++) {
        if (!Utils::is_whitespace(expression[i])) {
            expression_no_spaces += expression[i];
        }
    }
    this->expression = expression_no_spaces;
    this->index = 0;
    Expression* result = parse_expression();
    if (index != expression_no_spaces.size()) {
        throw ParserError("Extra information at the end of expression");
    }
    return result;
}