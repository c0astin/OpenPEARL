// hfujk

#ifndef RUNTIME_HFUJKCONTROL_H
#define RUNTIME_HFUJKCONTROL_H

#include <iostream>
#include <cstring>
#include <chrono>
#include <mutex>

#include "ResourceAllocationGraph.h"

using namespace std;

namespace pearlrt {

   class HfujkControl {

   private:

      HfujkControl() {}

      ResourceAllocationGraph* resourceAllocationGraph{};
      static int resourceAllocationGraphNodeCount;
      static HfujkControl* instance;
      static std::mutex instanceMutex;

   public:

      static constexpr const char* RELEASE_DATE = "18.02.2022";
      static constexpr const bool ACTIVATE_STD_OUT = true;

      static HfujkControl* getInstance()
      {
         if (!instance)
         {
            instance = new HfujkControl();
            instance->resourceAllocationGraph = new ResourceAllocationGraph(resourceAllocationGraphNodeCount);
         }

         return instance;
      }

      static bool configure(int resourceAllocationGraphNodeCount);

      static bool getDeadlockSituation();

      static void registerDeadlockOperation(const DeadlockOperation& deadlockOperation);

      static void printDeadlockOperations();

      static unsigned long long getCurrentTimestamp();

      static string formatTimestamp(unsigned long long timestamp, bool inMilliseconds = true);

      static string formatIdentifier(const string& identifier);

      static void performDeadlockOperation(DeadlockOperation& deadlockOperation);
   };
}

#endif //RUNTIME_HFUJKCONTROL_H
