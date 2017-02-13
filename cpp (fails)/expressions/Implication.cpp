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

Expressions::Implication::Implication(Expression *left, Expression *right) : BinaryOperation('>', left, right) {}

bool Expressions::Implication::calculate(const std::map<std::string, bool> &variables_values) {
    return !left->calculate(variables_values) | right->calculate(variables_values);
}

Expression* Expressions::Implication::substitute(const std::map<std::string, Expression *> &changes_to_apply) {
    return new Implication(left->substitute(changes_to_apply), right->substitute(changes_to_apply));
}

bool Expressions::Implication::is_substitute(Expression *expression) {
    if (!check_class_inherity<Implication>(*expression)) {
        return false;
    }
    Implication *implication_expression = reinterpret_cast<Implication*>(expression);
    return left->is_substitute(implication_expression->left) && right->is_substitute(implication_expression->right);
}

std::vector<std::string> Expressions::Implication::get_proof(size_t type_left, size_t type_right) {
    static const std::vector<std::string> proof_false_false = {
            "!A",                                                                       // (1) предположение
            "!A->A->!A",                                                                // (2) Сх. акс. 1
            "A->!A",                                                                    // (3) M.P. 1, 2
            "!B",                                                                       // (4) предположение
            "!B->A->!B",                                                                // (5) Сх. акс. 1
            "A->!B",                                                                    // (6) M.P. 4, 5
            "A->A->A",                                                                  // (7) Сх. акс. 1
            "(A->A->A)->(A->(A->A)->A)->A->A",                                          // (8) Сх. акс. 2
            "(A->(A->A)->A)->A->A",                                                     // (9) M.P. 7, 8
            "(A->(A->A)->A)",                                                           // (10) Сх. акс. 1
            "A->A",                                                                     // (11) M.P. 10, 9
            "(A->!B->A)",                                                               // (12) Сх. акс. 1
            "(A->!B->A)->A->(A->!B->A)",                                                // (13) Сх. акс. 1
            "A->(A->!B->A)",                                                            // (14) M.P. 12, 13
            "(A->A)->(A->A->(!B->A))->A->(!B->A)",                                      // (15) Сх. акс. 2
            "(A->A->(!B->A))->A->(!B->A)",                                              // (16) M.P. 11, 15
            "A->(!B->A)",                                                               // (17) M.P. 14, 16
            "(!A->!B->!A)",                                                             // (18) Сх. акс. 1
            "(!A->!B->!A)->A->(!A->!B->!A)",                                            // (19) Сх. акс. 1
            "A->(!A->!B->!A)",                                                          // (20) M.P. 18, 19
            "(A->!A)->(A->!A->(!B->!A))->A->(!B->!A)",                                  // (21) Сх. акс. 2
            "(A->!A->(!B->!A))->A->(!B->!A)",                                           // (22) M.P. 3, 21
            "A->(!B->!A)",                                                              // (23) M.P. 20, 22
            "((!B->A)->(!B->!A)->!!B)",                                                 // (24) Сх. акс. 9
            "((!B->A)->(!B->!A)->!!B)->A->((!B->A)->(!B->!A)->!!B)",                    // (25) Сх. акс. 1
            "A->((!B->A)->(!B->!A)->!!B)",                                              // (26) M.P. 24, 25
            "(A->(!B->A))->(A->(!B->A)->((!B->!A)->!!B))->A->((!B->!A)->!!B)",          // (27) Сх. акс. 2
            "(A->(!B->A)->((!B->!A)->!!B))->A->((!B->!A)->!!B)",                        // (28) M.P. 17, 27
            "A->((!B->!A)->!!B)",                                                       // (29) M.P. 26, 28
            "(A->(!B->!A))->(A->(!B->!A)->!!B)->A->!!B",                                // (30) Сх. акс. 2
            "(A->(!B->!A)->!!B)->A->!!B",                                               // (31) M.P. 23, 30
            "A->!!B",                                                                   // (32) M.P. 29, 31
            "(!!B->B)",                                                                 // (33) Сх. акс. 10
            "(!!B->B)->A->(!!B->B)",                                                    // (34) Сх. акс. 1
            "A->(!!B->B)",                                                              // (35) M.P. 33, 34
            "(A->!!B)->(A->!!B->B)->A->B",                                              // (36) Сх. акс. 2
            "(A->!!B->B)->A->B",                                                        // (37) M.P. 32, 36
            "A->B"                                                                      // (38) M.P. 35, 37, well done!
    };

    static const std::vector<std::string> proof_false_true = {
            "B",                                                                        // (1) предположение
            "B->A->B",                                                                  // (2) Сх. акс. 1
            "A->B"                                                                      // (3) M.P. 1, 2
    };

    static const std::vector<std::string> proof_true_false = {
            "A",                                                                        // (1) предположение
            "!B",                                                                       // (2) предположение
            "((A->B)->A)->((A->B)->!A)->!(A->B)",                                       // (3) Сх. акс. 9
            "A->(A->B)->A",                                                             // (4) Сх. акс. 1
            "(A->B)->A",                                                                // (5) M.P. 1, 4
            "((A->B)->!A)->!(A->B)",                                                    // (6) M.P. 5, 3
            "!B->(A->B)->!B",                                                           // (7) Сх. акс. 1
            "(A->B)->!B",                                                               // (8) M.P. 2, 7
            "(A->B)->(A->B)->(A->B)",                                                   // (9) Сх. акс. 1
            "((A->B)->(A->B)->(A->B))->((A->B)->((A->B)->(A->B))->(A->B))->(A->B)->(A->B)", // (10) Сх. акс. 2
            "((A->B)->((A->B)->(A->B))->(A->B))->(A->B)->(A->B)",                       // (11) M.P. 9, 10
            "((A->B)->((A->B)->(A->B))->(A->B))",                                       // (12) Сх. акс. 1
            "(A->B)->(A->B)",                                                           // (13) M.P. 12, 11
            "((A->B)->(A->!B)->!A)",                                                    // (14) Сх. акс. 9
            "((A->B)->(A->!B)->!A)->(A->B)->((A->B)->(A->!B)->!A)",                     // (15) Сх. акс. 1
            "(A->B)->((A->B)->(A->!B)->!A)",                                            // (16) M.P. 14, 15
            "((A->B)->(A->B))->((A->B)->(A->B)->((A->!B)->!A))->(A->B)->((A->!B)->!A)", // (17) Сх. акс. 2
            "((A->B)->(A->B)->((A->!B)->!A))->(A->B)->((A->!B)->!A)",                   // (18) M.P. 13, 17
            "(A->B)->((A->!B)->!A)",                                                    // (19) M.P. 16, 18
            "(!B->A->!B)",                                                              // (20) Сх. акс. 1
            "(!B->A->!B)->(A->B)->(!B->A->!B)",                                         // (21) Сх. акс. 1
            "(A->B)->(!B->A->!B)",                                                      // (22) M.P. 20, 21
            "((A->B)->!B)->((A->B)->!B->(A->!B))->(A->B)->(A->!B)",                     // (23) Сх. акс. 2
            "((A->B)->!B->(A->!B))->(A->B)->(A->!B)",                                   // (24) M.P. 8, 23
            "(A->B)->(A->!B)",                                                          // (25) M.P. 22, 24
            "((A->B)->(A->!B))->((A->B)->(A->!B)->!A)->(A->B)->!A",                     // (26) Сх. акс. 2
            "((A->B)->(A->!B)->!A)->(A->B)->!A",                                        // (27) M.P. 25, 26
            "(A->B)->!A",                                                               // (28) M.P. 19, 27
            "!(A->B)"                                                                   // (29) M.P. 28, 6, well done!
    };

    static const std::vector<std::string> proof_true_true = {
            "B",                                                                        // (1) предположение
            "B->A->B",                                                                  // (2) Сх. акс. 1
            "A->B"                                                                      // (3) M.P. 1, 2
    };

    static const std::vector<std::vector<std::vector<std::string>>> proofs = {
            {proof_false_false, proof_false_true},
            {proof_true_false,  proof_true_true}
    };

    return proofs[type_left][type_right];
}