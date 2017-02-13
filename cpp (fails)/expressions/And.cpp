#include "Expressions.h"

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

Expressions::And::And(Expression *left, Expression *right) : BinaryOperation('&', left, right) {}

bool Expressions::And::calculate(const std::map<std::string, bool> &variables_values) {
    return left->calculate(variables_values) & right->calculate(variables_values);
}

Expression* Expressions::And::substitute(const std::map<std::string, Expression *> &changes_to_apply) {
    return new And(left->substitute(changes_to_apply), right->substitute(changes_to_apply));
}

bool Expressions::And::is_substitute(Expression *expression) {
    if (!check_class_inherity<And>(*expression)) {
        return false;
    }
    And *and_expression = reinterpret_cast<And*>(expression);
    return left->is_substitute(and_expression->left) && right->is_substitute(and_expression->right);
}

std::vector<std::string> Expressions::And::get_proof(size_t type_left, size_t type_right) {
    static const std::vector<std::string> proof_false_false = {
            "!A",                                                                       // (1) предположение
            "(A&B->A)->(A&B->!A)->!(A&B)",                                              // (2) Сх. акс. 9
            "A&B->A",                                                                   // (3) Сх. акс. 4
            "(A&B->!A)->!(A&B)",                                                        // (4) M.P. 3, 2
            "!A->A&B ->!A",                                                             // (5) Сх. акс. 1
            "A&B->!A",                                                                  // (6) M.P. 1, 5
            "!(A&B)"                                                                    // (7) M.P. 6, 4
    };

    static const std::vector<std::string> proof_false_true = {
            "!A",                                                                       // (1) предположение
            "(A&B->A)->(A&B->!A)->!(A&B)",                                              // (2) Сх. акс. 9
            "A&B->A",                                                                   // (3) Сх. акс. 4
            "(A&B->!A)->!(A&B)",                                                        // (4) M.P. 3, 2
            "!A->A&B->!A",                                                              // (5) Сх. акс. 1
            "A&B->!A",                                                                  // (6) M.P. 1, 5
            "!(A&B)"                                                                    // (7) M.P. 6, 4
    };

    static const std::vector<std::string> proof_true_false = {
            "!B",                                                                       // (1) предположение
            "(A&B->B)->(A&B->!B)->!(A&B)",                                              // (2) Сх. акс. 9
            "A&B->B",                                                                   // (3) Сх. акс. 5
            "(A&B->!B)->!(A&B)",                                                        // (4) M.P. 3, 2
            "!B->A&B->!B",                                                              // (5) Сх. акс. 1
            "A&B->!B",                                                                  // (6) M.P. 1, 5
            "!(A&B)"                                                                    // (7) M.P. 6, 4
    };

    static const std::vector<std::string> proof_true_true = {
            "A",                                                                        // (1) предположение
            "B",                                                                        // (2) предположение
            "A->B->A&B",                                                                // (3) Сх. акс. 3
            "B->A&B",                                                                   // (4) M.P. 1, 3
            "A&B"                                                                       // (5) M.P. 2, 4
    };

    static const std::vector<std::vector<std::vector<std::string>>> proofs = {
            {proof_false_false, proof_false_true},
            {proof_true_false,  proof_true_true}
    };

    return proofs[type_left][type_right];
}