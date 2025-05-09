# super makefile for OpenPEARL
# 
USERID=$(shell id -u)
#export MAKEFLAGS += "-j 8"

-include configuration/.config

.PHONY: prl install runtime compiler help build all menuconfig static_analyzer

help:
	@echo "*************************************"
	@echo "master make for OpenPEARL"
	@echo "make build:   build compiler and runtime"
	@echo "make prepare: create installation targets with public write access"
	@echo "              should be executed once for developers"
	@echo "              (must be root)"
	@echo "make install: build and install to the configured installation target"
	@echo "              if target is /usr/local/  you must be root"
	@echo "              after make prepare, this may be done as normal user"
	@echo "make defconfig: create default configuration setting"
	@echo "make menuconfig: edit configuration setting"
	@echo "make all:     install"
	@echo "make clean:   clean in all project parts"
	@echo "make compiler-update:   build compiler and copy "
	@echo
	@echo "Usual sequence for developer"
	@echo "+ initially as root:        make prepare"
	@echo "+ initially as normal user: make menuconfig"
	@echo "+ cyclic as normal user:    make install (this includes build)"
	@echo "*************************************"
	echo $(CONFIG_ESP32_FLASH_INTERFACE)

all:	build install

build: configuration/include/autoconf.h  configuration/include/auto.conf \
	 runtime compiler errorChecker static_analyzer

runtime:
	(cd runtime; make all; )

compiler:
	(cd compiler/src; make all; )

errorChecker:
	(cd errorChecker/src; make ; )

static_analyzer:
	(cd static_analyzer; make ; )

prepare: configuration/include/autoconf.h
ifeq ($(USERID),0)
	   (cd runtime; make prepare; )
	   (cd imc3; make prepare; )
	   (cd compiler; make prepare; )
	   (cd static_analyzer; make prepare; )
	   touch $(CONFIG_INSTALL_Target)/bin/prl
	   chmod 777 $(CONFIG_INSTALL_Target)/bin/prl
	   touch $(CONFIG_INSTALL_Target)/bin/prlpp
	   chmod 777 $(CONFIG_INSTALL_Target)/bin/prlpp
else
	   $(error You must be root to execute this command)
endif

prl: showVersion
	runtime/includeComposer/ic  <prl.inc >prl

install: showVersion configuration/include/autoconf.h  flashinterface.inc 
	(cd compiler/src; make clean; )
	(cd compiler/src; make install; )
	(cd runtime; make clean; )
	(cd imc3; make clean; )
	(cd imc3; make install; )
	(cd runtime; make install; )
	(cd static_analyzer; make clean; )
	(cd static_analyzer; make install; )
	runtime/includeComposer/ic  <prl.inc >prl
ifeq ($(CONFIG_DOC_PREPORT),y)
	(cd language_report; make clean; )
	(cd language_report; make final; )
endif
	# is created by includeComposer but not needed here
	@rm -f sysincs.h
	@cp prl $(CONFIG_INSTALL_Target)/bin
	@cp prlpp $(CONFIG_INSTALL_Target)/bin

# create rule to run menuconfig, if no configuration/include/autoconf.h exists
#ifeq ($(wildcard "configuration/include/autoconf.h")),)

configuration/.config:
	(cd configuration; make menuconfig; cd ..)


compiler-update:
	(cd compiler/src; make install; )
	cp prl $(CONFIG_INSTALL_Target)/bin
	cp prlpp $(CONFIG_INSTALL_Target)/bin

.PHONY: configuration/include/autoconf.h
configuration/include/autoconf.h:  configuration/.config
ifneq ($(wildcard ./configuration/include/autoconf.*),./configuration/include/autoconf.h)
	(cd configuration; make include/autoconf.h; cd ..)
endif

clean:
	(cd imc3; make clean; )
	(cd runtime; make clean; )
	(cd compiler/src; make clean; )
	(cd testsuite; make clean; )
	(cd language_report; make clean; )
	(cd configuration; make clean;)
	rm -f prl

menuconfig:
	(cd configuration; make menuconfig; cd .. )
#	(cd configuration; make menuconfig; ./kconfig/conf -s fm/main.fm; cd .. )

.PHONY: defconfig
defconfig:
	cp configuration/def.config configuration/.config
	touch configuration/.config
	(cd configuration; make include/autoconf.h; cd ..)

#.PHONY: jenkins_prepare
#jenkins_prepare:
#	cp configuration/jenkins.config configuration/.config
#	(cd configuration; make include/autoconf.h; cd ..)
#
#jenkins: jenkins_prepare
#	make install

flashinterface.inc: ./configuration/.config
	@awk -F"=" 							\
	   'BEGIN { found = "CONFIG_INSTALL_FLASHINTERFACE_is_not_set" }\
	    $$1=="CONFIG_INSTALL_FLASHINTERFACE" { found = $$2 }	\
	    END {							\
		    print "INTERFACE=" found	\
                }'  $< >flashinterface.inc


.PHONY: showVersion
showVersion:
	@rm -f showVersion.inc
	@echo "# automatic generated, never commit to repo" >showVersion.inc
	@echo $(shell git log -1 --date=format:'%b %d %Y %H:%M:%S %z' | sed -n 3,3p | cut -c 9- | sed 's/^/echo last commit: /') >>showVersion.inc
	@echo $(shell date +'%b %d %Y %H:%M:%S %z' | sed 's/^/echo build date: /') >> showVersion.inc
	@echo "# automatic generated --- end" >>showVersion.inc


