#include <PearlIncludes.h>

namespace ns_SimWatertank {
/////////////////////////////////////////////////////////////////////////////
// PROLOGUE
/////////////////////////////////////////////////////////////////////////////


static const char* filename = (char*) "SimWatertank.prl";



/////////////////////////////////////////////////////////////////////////////
// CONSTANT POOL
/////////////////////////////////////////////////////////////////////////////
static /*const*/ pearlrt::Fixed<1>         CONST_FIXED_P_1_1(1);
static /*const*/ pearlrt::Fixed<1>         CONST_FIXED_P_0_1(0);
static /*const*/ pearlrt::Fixed<11>         CONST_FIXED_P_2000_11(2000);
static /*const*/ pearlrt::Fixed<7>         CONST_FIXED_P_100_7(100);
static /*const*/ pearlrt::Fixed<9>         CONST_FIXED_P_500_9(500);
static /*const*/ pearlrt::Fixed<10>         CONST_FIXED_P_700_10(700);
static /*const*/ pearlrt::Fixed<4>         CONST_FIXED_P_10_4(10);
static /*const*/ pearlrt::Fixed<4>         CONST_FIXED_P_15_4(15);
static /*const*/ pearlrt::Fixed<5>         CONST_FIXED_P_25_5(25);
static /*const*/ pearlrt::Fixed<3>         CONST_FIXED_P_5_3(5);
static /*const*/ pearlrt::Fixed<5>         CONST_FIXED_P_30_5(30);
static /*const*/ pearlrt::Fixed<4>         CONST_FIXED_P_12_4(12);
static /*const*/ pearlrt::Fixed<2>         CONST_FIXED_P_3_2(3);
static /*const*/ pearlrt::Fixed<5>         CONST_FIXED_P_20_5(20);
static /*const*/ pearlrt::Fixed<2>         CONST_FIXED_P_2_2(2);
static /*const*/ pearlrt::Fixed<10>         CONST_FIXED_P_1000_10(1000);
static /*const*/ pearlrt::Fixed<3>         CONST_FIXED_P_4_3(4);
static /*const*/ pearlrt::Fixed<6>         CONST_FIXED_P_60_6(60);
static /*const*/ pearlrt::Fixed<31>         CONST_FIXED_P_0_31(0);
static /*const*/ pearlrt::Fixed<31>         CONST_FIXED_P_1_31(1);
static /*const*/ pearlrt::Fixed<31>         CONST_FIXED_N_1_31(-1);
static /*const*/ pearlrt::Fixed<7>         CONST_FIXED_P_80_7(80);
static /*const*/ pearlrt::Float<23>         CONST_FLOAT_P_3_1415926_23(3.1415926);
static /*const*/ pearlrt::Float<23>         CONST_FLOAT_P_1000_0_23(1000.0);
static /*const*/ pearlrt::Float<23>         CONST_FLOAT_P_9_81_23(9.81);
static /*const*/ pearlrt::Float<23>         CONST_FLOAT_P_2_0_23(2.0);
static /*const*/ pearlrt::Float<23>         CONST_FLOAT_P_1_0_23(1.0);
static /*const*/ pearlrt::Float<23>         CONST_FLOAT_P_0_06_23(0.06);
static /*const*/ pearlrt::Float<23>         CONST_FLOAT_P_0_0_23(0.0);
static /*const*/ pearlrt::Float<23>         CONST_FLOAT_P_50000_0_23(50000.0);
static /*const*/ pearlrt::Float<23>         CONST_FLOAT_P_0_01_23(0.01);
static /*const*/ pearlrt::Float<23>         CONST_FLOAT_P_45_0_23(45.0);
static /*const*/ pearlrt::Float<23>         CONST_FLOAT_P_0_001_23(0.001);
static /*const*/ pearlrt::Character<7>         CONST_CHARACTER_4311989b_d76f_4225_8773_762462d29368("sim.log");
static /*const*/ pearlrt::Character<19>         CONST_CHARACTER_15612c53_9906_4bb1_a599_faff991464fd("STARTING SIMULATION");
static /*const*/ pearlrt::Character<27>         CONST_CHARACTER_dec48b0b_faea_48b2_9a1e_8af3052b57b0("REQUEST STOPPING SIMULATION");
static /*const*/ pearlrt::Character<6>         CONST_CHARACTER_833cba2a_7294_4767_b8dc_2ed41efa16b0(" SIM: ");
static /*const*/ pearlrt::Character<12>         CONST_CHARACTER_aa7a1631_29ce_4ba6_ae5c_3ecf3a195dd4("VALVE OPENED");
static /*const*/ pearlrt::Character<12>         CONST_CHARACTER_d95f0bdd_8109_4bf7_bbab_a673852b7c3c("VALVE CLOSED");
static /*const*/ pearlrt::Character<7>         CONST_CHARACTER_3076db1d_54bf_4434_b591_e07ea3be084e("Fields:");
static /*const*/ pearlrt::Character<32>         CONST_CHARACTER_ee11c121_5c58_4ff3_b5da_f03bd26efcf7("|#1|#2|#3|#4|#5|#6|#7|#8|#9|#10|");
static /*const*/ pearlrt::Character<26>         CONST_CHARACTER_fd5d3392_30a6_40ff_9acb_835887331591("# 1: simulation_is_running");
static /*const*/ pearlrt::Character<21>         CONST_CHARACTER_6e71b13d_399f_4850_bca0_49d0fa4f835d("# 2: pump_current_rpm");
static /*const*/ pearlrt::Character<21>         CONST_CHARACTER_0cc0b135_6f47_499d_9b09_664d7b110e94("# 3: pump_activations");
static /*const*/ pearlrt::Character<26>         CONST_CHARACTER_c344fca9_e144_482e_b0bb_759e5d3fb48e("# 4: pump_output_flow_rate");
static /*const*/ pearlrt::Character<22>         CONST_CHARACTER_ef097f00_dde0_4909_aee3_84628f942361("# 5: pressure_sensor_1");
static /*const*/ pearlrt::Character<18>         CONST_CHARACTER_f8347884_c173_45f8_a744_f72e7ad53eef("# 6: valve_enabled");
static /*const*/ pearlrt::Character<22>         CONST_CHARACTER_10910048_e9e7_438e_a374_1d40e54b7db9("# 7: pressure_sensor_2");
static /*const*/ pearlrt::Character<25>         CONST_CHARACTER_f51e38f1_6e97_42c9_9778_d3013bcbf924("# 8: float_switch_enabled");
static /*const*/ pearlrt::Character<33>         CONST_CHARACTER_a74a5c1e_63ca_41ec_a203_a9e2e67d9dd0("# 9: current_consumer_dissipation");
static /*const*/ pearlrt::Character<20>         CONST_CHARACTER_b27a9bf1_4eec_4683_9a13_8703aed05b71("#10: tank_fill_level");
static /*const*/ pearlrt::Character<76>         CONST_CHARACTER_603a8313_56f6_460c_a2dd_22c39a262067("----------------------------------------------------------------------------");
static /*const*/ pearlrt::Character<71>         CONST_CHARACTER_969c3946_8b2e_47a4_8829_9edb183af9a9(" 1    2    3          4          5 6          7 8         9          10");
static /*const*/ pearlrt::Character<1>         CONST_CHARACTER_55fd5723_1cd1_406d_bbb4_823096481e55("|");
static /*const*/ pearlrt::Character<25>         CONST_CHARACTER_6cf9609a_363d_4e0b_8a12_867504f48715("ALERT: WATERTANK OVERRUN!");
static /*const*/ pearlrt::Character<26>         CONST_CHARACTER_b15d81df_3faf_42d4_b80f_0c9d0af99c39("ALERT: WATERTANK UNDERRUN!");
static /*const*/ pearlrt::Character<23>         CONST_CHARACTER_fdb06380_71f3_4730_94df_c54ec60b568b("SIMULATION TASK STOPPED");
static /*const*/ pearlrt::BitString<1>         CONST_BITSTRING_1_0(0x0);
static /*const*/ pearlrt::BitString<1>         CONST_BITSTRING_1_1(0x1);
static /*const*/ pearlrt::Duration          CONST_DURATION_P_0_0_1_0(1,0,1);
static /*const*/ pearlrt::Duration          CONST_DURATION_P_0_0_10_0(10,0,1);
static /*const*/ pearlrt::Duration          CONST_DURATION_P_0_0_5_0(5,0,1);
static /*const*/ pearlrt::Duration          CONST_DURATION_P_0_0_0_001(0,1000,1);
static /*const*/ pearlrt::Duration          CONST_DURATION_P_0_0_0_01(0,10000,1);

/////////////////////////////////////////////////////////////////////////////
// TASK SPECIFIERS
/////////////////////////////////////////////////////////////////////////////
extern pearlrt::Task task_check_tank_status;


extern pearlrt::Task task_simulation;


extern pearlrt::Task task_log_status;


extern pearlrt::Task task_consumer;

}



/////////////////////////////////////////////////////////////////////////////
// STRUCTURE FORWARD DECLARATIONS
/////////////////////////////////////////////////////////////////////////////

/////////////////////////////////////////////////////////////////////////////
// STRUCTURE DECLARATIONS
/////////////////////////////////////////////////////////////////////////////

namespace ns_SimWatertank {


/////////////////////////////////////////////////////////////////////////////
// PROBLEM PART
/////////////////////////////////////////////////////////////////////////////

/////////////////////////////////////////////////////////////////////////////
// ARRAY DESCRIPTORS
/////////////////////////////////////////////////////////////////////////////
static pearlrt::ArrayDescriptor< 1 > ad_1_1_5 = {1,LIMITS({{1,5,1}})};


/////////////////////////////////////////////////////////////////////////////
// PROCEDURE SPECIFICATIONS
/////////////////////////////////////////////////////////////////////////////
/* public */ pearlrt::Float<23> _get_watertank_pressure(pearlrt::Task *me);

/* public */ void _open_valve(pearlrt::Task *me);

static void _closeLogfile(pearlrt::Task *me);

 /* public */ pearlrt::Float<23> _get_watertank_capacity(pearlrt::Task *me);

/* public */ void _stop_simulation(pearlrt::Task *me);

/* public */ void _set_pump_rotational_speed(pearlrt::Task *me, pearlrt::Fixed<15> );

static void _pressure_sensor_2_update(pearlrt::Task *me, pearlrt::Float<23> );

 /* public */ pearlrt::Float<23> _get_level(pearlrt::Task *me);

/* public */ pearlrt::Fixed<31> _get_pump_rotational_speed(pearlrt::Task *me);

static void _pump_update(pearlrt::Task *me, pearlrt::Float<23> );

 /* public */ void _start_simulation(pearlrt::Task *me);

static void _openLogfile(pearlrt::Task *me, pearlrt::RefCharacter , pearlrt::BitString<1> );

 static void _log(pearlrt::Task *me, pearlrt::RefCharacter );

 /* public */ pearlrt::Float<23> _get_pressure_sensor_1(pearlrt::Task *me);

/* public */ pearlrt::BitString<1> _get_valve_state(pearlrt::Task *me);

/* public */ pearlrt::Float<23> _get_pressure_sensor_2(pearlrt::Task *me);

/* public */ pearlrt::BitString<1> _get_float_switch_state(pearlrt::Task *me);

static void _tank_update(pearlrt::Task *me, pearlrt::Float<23> );

 /* public */ void _close_valve(pearlrt::Task *me);

/* public */ pearlrt::Float<23> _get_pump_pressure(pearlrt::Task *me);

static void _pressure_sensor_1_update(pearlrt::Task *me, pearlrt::Float<23> );

 static void _float_switch_update(pearlrt::Task *me, pearlrt::Float<23> );

 


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
static pearlrt::Character<7>  _fileName (CONST_CHARACTER_4311989b_d76f_4225_8773_762462d29368); 
static pearlrt::Character<80>  _message ; 
static const pearlrt::Float<23>  _pi (CONST_FLOAT_P_3_1415926_23); 
static pearlrt::BitString<1>  _simulation_is_running (CONST_BITSTRING_1_0); 
static pearlrt::Semaphore _sema ( 1,"_sema");
 
static pearlrt::Fixed<31>  _step (CONST_FIXED_P_0_1); 
static const pearlrt::Float<23>  _rho (CONST_FLOAT_P_1000_0_23); 
static const pearlrt::Float<23>  _gravity (CONST_FLOAT_P_9_81_23); 
static const pearlrt::Float<23>  _tank_length (CONST_FLOAT_P_2_0_23); 
static const pearlrt::Float<23>  _tank_width (CONST_FLOAT_P_2_0_23); 
static const pearlrt::Float<23>  _tank_height (CONST_FLOAT_P_2_0_23); 
static pearlrt::Float<23>  _tank_fill_level (CONST_FLOAT_P_1_0_23); 
static pearlrt::Float<23>  _pipe_diameter (CONST_FLOAT_P_0_06_23); 
static pearlrt::Fixed<15>  _pump_max_rpm (CONST_FIXED_P_2000_11); 
static pearlrt::Fixed<15>  _pump_idle_rpm (CONST_FIXED_P_100_7); 
static pearlrt::Fixed<15>  _pump_spin_up_speed (CONST_FIXED_P_500_9); 
static pearlrt::Fixed<15>  _pump_spin_down_speed (CONST_FIXED_P_700_10); 
static pearlrt::Fixed<15>  _pump_requested_rpm (CONST_FIXED_P_0_1); 
static pearlrt::Float<23>  _pump_current_rpm (CONST_FLOAT_P_0_0_23); 
static pearlrt::Fixed<31>  _pump_activations (CONST_FIXED_P_0_1); 
static pearlrt::Float<23>  _pump_output_flow_rate (CONST_FLOAT_P_0_0_23); 
static pearlrt::Float<23>  _pressure_sensor_1 (CONST_FLOAT_P_0_0_23); 
static pearlrt::Float<23>  _pressure_sensor_2 (CONST_FLOAT_P_0_0_23); 
static pearlrt::BitString<1>  _valve_enabled (CONST_BITSTRING_1_0); 
static pearlrt::BitString<1>  _float_switch_enabled (CONST_BITSTRING_1_0); 
static const pearlrt::Float<23>  _max_pressure (CONST_FLOAT_P_50000_0_23); 
static const pearlrt::Float<23>  _min_consumer_dissipation (CONST_FLOAT_P_0_0_23); 
static const pearlrt::Float<23>  _max_consumer_dissipation (CONST_FLOAT_P_0_01_23); 
static pearlrt::Float<23>  _current_consumer_dissipation (CONST_FLOAT_P_0_0_23); 
static pearlrt::Clock  _last_update ; 
static pearlrt::Fixed<31> data_consumer_activations[5] = {CONST_FIXED_P_10_4,CONST_FIXED_P_15_4,
CONST_FIXED_P_25_5,CONST_FIXED_P_5_3,CONST_FIXED_P_30_5};
 static pearlrt::Array< pearlrt::Fixed<31> > _consumer_activations((pearlrt::ArrayDescriptor<0> *)&ad_1_1_5, data_consumer_activations);
 
static pearlrt::Fixed<15>  _consumer_current_activation (CONST_FIXED_P_1_1); 







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
    me->setLocation(131, "SimWatertank.prl");
    _openLogfile( me, pearlrt::RefCharacter(_fileName), CONST_BITSTRING_1_0);

    me->setLocation(132, "SimWatertank.prl");
    _log( me, pearlrt::RefCharacter(CONST_CHARACTER_15612c53_9906_4bb1_a599_faff991464fd));

    me->setLocation(133, "SimWatertank.prl");
    _simulation_is_running = CONST_BITSTRING_1_1;

    me->setLocation(134, "SimWatertank.prl");
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



    me->setLocation(135, "SimWatertank.prl");
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



    me->setLocation(136, "SimWatertank.prl");
        task_consumer.activate( me,
    	     pearlrt::Task::AFTER,
    	    /* prio   */  pearlrt::Prio(),
    	    /* at     */  pearlrt::Clock(),
    	    /* after  */  CONST_DURATION_P_0_0_10_0,
    	    /* all    */  pearlrt::Duration(),
    	    /* until  */  pearlrt::Clock(),
    	    /* during */  pearlrt::Duration(),
    	    /* when   */  0
                       );



    me->setLocation(137, "SimWatertank.prl");
        task_check_tank_status.activate( me,
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
    me->setLocation(142, "SimWatertank.prl");
    _log( me, pearlrt::RefCharacter(CONST_CHARACTER_dec48b0b_faea_48b2_9a1e_8af3052b57b0));

    me->setLocation(143, "SimWatertank.prl");
    _simulation_is_running = CONST_BITSTRING_1_0;




    me->setLocation(locationLine,locationFile);
}
static void
_openLogfile(pearlrt::Task *me, pearlrt::RefCharacter _logFileName, pearlrt::BitString<1> _addToExistingFile)
{
    const char* locationFile = me->getLocationFile();
    int locationLine = me->getLocationLine(); 
    pearlrt::Character<30>  _fn ;

    me->setLocation(150, "SimWatertank.prl");
    _logFileName.store(_fn);

    me->setLocation(152, "SimWatertank.prl");
    	if (_addToExistingFile.getBoolean()) {
    	    me->setLocation(153, "SimWatertank.prl");
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


    	    me->setLocation(154, "SimWatertank.prl");
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
    	    me->setLocation(158, "SimWatertank.prl");
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


    	    me->setLocation(159, "SimWatertank.prl");
    	      _logFile->dationClose(pearlrt::Dation::CAN, (pearlrt::Fixed<15>*) 0);


    	    me->setLocation(163, "SimWatertank.prl");
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
    me->setLocation(169, "SimWatertank.prl");
      _logFile->dationClose(0, (pearlrt::Fixed<15>*) 0);





    me->setLocation(locationLine,locationFile);
}
 static void
_log(pearlrt::Task *me, pearlrt::RefCharacter _line)
{
    const char* locationFile = me->getLocationFile();
    int locationLine = me->getLocationLine(); 
    me->setLocation(174, "SimWatertank.prl");
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
            .dataPtr={.outData=&CONST_CHARACTER_833cba2a_7294_4767_b8dc_2ed41efa16b0},
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





    me->setLocation(locationLine,locationFile);
}
 /* public */ pearlrt::Float<23>
_get_level(pearlrt::Task *me)
{
    const char* locationFile = me->getLocationFile();
    int locationLine = me->getLocationLine(); 
    me->setLocation(199, "SimWatertank.prl");
    	   me->setLocation(locationLine,locationFile);
    	   return (_tank_fill_level);





    me->setLocation(locationLine,locationFile);
}
/* public */ pearlrt::Fixed<31>
_get_pump_rotational_speed(pearlrt::Task *me)
{
    const char* locationFile = me->getLocationFile();
    int locationLine = me->getLocationLine(); 
    pearlrt::Fixed<31>  _rpm ;

    me->setLocation(205, "SimWatertank.prl");
    {
    pearlrt::Semaphore *tmp_0[] = {&_sema}; 
       	 	
       pearlrt::Semaphore::request( me, 1, tmp_0);
        


    }
    me->setLocation(206, "SimWatertank.prl");
    _rpm = (_pump_current_rpm).entier();

    me->setLocation(207, "SimWatertank.prl");
    {
    pearlrt::Semaphore *tmp_0[] = {&_sema}; 
       	 	
      pearlrt::Semaphore::release( me, 1, tmp_0);
        


    }
    me->setLocation(208, "SimWatertank.prl");
    	   me->setLocation(locationLine,locationFile);
    	   return (_rpm);





    me->setLocation(locationLine,locationFile);
}
/* public */ void
_set_pump_rotational_speed(pearlrt::Task *me, pearlrt::Fixed<15> _rpm)
{
    const char* locationFile = me->getLocationFile();
    int locationLine = me->getLocationLine(); 
    me->setLocation(213, "SimWatertank.prl");
    {
    pearlrt::Semaphore *tmp_0[] = {&_sema}; 
       	 	
       pearlrt::Semaphore::request( me, 1, tmp_0);
        


    }
    me->setLocation(214, "SimWatertank.prl");
    _pump_requested_rpm = _rpm;

    me->setLocation(215, "SimWatertank.prl");
    {
    pearlrt::Semaphore *tmp_0[] = {&_sema}; 
       	 	
      pearlrt::Semaphore::release( me, 1, tmp_0);
        


    }



    me->setLocation(locationLine,locationFile);
}
/* public */ pearlrt::Float<23>
_get_pump_pressure(pearlrt::Task *me)
{
    const char* locationFile = me->getLocationFile();
    int locationLine = me->getLocationLine(); 
    me->setLocation(220, "SimWatertank.prl");
    	   me->setLocation(locationLine,locationFile);
    	   return (CONST_FLOAT_P_45_0_23);





    me->setLocation(locationLine,locationFile);
}
/* public */ void
_open_valve(pearlrt::Task *me)
{
    const char* locationFile = me->getLocationFile();
    int locationLine = me->getLocationLine(); 
    me->setLocation(225, "SimWatertank.prl");
    {
    pearlrt::Semaphore *tmp_0[] = {&_sema}; 
       	 	
       pearlrt::Semaphore::request( me, 1, tmp_0);
        


    }
    me->setLocation(226, "SimWatertank.prl");
    _valve_enabled = CONST_BITSTRING_1_1;

    me->setLocation(227, "SimWatertank.prl");
    {
    pearlrt::Semaphore *tmp_0[] = {&_sema}; 
       	 	
      pearlrt::Semaphore::release( me, 1, tmp_0);
        


    }
    me->setLocation(228, "SimWatertank.prl");
    _log( me, pearlrt::RefCharacter(CONST_CHARACTER_aa7a1631_29ce_4ba6_ae5c_3ecf3a195dd4));




    me->setLocation(locationLine,locationFile);
}
/* public */ void
_close_valve(pearlrt::Task *me)
{
    const char* locationFile = me->getLocationFile();
    int locationLine = me->getLocationLine(); 
    me->setLocation(233, "SimWatertank.prl");
    {
    pearlrt::Semaphore *tmp_0[] = {&_sema}; 
       	 	
       pearlrt::Semaphore::request( me, 1, tmp_0);
        


    }
    me->setLocation(234, "SimWatertank.prl");
    _valve_enabled = CONST_BITSTRING_1_0;

    me->setLocation(235, "SimWatertank.prl");
    {
    pearlrt::Semaphore *tmp_0[] = {&_sema}; 
       	 	
      pearlrt::Semaphore::release( me, 1, tmp_0);
        


    }
    me->setLocation(236, "SimWatertank.prl");
    _log( me, pearlrt::RefCharacter(CONST_CHARACTER_d95f0bdd_8109_4bf7_bbab_a673852b7c3c));




    me->setLocation(locationLine,locationFile);
}
/* public */ pearlrt::BitString<1>
_get_valve_state(pearlrt::Task *me)
{
    const char* locationFile = me->getLocationFile();
    int locationLine = me->getLocationLine(); 
    pearlrt::BitString<1>  _state ;

    me->setLocation(244, "SimWatertank.prl");
    {
    pearlrt::Semaphore *tmp_0[] = {&_sema}; 
       	 	
       pearlrt::Semaphore::request( me, 1, tmp_0);
        


    }
    me->setLocation(245, "SimWatertank.prl");
    _state = _valve_enabled;

    me->setLocation(246, "SimWatertank.prl");
    {
    pearlrt::Semaphore *tmp_0[] = {&_sema}; 
       	 	
      pearlrt::Semaphore::release( me, 1, tmp_0);
        


    }
    me->setLocation(247, "SimWatertank.prl");
    	   me->setLocation(locationLine,locationFile);
    	   return (_state);





    me->setLocation(locationLine,locationFile);
}
/* public */ pearlrt::Float<23>
_get_watertank_capacity(pearlrt::Task *me)
{
    const char* locationFile = me->getLocationFile();
    int locationLine = me->getLocationLine(); 
    me->setLocation(253, "SimWatertank.prl");
    	   me->setLocation(locationLine,locationFile);
    	   return (_tank_length*_tank_width*_tank_height);





    me->setLocation(locationLine,locationFile);
}
/* public */ pearlrt::Float<23>
_get_watertank_pressure(pearlrt::Task *me)
{
    const char* locationFile = me->getLocationFile();
    int locationLine = me->getLocationLine(); 
    pearlrt::Float<23>  _p (CONST_FLOAT_P_0_0_23);

    me->setLocation(259, "SimWatertank.prl");
    {
    pearlrt::Semaphore *tmp_0[] = {&_sema}; 
       	 	
       pearlrt::Semaphore::request( me, 1, tmp_0);
        


    }
    me->setLocation(260, "SimWatertank.prl");
    _p = _pressure_sensor_2;

    me->setLocation(261, "SimWatertank.prl");
    {
    pearlrt::Semaphore *tmp_0[] = {&_sema}; 
       	 	
      pearlrt::Semaphore::release( me, 1, tmp_0);
        


    }
    me->setLocation(263, "SimWatertank.prl");
    	   me->setLocation(locationLine,locationFile);
    	   return (_p);





    me->setLocation(locationLine,locationFile);
}
/* public */ pearlrt::BitString<1>
_get_float_switch_state(pearlrt::Task *me)
{
    const char* locationFile = me->getLocationFile();
    int locationLine = me->getLocationLine(); 
    pearlrt::BitString<1>  _state ;

    me->setLocation(271, "SimWatertank.prl");
    {
    pearlrt::Semaphore *tmp_0[] = {&_sema}; 
       	 	
       pearlrt::Semaphore::request( me, 1, tmp_0);
        


    }
    me->setLocation(272, "SimWatertank.prl");
    _state = _float_switch_enabled;

    me->setLocation(273, "SimWatertank.prl");
    {
    pearlrt::Semaphore *tmp_0[] = {&_sema}; 
       	 	
      pearlrt::Semaphore::release( me, 1, tmp_0);
        


    }
    me->setLocation(274, "SimWatertank.prl");
    	   me->setLocation(locationLine,locationFile);
    	   return (_state);





    me->setLocation(locationLine,locationFile);
}
/* public */ pearlrt::Float<23>
_get_pressure_sensor_1(pearlrt::Task *me)
{
    const char* locationFile = me->getLocationFile();
    int locationLine = me->getLocationLine(); 
    pearlrt::Float<23>  _pressure ;

    me->setLocation(280, "SimWatertank.prl");
    {
    pearlrt::Semaphore *tmp_0[] = {&_sema}; 
       	 	
       pearlrt::Semaphore::request( me, 1, tmp_0);
        


    }
    me->setLocation(281, "SimWatertank.prl");
    _pressure = _pressure_sensor_1;

    me->setLocation(282, "SimWatertank.prl");
    {
    pearlrt::Semaphore *tmp_0[] = {&_sema}; 
       	 	
      pearlrt::Semaphore::release( me, 1, tmp_0);
        


    }
    me->setLocation(283, "SimWatertank.prl");
    	   me->setLocation(locationLine,locationFile);
    	   return (_pressure);





    me->setLocation(locationLine,locationFile);
}
/* public */ pearlrt::Float<23>
_get_pressure_sensor_2(pearlrt::Task *me)
{
    const char* locationFile = me->getLocationFile();
    int locationLine = me->getLocationLine(); 
    pearlrt::Float<23>  _pressure ;

    me->setLocation(289, "SimWatertank.prl");
    {
    pearlrt::Semaphore *tmp_0[] = {&_sema}; 
       	 	
       pearlrt::Semaphore::request( me, 1, tmp_0);
        


    }
    me->setLocation(290, "SimWatertank.prl");
    _pressure = _pressure_sensor_2;

    me->setLocation(291, "SimWatertank.prl");
    {
    pearlrt::Semaphore *tmp_0[] = {&_sema}; 
       	 	
      pearlrt::Semaphore::release( me, 1, tmp_0);
        


    }
    me->setLocation(292, "SimWatertank.prl");
    	   me->setLocation(locationLine,locationFile);
    	   return (_pressure);





    me->setLocation(locationLine,locationFile);
}
static void
_pump_update(pearlrt::Task *me, pearlrt::Float<23> _dt)
{
    const char* locationFile = me->getLocationFile();
    int locationLine = me->getLocationLine(); 
    me->setLocation(360, "SimWatertank.prl");
    	if ((_pump_current_rpm == CONST_FIXED_P_0_1).bitAnd((_pump_requested_rpm > 
    CONST_FLOAT_P_0_0_23)).getBoolean()) {
    	    me->setLocation(362, "SimWatertank.prl");
    	    _pump_activations = _pump_activations+CONST_FIXED_P_1_1;



    	}


    me->setLocation(365, "SimWatertank.prl");
    	if ((_pump_current_rpm < _pump_requested_rpm).getBoolean()) {
    	    me->setLocation(367, "SimWatertank.prl");
    	    _pump_current_rpm = _pump_current_rpm+((((_dt*_pump_spin_up_speed))/((pearlrt::Float<23>)(
    	    CONST_FIXED_P_1000_10)))).entier();

    	    me->setLocation(368, "SimWatertank.prl");
    	    	if ((_pump_current_rpm > _pump_requested_rpm).getBoolean()) {
    	    	    me->setLocation(370, "SimWatertank.prl");
    	    	    _pump_current_rpm = _pump_requested_rpm;



    	    	}




    	}


    me->setLocation(374, "SimWatertank.prl");
    	if ((_pump_current_rpm > _pump_requested_rpm).getBoolean()) {
    	    me->setLocation(376, "SimWatertank.prl");
    	    _pump_current_rpm = _pump_current_rpm-((((_dt*_pump_spin_down_speed))/((pearlrt::Float<23>)(
    	    CONST_FIXED_P_1000_10)))).entier();

    	    me->setLocation(378, "SimWatertank.prl");
    	    	if ((_pump_current_rpm < CONST_FIXED_P_0_1).getBoolean()) {
    	    	    me->setLocation(380, "SimWatertank.prl");
    	    	    _pump_current_rpm = CONST_FIXED_P_0_1;



    	    	}




    	}


    me->setLocation(384, "SimWatertank.prl");
    	if ((_pump_current_rpm > _pump_max_rpm).getBoolean()) {
    	    me->setLocation(386, "SimWatertank.prl");
    	    _pump_current_rpm = _pump_max_rpm;



    	}


    me->setLocation(395, "SimWatertank.prl");
    _pump_output_flow_rate = (((_pi*_pipe_diameter.pow(CONST_FIXED_P_2_2)))/((pearlrt::Float<23>)(
    CONST_FIXED_P_4_3)))*(((_pump_current_rpm))/((pearlrt::Float<23>)(CONST_FIXED_P_60_6))
    );




    me->setLocation(locationLine,locationFile);
}
 static void
_pressure_sensor_1_update(pearlrt::Task *me, pearlrt::Float<23> _dt)
{
    const char* locationFile = me->getLocationFile();
    int locationLine = me->getLocationLine(); 
    pearlrt::Float<23>  _a (CONST_FLOAT_P_0_0_23);
    pearlrt::Float<23>  _v (CONST_FLOAT_P_0_0_23);
    pearlrt::Float<23>  _c (CONST_FLOAT_P_0_0_23);

    me->setLocation(424, "SimWatertank.prl");
    _a = ((_pi*_pipe_diameter.pow(CONST_FIXED_P_2_2)))/((pearlrt::Float<23>)(CONST_FIXED_P_4_3));

    me->setLocation(425, "SimWatertank.prl");
    _v = ((_pump_output_flow_rate))/((_a));

    me->setLocation(427, "SimWatertank.prl");
    	if ((_pump_current_rpm > CONST_FIXED_P_0_1).getBoolean()) {
    	    me->setLocation(429, "SimWatertank.prl");
    	    _c = (((CONST_FIXED_P_20_5*_rho*_gravity)))/((pearlrt::Float<23>)(CONST_FIXED_P_1000_10))
    	    +(((CONST_FIXED_P_1000_10*_v.pow(CONST_FIXED_P_2_2))))/((_pump_current_rpm));

    	    me->setLocation(430, "SimWatertank.prl");
    	    	if (_valve_enabled.getBoolean()) {
    	    	    me->setLocation(431, "SimWatertank.prl");
    	    	    _pressure_sensor_1 = _c;



    	    	} else {
    	    	    me->setLocation(433, "SimWatertank.prl");
    	    	    _pressure_sensor_1 = _pressure_sensor_1+_c*CONST_FLOAT_P_0_001_23;



    	    	}




    	} else {
    	    me->setLocation(436, "SimWatertank.prl");
    	    _pressure_sensor_1 = CONST_FLOAT_P_0_0_23;



    	}





    me->setLocation(locationLine,locationFile);
}
 static void
_pressure_sensor_2_update(pearlrt::Task *me, pearlrt::Float<23> _dt)
{
    const char* locationFile = me->getLocationFile();
    int locationLine = me->getLocationLine(); 
    me->setLocation(453, "SimWatertank.prl");
    _pressure_sensor_2 = (((_tank_fill_level))/(((_tank_width*_tank_length))))*_gravity
    *_rho;




    me->setLocation(locationLine,locationFile);
}
 static void
_tank_update(pearlrt::Task *me, pearlrt::Float<23> _dt)
{
    const char* locationFile = me->getLocationFile();
    int locationLine = me->getLocationLine(); 
    me->setLocation(463, "SimWatertank.prl");
    	if (_valve_enabled.getBoolean()) {
    	    me->setLocation(464, "SimWatertank.prl");
    	    _tank_fill_level = _tank_fill_level+((_pump_output_flow_rate))/((pearlrt::Float<23>)(
    	    CONST_FIXED_P_60_6))-(((_current_consumer_dissipation))/((pearlrt::Float<23>)(
    	    CONST_FIXED_P_100_7)));



    	} else {
    	    me->setLocation(468, "SimWatertank.prl");
    	    _tank_fill_level = _tank_fill_level-(((_current_consumer_dissipation))/((pearlrt::Float<23>)(
    	    CONST_FIXED_P_100_7)));



    	}


    me->setLocation(472, "SimWatertank.prl");
    	if ((_tank_fill_level >= _get_watertank_capacity( me)).getBoolean()) {
    	    me->setLocation(474, "SimWatertank.prl");
    	    _tank_fill_level = _get_watertank_capacity( me);



    	}


    me->setLocation(477, "SimWatertank.prl");
    	if ((_tank_fill_level < CONST_FLOAT_P_0_0_23).getBoolean()) {
    	    me->setLocation(479, "SimWatertank.prl");
    	    _tank_fill_level = CONST_FLOAT_P_0_0_23;



    	}





    me->setLocation(locationLine,locationFile);
}
 static void
_float_switch_update(pearlrt::Task *me, pearlrt::Float<23> _dt)
{
    const char* locationFile = me->getLocationFile();
    int locationLine = me->getLocationLine(); 
    me->setLocation(485, "SimWatertank.prl");
    _float_switch_enabled = (_tank_fill_level >= _get_watertank_capacity( me));




    me->setLocation(locationLine,locationFile);
}
 

/////////////////////////////////////////////////////////////////////////////
// TASK DECLARATIONS
/////////////////////////////////////////////////////////////////////////////
DCLTASK(_consumer, (pearlrt::Prio( (pearlrt::Fixed<15>)CONST_FIXED_P_20_5)), ((pearlrt::BitString<1>)0)) {
    pearlrt::Fixed<15>  _cstep (CONST_FIXED_P_0_1);

    me->setLocation(298, "SimWatertank.prl");
    {   // REPEAT 
            while ( 1 )
            {

                if (!(_simulation_is_running.getBoolean()))
                    break;

                 
                               me->setLocation(300, "SimWatertank.prl");
                               {
                               pearlrt::Semaphore *tmp_0[] = {&_sema}; 
                                  	 	
                                  pearlrt::Semaphore::request( me, 1, tmp_0);
                                   


                               }
                               me->setLocation(301, "SimWatertank.prl");
                               _current_consumer_dissipation = ((CONST_FIXED_P_10_4
                               *((((pearlrt::Float<23>)((CONST_FIXED_P_2_2*_cstep
                               ))).sin())*(((pearlrt::Float<23>)((CONST_FIXED_P_3_2
                               *_cstep))).cos())).abs()))/((pearlrt::Float<23>)(
                               CONST_FIXED_P_1000_10));

                               me->setLocation(302, "SimWatertank.prl");
                               _cstep = _cstep+CONST_FIXED_P_1_1;

                               me->setLocation(303, "SimWatertank.prl");
                               {
                               pearlrt::Semaphore *tmp_0[] = {&_sema}; 
                                  	 	
                                 pearlrt::Semaphore::release( me, 1, tmp_0);
                                   


                               }
                               me->setLocation(304, "SimWatertank.prl");
                                   me->resume( pearlrt::Task::AFTER,
                                         /* at     */  pearlrt::Clock(),
                                         /* after  */  CONST_DURATION_P_0_0_1_0,
                               	  /* when   */  0
                                          );






            } 
    } // REPEAT 





}
DCLTASK(_log_status, (pearlrt::Prio( (pearlrt::Fixed<15>)CONST_FIXED_P_20_5)), ((pearlrt::BitString<1>)0)) {
    pearlrt::Character<80>  _line ;

    me->setLocation(312, "SimWatertank.prl");
    _log( me, pearlrt::RefCharacter(CONST_CHARACTER_3076db1d_54bf_4434_b591_e07ea3be084e));

    me->setLocation(313, "SimWatertank.prl");
    _log( me, pearlrt::RefCharacter(CONST_CHARACTER_ee11c121_5c58_4ff3_b5da_f03bd26efcf7));

    me->setLocation(314, "SimWatertank.prl");
    _log( me, pearlrt::RefCharacter(CONST_CHARACTER_fd5d3392_30a6_40ff_9acb_835887331591));

    me->setLocation(315, "SimWatertank.prl");
    _log( me, pearlrt::RefCharacter(CONST_CHARACTER_6e71b13d_399f_4850_bca0_49d0fa4f835d));

    me->setLocation(316, "SimWatertank.prl");
    _log( me, pearlrt::RefCharacter(CONST_CHARACTER_0cc0b135_6f47_499d_9b09_664d7b110e94));

    me->setLocation(317, "SimWatertank.prl");
    _log( me, pearlrt::RefCharacter(CONST_CHARACTER_c344fca9_e144_482e_b0bb_759e5d3fb48e));

    me->setLocation(318, "SimWatertank.prl");
    _log( me, pearlrt::RefCharacter(CONST_CHARACTER_ef097f00_dde0_4909_aee3_84628f942361));

    me->setLocation(319, "SimWatertank.prl");
    _log( me, pearlrt::RefCharacter(CONST_CHARACTER_f8347884_c173_45f8_a744_f72e7ad53eef));

    me->setLocation(320, "SimWatertank.prl");
    _log( me, pearlrt::RefCharacter(CONST_CHARACTER_10910048_e9e7_438e_a374_1d40e54b7db9));

    me->setLocation(321, "SimWatertank.prl");
    _log( me, pearlrt::RefCharacter(CONST_CHARACTER_f51e38f1_6e97_42c9_9778_d3013bcbf924));

    me->setLocation(322, "SimWatertank.prl");
    _log( me, pearlrt::RefCharacter(CONST_CHARACTER_a74a5c1e_63ca_41ec_a203_a9e2e67d9dd0));

    me->setLocation(323, "SimWatertank.prl");
    _log( me, pearlrt::RefCharacter(CONST_CHARACTER_b27a9bf1_4eec_4683_9a13_8703aed05b71));

    me->setLocation(324, "SimWatertank.prl");
    _log( me, pearlrt::RefCharacter(CONST_CHARACTER_603a8313_56f6_460c_a2dd_22c39a262067));

    me->setLocation(325, "SimWatertank.prl");
    _log( me, pearlrt::RefCharacter(CONST_CHARACTER_969c3946_8b2e_47a4_8829_9edb183af9a9));

    me->setLocation(327, "SimWatertank.prl");
    {   // REPEAT 
            while ( 1 )
            {

                if (!(_simulation_is_running.getBoolean()))
                    break;

                 
                               me->setLocation(329, "SimWatertank.prl");
                               {
                               pearlrt::Semaphore *tmp_0[] = {&_sema}; 
                                  	 	
                                  pearlrt::Semaphore::request( me, 1, tmp_0);
                                   


                               }
                               me->setLocation(330, "SimWatertank.prl");
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
                                    .format=pearlrt::IOFormatEntry::B1w,
                                    .fp1={.f31=CONST_FIXED_P_1_1}
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
                                    .format=pearlrt::IOFormatEntry::B1w,
                                    .fp1={.f31=CONST_FIXED_P_1_1}
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
                                       .dataPtr={.outData=&CONST_CHARACTER_55fd5723_1cd1_406d_bbb4_823096481e55},
                                       .param1={.numberOfElements = 1},
                                    },
                                    {
                                       .dataType={pearlrt::IODataEntry::BIT,1},
                                       .dataPtr={.outData=&_simulation_is_running},
                                       .param1={.numberOfElements = 1},
                                    },
                                    {
                                       .dataType={pearlrt::IODataEntry::CHAR,1},
                                       .dataPtr={.outData=&CONST_CHARACTER_55fd5723_1cd1_406d_bbb4_823096481e55},
                                       .param1={.numberOfElements = 1},
                                    },
                                    {
                                       .dataType={pearlrt::IODataEntry::FLOAT,23},
                                       .dataPtr={.outData=&_pump_current_rpm},
                                       .param1={.numberOfElements = 1},
                                    },
                                    {
                                       .dataType={pearlrt::IODataEntry::CHAR,1},
                                       .dataPtr={.outData=&CONST_CHARACTER_55fd5723_1cd1_406d_bbb4_823096481e55},
                                       .param1={.numberOfElements = 1},
                                    },
                                    {
                                       .dataType={pearlrt::IODataEntry::FIXED,31},
                                       .dataPtr={.outData=&_pump_activations},
                                       .param1={.numberOfElements = 1},
                                    },
                                    {
                                       .dataType={pearlrt::IODataEntry::CHAR,1},
                                       .dataPtr={.outData=&CONST_CHARACTER_55fd5723_1cd1_406d_bbb4_823096481e55},
                                       .param1={.numberOfElements = 1},
                                    },
                                    {
                                       .dataType={pearlrt::IODataEntry::FLOAT,23},
                                       .dataPtr={.outData=&_pump_output_flow_rate},
                                       .param1={.numberOfElements = 1},
                                    },
                                    {
                                       .dataType={pearlrt::IODataEntry::CHAR,1},
                                       .dataPtr={.outData=&CONST_CHARACTER_55fd5723_1cd1_406d_bbb4_823096481e55},
                                       .param1={.numberOfElements = 1},
                                    },
                                    {
                                       .dataType={pearlrt::IODataEntry::FLOAT,23},
                                       .dataPtr={.outData=&_pressure_sensor_1},
                                       .param1={.numberOfElements = 1},
                                    },
                                    {
                                       .dataType={pearlrt::IODataEntry::CHAR,1},
                                       .dataPtr={.outData=&CONST_CHARACTER_55fd5723_1cd1_406d_bbb4_823096481e55},
                                       .param1={.numberOfElements = 1},
                                    },
                                    {
                                       .dataType={pearlrt::IODataEntry::BIT,1},
                                       .dataPtr={.outData=&_valve_enabled},
                                       .param1={.numberOfElements = 1},
                                    },
                                    {
                                       .dataType={pearlrt::IODataEntry::CHAR,1},
                                       .dataPtr={.outData=&CONST_CHARACTER_55fd5723_1cd1_406d_bbb4_823096481e55},
                                       .param1={.numberOfElements = 1},
                                    },
                                    {
                                       .dataType={pearlrt::IODataEntry::FLOAT,23},
                                       .dataPtr={.outData=&_pressure_sensor_2},
                                       .param1={.numberOfElements = 1},
                                    },
                                    {
                                       .dataType={pearlrt::IODataEntry::CHAR,1},
                                       .dataPtr={.outData=&CONST_CHARACTER_55fd5723_1cd1_406d_bbb4_823096481e55},
                                       .param1={.numberOfElements = 1},
                                    },
                                    {
                                       .dataType={pearlrt::IODataEntry::BIT,1},
                                       .dataPtr={.outData=&_float_switch_enabled},
                                       .param1={.numberOfElements = 1},
                                    },
                                    {
                                       .dataType={pearlrt::IODataEntry::CHAR,1},
                                       .dataPtr={.outData=&CONST_CHARACTER_55fd5723_1cd1_406d_bbb4_823096481e55},
                                       .param1={.numberOfElements = 1},
                                    },
                                    {
                                       .dataType={pearlrt::IODataEntry::FLOAT,23},
                                       .dataPtr={.outData=&_current_consumer_dissipation},
                                       .param1={.numberOfElements = 1},
                                    },
                                    {
                                       .dataType={pearlrt::IODataEntry::CHAR,1},
                                       .dataPtr={.outData=&CONST_CHARACTER_55fd5723_1cd1_406d_bbb4_823096481e55},
                                       .param1={.numberOfElements = 1},
                                    },
                                    {
                                       .dataType={pearlrt::IODataEntry::FLOAT,23},
                                       .dataPtr={.outData=&_tank_fill_level},
                                       .param1={.numberOfElements = 1},
                                    },
                                    {
                                       .dataType={pearlrt::IODataEntry::CHAR,1},
                                       .dataPtr={.outData=&CONST_CHARACTER_55fd5723_1cd1_406d_bbb4_823096481e55},
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

                               me->setLocation(352, "SimWatertank.prl");
                               {
                               pearlrt::Semaphore *tmp_0[] = {&_sema}; 
                                  	 	
                                 pearlrt::Semaphore::release( me, 1, tmp_0);
                                   


                               }
                               me->setLocation(353, "SimWatertank.prl");
                               _log( me, pearlrt::RefCharacter(_line));

                               me->setLocation(354, "SimWatertank.prl");
                                   me->resume( pearlrt::Task::AFTER,
                                         /* at     */  pearlrt::Clock(),
                                         /* after  */  CONST_DURATION_P_0_0_1_0,
                               	  /* when   */  0
                                          );






            } 
    } // REPEAT 





}
DCLTASK(_check_tank_status, (pearlrt::Prio( (pearlrt::Fixed<15>)CONST_FIXED_P_20_5)), ((pearlrt::BitString<1>)0)) {
    me->setLocation(490, "SimWatertank.prl");
    {   // REPEAT 
            while ( 1 )
            {

                if (!(_simulation_is_running.getBoolean()))
                    break;

                 
                               me->setLocation(492, "SimWatertank.prl");
                               {
                               pearlrt::Semaphore *tmp_0[] = {&_sema}; 
                                  	 	
                                  pearlrt::Semaphore::request( me, 1, tmp_0);
                                   


                               }
                               me->setLocation(493, "SimWatertank.prl");
                               	if ((_tank_fill_level >= _get_watertank_capacity( me)).getBoolean()) {
                               	    me->setLocation(495, "SimWatertank.prl");
                               	    _log( me, pearlrt::RefCharacter(CONST_CHARACTER_6cf9609a_363d_4e0b_8a12_867504f48715));



                               	}


                               me->setLocation(498, "SimWatertank.prl");
                               	if ((_tank_fill_level == CONST_FLOAT_P_0_0_23).bitAnd((
                               _current_consumer_dissipation > CONST_FLOAT_P_0_0_23)).getBoolean()) {
                               	    me->setLocation(500, "SimWatertank.prl");
                               	    _log( me, pearlrt::RefCharacter(CONST_CHARACTER_b15d81df_3faf_42d4_b80f_0c9d0af99c39));



                               	}


                               me->setLocation(502, "SimWatertank.prl");
                               {
                               pearlrt::Semaphore *tmp_0[] = {&_sema}; 
                                  	 	
                                 pearlrt::Semaphore::release( me, 1, tmp_0);
                                   


                               }
                               me->setLocation(504, "SimWatertank.prl");
                                   me->resume( pearlrt::Task::AFTER,
                                         /* at     */  pearlrt::Clock(),
                                         /* after  */  CONST_DURATION_P_0_0_5_0,
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

    me->setLocation(537, "SimWatertank.prl");
    {   // REPEAT 
            while ( 1 )
            {

                if (!(_simulation_is_running.getBoolean()))
                    break;

                 
                               me->setLocation(539, "SimWatertank.prl");
                               {
                               pearlrt::Semaphore *tmp_0[] = {&_sema}; 
                                  	 	
                                  pearlrt::Semaphore::request( me, 1, tmp_0);
                                   


                               }
                               me->setLocation(541, "SimWatertank.prl");
                               _current_clock = pearlrt::Clock::now();

                               me->setLocation(542, "SimWatertank.prl");
                               _dt = (((_current_clock-_last_update)))/((CONST_DURATION_P_0_0_0_001));

                               me->setLocation(544, "SimWatertank.prl");
                               _pump_update( me, _dt);

                               me->setLocation(545, "SimWatertank.prl");
                               _pressure_sensor_1_update( me, _dt);

                               me->setLocation(546, "SimWatertank.prl");
                               _tank_update( me, _dt);

                               me->setLocation(547, "SimWatertank.prl");
                               _pressure_sensor_2_update( me, _dt);

                               me->setLocation(548, "SimWatertank.prl");
                               _float_switch_update( me, _dt);

                               me->setLocation(550, "SimWatertank.prl");
                               {
                               pearlrt::Semaphore *tmp_0[] = {&_sema}; 
                                  	 	
                                 pearlrt::Semaphore::release( me, 1, tmp_0);
                                   


                               }
                               me->setLocation(552, "SimWatertank.prl");
                               _last_update = _current_clock;

                               me->setLocation(554, "SimWatertank.prl");
                                   me->resume( pearlrt::Task::AFTER,
                                         /* at     */  pearlrt::Clock(),
                                         /* after  */  CONST_DURATION_P_0_0_0_01,
                               	  /* when   */  0
                                          );






            } 
    } // REPEAT 


    me->setLocation(557, "SimWatertank.prl");
    _log( me, pearlrt::RefCharacter(CONST_CHARACTER_fdb06380_71f3_4730_94df_c54ec60b568b));




}




}
