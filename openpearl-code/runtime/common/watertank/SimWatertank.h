#pragma once

#include <PearlIncludes.h>

namespace ns_SimWatertank {
  void _start_simulation(pearlrt::Task *me);
  void _stop_simulation(pearlrt::Task *me);

  pearlrt::Float<23> _get_level(pearlrt::Task *me);

  pearlrt::Float<23> _get_pump_pressure(pearlrt::Task *me);
  pearlrt::Float<23> _get_watertank_pressure(pearlrt::Task *me);
  
  pearlrt::Float<23> _get_watertank_capacity(pearlrt::Task *me);
  pearlrt::Fixed<31> _get_pump_rotational_speed(pearlrt::Task *me);

  void _open_valve(pearlrt::Task *me);
  void _close_valve(pearlrt::Task *me);
}
