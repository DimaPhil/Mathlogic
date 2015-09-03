#ifndef MATHLOGIC_PROOFCHECKER_H
#define MATHLOGIC_PROOFCHECKER_H

#include "expressions/Expressions.h"
#include <map>
#include <algorithm>
#include <vector>

using Expressions::Expression;

template<class Base, class Derived>
bool check_class_inherity(Derived &derived) {
    try {
        dynamic_cast<Base &>(derived);
        return true;
    } catch (const std::bad_cast &) {
        return false;
    }
}

struct PredicateResult {
    Expression *not_free_term;
    Expression *formula;
    std::string variable_name;
    bool error_occurred;
    int index;

    PredicateResult() {
        error_occurred = false;
        index = -1;
    }
};

struct ProofChecker {
    friend class Variable;
    friend class Predicate;

    std::vector<Expression *> expressions;
    std::map<size_t, size_t> expression_hashes;
    static std::vector<Expression *> axioms;
    static std::vector<Expression *> arithmetic_axioms;
    static std::map<std::string, Expression *> variables;

    const int NO_AXIOM = -1;
    enum EXPRESSION_TYPE {
        VARIABLE, UNARY_OPERATION, BINARY_OPERATION,
        QUANTIFIER, ARGUMENTS_HANDLER, ZERO, STROKE, UNKNOWN_TYPE
    };

    ProofChecker();
    void clear();

    static ProofChecker *get_instance() {
        static ProofChecker checker;
        return &checker;
    }

    Expression *parse_expression(const std::string &expression);
    void add_expression(std::vector<Expression *> &expressions, const std::string &expression);
    void add_axioms(std::vector<Expression *> &axioms, std::vector<Expression *> &arithmetic_axioms);
    void add_expression(Expression *expression);

    bool is_axiom(Expression *axiom, Expression *expression);
    int get_axiom(Expression *expression);
    PredicateResult get_predicate_axiom(Expression *expression);
    int get_arithmetic_axiom(Expression *expression);
    std::pair<size_t, size_t> get_modus_ponens(Expression *expression);

    int find_expression(Expression *expression);
    PredicateResult get_predicate_rule(Expression *expression);
};

#endif //MATHLOGIC_PROOFCHECKER_H
