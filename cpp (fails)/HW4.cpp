#include <cassert>
#include <iostream>
#include <map>
#include <set>
#include <sstream>
#include <algorithm>

#include "expressions/Expressions.h"
#include "expressions/Parser.h"
#include "ProofChecker.h"
#include "ProofDeduction.h"

using namespace Expressions;

struct HW4 {
    HW4() { }

    void run(const char *input, const char *output) {
        assert(freopen(input, "r", stdin));
        assert(freopen(output, "w", stdout));
        std::ios_base::sync_with_stdio(false);
        std::cin.tie(0);

        std::string statement;
        getline(std::cin, statement);
        ProofDeduction::get_instance()->add_header(statement);
        std::vector<Expression*> result;
        int counter = 0;
        while (getline(std::cin, statement)) {
            if (!ProofDeduction::get_instance()->add_proof(statement, result)) {
                std::cout << "Вывод некорректен начиная с формулы номер " << counter + 1 << "\n";
                std::cout << ProofDeduction::get_instance()->get_error() << "\n";
                return;
            }
            counter++;
        }
        for (auto item : result) {
            std::cout << Utils::replace(item->to_string(), ">", "->") << "\n";
        }
    }

    void run() {
        run("tests/input.txt", "tests/output.txt");
    }
};