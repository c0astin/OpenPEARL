#
# This is a project Makefile. It is assumed the directory this Makefile resides in is a
# project subdirectory.
#

include ../../../configuration/.config

PROJECT_NAME := OpenPEARL
PROJECT_VER := 0.1

ifeq ($(CONFIG_ESP32_CHECK_STACK_OVERFLOW),y)
EXTRA_CXXFLAGS += -finstrument-functions 
endif

#COMPONENTS := main

include ../esp-idf/make/project.mk
$(warning CXX:$(CXX))
$(warning CXXFLAGS:$(CXXFLAGS))
$(warning LDLAGS:$(LDFLAGS))
