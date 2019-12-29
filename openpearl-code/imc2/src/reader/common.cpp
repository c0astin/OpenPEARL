/*
 * changes: 2019-11-18 (rm)
 *    error messages removed/moved to callee
 */
#include <iostream>
#include <vector>
#include <string>
#include <stdexcept>
#include <experimental/filesystem>
#include <experimental/optional>

#include "logger.hpp"
#include "util.hpp"
#include "types/common.hpp"

template<typename T>
using optional                = ::std::experimental::optional<T>;
using Attribute               = imc::types::common::Attribute;
using ParameterType           = imc::types::common::ParameterType;
using DataSpec                = imc::types::common::DataSpec;

namespace imc {
    namespace reader {
        namespace common {
            optional<ParameterType> ptype_from_name(const std::string& name) {
                if (name == "CHAR") {
                    return ParameterType::Char;
                } else if (name == "FIXED") {
                    return ParameterType::Fixed;
                } else if (name == "BIT") {
                    return ParameterType::Bit;
                } else {
                    // ::imc::logger::log::error() << "Unknown parameter type: " << name << std::endl;
                    // the error message is printed by the callee
                }

                return {};
            }

            optional<DataSpec> make_dataspec_from_node(const pugi::xml_node& node) {
                ::imc::logger::log::debug() << "Handling " << node << std::endl;
                if (node.first_child().type() == pugi::xml_node_type::node_pcdata) {
                    return node.first_child().value();
                } else {
                   // ::imc::logger::log::error() << "Type Error: Expected PCDATA type, got " << node.type() << std::endl;
                    return {};
                }
            }

            /** Parse attributes from the passed node
             */
            optional<std::vector<Attribute>> make_attributes_from_node(const pugi::xml_node& node) {
                ::imc::logger::log::debug() << "Handling " << node << std::endl;
                if (node.first_child().type() == pugi::xml_node_type::node_pcdata) {
                    std::string value(node.first_child().value());
                    return imc::util::strsplit(value, ',');
                } else {
                    //::imc::logger::log::error()
                    //    << "Type Error: While constructing Attribute: Expected PCDATA type, got "
                    //    << node.type() << std::endl;
                    return {};
                }
            }

            unsigned int line_from_attribute(
                    const pugi::xml_node node,
                    const char* attr,
                    unsigned int def = 0
                ) noexcept
            {
                auto line_attr = node.attribute(attr);

                if (!line_attr) {
                    ::imc::logger::log::warn()
                        << "Missing '" << attr << "' attribute in XML node 'SPC'"
                        << std::endl
                        << "Defaulting to = 0"
                        << std::endl;
                }

                return line_attr.as_uint(def);
            }

        }
    }
}



