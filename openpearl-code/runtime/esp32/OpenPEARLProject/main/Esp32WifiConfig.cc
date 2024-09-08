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
#include "esp_event.h"
#include "esp_log.h"

#include "lwip/err.h"
#include "lwip/sys.h"
//}
#include "Log.h"
#include "Esp32Nvs.h"

/* The event group allows multiple bits for each event,
   but we only care about one event - are we connected
   to the AP with an IP? */

#define WIFI_CONNECTED_BIT BIT0
#define WIFI_FAIL_BIT      BIT1

static int s_retry_num = 0;
#define EXAMPLE_ESP_MAXIMUM_RETRY  10

static EventGroupHandle_t s_wifi_event_group;

static const char *TAG = "wifi station";

static bool connected=false;
static      char passwd[64];
static      char ssid[64];

static void event_handler(void* arg, esp_event_base_t event_base,
                                int32_t event_id, void* event_data)
{
printf("event_handler: got event \n");
    if (event_base == WIFI_EVENT && event_id == WIFI_EVENT_STA_START) {
printf("STA_START received: --> connect\n");
        esp_wifi_connect();
    } else if (event_base == WIFI_EVENT && event_id == WIFI_EVENT_STA_DISCONNECTED) {
printf("STA_DISCONNETCED received: --> retry?\n");
        if (s_retry_num < EXAMPLE_ESP_MAXIMUM_RETRY) {
            esp_wifi_connect();
            s_retry_num++;
            ESP_LOGI(TAG, "retry to connect to the AP");
        } else {
            xEventGroupSetBits(s_wifi_event_group, WIFI_FAIL_BIT);
        }
        ESP_LOGI(TAG,"connect to the AP fail");
    } else if (event_base == IP_EVENT && event_id == IP_EVENT_STA_GOT_IP) {
        ip_event_got_ip_t* event = (ip_event_got_ip_t*) event_data;
        ESP_LOGI(TAG, "got ip:" IPSTR, IP2STR(&event->ip_info.ip));
        s_retry_num = 0;
        xEventGroupSetBits(s_wifi_event_group, WIFI_CONNECTED_BIT);
    }
}

static void readLine(char * line, size_t length) {
  printf("readline started\n");
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
   printf("readline done >%s<\n", line);
}


static void wifiTask(void * parameters) {
   char yesNo[4];
   bool tryAgain = true;

   printf("wifiTask started \n");
   pearlrt::Esp32Nvs* nvs = pearlrt::Esp32Nvs::getInstance();
   
   printf("wifiTask @1 \n");

   s_wifi_event_group = xEventGroupCreate();
   printf("wifiTask @1.1 \n");
   ESP_ERROR_CHECK(esp_netif_init());
   printf("wifiTask @1.2 \n");

    ESP_ERROR_CHECK(esp_event_loop_create_default());
   printf("wifiTask @1.3 \n");
    esp_netif_create_default_wifi_sta();
   printf("wifiTask @1.4 \n");

    wifi_init_config_t cfg = WIFI_INIT_CONFIG_DEFAULT();
   printf("wifiTask @1.5 \n");
    ESP_ERROR_CHECK(esp_wifi_init(&cfg));
   printf("wifiTask @1.6 \n");

   esp_event_handler_instance_t instance_any_id;
    esp_event_handler_instance_t instance_got_ip;
   printf("wifiTask @1.6 \n");
    ESP_ERROR_CHECK(esp_event_handler_instance_register(WIFI_EVENT,
                                                        ESP_EVENT_ANY_ID,
                                                        &event_handler,
                                                        NULL,
                                                        &instance_any_id));
   printf("wifiTask @1.7 \n");
    ESP_ERROR_CHECK(esp_event_handler_instance_register(IP_EVENT,
                                                        IP_EVENT_STA_GOT_IP,
                                                        &event_handler,
                                                        NULL,
                                                        &instance_got_ip));



   printf("wifiTask @2 \n");
   do {
      if (strlen(ssid) == 0) {
          printf("Enter SSID:");
          readLine(ssid,sizeof(ssid));
          printf("\nEnter Password:");
          readLine(passwd,sizeof(passwd));
      }
      printf("\ntry to connect to SSID: >%s< with password: >%s<\n",
             ssid, passwd);
     // wifi_init_config_t cfg = WIFI_INIT_CONFIG_DEFAULT();
      //ESP_ERROR_CHECK(esp_wifi_init(&cfg));

      wifi_config_t wifi_config;
      memset(&wifi_config, 0, sizeof(wifi_config));
      memcpy(wifi_config.sta.ssid, ssid, strlen(ssid));
      memcpy(wifi_config.sta.password, passwd, strlen(passwd));
     wifi_config.sta.threshold.authmode = WIFI_AUTH_WPA2_PSK;
     wifi_config.sta.pmf_cfg.capable = true;
     wifi_config.sta.pmf_cfg.required = false;

printf("set_mode()\n");
      ESP_ERROR_CHECK(esp_wifi_set_mode(WIFI_MODE_STA) );
printf("set_config\n");
      ESP_ERROR_CHECK(esp_wifi_set_config(ESP_IF_WIFI_STA, &wifi_config) );
printf("start\n");
      ESP_ERROR_CHECK(esp_wifi_start() );
printf("started\n");
   /* Waiting until either the connection is established (WIFI_CONNECTED_BIT) or connection failed for the maximum
     * number of re-tries (WIFI_FAIL_BIT). The bits are set by event_handler() (see above) */
    EventBits_t bits = xEventGroupWaitBits(s_wifi_event_group,
            WIFI_CONNECTED_BIT | WIFI_FAIL_BIT,
            pdFALSE,
            pdFALSE,
            portMAX_DELAY);
printf("got EventBits: %x\n", bits);
    /* xEventGroupWaitBits() returns the bits before the call returned, hence we can test which event actually
     * happened. */
    connected = 0;
    if (bits & WIFI_CONNECTED_BIT) {
        ESP_LOGI(TAG, "connected to ap SSID:%s password:%s",
                 ssid, passwd);
        connected = 1;
    } else if (bits & WIFI_FAIL_BIT) {
        ESP_LOGI(TAG, "Failed to connect to SSID:%s, password:%s",
                 ssid, passwd);
    } else {
        ESP_LOGE(TAG, "UNEXPECTED EVENT");
    }



    /* The event will not be processed after unregister */
    ESP_ERROR_CHECK(esp_event_handler_instance_unregister(IP_EVENT, IP_EVENT_STA_GOT_IP, instance_got_ip));
    ESP_ERROR_CHECK(esp_event_handler_instance_unregister(WIFI_EVENT, ESP_EVENT_ANY_ID, instance_any_id));
    vEventGroupDelete(s_wifi_event_group);

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

   Esp32WifiConfig::Esp32WifiConfig(char* sid, char* pwd) {
printf("Esp32WifiConfig: >%s<: >%s<\n", sid, pwd);
//Log::info("Esp32WifiConfig: %s: %s", sid, pwd);
     strncpy(ssid,sid,sizeof(ssid)-1);
     strncpy(passwd,pwd,sizeof(passwd)-1);
     if (ssid[0]==' ') {
        ssid[0] = '\0';
     }
printf("Esp32WifiConfig: >%s<: >%s<", ssid, passwd);
//Log::info("Esp32WifiConfig: %s: %s", ssid, passwd);

     
 printf(" try to start wifiTask\n");
      xTaskCreate(&wifiTaskWrapper, "wifiTask",
                  5000, //configMINIMAL_STACK_SIZE,
                  NULL, 5, NULL);
 printf(" try to start wifiTask done\n");
    
   }
}



