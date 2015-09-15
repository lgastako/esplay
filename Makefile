all:
	@cat Makefile

test:
	lein auto test

t: test

.PHONY: test
.PHONY: t
