#TESTS=$(sort $(wildcard *.prl))
SCRIPTDIR=../..
TESTS=$(shell $(SCRIPTDIR)/../getTestNumbers.sh *.prl)
.PHONY: all clean $(TESTS)

all: $(TESTS)
	$(SCRIPTDIR)/../../errorChecker/runTest.sh $(TESTS) 

#$(TESTS):
#	$(SCRIPTDIR)/../../errorChecker/runTest.sh $@

clean:
	@rm -f *.out *.xml *.cc *.log *.exp *.err *.dot
	@rm -f system.cc 
