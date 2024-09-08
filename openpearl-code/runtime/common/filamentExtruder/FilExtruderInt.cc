#include <stdio.h>

#include "FilExtruderInt.h"

//#define DEBUG 0

namespace ns_SimFilExtruder {

  FilExtruderInt::FilExtruderInt() {
#ifdef DEBUG
    std::cout << "FilExtruderInt initialized\n";
#endif
    m_connections = 0;
  }

  FilExtruderInt::~FilExtruderInt() {
  }

  FilExtruderInt* FilExtruderInt::getInstance() {
#ifdef DEBUG
    std::cout << "FilExtruderInt::getInstance\n";
#endif

    return (!m_instanceSingleton) ? m_instanceSingleton = new FilExtruderInt : m_instanceSingleton;
  }

  void FilExtruderInt::start_simulation(pearlrt::Task *me) {
    if ( m_connections == 0) {
#ifdef DEBUG    
    std::cout << "FilExtruderInt: start simulation\n";
#endif        
		      
      ns_SimFilExtruder::_start_simulation(me);
    } else {
#ifdef DEBUG    
    std::cout << "FilExtruderInt: simulator already running\n";
#endif        
    }
    	
     m_connections++;
     
#ifdef DEBUG    
    std::cout << "FilExtruderInt: start_simulation: conn=[" << m_connections << "]\n";
#endif
  }
  
  void FilExtruderInt::stop_simulation(pearlrt::Task *me) {
    this->m_connections--;
    if ( this->m_connections == 0) {
#ifdef DEBUG    
    std::cout << "FilExtruderInt: stop simulator, because no more connections left\n";
#endif        

      ns_SimFilExtruder::_stop_simulation(me);
    }
  }

    pearlrt::Fixed<31> FilExtruderInt::get_screwmotor_rotational_speed(pearlrt::Task *me) {
#ifdef DEBUG    
    std::cout << "FilExtruderInt: get_screwmotor_rotational_speed=" << ns_SimFilExtruder::_get_screwmotor_rotational_speed(me).x << "\n";
#endif        
    return ns_SimFilExtruder::_get_screwmotor_rotational_speed(me);
  }
  
  void FilExtruderInt::set_screwmotor_rotational_speed(pearlrt::Task *me, pearlrt::Fixed<31> rpm) {
#ifdef DEBUG    
    std::cout << "FilExtruderInt: set_screwmotor_rotational_speed=" << rpm.x << "\n";
#endif        
    return ns_SimFilExtruder::_set_screwmotor_rotational_speed(me,rpm);
  }

    pearlrt::Fixed<31> FilExtruderInt::get_spoolermotor_rotational_speed(pearlrt::Task *me) {
#ifdef DEBUG    
    std::cout << "FilExtruderInt: get_spoolermotor_rotational_speed=" << ns_SimFilExtruder::_get_spoolermotor_rotational_speed(me).x << "\n";
#endif        
    return ns_SimFilExtruder::_get_spoolermotor_rotational_speed(me);
  }
  
  void FilExtruderInt::set_spoolermotor_rotational_speed(pearlrt::Task *me, pearlrt::Fixed<31> rpm) {
#ifdef DEBUG    
    std::cout << "FilExtruderInt: set_spoolermotor_rotational_speed=" << rpm.x << "\n";
#endif        
    return ns_SimFilExtruder::_set_spoolermotor_rotational_speed(me,rpm);
  }

    pearlrt::Fixed<31> FilExtruderInt::get_screwheater_pwm(pearlrt::Task *me) {
#ifdef DEBUG    
    std::cout << "FilExtruderInt: get_screwheater_pwm=" << ns_SimFilExtruder::_get_screwheater_pwm(me).x << "\n";
#endif        
    return ns_SimFilExtruder::_get_screwheater_pwm(me);
  }
  
  void FilExtruderInt::set_screwheater_pwm(pearlrt::Task *me, pearlrt::Fixed<31> pwm) {
#ifdef DEBUG    
    std::cout << "FilExtruderInt: set_screwheater_pwm=" << pwm.x << "\n";
#endif        
    return ns_SimFilExtruder::_set_screwheater_pwm(me,pwm);
  }

    pearlrt::Float<23> FilExtruderInt::get_temperature_sensor(pearlrt::Task *me) {
#ifdef DEBUG    
    std::cout << "FilExtruderInt: get_temperature_sensor=" << ns_SimFilExtruder::_get_temperature_sensor(me).x << "\n";
#endif        
    return ns_SimFilExtruder::_get_temperature_sensor(me);
  }

    pearlrt::Float<23> FilExtruderInt::get_diameter_sensor(pearlrt::Task *me) {
#ifdef DEBUG    
    std::cout << "FilExtruderInt: get_diameter_sensor=" << ns_SimFilExtruder::_get_diameter_sensor(me).x << "\n";
#endif        
    return ns_SimFilExtruder::_get_diameter_sensor(me);
  }

  FilExtruderInt* FilExtruderInt::m_instanceSingleton = nullptr;
}
