#include "Expressions.h"

using Expressions::Expression;

Expressions::ForallQuantifier::ForallQuantifier(const std::string &variable, Expression *next)
        : Quantifier(variable, next, '@') {
}

Expressions::ForallQuantifier::ForallQuantifier(Expression *variable, Expression *next)
        : Quantifier(variable, next, '@') {
}

Expression* Expressions::ForallQuantifier::substitute(const std::map<std::string, Expression *> &changes_to_apply) {
    if (changes_to_apply.find(variable->to_string()) != changes_to_apply.end()) {
        return new ForallQuantifier(variable, next);
    }
    return new ForallQuantifier(variable, next->substitute(changes_to_apply));
}
