CC = g++

CFLAGS = -O2 -I./include
DIRS = $(shell ls -l  | grep '^d' | awk '{ if ($$9 != "include") print $$9}')
LDFLAGS = -lpthread -ljsoncpp -lcurl -L. $(patsubst %,-l%,$(DIRS)) -Wl,-rpath=.
EXEC = blockchain

all:dirmake $(EXEC)

dirmake:
	@for sub in $(DIRS); do (cd $$sub && make); done

$(EXEC):main.o
	$(CC) -o $@ $^ $(LDFLAGS)

main.o:main.cpp
	$(CC) -o $@ -c $^ $(CFLAGS)

.PHONY:clean	
clean:
	@for sub in $(DIRS); do (cd $$sub && make clean); done
	-rm -rf main.o $(EXEC)
