TESTS=$(sort $(wildcard *.prl))
SCRIPTDIR=..
.PHONY: all clean $(TESTS)

all: $(TESTS)
	$(SCRIPTDIR)/../../errorChecker/runTest.sh $(TESTS) 

$(TESTS):
	$(SCRIPTDIR)/../../errorChecker/runTest.sh $@

clean:
	@rm -f *.out
	@rm -f *.xml
	@rm -f *.cc
	@rm -f *.log
	@rm -f *.exp
	@rm -f *.err
	@rm -f system.cc 
