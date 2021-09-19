#!/bin/bash
#echo "***************"
path=`dirname $(realpath $0)`
#echo $path

passed=0
failed=0 
while [ $1 ]
do
#   echo "****" treat $1
   filename=$1
   basename=${filename%.*}
   $path/genExpectations $filename 1>/dev/null
   rc=$?
   if [ $rc -ne 0 ] ; then
      printf "%-40s :  problems in annotation definition\n" $1
      rm -f $basename.exp 
      failed=$(($failed +1))
   else
      # if more sources are possible, we must think about a new
      # calling mechanism withh all required input files
      # precompiled -  or not
      prl $1 2> $basename.err 1>/dev/null
#      echo "prl $1 done"
      $path/errorParser $basename.prl <$basename.err 1>/dev/null
      rc=$?
#      echo "errorParser $1 done"
      if [ $rc -ne 0 ] ; then
         printf "%-40s :  annotatations failed\n" $1
         failed=$(($failed +1))
         failedFiles="${failedFiles}\n\t$1"
      else 
         printf "%-40s :  all expected errors detected\n" $1 
         passed=$(($passed +1))
      fi
      #echo "clean files"
      rm -f $basename $basename.cc $basename.exp  $basename.err $basename.xml $basename.log system.cc
   fi

   shift
#   echo "next file:" $1
done

if [ $failed -ne 0 ]; then
   printf "%d files failed -- %d files passed\n" $failed $passed
   printf "failed:  ${failedFiles}\n"
   exit 1
else
   printf "%d files passed\n" $passed
   exit 0
fi
