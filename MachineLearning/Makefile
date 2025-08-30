#####################################################################################
# Specify:
# (1) The folder containing the source files + name of the compiled program
# (2) Name of the program
#####################################################################################
SRC := src
BIN := program

OBJ := obj
MKDIR := mkdir -p
RM := rm -rf

rwildcard = $(foreach d,$(wildcard $1*),$(call rwildcard,$d/,$2)$(filter $(subst *,%,$2),$d))

SRCs := $(call rwildcard,$(SRC)/,*.cpp)  # Recursively find all .cpp files

OBJs := $(patsubst $(SRC)/%.cpp, $(OBJ)/%.o, $(SRCs))

CXX := g++ -std=c++17 -mconsole
CPPFLAGS := -Iinclude -Iinclude/ann -Iinclude/tensor -Iinclude/sformat -Idemo -Isrc
CXXFLAGS := -pthread #-Wall
LDLIBS := -lm -lpthread

#############################################################################################
# Note:
# (1) Use -Iinclude/tensor: because put xtensor and its headers inside of folder tensor
# (2) Use -Iinclude/sformat: because put sformat and its headers inside of folder sformat
# (3) Use -Iinclude/ann: because put header files of ann inside of folder ann
# (4) Use -Idemo: because put header files of demos inside of this folder
#############################################################################################

# Default build target
all: $(BIN)

# Link all object files into the final executable
$(BIN): $(OBJs)
	$(CXX) $(CXXFLAGS) $(CPPFLAGS) $(OBJs) -o $@ $(LDLIBS)

# Compile .cpp files into .o files
$(OBJ)/%.o: $(SRC)/%.cpp
	$(MKDIR) $(dir $@)
	$(CXX) $(CXXFLAGS) $(CPPFLAGS) -c $< -o $@

# Clean rule to remove generated files
clean:
	$(RM) $(BIN)
	$(RM) $(OBJ)