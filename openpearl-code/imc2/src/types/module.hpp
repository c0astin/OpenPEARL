#ifndef IMC_TYPES_MODULE_HPP
#define IMC_TYPES_MODULE_HPP

#include <vector>
#include <string>

#include "types/common.hpp"
#include "types/problem.hpp"
#include "types/system.hpp"

namespace imc {
    namespace types {
        namespace module {
            template <typename T>
            using optional  = ::std::experimental::optional<T>;
            using path      = ::std::experimental::filesystem::path;

            using FileLocation  = types::common::FileLocation;
            using Attribute     = types::common::Attribute;
            using DataSpec      = types::common::DataSpec;
            using Problem       = types::problem::Problem;
            using System        = types::system::System;

            struct Module {
                std::vector<Problem> problem_parts;
                std::vector<System>  system_parts;

                Module(std::vector<System> system, std::vector<Problem> problem) noexcept
                    : problem_parts(problem)
                    , system_parts(system)
                {
                    // Nothing
                }
            };


        } // module
    } // types
} // imc

#endif // IMC_TYPES_MODULE_HPP

