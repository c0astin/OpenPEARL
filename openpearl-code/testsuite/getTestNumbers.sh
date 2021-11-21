#!/bin/bash
# get list of test numbers
# tests may consist of several files with the same starting number
# the numbers must consist of 3 digits
#files=`ls [0-9][0-9][0-9]*.prl`
numbers=()

for f in $* 
do
   num=${f:0:3}
   #   echo $num
   if [[ ! " ${numbers[*]} " =~ " $num " ]]; then
      numbers+=($num)
   fi
done
echo ${numbers[@]}

