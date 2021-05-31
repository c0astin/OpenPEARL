.ONESHELL:

# lets get the absolute path of this directory
MAKEFILE_PATH := $(abspath $(lastword $(MAKEFILE_LIST)))
CURRENT_DIR := $(dir $(MAKEFILE_PATH))

$(warning PATH=$(PATH))

# read system configuration setting from menuconfig
# this must be done after the path extractions, since include 
# modifies MAKEFILE_PATH
include ../../configuration/.config


#remove quotes from the config-variable
ESP_IDF_PATH:= $(subst $\",,$(CONFIG_ESP32_IDF_PATH))
$(warning ESP_IDF_PATH: $(ESP_IDF_PATH))

##setup the variable for esp-idf build process
#export IDF_PATH=$(ESP_IDF_PATH)
$(warning IDF_PATH: $(IDF_PATH))

#and have a look into this folder
UID=$(shell id -u)
ifneq (0,$(UID))
  ifeq (,$(wildcard $(ESP_IDF_PATH)/add_path.sh))
     $(error ESP_IDF_PATH seems to be wrong ($(ESP_IDF_PATH)))
  endif
  ifeq (,$(wildcard $(ESP_IDF_PATH)/components))
     $(error ESP_IDF_PATH seems to be wrong ($(ESP_IDF_PATH)))
  endif
endif

## have a look on the idf-version
#exp=$(shell . $(ESP_IDF_PATH)/export.sh)
#version=$(shell idf.py --version)
##version=idf.py --version
##$(warning IDF-Version: $(version))

#$(warning CURRENT_DIR: $(CURRENT_DIR))

OPENPEARL_DIR := $(abspath $(MAKEFILE_PATH)/../../..)
export OPENPEARL_DIR

IDF_PY := idf.py -C OpenPEARLProject

phony = all clean
_all: all


include makefile.conf

-include OpenPEARLProject/esp_idf_build.mk

IC_DIR = ../includeComposer
IC = $(IC_DIR)/ic
DWS = $(IC_DIR)/dws

INCS = .
CXXSRCS =
CRCS =

targets := esp32.xml

# ------------------- add plattform independent PEARL classes to project
CONFIG_HAS_I2C=y
include ../common/Files.common
XMLFILES  += $(addprefix ../common/,$(XML_COMMON))


## -------------------- add includes for the include composer
INCS += OpenPEARLProject/main/PEARL
INCS += OpenPEARLProject/main/common
INCS += OpenPEARLProject/main
INCS += OpenPEARLProject/main/addOns

# -------------------- add boardsupport component xml files
XMLFILES += OpenPEARLProject/main/StdOut.xml
XMLFILES += OpenPEARLProject/main/Console.xml
XMLFILES += OpenPEARLProject/main/Esp32Uart.xml
XMLFILES += OpenPEARLProject/main/Esp32WifiConfig.xml
XMLFILES += OpenPEARLProject/main/Esp32MqttTcpClient.xml
XMLFILES += OpenPEARLProject/main/Esp32I2CBus.xml
#XMLFILES += esp-idf/components/boardsupport/Esp32BME280.xml
#XMLFILES += esp-idf/components/boardsupport/Esp32Wifi.xml
#XMLFILES += OpenPEARLProject/main/TcpIpServer.xml
$(warning XMLFILES=$(XMLFILES))

define esp32build
  idf.py -C OpenPEARLProject
endef


# ---------------- add remaining board depending modules

# ------------------------ finalize variables 
INCS:=$(addprefix -I ,$(INCS))
#PURECXXFLAGS := $(CXXFLAGS)

#PURECFLAGS := $(CFLAGS)
#CFLAGS += $(INCS)
#CXXFLAGS += $(INCS)

# TARGET 3 is the ESP32
CXXFLAGS += -DTARGET=3

## allow non pod union members
#CXXFLAGS += -std=c++11

## list of files to check for code convention (make checkformat)
#CHECK_FILES := $(wildcard openpearlComponent/*.cc)
#CHECK_FILES += $(wildcard openpearlComponent/include/*.h)
#CHECK_FILES := $(sort $(CHECK_FILES))
#$(warning CHECK_FILES=$(CHECK_FILES))

all: patch $(targets)

tests/testStackOverflow.s: tests/testStackOverflow.cc
	$(CXX) $^ -S $(CXXFLAGS)  $(PURECXXFLAGS)

#OpenPEARLProject: ../common/Signals.hh ../common/Signals.hcc
#	@echo "[create] $@"
#	(./runbuild.sh $(IDF_PATH))

esp32.xml: $(XMLFILES)
	@echo "[create] $@"
	#echo $(XMLFILES)
	@echo "[build] $@"
	@echo "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>" > $@
	@echo "<platform file=\"$@\">" >> $@
	@cat $(XMLFILES) >> $@
	@echo "</platform>" >> $@

phony += clean
clean: 
	$(call esp32build) clean
	@rm -f $(targets)
	@rm -f $(GTEST_OBJS)
	@rm -f $(GTEST_OBJS1)
	@rm -f $(GTEST_OBJS2)
	@rm -f cc_bin.inc run_bin.inc
	@rm -f .depend pearl.h
	@rm -f $(PLIB_OBJS) $(OBJ)
	@rm -f $(addsuffix .diff,$(CHECK_FILES))
	@rm -f $(addsuffix .new,$(CHECK_FILES))
	@rm -rf html
	@rm -rf latex
	@rm -f ../common/tests/*.o
	@rm -f tests/*.o

phony += doc
doc:
	doxygen doc_stuff/Doxyfile
#	(cd latex; make pdf; cd ..)

phony += gitclean
gitclean:
	@echo "clean all files and links for git operations"
	@rm -rf OpenPEARLProject/build
	@rm OpenPEARLProject/main/common
	@rm OpenPEARLProject/main/addOns
	@rm OpenPEARLProject/main/PEARL
	@rm OpenPEARLProject/main/PearlIncludes.h

phony += links
links:
	ln -s ../../pearl.h OpenPEARLProject/main/PearlIncludes.h
	ln -s ../../../FreeRTOS/PEARL OpenPEARLProject/main/PEARL
	ln -s ../../../FreeRTOS/addOns OpenPEARLProject/main/addOns
	ln -s ../../../common OpenPEARLProject/main/common



phony += patch
patch:
	(cd patchFiles; ./patch.sh $(ESP_IDF_PATH))

phony += prepare
prepare:
	@mkdir -p $(CONFIG_INSTALL_Target)/lib/ldscripts
	@mkdir -p $(CONFIG_INSTALL_Target)/lib/OpenPEARLesp32
	@chmod 777 $(CONFIG_INSTALL_Target)/lib/OpenPEARLesp32
	@touch $(CONFIG_INSTALL_Target)/lib/esp32.xml
	@chmod 666 $(CONFIG_INSTALL_Target)/lib/esp32.xml

#.phone += OpenPEARLProject/idf2mk  
#OpenPEARLProject/idf2mk:  
#	(cd OpenPEARLProject; gcc idf2mk.c -o idf2mk)
#	#(cd OpenPEARLProject; ./idf2mk linker.txt $(IDF_PATH)/components/)
#	#(cd OpenPEARLProject; cat esp_idf_builf.mk)

phony += OpenPEARLProject
OpenPEARLProject:  
	echo [build] $@
	rm -f pearl2.h
	echo "create pearl.h"
	$(IC) -c $(INCS) -DOPENPEARL_ESP32  <PearlIncludes.h | $(DWS) >pearl2.h
	cat sysincs.h pearl2.h >pearl.h
	rm -f pearl2.h sysincs.h
	rm -f OpenPEARLProject/main/PearlIncludes.h
	ln -s ../../pearl.h OpenPEARLProject/main/PearlIncludes.h
	./runbuild.sh $(IDF_PATH)
	
phony += install
install: OpenPEARLProject esp32.xml cc_bin run_bin
	echo [install]
	ar d OpenPEARLProject/build/esp-idf/main/libmain.a testprog.cc.obj system.cc.obj
	(cd OpenPEARLProject; ./cp.cmd)
	cp OpenPEARLProject/build/bootloader/bootloader.bin $(CONFIG_INSTALL_Target)/lib/OpenPEARLesp32
	cp OpenPEARLProject/build/partition_table/partition-table.bin $(CONFIG_INSTALL_Target)/lib/OpenPEARLesp32
	cp esp32.xml $(CONFIG_INSTALL_Target)/lib


phony += cc_bin
cc_bin:
	@echo "[create] $@"
	@echo "# auto generated " > cc_bin.inc
	#
	@echo '$(CXX) -g -DTARGET=3 -std=c++11 -I /usr/local/include \
 		$(LINK_PARAMETERS_1) \
		  $$fn.cc $$SYSTEM -o $$fn.elf\
		$(LINK_PARAMETERS_2)' >> cc_bin.inc
	@echo 'python  $(ESP_IDF_PATH)/components/esptool_py/esptool/esptool.py --chip esp32 elf2image --flash_mode "dio" --flash_freq "40m" --flash_size "2MB"  -o $$fn.bin $$fn.elf' >> cc_bin.inc
	@echo 'size $$fn.elf' >> cc_bin.inc

phony += run_bin
run_bin:
	@echo "[create] $@"
	@echo "# auto generated " > run_bin.inc
	@echo 'python $(ESP_IDF_PATH)/components/esptool_py/esptool/esptool.py --chip esp32 --port $$INTERFACE --baud 115200 --before default_reset --after hard_reset write_flash -u --flash_mode dio --flash_freq 40m --flash_size detect  0x10000 $$fn.bin' >>run_bin.inc
	@echo 'python -m serial.tools.miniterm --rts 0 --dtr 0 --eol CR --raw $$INTERFACE 115200' >>run_bin.inc

phony += monitor
monitor:
	# note: /dev/ttyUSB0 fits in the momemnt - we should add a menuconfig item for this
	python -m serial.tools.miniterm --rts 0 --dtr 1 --eol CR --raw /dev/ttyUSB0 115200


monitor:
phony += initial_flash
initial_flash:
	python $(ESP_IDF_PATH)/components/esptool_py/esptool/esptool.py \
	   --chip esp32 --port $(CONFIG_ESP32_FLASH_INTERFACE) --baud 115200 \
           --before default_reset --after hard_reset write_flash \
           -u --flash_mode dio --flash_freq 40m \
           --flash_size detect\
           0x1000 $(CONFIG_INSTALL_Target)/lib/OpenPEARLesp32/bootloader.bin\
           0x8000 $(CONFIG_INSTALL_Target)/lib/OpenPEARLesp32/partition-table.bin

# -------------------------------------------------
# additional rules to enable make to build the autogenerated files
#
../common/Signals.hh: ../common/Signals.ods
	@echo create signal definitions from spreadsheet
	@(cd ../common; \
	 perl GenerateSignalDefinitions.pl GENERIC ESP32 FREERTOS 2>signals.log;)

../common/Signals.hcc: ../common/Signals.ods
	(cd ../common; perl GenerateSignalDefinitions.pl GENERIC ESP32 FREERTOS; )

#../Signals.cc: ../Signals.hh ../Signals.hcc

phony +=  checkformat
checkformat:
	@$(foreach f,$(CHECK_FILES),../codestyle/check.sh $(f);)


phony += help
help:
	@echo " make prepare	 - setup rw/permissions to installation files"
	@echo " make initial_flash - flash partition table and bootloader"
	@echo "                      must be done once for a new esp32-system"
	@echo " make install	 - build esp-specific OpenPEARL library and install"

phony += info
info:
	@echo "Makefile Summary"
	@echo "targets: " $(targets)
	@echo "CFLAGS " $(CFLAGS)
	@echo "CPPFLAGS " $(CPPFLAGS)
	@echo "INCS " $(INCS)
	@echo "CXXFLAGS " $(CXXFLAGS)
	@echo "LFLAGS " $(LFLAGS)
	@echo "LDFLAGS " $(LDFLAGS)
	@echo "PHONY " $(phony)


.PHONY: $(phony)

print-%  : ; @echo $* = $($*)
