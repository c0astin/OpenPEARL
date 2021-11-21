#!/bin/bash
# parameters actioa platform tests
# action 0: build all given tests without error result
#        1: build or run successful with error return code
#           if at least one test fails 
#        2: does not build/run
# platform is a platform name like linux
# test is the prefix number of the test files
# e.g.
# ./run-compile.sh 0 linux 023 024
# try to build 023*.prl  and 024*.prl to targert 023 and 024

PARAM=$1
shift
TARGET=$1
shift
TESTS=$*

nooftests=0
passed=0
failed=0

printf "\nTarget: %s\n" $TARGET
printf "===========================\n"
printf "%-10s :    prl->c++      prl->bin\n" "program"
for test in $TESTS
do
	nooftests=$(($nooftests + 1))
        files=`ls $test*.prl`
        printf "%-10s : %s" "$test " 
	for f in $files ;  do
           printf "%s " "$f"
        done
        printf "\n"

	prl -cc -b $TARGET $test*.prl  >$test.out 2>&1
	rc=$?
     
        base=`basename $test .prl`
	prl -b $TARGET $test*.cc  -o $test 1> /dev/null 2>&1
	rcbin=$?

	if [ $rc -eq 0 ]
	then
  		printf "%-10s :     PASSED     " " " 
                if [ $rcbin -eq 0 ]
                then
		   passed=$(($passed + 1))
     		   printf "    PASSED\n" 
                else
     		   printf "*** FAILED ***\n" 
		   failed=$(($failed + 1))
                fi
	else
		failed=$(($failed + 1))
  		printf "%-10s : *** FAILED ***\n" " "
	fi
done

msg=$(printf "Result: no of tests/passed/failed: %d/%d/%d\n" "$nooftests" "$passed" "$failed")
printf "$msg"

if [ $PARAM -eq 0 ]; then
    if [ -x "$(command -v notify-send)" ]; then
	notify-send -u normal "OpenPEARL: $msg"
    fi
    
    exit 0
fi

if [ $PARAM -eq 1 ]; then
   if [ $failed -ne 0 ]; then
       echo "at least 1 program did not compile"

       if [ -x "$(command -v notify-send)" ]; then
	   notify-send -u critical "OpenPEARL: at least 1 program did not compile"
       fi
      exit 1
   fi
   exit 0
fi

if [ $PARAM -eq 2 ]; then
   if [ $passed -ne 0 ]; then
      echo "at least 1 program did compile"

      if [ -x "$(command -v notify-send)" ]; then
	  notify-send -u critical "OpenPEARL: at least 1 program did not compile"
      fi
      exit 1
   fi
   exit 0
fi
echo "how did you reach this line?"
exit 3

