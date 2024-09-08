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
static /*const*/ pearlrt::Character<7>         CONST_CHARACTER_c8df13ee_6963_4375_9bb2_7b2d0ddb7ff5("sim.log");
static /*const*/ pearlrt::Character<19>         CONST_CHARACTER_bb3409c9_b9a2_4838_9e36_5d9e71d1661c("STARTING SIMULATION");
static /*const*/ pearlrt::Character<27>         CONST_CHARACTER_6631de0a_7730_4844_bf25_1d14bd3732e9("REQUEST STOPPING SIMULATION");
static /*const*/ pearlrt::Character<6>         CONST_CHARACTER_5a4910d3_2572_44e3_a706_3666de0b789a(" SIM: ");
static /*const*/ pearlrt::Character<12>         CONST_CHARACTER_7dc71339_a4ae_4040_bbfa_92bc8253de40("VALVE OPENED");
static /*const*/ pearlrt::Character<12>         CONST_CHARACTER_3d280b6f_12e0_4312_8bcc_0fb6bf9a0a93("VALVE CLOSED");
static /*const*/ pearlrt::Character<7>         CONST_CHARACTER_958bfba6_1b9c_44d3_8b46_a047745420b6("Fields:");
static /*const*/ pearlrt::Character<32>         CONST_CHARACTER_3a0a8a19_343d_468e_a9bd_a00102e9ba95("|#1|#2|#3|#4|#5|#6|#7|#8|#9|#10|");
static /*const*/ pearlrt::Character<26>         CONST_CHARACTER_84e8b362_1a68_4c3b_ae3b_ea8767185b8a("# 1: simulation_is_running");
static /*const*/ pearlrt::Character<21>         CONST_CHARACTER_273d8686_415e_4e39_878a_81cf4c9eb08b("# 2: pump_current_rpm");
static /*const*/ pearlrt::Character<21>         CONST_CHARACTER_e48a5634_96af_47cb_86b1_2db5d364bc67("# 3: pump_activations");
static /*const*/ pearlrt::Character<26>         CONST_CHARACTER_f9a906b1_148b_497c_940c_a38768e2d61d("# 4: pump_output_flow_rate");
static /*const*/ pearlrt::Character<22>         CONST_CHARACTER_04bb183b_04de_4ecf_8d46_1caadd852083("# 5: pressure_sensor_1");
static /*const*/ pearlrt::Character<18>         CONST_CHARACTER_39df34ea_149f_452a_8cda_e78b1899ccf6("# 6: valve_enabled");
static /*const*/ pearlrt::Character<22>         CONST_CHARACTER_c2894692_ec87_4e66_b3a1_4da46c41b98a("# 7: pressure_sensor_2");
static /*const*/ pearlrt::Character<25>         CONST_CHARACTER_1f986b70_02af_4642_8295_638753e3c474("# 8: float_switch_enabled");
static /*const*/ pearlrt::Character<33>         CONST_CHARACTER_482fcb56_f7dc_4c61_be54_b2425ad3630c("# 9: current_consumer_dissipation");
static /*const*/ pearlrt::Character<20>         CONST_CHARACTER_2cefe37e_9a56_46df_b128_12deb79d925d("#10: tank_fill_level");
static /*const*/ pearlrt::Character<76>         CONST_CHARACTER_71d97ef0_81d1_4e8c_96ff_dd8d28601053("----------------------------------------------------------------------------");
static /*const*/ pearlrt::Character<71>         CONST_CHARACTER_66351911_1635_4365_b5e8_b2ae368d0502(" 1    2    3          4          5 6          7 8         9          10");
static /*const*/ pearlrt::Character<1>         CONST_CHARACTER_cf1e53b6_6751_4c01_a313_800ff8aeac99("|");
static /*const*/ pearlrt::Character<25>         CONST_CHARACTER_1a92ebe6_e044_45dc_8ea7_3f62cfbd136b("ALERT: WATERTANK OVERRUN!");
static /*const*/ pearlrt::Character<26>         CONST_CHARACTER_68c1964d_48f5_4239_b17e_e7d2d1a327f2("ALERT: WATERTANK UNDERRUN!");
static /*const*/ pearlrt::Character<23>         CONST_CHARACTER_55430b23_c69a_42f2_a371_0d59fde08db6("SIMULATION TASK STOPPED");
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
static pearlrt::Character<7>  _fileName (CONST_CHARACTER_c8df13ee_6963_4375_9bb2_7b2d0ddb7ff5); 
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
    {
           _writeFormatted->dationOpen(
           0
                  	                , (pearlrt::RefCharacter*) 0
           , (pearlrt::Fixed<31>*) 0
           );
    }


    me->setLocation(133, "SimWatertank.prl");
    _log( me, pearlrt::RefCharacter(CONST_CHARACTER_bb3409c9_b9a2_4838_9e36_5d9e71d1661c));

    me->setLocation(134, "SimWatertank.prl");
    _simulation_is_running = CONST_BITSTRING_1_1;

    me->setLocation(135, "SimWatertank.prl");
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



    me->setLocation(136, "SimWatertank.prl");
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



    me->setLocation(137, "SimWatertank.prl");
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



    me->setLocation(138, "SimWatertank.prl");
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
    me->setLocation(143, "SimWatertank.prl");
    _log( me, pearlrt::RefCharacter(CONST_CHARACTER_6631de0a_7730_4844_bf25_1d14bd3732e9));

    me->setLocation(144, "SimWatertank.prl");
    _simulation_is_running = CONST_BITSTRING_1_0;

    me->setLocation(145, "SimWatertank.prl");
      _writeFormatted->dationClose(0, (pearlrt::Fixed<15>*) 0);





    me->setLocation(locationLine,locationFile);
}
static void
_openLogfile(pearlrt::Task *me, pearlrt::RefCharacter _logFileName, pearlrt::BitString<1> _addToExistingFile)
{
    const char* locationFile = me->getLocationFile();
    int locationLine = me->getLocationLine(); 
    pearlrt::Character<30>  _fn ;

    me->setLocation(152, "SimWatertank.prl");
    _logFileName.store(_fn);

    me->setLocation(154, "SimWatertank.prl");
    	if (_addToExistingFile.getBoolean()) {
    	    me->setLocation(155, "SimWatertank.prl");
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


    	    me->setLocation(156, "SimWatertank.prl");
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
    	    me->setLocation(160, "SimWatertank.prl");
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


    	    me->setLocation(161, "SimWatertank.prl");
    	      _logFile->dationClose(pearlrt::Dation::CAN, (pearlrt::Fixed<15>*) 0);


    	    me->setLocation(165, "SimWatertank.prl");
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
    me->setLocation(171, "SimWatertank.prl");
      _logFile->dationClose(0, (pearlrt::Fixed<15>*) 0);





    me->setLocation(locationLine,locationFile);
}
 static void
_log(pearlrt::Task *me, pearlrt::RefCharacter _line)
{
    const char* locationFile = me->getLocationFile();
    int locationLine = me->getLocationLine(); 
    me->setLocation(176, "SimWatertank.prl");
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
            .dataPtr={.outData=&CONST_CHARACTER_5a4910d3_2572_44e3_a706_3666de0b789a},
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


    me->setLocation(177, "SimWatertank.prl");
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
            .dataPtr={.outData=&CONST_CHARACTER_5a4910d3_2572_44e3_a706_3666de0b789a},
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
 /* public */ pearlrt::Float<23>
_get_level(pearlrt::Task *me)
{
    const char* locationFile = me->getLocationFile();
    int locationLine = me->getLocationLine(); 
    me->setLocation(202, "SimWatertank.prl");
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

    me->setLocation(208, "SimWatertank.prl");
    {
    pearlrt::Semaphore *tmp_0[] = {&_sema}; 
       	 	
       pearlrt::Semaphore::request( me, 1, tmp_0);
        


    }
    me->setLocation(209, "SimWatertank.prl");
    _rpm = (_pump_current_rpm).entier();

    me->setLocation(210, "SimWatertank.prl");
    {
    pearlrt::Semaphore *tmp_0[] = {&_sema}; 
       	 	
      pearlrt::Semaphore::release( me, 1, tmp_0);
        


    }
    me->setLocation(211, "SimWatertank.prl");
    	   me->setLocation(locationLine,locationFile);
    	   return (_rpm);





    me->setLocation(locationLine,locationFile);
}
/* public */ void
_set_pump_rotational_speed(pearlrt::Task *me, pearlrt::Fixed<15> _rpm)
{
    const char* locationFile = me->getLocationFile();
    int locationLine = me->getLocationLine(); 
    me->setLocation(216, "SimWatertank.prl");
    {
    pearlrt::Semaphore *tmp_0[] = {&_sema}; 
       	 	
       pearlrt::Semaphore::request( me, 1, tmp_0);
        


    }
    me->setLocation(217, "SimWatertank.prl");
    _pump_requested_rpm = _rpm;

    me->setLocation(218, "SimWatertank.prl");
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
    me->setLocation(223, "SimWatertank.prl");
    	   me->setLocation(locationLine,locationFile);
    	   return (CONST_FLOAT_P_45_0_23);





    me->setLocation(locationLine,locationFile);
}
/* public */ void
_open_valve(pearlrt::Task *me)
{
    const char* locationFile = me->getLocationFile();
    int locationLine = me->getLocationLine(); 
    me->setLocation(228, "SimWatertank.prl");
    {
    pearlrt::Semaphore *tmp_0[] = {&_sema}; 
       	 	
       pearlrt::Semaphore::request( me, 1, tmp_0);
        


    }
    me->setLocation(229, "SimWatertank.prl");
    _valve_enabled = CONST_BITSTRING_1_1;

    me->setLocation(230, "SimWatertank.prl");
    {
    pearlrt::Semaphore *tmp_0[] = {&_sema}; 
       	 	
      pearlrt::Semaphore::release( me, 1, tmp_0);
        


    }
    me->setLocation(231, "SimWatertank.prl");
    _log( me, pearlrt::RefCharacter(CONST_CHARACTER_7dc71339_a4ae_4040_bbfa_92bc8253de40));




    me->setLocation(locationLine,locationFile);
}
/* public */ void
_close_valve(pearlrt::Task *me)
{
    const char* locationFile = me->getLocationFile();
    int locationLine = me->getLocationLine(); 
    me->setLocation(236, "SimWatertank.prl");
    {
    pearlrt::Semaphore *tmp_0[] = {&_sema}; 
       	 	
       pearlrt::Semaphore::request( me, 1, tmp_0);
        


    }
    me->setLocation(237, "SimWatertank.prl");
    _valve_enabled = CONST_BITSTRING_1_0;

    me->setLocation(238, "SimWatertank.prl");
    {
    pearlrt::Semaphore *tmp_0[] = {&_sema}; 
       	 	
      pearlrt::Semaphore::release( me, 1, tmp_0);
        


    }
    me->setLocation(239, "SimWatertank.prl");
    _log( me, pearlrt::RefCharacter(CONST_CHARACTER_3d280b6f_12e0_4312_8bcc_0fb6bf9a0a93));




    me->setLocation(locationLine,locationFile);
}
/* public */ pearlrt::BitString<1>
_get_valve_state(pearlrt::Task *me)
{
    const char* locationFile = me->getLocationFile();
    int locationLine = me->getLocationLine(); 
    pearlrt::BitString<1>  _state ;

    me->setLocation(247, "SimWatertank.prl");
    {
    pearlrt::Semaphore *tmp_0[] = {&_sema}; 
       	 	
       pearlrt::Semaphore::request( me, 1, tmp_0);
        


    }
    me->setLocation(248, "SimWatertank.prl");
    _state = _valve_enabled;

    me->setLocation(249, "SimWatertank.prl");
    {
    pearlrt::Semaphore *tmp_0[] = {&_sema}; 
       	 	
      pearlrt::Semaphore::release( me, 1, tmp_0);
        


    }
    me->setLocation(250, "SimWatertank.prl");
    	   me->setLocation(locationLine,locationFile);
    	   return (_state);





    me->setLocation(locationLine,locationFile);
}
/* public */ pearlrt::Float<23>
_get_watertank_capacity(pearlrt::Task *me)
{
    const char* locationFile = me->getLocationFile();
    int locationLine = me->getLocationLine(); 
    me->setLocation(256, "SimWatertank.prl");
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

    me->setLocation(262, "SimWatertank.prl");
    {
    pearlrt::Semaphore *tmp_0[] = {&_sema}; 
       	 	
       pearlrt::Semaphore::request( me, 1, tmp_0);
        


    }
    me->setLocation(263, "SimWatertank.prl");
    _p = _pressure_sensor_2;

    me->setLocation(264, "SimWatertank.prl");
    {
    pearlrt::Semaphore *tmp_0[] = {&_sema}; 
       	 	
      pearlrt::Semaphore::release( me, 1, tmp_0);
        


    }
    me->setLocation(266, "SimWatertank.prl");
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

    me->setLocation(274, "SimWatertank.prl");
    {
    pearlrt::Semaphore *tmp_0[] = {&_sema}; 
       	 	
       pearlrt::Semaphore::request( me, 1, tmp_0);
        


    }
    me->setLocation(275, "SimWatertank.prl");
    _state = _float_switch_enabled;

    me->setLocation(276, "SimWatertank.prl");
    {
    pearlrt::Semaphore *tmp_0[] = {&_sema}; 
       	 	
      pearlrt::Semaphore::release( me, 1, tmp_0);
        


    }
    me->setLocation(277, "SimWatertank.prl");
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

    me->setLocation(283, "SimWatertank.prl");
    {
    pearlrt::Semaphore *tmp_0[] = {&_sema}; 
       	 	
       pearlrt::Semaphore::request( me, 1, tmp_0);
        


    }
    me->setLocation(284, "SimWatertank.prl");
    _pressure = _pressure_sensor_1;

    me->setLocation(285, "SimWatertank.prl");
    {
    pearlrt::Semaphore *tmp_0[] = {&_sema}; 
       	 	
      pearlrt::Semaphore::release( me, 1, tmp_0);
        


    }
    me->setLocation(286, "SimWatertank.prl");
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

    me->setLocation(292, "SimWatertank.prl");
    {
    pearlrt::Semaphore *tmp_0[] = {&_sema}; 
       	 	
       pearlrt::Semaphore::request( me, 1, tmp_0);
        


    }
    me->setLocation(293, "SimWatertank.prl");
    _pressure = _pressure_sensor_2;

    me->setLocation(294, "SimWatertank.prl");
    {
    pearlrt::Semaphore *tmp_0[] = {&_sema}; 
       	 	
      pearlrt::Semaphore::release( me, 1, tmp_0);
        


    }
    me->setLocation(295, "SimWatertank.prl");
    	   me->setLocation(locationLine,locationFile);
    	   return (_pressure);





    me->setLocation(locationLine,locationFile);
}
static void
_pump_update(pearlrt::Task *me, pearlrt::Float<23> _dt)
{
    const char* locationFile = me->getLocationFile();
    int locationLine = me->getLocationLine(); 
    me->setLocation(363, "SimWatertank.prl");
    	if ((_pump_current_rpm == CONST_FIXED_P_0_1).bitAnd((_pump_requested_rpm > 
    CONST_FLOAT_P_0_0_23)).getBoolean()) {
    	    me->setLocation(365, "SimWatertank.prl");
    	    _pump_activations = _pump_activations+CONST_FIXED_P_1_1;



    	}


    me->setLocation(368, "SimWatertank.prl");
    	if ((_pump_current_rpm < _pump_requested_rpm).getBoolean()) {
    	    me->setLocation(370, "SimWatertank.prl");
    	    _pump_current_rpm = _pump_current_rpm+((((_dt*_pump_spin_up_speed))/((pearlrt::Float<23>)(
    	    CONST_FIXED_P_1000_10)))).entier();

    	    me->setLocation(371, "SimWatertank.prl");
    	    	if ((_pump_current_rpm > _pump_requested_rpm).getBoolean()) {
    	    	    me->setLocation(373, "SimWatertank.prl");
    	    	    _pump_current_rpm = _pump_requested_rpm;



    	    	}




    	}


    me->setLocation(377, "SimWatertank.prl");
    	if ((_pump_current_rpm > _pump_requested_rpm).getBoolean()) {
    	    me->setLocation(379, "SimWatertank.prl");
    	    _pump_current_rpm = _pump_current_rpm-((((_dt*_pump_spin_down_speed))/((pearlrt::Float<23>)(
    	    CONST_FIXED_P_1000_10)))).entier();

    	    me->setLocation(381, "SimWatertank.prl");
    	    	if ((_pump_current_rpm < CONST_FIXED_P_0_1).getBoolean()) {
    	    	    me->setLocation(383, "SimWatertank.prl");
    	    	    _pump_current_rpm = CONST_FIXED_P_0_1;



    	    	}




    	}


    me->setLocation(387, "SimWatertank.prl");
    	if ((_pump_current_rpm > _pump_max_rpm).getBoolean()) {
    	    me->setLocation(389, "SimWatertank.prl");
    	    _pump_current_rpm = _pump_max_rpm;



    	}


    me->setLocation(398, "SimWatertank.prl");
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

    me->setLocation(427, "SimWatertank.prl");
    _a = ((_pi*_pipe_diameter.pow(CONST_FIXED_P_2_2)))/((pearlrt::Float<23>)(CONST_FIXED_P_4_3));

    me->setLocation(428, "SimWatertank.prl");
    _v = ((_pump_output_flow_rate))/((_a));

    me->setLocation(430, "SimWatertank.prl");
    	if ((_pump_current_rpm > CONST_FIXED_P_0_1).getBoolean()) {
    	    me->setLocation(432, "SimWatertank.prl");
    	    _c = (((CONST_FIXED_P_20_5*_rho*_gravity)))/((pearlrt::Float<23>)(CONST_FIXED_P_1000_10))
    	    +(((CONST_FIXED_P_1000_10*_v.pow(CONST_FIXED_P_2_2))))/((_pump_current_rpm));

    	    me->setLocation(433, "SimWatertank.prl");
    	    	if (_valve_enabled.getBoolean()) {
    	    	    me->setLocation(434, "SimWatertank.prl");
    	    	    _pressure_sensor_1 = _c;



    	    	} else {
    	    	    me->setLocation(436, "SimWatertank.prl");
    	    	    _pressure_sensor_1 = _pressure_sensor_1+_c*CONST_FLOAT_P_0_001_23;



    	    	}




    	} else {
    	    me->setLocation(439, "SimWatertank.prl");
    	    _pressure_sensor_1 = CONST_FLOAT_P_0_0_23;



    	}





    me->setLocation(locationLine,locationFile);
}
 static void
_pressure_sensor_2_update(pearlrt::Task *me, pearlrt::Float<23> _dt)
{
    const char* locationFile = me->getLocationFile();
    int locationLine = me->getLocationLine(); 
    me->setLocation(456, "SimWatertank.prl");
    _pressure_sensor_2 = (((_tank_fill_level))/(((_tank_width*_tank_length))))*_gravity
    *_rho;




    me->setLocation(locationLine,locationFile);
}
 static void
_tank_update(pearlrt::Task *me, pearlrt::Float<23> _dt)
{
    const char* locationFile = me->getLocationFile();
    int locationLine = me->getLocationLine(); 
    me->setLocation(466, "SimWatertank.prl");
    	if (_valve_enabled.getBoolean()) {
    	    me->setLocation(467, "SimWatertank.prl");
    	    _tank_fill_level = _tank_fill_level+((_pump_output_flow_rate))/((pearlrt::Float<23>)(
    	    CONST_FIXED_P_60_6))-(((_current_consumer_dissipation))/((pearlrt::Float<23>)(
    	    CONST_FIXED_P_100_7)));



    	} else {
    	    me->setLocation(471, "SimWatertank.prl");
    	    _tank_fill_level = _tank_fill_level-(((_current_consumer_dissipation))/((pearlrt::Float<23>)(
    	    CONST_FIXED_P_100_7)));



    	}


    me->setLocation(475, "SimWatertank.prl");
    	if ((_tank_fill_level >= _get_watertank_capacity( me)).getBoolean()) {
    	    me->setLocation(477, "SimWatertank.prl");
    	    _tank_fill_level = _get_watertank_capacity( me);



    	}


    me->setLocation(480, "SimWatertank.prl");
    	if ((_tank_fill_level < CONST_FLOAT_P_0_0_23).getBoolean()) {
    	    me->setLocation(482, "SimWatertank.prl");
    	    _tank_fill_level = CONST_FLOAT_P_0_0_23;



    	}





    me->setLocation(locationLine,locationFile);
}
 static void
_float_switch_update(pearlrt::Task *me, pearlrt::Float<23> _dt)
{
    const char* locationFile = me->getLocationFile();
    int locationLine = me->getLocationLine(); 
    me->setLocation(488, "SimWatertank.prl");
    _float_switch_enabled = (_tank_fill_level >= _get_watertank_capacity( me));




    me->setLocation(locationLine,locationFile);
}
 

/////////////////////////////////////////////////////////////////////////////
// TASK DECLARATIONS
/////////////////////////////////////////////////////////////////////////////
DCLTASK(_consumer, (pearlrt::Prio( (pearlrt::Fixed<15>)CONST_FIXED_P_20_5)), ((pearlrt::BitString<1>)0)) {
    pearlrt::Fixed<15>  _cstep (CONST_FIXED_P_0_1);

    me->setLocation(301, "SimWatertank.prl");
    {   // REPEAT 
            while ( 1 )
            {

                if (!(_simulation_is_running.getBoolean()))
                    break;

                 
                               me->setLocation(303, "SimWatertank.prl");
                               {
                               pearlrt::Semaphore *tmp_0[] = {&_sema}; 
                                  	 	
                                  pearlrt::Semaphore::request( me, 1, tmp_0);
                                   


                               }
                               me->setLocation(304, "SimWatertank.prl");
                               _current_consumer_dissipation = ((CONST_FIXED_P_10_4
                               *((((pearlrt::Float<23>)((CONST_FIXED_P_2_2*_cstep
                               ))).sin())*(((pearlrt::Float<23>)((CONST_FIXED_P_3_2
                               *_cstep))).cos())).abs()))/((pearlrt::Float<23>)(
                               CONST_FIXED_P_1000_10));

                               me->setLocation(305, "SimWatertank.prl");
                               _cstep = _cstep+CONST_FIXED_P_1_1;

                               me->setLocation(306, "SimWatertank.prl");
                               {
                               pearlrt::Semaphore *tmp_0[] = {&_sema}; 
                                  	 	
                                 pearlrt::Semaphore::release( me, 1, tmp_0);
                                   


                               }
                               me->setLocation(307, "SimWatertank.prl");
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

    me->setLocation(315, "SimWatertank.prl");
    _log( me, pearlrt::RefCharacter(CONST_CHARACTER_958bfba6_1b9c_44d3_8b46_a047745420b6));

    me->setLocation(316, "SimWatertank.prl");
    _log( me, pearlrt::RefCharacter(CONST_CHARACTER_3a0a8a19_343d_468e_a9bd_a00102e9ba95));

    me->setLocation(317, "SimWatertank.prl");
    _log( me, pearlrt::RefCharacter(CONST_CHARACTER_84e8b362_1a68_4c3b_ae3b_ea8767185b8a));

    me->setLocation(318, "SimWatertank.prl");
    _log( me, pearlrt::RefCharacter(CONST_CHARACTER_273d8686_415e_4e39_878a_81cf4c9eb08b));

    me->setLocation(319, "SimWatertank.prl");
    _log( me, pearlrt::RefCharacter(CONST_CHARACTER_e48a5634_96af_47cb_86b1_2db5d364bc67));

    me->setLocation(320, "SimWatertank.prl");
    _log( me, pearlrt::RefCharacter(CONST_CHARACTER_f9a906b1_148b_497c_940c_a38768e2d61d));

    me->setLocation(321, "SimWatertank.prl");
    _log( me, pearlrt::RefCharacter(CONST_CHARACTER_04bb183b_04de_4ecf_8d46_1caadd852083));

    me->setLocation(322, "SimWatertank.prl");
    _log( me, pearlrt::RefCharacter(CONST_CHARACTER_39df34ea_149f_452a_8cda_e78b1899ccf6));

    me->setLocation(323, "SimWatertank.prl");
    _log( me, pearlrt::RefCharacter(CONST_CHARACTER_c2894692_ec87_4e66_b3a1_4da46c41b98a));

    me->setLocation(324, "SimWatertank.prl");
    _log( me, pearlrt::RefCharacter(CONST_CHARACTER_1f986b70_02af_4642_8295_638753e3c474));

    me->setLocation(325, "SimWatertank.prl");
    _log( me, pearlrt::RefCharacter(CONST_CHARACTER_482fcb56_f7dc_4c61_be54_b2425ad3630c));

    me->setLocation(326, "SimWatertank.prl");
    _log( me, pearlrt::RefCharacter(CONST_CHARACTER_2cefe37e_9a56_46df_b128_12deb79d925d));

    me->setLocation(327, "SimWatertank.prl");
    _log( me, pearlrt::RefCharacter(CONST_CHARACTER_71d97ef0_81d1_4e8c_96ff_dd8d28601053));

    me->setLocation(328, "SimWatertank.prl");
    _log( me, pearlrt::RefCharacter(CONST_CHARACTER_66351911_1635_4365_b5e8_b2ae368d0502));

    me->setLocation(330, "SimWatertank.prl");
    {   // REPEAT 
            while ( 1 )
            {

                if (!(_simulation_is_running.getBoolean()))
                    break;

                 
                               me->setLocation(332, "SimWatertank.prl");
                               {
                               pearlrt::Semaphore *tmp_0[] = {&_sema}; 
                                  	 	
                                  pearlrt::Semaphore::request( me, 1, tmp_0);
                                   


                               }
                               me->setLocation(333, "SimWatertank.prl");
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
                                       .dataPtr={.outData=&CONST_CHARACTER_cf1e53b6_6751_4c01_a313_800ff8aeac99},
                                       .param1={.numberOfElements = 1},
                                    },
                                    {
                                       .dataType={pearlrt::IODataEntry::BIT,1},
                                       .dataPtr={.outData=&_simulation_is_running},
                                       .param1={.numberOfElements = 1},
                                    },
                                    {
                                       .dataType={pearlrt::IODataEntry::CHAR,1},
                                       .dataPtr={.outData=&CONST_CHARACTER_cf1e53b6_6751_4c01_a313_800ff8aeac99},
                                       .param1={.numberOfElements = 1},
                                    },
                                    {
                                       .dataType={pearlrt::IODataEntry::FLOAT,23},
                                       .dataPtr={.outData=&_pump_current_rpm},
                                       .param1={.numberOfElements = 1},
                                    },
                                    {
                                       .dataType={pearlrt::IODataEntry::CHAR,1},
                                       .dataPtr={.outData=&CONST_CHARACTER_cf1e53b6_6751_4c01_a313_800ff8aeac99},
                                       .param1={.numberOfElements = 1},
                                    },
                                    {
                                       .dataType={pearlrt::IODataEntry::FIXED,31},
                                       .dataPtr={.outData=&_pump_activations},
                                       .param1={.numberOfElements = 1},
                                    },
                                    {
                                       .dataType={pearlrt::IODataEntry::CHAR,1},
                                       .dataPtr={.outData=&CONST_CHARACTER_cf1e53b6_6751_4c01_a313_800ff8aeac99},
                                       .param1={.numberOfElements = 1},
                                    },
                                    {
                                       .dataType={pearlrt::IODataEntry::FLOAT,23},
                                       .dataPtr={.outData=&_pump_output_flow_rate},
                                       .param1={.numberOfElements = 1},
                                    },
                                    {
                                       .dataType={pearlrt::IODataEntry::CHAR,1},
                                       .dataPtr={.outData=&CONST_CHARACTER_cf1e53b6_6751_4c01_a313_800ff8aeac99},
                                       .param1={.numberOfElements = 1},
                                    },
                                    {
                                       .dataType={pearlrt::IODataEntry::FLOAT,23},
                                       .dataPtr={.outData=&_pressure_sensor_1},
                                       .param1={.numberOfElements = 1},
                                    },
                                    {
                                       .dataType={pearlrt::IODataEntry::CHAR,1},
                                       .dataPtr={.outData=&CONST_CHARACTER_cf1e53b6_6751_4c01_a313_800ff8aeac99},
                                       .param1={.numberOfElements = 1},
                                    },
                                    {
                                       .dataType={pearlrt::IODataEntry::BIT,1},
                                       .dataPtr={.outData=&_valve_enabled},
                                       .param1={.numberOfElements = 1},
                                    },
                                    {
                                       .dataType={pearlrt::IODataEntry::CHAR,1},
                                       .dataPtr={.outData=&CONST_CHARACTER_cf1e53b6_6751_4c01_a313_800ff8aeac99},
                                       .param1={.numberOfElements = 1},
                                    },
                                    {
                                       .dataType={pearlrt::IODataEntry::FLOAT,23},
                                       .dataPtr={.outData=&_pressure_sensor_2},
                                       .param1={.numberOfElements = 1},
                                    },
                                    {
                                       .dataType={pearlrt::IODataEntry::CHAR,1},
                                       .dataPtr={.outData=&CONST_CHARACTER_cf1e53b6_6751_4c01_a313_800ff8aeac99},
                                       .param1={.numberOfElements = 1},
                                    },
                                    {
                                       .dataType={pearlrt::IODataEntry::BIT,1},
                                       .dataPtr={.outData=&_float_switch_enabled},
                                       .param1={.numberOfElements = 1},
                                    },
                                    {
                                       .dataType={pearlrt::IODataEntry::CHAR,1},
                                       .dataPtr={.outData=&CONST_CHARACTER_cf1e53b6_6751_4c01_a313_800ff8aeac99},
                                       .param1={.numberOfElements = 1},
                                    },
                                    {
                                       .dataType={pearlrt::IODataEntry::FLOAT,23},
                                       .dataPtr={.outData=&_current_consumer_dissipation},
                                       .param1={.numberOfElements = 1},
                                    },
                                    {
                                       .dataType={pearlrt::IODataEntry::CHAR,1},
                                       .dataPtr={.outData=&CONST_CHARACTER_cf1e53b6_6751_4c01_a313_800ff8aeac99},
                                       .param1={.numberOfElements = 1},
                                    },
                                    {
                                       .dataType={pearlrt::IODataEntry::FLOAT,23},
                                       .dataPtr={.outData=&_tank_fill_level},
                                       .param1={.numberOfElements = 1},
                                    },
                                    {
                                       .dataType={pearlrt::IODataEntry::CHAR,1},
                                       .dataPtr={.outData=&CONST_CHARACTER_cf1e53b6_6751_4c01_a313_800ff8aeac99},
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

                               me->setLocation(355, "SimWatertank.prl");
                               {
                               pearlrt::Semaphore *tmp_0[] = {&_sema}; 
                                  	 	
                                 pearlrt::Semaphore::release( me, 1, tmp_0);
                                   


                               }
                               me->setLocation(356, "SimWatertank.prl");
                               _log( me, pearlrt::RefCharacter(_line));

                               me->setLocation(357, "SimWatertank.prl");
                                   me->resume( pearlrt::Task::AFTER,
                                         /* at     */  pearlrt::Clock(),
                                         /* after  */  CONST_DURATION_P_0_0_1_0,
                               	  /* when   */  0
                                          );






            } 
    } // REPEAT 





}
DCLTASK(_check_tank_status, (pearlrt::Prio( (pearlrt::Fixed<15>)CONST_FIXED_P_20_5)), ((pearlrt::BitString<1>)0)) {
    me->setLocation(493, "SimWatertank.prl");
    {   // REPEAT 
            while ( 1 )
            {

                if (!(_simulation_is_running.getBoolean()))
                    break;

                 
                               me->setLocation(495, "SimWatertank.prl");
                               {
                               pearlrt::Semaphore *tmp_0[] = {&_sema}; 
                                  	 	
                                  pearlrt::Semaphore::request( me, 1, tmp_0);
                                   


                               }
                               me->setLocation(496, "SimWatertank.prl");
                               	if ((_tank_fill_level >= _get_watertank_capacity( me)).getBoolean()) {
                               	    me->setLocation(498, "SimWatertank.prl");
                               	    _log( me, pearlrt::RefCharacter(CONST_CHARACTER_1a92ebe6_e044_45dc_8ea7_3f62cfbd136b));



                               	}


                               me->setLocation(501, "SimWatertank.prl");
                               	if ((_tank_fill_level == CONST_FLOAT_P_0_0_23).bitAnd((
                               _current_consumer_dissipation > CONST_FLOAT_P_0_0_23)).getBoolean()) {
                               	    me->setLocation(503, "SimWatertank.prl");
                               	    _log( me, pearlrt::RefCharacter(CONST_CHARACTER_68c1964d_48f5_4239_b17e_e7d2d1a327f2));



                               	}


                               me->setLocation(505, "SimWatertank.prl");
                               {
                               pearlrt::Semaphore *tmp_0[] = {&_sema}; 
                                  	 	
                                 pearlrt::Semaphore::release( me, 1, tmp_0);
                                   


                               }
                               me->setLocation(507, "SimWatertank.prl");
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

    me->setLocation(540, "SimWatertank.prl");
    {   // REPEAT 
            while ( 1 )
            {

                if (!(_simulation_is_running.getBoolean()))
                    break;

                 
                               me->setLocation(542, "SimWatertank.prl");
                               {
                               pearlrt::Semaphore *tmp_0[] = {&_sema}; 
                                  	 	
                                  pearlrt::Semaphore::request( me, 1, tmp_0);
                                   


                               }
                               me->setLocation(544, "SimWatertank.prl");
                               _current_clock = pearlrt::Clock::now();

                               me->setLocation(545, "SimWatertank.prl");
                               _dt = (((_current_clock-_last_update)))/((CONST_DURATION_P_0_0_0_001));

                               me->setLocation(547, "SimWatertank.prl");
                               _pump_update( me, _dt);

                               me->setLocation(548, "SimWatertank.prl");
                               _pressure_sensor_1_update( me, _dt);

                               me->setLocation(549, "SimWatertank.prl");
                               _tank_update( me, _dt);

                               me->setLocation(550, "SimWatertank.prl");
                               _pressure_sensor_2_update( me, _dt);

                               me->setLocation(551, "SimWatertank.prl");
                               _float_switch_update( me, _dt);

                               me->setLocation(553, "SimWatertank.prl");
                               {
                               pearlrt::Semaphore *tmp_0[] = {&_sema}; 
                                  	 	
                                 pearlrt::Semaphore::release( me, 1, tmp_0);
                                   


                               }
                               me->setLocation(555, "SimWatertank.prl");
                               _last_update = _current_clock;

                               me->setLocation(557, "SimWatertank.prl");
                                   me->resume( pearlrt::Task::AFTER,
                                         /* at     */  pearlrt::Clock(),
                                         /* after  */  CONST_DURATION_P_0_0_0_01,
                               	  /* when   */  0
                                          );






            } 
    } // REPEAT 


    me->setLocation(560, "SimWatertank.prl");
    _log( me, pearlrt::RefCharacter(CONST_CHARACTER_55430b23_c69a_42f2_a371_0d59fde08db6));




}




}
