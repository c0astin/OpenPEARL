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
static /*const*/ pearlrt::Character<7>         CONST_CHARACTER_a6c3c336_514a_416c_a491_ea0cc286eac0("sim.log");
static /*const*/ pearlrt::Character<19>         CONST_CHARACTER_e297f8c6_7866_46dd_a513_8fd4605f8727("STARTING SIMULATION");
static /*const*/ pearlrt::Character<27>         CONST_CHARACTER_57dcf370_1d38_4545_8545_3dfe2793825b("REQUEST STOPPING SIMULATION");
static /*const*/ pearlrt::Character<6>         CONST_CHARACTER_1f4552a8_8e10_4344_a023_d2ffacf7e957(" SIM: ");
static /*const*/ pearlrt::Character<12>         CONST_CHARACTER_4a0a83db_970e_4896_8806_34469d9be16b("VALVE OPENED");
static /*const*/ pearlrt::Character<12>         CONST_CHARACTER_2fbc7c06_1e40_43ac_a18e_57177bed072d("VALVE CLOSED");
static /*const*/ pearlrt::Character<7>         CONST_CHARACTER_6459b84f_0d8b_4f88_a60f_276173be1d12("Fields:");
static /*const*/ pearlrt::Character<32>         CONST_CHARACTER_366d72f1_d880_419a_b2d3_639f1e7e084e("|#1|#2|#3|#4|#5|#6|#7|#8|#9|#10|");
static /*const*/ pearlrt::Character<26>         CONST_CHARACTER_d1dd6135_9aa4_4342_93c0_dd11ed5720f7("# 1: simulation_is_running");
static /*const*/ pearlrt::Character<21>         CONST_CHARACTER_d9c71999_83c5_41dd_91db_c7753070be78("# 2: pump_current_rpm");
static /*const*/ pearlrt::Character<21>         CONST_CHARACTER_fa8428f9_faee_4375_8d49_66ec1b908151("# 3: pump_activations");
static /*const*/ pearlrt::Character<26>         CONST_CHARACTER_500f328c_c9f4_4362_90a8_24363b9ef856("# 4: pump_output_flow_rate");
static /*const*/ pearlrt::Character<22>         CONST_CHARACTER_491588d5_8115_458e_9cb9_d13acfd7699b("# 5: pressure_sensor_1");
static /*const*/ pearlrt::Character<18>         CONST_CHARACTER_d568d20e_a5db_4e1b_a714_5f8970c1981a("# 6: valve_enabled");
static /*const*/ pearlrt::Character<22>         CONST_CHARACTER_01a3fe7f_8c4d_4eb1_9335_cf37c2e8462a("# 7: pressure_sensor_2");
static /*const*/ pearlrt::Character<25>         CONST_CHARACTER_cad32a20_bdb6_445f_8c86_1b87ba459051("# 8: float_switch_enabled");
static /*const*/ pearlrt::Character<33>         CONST_CHARACTER_fd78d054_33b9_4a6e_a2e5_ff0586b1e4c4("# 9: current_consumer_dissipation");
static /*const*/ pearlrt::Character<20>         CONST_CHARACTER_e9d0ec22_c7ba_4fba_88ee_995f42c78632("#10: tank_fill_level");
static /*const*/ pearlrt::Character<76>         CONST_CHARACTER_8353be6b_0cb1_4951_9677_c43dd841ad84("----------------------------------------------------------------------------");
static /*const*/ pearlrt::Character<71>         CONST_CHARACTER_52a9530f_ce0a_4483_8cd6_35c26f06b8da(" 1    2    3          4          5 6          7 8         9          10");
static /*const*/ pearlrt::Character<1>         CONST_CHARACTER_2ae0c77a_51b0_4e7b_917e_54b127678040("|");
static /*const*/ pearlrt::Character<25>         CONST_CHARACTER_23284f6a_94f5_4cb4_8561_f49c69d6773e("ALERT: WATERTANK OVERRUN!");
static /*const*/ pearlrt::Character<26>         CONST_CHARACTER_8482daf4_0112_4978_8e50_2e888e74144a("ALERT: WATERTANK UNDERRUN!");
static /*const*/ pearlrt::Character<23>         CONST_CHARACTER_ed01d56b_ab3b_4dd6_9564_0c45fb0d04c8("SIMULATION TASK STOPPED");
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
static pearlrt::Character<7>  _fileName (CONST_CHARACTER_a6c3c336_514a_416c_a491_ea0cc286eac0); 
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
    _log( me, pearlrt::RefCharacter(CONST_CHARACTER_e297f8c6_7866_46dd_a513_8fd4605f8727));

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
    _log( me, pearlrt::RefCharacter(CONST_CHARACTER_57dcf370_1d38_4545_8545_3dfe2793825b));

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
            .dataPtr={.outData=&CONST_CHARACTER_1f4552a8_8e10_4344_a023_d2ffacf7e957},
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
    _log( me, pearlrt::RefCharacter(CONST_CHARACTER_4a0a83db_970e_4896_8806_34469d9be16b));




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
    _log( me, pearlrt::RefCharacter(CONST_CHARACTER_2fbc7c06_1e40_43ac_a18e_57177bed072d));




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
    _log( me, pearlrt::RefCharacter(CONST_CHARACTER_6459b84f_0d8b_4f88_a60f_276173be1d12));

    me->setLocation(313, "SimWatertank.prl");
    _log( me, pearlrt::RefCharacter(CONST_CHARACTER_366d72f1_d880_419a_b2d3_639f1e7e084e));

    me->setLocation(314, "SimWatertank.prl");
    _log( me, pearlrt::RefCharacter(CONST_CHARACTER_d1dd6135_9aa4_4342_93c0_dd11ed5720f7));

    me->setLocation(315, "SimWatertank.prl");
    _log( me, pearlrt::RefCharacter(CONST_CHARACTER_d9c71999_83c5_41dd_91db_c7753070be78));

    me->setLocation(316, "SimWatertank.prl");
    _log( me, pearlrt::RefCharacter(CONST_CHARACTER_fa8428f9_faee_4375_8d49_66ec1b908151));

    me->setLocation(317, "SimWatertank.prl");
    _log( me, pearlrt::RefCharacter(CONST_CHARACTER_500f328c_c9f4_4362_90a8_24363b9ef856));

    me->setLocation(318, "SimWatertank.prl");
    _log( me, pearlrt::RefCharacter(CONST_CHARACTER_491588d5_8115_458e_9cb9_d13acfd7699b));

    me->setLocation(319, "SimWatertank.prl");
    _log( me, pearlrt::RefCharacter(CONST_CHARACTER_d568d20e_a5db_4e1b_a714_5f8970c1981a));

    me->setLocation(320, "SimWatertank.prl");
    _log( me, pearlrt::RefCharacter(CONST_CHARACTER_01a3fe7f_8c4d_4eb1_9335_cf37c2e8462a));

    me->setLocation(321, "SimWatertank.prl");
    _log( me, pearlrt::RefCharacter(CONST_CHARACTER_cad32a20_bdb6_445f_8c86_1b87ba459051));

    me->setLocation(322, "SimWatertank.prl");
    _log( me, pearlrt::RefCharacter(CONST_CHARACTER_fd78d054_33b9_4a6e_a2e5_ff0586b1e4c4));

    me->setLocation(323, "SimWatertank.prl");
    _log( me, pearlrt::RefCharacter(CONST_CHARACTER_e9d0ec22_c7ba_4fba_88ee_995f42c78632));

    me->setLocation(324, "SimWatertank.prl");
    _log( me, pearlrt::RefCharacter(CONST_CHARACTER_8353be6b_0cb1_4951_9677_c43dd841ad84));

    me->setLocation(325, "SimWatertank.prl");
    _log( me, pearlrt::RefCharacter(CONST_CHARACTER_52a9530f_ce0a_4483_8cd6_35c26f06b8da));

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
                                       .dataPtr={.outData=&CONST_CHARACTER_2ae0c77a_51b0_4e7b_917e_54b127678040},
                                       .param1={.numberOfElements = 1},
                                    },
                                    {
                                       .dataType={pearlrt::IODataEntry::BIT,1},
                                       .dataPtr={.outData=&_simulation_is_running},
                                       .param1={.numberOfElements = 1},
                                    },
                                    {
                                       .dataType={pearlrt::IODataEntry::CHAR,1},
                                       .dataPtr={.outData=&CONST_CHARACTER_2ae0c77a_51b0_4e7b_917e_54b127678040},
                                       .param1={.numberOfElements = 1},
                                    },
                                    {
                                       .dataType={pearlrt::IODataEntry::FLOAT,23},
                                       .dataPtr={.outData=&_pump_current_rpm},
                                       .param1={.numberOfElements = 1},
                                    },
                                    {
                                       .dataType={pearlrt::IODataEntry::CHAR,1},
                                       .dataPtr={.outData=&CONST_CHARACTER_2ae0c77a_51b0_4e7b_917e_54b127678040},
                                       .param1={.numberOfElements = 1},
                                    },
                                    {
                                       .dataType={pearlrt::IODataEntry::FIXED,31},
                                       .dataPtr={.outData=&_pump_activations},
                                       .param1={.numberOfElements = 1},
                                    },
                                    {
                                       .dataType={pearlrt::IODataEntry::CHAR,1},
                                       .dataPtr={.outData=&CONST_CHARACTER_2ae0c77a_51b0_4e7b_917e_54b127678040},
                                       .param1={.numberOfElements = 1},
                                    },
                                    {
                                       .dataType={pearlrt::IODataEntry::FLOAT,23},
                                       .dataPtr={.outData=&_pump_output_flow_rate},
                                       .param1={.numberOfElements = 1},
                                    },
                                    {
                                       .dataType={pearlrt::IODataEntry::CHAR,1},
                                       .dataPtr={.outData=&CONST_CHARACTER_2ae0c77a_51b0_4e7b_917e_54b127678040},
                                       .param1={.numberOfElements = 1},
                                    },
                                    {
                                       .dataType={pearlrt::IODataEntry::FLOAT,23},
                                       .dataPtr={.outData=&_pressure_sensor_1},
                                       .param1={.numberOfElements = 1},
                                    },
                                    {
                                       .dataType={pearlrt::IODataEntry::CHAR,1},
                                       .dataPtr={.outData=&CONST_CHARACTER_2ae0c77a_51b0_4e7b_917e_54b127678040},
                                       .param1={.numberOfElements = 1},
                                    },
                                    {
                                       .dataType={pearlrt::IODataEntry::BIT,1},
                                       .dataPtr={.outData=&_valve_enabled},
                                       .param1={.numberOfElements = 1},
                                    },
                                    {
                                       .dataType={pearlrt::IODataEntry::CHAR,1},
                                       .dataPtr={.outData=&CONST_CHARACTER_2ae0c77a_51b0_4e7b_917e_54b127678040},
                                       .param1={.numberOfElements = 1},
                                    },
                                    {
                                       .dataType={pearlrt::IODataEntry::FLOAT,23},
                                       .dataPtr={.outData=&_pressure_sensor_2},
                                       .param1={.numberOfElements = 1},
                                    },
                                    {
                                       .dataType={pearlrt::IODataEntry::CHAR,1},
                                       .dataPtr={.outData=&CONST_CHARACTER_2ae0c77a_51b0_4e7b_917e_54b127678040},
                                       .param1={.numberOfElements = 1},
                                    },
                                    {
                                       .dataType={pearlrt::IODataEntry::BIT,1},
                                       .dataPtr={.outData=&_float_switch_enabled},
                                       .param1={.numberOfElements = 1},
                                    },
                                    {
                                       .dataType={pearlrt::IODataEntry::CHAR,1},
                                       .dataPtr={.outData=&CONST_CHARACTER_2ae0c77a_51b0_4e7b_917e_54b127678040},
                                       .param1={.numberOfElements = 1},
                                    },
                                    {
                                       .dataType={pearlrt::IODataEntry::FLOAT,23},
                                       .dataPtr={.outData=&_current_consumer_dissipation},
                                       .param1={.numberOfElements = 1},
                                    },
                                    {
                                       .dataType={pearlrt::IODataEntry::CHAR,1},
                                       .dataPtr={.outData=&CONST_CHARACTER_2ae0c77a_51b0_4e7b_917e_54b127678040},
                                       .param1={.numberOfElements = 1},
                                    },
                                    {
                                       .dataType={pearlrt::IODataEntry::FLOAT,23},
                                       .dataPtr={.outData=&_tank_fill_level},
                                       .param1={.numberOfElements = 1},
                                    },
                                    {
                                       .dataType={pearlrt::IODataEntry::CHAR,1},
                                       .dataPtr={.outData=&CONST_CHARACTER_2ae0c77a_51b0_4e7b_917e_54b127678040},
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
                               	    _log( me, pearlrt::RefCharacter(CONST_CHARACTER_23284f6a_94f5_4cb4_8561_f49c69d6773e));



                               	}


                               me->setLocation(498, "SimWatertank.prl");
                               	if ((_tank_fill_level == CONST_FLOAT_P_0_0_23).bitAnd((
                               _current_consumer_dissipation > CONST_FLOAT_P_0_0_23)).getBoolean()) {
                               	    me->setLocation(500, "SimWatertank.prl");
                               	    _log( me, pearlrt::RefCharacter(CONST_CHARACTER_8482daf4_0112_4978_8e50_2e888e74144a));



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
    _log( me, pearlrt::RefCharacter(CONST_CHARACTER_ed01d56b_ab3b_4dd6_9564_0c45fb0d04c8));




}




}
