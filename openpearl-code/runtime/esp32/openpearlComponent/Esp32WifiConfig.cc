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

#include "Esp32WifiConfig.h"

//extern "C" {
#include <string.h>
#include "freertos/FreeRTOS.h"
#include "freertos/task.h"
#include "freertos/event_groups.h"
#include "esp_system.h"
#include "esp_wifi.h"
#include "esp_event_loop.h"
#include "esp_log.h"

#include "lwip/err.h"
#include "lwip/sys.h"
//}
#include "Log.h"
#include "Esp32Nvs.h"

/* FreeRTOS event group to signal when we are connected*/
static EventGroupHandle_t wifi_event_group;

/* The event group allows multiple bits for each event,
   but we only care about one event - are we connected
   to the AP with an IP? */
const int WIFI_CONNECTED_BIT = BIT0;

static bool connected=false;
static      char passwd[64];
static      char ssid[64];

static esp_err_t event_handler(void *ctx, system_event_t *event)
{
    switch(event->event_id) {
    case SYSTEM_EVENT_STA_START:
        printf("SYSTEM_EVENT_STA_STARTED\n");
        break;
    case SYSTEM_EVENT_STA_GOT_IP:
        printf("got ip:%s\n",
                 ip4addr_ntoa(&event->event_info.got_ip.ip_info.ip));
        xEventGroupSetBits(wifi_event_group, WIFI_CONNECTED_BIT);
        connected = true;
        break;
    case SYSTEM_EVENT_AP_STACONNECTED:
        printf("station:" MACSTR " join, AID=%d\n",
                 MAC2STR(event->event_info.sta_connected.mac),
                 event->event_info.sta_connected.aid);
        break;
    case SYSTEM_EVENT_AP_STADISCONNECTED:
        printf("station:" MACSTR "leave, AID=%d\n",
                 MAC2STR(event->event_info.sta_disconnected.mac),
                 event->event_info.sta_disconnected.aid);
        break;
    case SYSTEM_EVENT_STA_DISCONNECTED:
        printf("DISCONNECTED\n");
        esp_wifi_connect();
        xEventGroupClearBits(wifi_event_group, WIFI_CONNECTED_BIT);
        break;
    default:
        break;
    }
    return ESP_OK;
}

static void readLine(char * line, size_t length) {
      int count = 0;
      line[0] = '\0';
        while (count < length-1) {
            int c = fgetc(stdin);
            if (c == '\n') {
                line[count] = '\0';
                break;
            } else if (c > 0 && c < 127) {
                line[count] = c;
                line[count+1] = c;
                ++count;
                printf("%c",c);
            }
            vTaskDelay(10 / portTICK_PERIOD_MS);
        }
}

static void wifiTask(void * parameters) {
   char yesNo[4];
   bool tryAgain = true;

   pearlrt::Esp32Nvs* nvs = pearlrt::Esp32Nvs::getInstance();

   printf("wifiTask started\n");

     
   wifi_event_group = xEventGroupCreate();

printf("tctip_init()\n");
   tcpip_adapter_init();
   ESP_ERROR_CHECK(esp_event_loop_init(event_handler, NULL) );

   do {
      if (strlen(ssid) == 0) {
          printf("Enter SSID:");
          readLine(ssid,sizeof(ssid));
          printf("\nEnter Password:");
          readLine(passwd,sizeof(passwd));
      }
      printf("\ntry to connect to SSID: >%s< with password: >%s<\n",
             ssid, passwd);
      wifi_init_config_t cfg = WIFI_INIT_CONFIG_DEFAULT();
      ESP_ERROR_CHECK(esp_wifi_init(&cfg));

      wifi_config_t wifi_config;
      memset(&wifi_config, 0, sizeof(wifi_config));
      memcpy(wifi_config.sta.ssid, ssid, strlen(ssid));
      memcpy(wifi_config.sta.password, passwd, strlen(passwd));
      wifi_config.sta.bssid_set = 0;

printf("set_mode()\n");
      ESP_ERROR_CHECK(esp_wifi_set_mode(WIFI_MODE_STA) );
printf("set_config\n");
      ESP_ERROR_CHECK(esp_wifi_set_config(ESP_IF_WIFI_STA, &wifi_config) );
printf("start\n");
      ESP_ERROR_CHECK(esp_wifi_start() );
printf("connect\n");
      ESP_ERROR_CHECK(esp_wifi_connect());

 printf("wifi_init_sta finished.\n");

      printf("wait 10 seconds to connect\n"); 
      // Wait for successful connection
      for (int i=0; i< 10 && !connected; i++) {
         vTaskDelay(1000/portTICK_PERIOD_MS);
      }
      if (connected) {
         printf("connected! save parameters in NVS? (y/n)");
         readLine(yesNo,sizeof(yesNo));
         if (yesNo[0] == 'y' || yesNo[0] == 'Y') {
            printf("save parameters NVS\n");
            nvs->setItem((char*)"wifi_SSID", ssid);
            nvs->setItem((char*)"wifi_PASS", passwd);
            nvs->commit();
         }
         tryAgain = false;
      } else {
         esp_wifi_stop();
         printf("not connected!\ntry again? (y/n)");
         readLine(yesNo,sizeof(yesNo));
         if (yesNo[0] != 'y' && yesNo[0] != 'Y') {
            tryAgain = false;
         }
         ssid[0] = '\0';  // invalidate ssid to enable dialog
      }
   }
   while(tryAgain);
 
   printf("wait for RESET or new application\n"); 
   while(1) {        
      vTaskDelay(1);
   }
}

static void wifiTaskWrapper(void * parameters) {
   wifiTask(parameters);
}
    

/**
 \brief Implementation of Esp32WifiConfiguration 

  This module tries to connect to an access point 
  and writes the access parameter in the non volatile storage (nvs)
*/

namespace pearlrt {
#define STACK_SIZE 1000
static uint32_t  stack[STACK_SIZE/4];
static StaticTask_t tcb;

   Esp32WifiConfig::Esp32WifiConfig(char* sid, char* pwd) {
     TaskHandle_t tskHandle;
Log::info("Esp32WifiConfig: %s: %s", sid, pwd);
     strncpy(ssid,sid,sizeof(ssid)-1);
     strncpy(passwd,pwd,sizeof(passwd)-1);
Log::info("Esp32WifiConfig: %s: %s", ssid, passwd);

     
#if 0
     boolean ssidPasswdFound = true; 
     ssid[0] = 0;
     Esp32Nvs* nvs = Esp32Nvs::getInstance();
     try {
        nvs->getItem((char*)"ssid",ssid,sizeof(ssid));
     } catch(Signal &s) {
        printf("*** got nothing for ssid ***\n");
     };
     printf("got >%s<\n", ssid);
     nvs->setItem((char*)"ssid",(char*)"OpenPEARL");
     nvs->getItem((char*)"ssid",ssid,sizeof(ssid));
     printf("got >%s<\n", ssid);
 
     nvs->getItem((char*)"ssid",ssid,sizeof(passwd));
     printf("got >%s<\n", passwd);
#endif
 printf(" try to start wifiTask\n");
      xTaskCreate(&wifiTaskWrapper, "wifiTask",
                  5000, //configMINIMAL_STACK_SIZE,
                  NULL, 5, NULL);
/*
     tskHandle=xTaskCreateStatic(&wifiTaskWrapper,NULL,STACK_SIZE,NULL,10,
         (StackType_t*)stack,&tcb);
*/
 printf(" try to start wifiTask done\n");
    
   }
}



