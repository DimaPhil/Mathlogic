#include "HW1.cpp"
#include "HW2.cpp"
#include "HW3.cpp"
#include "HW4.cpp"

#include <ctime>

const std::string FILE_PREFIX = "/home/dmitry/Documents/work/git/Mathlogic/";

int main() {
    //HW1 homework1;
    //homework1.run((FILE_PREFIX + "tests/01_big.in").c_str(), (FILE_PREFIX + "tests/01_big.out").c_str());

    //HW2 homework2;
    //homework2.run((FILE_PREFIX + "tests/02.in").c_str(), (FILE_PREFIX + "tests/02.out").c_str());

    HW3 homework3;
    homework3.run((FILE_PREFIX + "tests/03.in").c_str(), (FILE_PREFIX + "tests/03.out").c_str());

    //HW4 homework4;
    //homework4.run((FILE_PREFIX + "tests/04.in").c_str(), (FILE_PREFIX + "tests/04.out").c_str());

    fprintf(stderr, "%.2lf\n", (double)clock() / CLOCKS_PER_SEC);
    return 0;
}