#!/bin/bash
# Name: prl
#echo "called with :" $@
#
# preset variables

#----- read list of supported targets
#include "runtime/targets.inc"

if [ "${OpenPEARLTarget}" = "" ]
then
   PLATTFORM=linux
fi

DEBUG=0
SYSTEM="system.cc"
COMPILEONLY=0
StopBeforeCpp=0
RUN=
HELP=
INPUT=
imcInputFiles=
objects=
target=
PPONLY=0
noPP=0
PPFLAGS=""
    
standard="-std=OpenPEARL"
if [[ $PRLFLAGS =~ "-std=PEARL90" ]]; then
   standard="-std=PEARL90"
fi
if [[ $PRLFLAGS =~ "-std=OpenPEARL" ]]; then
   standard="-std=OpenPEARL"
fi

while [ "$1" != "" ] ; do
    #echo "input: " $INPUT
    case $1 in
        -std=OpenPEARL) standard=$1;                   shift;;
        -std=PEARL90)   standard=$1;                   shift;;
        -o)             target=$2;                     shift 2;;
        -b)             PLATTFORM=$2;                  shift 2;;
        -i)             INTERFACE=$2;                  shift 2;;
        -cc)            StopBeforeCpp=1;               shift;;
        -c)             COMPILEONLY=1;                 shift;;
	-I)             PPFLAGS+=" -I$2";              shift 2;;
	-D)             PPFLAGS+=" -D$2";              shift 2;;
	-U)             PPFLAGS+=" -U$2";              shift 2;;
	-E)             PPONLY=1;                      shift;;
	-nopp)          noPP=1;                        shift;;
        -r)             RUN=1;                         shift;;
        -v)             VERBOSE=1;                     shift;;
        -noimc)         noIMC=1;                       shift;;
        -nosys)         NOSYSTEMPART=1;                shift;;
        -h)             HELP=1;                        shift;;
        -keep)          keepFiles=1;                   shift;;
	 -*) echo "unknown option " $1; 
               shift;
	       exit 1;;
        *) INPUT="$INPUT $1";        shift;;
     esac
done

#echo "HELP=" $HELP 
#echo PLATFORM = $PLATFORM
#echo COMPILEONLY = $COMPILEONLY
#echo PPONLY = $PPONLY
#echo INPUT = $INPUT
#echo "StopBeforeCpp= " $StopBeforeCpp
#echo "PPFLAGS=" $PPFLAGS

if [ -n "$HELP" ]
then
#        ==============================================================================
   echo "Usage $0 [options] file file ..."
   echo "   Parameters: " 
   echo "    -std=OpenPEARL             (default) "
   echo "    -std=PEARL90               (disable OpenPEARL extensions)"
   echo "    -c                         compile only (until object and xml output)" 
   echo "    -o <outputFileName>        defines the name of the executable"
   echo "    -cc                        compile only until C++ source and xml"
   echo "                               (for developers)" 
   echo "    -keep                      keep intermediate files (for developers)"
   echo "    -b <plattform>             build for plattform (defaults to linux)" 
   echo "                               or OpenPEARLTarget environment variable" 
   echo "                               known plattforms: $plattforms"
   echo "    -D name                    Predefine name as a macro with a definition of 1"
   echo "    -D name=definition         Predefine name as a macro with value of definition"
   echo "    -E                         preprocess only"
   echo "    -I dir                     add the directory dir to the list of directories"
   echo "                               to be searched by the preprocessor"
   echo "    -r                         run application on MCU" 
   echo "    -i <interface>             download interface for MCU (default /dev/ttyUSB0)" 
   echo "    -noimc                     do not invoke the imc - current system.cc is used"
   echo "                               (for developers)"
#   echo "    -nosys do not invoke the imc - no system part is used"
   echo "    -U name                    Cancel a previous definition with -D name"
   echo "    -v                         verbose: print log file to console"
   echo "    -h                         this page" 
   echo "   Note: "
   echo "     * If only one input file (.prl, .cc or .o) is given, the name of binary output"
   echo "       is derived from the input file."
   echo "     * If more input files are given the name of the binary output is defines by "
   echo "       the -o option, which defaults to a.out "
   echo "   Example: "
   echo "     prl x.prl -c   : compile "
   echo "     prl x.prl      : compile and link"
   echo "     prl x.prl -r   : compile, link and run"
   echo "     prl x.cc       : compile previously created C++ source and link"
   echo "     prl -r x       : run application "
   exit 1
fi

#--------------
# search target platform
#echo Plattform: $PLATTFORM
#echo plattforms: $plattforms
found=0
for p in $plattforms; do
   if [ "$p" = "$PLATTFORM" ]
   then
      found=1;
   fi
done
if [ $found -eq 0 ]
then
   echo "unsupported target plattform: "$PLATTFORM;
   exit 1;
fi

#---------------------
#  check number of input files
nbr=0
for f in $INPUT; do
  ((nbr++))
done

if [[ "$nbr" -eq "0" ]] ; then
   echo "no input files"
   exit 1
elif [[ "$nbr" -eq "1" ]] ; then
  if [[ "$target" -eq "" ]] ; then
     # replace default output
     t=${INPUT%.*}
#echo "t=" $t
     target=${t// /}
  fi
else
  if [[ "$target" -eq "" ]] ; then
     # set default output
     target="a.out"
  fi
#  echo "target= " $target
fi

# ----- check input parameters and decide what to do
# --- result flags are prlcc, ccbin, runbin
# check option compatibility
if [[ "$COMPILEONLY" = "1" &&  "$StopBeforeCpp" = "1" ]] ; then
      echo "cannot specify -c and -cc simultaneously"
      exit 1
fi
if [[ "$COMPILEONLY" = "1" &&  "$RUN" = "1" ]] ; then
      echo "cannot specify -c and -r simultaneously"
      exit 1
fi
if [[ "$StopBeforeCpp" = "1" &&  "$RUN" = "1" ]] ; then
      echo "cannot specify -cc and -r simultaneously"
      exit 1
fi

#---------------------
# remove old .log .out files
rm -f $target.out $target.log

   # ---------------
   # stage 1 .prl->.cc/.xml/.o
   for inputFile in $INPUT; do
      #echo "-----------------------------  treat " $inputFile
      #echo "rm: " $intermediateFiles
      case ${inputFile##*/} in
	 (?*.*) inp_ext=${inputFile##*.};;
	 (*) inp_ext=;;
      esac
       
      if [ -z "$inp_ext" ]
      then
	  echo "terminate due to no file extension given."
	  exit -1
      fi
          
      fn=${inputFile%.*}
      targetDir="${inputFile%/*}"
      if [[ "$fn" = "$inp_ext" ]] ; then
         inp_ext=""
      fi

      outputPrlCC=""
   
     if [[ "$inp_ext" = "o" ]] ; then
#echo "treat object"
        objects="$objects $fn.o"
        imcInputFiles="$imcInputFiles $fn"
     elif [[ "$inp_ext" = "prl" ||  "$inp_ext" = "cc" ]] ; then
#echo "treat prl or cc"
         if [ "$inp_ext" = "prl" ] ; then
            #echo "PRL-> C++"
            if [[ "$StopBeforeCpp" = "1" ]] ; then
               outputPrlCC=$fn.cc 
            else
               outputPrlCC=`mktemp -t openpearl.XXXXXXX --suffix=".cc"` || exit 1
               intermediateFiles+=" $outputPrlCC"
            fi
	    imcoutput=$fn
	    
	    # --------------------
	    # run preprocessor
	    if [[ "$noPP" != "1" ]] ; then
		outputPrlPP=`mktemp -t openpearl.XXXXXXX --suffix=".prl"` || exit 1
		intermediateFiles+=" $outputPrlPP"
		echo ">>> /usr/local/bin/prlpp --quiet $PPFLAGS $fn.prl --output $outputPrlPP"
		/usr/local/bin/prlpp --quiet $PPFLAGS $fn.prl --output $outputPrlPP
		rc=$?
		if [ $rc -ne 0 ]
		then
		    echo "terminate due to errors in stage 1a (pp)" 1>>$fn.log
		    exit $rc
		fi
		prlccinput="/tmp/"`basename $outputPrlPP .prl`		
	    fi

            if [ $DEBUG -ne 0 ] ; then    
		echo ">>> prl: fn=" $fn 
		echo ">>> prl: prlccinput=" $prlccinput
		echo ">>> prl: prlccinput=" $prlccinput
	    fi
	    
            # echo $fn.prl " ---> " $outputPrlCC 
	    #include "compiler/prl_cc.inc"
	    
            rc=$?

            if [[ "$StopBeforeCpp" = "0" && "$COMPILEONLY" = "0" ]] ; then
               intermediateFiles+=" $fn.xml"
            fi 

            intermediateFiles+=" $fn.log $fn.out"

            if [ $rc -ne 0 ] ; then
              echo "terminate due to errors in stage 1" 1>>$fn.log
              exit $rc
            fi
            inp_ext="cc"
            if [ "$StopBeforeCpp" = "1" ] ; then
      	       continue;
            fi
         fi   # of if extension .prl
         if [ "$StopBeforeCpp" = "1" ] ; then
            continue;
         fi
   
         if [ "$inp_ext" = "cc" ] ; then
            imcInputFiles="$imcInputFiles $fn"
            #echo "C++-> .o"
            if [[ "$COMPILEONLY" = "1" ]] ; then
               outputCcO=$fn.o 
               objects="$objects $fn.o"
            else
               outputCcO=`mktemp -t openpearl.XXXXXXX --suffix=".o"` || exit 1
               linkerFiles+=" $outputCcO"
               intermediateFiles+=" $outputCcO"
               objects="$objects $outputCcO"
            fi
            if [[ "$outputPrlCC" = "" ]] ; then
               outputPrlCC=$fn.cc
            fi

            if [ $DEBUG -ne 0 ] ; then    
		echo "[100] prl: fn=" $fn 
		echo "[101] prl: outputPrlCC=" $outputPrlCC
		echo "[102] prl: outputCcO=" $$outputCcO 
	    fi
	    
   #         echo "cmd: " g++ -DTARGET=1 -c -Wall -g  -fstack-protector -pthread -O2 -fno-non-call-exceptions -DTARGET=1 -std=c++11    -I "/usr/local"/include  $outputPrlCC -o $outputCcO
   #
   #        rc=$?
   #include "runtime/cc_o.inc"
   
            if [ "$VERBOSE" = "1" ] ; then
               cat $fn.log
            fi
       
            if [ $rc -ne 0 ] ; then
               echo "compilation stage 2 terminated with errors"
               echo " --> check PEARL source code for problems"
               exit $rc
            fi
         fi  # of if treat extension cc
      else
         if [[ "$inp_ext" = "" ]] ; then
           target="$fn"
         else
            echo "cannot treat extension " $inp_ext
         fi
      fi
   done
   
   if [[ "$StopBeforeCpp" = "1" ||  "$COMPILEONLY" = "1" ]] ; then
      if [[ "$keepFiles" != "1" ]] ; then
         rm -f $intermediateFiles
      fi
      exit;
   fi
   	
   # --------------------
   # run imc
   if [[ "$noIMC" != "1" && "$imcInputFiles" != "" ]] ; then
       java -cp "/usr/local"/bin/imc.jar imc/main/InterModuleChecker $standard -b $PLATTFORM  -I "/usr/local" -o system.cc $imcInputFiles
   
       rc=$?
       if [ $rc -ne 0 ]
       then
          echo "terminate due to errors in stage 1b (imc)" 1>>$fn.log
          exit $rc
       fi
       intermediateFiles+=" system.cc"
       objects="$objects  system.cc"
   fi
   
   # ------------------------
   #if [ "$NOSYSTEMPART" != "1" ] ; then
   #echo "compile system.cc"
   # g++ -DTARGET=1 -c -Wall -g  -fstack-protector -pthread -O2 -fno-non-call-exceptions -DTARGET=1 -std=c++11    -I "/usr/local"/include  system.cc   >>system.out 2>>system.log
   #
   # rc=$?
   #         echo "cmd: " g++ -DTARGET=1 -c -Wall -g  -fstack-protector -pthread -O2 -fno-non-call-exceptions -DTARGET=1 -std=c++11    -I "/usr/local"/include  $outputPrlCC -o $outputCcO
   #	$objects ="$objects system.cc"
   #
   #     if [ "$VERBOSE" = "1" ] ; then
   #         cat system.log
   #     fi
   #    
   #     if [ $rc -ne 0 ]
   #     then
   #        echo "compilation stage 2 terminated with errors"
   #        echo " --> check PEARL source code for problems"
   #        exit $rc
   #     fi
   #fi
   
   #-------------------
   #echo link executable
   #echo "objects: " $objects
   #echo "target: " $target
   if [[ "$objects" != "" ]] ; then 
     # echo $objects " --> " $target
      #include "runtime/o_bin.inc"
   
      if [ "$VERBOSE" = "1" ] ; then
         cat $target.log
      fi
       
      if [ $rc -ne 0 ] ; then
         echo "linking terminated with errors"
         echo " --> check $target.log for problems"
         exit $rc
      fi
   fi

   if [[ "$keepFiles" != "1" ]] ; then
      rm -f $intermediateFiles
   fi


if [[ "$RUN" = "1" ]] ; then
   # -------------------------------
   # run executable
   #echo "run " $target
   #include "runtime/run_bin.inc"
   rc=$?

   if [ $rc -ne 0 ]; then
      echo "*** program terminated with error ($rc) ***"
   fi
fi
