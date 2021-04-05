/*
  [A "BSD license"]
  Copyright (c) 2021      Daniel Leer
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

#include "Esp32I2CBus.h"

#include "esp_log.h"
#include "driver/i2c.h"
#include "esp_pm.h"

#if !TESTMODE // see also Esp32I2CBus.h, needed in Normal Mode 
    #include "Signals.h"
    #include "Log.h"
#endif

#define I2C_MASTER_TX_BUF_DISABLE 0                           /*!< I2C master doesn't need buffer */
#define I2C_MASTER_RX_BUF_DISABLE 0                           /*!< I2C master doesn't need buffer */

#define MAXBYTES 3315                           /*!< max Bytes allowed in one command chain, if more is needed use END command -> see ESP-IDF Guide for more info */
#define WRITE_BIT I2C_MASTER_WRITE              /*!< I2C master write */
#define READ_BIT I2C_MASTER_READ                /*!< I2C master read */
#define ACK_CHECK_EN 0x1                        /*!< I2C master will check ack from slave*/
#define ACK_CHECK_DIS 0x0                       /*!< I2C master will not check ack from slave */


namespace pearlrt{

    Esp32I2CBus::Esp32I2CBus(int portNum, int sda, int scl, int sclSpeed) {
        
        esp_err_t ret = 0;
        this->portNum = portNum;
        
        i2c_config_t configiic;
        configiic.mode = I2C_MODE_MASTER;
        configiic.sda_io_num = (gpio_num_t)sda;
        configiic.sda_pullup_en = GPIO_PULLUP_ENABLE;
        configiic.scl_io_num = (gpio_num_t)scl;
        configiic.scl_pullup_en = GPIO_PULLUP_ENABLE;
        configiic.master.clk_speed = sclSpeed;
        ret = i2c_param_config((i2c_port_t)this->portNum, &configiic);
        if(ret != ESP_OK){
            LOGGER("EspI2CBus on Port %d in init, i2c_param_config exited with: (%s)", this->portNum, esp_err_to_name(ret));
            THROW_PARAMFAIL
        }
        
        ret = i2c_driver_install((i2c_port_t)this->portNum, configiic.mode, I2C_MASTER_RX_BUF_DISABLE, I2C_MASTER_TX_BUF_DISABLE, 0);
        if(ret != ESP_OK){
            LOGGER("EspI2CBus on Port %d in init, i2c_driver_install exited with: (%s)", this->portNum, esp_err_to_name(ret));
            THROW_DATIONFAIL
        }
        
        if (LOW_CPU_CLCK){
            ets_delay_us(100);
            esp_pm_config_esp32_t configFreq;
            configFreq.max_freq_mhz = 10;
            configFreq.min_freq_mhz = 10;
            configFreq.light_sleep_enable = false;
            LOGGER("Config Frequence: %s\n", esp_err_to_name(esp_pm_configure(&configFreq)));
        }
        
        INIT_MUTEX("Esp32I2CBus");
    }

    int Esp32I2CBus::readData(int adr, int n, uint8_t *data) {
        
        esp_err_t ret = 0;
        
        if ((n < 1)||(n>MAXBYTES)) {
            LOGGER("EspI2CBus on Port %d, Adress %x in readData: length of data is not valid", this->portNum, adr);
            THROW_PARAMFAIL
        }
        
        MUTEX_LOCK();
        i2c_cmd_handle_t cmd = i2c_cmd_link_create();
        try{
            i2c_master_start(cmd);
            i2c_master_write_byte(cmd, (adr << 1) | READ_BIT, ACK_CHECK_EN);
            if (n > 1) {
                i2c_master_read(cmd, data, n - 1, I2C_MASTER_ACK);
            }
            i2c_master_read_byte(cmd, data + n - 1, I2C_MASTER_NACK);
            i2c_master_stop(cmd);
            ret = i2c_master_cmd_begin((i2c_port_t)this->portNum, cmd, 1000 / portTICK_RATE_MS);
            if (ret != ESP_OK) {
                i2c_cmd_link_delete(cmd);
                MUTEX_UNLOCK();
                LOGGER("EspI2CBus on Port %d, Adress %x in readData: (%s)", this->portNum, adr,  esp_err_to_name(ret));
                THROW_READINGFAIL
            }
        
        } catch (TERMINATE_TASK) {
            i2c_cmd_link_delete(cmd);
            MUTEX_UNLOCK();
            throw;
        }
        i2c_cmd_link_delete(cmd);
        MUTEX_UNLOCK();
        return n;
    }
    
    int Esp32I2CBus::writeData(int adr, int n, uint8_t *data) {
        
        esp_err_t ret = 0;
        
        if ((n < 1)||(n>MAXBYTES)) {
            LOGGER("EspI2CBus on Port %d, Adress %x in writeData: length of data is not valid", this->portNum, adr);
            THROW_PARAMFAIL
        }
        
        MUTEX_LOCK();
        i2c_cmd_handle_t cmd = i2c_cmd_link_create();
        try{
            i2c_master_start(cmd);
            i2c_master_write_byte(cmd, (adr << 1) | WRITE_BIT, ACK_CHECK_EN);
            i2c_master_write(cmd, data, n, ACK_CHECK_EN);
            i2c_master_stop(cmd);
            ret = i2c_master_cmd_begin((i2c_port_t)this->portNum, cmd, 1000 / portTICK_RATE_MS);
            if (ret != ESP_OK) {
                i2c_cmd_link_delete(cmd);
                MUTEX_UNLOCK();
                LOGGER("EspI2CBus on Port %d, Adress %x in readData: (%s)", this->portNum, adr, esp_err_to_name(ret));
                THROW_WRITINGFAIL
            }
        } catch (TERMINATE_TASK) {
            i2c_cmd_link_delete(cmd);
            MUTEX_UNLOCK();
            throw;
        }
        i2c_cmd_link_delete(cmd);
        MUTEX_UNLOCK();
        return n;
    }
    
    void Esp32I2CBus::rdwr(int n, I2CProvider::I2CMessage* data) {
      LOGGER("I2C Bus: repeated start function not included");
      THROW_DATIONFAIL
    }
}
