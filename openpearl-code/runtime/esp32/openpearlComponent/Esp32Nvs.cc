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

#include <cstddef>  // size_t
#include "Esp32Nvs.h"

//#include <string.h>
#include "freertos/FreeRTOS.h"
#include "freertos/task.h"
//#include "freertos/event_groups.h"
//#include "esp_system.h"
//#include "esp_wifi.h"
//#include "esp_event_loop.h"
//#include "esp_log.h"
#include "nvs_flash.h"

//#include "lwip/err.h"
//#include "lwip/sys.h"
//}
#include "Log.h"
#include "Signals.h"

/**
 \brief acess function to the non volatile storage in the esp32

  This module provides access to the namespace 'OpenPEARL'
*/

namespace pearlrt {
  Esp32Nvs* Esp32Nvs::instance  = 0;
  Esp32Nvs::Esp32Nvs() {

      assert(sizeof(nvs_handle) == sizeof(nvs));

      // Initialize NVS
      esp_err_t err = nvs_flash_init();
      if (err == ESP_ERR_NVS_NO_FREE_PAGES || err == ESP_ERR_NVS_NEW_VERSION_FOUND) {
        // NVS partition was truncated and needs to be erased
        // Retry nvs_flash_init
        ESP_ERROR_CHECK(nvs_flash_erase());
        err = nvs_flash_init();
      }
      ESP_ERROR_CHECK( err );
   
    //printf("Opening Non-Volatile Storage (NVS) handle... ");
    err = nvs_open("OpenPEARL", NVS_READWRITE, &nvs);
      printf("open returns %d\n", err);
    if (err != ESP_OK) {
        Log::error("Esp32Nvs: could not open namespace (%s)",
           esp_err_to_name(err));
           throw theInternalSignal;
    }
  }

 
  Esp32Nvs* Esp32Nvs::getInstance() {
     if (!instance) {
        instance = new Esp32Nvs();
     }
     return instance;
  }

  void Esp32Nvs::getItem(char* item, char* data, size_t maxLength) {
     size_t dataLength = maxLength; 
     *data = 0;  // clear data
     esp_err_t err = nvs_get_str(nvs, item, data, &dataLength);

     switch (err) {
            case ESP_OK:
                // Log::info("Esp32Nvs: %s is %s", item, data);
                break;
            case ESP_ERR_NVS_NOT_FOUND:
                Log::info("Esp32Nvs: item %s is not initialized yet!",
                          item);
                throw theInternalSignal;
            case ESP_ERR_NVS_INVALID_LENGTH:
                Log::error("Esp32Nvs: data for item %s longer than %d (%d)",
                  item, maxLength, dataLength);
                throw theInternalSignal;

            default :
                Log::error("Esp32Nvs: error: reading item %s:%s ",
                    item,  esp_err_to_name(err));
           throw theInternalSignal;
        }
      }

  void Esp32Nvs::setItem(char* item, char* data) {
     esp_err_t err = nvs_set_str(nvs, item, data);
     switch (err) {
            case ESP_OK:
                // Log::info("Esp32Nvs: %s is now %s", item, data);
                break;

            default :
                Log::error("Esp32Nvs: error: writing item %s:%s ",
                    item,  esp_err_to_name(err));
           throw theInternalSignal;
       }
    }

   void Esp32Nvs::commit(void) {
        esp_err_t err = nvs_commit(nvs);
        if (err != ESP_OK) {
           Log::error("Esp32Nvs: commit failed: %s", esp_err_to_name(err));
           throw theInternalSignal;
        }
   }

}



