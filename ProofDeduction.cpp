#include <iostream>
#include <algorithm>

#include "ProofDeduction.h"
#include "expressions/ParserFormalArithmetic.h"

Expression* ProofDeduction::parse_expression(const std::string &const_expression) {
    std::string expression = const_expression;
    expression = Utils::replace(expression, ' ');
    expression = Utils::replace(expression, '\t');
    expression = Utils::replace(expression, "->", ">");

    ParserFormalArithmetic parser;
    try {
        return parser.parse(expression);
    } catch (const ParserFormalArithmetic::ParserError &e) {
        std::cerr << "Error in parsing expression\nExpression: " << expression << "\nError: " << e.what() <<
        '\n';
        return nullptr;
    }
}

int ProofDeduction::get_assumption(Expression *expression) {
    for (size_t i = 0; i < assumptions.size(); i++) {
        if (assumptions[i]->equals(expression)) {
            return (int)i;
        }
    }
    return NO_ASSUMPTION;
}

void ProofDeduction::add_header(std::string s) {
    clear();
    size_t breaker = s.find("|-", 0);
    if (breaker == std::string::npos) {
        std::cerr << "Syntax error in proposal" << "\n";
        exit(-1);
    }
    size_t breaker_old = breaker;
    s = ';' + s.substr(0, breaker);
    breaker_old++;
    breaker = s.rfind(';', s.size() - 1);
    proposal_string = s.substr(breaker + 1, breaker_old - breaker - 1);
    proposal = parse_expression(proposal_string);
    proposal_free_variables = proposal->get_variables();
    breaker_old = breaker;
    while (breaker != 0)
    {
        breaker = s.rfind(';', breaker_old - 1);
        assumptions.emplace_back(parse_expression(s.substr(breaker + 1, breaker_old - breaker - 1)));
        breaker_old = breaker;
    }
    proposal_string = "(" + proposal_string + ")";
}

void ProofDeduction::add_self_proof(std::vector<Expression *> &result) {
    result.push_back(parse_expression(proposal_string + "->(" + proposal_string + "->" + proposal_string + ")"));
    result.push_back(parse_expression("(" + proposal_string + "->(" + proposal_string + "->" + proposal_string + "))->" +
                                      "(" + proposal_string + "->((" + proposal_string + "->" + proposal_string + ")->" + proposal_string + "))->" +
                                      "(" + proposal_string + "->" + proposal_string + ")"));
    result.push_back(parse_expression(
            "(" + proposal_string + "->((" + proposal_string + "->" + proposal_string + ")->" +
            proposal_string + "))->(" + proposal_string + "->" + proposal_string + ")"));
    result.push_back(parse_expression("(" + proposal_string + "->((" + proposal_string + "->" + proposal_string + ")" + "->" + proposal_string + "))"));
}

void ProofDeduction::add_axiom_proof(Expression *expression, std::vector<Expression*> &result) {
    result.emplace_back(expression);
    result.emplace_back(parse_expression("(" + expression->to_string() + ")->((" + proposal_string + ")->(" + expression->to_string() + ")"));
}

void ProofDeduction::add_modus_ponens_proof(Expression *expression, std::vector<Expression*> &result) {
    std::pair<size_t, size_t> approves = proof_checker.get_modus_ponens(expression);
    std::string expression_string = expression->to_string();
    std::string alpha = proof_checker.expressions[approves.first]->to_string();
    std::string beta = proof_checker.expressions[approves.second]->to_string();
    std::string res = "(" + proposal_string + "->" + beta + ")->((" + proposal_string + "->(" + beta +
                      "->" + expression_string + "))->(" + proposal_string + "->" + expression_string + "))";
    result.emplace_back(parse_expression(res));
    res = "(" + proposal_string + "->(" + alpha + "))->(" + proposal_string + "->" + expression_string + ")";
    result.emplace_back(parse_expression(res));
}

void ProofDeduction::add_rule_exists_proof(Expression *expression, std::vector<Expression*> &result) {
    using Expressions::Implication;
    using Expressions::ExistsQuantifier;

    Implication *implication = reinterpret_cast<Implication*>(expression);
    std::string A = proposal_string;
    std::string B = reinterpret_cast<ExistsQuantifier*>(implication->left)->next->to_string();
    std::string C = implication->right->to_string();
    ProofDeduction proof_deduction = ProofDeduction();
    std::vector<Expression*> new_result;
    proof_deduction.add_header(A + "->" + B + "->" + C + ";" + B + ";" + A + "|-" + C);
    proof_deduction.add_proof(A + "->" + B + "->" + C, new_result);
    proof_deduction.add_proof(A, new_result);
    proof_deduction.add_proof(B + "->" + C, new_result);
    proof_deduction.add_proof(B, new_result);
    proof_deduction.add_proof(C, new_result);
    proof_deduction.clear();
    proof_deduction.add_header(A + "->" + B + "->" + C + ";" + B + "|-" + A + "->" + C);
    for (auto item : new_result) {
        proof_deduction.add_proof(item, result);
    }
    proof_deduction.clear();
    new_result.clear();
    B = reinterpret_cast<ExistsQuantifier*>(implication->left)->to_string();
    result.emplace_back(parse_expression(B + "->" + A + "->" + C));
    proof_deduction.add_header(B + "->" + A + "->" + C + ";"
                  + A + ";" + B + "|-" + C);
    proof_deduction.add_proof(B + "->" + A + "->" + C, new_result);
    proof_deduction.add_proof(B, new_result);
    proof_deduction.add_proof(A + "->" + C, new_result);
    proof_deduction.add_proof(A, new_result);
    proof_deduction.add_proof(C, new_result);
    proof_deduction.clear();
    proof_deduction.add_header(B + "->" + A + "->" + C + ";"
                  + A + "|-" + B + "->" + C);
    for (auto item : new_result) {
        proof_deduction.add_proof(item, result);
    }
}

void ProofDeduction::add_rule_forall_proof(Expression *expression, std::vector<Expression*> &result) {
    using Expressions::Implication;
    using Expressions::ForallQuantifier;

    Implication *implication = reinterpret_cast<Implication*>(expression);
    std::string A = proposal_string;
    std::string B = implication->left->to_string();
    std::string C = reinterpret_cast<ForallQuantifier*>(implication->right)->next->to_string();
    ProofDeduction proof_deduction = ProofDeduction();
    std::string AaB = "(" + A + "&" + B + ")";
    proof_deduction.add_header(A + "->" + B + "->" + C + ";" +
                  AaB + "|-" + C);
    proof_deduction.add_proof(AaB + "->" + A, result);
    proof_deduction.add_proof(AaB + "->" + B, result);
    proof_deduction.add_proof(AaB, result);
    proof_deduction.add_proof(A, result);
    proof_deduction.add_proof(B, result);
    proof_deduction.add_proof(A + "->" + B + "->" + C, result);
    proof_deduction.add_proof(B + "->" + C, result);
    proof_deduction.add_proof(C, result);
    C = implication->right->to_string();
    result.emplace_back(new Implication(parse_expression(AaB), implication->right));
    std::vector<Expression*> new_result;
    ProofDeduction proof_deduction_new = ProofDeduction();
    proof_deduction_new.add_header(AaB + "->" + C + ";" + A + ";" + B + "|-" + C);
    proof_deduction_new.add_proof(A + "->" + B + "->" + AaB, new_result);
    proof_deduction_new.add_proof(A, new_result);
    proof_deduction_new.add_proof(B, new_result);
    proof_deduction_new.add_proof(B + "->" + AaB, new_result);
    proof_deduction_new.add_proof(AaB, new_result);
    proof_deduction_new.add_proof(AaB + "->" + C, new_result);
    proof_deduction_new.add_proof(C, new_result);
    proof_deduction_new.clear();
    proof_deduction_new.add_header(AaB + "->" + C + ";" + A + "|-" + B + "->" + C);
    for (auto item : new_result) {
        proof_deduction_new.add_proof(item, result);
    }
}

void ProofDeduction::add_proof(Expression *expression, std::vector<Expression*> &result) {
    if (proof_checker.find_expression(expression) != -1) {
        return;
    }
    int axiom_index = proof_checker.get_axiom(expression);
    int arithmetic_axiom_index = proof_checker.get_arithmetic_axiom(expression);
    PredicateResult predicate_axiom_index = proof_checker.get_predicate_axiom(expression);
    int assumption_index = get_assumption(expression);
    PredicateResult predicate_rule_index = proof_checker.get_predicate_rule(expression);
    if (axiom_index != -1 || assumption_index != -1 || arithmetic_axiom_index != -1 || predicate_axiom_index.index != -1) {
        add_axiom_proof(expression, result);
    } else if (proposal->equals(expression)) {
        add_self_proof(result);
    } else if (proof_checker.get_modus_ponens(expression).second != static_cast<size_t>(-1)) {
        add_modus_ponens_proof(expression, result);
    } else if (predicate_rule_index.index != -1) {
        Expressions::Implication* implication = reinterpret_cast<Expressions::Implication*>(expression);
        if (check_class_inherity<Expressions::ExistsQuantifier>(*implication->left)) {
            add_rule_exists_proof(expression, result);
        } else {
            add_rule_forall_proof(expression, result);
        }
    }
    proof_checker.add_expression(expression);
    result.emplace_back(new Expressions::Implication(proposal, expression));
}

bool ProofDeduction::add_proof(const std::string &expression_string, std::vector<Expression*> &result) {
    Expression *eptr(parse_expression(expression_string));
    if (proposal->equals(eptr) ||
        get_assumption(eptr) != -1 ||
        proof_checker.get_arithmetic_axiom(eptr) != -1 ||
        proof_checker.get_axiom(eptr) != -1 || proof_checker.get_modus_ponens(eptr).first != static_cast<size_t>(-1)) {
        add_proof(eptr, result);
        return true;
    }
    PredicateResult a_predicate = proof_checker.get_predicate_axiom(eptr);
    if (a_predicate.index != -1) {
        if (a_predicate.error_occurred) {
            error_string = "Терм " + a_predicate.not_free_term->to_string() +
                     " не свободен для подстановки в " + a_predicate.formula->to_string() + "\n";
            return false;
        }
        if (std::find(proposal_free_variables.begin(), proposal_free_variables.end(), a_predicate.variable_name)
            != proposal_free_variables.end())
        {
            error_string = "Используется схема аксиом с квантором по переменной "
                     + a_predicate.variable_name + " входящей свободно в допущение" + proposal_string + "\n";
            return false;
        }
        add_proof(eptr, result);
        return true;
    }
    PredicateResult r_predicate = proof_checker.get_predicate_rule(eptr);
    if (r_predicate.index != -1) {
        if (r_predicate.error_occurred) {
            // правило вывода, входит свободно в часть
            error_string = "Переменная "
                     + r_predicate.variable_name + " входит свободно в формулу " + r_predicate.formula->to_string() + "\n";
            return false;
        }
        if (std::find(proposal_free_variables.begin(), proposal_free_variables.end(), r_predicate.variable_name)
            != proposal_free_variables.end()) {
            error_string = "Используется правило вывода с квантором по переменной "
                     + r_predicate.variable_name + " входящей свободно в допущение" + proposal_string + "\n";
            return false;
        }
        add_proof(eptr, result);
        return true;
    }
    std::cerr << "Proofment to nepravil'noe" << "\n";
    std::cerr << eptr->to_string() << "\n";
    return false;
}