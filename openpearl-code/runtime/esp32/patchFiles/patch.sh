#! /bin/bash

# call this function with esp-idf-path file to check
# if the original file is detected - the file becomes copies to $1/$2.original
function testAndReplace {
   diff $1/$2 $2.original 1>/dev/null
   rc1=$?
   diff $1/$2 $2.modified 1>/dev/null
   rc2=$?
   if [ $rc1 -eq 0 ] && [ $rc2 -ne 0 ]; then
      echo $2: original file in esp-idf
      cp $1/$2 $1/$2.original
      cp $2.modified $1/$2
   fi
   if [ $rc1 -ne 0 ] && [ $rc2 -eq 0 ]; then
      echo $2: modified file in esp-idf
   fi
   if [ $rc1 -ne 0 ] && [ $rc2 -ne 0 ]; then
      echo $2: file differs from original and modified version
      echo please compare the versions and update $2.original
      exit 1
   fi
}

if [ "$1" == "" ]
then
   echo "call with pathname to esp-idf"
   exit 1
fi

espidf=$1/components/freertos
testAndReplace $espidf tasks.c
testAndReplace $espidf timers.c
testAndReplace $espidf/include/freertos task.h 
testAndReplace $espidf/include/freertos timers.h 
testAndReplace $1/components/freertos/xtensa/include/freertos FreeRTOSConfig.h
