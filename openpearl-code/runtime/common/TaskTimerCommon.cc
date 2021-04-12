/* _POSIX_TIMERS required for FreeRTOS based system */
#ifndef _POSIX_TIMERS
 // note: this produces a warning from xtensa-g++ about duplicate
 //       definition in time.h
 //       without this define we don't have access to timer_settime()
 #define _POSIX_TIMERS
#endif

#include <time.h>
#include "TaskCommon.h"
#include "TaskTimerCommon.h"
#include <stdio.h>

extern "C" {
   extern int timer_gettime(timer_t timerid, struct itimerspec *value);
};


namespace pearlrt {
   void TaskTimerCommon::update() {
      //check for endcondition for during
      if (counts < 0) {
         // do it eternally
      } else {
         counts --;

         if (counts == 0) {
            // became zero --> stop timer
            if (stop()) {
               // error during cancellation
               // ?????????????????????????
            }
         }
      }

      callback(task);
      return;
   }

   TaskTimerCommon::TaskTimerCommon() {
      counts = 0;
      countsBackup = 0;
   }

   void TaskTimerCommon::set(int condition,
                       Clock at, Duration after, Duration all,
                       Clock until, Duration during) {
      int counts;
      static const Duration nullDelay(0,0);
      static const Duration oneDay(24 * 60 * 60,0);
      Clock now = Clock::now();

      // calculate start delay
      if (condition & TaskCommon::AFTER) {
         if ((after < nullDelay).getBoolean()) {
            Log::error("Task %s: negative value at AFTER", task->getName());
            throw theIllegalSchedulingSignal;
         }

         if ((after > nullDelay).getBoolean()) {
            counts = 1;
         } else {
            counts = 0;  // AFTER 0 SEC disabled start delay
            after = nullDelay;  // start immedially
         }
      } else {
         after = nullDelay;  // start immedially
         counts = 0;
      }

      if (condition & TaskCommon::AT) {
         after = at - now;

         if ((after < nullDelay).getBoolean()) {
            Log::warn("Task %s: negative value at AFTER --> next day",
                      task->getName());

            do {
               after += oneDay;
            } while ((after < nullDelay).getBoolean());
         }

         counts = 1;
      }

      its.it_value.tv_sec = after.getSec();
      its.it_value.tv_nsec = after.getUsec() * 1000;
      // calculate repetition counter for the schedule
      its.it_interval.tv_sec = 0;
      its.it_interval.tv_nsec = 0;

      // calculate repetition counts
      if (condition & TaskCommon::ALL) {
         if ((all <= nullDelay).getBoolean()) {
            Log::error("Task %s: negative value at ALL", task->getName());
            throw theIllegalSchedulingSignal;
         }

         its.it_interval.tv_sec = all.getSec();;
         its.it_interval.tv_nsec = all.getUsec() * 1000;

         if (condition & TaskCommon::UNTIL) {
            // transform absolute end time into relative duration
            during = until - now;

            // the AT-value is transformed into an AFTER some lines
            // above --> treat both
            if (condition & (TaskCommon::AT | TaskCommon::AFTER)) {
               during = during - after;
            }

            if ((during <= nullDelay).getBoolean()) {
               do {
                  during += oneDay;
               } while ((during < nullDelay).getBoolean());
            }

            counts = (during / all).x + 1;
         } else if (condition & TaskCommon::DURING) {
            // the AT-value is transformed into an AFTER some lines
            // above --> treat both
            /* regard DURING based on the end of the delayed start
               --> remove these lines
                        if (condition & (TaskCommon::AT | TaskCommon::AFTER)) {
                           during = during - after;
                        }
            */
            if ((during <= nullDelay).getBoolean()) {
               Log::error("Task %s: negative (effective) value at DURING",
                          task->getName());

               throw theIllegalSchedulingSignal;
            }

            counts = (during / all).x + 1;
            Log::debug(
               "task %s: scheduled after=%.3f s all %.3f s %d times",
               task->getName(),
               its.it_value.tv_sec + its.it_value.tv_nsec / 1e9,
               its.it_interval.tv_sec + its.it_interval.tv_nsec / 1e9,
               counts);
         } else {
            counts = -1;
            Log::debug(
               "task %s: scheduled  after=%.3f s all %.3f s eternally",
               task->getName(),
               its.it_value.tv_sec + its.it_value.tv_nsec / 1e9,
               its.it_interval.tv_sec + its.it_interval.tv_nsec / 1e9);
         }
      }

      if ((condition & (TaskCommon:: AT | TaskCommon::ALL | TaskCommon::AFTER |
                        TaskCommon::UNTIL | TaskCommon::DURING)) != 0) {
         // timed activate/continue, set initial delay to repetion delay
         // if no initial delay is specified
         if (its.it_value.tv_sec == 0 && its.it_value.tv_nsec == 0) {
            its.it_value.tv_sec = its.it_interval.tv_sec;
            its.it_value.tv_nsec = its.it_interval.tv_nsec;
         }
      }

      countsBackup = counts;
   }

   int TaskTimerCommon::start() {
      counts = countsBackup;   // restore number for triggeredActivate
      Log::debug("%s: TaskTimer::start", task->getName());

      if (its.it_value.tv_sec != 0 || its.it_value.tv_nsec != 0) {
         if (timer_settime(timer, 0, &its, NULL) == -1) {
            Log::error("task %s: error setting schedule timer",
                       task->getName());
            return (-1);
         }
      }

      return 0;
   }

   bool TaskTimerCommon::isActive() {
      return counts != 0;
   }

   bool TaskTimerCommon::isSet() {
      return countsBackup != 0;
   }

   int TaskTimerCommon::stop() {
      // need local variable to keep time settings of the time
      // unchanged for new start of the timer
      struct itimerspec its;

      its.it_value.tv_sec = 0;
      its.it_value.tv_nsec = 0;
      its.it_interval.tv_sec = 0;
      its.it_interval.tv_nsec = 0;
      counts = 0;

      //kill the timer
      if (timer_settime(timer, 0, &its, NULL) == -1) {
         Log::error(
            "task %s: error cancelling timer", task->getName());
         return (-1);
      }

      return 0;
   }



   int TaskTimerCommon::cancel() {
      countsBackup = 0;
      return stop();
   }



   void TaskTimerCommon::detailedStatus(char *id, char * line) {
      struct itimerspec its;
      float next, rept;

      timer_gettime(timer, &its);
      next = its.it_value.tv_sec + its.it_value.tv_nsec / 1.e9;
      rept = its.it_interval.tv_sec + its.it_interval.tv_nsec / 1.e9;

      if (counts > 0) {
         sprintf(line,
                 "%s next %.1f sec : all %.1f sec : %d times remaining",
                 id, next, rept, counts);
      } else {
         sprintf(line,
                 "%s next %.1f sec : all %.1f sec : eternally",
                 id, next, rept);
      }

   }
}
