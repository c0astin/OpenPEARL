#ifndef IMC_READER_SYSTEM_HPP
#define IMC_READER_SYSTEM_HPP

#include <experimental/filesystem>
#include <experimental/optional>

#include <pugixml.hpp>

#include "types/platform.hpp"
#include "types/system.hpp"

namespace imc {
    namespace reader {
        namespace module {

            template <typename T>
            using optional  = ::std::experimental::optional<T>;
            using path      = ::std::experimental::filesystem::path;

            using Platform  = types::platform::Platform;
            using System    = types::system::System;

            optional<System> fetch_system(
                    const pugi::xml_node& child,
                    const path& filepath
                ) noexcept;

        } // module
    } // reader
} // imc

#endif // IMC_READER_SYSTEM_HPP

