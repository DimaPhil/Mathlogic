cmake_minimum_required(VERSION 3.3)
project(Mathlogic)

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++14 -pedantic -Wall -O2 -Werror")

set(SOURCE_FILES Utils.cpp)

set(EXPRESSIONS_FILES expressions/Expressions.cpp)
set(EXPRESSIONS_FILES ${EXPRESSIONS_FILES} expressions/Parser.cpp)
set(EXPRESSIONS_FILES ${EXPRESSIONS_FILES} expressions/ParserFormalArithmetic.cpp)
set(EXPRESSIONS_FILES ${EXPRESSIONS_FILES} expressions/Variable.cpp)
set(EXPRESSIONS_FILES ${EXPRESSIONS_FILES} expressions/Not.cpp)
set(EXPRESSIONS_FILES ${EXPRESSIONS_FILES} expressions/Implication.cpp)
set(EXPRESSIONS_FILES ${EXPRESSIONS_FILES} expressions/And.cpp)
set(EXPRESSIONS_FILES ${EXPRESSIONS_FILES} expressions/Or.cpp)
set(EXPRESSIONS_FILES ${EXPRESSIONS_FILES} expressions/Equality.cpp)
set(EXPRESSIONS_FILES ${EXPRESSIONS_FILES} expressions/Add.cpp)
set(EXPRESSIONS_FILES ${EXPRESSIONS_FILES} expressions/Multiply.cpp)
set(EXPRESSIONS_FILES ${EXPRESSIONS_FILES} expressions/ForallQuantifier.cpp)
set(EXPRESSIONS_FILES ${EXPRESSIONS_FILES} expressions/ExistsQuantifier.cpp)
set(EXPRESSIONS_FILES ${EXPRESSIONS_FILES} expressions/Predicate.cpp)

set(HEADER_FILES expressions/Expressions.h expressions/Parser.h expressions/ParserFormalArithmetic.h Utils.h)
set(MAIN_FILES main.cpp HW1.cpp HW2.cpp HW3.cpp HW4.cpp
               ProofChecker.h ProofChecker.cpp ProofDeduction.h ProofDeduction.cpp)
add_executable(Mathlogic ${HEADER_FILES} ${SOURCE_FILES} ${EXPRESSIONS_FILES} ${MAIN_FILES})