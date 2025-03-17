.PHONY: all build run clean

all: build 

build:
	(cd build; make redo)
	(cd verifyAnnotations; make)
	(cd verifyAnnotationsPEARL90; make)

run:
	(cd run; make all)

clean:
	(cd build; make clean)
	(cd run; make clean)
	(cd verifyAnnotations; make clean)
	
