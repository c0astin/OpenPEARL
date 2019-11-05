#include <vector>
#include <string>

#include <experimental/filesystem>
#include <experimental/optional>

#include <pugixml.hpp>

#include "logger.hpp"
#include "reader/common.hpp"
#include "reader/reader.hpp"
#include "reader/module/system.hpp"
#include "types/system.hpp"
#include "types/common.hpp"

template<typename T>
using optional               = ::std::experimental::optional<T>;
using path                    = ::std::experimental::filesystem::path;
using ParameterInstance      = imc::types::system::ParameterInstance;
using FileLocation           = imc::types::common::FileLocation;
using UserName               = imc::types::system::UserName;
using SystemName             = imc::types::system::SystemName;
using SystemNameResourceType = imc::types::system::SystemNameResourceType;
using System                 = imc::types::system::System;

optional<ParameterInstance> make_param_instance_from_node(const pugi::xml_node& node) noexcept {
    auto node_name = std::string(node.name());
    auto ptype = ::imc::reader::common::ptype_from_name(node_name);
    if (!ptype) {
        ::imc::logger::log::error() << "Error constructing ParameterType instance" << std::endl;
        return {};
    }

    auto data = node.first_child();
    if (!data) {
        ::imc::logger::log::error() << "Error constructing ParameterType instance: No data" << std::endl;
        return {};
    }

    std::string value(data.value());
    if (value.empty()) {
        ::imc::logger::log::error() << "Error constructing ParameterType instance: No data in node" << std::endl;
        return {};
    }

    ::imc::logger::log::debug() << "Building ParameterInstance" << std::endl;
    auto pi = ParameterInstance(*ptype, value);
    ::imc::logger::log::debug() << pi << std::endl;

    return pi;
}

optional<std::vector<UserName>> fetch_usernames(
        const pugi::xml_node& node,
        const path& filepath
    ) noexcept
{
    std::vector<UserName> names;

    for (auto child: node.children()) {
        if (std::string(child.name()) == "username") {
            auto name = child.attribute("name");
            unsigned int line = imc::reader::common::line_from_attribute(child, "line", 0);

            auto first_child = child.first_child();

            if (std::string(first_child.name()) == "sysname") {
                auto sysname = first_child.attribute("name");
                if (!sysname) {
                    ::imc::logger::log::error() << "System name missing" << std::endl;
                    return {};
                }

                std::vector<ParameterInstance> params;
                auto parameter_child = first_child.first_child();
                if (parameter_child && std::string(parameter_child.name()) == "parameters") {
                    for (pugi::xml_node param_node : parameter_child.children()) {
                        auto param = make_param_instance_from_node(param_node);
                        if (!param) {
                            ::imc::logger::log::error() << "Constructing ParameterInstance failed" << std::endl;
                            return {};
                        }

                        ::imc::logger::log::debug()
                            << "Constructing ParameterInstance succeeded: "
                            << *param
                            << std::endl;
                        params.push_back(std::move(*param));
                    }
                }

                FileLocation fl;
                fl.set_file(filepath);
                fl.set_line(line);

                auto un = UserName(
                        std::move(std::string(name.as_string())),
                        std::move(std::string(sysname.as_string())),
                        std::move(fl),
                        std::move(params));

                names.push_back(std::move(un));
            } else {
                ::imc::logger::log::error() << "Unknown child: " << first_child.name() << std::endl;
                return {};
            }
        }
    }

    return names;
}

optional<std::vector<SystemName>> fetch_systemnames(const pugi::xml_node& node) noexcept {
    std::vector<SystemName> names;

    ::imc::logger::log::debug() << "Parsing System names" << std::endl;
    for (auto child: node.children()) {
        if (std::string(child.name()) == "configuration") {
            auto first_child = child.first_child();

            if (std::string(first_child.name()) == "sysname") {
                auto sysname = first_child.attribute("name");
                if (!sysname) {
                    ::imc::logger::log::error() << "System name missing" << std::endl;
                    return {};
                }

                std::vector<ParameterInstance> params;
                auto parameter_child = first_child.first_child();
                if (parameter_child && std::string(parameter_child.name()) == "parameters") {
                    for (pugi::xml_node param_node : parameter_child.children()) {
                        auto param = make_param_instance_from_node(param_node);
                        if (!param) {
                            ::imc::logger::log::error() << "Constructing ParameterInstance failed" << std::endl;
                            return {};
                        }

                        params.push_back(std::move(*param));
                    }
                }

                auto sn = SystemName(
                        std::move(std::string(sysname.as_string())),
                        std::move(SystemNameResourceType::Configuration),
                        std::move(params));

                names.push_back(std::move(sn));
            } else {
                ::imc::logger::log::error() << "Unknown child: " << first_child.name() << std::endl;
                return {};
            }
        }
    }

    ::imc::logger::log::debug() << "System names aggregated" << std::endl;
    return names;
}

namespace imc {
    namespace reader {
        namespace module {
            optional<System> fetch_system(
                    const pugi::xml_node& node,
                    const path& filepath
                ) noexcept
            {
                auto usernames = fetch_usernames(node, filepath);
                if (!usernames) {
                    return {};
                }

                ::imc::logger::log::debug() << "Usernames = [" << std::endl;
                for (auto& name : *usernames) {
                    ::imc::logger::log::debug() << "  " << name << "," << std::endl;
                }
                ::imc::logger::log::debug() << "]" << std::endl;

                auto sysnames  = fetch_systemnames(node);
                if (!sysnames) {
                    return {};
                }

                ::imc::logger::log::debug() << "Systennames = [" << std::endl;
                for (auto& name : *sysnames) {
                    ::imc::logger::log::debug() << "  " << name << "," << std::endl;
                }
                ::imc::logger::log::debug() << "]" << std::endl;

                return System(std::move(*usernames), std::move(*sysnames));
            }
        }
    }
}



