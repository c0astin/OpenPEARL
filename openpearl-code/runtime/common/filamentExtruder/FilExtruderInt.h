#pragma once

#include <cstdint> 
#include <PearlIncludes.h>
#include "filamentExtruder/SimFilExtruder.h"

namespace ns_SimFilExtruder {
  class FilExtruderInt {
  private:
    FilExtruderInt();
    ~FilExtruderInt();

#if 0
    // private copy constructor and assignment operator
    FilExtruderInt(const FilExtruderInt&);
    FilExtruderInt& operator=(const FilExtruderInt&);
    
    // disable copy/move -- this is a Singleton
    FilExtruderInt(const FilExtruderInt&) = delete;
    FilExtruderInt(FilExtruderInt&&) = delete;
    FilExtruderInt& operator=(const FilExtruderInt&) = delete;
    FilExtruderInt& operator=(FilExtruderInt&&) = delete;
#endif
    
    static FilExtruderInt* m_instanceSingleton;   
    uint8_t m_connections;
    
  public:
    static FilExtruderInt* getInstance();
    
    void start_simulation(pearlrt::Task *me);
    void stop_simulation(pearlrt::Task *me);

    pearlrt::Fixed<31> get_screwmotor_rotational_speed(pearlrt::Task *me);
    void set_screwmotor_rotational_speed(pearlrt::Task *me, pearlrt::Fixed<31> rpm);
    pearlrt::Fixed<31> get_spoolermotor_rotational_speed(pearlrt::Task *me);
    void set_spoolermotor_rotational_speed(pearlrt::Task *me, pearlrt::Fixed<31> rpm);
    pearlrt::Fixed<31> get_screwheater_pwm(pearlrt::Task *me);
    void set_screwheater_pwm(pearlrt::Task *me, pearlrt::Fixed<31> pwm);
    pearlrt::Float<23> get_temperature_sensor(pearlrt::Task *me);
    pearlrt::Float<23> get_diameter_sensor(pearlrt::Task *me);

  
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

