#include <stdio.h>

#include "WatertankInt.h"

namespace ns_SimWatertank {

  WatertankInt::WatertankInt() {
    std::cout << "WatertankInt initialized\n";
    m_connections = 0;
  }

  WatertankInt::~WatertankInt() {
  }

  WatertankInt* WatertankInt::instance() {
    static WatertankInt* _instance = 0;
  
    if (!_instance) {
      _instance = new WatertankInt();
    }
  
    return _instance;
  }

  void WatertankInt::start_simulation(pearlrt::Task *me) {
    std::cout << "WatertankInt: start_simlulation (1): *** conn=[";
    std::cout << this->m_connections << "]\n";
    
    if ( m_connections == 0)
      ns_SimWatertank::_start_simulation(me);
    else
      m_connections++;

    std::cout << "WatertankInt: start_simlulation (2): *** conn=[";
    std::cout << this->m_connections << "]\n";
  }
  
  void WatertankInt::stop_simulation(pearlrt::Task *me) {
    std::cout << "WatertankInt: stop_simlulation (1): *** conn=[";
    std::cout << this->m_connections << "]\n";

    m_connections--;
    if ( m_connections == 0)
      ns_SimWatertank::_stop_simulation(me);

    std::cout << "WatertankInt: stop_simlulation (2): *** conn=[";
    std::cout << this->m_connections << "]\n";
  }
  
  pearlrt::Float<23> WatertankInt::get_level(pearlrt::Task *me) {
    std::cout << "WatertankInt: get_level=" << ns_SimWatertank::_get_level(me).x << "\n";
    return ns_SimWatertank::_get_level(me);
  }

  pearlrt::Float<23> WatertankInt::get_watertank_capacity(pearlrt::Task *me) {
    std::cout << "WatertankInt: get_watertank_capacity=" << ns_SimWatertank::_get_watertank_capacity(me).x << "\n";
    return ns_SimWatertank::_get_watertank_capacity(me);
  }

  pearlrt::Float<23> WatertankInt::get_pump_pressure(pearlrt::Task *me) {
    std::cout << "WatertankInt: get_pump_pressure=" << ns_SimWatertank::_get_pump_pressure(me).x << "\n";
    return ns_SimWatertank::_get_pump_pressure(me);
  }
  
  pearlrt::Fixed<31> WatertankInt::get_pump_rotational_speed(pearlrt::Task *me) {
    std::cout << "WatertankInt: get_pump_rotational_speed=" << ns_SimWatertank::_get_pump_rotational_speed(me).x << "\n";
    return ns_SimWatertank::_get_pump_rotational_speed(me);
  }
  
  void WatertankInt::set_pump_rotational_speed(pearlrt::Task *me, pearlrt::Fixed<15> rpm) {
    std::cout << "WatertankInt: set_pump_rotational_speed=" << rpm.x << "\n";
    return ns_SimWatertank::_set_pump_rotational_speed(me,rpm);
  }

  pearlrt::BitString<1> WatertankInt::get_float_switch_state(pearlrt::Task *me) {
    std::cout << "WatertankInt: get_float_switch_state=" << ns_SimWatertank::_get_float_switch_state(me).x << "\n";
    return ns_SimWatertank::_get_float_switch_state(me);
  }

  pearlrt::Float<23> WatertankInt::get_pressure_sensor_1(pearlrt::Task *me) {
    std::cout << "WatertankInt: get_pressure_sensor_1=" << ns_SimWatertank::_get_pressure_sensor_1(me).x << "\n";
    return ns_SimWatertank::_get_pressure_sensor_1(me);
  }

  pearlrt::Float<23> WatertankInt::get_pressure_sensor_2(pearlrt::Task *me) {
    std::cout << "WatertankInt: get_pressure_sensor_2=" << ns_SimWatertank::_get_pressure_sensor_2(me).x << "\n";
    return ns_SimWatertank::_get_pressure_sensor_2(me);
  }
  
  void WatertankInt::open_valve(pearlrt::Task *me) {
    std::cout << "WatertankInt: open_valve" "\n";
    return ns_SimWatertank::_open_valve(me);
  }

  void WatertankInt::close_valve(pearlrt::Task *me) {
    std::cout << "WatertankInt: close_valve" "\n";
    return ns_SimWatertank::_close_valve(me);
  }

}
