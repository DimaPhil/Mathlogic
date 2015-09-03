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

Expressions::Or::Or(Expression *left, Expression *right) : BinaryOperation('|', left, right) {}

bool Expressions::Or::calculate(const std::map<std::string, bool> &variables_values) {
    return left->calculate(variables_values) | right->calculate(variables_values);
}

Expression* Expressions::Or::substitute(const std::map<std::string, Expression *> &changes_to_apply) {
    return new Or(left->substitute(changes_to_apply), right->substitute(changes_to_apply));
}

bool Expressions::Or::is_substitute(Expression *expression) {
    if (!check_class_inherity<Or>(*expression)) {
        return false;
    }
    Or *or_expression = reinterpret_cast<Or*>(expression);
    return left->is_substitute(or_expression->left) && right->is_substitute(or_expression->right);
}

std::vector<std::string> Expressions::Or::get_proof(size_t type_left, size_t type_right) {
    static const std::vector<std::string> proof_false_false = {
            "!A",                                                                       // (1) предположение
            "!B",                                                                       // (2) предположение
            "(A|B->A)->(A|B->!A)->!(A|B)",                                              // (3) Сх. акс. 9
            "(A->A)->(B->A)->(A|B->A)",                                                 // (4) Сх. акс. 8
            "A->A->A",                                                                  // (5) Сх. акс. 1
            "(A->A->A)->(A->(A->A)->A)->(A->A)",                                        // (6) Сх. акс. 2
            "(A->(A->A)->A)->(A->A)",                                                   // (7) M.P. 5, 6
            "A->(A->A)->A",                                                             // (8) Сх. акс. 1
            "A->A",                                                                     // (9) M.P. 8, 7
            "(B->A)->(A|B->A)",                                                         // (10) M.P. 9, 4
            "!B->B->!B",                                                                // (11) Сх. акс. 1
            "B->!B",                                                                    // (12) M.P. 2, 11
            "B->B->B",                                                                  // (13) Сх. акс. 1
            "(B->B->B)->(B->(B->B)->B)->B->B",                                          // (14) Сх. акс. 2
            "(B->(B->B)->B)->B->B",                                                     // (15) M.P. 13, 14
            "(B->(B->B)->B)",                                                           // (16) Сх. акс. 1
            "B->B",                                                                     // (17) M.P. 16, 15
            "(!!A->A)",                                                                 // (18) Сх. акс. 10
            "(!!A->A)->B->(!!A->A)",                                                    // (19) Сх. акс. 1
            "B->(!!A->A)",                                                              // (20) M.P. 18, 19
            "((!A->B)->(!A->!B)->!!A)",                                                 // (21) Сх. акс. 9
            "((!A->B)->(!A->!B)->!!A)->B->((!A->B)->(!A->!B)->!!A)",                    // (22) Сх. акс. 1
            "B->((!A->B)->(!A->!B)->!!A)",                                              // (23) M.P. 21, 22
            "(B->!A->B)",                                                               // (24) Сх. акс. 1
            "(B->!A->B)->B->(B->!A->B)",                                                // (25) Сх. акс. 1
            "B->(B->!A->B)",                                                            // (26) M.P. 24, 25
            "(B->B)->(B->B->(!A->B))->B->(!A->B)",                                      // (27) Сх. акс. 2
            "(B->B->(!A->B))->B->(!A->B)",                                              // (28) M.P. 17, 27
            "B->(!A->B)",                                                               // (29) M.P. 26, 28
            "(B->(!A->B))->(B->(!A->B)->((!A->!B)->!!A))->B->((!A->!B)->!!A)",          // (30) Сх. акс. 2
            "(B->(!A->B)->((!A->!B)->!!A))->B->((!A->!B)->!!A)",                        // (31) M.P. 29, 30
            "B->((!A->!B)->!!A)",                                                       // (32) M.P. 23, 31
            "(!B->!A->!B)",                                                             // (33) Сх. акс. 1
            "(!B->!A->!B)->B->(!B->!A->!B)",                                            // (34) Сх. акс. 1
            "B->(!B->!A->!B)",                                                          // (35) M.P. 33, 34
            "(B->!B)->(B->!B->(!A->!B))->B->(!A->!B)",                                  // (36) Сх. акс. 1
            "(B->!B->(!A->!B))->B->(!A->!B)",                                           // (37) M.P. 12, 36
            "B->(!A->!B)",                                                              // (38) M.P. 35, 37
            "(B->(!A->!B))->(B->(!A->!B)->!!A)->B->!!A",                                // (39) Сх. акс. 2
            "(B->(!A->!B)->!!A)->B->!!A",                                               // (40) M.P. 38, 39
            "B->!!A",                                                                   // (41) M.P. 32, 40
            "(B->!!A)->(B->!!A->A)->B->A",                                              // (42) Сх. акс. 2
            "(B->!!A->A)->B->A",                                                        // (43) M.P. 41, 42
            "B->A",                                                                     // (44) M.P. 20, 43
            "A|B->A",                                                                   // (45) M.P. 44, 10
            "(A|B->!A)->!(A|B)",                                                        // (46) M.P. 45, 3
            "!A->A|B->!A",                                                              // (47) Сх. акс. 1
            "A|B->!A",                                                                  // (48) M.P. 1, 47
            "!(A|B)"                                                                    // (49) M.P. 48, 46, woo-hoo!
    };

    static const std::vector<std::string> proof_false_true = {
            "B",                                                                        // (1) предположение
            "B->A|B",                                                                   // (2) Сх. акс. 7
            "A|B"                                                                       // (3) M.P. 1, 2
    };

    static const std::vector<std::string> proof_true_false = {
            "A",                                                                        // (1) предположение
            "A->A|B",                                                                   // (2) Сх. акс. 6
            "A|B"                                                                       // (3) M.P. 1, 2
    };

    static const std::vector<std::string> proof_true_true = {
            "A",                                                                        // (1) предположение
            "A->A|B",                                                                   // (2) Сх. акс. 6
            "A|B"                                                                       // (3) M.P. 1, 2
    };

    static const std::vector<std::vector<std::vector<std::string>>> proofs = {

            {proof_false_false, proof_false_true},
            {proof_true_false,  proof_true_true}
    };

    return proofs[type_left][type_right];
}