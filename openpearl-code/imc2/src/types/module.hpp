/*
 * changes Nov-2019 (rm)
 *   max 1 system part per module
 *   max 1 problem part per module
 */
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
                optional<Problem> problem_part;
                optional<System>  system_part;
                std::string module_name;
                std::string file_name;

                Module(optional<System> & system, optional<Problem>& problem, std::string m_name, std::string f_name) noexcept
                    : problem_part(problem)
                    , system_part(system)
					, module_name(m_name)
					, file_name(f_name)
                {
                    // Nothing
                }
            };


        } // module
    } // types
} // imc

#endif // IMC_TYPES_MODULE_HPP

