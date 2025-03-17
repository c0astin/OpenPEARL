.PHONY: all
#SUBDIRS =  04_ProgramStructure 10_inputAndOutput
SUBDIRS =  $(sort $(wildcard */.))
ERRORCHECKER = ../../errorChecker/errorParser ../../errorChecker/genExpectations
export PRLFLAGS=-std=PEARL90 

all: $(ERRORCHECKER) $(SUBDIRS)

$(SUBDIRS):
	@echo "entring "$@
	@make -C $@ $(MAKECMDGOALS)

$(ERRORCHECKER):
	(cd ../../errorChecker; make)

.PHONY: $(SUBDIRS) 

clean:
	for dir in $(SUBDIRS); do \
	   make -C $$dir clean; \
	done	
