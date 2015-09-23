all:
	@cat Makefile

clean:
	lein clean

test:
	lein test \
		aggregate-test \
		core-test \
		errors-test \
		event-test \
		index-test \
		play-test \
		projection-test \
		schema-test \
		store-test \
		validators-test

t: test

.PHONY: test
.PHONY: t
