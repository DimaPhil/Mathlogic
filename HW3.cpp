#include <cassert>
#include <iostream>
#include <map>
#include <set>

#include "expressions/Expressions.h"
#include "expressions/Parser.h"

using Expressions::BinaryOperation;
using Expressions::UnaryOperation;
using Expressions::Variable;
using Expressions::Not;

static std::vector<std::string> proof_exclude_third = {
        "!A->(A|!A)",
        "(((!A)->(A|!A)))->(((!A)->(A|!A)))->(((!A)->(A|!A)))",
        "((((!A)->(A|!A)))->(((!A)->(A|!A)))->(((!A)->(A|!A))))->((((!A)->(A|!A)))->((((!A)->(A|!A)))->(((!A)->(A|!A))))->(((!A)->(A|!A))))->(((!A)->(A|!A)))->(((!A)->(A|!A)))",
        "((((!A)->(A|!A)))->((((!A)->(A|!A)))->(((!A)->(A|!A))))->(((!A)->(A|!A))))->(((!A)->(A|!A)))->(((!A)->(A|!A)))",
        "((((!A)->(A|!A)))->((((!A)->(A|!A)))->(((!A)->(A|!A))))->(((!A)->(A|!A))))",
        "(((!A)->(A|!A)))->(((!A)->(A|!A)))",
        "(((!A)->(A|!A))->((!A)->!(A|!A))->!(!A))",
        "(((!A)->(A|!A))->((!A)->!(A|!A))->!(!A))->(((!A)->(A|!A)))->(((!A)->(A|!A))->((!A)->!(A|!A))->!(!A))",
        "(((!A)->(A|!A)))->(((!A)->(A|!A))->((!A)->!(A|!A))->!(!A))",
        "((((!A)->(A|!A)))->((!A)->(A|!A)))->((((!A)->(A|!A)))->((!A)->(A|!A))->(((!A)->!(A|!A))->!(!A)))->(((!A)->(A|!A)))->(((!A)->!(A|!A))->!(!A))",
        "((((!A)->(A|!A)))->((!A)->(A|!A))->(((!A)->!(A|!A))->!(!A)))->(((!A)->(A|!A)))->(((!A)->!(A|!A))->!(!A))",
        "(((!A)->(A|!A)))->(((!A)->!(A|!A))->!(!A))",
        "((!(A|!A)->((!A)->!(A|!A))) ->(!(A|!A)->((!A)->!(A|!A))->!(!A))->(!(A|!A)->!(!A)))",
        "((!(A|!A)->((!A)->!(A|!A))) ->(!(A|!A)->((!A)->!(A|!A))->!(!A))->(!(A|!A)->!(!A)))->(((!A)->(A|!A)))->((!(A|!A)->((!A)->!(A|!A))) ->(!(A|!A)->((!A)->!(A|!A))->!(!A))->(!(A|!A)->!(!A)))",
        "(((!A)->(A|!A)))->((!(A|!A)->((!A)->!(A|!A))) ->(!(A|!A)->((!A)->!(A|!A))->!(!A))->(!(A|!A)->!(!A)))",
        "(!(A|!A)->(!A)->!(A|!A))",
        "(!(A|!A)->(!A)->!(A|!A))->(((!A)->(A|!A)))->(!(A|!A)->(!A)->!(A|!A))",
        "(((!A)->(A|!A)))->(!(A|!A)->(!A)->!(A|!A))",
        "((((!A)->(A|!A)))->(!(A|!A)->(!A)->!(A|!A)))->((((!A)->(A|!A)))->(!(A|!A)->(!A)->!(A|!A))->((!(A|!A)->((!A)->!(A|!A))->!(!A))->(!(A|!A)->!(!A))))->(((!A)->(A|!A)))->((!(A|!A)->((!A)->!(A|!A))->!(!A))->(!(A|!A)->!(!A)))",
        "((((!A)->(A|!A)))->(!(A|!A)->(!A)->!(A|!A))->((!(A|!A)->((!A)->!(A|!A))->!(!A))->(!(A|!A)->!(!A))))->(((!A)->(A|!A)))->((!(A|!A)->((!A)->!(A|!A))->!(!A))->(!(A|!A)->!(!A)))",
        "(((!A)->(A|!A)))->((!(A|!A)->((!A)->!(A|!A))->!(!A))->(!(A|!A)->!(!A)))",
        "((((!A)->!(A|!A))->!(!A))->!(A|!A)->(((!A)->!(A|!A))->!(!A)))",
        "((((!A)->!(A|!A))->!(!A))->!(A|!A)->(((!A)->!(A|!A))->!(!A)))->(((!A)->(A|!A)))->((((!A)->!(A|!A))->!(!A))->!(A|!A)->(((!A)->!(A|!A))->!(!A)))",
        "(((!A)->(A|!A)))->((((!A)->!(A|!A))->!(!A))->!(A|!A)->(((!A)->!(A|!A))->!(!A)))",
        "((((!A)->(A|!A)))->(((!A)->!(A|!A))->!(!A)))->((((!A)->(A|!A)))->(((!A)->!(A|!A))->!(!A))->(!(A|!A)->(((!A)->!(A|!A))->!(!A))))->(((!A)->(A|!A)))->(!(A|!A)->(((!A)->!(A|!A))->!(!A)))",
        "((((!A)->(A|!A)))->(((!A)->!(A|!A))->!(!A))->(!(A|!A)->(((!A)->!(A|!A))->!(!A))))->(((!A)->(A|!A)))->(!(A|!A)->(((!A)->!(A|!A))->!(!A)))",
        "(((!A)->(A|!A)))->(!(A|!A)->(((!A)->!(A|!A))->!(!A)))",
        "((((!A)->(A|!A)))->(!(A|!A)->(((!A)->!(A|!A))->!(!A))))->((((!A)->(A|!A)))->(!(A|!A)->(((!A)->!(A|!A))->!(!A)))->(!(A|!A)->!(!A)))->(((!A)->(A|!A)))->(!(A|!A)->!(!A))",
        "((((!A)->(A|!A)))->(!(A|!A)->(((!A)->!(A|!A))->!(!A)))->(!(A|!A)->!(!A)))->(((!A)->(A|!A)))->(!(A|!A)->!(!A))",
        "(((!A)->(A|!A)))->(!(A|!A)->!(!A))",
        "(!(A|!A))->!!A",
        "A->(A|!A)",
        "((A->(A|!A)))->((A->(A|!A)))->((A->(A|!A)))",
        "(((A->(A|!A)))->((A->(A|!A)))->((A->(A|!A))))->(((A->(A|!A)))->(((A->(A|!A)))->((A->(A|!A))))->((A->(A|!A))))->((A->(A|!A)))->((A->(A|!A)))",
        "(((A->(A|!A)))->(((A->(A|!A)))->((A->(A|!A))))->((A->(A|!A))))->((A->(A|!A)))->((A->(A|!A)))",
        "(((A->(A|!A)))->(((A->(A|!A)))->((A->(A|!A))))->((A->(A|!A))))",
        "((A->(A|!A)))->((A->(A|!A)))",
        "((A->(A|!A))->(A->!(A|!A))->!A)",
        "((A->(A|!A))->(A->!(A|!A))->!A)->((A->(A|!A)))->((A->(A|!A))->(A->!(A|!A))->!A)",
        "((A->(A|!A)))->((A->(A|!A))->(A->!(A|!A))->!A)",
        "(((A->(A|!A)))->(A->(A|!A)))->(((A->(A|!A)))->(A->(A|!A))->((A->!(A|!A))->!A))->((A->(A|!A)))->((A->!(A|!A))->!A)",
        "(((A->(A|!A)))->(A->(A|!A))->((A->!(A|!A))->!A))->((A->(A|!A)))->((A->!(A|!A))->!A)",
        "((A->(A|!A)))->((A->!(A|!A))->!A)",
        "((!(A|!A)->(A->!(A|!A))) ->(!(A|!A)->(A->!(A|!A))->!A)->(!(A|!A)->!A))",
        "((!(A|!A)->(A->!(A|!A))) ->(!(A|!A)->(A->!(A|!A))->!A)->(!(A|!A)->!A))->((A->(A|!A)))->((!(A|!A)->(A->!(A|!A))) ->(!(A|!A)->(A->!(A|!A))->!A)->(!(A|!A)->!A))",
        "((A->(A|!A)))->((!(A|!A)->(A->!(A|!A))) ->(!(A|!A)->(A->!(A|!A))->!A)->(!(A|!A)->!A))",
        "(!(A|!A)->A->!(A|!A))",
        "(!(A|!A)->A->!(A|!A))->((A->(A|!A)))->(!(A|!A)->A->!(A|!A))",
        "((A->(A|!A)))->(!(A|!A)->A->!(A|!A))",
        "(((A->(A|!A)))->(!(A|!A)->A->!(A|!A)))->(((A->(A|!A)))->(!(A|!A)->A->!(A|!A))->((!(A|!A)->(A->!(A|!A))->!A)->(!(A|!A)->!A)))->((A->(A|!A)))->((!(A|!A)->(A->!(A|!A))->!A)->(!(A|!A)->!A))",
        "(((A->(A|!A)))->(!(A|!A)->A->!(A|!A))->((!(A|!A)->(A->!(A|!A))->!A)->(!(A|!A)->!A)))->((A->(A|!A)))->((!(A|!A)->(A->!(A|!A))->!A)->(!(A|!A)->!A))",
        "((A->(A|!A)))->((!(A|!A)->(A->!(A|!A))->!A)->(!(A|!A)->!A))",
        "(((A->!(A|!A))->!A)->!(A|!A)->((A->!(A|!A))->!A))",
        "(((A->!(A|!A))->!A)->!(A|!A)->((A->!(A|!A))->!A))->((A->(A|!A)))->(((A->!(A|!A))->!A)->!(A|!A)->((A->!(A|!A))->!A))",
        "((A->(A|!A)))->(((A->!(A|!A))->!A)->!(A|!A)->((A->!(A|!A))->!A))",
        "(((A->(A|!A)))->((A->!(A|!A))->!A))->(((A->(A|!A)))->((A->!(A|!A))->!A)->(!(A|!A)->((A->!(A|!A))->!A)))->((A->(A|!A)))->(!(A|!A)->((A->!(A|!A))->!A))",
        "(((A->(A|!A)))->((A->!(A|!A))->!A)->(!(A|!A)->((A->!(A|!A))->!A)))->((A->(A|!A)))->(!(A|!A)->((A->!(A|!A))->!A))",
        "((A->(A|!A)))->(!(A|!A)->((A->!(A|!A))->!A))",
        "(((A->(A|!A)))->(!(A|!A)->((A->!(A|!A))->!A)))->(((A->(A|!A)))->(!(A|!A)->((A->!(A|!A))->!A))->(!(A|!A)->!A))->((A->(A|!A)))->(!(A|!A)->!A)",
        "(((A->(A|!A)))->(!(A|!A)->((A->!(A|!A))->!A))->(!(A|!A)->!A))->((A->(A|!A)))->(!(A|!A)->!A)",
        "((A->(A|!A)))->(!(A|!A)->!A)",
        "(!(A|!A))->!A",
        "((!(A|!A))->!A)->(!(A|!A)->!(!A))->(!!(A|!A))",
        "(!(A|!A)->!(!A))->!(!(A|!A))",
        "!(!(A|!A))",
        "!!(A|!A)->(A|!A)",
        "A|!A"
};

struct HW3 {
    const int NO_AXIOM = -1;
    enum EXPRESSION_TYPE {
        VARIABLE, UNARY_OPERATION, BINARY_OPERATION, UNKNOWN_TYPE
    };

    template<class Base, class Derived>
    bool check_class_inherity(Derived &derived) {
        try {
            dynamic_cast<Base &>(derived);
            return true;
        } catch (const std::bad_cast &) {
            return false;
        }
    }

    HW3() { }

    Expression *parse_expression(const std::string &const_expression) {
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

    void add_expression(std::vector<Expression *> &expressions, const std::string &expression) {
        expressions.emplace_back(parse_expression(expression));
    }

    void add_axioms(std::vector<Expression *> &axioms) {
        add_expression(axioms, "A->B->A");
        add_expression(axioms, "(A->B)->(A->B->C)->(A->C)");
        add_expression(axioms, "A&B->A");
        add_expression(axioms, "A&B->B");
        add_expression(axioms, "A->B->A&B");
        add_expression(axioms, "A->A|B");
        add_expression(axioms, "B->A|B");
        add_expression(axioms, "(A->Q)->(B->Q)->(A|B->Q)");
        add_expression(axioms, "(A->B)->(A->!B)->!A");
        add_expression(axioms, "!!A->A");
    }

    //bad function, but don't know, how to do it better
    //if new classes were added, modify this function
    EXPRESSION_TYPE get_expression_type(Expression *expression) {
        if (check_class_inherity<Variable>(*expression)) {
            return VARIABLE;
        }
        if (check_class_inherity<UnaryOperation>(*expression)) {
            return UNARY_OPERATION;
        }
        if (check_class_inherity<BinaryOperation>(*expression)) {
            return BINARY_OPERATION;
        }
        return UNKNOWN_TYPE;
    }

    bool expression_matches(Expression *needle, Expression *haystack, std::map<std::string, Expression *> &variables) {
        EXPRESSION_TYPE needle_type = get_expression_type(needle);
        EXPRESSION_TYPE haystack_type = get_expression_type(haystack);
        assert(needle_type != UNKNOWN_TYPE);
        assert(haystack_type != UNKNOWN_TYPE);
        if (needle_type != haystack_type && needle_type != VARIABLE) {
            return false;
        }
        if (needle_type == VARIABLE) {
            Variable *needle_variable = reinterpret_cast<Variable *>(needle);
            if (variables.find(needle_variable->to_string()) != variables.end()) {
                return variables[needle_variable->to_string()]->equals(haystack);
            }
            variables[needle_variable->to_string()] = haystack;
            return true;
        }
        if (needle_type == UNARY_OPERATION) {
            UnaryOperation *unary_needle = reinterpret_cast<UnaryOperation *>(needle);
            UnaryOperation *unary_haystack = reinterpret_cast<UnaryOperation *>(haystack);
            return unary_needle->operation == unary_haystack->operation &&
                   expression_matches(unary_needle->operand, unary_haystack->operand, variables);
        }
        if (needle_type == BINARY_OPERATION) {
            BinaryOperation *binary_needle = reinterpret_cast<BinaryOperation *>(needle);
            BinaryOperation *binary_haystack = reinterpret_cast<BinaryOperation *>(haystack);
            return binary_needle->operation == binary_haystack->operation &&
                   expression_matches(binary_needle->left, binary_haystack->left, variables) &&
                   expression_matches(binary_needle->right, binary_haystack->right, variables);
        }
        throw std::runtime_error("An error occurred in HW1::expression_matches()");
    }

    bool expression_matches(Expression *needle, Expression *haystack) {
        std::map<std::string, Expression *> variables;
        return expression_matches(needle, haystack, variables);
    }

    bool is_axiom(Expression *axiom, Expression *expression) {
        return expression_matches(axiom, expression);
    }

    int get_axiom(const std::vector<Expression *> &axioms, Expression *expression) {
        for (size_t i = 0; i < axioms.size(); i++) {
            Expression *axiom = axioms[i];
            if (is_axiom(axiom, expression)) {
                return (int) i + 1;
            }
        }
        return NO_AXIOM;
    }

    struct Result {
        bool is_always_true;
        std::map<std::string, bool> variables_values;

        Result() { }

        Result(bool is_always_true) {
            this->is_always_true = is_always_true;
            variables_values.clear();
        }

        Result(bool is_always_true, size_t mask, const std::vector<std::string> &variables) {
            this->is_always_true = is_always_true;
            variables_values = mask_to_map(mask, variables);
        }
    };

    template<typename T>
    static std::map<T, bool> mask_to_map(size_t mask, const std::vector<T> &elements) {
        std::map<T, bool> result;
        for (size_t i = 0; i < elements.size(); i++) {
            result[elements[i]] = (mask & (1 << i)) != 0;
        }
        return result;
    }

    Result check_always_true(Expression *expression) {
        std::map<std::string, bool> variables_values;
        std::vector<std::string> variables = expression->get_variables();
        for (size_t mask = 0; mask < (1U << variables.size()); mask++) {
            try {
                if (!expression->calculate(mask_to_map(mask, variables))) {
                    return Result(false, mask, variables);
                }
            } catch (const std::exception &e) {
                std::cerr << "Error in check_always_true(): " << e.what() << '\n';
            }
        }
        return Result(true);
    }

    struct ProofPart {
        std::map<std::string, bool> variables_values;
        std::vector<Expression *> proof;

        ProofPart() { }

        ProofPart(const std::map<std::string, bool> &variables_values) {
            this->variables_values = variables_values;
        }

        ProofPart(const std::map<std::string, bool> &variables_values, const std::vector<Expression *> &proof) {
            this->variables_values = variables_values;
            this->proof = proof;
        }
    };

    std::vector<ProofPart>::iterator find_variable_negation(std::vector<ProofPart> &parts,
                                                            ProofPart &part,
                                                            const std::string &variable) {
        auto part_variable_negation = parts.begin();
        while (part_variable_negation != parts.end()) {
            bool fail = false;
            for (auto variable_and_value : part_variable_negation->variables_values) {
                if (variable_and_value.first != variable &&
                    variable_and_value.second != part.variables_values[variable_and_value.first]) {
                    fail = true;
                    break;
                }
                if (variable_and_value.first == variable &&
                    variable_and_value.second == part.variables_values[variable_and_value.first]) {
                    fail = true;
                    break;
                }
            }
            if (!fail) {
                break;
            }
            ++part_variable_negation;
        }
        return part_variable_negation;
    }

    std::vector<std::string> deltas;
    std::map<size_t, std::vector<std::pair<size_t, size_t>>> parts;
    std::map<size_t, size_t> all_hashes;
    size_t line_number = 0;

    void prove_deduction(Expression *expression, std::vector<Expression *> &result,
                         const std::set<size_t> &assumptions, Expression *alpha) {
        deltas.emplace_back(expression->to_string());
        ++line_number;

        std::vector<Expression *> axioms;
        add_axioms(axioms);
        std::string delta = '(' + expression->to_string() + ')';

        if (check_class_inherity<BinaryOperation>(*expression)) {
            BinaryOperation *binary_expression = reinterpret_cast<BinaryOperation *>(expression);
            if (binary_expression->operation == '>') {
                parts[binary_expression->right->hash()].push_back(
                        std::make_pair(binary_expression->left->hash(), line_number));
            }
        }
        if (get_axiom(axioms, expression) != -1 || assumptions.find(expression->hash()) != assumptions.end()) {
            result.emplace_back(parse_expression(delta));
            result.emplace_back(parse_expression("(" + delta + ")->(" + alpha->to_string() + "->" + delta + ")"));
        } else if (expression->equals(alpha)) {
            std::string a = alpha->to_string();
            result.emplace_back(parse_expression(a + "->(" + a + "->" + a + ")"));
            result.emplace_back(parse_expression("(" + a + "->(" + a + "->" + a + "))->" +
                                              "(" + a + "->((" + a + "->" + a + ")->" + a + "))->" +
                                              "(" + a + "->" + a + ")"));
            result.emplace_back(
                    parse_expression("(" + a + "->((" + a + "->" + a + ")->" + a + "))->(" + a + "->" + a + ")"));
            result.emplace_back(parse_expression("(" + a + "->((" + a + "->" + a + ")" + "->" + a + "))"));
        } else {
            std::vector<std::pair<size_t, size_t>> &part = parts[expression->hash()];
            for (size_t i = 0; i < part.size(); i++) {
                auto it = all_hashes.find(part[i].first);
                if (it != all_hashes.end()) {
                    std::string delta_i = delta;
                    std::string delta_j = deltas[it->second - 1];
                    std::string a = alpha->to_string();
                    result.emplace_back(parse_expression(
                            "(" + a + "->" + delta_j + ")->((" + a + "->((" + delta_j + ")->" + delta_i + "))->" +
                            "(" + a + "->" + delta_i + "))"));
                    result.emplace_back(parse_expression(
                            "((" + a + "->((" + delta_j + ")->" + delta_i + "))->(" + a + "->" + delta_i + "))"));
                    break;
                }
            }
        }
        all_hashes[expression->hash()] = line_number;
        result.emplace_back(new Expressions::Implication(alpha, expression));
    }

    void prove_variable(const std::string &variable, ProofPart &part) {
        deltas.clear();
        parts.clear();
        all_hashes.clear();
        line_number = 0;

        Expression *alpha_expression = new Variable(variable);
        if (!part.variables_values[variable]) {
            alpha_expression = new Not(alpha_expression);
        }
        std::set<size_t> assumptions;
        for (auto variable_and_value : part.variables_values) {
            if (variable_and_value.first == variable) {
                continue;
            }
            if (variable_and_value.second) {
                assumptions.insert((new Variable(variable_and_value.first))->hash());
            } else {
                assumptions.insert((new Not(new Variable(variable_and_value.first)))->hash());
            }
        }
        std::vector<Expression *> result;
        for (Expression *expression: part.proof) {
            prove_deduction(expression, result, assumptions, alpha_expression);
        }
        part.proof.assign(result.begin(), result.end());
        part.variables_values.erase(variable);
    }

    void prove_with_exclude_third(Expression *expression, std::vector<Expression *> &result,
                                  const std::string &variable) {
        for (size_t i = 0; i < proof_exclude_third.size(); i++) {
            size_t index = 0;
            while (true) {
                index = proof_exclude_third[i].find("A", index);
                if (index == std::string::npos) {
                    break;
                }
                proof_exclude_third[i].replace(index, 1, variable);
                index += variable.size();
            }
            result.emplace_back(parse_expression(proof_exclude_third[i]));
            index = 0;
            while (true) {
                index = proof_exclude_third[i].find(variable, index);
                if (index == std::string::npos) {
                    break;
                }
                proof_exclude_third[i].replace(index, variable.size(), "A");
                index++;
            }
        }
        std::vector<std::string> proof = {
                "(P->(A))->(!P->(A))->((P|!P)->(A))", // (1) Сх. акс. 8
                "(!P->(A))->((P|!P)->(A))",           // (2) M.P. (P->A), (1)
                "(P|!P)->(A)",                        // (3) M.P. (!P->A), (2)
                "A"                                   // (4) M.P. (P|!P), (3)
        };

        std::map<std::string, Expression*> changes_to_apply;
        changes_to_apply["P"] = new Variable(variable);
        changes_to_apply["A"] = expression;
        for (size_t i = 0; i < proof.size(); i++) {
            Expression *parsed_proof = parse_expression(proof[i]);
            result.emplace_back(parsed_proof->substitute(changes_to_apply));
        }
    }

    void prove_expression(Expression *expression) {
        std::vector<std::string> variables = expression->get_variables();
         std::vector<ProofPart> parts;
        for (size_t mask = 0; mask < (1U << variables.size()); mask++) {
            std::map<std::string, bool> variables_values = mask_to_map(mask, variables);
            parts.push_back(ProofPart(variables_values));
            expression->prove_with_values(parts.back().proof, variables_values);
        }
        std::map<std::string, bool> variables_values = mask_to_map(0, variables);
        while (!variables_values.empty()) {
            std::string variable = variables_values.begin()->first;
            auto part = parts.begin();
            while (part != parts.end()) {
                if (part->variables_values.find(variable) != part->variables_values.end()) {
                    break;
                }
                ++part;
            }
            if (part == parts.end()) {
                variables_values.erase(variable);
                continue;
            }
            auto part_variable_negation = find_variable_negation(parts, *part, variable);
            prove_variable(variable, *part);
            prove_variable(variable, *part_variable_negation);

            std::vector<Expression *> result;
            result.insert(result.end(), part->proof.begin(), part->proof.end());
            result.insert(result.end(), part_variable_negation->proof.begin(), part_variable_negation->proof.end());
            std::map<std::string, bool> new_variables_values(part->variables_values.begin(), part->variables_values.end());

            prove_with_exclude_third(expression, result, variable);

            parts.erase(part);
            parts.erase(part_variable_negation + (part < part_variable_negation ? -1 : 0));
            parts.emplace_back(new_variables_values, result);
            ++part;
        }

        for (Expression *part : parts.begin()->proof) {
            std::cout << Utils::replace(part->to_string(), ">", "->") << '\n';
        }
    }

    void run(const char *input, const char *output) {
        assert(freopen(input, "r", stdin));
        assert(freopen(output, "w", stdout));
        std::ios_base::sync_with_stdio(false);
        std::cin.tie(0);

        std::string expression_to_check_string;
        std::cin >> expression_to_check_string;
        Expression *expression_to_check = parse_expression(expression_to_check_string);
        Result result = check_always_true(expression_to_check);
        if (!result.is_always_true) {
            std::cout << "Высказывание ложно при ";
            for (auto it = result.variables_values.begin(); it != result.variables_values.end();) {
                std::cout << it->first << "=" << (it->second ? "И" : "Л");
                ++it;
                if (it != result.variables_values.end()) {
                    std::cout << ", ";
                }
            }
        } else {
            prove_expression(expression_to_check);
        }
    }

    void run() {
        run("tests/input.txt", "tests/output.txt");
    }
};