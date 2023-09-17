#pragma once

#include <cstdint> 
#include <PearlIncludes.h>
#include "watertank/SimWatertank.h"

namespace ns_SimWatertank {
  class WatertankInt {
  private:
    WatertankInt();
    ~WatertankInt();
    
    uint8_t m_connections;
    
  public:
    static WatertankInt* instance();
    
    void start_simulation(pearlrt::Task *me);
    void stop_simulation(pearlrt::Task *me);

    pearlrt::Float<23> get_watertank_capacity(pearlrt::Task *me);
    pearlrt::Float<23> get_level(pearlrt::Task *me);

    pearlrt::Float<23> get_pump_pressure(pearlrt::Task *me);
    pearlrt::Float<23> get_watertank_pressure(pearlrt::Task *me);
  
    pearlrt::Fixed<31> get_pump_rotational_speed(pearlrt::Task *me);
    void set_pump_rotational_speed(pearlrt::Task *me, pearlrt::Fixed<15> rpm);

    void pump_switch_on(pearlrt::Task *me);
    void pump_switch_off(pearlrt::Task *me);
  
    pearlrt::BitString<1> get_float_switch_state(pearlrt::Task *me);

    pearlrt::Float<23> get_pressure_sensor_1(pearlrt::Task *me);
    pearlrt::Float<23> get_pressure_sensor_2(pearlrt::Task *me);
  
    void open_valve(pearlrt::Task *me);
    void close_valve(pearlrt::Task *me);
  };
  
}
