#pragma once

#include <PearlIncludes.h>

namespace ns_SimFilExtruder {
  void _start_simulation(pearlrt::Task *me);
  void _stop_simulation(pearlrt::Task *me);
  pearlrt::Fixed<31> _get_screwmotor_rotational_speed(pearlrt::Task *me);
  void _set_screwmotor_rotational_speed(pearlrt::Task *me, pearlrt::Fixed<15>);
  pearlrt::Fixed<31> _get_spoolermotor_rotational_speed(pearlrt::Task *me);
  void _set_spoolermotor_rotational_speed(pearlrt::Task *me, pearlrt::Fixed<15>);
  pearlrt::Fixed<31> _get_screwheater_pwm(pearlrt::Task *me);
  void _set_screwheater_pwm(pearlrt::Task *me, pearlrt::Fixed<15>);
  pearlrt::Float<23> _get_temperature_sensor(pearlrt::Task *me);
  pearlrt::Float<23> _get_diameter_sensor(pearlrt::Task *me);

}
