###############################################################################
## [The "BSD license"]
## Copyright (c) 2012-2022 Marcel Schaible
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

include ../Makefile.inc
include ../../configuration/.config

##
## antlr generates out of OpenPearl.g4 the following files:
##
ANTLR_GENERATED_FILES = OpenPearl.tokens          \
                        OpenPearlBaseVisitor.java \
                        OpenPearlLexer.java       \
                        OpenPearlLexer.tokens     \
                        OpenPearlParser.java      \
                        OpenPearlVisitor.java

## ANTLR_FLAGS = -XdbgST
ANTLR_FLAGS = 

OUTPUT_PRL_DIR = ../prl-out
OUTPUT_PP_DIR = ../pp-out
##JAVAC_FLAGS = -Xlint:unchecked
JAVAC_FLAGS =

# libs for deadlock detection
#MINIMALJSON= ../minimal-json-0.9.5.jar
#APACHE_LANG3= ../commons-lang3-3.12.0.jar

###############################################################################

.PHONY: gen prl pp

help:
	@echo "      ---------------------------------------------------"
	@echo "      make help     - this output"
	@echo "      make all      - gen;prl;pp;install in one pass"
	@echo "      make gen      - generate parser"
	@echo "      make prl      - build compiler"
	@echo "      make pp       - build preprocessor"	
	@echo "      make test     - run regression test suite"
	@echo "      make install  - install compiler and compile-script"
	@echo "      ---------------------------------------------------"

all: gen prl pp install

.cmd_ok:
	./check_cmds.sh

###############################################################################

gen ./check_cmds.sh $(ANTLR_GENERATED_FILES) : OpenPearl.g4 
	java -jar $(CONFIG_COMPILER_ANTLR) -no-listener -visitor -package org.openpearl.compiler -o org/openpearl/compiler $(ANTLR_FLAGS) OpenPearl.g4

###############################################################################

prl:
	test -d $(OUTPUT_PRL_DIR) || mkdir $(OUTPUT_PRL_DIR)
	#javac -cp .:$(CONFIG_COMPILER_ANTLR):$(MINIMALJSON):$(APACHE_LANG3) -d $(OUTPUT_PRL_DIR) $(JAVAC_FLAGS) org/openpearl/compiler/Compiler.java
	javac -cp .:$(CONFIG_COMPILER_ANTLR) -d $(OUTPUT_PRL_DIR) $(JAVAC_FLAGS) org/openpearl/compiler/Compiler.java
	rm -f openpearl_compiler.jar
	cp OpenPearlCpp.stg $(OUTPUT_PRL_DIR)
	cp IMC.stg $(OUTPUT_PRL_DIR)

	cd $(OUTPUT_PRL_DIR) && jar cf  openpearl_compiler.jar .
#	cd $(OUTPUT_PRL_DIR) && jar cf  openpearl_compiler.jar \
#				    org/openpearl/compiler/*.class \
#	                            org/openpearl/compiler/SymbolTable/*.class \
#	                            org/openpearl/compiler/ControlFlowGraph/*.class \
#	                            org/openpearl/compiler/SemanticAnalysis/*.class \
#	                            org/openpearl/compiler/Exception/*.class \
#	                            org/openpearl/compiler/Graph/*.class \
#	                            org/openpearl/compiler/DeadLockDetection/*.class \
#	                            org/openpearl/compiler/DeadLockDetection/ControlFlowGraphEntities/*.class \
#	                            org/openpearl/compiler/DeadLockDetection/GraphUtils/*.class \
#				    OpenPearlCpp.stg IMC.stg

##############################################################################

pp:
	test -d $(OUTPUT_PP_DIR) || mkdir $(OUTPUT_PP_DIR)
	javac -cp .:$(CONFIG_COMPILER_ANTLR) -d $(OUTPUT_PP_DIR) $(JAVAC_FLAGS) org/openpearl/preprocessor/Preprocessor.java
	rm -f openpearl_preprocessor.jar
	cd $(OUTPUT_PP_DIR) && jar cf  openpearl_preprocessor.jar \
				    org/openpearl/preprocessor/*.class

##############################################################################

test:
	../regression.sh

###############################################################################

prepare:
	touch $(CONFIG_INSTALL_Target)/bin/openpearl_compiler.jar 
	chmod 777 $(CONFIG_INSTALL_Target)/bin/openpearl_compiler.jar 
	touch $(CONFIG_INSTALL_Target)/bin/openpearl_preprocessor.jar 
	chmod 777 $(CONFIG_INSTALL_Target)/bin/openpearl_preprocessor.jar 

install: gen prl pp prl_cc.inc
#	cp prl_c $(INSTALL_DIR)
#	cp smallpearl_compiler.jar $(INSTALL_DIR)
	cp $(OUTPUT_PRL_DIR)/openpearl_compiler.jar $(CONFIG_INSTALL_Target)/bin
	cp $(OUTPUT_PP_DIR)/openpearl_preprocessor.jar $(CONFIG_INSTALL_Target)/bin
	@echo 
	@echo '========================================='
	@echo '==             SUCCESS                 =='
	@echo '========================================='
	@echo 

.PHONY: prl_cc.inc
prl_cc.inc:
	echo "##### auto generated  start " > ../prl_cc.inc
	echo 'java -cp $(CONFIG_INSTALL_Target)/bin/openpearl_compiler.jar:$(CONFIG_COMPILER_ANTLR)  org/openpearl/compiler/Compiler \' >> ../prl_cc.inc
	echo '   $$compilerFlags $$prlccinput.prl $$sourceOption \' >> ../prl_cc.inc
	# echo '   $$compilerFlags $$PRLFLAGS $$prlccinput.prl \' >> ../prl_cc.inc
	echo '   --imc-output $$fn.xml \' >> ../prl_cc.inc
	echo '   --output $$outputPrlCC ' >> ../prl_cc.inc	
	echo "##### auto generated end " >> ../prl_cc.inc

###############################################################################

clean:
	$(RM) org/openpearl/compiler/OpenPearl.tokens
	$(RM) org/openpearl/compiler/OpenPearl.interp
	$(RM) org/openpearl/compiler/OpenPearlBaseVisitor.java
	$(RM) org/openpearl/compiler/OpenPearlLexer.java
	$(RM) org/openpearl/compiler/OpenPearlLexer.tokens
	$(RM) org/openpearl/compiler/OpenPearlLexer.interp
	$(RM) org/openpearl/compiler/OpenPearlParser.java
	$(RM) org/openpearl/compiler/OpenPearlVisitor.java
	$(RM) $(OUTPUT_PRL_DIR)/org/openpearl/compiler/*.class
	$(RM) $(OUTPUT_PRL_DIR)/org/openpearl/compiler/SymbolTable/*.class
	$(RM) $(OUTPUT_PRL_DIR)/org/openpearl/compiler/SemanticAnalysis/*.class
	$(RM) $(OUTPUT_PRL_DIR)/org/openpearl/compiler/AST/*.class
	$(RM) $(OUTPUT_PRL_DIR)/org/openpearl/compiler/ControlFlowGraph/*.class
	$(RM) $(OUTPUT_PRL_DIR)/org/openpearl/compiler/DeadLockDetection/*.class
	$(RM) $(OUTPUT_PRL_DIR)/org/openpearl/compiler/Exception/*.class
	$(RM) $(OUTPUT_PRL_DIR)/org/openpearl/compiler/Graph/*.class
	$(RM) $(OUTPUT_PRL_DIR)/org/openpearl/preprocessor/*.class
	$(RM) $(OUTPUT_PP_DIR)/*.jar
	$(RM) $(OUTPUT_PRL_DIR)/*.jar
	$(RM) $(OUTPUT_PRL_DIR)/org/openpearl/preprocessor/*.class
	$(RM) $(OUTPUT_PRL_DIR)/*.stg
	$(RM) ../prl_cc.inc

###############################################################################

