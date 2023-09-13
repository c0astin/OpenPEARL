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

}
