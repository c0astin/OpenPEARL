#include <iostream>
#include <experimental/filesystem>

#include <pugixml.hpp>

#include "reader/reader.hpp"
#include "logger.hpp"

namespace imc {
    namespace reader {
        using path      = ::std::experimental::filesystem::path;

        pugi::xml_parse_result load_xml(const path& p) noexcept {
            pugi::xml_document doc;
            std::string path_str       = p;
            pugi::xml_parse_result res = doc.load_file(path_str.c_str());

            // TODO remove output
            ::imc::logger::log::info() << "Load result: " << res.description() << std::endl;

            return res;
        }
    }
}
