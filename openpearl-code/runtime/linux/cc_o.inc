g++ -Wall -g  -fstack-protector -fexceptions -pthread -O2 -fno-non-call-exceptions -DTARGET=1 -std=c++11    -I "/usr/local"/include \
   -c $outputPrlCC -o $outputCcO >>$fn.out 2>>$fn.log
