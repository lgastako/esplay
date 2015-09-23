all:
	@cat Makefile

clean:
	lein clean

test:
	lein test \
		esplay.aggregate-test \
		esplay.core-test \
		esplay.errors-test \
		esplay.event-test \
		esplay.index-test \
		esplay.play-test \
		esplay.projection-test \
		esplay.schemas-test \
		esplay.store-test \
		esplay.validators-test

t: test

.PHONY: test
.PHONY: t
