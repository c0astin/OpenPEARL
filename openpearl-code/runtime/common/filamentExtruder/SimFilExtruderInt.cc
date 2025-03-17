#include <stdio.h>

#include "SimFilExtruderInt.h"

//#define DEBUG 0

namespace ns_SimFilExtruder {

  SimFilExtruderInt::SimFilExtruderInt() {
#ifdef DEBUG
    std::cout << "SimFilExtruderInt initialized\n";
#endif
    m_connections = 0;
  }

  SimFilExtruderInt::~SimFilExtruderInt() {
  }

  SimFilExtruderInt* SimFilExtruderInt::getInstance() {
#ifdef DEBUG
    std::cout << "SimFilExtruderInt::getInstance\n";
#endif

    return (!m_instanceSingleton) ? m_instanceSingleton = new SimFilExtruderInt : m_instanceSingleton;
  }

  void SimFilExtruderInt::start_simulation(pearlrt::Task *me) {
    if ( m_connections == 0) {
#ifdef DEBUG    
    std::cout << "SimFilExtruderInt: start simulation\n";
#endif        
		      
      ns_SimFilExtruder::_start_simulation(me);
    } else {
#ifdef DEBUG    
    std::cout << "SimFilExtruderInt: simulator already running\n";
#endif        
    }
    	
     m_connections++;
     
#ifdef DEBUG    
    std::cout << "SimFilExtruderInt: start_simulation: conn=[" << m_connections << "]\n";
#endif
  }
  
  void SimFilExtruderInt::stop_simulation(pearlrt::Task *me) {
    this->m_connections--;
    if ( this->m_connections == 0) {
#ifdef DEBUG    
    std::cout << "SimFilExtruderInt: stop simulator, because no more connections left\n";
#endif        

      ns_SimFilExtruder::_stop_simulation(me);
    }
  }

    pearlrt::Fixed<31> SimFilExtruderInt::get_screwmotor_rotational_speed(pearlrt::Task *me) {
#ifdef DEBUG    
    std::cout << "SimFilExtruderInt: get_screwmotor_rotational_speed=" << ns_SimFilExtruder::_get_screwmotor_rotational_speed(me).x << "\n";
#endif        
    return ns_SimFilExtruder::_get_screwmotor_rotational_speed(me);
  }
  
  void SimFilExtruderInt::set_screwmotor_rotational_speed(pearlrt::Task *me, pearlrt::Fixed<31> rpm) {
#ifdef DEBUG    
    std::cout << "SimFilExtruderInt: set_screwmotor_rotational_speed=" << rpm.x << "\n";
#endif        
    return ns_SimFilExtruder::_set_screwmotor_rotational_speed(me,rpm);
  }

    pearlrt::Fixed<31> SimFilExtruderInt::get_spoolermotor_rotational_speed(pearlrt::Task *me) {
#ifdef DEBUG    
    std::cout << "SimFilExtruderInt: get_spoolermotor_rotational_speed=" << ns_SimFilExtruder::_get_spoolermotor_rotational_speed(me).x << "\n";
#endif        
    return ns_SimFilExtruder::_get_spoolermotor_rotational_speed(me);
  }
  
  void SimFilExtruderInt::set_spoolermotor_rotational_speed(pearlrt::Task *me, pearlrt::Fixed<31> rpm) {
#ifdef DEBUG    
    std::cout << "SimFilExtruderInt: set_spoolermotor_rotational_speed=" << rpm.x << "\n";
#endif        
    return ns_SimFilExtruder::_set_spoolermotor_rotational_speed(me,rpm);
  }

    pearlrt::Fixed<31> SimFilExtruderInt::get_screwheater_pwm(pearlrt::Task *me) {
#ifdef DEBUG    
    std::cout << "SimFilExtruderInt: get_screwheater_pwm=" << ns_SimFilExtruder::_get_screwheater_pwm(me).x << "\n";
#endif        
    return ns_SimFilExtruder::_get_screwheater_pwm(me);
  }
  
  void SimFilExtruderInt::set_screwheater_pwm(pearlrt::Task *me, pearlrt::Fixed<31> pwm) {
#ifdef DEBUG    
    std::cout << "SimFilExtruderInt: set_screwheater_pwm=" << pwm.x << "\n";
#endif        
    return ns_SimFilExtruder::_set_screwheater_pwm(me,pwm);
  }

    pearlrt::Float<23> SimFilExtruderInt::get_temperature_sensor(pearlrt::Task *me) {
#ifdef DEBUG    
    std::cout << "SimFilExtruderInt: get_temperature_sensor=" << ns_SimFilExtruder::_get_temperature_sensor(me).x << "\n";
#endif        
    return ns_SimFilExtruder::_get_temperature_sensor(me);
  }

    pearlrt::Float<23> SimFilExtruderInt::get_diameter_sensor(pearlrt::Task *me) {
#ifdef DEBUG    
    std::cout << "SimFilExtruderInt: get_diameter_sensor=" << ns_SimFilExtruder::_get_diameter_sensor(me).x << "\n";
#endif        
    return ns_SimFilExtruder::_get_diameter_sensor(me);
  }

    pearlrt::BitString<64> SimFilExtruderInt::read_msg(pearlrt::Task *me) {
#ifdef DEBUG    
    std::cout << "SimFilExtruderInt: read_can_msg=" << ns_SimFilExtruder::_read_can_msg(me).x << "\n";
#endif        
    return ns_SimFilExtruder::_read_msg(me);
  }
  
  void SimFilExtruderInt::send_msg(pearlrt::Task *me, pearlrt::BitString<64> msg) {
#ifdef DEBUG    
    std::cout << "SimFilExtruderInt: send_can_msg=" << msg.x << "\n";
#endif        
    return ns_SimFilExtruder::_send_msg(me,msg);
  }

    pearlrt::BitString<1> SimFilExtruderInt::get_contact_switch_state(pearlrt::Task *me) {
#ifdef DEBUG    
    std::cout << "SimFilExtruderInt: get_contact_switch_state=" << ns_SimFilExtruder::_get_contact_switch_state(me).x << "\n";
#endif        
    return ns_SimFilExtruder::_get_contact_switch_state(me);
  }

  SimFilExtruderInt* SimFilExtruderInt::m_instanceSingleton = nullptr;

  
}
