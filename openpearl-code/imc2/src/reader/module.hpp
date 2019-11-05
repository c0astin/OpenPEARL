#ifndef IMC_READER_MODULE_HPP
#define IMC_READER_MODULE_HPP

#include <experimental/filesystem>
#include <experimental/optional>

#include <pugixml.hpp>

#include "reader/reader.hpp"
#include "reader/module/problem.hpp"
#include "reader/module/system.hpp"
#include "types/module.hpp"
#include "types/platform.hpp"
#include "types/problem.hpp"

namespace imc {
    namespace reader {

        template <typename T>
        using optional  = ::std::experimental::optional<T>;
        using path      = ::std::experimental::filesystem::path;

        using Module           = types::module::Module;
        using Platform         = types::platform::Platform;

        /** @brief Reader implementation for reading the module XML file
         */
        class ModuleXMLReader : public Reader<Module> {
            public:
                ModuleXMLReader(path filepath)
                    : Reader(filepath)
                {
                    // nothing
                }

                ModuleXMLReader(ModuleXMLReader&&) = default;

                /** @brief Call operator required function overload
                 *
                 * @todo Port call operator to return `variant<T, E>`.
                 */
                optional<Module> operator()(void) override;
        };

        ModuleXMLReader module_reader_from_str(std::string& path) noexcept;

    } // reader
} // imc

#endif // IMC_READER_MODULE_HPP

