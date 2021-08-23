#!/bin/bash

prlfiles=`find . -maxdepth 10 -name "*.prl"`
prlfiles=$(echo $prlfiles | xargs -n1 | sort | xargs)
echo "files to check:"
echo $prlfiles
echo "---------"

../../errorChecker/runTest.sh $prlfiles
if [ $? -ne 0 ]; then
   exit 1
fi 
