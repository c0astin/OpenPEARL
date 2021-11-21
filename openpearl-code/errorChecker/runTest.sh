#!/bin/bash
#echo "***************"
path=`dirname $(realpath $0)`
#echo $path

passed=0
failed=0 
while [ $1 ]
do
#    echo "****" treat $1
   files=`ls $1*.prl`
   # echo $files
   allExpectationsOk=1
   for f in $files ; do
   # echo "treat $f"
      $path/genExpectations $f 1>/dev/null
      rc=$?
      basename=${f%.*}
      if [ $rc -ne 0 ] ; then
         printf "%-40s :  problems in annotation definition\n" $1
         rm -f $basename.exp 
         allExpectationsOk=0
         failed=$(($failed +1))
      else
         cat $basename.exp >> $1.exp
      fi
   done
   if [[ "$allExpectationsOk" -eq "1" ]] ; then
      # if more sources are possible, we must think about a new
      # calling mechanism with all required input files
      # precompiled -  or not
      prl $1*.prl 2> $1.err 1>/dev/null
   #   echo "prl $1 done"
      $path/errorParser $files <$1.err 1>/dev/null
      rc=$?
   #   echo "errorParser $1 done"
      if [ $rc -ne 0 ] ; then
         printf "%-40s :  annotatations failed\n" $1
         failed=$(($failed +1))
         failedFiles="${failedFiles}\n\t$1"
      else 
         printf "%-40s :  all expected errors detected\n" $1 
         passed=$(($passed +1))
      fi
    fi
    #echo "clean files"
    rm -f system.cc $1.err $1.exp $1 $1.out
    for f in $files ; do
       basename=${f%.*}
       rm -f $basename $basename.cc $basename.exp  $basename.err $basename.xml $basename.log $basename.out
    done

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
