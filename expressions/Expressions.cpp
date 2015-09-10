#include <stdexcept>
#include <functional>
#include <algorithm>
#include <map>
#include <iostream>
#include <assert.h>

#include "Expressions.h"
#include "Parser.h"
#include "ParserFormalArithmetic.h"

using Expressions::Expression;
using Expressions::UnaryOperation;
using Expressions::BinaryOperation;

using Expressions::SubstitutionState;

template<class Base, class Derived>
bool check_class_inherity(Derived &derived) {
    try {
        dynamic_cast<Base &>(derived);
        return true;
    } catch (const std::bad_cast &) {
        return false;
    }
}

Expression* parse_expression(const std::string &const_expression) {
    std::string expression = const_expression;
    expression = Utils::replace(expression, ' ');
    expression = Utils::replace(expression, '\t');
    expression = Utils::replace(expression, "->", ">");

    ParserFormalArithmetic parser;
    try {
        return parser.parse(expression);
    } catch (const ParserFormalArithmetic::ParserError &e) {
        std::cerr << "Error in parsing expression\nExpression: " << expression << "\nError: " << e.what() << '\n';
        return nullptr;
    }
}

/*----------------------------------------------------------------------------------------------------------*/

Expressions::UnaryOperation::UnaryOperation() { }

Expressions::UnaryOperation::UnaryOperation(const char operation, Expression *operand) {
    this->operation = operation;
    this->operand = operand;
}

bool Expressions::UnaryOperation::equals(Expression *expression) {
    if (!check_class_inherity<UnaryOperation>(*expression)) {
        return false;
    }
    UnaryOperation *unary_expression = reinterpret_cast<UnaryOperation *>(expression);
    return operation == unary_expression->operation && operand->equals(unary_expression->operand);
}

std::string Expressions::UnaryOperation::simple_to_string() {
    return std::string(1, operation) + operand->simple_to_string();
}

std::string Expressions::UnaryOperation::to_string() {
    std::string result = std::string(1, operation);
    if (!check_class_inherity<Variable>(*operand)) {
        result += "(" + operand->to_string() + ")";
    } else {
        result += operand->to_string();
    }
    return result;
}

size_t UnaryOperation::hash() {
    return std::hash<std::string>()(simple_to_string());
}

std::vector<std::string> Expressions::UnaryOperation::get_variables() {
    return operand->get_variables();
}

Expressions::SubstitutionState UnaryOperation::is_free_to_substitute(const std::string &variable_name,
                                                const std::vector<std::string> &free_variables) {
    return operand->is_free_to_substitute(variable_name, free_variables);
}

/*----------------------------------------------------------------------------------------------------------*/

Expressions::BinaryOperation::BinaryOperation() { }

Expressions::BinaryOperation::BinaryOperation(const char operation, Expression *left, Expression *right) {
    this->operation = operation;
    this->left = left;
    this->right = right;
}

bool Expressions::BinaryOperation::equals(Expression *expression) {
    if (!check_class_inherity<BinaryOperation>(*expression)) {
        return false;
    }
    BinaryOperation *binary_expression = reinterpret_cast<BinaryOperation *>(expression);
    return operation == binary_expression->operation &&
           left->equals(binary_expression->left) &&
           right->equals(binary_expression->right);
}

std::string Expressions::BinaryOperation::simple_to_string() {
    return left->simple_to_string() + std::string(1, operation) + right->simple_to_string();
}

std::string Expressions::BinaryOperation::to_string() {
    std::string left_expression, right_expression;
    if (check_class_inherity<BinaryOperation>(*left)) {
        left_expression = "(" + left->to_string() + ")";
    } else {
        left_expression = left->to_string();
    }
    if (check_class_inherity<BinaryOperation>(*right)) {
        right_expression = "(" + right->to_string() + ")";
    } else {
        right_expression = right->to_string();
    }
    return left_expression + std::string(1, operation) + right_expression;
}

size_t BinaryOperation::hash() {
    return std::hash<std::string>()(simple_to_string());
}

std::vector<std::string> BinaryOperation::get_variables() {
    std::vector<std::string> result_left = left->get_variables();
    std::vector<std::string> result_right = right->get_variables();
    result_left.insert(result_left.end(), result_right.begin(), result_right.end());
    std::sort(result_left.begin(), result_left.end());
    result_left.erase(std::unique(result_left.begin(), result_left.end()), result_left.end());
    return result_left;
}

void BinaryOperation::prove_with_values(std::vector<Expression *> &proof,
                                        const std::map<std::string, bool> &variables_values) {
    left->prove_with_values(proof, variables_values);
    right->prove_with_values(proof, variables_values);
    size_t type_left = left->calculate(variables_values) ? 1 : 0;
    size_t type_right = right->calculate(variables_values) ? 1 : 0;
    std::vector<std::string> new_proof = this->get_proof(type_left, type_right);
    std::map<std::string, Expression *> changes_to_apply;
    changes_to_apply["A"] = left;
    changes_to_apply["B"] = right;
    for (size_t i = 0; i < new_proof.size(); i++) {
        Expression *parsed_proof = parse_expression(new_proof[i]);
        proof.emplace_back(parsed_proof->substitute(changes_to_apply));
    }
}

bool BinaryOperation::is_substitute(Expression *expression) {
    if (!check_class_inherity<BinaryOperation>(*expression)) {
        return false;
    }
    BinaryOperation *binary_expression = reinterpret_cast<BinaryOperation*>(expression);
    return left->is_substitute(binary_expression->left) && right->is_substitute(binary_expression->right);
}

Expressions::SubstitutionState BinaryOperation::is_free_to_substitute(const std::string &variable_name,
                                                                      const std::vector<std::string> &free_variables) {
    SubstitutionState result(left->is_free_to_substitute(variable_name, free_variables));
    result = right->is_free_to_substitute(variable_name, free_variables);
    return result;
}

/*----------------------------------------------------------------------------------------------------------*/

Expressions::Quantifier::Quantifier() {
}


Expressions::Quantifier::Quantifier(const std::string &variable, Expression *next, char operation)
        : variable(new Variable(variable))
        , next(next)
        , operation(operation) {
}

Expressions::Quantifier::Quantifier(Expression *variable, Expression *next, char operation)
        : variable(variable)
        , next(next)
        , operation(operation) {
}

bool Expressions::Quantifier::equals(Expression *expression) {
    if (!check_class_inherity<Quantifier>(*expression)) {
        return false;
    }
    Quantifier *quantifier_expression = reinterpret_cast<Quantifier*>(expression);
    return quantifier_expression->operation == operation &&
           quantifier_expression->variable == variable &&
           next->equals(quantifier_expression->next);
}

bool Expressions::Quantifier::is_substitute(Expression *expression) {
    return check_class_inherity<Quantifier>(*expression) &&
           next->is_substitute(reinterpret_cast<Quantifier*>(expression)->next);
}

std::string Expressions::Quantifier::to_string() {
    return std::string(1, operation) + variable->to_string() + "(" + next->to_string() + ")";
}

std::vector<std::string> Expressions::Quantifier::get_variables() {
    std::vector<std::string> variables = next->get_variables();
    auto it = std::find(variables.begin(), variables.end(), variable->to_string());
    if (it != variables.end()) {
        variables.erase(it);
    }
    return variables;
}

size_t Expressions::Quantifier::hash() {
    return std::hash<std::string>()(to_string());
}

void Expressions::Quantifier::prove_with_values(std::vector<Expression *> &proof,
                                                const std::map<std::string, bool> &variables_values) {
}

bool Expressions::Quantifier::calculate(const std::map<std::string, bool> &variables_values) {
    return false;
}

SubstitutionState Expressions::Quantifier::is_free_to_substitute(const std::string &variable_name,
                                                                 const std::vector<std::string> &free_variables) {
    if (variable_name == variable->to_string()) {
        return SubstitutionState();
    }
    SubstitutionState result(next->is_free_to_substitute(variable_name, free_variables));
    if (result.was_substituted &&
        std::find(free_variables.begin(), free_variables.end(), variable->to_string()) != free_variables.end()) {
        result.successuful = false;
    }
    return result;
}

/*----------------------------------------------------------------------------------------------------------*/

Expressions::ArgumentsHandler::ArgumentsHandler() {
}

Expressions::ArgumentsHandler::ArgumentsHandler(const std::string &name, const std::vector<Expression *> &terms) {
    this->name = name;
    this->terms = terms;
}

std::string Expressions::ArgumentsHandler::to_string() {
    std::string result = name;
    if (!terms.empty()) {
        result += '(';
        result += terms[0]->to_string();
        for (size_t i = 1; i < terms.size(); i++) {
            result += "," + terms[i]->to_string();
        }
        result += ')';
    }
    return result;
}

bool Expressions::ArgumentsHandler::equals(Expression *expression) {
    if (!check_class_inherity<ArgumentsHandler>(*expression)) {
        return false;
    }
    ArgumentsHandler *arguments_expression = reinterpret_cast<ArgumentsHandler*>(expression);
    bool result = arguments_expression->name == name && arguments_expression->terms.size() == terms.size();
    for (size_t i = 0; i < terms.size(); i++) {
        result &= terms[i]->equals(arguments_expression->terms[i]);
    }
    return result;
}

size_t Expressions::ArgumentsHandler::hash() {
    return std::hash<std::string>()(to_string());
}

std::vector<std::string> Expressions::ArgumentsHandler::get_variables() {
    std::vector<std::string> result;
    for (size_t i = 0; i < terms.size(); i++) {
        std::vector<std::string> new_variables = terms[i]->get_variables();
        result.insert(result.end(), new_variables.begin(), new_variables.end());
    }
    return result;
}

void Expressions::ArgumentsHandler::prove_with_values(std::vector<Expression *> &proof,
                                                      const std::map<std::string, bool> &variables_values) {
}

bool Expressions::ArgumentsHandler::calculate(const std::map<std::string, bool> &variables_values) {
    return false;
}

bool Expressions::ArgumentsHandler::is_substitute(Expression *expression) {
    if (!check_class_inherity<ArgumentsHandler>(*expression)) {
        return false;
    }
    ArgumentsHandler *arguments_expression = reinterpret_cast<ArgumentsHandler*>(expression);
    bool ok = (name == arguments_expression->name && terms.size() == arguments_expression->terms.size());
    for (size_t i = 0; i < terms.size(); i++) {
        ok &= terms[i]->is_substitute(arguments_expression->terms[i]);
    }
    return ok;
}

SubstitutionState Expressions::ArgumentsHandler::is_free_to_substitute(const std::string &variable_name,
                                                                       const std::vector<std::string> &free_variables) {
    SubstitutionState result;
    for (auto term : terms) {
        result = term->is_free_to_substitute(variable_name, free_variables);
    }
    return result;
}

/*----------------------------------------------------------------------------------------------------------*/

Expressions::Zero::Zero() {
    name = "0";
}

std::string Expressions::Zero::to_string() {
    return name;
}

bool Expressions::Zero::equals(Expression *expression) {
    return check_class_inherity<Zero>(*expression);
}

bool Expressions::Zero::is_substitute(Expression *expression) {
    return equals(expression);
}

size_t Expressions::Zero::hash() {
    return std::hash<std::string>()(to_string());
}

std::vector<std::string> Expressions::Zero::get_variables() {
    return std::vector<std::string>();
}

Expression *Expressions::Zero::substitute(const std::map<std::string, Expression *> &changes_to_apply) {
    return new Zero();
}

void Expressions::Zero::prove_with_values(std::vector<Expression *> &proof,
                                          const std::map<std::string, bool> &variables_values) {
}

bool Expressions::Zero::calculate(const std::map<std::string, bool> &variables_values) {
    return false;
}

SubstitutionState Expressions::Zero::is_free_to_substitute(const std::string &variable_name,
                                                           const std::vector<std::string> &free_variables) {
    return SubstitutionState();
}

/*----------------------------------------------------------------------------------------------------------*/

Expressions::Stroke::Stroke(Expression *operand) {
    operation = "\'";
    this->operand = operand;
}


std::string Expressions::Stroke::to_string() {
    return operand->to_string() + "\'";
}

bool Expressions::Stroke::equals(Expression *expression) {
    if (!check_class_inherity<Stroke>(*expression)) {
        return false;
    }
    return operand->equals(reinterpret_cast<Stroke*>(expression)->operand);
}

bool Expressions::Stroke::is_substitute(Expression *expression) {
    return check_class_inherity<Stroke>(*expression) &&
           operand->is_substitute(reinterpret_cast<Stroke*>(expression)->operand);
}

size_t Expressions::Stroke::hash() {
    return std::hash<std::string>()(to_string());
}

std::vector<std::string> Expressions::Stroke::get_variables() {
    return operand->get_variables();
}

Expression *Expressions::Stroke::substitute(const std::map<std::string, Expression *> &changes_to_apply) {
    return new Stroke(operand->substitute(changes_to_apply));
}

void Expressions::Stroke::prove_with_values(std::vector<Expression *> &proof,
                                            const std::map<std::string, bool> &variables_values) {
}

bool Expressions::Stroke::calculate(const std::map<std::string, bool> &variables_values) {
    return operand->calculate(variables_values);
}

Expressions::SubstitutionState Expressions::Stroke::is_free_to_substitute(const std::string &variable_name,
                                                     const std::vector<std::string> &free_variables) {
    return operand->is_free_to_substitute(variable_name, free_variables);
}

/*----------------------------------------------------------------------------------------------------------*/