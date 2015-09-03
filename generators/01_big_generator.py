import sys
import time

PREFIX_TO_TESTS = "../tests/"

start_time = time.time()
test_file = open(PREFIX_TO_TESTS + "01_big.in", "w")
for i in range(5000):
    test_file.write("A->" * 1000 + "A\n")
test_file.close()
finish_time = time.time()
print("Elapsed generation time: " + str(finish_time - start_time))
