TEXFILES = 01-introduction.tex 		\
	02-fundamentals.tex 		\
	03-language_forms.tex		\
	04-program_structure.tex	\
	05-variables_and_constants.tex	\
	06-expressions.tex		\
	07-sequential.tex		\
	08-procedures.tex		\
	09-parallel_activities.tex	\
	10-input_and_output.tex		\
	11-signals.tex			\
	12-preprocessor.tex			\
	90-appendix.tex			\
	macrosShowChanges.tex		\
	macrosShowResult.tex		\
	hyperMacros.tex

BNFFILES=$(wildcard *.bnf)


.PHONY: clean final
include ../configuration/.config

p-report.pdf:	p-report.tex $(TEXFILES) $(BNFFILES)
	pdflatex p-report.tex
	makeindex p-report.idx

final:
	pdflatex p-report.tex
	makeindex p-report.idx
	pdflatex p-report.tex
	makeindex p-report.idx
	pdflatex p-report.tex
	makeindex p-report.idx

clean:
	@echo "cleaning temporary files"
	@rm -rf p-report.cb 
	@rm -rf p-report.cb2
	@rm -rf p-report.log
	@rm -rf p-report.out
	@rm -rf p-report.toc
	@rm -rf p-report.aux
	@rm -rf p-report.idx p-report.ilg p-report.ind

