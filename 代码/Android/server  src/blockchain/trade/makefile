TOPDIR = ./..

CC = g++
CFLAGS = -O2 -I$(TOPDIR)/include $$(mysql_config --cflags)
LDFLAGS = -lpthread $$(mysql_config --libs)
SRC = $(wildcard *.cpp)
OBJ = $(patsubst %.cpp,%.o,$(SRC))
LIB = $(TOPDIR)/lib$(shell basename $$(pwd)).so

$(LIB):$(OBJ)
	$(CC) -shared -o $@ $^ $(LDFLAGS)

%.o:%.cpp
	$(CC) $(CFLAGS) -fPIC -o $@ -c $<
	
.PHONY:clean
clean:
	-rm -rf $(LIB) $(OBJ)
