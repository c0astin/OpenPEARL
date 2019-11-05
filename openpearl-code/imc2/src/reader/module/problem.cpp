#include <experimental/filesystem>

#include <pugixml.hpp>

#include "logger.hpp"
#include "reader/common.hpp"
#include "reader/reader.hpp"
#include "reader/module/problem.hpp"
#include "types/problem.hpp"

template<typename T>
using optional                = ::std::experimental::optional<T>;
using path                    = ::std::experimental::filesystem::path;
using Attribute               = imc::types::common::Attribute;
using DataSpec                = imc::types::common::DataSpec;
using FileLocation            = imc::types::common::FileLocation;
using Type                    = imc::types::problem::Type;
using Specification           = imc::types::problem::Specification;
using Declaration             = imc::types::problem::Declaration;

optional<std::string> make_name_from_node(const pugi::xml_node& node, const char* errtext) {
    auto name_attr = node.attribute("name");
    if (!name_attr) {
        ::imc::logger::log::error() << errtext << std::endl;
        return {};
    }
    std::string name = name_attr.value();
    return name;
}

optional<Type> make_type_from_node(const pugi::xml_node& node, const char* errtext) {
    auto type_attr = node.attribute("type");
    if (!type_attr) {
        ::imc::logger::log::error() << errtext << std::endl
                    << "skipping..." << std::endl;
        return {};
    }
    std::string tyname = type_attr.value();

    if (tyname == "dation") {
        return Type::Dation;
    } else if (tyname == "interrupt") {
        return Type::Interrupt;
    } else if (tyname == "Signal") {
        return Type::Signal;
    } else {
        ::imc::logger::log::error() << "Unknown parameter type: " << tyname << std::endl;
    }

    return {};
}

optional<std::vector<Specification>> fetch_specifications(
        const pugi::xml_node& problem,
        const path& filepath
    ) noexcept
{
    std::vector<Specification> l;

    for (pugi::xml_node child : problem.children()) {
        if (std::string(child.name()) == "spc") {
            auto name = make_name_from_node(child, "'module.problem.spc<name>' missing");
            if (!name) { return {}; }

            auto ty = make_type_from_node(child, "'module.problem.spc<type>' missing");
            if (!ty) { return {}; }

            unsigned int line = imc::reader::common::line_from_attribute(child, "line", 0);

            optional<DataSpec>    dataspec;
            std::vector<Attribute>  attributes;

            for (pugi::xml_node spc_child : child.children()) {
                std::string spc_child_name(spc_child.name());

                if (spc_child_name == "attributes") {
                    auto attrs = imc::reader::common::make_attributes_from_node(spc_child);
                    if (!attrs) {
                        ::imc::logger::log::error() << "Constructing Attribute failed" << std::endl;
                        return {};
                    }

                    attributes = std::vector<Attribute>(
                        std::make_move_iterator(std::begin(*attrs)),
                        std::make_move_iterator(std::end(*attrs))
                    );
                } else if (spc_child_name == "data") {
                    dataspec = imc::reader::common::make_dataspec_from_node(spc_child);
                    if (!dataspec) {
                        ::imc::logger::log::error() << "Constructing DataSpec failed" << std::endl;
                        return {};
                    }
                } else {
                    ::imc::logger::log::error()
                        << "Unknown node type in 'problem.spc<name = "
                        << spc_child_name << ">'"
                        << std::endl;
                    return {};
                }
            }

            FileLocation fl;
            fl.set_file(filepath);
            fl.set_line(line);

            auto spc = Specification(
                    std::move(*ty),
                    std::move(*name),
                    std::move(attributes),
                    std::move(*dataspec),
                    std::move(fl));

            l.push_back(std::move(spc));
        }
    }

    ::imc::logger::log::debug() << "Constructed: [ " << std::endl;
    for (auto& elem: l) {
        ::imc::logger::log::debug() << "  " << elem << ", " << std::endl;
    }
    ::imc::logger::log::debug() << "]" << std::endl;
    return l;
}

optional<std::vector<Declaration>> fetch_declarations(
        const pugi::xml_node& problem,
        const path& filepath
    ) noexcept
{
    std::vector<Declaration> l;

    for (pugi::xml_node child : problem.children()) {
        if (std::string(child.name()) == "dcl") {
            auto name = make_name_from_node(child, "'module.problem.dcl<name>' missing");
            if (!name) { return {}; }

            auto ty = make_type_from_node(child, "'module.problem.dcl<type>' missing");
            if (!ty) { return {}; }

            unsigned int line = imc::reader::common::line_from_attribute(child, "line", 0);

            optional<DataSpec>    dataspec;
            std::vector<Attribute>  attributes;

            for (pugi::xml_node spc_child : child.children()) {
                std::string spc_child_name(spc_child.name());

                if (spc_child_name == "attributes") {
                    auto attrs = imc::reader::common::make_attributes_from_node(spc_child);
                    if (!attrs) {
                        ::imc::logger::log::error() << "Constructing Attribute failed" << std::endl;
                        return {};
                    }

                    attributes = std::vector<Attribute>(
                        std::make_move_iterator(std::begin(*attrs)),
                        std::make_move_iterator(std::end(*attrs))
                    );
                } else if (spc_child_name == "data") {
                    dataspec = imc::reader::common::make_dataspec_from_node(spc_child);
                    if (!dataspec) {
                        ::imc::logger::log::error() << "Constructing DataSpec failed" << std::endl;
                        return {};
                    }
                } else {
                    ::imc::logger::log::error()
                        << "Unknown node type in 'problem.spc<name = "
                        << spc_child_name << ">'"
                        << std::endl;
                    return {};
                }
            }

            FileLocation fl;
            fl.set_file(filepath);
            fl.set_line(line);

            auto dcl = Declaration(
                    std::move(*ty),
                    std::move(*name),
                    std::move(attributes),
                    std::move(*dataspec),
                    std::move(fl));

            l.push_back(std::move(dcl));
        }
    }

    ::imc::logger::log::debug() << "Constructed: [ " << std::endl;
    for (auto& elem: l) {
        ::imc::logger::log::debug() << "  " << elem << ", " << std::endl;
    }
    ::imc::logger::log::debug() << "]" << std::endl;
    return l;
}

namespace imc {
    namespace reader {
        namespace module {
            optional<Problem> fetch_problem(const pugi::xml_node& child, const path& filepath) noexcept {
                std::vector<Specification> spcs;
                std::vector<Declaration> decls;

                {
                    ::imc::logger::log::debug() << ">>>>>>>>>>>>>>>>" << std::endl;
                    ::imc::logger::log::debug() << "Fetching SPCs" << std::endl;
                    auto opt_specifications = fetch_specifications(child, filepath);
                    if (!opt_specifications) {
                        ::imc::logger::log::error() << "Error while fetching specifications" << std::endl;
                        return {};
                    }
                    spcs = *opt_specifications;
                    ::imc::logger::log::debug() << "Fetching specifications worked" << std::endl;
                    ::imc::logger::log::debug() << "<<<<<<<<<<<<<<<<" << std::endl;
                }

                {
                    ::imc::logger::log::debug() << ">>>>>>>>>>>>>>>>" << std::endl;
                    ::imc::logger::log::debug() << "Fetching DCLs" << std::endl;
                    auto opt_decls = fetch_declarations(child, filepath);
                    if (!opt_decls) {
                        ::imc::logger::log::error() << "Error while fetching declarations" << std::endl;
                        return {};
                    }
                    decls = *opt_decls;
                    ::imc::logger::log::debug() << "Fetching declarations worked" << std::endl;
                    ::imc::logger::log::debug() << "<<<<<<<<<<<<<<<<" << std::endl;
                }

                auto prob = Problem(spcs, decls);
                return prob;
            }
        }
    }
}


