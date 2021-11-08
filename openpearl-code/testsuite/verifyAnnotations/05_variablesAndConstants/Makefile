.PHONY: all
SUBDIRS =  $(sort $(wildcard */.))

all: $(SUBDIRS)

$(SUBDIRS):
	@echo "entring "$@
	@make -C $@ $(MAKECMDGOALS)

.PHONY: $(SUBDIRS) 

clean:
	for dir in $(SUBDIRS); do \
	   make -C $$dir clean; \
	done	
