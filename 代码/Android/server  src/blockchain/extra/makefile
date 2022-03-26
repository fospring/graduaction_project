TOPDIR = ./..

CC = g++
CFLAGS = -O2 -I$(TOPDIR)/include
SRC = $(wildcard *.cpp)
OBJ = $(patsubst %.cpp,%.o,$(SRC))
LIB = $(TOPDIR)/lib$(shell basename $$(pwd)).so

$(LIB):$(OBJ)
	$(CC) -shared -o $@ $^

%.o:%.cpp
	$(CC) $(CFLAGS) -fPIC -o $@ -c $<
	
.PHONY:clean
clean:
	-rm -rf $(LIB) $(OBJ)
