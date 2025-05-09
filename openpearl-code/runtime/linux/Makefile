#/*
# [The "BSD license"]
# Copyright (c) 2012-2015 Rainer Mueller
# All rights reserved.
#
# Redistribution and use in source and binary forms, with or without
# modification, are permitted provided that the following conditions
# are met:
#
# 1. Redistributions of source code must retain the above copyright
#    notice, this list of conditions and the following disclaimer.
# 2. Redistributions in binary form must reproduce the above copyright
#    notice, this list of conditions and the following disclaimer in the
#    documentation and/or other materials provided with the distribution.
# 3. The name of the author may not be used to endorse or promote products
#    derived from this software without specific prior written permission.
#
# THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
# IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
# OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
# IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
# INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
# NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
# DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
# THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
# (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
# THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
#*/

# read system configarion setting from menuconfig
include ../../configuration/.config

# pearl runtime system for linux
# this makefile is only called from the superior makefile
# if CONFIG_LINUX is set to y

INCLUDES = -I . -I ../common 
# -I ../common/DynamicDeadlockDetection

CXXFLAGS = -Wall -g  -fstack-protector -fexceptions
CXXFLAGS += -pthread
CXXFLAGS+= -O2
CXXFLAGS += -fno-non-call-exceptions
CPPFLAGS += $(INCLUDES)
LDLIBS = -lpthread -lrt 
CXXFLAGS += -DTARGET=1
CXXFLAGS += -std=c++11 

#LINK.o = @echo link $<; $(LINK.cc)
LINK.o = $(LINK.cc) $(LDLIBS)

# shortcut for link command, since the implicit link rules of 
# make produces problems if the dependencies reside in different
# targets. Thus an explicit rule is added for the targets (especially
# in the tests/ directory
LINK = @echo link $@; $(CXX) $(CXXFLAGS) $(CPPFLAGS) $(LDFLAGS) $^ -o $@ $(LDLIBS)

# make silent compilations 
COMPILE.cc = @echo compile $<; $(CXX) $(CXXFLAGS) $(CPPFLAGS) $(TARGET_ARCH) -c
#COMPILE.cc = $(CXX) $(CXXFLAGS) $(CPPFLAGS) $(TARGET_ARCH) -c

# helper commands include composer, deleted white spaces
IC_DIR = ../includeComposer
IC = $(IC_DIR)/ic
DWS = $(IC_DIR)/dws

DEPENDFILE = .depend

phony = _all
_all: all

SRCANDH = \
	capabilitiesFromPermissions.cc \
	TaskTimer.cc \
	Task.cc \
	Mutex.cc PrioMapper.cc \
	CSema.cc \
	InterruptableSystemDationNB.cc \
	Pipe.cc PipeNBR.cc Disc.cc \
	StdIn.cc StdOut.cc StdError.cc \
	UnixSignal.cc \
	TcpIpServer.cc \
	Console.cc \


XML_FILES += Pipe.xml PipeNBR.xml Disc.xml
XML_FILES += StdIn.xml StdOut.xml StdError.xml
XML_FILES += Console.xml
XML_FILES += UnixSignal.xml
XML_FILES += TcpIpServer.xml


SRCONLY =os.cc \
	TaskMonitor.cc \
	Log.cc

# ---------------------------------------
# import lists of common files for all implementations
# and add prefix of directory common to file lists

# linux contains always i2c support -- maybe no i2c 
# interface is available
CONFIG_HAS_I2C=y
include ../common/Files.common
CXX_COMMON := $(addprefix ../common/,$(CXX_COMMON))
HDR_COMMON := $(addprefix ../common/,$(HDR_COMMON))
XML_FILES += $(addprefix ../common/,$(XML_COMMON))

# -----------------------------------
# add driver specific stuff to file lists


ifeq ($(CONFIG_LINUX_Raspberry),y) 
   # switch off ps_abi warnings
   CXXFLAGS+= -Wno-psabi

   ifeq ($(CONFIG_LINUX_Raspberry_Version_Auto),y) 
      # set CONFIG_LINUX_Raspberry_Version_V1, ..V3 depending of /proc/cpuinfo
      # raspberry revision numbers are taken from http://www.gtkdb.de/index_36_1831.html
      # and http://www.raspberrypi-spy.co.uk/2012/09/checking-your-raspberry-pi-board-version/
      ver := $(shell awk '$$1 == "Revision" {print $$3}' /proc/cpuinfo)
      ifeq ($(ver),000d)
         rpiVersion = V1B
      else ifeq ($(ver),000e)
         rpiVersion = V1B
      else ifeq ($(ver),000f)
         rpiVersion = V1B
      else ifeq ($(ver),a01041)
         rpiVersion = V2B
      else ifeq ($(ver),a21041)
         rpiVersion = V2B
      else ifeq ($(ver),a02082)
         rpiVersion = V3B
      else ifeq ($(ver),a22082)
         rpiVersion = V3B
      else ifeq ($(ver),a020d3)
         # is a V 3B+, but this doues not matter up to now
         rpiVersion = V3B
      else ifeq ($(ver),a03111)
         # 1GB
         rpiVersion = V4B
      else ifeq ($(ver),b03111)
         # 2GB
         rpiVersion = V4B
      else ifeq ($(ver),c03111)
         # 4GB
         rpiVersion = V4B
      else
         $(error raspberry revision $(ver) not known by auto detection)
      endif 
   else 
     ifeq ($(CONFIG_LINUX_Raspberry_Version_V1B),y) 
         rpiVersion = V1B
     else ifeq ($(CONFIG_LINUX_Raspberry_Version_V2B),y) 
         rpiVersion = V2B
     else ifeq ($(CONFIG_LINUX_Raspberry_Version_V3B),y) 
         rpiVersion = V3B
     else ifeq ($(CONFIG_LINUX_Raspberry_Version_V4B),y) 
         rpiVersion = V4B
     endif
   endif
   CXXFLAGS += -DCONFIG_LINUX_Raspberry_Version_$(rpiVersion)
# $(warning $(CXXFLAGS))

   INCLUDES += -I ./raspberry 
   INCLUDES += -I ../raspberry 

   SRCANDH += raspberry/RPiGpio.cc
   SRCANDH += raspberry/RPiDigitalOut.cc
   SRCANDH += raspberry/RPiDigitalIn.cc
   SRCANDH += raspberry/RPiGpioInterrupt.cc
   SRCANDH += raspberry/RPiGpioInterruptHandler.cc
   SRCANDH += raspberry/RPiGpioInterruptTask.cc
   XML_FILES += raspberry/RPiDigitalOut_$(rpiVersion).xml
   XML_FILES += raspberry/RPiDigitalIn_$(rpiVersion).xml
   XML_FILES += raspberry/RPiGpioInterrupt.xml
   XML_FILES += raspberry/RPiGpioInterruptTask.xml
   LDLIBS += -l gpiod

   # create pseudo device for reserved bits of devices or for other applications
   # this option is set by menueconfig; we just add a phony target ad do the 
   # required operations there
   # the pseudo device is created during each build of the OpenPEARL system
   ifeq ($(rpiVersion),V1B) 
	XML_FILES += raspberry/reservedGpios.xml
	targets += reservedGpios
        missingGPIOs = "0,1,5,6,12,13,16,19-21,26"
   else ifeq ($(CONFIG_LINUX_Raspberry_reservedGpios),"")
	targets += no_reservedGpios
   else
	XML_FILES += raspberry/reservedGpios.xml
	targets += reservedGpios
   endif
endif

$(warning $(missingGPIOs))
$(warning $(rpiVersion))

SRCANDH += I2CBus.cc
XML_FILES += I2CBus.xml

ifeq ($(CONFIG_CANSUPPORT),y) 
ifeq ($(CONFIG_LINUX_PCAN),y) 
   LDFLAGS += -lpcan
   SRCANDH += PCan.cc
   XML_FILES += PCan.xml
endif
endif

targets += libOpenPEARLlinux.a linux.xml

#$(warning $(CXX_COMMON_TEST_PROGS))

ifeq ($(CONFIG_LINUX_tests),y)
  include ../common/tests/Files
  include tests/Makefile.inc
  CHECK_FILES += $(GTEST_SRC) $(GTEST_DATION_SRC) $(TEST_SRC)
endif

# ------------------------------------
SRC = $(SRCANDH) $(SRCONLY) $(CXX_COMMON)
SRC_H = $(SRCANDH:%.cc=%.h) PearlIncludes.h  $(HDR_COMMON)

# ------------------------------------
# list of file for checkformat
CHECK_FILES = $(sort $(SRC)  $(SRC_H) \
		     $(CXX_COMMON) $(HDR_COMMON) ) 

CHECK_FILES := $(sort $(CHECK_FILES))
#$(warning src = $(SRC))

PLIB_OBJ = $(SRC:%.cc=%.o)
PLIB_OBJ := $(PLIB_OBJ:%.c=%.o)
#$(warning plibobj = $(PLIB_OBJ))

ALL_SRCS = $(SRC) $(TEST_SRCS) $(TEST_DATION_SRCS) $(GTEST_SRCS)
#$(warning allsrc = $(ALL_SRCS))

# $(warning XML_FILES = $(XML_FILES))


OBJS = $(ALL_SRCS:%.cc=%.o)
OBJS := $(OBJS:%.c=%.o)

# ------------------------------

phony += all
all: depend  $(targets) 

phony += help
help:
	@echo "-----------------"
	@echo "make all         : build the pearl library lubOpenPEARLlinux.a"
	@echo "make clean       : remove all generated files"
	@echo "make checkformat : apply code style checks"
	@echo "make doc         : generate Doxygen documentation in HTML"
	@echo "make install     : install to $(CONFIG_INSTALL_Target) in lib"
	@echo "                 : and prepare include file" 
	@echo "make prepare     : create dummy installation files with public access"
	@echo "-----------------"
	@echo

phony += prepare
prepare:
	@touch $(CONFIG_INSTALL_Target)/lib/libOpenPEARLlinux.a
	@chmod 666 $(CONFIG_INSTALL_Target)/lib/libOpenPEARLlinux.a
	@touch $(CONFIG_INSTALL_Target)/lib/linux.xml
	@chmod 666 $(CONFIG_INSTALL_Target)/lib/linux.xml

phony += install
install: all
	@echo "[install] runtime linux in $(CONFIG_INSTALL_Target)/..."
	@rm -f pearl2.h
	@$(IC) -c $(INCLUDES)  <PearlIncludes.h | $(DWS) >pearl2.h
	@cat sysincs.h pearl2.h \
		>pearl.h
	@cp libOpenPEARLlinux.a $(CONFIG_INSTALL_Target)/lib
	@rm -f pearl2.h sysincs.h
	# ########
	@echo '$(CXX) $(CXXFLAGS) $(CFLAGS) $(LDFLAGS) -I $(CONFIG_INSTALL_Target)/include \' > cc_o.inc
	@echo '   -c $$outputPrlCC -o $$outputCcO >>$$fn.out 2>>$$fn.log' >> cc_o.inc

	@echo '$(CXX) $(CXXFLAGS) $(CFLAGS) $(LDFLAGS) -I $(CONFIG_INSTALL_Target)/include $$objects -o $$target $(CONFIG_INSTALL_Target)/lib/libOpenPEARLlinux.a $(LDLIBS) >>$$target.out 2>>$$target.log' > o_bin.inc
	# ##########
	@echo './$$target' >run_bin.inc
	# #########
	@cp linux.xml $(CONFIG_INSTALL_Target)/lib/linux.xml

libOpenPEARLlinux.a: $(PLIB_OBJ)
	@rm -f $@
	@echo "[build]  libOpenPEARL.a"
	@echo $(PLIB_OBJ)
	@ar r $@  $(PLIB_OBJ) 

# -------------------------------------------------
linux.xml: $(XML_FILES)
	@echo "[build] linux.xml" 
	@echo "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>" > linux.xml
	@echo "<platform file=\"linux.xml\">" >> linux.xml
	@cat $(XML_FILES) >> linux.xml
	@echo "</platform>" >> linux.xml

.phony += reservedGpios
reservedGpios:
	@echo "[build] raspberry/reservedGpios.xml" 
	@rm -f raspberry/reservedGpios.xml
	@echo "   <configuration name=\"reservedGpio\">" >raspberry/reservedGpios.xml
	$(eval BitList=$(shell ../../imc3/convertList $(CONFIG_LINUX_Raspberry_reservedGpios) $(missingGPIOs)))
	@echo "      <check pinDoesNotCollide=\"RPiGPIO\" bitList=\"$(BitList)\"/>" >>raspberry/reservedGpios.xml
	@echo "   </configuration>" >>raspberry/reservedGpios.xml

.phony += no_reservedGpios
no_reservedGpios:
	rm -f raspberry/reservedGpios.xml

# -------------------------------------------------
# additional rules to enable make to build the autogenerated files
#
../common/Signals.hh: ../common/Signals.ods
	@echo create signal definitions from spreadsheet
	@(cd ../common; \
	  perl GenerateSignalDefinitions.pl GENERIC LINUX 2>signals.log;)

../common/Signals.hcc: ../common/Signals.ods
	(cd ../common; perl GenerateSignalDefinitions.pl GENERIC LINUX;)

../common/Signals.xml: ../common/Signals.ods
	(cd ../common; perl GenerateSignalDefinitions.pl GENERIC LINUX;)

XML_FILES += ../common/Signals.xml

../Signals.cc: ../Signals.hh ../Signals.hcc

#---------------------------------------------------------
phony += clean
clean: 
	@echo [clean] created files in various directories
	@rm -f $(OBJS)
	@rm -f $(targets)
	@rm -f pearl_log.txt
	@rm -rf latex
	@rm -rf html
	@rm -f .depend
	@rm -f ../common/tests/*.new
	@rm -f ../common/tests/*.diff
	@rm -f ../common/tests/*.o
	@rm -f ../common/*.new
	@rm -f ../common/*.diff
	@rm -f ../common/*.o
	@rm -f *.new
	@rm -f *.diff
	@rm -f *.log
	@rm -f raspberry/*.new
	@rm -f raspberry/*.diff
	@rm -f raspberry/*.o
	@rm -f drivers/*.new
	@rm -f drivers/*.diff
	@rm -f drivers/*.o
	@rm -f core
	@rm -f ../common/Signals.hh ../common/Signals.hcc
	@rm -f ../common/Signals.xml
	@rm -f ../common/signallist.tex 
	@rm -f ../common/signals.log
	@rm -f sysincs.h  pearl2.h pearl.h
	@rm -f cc_bin.inc run_bin.inc
	@rm -f $(TEST_FILES_2_REMOVE)

phony += checkformat
checkformat: 
	@$(foreach f,$(CHECK_FILES),../codestyle/check.sh $(f);)

phony += doc
doc:
	doxygen doc_stuff/Doxyfile
#	(cd latex; make pdf; cd ..)

phony += depend
depend: $(ALL_SRCS) ../common/Signals.hh ../common/Signals.hcc
	@echo update dependencies ...
	@$(CXX) -MM  $(CXXFLAGS) $(CPPFLAGS) $(INCLUDES) $(ALL_SRCS) > $(DEPENDFILE)

-include .depend

.PHONY: $(phony)

# $(warning $(targets))
