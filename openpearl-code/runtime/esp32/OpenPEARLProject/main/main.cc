/*
 [The "BSD license"]
 Copyright (c) 2014 Rainer Mueller
 Copyright (c) 2018 Michael Kotzjan
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions
 are met:

 1. Redistributions of source code must retain the above copyright
    notice, this list of conditions and the following disclaimer.
 2. Redistributions in binary form must reproduce the above copyright
    notice, this list of conditions and the following disclaimer in the
    documentation and/or other materials provided with the distribution.
 3. The name of the author may not be used to endorse or promote products
    derived from this software without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

/**
	\file
	\brief OpenPEARL entry

	system start

\mainpage

The OpenPEARL system for the ESP board is based on the
FreeRTOS scheduler.

Most of the classes are plattform independent.
This documentation consists of the plattform dependent and plattform
independent parts.


*/


//#include <debug_frmwrk.h>


/* Scheduler includes. */
#include <freertos/FreeRTOS.h>
#include <freertos/queue.h>
#include <freertos/task.h>
#include <freertos/semphr.h>

#include "driver/gpio.h"

#include "GenericTask.h"
#include "Signals.h"
#include "TaskList.h"
#include "Task.h"
#include "TaskMonitor.h"
#include "Control.h"
#include "Log.h"
#include "Clock.h"
#include "service.h"
#include "Esp32Clock.h"
//#include "Esp32WifiConfig.h"

// read options from menuconfig
#include "../../configuration/include/autoconf.h"


using namespace pearlrt;
/*-----------------------------------------------------------*/
/**
main entry to the OpenPearl application

Show the list of defined tasks, activate all "MAIN"-tasks
This is done in an FreeRTOS thread created by the esp-idf

\returns nothing - will never return!
*/
extern "C" {
   void  app_main() {
      char line[40];

      printf("OpenPEARL started\n");
      Control::initModules();

      // test for clock initialization

      if (!Esp32Clock::isClockSelected()) {
         printf("set default clock source\n");
         Esp32Clock dummy(0);  // the object may be discarded immediately
      }

      printf("set log level \n");
//      Log::getInstance()->setLevel(0x0c); //e+w
      Log::getInstance()->setLevel(0x0e);   //e+w+i
//      Log::getInstance()->setLevel(0x0f);     //e+w+i+d

      // start background service task
      init_service();

      /*
       * This task starts all PEARL90 main tasks, afterwards the
       * task suspends itself until another task resume it
       */
      Log::info("Defined Tasks");

      // format with sprintf, since Log does not allow format parameters
      sprintf(line, "%-10.10s %4s %s", "Name", "Prio", "isMain");
      Log::info(line);
      TaskList::Instance().sort(); // sort taskList

      for (int i = 0; i < TaskList::Instance().size(); i++) {
         Task *t = TaskList::Instance().getTaskByIndex(i);

         // format with sprintf, since Log does not allow format parameters
         sprintf(line, "%-10.10s  %3d  %2d", t->getName(),
                 (t->getPrio()).x,
                 t->getIsMain());
         Log::info(line);
         t->init();
      }


      if (TaskList::Instance().size() == 0) {
         printf("no task defined --> exit.\n");
         //exit(1); //!! no exit on esp32
      }

      /*****************init end*******************/
      //activate all threads which declared with "main"
      Log::info("start all main-threads");

      for (int i = 0; i < TaskList::Instance().size();  i++) {
         Task *t = TaskList::Instance().getTaskByIndex(i);

         if (t->getIsMain()) {
            t->activate(t);
         }
      }

      // all heap elements should be allocated now !
      // dump unused size to log
      Log::info("Free Heap size: %d byte", xPortGetFreeHeapSize());

#if 0
/*
      xTaskCreate(&blink_task, "blink_task",
                  5000, //configMINIMAL_STACK_SIZE,
                  NULL, 5, NULL);
      printf("blink started\n");
*/
#endif
   }

}
