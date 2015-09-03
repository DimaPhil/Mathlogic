#include "Expressions.h"
#include "../ProofChecker.h"

using Expressions::Expression;

Expressions::Variable::Variable(const char *name) {
    this->name = std::string(name);
}

Expressions::Variable::Variable(const std::string &name) {
    this->name = name;
}

bool Expressions::Variable::equals(Expression *expression) {
    if (!check_class_inherity<Variable>(*expression)) {
        return false;
    }
    Variable *variable_expression = reinterpret_cast<Variable *>(expression);
    return name == variable_expression->name;
}

std::string Expressions::Variable::simple_to_string() {
    return name;
}

std::string Expressions::Variable::to_string() {
    return name;
}

size_t Expressions::Variable::hash() {
    return std::hash<std::string>()(name);
}

std::vector<std::string> Expressions::Variable::get_variables() {
    return {name};
}

bool Expressions::Variable::calculate(const std::map<std::string, bool> &variables_values) {
    auto it = variables_values.find(name);
    if (it == variables_values.end()) {
        throw std::runtime_error("No variable " + name + " in expression");
    }
    return it->second;
}

void Expressions::Variable::prove_with_values(std::vector<Expression *> &proof,
                                              const std::map<std::string, bool> &variables_values) {
}

Expression* Expressions::Variable::substitute(const std::map<std::string, Expression*> &changes_to_apply) {
    if (changes_to_apply.find(name) == changes_to_apply.end()) {
        throw std::runtime_error("Can't find variable to substitute");
    } else {
        return changes_to_apply.find(name)->second;
    }
}

bool Expressions::Variable::is_substitute(Expression *expression) {
    ProofChecker *checker = ProofChecker::get_instance();
    if (checker->variables.find(name) == checker->variables.end()) {
        checker->variables[name] = expression;
    } else if (checker->variables[name]->hash() != expression->hash()) {
        return false;
    }
    return true;
}

Expressions::SubstitutionState Expressions::Variable::is_free_to_substitute(const std::string &variable_name,
                                                               const std::vector<std::string> &free_variables) {
    return SubstitutionState(name == variable_name);
}