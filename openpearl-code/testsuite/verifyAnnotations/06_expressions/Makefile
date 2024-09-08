.PHONY: all
SUBDIRS =  $(dir $(sort $(wildcard */Makefile)))

all: $(SUBDIRS)

$(SUBDIRS):
	@echo "entring "$@
	@make -C $@ $(MAKECMDGOALS)

.PHONY: $(SUBDIRS) 

clean:
	for dir in $(SUBDIRS); do \
	   make -C $$dir clean; \
	done	
