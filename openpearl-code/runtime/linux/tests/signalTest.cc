#include <PearlIncludes.h>

namespace ns_signalTest {
/////////////////////////////////////////////////////////////////////////////
// PROLOGUE
/////////////////////////////////////////////////////////////////////////////


static const char* filename = (char*) "signalTest.prl";



/////////////////////////////////////////////////////////////////////////////
// CONSTANT POOL
/////////////////////////////////////////////////////////////////////////////
static /*const*/ pearlrt::Fixed<1>         CONST_FIXED_P_0_1(0);
static /*const*/ pearlrt::Fixed<2>         CONST_FIXED_P_3_2(3);
static /*const*/ pearlrt::Fixed<3>         CONST_FIXED_P_4_3(4);
static /*const*/ pearlrt::Fixed<2>         CONST_FIXED_P_2_2(2);
static /*const*/ pearlrt::Fixed<1>         CONST_FIXED_P_1_1(1);
static /*const*/ pearlrt::Fixed<3>         CONST_FIXED_P_5_3(5);
static /*const*/ pearlrt::Fixed<4>         CONST_FIXED_P_10_4(10);
static /*const*/ pearlrt::Fixed<3>         CONST_FIXED_P_6_3(6);
static /*const*/ pearlrt::Fixed<4>         CONST_FIXED_P_11_4(11);
static /*const*/ pearlrt::Fixed<4>         CONST_FIXED_P_12_4(12);
static /*const*/ pearlrt::Fixed<7>         CONST_FIXED_P_100_7(100);
static /*const*/ pearlrt::Fixed<31>         CONST_FIXED_P_0_31(0);
static /*const*/ pearlrt::Fixed<31>         CONST_FIXED_P_1_31(1);
static /*const*/ pearlrt::Fixed<31>         CONST_FIXED_N_1_31(-1);
static /*const*/ pearlrt::Fixed<7>         CONST_FIXED_P_80_7(80);
static /*const*/ pearlrt::Fixed<7>         CONST_FIXED_P_0_7(0);
static /*const*/ pearlrt::Fixed<7>         CONST_FIXED_P_1_7(1);
static /*const*/ pearlrt::Character<11>         CONST_CHARACTER_30f9cc35_6b41_493d_b86e_d3f26b5b816f("starting p=");
static /*const*/ pearlrt::Character<15>         CONST_CHARACTER_3e0fcb4a_c6d1_47da_84ad_e9a5dc676a74("--- restart ---");
static /*const*/ pearlrt::Character<19>         CONST_CHARACTER_58c4bc6e_cf9d_440a_94f5_5d2b897dd9b6("proc x: got signal ");
static /*const*/ pearlrt::Character<29>         CONST_CHARACTER_d8668b7b_8dd5_4a83_a12b_87c34b8103a5(" arithmetic error (returning)");
static /*const*/ pearlrt::Character<36>         CONST_CHARACTER_cf949c68_f962_4202_a354_eb812b800418("proc x: Overflow occured (returning)");
static /*const*/ pearlrt::Character<30>         CONST_CHARACTER_2f5a893f_3ea4_4c74_bbaa_1542ba36b733("Overflow occured (terminating)");
static /*const*/ pearlrt::Character<36>         CONST_CHARACTER_caca90b0_96aa_4e62_a3be_f9f8bf43a479("PROC X: divide by 0 (restarting)  p=");
static /*const*/ pearlrt::Character<27>         CONST_CHARACTER_acc45d92_c451_4517_803e_0e3e5021f91b("div0-handler: do a new div0");
static /*const*/ pearlrt::Character<35>         CONST_CHARACTER_e614fc5d_8cde_4c75_942d_81e6418b8493("start looping until overflow occurs");
static /*const*/ pearlrt::Character<10>         CONST_CHARACTER_186a7388_e7ad_4f67_887e_49d01fcc3c79("t1 started");
static /*const*/ pearlrt::Character<29>         CONST_CHARACTER_a57c8efb_067b_4232_8eea_8919a2a0d56b("t1 got signal --> terminating");
static /*const*/ pearlrt::Character<17>         CONST_CHARACTER_067d8f62_0dff_429a_9087_5ddde9c6f203("ON arith finished");
static /*const*/ pearlrt::Character<14>         CONST_CHARACTER_1f18d3d7_e233_4db4_ae93_bf23287ec4a9("t1 : CALL x(0)");

/////////////////////////////////////////////////////////////////////////////
// TASK SPECIFIERS
/////////////////////////////////////////////////////////////////////////////
extern pearlrt::Task task_t1;

}



/////////////////////////////////////////////////////////////////////////////
// STRUCTURE FORWARD DECLARATIONS
/////////////////////////////////////////////////////////////////////////////

/////////////////////////////////////////////////////////////////////////////
// STRUCTURE DECLARATIONS
/////////////////////////////////////////////////////////////////////////////

namespace ns_signalTest {


/////////////////////////////////////////////////////////////////////////////
// SYSTEM PART
/////////////////////////////////////////////////////////////////////////////

/////////////////////////////////////////////////////////////////////////////
// PROBLEM PART
/////////////////////////////////////////////////////////////////////////////

/////////////////////////////////////////////////////////////////////////////
// ARRAY DESCRIPTORS
/////////////////////////////////////////////////////////////////////////////


/////////////////////////////////////////////////////////////////////////////
// SIGNAL SPECIFICATIONS
/////////////////////////////////////////////////////////////////////////////
extern pearlrt::Signal *_div0;


extern pearlrt::Signal *_arith;


extern pearlrt::Signal *_overflow;



/////////////////////////////////////////////////////////////////////////////
// PROCEDURE SPECIFICATIONS
/////////////////////////////////////////////////////////////////////////////
static void _x(pearlrt::Task *me, pearlrt::Fixed<15> );

 


/////////////////////////////////////////////////////////////////////////////
// DATION SPECIFICATIONS
/////////////////////////////////////////////////////////////////////////////
extern pearlrt::SystemDationNB * _so; 



/////////////////////////////////////////////////////////////////////////////
// DATION DECLARATIONS
/////////////////////////////////////////////////////////////////////////////
static pearlrt::DationPG *_to;
 

/////////////////////////////////////////////////////////////////////////////
// SYSTEM DATION INITIALIZER
/////////////////////////////////////////////////////////////////////////////
static void setupDationsAndPointers() {
   static pearlrt::DationDim2 h_dim_to(80); 
   static pearlrt::DationPG d_to(_so, pearlrt::Dation::OUT  | pearlrt::Dation::FORWARD | pearlrt::Dation::NOCYCL | pearlrt::Dation::STREAM, &h_dim_to);
   _to = &d_to;




} 

static pearlrt::Control::Initializer init={setupDationsAndPointers,NULL};
static int dummy = pearlrt::Control::addInitializer(&init);

/////////////////////////////////////////////////////////////////////////////
// VARIABLE DECLARATIONS
/////////////////////////////////////////////////////////////////////////////






/////////////////////////////////////////////////////////////////////////////
// CONSTANT SEMAPHORE ARRAYS
/////////////////////////////////////////////////////////////////////////////

/////////////////////////////////////////////////////////////////////////////
// CONSTANT BOLT ARRAYS
/////////////////////////////////////////////////////////////////////////////

/////////////////////////////////////////////////////////////////////////////
// PROCEDURE DECLARATIONS
/////////////////////////////////////////////////////////////////////////////
static void
_x(pearlrt::Task *me, pearlrt::Fixed<15> _p)
{
    const char* locationFile = me->getLocationFile();
    int locationLine = me->getLocationLine(); 
    pearlrt::Fixed<31>  _k ;
    pearlrt::Fixed<31>  _errno (CONST_FIXED_P_0_1);

     
    // SIGNAL REACTION FRAME 
    // signal handling data
    int indexOfSignalAction = -1; 
    pearlrt::SignalAction sigActions[4];
    pearlrt::ScheduledSignalActions scheduledSignalActions(4, sigActions);

    tryAgain:
    try {
       switch (indexOfSignalAction) {
          case -1: break;


    	   case 1 : 
    	       // BLOCK BEGIN 
    	       {
    	       me->setLocation(32, "signalTest.prl");
    	       // put statement 
    	       {
    	          static pearlrt::IOFormatEntry formatEntries[]  = {
    	         {
    	            .format=pearlrt::IOFormatEntry::A
    	         },
    	         {
    	            .format=pearlrt::IOFormatEntry::F,
    	            .fp1={.f31=CONST_FIXED_P_4_3},
    	            .fp2={.f31=0}
    	         },
    	         {
    	            .format=pearlrt::IOFormatEntry::A
    	         },
    	         {
    	            .format=pearlrt::IOFormatEntry::SKIP,
    	            .fp1={.f31=CONST_FIXED_P_1_31}
    	         }
    	       };


    	          pearlrt::IODataEntry dataEntries[]  = {
    	            {
    	               .dataType={pearlrt::IODataEntry::CHAR,19},
    	               .dataPtr={.outData=&CONST_CHARACTER_58c4bc6e_cf9d_440a_94f5_5d2b897dd9b6},
    	               .param1={.numberOfElements = 1},
    	            },
    	            {
    	               .dataType={pearlrt::IODataEntry::FIXED,31},
    	               .dataPtr={.outData=&_errno},
    	               .param1={.numberOfElements = 1},
    	            },
    	            {
    	               .dataType={pearlrt::IODataEntry::CHAR,29},
    	               .dataPtr={.outData=&CONST_CHARACTER_d8668b7b_8dd5_4a83_a12b_87c34b8103a5},
    	               .param1={.numberOfElements = 1},
    	            }
    	          };

    	          pearlrt::IODataList dataList = {
    	            .nbrOfEntries=sizeof(dataEntries)/sizeof(dataEntries[0]),
    	            .entry=dataEntries};
    	         static pearlrt::IOFormatList formatList = {
    	            .nbrOfEntries=sizeof(formatEntries)/sizeof(formatEntries[0]),
    	            .entry=formatEntries};
    	         _to->put(me, &dataList , &formatList);
    	       }


    	       me->setLocation(33, "signalTest.prl");
    	       	   me->setLocation(locationLine,locationFile);
    	       	   return;





    	       }
    	       // BLOCK END 

    	       break;
    	   case 2 : 
    	       // BLOCK BEGIN 
    	       {
    	       me->setLocation(41, "signalTest.prl");
    	       // put statement 
    	       {
    	          static pearlrt::IOFormatEntry formatEntries[]  = {
    	         {
    	            .format=pearlrt::IOFormatEntry::A
    	         },
    	         {
    	            .format=pearlrt::IOFormatEntry::SKIP,
    	            .fp1={.f31=CONST_FIXED_P_1_31}
    	         }
    	       };


    	          pearlrt::IODataEntry dataEntries[]  = {
    	            {
    	               .dataType={pearlrt::IODataEntry::CHAR,36},
    	               .dataPtr={.outData=&CONST_CHARACTER_cf949c68_f962_4202_a354_eb812b800418},
    	               .param1={.numberOfElements = 1},
    	            }
    	          };

    	          pearlrt::IODataList dataList = {
    	            .nbrOfEntries=sizeof(dataEntries)/sizeof(dataEntries[0]),
    	            .entry=dataEntries};
    	         static pearlrt::IOFormatList formatList = {
    	            .nbrOfEntries=sizeof(formatEntries)/sizeof(formatEntries[0]),
    	            .entry=formatEntries};
    	         _to->put(me, &dataList , &formatList);
    	       }


    	       me->setLocation(42, "signalTest.prl");
    	       	   me->setLocation(locationLine,locationFile);
    	       	   return;





    	       }
    	       // BLOCK END 

    	       break;
    	   case 3 : 
    	       // BLOCK BEGIN 
    	       {
    	       me->setLocation(49, "signalTest.prl");
    	       // put statement 
    	       {
    	          static pearlrt::IOFormatEntry formatEntries[]  = {
    	         {
    	            .format=pearlrt::IOFormatEntry::A
    	         },
    	         {
    	            .format=pearlrt::IOFormatEntry::SKIP,
    	            .fp1={.f31=CONST_FIXED_P_1_31}
    	         }
    	       };


    	          pearlrt::IODataEntry dataEntries[]  = {
    	            {
    	               .dataType={pearlrt::IODataEntry::CHAR,30},
    	               .dataPtr={.outData=&CONST_CHARACTER_2f5a893f_3ea4_4c74_bbaa_1542ba36b733},
    	               .param1={.numberOfElements = 1},
    	            }
    	          };

    	          pearlrt::IODataList dataList = {
    	            .nbrOfEntries=sizeof(dataEntries)/sizeof(dataEntries[0]),
    	            .entry=dataEntries};
    	         static pearlrt::IOFormatList formatList = {
    	            .nbrOfEntries=sizeof(formatEntries)/sizeof(formatEntries[0]),
    	            .entry=formatEntries};
    	         _to->put(me, &dataList , &formatList);
    	       }


    	       me->setLocation(50, "signalTest.prl");
    	       me->terminate(me);






    	       }
    	       // BLOCK END 

    	       break;
    	   case 4 : 
    	       // BLOCK BEGIN 
    	       {
    	       pearlrt::Fixed<31>  _qqq ;

    	       me->setLocation(64, "signalTest.prl");
    	       // put statement 
    	       {
    	          static pearlrt::IOFormatEntry formatEntries[]  = {
    	         {
    	            .format=pearlrt::IOFormatEntry::A
    	         },
    	         {
    	            .format=pearlrt::IOFormatEntry::F,
    	            .fp1={.f31=CONST_FIXED_P_3_2},
    	            .fp2={.f31=0}
    	         },
    	         {
    	            .format=pearlrt::IOFormatEntry::SKIP,
    	            .fp1={.f31=CONST_FIXED_P_1_31}
    	         }
    	       };


    	          pearlrt::IODataEntry dataEntries[]  = {
    	            {
    	               .dataType={pearlrt::IODataEntry::CHAR,36},
    	               .dataPtr={.outData=&CONST_CHARACTER_caca90b0_96aa_4e62_a3be_f9f8bf43a479},
    	               .param1={.numberOfElements = 1},
    	            },
    	            {
    	               .dataType={pearlrt::IODataEntry::FIXED,15},
    	               .dataPtr={.outData=&_p},
    	               .param1={.numberOfElements = 1},
    	            }
    	          };

    	          pearlrt::IODataList dataList = {
    	            .nbrOfEntries=sizeof(dataEntries)/sizeof(dataEntries[0]),
    	            .entry=dataEntries};
    	         static pearlrt::IOFormatList formatList = {
    	            .nbrOfEntries=sizeof(formatEntries)/sizeof(formatEntries[0]),
    	            .entry=formatEntries};
    	         _to->put(me, &dataList , &formatList);
    	       }


    	       me->setLocation(65, "signalTest.prl");
    	       _qqq = CONST_FIXED_P_1_1;

    	       me->setLocation(66, "signalTest.prl");
    	       	if ((_p == CONST_FIXED_P_6_3).getBoolean()) {
    	       	    me->setLocation(67, "signalTest.prl");
    	       	    	goto _exit;



    	       	}


    	       _p11:
    	       me->setLocation(69, "signalTest.prl");
    	       	if ((_p == CONST_FIXED_P_11_4).getBoolean()) {
    	       	    me->setLocation(71, "signalTest.prl");
    	       	    	   me->setLocation(locationLine,locationFile);
    	       	    	   return;




    	       	}


    	       me->setLocation(73, "signalTest.prl");
    	       	if ((_p == CONST_FIXED_P_12_4).getBoolean()) {
    	       	    me->setLocation(74, "signalTest.prl");
    	       	    // put statement 
    	       	    {
    	       	       static pearlrt::IOFormatEntry formatEntries[]  = {
    	       	      {
    	       	         .format=pearlrt::IOFormatEntry::A
    	       	      },
    	       	      {
    	       	         .format=pearlrt::IOFormatEntry::SKIP,
    	       	         .fp1={.f31=CONST_FIXED_P_1_31}
    	       	      }
    	       	    };


    	       	       pearlrt::IODataEntry dataEntries[]  = {
    	       	         {
    	       	            .dataType={pearlrt::IODataEntry::CHAR,27},
    	       	            .dataPtr={.outData=&CONST_CHARACTER_acc45d92_c451_4517_803e_0e3e5021f91b},
    	       	            .param1={.numberOfElements = 1},
    	       	         }
    	       	       };

    	       	       pearlrt::IODataList dataList = {
    	       	         .nbrOfEntries=sizeof(dataEntries)/sizeof(dataEntries[0]),
    	       	         .entry=dataEntries};
    	       	      static pearlrt::IOFormatList formatList = {
    	       	         .nbrOfEntries=sizeof(formatEntries)/sizeof(formatEntries[0]),
    	       	         .entry=formatEntries};
    	       	      _to->put(me, &dataList , &formatList);
    	       	    }


    	       	    me->setLocation(75, "signalTest.prl");
    	       	    _k = CONST_FIXED_P_11_4/(_k-_k);



    	       	}


    	       me->setLocation(77, "signalTest.prl");
    	       _p = CONST_FIXED_P_6_3;

    	       me->setLocation(78, "signalTest.prl");
    	       	goto _restart;




    	       }
    	       // BLOCK END 

    	       break;
       }
    me->setLocation(25, "signalTest.prl");
    // put statement 
    {
       static pearlrt::IOFormatEntry formatEntries[]  = {
      {
         .format=pearlrt::IOFormatEntry::A
      },
      {
         .format=pearlrt::IOFormatEntry::F,
         .fp1={.f31=CONST_FIXED_P_3_2},
         .fp2={.f31=0}
      },
      {
         .format=pearlrt::IOFormatEntry::SKIP,
         .fp1={.f31=CONST_FIXED_P_1_31}
      }
    };


       pearlrt::IODataEntry dataEntries[]  = {
         {
            .dataType={pearlrt::IODataEntry::CHAR,11},
            .dataPtr={.outData=&CONST_CHARACTER_30f9cc35_6b41_493d_b86e_d3f26b5b816f},
            .param1={.numberOfElements = 1},
         },
         {
            .dataType={pearlrt::IODataEntry::FIXED,15},
            .dataPtr={.outData=&_p},
            .param1={.numberOfElements = 1},
         }
       };

       pearlrt::IODataList dataList = {
         .nbrOfEntries=sizeof(dataEntries)/sizeof(dataEntries[0]),
         .entry=dataEntries};
      static pearlrt::IOFormatList formatList = {
         .nbrOfEntries=sizeof(formatEntries)/sizeof(formatEntries[0]),
         .entry=formatEntries};
      _to->put(me, &dataList , &formatList);
    }


    _restart:
    me->setLocation(27, "signalTest.prl");
    // put statement 
    {
       static pearlrt::IOFormatEntry formatEntries[]  = {
      {
         .format=pearlrt::IOFormatEntry::A
      },
      {
         .format=pearlrt::IOFormatEntry::SKIP,
         .fp1={.f31=CONST_FIXED_P_1_31}
      }
    };


       pearlrt::IODataEntry dataEntries[]  = {
         {
            .dataType={pearlrt::IODataEntry::CHAR,15},
            .dataPtr={.outData=&CONST_CHARACTER_3e0fcb4a_c6d1_47da_84ad_e9a5dc676a74},
            .param1={.numberOfElements = 1},
         }
       };

       pearlrt::IODataList dataList = {
         .nbrOfEntries=sizeof(dataEntries)/sizeof(dataEntries[0]),
         .entry=dataEntries};
      static pearlrt::IOFormatList formatList = {
         .nbrOfEntries=sizeof(formatEntries)/sizeof(formatEntries[0]),
         .entry=formatEntries};
      _to->put(me, &dataList , &formatList);
    }


    me->setLocation(31, "signalTest.prl");
       scheduledSignalActions.setAction(1,_arith,_errno);


    me->setLocation(36, "signalTest.prl");
    _k = CONST_FIXED_P_2_2;

    me->setLocation(38, "signalTest.prl");
    	if ((_p == CONST_FIXED_P_1_1).getBoolean()) {
    	    me->setLocation(40, "signalTest.prl");
    	       scheduledSignalActions.setAction(2,_overflow);




    	}


    me->setLocation(46, "signalTest.prl");
    	if ((_p > CONST_FIXED_P_5_3).getBoolean()) {
    	    me->setLocation(48, "signalTest.prl");
    	       scheduledSignalActions.setAction(3,_overflow);




    	}


    me->setLocation(56, "signalTest.prl");
    	if ((_p == CONST_FIXED_P_10_4).getBoolean()) {
    	    me->setLocation(57, "signalTest.prl");
    	    _k = CONST_FIXED_P_10_4/(_k-_k);



    	}


    me->setLocation(62, "signalTest.prl");
       scheduledSignalActions.setAction(4,_div0);


    me->setLocation(81, "signalTest.prl");
    	if ((_p == CONST_FIXED_P_11_4).getBoolean()) {
    	    me->setLocation(83, "signalTest.prl");
    	    _k = CONST_FIXED_P_10_4/(_k-_k);



    	}


    me->setLocation(86, "signalTest.prl");
    	if ((_p == CONST_FIXED_P_12_4).getBoolean()) {
    	    me->setLocation(88, "signalTest.prl");
    	    _k = CONST_FIXED_P_10_4/(_k-_k);



    	}


    me->setLocation(91, "signalTest.prl");
    // put statement 
    {
       static pearlrt::IOFormatEntry formatEntries[]  = {
      {
         .format=pearlrt::IOFormatEntry::A
      },
      {
         .format=pearlrt::IOFormatEntry::SKIP,
         .fp1={.f31=CONST_FIXED_P_1_31}
      }
    };


       pearlrt::IODataEntry dataEntries[]  = {
         {
            .dataType={pearlrt::IODataEntry::CHAR,35},
            .dataPtr={.outData=&CONST_CHARACTER_e614fc5d_8cde_4c75_942d_81e6418b8493},
            .param1={.numberOfElements = 1},
         }
       };

       pearlrt::IODataList dataList = {
         .nbrOfEntries=sizeof(dataEntries)/sizeof(dataEntries[0]),
         .entry=dataEntries};
      static pearlrt::IOFormatList formatList = {
         .nbrOfEntries=sizeof(formatEntries)/sizeof(formatEntries[0]),
         .entry=formatEntries};
      _to->put(me, &dataList , &formatList);
    }


    me->setLocation(92, "signalTest.prl");
    {   // REPEAT 
        pearlrt::Fixed<7> a_value(CONST_FIXED_P_1_1);

        pearlrt::Fixed<7> e_value;
        e_value = CONST_FIXED_P_100_7;

        pearlrt::Fixed<7> s_value(CONST_FIXED_P_1_1);

            while ((((s_value > CONST_FIXED_P_0_1).getBoolean()) &&
                    ((a_value <= e_value).getBoolean())) ||
                    (((s_value < CONST_FIXED_P_0_1).getBoolean()) &&
                    ((a_value >= e_value).getBoolean())))
            {

                 
                               me->setLocation(93, "signalTest.prl");
                               _k = _k*_k;




                me->setLocation(93, filename);
    			try {
                   a_value = a_value + s_value;
                }
                catch(pearlrt::FixedRangeSignal &ex) {
                    break;
                }

            }
    } // REPEAT 


    _exit:
    me->setLocation(95, "signalTest.prl");
        ;



    } catch(pearlrt::Signal & s) {
       indexOfSignalAction = scheduledSignalActions.getActionIndex(&s);
       if (indexOfSignalAction == 0) {
          // no handler found
          throw;
       }
       goto tryAgain;
    } 
    // END OF SIGNAL FRAME

    me->setLocation(locationLine,locationFile);
}
 

/////////////////////////////////////////////////////////////////////////////
// TASK DECLARATIONS
/////////////////////////////////////////////////////////////////////////////
DCLTASK(_t1, (pearlrt::Prio( (pearlrt::Fixed<15>)255)), ((pearlrt::BitString<1>)1)) {
    pearlrt::Fixed<15>  _f ;
    pearlrt::Fixed<31>  _errno ;

     
    // SIGNAL REACTION FRAME 
    // signal handling data
    int indexOfSignalAction = -1; 
    pearlrt::SignalAction sigActions[1];
    pearlrt::ScheduledSignalActions scheduledSignalActions(1, sigActions);

    tryAgain:
    try {
       switch (indexOfSignalAction) {
          case -1: break;


    	   case 1 : 
    	       // BLOCK BEGIN 
    	       {
    	       me->setLocation(107, "signalTest.prl");
    	       // put statement 
    	       {
    	          static pearlrt::IOFormatEntry formatEntries[]  = {
    	         {
    	            .format=pearlrt::IOFormatEntry::SKIP,
    	            .fp1={.f31=CONST_FIXED_P_1_31}
    	         },
    	         {
    	            .format=pearlrt::IOFormatEntry::A
    	         },
    	         {
    	            .format=pearlrt::IOFormatEntry::SKIP,
    	            .fp1={.f31=CONST_FIXED_P_1_31}
    	         }
    	       };


    	          pearlrt::IODataEntry dataEntries[]  = {
    	            {
    	               .dataType={pearlrt::IODataEntry::CHAR,29},
    	               .dataPtr={.outData=&CONST_CHARACTER_a57c8efb_067b_4232_8eea_8919a2a0d56b},
    	               .param1={.numberOfElements = 1},
    	            }
    	          };

    	          pearlrt::IODataList dataList = {
    	            .nbrOfEntries=sizeof(dataEntries)/sizeof(dataEntries[0]),
    	            .entry=dataEntries};
    	         static pearlrt::IOFormatList formatList = {
    	            .nbrOfEntries=sizeof(formatEntries)/sizeof(formatEntries[0]),
    	            .entry=formatEntries};
    	         _to->put(me, &dataList , &formatList);
    	       }


    	       me->setLocation(108, "signalTest.prl");
    	       me->terminate(me);






    	       }
    	       // BLOCK END 

    	       break;
       }
    me->setLocation(103, "signalTest.prl");
    {
           _to->dationOpen(
           0
                  	                , (pearlrt::RefCharacter*) 0
           , (pearlrt::Fixed<31>*) 0
           );
    }


    me->setLocation(104, "signalTest.prl");
    // put statement 
    {
       static pearlrt::IOFormatEntry formatEntries[]  = {
      {
         .format=pearlrt::IOFormatEntry::A
      },
      {
         .format=pearlrt::IOFormatEntry::SKIP,
         .fp1={.f31=CONST_FIXED_P_1_31}
      }
    };


       pearlrt::IODataEntry dataEntries[]  = {
         {
            .dataType={pearlrt::IODataEntry::CHAR,10},
            .dataPtr={.outData=&CONST_CHARACTER_186a7388_e7ad_4f67_887e_49d01fcc3c79},
            .param1={.numberOfElements = 1},
         }
       };

       pearlrt::IODataList dataList = {
         .nbrOfEntries=sizeof(dataEntries)/sizeof(dataEntries[0]),
         .entry=dataEntries};
      static pearlrt::IOFormatList formatList = {
         .nbrOfEntries=sizeof(formatEntries)/sizeof(formatEntries[0]),
         .entry=formatEntries};
      _to->put(me, &dataList , &formatList);
    }


    me->setLocation(106, "signalTest.prl");
       scheduledSignalActions.setAction(1,_arith);


    me->setLocation(111, "signalTest.prl");
    // put statement 
    {
       static pearlrt::IOFormatEntry formatEntries[]  = {
      {
         .format=pearlrt::IOFormatEntry::A
      },
      {
         .format=pearlrt::IOFormatEntry::SKIP,
         .fp1={.f31=CONST_FIXED_P_1_31}
      }
    };


       pearlrt::IODataEntry dataEntries[]  = {
         {
            .dataType={pearlrt::IODataEntry::CHAR,17},
            .dataPtr={.outData=&CONST_CHARACTER_067d8f62_0dff_429a_9087_5ddde9c6f203},
            .param1={.numberOfElements = 1},
         }
       };

       pearlrt::IODataList dataList = {
         .nbrOfEntries=sizeof(dataEntries)/sizeof(dataEntries[0]),
         .entry=dataEntries};
      static pearlrt::IOFormatList formatList = {
         .nbrOfEntries=sizeof(formatEntries)/sizeof(formatEntries[0]),
         .entry=formatEntries};
      _to->put(me, &dataList , &formatList);
    }


    me->setLocation(113, "signalTest.prl");
    // put statement 
    {
       static pearlrt::IOFormatEntry formatEntries[]  = {
      {
         .format=pearlrt::IOFormatEntry::A
      },
      {
         .format=pearlrt::IOFormatEntry::SKIP,
         .fp1={.f31=CONST_FIXED_P_1_31}
      }
    };


       pearlrt::IODataEntry dataEntries[]  = {
         {
            .dataType={pearlrt::IODataEntry::CHAR,14},
            .dataPtr={.outData=&CONST_CHARACTER_1f18d3d7_e233_4db4_ae93_bf23287ec4a9},
            .param1={.numberOfElements = 1},
         }
       };

       pearlrt::IODataList dataList = {
         .nbrOfEntries=sizeof(dataEntries)/sizeof(dataEntries[0]),
         .entry=dataEntries};
      static pearlrt::IOFormatList formatList = {
         .nbrOfEntries=sizeof(formatEntries)/sizeof(formatEntries[0]),
         .entry=formatEntries};
      _to->put(me, &dataList , &formatList);
    }


    me->setLocation(114, "signalTest.prl");
    _f = CONST_FIXED_P_0_1;

    me->setLocation(115, "signalTest.prl");
    _x( me, _f);

    me->setLocation(117, "signalTest.prl");
    _f = CONST_FIXED_P_11_4;

    me->setLocation(118, "signalTest.prl");
    _x( me, _f);

    me->setLocation(119, "signalTest.prl");
    _f = CONST_FIXED_P_10_4;

    me->setLocation(120, "signalTest.prl");
    _x( me, _f);

    me->setLocation(121, "signalTest.prl");
    _f = CONST_FIXED_P_1_1;

    me->setLocation(122, "signalTest.prl");
    _x( me, _f);

    me->setLocation(123, "signalTest.prl");
    _f = CONST_FIXED_P_12_4;

    me->setLocation(124, "signalTest.prl");
    _x( me, _f);



    } catch(pearlrt::Signal & s) {
       indexOfSignalAction = scheduledSignalActions.getActionIndex(&s);
       if (indexOfSignalAction == 0) {
          // no handler found
          throw;
       }
       goto tryAgain;
    } 
    // END OF SIGNAL FRAME

}




}
