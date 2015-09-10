#include <iostream>

#include "ParserFormalArithmetic.h"

using namespace Expressions;

ParserFormalArithmetic::ParserFormalArithmetic() {}

std::string ParserFormalArithmetic::parse_variable() {
    std::string name = "";
    name += expression[index++];
    while (index < expression.size() && isdigit(expression[index])) {
        name += expression[index++];
    }
    return name;
}

Expression* ParserFormalArithmetic::parse_multiplier() {
    Expression *result = nullptr;
    if (expression[index] == '(') {
        index++;
        result = parse_term();
        index++;
    } else if (expression[index] == '0') {
        result = new Zero();
        index++;
    } else if ('a' <= expression[index] && expression[index] <= 'z') {
        size_t start_index = index++;
        while (isdigit(expression[index])) {
            index++;
        }
        if (expression[index] != '(') {
            result = new Variable(expression.substr(start_index, index - start_index));
        } else {
            size_t finish_index = index;
            std::vector<Expression*> terms;
            if (expression[index] == '(') {
                index++;
                while (expression[index] != ')') {
                    terms.emplace_back(parse_term());
                }
                index++;
            }
            //FunctionMy???
            result = new Predicate(expression.substr(start_index, finish_index - start_index), terms);
        }
    }
    while (index < expression.size() && expression[index] == '\'') {
        index++;
        result = new Stroke(result);
    }
    return result;
}

Expression* ParserFormalArithmetic::parse_summand() {
    Expression *result = parse_multiplier();
    while (index < expression.size() && expression[index] == '*') {
        index++;
        Expression *term = parse_multiplier();
        result = new Multiply(result, term);
    }
    return result;
}

Expression* ParserFormalArithmetic::parse_term() {
    Expression *result = parse_summand();
    while (index < expression.size() && expression[index] == '+') {
        index++;
        Expression *term = parse_summand();
        result = new Add(result, term);
    }
    return result;
}

Expression* ParserFormalArithmetic::parse_predicate() {
    if ('A' <= expression[index] && expression[index] <= 'Z') {
        size_t start_index = index++;
        while (index < expression.size() && isdigit(expression[index])) {
            index++;
        }
        size_t finish_index = index;
        std::vector<Expression*> terms;
        if (expression[index] == '(') {
            while (index < expression.size() && expression[index] != ')') {
                index++;
                Expression *term = parse_term();
                terms.emplace_back(term);
            }
            index++;
        }
        return new Predicate(expression.substr(start_index, finish_index - start_index), terms);
    }
    Expression *left = parse_term();
    index++;
    Expression *right = parse_term();
    return new Equality(left, right);
}

Expression* ParserFormalArithmetic::parse_and() {
    if (index >= expression.size()) {
        throw ParserError("Unexpected end of expression");
    }
    if (expression[index] == '!') {
        index++;
        return new Not(parse_and());
    }
    if (expression[index] == '(') {
        bool is_predicate = true;
        size_t equals_pos = expression.find("=", index);
        if (equals_pos != std::string::npos) {
            size_t balance = 0;
            for (size_t i = index; i < equals_pos; i++) {
                if (expression[i] == '(') {
                    ++balance;
                } else if (expression[i] == ')') {
                    is_predicate &= (--balance) > 0;
                }
            }
            is_predicate &= balance == 0;
        } else {
            is_predicate = false;
        }
        if (is_predicate) {
            return parse_predicate();
        } else {
            index++;
            Expression *result = parse_expression();
            if (index >= expression.size() || expression[index] != ')') {
                throw ParserError("No pair for open parenthesis");
            }
            index++;
            return result;
        }
    }
    if (expression[index] == '@' || expression[index] == '?') {
        char quantifier = expression[index++];
        std::string variable = parse_variable();
        Expression *next = parse_and();
        if (quantifier == '@') {
            return new ForallQuantifier(variable, next);
        } else {
            return new ExistsQuantifier(variable, next);
        }
    }
    return parse_predicate();
    //throw ParserError(std::string("Unexpected symbol in expression: expected '!', '(' or ['A'..'Z'], but found ") + expression[index] + " (index = " + std::to_string(index + 1) + ")");
}

Expression* ParserFormalArithmetic::parse_or() {
    Expression *result = parse_and();
    while (index < expression.size() && expression[index] == '&') {
        index++;
        result = new And(result, parse_and());
    }
    return result;
}

Expression* ParserFormalArithmetic::parse_implication() {
    Expression *result = parse_or();
    while (index < expression.size() && expression[index] == '|') {
        index++;
        result = new Or(result, parse_or());
    }
    return result;
}

Expression* ParserFormalArithmetic::parse_expression() {
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

Expression* ParserFormalArithmetic::parse(const std::string &expression) {
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