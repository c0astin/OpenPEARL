/*
 * parse the module xml-tree in memory and create a vector of usernames and configurations.
 * These lists are handed over to an object of type system for further analysis.
 *
 * Changes to the first implementation: 2019-11-29 (rainer mueller)
 * Usernames and configurations may possess an association. This was already treated for the
 * case that the association is already an username.
 * The OpenPEARL grammar allows that
 * 1) an association is a system name - maybe with parameters
 * 2) an association may possess another association
 * These case were not treated
 *
 * To distinguish an association as username and systemname, we parse the usernames two times
 * + The first pass gathers a list ofd usernames
 * + the second pass  generates the list of usernames with the complete parameters and acciciation
 * + in case the association target is a system name, we create an intermediate username and add this
 *   to the list of usernames.
 *   This may occur even for configurations. Therefore the vectors for usernames becomes a module wide
 *   visible static element. The confugurations are treated the same way for symmetry.
 * + The execution model is sequential - multiple modules are parse sequentially.
 *   Thus we clear the vectors at beginning of parsing
 *
 * The treatment of parameters and association is similar for usernames and configurations, thus
 * this stuff was moved into a separate function
 */

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
using Configuration             = imc::types::system::Configuration;
//using SystemNameResourceType = imc::types::system::SystemNameResourceType;
using System                 = imc::types::system::System;

static std::vector<UserName> userNameEntries;   // the list of username entries
static std::vector<std::string> userNames;      // the pure list of usernames
static std::vector<Configuration> configNames;  // the list of configurations
static FileLocation fl;                         // the file location of the currently treated element

static std::string auto_username() {
    static int nbr = 0;
    std::stringstream ss;
    ss << "auto_" << nbr;
    nbr ++;
    return ss.str();
}

optional<ParameterInstance> make_param_instance_from_node(const pugi::xml_node& node) noexcept {
    auto node_name = std::string(node.name());
    auto ptype = ::imc::reader::common::ptype_from_name(node_name);
    if (!ptype) {
        ::imc::logger::log::error() << fl << ": internal compiler error: ParameterType '<" << node_name << "'> unknown" << std::endl;
        return {};
    }

    auto data = node.first_child();
    if (!data) {
        ::imc::logger::log::error() << fl << ": internal compiler error:: parameter has no <data> tag" << std::endl;
        return {};
    }

    std::string value(data.value());
    if (value.empty()) {
        ::imc::logger::log::error() << fl << ": internal compiler error: no data in node" << std::endl;
        return {};
    }

    ::imc::logger::log::debug() << "Building ParameterInstance" << std::endl;
    auto pi = ParameterInstance(*ptype, value);
    ::imc::logger::log::debug() << pi << std::endl;

    return pi;
}

static void collect_usernames( const pugi::xml_node& node) noexcept {
    for (auto child: node.children()) {
        if (std::string(child.name()) == "username") {
            auto name = child.attribute("name");

            unsigned int line = imc::reader::common::line_from_attribute(child, "line", 0);
            fl.set_line(line);

            if (!name) {
                ::imc::logger::log::error() << fl << ": <username> attribute 'name' missing" << std::endl;
            }
            userNames.push_back(std::move(std::string(name.as_string())));
        }
    }
}

static bool is_in_usernames(std::string& s) noexcept {
    for (std::string un : userNames) {
        if (s == un ) {
            return true;
        }
    }
    return false;
}

static bool is_system_name(std::string name) {
    ::imc::types::platform::Platform pf = ::imc::types::platform::Platform::getInstance();

    std::vector<::imc::types::platform::Dation> dations = pf.get_dations();
    for (::imc::types::platform::Dation d : dations) {
        if (d.get_name() == name) {
            return true;
        }
    }

    std::vector<::imc::types::platform::Connection> conns = pf.get_connections();
    for (::imc::types::platform::Connection c : conns) {
        if (c.get_name() == name) {
            return true;
        }
    }

    std::vector<::imc::types::platform::Configuration> confs = pf.get_configurations();
    for (::imc::types::platform::Configuration cnf : confs) {
        if (cnf.get_name() == name) {
            return true;
        }
    }

    std::vector<::imc::types::platform::Interrupt> ints = pf.get_interrupts();
    for (::imc::types::platform::Interrupt i : ints) {
        if (i.get_name() == name) {
            return true;
        }
    }

    std::vector<::imc::types::platform::Signal> sigs = pf.get_signals();
    for (::imc::types::platform::Signal s : sigs) {
        if (s.get_name() == name) {
            return true;
        }
    }

    return false;
}

/*
 * treat the sysname, parameter and association stuff
 * in case of direct associations to system names an intermediate usernames becomes
 *    created and linked to the parent system object (username or configuration)
 * username is NULL, if we are working on a configuration!
 * filepath is the path of the current module xml-file
 * line is the current line
 *
 * node is ether a sysname node in user name declaration, in configuration or a association
 *    node must be ether of type <sysname> or <association>
 */
static void treat_username_or_association(std::string * username, const pugi::xml_node& node) noexcept {
    //pugi::xml_node parameter_child;

    if (std::string(node.name()) != "sysname" &&
        std::string(node.name()) != "association") {
        ::imc::logger::log::error() << fl << ": internal compiler error: ether <sysname> or <association> node expected" << std::endl;
        return;
    }

    auto sysname = node.attribute("name");
    if (!sysname) {
        ::imc::logger::log::error() << fl << ": internal compiler error: system name missing" << std::endl;
        return;
    }

    std::vector<ParameterInstance> params;
    std::string assocName="";  // no association required

    for (auto parameter_child : node.children()) {
        if (parameter_child && std::string(parameter_child.name()) == "parameters") {
            for (pugi::xml_node param_node : parameter_child.children()) {
                auto param = make_param_instance_from_node(param_node);
                if (!param) {
                    // detailed message already printed
                    // ::imc::logger::log::error() << fl << "internal compiler error: constructing ParameterInstance failed" << std::endl;
                    return;
                }

                ::imc::logger::log::debug()
                    << "Constructing ParameterInstance succeeded: "
                    << *param
                    << std::endl;
                params.push_back(std::move(*param));
            }
        } else if (parameter_child && std::string(parameter_child.name()) == "association") {
            assocName = std::string(parameter_child.attribute("name").value());
            if (is_in_usernames(assocName)) {
                // nothing special to do
                // we could check if there are parameters -- none are allowed
                bool hasParameters = false;
                for (auto n : parameter_child.children()) {
                    if (std::string(n.name()) == "parameters") {
                        hasParameters = true;
                    }
                }
                if (hasParameters) {
                    ::imc::logger::log::error() << fl << ": username '" << assocName << "' must not have parameters" << std::endl;
                }
            } else {
                // we must construct an artificial username for the association
                if (is_system_name(assocName)) {
                    std::string autoUsername = auto_username();
                    assocName = autoUsername;  // replace the association name by the artifical username
                    treat_username_or_association(&autoUsername,  parameter_child);
                } else {
                    ::imc::logger::log::error() << fl << ": association '" << assocName << "' is nether a user name nor a system name" << std::endl;
                    // forget this entry
                    return;
                }

            }
            ::imc::logger::log::debug()
                 << "found association requirement:"
                 << assocName
                 << std::endl;
        }
    }

    if (username) {
       auto un = UserName(
            std::move(*username),
            std::move(std::string(sysname.as_string())),
            std::move(assocName),
            fl,
            std::move(params));

       userNameEntries.push_back(std::move(un));
    } else {
        auto sn = Configuration(
                 std::move(std::string(sysname.as_string())),
                 //std::move(SystemNameResourceType::Configuration),
                 std::move(assocName),
                 fl,
                 std::move(params));

         configNames.push_back(std::move(sn));
    }
}


static optional<std::vector<UserName>> fetch_usernames( const pugi::xml_node& node) noexcept
{


    for (auto child: node.children()) {
        if (std::string(child.name()) == "username") {
            auto name = child.attribute("name");
            unsigned int line = imc::reader::common::line_from_attribute(child, "line", 0);
            fl.set_line(line);

            auto first_child = child.first_child();

            std::string un = name.as_string();
            treat_username_or_association(&un, first_child);
        }
    }

    return userNameEntries;
}

static optional<std::vector<Configuration>> fetch_configurations(const pugi::xml_node& node) noexcept {


    ::imc::logger::log::debug() << "Parsing Configurations" << std::endl;
    for (auto child: node.children()) {
        if (std::string(child.name()) == "configuration") {
            unsigned int line = imc::reader::common::line_from_attribute(child, "line", 0);
            fl.set_line(line);

            auto first_child = child.first_child();

            treat_username_or_association(NULL, first_child);

        }
    }

    ::imc::logger::log::debug() << "System names aggregated" << std::endl;
    return configNames;
}



namespace imc {
    namespace reader {
        namespace module {
            optional<System> fetch_system(
                    const pugi::xml_node& node,
                    const path& filepath
                ) noexcept
            {
                userNames.clear();
                userNameEntries.clear();
                configNames.clear();
                fl.set_file(filepath);
                fl.set_line(-1);   // no line set

                collect_usernames(node);

                auto usernames = fetch_usernames(node);
                if (!usernames) {
                    return {};
                }

                ::imc::logger::log::debug() << "Usernames = [" << std::endl;
                for (auto& name : *usernames) {
                    ::imc::logger::log::debug() << "  " << name << "," << std::endl;
                }
                ::imc::logger::log::debug() << "]" << std::endl;

                auto configurations  = fetch_configurations(node);
                if (!configurations) {
                    return {};
                }

                ::imc::logger::log::debug() << "Configurations = [" << std::endl;
                for (auto& name : *configurations) {
                    ::imc::logger::log::debug() << "  " << name << "," << std::endl;
                }
                ::imc::logger::log::debug() << "]" << std::endl;

                return System(std::move(*usernames), std::move(*configurations));
            }
        }
    }
}



