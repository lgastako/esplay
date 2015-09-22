all:
	@cat Makefile

clean:
	lein clean

test:
	lein auto test

t: test

.PHONY: test
.PHONY: t
