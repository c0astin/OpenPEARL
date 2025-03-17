#pragma once

#include <cstdint> 
#include <PearlIncludes.h>
#include "filamentExtruder/SimFilExtruder.h"

namespace ns_SimFilExtruder {
  class SimFilExtruderInt {
  private:
    SimFilExtruderInt();
    ~SimFilExtruderInt();

#if 0
    // private copy constructor and assignment operator
    SimFilExtruderInt(const SimFilExtruderInt&);
    SimFilExtruderInt& operator=(const SimFilExtruderInt&);
    
    // disable copy/move -- this is a Singleton
    SimFilExtruderInt(const SimFilExtruderInt&) = delete;
    SimFilExtruderInt(SimFilExtruderInt&&) = delete;
    SimFilExtruderInt& operator=(const SimFilExtruderInt&) = delete;
    SimFilExtruderInt& operator=(SimFilExtruderInt&&) = delete;
#endif
    
    static SimFilExtruderInt* m_instanceSingleton;   
    uint8_t m_connections;
    
  public:
    static SimFilExtruderInt* getInstance();
    
    void start_simulation(pearlrt::Task *me);
    void stop_simulation(pearlrt::Task *me);

    pearlrt::Fixed<31> get_screwmotor_rotational_speed(pearlrt::Task *me);
    void set_screwmotor_rotational_speed(pearlrt::Task *me, pearlrt::Fixed<31> rpm);
    pearlrt::Fixed<31> get_spoolermotor_rotational_speed(pearlrt::Task *me);
    void set_spoolermotor_rotational_speed(pearlrt::Task *me, pearlrt::Fixed<31> rpm);
    pearlrt::Fixed<31> get_screwheater_pwm(pearlrt::Task *me);
    void set_screwheater_pwm(pearlrt::Task *me, pearlrt::Fixed<31> pwm);
    pearlrt::BitString<64> read_msg(pearlrt::Task *me);
    void send_msg(pearlrt::Task *me, pearlrt::BitString<64> msg);
    pearlrt::Float<23> get_temperature_sensor(pearlrt::Task *me);
    pearlrt::Float<23> get_diameter_sensor(pearlrt::Task *me);
    pearlrt::BitString<1> get_contact_switch_state(pearlrt::Task *me);

  
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

