###############################################################################
## [A "BSD license"]
## Copyright (c) 2023 Marcel Schaible
## All rights reserved.
##
## Redistribution and use in source and binary forms, with or without
## modification, are permitted provided that the following conditions
## are met:
##
## 1. Redistributions of source code must retain the above copyright
##    notice, this list of conditions and the following disclaimer.
## 2. Redistributions in binary form must reproduce the above copyright
##    notice, this list of conditions and the following disclaimer in the
##    documentation and/or other materials provided with the distribution.
## 3. The name of the author may not be used to endorse or promote products
##    derived from this software without specific prior written permission.
##
## THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
## IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
## OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
## IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
## INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
## NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
## DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
## THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
## (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
## THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
###############################################################################

include ../../../configuration/include/auto.conf


help:
	@echo "      ---------------------------------------------------"
	@echo "      make build    - build simulator"
	@echo "      make install  - install simulator"
	@echo "      make prepare  - prepare installation location"
	@echo "      make clean    - remove generated files"
	@echo "      ---------------------------------------------------"


all:  install

###############################################################################

build: SimFilExtruder.cc SimFilExtruderSystem.xml

##############################################################################

prepare:
	mkdir $(CONFIG_INSTALL_Target)/lib/SimFilExtruder
	chmod 777 $(CONFIG_INSTALL_Target)/lib/SimFilExtruder

###############################################################################

install: SimFilExtruderSystem.xml SimFilExtruder.cc
	cp SimFilExtruderSystem.xml $(CONFIG_INSTALL_Target)/lib/SimFilExtruder


###############################################################################

.PHONY: SimFilExtruder.cc
SimFilExtruder.cc:
	prl -cc SimFilExtruderControl.prl
	prl -cc SimFilExtruder.prl 

.PHONY: SimFilExtruderSystem.xml
SimFilExtruderSystem.xml:
	prl -cc SimFilExtruderSystem.prl

###############################################################################

clean:
	$(RM) SimFilExtruder.cc SimFilExtruderSystem.cc SimFilExtruder.xml SimFilExtruderSystem.xml SimFilExtruderControl.cc SimFilExtruderControl.xml

###############################################################################

