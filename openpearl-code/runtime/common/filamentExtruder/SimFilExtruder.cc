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
static /*const*/ pearlrt::Fixed<8>         CONST_FIXED_P_170_8(170);
static /*const*/ pearlrt::Fixed<8>         CONST_FIXED_P_210_8(210);
static /*const*/ pearlrt::Fixed<5>         CONST_FIXED_P_21_5(21);
static /*const*/ pearlrt::Fixed<5>         CONST_FIXED_P_25_5(25);
static /*const*/ pearlrt::Fixed<4>         CONST_FIXED_P_15_4(15);
static /*const*/ pearlrt::Fixed<2>         CONST_FIXED_P_3_2(3);
static /*const*/ pearlrt::Fixed<7>         CONST_FIXED_P_100_7(100);
static /*const*/ pearlrt::Fixed<6>         CONST_FIXED_P_50_6(50);
static /*const*/ pearlrt::Fixed<1>         CONST_FIXED_P_0_1(0);
static /*const*/ pearlrt::Fixed<5>         CONST_FIXED_P_23_5(23);
static /*const*/ pearlrt::Fixed<9>         CONST_FIXED_P_300_9(300);
static /*const*/ pearlrt::Fixed<8>         CONST_FIXED_P_200_8(200);
static /*const*/ pearlrt::Fixed<6>         CONST_FIXED_P_52_6(52);
static /*const*/ pearlrt::Fixed<7>         CONST_FIXED_P_70_7(70);
static /*const*/ pearlrt::Fixed<7>         CONST_FIXED_P_67_7(67);
static /*const*/ pearlrt::Fixed<4>         CONST_FIXED_P_10_4(10);
static /*const*/ pearlrt::Fixed<2>         CONST_FIXED_P_2_2(2);
static /*const*/ pearlrt::Fixed<10>         CONST_FIXED_P_1000_10(1000);
static /*const*/ pearlrt::Fixed<6>         CONST_FIXED_P_60_6(60);
static /*const*/ pearlrt::Fixed<8>         CONST_FIXED_P_180_8(180);
static /*const*/ pearlrt::Fixed<13>         CONST_FIXED_P_5000_13(5000);
static /*const*/ pearlrt::Fixed<3>         CONST_FIXED_P_4_3(4);
static /*const*/ pearlrt::Fixed<4>         CONST_FIXED_P_9_4(9);
static /*const*/ pearlrt::Fixed<3>         CONST_FIXED_P_7_3(7);
static /*const*/ pearlrt::Fixed<3>         CONST_FIXED_P_5_3(5);
static /*const*/ pearlrt::Fixed<3>         CONST_FIXED_P_6_3(6);
static /*const*/ pearlrt::Fixed<4>         CONST_FIXED_P_8_4(8);
static /*const*/ pearlrt::Fixed<4>         CONST_FIXED_P_12_4(12);
static /*const*/ pearlrt::Fixed<5>         CONST_FIXED_P_20_5(20);
static /*const*/ pearlrt::Fixed<31>         CONST_FIXED_P_0_31(0);
static /*const*/ pearlrt::Fixed<31>         CONST_FIXED_P_1_31(1);
static /*const*/ pearlrt::Fixed<31>         CONST_FIXED_N_1_31(-1);
static /*const*/ pearlrt::Fixed<16>         CONST_FIXED_P_60000_16(60000);
static /*const*/ pearlrt::Fixed<4>         CONST_FIXED_P_0_4(0);
static /*const*/ pearlrt::Fixed<4>         CONST_FIXED_P_1_4(1);
static /*const*/ pearlrt::Float<23>         CONST_FLOAT_P_10_0_23(10.0);
static /*const*/ pearlrt::Float<23>         CONST_FLOAT_P_3_1415926_23(3.1415926);
static /*const*/ pearlrt::Float<23>         CONST_FLOAT_P_0_00124_23(0.00124);
static /*const*/ pearlrt::Float<23>         CONST_FLOAT_P_1800_0_23(1800.0);
static /*const*/ pearlrt::Float<23>         CONST_FLOAT_P_480_0_23(480.0);
static /*const*/ pearlrt::Float<23>         CONST_FLOAT_P_25_0_23(25.0);
static /*const*/ pearlrt::Float<23>         CONST_FLOAT_P_7_0_23(7.0);
static /*const*/ pearlrt::Float<23>         CONST_FLOAT_P_0_95_23(0.95);
static /*const*/ pearlrt::Float<23>         CONST_FLOAT_P_50_0_23(50.0);
static /*const*/ pearlrt::Float<23>         CONST_FLOAT_P_0_0_23(0.0);
static /*const*/ pearlrt::Float<23>         CONST_FLOAT_P_800_0_23(800.0);
static /*const*/ pearlrt::Float<23>         CONST_FLOAT_P_23_0_23(23.0);
static /*const*/ pearlrt::Float<23>         CONST_FLOAT_P_52_0_23(52.0);
static /*const*/ pearlrt::Float<23>         CONST_FLOAT_P_5_0_23(5.0);
static /*const*/ pearlrt::Float<23>         CONST_FLOAT_P_1000_0_23(1000.0);
static /*const*/ pearlrt::Float<23>         CONST_FLOAT_P_0_03_23(0.03);
static /*const*/ pearlrt::Float<23>         CONST_FLOAT_P_0_2_23(0.2);
static /*const*/ pearlrt::Float<23>         CONST_FLOAT_P_0_01_23(0.01);
static /*const*/ pearlrt::Float<23>         CONST_FLOAT_P_0_5_23(0.5);
static /*const*/ pearlrt::Character<15>         CONST_CHARACTER_ae120d22_c47b_4ea6_bc61_0078eea9cba3("extruderSim.cfg");
static /*const*/ pearlrt::Character<7>         CONST_CHARACTER_6d97f730_8cd9_4787_a1f9_0ee046e575d1("sim.log");
static /*const*/ pearlrt::Character<66>         CONST_CHARACTER_b9752ae0_4fed_4285_bef9_df502832716d("WARNING: Keine Konfigurationsdatei gefunden. Default wird geladen.");
static /*const*/ pearlrt::Character<49>         CONST_CHARACTER_75383ac2_12bc_4c8b_b9ad_4c340efd8fd5("WARNING: Fehler beim Lesen von Config-Eintrag Nr.");
static /*const*/ pearlrt::Character<27>         CONST_CHARACTER_a4efaf43_a194_4d6a_a9a6_610c6cbcff20(".Default-Wert wird geladen.");
static /*const*/ pearlrt::Character<19>         CONST_CHARACTER_dd4c299a_7ac8_4670_bbcd_0edfc3d8695f("STARTING SIMULATION");
static /*const*/ pearlrt::Character<19>         CONST_CHARACTER_41831e0e_0464_44f4_b544_b0858903a816("STOPPING SIMULATION");
static /*const*/ pearlrt::Character<79>         CONST_CHARACTER_c9956ab3_a8ab_4c13_bcb9_c0bc8e5e7ee3("WARNING: Temperature out of target range. Screw RPM set to 0 to prevent damage.");
static /*const*/ pearlrt::Character<51>         CONST_CHARACTER_763db3e5_076f_4bae_8827_57b173ea46d8("WARNING: Overtemperature! Turn off heating element.");
static /*const*/ pearlrt::Character<10>         CONST_CHARACTER_edfadc6e_6d80_4329_af6b_c58fd2699ef3("SPOOL FULL");
static /*const*/ pearlrt::Character<6>         CONST_CHARACTER_79184312_d781_4aad_92d0_708c3a5c4793(" SIM: ");
static /*const*/ pearlrt::Character<7>         CONST_CHARACTER_30796b60_ec38_47ac_86c9_779d7ba72621("Fields:");
static /*const*/ pearlrt::Character<32>         CONST_CHARACTER_af53aa16_1f0b_4dde_8e3c_1e4c93784216("|#1|#2|#3|#4|#5|#6|#7|#8|#9|#10|");
static /*const*/ pearlrt::Character<26>         CONST_CHARACTER_260882a5_9711_432c_bada_55aa02594180("# 1: simulation_is_running");
static /*const*/ pearlrt::Character<19>         CONST_CHARACTER_1fa66cfe_4e92_4d00_a288_f255c7ef2ba9("# 2: ScrewMotor RPM");
static /*const*/ pearlrt::Character<21>         CONST_CHARACTER_3e528e3d_2d15_49ab_9b28_35b8849baf84("# 3: SpoolerMotor RPM");
static /*const*/ pearlrt::Character<20>         CONST_CHARACTER_3ff1444d_47b8_4fd5_93b1_896b4e602a68("# 4: ScrewHeater PWM");
static /*const*/ pearlrt::Character<22>         CONST_CHARACTER_04699213_9055_48c0_ae0d_8dd52453db52("# 5: Filament Diameter");
static /*const*/ pearlrt::Character<25>         CONST_CHARACTER_d2690fee_60f1_40ff_a075_52e0a5cb3d97("# 6: Extruder Temperature");
static /*const*/ pearlrt::Character<26>         CONST_CHARACTER_9cbb090b_919f_40d1_b652_f6344c4e569c("# 7: Contact Switch Status");
static /*const*/ pearlrt::Character<29>         CONST_CHARACTER_335dc995_3656_45e2_b331_97bbea35e23b("# 8: Current Winding Diameter");
static /*const*/ pearlrt::Character<27>         CONST_CHARACTER_1f00ba2d_30db_4122_b325_10acdbf98de9("# 9: Avg. filament diameter");
static /*const*/ pearlrt::Character<22>         CONST_CHARACTER_5109b8eb_c88a_4950_95ae_0956c5f2116c("# 10: Nbr. of Windings");
static /*const*/ pearlrt::Character<27>         CONST_CHARACTER_c7f2e3b5_7b40_4005_9f14_bc91d1bc9ffe("# 11: Spooled Filament Mass");
static /*const*/ pearlrt::Character<76>         CONST_CHARACTER_32a39f3e_577b_472b_86ae_168add875ede("----------------------------------------------------------------------------");
static /*const*/ pearlrt::Character<65>         CONST_CHARACTER_97d7e32c_e4dd_45a0_b84a_931d842f634a("|  1  |  2  |  3  |  4  |  5  |  6  |  7  |  8  |  9  | 10 | 11 |");
static /*const*/ pearlrt::Character<1>         CONST_CHARACTER_2be3ed2d_21e1_4bcc_be1c_b220338a566b("|");
static /*const*/ pearlrt::Character<38>         CONST_CHARACTER_0ef7af7f_e552_4cbf_88f5_8138132c355b("Layer Stats: Current winding diameter:");
static /*const*/ pearlrt::Character<28>         CONST_CHARACTER_2dc637c9_4bea_4214_a797_2e7c0641a30a(", Average filament diameter:");
static /*const*/ pearlrt::Character<22>         CONST_CHARACTER_5e491a07_57f5_4a1d_8f61_38791ff13e6e(", Filament mass total:");
static /*const*/ pearlrt::Character<39>         CONST_CHARACTER_df64d409_cd3f_4bf3_b0fc_c377d227bc7a("Spool Stats: Average filament diameter:");
static /*const*/ pearlrt::Character<17>         CONST_CHARACTER_408fce92_9257_437f_bb0d_1d5eaec44450(", Total Windings:");
static /*const*/ pearlrt::BitString<1>         CONST_BITSTRING_1_0(0x0);
static /*const*/ pearlrt::BitString<4>         CONST_BITSTRING_4_0(0x0);
static /*const*/ pearlrt::BitString<1>         CONST_BITSTRING_1_1(0x1);
static /*const*/ pearlrt::Duration          CONST_DURATION_P_0_0_0_01(0,10000,1);
static /*const*/ pearlrt::Duration          CONST_DURATION_P_0_0_1_0(1,0,1);
static /*const*/ pearlrt::Duration          CONST_DURATION_P_0_0_0_001(0,1000,1);

/////////////////////////////////////////////////////////////////////////////
// TASK SPECIFIERS
/////////////////////////////////////////////////////////////////////////////
extern pearlrt::Task task_simulation;


extern pearlrt::Task task_log_status;

}



/////////////////////////////////////////////////////////////////////////////
// STRUCTURE FORWARD DECLARATIONS
/////////////////////////////////////////////////////////////////////////////
struct S12C1A31_1_1_10;

/////////////////////////////////////////////////////////////////////////////
// STRUCTURE DECLARATIONS
/////////////////////////////////////////////////////////////////////////////
struct S12C1A31_1_1_10 {
   pearlrt::BitString<1> m0;
   pearlrt::Fixed<31> m1[10] ;
};


namespace ns_SimFilExtruder {


/////////////////////////////////////////////////////////////////////////////
// PROBLEM PART
/////////////////////////////////////////////////////////////////////////////

/////////////////////////////////////////////////////////////////////////////
// ARRAY DESCRIPTORS
/////////////////////////////////////////////////////////////////////////////
static pearlrt::ArrayDescriptor< 1 > ad_1_1_10 = {1,LIMITS({{1,10,1}})};

/////////////////////////////////////////////////////////////////////////////
// INTERRUPT SPECIFICATIONS
/////////////////////////////////////////////////////////////////////////////
extern pearlrt::Interrupt *_contactswitchirpt;




/////////////////////////////////////////////////////////////////////////////
// SIGNAL SPECIFICATIONS
/////////////////////////////////////////////////////////////////////////////
extern pearlrt::Signal *_openFileErr;



/////////////////////////////////////////////////////////////////////////////
// PROCEDURE SPECIFICATIONS
/////////////////////////////////////////////////////////////////////////////
static pearlrt::Float<23> _calc_act_winding_speed(pearlrt::Task *me);

 /* public */ void _stop_simulation(pearlrt::Task *me);

/* public */ void _send_msg(pearlrt::Task *me, pearlrt::BitString<64> );

/* public */ pearlrt::BitString<64> _read_msg(pearlrt::Task *me);

static void _screwheater_update(pearlrt::Task *me, pearlrt::Float<23> );

 static void _temperature_sensor_update(pearlrt::Task *me, pearlrt::Float<23> );

 static void _process_surv(pearlrt::Task *me, pearlrt::Float<23> );

 static void _diameter_sensor_update(pearlrt::Task *me, pearlrt::Float<23> );

 static void _log(pearlrt::Task *me, pearlrt::RefCharacter );

 /* public */ void _set_screwmotor_rotational_speed(pearlrt::Task *me, pearlrt::Fixed<15> );

static void _contact_switch_update(pearlrt::Task *me);

 /* public */ pearlrt::Float<23> _get_temperature_sensor(pearlrt::Task *me);

static pearlrt::Float<23> _calc_diameter(pearlrt::Task *me);

 static void _screwmotor_update(pearlrt::Task *me, pearlrt::Float<23> );

 static S12C1A31_1_1_10 _read_config(pearlrt::Task *me);

 static pearlrt::Float<23> _calc_wall_velocity_z(pearlrt::Task *me);

 static pearlrt::Float<23> _calc_volumetric_flow(pearlrt::Task *me);

 /* public */ pearlrt::Fixed<31> _get_spoolermotor_rotational_speed(pearlrt::Task *me);

static void _closeLogfile(pearlrt::Task *me);

 /* public */ void _set_screwheater_pwm(pearlrt::Task *me, pearlrt::Fixed<15> );

/* public */ pearlrt::Fixed<31> _get_screwheater_pwm(pearlrt::Task *me);

/* public */ pearlrt::Float<23> _get_diameter_sensor(pearlrt::Task *me);

/* public */ pearlrt::BitString<1> _get_contact_switch_state(pearlrt::Task *me);

static void _log_spool_stats(pearlrt::Task *me);

 /* public */ pearlrt::Fixed<31> _get_screwmotor_rotational_speed(pearlrt::Task *me);

/* public */ void _start_simulation(pearlrt::Task *me);

static pearlrt::Float<23> _calc_mass_flow(pearlrt::Task *me);

 /* public */ void _set_spoolermotor_rotational_speed(pearlrt::Task *me, pearlrt::Fixed<15> );

static void _openLogfile(pearlrt::Task *me, pearlrt::RefCharacter , pearlrt::BitString<1> );

 static void _log_layer_stats(pearlrt::Task *me);

 static void _spool_reset(pearlrt::Task *me);

 static pearlrt::BitString<1> _check_overtemp(pearlrt::Task *me);

 static void _spoolermotor_update(pearlrt::Task *me, pearlrt::Float<23> );

 static pearlrt::BitString<1> _check_temperature_limits(pearlrt::Task *me);

 


/////////////////////////////////////////////////////////////////////////////
// DATION SPECIFICATIONS
/////////////////////////////////////////////////////////////////////////////
extern pearlrt::SystemDationNB * _pipeSim; 


extern pearlrt::SystemDationNB * _pipeMsgIn; 


extern pearlrt::SystemDationNB * _pipeMsgOut; 


extern pearlrt::SystemDationNB * _homeFolder; 



/////////////////////////////////////////////////////////////////////////////
// DATION DECLARATIONS
/////////////////////////////////////////////////////////////////////////////
static pearlrt::DationPG *_writeLogPipe;
 static pearlrt::DationRW *_readMsgPipe;
 static pearlrt::DationRW *_writeMsgPipe;
 static pearlrt::DationPG *_cfgFile;
 static pearlrt::DationPG *_logFile;
 

/////////////////////////////////////////////////////////////////////////////
// SYSTEM DATION INITIALIZER
/////////////////////////////////////////////////////////////////////////////
static void setupDationsAndPointers() {
   static pearlrt::DationDim2 h_dim_writeLogPipe(100); 
   static pearlrt::DationPG d_writeLogPipe(_pipeSim, pearlrt::Dation::OUT  | pearlrt::Dation::FORWARD | pearlrt::Dation::NOCYCL | pearlrt::Dation::STREAM, &h_dim_writeLogPipe);
   _writeLogPipe = &d_writeLogPipe;


   static pearlrt::DationDim2 h_dim_readMsgPipe(100); 
   static pearlrt::DationRW d_readMsgPipe(_pipeMsgIn, pearlrt::Dation::IN  | pearlrt::Dation::FORWARD | pearlrt::Dation::NOCYCL | pearlrt::Dation::STREAM, &h_dim_readMsgPipe,sizeof(pearlrt::BitString<64>));
   _readMsgPipe = &d_readMsgPipe;


   static pearlrt::DationDim2 h_dim_writeMsgPipe(100); 
   static pearlrt::DationRW d_writeMsgPipe(_pipeMsgOut, pearlrt::Dation::OUT  | pearlrt::Dation::FORWARD | pearlrt::Dation::NOCYCL | pearlrt::Dation::STREAM, &h_dim_writeMsgPipe,sizeof(pearlrt::BitString<64>));
   _writeMsgPipe = &d_writeMsgPipe;


   static pearlrt::DationDim2 h_dim_cfgFile(70); 
   static pearlrt::DationPG d_cfgFile(_homeFolder, pearlrt::Dation::IN  | pearlrt::Dation::FORWARD | pearlrt::Dation::NOCYCL | pearlrt::Dation::STREAM, &h_dim_cfgFile);
   _cfgFile = &d_cfgFile;


   static pearlrt::DationDim2 h_dim_logFile(100); 
   static pearlrt::DationPG d_logFile(_homeFolder, pearlrt::Dation::OUT  | pearlrt::Dation::FORWARD | pearlrt::Dation::NOCYCL | pearlrt::Dation::STREAM, &h_dim_logFile);
   _logFile = &d_logFile;




} 

static pearlrt::Control::Initializer init={setupDationsAndPointers,NULL};
static int dummy = pearlrt::Control::addInitializer(&init);

/////////////////////////////////////////////////////////////////////////////
// VARIABLE DECLARATIONS
/////////////////////////////////////////////////////////////////////////////
static pearlrt::Character<20>  _configFileName (CONST_CHARACTER_ae120d22_c47b_4ea6_bc61_0078eea9cba3); 
static pearlrt::Character<7>  _fileName (CONST_CHARACTER_6d97f730_8cd9_4787_a1f9_0ee046e575d1); 
static pearlrt::Semaphore _sema ( 1,"_sema");
 
static pearlrt::BitString<1>  _simulation_is_running (CONST_BITSTRING_1_0); 
static pearlrt::Duration  _sim_interval (CONST_DURATION_P_0_0_0_01); 
static pearlrt::Float<23>  _sim_speed_factor (CONST_FLOAT_P_10_0_23); 
static pearlrt::Duration  _log_interval (CONST_DURATION_P_0_0_1_0); 
static const pearlrt::Float<23>  _pi (CONST_FLOAT_P_3_1415926_23); 
static pearlrt::Fixed<15>  _min_processing_temp (CONST_FIXED_P_170_8); 
static pearlrt::Fixed<15>  _max_processing_temp (CONST_FIXED_P_210_8); 
static pearlrt::Float<23>  _density (CONST_FLOAT_P_0_00124_23); 
static pearlrt::Float<23>  _spec_heat_pla (CONST_FLOAT_P_1800_0_23); 
static pearlrt::Float<23>  _spec_heat_steel (CONST_FLOAT_P_480_0_23); 
static pearlrt::Float<23>  _h_air_steel (CONST_FLOAT_P_25_0_23); 
static pearlrt::Float<23>  _screw_mass (CONST_FLOAT_P_7_0_23); 
static pearlrt::Float<23>  _theta_b (CONST_FIXED_P_21_5); 
static pearlrt::Float<23>  _D_barrel (CONST_FIXED_P_25_5); 
static pearlrt::Float<23>  _A_cylinder (CONST_FIXED_P_1_1); 
static pearlrt::Float<23>  _channel_width (CONST_FIXED_P_15_4); 
static pearlrt::Float<23>  _channel_depth (CONST_FIXED_P_3_2); 
static pearlrt::Float<23>  _F_d (CONST_FLOAT_P_0_95_23); 
static pearlrt::Float<23>  _bulk_density (CONST_FIXED_P_100_7); 
static pearlrt::Fixed<15>  _screwmotor_max_rpm (CONST_FIXED_P_50_6); 
static pearlrt::Fixed<15>  _screwmotor_idle_rpm (CONST_FIXED_P_1_1); 
static pearlrt::Float<23>  _screwmotor_spin_up_speed (CONST_FLOAT_P_25_0_23); 
static pearlrt::Float<23>  _screwmotor_spin_down_speed (CONST_FLOAT_P_50_0_23); 
static pearlrt::Fixed<15>  _screwmotor_requested_rpm (CONST_FIXED_P_0_1); 
static pearlrt::Float<23>  _screwmotor_current_rpm (CONST_FLOAT_P_0_0_23); 
static const pearlrt::Fixed<15>  _screwheater_max_pwm (CONST_FIXED_P_100_7); 
static pearlrt::Float<23>  _screwheater_power_max (CONST_FLOAT_P_800_0_23); 
static pearlrt::Float<23>  _screwheater_current_pwm (CONST_FLOAT_P_0_0_23); 
static pearlrt::Float<23>  _screwheater_requested_pwm (CONST_FLOAT_P_0_0_23); 
static pearlrt::BitString<1>  _temp_ok (CONST_BITSTRING_1_0); 
static pearlrt::BitString<1>  _overtemp (CONST_BITSTRING_1_0); 
static pearlrt::Float<23>  _temperature_sensor (CONST_FLOAT_P_23_0_23); 
static pearlrt::Fixed<15>  _ambient_temp (CONST_FIXED_P_23_5); 
static pearlrt::Fixed<15>  _overtemp_limit (CONST_FIXED_P_300_9); 
static pearlrt::Fixed<15>  _spoolermotor_max_rpm (CONST_FIXED_P_300_9); 
static pearlrt::Fixed<15>  _spoolermotor_idle_rpm (CONST_FIXED_P_1_1); 
static pearlrt::Fixed<15>  _spoolermotor_spin_up_speed (CONST_FIXED_P_100_7); 
static pearlrt::Fixed<15>  _spoolermotor_spin_down_speed (CONST_FIXED_P_200_8); 
static pearlrt::Fixed<15>  _spoolermotor_requested_rpm (CONST_FIXED_P_0_1); 
static pearlrt::Float<23>  _spoolermotor_current_rpm (CONST_FLOAT_P_0_0_23); 
static pearlrt::Fixed<15>  _spool_diameter (CONST_FIXED_P_52_6); 
static pearlrt::Float<23>  _winding_diameter (CONST_FLOAT_P_52_0_23); 
static pearlrt::Float<23>  _max_winding_diameter (CONST_FIXED_P_70_7); 
static pearlrt::Fixed<15>  _spool_width (CONST_FIXED_P_67_7); 
static pearlrt::Float<23>  _total_revs (CONST_FLOAT_P_0_0_23); 
static pearlrt::Float<23>  _total_revs_layer (CONST_FLOAT_P_0_0_23); 
static pearlrt::Float<23>  _wound_up_fil_mass (CONST_FLOAT_P_0_0_23); 
static pearlrt::BitString<1>  _winding (CONST_BITSTRING_1_0); 
static pearlrt::Fixed<31>  _prep_time (CONST_FIXED_P_0_1); 
static pearlrt::Float<23>  _diameter_sensor (CONST_FLOAT_P_0_0_23); 
static pearlrt::Float<23>  _avg_dia_layer (CONST_FLOAT_P_0_0_23); 
static pearlrt::Float<23>  _avg_dia (CONST_FLOAT_P_0_0_23); 
static pearlrt::Float<23>  _upper_diameter_limit (CONST_FLOAT_P_5_0_23); 
static pearlrt::BitString<1>  _contact_switch_enabled (CONST_BITSTRING_1_0); 
static pearlrt::Float<23>  _elapsed_time (CONST_FLOAT_P_0_0_23); 
static pearlrt::Float<23>  _act_winding_speed (CONST_FLOAT_P_0_0_23); 
static pearlrt::Float<23>  _volumetric_flow (CONST_FLOAT_P_0_0_23); 
static pearlrt::Float<23>  _mass_flow (CONST_FLOAT_P_0_0_23); 
static pearlrt::Float<23>  _wall_velocity_z ; 







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
    S12C1A31_1_1_10  _cfgErrors ;

    me->setLocation(201, "SimFilExtruder.prl");
    _cfgErrors = _read_config( me);

    me->setLocation(203, "SimFilExtruder.prl");
    _openLogfile( me, pearlrt::RefCharacter(_fileName), CONST_BITSTRING_1_0);

    me->setLocation(206, "SimFilExtruder.prl");
    	if ((_cfgErrors.m0 == CONST_BITSTRING_1_1).getBoolean()) {
    	    me->setLocation(207, "SimFilExtruder.prl");
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
    	            .dataType={pearlrt::IODataEntry::CHAR,66},
    	            .dataPtr={.outData=&CONST_CHARACTER_b9752ae0_4fed_4285_bef9_df502832716d},
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




    	}


    me->setLocation(209, "SimFilExtruder.prl");
    {   // REPEAT 
        pearlrt::Fixed<31> a_value(CONST_FIXED_P_1_1);

        pearlrt::Fixed<31> e_value;
        e_value = CONST_FIXED_P_10_4;

        pearlrt::Fixed<31> s_value(CONST_FIXED_P_1_1);

        pearlrt::Fixed<31> _i;
        _i = a_value;

            while ((((s_value > CONST_FIXED_P_0_1).getBoolean()) &&
                    ((a_value <= e_value).getBoolean())) ||
                    (((s_value < CONST_FIXED_P_0_1).getBoolean()) &&
                    ((a_value >= e_value).getBoolean())))
            {

                _i = a_value;

                 
                               me->setLocation(211, "SimFilExtruder.prl");
                               	if (((*(_cfgErrors.m1 + ad_1_1_10.offset(pearlrt::Fixed<31>(
                                   _i)))) > CONST_FIXED_P_0_1).getBoolean()) {
                               	    me->setLocation(212, "SimFilExtruder.prl");
                               	    // put statement 
                               	    {
                               	       static pearlrt::IOFormatEntry formatEntries[]  = {
                               	      {
                               	         .format=pearlrt::IOFormatEntry::A
                               	      },
                               	      {
                               	         .format=pearlrt::IOFormatEntry::F,
                               	         .fp1={.f31=CONST_FIXED_P_2_2},
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
                               	            .dataType={pearlrt::IODataEntry::CHAR,49},
                               	            .dataPtr={.outData=&CONST_CHARACTER_75383ac2_12bc_4c8b_b9ad_4c340efd8fd5},
                               	            .param1={.numberOfElements = 1},
                               	         },
                               	         {
                               	            .dataType={pearlrt::IODataEntry::FIXED,31},
                               	            .dataPtr={.outData=&_i},
                               	            .param1={.numberOfElements = 1},
                               	         },
                               	         {
                               	            .dataType={pearlrt::IODataEntry::CHAR,27},
                               	            .dataPtr={.outData=&CONST_CHARACTER_a4efaf43_a194_4d6a_a9a6_610c6cbcff20},
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




                               	}





                me->setLocation(210, filename);
    			try {
                   a_value = a_value + s_value;
                }
                catch(pearlrt::FixedRangeSignal &ex) {
                    break;
                }

            }
    } // REPEAT 


    me->setLocation(217, "SimFilExtruder.prl");
    {
           _writeMsgPipe->dationOpen(
           0
                  	                , (pearlrt::RefCharacter*) 0
           , (pearlrt::Fixed<31>*) 0
           );
    }


    me->setLocation(218, "SimFilExtruder.prl");
    {
           _writeLogPipe->dationOpen(
           0
                  	                , (pearlrt::RefCharacter*) 0
           , (pearlrt::Fixed<31>*) 0
           );
    }


    me->setLocation(219, "SimFilExtruder.prl");
    {
           _readMsgPipe->dationOpen(
           0
                  	                , (pearlrt::RefCharacter*) 0
           , (pearlrt::Fixed<31>*) 0
           );
    }


    me->setLocation(221, "SimFilExtruder.prl");
    _log( me, pearlrt::RefCharacter(CONST_CHARACTER_dd4c299a_7ac8_4670_bbcd_0edfc3d8695f));

    me->setLocation(222, "SimFilExtruder.prl");
    _simulation_is_running = CONST_BITSTRING_1_1;

    me->setLocation(223, "SimFilExtruder.prl");
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



    me->setLocation(224, "SimFilExtruder.prl");
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
    me->setLocation(229, "SimFilExtruder.prl");
    _log( me, pearlrt::RefCharacter(CONST_CHARACTER_41831e0e_0464_44f4_b544_b0858903a816));

    me->setLocation(230, "SimFilExtruder.prl");
    _simulation_is_running = CONST_BITSTRING_1_0;

    me->setLocation(231, "SimFilExtruder.prl");
      _writeLogPipe->dationClose(0, (pearlrt::Fixed<15>*) 0);


    me->setLocation(232, "SimFilExtruder.prl");
      _writeMsgPipe->dationClose(0, (pearlrt::Fixed<15>*) 0);


    me->setLocation(233, "SimFilExtruder.prl");
      _readMsgPipe->dationClose(0, (pearlrt::Fixed<15>*) 0);


    me->setLocation(234, "SimFilExtruder.prl");
    _closeLogfile( me);




    me->setLocation(locationLine,locationFile);
}
/* public */ pearlrt::Fixed<31>
_get_screwmotor_rotational_speed(pearlrt::Task *me)
{
    const char* locationFile = me->getLocationFile();
    int locationLine = me->getLocationLine(); 
    pearlrt::Fixed<31>  _rpm ;

    me->setLocation(275, "SimFilExtruder.prl");
    {
    pearlrt::Semaphore *tmp_0[] = {&_sema}; 
       	 	
       pearlrt::Semaphore::request( me, 1, tmp_0);
        


    }
    me->setLocation(276, "SimFilExtruder.prl");
    _rpm = (_screwmotor_current_rpm).entier();

    me->setLocation(277, "SimFilExtruder.prl");
    {
    pearlrt::Semaphore *tmp_0[] = {&_sema}; 
       	 	
      pearlrt::Semaphore::release( me, 1, tmp_0);
        


    }
    me->setLocation(278, "SimFilExtruder.prl");
    	   me->setLocation(locationLine,locationFile);
    	   return (_rpm);





    me->setLocation(locationLine,locationFile);
}
/* public */ void
_set_screwmotor_rotational_speed(pearlrt::Task *me, pearlrt::Fixed<15> _rpm)
{
    const char* locationFile = me->getLocationFile();
    int locationLine = me->getLocationLine(); 
    me->setLocation(284, "SimFilExtruder.prl");
    {
    pearlrt::Semaphore *tmp_0[] = {&_sema}; 
       	 	
       pearlrt::Semaphore::request( me, 1, tmp_0);
        


    }
    me->setLocation(285, "SimFilExtruder.prl");
    _screwmotor_requested_rpm = _rpm;

    me->setLocation(286, "SimFilExtruder.prl");
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
    me->setLocation(293, "SimFilExtruder.prl");
    	if ((_temp_ok == CONST_BITSTRING_1_0).bitAnd((_screwmotor_requested_rpm > CONST_FIXED_P_0_1)).getBoolean()) {
    	    me->setLocation(295, "SimFilExtruder.prl");
    	    _screwmotor_requested_rpm = CONST_FIXED_P_0_1;

    	    me->setLocation(296, "SimFilExtruder.prl");
    	    _log( me, pearlrt::RefCharacter(CONST_CHARACTER_c9956ab3_a8ab_4c13_bcb9_c0bc8e5e7ee3));



    	}


    me->setLocation(300, "SimFilExtruder.prl");
    	if ((_screwmotor_requested_rpm > _screwmotor_max_rpm).getBoolean()) {
    	    me->setLocation(302, "SimFilExtruder.prl");
    	    _screwmotor_requested_rpm = _screwmotor_max_rpm;



    	}


    me->setLocation(306, "SimFilExtruder.prl");
    	if ((_screwmotor_current_rpm < _screwmotor_requested_rpm).getBoolean()) {
    	    me->setLocation(308, "SimFilExtruder.prl");
    	    _screwmotor_current_rpm = _screwmotor_current_rpm+((_dt*_screwmotor_spin_up_speed))/((pearlrt::Float<23>)(
    	    CONST_FIXED_P_1000_10));

    	    me->setLocation(309, "SimFilExtruder.prl");
    	    	if ((_screwmotor_current_rpm > _screwmotor_requested_rpm).getBoolean()) {
    	    	    me->setLocation(311, "SimFilExtruder.prl");
    	    	    _screwmotor_current_rpm = _screwmotor_requested_rpm;



    	    	}




    	}


    me->setLocation(316, "SimFilExtruder.prl");
    	if ((_screwmotor_current_rpm > _screwmotor_requested_rpm).getBoolean()) {
    	    me->setLocation(318, "SimFilExtruder.prl");
    	    _screwmotor_current_rpm = _screwmotor_current_rpm-((_dt*_screwmotor_spin_down_speed))/((pearlrt::Float<23>)(
    	    CONST_FIXED_P_1000_10));

    	    me->setLocation(319, "SimFilExtruder.prl");
    	    	if ((_screwmotor_current_rpm < CONST_FIXED_P_0_1).getBoolean()) {
    	    	    me->setLocation(321, "SimFilExtruder.prl");
    	    	    _screwmotor_current_rpm = CONST_FIXED_P_0_1;



    	    	}


    	    me->setLocation(323, "SimFilExtruder.prl");
    	    	if ((_screwmotor_current_rpm < _screwmotor_requested_rpm).getBoolean()) {
    	    	    me->setLocation(325, "SimFilExtruder.prl");
    	    	    _screwmotor_current_rpm = _screwmotor_requested_rpm;



    	    	}




    	}





    me->setLocation(locationLine,locationFile);
}
 /* public */ pearlrt::Fixed<31>
_get_screwheater_pwm(pearlrt::Task *me)
{
    const char* locationFile = me->getLocationFile();
    int locationLine = me->getLocationLine(); 
    pearlrt::Fixed<31>  _pwm ;

    me->setLocation(337, "SimFilExtruder.prl");
    {
    pearlrt::Semaphore *tmp_0[] = {&_sema}; 
       	 	
       pearlrt::Semaphore::request( me, 1, tmp_0);
        


    }
    me->setLocation(338, "SimFilExtruder.prl");
    _pwm = (_screwheater_current_pwm).entier();

    me->setLocation(339, "SimFilExtruder.prl");
    {
    pearlrt::Semaphore *tmp_0[] = {&_sema}; 
       	 	
      pearlrt::Semaphore::release( me, 1, tmp_0);
        


    }
    me->setLocation(340, "SimFilExtruder.prl");
    	   me->setLocation(locationLine,locationFile);
    	   return (_pwm);





    me->setLocation(locationLine,locationFile);
}
/* public */ void
_set_screwheater_pwm(pearlrt::Task *me, pearlrt::Fixed<15> _pwm)
{
    const char* locationFile = me->getLocationFile();
    int locationLine = me->getLocationLine(); 
    me->setLocation(346, "SimFilExtruder.prl");
    {
    pearlrt::Semaphore *tmp_0[] = {&_sema}; 
       	 	
       pearlrt::Semaphore::request( me, 1, tmp_0);
        


    }
    me->setLocation(347, "SimFilExtruder.prl");
    _screwheater_requested_pwm = _pwm;

    me->setLocation(348, "SimFilExtruder.prl");
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
    me->setLocation(354, "SimFilExtruder.prl");
    	if ((_overtemp == CONST_BITSTRING_1_1).bitAnd((_screwheater_current_pwm > CONST_FIXED_P_0_1)).getBoolean()) {
    	    me->setLocation(356, "SimFilExtruder.prl");
    	    _log( me, pearlrt::RefCharacter(CONST_CHARACTER_763db3e5_076f_4bae_8827_57b173ea46d8));



    	}


    me->setLocation(360, "SimFilExtruder.prl");
    	if ((_screwheater_requested_pwm > CONST_FLOAT_P_0_0_23).getBoolean()) {
    	    me->setLocation(362, "SimFilExtruder.prl");
    	    	if ((_screwheater_requested_pwm > _screwheater_max_pwm).getBoolean()) {
    	    	    me->setLocation(364, "SimFilExtruder.prl");
    	    	    _screwheater_current_pwm = _screwheater_max_pwm;



    	    	} else {
    	    	    me->setLocation(366, "SimFilExtruder.prl");
    	    	    _screwheater_current_pwm = _screwheater_requested_pwm;



    	    	}




    	} else {
    	    me->setLocation(369, "SimFilExtruder.prl");
    	    _screwheater_current_pwm = CONST_FLOAT_P_0_0_23;



    	}





    me->setLocation(locationLine,locationFile);
}
 /* public */ pearlrt::Float<23>
_get_temperature_sensor(pearlrt::Task *me)
{
    const char* locationFile = me->getLocationFile();
    int locationLine = me->getLocationLine(); 
    pearlrt::Float<23>  _temperature ;

    me->setLocation(380, "SimFilExtruder.prl");
    {
    pearlrt::Semaphore *tmp_0[] = {&_sema}; 
       	 	
       pearlrt::Semaphore::request( me, 1, tmp_0);
        


    }
    me->setLocation(381, "SimFilExtruder.prl");
    _temperature = _temperature_sensor;

    me->setLocation(382, "SimFilExtruder.prl");
    {
    pearlrt::Semaphore *tmp_0[] = {&_sema}; 
       	 	
      pearlrt::Semaphore::release( me, 1, tmp_0);
        


    }
    me->setLocation(383, "SimFilExtruder.prl");
    	   me->setLocation(locationLine,locationFile);
    	   return (_temperature);





    me->setLocation(locationLine,locationFile);
}
static void
_temperature_sensor_update(pearlrt::Task *me, pearlrt::Float<23> _dt)
{
    const char* locationFile = me->getLocationFile();
    int locationLine = me->getLocationLine(); 
    pearlrt::Float<23>  _temp_change ;
    pearlrt::Float<23>  _cooling_pwr_conv ;
    pearlrt::Float<23>  _cooling_pwr_fil ;
    pearlrt::Float<23>  _heating_pwr_element ;

    me->setLocation(395, "SimFilExtruder.prl");
    _heating_pwr_element = _screwheater_current_pwm*_screwheater_power_max;

    me->setLocation(398, "SimFilExtruder.prl");
    _cooling_pwr_conv = (_ambient_temp-_temperature_sensor)*_h_air_steel*_A_cylinder;

    me->setLocation(401, "SimFilExtruder.prl");
    _cooling_pwr_fil = (((_ambient_temp-_temperature_sensor)*_mass_flow*_spec_heat_pla))/((pearlrt::Float<23>)(
    CONST_FIXED_P_1000_10));

    me->setLocation(404, "SimFilExtruder.prl");
    _temp_change = (((_heating_pwr_element+_cooling_pwr_conv+_cooling_pwr_fil)*_dt))/((
    (_screw_mass*_spec_heat_steel*CONST_FIXED_P_100_7*CONST_FIXED_P_1000_10)));

    me->setLocation(407, "SimFilExtruder.prl");
    _temperature_sensor = _temperature_sensor+_sim_speed_factor*_temp_change;




    me->setLocation(locationLine,locationFile);
}
 /* public */ pearlrt::Fixed<31>
_get_spoolermotor_rotational_speed(pearlrt::Task *me)
{
    const char* locationFile = me->getLocationFile();
    int locationLine = me->getLocationLine(); 
    pearlrt::Fixed<31>  _rpm ;

    me->setLocation(417, "SimFilExtruder.prl");
    {
    pearlrt::Semaphore *tmp_0[] = {&_sema}; 
       	 	
       pearlrt::Semaphore::request( me, 1, tmp_0);
        


    }
    me->setLocation(418, "SimFilExtruder.prl");
    _rpm = (_spoolermotor_current_rpm).entier();

    me->setLocation(419, "SimFilExtruder.prl");
    {
    pearlrt::Semaphore *tmp_0[] = {&_sema}; 
       	 	
      pearlrt::Semaphore::release( me, 1, tmp_0);
        


    }
    me->setLocation(420, "SimFilExtruder.prl");
    	   me->setLocation(locationLine,locationFile);
    	   return (_rpm);





    me->setLocation(locationLine,locationFile);
}
/* public */ void
_set_spoolermotor_rotational_speed(pearlrt::Task *me, pearlrt::Fixed<15> _rpm)
{
    const char* locationFile = me->getLocationFile();
    int locationLine = me->getLocationLine(); 
    me->setLocation(426, "SimFilExtruder.prl");
    {
    pearlrt::Semaphore *tmp_0[] = {&_sema}; 
       	 	
       pearlrt::Semaphore::request( me, 1, tmp_0);
        


    }
    me->setLocation(427, "SimFilExtruder.prl");
    _spoolermotor_requested_rpm = _rpm;

    me->setLocation(428, "SimFilExtruder.prl");
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
    me->setLocation(434, "SimFilExtruder.prl");
    	if ((_spoolermotor_requested_rpm > _spoolermotor_max_rpm).getBoolean()) {
    	    me->setLocation(436, "SimFilExtruder.prl");
    	    _spoolermotor_requested_rpm = _spoolermotor_max_rpm;



    	}


    me->setLocation(440, "SimFilExtruder.prl");
    	if ((_spoolermotor_current_rpm < _spoolermotor_requested_rpm).getBoolean()) {
    	    me->setLocation(442, "SimFilExtruder.prl");
    	    _spoolermotor_current_rpm = _spoolermotor_current_rpm+((_dt*_spoolermotor_spin_up_speed))/((pearlrt::Float<23>)(
    	    CONST_FIXED_P_1000_10));

    	    me->setLocation(443, "SimFilExtruder.prl");
    	    	if ((_spoolermotor_current_rpm > _spoolermotor_requested_rpm).getBoolean()) {
    	    	    me->setLocation(445, "SimFilExtruder.prl");
    	    	    _spoolermotor_current_rpm = _spoolermotor_requested_rpm;



    	    	}




    	}


    me->setLocation(450, "SimFilExtruder.prl");
    	if ((_spoolermotor_current_rpm > _spoolermotor_requested_rpm).getBoolean()) {
    	    me->setLocation(452, "SimFilExtruder.prl");
    	    _spoolermotor_current_rpm = _spoolermotor_current_rpm-((_dt*_spoolermotor_spin_down_speed))/((pearlrt::Float<23>)(
    	    CONST_FIXED_P_1000_10));

    	    me->setLocation(454, "SimFilExtruder.prl");
    	    	if ((_spoolermotor_current_rpm < CONST_FIXED_P_0_1).getBoolean()) {
    	    	    me->setLocation(456, "SimFilExtruder.prl");
    	    	    _spoolermotor_current_rpm = CONST_FIXED_P_0_1;



    	    	}


    	    me->setLocation(458, "SimFilExtruder.prl");
    	    	if ((_spoolermotor_current_rpm < _spoolermotor_requested_rpm).getBoolean()) {
    	    	    me->setLocation(460, "SimFilExtruder.prl");
    	    	    _spoolermotor_current_rpm = _spoolermotor_requested_rpm;



    	    	}




    	}





    me->setLocation(locationLine,locationFile);
}
 /* public */ pearlrt::Float<23>
_get_diameter_sensor(pearlrt::Task *me)
{
    const char* locationFile = me->getLocationFile();
    int locationLine = me->getLocationLine(); 
    pearlrt::Float<23>  _diameter ;

    me->setLocation(472, "SimFilExtruder.prl");
    {
    pearlrt::Semaphore *tmp_0[] = {&_sema}; 
       	 	
       pearlrt::Semaphore::request( me, 1, tmp_0);
        


    }
    me->setLocation(473, "SimFilExtruder.prl");
    _diameter = _diameter_sensor;

    me->setLocation(474, "SimFilExtruder.prl");
    {
    pearlrt::Semaphore *tmp_0[] = {&_sema}; 
       	 	
      pearlrt::Semaphore::release( me, 1, tmp_0);
        


    }
    me->setLocation(475, "SimFilExtruder.prl");
    	   me->setLocation(locationLine,locationFile);
    	   return (_diameter);





    me->setLocation(locationLine,locationFile);
}
static void
_diameter_sensor_update(pearlrt::Task *me, pearlrt::Float<23> _dt)
{
    const char* locationFile = me->getLocationFile();
    int locationLine = me->getLocationLine(); 
    pearlrt::Float<23>  _new_revs_to_add ;
    pearlrt::Float<23>  _old_revs_layer ;
    pearlrt::Float<23>  _old_revs ;

    me->setLocation(486, "SimFilExtruder.prl");
    _elapsed_time = _elapsed_time+(((_dt))/((CONST_FLOAT_P_1000_0_23)));

    me->setLocation(487, "SimFilExtruder.prl");
    	if ((_spoolermotor_current_rpm > CONST_FIXED_P_0_1).getBoolean()) {
    	    me->setLocation(489, "SimFilExtruder.prl");
    	    _bulk_density = CONST_FLOAT_P_0_95_23+CONST_FLOAT_P_0_03_23*((((CONST_FLOAT_P_0_2_23
    	    *_elapsed_time))).sin())+CONST_FLOAT_P_0_01_23*((((CONST_FLOAT_P_0_5_23
    	    *_elapsed_time+((_pi))/((pearlrt::Float<23>)(CONST_FIXED_P_3_2))))).sin());

    	    me->setLocation(490, "SimFilExtruder.prl");
    	    _diameter_sensor = _calc_diameter( me);

    	    me->setLocation(491, "SimFilExtruder.prl");
    	    _mass_flow = _calc_mass_flow( me);

    	    me->setLocation(494, "SimFilExtruder.prl");
    	    	if ((_winding == CONST_BITSTRING_1_1).getBoolean()) {
    	    	    me->setLocation(495, "SimFilExtruder.prl");
    	    	    _new_revs_to_add = ((_spoolermotor_current_rpm*_dt))/((pearlrt::Float<23>)(
    	    	    (CONST_FIXED_P_60000_16)));

    	    	    me->setLocation(496, "SimFilExtruder.prl");
    	    	    _old_revs_layer = _total_revs_layer;

    	    	    me->setLocation(497, "SimFilExtruder.prl");
    	    	    _old_revs = _total_revs;

    	    	    me->setLocation(500, "SimFilExtruder.prl");
    	    	    _total_revs_layer = _total_revs_layer+_new_revs_to_add;

    	    	    me->setLocation(501, "SimFilExtruder.prl");
    	    	    _total_revs = _total_revs+_new_revs_to_add;

    	    	    me->setLocation(504, "SimFilExtruder.prl");
    	    	    _avg_dia_layer = (((_avg_dia_layer*_old_revs_layer+_diameter_sensor
    	    	    *_new_revs_to_add)))/((_total_revs_layer));

    	    	    me->setLocation(505, "SimFilExtruder.prl");
    	    	    _avg_dia = (((_avg_dia*_old_revs+_diameter_sensor*_new_revs_to_add
    	    	    )))/((_total_revs));

    	    	    me->setLocation(509, "SimFilExtruder.prl");
    	    	    _wound_up_fil_mass = _wound_up_fil_mass+((_mass_flow*_dt))/((CONST_FLOAT_P_1000_0_23));

    	    	    me->setLocation(512, "SimFilExtruder.prl");
    	    	    	if ((_total_revs_layer*_avg_dia_layer > _spool_width).getBoolean()) {
    	    	    	    me->setLocation(514, "SimFilExtruder.prl");
    	    	    	    _winding_diameter = _winding_diameter+CONST_FIXED_P_2_2*_avg_dia_layer;

    	    	    	    me->setLocation(515, "SimFilExtruder.prl");
    	    	    	    _log_layer_stats( me);

    	    	    	    me->setLocation(516, "SimFilExtruder.prl");
    	    	    	    _avg_dia_layer = CONST_FIXED_P_0_1;

    	    	    	    me->setLocation(517, "SimFilExtruder.prl");
    	    	    	    _total_revs_layer = CONST_FIXED_P_0_1;



    	    	    	}




    	    	}




    	} else {
    	    me->setLocation(521, "SimFilExtruder.prl");
    	    _diameter_sensor = CONST_FLOAT_P_0_0_23;



    	}





    me->setLocation(locationLine,locationFile);
}
 static pearlrt::Float<23>
_calc_act_winding_speed(pearlrt::Task *me)
{
    const char* locationFile = me->getLocationFile();
    int locationLine = me->getLocationLine(); 
    me->setLocation(529, "SimFilExtruder.prl");
    _act_winding_speed = ((_pi*_winding_diameter*_spoolermotor_current_rpm))/((pearlrt::Float<23>)(
    CONST_FIXED_P_60_6));

    me->setLocation(531, "SimFilExtruder.prl");
    	   me->setLocation(locationLine,locationFile);
    	   return (_act_winding_speed);





    me->setLocation(locationLine,locationFile);
}
 static pearlrt::Float<23>
_calc_diameter(pearlrt::Task *me)
{
    const char* locationFile = me->getLocationFile();
    int locationLine = me->getLocationLine(); 
    pearlrt::Float<23>  _diameter (CONST_FLOAT_P_0_0_23);

    me->setLocation(538, "SimFilExtruder.prl");
    _diameter = CONST_FIXED_P_2_2*((((((_calc_volumetric_flow( me)))/(((_calc_act_winding_speed( me)
    *_pi)))))).sqrt());

    me->setLocation(539, "SimFilExtruder.prl");
    	if ((_diameter > _upper_diameter_limit).getBoolean()) {
    	    me->setLocation(540, "SimFilExtruder.prl");
    	    _diameter = _upper_diameter_limit;



    	}


    me->setLocation(542, "SimFilExtruder.prl");
    	   me->setLocation(locationLine,locationFile);
    	   return (_diameter);





    me->setLocation(locationLine,locationFile);
}
 static pearlrt::Float<23>
_calc_volumetric_flow(pearlrt::Task *me)
{
    const char* locationFile = me->getLocationFile();
    int locationLine = me->getLocationLine(); 
    me->setLocation(550, "SimFilExtruder.prl");
    _volumetric_flow = ((_bulk_density*_calc_wall_velocity_z( me)*_channel_width
    *_channel_depth*_F_d))/((pearlrt::Float<23>)(CONST_FIXED_P_2_2));

    me->setLocation(551, "SimFilExtruder.prl");
    	   me->setLocation(locationLine,locationFile);
    	   return (_volumetric_flow);





    me->setLocation(locationLine,locationFile);
}
 static pearlrt::Float<23>
_calc_mass_flow(pearlrt::Task *me)
{
    const char* locationFile = me->getLocationFile();
    int locationLine = me->getLocationLine(); 
    me->setLocation(558, "SimFilExtruder.prl");
    _mass_flow = _calc_volumetric_flow( me)*_density;

    me->setLocation(559, "SimFilExtruder.prl");
    	   me->setLocation(locationLine,locationFile);
    	   return (_mass_flow);





    me->setLocation(locationLine,locationFile);
}
 static pearlrt::Float<23>
_calc_wall_velocity_z(pearlrt::Task *me)
{
    const char* locationFile = me->getLocationFile();
    int locationLine = me->getLocationLine(); 
    me->setLocation(566, "SimFilExtruder.prl");
    _wall_velocity_z = ((_pi*_screwmotor_current_rpm*_D_barrel*((((((_theta_b*_pi))/((pearlrt::Float<23>)(
    CONST_FIXED_P_180_8))))).cos())))/((pearlrt::Float<23>)(CONST_FIXED_P_60_6));

    me->setLocation(567, "SimFilExtruder.prl");
    	   me->setLocation(locationLine,locationFile);
    	   return (_wall_velocity_z);





    me->setLocation(locationLine,locationFile);
}
 /* public */ pearlrt::BitString<1>
_get_contact_switch_state(pearlrt::Task *me)
{
    const char* locationFile = me->getLocationFile();
    int locationLine = me->getLocationLine(); 
    pearlrt::BitString<1>  _state ;

    me->setLocation(577, "SimFilExtruder.prl");
    {
    pearlrt::Semaphore *tmp_0[] = {&_sema}; 
       	 	
       pearlrt::Semaphore::request( me, 1, tmp_0);
        


    }
    me->setLocation(578, "SimFilExtruder.prl");
    _state = _contact_switch_enabled;

    me->setLocation(579, "SimFilExtruder.prl");
    {
    pearlrt::Semaphore *tmp_0[] = {&_sema}; 
       	 	
      pearlrt::Semaphore::release( me, 1, tmp_0);
        


    }
    me->setLocation(580, "SimFilExtruder.prl");
    	   me->setLocation(locationLine,locationFile);
    	   return (_state);





    me->setLocation(locationLine,locationFile);
}
static void
_contact_switch_update(pearlrt::Task *me)
{
    const char* locationFile = me->getLocationFile();
    int locationLine = me->getLocationLine(); 
    me->setLocation(585, "SimFilExtruder.prl");
    	if ((_winding_diameter >= _max_winding_diameter).getBoolean()) {
    	    me->setLocation(586, "SimFilExtruder.prl");
    	    	if ((_contact_switch_enabled == CONST_BITSTRING_1_0).getBoolean()) {
    	    	    me->setLocation(587, "SimFilExtruder.prl");
    	    	    _contact_switch_enabled = CONST_BITSTRING_1_1;

    	    	    me->setLocation(588, "SimFilExtruder.prl");
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
    	    	            .dataPtr={.outData=&CONST_CHARACTER_edfadc6e_6d80_4329_af6b_c58fd2699ef3},
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


    	    	    me->setLocation(589, "SimFilExtruder.prl");
    	    	    _contactswitchirpt->trigger();



    	    	}




    	} else {
    	    me->setLocation(592, "SimFilExtruder.prl");
    	    	if ((_contact_switch_enabled == CONST_BITSTRING_1_1).getBoolean()) {
    	    	    me->setLocation(593, "SimFilExtruder.prl");
    	    	    _contact_switch_enabled = CONST_BITSTRING_1_0;



    	    	}




    	}





    me->setLocation(locationLine,locationFile);
}
 static void
_process_surv(pearlrt::Task *me, pearlrt::Float<23> _dt)
{
    const char* locationFile = me->getLocationFile();
    int locationLine = me->getLocationLine(); 
    me->setLocation(603, "SimFilExtruder.prl");
    _temp_ok = _check_temperature_limits( me);

    me->setLocation(604, "SimFilExtruder.prl");
    _overtemp = _check_overtemp( me);

    me->setLocation(605, "SimFilExtruder.prl");
    	if ((_spoolermotor_current_rpm == CONST_FIXED_P_0_1).bitOr((_screwmotor_current_rpm == 
    CONST_FIXED_P_0_1)).getBoolean()) {
    	    me->setLocation(606, "SimFilExtruder.prl");
    	    	if ((_winding == CONST_BITSTRING_1_1).getBoolean()) {
    	    	    me->setLocation(607, "SimFilExtruder.prl");
    	    	    _spool_reset( me);



    	    	}


    	    me->setLocation(609, "SimFilExtruder.prl");
    	    _prep_time = CONST_FIXED_P_0_1;

    	    me->setLocation(610, "SimFilExtruder.prl");
    	    _winding = CONST_BITSTRING_1_0;



    	} else {
    	    me->setLocation(612, "SimFilExtruder.prl");
    	    	if ((_prep_time < CONST_FIXED_P_5000_13).getBoolean()) {
    	    	    me->setLocation(613, "SimFilExtruder.prl");
    	    	    _prep_time = _prep_time+(_dt).entier();



    	    	} else {
    	    	    me->setLocation(615, "SimFilExtruder.prl");
    	    	    _winding = CONST_BITSTRING_1_1;



    	    	}




    	}





    me->setLocation(locationLine,locationFile);
}
 static void
_spool_reset(pearlrt::Task *me)
{
    const char* locationFile = me->getLocationFile();
    int locationLine = me->getLocationLine(); 
    me->setLocation(621, "SimFilExtruder.prl");
    _log_spool_stats( me);

    me->setLocation(622, "SimFilExtruder.prl");
    _total_revs_layer = CONST_FIXED_P_0_1;

    me->setLocation(623, "SimFilExtruder.prl");
    _total_revs = CONST_FIXED_P_0_1;

    me->setLocation(624, "SimFilExtruder.prl");
    _avg_dia_layer = CONST_FIXED_P_0_1;

    me->setLocation(625, "SimFilExtruder.prl");
    _avg_dia = CONST_FIXED_P_0_1;

    me->setLocation(626, "SimFilExtruder.prl");
    _wound_up_fil_mass = CONST_FIXED_P_0_1;

    me->setLocation(627, "SimFilExtruder.prl");
    _prep_time = CONST_FIXED_P_0_1;

    me->setLocation(628, "SimFilExtruder.prl");
    _winding_diameter = _spool_diameter;




    me->setLocation(locationLine,locationFile);
}
 static pearlrt::BitString<1>
_check_temperature_limits(pearlrt::Task *me)
{
    const char* locationFile = me->getLocationFile();
    int locationLine = me->getLocationLine(); 
    me->setLocation(634, "SimFilExtruder.prl");
    	if ((_min_processing_temp < _temperature_sensor).bitAnd((_temperature_sensor < 
    _max_processing_temp)).getBoolean()) {
    	    me->setLocation(636, "SimFilExtruder.prl");
    	    	   me->setLocation(locationLine,locationFile);
    	    	   return (CONST_BITSTRING_1_1);




    	}


    me->setLocation(638, "SimFilExtruder.prl");
    	   me->setLocation(locationLine,locationFile);
    	   return (CONST_BITSTRING_1_0);





    me->setLocation(locationLine,locationFile);
}
 static pearlrt::BitString<1>
_check_overtemp(pearlrt::Task *me)
{
    const char* locationFile = me->getLocationFile();
    int locationLine = me->getLocationLine(); 
    me->setLocation(644, "SimFilExtruder.prl");
    	if ((_temperature_sensor > _overtemp_limit).getBoolean()) {
    	    me->setLocation(645, "SimFilExtruder.prl");
    	    	   me->setLocation(locationLine,locationFile);
    	    	   return (CONST_BITSTRING_1_1);




    	}


    me->setLocation(647, "SimFilExtruder.prl");
    	   me->setLocation(locationLine,locationFile);
    	   return (CONST_BITSTRING_1_0);





    me->setLocation(locationLine,locationFile);
}
 /* public */ pearlrt::BitString<64>
_read_msg(pearlrt::Task *me)
{
    const char* locationFile = me->getLocationFile();
    int locationLine = me->getLocationLine(); 
    pearlrt::BitString<64>  _msg (CONST_BITSTRING_4_0);

    me->setLocation(658, "SimFilExtruder.prl");
    {
    pearlrt::Semaphore *tmp_0[] = {&_sema}; 
       	 	
       pearlrt::Semaphore::request( me, 1, tmp_0);
        


    }
    me->setLocation(659, "SimFilExtruder.prl");
    // read statement 
    {


       pearlrt::IODataEntry dataEntries[]  = {
         {
            .dataType={pearlrt::IODataEntry::BIT,64},
            .dataPtr={.outData=&_msg},
            .param1={.numberOfElements = 1},
         }
       };

       pearlrt::IODataList dataList = {
         .nbrOfEntries=sizeof(dataEntries)/sizeof(dataEntries[0]),
         .entry=dataEntries};
    pearlrt::IOFormatList formatList = {
    .nbrOfEntries=0,
         .entry=NULL};

      _readMsgPipe->read(me, &dataList , &formatList);
    }


    me->setLocation(660, "SimFilExtruder.prl");
    {
    pearlrt::Semaphore *tmp_0[] = {&_sema}; 
       	 	
      pearlrt::Semaphore::release( me, 1, tmp_0);
        


    }
    me->setLocation(661, "SimFilExtruder.prl");
    	   me->setLocation(locationLine,locationFile);
    	   return (_msg);





    me->setLocation(locationLine,locationFile);
}
/* public */ void
_send_msg(pearlrt::Task *me, pearlrt::BitString<64> _msg)
{
    const char* locationFile = me->getLocationFile();
    int locationLine = me->getLocationLine(); 
    me->setLocation(666, "SimFilExtruder.prl");
    {
    pearlrt::Semaphore *tmp_0[] = {&_sema}; 
       	 	
       pearlrt::Semaphore::request( me, 1, tmp_0);
        


    }
    me->setLocation(667, "SimFilExtruder.prl");
    // write statement 
    {


       pearlrt::IODataEntry dataEntries[]  = {
         {
            .dataType={pearlrt::IODataEntry::BIT,64},
            .dataPtr={.outData=&_msg},
            .param1={.numberOfElements = 1},
         }
       };

       pearlrt::IODataList dataList = {
         .nbrOfEntries=sizeof(dataEntries)/sizeof(dataEntries[0]),
         .entry=dataEntries};
    pearlrt::IOFormatList formatList = {
    .nbrOfEntries=0,
         .entry=NULL};

      _writeMsgPipe->write(me, &dataList , &formatList);
    }


    me->setLocation(668, "SimFilExtruder.prl");
    {
    pearlrt::Semaphore *tmp_0[] = {&_sema}; 
       	 	
      pearlrt::Semaphore::release( me, 1, tmp_0);
        


    }



    me->setLocation(locationLine,locationFile);
}
static S12C1A31_1_1_10
_read_config(pearlrt::Task *me)
{
    const char* locationFile = me->getLocationFile();
    int locationLine = me->getLocationLine(); 
    pearlrt::Character<70>  _data ;
    S12C1A31_1_1_10  _cfgErrors ;
    pearlrt::Fixed<31>  _cfgError ;

     
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
    	       me->setLocation(680, "SimFilExtruder.prl");
    	           ;

    	       me->setLocation(681, "SimFilExtruder.prl");
    	       _cfgErrors.m0 = CONST_BITSTRING_1_1;

    	       me->setLocation(682, "SimFilExtruder.prl");
    	       	   me->setLocation(locationLine,locationFile);
    	       	   return (_cfgErrors);





    	       }
    	       // BLOCK END 

    	       break;
       }
    me->setLocation(679, "SimFilExtruder.prl");
       scheduledSignalActions.setAction(1,_openFileErr);


    me->setLocation(685, "SimFilExtruder.prl");
    {
        {
           pearlrt::RefCharacter rcForFilename; 
    	
             rcForFilename.setWork(_configFileName);

           _cfgFile->dationOpen(
           pearlrt::Dation::IDF | pearlrt::Dation::OLD
           , &rcForFilename
           , (pearlrt::Fixed<31>*) 0
           );
       	}
    }


    me->setLocation(686, "SimFilExtruder.prl");
    // get statement 
    {
    pearlrt::IOFormatEntry formatEntries[]  = {
      {
         .format=pearlrt::IOFormatEntry::A
      },
      {
         .format=pearlrt::IOFormatEntry::RST,
         .fp1={.fxxPtr={.voidPtr=&_cfgError,.size=31}}
      },
      {
         .format=pearlrt::IOFormatEntry::SKIP,
         .fp1={.f31=CONST_FIXED_P_1_31}
      }
    };


       pearlrt::IODataEntry dataEntries[]  = {
         {
            .dataType={pearlrt::IODataEntry::CHAR,70},
            .dataPtr={.outData=&_data},
            .param1={.numberOfElements = 1},
         }
       };

       pearlrt::IODataList dataList = {
         .nbrOfEntries=sizeof(dataEntries)/sizeof(dataEntries[0]),
         .entry=dataEntries};
    pearlrt::IOFormatList formatList = {
         .nbrOfEntries=sizeof(formatEntries)/sizeof(formatEntries[0]),
         .entry=formatEntries};
      _cfgFile->get(me, &dataList , &formatList);
    }


    me->setLocation(689, "SimFilExtruder.prl");
    // get statement 
    {
    pearlrt::IOFormatEntry formatEntries[]  = {
      {
         .format=pearlrt::IOFormatEntry::A
      },
      {
         .format=pearlrt::IOFormatEntry::RST,
         .fp1={.fxxPtr={.voidPtr=&(*(_cfgErrors.m1 + ad_1_1_10.offset(pearlrt::Fixed<31>(
          CONST_FIXED_P_1_1)))),.size=31}}
      },
      {
         .format=pearlrt::IOFormatEntry::SKIP,
         .fp1={.f31=CONST_FIXED_P_1_31}
      }
    };


       pearlrt::IODataEntry dataEntries[]  = {
         {
            .dataType={pearlrt::IODataEntry::CHAR,70},
            .dataPtr={.outData=&_data},
            .param1={.numberOfElements = 1},
         }
       };

       pearlrt::IODataList dataList = {
         .nbrOfEntries=sizeof(dataEntries)/sizeof(dataEntries[0]),
         .entry=dataEntries};
    pearlrt::IOFormatList formatList = {
         .nbrOfEntries=sizeof(formatEntries)/sizeof(formatEntries[0]),
         .entry=formatEntries};
      _cfgFile->get(me, &dataList , &formatList);
    }


    me->setLocation(690, "SimFilExtruder.prl");
    //CONVERT FROM statement
    {
      pearlrt::RefCharacter rc(_data);
                                                 // true indicates output
      pearlrt::StringDationConvert strDation(&rc, false);
    pearlrt::IOFormatEntry formatEntries[]  = {
      {
         .format=pearlrt::IOFormatEntry::RST,
         .fp1={.fxxPtr={.voidPtr=&(*(_cfgErrors.m1 + ad_1_1_10.offset(pearlrt::Fixed<31>(
          CONST_FIXED_P_1_1)))),.size=31}}
      },
      {
         .format=pearlrt::IOFormatEntry::F,
         .fp1={.f31=CONST_FIXED_P_4_3},
         .fp2={.f31=0}
      }
    };


       pearlrt::IODataEntry dataEntries[]  = {
         {
            .dataType={pearlrt::IODataEntry::FLOAT,23},
            .dataPtr={.outData=&_sim_speed_factor},
            .param1={.numberOfElements = 1},
         }
       };

       pearlrt::IODataList dataList = {
         .nbrOfEntries=sizeof(dataEntries)/sizeof(dataEntries[0]),
         .entry=dataEntries};
    pearlrt::IOFormatList formatList = {
         .nbrOfEntries=sizeof(formatEntries)/sizeof(formatEntries[0]),
         .entry=formatEntries};

       strDation.convertFrom(me, &dataList , &formatList);
    }

    me->setLocation(691, "SimFilExtruder.prl");
    // get statement 
    {
    pearlrt::IOFormatEntry formatEntries[]  = {
      {
         .format=pearlrt::IOFormatEntry::A
      },
      {
         .format=pearlrt::IOFormatEntry::RST,
         .fp1={.fxxPtr={.voidPtr=&(*(_cfgErrors.m1 + ad_1_1_10.offset(pearlrt::Fixed<31>(
          CONST_FIXED_P_2_2)))),.size=31}}
      },
      {
         .format=pearlrt::IOFormatEntry::SKIP,
         .fp1={.f31=CONST_FIXED_P_1_31}
      }
    };


       pearlrt::IODataEntry dataEntries[]  = {
         {
            .dataType={pearlrt::IODataEntry::CHAR,70},
            .dataPtr={.outData=&_data},
            .param1={.numberOfElements = 1},
         }
       };

       pearlrt::IODataList dataList = {
         .nbrOfEntries=sizeof(dataEntries)/sizeof(dataEntries[0]),
         .entry=dataEntries};
    pearlrt::IOFormatList formatList = {
         .nbrOfEntries=sizeof(formatEntries)/sizeof(formatEntries[0]),
         .entry=formatEntries};
      _cfgFile->get(me, &dataList , &formatList);
    }


    me->setLocation(692, "SimFilExtruder.prl");
    //CONVERT FROM statement
    {
      pearlrt::RefCharacter rc(_data);
                                                 // true indicates output
      pearlrt::StringDationConvert strDation(&rc, false);
    pearlrt::IOFormatEntry formatEntries[]  = {
      {
         .format=pearlrt::IOFormatEntry::RST,
         .fp1={.fxxPtr={.voidPtr=&(*(_cfgErrors.m1 + ad_1_1_10.offset(pearlrt::Fixed<31>(
          CONST_FIXED_P_2_2)))),.size=31}}
      },
      {
         .format=pearlrt::IOFormatEntry::D,
         .fp1={.f31=CONST_FIXED_P_9_4},
         .fp2={.f31=CONST_FIXED_P_3_2}
       
      }
    };


       pearlrt::IODataEntry dataEntries[]  = {
         {
            .dataType={pearlrt::IODataEntry::DURATION,},
            .dataPtr={.outData=&_sim_interval},
            .param1={.numberOfElements = 1},
         }
       };

       pearlrt::IODataList dataList = {
         .nbrOfEntries=sizeof(dataEntries)/sizeof(dataEntries[0]),
         .entry=dataEntries};
    pearlrt::IOFormatList formatList = {
         .nbrOfEntries=sizeof(formatEntries)/sizeof(formatEntries[0]),
         .entry=formatEntries};

       strDation.convertFrom(me, &dataList , &formatList);
    }

    me->setLocation(693, "SimFilExtruder.prl");
    // get statement 
    {
    pearlrt::IOFormatEntry formatEntries[]  = {
      {
         .format=pearlrt::IOFormatEntry::A
      },
      {
         .format=pearlrt::IOFormatEntry::RST,
         .fp1={.fxxPtr={.voidPtr=&(*(_cfgErrors.m1 + ad_1_1_10.offset(pearlrt::Fixed<31>(
          CONST_FIXED_P_3_2)))),.size=31}}
      },
      {
         .format=pearlrt::IOFormatEntry::SKIP,
         .fp1={.f31=CONST_FIXED_P_1_31}
      }
    };


       pearlrt::IODataEntry dataEntries[]  = {
         {
            .dataType={pearlrt::IODataEntry::CHAR,70},
            .dataPtr={.outData=&_data},
            .param1={.numberOfElements = 1},
         }
       };

       pearlrt::IODataList dataList = {
         .nbrOfEntries=sizeof(dataEntries)/sizeof(dataEntries[0]),
         .entry=dataEntries};
    pearlrt::IOFormatList formatList = {
         .nbrOfEntries=sizeof(formatEntries)/sizeof(formatEntries[0]),
         .entry=formatEntries};
      _cfgFile->get(me, &dataList , &formatList);
    }


    me->setLocation(694, "SimFilExtruder.prl");
    //CONVERT FROM statement
    {
      pearlrt::RefCharacter rc(_data);
                                                 // true indicates output
      pearlrt::StringDationConvert strDation(&rc, false);
    pearlrt::IOFormatEntry formatEntries[]  = {
      {
         .format=pearlrt::IOFormatEntry::RST,
         .fp1={.fxxPtr={.voidPtr=&(*(_cfgErrors.m1 + ad_1_1_10.offset(pearlrt::Fixed<31>(
          CONST_FIXED_P_3_2)))),.size=31}}
      },
      {
         .format=pearlrt::IOFormatEntry::D,
         .fp1={.f31=CONST_FIXED_P_9_4},
         .fp2={.f31=CONST_FIXED_P_3_2}
       
      }
    };


       pearlrt::IODataEntry dataEntries[]  = {
         {
            .dataType={pearlrt::IODataEntry::DURATION,},
            .dataPtr={.outData=&_log_interval},
            .param1={.numberOfElements = 1},
         }
       };

       pearlrt::IODataList dataList = {
         .nbrOfEntries=sizeof(dataEntries)/sizeof(dataEntries[0]),
         .entry=dataEntries};
    pearlrt::IOFormatList formatList = {
         .nbrOfEntries=sizeof(formatEntries)/sizeof(formatEntries[0]),
         .entry=formatEntries};

       strDation.convertFrom(me, &dataList , &formatList);
    }

    me->setLocation(695, "SimFilExtruder.prl");
    // get statement 
    {
    pearlrt::IOFormatEntry formatEntries[]  = {
      {
         .format=pearlrt::IOFormatEntry::A
      },
      {
         .format=pearlrt::IOFormatEntry::RST,
         .fp1={.fxxPtr={.voidPtr=&_cfgError,.size=31}}
      },
      {
         .format=pearlrt::IOFormatEntry::SKIP,
         .fp1={.f31=CONST_FIXED_P_1_31}
      }
    };


       pearlrt::IODataEntry dataEntries[]  = {
         {
            .dataType={pearlrt::IODataEntry::CHAR,70},
            .dataPtr={.outData=&_data},
            .param1={.numberOfElements = 1},
         }
       };

       pearlrt::IODataList dataList = {
         .nbrOfEntries=sizeof(dataEntries)/sizeof(dataEntries[0]),
         .entry=dataEntries};
    pearlrt::IOFormatList formatList = {
         .nbrOfEntries=sizeof(formatEntries)/sizeof(formatEntries[0]),
         .entry=formatEntries};
      _cfgFile->get(me, &dataList , &formatList);
    }


    me->setLocation(698, "SimFilExtruder.prl");
    // get statement 
    {
    pearlrt::IOFormatEntry formatEntries[]  = {
      {
         .format=pearlrt::IOFormatEntry::A
      },
      {
         .format=pearlrt::IOFormatEntry::RST,
         .fp1={.fxxPtr={.voidPtr=&(*(_cfgErrors.m1 + ad_1_1_10.offset(pearlrt::Fixed<31>(
          CONST_FIXED_P_4_3)))),.size=31}}
      },
      {
         .format=pearlrt::IOFormatEntry::SKIP,
         .fp1={.f31=CONST_FIXED_P_1_31}
      }
    };


       pearlrt::IODataEntry dataEntries[]  = {
         {
            .dataType={pearlrt::IODataEntry::CHAR,70},
            .dataPtr={.outData=&_data},
            .param1={.numberOfElements = 1},
         }
       };

       pearlrt::IODataList dataList = {
         .nbrOfEntries=sizeof(dataEntries)/sizeof(dataEntries[0]),
         .entry=dataEntries};
    pearlrt::IOFormatList formatList = {
         .nbrOfEntries=sizeof(formatEntries)/sizeof(formatEntries[0]),
         .entry=formatEntries};
      _cfgFile->get(me, &dataList , &formatList);
    }


    me->setLocation(699, "SimFilExtruder.prl");
    //CONVERT FROM statement
    {
      pearlrt::RefCharacter rc(_data);
                                                 // true indicates output
      pearlrt::StringDationConvert strDation(&rc, false);
    pearlrt::IOFormatEntry formatEntries[]  = {
      {
         .format=pearlrt::IOFormatEntry::RST,
         .fp1={.fxxPtr={.voidPtr=&(*(_cfgErrors.m1 + ad_1_1_10.offset(pearlrt::Fixed<31>(
          CONST_FIXED_P_4_3)))),.size=31}}
      },
      {
         .format=pearlrt::IOFormatEntry::F,
         .fp1={.f31=CONST_FIXED_P_7_3},
         .fp2={.f31=CONST_FIXED_P_2_2}
      }
    };


       pearlrt::IODataEntry dataEntries[]  = {
         {
            .dataType={pearlrt::IODataEntry::FLOAT,23},
            .dataPtr={.outData=&_max_winding_diameter},
            .param1={.numberOfElements = 1},
         }
       };

       pearlrt::IODataList dataList = {
         .nbrOfEntries=sizeof(dataEntries)/sizeof(dataEntries[0]),
         .entry=dataEntries};
    pearlrt::IOFormatList formatList = {
         .nbrOfEntries=sizeof(formatEntries)/sizeof(formatEntries[0]),
         .entry=formatEntries};

       strDation.convertFrom(me, &dataList , &formatList);
    }

    me->setLocation(700, "SimFilExtruder.prl");
    // get statement 
    {
    pearlrt::IOFormatEntry formatEntries[]  = {
      {
         .format=pearlrt::IOFormatEntry::A
      },
      {
         .format=pearlrt::IOFormatEntry::RST,
         .fp1={.fxxPtr={.voidPtr=&(*(_cfgErrors.m1 + ad_1_1_10.offset(pearlrt::Fixed<31>(
          CONST_FIXED_P_5_3)))),.size=31}}
      },
      {
         .format=pearlrt::IOFormatEntry::SKIP,
         .fp1={.f31=CONST_FIXED_P_1_31}
      }
    };


       pearlrt::IODataEntry dataEntries[]  = {
         {
            .dataType={pearlrt::IODataEntry::CHAR,70},
            .dataPtr={.outData=&_data},
            .param1={.numberOfElements = 1},
         }
       };

       pearlrt::IODataList dataList = {
         .nbrOfEntries=sizeof(dataEntries)/sizeof(dataEntries[0]),
         .entry=dataEntries};
    pearlrt::IOFormatList formatList = {
         .nbrOfEntries=sizeof(formatEntries)/sizeof(formatEntries[0]),
         .entry=formatEntries};
      _cfgFile->get(me, &dataList , &formatList);
    }


    me->setLocation(701, "SimFilExtruder.prl");
    //CONVERT FROM statement
    {
      pearlrt::RefCharacter rc(_data);
                                                 // true indicates output
      pearlrt::StringDationConvert strDation(&rc, false);
    pearlrt::IOFormatEntry formatEntries[]  = {
      {
         .format=pearlrt::IOFormatEntry::RST,
         .fp1={.fxxPtr={.voidPtr=&(*(_cfgErrors.m1 + ad_1_1_10.offset(pearlrt::Fixed<31>(
          CONST_FIXED_P_5_3)))),.size=31}}
      },
      {
         .format=pearlrt::IOFormatEntry::F,
         .fp1={.f31=CONST_FIXED_P_4_3},
         .fp2={.f31=0}
      }
    };


       pearlrt::IODataEntry dataEntries[]  = {
         {
            .dataType={pearlrt::IODataEntry::FIXED,15},
            .dataPtr={.outData=&_spool_width},
            .param1={.numberOfElements = 1},
         }
       };

       pearlrt::IODataList dataList = {
         .nbrOfEntries=sizeof(dataEntries)/sizeof(dataEntries[0]),
         .entry=dataEntries};
    pearlrt::IOFormatList formatList = {
         .nbrOfEntries=sizeof(formatEntries)/sizeof(formatEntries[0]),
         .entry=formatEntries};

       strDation.convertFrom(me, &dataList , &formatList);
    }

    me->setLocation(702, "SimFilExtruder.prl");
    // get statement 
    {
    pearlrt::IOFormatEntry formatEntries[]  = {
      {
         .format=pearlrt::IOFormatEntry::A
      },
      {
         .format=pearlrt::IOFormatEntry::RST,
         .fp1={.fxxPtr={.voidPtr=&(*(_cfgErrors.m1 + ad_1_1_10.offset(pearlrt::Fixed<31>(
          CONST_FIXED_P_6_3)))),.size=31}}
      },
      {
         .format=pearlrt::IOFormatEntry::SKIP,
         .fp1={.f31=CONST_FIXED_P_1_31}
      }
    };


       pearlrt::IODataEntry dataEntries[]  = {
         {
            .dataType={pearlrt::IODataEntry::CHAR,70},
            .dataPtr={.outData=&_data},
            .param1={.numberOfElements = 1},
         }
       };

       pearlrt::IODataList dataList = {
         .nbrOfEntries=sizeof(dataEntries)/sizeof(dataEntries[0]),
         .entry=dataEntries};
    pearlrt::IOFormatList formatList = {
         .nbrOfEntries=sizeof(formatEntries)/sizeof(formatEntries[0]),
         .entry=formatEntries};
      _cfgFile->get(me, &dataList , &formatList);
    }


    me->setLocation(703, "SimFilExtruder.prl");
    //CONVERT FROM statement
    {
      pearlrt::RefCharacter rc(_data);
                                                 // true indicates output
      pearlrt::StringDationConvert strDation(&rc, false);
    pearlrt::IOFormatEntry formatEntries[]  = {
      {
         .format=pearlrt::IOFormatEntry::RST,
         .fp1={.fxxPtr={.voidPtr=&(*(_cfgErrors.m1 + ad_1_1_10.offset(pearlrt::Fixed<31>(
          CONST_FIXED_P_6_3)))),.size=31}}
      },
      {
         .format=pearlrt::IOFormatEntry::F,
         .fp1={.f31=CONST_FIXED_P_4_3},
         .fp2={.f31=0}
      }
    };


       pearlrt::IODataEntry dataEntries[]  = {
         {
            .dataType={pearlrt::IODataEntry::FIXED,15},
            .dataPtr={.outData=&_spool_diameter},
            .param1={.numberOfElements = 1},
         }
       };

       pearlrt::IODataList dataList = {
         .nbrOfEntries=sizeof(dataEntries)/sizeof(dataEntries[0]),
         .entry=dataEntries};
    pearlrt::IOFormatList formatList = {
         .nbrOfEntries=sizeof(formatEntries)/sizeof(formatEntries[0]),
         .entry=formatEntries};

       strDation.convertFrom(me, &dataList , &formatList);
    }

    me->setLocation(704, "SimFilExtruder.prl");
    _winding_diameter = _spool_diameter;

    me->setLocation(705, "SimFilExtruder.prl");
    // get statement 
    {
    pearlrt::IOFormatEntry formatEntries[]  = {
      {
         .format=pearlrt::IOFormatEntry::A
      },
      {
         .format=pearlrt::IOFormatEntry::RST,
         .fp1={.fxxPtr={.voidPtr=&_cfgError,.size=31}}
      },
      {
         .format=pearlrt::IOFormatEntry::SKIP,
         .fp1={.f31=CONST_FIXED_P_1_31}
      }
    };


       pearlrt::IODataEntry dataEntries[]  = {
         {
            .dataType={pearlrt::IODataEntry::CHAR,70},
            .dataPtr={.outData=&_data},
            .param1={.numberOfElements = 1},
         }
       };

       pearlrt::IODataList dataList = {
         .nbrOfEntries=sizeof(dataEntries)/sizeof(dataEntries[0]),
         .entry=dataEntries};
    pearlrt::IOFormatList formatList = {
         .nbrOfEntries=sizeof(formatEntries)/sizeof(formatEntries[0]),
         .entry=formatEntries};
      _cfgFile->get(me, &dataList , &formatList);
    }


    me->setLocation(708, "SimFilExtruder.prl");
    // get statement 
    {
    pearlrt::IOFormatEntry formatEntries[]  = {
      {
         .format=pearlrt::IOFormatEntry::A
      },
      {
         .format=pearlrt::IOFormatEntry::RST,
         .fp1={.fxxPtr={.voidPtr=&(*(_cfgErrors.m1 + ad_1_1_10.offset(pearlrt::Fixed<31>(
          CONST_FIXED_P_7_3)))),.size=31}}
      },
      {
         .format=pearlrt::IOFormatEntry::SKIP,
         .fp1={.f31=CONST_FIXED_P_1_31}
      }
    };


       pearlrt::IODataEntry dataEntries[]  = {
         {
            .dataType={pearlrt::IODataEntry::CHAR,70},
            .dataPtr={.outData=&_data},
            .param1={.numberOfElements = 1},
         }
       };

       pearlrt::IODataList dataList = {
         .nbrOfEntries=sizeof(dataEntries)/sizeof(dataEntries[0]),
         .entry=dataEntries};
    pearlrt::IOFormatList formatList = {
         .nbrOfEntries=sizeof(formatEntries)/sizeof(formatEntries[0]),
         .entry=formatEntries};
      _cfgFile->get(me, &dataList , &formatList);
    }


    me->setLocation(709, "SimFilExtruder.prl");
    //CONVERT FROM statement
    {
      pearlrt::RefCharacter rc(_data);
                                                 // true indicates output
      pearlrt::StringDationConvert strDation(&rc, false);
    pearlrt::IOFormatEntry formatEntries[]  = {
      {
         .format=pearlrt::IOFormatEntry::RST,
         .fp1={.fxxPtr={.voidPtr=&(*(_cfgErrors.m1 + ad_1_1_10.offset(pearlrt::Fixed<31>(
          CONST_FIXED_P_7_3)))),.size=31}}
      },
      {
         .format=pearlrt::IOFormatEntry::F,
         .fp1={.f31=CONST_FIXED_P_4_3},
         .fp2={.f31=0}
      }
    };


       pearlrt::IODataEntry dataEntries[]  = {
         {
            .dataType={pearlrt::IODataEntry::FIXED,15},
            .dataPtr={.outData=&_ambient_temp},
            .param1={.numberOfElements = 1},
         }
       };

       pearlrt::IODataList dataList = {
         .nbrOfEntries=sizeof(dataEntries)/sizeof(dataEntries[0]),
         .entry=dataEntries};
    pearlrt::IOFormatList formatList = {
         .nbrOfEntries=sizeof(formatEntries)/sizeof(formatEntries[0]),
         .entry=formatEntries};

       strDation.convertFrom(me, &dataList , &formatList);
    }

    me->setLocation(710, "SimFilExtruder.prl");
    // get statement 
    {
    pearlrt::IOFormatEntry formatEntries[]  = {
      {
         .format=pearlrt::IOFormatEntry::A
      },
      {
         .format=pearlrt::IOFormatEntry::RST,
         .fp1={.fxxPtr={.voidPtr=&(*(_cfgErrors.m1 + ad_1_1_10.offset(pearlrt::Fixed<31>(
          CONST_FIXED_P_8_4)))),.size=31}}
      },
      {
         .format=pearlrt::IOFormatEntry::SKIP,
         .fp1={.f31=CONST_FIXED_P_1_31}
      }
    };


       pearlrt::IODataEntry dataEntries[]  = {
         {
            .dataType={pearlrt::IODataEntry::CHAR,70},
            .dataPtr={.outData=&_data},
            .param1={.numberOfElements = 1},
         }
       };

       pearlrt::IODataList dataList = {
         .nbrOfEntries=sizeof(dataEntries)/sizeof(dataEntries[0]),
         .entry=dataEntries};
    pearlrt::IOFormatList formatList = {
         .nbrOfEntries=sizeof(formatEntries)/sizeof(formatEntries[0]),
         .entry=formatEntries};
      _cfgFile->get(me, &dataList , &formatList);
    }


    me->setLocation(711, "SimFilExtruder.prl");
    //CONVERT FROM statement
    {
      pearlrt::RefCharacter rc(_data);
                                                 // true indicates output
      pearlrt::StringDationConvert strDation(&rc, false);
    pearlrt::IOFormatEntry formatEntries[]  = {
      {
         .format=pearlrt::IOFormatEntry::RST,
         .fp1={.fxxPtr={.voidPtr=&(*(_cfgErrors.m1 + ad_1_1_10.offset(pearlrt::Fixed<31>(
          CONST_FIXED_P_8_4)))),.size=31}}
      },
      {
         .format=pearlrt::IOFormatEntry::F,
         .fp1={.f31=CONST_FIXED_P_7_3},
         .fp2={.f31=CONST_FIXED_P_2_2}
      }
    };


       pearlrt::IODataEntry dataEntries[]  = {
         {
            .dataType={pearlrt::IODataEntry::FLOAT,23},
            .dataPtr={.outData=&_temperature_sensor},
            .param1={.numberOfElements = 1},
         }
       };

       pearlrt::IODataList dataList = {
         .nbrOfEntries=sizeof(dataEntries)/sizeof(dataEntries[0]),
         .entry=dataEntries};
    pearlrt::IOFormatList formatList = {
         .nbrOfEntries=sizeof(formatEntries)/sizeof(formatEntries[0]),
         .entry=formatEntries};

       strDation.convertFrom(me, &dataList , &formatList);
    }

    me->setLocation(712, "SimFilExtruder.prl");
    // get statement 
    {
    pearlrt::IOFormatEntry formatEntries[]  = {
      {
         .format=pearlrt::IOFormatEntry::A
      },
      {
         .format=pearlrt::IOFormatEntry::RST,
         .fp1={.fxxPtr={.voidPtr=&(*(_cfgErrors.m1 + ad_1_1_10.offset(pearlrt::Fixed<31>(
          CONST_FIXED_P_9_4)))),.size=31}}
      },
      {
         .format=pearlrt::IOFormatEntry::SKIP,
         .fp1={.f31=CONST_FIXED_P_1_31}
      }
    };


       pearlrt::IODataEntry dataEntries[]  = {
         {
            .dataType={pearlrt::IODataEntry::CHAR,70},
            .dataPtr={.outData=&_data},
            .param1={.numberOfElements = 1},
         }
       };

       pearlrt::IODataList dataList = {
         .nbrOfEntries=sizeof(dataEntries)/sizeof(dataEntries[0]),
         .entry=dataEntries};
    pearlrt::IOFormatList formatList = {
         .nbrOfEntries=sizeof(formatEntries)/sizeof(formatEntries[0]),
         .entry=formatEntries};
      _cfgFile->get(me, &dataList , &formatList);
    }


    me->setLocation(713, "SimFilExtruder.prl");
    //CONVERT FROM statement
    {
      pearlrt::RefCharacter rc(_data);
                                                 // true indicates output
      pearlrt::StringDationConvert strDation(&rc, false);
    pearlrt::IOFormatEntry formatEntries[]  = {
      {
         .format=pearlrt::IOFormatEntry::RST,
         .fp1={.fxxPtr={.voidPtr=&(*(_cfgErrors.m1 + ad_1_1_10.offset(pearlrt::Fixed<31>(
          CONST_FIXED_P_9_4)))),.size=31}}
      },
      {
         .format=pearlrt::IOFormatEntry::F,
         .fp1={.f31=CONST_FIXED_P_4_3},
         .fp2={.f31=0}
      }
    };


       pearlrt::IODataEntry dataEntries[]  = {
         {
            .dataType={pearlrt::IODataEntry::FIXED,15},
            .dataPtr={.outData=&_min_processing_temp},
            .param1={.numberOfElements = 1},
         }
       };

       pearlrt::IODataList dataList = {
         .nbrOfEntries=sizeof(dataEntries)/sizeof(dataEntries[0]),
         .entry=dataEntries};
    pearlrt::IOFormatList formatList = {
         .nbrOfEntries=sizeof(formatEntries)/sizeof(formatEntries[0]),
         .entry=formatEntries};

       strDation.convertFrom(me, &dataList , &formatList);
    }

    me->setLocation(714, "SimFilExtruder.prl");
    // get statement 
    {
    pearlrt::IOFormatEntry formatEntries[]  = {
      {
         .format=pearlrt::IOFormatEntry::A
      },
      {
         .format=pearlrt::IOFormatEntry::RST,
         .fp1={.fxxPtr={.voidPtr=&(*(_cfgErrors.m1 + ad_1_1_10.offset(pearlrt::Fixed<31>(
          CONST_FIXED_P_10_4)))),.size=31}}
      }
    };


       pearlrt::IODataEntry dataEntries[]  = {
         {
            .dataType={pearlrt::IODataEntry::CHAR,70},
            .dataPtr={.outData=&_data},
            .param1={.numberOfElements = 1},
         }
       };

       pearlrt::IODataList dataList = {
         .nbrOfEntries=sizeof(dataEntries)/sizeof(dataEntries[0]),
         .entry=dataEntries};
    pearlrt::IOFormatList formatList = {
         .nbrOfEntries=sizeof(formatEntries)/sizeof(formatEntries[0]),
         .entry=formatEntries};
      _cfgFile->get(me, &dataList , &formatList);
    }


    me->setLocation(715, "SimFilExtruder.prl");
    //CONVERT FROM statement
    {
      pearlrt::RefCharacter rc(_data);
                                                 // true indicates output
      pearlrt::StringDationConvert strDation(&rc, false);
    pearlrt::IOFormatEntry formatEntries[]  = {
      {
         .format=pearlrt::IOFormatEntry::RST,
         .fp1={.fxxPtr={.voidPtr=&(*(_cfgErrors.m1 + ad_1_1_10.offset(pearlrt::Fixed<31>(
          CONST_FIXED_P_10_4)))),.size=31}}
      },
      {
         .format=pearlrt::IOFormatEntry::F,
         .fp1={.f31=CONST_FIXED_P_4_3},
         .fp2={.f31=0}
      }
    };


       pearlrt::IODataEntry dataEntries[]  = {
         {
            .dataType={pearlrt::IODataEntry::FIXED,15},
            .dataPtr={.outData=&_max_processing_temp},
            .param1={.numberOfElements = 1},
         }
       };

       pearlrt::IODataList dataList = {
         .nbrOfEntries=sizeof(dataEntries)/sizeof(dataEntries[0]),
         .entry=dataEntries};
    pearlrt::IOFormatList formatList = {
         .nbrOfEntries=sizeof(formatEntries)/sizeof(formatEntries[0]),
         .entry=formatEntries};

       strDation.convertFrom(me, &dataList , &formatList);
    }

    me->setLocation(717, "SimFilExtruder.prl");
      _cfgFile->dationClose(0, (pearlrt::Fixed<15>*) 0);


    me->setLocation(718, "SimFilExtruder.prl");
    	   me->setLocation(locationLine,locationFile);
    	   return (_cfgErrors);




    } catch(pearlrt::Signal & s) {
       indexOfSignalAction = scheduledSignalActions.getActionIndexAndSetRstAndDisableHandler(&s);
       if (indexOfSignalAction == 0) {
          // no handler found
          throw;
       } 
       goto tryAgain;
    } 
    // END OF SIGNAL FRAME

    me->setLocation(locationLine,locationFile);
}
 static void
_openLogfile(pearlrt::Task *me, pearlrt::RefCharacter _logFileName, pearlrt::BitString<1> _addToExistingFile)
{
    const char* locationFile = me->getLocationFile();
    int locationLine = me->getLocationLine(); 
    pearlrt::Character<30>  _fn ;

    me->setLocation(726, "SimFilExtruder.prl");
    _logFileName.store(_fn);

    me->setLocation(728, "SimFilExtruder.prl");
    	if (_addToExistingFile.getBoolean()) {
    	    me->setLocation(729, "SimFilExtruder.prl");
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


    	    me->setLocation(730, "SimFilExtruder.prl");
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
    	    me->setLocation(734, "SimFilExtruder.prl");
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


    	    me->setLocation(735, "SimFilExtruder.prl");
    	      _logFile->dationClose(pearlrt::Dation::CAN, (pearlrt::Fixed<15>*) 0);


    	    me->setLocation(739, "SimFilExtruder.prl");
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
    me->setLocation(745, "SimFilExtruder.prl");
      _logFile->dationClose(0, (pearlrt::Fixed<15>*) 0);





    me->setLocation(locationLine,locationFile);
}
 static void
_log(pearlrt::Task *me, pearlrt::RefCharacter _line)
{
    const char* locationFile = me->getLocationFile();
    int locationLine = me->getLocationLine(); 
    me->setLocation(750, "SimFilExtruder.prl");
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
            .dataPtr={.outData=&CONST_CHARACTER_79184312_d781_4aad_92d0_708c3a5c4793},
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


    me->setLocation(751, "SimFilExtruder.prl");
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
            .dataPtr={.outData=&CONST_CHARACTER_79184312_d781_4aad_92d0_708c3a5c4793},
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
      _writeLogPipe->put(me, &dataList , &formatList);
    }





    me->setLocation(locationLine,locationFile);
}
 static void
_log_layer_stats(pearlrt::Task *me)
{
    const char* locationFile = me->getLocationFile();
    int locationLine = me->getLocationLine(); 
    me->setLocation(808, "SimFilExtruder.prl");
    // put statement 
    {
       static pearlrt::IOFormatEntry formatEntries[]  = {
      {
         .format=pearlrt::IOFormatEntry::A
      },
      {
         .format=pearlrt::IOFormatEntry::F,
         .fp1={.f31=CONST_FIXED_P_8_4},
         .fp2={.f31=CONST_FIXED_P_3_2}
      },
      {
         .format=pearlrt::IOFormatEntry::A
      },
      {
         .format=pearlrt::IOFormatEntry::F,
         .fp1={.f31=CONST_FIXED_P_8_4},
         .fp2={.f31=CONST_FIXED_P_3_2}
      },
      {
         .format=pearlrt::IOFormatEntry::A
      },
      {
         .format=pearlrt::IOFormatEntry::F,
         .fp1={.f31=CONST_FIXED_P_8_4},
         .fp2={.f31=CONST_FIXED_P_3_2}
      },
      {
         .format=pearlrt::IOFormatEntry::SKIP,
         .fp1={.f31=CONST_FIXED_P_1_31}
      }
    };


       pearlrt::IODataEntry dataEntries[]  = {
         {
            .dataType={pearlrt::IODataEntry::CHAR,38},
            .dataPtr={.outData=&CONST_CHARACTER_0ef7af7f_e552_4cbf_88f5_8138132c355b},
            .param1={.numberOfElements = 1},
         },
         {
            .dataType={pearlrt::IODataEntry::FLOAT,23},
            .dataPtr={.outData=&_winding_diameter},
            .param1={.numberOfElements = 1},
         },
         {
            .dataType={pearlrt::IODataEntry::CHAR,28},
            .dataPtr={.outData=&CONST_CHARACTER_2dc637c9_4bea_4214_a797_2e7c0641a30a},
            .param1={.numberOfElements = 1},
         },
         {
            .dataType={pearlrt::IODataEntry::FLOAT,23},
            .dataPtr={.outData=&_avg_dia_layer},
            .param1={.numberOfElements = 1},
         },
         {
            .dataType={pearlrt::IODataEntry::CHAR,22},
            .dataPtr={.outData=&CONST_CHARACTER_5e491a07_57f5_4a1d_8f61_38791ff13e6e},
            .param1={.numberOfElements = 1},
         },
         {
            .dataType={pearlrt::IODataEntry::FLOAT,23},
            .dataPtr={.outData=&_wound_up_fil_mass},
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
 static void
_log_spool_stats(pearlrt::Task *me)
{
    const char* locationFile = me->getLocationFile();
    int locationLine = me->getLocationLine(); 
    me->setLocation(816, "SimFilExtruder.prl");
    // put statement 
    {
       static pearlrt::IOFormatEntry formatEntries[]  = {
      {
         .format=pearlrt::IOFormatEntry::A
      },
      {
         .format=pearlrt::IOFormatEntry::F,
         .fp1={.f31=CONST_FIXED_P_8_4},
         .fp2={.f31=CONST_FIXED_P_3_2}
      },
      {
         .format=pearlrt::IOFormatEntry::A
      },
      {
         .format=pearlrt::IOFormatEntry::F,
         .fp1={.f31=CONST_FIXED_P_8_4},
         .fp2={.f31=CONST_FIXED_P_3_2}
      },
      {
         .format=pearlrt::IOFormatEntry::A
      },
      {
         .format=pearlrt::IOFormatEntry::F,
         .fp1={.f31=CONST_FIXED_P_8_4},
         .fp2={.f31=CONST_FIXED_P_3_2}
      },
      {
         .format=pearlrt::IOFormatEntry::SKIP,
         .fp1={.f31=CONST_FIXED_P_1_31}
      }
    };


       pearlrt::IODataEntry dataEntries[]  = {
         {
            .dataType={pearlrt::IODataEntry::CHAR,39},
            .dataPtr={.outData=&CONST_CHARACTER_df64d409_cd3f_4bf3_b0fc_c377d227bc7a},
            .param1={.numberOfElements = 1},
         },
         {
            .dataType={pearlrt::IODataEntry::FLOAT,23},
            .dataPtr={.outData=&_avg_dia},
            .param1={.numberOfElements = 1},
         },
         {
            .dataType={pearlrt::IODataEntry::CHAR,22},
            .dataPtr={.outData=&CONST_CHARACTER_5e491a07_57f5_4a1d_8f61_38791ff13e6e},
            .param1={.numberOfElements = 1},
         },
         {
            .dataType={pearlrt::IODataEntry::FLOAT,23},
            .dataPtr={.outData=&_wound_up_fil_mass},
            .param1={.numberOfElements = 1},
         },
         {
            .dataType={pearlrt::IODataEntry::CHAR,17},
            .dataPtr={.outData=&CONST_CHARACTER_408fce92_9257_437f_bb0d_1d5eaec44450},
            .param1={.numberOfElements = 1},
         },
         {
            .dataType={pearlrt::IODataEntry::FLOAT,23},
            .dataPtr={.outData=&_total_revs},
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
 

/////////////////////////////////////////////////////////////////////////////
// TASK DECLARATIONS
/////////////////////////////////////////////////////////////////////////////
DCLTASK(_simulation, (pearlrt::Prio( (pearlrt::Fixed<15>)CONST_FIXED_P_10_4)), ((pearlrt::BitString<1>)0)) {
    pearlrt::Clock  _current_clock ;
    pearlrt::Clock  _last_update ;
    pearlrt::Float<23>  _dt ;

    me->setLocation(245, "SimFilExtruder.prl");
    _last_update = pearlrt::Clock::now();

    me->setLocation(248, "SimFilExtruder.prl");
    {   // REPEAT 
            while ( 1 )
            {

                if (!(_simulation_is_running.getBoolean()))
                    break;

                 
                               me->setLocation(250, "SimFilExtruder.prl");
                               {
                               pearlrt::Semaphore *tmp_0[] = {&_sema}; 
                                  	 	
                                  pearlrt::Semaphore::request( me, 1, tmp_0);
                                   


                               }
                               me->setLocation(251, "SimFilExtruder.prl");
                               _current_clock = pearlrt::Clock::now();

                               me->setLocation(252, "SimFilExtruder.prl");
                               _dt = (((_current_clock-_last_update)))/((CONST_DURATION_P_0_0_0_001));

                               me->setLocation(254, "SimFilExtruder.prl");
                               _screwmotor_update( me, _dt);

                               me->setLocation(255, "SimFilExtruder.prl");
                               _screwheater_update( me, _dt);

                               me->setLocation(256, "SimFilExtruder.prl");
                               _spoolermotor_update( me, _dt);

                               me->setLocation(257, "SimFilExtruder.prl");
                               _diameter_sensor_update( me, _dt);

                               me->setLocation(258, "SimFilExtruder.prl");
                               _temperature_sensor_update( me, _dt);

                               me->setLocation(259, "SimFilExtruder.prl");
                               _process_surv( me, _dt);

                               me->setLocation(260, "SimFilExtruder.prl");
                               _contact_switch_update( me);

                               me->setLocation(261, "SimFilExtruder.prl");
                               {
                               pearlrt::Semaphore *tmp_0[] = {&_sema}; 
                                  	 	
                                 pearlrt::Semaphore::release( me, 1, tmp_0);
                                   


                               }
                               me->setLocation(263, "SimFilExtruder.prl");
                               _last_update = _current_clock;

                               me->setLocation(265, "SimFilExtruder.prl");
                                   me->resume( pearlrt::Task::AFTER,
                                         /* at     */  pearlrt::Clock(),
                                         /* after  */  _sim_interval,
                               	  /* when   */  0
                                          );






            } 
    } // REPEAT 





}
DCLTASK(_log_status, (pearlrt::Prio( (pearlrt::Fixed<15>)CONST_FIXED_P_20_5)), ((pearlrt::BitString<1>)0)) {
    pearlrt::Character<100>  _line ;

    me->setLocation(757, "SimFilExtruder.prl");
    _log( me, pearlrt::RefCharacter(CONST_CHARACTER_30796b60_ec38_47ac_86c9_779d7ba72621));

    me->setLocation(758, "SimFilExtruder.prl");
    _log( me, pearlrt::RefCharacter(CONST_CHARACTER_af53aa16_1f0b_4dde_8e3c_1e4c93784216));

    me->setLocation(759, "SimFilExtruder.prl");
    _log( me, pearlrt::RefCharacter(CONST_CHARACTER_260882a5_9711_432c_bada_55aa02594180));

    me->setLocation(760, "SimFilExtruder.prl");
    _log( me, pearlrt::RefCharacter(CONST_CHARACTER_1fa66cfe_4e92_4d00_a288_f255c7ef2ba9));

    me->setLocation(761, "SimFilExtruder.prl");
    _log( me, pearlrt::RefCharacter(CONST_CHARACTER_3e528e3d_2d15_49ab_9b28_35b8849baf84));

    me->setLocation(762, "SimFilExtruder.prl");
    _log( me, pearlrt::RefCharacter(CONST_CHARACTER_3ff1444d_47b8_4fd5_93b1_896b4e602a68));

    me->setLocation(763, "SimFilExtruder.prl");
    _log( me, pearlrt::RefCharacter(CONST_CHARACTER_04699213_9055_48c0_ae0d_8dd52453db52));

    me->setLocation(764, "SimFilExtruder.prl");
    _log( me, pearlrt::RefCharacter(CONST_CHARACTER_d2690fee_60f1_40ff_a075_52e0a5cb3d97));

    me->setLocation(765, "SimFilExtruder.prl");
    _log( me, pearlrt::RefCharacter(CONST_CHARACTER_9cbb090b_919f_40d1_b652_f6344c4e569c));

    me->setLocation(766, "SimFilExtruder.prl");
    _log( me, pearlrt::RefCharacter(CONST_CHARACTER_335dc995_3656_45e2_b331_97bbea35e23b));

    me->setLocation(767, "SimFilExtruder.prl");
    _log( me, pearlrt::RefCharacter(CONST_CHARACTER_1f00ba2d_30db_4122_b325_10acdbf98de9));

    me->setLocation(768, "SimFilExtruder.prl");
    _log( me, pearlrt::RefCharacter(CONST_CHARACTER_5109b8eb_c88a_4950_95ae_0956c5f2116c));

    me->setLocation(769, "SimFilExtruder.prl");
    _log( me, pearlrt::RefCharacter(CONST_CHARACTER_c7f2e3b5_7b40_4005_9f14_bc91d1bc9ffe));

    me->setLocation(770, "SimFilExtruder.prl");
    _log( me, pearlrt::RefCharacter(CONST_CHARACTER_32a39f3e_577b_472b_86ae_168add875ede));

    me->setLocation(771, "SimFilExtruder.prl");
    _log( me, pearlrt::RefCharacter(CONST_CHARACTER_97d7e32c_e4dd_45a0_b84a_931d842f634a));

    me->setLocation(773, "SimFilExtruder.prl");
    {   // REPEAT 
            while ( 1 )
            {

                if (!(_simulation_is_running.getBoolean()))
                    break;

                 
                               me->setLocation(775, "SimFilExtruder.prl");
                               {
                               pearlrt::Semaphore *tmp_0[] = {&_sema}; 
                                  	 	
                                  pearlrt::Semaphore::request( me, 1, tmp_0);
                                   


                               }
                               me->setLocation(776, "SimFilExtruder.prl");
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
                                    .fp1={.f31=CONST_FIXED_P_7_3},
                                    .fp2={.f31=CONST_FIXED_P_3_2}
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
                                    .fp1={.f31=CONST_FIXED_P_7_3},
                                    .fp2={.f31=CONST_FIXED_P_3_2}
                                 },
                                 {
                                    .format=pearlrt::IOFormatEntry::A
                                 },
                                 {
                                    .format=pearlrt::IOFormatEntry::F,
                                    .fp1={.f31=CONST_FIXED_P_7_3},
                                    .fp2={.f31=CONST_FIXED_P_3_2}
                                 },
                                 {
                                    .format=pearlrt::IOFormatEntry::A
                                 },
                                 {
                                    .format=pearlrt::IOFormatEntry::F,
                                    .fp1={.f31=CONST_FIXED_P_7_3},
                                    .fp2={.f31=CONST_FIXED_P_3_2}
                                 },
                                 {
                                    .format=pearlrt::IOFormatEntry::A
                                 },
                                 {
                                    .format=pearlrt::IOFormatEntry::F,
                                    .fp1={.f31=CONST_FIXED_P_7_3},
                                    .fp2={.f31=CONST_FIXED_P_3_2}
                                 },
                                 {
                                    .format=pearlrt::IOFormatEntry::A
                                 }
                               };

                                  pearlrt::Float<23>  tempVar9 (_diameter_sensor
                                  *CONST_FIXED_P_1000_10);

                                  pearlrt::IODataEntry dataEntries[]  = {
                                    {
                                       .dataType={pearlrt::IODataEntry::CHAR,1},
                                       .dataPtr={.outData=&CONST_CHARACTER_2be3ed2d_21e1_4bcc_be1c_b220338a566b},
                                       .param1={.numberOfElements = 1},
                                    },
                                    {
                                       .dataType={pearlrt::IODataEntry::BIT,1},
                                       .dataPtr={.outData=&_simulation_is_running},
                                       .param1={.numberOfElements = 1},
                                    },
                                    {
                                       .dataType={pearlrt::IODataEntry::CHAR,1},
                                       .dataPtr={.outData=&CONST_CHARACTER_2be3ed2d_21e1_4bcc_be1c_b220338a566b},
                                       .param1={.numberOfElements = 1},
                                    },
                                    {
                                       .dataType={pearlrt::IODataEntry::FLOAT,23},
                                       .dataPtr={.outData=&_screwmotor_current_rpm},
                                       .param1={.numberOfElements = 1},
                                    },
                                    {
                                       .dataType={pearlrt::IODataEntry::CHAR,1},
                                       .dataPtr={.outData=&CONST_CHARACTER_2be3ed2d_21e1_4bcc_be1c_b220338a566b},
                                       .param1={.numberOfElements = 1},
                                    },
                                    {
                                       .dataType={pearlrt::IODataEntry::FLOAT,23},
                                       .dataPtr={.outData=&_spoolermotor_current_rpm},
                                       .param1={.numberOfElements = 1},
                                    },
                                    {
                                       .dataType={pearlrt::IODataEntry::CHAR,1},
                                       .dataPtr={.outData=&CONST_CHARACTER_2be3ed2d_21e1_4bcc_be1c_b220338a566b},
                                       .param1={.numberOfElements = 1},
                                    },
                                    {
                                       .dataType={pearlrt::IODataEntry::FLOAT,23},
                                       .dataPtr={.outData=&_screwheater_current_pwm},
                                       .param1={.numberOfElements = 1},
                                    },
                                    {
                                       .dataType={pearlrt::IODataEntry::CHAR,1},
                                       .dataPtr={.outData=&CONST_CHARACTER_2be3ed2d_21e1_4bcc_be1c_b220338a566b},
                                       .param1={.numberOfElements = 1},
                                    },
                                    {
                                       .dataType={pearlrt::IODataEntry::FLOAT,23},
                                       .dataPtr={.outData=&tempVar9},
                                       .param1={.numberOfElements = 1},
                                    },
                                    {
                                       .dataType={pearlrt::IODataEntry::CHAR,1},
                                       .dataPtr={.outData=&CONST_CHARACTER_2be3ed2d_21e1_4bcc_be1c_b220338a566b},
                                       .param1={.numberOfElements = 1},
                                    },
                                    {
                                       .dataType={pearlrt::IODataEntry::FLOAT,23},
                                       .dataPtr={.outData=&_temperature_sensor},
                                       .param1={.numberOfElements = 1},
                                    },
                                    {
                                       .dataType={pearlrt::IODataEntry::CHAR,1},
                                       .dataPtr={.outData=&CONST_CHARACTER_2be3ed2d_21e1_4bcc_be1c_b220338a566b},
                                       .param1={.numberOfElements = 1},
                                    },
                                    {
                                       .dataType={pearlrt::IODataEntry::BIT,1},
                                       .dataPtr={.outData=&_contact_switch_enabled},
                                       .param1={.numberOfElements = 1},
                                    },
                                    {
                                       .dataType={pearlrt::IODataEntry::CHAR,1},
                                       .dataPtr={.outData=&CONST_CHARACTER_2be3ed2d_21e1_4bcc_be1c_b220338a566b},
                                       .param1={.numberOfElements = 1},
                                    },
                                    {
                                       .dataType={pearlrt::IODataEntry::FLOAT,23},
                                       .dataPtr={.outData=&_winding_diameter},
                                       .param1={.numberOfElements = 1},
                                    },
                                    {
                                       .dataType={pearlrt::IODataEntry::CHAR,1},
                                       .dataPtr={.outData=&CONST_CHARACTER_2be3ed2d_21e1_4bcc_be1c_b220338a566b},
                                       .param1={.numberOfElements = 1},
                                    },
                                    {
                                       .dataType={pearlrt::IODataEntry::FLOAT,23},
                                       .dataPtr={.outData=&_avg_dia},
                                       .param1={.numberOfElements = 1},
                                    },
                                    {
                                       .dataType={pearlrt::IODataEntry::CHAR,1},
                                       .dataPtr={.outData=&CONST_CHARACTER_2be3ed2d_21e1_4bcc_be1c_b220338a566b},
                                       .param1={.numberOfElements = 1},
                                    },
                                    {
                                       .dataType={pearlrt::IODataEntry::FLOAT,23},
                                       .dataPtr={.outData=&_total_revs},
                                       .param1={.numberOfElements = 1},
                                    },
                                    {
                                       .dataType={pearlrt::IODataEntry::CHAR,1},
                                       .dataPtr={.outData=&CONST_CHARACTER_2be3ed2d_21e1_4bcc_be1c_b220338a566b},
                                       .param1={.numberOfElements = 1},
                                    },
                                    {
                                       .dataType={pearlrt::IODataEntry::FLOAT,23},
                                       .dataPtr={.outData=&_wound_up_fil_mass},
                                       .param1={.numberOfElements = 1},
                                    },
                                    {
                                       .dataType={pearlrt::IODataEntry::CHAR,1},
                                       .dataPtr={.outData=&CONST_CHARACTER_2be3ed2d_21e1_4bcc_be1c_b220338a566b},
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

                               me->setLocation(800, "SimFilExtruder.prl");
                               {
                               pearlrt::Semaphore *tmp_0[] = {&_sema}; 
                                  	 	
                                 pearlrt::Semaphore::release( me, 1, tmp_0);
                                   


                               }
                               me->setLocation(801, "SimFilExtruder.prl");
                               _log( me, pearlrt::RefCharacter(_line));

                               me->setLocation(802, "SimFilExtruder.prl");
                                   me->resume( pearlrt::Task::AFTER,
                                         /* at     */  pearlrt::Clock(),
                                         /* after  */  _log_interval,
                               	  /* when   */  0
                                          );






            } 
    } // REPEAT 





}




}
