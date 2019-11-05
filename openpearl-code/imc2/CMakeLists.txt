cmake_minimum_required (VERSION 3.1)

#
# Project settings
#

project (IMC)
set (IMC_VERSION_MAJOR 1)
set (IMC_VERSION_MINOR 0)

set (PROJECT_SOURCE_DIR "src")
set (PROJECT_BINARY_DIR "target")

# configure a header file to pass some of the CMake settings
# to the source code
configure_file (
  "${PROJECT_SOURCE_DIR}/imc.hpp.in"
  "${PROJECT_BINARY_DIR}/imc.hpp"
  )

#
# Libraries
#

set(CMAKE_MODULE_PATH ${CMAKE_SOURCE_DIR}/cmake/Modules)

find_package (Pugixml REQUIRED)
include_directories (BEFORE "${PUGIXML_INCLUDE_DIR}")

find_package (Docopt REQUIRED)
include_directories (BEFORE "${docopt_INCLUDE_DIR}")

find_package (ChaiScript REQUIRED)
include_directories (BEFORE "${chaiscript_INCLUDE_DIR}")

# add the executable
add_subdirectory(src)

# TESTS

enable_testing()

add_test(NAME 001
        COMMAND imc -o /dev/null -S scripts -b tests/001/platform.xml tests/001/module.xml
        )

add_test(NAME 002
        COMMAND imc -o /dev/null -S scripts -b tests/002/platform.xml tests/002/module.xml
        )

add_test(NAME 003
        COMMAND imc -o /dev/null -S scripts -b tests/003/platform.xml tests/003/module.xml
        )

add_test(NAME 004
        COMMAND imc -o /dev/null -S scripts -b tests/004/platform.xml tests/004/module_1.xml tests/004/module_2.xml
        )

add_test(NAME 005
        COMMAND imc -o /dev/null -S scripts -b tests/005/platform.xml tests/005/module.xml
        )

add_test(NAME 006
        COMMAND imc -o /dev/null -S scripts -b tests/006/platform.xml tests/006/module.xml
        )
set_tests_properties(006 PROPERTIES WILL_FAIL TRUE)

add_test(NAME 007
        COMMAND imc -o /dev/null -S scripts -b tests/007/platform.xml tests/007/module.xml
        )

add_test(NAME 008
        COMMAND imc -o /dev/null -S scripts -b tests/008/platform.xml tests/008/module.xml
        )
set_tests_properties(008 PROPERTIES WILL_FAIL TRUE)

add_test(NAME 009
        COMMAND imc -o /dev/null -S scripts -b tests/009/platform.xml tests/009/module.xml
        )
set_tests_properties(009 PROPERTIES WILL_FAIL TRUE)

add_test(NAME 010
        COMMAND imc -o /dev/null -S scripts -b tests/010/platform.xml tests/010/module.xml
        )
set_tests_properties(010 PROPERTIES WILL_FAIL TRUE)

add_test(NAME 011
        COMMAND imc -o /dev/null -S scripts -b tests/011/platform.xml tests/011/module.xml
        )
set_tests_properties(011 PROPERTIES WILL_FAIL TRUE)

