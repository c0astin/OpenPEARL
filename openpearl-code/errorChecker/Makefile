TARGETS = genExpectations errorParser
all: $(TARGETS)

genExpectations: genExpectations.c
	gcc genExpectations.c -o genExpectations

errorParser: errorParser.c
	gcc errorParser.c -o errorParser

clean:
	rm -f $(TARGETS)

