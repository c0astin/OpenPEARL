# The following lines of boilerplate have to be in your project's
# CMakeLists in this exact order for cmake to work correctly
cmake_minimum_required(VERSION 3.5)

set(CMAKE_CXX_STANDARD 11)
set(CMAKE_CXX_STANDARD_REQUIRED true)
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -mtext-section-literals")
add_compile_definitions(OPENPEARL_ESP32)

set(EXTRA_COMPONENT_DIRS "./OpenPEARL" "./main")

include($ENV{IDF_PATH}/tools/cmake/project.cmake)
project(OpenPEARL-project)
