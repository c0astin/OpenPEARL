/**
  * UART Comms Header
  * Author: Patrick Scherer
  *
  * Provides a quick overview on which API functions can be used from outside the source file.
  */

  /*
   [The "BSD license"]
   Copyright (c) 2018-2019 Patrick Scherer
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

   THIS SOFTWARE IS PROVIDED BY THE AUTHOR ''AS IS'' AND ANY EXPRESS OR
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


  #ifndef __UARTCOMMS_H__
  #define __UARTCOMMS_H__

  #include "esp_system.h"
  #include "freertos/FreeRTOS.h"
  #include "freertos/semphr.h"

  extern QueueHandle_t xQueueRX;  //Non-static for inputProcessing.c, can be wrapped later

  void sendSingleUartCharacter(uint8_t charToSend);
  void sendMultipleUartCharacters(uint8_t* charsToSend, uint8_t itemCount);
  uint8_t receiveSingleUartCharacter();

  bool setupUartComms();
  bool enableUartComms();
  bool disableUartComms();

  #endif /* __UARTCOMMS_H__ */
