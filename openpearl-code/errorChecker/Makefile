TARGETS = genExpectations errorParser
all: $(TARGETS)

genExpectations: genExpectations.c config.h
	gcc genExpectations.c -o genExpectations

errorParser: errorParser.c config.h
	gcc errorParser.c -o errorParser

clean:
	rm -f $(TARGETS)

