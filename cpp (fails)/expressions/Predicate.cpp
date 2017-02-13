#include "Expressions.h"
#include "../ProofChecker.h"

using Expressions::Expression;

Expressions::Predicate::Predicate(const std::string &name, const std::vector<Expression*> &terms)
        : ArgumentsHandler(name, terms) {
}

Expression* Expressions::Predicate::substitute(const std::map<std::string, Expression *> &changes_to_apply) {
    std::vector<Expression*> substituted_terms(terms.size());
    for (size_t i = 0; i < terms.size(); i++) {
        substituted_terms[i] = terms[i]->substitute(changes_to_apply);
    }
    return new Predicate(name, substituted_terms);
}

bool Expressions::Predicate::is_substitute(Expression *expression) {
    if (!terms.empty()) {
        return ArgumentsHandler::is_substitute(expression);
    }
    ProofChecker *checker = ProofChecker::get_instance();
    if (checker->variables.find(name) == checker->variables.end()) {
        checker->variables[name] = expression;
    } else if (checker->variables[name]->hash() != expression->hash()) {
        return false;
    }
    return true;
}