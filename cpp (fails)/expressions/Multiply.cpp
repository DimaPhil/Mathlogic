#include "Expressions.h"

using Expressions::Expression;

Expressions::Multiply::Multiply(Expression *left, Expression *right) : BinaryOperation('*', left, right) {}

bool Expressions::Multiply::calculate(const std::map<std::string, bool> &variables_values) {
    return false;
}

std::vector<std::string> Expressions::Multiply::get_proof(size_t, size_t) {
    return std::vector<std::string>();
}

Expression* Expressions::Multiply::substitute(const std::map<std::string, Expression *> &changes_to_apply) {
    return new Multiply(left->substitute(changes_to_apply), right->substitute(changes_to_apply));
}