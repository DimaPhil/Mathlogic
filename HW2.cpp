#include <cassert>
#include <iostream>
#include <map>
#include <set>
#include <sstream>

#include "expressions/Expressions.h"
#include "expressions/Parser.h"

using Expressions::BinaryOperation;
using Expressions::UnaryOperation;
using Expressions::Variable;

struct HW2 {
    const int NO_AXIOM = -1;
    enum EXPRESSION_TYPE {VARIABLE, UNARY_OPERATION, BINARY_OPERATION, UNKNOWN_TYPE};

    template <class Base, class Derived>
    bool check_class_inherity(Derived &derived) {
        try {
            dynamic_cast<Base&>(derived);
            return true;
        } catch (const std::bad_cast&) {
            return false;
        }
    }

    HW2() {}

    Expression* parse_expression(const std::string &const_expression) {
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

    void add_expression(std::vector<Expression*> &expressions, const std::string &expression) {
        expressions.emplace_back(parse_expression(expression));
    }

    void add_axioms(std::vector<Expression*> &axioms) {
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

    bool expression_matches(Expression *needle, Expression *haystack, std::map<std::string, Expression*> &variables) {
        EXPRESSION_TYPE needle_type = get_expression_type(needle);
        EXPRESSION_TYPE haystack_type = get_expression_type(haystack);
        assert(needle_type != UNKNOWN_TYPE);
        assert(haystack_type != UNKNOWN_TYPE);
        if (needle_type != haystack_type && needle_type != VARIABLE) {
            return false;
        }
        if (needle_type == VARIABLE) {
            Variable *needle_variable = reinterpret_cast<Variable*>(needle);
            if (variables.find(needle_variable->to_string()) != variables.end()) {
                return variables[needle_variable->to_string()]->equals(haystack);
            }
            variables[needle_variable->to_string()] = haystack;
            return true;
        }
        if (needle_type == UNARY_OPERATION) {
            UnaryOperation *unary_needle = reinterpret_cast<UnaryOperation*>(needle);
            UnaryOperation *unary_haystack = reinterpret_cast<UnaryOperation*>(haystack);
            return unary_needle->operation == unary_haystack->operation &&
                   expression_matches(unary_needle->operand, unary_haystack->operand, variables);
        }
        if (needle_type == BINARY_OPERATION) {
            BinaryOperation *binary_needle = reinterpret_cast<BinaryOperation*>(needle);
            BinaryOperation *binary_haystack = reinterpret_cast<BinaryOperation*>(haystack);
            return binary_needle->operation == binary_haystack->operation &&
                   expression_matches(binary_needle->left, binary_haystack->left, variables) &&
                   expression_matches(binary_needle->right, binary_haystack->right, variables);
        }
        throw std::runtime_error("An error occurred in HW1::expression_matches()");
    }

    bool expression_matches(Expression *needle, Expression *haystack) {
        std::map<std::string, Expression*> variables;
        return expression_matches(needle, haystack, variables);
    }

    bool is_axiom(Expression *axiom, Expression *expression) {
        return expression_matches(axiom, expression);
    }

    int get_axiom(const std::vector<Expression*> &axioms, Expression *expression) {
        for (size_t i = 0; i < axioms.size(); i++) {
            Expression *axiom = axioms[i];
            if (is_axiom(axiom, expression)) {
                return (int)i + 1;
            }
        }
        return NO_AXIOM;
    }

    void add_self_proof(std::vector<std::string> &result, const std::string &proposal_string) {
        result.push_back(proposal_string + "->(" + proposal_string + "->" + proposal_string + ")");
        result.push_back("(" + proposal_string + "->(" + proposal_string + "->" + proposal_string + "))->" +
                         "(" + proposal_string + "->((" + proposal_string + "->" + proposal_string + ")->" +
                         proposal_string + "))->" +
                         "(" + proposal_string + "->" + proposal_string + ")");
        result.push_back("(" + proposal_string + "->((" + proposal_string + "->" + proposal_string + ")->" +
                         proposal_string + "))->(" + proposal_string + "->" + proposal_string + ")");
        result.push_back("(" + proposal_string + "->((" + proposal_string + "->" + proposal_string + ")" + "->" +
                         proposal_string + "))");
    }

    void run(const char *input, const char *output) {
        assert(freopen(input, "r", stdin));
        assert(freopen(output, "w", stdout));
        std::ios_base::sync_with_stdio(false);
        std::cin.tie(0);

        std::vector<Expression*> axioms;
        add_axioms(axioms);

        std::string first_line;
        std::getline(std::cin, first_line);
        first_line = Utils::replace(first_line, ' ');

        std::vector<std::string> split_line = Utils::split(first_line, "|-");
        assert(split_line.size() == 2);

        std::string needle = split_line[1];
        std::vector<std::string> assumptions_strings = Utils::split(split_line[0], ',');
        std::string proposal_string = assumptions_strings.back();
        Expression *proposal_string_expression = parse_expression(proposal_string);
        if (check_class_inherity<BinaryOperation>(*proposal_string_expression)) {
            proposal_string = "(" + proposal_string + ")";
        }
        assumptions_strings.pop_back();

        std::set<size_t> assumptions;
        for (size_t i = 0; i < assumptions_strings.size(); i++) {
            assumptions.insert(parse_expression(assumptions_strings[i])->hash());
        }

        std::string delta;
        std::vector<std::string> deltas;
        std::vector<std::string> result;
        std::map<size_t, std::vector<std::pair<size_t, size_t>>> parts;
        std::map<size_t, size_t> all_hashes;
        size_t line_number = 0;
        while (std::getline(std::cin, delta)) {
            ++line_number;
            deltas.push_back(delta);

            Expression *expression = parse_expression(delta);
            if (check_class_inherity<BinaryOperation>(*expression)) {
                BinaryOperation *binary_expression = reinterpret_cast<BinaryOperation*>(expression);
                if (binary_expression->operation == '>') {
                    parts[binary_expression->right->hash()].push_back(
                            std::make_pair(binary_expression->left->hash(), line_number));
                }
            }
            if (get_axiom(axioms, expression) != NO_AXIOM ||
                assumptions.find(expression->hash()) != assumptions.end()) {
                result.push_back(delta);
                result.push_back("(" + delta + ")->(" + proposal_string + "->" + delta + ")");
            } else if (expression->equals(proposal_string_expression)) {
                add_self_proof(result, proposal_string);
            } else {
                std::vector<std::pair<size_t, size_t>> &part = parts[expression->hash()];
                for (size_t i = 0; i < part.size(); i++) {
                    auto it = all_hashes.find(part[i].first);
                    if (it != all_hashes.end()) {
                        std::string delta_i = delta;
                        std::string delta_j = deltas[it->second - 1];
                        result.push_back("(" + proposal_string + "->" + delta_j + ")->((" + proposal_string + "->((" + delta_j + ")->" + delta_i + "))->" +
                                         "(" + proposal_string + "->" + delta_i + "))");
                        result.push_back("((" + proposal_string + "->((" + delta_j + ")->" + delta_i + "))->(" +
                                         proposal_string + "->" + delta_i + "))");
                        break;
                    }
                }
            }
            all_hashes[expression->hash()] = line_number;
            result.push_back(proposal_string + "->" + delta);
        }

        for (size_t i = 0; i < assumptions_strings.size(); i++) {
            std::cout << assumptions_strings[i];
            if (i + 1 < assumptions_strings.size()) {
                std::cout << ",";
            }
        }
        std::cout << "|-";
        std::cout << proposal_string << "->" << needle << '\n';
        for (size_t i = 0; i < result.size(); i++) {
            std::cout << result[i] << '\n';
        }
    }

    void run() {
        run("tests/input.txt", "tests/output.txt");
    }
};