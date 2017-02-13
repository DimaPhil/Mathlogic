#include "Expressions.h"

using Expressions::Expression;

Expressions::ExistsQuantifier::ExistsQuantifier(const std::string &variable, Expression *next)
        : Quantifier(variable, next, '?') {
}

Expressions::ExistsQuantifier::ExistsQuantifier(Expression *variable, Expression *next)
        : Quantifier(variable, next, '?') {
}

Expression* Expressions::ExistsQuantifier::substitute(const std::map<std::string, Expression *> &changes_to_apply) {
    if (changes_to_apply.find(variable->to_string()) != changes_to_apply.end()) {
        return new ExistsQuantifier(variable, next);
    }
    return new ExistsQuantifier(variable, next->substitute(changes_to_apply));
}