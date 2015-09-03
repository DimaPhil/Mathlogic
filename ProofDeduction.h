#ifndef MATHLOGIC_PROOFDEDUCTION_H
#define MATHLOGIC_PROOFDEDUCTION_H

#include "expressions/Expressions.h"
#include "ProofChecker.h"

using Expressions::Expression;

struct ProofDeduction {
    const int NO_ASSUMPTION = -1;

    Expression *proposal;
    std::vector<std::string> proposal_free_variables;
    std::string proposal_string;
    std::vector<Expression*> assumptions;
    ProofChecker proof_checker;
    std::string error_string;

    void clear() {
        assumptions.clear();
        proposal_string.clear();
        proof_checker.clear();
    }

    static ProofDeduction* get_instance() {
        static ProofDeduction proof_deduction;
        return &proof_deduction;
    }

    std::string &get_error() {
        return error_string;
    }

    Expression* parse_expression(const std::string &const_expression);
    int get_assumption(Expression *expression);
    void add_header(std::string s);
    void add_self_proof(std::vector<Expression*> &result);
    void add_axiom_proof(Expression *expression, std::vector<Expression*> &result);
    void add_modus_ponens_proof(Expression *expression, std::vector<Expression*> &result);
    void add_rule_exists_proof(Expression *expression, std::vector<Expression*> &result);
    void add_rule_forall_proof(Expression *expression, std::vector<Expression*> &result);
    void add_proof(Expression *expression, std::vector<Expression*> &result);
    bool add_proof(const std::string &expression_string, std::vector<Expression*> &result);
};

#endif //MATHLOGIC_PROOFDEDUCTION_H
