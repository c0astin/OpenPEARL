/*
 [A "BSD license"]
 Copyright (c) 2020      Rainer Mueller
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

#include "Character.h"
#include "Mutex.h"

namespace pearlrt {

/**
 \brief Implementation of Esp32MqttTcpClient 

  Only 1 instance is allowed. 
*/
   class Esp32MqttTcpClient {
    public:
      Esp32MqttTcpClient(char* brokerIp, uint32_t port);
      static Esp32MqttTcpClient* getInstance();
      void publish(Character<40> topic, Character<40> data);
      void subscribe(Character<40> topic);
      void readMessage(Character<40> & topic, Character<40> & data);
      void setClient(void* esp_mqtt_client_handle);
      uint32_t getPort();
      void received(size_t tl, char* t, size_t dl, char*d);
    private:
      static Esp32MqttTcpClient* instance;
      // real type is esp_mqtt_client_handle_t, which is a pointer 
      void * client;
      uint32_t brokerPort;
      char* receiveTopic;
      size_t receiveTopicLength;
      char* receiveData;
      size_t receiveDataLength;
      Mutex mutex;
   };
}



