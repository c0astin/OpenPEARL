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
static /*const*/ pearlrt::Fixed<4>         CONST_FIXED_P_10_4(10);
static /*const*/ pearlrt::Fixed<4>         CONST_FIXED_P_15_4(15);
static /*const*/ pearlrt::Fixed<5>         CONST_FIXED_P_25_5(25);
static /*const*/ pearlrt::Fixed<3>         CONST_FIXED_P_5_3(5);
static /*const*/ pearlrt::Fixed<5>         CONST_FIXED_P_30_5(30);
static /*const*/ pearlrt::Fixed<4>         CONST_FIXED_P_12_4(12);
static /*const*/ pearlrt::Fixed<2>         CONST_FIXED_P_3_2(3);
static /*const*/ pearlrt::Fixed<2>         CONST_FIXED_P_2_2(2);
static /*const*/ pearlrt::Fixed<4>         CONST_FIXED_P_8_4(8);
static /*const*/ pearlrt::Fixed<3>         CONST_FIXED_P_4_3(4);
static /*const*/ pearlrt::Fixed<10>         CONST_FIXED_P_1000_10(1000);
static /*const*/ pearlrt::Fixed<6>         CONST_FIXED_P_60_6(60);
static /*const*/ pearlrt::Fixed<5>         CONST_FIXED_P_20_5(20);
static /*const*/ pearlrt::Fixed<31>         CONST_FIXED_P_0_31(0);
static /*const*/ pearlrt::Fixed<31>         CONST_FIXED_P_1_31(1);
static /*const*/ pearlrt::Fixed<31>         CONST_FIXED_N_1_31(-1);
static /*const*/ pearlrt::Fixed<15>         CONST_FIXED_P_20000_15(20000);
static /*const*/ pearlrt::Fixed<7>         CONST_FIXED_P_80_7(80);
static /*const*/ pearlrt::Float<23>         CONST_FLOAT_P_3_1415926_23(3.1415926);
static /*const*/ pearlrt::Float<23>         CONST_FLOAT_P_1000_0_23(1000.0);
static /*const*/ pearlrt::Float<23>         CONST_FLOAT_P_2_0_23(2.0);
static /*const*/ pearlrt::Float<23>         CONST_FLOAT_P_0_0_23(0.0);
static /*const*/ pearlrt::Float<23>         CONST_FLOAT_P_0_06_23(0.06);
static /*const*/ pearlrt::Float<23>         CONST_FLOAT_P_300_0_23(300.0);
static /*const*/ pearlrt::Float<23>         CONST_FLOAT_P_0_1_23(0.1);
static /*const*/ pearlrt::Float<23>         CONST_FLOAT_P_45_0_23(45.0);
static /*const*/ pearlrt::Float<23>         CONST_FLOAT_P_46_0_23(46.0);
static /*const*/ pearlrt::Float<23>         CONST_FLOAT_P_9_81_23(9.81);
static /*const*/ pearlrt::Character<7>         CONST_CHARACTER_b9a1bc2d_347d_4a1b_a4aa_50c9c20ac096("sim.log");
static /*const*/ pearlrt::Character<2>         CONST_CHARACTER_e7968139_0ebf_49a8_9fb2_066f71281b5f("./");
static /*const*/ pearlrt::Character<19>         CONST_CHARACTER_67e3435d_32a9_40b3_a759_0ef894570bfd("Starting simulation");
static /*const*/ pearlrt::Character<27>         CONST_CHARACTER_0027ba60_5fa5_4279_89e3_bf7681ab953f("Request stopping simulation");
static /*const*/ pearlrt::Character<6>         CONST_CHARACTER_2443b140_ca9b_4acf_9965_fc6714930c22(" SIM: ");
static /*const*/ pearlrt::Character<16>         CONST_CHARACTER_bd4347dc_b43e_4bae_99ea_3a77d55348db("pump switched on");
static /*const*/ pearlrt::Character<17>         CONST_CHARACTER_d2943537_2d73_4fe7_9bdc_d9cc93baab7c("pump switched off");
static /*const*/ pearlrt::Character<12>         CONST_CHARACTER_50b9d833_3482_4f45_9124_7050fe3a3011("valve opened");
static /*const*/ pearlrt::Character<12>         CONST_CHARACTER_af721cd0_bcb6_4a6b_9404_74abd7e1a62a("valve closed");
static /*const*/ pearlrt::Character<7>         CONST_CHARACTER_0abe6ea3_11f1_4e68_b6c8_f9e26ca87b9a("Fields:");
static /*const*/ pearlrt::Character<36>         CONST_CHARACTER_fb2e0adc_5e6b_4ae5_b050_a883532d7f5d("|#1|#2|#3|#4|#5|#6|#7|#8|#9|#10|#11|");
static /*const*/ pearlrt::Character<26>         CONST_CHARACTER_7ddd70b3_a7e9_4740_8f8d_5377224a8232("# 1: simulation_is_running");
static /*const*/ pearlrt::Character<17>         CONST_CHARACTER_1799d3fa_a8e1_4bdd_a2b5_7d4361447c87("# 2: pump_enabled");
static /*const*/ pearlrt::Character<21>         CONST_CHARACTER_7dade90b_6089_4ca0_b2ea_a85295cb083e("# 3: pump_current_rpm");
static /*const*/ pearlrt::Character<21>         CONST_CHARACTER_2e211b9a_d221_478f_80a1_2ec01170d40d("# 4: pump_activations");
static /*const*/ pearlrt::Character<26>         CONST_CHARACTER_794a7bcf_4f79_45b3_b5b0_12113091f986("# 5: pump_output_flow_rate");
static /*const*/ pearlrt::Character<22>         CONST_CHARACTER_c40282f0_39d3_419c_b244_3ed3505e0a60("# 6: pressure_sensor_1");
static /*const*/ pearlrt::Character<18>         CONST_CHARACTER_9397f290_bac2_4283_b692_d00d847af6bc("# 7: valve_enabled");
static /*const*/ pearlrt::Character<22>         CONST_CHARACTER_2e31551a_1268_44b4_94a4_134f810ba0d9("# 8: pressure_sensor_2");
static /*const*/ pearlrt::Character<25>         CONST_CHARACTER_25cfa725_00cb_42c3_af1e_587664aa43d7("# 9: float_switch_enabled");
static /*const*/ pearlrt::Character<33>         CONST_CHARACTER_9149eac3_a7c7_461b_92f6_6257b657feda("#10: current_consumer_dissipation");
static /*const*/ pearlrt::Character<20>         CONST_CHARACTER_bc09941a_1cc8_46f1_ba48_4964c119c2a9("#11: tank_fill_level");
static /*const*/ pearlrt::Character<76>         CONST_CHARACTER_93f6170e_4265_4c4f_bbfc_845e03d5fc6e("----------------------------------------------------------------------------");
static /*const*/ pearlrt::Character<1>         CONST_CHARACTER_6252f135_4bdb_4288_aaf3_d79c0f7e66b9("|");
static /*const*/ pearlrt::Character<25>         CONST_CHARACTER_290cca8e_1e27_48bc_8bae_0842b07bb12e("ALERT: WATERTANK OVERRUN!");
static /*const*/ pearlrt::Character<26>         CONST_CHARACTER_c6ec4c11_4b64_42bc_bd63_0518417a8546("ALERT: WATERTANK UNDERRUN!");
static /*const*/ pearlrt::Character<23>         CONST_CHARACTER_1edd0b68_033e_4d0a_8a87_3cf4bf46ee16("simulation task stopped");
static /*const*/ pearlrt::BitString<1>         CONST_BITSTRING_1_0(0x0);
static /*const*/ pearlrt::BitString<1>         CONST_BITSTRING_1_1(0x1);
static /*const*/ pearlrt::Duration          CONST_DURATION_P_0_0_10_0(10,0,1);
static /*const*/ pearlrt::Duration          CONST_DURATION_P_0_0_1_0(1,0,1);
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
// SYSTEM PART
/////////////////////////////////////////////////////////////////////////////

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

/* public */ void _pump_switch_off(pearlrt::Task *me);

/* public */ void _set_pump_rotational_speed(pearlrt::Task *me, pearlrt::Fixed<15> );

static void _pressure_sensor_2_update(pearlrt::Task *me, pearlrt::Float<23> );

 /* public */ pearlrt::Float<23> _get_level(pearlrt::Task *me);

/* public */ pearlrt::Fixed<31> _get_pump_rotational_speed(pearlrt::Task *me);

static void _pump_update(pearlrt::Task *me, pearlrt::Float<23> );

 /* public */ void _start_simulation(pearlrt::Task *me);

static void _openLogfile(pearlrt::Task *me, pearlrt::RefCharacter , pearlrt::BitString<1> );

 static void _log(pearlrt::Task *me, pearlrt::RefCharacter );

 /* public */ pearlrt::Float<23> _get_pressure_sensor_1(pearlrt::Task *me);

/* public */ pearlrt::Float<23> _get_pressure_sensor_2(pearlrt::Task *me);

/* public */ pearlrt::BitString<1> _get_float_switch_state(pearlrt::Task *me);

static void _tank_update(pearlrt::Task *me, pearlrt::Float<23> );

 /* public */ void _close_valve(pearlrt::Task *me);

/* public */ pearlrt::Float<23> _get_pump_pressure(pearlrt::Task *me);

static void _pressure_sensor_1_update(pearlrt::Task *me, pearlrt::Float<23> );

 static void _float_switch_update(pearlrt::Task *me, pearlrt::Float<23> );

 /* public */ void _pump_switch_on(pearlrt::Task *me);




/////////////////////////////////////////////////////////////////////////////
// DATION SPECIFICATIONS
/////////////////////////////////////////////////////////////////////////////
extern pearlrt::SystemDationNB * _homeFolder; 



/////////////////////////////////////////////////////////////////////////////
// DATION DECLARATIONS
/////////////////////////////////////////////////////////////////////////////
static pearlrt::DationPG *_logFile;
 

/////////////////////////////////////////////////////////////////////////////
// SYSTEM DATION INITIALIZER
/////////////////////////////////////////////////////////////////////////////
static void setupDationsAndPointers() {
   static pearlrt::DationDim2 h_dim_logFile(80); 
   static pearlrt::DationPG d_logFile(_homeFolder, pearlrt::Dation::OUT  | pearlrt::Dation::FORWARD | pearlrt::Dation::NOCYCL | pearlrt::Dation::STREAM, &h_dim_logFile);
   _logFile = &d_logFile;




} 

static pearlrt::Control::Initializer init={setupDationsAndPointers,NULL};
static int dummy = pearlrt::Control::addInitializer(&init);

/////////////////////////////////////////////////////////////////////////////
// VARIABLE DECLARATIONS
/////////////////////////////////////////////////////////////////////////////
static pearlrt::Character<7>  _fileName (CONST_CHARACTER_b9a1bc2d_347d_4a1b_a4aa_50c9c20ac096); 
static pearlrt::Character<80>  _message ; 
static const pearlrt::Float<23>  _pi (CONST_FLOAT_P_3_1415926_23); 
static pearlrt::BitString<1>  _simulation_is_running (CONST_BITSTRING_1_0); 
static pearlrt::Semaphore _sema ( 1,"_sema");
 
static pearlrt::Fixed<31>  _step (CONST_FIXED_P_0_1); 
static const pearlrt::Float<23>  _rho (CONST_FLOAT_P_1000_0_23); 
static const pearlrt::Float<23>  _tank_length (CONST_FLOAT_P_2_0_23); 
static const pearlrt::Float<23>  _tank_width (CONST_FLOAT_P_2_0_23); 
static const pearlrt::Float<23>  _tank_height (CONST_FLOAT_P_2_0_23); 
static pearlrt::Float<23>  _tank_fill_level (CONST_FLOAT_P_0_0_23); 
static pearlrt::Float<23>  _pipe_diameter (CONST_FLOAT_P_0_06_23); 
static pearlrt::Fixed<31>  _pump_max_rpm (CONST_FIXED_P_2000_11); 
static pearlrt::Fixed<15>  _pump_idle_rpm (CONST_FIXED_P_100_7); 
static pearlrt::Float<23>  _pump_spin_up_speed (CONST_FLOAT_P_300_0_23); 
static pearlrt::Float<23>  _pump_spin_down_speed (CONST_FLOAT_P_1000_0_23); 
static pearlrt::Fixed<15>  _pump_requested_rpm (CONST_FIXED_P_0_1); 
static pearlrt::Float<23>  _pump_current_rpm (CONST_FLOAT_P_0_0_23); 
static pearlrt::BitString<1>  _pump_enabled (CONST_BITSTRING_1_0); 
static pearlrt::Fixed<31>  _pump_activations (CONST_FIXED_P_0_1); 
static pearlrt::Float<23>  _pump_output_flow_rate (CONST_FLOAT_P_0_0_23); 
static pearlrt::Float<23>  _pressure_sensor_1 (CONST_FLOAT_P_0_0_23); 
static pearlrt::Float<23>  _pressure_sensor_2 (CONST_FLOAT_P_0_0_23); 
static pearlrt::BitString<1>  _valve_enabled (CONST_BITSTRING_1_0); 
static pearlrt::BitString<1>  _float_switch_enabled (CONST_BITSTRING_1_0); 
static const pearlrt::Float<23>  _min_consumer_dissipation (CONST_FLOAT_P_0_0_23); 
static const pearlrt::Float<23>  _max_consumer_dissipation (CONST_FLOAT_P_0_1_23); 
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
    me->setLocation(129, "SimWatertank.prl");
    _openLogfile( me, pearlrt::RefCharacter(_fileName), CONST_BITSTRING_1_0);

    me->setLocation(130, "SimWatertank.prl");
    _log( me, pearlrt::RefCharacter(CONST_CHARACTER_67e3435d_32a9_40b3_a759_0ef894570bfd));

    me->setLocation(131, "SimWatertank.prl");
    _simulation_is_running = CONST_BITSTRING_1_1;

    me->setLocation(132, "SimWatertank.prl");
        task_log_status.activate( me,
    	     0,
    	    /* prio   */  pearlrt::Prio(),
    	    /* at     */  pearlrt::Clock(),
    	    /* after  */  pearlrt::Duration(),
    	    /* all    */  pearlrt::Duration(),
    	    /* until  */  pearlrt::Clock(),
    	    /* during */  pearlrt::Duration(),
    	    /* when   */  0
                       );



    me->setLocation(133, "SimWatertank.prl");
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



    me->setLocation(134, "SimWatertank.prl");
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



    me->setLocation(135, "SimWatertank.prl");
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
    me->setLocation(140, "SimWatertank.prl");
    _log( me, pearlrt::RefCharacter(CONST_CHARACTER_0027ba60_5fa5_4279_89e3_bf7681ab953f));

    me->setLocation(141, "SimWatertank.prl");
    _simulation_is_running = CONST_BITSTRING_1_0;

    me->setLocation(142, "SimWatertank.prl");
    _closeLogfile( me);


    me->setLocation(locationLine,locationFile);
}
static void
_openLogfile(pearlrt::Task *me, pearlrt::RefCharacter _logFileName, pearlrt::BitString<1> _addToExistingFile)
{
    const char* locationFile = me->getLocationFile();
    int locationLine = me->getLocationLine(); 
    pearlrt::Character<30>  _fn ;

    me->setLocation(148, "SimWatertank.prl");
    _logFileName.store(_fn);

    me->setLocation(150, "SimWatertank.prl");
    	if (_addToExistingFile.getBoolean()) {
    	    me->setLocation(151, "SimWatertank.prl");
    	    {
    	        _logFile->dationOpen(
    	            pearlrt::Dation::IDF | pearlrt::Dation::ANY
    	            , &_fn
    	        , (pearlrt::Fixed<31>*) 0
    	        );
    	    }


    	    me->setLocation(152, "SimWatertank.prl");
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
    	    me->setLocation(156, "SimWatertank.prl");
    	    {
    	        _logFile->dationOpen(
    	            pearlrt::Dation::IDF | pearlrt::Dation::ANY
    	            , &_fn
    	        , (pearlrt::Fixed<31>*) 0
    	        );
    	    }


    	    me->setLocation(157, "SimWatertank.prl");
    	      _logFile->dationClose(pearlrt::Dation::CAN, (pearlrt::Fixed<15>*) 0);


    	    me->setLocation(161, "SimWatertank.prl");
    	    {
    	        _logFile->dationOpen(
    	            pearlrt::Dation::IDF | pearlrt::Dation::NEW
    	            , &_fn
    	        , (pearlrt::Fixed<31>*) 0
    	        );
    	    }




    	}



    me->setLocation(locationLine,locationFile);
}
 static void
_closeLogfile(pearlrt::Task *me)
{
    const char* locationFile = me->getLocationFile();
    int locationLine = me->getLocationLine(); 
    me->setLocation(167, "SimWatertank.prl");
      _logFile->dationClose(0, (pearlrt::Fixed<15>*) 0);



    me->setLocation(locationLine,locationFile);
}
 static void
_log(pearlrt::Task *me, pearlrt::RefCharacter _line)
{
    const char* locationFile = me->getLocationFile();
    int locationLine = me->getLocationLine(); 
    me->setLocation(172, "SimWatertank.prl");
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
            .dataPtr={.outData=&CONST_CHARACTER_2443b140_ca9b_4acf_9965_fc6714930c22},
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
    me->setLocation(197, "SimWatertank.prl");
    	   me->setLocation(locationLine,locationFile);
    	   return (_tank_fill_level);



    me->setLocation(locationLine,locationFile);
}
/* public */ pearlrt::Fixed<31>
_get_pump_rotational_speed(pearlrt::Task *me)
{
    const char* locationFile = me->getLocationFile();
    int locationLine = me->getLocationLine(); 
    me->setLocation(202, "SimWatertank.prl");
    	   me->setLocation(locationLine,locationFile);
    	   return ((_pump_current_rpm).entier());



    me->setLocation(locationLine,locationFile);
}
/* public */ void
_set_pump_rotational_speed(pearlrt::Task *me, pearlrt::Fixed<15> _rpm)
{
    const char* locationFile = me->getLocationFile();
    int locationLine = me->getLocationLine(); 
    me->setLocation(207, "SimWatertank.prl");
    _pump_requested_rpm = _rpm;


    me->setLocation(locationLine,locationFile);
}
/* public */ void
_pump_switch_on(pearlrt::Task *me)
{
    const char* locationFile = me->getLocationFile();
    int locationLine = me->getLocationLine(); 
    me->setLocation(212, "SimWatertank.prl");
    {
    pearlrt::Semaphore *tmp_0[] = {&_sema}; 
       	 	
       pearlrt::Semaphore::request( me, 1, tmp_0);
        


    }
    me->setLocation(214, "SimWatertank.prl");
    	if (_pump_enabled.bitNot().getBoolean()) {
    	    me->setLocation(216, "SimWatertank.prl");
    	    _pump_activations = _pump_activations+CONST_FIXED_P_1_1;



    	}


    me->setLocation(219, "SimWatertank.prl");
    {
    pearlrt::Semaphore *tmp_0[] = {&_sema}; 
       	 	
      pearlrt::Semaphore::release( me, 1, tmp_0);
        


    }
    me->setLocation(221, "SimWatertank.prl");
    _log( me, pearlrt::RefCharacter(CONST_CHARACTER_bd4347dc_b43e_4bae_99ea_3a77d55348db));

    me->setLocation(222, "SimWatertank.prl");
    _pump_enabled = CONST_BITSTRING_1_1;


    me->setLocation(locationLine,locationFile);
}
/* public */ void
_pump_switch_off(pearlrt::Task *me)
{
    const char* locationFile = me->getLocationFile();
    int locationLine = me->getLocationLine(); 
    me->setLocation(227, "SimWatertank.prl");
    {
    pearlrt::Semaphore *tmp_0[] = {&_sema}; 
       	 	
       pearlrt::Semaphore::request( me, 1, tmp_0);
        


    }
    me->setLocation(228, "SimWatertank.prl");
    _log( me, pearlrt::RefCharacter(CONST_CHARACTER_d2943537_2d73_4fe7_9bdc_d9cc93baab7c));

    me->setLocation(229, "SimWatertank.prl");
    _pump_enabled = CONST_BITSTRING_1_0;

    me->setLocation(230, "SimWatertank.prl");
    _pump_requested_rpm = CONST_FIXED_P_0_1;

    me->setLocation(231, "SimWatertank.prl");
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
    me->setLocation(236, "SimWatertank.prl");
    	   me->setLocation(locationLine,locationFile);
    	   return (CONST_FLOAT_P_45_0_23);



    me->setLocation(locationLine,locationFile);
}
/* public */ void
_open_valve(pearlrt::Task *me)
{
    const char* locationFile = me->getLocationFile();
    int locationLine = me->getLocationLine(); 
    me->setLocation(241, "SimWatertank.prl");
    {
    pearlrt::Semaphore *tmp_0[] = {&_sema}; 
       	 	
       pearlrt::Semaphore::request( me, 1, tmp_0);
        


    }
    me->setLocation(242, "SimWatertank.prl");
    _valve_enabled = CONST_BITSTRING_1_1;

    me->setLocation(243, "SimWatertank.prl");
    _log( me, pearlrt::RefCharacter(CONST_CHARACTER_50b9d833_3482_4f45_9124_7050fe3a3011));

    me->setLocation(244, "SimWatertank.prl");
    {
    pearlrt::Semaphore *tmp_0[] = {&_sema}; 
       	 	
      pearlrt::Semaphore::release( me, 1, tmp_0);
        


    }
    me->setLocation(locationLine,locationFile);
}
/* public */ void
_close_valve(pearlrt::Task *me)
{
    const char* locationFile = me->getLocationFile();
    int locationLine = me->getLocationLine(); 
    me->setLocation(249, "SimWatertank.prl");
    {
    pearlrt::Semaphore *tmp_0[] = {&_sema}; 
       	 	
       pearlrt::Semaphore::request( me, 1, tmp_0);
        


    }
    me->setLocation(250, "SimWatertank.prl");
    _valve_enabled = CONST_BITSTRING_1_0;

    me->setLocation(251, "SimWatertank.prl");
    _log( me, pearlrt::RefCharacter(CONST_CHARACTER_af721cd0_bcb6_4a6b_9404_74abd7e1a62a));

    me->setLocation(252, "SimWatertank.prl");
    {
    pearlrt::Semaphore *tmp_0[] = {&_sema}; 
       	 	
      pearlrt::Semaphore::release( me, 1, tmp_0);
        


    }
    me->setLocation(locationLine,locationFile);
}
/* public */ pearlrt::Float<23>
_get_watertank_capacity(pearlrt::Task *me)
{
    const char* locationFile = me->getLocationFile();
    int locationLine = me->getLocationLine(); 
    me->setLocation(257, "SimWatertank.prl");
    	   me->setLocation(locationLine,locationFile);
    	   return (_tank_length*_tank_width*_tank_height);



    me->setLocation(locationLine,locationFile);
}
/* public */ pearlrt::Float<23>
_get_watertank_pressure(pearlrt::Task *me)
{
    const char* locationFile = me->getLocationFile();
    int locationLine = me->getLocationLine(); 
    me->setLocation(262, "SimWatertank.prl");
    {
    pearlrt::Semaphore *tmp_0[] = {&_sema}; 
       	 	
       pearlrt::Semaphore::request( me, 1, tmp_0);
        


    }
    me->setLocation(263, "SimWatertank.prl");
    {
    pearlrt::Semaphore *tmp_0[] = {&_sema}; 
       	 	
      pearlrt::Semaphore::release( me, 1, tmp_0);
        


    }
    me->setLocation(265, "SimWatertank.prl");
    	   me->setLocation(locationLine,locationFile);
    	   return (CONST_FLOAT_P_46_0_23);



    me->setLocation(locationLine,locationFile);
}
/* public */ pearlrt::BitString<1>
_get_float_switch_state(pearlrt::Task *me)
{
    const char* locationFile = me->getLocationFile();
    int locationLine = me->getLocationLine(); 
    pearlrt::BitString<1>  _state ;

    me->setLocation(273, "SimWatertank.prl");
    {
    pearlrt::Semaphore *tmp_0[] = {&_sema}; 
       	 	
       pearlrt::Semaphore::request( me, 1, tmp_0);
        


    }
    me->setLocation(274, "SimWatertank.prl");
    _state = _float_switch_enabled;

    me->setLocation(275, "SimWatertank.prl");
    {
    pearlrt::Semaphore *tmp_0[] = {&_sema}; 
       	 	
      pearlrt::Semaphore::release( me, 1, tmp_0);
        


    }
    me->setLocation(276, "SimWatertank.prl");
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

    me->setLocation(282, "SimWatertank.prl");
    {
    pearlrt::Semaphore *tmp_0[] = {&_sema}; 
       	 	
       pearlrt::Semaphore::request( me, 1, tmp_0);
        


    }
    me->setLocation(283, "SimWatertank.prl");
    {
    pearlrt::Semaphore *tmp_0[] = {&_sema}; 
       	 	
      pearlrt::Semaphore::release( me, 1, tmp_0);
        


    }
    me->setLocation(284, "SimWatertank.prl");
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

    me->setLocation(290, "SimWatertank.prl");
    {
    pearlrt::Semaphore *tmp_0[] = {&_sema}; 
       	 	
       pearlrt::Semaphore::request( me, 1, tmp_0);
        


    }
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
    pearlrt::Character<80>  _msg ;

    me->setLocation(363, "SimWatertank.prl");
    	if (_pump_enabled.getBoolean()) {
    	    me->setLocation(365, "SimWatertank.prl");
    	    _pump_current_rpm = _pump_current_rpm+((_dt*_pump_spin_up_speed))/((pearlrt::Float<23>)(
    	    CONST_FIXED_P_1000_10));

    	    me->setLocation(367, "SimWatertank.prl");
    	    	if ((_pump_current_rpm > _pump_requested_rpm).getBoolean()) {
    	    	    me->setLocation(369, "SimWatertank.prl");
    	    	    _pump_current_rpm = _pump_requested_rpm;



    	    	}




    	} else {
    	    me->setLocation(372, "SimWatertank.prl");
    	    	if ((_pump_current_rpm > CONST_FLOAT_P_0_0_23).getBoolean()) {
    	    	    me->setLocation(374, "SimWatertank.prl");
    	    	    _pump_current_rpm = _pump_current_rpm-((_dt*_pump_spin_down_speed))/((pearlrt::Float<23>)(
    	    	    CONST_FIXED_P_1000_10));

    	    	    me->setLocation(376, "SimWatertank.prl");
    	    	    	if ((_pump_current_rpm < CONST_FLOAT_P_0_0_23).getBoolean()) {
    	    	    	    me->setLocation(378, "SimWatertank.prl");
    	    	    	    _pump_current_rpm = CONST_FLOAT_P_0_0_23;



    	    	    	}




    	    	}




    	}


    me->setLocation(389, "SimWatertank.prl");
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

    me->setLocation(417, "SimWatertank.prl");
    _a = ((_pi*_pipe_diameter.pow(CONST_FIXED_P_2_2)))/((pearlrt::Float<23>)(CONST_FIXED_P_4_3));

    me->setLocation(418, "SimWatertank.prl");
    _v = ((_pump_output_flow_rate))/((_a));

    me->setLocation(420, "SimWatertank.prl");
    	if ((_pump_current_rpm > CONST_FIXED_P_0_1).getBoolean()) {
    	    me->setLocation(422, "SimWatertank.prl");
    	    _pressure_sensor_1 = (((CONST_FIXED_P_20000_15*CONST_FLOAT_P_9_81_23)))/((pearlrt::Float<23>)(
    	    CONST_FIXED_P_1000_10))+(((CONST_FIXED_P_1000_10*_v.pow(CONST_FIXED_P_2_2)
    	    )))/((_pump_current_rpm));



    	} else {
    	    me->setLocation(424, "SimWatertank.prl");
    	    _pressure_sensor_1 = CONST_FLOAT_P_0_0_23;



    	}



    me->setLocation(locationLine,locationFile);
}
 static void
_pressure_sensor_2_update(pearlrt::Task *me, pearlrt::Float<23> _dt)
{
    const char* locationFile = me->getLocationFile();
    int locationLine = me->getLocationLine(); 
    me->setLocation(442, "SimWatertank.prl");
    _pressure_sensor_2 = ((_tank_fill_level))/(((_tank_width*_tank_width)*CONST_FLOAT_P_9_81_23
    *CONST_FIXED_P_10_4));


    me->setLocation(locationLine,locationFile);
}
 static void
_tank_update(pearlrt::Task *me, pearlrt::Float<23> _dt)
{
    const char* locationFile = me->getLocationFile();
    int locationLine = me->getLocationLine(); 
    me->setLocation(447, "SimWatertank.prl");
    	if (_valve_enabled.getBoolean()) {
    	    me->setLocation(448, "SimWatertank.prl");
    	    _tank_fill_level = _tank_fill_level+((_pump_output_flow_rate))/((pearlrt::Float<23>)(
    	    CONST_FIXED_P_60_6))-_current_consumer_dissipation;



    	} else {
    	    me->setLocation(452, "SimWatertank.prl");
    	    _tank_fill_level = _tank_fill_level-_current_consumer_dissipation;



    	}


    me->setLocation(456, "SimWatertank.prl");
    	if ((_tank_fill_level >= _get_watertank_capacity( me)).getBoolean()) {
    	    me->setLocation(458, "SimWatertank.prl");
    	    _tank_fill_level = _get_watertank_capacity( me);



    	}


    me->setLocation(461, "SimWatertank.prl");
    	if ((_tank_fill_level < CONST_FLOAT_P_0_0_23).getBoolean()) {
    	    me->setLocation(463, "SimWatertank.prl");
    	    _tank_fill_level = CONST_FLOAT_P_0_0_23;



    	}



    me->setLocation(locationLine,locationFile);
}
 static void
_float_switch_update(pearlrt::Task *me, pearlrt::Float<23> _dt)
{
    const char* locationFile = me->getLocationFile();
    int locationLine = me->getLocationLine(); 
    me->setLocation(469, "SimWatertank.prl");
    _float_switch_enabled = (_tank_fill_level >= _get_watertank_capacity( me));


    me->setLocation(locationLine,locationFile);
}
 

/////////////////////////////////////////////////////////////////////////////
// TASK DECLARATIONS
/////////////////////////////////////////////////////////////////////////////
DCLTASK(_consumer, (pearlrt::Prio( (pearlrt::Fixed<15>)255)), ((pearlrt::BitString<1>)0)) {
    pearlrt::Fixed<15>  _step (CONST_FIXED_P_0_1);

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
                               _current_consumer_dissipation = ((((((pearlrt::Float<23>)(
                               (CONST_FIXED_P_2_2*_step))).sin())*(((pearlrt::Float<23>)(
                               (CONST_FIXED_P_3_2*_step))).cos())))/((pearlrt::Float<23>)(
                               CONST_FIXED_P_100_7))).abs();

                               me->setLocation(302, "SimWatertank.prl");
                               _step = _step+CONST_FIXED_P_1_1;

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
DCLTASK(_log_status, (pearlrt::Prio( (pearlrt::Fixed<15>)255)), ((pearlrt::BitString<1>)0)) {
    pearlrt::Character<80>  _line ;

    me->setLocation(312, "SimWatertank.prl");
    _log( me, pearlrt::RefCharacter(CONST_CHARACTER_0abe6ea3_11f1_4e68_b6c8_f9e26ca87b9a));

    me->setLocation(313, "SimWatertank.prl");
    _log( me, pearlrt::RefCharacter(CONST_CHARACTER_fb2e0adc_5e6b_4ae5_b050_a883532d7f5d));

    me->setLocation(314, "SimWatertank.prl");
    _log( me, pearlrt::RefCharacter(CONST_CHARACTER_7ddd70b3_a7e9_4740_8f8d_5377224a8232));

    me->setLocation(315, "SimWatertank.prl");
    _log( me, pearlrt::RefCharacter(CONST_CHARACTER_1799d3fa_a8e1_4bdd_a2b5_7d4361447c87));

    me->setLocation(316, "SimWatertank.prl");
    _log( me, pearlrt::RefCharacter(CONST_CHARACTER_7dade90b_6089_4ca0_b2ea_a85295cb083e));

    me->setLocation(317, "SimWatertank.prl");
    _log( me, pearlrt::RefCharacter(CONST_CHARACTER_2e211b9a_d221_478f_80a1_2ec01170d40d));

    me->setLocation(318, "SimWatertank.prl");
    _log( me, pearlrt::RefCharacter(CONST_CHARACTER_794a7bcf_4f79_45b3_b5b0_12113091f986));

    me->setLocation(319, "SimWatertank.prl");
    _log( me, pearlrt::RefCharacter(CONST_CHARACTER_c40282f0_39d3_419c_b244_3ed3505e0a60));

    me->setLocation(320, "SimWatertank.prl");
    _log( me, pearlrt::RefCharacter(CONST_CHARACTER_9397f290_bac2_4283_b692_d00d847af6bc));

    me->setLocation(321, "SimWatertank.prl");
    _log( me, pearlrt::RefCharacter(CONST_CHARACTER_2e31551a_1268_44b4_94a4_134f810ba0d9));

    me->setLocation(322, "SimWatertank.prl");
    _log( me, pearlrt::RefCharacter(CONST_CHARACTER_25cfa725_00cb_42c3_af1e_587664aa43d7));

    me->setLocation(323, "SimWatertank.prl");
    _log( me, pearlrt::RefCharacter(CONST_CHARACTER_9149eac3_a7c7_461b_92f6_6257b657feda));

    me->setLocation(324, "SimWatertank.prl");
    _log( me, pearlrt::RefCharacter(CONST_CHARACTER_bc09941a_1cc8_46f1_ba48_4964c119c2a9));

    me->setLocation(325, "SimWatertank.prl");
    _log( me, pearlrt::RefCharacter(CONST_CHARACTER_93f6170e_4265_4c4f_bbfc_845e03d5fc6e));

    me->setLocation(326, "SimWatertank.prl");
    {   // REPEAT 
            while ( 1 )
            {

                if (!(_simulation_is_running.getBoolean()))
                    break;

                 
                               me->setLocation(328, "SimWatertank.prl");
                               {
                               pearlrt::Semaphore *tmp_0[] = {&_sema}; 
                                  	 	
                                  pearlrt::Semaphore::request( me, 1, tmp_0);
                                   


                               }
                               me->setLocation(329, "SimWatertank.prl");
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
                                    .format=pearlrt::IOFormatEntry::B1w,
                                    .fp1={.f31=CONST_FIXED_P_1_1}
                                 },
                                 {
                                    .format=pearlrt::IOFormatEntry::A
                                 },
                                 {
                                    .format=pearlrt::IOFormatEntry::F,
                                    .fp1={.f31=CONST_FIXED_P_8_4},
                                    .fp2={.f31=CONST_FIXED_P_2_2}
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
                                       .dataPtr={.outData=&CONST_CHARACTER_6252f135_4bdb_4288_aaf3_d79c0f7e66b9},
                                       .param1={.numberOfElements = 1},
                                    },
                                    {
                                       .dataType={pearlrt::IODataEntry::BIT,1},
                                       .dataPtr={.outData=&_simulation_is_running},
                                       .param1={.numberOfElements = 1},
                                    },
                                    {
                                       .dataType={pearlrt::IODataEntry::CHAR,1},
                                       .dataPtr={.outData=&CONST_CHARACTER_6252f135_4bdb_4288_aaf3_d79c0f7e66b9},
                                       .param1={.numberOfElements = 1},
                                    },
                                    {
                                       .dataType={pearlrt::IODataEntry::BIT,1},
                                       .dataPtr={.outData=&_pump_enabled},
                                       .param1={.numberOfElements = 1},
                                    },
                                    {
                                       .dataType={pearlrt::IODataEntry::CHAR,1},
                                       .dataPtr={.outData=&CONST_CHARACTER_6252f135_4bdb_4288_aaf3_d79c0f7e66b9},
                                       .param1={.numberOfElements = 1},
                                    },
                                    {
                                       .dataType={pearlrt::IODataEntry::FLOAT,23},
                                       .dataPtr={.outData=&_pump_current_rpm},
                                       .param1={.numberOfElements = 1},
                                    },
                                    {
                                       .dataType={pearlrt::IODataEntry::CHAR,1},
                                       .dataPtr={.outData=&CONST_CHARACTER_6252f135_4bdb_4288_aaf3_d79c0f7e66b9},
                                       .param1={.numberOfElements = 1},
                                    },
                                    {
                                       .dataType={pearlrt::IODataEntry::FIXED,31},
                                       .dataPtr={.outData=&_pump_activations},
                                       .param1={.numberOfElements = 1},
                                    },
                                    {
                                       .dataType={pearlrt::IODataEntry::CHAR,1},
                                       .dataPtr={.outData=&CONST_CHARACTER_6252f135_4bdb_4288_aaf3_d79c0f7e66b9},
                                       .param1={.numberOfElements = 1},
                                    },
                                    {
                                       .dataType={pearlrt::IODataEntry::FLOAT,23},
                                       .dataPtr={.outData=&_pump_output_flow_rate},
                                       .param1={.numberOfElements = 1},
                                    },
                                    {
                                       .dataType={pearlrt::IODataEntry::CHAR,1},
                                       .dataPtr={.outData=&CONST_CHARACTER_6252f135_4bdb_4288_aaf3_d79c0f7e66b9},
                                       .param1={.numberOfElements = 1},
                                    },
                                    {
                                       .dataType={pearlrt::IODataEntry::FLOAT,23},
                                       .dataPtr={.outData=&_pressure_sensor_1},
                                       .param1={.numberOfElements = 1},
                                    },
                                    {
                                       .dataType={pearlrt::IODataEntry::CHAR,1},
                                       .dataPtr={.outData=&CONST_CHARACTER_6252f135_4bdb_4288_aaf3_d79c0f7e66b9},
                                       .param1={.numberOfElements = 1},
                                    },
                                    {
                                       .dataType={pearlrt::IODataEntry::BIT,1},
                                       .dataPtr={.outData=&_valve_enabled},
                                       .param1={.numberOfElements = 1},
                                    },
                                    {
                                       .dataType={pearlrt::IODataEntry::CHAR,1},
                                       .dataPtr={.outData=&CONST_CHARACTER_6252f135_4bdb_4288_aaf3_d79c0f7e66b9},
                                       .param1={.numberOfElements = 1},
                                    },
                                    {
                                       .dataType={pearlrt::IODataEntry::FLOAT,23},
                                       .dataPtr={.outData=&_pressure_sensor_2},
                                       .param1={.numberOfElements = 1},
                                    },
                                    {
                                       .dataType={pearlrt::IODataEntry::CHAR,1},
                                       .dataPtr={.outData=&CONST_CHARACTER_6252f135_4bdb_4288_aaf3_d79c0f7e66b9},
                                       .param1={.numberOfElements = 1},
                                    },
                                    {
                                       .dataType={pearlrt::IODataEntry::BIT,1},
                                       .dataPtr={.outData=&_float_switch_enabled},
                                       .param1={.numberOfElements = 1},
                                    },
                                    {
                                       .dataType={pearlrt::IODataEntry::CHAR,1},
                                       .dataPtr={.outData=&CONST_CHARACTER_6252f135_4bdb_4288_aaf3_d79c0f7e66b9},
                                       .param1={.numberOfElements = 1},
                                    },
                                    {
                                       .dataType={pearlrt::IODataEntry::FLOAT,23},
                                       .dataPtr={.outData=&_current_consumer_dissipation},
                                       .param1={.numberOfElements = 1},
                                    },
                                    {
                                       .dataType={pearlrt::IODataEntry::CHAR,1},
                                       .dataPtr={.outData=&CONST_CHARACTER_6252f135_4bdb_4288_aaf3_d79c0f7e66b9},
                                       .param1={.numberOfElements = 1},
                                    },
                                    {
                                       .dataType={pearlrt::IODataEntry::FLOAT,23},
                                       .dataPtr={.outData=&_tank_fill_level},
                                       .param1={.numberOfElements = 1},
                                    },
                                    {
                                       .dataType={pearlrt::IODataEntry::CHAR,1},
                                       .dataPtr={.outData=&CONST_CHARACTER_6252f135_4bdb_4288_aaf3_d79c0f7e66b9},
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

                               me->setLocation(353, "SimWatertank.prl");
                               {
                               pearlrt::Semaphore *tmp_0[] = {&_sema}; 
                                  	 	
                                 pearlrt::Semaphore::release( me, 1, tmp_0);
                                   


                               }
                               me->setLocation(354, "SimWatertank.prl");
                               _log( me, pearlrt::RefCharacter(_line));

                               me->setLocation(355, "SimWatertank.prl");
                                   me->resume( pearlrt::Task::AFTER,
                                         /* at     */  pearlrt::Clock(),
                                         /* after  */  CONST_DURATION_P_0_0_1_0,
                               	  /* when   */  0
                                          );




            } 
    } // REPEAT 



}
DCLTASK(_check_tank_status, (pearlrt::Prio( (pearlrt::Fixed<15>)255)), ((pearlrt::BitString<1>)0)) {
    me->setLocation(474, "SimWatertank.prl");
    {   // REPEAT 
            while ( 1 )
            {

                if (!(_simulation_is_running.getBoolean()))
                    break;

                 
                               me->setLocation(476, "SimWatertank.prl");
                               {
                               pearlrt::Semaphore *tmp_0[] = {&_sema}; 
                                  	 	
                                  pearlrt::Semaphore::request( me, 1, tmp_0);
                                   


                               }
                               me->setLocation(477, "SimWatertank.prl");
                               	if ((_tank_fill_level >= _get_watertank_capacity( me)).getBoolean()) {
                               	    me->setLocation(479, "SimWatertank.prl");
                               	    _log( me, pearlrt::RefCharacter(CONST_CHARACTER_290cca8e_1e27_48bc_8bae_0842b07bb12e));



                               	}


                               me->setLocation(482, "SimWatertank.prl");
                               	if ((_tank_fill_level == CONST_FLOAT_P_0_0_23).bitAnd((
                               _current_consumer_dissipation > CONST_FLOAT_P_0_0_23)).getBoolean()) {
                               	    me->setLocation(484, "SimWatertank.prl");
                               	    _log( me, pearlrt::RefCharacter(CONST_CHARACTER_c6ec4c11_4b64_42bc_bd63_0518417a8546));



                               	}


                               me->setLocation(486, "SimWatertank.prl");
                               {
                               pearlrt::Semaphore *tmp_0[] = {&_sema}; 
                                  	 	
                                 pearlrt::Semaphore::release( me, 1, tmp_0);
                                   


                               }
                               me->setLocation(488, "SimWatertank.prl");
                                   me->resume( pearlrt::Task::AFTER,
                                         /* at     */  pearlrt::Clock(),
                                         /* after  */  CONST_DURATION_P_0_0_5_0,
                               	  /* when   */  0
                                          );




            } 
    } // REPEAT 



}
DCLTASK(_simulation, (pearlrt::Prio( (pearlrt::Fixed<15>)CONST_FIXED_P_100_7)), ((pearlrt::BitString<1>)0)) {
    pearlrt::Clock  _current_clock ;
    pearlrt::Float<23>  _dt ;
    pearlrt::Character<80>  _msg ;
    pearlrt::Fixed<31>  _err ;

    me->setLocation(499, "SimWatertank.prl");
    {   // REPEAT 
            while ( 1 )
            {

                if (!(_simulation_is_running.getBoolean()))
                    break;

                 
                               me->setLocation(501, "SimWatertank.prl");
                               {
                               pearlrt::Semaphore *tmp_0[] = {&_sema}; 
                                  	 	
                                  pearlrt::Semaphore::request( me, 1, tmp_0);
                                   


                               }
                               me->setLocation(502, "SimWatertank.prl");
                               _current_clock = pearlrt::Clock::now();

                               me->setLocation(503, "SimWatertank.prl");
                               _dt = (((_current_clock-_last_update)))/((CONST_DURATION_P_0_0_0_001));

                               me->setLocation(505, "SimWatertank.prl");
                               _pump_update( me, _dt);

                               me->setLocation(506, "SimWatertank.prl");
                               _pressure_sensor_1_update( me, _dt);

                               me->setLocation(507, "SimWatertank.prl");
                               _pressure_sensor_2_update( me, _dt);

                               me->setLocation(508, "SimWatertank.prl");
                               _tank_update( me, _dt);

                               me->setLocation(509, "SimWatertank.prl");
                               _float_switch_update( me, _dt);

                               me->setLocation(511, "SimWatertank.prl");
                               {
                               pearlrt::Semaphore *tmp_0[] = {&_sema}; 
                                  	 	
                                 pearlrt::Semaphore::release( me, 1, tmp_0);
                                   


                               }
                               me->setLocation(513, "SimWatertank.prl");
                               _last_update = _current_clock;

                               me->setLocation(515, "SimWatertank.prl");
                                   me->resume( pearlrt::Task::AFTER,
                                         /* at     */  pearlrt::Clock(),
                                         /* after  */  CONST_DURATION_P_0_0_0_01,
                               	  /* when   */  0
                                          );




            } 
    } // REPEAT 


    me->setLocation(518, "SimWatertank.prl");
    _log( me, pearlrt::RefCharacter(CONST_CHARACTER_1edd0b68_033e_4d0a_8a87_3cf4bf46ee16));


}




}
