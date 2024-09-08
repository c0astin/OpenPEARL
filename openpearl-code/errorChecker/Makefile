TARGETS = genExpectations errorParser
CFLAGS += -g

all: $(TARGETS)

genExpectations: genExpectations.c config.h
	gcc $(CFLAGS) genExpectations.c -o genExpectations

errorParser: errorParser.c config.h
	gcc $(CFLAGS) errorParser.c -o errorParser

clean:
	rm -f $(TARGETS)

