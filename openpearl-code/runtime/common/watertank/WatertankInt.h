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
  };
}
