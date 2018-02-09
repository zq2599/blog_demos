version=0.09

name=lua-redis-parser
dist=$(name)-$(version)

LUA_VERSION =   5.1

# See http://lua-users.org/wiki/BuildingModules for platform specific
# details.

## Linux/BSD
PREFIX ?=          /usr/local
LDFLAGS +=         -shared

## OSX (Macports)
#PREFIX ?=          /opt/local
#LDFLAGS +=         -bundle -undefined dynamic_lookup

LUA_INCLUDE_DIR ?= $(PREFIX)/include
LUA_LIB_DIR ?=     $(PREFIX)/lib/lua/$(LUA_VERSION)

#CFLAGS ?=          -g -Wall -pedantic -fno-inline
CFLAGS ?=          -g -O -Wall
override CFLAGS += -fpic -I$(LUA_INCLUDE_DIR)

INSTALL ?= install

.PHONY: all clean dist test t

#CC = gcc
RM = rm -f

all: parser.so

redis-parser.o: ddebug.h

parser.so: redis-parser.o
	$(CC) $(LDFLAGS) -o $@ $^

install:
	$(INSTALL) -d $(DESTDIR)/$(LUA_LIB_DIR)/redis
	$(INSTALL) parser.so $(DESTDIR)/$(LUA_LIB_DIR)/redis

clean:
	$(RM) *.so *.o redis/*.so

test: all
	$(INSTALL) -d redis
	$(INSTALL) parser.so redis/
	prove -r t

valtest: parser.so
	$(INSTALL) -d redis
	$(INSTALL) parser.so redis/
	TEST_LUA_USE_VALGRIND=1 prove -r t

t: parser.so
	$(INSTALL) -d redis
	$(INSTALL) parser.so redis/
	TEST_LUA_USE_VALGRIND=1 prove t/sanity.t

dist:
	git archive --prefix="$(dist)/" master | \
		gzip -9 > "$(dist).tar.gz"
	git archive --prefix="$(dist)/" \
		-o "$(dist).zip" master

