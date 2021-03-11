#!/bin/bash

prlfiles=`find . -maxdepth 10 -name "*.prl"`
echo "files to check:"
echo $prlfiles
echo "---------"

../../errorChecker/runTest.sh $prlfiles
if [ $? -ne 0 ]; then
   exit 1
fi 
