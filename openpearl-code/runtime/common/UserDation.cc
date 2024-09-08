/*
 [The "BSD license"]
 Copyright (c) 2012-2014 Rainer Mueller
 Copyright (c) 2013-2014 Holger Koelle
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

\brief class which provides mutual exclusion locking for dation operations

 */
#include <stdio.h>
#include "UserDation.h"
#include "Signals.h"
#include "TaskCommon.h"
#include "compare.h"
#include "Mutex.h"

namespace pearlrt {

    UserDation::UserDation() {
        //currentTask = NULL;
        //rstValue = NULL;
        rstVoidPointer = NULL;
        dationStatus = CLOSED;
        isBusy = false;
        idfName.setWork(idfNameStorage);
        mutexUserDation.name("UserDation");
        idfNameGiven=false;
    }

    void UserDation::internalDationClose(const int p) {
        static Fixed<31> one(1);
        int prmFlag = 0;

        assertOpen();

        checkCloseParametersAndDecrementCounter(p, systemDation);

        if (counter.x == 0) {
            // last close detected --> close system dation and mark the
            // userdation as closed, even if errors during
            // closing the system dation occur


            // if no CLOSE parameters are set, than check if there
            // are some already set by previous OPEN or CLOSE statements
            if ( (p & (PRM|CAN)) == 0) {
                // nothing set, check previous close parameters
                if ( (dationParams & CLOSEPRM) != 0) {
                    prmFlag |= PRM;   // PRM in CLOSE dominates any setting from OPEN
                }
                if ( (dationParams & CLOSECAN) != 0) {
                    prmFlag |= CAN;   // CAN in CLOSE dominates any setting from OPEN
                }
            } else {
                // CAN or PRM is set
                if ( p & PRM ) {
                    prmFlag |= PRM; 
                }
                if ( p & CAN ) {
                    prmFlag |= CAN;
                }
            }
            if (prmFlag == 0) {
                // no setting from CLOSE found, check OPEN statements
                if (dationParams & OPENPRM) {
                    prmFlag |= PRM;
                }
                if (dationParams & OPENCAN) {
                    prmFlag |= CAN;
                }
            }
            dationStatus = CLOSED;
            closeSystemDation(prmFlag);
            dationParams &= ~(OPENMASK | CLOSEMASK);
        }
    }

    void UserDation::restart(TaskCommon * me,
            Dation::DationParams direction) {
        // to be called from when the task was suspended while being
        // registered in the wait queue
        // test if the device is free --> start i/o processing
        // else add to the wait queue
        // note:
        //  (1) global task lock is locked
        //  (2) the task is blocked

        // for testing purpose it is possible to use
        // without a task object
        if (me) {
            if (systemDation->allowMultipleIORequests()) {
                // register current task in system dation
                systemDation->registerWaitingTask(me, direction);
                Log::debug("%s: added to wait queue", me->getName());
            } else {
                if (isBusy) {
                    // user dation is busy --> add current task to wait queue
                    // and wait for end of the i/o operation
                    waitQueue.insert(me);

                    Log::debug("%s: added to wait queue", me->getName());
                } else {
                    me->enterIO(this);
                    isBusy = true;
                    me->unblock();
                }
            }
        }
    }

    void UserDation::beginSequence(TaskCommon * me,
            Dation::DationParams direction) {
        // this method is called before any dation operation starts
        // in the application
        struct BlockData bd;
        bool blockThread;

        //printf("beginSequence this=%p task=%s  isBusy=%d dir=%d systemDation=%p\n",
        //     this, me->getName(),isBusy, direction, systemDation);

        // verify that the dation is really open
        assertOpen();


        // for testing purpose it is possible to use
        // without a task object
        if (me) {
            mutexUserDation.lock();

            // the taskState of at least one task will change - maybe
            // more than one task is affected
            // lock the global task lock, and treat async requests
            // if pending, after the lock is gained
            TaskCommon::mutexLock();

            rstVoidPointer = NULL;
            currentDirection = direction;

            me->enterIO(this);

            if (systemDation->allowMultipleIORequests()) {
                // register current task in system dation
                systemDation->registerWaitingTask(me, direction);
                bd.reason = IO_MULTIPLE_IO;
                bd.u.io.dation = this;
                bd.u.io.direction = direction;
                blockThread = true;
            } else if (isBusy) {
                // user dation is busy --> add current task to wait queue
                // and wait for end of the i/o operation
                waitQueue.insert(me);
                bd.reason = IOWAITQUEUE;
                bd.u.io.dation = this;
                bd.u.io.direction = direction;

                Log::debug("%s: added to wait queue and waits...", me->getName());
                blockThread = true;
            } else {
                isBusy = true;
                blockThread = false;
            }


            if (blockThread) {
                // aquired task block sema, since unblock expects this
                mutexUserDation.unlock();
                me->block(&bd);    // block releases the global task lock
                //printf("try to obtain locks for userdation als tasks\n");
                mutexUserDation.lock();
                TaskCommon::mutexLock(); // we need access to the global task lock
                //printf("  ... success: got locks for userdation als tasks\n");
            }


            beginSequenceHook(me, direction);       // deal with TFU stuff

            TaskCommon::mutexUnlock(); // we need no longer access to task data
            mutexUserDation.unlock();

            // maybe we must suspend/terminate on return from a
            // non interruptable system device
            me->scheduleCallback();
        }
    }

    void UserDation::endSequence(TaskCommon * me,
            Dation::DationParams direction) {
        TaskCommon* pendingTask;


        // for testing purpose it it possible to use
        // without a task object
        if (me) {
            mutexUserDation.lock();

            endSequenceHook(direction);   // deal with TFU stuff

            // gain global task lock, since the task state of at least one
            // task changes
            TaskCommon::mutexLock();

            me->leaveIO();


            // check whether another task waits for this user dation
            Log::debug("%s: check wait queue", me->getName());
            pendingTask = waitQueue.getHead();
            if (pendingTask == NULL) {
                Log::debug("nobody waits --> done");
                isBusy = false;
            } else {
                Log::debug("%s waits --> goon", pendingTask->getName());
                waitQueue.remove(pendingTask);
                pendingTask->unblock();
            }

            TaskCommon::mutexUnlock();
            mutexUserDation.unlock();

            //         if (systemDation->allowMultipleIORequests()) {
            //            printf("UserDation::endSequence: finish io_multiple_requst\n");
            //         }
            me->scheduleCallback();
        }

    }

    PriorityQueue* UserDation::getWaitQueue() {
        return & waitQueue;
    }


    void UserDation::assertOpen() {
        if (dationStatus == CLOSED) {
            Log::error("dation open required");
            throw theDationNotOpenSignal;
        }
    }

    void UserDation::suspend(TaskCommon * ioPerformingTask) {
        systemDation->suspend(ioPerformingTask);
    }

    void UserDation::terminate(TaskCommon * ioPerformingTask) {
        systemDation->terminate(ioPerformingTask);
    }


    Dation::DationParams UserDation::getCurrentDirection() {
        return currentDirection;
    }

    void UserDation::checkOpenParametersAndIncrementCounter(int parameters,
            RefCharacter * newFilename,
            SystemDation * parent) {
        static Fixed<31> one(1);
        int systemDationParams;

        // enshure default values
        if ((parameters & (OLD|NEW|ANY))==0 ) {
           parameters |= ANY;
        }
        if ((parameters & (PRM|CAN))==0 ) {
           parameters |= PRM;
        }
      

        if (dationStatus == CLOSED) {
            dationParams &= ~(OPENMASK|CLOSEMASK); // reset open and close flags
            counter.x = 0;
            idfNameGiven = false;

            // check towards the system dation characteristics
            systemDationParams = parent->capabilities();
            if (systemDationParams & IDF) {
                if ((parameters & IDF)==0) {
                    if (parameters & ANY)  {
                        idfNameGiven=false;
                    } else {
                        Log::error("UserDation: system dation requires IDF but was not given");
                        throw theDationParamSignal;
                    }
                }
            }
            if ((systemDationParams & IDF)==0) {
                if ((parameters & IDF)!=0) {
                    Log::error("UserDation: system dation does not support IDF but IDF was given");
                    throw theDationParamSignal;
                }
            }

            if ((systemDationParams & OLD)==0) {
                if ((parameters & OLD)!=0) {
                    Log::error("UserDation: system dation does not support OLD but OLD was given");
                    throw theDationParamSignal;
                }
            }
            if ((systemDationParams & NEW)==0) {
                if ((parameters & NEW)!=0) {
                    Log::error("UserDation: system dation does not support NEW but NEW was given");
                    throw theDationParamSignal;
                }
            }
            if ((systemDationParams & CAN)==0) {
                if ((parameters & CAN)!=0) {
                    Log::error("UserDation: system dation does not support CAN but CAN was given");
                    throw theDationParamSignal;
                }
            }
            // no need to check ANY, because this is always possible

            // seams to be ok --> memorize the current openParameters
            // is done at the end of this function

        } else {
            // dation already opened -->
            // check versus prior OPEN statements
            if (parameters & IDF) {
                if ((dationParams & OPENIDF)) {
                    // check filename

                    if (!(newFilename->equal(&idfName))) {
                        Log::error("UserDation: Dation already open, IDF name differs");
                        throw theOpenFailedSignal;
                    }
                } else {
                    Log::error("UserDation: Dation already opened without IDF name, which conflicts with IDF");
                    throw theOpenFailedSignal;
                }
            } else {
                if (dationParams & OPENIDF) {
                    Log::error("UserDation: Dation already opened with IDF name, which conflicts with missing IDF");
                    throw theOpenFailedSignal;
                }
            }


            if (dationParams & OPENNEW) {
                if (parameters & OLD) {
                    Log::error("UserDation: dation was previously opened with NEW, which conflicts with OLD");
                    throw theDationParamSignal;
                }
                if (parameters & ANY) {
                    Log::error("UserDation: dation was previously opened with NEW, which conflicts with ANY");
                    throw theDationParamSignal;
                }
            }
            if (dationParams & OPENOLD) {
                if (parameters & NEW) {
                    Log::error("UserDation: dation was previously opened with OLD, which conflicts with NEW");
                    throw theDationParamSignal;
                }
                if (parameters & ANY) {
                    Log::error("UserDation: dation was previously opened with OLD, which conflicts with ANY");
                    throw theDationParamSignal;
                }
            }
            if (dationParams & OPENANY) {
                if (parameters & NEW) {
                    Log::error("UserDation: dation was previously opened with ANY, which conflicts with NEW");
                    throw theDationParamSignal;
                }
                if (parameters & OLD) {
                    Log::error("UserDation: dation was previously opened with ANY, which conflicts with OLD");
                    throw theDationParamSignal;
                }
            }

            if (dationParams & OPENPRM) {
                if (parameters & CAN) {
                    Log::error("UserDation: dation was previously opened with PRM, which conflicts with CAN");
                    throw theDationParamSignal;
                }
            }
            if (dationParams & OPENCAN) {
                if (parameters & PRM) {
                    Log::error("UserDation: dation was previously opened with CAN, which conflicts with PRM");
                    throw theDationParamSignal;
                }
            }
        }


        // seams to be fine up to now!
        // memorize the current flags in OPEN...-flags
        if (parameters & CAN) {
            dationParams |= OPENCAN;
        } else {
            dationParams |= OPENPRM;
        }

        if (parameters & OLD) {
            dationParams |= OPENOLD | OLD;
        }
        if (parameters & NEW) {
            dationParams |= OPENNEW | NEW;
        }
        if ((parameters & (OLD|NEW)) == 0) {
            dationParams |= (OPENANY|ANY);   // may be also an implicit ANY
        }
        if (parameters & IDF)  {
            dationParams |= OPENIDF | IDF;
            if (newFilename != NULL) {
                idfNameGiven = true;
                idfName.setCurrent(newFilename->getCurrent().x);
                newFilename->store(idfNameStorage);
            }
        } else {
            dationParams |= OPENNOIDF;
        }

        // fine, the dation is opened again with the same parameters
        counter = counter + one;

    }

    void UserDation::checkCloseParametersAndDecrementCounter(int parameters,
            SystemDation * parent) {
        static Fixed<31> one(1);
        int systemDationParams;

        // check towards the system dation characteristics
        systemDationParams = parent->capabilities();
        if ((systemDationParams & CAN)==0) {
            if ((parameters & CAN)!=0) {
                Log::error("UserDation: system dation does not support CAN but CAN was given");
                throw theDationParamSignal;
            }
        }

        // check versus prior CLOSE statements if counter > 0
        if (counter.x > 0) {
            if (dationParams & CLOSECAN) {
                if (parameters & PRM) {
                    Log::error("UserDation: dation was previously closed with CAN, which conflicts with PRM");
                    throw theDationParamSignal;
                }
            }
            if (dationParams & CLOSEPRM) {
                if (parameters & CAN) {
                    Log::error("UserDation: dation was previously closed with PRM, which conflicts with CAN");
                    throw theDationParamSignal;
                }
            }

            if (parameters & CAN) {
                dationParams |= CLOSECAN;
            }
            if (parameters & PRM) {
                dationParams |= CLOSEPRM;
            }
            counter = counter - one;
            // do the close on the system dation in the calling routine
        }
    }
}
