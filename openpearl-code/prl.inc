#!/bin/sh
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
COMPILEONLY=
RUN=
HELP=
CONFIGELEMENT=

while [ "$1" != "" ] ; do
    case $1 in
        -b) PLATTFORM=$2;  shift 2;;
        -i) INTERFACE=$2;  shift 2;;
        -c) COMPILEONLY=1;        shift;;
        -r) RUN=1;                shift;;
        -v) VERBOSE=1;            shift;;
        -nosys) NOSYSTEMPART=1;   shift;;
        -h) HELP=1;        shift;;
        -add) CONFIGELEMENT=$2;  shift 2;;
	 *) if [ "$INPUT" != "" ] ; then
               echo "illegal parameter " $1; 
               shift;
	       exit 1;
            fi;
            INPUT=$1; shift;;
     esac
done

#echo "HELP=" $HELP 
#echo PLATFORM = $PLATFORM
#echo COMPLEONLY = $COMPLEONLY
#echo INPUT = $INPUT

if [ -n "$HELP" ]
then 
   echo "Usage $0 [parameters] file"
   echo "   Parameters: " 
   echo "    -c compile only until C++ source" 
   echo "    -b <plattform> build for plattform (defaults to linux)" 
   echo "        or OpenPEARLTarget environment variable" 
   echo "        known plattforms: $plattforms"
   echo "    -r run application on MCU" 
   echo "    -i <interface> download interface for MCU (default /dev/ttyUSB0)" 
   echo "    -nosys do not invoke the imc - no system part is used"
   echo "    -add <configuration> add configuration element by imc"
   echo "    -v verbose: prints c++ compiler output to console"
   echo "    -h this page" 
   echo "   Example: "
   echo "     prl x.prl -c   : compile only until C++ source"
   echo "     prl x.prl      : compile and link"
   echo "     prl x.prl -r   : compile, link and run"
   echo "     prl x.cc       : compile previously created C++ source and link"
   echo "     prl -r x       : run application "
   exit 1
fi

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

inp_ext=${INPUT##*.}
fn=${INPUT%.*}
  
#echo ext: $inp_ext
#echo fn: $fn 

# ----- check input parameters and decide what to do
# --- result flags are prlcc, ccbin, runbin
if [ "$COMPILEONLY" = "1" ] ; then
   if [ "$RUN" = "1" ] ; then
      echo "cannot specify -c and -r simultaneously"
      exit 1
   fi
   if [ "$inp_ext" != "prl" ] ; then
      echo " option -c requires pearl-source"
      exit 1
   fi
fi

if [ "$inp_ext" = "prl" ] ; then
   prlcc=1
   if [ "$COMPILEONLY" = "1" ] ; then
      ccbin=0
      runbin=0
   else
      ccbin=1
   fi
   if [ "$RUN" = "1" ] ; then
      runbin=1
   fi
fi

if [ "$inp_ext" = "cc" ] ; then
   prlcc=0
   ccbin=1
   if [ "$RUN" = "1" ] ; then
      runbin=1
   fi
fi

if [ "$inp_ext" = "$fn" ] ; then
   prlcc=0
   ccbin=0
   if [ "$RUN" = "1" ] ; then
      runbin=1
   else 
      echo " option -r needs prl, cc or application"
      exit 1
   fi
fi
# -------------
#echo "prlcc" $prlcc
#echo "ccbin" $ccbin
#echo "runbin" $runbin

# --------------------
# evaluate flags and build the desired targets

if [ "$prlcc" = "1" ] ; then 
#   echo "PRL-> C++"
 #include "compiler/prl_cc.inc"
 rc=$?
 if [ $rc -ne 0 ]
 then
  echo "terminate due to errors in stage 1"
  exit $rc
 fi
fi

if [ "$COMPILEONLY" = "1" ] ; then
   exit 0
fi

if [ "$NOSYSTEMPART" != "1" ] ; then
#   echo "run IMC"
  SYSTEM="system.cc"
  #include "imc3/imc.inc"
  rc=$?
  if [ $rc -ne 0 ]
  then
    echo "terminate due to errors in stage 1b (imc)"
    exit $rc
  fi
fi

if [ "$ccbin" = "1" ] ; then 
  #  echo "C++->BIN"
#include "runtime/cc_bin.inc"

  if [ "$VERBOSE" = "1" ] ; then
      cat $fn.log
  fi
    
  if [ $rc -ne 0 ]
  then
     echo "compilation stage 2 terminated with errors"
     echo " --> check PEARL source code for problems"
     exit $rc
  fi
fi

if [ "$runbin" = "1" ] ; then 
   #include "runtime/run_bin.inc"
   if [ $rc -ne 0 ]; then
      echo "*** program terminated with error ($rc) ***"
   fi
fi
