#include "Expressions.h"

using Expressions::Expression;

Expressions::Equality::Equality(Expression *left, Expression *right) : BinaryOperation('=', left, right) {}

bool Expressions::Equality::calculate(const std::map<std::string, bool> &variables_values) {
    return false;
}

std::vector<std::string> Expressions::Equality::get_proof(size_t, size_t) {
    return std::vector<std::string>();
}

Expression* Expressions::Equality::substitute(const std::map<std::string, Expression *> &changes_to_apply) {
    return new Equality(left->substitute(changes_to_apply), right->substitute(changes_to_apply));
}