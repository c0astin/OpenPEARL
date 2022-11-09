###############################################################################
## [A "BSD license"]
## Copyright (c) 2022 Rainer Müller
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

PACKAGE_DIR = static_analyzer
OUTPUT_DIR = $(PACKAGE_DIR)-out
#JAVAC_FLAGS = -Xlint:unchecked
JAVAC_FLAGS = 
MINIMALJSON= 
###############################################################################

include ../configuration/include/auto.conf


help:
	@echo "      ---------------------------------------------------"
	@echo "      make build    - build static_analyzer"
	@echo "      make install  - install static_anylzer"
	@echo "      make prepare  - prepare installation location"
	@echo "      ---------------------------------------------------"


all:  install

###############################################################################

build: run_static_analyzer.inc 
	(test -d $(OUTPUT_DIR) || mkdir $(OUTPUT_DIR))
	( javac -d  $(OUTPUT_DIR) $(JAVAC_FLAGS) $(PACKAGE_DIR)/StaticAnalyzer.java  )
	(cd $(OUTPUT_DIR) && jar -cf  static_analyzer.jar  .)

##############################################################################

prepare:
	touch $(CONFIG_INSTALL_Target)/bin/static_analyzer.jar
	chmod 777 $(CONFIG_INSTALL_Target)/bin/static_analyzer.jar


###############################################################################

install: build run_static_analyzer.inc 
	cp $(OUTPUT_DIR)/static_analyzer.jar $(CONFIG_INSTALL_Target)/bin


###############################################################################

.PHONY: run_static_analyzer.inc
run_static_analyzer.inc:
	echo "##### auto generated  start " > run_static_analyzer.inc
	echo 'java -cp $(CONFIG_INSTALL_Target)/bin/static_analyzer.jar  static_analyzer/StaticAnalyzer \' >> run_static_analyzer.inc
	echo '    $$imcInputFiles \' >> run_static_analyzer.inc
	echo "##### auto generated end " >> run_static_analyzer.inc

###############################################################################

clean:
	$(RM) -rf $(OUTPUT_DIR)
	$(RM) run_static_analzer.inc

###############################################################################

