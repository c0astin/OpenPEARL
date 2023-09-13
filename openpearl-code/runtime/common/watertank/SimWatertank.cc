#include <PearlIncludes.h>

namespace ns_SimWatertank {
/////////////////////////////////////////////////////////////////////////////
// PROLOGUE
/////////////////////////////////////////////////////////////////////////////


static const char* filename = (char*) "SimWatertank.prl";



/////////////////////////////////////////////////////////////////////////////
// CONSTANT POOL
/////////////////////////////////////////////////////////////////////////////
static /*const*/ pearlrt::Fixed<1>         CONST_FIXED_P_0_1(0);
static /*const*/ pearlrt::Fixed<11>         CONST_FIXED_P_2000_11(2000);
static /*const*/ pearlrt::Fixed<7>         CONST_FIXED_P_100_7(100);
static /*const*/ pearlrt::Fixed<4>         CONST_FIXED_P_10_4(10);
static /*const*/ pearlrt::Fixed<4>         CONST_FIXED_P_15_4(15);
static /*const*/ pearlrt::Fixed<5>         CONST_FIXED_P_25_5(25);
static /*const*/ pearlrt::Fixed<3>         CONST_FIXED_P_5_3(5);
static /*const*/ pearlrt::Fixed<5>         CONST_FIXED_P_30_5(30);
static /*const*/ pearlrt::Fixed<1>         CONST_FIXED_P_1_1(1);
static /*const*/ pearlrt::Fixed<31>         CONST_FIXED_P_0_31(0);
static /*const*/ pearlrt::Fixed<31>         CONST_FIXED_P_1_31(1);
static /*const*/ pearlrt::Fixed<31>         CONST_FIXED_N_1_31(-1);
static /*const*/ pearlrt::Fixed<7>         CONST_FIXED_P_80_7(80);
static /*const*/ pearlrt::Float<23>         CONST_FLOAT_P_1000_0_23(1000.0);
static /*const*/ pearlrt::Float<23>         CONST_FLOAT_P_1_0_23(1.0);
static /*const*/ pearlrt::Float<23>         CONST_FLOAT_P_0_0_23(0.0);
static /*const*/ pearlrt::Float<23>         CONST_FLOAT_P_100_0_23(100.0);
static /*const*/ pearlrt::Float<23>         CONST_FLOAT_P_300_0_23(300.0);
static /*const*/ pearlrt::Float<23>         CONST_FLOAT_P_0_1_23(0.1);
static /*const*/ pearlrt::Float<23>         CONST_FLOAT_P_45_0_23(45.0);
static /*const*/ pearlrt::Float<23>         CONST_FLOAT_P_46_0_23(46.0);
static /*const*/ pearlrt::Character<7>         CONST_CHARACTER_0159ed1e_cbd5_447f_9748_133c154df33a("sim.log");
static /*const*/ pearlrt::Character<2>         CONST_CHARACTER_6a7f6db1_4891_4fbe_9332_9f0786ffcc58("./");
static /*const*/ pearlrt::Character<15>         CONST_CHARACTER_7d99b221_7f8e_421f_b363_f218262994c1("Stop Simulation");
static /*const*/ pearlrt::Character<20>         CONST_CHARACTER_6693a7d2_bd7a_428d_a9a6_4ab51acfbb39("new log file created");
static /*const*/ pearlrt::Character<78>         CONST_CHARACTER_f1cc5583_2d6a_4aa7_b0fc_43ba7424ada2("------------------------------------------------------------------------------");
static /*const*/ pearlrt::Character<21>         CONST_CHARACTER_31d6320c_8136_40a1_b4e9_6e9eb5d8fef0("Simulator starting...");
static /*const*/ pearlrt::Character<18>         CONST_CHARACTER_45d965e2_ed26_4d96_8838_6c4f822be6b7("Simulator stopped.");
static /*const*/ pearlrt::BitString<1>         CONST_BITSTRING_1_0(0x0);
static /*const*/ pearlrt::BitString<1>         CONST_BITSTRING_1_1(0x1);
static /*const*/ pearlrt::Duration          CONST_DURATION_P_0_0_1_0(1,0,1);
static /*const*/ pearlrt::Duration          CONST_DURATION_P_0_0_0_001(0,1000,1);
static /*const*/ pearlrt::Duration          CONST_DURATION_P_0_0_0_01(0,10000,1);
static /*const*/ pearlrt::Duration          CONST_DURATION_P_0_0_5_0(5,0,1);
static /*const*/ pearlrt::Duration          CONST_DURATION_P_0_0_10_0(10,0,1);

/////////////////////////////////////////////////////////////////////////////
// TASK SPECIFIERS
/////////////////////////////////////////////////////////////////////////////
extern pearlrt::Task task_simulation;


extern pearlrt::Task task_consumer;


extern pearlrt::Task task_test;


extern pearlrt::Task task_log_status;

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
static void _log(pearlrt::Task *me, pearlrt::RefCharacter );

 /* public */ pearlrt::Float<23> _get_watertank_pressure(pearlrt::Task *me);

/* public */ void _open_valve(pearlrt::Task *me);

static void _closeLogfile(pearlrt::Task *me);

 /* public */ pearlrt::Float<23> _get_watertank_capacity(pearlrt::Task *me);

/* public */ void _stop_simulation(pearlrt::Task *me);

/* public */ void _pump_switch_off(pearlrt::Task *me);

/* public */ void _close_valve(pearlrt::Task *me);

/* public */ void _set_pump_rotational_speed(pearlrt::Task *me, pearlrt::Fixed<31> );

/* public */ pearlrt::Float<23> _get_pump_pressure(pearlrt::Task *me);

/* public */ pearlrt::Float<23> _get_level(pearlrt::Task *me);

/* public */ pearlrt::Fixed<31> _get_pump_rotational_speed(pearlrt::Task *me);

/* public */ void _start_simulation(pearlrt::Task *me);

/* public */ void _pump_switch_on(pearlrt::Task *me);

static void _openLogfile(pearlrt::Task *me, pearlrt::RefCharacter , pearlrt::BitString<1> );

 


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
static pearlrt::Character<7>  _fileName (CONST_CHARACTER_0159ed1e_cbd5_447f_9748_133c154df33a); 
static pearlrt::Character<80>  _message ; 
static pearlrt::BitString<1>  _simulation_is_running (CONST_BITSTRING_1_0); 
static pearlrt::Fixed<31>  _step (CONST_FIXED_P_0_1); 
static const pearlrt::Float<23>  _rho (CONST_FLOAT_P_1000_0_23); 
static const pearlrt::Float<23>  _tank_length (CONST_FLOAT_P_1_0_23); 
static const pearlrt::Float<23>  _tank_width (CONST_FLOAT_P_1_0_23); 
static const pearlrt::Float<23>  _tank_height (CONST_FLOAT_P_1_0_23); 
static pearlrt::Float<23>  _tank_fill_level (CONST_FLOAT_P_0_0_23); 
static pearlrt::Fixed<31>  _pump_max_rpm (CONST_FIXED_P_2000_11); 
static pearlrt::Fixed<31>  _pump_idle_rpm (CONST_FIXED_P_100_7); 
static pearlrt::Float<23>  _pump_spin_up_speed (CONST_FLOAT_P_100_0_23); 
static pearlrt::Float<23>  _pump_spin_down_speed (CONST_FLOAT_P_300_0_23); 
static pearlrt::Fixed<31>  _pump_requested_rpm (CONST_FIXED_P_0_1); 
static pearlrt::Fixed<31>  _pump_current_rpm (CONST_FIXED_P_0_1); 
static pearlrt::BitString<1>  _pump_enabled (CONST_BITSTRING_1_0); 
static pearlrt::Fixed<31>  _pump_activations (CONST_FIXED_P_0_1); 
static const pearlrt::Float<23>  _min_consumer_dissipation (CONST_FLOAT_P_0_0_23); 
static const pearlrt::Float<23>  _max_consumer_dissipation (CONST_FLOAT_P_0_1_23); 
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
    me->setLocation(37, "SimWatertank.prl");
    _simulation_is_running = CONST_BITSTRING_1_1;

    me->setLocation(38, "SimWatertank.prl");
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



    me->setLocation(39, "SimWatertank.prl");
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
    me->setLocation(44, "SimWatertank.prl");
    _log( me, pearlrt::RefCharacter(CONST_CHARACTER_7d99b221_7f8e_421f_b363_f218262994c1));

    me->setLocation(45, "SimWatertank.prl");
    _simulation_is_running = CONST_BITSTRING_1_0;

    me->setLocation(46, "SimWatertank.prl");
    		task_simulation.prevent(me);





    me->setLocation(locationLine,locationFile);
}
static void
_openLogfile(pearlrt::Task *me, pearlrt::RefCharacter _logFileName, pearlrt::BitString<1> _addToExistingFile)
{
    const char* locationFile = me->getLocationFile();
    int locationLine = me->getLocationLine(); 
    pearlrt::Character<30>  _fn ;

    me->setLocation(52, "SimWatertank.prl");
    _logFileName.store(_fn);

    me->setLocation(54, "SimWatertank.prl");
    	if (_addToExistingFile.getBoolean()) {
    	    me->setLocation(55, "SimWatertank.prl");
    	    {
    	        _logFile->dationOpen(
    	            pearlrt::Dation::IDF | pearlrt::Dation::ANY
    	            , &_fn
    	        , (pearlrt::Fixed<31>*) 0
    	        );
    	    }


    	    me->setLocation(56, "SimWatertank.prl");
    	    // put statement 
    	    {
    	       static pearlrt::IOFormatEntry formatEntries[]  = {
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
    	    me->setLocation(60, "SimWatertank.prl");
    	    {
    	        _logFile->dationOpen(
    	            pearlrt::Dation::IDF | pearlrt::Dation::ANY
    	            , &_fn
    	        , (pearlrt::Fixed<31>*) 0
    	        );
    	    }


    	    me->setLocation(61, "SimWatertank.prl");
    	      _logFile->dationClose(pearlrt::Dation::CAN, (pearlrt::Fixed<15>*) 0);


    	    me->setLocation(65, "SimWatertank.prl");
    	    {
    	        _logFile->dationOpen(
    	            pearlrt::Dation::IDF | pearlrt::Dation::NEW
    	            , &_fn
    	        , (pearlrt::Fixed<31>*) 0
    	        );
    	    }


    	    me->setLocation(66, "SimWatertank.prl");
    	    _log( me, pearlrt::RefCharacter(CONST_CHARACTER_6693a7d2_bd7a_428d_a9a6_4ab51acfbb39));



    	}



    me->setLocation(locationLine,locationFile);
}
 static void
_closeLogfile(pearlrt::Task *me)
{
    const char* locationFile = me->getLocationFile();
    int locationLine = me->getLocationLine(); 
    me->setLocation(72, "SimWatertank.prl");
      _logFile->dationClose(0, (pearlrt::Fixed<15>*) 0);



    me->setLocation(locationLine,locationFile);
}
 static void
_log(pearlrt::Task *me, pearlrt::RefCharacter _line)
{
    const char* locationFile = me->getLocationFile();
    int locationLine = me->getLocationLine(); 
    me->setLocation(locationLine,locationFile);
}
 /* public */ pearlrt::Float<23>
_get_level(pearlrt::Task *me)
{
    const char* locationFile = me->getLocationFile();
    int locationLine = me->getLocationLine(); 
    me->setLocation(133, "SimWatertank.prl");
    	   me->setLocation(locationLine,locationFile);
    	   return (_tank_fill_level);



    me->setLocation(locationLine,locationFile);
}
/* public */ pearlrt::Fixed<31>
_get_pump_rotational_speed(pearlrt::Task *me)
{
    const char* locationFile = me->getLocationFile();
    int locationLine = me->getLocationLine(); 
    me->setLocation(139, "SimWatertank.prl");
    	   me->setLocation(locationLine,locationFile);
    	   return (_pump_current_rpm);



    me->setLocation(locationLine,locationFile);
}
/* public */ void
_set_pump_rotational_speed(pearlrt::Task *me, pearlrt::Fixed<31> _rpm)
{
    const char* locationFile = me->getLocationFile();
    int locationLine = me->getLocationLine(); 
    me->setLocation(145, "SimWatertank.prl");
    _pump_requested_rpm = _rpm;


    me->setLocation(locationLine,locationFile);
}
/* public */ void
_pump_switch_on(pearlrt::Task *me)
{
    const char* locationFile = me->getLocationFile();
    int locationLine = me->getLocationLine(); 
    me->setLocation(151, "SimWatertank.prl");
    	if (_pump_enabled.bitNot().getBoolean()) {
    	    me->setLocation(153, "SimWatertank.prl");
    	    _pump_activations = _pump_activations+CONST_FIXED_P_1_1;



    	}


    me->setLocation(156, "SimWatertank.prl");
    	if ((_pump_requested_rpm == CONST_FIXED_P_0_1).getBoolean()) {
    	    me->setLocation(158, "SimWatertank.prl");
    	    _pump_requested_rpm = _pump_idle_rpm;



    	}


    me->setLocation(161, "SimWatertank.prl");
    _pump_enabled = CONST_BITSTRING_1_1;


    me->setLocation(locationLine,locationFile);
}
/* public */ void
_pump_switch_off(pearlrt::Task *me)
{
    const char* locationFile = me->getLocationFile();
    int locationLine = me->getLocationLine(); 
    me->setLocation(167, "SimWatertank.prl");
    	if (_pump_enabled.getBoolean()) {
    	    me->setLocation(169, "SimWatertank.prl");
    	    _pump_activations = _pump_activations+CONST_FIXED_P_1_1;



    	}


    me->setLocation(172, "SimWatertank.prl");
    _pump_enabled = CONST_BITSTRING_1_0;

    me->setLocation(173, "SimWatertank.prl");
    _pump_current_rpm = CONST_FIXED_P_0_1;

    me->setLocation(174, "SimWatertank.prl");
    _pump_requested_rpm = CONST_FIXED_P_0_1;


    me->setLocation(locationLine,locationFile);
}
/* public */ pearlrt::Float<23>
_get_pump_pressure(pearlrt::Task *me)
{
    const char* locationFile = me->getLocationFile();
    int locationLine = me->getLocationLine(); 
    me->setLocation(180, "SimWatertank.prl");
    	   me->setLocation(locationLine,locationFile);
    	   return (CONST_FLOAT_P_45_0_23);



    me->setLocation(locationLine,locationFile);
}
/* public */ void
_open_valve(pearlrt::Task *me)
{
    const char* locationFile = me->getLocationFile();
    int locationLine = me->getLocationLine(); 
    me->setLocation(locationLine,locationFile);
}
/* public */ void
_close_valve(pearlrt::Task *me)
{
    const char* locationFile = me->getLocationFile();
    int locationLine = me->getLocationLine(); 
    me->setLocation(locationLine,locationFile);
}
/* public */ pearlrt::Float<23>
_get_watertank_capacity(pearlrt::Task *me)
{
    const char* locationFile = me->getLocationFile();
    int locationLine = me->getLocationLine(); 
    me->setLocation(196, "SimWatertank.prl");
    	   me->setLocation(locationLine,locationFile);
    	   return (_tank_length*_tank_width*_tank_height);



    me->setLocation(locationLine,locationFile);
}
/* public */ pearlrt::Float<23>
_get_watertank_pressure(pearlrt::Task *me)
{
    const char* locationFile = me->getLocationFile();
    int locationLine = me->getLocationLine(); 
    me->setLocation(202, "SimWatertank.prl");
    	   me->setLocation(locationLine,locationFile);
    	   return (CONST_FLOAT_P_46_0_23);



    me->setLocation(locationLine,locationFile);
}


/////////////////////////////////////////////////////////////////////////////
// TASK DECLARATIONS
/////////////////////////////////////////////////////////////////////////////
DCLTASK(_consumer, (pearlrt::Prio( (pearlrt::Fixed<15>)255)), ((pearlrt::BitString<1>)0)) {
}
DCLTASK(_log_status, (pearlrt::Prio( (pearlrt::Fixed<15>)255)), ((pearlrt::BitString<1>)0)) {
    pearlrt::Character<80>  _line ;

    me->setLocation(213, "SimWatertank.prl");
    {   // REPEAT 
            while ( 1 )
            {

                 
                               me->setLocation(214, "SimWatertank.prl");
                               	if ((_simulation_is_running == CONST_BITSTRING_1_0).getBoolean()) {
                               	    me->setLocation(216, "SimWatertank.prl");
                               	    		me->prevent(me);






                               	}


                               me->setLocation(221, "SimWatertank.prl");
                               _log( me, pearlrt::RefCharacter(CONST_CHARACTER_f1cc5583_2d6a_4aa7_b0fc_43ba7424ada2));

                               me->setLocation(222, "SimWatertank.prl");
                                   me->resume( pearlrt::Task::AFTER,
                                         /* at     */  pearlrt::Clock(),
                                         /* after  */  CONST_DURATION_P_0_0_1_0,
                               	  /* when   */  0
                                          );




            } 
    } // REPEAT 



}
DCLTASK(_simulation, (pearlrt::Prio( (pearlrt::Fixed<15>)255)), ((pearlrt::BitString<1>)0)) {
    pearlrt::Duration  _delta_time ;
    pearlrt::Clock  _current_clock ;
    pearlrt::Float<23>  _dt1 ;

    me->setLocation(244, "SimWatertank.prl");
    {   // REPEAT 
            while ( 1 )
            {

                 
                               me->setLocation(245, "SimWatertank.prl");
                               	if ((_simulation_is_running == CONST_BITSTRING_1_0).getBoolean()) {
                               	    me->setLocation(247, "SimWatertank.prl");
                               	    		me->prevent(me);






                               	}


                               me->setLocation(252, "SimWatertank.prl");
                               _current_clock = pearlrt::Clock::now();

                               me->setLocation(253, "SimWatertank.prl");
                               _delta_time = _last_update-_current_clock;

                               me->setLocation(254, "SimWatertank.prl");
                               _dt1 = ((_delta_time))/((CONST_DURATION_P_0_0_0_001));

                               me->setLocation(256, "SimWatertank.prl");
                               	if (_pump_enabled.getBoolean()) {
                               	    me->setLocation(258, "SimWatertank.prl");
                               	    	if ((_pump_current_rpm < _pump_requested_rpm).getBoolean()) {
                               	    	    me->setLocation(260, "SimWatertank.prl");
                               	    	    _pump_current_rpm = _pump_current_rpm+(
                               	    	    (CONST_FLOAT_P_1_0_23*_pump_spin_up_speed
                               	    	    )).entier();

                               	    	    me->setLocation(262, "SimWatertank.prl");
                               	    	    	if ((_pump_current_rpm > _pump_requested_rpm).getBoolean()) {
                               	    	    	    me->setLocation(264, "SimWatertank.prl");
                               	    	    	    _pump_current_rpm = _pump_requested_rpm;



                               	    	    	}




                               	    	}


                               	    me->setLocation(267, "SimWatertank.prl");
                               	    	if ((_pump_current_rpm > _pump_requested_rpm).getBoolean()) {
                               	    	    me->setLocation(269, "SimWatertank.prl");
                               	    	    _pump_current_rpm = _pump_current_rpm-(
                               	    	    (CONST_FLOAT_P_1_0_23*_pump_spin_down_speed
                               	    	    )).entier();

                               	    	    me->setLocation(271, "SimWatertank.prl");
                               	    	    	if ((_pump_current_rpm < _pump_requested_rpm).getBoolean()) {
                               	    	    	    me->setLocation(273, "SimWatertank.prl");
                               	    	    	    _pump_current_rpm = _pump_requested_rpm;



                               	    	    	}




                               	    	}




                               	}


                               me->setLocation(278, "SimWatertank.prl");
                               _tank_fill_level = _tank_fill_level+CONST_FLOAT_P_1_0_23;

                               me->setLocation(279, "SimWatertank.prl");
                               _last_update = _current_clock;

                               me->setLocation(281, "SimWatertank.prl");
                                   me->resume( pearlrt::Task::AFTER,
                                         /* at     */  pearlrt::Clock(),
                                         /* after  */  CONST_DURATION_P_0_0_0_01,
                               	  /* when   */  0
                                          );




            } 
    } // REPEAT 



}
DCLTASK(_test, (pearlrt::Prio( (pearlrt::Fixed<15>)255)), ((pearlrt::BitString<1>)1)) {
    me->setLocation(288, "SimWatertank.prl");
    _openLogfile( me, pearlrt::RefCharacter(_fileName), CONST_BITSTRING_1_1);

    me->setLocation(289, "SimWatertank.prl");
    _log( me, pearlrt::RefCharacter(CONST_CHARACTER_31d6320c_8136_40a1_b4e9_6e9eb5d8fef0));

    me->setLocation(291, "SimWatertank.prl");
    _start_simulation( me);

    me->setLocation(293, "SimWatertank.prl");
        me->resume( pearlrt::Task::AFTER,
              /* at     */  pearlrt::Clock(),
              /* after  */  CONST_DURATION_P_0_0_5_0,
    	  /* when   */  0
               );



    me->setLocation(294, "SimWatertank.prl");
    _pump_switch_on( me);

    me->setLocation(295, "SimWatertank.prl");
        me->resume( pearlrt::Task::AFTER,
              /* at     */  pearlrt::Clock(),
              /* after  */  CONST_DURATION_P_0_0_10_0,
    	  /* when   */  0
               );



    me->setLocation(297, "SimWatertank.prl");
    _pump_switch_on( me);

    me->setLocation(298, "SimWatertank.prl");
        me->resume( pearlrt::Task::AFTER,
              /* at     */  pearlrt::Clock(),
              /* after  */  CONST_DURATION_P_0_0_10_0,
    	  /* when   */  0
               );



    me->setLocation(300, "SimWatertank.prl");
    _set_pump_rotational_speed( me, CONST_FIXED_P_2000_11);

    me->setLocation(301, "SimWatertank.prl");
        me->resume( pearlrt::Task::AFTER,
              /* at     */  pearlrt::Clock(),
              /* after  */  CONST_DURATION_P_0_0_10_0,
    	  /* when   */  0
               );



    me->setLocation(303, "SimWatertank.prl");
    _set_pump_rotational_speed( me, CONST_FIXED_P_100_7);

    me->setLocation(304, "SimWatertank.prl");
        me->resume( pearlrt::Task::AFTER,
              /* at     */  pearlrt::Clock(),
              /* after  */  CONST_DURATION_P_0_0_10_0,
    	  /* when   */  0
               );



    me->setLocation(306, "SimWatertank.prl");
    _set_pump_rotational_speed( me, CONST_FIXED_P_2000_11);

    me->setLocation(307, "SimWatertank.prl");
        me->resume( pearlrt::Task::AFTER,
              /* at     */  pearlrt::Clock(),
              /* after  */  CONST_DURATION_P_0_0_10_0,
    	  /* when   */  0
               );



    me->setLocation(309, "SimWatertank.prl");
    _pump_switch_off( me);

    me->setLocation(310, "SimWatertank.prl");
        me->resume( pearlrt::Task::AFTER,
              /* at     */  pearlrt::Clock(),
              /* after  */  CONST_DURATION_P_0_0_10_0,
    	  /* when   */  0
               );



    me->setLocation(312, "SimWatertank.prl");
    _stop_simulation( me);

    me->setLocation(314, "SimWatertank.prl");
    _log( me, pearlrt::RefCharacter(CONST_CHARACTER_45d965e2_ed26_4d96_8838_6c4f822be6b7));

    me->setLocation(315, "SimWatertank.prl");
    _closeLogfile( me);


}




}
