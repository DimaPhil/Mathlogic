#ifndef CLIONPROJECTS_EXPRESSION_H
#define CLIONPROJECTS_EXPRESSION_H

#include "../Utils.h"

#include <string>
#include <vector>
#include <typeinfo>
#include <map>

namespace Expressions {
    struct SubstitutionState {
        bool was_substituted;
        bool successuful;

        SubstitutionState()
                : was_substituted(false)
                , successuful(true) {}

        SubstitutionState(bool was_substituted, bool successful = true) {
            this->was_substituted = was_substituted;
            this->successuful = successful;
        }

        SubstitutionState& operator = (const SubstitutionState &other) {
            was_substituted |= other.was_substituted;
            successuful &= other.successuful;
            return *this;
        }
    };

    struct Expression {
        virtual bool equals(Expression *expression) = 0;
        virtual std::string to_string() = 0;
        virtual std::string simple_to_string() { return std::string(); }
        virtual size_t hash() = 0;
        virtual std::vector<std::string> get_variables() = 0;
        virtual bool calculate(const std::map<std::string, bool> &variables_values) = 0;
        virtual void prove_with_values(std::vector<Expression *> &proof,
                                       const std::map<std::string, bool> &variables_values) = 0;
        virtual Expression *substitute(const std::map<std::string, Expression *> &changes_to_apply) = 0;
        virtual bool is_substitute(Expression *expression) = 0;
        virtual SubstitutionState is_free_to_substitute(const std::string &variable_name,
                                                        const std::vector<std::string> &free_variables) = 0;

        virtual ~Expression() {}
    };

    struct Variable : public Expression {
        std::string name;

        Variable(const char *name);
        Variable(const std::string &name);
        bool equals(Expression *expression);
        std::string to_string();
        std::string simple_to_string();
        size_t hash();
        std::vector<std::string> get_variables();
        bool calculate(const std::map<std::string, bool> &variables_values);
        void prove_with_values(std::vector<Expression *> &proof, const std::map<std::string, bool> &variables_values);
        Expression *substitute(const std::map<std::string, Expression *> &changes_to_apply);
        bool is_substitute(Expression *expression);
        SubstitutionState is_free_to_substitute(const std::string &variable_name,
                                                        const std::vector<std::string> &free_variables);
    };

    struct UnaryOperation : public Expression {
        char operation;
        Expression *operand;

        UnaryOperation();
        UnaryOperation(const char operation, Expression *operand);
        bool equals(Expression *expression);
        std::string to_string();
        std::string simple_to_string();
        size_t hash();
        std::vector<std::string> get_variables();

        virtual SubstitutionState is_free_to_substitute(const std::string &variable_name,
                                                        const std::vector<std::string> &free_variables) override;
    };

    struct Not : public UnaryOperation {
        Not(Expression *operand);
        bool calculate(const std::map<std::string, bool> &variables_values);
        Expression *substitute(const std::map<std::string, Expression *> &changes_to_apply);
        void prove_with_values(std::vector<Expression *> &proof, const std::map<std::string, bool> &variables_values);

        virtual bool is_substitute(Expression *expression) override;
    };

    struct BinaryOperation : public Expression {
        char operation;
        Expression *left;
        Expression *right;

        BinaryOperation();
        BinaryOperation(const char operation, Expression *left, Expression *right);
        bool equals(Expression *expression);
        std::string to_string();
        std::string simple_to_string();
        size_t hash();
        std::vector<std::string> get_variables();
        void prove_with_values(std::vector<Expression *> &proof, const std::map<std::string, bool> &variables_values);
        virtual std::vector<std::string> get_proof(size_t type_left, size_t type_right) = 0;

        virtual bool is_substitute(Expression *expression) override;
        virtual SubstitutionState is_free_to_substitute(const std::string &variable_name,
                                                        const std::vector<std::string> &free_variables) override;

        virtual ~BinaryOperation() {}
    };

    struct Implication : public BinaryOperation {
        Implication(Expression *left, Expression *right);
        bool calculate(const std::map<std::string, bool> &variables_values);
        Expression *substitute(const std::map<std::string, Expression *> &changes_to_apply);
        virtual bool is_substitute(Expression *expression) override;
        std::vector<std::string> get_proof(size_t type_left, size_t type_right);
    };

    struct And : public BinaryOperation {
        And(Expression *left, Expression *right);
        bool calculate(const std::map<std::string, bool> &variables_values);
        Expression *substitute(const std::map<std::string, Expression *> &changes_to_apply);
        virtual bool is_substitute(Expression *expression) override;
        std::vector<std::string> get_proof(size_t type_left, size_t type_right);
    };

    struct Or : public BinaryOperation {
        Or(Expression *left, Expression *right);
        bool calculate(const std::map<std::string, bool> &variables_values);
        Expression *substitute(const std::map<std::string, Expression *> &changes_to_apply);
        virtual bool is_substitute(Expression *expression) override;
        std::vector<std::string> get_proof(size_t type_left, size_t type_right);
    };

    struct Equality : public BinaryOperation {
        Equality(Expression *left, Expression *right);

        Expression *substitute(const std::map<std::string, Expression *> &changes_to_apply);
        bool calculate(const std::map<std::string, bool> &variables_values);
        std::vector<std::string> get_proof(size_t type_left, size_t type_right);
    };

    struct Add : public BinaryOperation {
        Add(Expression *left, Expression *right);

        Expression *substitute(const std::map<std::string, Expression *> &changes_to_apply);
        bool calculate(const std::map<std::string, bool> &variables_values);
        std::vector<std::string> get_proof(size_t type_left, size_t type_right);
    };

    struct Multiply : public BinaryOperation {
        Multiply(Expression *left, Expression *right);

        Expression *substitute(const std::map<std::string, Expression *> &changes_to_apply);
        bool calculate(const std::map<std::string, bool> &variables_values);
        std::vector<std::string> get_proof(size_t type_left, size_t type_right);
    };

    struct Quantifier : public Expression {
        Expression *variable;
        Expression *next;
        char operation;

        Quantifier();
        Quantifier(const std::string &variable, Expression *next, char operation);
        Quantifier(Expression *variable, Expression *next, char operation);

        bool equals(Expression *expression);
        bool is_substitute(Expression *expression);
        std::string to_string();
        std::vector<std::string> get_variables();
        size_t hash();
        SubstitutionState is_free_to_substitute(const std::string &variable_name,
                                                const std::vector<std::string> &free_variables);

        void prove_with_values(std::vector<Expression *> &proof,
                                       const std::map<std::string, bool> &variables_values);
        bool calculate(const std::map<std::string, bool> &variables_values);
    };

    struct ForallQuantifier : public Quantifier {
        ForallQuantifier(const std::string &variable, Expression *next);
        ForallQuantifier(Expression *variable, Expression *next);

        Expression *substitute(const std::map<std::string, Expression *> &changes_to_apply);
    };

    struct ExistsQuantifier : public Quantifier {
        ExistsQuantifier(const std::string &variable, Expression *next);
        ExistsQuantifier(Expression *variable, Expression *next);

        Expression *substitute(const std::map<std::string, Expression *> &changes_to_apply);
    };

    struct ArgumentsHandler : public Expression {
        std::string name;
        std::vector<Expression*> terms;

        ArgumentsHandler();
        ArgumentsHandler(const std::string &name, const std::vector<Expression*> &terms);

        std::string to_string();
        bool equals(Expression *expression);
        size_t hash();
        std::vector<std::string> get_variables();

        void prove_with_values(std::vector<Expression *> &proof,
                               const std::map<std::string, bool> &variables_values);
        bool calculate(const std::map<std::string, bool> &variables_values);
        bool is_substitute(Expression *expression);
        SubstitutionState is_free_to_substitute(const std::string &variable_name,
                                   const std::vector<std::string> &free_variables);
    };

    struct Predicate : public ArgumentsHandler {
        Predicate(const std::string &name, const std::vector<Expression*> &terms);

        Expression *substitute(const std::map<std::string, Expression *> &changes_to_apply);
        bool is_substitute(Expression *expression);
    };

    struct Zero : public Expression {
        std::string name;

        Zero();

        std::string to_string();
        bool equals(Expression *expression);
        size_t hash();
        std::vector<std::string> get_variables();
        Expression *substitute(const std::map<std::string, Expression *> &changes_to_apply);

        void prove_with_values(std::vector<Expression *> &proof,
                               const std::map<std::string, bool> &variables_values);
        bool calculate(const std::map<std::string, bool> &variables_values);
        bool is_substitute(Expression *expression);
        SubstitutionState is_free_to_substitute(const std::string &variable_name,
                                                const std::vector<std::string> &free_variables);
    };

    struct Stroke : public Expression {
        std::string operation;
        Expression *operand;

        Stroke(Expression *operand);

        std::string to_string();
        bool equals(Expression *expression);
        size_t hash();
        std::vector<std::string> get_variables();
        Expression *substitute(const std::map<std::string, Expression *> &changes_to_apply);
        bool is_substitute(Expression *expression);

        void prove_with_values(std::vector<Expression *> &proof,
                               const std::map<std::string, bool> &variables_values);
        bool calculate(const std::map<std::string, bool> &variables_values);
        SubstitutionState is_free_to_substitute(const std::string &variable_name,
                                                const std::vector<std::string> &free_variables);
    };
}

#endif //CLIONPROJECTS_EXPRESSION_H