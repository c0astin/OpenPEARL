#!/bin/bash

cat <<EOF > 026_arith.prl
MODULE(arith);

SYSTEM;
  so: StdOut;	! Terminal Ausgabe bei OpenPEARL

PROBLEM;	
  SPC so DATION OUT SYSTEM ALPHIC;
  DCL stdOut DATION OUT ALPHIC DIM(*,80) FORWARD CREATED (so);
  DCL cres    FIXED(31);

  __cpp__("int tn = 0;");
  __cpp__("int tp = 0;");
  __cpp__("int tf = 0;");	

printHeader: PROC ;
    PUT 'Expression				         Expected           Calc'         TO stdOut BY A,SKIP;
    PUT '--------------------------------------------------------------------------------' TO stdOut BY A,SKIP;	

END;

printFooter: PROC ;
    PUT '--------------------------------------------------------------------------------' TO stdOut BY A,SKIP;	
    __cpp__("_cres.x =tn ;");
    PUT 'Numner of tests: ', cres TO stdOut BY A(40),F(10),SKIP;	    __cpp__("_cres.x =tn ;");
    __cpp__("_cres.x =tp ;");
    PUT '         Passed: ', cres TO stdOut BY A(40),F(10),SKIP;	
    __cpp__("_cres.x =tf ;");	  
    PUT '         Failed: ', cres TO stdOut BY A(40),F(10),SKIP;	
END;

checkAndPrint: PROC (expr CHAR(40), res FIXED, eres FIXED);
__cpp__("tn++;");	  
IF res == eres THEN
    __cpp__("tp++;");	
    PUT expr, '       ', eres , '     ', res, '       C' TO stdOut BY A,A,F(10),A,F(10),A,SKIP;	
  ELSE
    __cpp__("tf++;");	
    PUT expr, '       ', eres , '     ', res, '       F <---' TO stdOut BY A,A,F(10),A,F(10),A,SKIP;	
  FIN;
END;

main: TASK MAIN;
  DCL i1      FIXED(31);
  DCL i2      FIXED(31)  INIT(3);
  DCL i3      FIXED(31)  INIT(5);
  DCL i4      FIXED(31)  INIT(7);
  DCL i5      FIXED(31)  INIT(11);
  DCL i6      FIXED(31)  INIT(13);
  DCL eres    FIXED(31);

  OPEN stdOut;

  printHeader;
EOF

# number of expressions:
ec=100

for en in $(seq 1 $ec)
do
    # number of terms:
    tc=$((2 + $RANDOM % 4))

    # first term:
    num=$((1 + $RANDOM % 100))

    pexpr="$num"
    cexpr="$num"

    pvexpr="i2"
    cvexpr="_i2.x"

    for n in $(seq 2 $tc)
    do
	num=$((1 + $RANDOM % 20))
	op=$((1 + $RANDOM % 3))
	neg=$((1 + $RANDOM % 10))

	case $op in
	    1) 
		pexpr="${pexpr} +"
		cexpr="${cexpr} +"
		pvexpr="${pvexpr} +"
		cvexpr="${cvexpr} +"

		;;
	    2)
		pexpr="${pexpr} -"
		cexpr="${cexpr} -"
		pvexpr="${pvexpr} -"
		cvexpr="${cvexpr} -"
 		;;
	    3)
		pexpr="${pexpr} *"
		cexpr="${cexpr} *"
		pvexpr="${pvexpr} *"
		cvexpr="${cvexpr} *"
		;;
	    4)
		pexpr="${pexpr} //"
		cexpr="${cexpr} /"
		pvexpr="${pvexpr} //"
		cvexpr="${cvexpr} /"
		;;
	esac

	if [ $neg -lt 25 ]
	then
	    pexpr="${pexpr} ${num}"
	    cexpr="${cexpr} ${num}"
    	    pvexpr="${pvexpr} i${n}"
	    cvexpr="${cvexpr} _i${n}.x"
	else
   	    pexpr="${pexpr} -${num}"
	    cexpr="${cexpr} -${num}"
	    pvexpr="${pvexpr} -i${n}"
	    cvexpr="${cvexpr} -_i${n}.x"
	fi
    done

    echo "    i1 = ${pexpr};"  >> 026_arith.prl
    echo "    __cpp__(\"_eres.x = (${cexpr});\");" >> 026_arith.prl
    echo "    checkAndPrint('${pexpr}', i1, eres);" >> 026_arith.prl

#    echo "    i1 = ${pvexpr};"  >> 026_arith.prl
#    echo "    __cpp__(\"_eres.x = (${cvexpr});\");" >> 026_arith.prl
#    echo "    checkAndPrint('${pvexpr}', i1, eres);" >> 026_arith.prl
done

cat <<EOF >> 026_arith.prl
  printFooter;

  CLOSE stdOut;
  __cpp__("pearlrt::Control::setExitCode(tf!=0);");
END;

MODEND;
EOF
