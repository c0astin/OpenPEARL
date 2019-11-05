#ifndef IMC_READER_COMMON_HPP
#define IMC_READER_COMMON_HPP

#include <experimental/filesystem>
#include <experimental/optional>

#include <pugixml.hpp>

#include "reader/reader.hpp"
#include "types/platform.hpp"
#include "types/problem.hpp"

namespace imc {
    namespace reader {
        namespace common {

            template<typename T>
            using optional                = ::std::experimental::optional<T>;
            using Attribute               = imc::types::common::Attribute;
            using ParameterType           = imc::types::platform::ParameterType;
            using DataSpec                = imc::types::common::DataSpec;

            optional<ParameterType> ptype_from_name(const std::string& name);
            optional<DataSpec> make_dataspec_from_node(const pugi::xml_node& node);
            optional<std::vector<Attribute>> make_attributes_from_node(const pugi::xml_node& node);

            unsigned int line_from_attribute(const pugi::xml_node node, const char* attr, unsigned int def = 0) noexcept;

        } // common
    } // reader
} // imc

#endif // IMC_READER_COMMON_HPP

