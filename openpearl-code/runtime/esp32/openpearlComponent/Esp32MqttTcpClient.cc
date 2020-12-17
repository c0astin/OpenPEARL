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

#include "Esp32MqttTcpClient.h"

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
#include "mqtt_client.h"

#include "Log.h"
#include "Esp32Nvs.h"

/* FreeRTOS event group to signal when we are connected*/
static EventGroupHandle_t event_group;
static const char *TAG = "MQTT_EXAMPLE";

/* The event group allows multiple bits for each event,
   but we only care about one event - are we connected
   to the AP with an IP? */
const static int CONNECTED_BIT = BIT0;
const static int RECEIVED_MQTT_MSG_BIT = BIT1;

static bool connected=false;

static esp_err_t wifi_event_handler(void *ctx, system_event_t *event)
{
    switch(event->event_id) {
    case SYSTEM_EVENT_STA_START:
        printf("SYSTEM_EVENT_STA_STARTED\n");
        esp_wifi_connect();
        break;
    case SYSTEM_EVENT_STA_GOT_IP:
        pearlrt::Log::info("got ip:%s",
                 ip4addr_ntoa(&event->event_info.got_ip.ip_info.ip));
        xEventGroupSetBits(event_group, CONNECTED_BIT);
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
        connected = false;
        //printf("DISCONNECTED\n");
        esp_wifi_connect();
        xEventGroupClearBits(event_group, CONNECTED_BIT);
        break;
    default:
        break;
    }
    return ESP_OK;
}

static esp_err_t mqtt_event_handler(esp_mqtt_event_handle_t event)
{
    //esp_mqtt_client_handle_t client = event->client;
    //int msg_id;
    // your_context_t *context = event->context;
    //printf("mqtt_event_handler: id=%d client=%p\n", event->event_id,client);
    switch (event->event_id) {
        case MQTT_EVENT_CONNECTED:
            ESP_LOGI(TAG, "MQTT_EVENT_CONNECTED");
/*
            msg_id = esp_mqtt_client_publish(client, "/topic/qos1", "data_3", 0, 1, 0);
            ESP_LOGI(TAG, "sent publish successful, msg_id=%d", msg_id);

            msg_id = esp_mqtt_client_subscribe(client, "/topic/qos0", 0);
            ESP_LOGI(TAG, "sent subscribe successful, msg_id=%d", msg_id);

            msg_id = esp_mqtt_client_subscribe(client, "/topic/qos1", 1);
            ESP_LOGI(TAG, "sent subscribe successful, msg_id=%d", msg_id);

            msg_id = esp_mqtt_client_unsubscribe(client, "/topic/qos1");
            ESP_LOGI(TAG, "sent unsubscribe successful, msg_id=%d", msg_id);
*/
            break;
        case MQTT_EVENT_DISCONNECTED:
            ESP_LOGI(TAG, "MQTT_EVENT_DISCONNECTED");
            break;
        case MQTT_EVENT_SUBSCRIBED:
/*            ESP_LOGI(TAG, "MQTT_EVENT_SUBSCRIBED, msg_id=%d", event->msg_id);
            msg_id = esp_mqtt_client_publish(client, "/topic/qos0", "data", 0, 0, 0);
            ESP_LOGI(TAG, "sent publish successful, msg_id=%d", msg_id);
*/
            break;
        case MQTT_EVENT_UNSUBSCRIBED:
            ESP_LOGI(TAG, "MQTT_EVENT_UNSUBSCRIBED, msg_id=%d", event->msg_id);
            break;
        case MQTT_EVENT_PUBLISHED:
            ESP_LOGI(TAG, "MQTT_EVENT_PUBLISHED, msg_id=%d", event->msg_id);
            break;
        case MQTT_EVENT_DATA:
            ESP_LOGI(TAG, "MQTT_EVENT_DATA");
    pearlrt::Esp32MqttTcpClient::getInstance()->
            received(
             event->topic_len, event->topic, 
             event->data_len, event->data);
            //printf("TOPIC=%.*s\r\n", event->topic_len, event->topic);
            //printf("DATA=%.*s\r\n", event->data_len, event->data);

            break;
        case MQTT_EVENT_ERROR:
            ESP_LOGI(TAG, "MQTT_EVENT_ERROR");
            break;
        case MQTT_EVENT_BEFORE_CONNECT:
	    ESP_LOGI(TAG,"MQTT_EVENT_BEFORE_CONNECT");
            break;
        default:
            ESP_LOGI(TAG, "Other event id:%d", event->event_id);
            break;
    }
    return ESP_OK;
}




static void wifiTask(char * brokerIp) {
   char ssid[32];
   char passwd[32];

//   printf("wifiTask started\n");

//   ESP_LOGI(TAG, "[APP] Startup..");
//   ESP_LOGI(TAG, "[APP] Free memory: %d bytes", esp_get_free_heap_size());
//   ESP_LOGI(TAG, "[APP] IDF version: %s", esp_get_idf_version());

   esp_log_level_set("*", ESP_LOG_ERROR);
   esp_log_level_set("wifi", ESP_LOG_WARN); 
   esp_log_level_set("MQTT_CLIENT", ESP_LOG_INFO);
//   esp_log_level_set("TRANSPORT_TCP", ESP_LOG_VERBOSE);
//   esp_log_level_set("TRANSPORT_SSL", ESP_LOG_VERBOSE);
//   esp_log_level_set("TRANSPORT", ESP_LOG_VERBOSE);
//   esp_log_level_set("OUTBOX", ESP_LOG_VERBOSE);

   pearlrt::Esp32Nvs* nvs = pearlrt::Esp32Nvs::getInstance();

   tcpip_adapter_init();

   event_group = xEventGroupCreate();

   ESP_ERROR_CHECK(esp_event_loop_init(wifi_event_handler, NULL) );

     nvs->getItem((char*)"wifi_SSID", ssid,sizeof(ssid));
     nvs->getItem((char*)"wifi_PASS", passwd, sizeof(passwd));
     //printf("try to connect to SSID: >%s< with password: >%s<\n",
     //        ssid, passwd);

      wifi_init_config_t cfg = WIFI_INIT_CONFIG_DEFAULT();
      ESP_ERROR_CHECK(esp_wifi_init(&cfg));

      ESP_ERROR_CHECK(esp_wifi_set_storage(WIFI_STORAGE_RAM));

      wifi_config_t wifi_config;
      memset(&wifi_config, 0, sizeof(wifi_config));
      memcpy(wifi_config.sta.ssid, ssid, strlen(ssid));
      memcpy(wifi_config.sta.password, passwd, strlen(passwd));
      wifi_config.sta.bssid_set = 0;

      ESP_ERROR_CHECK(esp_wifi_set_mode(WIFI_MODE_STA) );
      ESP_ERROR_CHECK(esp_wifi_set_config(ESP_IF_WIFI_STA, &wifi_config) );
      ESP_ERROR_CHECK(esp_wifi_start() );
      ESP_LOGI(TAG, "Waiting for wifi");
    xEventGroupWaitBits(event_group, CONNECTED_BIT, false, true, portMAX_DELAY);

    esp_mqtt_client_config_t mqtt_cfg;
    memset(&mqtt_cfg,0,sizeof(mqtt_cfg));
    mqtt_cfg.event_handle = mqtt_event_handler;
    mqtt_cfg.uri = brokerIp;
    mqtt_cfg.port = pearlrt::Esp32MqttTcpClient::getInstance()->getPort();
//printf("broker.uri=%s\n", mqtt_cfg.uri);

    esp_mqtt_client_handle_t client = esp_mqtt_client_init(&mqtt_cfg);
    pearlrt::Esp32MqttTcpClient::getInstance()->setClient(client);
    esp_mqtt_client_start(client);

   while(1) {        
      vTaskDelay(1);
   }
}

static void wifiTaskWrapper(void * parameters) {
   wifiTask((char*)parameters);
}
    

/**
 \brief Implementation of Esp32MqttTcpClient 

*/

namespace pearlrt {

   static void wait4Connection() {
      while(!connected) {
         printf("wait for connection...\n");
         vTaskDelay(1000 / portTICK_PERIOD_MS);
         
      }
   }
   static void trimString(char* string, size_t length) {
      int index=length-1;
      for (int i=0; i<length; i++) {
         if (string[i] != ' ') {
            index = i+1;
         }
      } 
      string[index] = '\0';
   }
   Esp32MqttTcpClient* Esp32MqttTcpClient::instance = NULL;

   Esp32MqttTcpClient::Esp32MqttTcpClient(char* brokerIp, uint32_t port) {
      TaskHandle_t tskHandle;
      Log::info("Esp32MqttTcpClient: %s:%d", brokerIp,port);
      instance = this;
      brokerPort = port;

      xTaskCreate(&wifiTaskWrapper, "wifiTask",
                  5000,
                  brokerIp, 5, &tskHandle);
   }

   Esp32MqttTcpClient* Esp32MqttTcpClient::getInstance() {
       return instance;
   }

   uint32_t Esp32MqttTcpClient::getPort() {
      return brokerPort;
   }

   void Esp32MqttTcpClient::setClient(void* esp_mqtt_client_handle) {
      client = esp_mqtt_client_handle;
   }

   void Esp32MqttTcpClient::subscribe(Character<40> topic) {
       char * topic_c;
       topic_c = topic.data;
       int msg_id;

       wait4Connection();

       mutex.lock();
         trimString(topic_c,sizeof(topic));
         msg_id = esp_mqtt_client_subscribe((esp_mqtt_client_handle_t)client,
                                           topic_c, 0);
       mutex.unlock();
   }

   void Esp32MqttTcpClient::publish(Character<40> topic, Character<40> data) {
       char * topic_c;
       char * data_c;
       int msg_id;

       wait4Connection();

       mutex.lock();
         topic_c = topic.data;
         trimString(topic_c,sizeof(topic));
         data_c = data.data;
         trimString(data_c,sizeof(data));
//printf("publish(%p,>%s<,>%s<\n", client,topic_c, data_c);
         msg_id = esp_mqtt_client_publish((esp_mqtt_client_handle_t)client,
                           topic_c, data_c, 0, 1, 0);
       mutex.unlock();
   }

   void Esp32MqttTcpClient::received(size_t tl, char* t, size_t dl, char*d) {
       mutex.lock();
          receiveTopic = t;
          receiveTopicLength = tl;
          receiveData = d;
          receiveDataLength= dl;
          xEventGroupSetBits(event_group, RECEIVED_MQTT_MSG_BIT);
       mutex.unlock();
   }

   static void copyCStringToPrlString(char*prlString, size_t prlStringLength,
                                      char* cString, size_t cStringLength) {
      for (int i=0; i<cStringLength; i++) {
         *prlString++ = *cString ++;
      }
      for (int i = 0; i< prlStringLength - cStringLength;i++) {
         *prlString++ = ' ';
      } 
   }

   void Esp32MqttTcpClient::readMessage(Character<40> & topic,
                                        Character<40> & data) {
       wait4Connection();
      xEventGroupWaitBits(event_group, RECEIVED_MQTT_MSG_BIT,
                           true, true, portMAX_DELAY);
       mutex.lock();
          if (receiveTopicLength > sizeof(topic)) {
             pearlrt::Log::error("MQTT Client: topic too long");
             mutex.unlock();
             throw theCharacterTooLongSignal;
          } 
          if (receiveDataLength > sizeof(data)) {
             pearlrt::Log::error("MQTT Client: data too long");
             mutex.unlock();
             throw theCharacterTooLongSignal;
          } 
          copyCStringToPrlString(topic.data, sizeof(topic),
                          receiveTopic,receiveTopicLength);
          copyCStringToPrlString(data.data, sizeof(data),
                          receiveData,receiveDataLength);
       mutex.unlock();
   }
}



