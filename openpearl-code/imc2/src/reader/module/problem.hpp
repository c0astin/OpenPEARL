#ifndef IMC_READER_PROBLEM_HPP
#define IMC_READER_PROBLEM_HPP

#include <experimental/filesystem>
#include <experimental/optional>

#include <pugixml.hpp>

#include "types/platform.hpp"
#include "types/problem.hpp"

namespace imc {
    namespace reader {
        namespace module {

            template <typename T>
            using optional  = ::std::experimental::optional<T>;
            using path      = ::std::experimental::filesystem::path;

            using Platform  = types::platform::Platform;
            using Problem   = types::problem::Problem;

            optional<Problem> fetch_problem(const pugi::xml_node& child, const path& filepath) noexcept;

        } // module
    } // reader
} // imc

#endif // IMC_READER_PROBLEM_HPP

