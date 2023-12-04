.PHONY: all
#SUBDIRS =  04_ProgramStructure 10_inputAndOutput
#SUBDIRS =  $(dir $(sort $(wildcard */Makefile)))
SUBDIRS =  $(dir $(sort $(wildcard [0..9]*/Makefile)))


ERRORCHECKER = ../../errorChecker/errorParser ../../errorChecker/genExpectations
export PRLFLAGS=-std=OpenPEARL

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
