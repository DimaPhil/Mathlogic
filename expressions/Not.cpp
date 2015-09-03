#include <iostream>

#include "Expressions.h"
#include "Parser.h"

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

static Expression* parse_expression(const std::string &const_expression) {
    std::string expression = const_expression;
    expression = Utils::replace(expression, ' ');
    expression = Utils::replace(expression, '\t');
    expression = Utils::replace(expression, "->", ">");

    Parser parser;
    try {
        return parser.parse(expression);
    } catch (const Parser::ParserError &e) {
        std::cerr << "Error in parsing expression\nExpression: " << expression << "\nError: " << e.what() << '\n';
        return nullptr;
    }
}

Expressions::Not::Not(Expression *operand) : UnaryOperation('!', operand) {}

bool Expressions::Not::calculate(const std::map<std::string, bool> &variables_values) {
    return !operand->calculate(variables_values);
}

Expression* Expressions::Not::substitute(const std::map<std::string, Expression *> &changes_to_apply) {
    return new Not(operand->substitute(changes_to_apply));
}

bool Expressions::Not::is_substitute(Expression *expression) {
    return check_class_inherity<UnaryOperation>(*expression) &&
           operand->is_substitute(reinterpret_cast<UnaryOperation*>(expression)->operand);
}

void Expressions::Not::prove_with_values(std::vector<Expression *> &proof,
                                         const std::map<std::string, bool> &variables_values) {
    static const std::vector<std::string> proof_true = {
            "A",                                            // (1) предположение
            "(!A->A)->(!A->!A)->!!A",                       // (2) Сх. акс. 9
            "A->!A->A",                                     // (3) Сх. акс. 1
            "!A->A",                                        // (4) M.P. 1, 3
            "(!A->!A)->!!A",                                // (5) M.P. 4, 2
            "!A->!A->!A",                                   // (6) Сх. акс. 1
            "(!A->!A->!A)->(!A->(!A->!A)->!A)->(!A->!A)",   // (7) Сх. акс. 2
            "(!A->(!A->!A)->!A)->(!A->!A)",                 // (8) M.P. 6, 7
            "!A->(!A->!A)->!A",                             // (9) Сх. акс. 1
            "!A->!A",                                       // (10) M.P. 9, 8
            "!!A"                                           // (11) M.P. 10, 5
    };

    operand->prove_with_values(proof, variables_values);
    if (!operand->calculate(variables_values)) {
        proof.emplace_back(new Not(operand));
        return;
    }

    std::vector<std::string> new_proof = proof_true;
    std::map<std::string, Expression *> changes_to_apply;
    changes_to_apply["A"] = operand;
    for (size_t i = 0; i < new_proof.size(); i++) {
        Expression *parsed_proof = parse_expression(new_proof[i]);
        proof.emplace_back(parsed_proof->substitute(changes_to_apply));
    }
}