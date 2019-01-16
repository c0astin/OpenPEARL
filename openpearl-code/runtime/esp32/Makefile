ESP_IDF_PATH=~/esp/esp-idf

MAKEFILE_PATH := $(abspath $(lastword $(MAKEFILE_LIST)))
CURRENT_DIR := $(dir $(MAKEFILE_PATH))
OPENPEARL_DIR := $(abspath $(MAKEFILE_PATH)/../../..)
export OPENPEARL_DIR

# read system configuration setting from menuconfig
include ../../configuration/.config

phony = all clean
_all: all


include makefile.conf

CC := $(CROSS)$(CC)
CXX := $(CROSS)$(CXX)
AR := $(CROSS)$(AR)

# make silent compilations
COMPILE.c = @echo compile $<; $(CC) $(CFLAGS) $(CPPFLAGS) $(TARGET_ARCH) -c
COMPILE.cc = @echo [compile] $<; $(CXX) $(CXXFLAGS) $(CPPFLAGS) $(TARGET_ARCH) -c
#COMPILE.cc = echo [compile] $<; $(CXX) $(CXXFLAGS) $(CPPFLAGS) $(TARGET_ARCH) -c
LINK.o = $(LINK.cc)
#LINK = @echo [link] $@; $(CXX) $(CXXFLAGS) $(CPPFLAGS) $(LDFLAGS) $^ -o $@
LINK = @echo [link] $@; $(CXX) $(PURECFLAGS) -DTARGET=3 $(CXXFLAGS) $(CPPFLAGS) $(LFLAGS) $(LDFLAGS) $^ -o $@

IC_DIR = ../includeComposer
IC = $(IC_DIR)/ic
DWS = $(IC_DIR)/dws

INCS = .
CXXSRCS =
CRCS =

targets := esp32.xml



# ------------------- add FreeRTOS specific PEARL classes to project
#include esp-idf/components/freertos/PEARL/Files
#XMLFILES += $(addprefix esp-idf/components/freertos/PEARL/,$(PEARL_FREERTOS_XML))

# ------------------- add plattform independent PEARL classes to project
include ../common/Files.common
XMLFILES  += $(addprefix ../common/,$(XML_COMMON))

# -------------------- add includes for the include composer
INCS += esp-idf/components/freertos_specific/addOns
INCS += esp-idf/components/freertos_specific/PEARL
INCS += esp-idf/components/openpearl/include
INCS += esp-idf/components/openpearl/common
//INCS += ../common

# -------------------- add boardsupport component xml files
#XMLFILES += esp-idf/components/boardsupport/Esp32Uart.xml
XMLFILES += openpearlComponent/StdOut.xml
#XMLFILES += esp-idf/components/boardsupport/Esp32BME280.xml
#XMLFILES += esp-idf/components/boardsupport/Esp32Wifi.xml
#XMLFILES += esp-idf/components/boardsupport/TcpIpServer.xml

define esp32build
make -C esp-idf-project
endef


# ---------------- add remaining board depending modules

# ------------------------ finalize variables 
INCS:=$(addprefix -I ,$(INCS))
PURECXXFLAGS := $(CXXFLAGS)
#ifeq ($(CONFIG_LPC1768_CHECK_STACK_OVERFLOW),y)
#PURECXXFLAGS += -finstrument-functions 
#endif

PURECFLAGS := $(CFLAGS)
CFLAGS += $(INCS)
CXXFLAGS += $(INCS)
CXXFLAGS += -DTARGET=3


CHECK_FILES := $(sort $(CHECK_FILES))

#$(warning BOARDSUPPORT_OBJ=$(BOARDSUPPORT_OBJ))
#$(warning XMLFILES=$(XMLFILES))

all: setuplinks esp-idf-build $(targets)

tests/testStackOverflow.s: tests/testStackOverflow.cc
	$(CXX) $^ -S $(CXXFLAGS)  $(PURECXXFLAGS)

esp-idf-build: ../common/Signals.hh ../common/Signals.hcc
	@echo "[create] $@"
	cp -p FreeRTOSConfig.h $(ESP_IDF_PATH)/components/freertos/include/freertos
	export IDF_PATH=$(ESP_IDF_PATH)
	$(call esp32build)

#libOpenPEARLesp32.a: esp-idf-build $(PLIB_OBJS)
#	@echo "[create] $@"
#	@rm   -f libOpenPEARLesp32.a
#	@rm   -f libOpenPEARLesp32complete.a
#	@rm   -f libOpenPEARLesp32complete.mri
#	@$(AR) r libOpenPEARLesp32.a $(PLIB_OBJS)
#	@echo "create libOpenPEARLesp32complete.a" > libOpenPEARLesp32complete.mri
#	@echo "addlib libOpenPEARLesp32.a" >> libOpenPEARLesp32complete.mri
#	@find esp-idf-template/build/ -maxdepth 2 -regex ".*\.a" | grep -v main | grep -v freertos | sed -e 's/^/addlib /' >> libOpenPEARLesp32complete.mri
#	# Add component libraries according to specific component.mk files
#	@echo "addlib esp-idf/components/newlib/lib/libc.a" >> libOpenPEARLesp32complete.mri
#	@echo "addlib esp-idf/components/newlib/lib/libm.a" >> libOpenPEARLesp32complete.mri
#	@echo "addlib esp-idf/components/bt/lib/libbtdm_app.a" >> libOpenPEARLesp32complete.mri
#	@echo "addlib esp-idf/components/esp32/libhal.a" >> libOpenPEARLesp32complete.mri
#	@find esp-idf/components/esp32/lib -regex ".*\.a" | sed -e 's/^/addlib /' >> libOpenPEARLesp32complete.mri
#	@echo "save\nend" >> libOpenPEARLesp32complete.mri
#	@$(AR) -M <libOpenPEARLesp32complete.mri
	#@mv   -f libOpenPEARLesp32complete.a libOpenPEARLesp32.a

esp32.xml: $(XMLFILES)
	@echo "[create] $@"
	#echo $(XMLFILES)
	@echo "[build] $@"
	@echo "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>" > $@
	@echo "<platform file=\"$@\">" >> $@
	@cat $(XMLFILES) >> $@
	@echo "</platform>" >> $@


#phony += depend
#depend: ../common/Signals.hh ../common/Signals.hcc $(SRCS)
#	@echo "[create] $@"
#	@$(CXX) -MM $(CFLAGS) $(CPPFLAGS) $(INCS) $^ >.depend

phony += clean
clean:
	$(call esp32build) clean
	@rm -f $(BOARDSUPPORT_OBJ)
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
	rm -f esp-idf 
	rm -f openpearlComponent/common
	rm -f FreeRTOSspecificComponent/PEARL
	rm -f FreeRTOSspecificComponent/addOns
	rm -rf esp-idf-project/build

phony += setuplinks
setuplinks:
	# we must remove the target of the 'ln' command first
	# to avoid spurious other links
	rm -f esp-idf
	ln -s $(ESP_IDF_PATH) esp-idf
	rm -f openpearlComponent/common
	ln -s $(CURRENT_DIR)../common  openpearlComponent/common
	rm -f $(ESP_IDF_PATH)/components/openpearl
	ln -s $(CURRENT_DIR)openpearlComponent $(ESP_IDF_PATH)/components/openpearl
	rm -f $(ESP_IDF_PATH)/components/freertos_specific
	ln -s $(CURRENT_DIR)FreeRTOSspecificComponent $(ESP_IDF_PATH)/components/freertos_specific

	rm -f FreeRTOSspecificComponent/PEARL
	ln -sf $(CURRENT_DIR)../FreeRTOS/PEARL $(CURRENT_DIR)/FreeRTOSspecificComponent/PEARL
	rm -f FreeRTOSspecificComponent/addOns
	ln -sf $(CURRENT_DIR)../FreeRTOS/addOns $(ESP_IDF_PATH)/components/freertos_specific/addOns

phony += prepare
prepare:
	@mkdir -p $(CONFIG_INSTALL_Target)/lib/ldscripts
	#@mkdir -p $(CONFIG_INSTALL_Target)/lib/OpenPEARLesp32
	#chmod 777 $(CONFIG_INSTALL_Target)/lib/OpenPEARLesp32
	#@touch $(CONFIG_INSTALL_Target)/lib/ldscripts/OpenPEARLesp32.ld
	#@chmod 666 $(CONFIG_INSTALL_Target)/lib/ldscripts/OpenPEARLesp32.ld
	@touch $(CONFIG_INSTALL_Target)/lib/esp32.xml
	@chmod 666 $(CONFIG_INSTALL_Target)/lib/esp32.xml

phony += install
install: all cc_bin run_bin
	@echo [install]
	@rm -f pearl2.h
	@echo $(INCS)
	@$(IC) -c $(INCS)  <PearlIncludes.h | $(DWS) >pearl2.h
	@cat sysincs.h pearl2.h >pearl.h
	@rm -f pearl2.h sysincs.h
	#Find and copy all generated esp32 libraries
	#limit search to depth 2 to exclude libraries from the bootloader
	@find esp-idf-project/build/ -maxdepth 2 -regex ".*\.a" -exec cp {} $(CONFIG_INSTALL_Target)/lib/OpenPEARLesp32 \;
	#Find and copy all generated linker files
	@find esp-idf-project/build/ -regex ".*\.ld" -exec cp {} $(CONFIG_INSTALL_Target)/lib/OpenPEARLesp32 \;
	@find $(ESP_IDF_PATH)/components -regex ".*\.a" -exec cp {} $(CONFIG_INSTALL_Target)/lib/OpenPEARLesp32 \;
	#Find and copy all predefined linker files
	@find $(ESP_IDF_PATH)/components/ -regex ".*\.ld" -exec cp {} $(CONFIG_INSTALL_Target)/lib/OpenPEARLesp32 \;
	cp esp-idf-project/build/bootloader/bootloader.bin $(CONFIG_INSTALL_Target)/lib/OpenPEARLesp32
	cp esp-idf-project/build/partitions_singleapp.bin $(CONFIG_INSTALL_Target)/lib/OpenPEARLesp32
	@mkdir -p $(CONFIG_INSTALL_Target)/lib/ldscripts
	#@cp OpenPEARLesp32.ld $(CONFIG_INSTALL_Target)/lib/ldscripts
	@cp esp32.xml $(CONFIG_INSTALL_Target)/lib

####### TODO
RUNTIMELIBS :=-L/usr/local/lib/OpenPEARLesp32  -lapp_trace -lapp_update\
		-laws_iot -lbootloader_support -lbt -lcoap -lconsole \
		-lcxx -u __cxa_guard_dummy -ldriver \
		-lesp_ringbuf \
		-lesp32 -lhal  -lcore -lrtc -lnet80211 -lpp -lwpa \
		-lsmartconfig -lcoexist -lwps -lwpa2 -lespnow -lphy \
		-lesp_adc_cal -lethernet -lexpat -lfatfs \
		-lfreertos -Wl,--undefined=uxTopUsedPriority -lheap \
		-lidf_test -ljsmn -ljson -llibsodium -llog -llwip \
		-lmbedtls -lmdns -lmicro-ecc -lc -lm -lnewlib -lnghttp \
		-lnvs_flash -lopenpearl -lfreertos_specific -lopenssl \
		-lpthread -lsdmmc -lsoc -lspi_flash -lspiffs \
		-ltcpip_adapter -lulp -lvfs -lwear_levelling \
		-lwpa_supplicant -lxtensa-debug-module \
		-lgcc -lstdc++ -lgcov -Wl,--end-group -Wl,-EL
LINKERSCRIPTS:= -T esp32_out.ld -u ld_include_panic_highint_hdl \
		-T esp32.common.ld -T esp32.rom.ld -T esp32.peripherals.ld\
		-T esp32.rom.spiram_incompatible_fns.ld 

phony += cc_bin
cc_bin:
	@echo "[create] $@"
	@echo "# auto generated " > cc_bin.inc
	@echo '$(CXX) -g -DTARGET=3 -nostdlib -ffunction-sections -fdata-sections -Wl,--gc-sections -Wl,-static -Wl,--start-group -mlongcalls -frtti -u call_user_start_cpu0 -Os -I $(CONFIG_INSTALL_Target)/include $$SYSTEM $$fn.cc $(RUNTIMELIBS) $(LINKERSCRIPTS) -o $$fn -Wl,-Map=esp32.map' >> cc_bin.inc
	@echo 'python $(CURRENT_DIR)esp-idf/components/esptool_py/esptool/esptool.py --chip esp32 elf2image --flash_mode "dio" --flash_freq "40m" --flash_size "2MB"  -o $$fn.bin $$fn' >> cc_bin.inc

######## TODO
phony += run_bin
run_bin:
	@echo "[create] $@"
	@echo "# auto generated " > run_bin.inc
	@echo 'python $(ESP_IDF_PATH)/components/esptool_py/esptool/esptool.py --chip esp32 --port /dev/ttyUSB1 --baud 115200 --before default_reset --after hard_reset write_flash -u --flash_mode dio --flash_freq 40m --flash_size detect 0x1000 $(CONFIG_INSTALL_Target)/lib/OpenPEARLesp32/bootloader.bin 0x10000 $$fn.bin 0x8000 $(CONFIG_INSTALL_Target)/lib/OpenPEARLesp32/partitions_singleapp.bin' >>run_bin.inc
	@echo 'python -m serial.tools.miniterm --rts 0 --dtr 0 --raw /dev/ttyUSB1 115200' >>run_bin.inc

# -------------------------------------------------
# additional rules to enable make to build the autogenerated files
#
../common/Signals.hh: ../common/Signals.ods
	@echo create signal definitions from spreadsheet
	@(cd ../common; \
	 perl GenerateSignalDefinitions.pl GENERIC LPC1768 2>signals.log;)

../common/Signals.hcc: ../common/Signals.ods
	(cd ../common; perl GenerateSignalDefinitions.pl GENERIC LPC1768; )

#../Signals.cc: ../Signals.hh ../Signals.hcc

phony +=  checkformat
checkformat:
	@$(foreach f,$(CHECK_FILES),../codestyle/check.sh $(f);)

-include .depend

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
