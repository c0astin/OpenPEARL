# ----
FUNCTION(PREPEND var prefix)
  SET(listvar "")
  FOREACH(f ${ARGN})
     LIST(APPEND listvar "${prefix}/$f")
  ENDFOREACH(f)
  SET(${var} "${listvar}" PARENT_SCOPE)
ENDFUNCTION(PREPEND)

#--------------
set(include_dirs
   common 
   PEARL 
   addOns
   .)

set(srcs
    testprog.cc
    system.cc
    # ---
    main.cc
    Log.cc
    Esp32I2CBus.cc
    Esp32Clock.cc
    Esp32MqttTcpClient.cc
    Esp32WifiConfig.cc
    Esp32Nvs.cc
    StdOut.cc
    # ---
    PEARL/CSema.cc
    PEARL/Mutex.cc
    PEARL/PrioMapper.cc
    PEARL/Task.cc 
    PEARL/TaskMonitor.cc
    PEARL/TaskTimer.cc
    #PEARL/GenericUart.cc 
    # ---
    common/Array.cc 
    common/Bolt.cc 
    common/LogFile.cc common/Log.cc
    common/Clock.cc  common/PutClock.cc common/GetClock.cc 
    common/Duration.cc common/PutDuration.cc common/GetDuration.cc 
    common/Signals.cc 
    common/ScheduleSignalAction.cc 
    common/Fixed63.cc common/Fixed.cc 
    common/Prio.cc 
    common/CharSlice.cc common/Character.cc common/RefChar.cc 
    common/compare.cc 
    common/Interrupt.cc 
    common/Source.cc 
    common/RefCharSink.cc common/RefCharSource.cc 
    common/SystemDationNBSource.cc common/SystemDationNBSink.cc 
    common/PutBitString.cc 
    common/GetBitString.cc 
    common/GetHelper.cc common/PutHelper.cc 
    common/TaskWhenLinks.cc 
    common/TaskCommon.cc 
    common/TaskList.cc 
    common/TaskMonitor.cc 
    common/TaskTimerCommon.cc 
    common/MutexCommon.cc common/CSemaCommon.cc 
    common/Semaphore.cc common/PriorityQueue.cc 
    common/Rst.cc 
    common/SystemDation.cc
    common/SystemDationNB.cc 
    common/DationRW.cc 
    common/IOFormats.cc 
    common/IOJob.cc 
    common/DationPG.cc
    common/StringDationConvert.cc
    common/DationTS.cc
    common/UserDationNB.cc common/UserDation.cc 
    common/TFUBuffer.cc 
    common/DationDim.cc  common/DationDim1.cc common/DationDim2.cc common/DationDim3.cc
    common/FloatHelper.cc 
    common/SoftInt.cc 
    common/Control.cc 
    common/ConsoleCommon.cc
    common/FullDuplexDationAbortNB.cc
    common/SampleBasicDation.cc
    common/LM75.cc
    common/SendXML.cc
    common/DynamicDeadlockDetection/DynamicDeadlockDetection.cc 
    common/DynamicDeadlockDetection/DeadlockOperation.cc 
    common/DynamicDeadlockDetection/PerformedDeadlockOperation.cc 
    common/DynamicDeadlockDetection/Graph.cc 
    common/DynamicDeadlockDetection/ResourceAllocationGraph.cc

    # ---
    addOns/timer.c
    addOns/service.c
    addOns/clock.c
    addOns/FreeRTOSClock.cc
    addOns/testStackOverflow.cc
)


idf_component_register(
   SRCS  "${srcs}"
   INCLUDE_DIRS ${include_dirs}
   REQUIRES freertos mqtt nvs_flash 
)

