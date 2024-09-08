#include <PearlIncludes.h>

namespace ns_SimFilExtruder {
/////////////////////////////////////////////////////////////////////////////
// PROLOGUE
/////////////////////////////////////////////////////////////////////////////


static const char* filename = (char*) "SimFilExtruder.prl";



/////////////////////////////////////////////////////////////////////////////
// CONSTANT POOL
/////////////////////////////////////////////////////////////////////////////
static /*const*/ pearlrt::Fixed<1>         CONST_FIXED_P_1_1(1);
static /*const*/ pearlrt::Fixed<1>         CONST_FIXED_P_0_1(0);
static /*const*/ pearlrt::Fixed<10>         CONST_FIXED_P_1000_10(1000);
static /*const*/ pearlrt::Fixed<7>         CONST_FIXED_P_100_7(100);
static /*const*/ pearlrt::Fixed<9>         CONST_FIXED_P_500_9(500);
static /*const*/ pearlrt::Fixed<10>         CONST_FIXED_P_700_10(700);
static /*const*/ pearlrt::Fixed<3>         CONST_FIXED_P_5_3(5);
static /*const*/ pearlrt::Fixed<4>         CONST_FIXED_P_12_4(12);
static /*const*/ pearlrt::Fixed<2>         CONST_FIXED_P_3_2(3);
static /*const*/ pearlrt::Fixed<5>         CONST_FIXED_P_20_5(20);
static /*const*/ pearlrt::Fixed<3>         CONST_FIXED_P_4_3(4);
static /*const*/ pearlrt::Fixed<4>         CONST_FIXED_P_10_4(10);
static /*const*/ pearlrt::Fixed<31>         CONST_FIXED_P_0_31(0);
static /*const*/ pearlrt::Fixed<31>         CONST_FIXED_P_1_31(1);
static /*const*/ pearlrt::Fixed<31>         CONST_FIXED_N_1_31(-1);
static /*const*/ pearlrt::Fixed<7>         CONST_FIXED_P_80_7(80);
static /*const*/ pearlrt::Float<23>         CONST_FLOAT_P_3_1415926_23(3.1415926);
static /*const*/ pearlrt::Float<23>         CONST_FLOAT_P_0_0_23(0.0);
static /*const*/ pearlrt::Float<23>         CONST_FLOAT_P_23_0_23(23.0);
static /*const*/ pearlrt::Float<23>         CONST_FLOAT_P_0_2_23(0.2);
static /*const*/ pearlrt::Float<23>         CONST_FLOAT_P_0_1_23(0.1);
static /*const*/ pearlrt::Float<23>         CONST_FLOAT_P_0_001_23(0.001);
static /*const*/ pearlrt::Character<7>         CONST_CHARACTER_2ecae67c_c2f0_4175_b28d_f9bf66d161d9("sim.log");
static /*const*/ pearlrt::Character<19>         CONST_CHARACTER_75490a86_7f99_48b3_b5cc_34783cba7e3c("STARTING SIMULATION");
static /*const*/ pearlrt::Character<27>         CONST_CHARACTER_9beb1e42_368d_47ab_86c0_bfd5ff08f29e("REQUEST STOPPING SIMULATION");
static /*const*/ pearlrt::Character<6>         CONST_CHARACTER_5263dfec_6b4d_4da4_9f23_89a05fc1f240(" SIM: ");
static /*const*/ pearlrt::Character<7>         CONST_CHARACTER_300156ea_9211_4ce7_8fc1_82e67473b489("Fields:");
static /*const*/ pearlrt::Character<32>         CONST_CHARACTER_2608328c_8de1_4ea7_81fc_d726ea42686b("|#1|#2|#3|#4|#5|#6|#7|#8|#9|#10|");
static /*const*/ pearlrt::Character<26>         CONST_CHARACTER_48edb339_d9fb_46e7_b45d_6b5656c39c67("# 1: simulation_is_running");
static /*const*/ pearlrt::Character<19>         CONST_CHARACTER_f637fba8_d82c_414b_b409_c7d4a1c9a60c("# 2: ScrewMotor RPM");
static /*const*/ pearlrt::Character<27>         CONST_CHARACTER_478b89e0_5170_4b5b_8d52_282378a6973e("# 3: ScrewMotor Activations");
static /*const*/ pearlrt::Character<21>         CONST_CHARACTER_70908139_2a03_49aa_8dc3_be9bb2ea208c("# 4: SpoolerMotor RPM");
static /*const*/ pearlrt::Character<29>         CONST_CHARACTER_ab0abfc2_52a8_48b2_9da6_0232773f6ae4("# 5: SpoolerMotor Activations");
static /*const*/ pearlrt::Character<20>         CONST_CHARACTER_37d98853_98af_4c4a_861a_40607a6d961e("# 6: ScrewHeater PWM");
static /*const*/ pearlrt::Character<22>         CONST_CHARACTER_7ede454e_9a77_4921_be03_9221f65caa23("# 7: Filament Diameter");
static /*const*/ pearlrt::Character<25>         CONST_CHARACTER_9c3f7b32_9377_4c14_a244_63d65127730a("# 8: Extruder Temperature");
static /*const*/ pearlrt::Character<76>         CONST_CHARACTER_07c63df2_903f_4bd0_95fb_455a1c0f38e7("----------------------------------------------------------------------------");
static /*const*/ pearlrt::Character<37>         CONST_CHARACTER_e0a3735a_9682_484c_9345_f914789de054(" 1    2    3    4    5    6    7    8");
static /*const*/ pearlrt::Character<1>         CONST_CHARACTER_9302be52_a04f_403e_bc3c_1dae7b293596("|");
static /*const*/ pearlrt::Character<23>         CONST_CHARACTER_037b8fbf_3121_42ba_b578_98cf37392559("SIMULATION TASK STOPPED");
static /*const*/ pearlrt::BitString<1>         CONST_BITSTRING_1_0(0x0);
static /*const*/ pearlrt::BitString<1>         CONST_BITSTRING_1_1(0x1);
static /*const*/ pearlrt::Duration          CONST_DURATION_P_0_0_1_0(1,0,1);
static /*const*/ pearlrt::Duration          CONST_DURATION_P_0_0_0_001(0,1000,1);
static /*const*/ pearlrt::Duration          CONST_DURATION_P_0_0_0_01(0,10000,1);

/////////////////////////////////////////////////////////////////////////////
// TASK SPECIFIERS
/////////////////////////////////////////////////////////////////////////////
extern pearlrt::Task task_simulation;


extern pearlrt::Task task_log_status;

}



/////////////////////////////////////////////////////////////////////////////
// STRUCTURE FORWARD DECLARATIONS
/////////////////////////////////////////////////////////////////////////////

/////////////////////////////////////////////////////////////////////////////
// STRUCTURE DECLARATIONS
/////////////////////////////////////////////////////////////////////////////

namespace ns_SimFilExtruder {


/////////////////////////////////////////////////////////////////////////////
// PROBLEM PART
/////////////////////////////////////////////////////////////////////////////

/////////////////////////////////////////////////////////////////////////////
// ARRAY DESCRIPTORS
/////////////////////////////////////////////////////////////////////////////


/////////////////////////////////////////////////////////////////////////////
// PROCEDURE SPECIFICATIONS
/////////////////////////////////////////////////////////////////////////////
/* public */ pearlrt::Fixed<31> _get_spoolermotor_rotational_speed(pearlrt::Task *me);

static void _closeLogfile(pearlrt::Task *me);

 /* public */ void _stop_simulation(pearlrt::Task *me);

/* public */ void _set_screwheater_pwm(pearlrt::Task *me, pearlrt::Fixed<15> );

static void _screwheater_update(pearlrt::Task *me, pearlrt::Float<23> );

 /* public */ pearlrt::Fixed<31> _get_screwheater_pwm(pearlrt::Task *me);

/* public */ pearlrt::Float<23> _get_diameter_sensor(pearlrt::Task *me);

static void _temperature_sensor_update(pearlrt::Task *me, pearlrt::Float<23> );

 /* public */ pearlrt::Fixed<31> _get_screwmotor_rotational_speed(pearlrt::Task *me);

/* public */ void _start_simulation(pearlrt::Task *me);

static void _diameter_sensor_update(pearlrt::Task *me, pearlrt::Float<23> );

 /* public */ void _set_spoolermotor_rotational_speed(pearlrt::Task *me, pearlrt::Fixed<15> );

static void _openLogfile(pearlrt::Task *me, pearlrt::RefCharacter , pearlrt::BitString<1> );

 static void _log(pearlrt::Task *me, pearlrt::RefCharacter );

 /* public */ void _set_screwmotor_rotational_speed(pearlrt::Task *me, pearlrt::Fixed<15> );

/* public */ pearlrt::Float<23> _get_temperature_sensor(pearlrt::Task *me);

static void _spoolermotor_update(pearlrt::Task *me, pearlrt::Float<23> );

 static void _screwmotor_update(pearlrt::Task *me, pearlrt::Float<23> );

 


/////////////////////////////////////////////////////////////////////////////
// DATION SPECIFICATIONS
/////////////////////////////////////////////////////////////////////////////
extern pearlrt::SystemDationNB * _pipe; 


extern pearlrt::SystemDationNB * _homeFolder; 



/////////////////////////////////////////////////////////////////////////////
// DATION DECLARATIONS
/////////////////////////////////////////////////////////////////////////////
static pearlrt::DationPG *_writeFormatted;
 static pearlrt::DationRW *_readInternal;
 static pearlrt::DationPG *_logFile;
 

/////////////////////////////////////////////////////////////////////////////
// SYSTEM DATION INITIALIZER
/////////////////////////////////////////////////////////////////////////////
static void setupDationsAndPointers() {
   static pearlrt::DationDim2 h_dim_writeFormatted(80); 
   static pearlrt::DationPG d_writeFormatted(_pipe, pearlrt::Dation::OUT  | pearlrt::Dation::FORWARD | pearlrt::Dation::NOCYCL | pearlrt::Dation::STREAM, &h_dim_writeFormatted);
   _writeFormatted = &d_writeFormatted;


   static pearlrt::DationDim2 h_dim_readInternal(80); 
   static pearlrt::DationRW d_readInternal(_pipe, pearlrt::Dation::IN  | pearlrt::Dation::FORWARD | pearlrt::Dation::NOCYCL | pearlrt::Dation::STREAM, &h_dim_readInternal,sizeof(pearlrt::Character<1>));
   _readInternal = &d_readInternal;


   static pearlrt::DationDim2 h_dim_logFile(80); 
   static pearlrt::DationPG d_logFile(_homeFolder, pearlrt::Dation::OUT  | pearlrt::Dation::FORWARD | pearlrt::Dation::NOCYCL | pearlrt::Dation::STREAM, &h_dim_logFile);
   _logFile = &d_logFile;




} 

static pearlrt::Control::Initializer init={setupDationsAndPointers,NULL};
static int dummy = pearlrt::Control::addInitializer(&init);

/////////////////////////////////////////////////////////////////////////////
// VARIABLE DECLARATIONS
/////////////////////////////////////////////////////////////////////////////
static pearlrt::Character<7>  _fileName (CONST_CHARACTER_2ecae67c_c2f0_4175_b28d_f9bf66d161d9); 
static pearlrt::Character<80>  _message ; 
static const pearlrt::Float<23>  _pi (CONST_FLOAT_P_3_1415926_23); 
static pearlrt::BitString<1>  _simulation_is_running (CONST_BITSTRING_1_0); 
static pearlrt::Semaphore _sema ( 1,"_sema");
 
static pearlrt::Fixed<31>  _step (CONST_FIXED_P_0_1); 
static pearlrt::Fixed<15>  _screwmotor_max_rpm (CONST_FIXED_P_1000_10); 
static pearlrt::Fixed<15>  _screwmotor_idle_rpm (CONST_FIXED_P_100_7); 
static pearlrt::Fixed<15>  _screwmotor_spin_up_speed (CONST_FIXED_P_500_9); 
static pearlrt::Fixed<15>  _screwmotor_spin_down_speed (CONST_FIXED_P_700_10); 
static pearlrt::Fixed<15>  _screwmotor_requested_rpm (CONST_FIXED_P_0_1); 
static pearlrt::Float<23>  _screwmotor_current_rpm (CONST_FLOAT_P_0_0_23); 
static pearlrt::Fixed<31>  _screwmotor_activations (CONST_FIXED_P_0_1); 
static pearlrt::Fixed<15>  _screwheater_max_pwm (CONST_FIXED_P_100_7); 
static pearlrt::Float<23>  _screwheater_current_pwm (CONST_FLOAT_P_0_0_23); 
static pearlrt::Float<23>  _screwheater_requested_pwm (CONST_FLOAT_P_0_0_23); 
static pearlrt::Fixed<31>  _screwheater_activations (CONST_FIXED_P_0_1); 
static pearlrt::Float<23>  _temperature_sensor (CONST_FLOAT_P_23_0_23); 
static pearlrt::Fixed<15>  _spoolermotor_max_rpm (CONST_FIXED_P_500_9); 
static pearlrt::Fixed<15>  _spoolermotor_idle_rpm (CONST_FIXED_P_5_3); 
static pearlrt::Fixed<15>  _spoolermotor_spin_up_speed (CONST_FIXED_P_500_9); 
static pearlrt::Fixed<15>  _spoolermotor_spin_down_speed (CONST_FIXED_P_700_10); 
static pearlrt::Fixed<15>  _spoolermotor_requested_rpm (CONST_FIXED_P_0_1); 
static pearlrt::Float<23>  _spoolermotor_current_rpm (CONST_FLOAT_P_0_0_23); 
static pearlrt::Fixed<31>  _spoolermotor_activations (CONST_FIXED_P_0_1); 
static pearlrt::Float<23>  _diameter_sensor (CONST_FLOAT_P_0_0_23); 
static pearlrt::Clock  _last_update ; 







/////////////////////////////////////////////////////////////////////////////
// CONSTANT SEMAPHORE ARRAYS
/////////////////////////////////////////////////////////////////////////////

/////////////////////////////////////////////////////////////////////////////
// CONSTANT BOLT ARRAYS
/////////////////////////////////////////////////////////////////////////////

/////////////////////////////////////////////////////////////////////////////
// PROCEDURE DECLARATIONS
/////////////////////////////////////////////////////////////////////////////
/* public */ void
_start_simulation(pearlrt::Task *me)
{
    const char* locationFile = me->getLocationFile();
    int locationLine = me->getLocationLine(); 
    me->setLocation(145, "SimFilExtruder.prl");
    _openLogfile( me, pearlrt::RefCharacter(_fileName), CONST_BITSTRING_1_0);

    me->setLocation(146, "SimFilExtruder.prl");
    {
           _writeFormatted->dationOpen(
           0
                  	                , (pearlrt::RefCharacter*) 0
           , (pearlrt::Fixed<31>*) 0
           );
    }


    me->setLocation(147, "SimFilExtruder.prl");
    _log( me, pearlrt::RefCharacter(CONST_CHARACTER_75490a86_7f99_48b3_b5cc_34783cba7e3c));

    me->setLocation(148, "SimFilExtruder.prl");
    _simulation_is_running = CONST_BITSTRING_1_1;

    me->setLocation(149, "SimFilExtruder.prl");
        task_log_status.activate( me,
    	     pearlrt::Task::AFTER,
    	    /* prio   */  pearlrt::Prio(),
    	    /* at     */  pearlrt::Clock(),
    	    /* after  */  CONST_DURATION_P_0_0_1_0,
    	    /* all    */  pearlrt::Duration(),
    	    /* until  */  pearlrt::Clock(),
    	    /* during */  pearlrt::Duration(),
    	    /* when   */  0
                       );



    me->setLocation(150, "SimFilExtruder.prl");
        task_simulation.activate( me,
    	     0,
    	    /* prio   */  pearlrt::Prio(),
    	    /* at     */  pearlrt::Clock(),
    	    /* after  */  pearlrt::Duration(),
    	    /* all    */  pearlrt::Duration(),
    	    /* until  */  pearlrt::Clock(),
    	    /* during */  pearlrt::Duration(),
    	    /* when   */  0
                       );






    me->setLocation(locationLine,locationFile);
}
/* public */ void
_stop_simulation(pearlrt::Task *me)
{
    const char* locationFile = me->getLocationFile();
    int locationLine = me->getLocationLine(); 
    me->setLocation(155, "SimFilExtruder.prl");
    _log( me, pearlrt::RefCharacter(CONST_CHARACTER_9beb1e42_368d_47ab_86c0_bfd5ff08f29e));

    me->setLocation(156, "SimFilExtruder.prl");
    _simulation_is_running = CONST_BITSTRING_1_0;

    me->setLocation(157, "SimFilExtruder.prl");
      _writeFormatted->dationClose(0, (pearlrt::Fixed<15>*) 0);





    me->setLocation(locationLine,locationFile);
}
static void
_openLogfile(pearlrt::Task *me, pearlrt::RefCharacter _logFileName, pearlrt::BitString<1> _addToExistingFile)
{
    const char* locationFile = me->getLocationFile();
    int locationLine = me->getLocationLine(); 
    pearlrt::Character<30>  _fn ;

    me->setLocation(164, "SimFilExtruder.prl");
    _logFileName.store(_fn);

    me->setLocation(166, "SimFilExtruder.prl");
    	if (_addToExistingFile.getBoolean()) {
    	    me->setLocation(167, "SimFilExtruder.prl");
    	    {
    	        {
    	           pearlrt::RefCharacter rcForFilename; 
    	    	
    	             rcForFilename.setWork(_fn);

    	           _logFile->dationOpen(
    	           pearlrt::Dation::IDF | pearlrt::Dation::ANY
    	           , &rcForFilename
    	           , (pearlrt::Fixed<31>*) 0
    	           );
    	       	}
    	    }


    	    me->setLocation(168, "SimFilExtruder.prl");
    	    // put statement 
    	    {
    	       static pearlrt::IOFormatEntry formatEntries[]  = {
    	      {
    	         .format=pearlrt::IOFormatEntry::EOFPOS
    	      }
    	    };


    	    pearlrt::IODataList dataList = {
    	         .nbrOfEntries=0,
    	         .entry=NULL};
    	      static pearlrt::IOFormatList formatList = {
    	         .nbrOfEntries=sizeof(formatEntries)/sizeof(formatEntries[0]),
    	         .entry=formatEntries};
    	      _logFile->put(me, &dataList , &formatList);
    	    }




    	} else {
    	    me->setLocation(172, "SimFilExtruder.prl");
    	    {
    	        {
    	           pearlrt::RefCharacter rcForFilename; 
    	    	
    	             rcForFilename.setWork(_fn);

    	           _logFile->dationOpen(
    	           pearlrt::Dation::IDF | pearlrt::Dation::ANY
    	           , &rcForFilename
    	           , (pearlrt::Fixed<31>*) 0
    	           );
    	       	}
    	    }


    	    me->setLocation(173, "SimFilExtruder.prl");
    	      _logFile->dationClose(pearlrt::Dation::CAN, (pearlrt::Fixed<15>*) 0);


    	    me->setLocation(177, "SimFilExtruder.prl");
    	    {
    	        {
    	           pearlrt::RefCharacter rcForFilename; 
    	    	
    	             rcForFilename.setWork(_fn);

    	           _logFile->dationOpen(
    	           pearlrt::Dation::IDF | pearlrt::Dation::NEW
    	           , &rcForFilename
    	           , (pearlrt::Fixed<31>*) 0
    	           );
    	       	}
    	    }




    	}





    me->setLocation(locationLine,locationFile);
}
 static void
_closeLogfile(pearlrt::Task *me)
{
    const char* locationFile = me->getLocationFile();
    int locationLine = me->getLocationLine(); 
    me->setLocation(183, "SimFilExtruder.prl");
      _logFile->dationClose(0, (pearlrt::Fixed<15>*) 0);





    me->setLocation(locationLine,locationFile);
}
 static void
_log(pearlrt::Task *me, pearlrt::RefCharacter _line)
{
    const char* locationFile = me->getLocationFile();
    int locationLine = me->getLocationLine(); 
    me->setLocation(188, "SimFilExtruder.prl");
    // put statement 
    {
       static pearlrt::IOFormatEntry formatEntries[]  = {
      {
         .format=pearlrt::IOFormatEntry::T,
         .fp1={.f31=CONST_FIXED_P_12_4},
         .fp2={.f31=CONST_FIXED_P_3_2}
      },
      {
         .format=pearlrt::IOFormatEntry::A
      },
      {
         .format=pearlrt::IOFormatEntry::A
      },
      {
         .format=pearlrt::IOFormatEntry::SKIP,
         .fp1={.f31=CONST_FIXED_P_1_31}
      }
    };

       pearlrt::Clock  tempVar0 (pearlrt::Clock::now());

       pearlrt::IODataEntry dataEntries[]  = {
         {
            .dataType={pearlrt::IODataEntry::CLOCK,},
            .dataPtr={.outData=&tempVar0},
            .param1={.numberOfElements = 1},
         },
         {
            .dataType={pearlrt::IODataEntry::CHAR,6},
            .dataPtr={.outData=&CONST_CHARACTER_5263dfec_6b4d_4da4_9f23_89a05fc1f240},
            .param1={.numberOfElements = 1},
         },
         {
            .dataType={pearlrt::IODataEntry::REFCHAR,0},
            .dataPtr={.outData=&_line},
            .param1={.numberOfElements = 1},
         }
       };

       pearlrt::IODataList dataList = {
         .nbrOfEntries=sizeof(dataEntries)/sizeof(dataEntries[0]),
         .entry=dataEntries};
      static pearlrt::IOFormatList formatList = {
         .nbrOfEntries=sizeof(formatEntries)/sizeof(formatEntries[0]),
         .entry=formatEntries};
      _logFile->put(me, &dataList , &formatList);
    }


    me->setLocation(189, "SimFilExtruder.prl");
    // put statement 
    {
       static pearlrt::IOFormatEntry formatEntries[]  = {
      {
         .format=pearlrt::IOFormatEntry::T,
         .fp1={.f31=CONST_FIXED_P_12_4},
         .fp2={.f31=CONST_FIXED_P_3_2}
      },
      {
         .format=pearlrt::IOFormatEntry::A
      },
      {
         .format=pearlrt::IOFormatEntry::A
      },
      {
         .format=pearlrt::IOFormatEntry::SKIP,
         .fp1={.f31=CONST_FIXED_P_1_31}
      }
    };

       pearlrt::Clock  tempVar0 (pearlrt::Clock::now());

       pearlrt::IODataEntry dataEntries[]  = {
         {
            .dataType={pearlrt::IODataEntry::CLOCK,},
            .dataPtr={.outData=&tempVar0},
            .param1={.numberOfElements = 1},
         },
         {
            .dataType={pearlrt::IODataEntry::CHAR,6},
            .dataPtr={.outData=&CONST_CHARACTER_5263dfec_6b4d_4da4_9f23_89a05fc1f240},
            .param1={.numberOfElements = 1},
         },
         {
            .dataType={pearlrt::IODataEntry::REFCHAR,0},
            .dataPtr={.outData=&_line},
            .param1={.numberOfElements = 1},
         }
       };

       pearlrt::IODataList dataList = {
         .nbrOfEntries=sizeof(dataEntries)/sizeof(dataEntries[0]),
         .entry=dataEntries};
      static pearlrt::IOFormatList formatList = {
         .nbrOfEntries=sizeof(formatEntries)/sizeof(formatEntries[0]),
         .entry=formatEntries};
      _writeFormatted->put(me, &dataList , &formatList);
    }





    me->setLocation(locationLine,locationFile);
}
 /* public */ pearlrt::Fixed<31>
_get_screwmotor_rotational_speed(pearlrt::Task *me)
{
    const char* locationFile = me->getLocationFile();
    int locationLine = me->getLocationLine(); 
    pearlrt::Fixed<31>  _rpm ;

    me->setLocation(245, "SimFilExtruder.prl");
    {
    pearlrt::Semaphore *tmp_0[] = {&_sema}; 
       	 	
       pearlrt::Semaphore::request( me, 1, tmp_0);
        


    }
    me->setLocation(246, "SimFilExtruder.prl");
    _rpm = (_screwmotor_current_rpm).entier();

    me->setLocation(247, "SimFilExtruder.prl");
    {
    pearlrt::Semaphore *tmp_0[] = {&_sema}; 
       	 	
      pearlrt::Semaphore::release( me, 1, tmp_0);
        


    }
    me->setLocation(248, "SimFilExtruder.prl");
    	   me->setLocation(locationLine,locationFile);
    	   return (_rpm);





    me->setLocation(locationLine,locationFile);
}
/* public */ void
_set_screwmotor_rotational_speed(pearlrt::Task *me, pearlrt::Fixed<15> _rpm)
{
    const char* locationFile = me->getLocationFile();
    int locationLine = me->getLocationLine(); 
    me->setLocation(253, "SimFilExtruder.prl");
    {
    pearlrt::Semaphore *tmp_0[] = {&_sema}; 
       	 	
       pearlrt::Semaphore::request( me, 1, tmp_0);
        


    }
    me->setLocation(254, "SimFilExtruder.prl");
    _screwmotor_requested_rpm = _rpm;

    me->setLocation(255, "SimFilExtruder.prl");
    {
    pearlrt::Semaphore *tmp_0[] = {&_sema}; 
       	 	
      pearlrt::Semaphore::release( me, 1, tmp_0);
        


    }



    me->setLocation(locationLine,locationFile);
}
static void
_screwmotor_update(pearlrt::Task *me, pearlrt::Float<23> _dt)
{
    const char* locationFile = me->getLocationFile();
    int locationLine = me->getLocationLine(); 
    me->setLocation(259, "SimFilExtruder.prl");
    	if ((_screwmotor_current_rpm == CONST_FIXED_P_0_1).bitAnd((_screwmotor_requested_rpm > 
    CONST_FLOAT_P_0_0_23)).getBoolean()) {
    	    me->setLocation(261, "SimFilExtruder.prl");
    	    _screwmotor_activations = _screwmotor_activations+CONST_FIXED_P_1_1;



    	}


    me->setLocation(264, "SimFilExtruder.prl");
    	if ((_screwmotor_current_rpm < _screwmotor_requested_rpm).getBoolean()) {
    	    me->setLocation(266, "SimFilExtruder.prl");
    	    _screwmotor_current_rpm = _screwmotor_current_rpm+((((_dt*_screwmotor_spin_up_speed))/((pearlrt::Float<23>)(
    	    CONST_FIXED_P_1000_10)))).entier();

    	    me->setLocation(267, "SimFilExtruder.prl");
    	    	if ((_screwmotor_current_rpm > _screwmotor_requested_rpm).getBoolean()) {
    	    	    me->setLocation(269, "SimFilExtruder.prl");
    	    	    _screwmotor_current_rpm = _screwmotor_requested_rpm;



    	    	}




    	}


    me->setLocation(273, "SimFilExtruder.prl");
    	if ((_screwmotor_current_rpm > _screwmotor_requested_rpm).getBoolean()) {
    	    me->setLocation(275, "SimFilExtruder.prl");
    	    _screwmotor_current_rpm = _screwmotor_current_rpm-((((_dt*_screwmotor_spin_down_speed))/((pearlrt::Float<23>)(
    	    CONST_FIXED_P_1000_10)))).entier();

    	    me->setLocation(277, "SimFilExtruder.prl");
    	    	if ((_screwmotor_current_rpm < CONST_FIXED_P_0_1).getBoolean()) {
    	    	    me->setLocation(279, "SimFilExtruder.prl");
    	    	    _screwmotor_current_rpm = CONST_FIXED_P_0_1;



    	    	}




    	}


    me->setLocation(283, "SimFilExtruder.prl");
    	if ((_screwmotor_current_rpm > _screwmotor_max_rpm).getBoolean()) {
    	    me->setLocation(285, "SimFilExtruder.prl");
    	    _screwmotor_current_rpm = _screwmotor_max_rpm;



    	}





    me->setLocation(locationLine,locationFile);
}
 /* public */ pearlrt::Fixed<31>
_get_screwheater_pwm(pearlrt::Task *me)
{
    const char* locationFile = me->getLocationFile();
    int locationLine = me->getLocationLine(); 
    pearlrt::Fixed<31>  _pwm ;

    me->setLocation(297, "SimFilExtruder.prl");
    {
    pearlrt::Semaphore *tmp_0[] = {&_sema}; 
       	 	
       pearlrt::Semaphore::request( me, 1, tmp_0);
        


    }
    me->setLocation(298, "SimFilExtruder.prl");
    _pwm = (_screwheater_current_pwm).entier();

    me->setLocation(299, "SimFilExtruder.prl");
    {
    pearlrt::Semaphore *tmp_0[] = {&_sema}; 
       	 	
      pearlrt::Semaphore::release( me, 1, tmp_0);
        


    }
    me->setLocation(300, "SimFilExtruder.prl");
    	   me->setLocation(locationLine,locationFile);
    	   return (_pwm);





    me->setLocation(locationLine,locationFile);
}
/* public */ void
_set_screwheater_pwm(pearlrt::Task *me, pearlrt::Fixed<15> _pwm)
{
    const char* locationFile = me->getLocationFile();
    int locationLine = me->getLocationLine(); 
    me->setLocation(305, "SimFilExtruder.prl");
    {
    pearlrt::Semaphore *tmp_0[] = {&_sema}; 
       	 	
       pearlrt::Semaphore::request( me, 1, tmp_0);
        


    }
    me->setLocation(306, "SimFilExtruder.prl");
    _screwheater_requested_pwm = _pwm;

    me->setLocation(307, "SimFilExtruder.prl");
    {
    pearlrt::Semaphore *tmp_0[] = {&_sema}; 
       	 	
      pearlrt::Semaphore::release( me, 1, tmp_0);
        


    }



    me->setLocation(locationLine,locationFile);
}
static void
_screwheater_update(pearlrt::Task *me, pearlrt::Float<23> _dt)
{
    const char* locationFile = me->getLocationFile();
    int locationLine = me->getLocationLine(); 
    me->setLocation(311, "SimFilExtruder.prl");
    	if ((_screwheater_current_pwm == CONST_FIXED_P_0_1).bitAnd((_screwheater_requested_pwm > 
    CONST_FLOAT_P_0_0_23)).getBoolean()) {
    	    me->setLocation(313, "SimFilExtruder.prl");
    	    _screwheater_activations = _screwheater_activations+CONST_FIXED_P_1_1;



    	}


    me->setLocation(316, "SimFilExtruder.prl");
    	if ((_screwheater_requested_pwm > CONST_FLOAT_P_0_0_23).getBoolean()) {
    	    me->setLocation(318, "SimFilExtruder.prl");
    	    	if ((_screwheater_requested_pwm > _screwheater_max_pwm).getBoolean()) {
    	    	    me->setLocation(320, "SimFilExtruder.prl");
    	    	    _screwheater_current_pwm = _screwheater_max_pwm;



    	    	} else {
    	    	    me->setLocation(322, "SimFilExtruder.prl");
    	    	    _screwheater_current_pwm = _screwheater_requested_pwm;



    	    	}




    	} else {
    	    me->setLocation(325, "SimFilExtruder.prl");
    	    _screwheater_current_pwm = CONST_FLOAT_P_0_0_23;

    	    me->setLocation(325, "SimFilExtruder.prl");
    	        ;



    	}





    me->setLocation(locationLine,locationFile);
}
 /* public */ pearlrt::Float<23>
_get_temperature_sensor(pearlrt::Task *me)
{
    const char* locationFile = me->getLocationFile();
    int locationLine = me->getLocationLine(); 
    pearlrt::Float<23>  _temperature ;

    me->setLocation(336, "SimFilExtruder.prl");
    {
    pearlrt::Semaphore *tmp_0[] = {&_sema}; 
       	 	
       pearlrt::Semaphore::request( me, 1, tmp_0);
        


    }
    me->setLocation(337, "SimFilExtruder.prl");
    _temperature = _temperature_sensor;

    me->setLocation(338, "SimFilExtruder.prl");
    {
    pearlrt::Semaphore *tmp_0[] = {&_sema}; 
       	 	
      pearlrt::Semaphore::release( me, 1, tmp_0);
        


    }
    me->setLocation(339, "SimFilExtruder.prl");
    	   me->setLocation(locationLine,locationFile);
    	   return (_temperature);





    me->setLocation(locationLine,locationFile);
}
static void
_temperature_sensor_update(pearlrt::Task *me, pearlrt::Float<23> _dt)
{
    const char* locationFile = me->getLocationFile();
    int locationLine = me->getLocationLine(); 
    me->setLocation(343, "SimFilExtruder.prl");
    	if ((_screwheater_current_pwm > CONST_FIXED_P_0_1).getBoolean()) {
    	    me->setLocation(345, "SimFilExtruder.prl");
    	    _temperature_sensor = _temperature_sensor+CONST_FLOAT_P_0_2_23;



    	} else {
    	    me->setLocation(347, "SimFilExtruder.prl");
    	    	if ((_temperature_sensor <= CONST_FLOAT_P_23_0_23).getBoolean()) {
    	    	    me->setLocation(349, "SimFilExtruder.prl");
    	    	    _temperature_sensor = CONST_FLOAT_P_23_0_23;



    	    	} else {
    	    	    me->setLocation(351, "SimFilExtruder.prl");
    	    	    _temperature_sensor = _temperature_sensor-CONST_FLOAT_P_0_1_23;



    	    	}




    	}





    me->setLocation(locationLine,locationFile);
}
 /* public */ pearlrt::Fixed<31>
_get_spoolermotor_rotational_speed(pearlrt::Task *me)
{
    const char* locationFile = me->getLocationFile();
    int locationLine = me->getLocationLine(); 
    pearlrt::Fixed<31>  _rpm ;

    me->setLocation(365, "SimFilExtruder.prl");
    {
    pearlrt::Semaphore *tmp_0[] = {&_sema}; 
       	 	
       pearlrt::Semaphore::request( me, 1, tmp_0);
        


    }
    me->setLocation(366, "SimFilExtruder.prl");
    _rpm = (_spoolermotor_current_rpm).entier();

    me->setLocation(367, "SimFilExtruder.prl");
    {
    pearlrt::Semaphore *tmp_0[] = {&_sema}; 
       	 	
      pearlrt::Semaphore::release( me, 1, tmp_0);
        


    }
    me->setLocation(368, "SimFilExtruder.prl");
    	   me->setLocation(locationLine,locationFile);
    	   return (_rpm);





    me->setLocation(locationLine,locationFile);
}
/* public */ void
_set_spoolermotor_rotational_speed(pearlrt::Task *me, pearlrt::Fixed<15> _rpm)
{
    const char* locationFile = me->getLocationFile();
    int locationLine = me->getLocationLine(); 
    me->setLocation(373, "SimFilExtruder.prl");
    {
    pearlrt::Semaphore *tmp_0[] = {&_sema}; 
       	 	
       pearlrt::Semaphore::request( me, 1, tmp_0);
        


    }
    me->setLocation(374, "SimFilExtruder.prl");
    _spoolermotor_requested_rpm = _rpm;

    me->setLocation(375, "SimFilExtruder.prl");
    {
    pearlrt::Semaphore *tmp_0[] = {&_sema}; 
       	 	
      pearlrt::Semaphore::release( me, 1, tmp_0);
        


    }



    me->setLocation(locationLine,locationFile);
}
static void
_spoolermotor_update(pearlrt::Task *me, pearlrt::Float<23> _dt)
{
    const char* locationFile = me->getLocationFile();
    int locationLine = me->getLocationLine(); 
    me->setLocation(379, "SimFilExtruder.prl");
    	if ((_spoolermotor_current_rpm == CONST_FIXED_P_0_1).bitAnd((_spoolermotor_requested_rpm > 
    CONST_FLOAT_P_0_0_23)).getBoolean()) {
    	    me->setLocation(381, "SimFilExtruder.prl");
    	    _spoolermotor_activations = _spoolermotor_activations+CONST_FIXED_P_1_1;



    	}


    me->setLocation(384, "SimFilExtruder.prl");
    	if ((_spoolermotor_current_rpm < _spoolermotor_requested_rpm).getBoolean()) {
    	    me->setLocation(386, "SimFilExtruder.prl");
    	    _spoolermotor_current_rpm = _spoolermotor_current_rpm+((((_dt*_spoolermotor_spin_up_speed))/((pearlrt::Float<23>)(
    	    CONST_FIXED_P_1000_10)))).entier();

    	    me->setLocation(387, "SimFilExtruder.prl");
    	    	if ((_spoolermotor_current_rpm > _spoolermotor_requested_rpm).getBoolean()) {
    	    	    me->setLocation(389, "SimFilExtruder.prl");
    	    	    _spoolermotor_current_rpm = _spoolermotor_requested_rpm;



    	    	}




    	}


    me->setLocation(393, "SimFilExtruder.prl");
    	if ((_spoolermotor_current_rpm > _spoolermotor_requested_rpm).getBoolean()) {
    	    me->setLocation(395, "SimFilExtruder.prl");
    	    _spoolermotor_current_rpm = _spoolermotor_current_rpm-((((_dt*_spoolermotor_spin_down_speed))/((pearlrt::Float<23>)(
    	    CONST_FIXED_P_1000_10)))).entier();

    	    me->setLocation(397, "SimFilExtruder.prl");
    	    	if ((_spoolermotor_current_rpm < CONST_FIXED_P_0_1).getBoolean()) {
    	    	    me->setLocation(399, "SimFilExtruder.prl");
    	    	    _spoolermotor_current_rpm = CONST_FIXED_P_0_1;



    	    	}




    	}


    me->setLocation(403, "SimFilExtruder.prl");
    	if ((_spoolermotor_current_rpm > _spoolermotor_max_rpm).getBoolean()) {
    	    me->setLocation(405, "SimFilExtruder.prl");
    	    _spoolermotor_current_rpm = _spoolermotor_max_rpm;



    	}





    me->setLocation(locationLine,locationFile);
}
 /* public */ pearlrt::Float<23>
_get_diameter_sensor(pearlrt::Task *me)
{
    const char* locationFile = me->getLocationFile();
    int locationLine = me->getLocationLine(); 
    pearlrt::Float<23>  _diameter ;

    me->setLocation(416, "SimFilExtruder.prl");
    {
    pearlrt::Semaphore *tmp_0[] = {&_sema}; 
       	 	
       pearlrt::Semaphore::request( me, 1, tmp_0);
        


    }
    me->setLocation(417, "SimFilExtruder.prl");
    _diameter = _diameter_sensor;

    me->setLocation(418, "SimFilExtruder.prl");
    {
    pearlrt::Semaphore *tmp_0[] = {&_sema}; 
       	 	
      pearlrt::Semaphore::release( me, 1, tmp_0);
        


    }
    me->setLocation(419, "SimFilExtruder.prl");
    	   me->setLocation(locationLine,locationFile);
    	   return (_diameter);





    me->setLocation(locationLine,locationFile);
}
static void
_diameter_sensor_update(pearlrt::Task *me, pearlrt::Float<23> _dt)
{
    const char* locationFile = me->getLocationFile();
    int locationLine = me->getLocationLine(); 
    me->setLocation(423, "SimFilExtruder.prl");
    	if ((_spoolermotor_current_rpm > CONST_FIXED_P_0_1).getBoolean()) {
    	    me->setLocation(425, "SimFilExtruder.prl");
    	    _diameter_sensor = _diameter_sensor+CONST_FLOAT_P_0_001_23;



    	} else {
    	    me->setLocation(427, "SimFilExtruder.prl");
    	    _diameter_sensor = CONST_FLOAT_P_0_0_23;



    	}





    me->setLocation(locationLine,locationFile);
}
 

/////////////////////////////////////////////////////////////////////////////
// TASK DECLARATIONS
/////////////////////////////////////////////////////////////////////////////
DCLTASK(_log_status, (pearlrt::Prio( (pearlrt::Fixed<15>)CONST_FIXED_P_20_5)), ((pearlrt::BitString<1>)0)) {
    pearlrt::Character<80>  _line ;

    me->setLocation(194, "SimFilExtruder.prl");
    _log( me, pearlrt::RefCharacter(CONST_CHARACTER_300156ea_9211_4ce7_8fc1_82e67473b489));

    me->setLocation(195, "SimFilExtruder.prl");
    _log( me, pearlrt::RefCharacter(CONST_CHARACTER_2608328c_8de1_4ea7_81fc_d726ea42686b));

    me->setLocation(196, "SimFilExtruder.prl");
    _log( me, pearlrt::RefCharacter(CONST_CHARACTER_48edb339_d9fb_46e7_b45d_6b5656c39c67));

    me->setLocation(197, "SimFilExtruder.prl");
    _log( me, pearlrt::RefCharacter(CONST_CHARACTER_f637fba8_d82c_414b_b409_c7d4a1c9a60c));

    me->setLocation(198, "SimFilExtruder.prl");
    _log( me, pearlrt::RefCharacter(CONST_CHARACTER_478b89e0_5170_4b5b_8d52_282378a6973e));

    me->setLocation(199, "SimFilExtruder.prl");
    _log( me, pearlrt::RefCharacter(CONST_CHARACTER_70908139_2a03_49aa_8dc3_be9bb2ea208c));

    me->setLocation(200, "SimFilExtruder.prl");
    _log( me, pearlrt::RefCharacter(CONST_CHARACTER_ab0abfc2_52a8_48b2_9da6_0232773f6ae4));

    me->setLocation(201, "SimFilExtruder.prl");
    _log( me, pearlrt::RefCharacter(CONST_CHARACTER_37d98853_98af_4c4a_861a_40607a6d961e));

    me->setLocation(202, "SimFilExtruder.prl");
    _log( me, pearlrt::RefCharacter(CONST_CHARACTER_7ede454e_9a77_4921_be03_9221f65caa23));

    me->setLocation(203, "SimFilExtruder.prl");
    _log( me, pearlrt::RefCharacter(CONST_CHARACTER_9c3f7b32_9377_4c14_a244_63d65127730a));

    me->setLocation(204, "SimFilExtruder.prl");
    _log( me, pearlrt::RefCharacter(CONST_CHARACTER_07c63df2_903f_4bd0_95fb_455a1c0f38e7));

    me->setLocation(205, "SimFilExtruder.prl");
    _log( me, pearlrt::RefCharacter(CONST_CHARACTER_e0a3735a_9682_484c_9345_f914789de054));

    me->setLocation(207, "SimFilExtruder.prl");
    {   // REPEAT 
            while ( 1 )
            {

                if (!(_simulation_is_running.getBoolean()))
                    break;

                 
                               me->setLocation(209, "SimFilExtruder.prl");
                               {
                               pearlrt::Semaphore *tmp_0[] = {&_sema}; 
                                  	 	
                                  pearlrt::Semaphore::request( me, 1, tmp_0);
                                   


                               }
                               me->setLocation(210, "SimFilExtruder.prl");
                               //CONVERT TO statement
                               {
                                 pearlrt::RefCharacter rc(_line);
                                                                            // true indicates output
                                 pearlrt::StringDationConvert strDation(&rc, true);
                                  static pearlrt::IOFormatEntry formatEntries[]  = {
                                 {
                                    .format=pearlrt::IOFormatEntry::A
                                 },
                                 {
                                    .format=pearlrt::IOFormatEntry::B1w,
                                    .fp1={.f31=CONST_FIXED_P_1_1}
                                 },
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
                                    .format=pearlrt::IOFormatEntry::F,
                                    .fp1={.f31=CONST_FIXED_P_4_3},
                                    .fp2={.f31=0}
                                 },
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
                                    .format=pearlrt::IOFormatEntry::F,
                                    .fp1={.f31=CONST_FIXED_P_4_3},
                                    .fp2={.f31=0}
                                 },
                                 {
                                    .format=pearlrt::IOFormatEntry::A
                                 },
                                 {
                                    .format=pearlrt::IOFormatEntry::F,
                                    .fp1={.f31=CONST_FIXED_P_10_4},
                                    .fp2={.f31=CONST_FIXED_P_4_3}
                                 },
                                 {
                                    .format=pearlrt::IOFormatEntry::A
                                 },
                                 {
                                    .format=pearlrt::IOFormatEntry::F,
                                    .fp1={.f31=CONST_FIXED_P_10_4},
                                    .fp2={.f31=CONST_FIXED_P_4_3}
                                 },
                                 {
                                    .format=pearlrt::IOFormatEntry::A
                                 },
                                 {
                                    .format=pearlrt::IOFormatEntry::F,
                                    .fp1={.f31=CONST_FIXED_P_10_4},
                                    .fp2={.f31=CONST_FIXED_P_4_3}
                                 },
                                 {
                                    .format=pearlrt::IOFormatEntry::A
                                 }
                               };


                                  pearlrt::IODataEntry dataEntries[]  = {
                                    {
                                       .dataType={pearlrt::IODataEntry::CHAR,1},
                                       .dataPtr={.outData=&CONST_CHARACTER_9302be52_a04f_403e_bc3c_1dae7b293596},
                                       .param1={.numberOfElements = 1},
                                    },
                                    {
                                       .dataType={pearlrt::IODataEntry::BIT,1},
                                       .dataPtr={.outData=&_simulation_is_running},
                                       .param1={.numberOfElements = 1},
                                    },
                                    {
                                       .dataType={pearlrt::IODataEntry::CHAR,1},
                                       .dataPtr={.outData=&CONST_CHARACTER_9302be52_a04f_403e_bc3c_1dae7b293596},
                                       .param1={.numberOfElements = 1},
                                    },
                                    {
                                       .dataType={pearlrt::IODataEntry::FLOAT,23},
                                       .dataPtr={.outData=&_screwmotor_current_rpm},
                                       .param1={.numberOfElements = 1},
                                    },
                                    {
                                       .dataType={pearlrt::IODataEntry::CHAR,1},
                                       .dataPtr={.outData=&CONST_CHARACTER_9302be52_a04f_403e_bc3c_1dae7b293596},
                                       .param1={.numberOfElements = 1},
                                    },
                                    {
                                       .dataType={pearlrt::IODataEntry::FIXED,31},
                                       .dataPtr={.outData=&_screwmotor_activations},
                                       .param1={.numberOfElements = 1},
                                    },
                                    {
                                       .dataType={pearlrt::IODataEntry::CHAR,1},
                                       .dataPtr={.outData=&CONST_CHARACTER_9302be52_a04f_403e_bc3c_1dae7b293596},
                                       .param1={.numberOfElements = 1},
                                    },
                                    {
                                       .dataType={pearlrt::IODataEntry::FLOAT,23},
                                       .dataPtr={.outData=&_spoolermotor_current_rpm},
                                       .param1={.numberOfElements = 1},
                                    },
                                    {
                                       .dataType={pearlrt::IODataEntry::CHAR,1},
                                       .dataPtr={.outData=&CONST_CHARACTER_9302be52_a04f_403e_bc3c_1dae7b293596},
                                       .param1={.numberOfElements = 1},
                                    },
                                    {
                                       .dataType={pearlrt::IODataEntry::FIXED,31},
                                       .dataPtr={.outData=&_spoolermotor_activations},
                                       .param1={.numberOfElements = 1},
                                    },
                                    {
                                       .dataType={pearlrt::IODataEntry::CHAR,1},
                                       .dataPtr={.outData=&CONST_CHARACTER_9302be52_a04f_403e_bc3c_1dae7b293596},
                                       .param1={.numberOfElements = 1},
                                    },
                                    {
                                       .dataType={pearlrt::IODataEntry::FLOAT,23},
                                       .dataPtr={.outData=&_screwheater_current_pwm},
                                       .param1={.numberOfElements = 1},
                                    },
                                    {
                                       .dataType={pearlrt::IODataEntry::CHAR,1},
                                       .dataPtr={.outData=&CONST_CHARACTER_9302be52_a04f_403e_bc3c_1dae7b293596},
                                       .param1={.numberOfElements = 1},
                                    },
                                    {
                                       .dataType={pearlrt::IODataEntry::FLOAT,23},
                                       .dataPtr={.outData=&_diameter_sensor},
                                       .param1={.numberOfElements = 1},
                                    },
                                    {
                                       .dataType={pearlrt::IODataEntry::CHAR,1},
                                       .dataPtr={.outData=&CONST_CHARACTER_9302be52_a04f_403e_bc3c_1dae7b293596},
                                       .param1={.numberOfElements = 1},
                                    },
                                    {
                                       .dataType={pearlrt::IODataEntry::FLOAT,23},
                                       .dataPtr={.outData=&_temperature_sensor},
                                       .param1={.numberOfElements = 1},
                                    },
                                    {
                                       .dataType={pearlrt::IODataEntry::CHAR,1},
                                       .dataPtr={.outData=&CONST_CHARACTER_9302be52_a04f_403e_bc3c_1dae7b293596},
                                       .param1={.numberOfElements = 1},
                                    }
                                  };

                                  pearlrt::IODataList dataList = {
                                    .nbrOfEntries=sizeof(dataEntries)/sizeof(dataEntries[0]),
                                    .entry=dataEntries};
                                  static pearlrt::IOFormatList formatList = {
                                    .nbrOfEntries=sizeof(formatEntries)/sizeof(formatEntries[0]),
                                    .entry=formatEntries};

                                  strDation.convertTo(me, &dataList , &formatList);
                               }

                               me->setLocation(228, "SimFilExtruder.prl");
                               {
                               pearlrt::Semaphore *tmp_0[] = {&_sema}; 
                                  	 	
                                 pearlrt::Semaphore::release( me, 1, tmp_0);
                                   


                               }
                               me->setLocation(229, "SimFilExtruder.prl");
                               _log( me, pearlrt::RefCharacter(_line));

                               me->setLocation(230, "SimFilExtruder.prl");
                                   me->resume( pearlrt::Task::AFTER,
                                         /* at     */  pearlrt::Clock(),
                                         /* after  */  CONST_DURATION_P_0_0_1_0,
                               	  /* when   */  0
                                          );






            } 
    } // REPEAT 





}
DCLTASK(_simulation, (pearlrt::Prio( (pearlrt::Fixed<15>)CONST_FIXED_P_10_4)), ((pearlrt::BitString<1>)0)) {
    pearlrt::Clock  _current_clock ;
    pearlrt::Float<23>  _dt ;
    pearlrt::Character<80>  _msg ;
    pearlrt::Fixed<31>  _err ;

    me->setLocation(440, "SimFilExtruder.prl");
    {   // REPEAT 
            while ( 1 )
            {

                if (!(_simulation_is_running.getBoolean()))
                    break;

                 
                               me->setLocation(442, "SimFilExtruder.prl");
                               {
                               pearlrt::Semaphore *tmp_0[] = {&_sema}; 
                                  	 	
                                  pearlrt::Semaphore::request( me, 1, tmp_0);
                                   


                               }
                               me->setLocation(443, "SimFilExtruder.prl");
                               _current_clock = pearlrt::Clock::now();

                               me->setLocation(444, "SimFilExtruder.prl");
                               _dt = (((_current_clock-_last_update)))/((CONST_DURATION_P_0_0_0_001));

                               me->setLocation(445, "SimFilExtruder.prl");
                               _screwmotor_update( me, _dt);

                               me->setLocation(446, "SimFilExtruder.prl");
                               _screwheater_update( me, _dt);

                               me->setLocation(447, "SimFilExtruder.prl");
                               _spoolermotor_update( me, _dt);

                               me->setLocation(448, "SimFilExtruder.prl");
                               _diameter_sensor_update( me, _dt);

                               me->setLocation(449, "SimFilExtruder.prl");
                               _temperature_sensor_update( me, _dt);

                               me->setLocation(450, "SimFilExtruder.prl");
                               {
                               pearlrt::Semaphore *tmp_0[] = {&_sema}; 
                                  	 	
                                 pearlrt::Semaphore::release( me, 1, tmp_0);
                                   


                               }
                               me->setLocation(452, "SimFilExtruder.prl");
                               _last_update = _current_clock;

                               me->setLocation(454, "SimFilExtruder.prl");
                                   me->resume( pearlrt::Task::AFTER,
                                         /* at     */  pearlrt::Clock(),
                                         /* after  */  CONST_DURATION_P_0_0_0_01,
                               	  /* when   */  0
                                          );






            } 
    } // REPEAT 


    me->setLocation(458, "SimFilExtruder.prl");
    _log( me, pearlrt::RefCharacter(CONST_CHARACTER_037b8fbf_3121_42ba_b578_98cf37392559));




}




}
