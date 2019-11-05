#ifndef IMC_READER_PLATFORM_HPP
#define IMC_READER_PLATFORM_HPP

#include <experimental/filesystem>
#include <experimental/optional>

#include <pugixml.hpp>

#include "reader/reader.hpp"
#include "types/platform.hpp"
#include "types/problem.hpp"

namespace imc {
    namespace reader {

        template <typename T>
        using optional  = ::std::experimental::optional<T>;
        using path      = ::std::experimental::filesystem::path;

        using Platform  = types::platform::Platform;
        using Problem   = types::problem::Problem;

        /** @brief Reader implementation for reading the platform XML file
         */
        class PlatformXMLReader : public Reader<Platform> {
            private:
                path&& searchpath;

            public:
                PlatformXMLReader(path&& filepath, path&& searchpath)
                    : Reader(std::move(filepath))
                    , searchpath(std::move(searchpath))
                {
                    // empty
                }

                PlatformXMLReader(PlatformXMLReader&&) = default;

                /** @brief Call operator required function overload
                 *
                 * @todo Port call operator to return `variant<T, E>`.
                 */
                optional<Platform> operator()(void) override;
        };

    } // reader
} // imc

#endif // IMC_READER_PLATFORM_HPP

