include_directories(
    ${CMAKE_SOURCE_DIR}/src
    ${CMAKE_SOURCE_DIR}/target

    ${PUGIXML_INCLUDE_DIRS}
    ${docopt_INCLUDE_DIRS}
    ${chaiscript_INCLUDE_DIRS}
)

set(SOURCE_FILES
    main.cpp
    logger.cpp
    util.cpp
    reader/platform.cpp
    reader/module/problem.cpp
    reader/module/system.cpp
    reader/module.cpp
    reader/common.cpp
    reader/reader.cpp
    types/platform.cpp
    types/system.cpp
    types/problem.cpp
    checker/check.cpp
    checker/checker.cpp
    checker/checks/check_associations_provided.cpp
    checker/checks/check_each_spc_has_one_dcl.cpp
    checker/checks/check_params_match.cpp
    checker/checks/check_pin_layout.cpp
    checker/checks/check_username_sysname_mappings.cpp
    checker/error.cpp
    expr/computer.cpp
    codegen/codegen.cpp
)


#
# Compiler configuration
#
set(CMAKE_CXX_STANDARD 14)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

set(CMAKE_CXX_FLAGS "-g -Wall -Wextra -Wpedantic")

message (STATUS "C++ Standard = ${CMAKE_CXX_STANDARD}")
message (STATUS "C++ Flags    = ${CMAKE_CXX_FLAGS}")

add_executable(imc ${SOURCE_FILES})
target_link_libraries(imc dl)
target_link_libraries(imc pthread)
target_link_libraries(imc "${docopt_LIBRARY}")
target_link_libraries(imc "${PUGIXML_LIBRARY}")
target_link_libraries(imc stdc++fs)
