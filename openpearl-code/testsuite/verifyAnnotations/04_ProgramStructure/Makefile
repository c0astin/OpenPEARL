TESTS=$(sort $(wildcard *.prl))
SCRIPTDIR=..

all: $(TESTS)
	$(SCRIPTDIR)/../../errorChecker/runTest.sh $(TESTS) 

clean:
	@rm -f *.out
	@rm -f *.xml
	@rm -f *.cc
	@rm -f *.log
	@rm -f *.exp
	@rm -f system.cc 
