#pragma once

#include <cstdint> 
#include <PearlIncludes.h>
#include "watertank/SimWatertank.h"

namespace ns_SimWatertank {
  class WatertankInt {
  private:
    WatertankInt();
    ~WatertankInt();

#if 0
    // private copy constructor and assignment operator
    WatertankInt(const WatertankInt&);
    WatertankInt& operator=(const WatertankInt&);
    
    // disable copy/move -- this is a Singleton
    WatertankInt(const WatertankInt&) = delete;
    WatertankInt(WatertankInt&&) = delete;
    WatertankInt& operator=(const WatertankInt&) = delete;
    WatertankInt& operator=(WatertankInt&&) = delete;
#endif
    
    static WatertankInt* m_instanceSingleton;   
    uint8_t m_connections;
    
  public:
    static WatertankInt* getInstance();
    
    void start_simulation(pearlrt::Task *me);
    void stop_simulation(pearlrt::Task *me);

    pearlrt::Float<23> get_watertank_capacity(pearlrt::Task *me);
    pearlrt::Float<23> get_level(pearlrt::Task *me);

    pearlrt::Float<23> get_pump_pressure(pearlrt::Task *me);
    pearlrt::Float<23> get_watertank_pressure(pearlrt::Task *me);
  
    pearlrt::Fixed<31> get_pump_rotational_speed(pearlrt::Task *me);
    void set_pump_rotational_speed(pearlrt::Task *me, pearlrt::Fixed<31> rpm);

    pearlrt::BitString<1> get_float_switch_state(pearlrt::Task *me);

    pearlrt::Float<23> get_pressure_sensor_1(pearlrt::Task *me);
    pearlrt::Float<23> get_pressure_sensor_2(pearlrt::Task *me);
  
    void open_valve(pearlrt::Task *me);
    void close_valve(pearlrt::Task *me);
    pearlrt::BitString<1> get_valve_state(pearlrt::Task *me);   
  };

}


namespace pearlrt {
  class Simulator {
  public:
    Simulator(char mode) {};

  private:
    ~Simulator();
  };
}

