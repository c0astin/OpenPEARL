/*
 [The "BSD license"]
 Copyright (c) 2012-2013 Rainer Mueller
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

\brief list of header file

The compiler does not needed to know all header files of the run time system.
Only this file mus be included.

*/

#include "TaskCommon.h"
#include "Task.h"
//#include "GenericTask.h"
#include "TaskTimer.h"
#include "TaskTimerCommon.h"
#include "TaskMonitor.h"
#include "Clock.h"
#include "PutClock.h"
#include "GetClock.h"

#include "Duration.h"
#include "PutDuration.h"
#include "GetDuration.h"

#include "Interrupt.h"

#include "Fixed.h"
#include "PutFixed.h"
#include "GetFixed.h"

#include "Fixed63.h"
#include "Character.h"
#include "PutCharacter.h"
#include "GetCharacter.h"

#include "Ref.h"
#include "RefChar.h"
#include "ScheduleSignalAction.h"
#include "Prio.h"
#include "BitString.h"
#include "PutBitString.h"
#include "GetBitString.h"
#include "RefCharSink.h"
#include "Signals.h"
#include "Device.h"
#include "SystemDationNB.h"
#include "SystemDationB.h"
#include "UserDation.h"
#include "DationPG.h"
#include "DationRW.h"
#include "DationTS.h"
#include "StringDationConvert.h"
#include "UserDationNB.h"
#include "DationDim.h"
#include "DationDim1.h"
#include "DationDim2.h"
#include "DationDim3.h"
#include "TFURecord.h"
#include "Semaphore.h"
#include "Bolt.h"

#include "SystemDationNBSink.h"
#include "SystemDationNBSource.h"

#include "Control.h"
#include "SendXML.h"

//#include "StdOut.h"
#include "SoftInt.h"

#include "Float.h"
#include "compare.h"

#include "SampleBasicDation.h"

#include "Array.h"

//#include "Esp32BME280.h"
//#include "Esp32Wifi.h"
//#include "TcpIpServer.h"

#include "Console.h"
#include "StdOut.h"
#include "Esp32Uart.h"
#include "Esp32WifiConfig.h"
#include "Esp32MqttTcpClient.h"
#include "Esp32I2CBus.h"

#include "BME280.h"
#include "PCA9685.h"
#include "PCA9685Channel.h"
#include "LM75.h"
#include "ADS1015SE.h"
#include "PCF8574In.h"
#include "PCF8574Out.h"

#include "DynamicDeadlockDetection/DeadlockOperation.h"
#include "DynamicDeadlockDetection/PerformedDeadlockOperation.h"
#include "DynamicDeadlockDetection/Graph.h"
#include "DynamicDeadlockDetection/ResourceAllocationGraph.h"
#include "DynamicDeadlockDetection/DynamicDeadlockDetection.h"

