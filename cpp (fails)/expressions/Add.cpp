#include "Expressions.h"

using Expressions::Expression;

Expressions::Add::Add(Expression *left, Expression *right) : BinaryOperation('+', left, right) {}

bool Expressions::Add::calculate(const std::map<std::string, bool> &variables_values) {
    return false;
}

std::vector<std::string> Expressions::Add::get_proof(size_t, size_t) {
    return std::vector<std::string>();
}

Expression* Expressions::Add::substitute(const std::map<std::string, Expression *> &changes_to_apply) {
    return new Add(left->substitute(changes_to_apply), right->substitute(changes_to_apply));
}