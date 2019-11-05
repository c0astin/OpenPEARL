#include <iostream>
#include <vector>
#include <string>
#include <stdexcept>
#include <memory>
#include <experimental/filesystem>
#include <experimental/optional>

#include "logger.hpp"
#include "util.hpp"
#include "reader/common.hpp"
#include "reader/reader.hpp"
#include "reader/platform.hpp"
#include "expr/computer.hpp"

#include "types/common.hpp"
#include "types/platform.hpp"
#include "util.hpp"

template<typename T>
using optional                = ::std::experimental::optional<T>;
namespace fs                  = ::std::experimental::filesystem;

using Attribute               = imc::types::common::Attribute;
using DataSpec                = imc::types::common::DataSpec;
using Configuration           = imc::types::platform::Configuration;
using Parameter               = imc::types::platform::Parameter;
using ParameterType           = imc::types::platform::ParameterType;
using Restriction             = imc::types::platform::Restriction;
using AssociationProviderType = imc::types::platform::AssociationProviderType;
template<typename T>
using Value                   = imc::types::platform::Value<T>;
template<typename T>
using ValueRestriction        = imc::types::platform::ValueRestriction<T>;
using FixedGtRestriction      = imc::types::platform::FixedGtRestriction;
using NotEmptyRestriction     = imc::types::platform::NotEmptyRestriction;
using AllRestriction          = imc::types::platform::AllRestriction;
using FixedRangeRestriction   = imc::types::platform::FixedRangeRestriction;
using Dation                  = imc::types::platform::Dation;
using Signal                  = imc::types::platform::Signal;
using Interrupt               = imc::types::platform::Interrupt;
using Connection              = imc::types::platform::Connection;
using LayoutGenerator         = imc::types::platform::LayoutGenerator;

//
// Private helper functions
//

std::shared_ptr<ValueRestriction<std::string>> value_restriction_from_node_consistsof(const pugi::xml_node& node) noexcept {
    ValueRestriction<std::string> valres;
    valres.allowed_values = imc::util::strsplit(node.value(), ',');
    return std::make_shared<ValueRestriction<std::string>>(valres);
}

std::shared_ptr<ValueRestriction<unsigned int>> value_restriction_from_node_values(const pugi::xml_node& node) noexcept {
    ValueRestriction<unsigned int> valres;

    std::vector<std::string> allowed_values = imc::util::strsplit(node.value(), ',');

    ::imc::logger::log::debug()
        << "parsed values = " << node.value() << " -> " << allowed_values << std::endl;

    for (std::string& val : allowed_values) {
        if (!val.empty() && !::imc::util::is_blank(val)) {
            try {
                valres.allowed_values.push_back(std::stoi(val));
            } catch (const std::invalid_argument& e) {
                auto test_postfixed = ::imc::util::postfixed_to_int(val);
                if (test_postfixed) {
                    valres.allowed_values.push_back(*test_postfixed);
                } else {
                    ::imc::logger::log::error() << "Cannot convert to integer: " << val << std::endl
                                 << "In node: " << node.name() << std::endl;
                    return {};
                }
            } catch (const std::out_of_range& e) {
                ::imc::logger::log::error() << "Out of range: " << val << std::endl
                             << "In node: " << node.name() << std::endl;
                return {};
            }
        }
    }

    ::imc::logger::log::debug() << "Constructed: " << valres << std::endl;
    return std::make_shared<ValueRestriction<unsigned int>>(valres);
}

std::shared_ptr<FixedGtRestriction> fixedgt_restriction_from_node(const pugi::xml_node& node) noexcept {
    unsigned int lower;
    std::string value = node.first_child().value();

    if (value.size() == 0) {
        ::imc::logger::log::error() << "Empty Value in node: " << node << std::endl;
        return {};
    }

    if (value[0] == '[' && value[value.size() - 1] == ']') { // we might have an expression here
        // Remove sorrounding [] brackets from input
        std::string         expr(value.substr(1, value.size()-2));
        FixedGtRestriction  fgtr(expr);

        ::imc::logger::log::debug() << "Constructed: " << fgtr << std::endl;
        return std::make_shared<FixedGtRestriction>(fgtr);
    } else {
        try {
            lower = std::stoi(node.first_child().value());
        } catch (const std::invalid_argument& e) {
            ::imc::logger::log::error() << "Cannot convert to integer: " << node.value() << std::endl
                         << "In node: " << node << std::endl
                         << "invalid argument error" << std::endl;
            return {};
        } catch (const std::out_of_range& e) {
            ::imc::logger::log::error() << "Out of range: " << node.value() << std::endl
                         << "In node: " << node << std::endl
                         << "out of range error" << std::endl;
            return {};
        }

        FixedGtRestriction fgtr(lower);

        ::imc::logger::log::debug() << "Constructed: " << fgtr << std::endl;
        return std::make_shared<FixedGtRestriction>(fgtr);
    }
}

std::shared_ptr<NotEmptyRestriction> notempty_restriction_from_node(const pugi::xml_node&) noexcept {
    NotEmptyRestriction ne;
    ::imc::logger::log::debug() << "Constructed: " << ne << std::endl;
    return std::make_shared<NotEmptyRestriction>(ne);
}

std::shared_ptr<FixedRangeRestriction> fixedrange_restriction_from_node(const pugi::xml_node& node) noexcept {
    std::string lower_s;
    std::string upper_s;

    {
        if (pugi::xml_node_type::node_pcdata == node.first_child().type()) {
            auto fc = node.first_child();
            std::vector<std::string> list = imc::util::strsplit(fc.value(), ',');
            if (list.size() != 2) {
                ::imc::logger::log::error()
                    << "Expected 2 values in '"
                    << fc.value()
                    << "' found: "
                    << list.size()
                    << std::endl;
                return {};
            }
            lower_s = list[0];
            upper_s = list[1];

            ::imc::logger::log::debug()
                << "Found restriction elements: '" << lower_s << "' and '" << upper_s << "'"
                << std::endl;
        } else {
            ::imc::logger::log::error() << "Node "
                << node.name()
                << " shoudl hold plain text data, but doesn't"
                << std::endl;
            return {};
        }
    }

    Value<unsigned int> lower;
    Value<unsigned int> upper;

    if (lower_s[0] == '[' && lower_s[lower_s.size() - 1] == ']') { // Looks like an expression
        ::imc::logger::log::debug() << "Having expression in lower bound" << std::endl;
        // Remove sorrounding [] brackets from input
        std::string         expr(lower_s.substr(1, lower_s.size()-2));
        lower.expression = expr;
    } else {
        ::imc::logger::log::debug() << "Having value in lower bound" << std::endl;
        try {
            lower.value = std::stoi(lower_s);
        } catch (const std::invalid_argument& e) {
            ::imc::logger::log::error() << "Cannot convert to integer: " << node.value() << std::endl
                         << "In node: " << node.name() << std::endl;
            return {};
        } catch (const std::out_of_range& e) {
            ::imc::logger::log::error() << "Out of range: " << node.value() << std::endl
                         << "In node: " << node.name() << std::endl;
            return {};
        }
    }

    if (upper_s[0] == '[' && upper_s[upper_s.size() - 1] == ']') { // Looks like an expression
        ::imc::logger::log::debug() << "Having expression in upper bound" << std::endl;
        // Remove sorrounding [] brackets from input
        std::string         expr(upper_s.substr(1, upper_s.size()-2));
        upper.expression = expr;
    } else {
        ::imc::logger::log::debug() << "Having value in upper bound" << std::endl;
        try {
            upper.value = std::stoi(upper_s);
        } catch (const std::invalid_argument& e) {
            ::imc::logger::log::error() << "Cannot convert to integer: " << node.value() << std::endl
                         << "In node: " << node.name() << std::endl;
            return {};
        } catch (const std::out_of_range& e) {
            ::imc::logger::log::error() << "Out of range: " << node.value() << std::endl
                         << "In node: " << node.name() << std::endl;
            return {};
        }
    }

    FixedRangeRestriction frr(lower, upper);

    ::imc::logger::log::debug() << "Constructed: " << frr << std::endl;

    return std::make_shared<FixedRangeRestriction>(frr);
}

std::shared_ptr<Restriction> restriction_from_node(const pugi::xml_node& node) {
    std::string restriction_name(node.name());
    ::imc::logger::log::debug() << "Restriction name = " << restriction_name << std::endl;

    if (restriction_name == "VALUES") {
        return value_restriction_from_node_values(node.first_child());
    } else if (restriction_name == "ConsistsOf") {
        return value_restriction_from_node_consistsof(node);
    } else if (restriction_name == "FIXEDGT") {
        return fixedgt_restriction_from_node(node);
    } else if (restriction_name == "NotEmpty") {
        std::shared_ptr<NotEmptyRestriction> r = notempty_restriction_from_node(node);
        if (!r) {
            ::imc::logger::log::error() << "Not able to construct NotEmptyRestriction" << std::endl;
        }
        return r;
    } else if (restriction_name == "FIXEDRANGE") {
        return fixedrange_restriction_from_node(node);
    } else if (restriction_name == "ALL") {
        AllRestriction allres;
        return std::make_shared<AllRestriction>(allres);
    } else {
        ::imc::logger::log::error() << "Unknown restriction: " << restriction_name << std::endl;
    }

    return {};
}

optional<Parameter> make_param_from_node(const pugi::xml_node& node) {
    std::string name = node.name();
    ::imc::logger::log::debug() << "Constructing param with type " << name << std::endl;
    auto opt_ptype = ::imc::reader::common::ptype_from_name(name);
    if (!opt_ptype) {
        return {};
    }
    ParameterType ptype = *opt_ptype;

    ::imc::logger::log::debug() << "Constructing param: type = " << ptype << std::endl;
    Parameter p(std::move(ptype));

    {
        auto length_attr = node.attribute("length");
        if (length_attr) {
            auto len = length_attr.as_uint();
            ::imc::logger::log::debug() << "Setting param length to " << len << std::endl;
            p.set_len(len);
        }
    }

    {
        auto nick_attr = node.attribute("nick");
        if (nick_attr) {
            auto nick = nick_attr.as_string();
            ::imc::logger::log::debug() << "Setting param nick to " << nick << std::endl;
            p.set_nick(nick);
        }
    }

    for (auto restriction_node : node.children()) {
        std::shared_ptr<Restriction> restriction = restriction_from_node(restriction_node);
        if (!restriction) {
            ::imc::logger::log::error() << "Constructing Restriction failed" << std::endl;
            return {};
        }

        p.set_restriction(std::move(restriction));
    }

    ::imc::logger::log::debug() << "Constructed: " << p << std::endl;
    return p;
}

optional<std::string> make_association_provider_from_node(const pugi::xml_node& node) {
    ::imc::logger::log::debug() << "Fetching AssociationProviderType" << std::endl;
    auto first_child = node.first_child();
    if (first_child.empty()) {
        ::imc::logger::log::error()
            << "Expected to have a child in "
            << node.type()
            << " but there is none."
            << std::endl;
        return {};
    }

    auto attr = first_child.attribute("name");

    if (attr.empty()) {
        ::imc::logger::log::error() << "Missing attribute 'name' in node " << first_child.type() << std::endl;
        return {};
    }

    return attr.value();
}

optional<std::string> fetch_needed_association_from_node(const pugi::xml_node& node) noexcept {
    auto req_association_name = node.attribute("name");
    if (!req_association_name) {
        ::imc::logger::log::error() << "Error while parsing name of required association in "
            << "'platform.<_>.needsAssociation'"
            << std::endl;

        return {};
    }

    return std::string(req_association_name.as_string());
}

/** Returns the name of the generator function registered in the engine
 *
 */
optional<std::string> fetch_layout_generator(
        std::shared_ptr<chaiscript::ChaiScript> engine,
        const pugi::xml_node& node
    ) noexcept
{
    ::imc::logger::log::debug() << "Fetching LayoutGenerator" << std::endl;
    auto first_child = node.first_child();
    if (first_child.empty()) {
        ::imc::logger::log::error()
            << "Expected to have a child in "
            << node.type()
            << " but there is none."
            << std::endl;
        return {};
    }

    auto name = node.attribute("name");

    if (!name) {
        ::imc::logger::log::error()
            << "Error while fetching name of generator"
            << "'platform.<_>.layoutGenerator<name>'"
            << std::endl;

        return {};
    }

    std::string code = first_child.value();

    ::imc::logger::log::debug()
        << "Found generator script '" << name.as_string() << "': <<<"
        << code
        << ">>>"
        << std::endl;

    if (!::imc::expr::create_layout_generator(engine, code, name.as_string())) {
        return {};
    }

    return name.as_string();
}

optional<std::vector<Dation>> fetch_dations(
        std::shared_ptr<chaiscript::ChaiScript> engine,
        const pugi::xml_node& platform
    ) noexcept
{
    std::vector<Dation> l;

    for (pugi::xml_node child : platform.children()) {
        if (std::string(child.name()) == "dation") {
            auto name_attr = child.attribute("name");
            if (!name_attr) {
                ::imc::logger::log::error() << "'platform.<dation>.name' missing" << std::endl
                            << "skipping..." << std::endl;
                return {};
            }

            std::string name = name_attr.value();

            Dation d(std::move(name));

            for (pugi::xml_node dation_child : child.children()) {
                std::string dation_child_name(dation_child.name());
                if (dation_child_name  == "parameters") {
                    for (pugi::xml_node param_node : dation_child.children()) {
                        auto param = make_param_from_node(param_node);
                        if (!param) {
                            ::imc::logger::log::error() << "Constructing Parameter failed" << std::endl;
                            return {};
                        }
                        d.push_back_param(std::move(*param));
                    }
                } else if (dation_child_name == "attributes") {
                    auto attributes = ::imc::reader::common::make_attributes_from_node(dation_child);
                    if (!attributes) {
                        ::imc::logger::log::error() << "Constructing Attribute failed" << std::endl;
                        return {};
                    }
                    for (auto attr: *attributes) {
                        d.push_back_attribute(std::move(attr));
                    }
                } else if (dation_child_name == "data") {
                    auto dataspec = imc::reader::common::make_dataspec_from_node(dation_child);
                    if (!dataspec) {
                        ::imc::logger::log::error() << "Constructing DataSpec failed" << std::endl;
                        return {};
                    }
                    d.set_dataspec(std::move(*dataspec));
                } else if (dation_child_name == "associationProvider") {
                    auto assocprov = make_association_provider_from_node(dation_child);
                    if (!assocprov) {
                        ::imc::logger::log::error() << "Constructing AssociationProviderType failed" << std::endl;
                        return {};
                    } else {
                        ::imc::logger::log::debug()
                            << "Found AssociationProviderType: '" << *assocprov << "'"
                            << std::endl;
                    }

                    d.add_provided_association(std::move(*assocprov));
                } else if (dation_child_name == "needAssociation") {
                    auto needassoc = fetch_needed_association_from_node(dation_child);
                    if (!needassoc) {
                        ::imc::logger::log::error() << "Constructing required association provider failed" << std::endl;
                        return {};
                    }
                    d.set_required_association_provider(std::move(*needassoc));
                } else if (dation_child_name == "layoutGenerator") {
                    optional<std::string> generator_name = fetch_layout_generator(engine, dation_child);
                    if (!generator_name) {
                        return {};
                    }

                    ::imc::logger::log::debug()
                        << "Setting layout generator function in connection: " << *generator_name
                        << std::endl;

                    d.set_layout_generator(engine, *generator_name);
                } else {
                    ::imc::logger::log::error() << "Unknown node type in 'platform.<dation>': "
                                << "'" << dation_child_name << "'"
                                << std::endl;
                    return {};
                }
            }


            l.push_back(std::move(d));
        }
    }

    ::imc::logger::log::debug() << "Constructed: [ " << std::endl;
    for (auto& elem: l) {
        ::imc::logger::log::debug() << "  " << elem << ", " << std::endl;
    }
    ::imc::logger::log::debug() << "]" << std::endl;
    return l;
}

optional<std::vector<Signal>> fetch_signals(const pugi::xml_node& platform) {
    std::vector<Signal> l;

    for(pugi::xml_node child : platform.children()) {
        if (std::string(child.name()) == "signal") {
            auto name_attr = child.attribute("name");
            if (!name_attr) {
                ::imc::logger::log::error() << "WARNING: 'platform.<signal>.name' missing" << std::endl
                             << "WARNING: skipping..." << std::endl;
                return {};
            }

            std::string name = name_attr.value();

            // TODO: Handle attributes

            l.push_back(Signal(std::move(name)));
        }
    }

    ::imc::logger::log::debug() << "Constructed: [ " << std::endl;
    for (auto& elem: l) {
        ::imc::logger::log::debug() << "  " << elem << ", " << std::endl;
    }
    ::imc::logger::log::debug() << "]" << std::endl;

    return l;
}

optional<std::vector<Interrupt>> fetch_interrupts(const pugi::xml_node& platform) {
    std::vector<Interrupt> interrupts;

    for (pugi::xml_node child : platform.children()) {
        if (std::string(child.name()) == "interrupt") {
            auto attr_name = child.attribute("name");
            if (!attr_name) {
                ::imc::logger::log::error() << "Interrupt without name attribute" << std::endl;
                return {};
            }

            std::vector<Interrupt> l;
            std::string interrupt_name = attr_name.as_string();

            Interrupt interrupt(std::move(interrupt_name));

            for (pugi::xml_node interrupt_child : child.children()) {
                std::string interrupt_child_name(interrupt_child.name());
                if (interrupt_child_name  == "parameters") {
                    for (pugi::xml_node param_node : interrupt_child.children()) {
                        auto param = make_param_from_node(param_node);
                        if (!param) {
                            ::imc::logger::log::error() << "Constructing Parameter failed" << std::endl;
                            return {};
                        }
                        interrupt.push_back_param(std::move(*param));
                    }
                } else {
                    ::imc::logger::log::error() << "Unknown node type in 'platform.<interrupt>': "
                                 << "'" << interrupt_child_name << "'"
                                 << std::endl;

                    return {};
                }
            }

            interrupts.push_back(std::move(interrupt));
        }
    }

    ::imc::logger::log::debug() << "Constructed: [ " << std::endl;
    for (auto& elem: interrupts) {
        ::imc::logger::log::debug() << "  " << elem << ", " << std::endl;
    }
    ::imc::logger::log::debug() << "]" << std::endl;

    return interrupts;
}

optional<std::vector<std::string>> fetch_provided_association_names_from_node(const pugi::xml_node& node) noexcept {
    std::vector<std::string> lst;

    for(auto child : node.children()) {
        if (std::string(child.name()) == "associationType") {
            auto name = child.attribute("name");
            if (!name) {
                ::imc::logger::log::error() << "Error getting 'name' from associationType" << std::endl;
                return {};
            }
            ::imc::logger::log::debug() << "Pushing name = '" << name.as_string() << "'" << std::endl;
            lst.push_back(std::string(name.as_string()));
        }
    }

    return lst;
}

optional<std::vector<Configuration>> fetch_configurations(const pugi::xml_node& platform) {
    std::vector<Configuration> configurations;

    for (pugi::xml_node child : platform.children()) {
        if (std::string(child.name()) == "configuration") {
            auto attr_name = child.attribute("name");
            if (!attr_name) {
                ::imc::logger::log::error() << "Configuration without name attribute" << std::endl;
                return {};
            }

            std::string config_name(attr_name.as_string());

            Configuration configuration(std::move(config_name));

            // Dirty hack: If the attr "instances" is a non-empty string, we try to use it as uint.
            {
                auto instance_attr_name = child.attribute("instances");
                if (!instance_attr_name && std::string(instance_attr_name.as_string()) != "") {
                    // As pugi::xml_attribute::as_uint() returns 0 as default value,
                    // this does not fail, but might result in unexpected behaviour
                    configuration.set_instances(instance_attr_name.as_uint());
                }
            }

            for (pugi::xml_node configuration_child : child.children()) {
                std::string configuration_child_name(configuration_child.name());
                if (configuration_child_name  == "parameters") {
                    for (pugi::xml_node param_node : configuration_child.children()) {
                        auto param = make_param_from_node(param_node);
                        if (!param) {
                            ::imc::logger::log::error() << "Constructing Parameter failed" << std::endl;
                            return {};
                        }
                        configuration.push_back_param(std::move(*param));
                    }
                } else if (configuration_child_name == "needAssociation") {
                    auto s = fetch_needed_association_from_node(configuration_child);
                    if (!s) {
                        return {};
                    }

                    ::imc::logger::log::debug()
                        << "Setting required association (by name) in configuration: "
                        << "'" << *s << "'"
                        << std::endl;

                    configuration.set_required_association_provider(std::move(*s));
                } else {
                    ::imc::logger::log::error() << "Unknown node type in 'platform.<configuration>': "
                                 << "'" << configuration_child_name << "'"
                                 << std::endl;

                    return {};
                }
            }

           configurations.push_back(std::move(configuration));
        }
    }

    ::imc::logger::log::debug() << "Constructed: [ " << std::endl;
    for (auto& elem: configurations) {
        ::imc::logger::log::debug() << "  " << elem << ", " << std::endl;
    }
    ::imc::logger::log::debug() << "]" << std::endl;

    return configurations;
}

optional<std::vector<Connection>> fetch_connections(
        std::shared_ptr<chaiscript::ChaiScript> engine,
        const pugi::xml_node& platform
    )
{
    std::vector<Connection> connections;

    for (pugi::xml_node child : platform.children()) {
        if (std::string(child.name()) == "connection") {
            auto attr_name = child.attribute("name");
            if (!attr_name) {
                ::imc::logger::log::error() << "Connection without name attribute" << std::endl;
                return {};
            }

            std::string conn_name(attr_name.as_string());

            Connection conn(std::move(conn_name));

            for (pugi::xml_node connection_child : child.children()) {
                std::string connection_child_name(connection_child.name());
                if (connection_child_name  == "parameters") {
                    for (pugi::xml_node param_node : connection_child.children()) {
                        auto param = make_param_from_node(param_node);
                        if (!param) {
                            ::imc::logger::log::error() << "Constructing Parameter failed" << std::endl;
                            return {};
                        }
                        conn.push_back_param(std::move(*param));
                    }
                } else if (connection_child_name == "associationProvider") {
                    auto provided = fetch_provided_association_names_from_node(connection_child);

                    if (!provided) {
                        return {};
                    }

                    for (auto elem: *provided) {
                        ::imc::logger::log::debug()
                            << "Adding provided association (by name) to connection: "
                            << "'" << elem << "'"
                            << std::endl;

                        conn.add_provided_association(std::move(elem));
                    }

                    ::imc::logger::log::debug()
                        << "Added all provided associations to connection"
                        << std::endl;
                } else if (connection_child_name == "needAssociation") {
                    auto s = fetch_needed_association_from_node(connection_child);
                    if (!s) {
                        return {};
                    }

                    ::imc::logger::log::debug()
                        << "Setting required association (by name) in connection: "
                        << "'" << *s << "'"
                        << std::endl;

                    conn.set_required_association_provider(std::move(*s));
                } else if (connection_child_name == "layoutGenerator") {
                    optional<std::string> generator_name = fetch_layout_generator(engine, connection_child);
                    if (!generator_name) {
                        return {};
                    }

                    ::imc::logger::log::debug()
                        << "Setting layout generator function in connection: " << *generator_name
                        << std::endl;

                    conn.set_layout_generator(engine, *generator_name);
                } else {
                    ::imc::logger::log::error() << "Unknown node type in 'platform.<connection>': "
                                 << "'" << connection_child_name << "'"
                                 << std::endl;
                    return {};
                }
            }

            ::imc::logger::log::debug() << "Constructed: " << conn << std::endl;
            connections.push_back(std::move(conn));
        }
    }

    ::imc::logger::log::debug() << "Constructed: [ " << std::endl;
    for (auto& elem: connections) {
        ::imc::logger::log::debug() << "  " << elem << ", " << std::endl;
    }
    ::imc::logger::log::debug() << "]" << std::endl;

    return connections;
}

//
// Implementation of PlatformXMLReader
//

namespace imc {
    namespace reader {
        using path      = ::std::experimental::filesystem::path;

        optional<Platform> PlatformXMLReader::operator()(void)
        {
            std::shared_ptr<chaiscript::ChaiScript> engine = std::make_shared<chaiscript::ChaiScript>();

            { // load all scripts in the searchpath into the engine
                for (auto& p : fs::recursive_directory_iterator(this->searchpath)) {
                    if (fs::is_regular_file(p.status())) {
                        ::imc::logger::log::debug()
                            << "Trying to load " << p.path() << std::endl;

                        try {
                            engine->eval_file(p.path());
                        } catch (chaiscript::exception::eval_error& ee) {
                            ::imc::logger::log::error()
                                << "Error while loading "
                                << p.path()
                                << ": " << ee.pretty_print()
                                << std::endl
                                << "in " << ee.filename << " at "
                                << ee.start_position.line << ":" << ee.start_position.column
                                << " -> "
                                << std::endl
                                << ee.detail
                                << std::endl;

                            return {};
                        }

                    }
                }
            }

            ::imc::expr::register_layout_type(engine);

            pugi::xml_document document;
            pugi::xml_parse_result res = document.load_file(std::string(this->filepath).c_str());

            ::imc::logger::log::debug() << "Document loaded" << std::endl;

            if (!res) {
                ::imc::logger::log::error() << "Parsing error: " << res.description() << std::endl;
                return {};
            }

            auto platform = document.child("platform");
            if (!platform) {
                ::imc::logger::log::error() << "Node 'platform' missing." << std::endl;
                return {};
            }

            ::imc::logger::log::debug() << "Having 'platform' node" << std::endl;

            auto file = platform.attribute("file");

            std::vector<Dation> dations;
            std::vector<Signal> signals;
            std::vector<Interrupt> interrupts;
            std::vector<Configuration> configs;
            std::vector<Connection> conns;

            {
                ::imc::logger::log::debug() << ">>>>>>>>>>>>>>>>" << std::endl;
                ::imc::logger::log::debug() << "Fetching dations" << std::endl;
                auto opt_dations = fetch_dations(engine, platform);
                if (!opt_dations) {
                    ::imc::logger::log::error() << "Error while fetching dations" << std::endl;
                    return {};
                }
                dations = *opt_dations;
                ::imc::logger::log::debug() << "Fetching dations worked" << std::endl;
                ::imc::logger::log::debug() << "<<<<<<<<<<<<<<<<" << std::endl;
            }

            {
                ::imc::logger::log::debug() << ">>>>>>>>>>>>>>>>" << std::endl;
                ::imc::logger::log::debug() << "Fetching signals" << std::endl;
                auto opt_signals = fetch_signals(platform);
                if (!opt_signals) {
                    ::imc::logger::log::error() << "Error while fetching signals" << std::endl;
                    return {};
                }
                signals = *opt_signals;
                ::imc::logger::log::debug() << "Fetching signals worked" << std::endl;
                ::imc::logger::log::debug() << "<<<<<<<<<<<<<<<<" << std::endl;
            }

            {
                ::imc::logger::log::debug() << ">>>>>>>>>>>>>>>>" << std::endl;
                ::imc::logger::log::debug() << "Fetching interrupts" << std::endl;
                auto opt_interrupts = fetch_interrupts(platform);
                if (!opt_interrupts) {
                    ::imc::logger::log::error() << "Error while fetching interrupts" << std::endl;
                    return {};
                }
                interrupts = *opt_interrupts;
                ::imc::logger::log::debug() << "Fetching interrupts worked" << std::endl;
                ::imc::logger::log::debug() << "<<<<<<<<<<<<<<<<" << std::endl;
            }

            {
                ::imc::logger::log::debug() << ">>>>>>>>>>>>>>>>" << std::endl;
                ::imc::logger::log::debug() << "Fetching configurations" << std::endl;
                auto opt_configs = fetch_configurations(platform);
                if (!opt_configs) {
                    ::imc::logger::log::error() << "Error while fetching configs" << std::endl;
                    return {};
                }
                configs = *opt_configs;
                ::imc::logger::log::debug() << "Fetching configurations worked" << std::endl;
                ::imc::logger::log::debug() << "<<<<<<<<<<<<<<<<" << std::endl;
            }

            {
                ::imc::logger::log::debug() << ">>>>>>>>>>>>>>>>" << std::endl;
                ::imc::logger::log::debug() << "Fetching connections" << std::endl;
                auto opt_conns = fetch_connections(engine, platform);
                if (!opt_conns) {
                    ::imc::logger::log::error() << "Error while fetching conns" << std::endl;
                    return {};
                }
                conns = *opt_conns;
                ::imc::logger::log::debug() << "Fetching connections worked" << std::endl;
                ::imc::logger::log::debug() << "<<<<<<<<<<<<<<<<" << std::endl;
            }

            return Platform(
                    path(file.as_string()),
                    std::move(dations),
                    std::move(signals),
                    std::move(interrupts),
                    std::move(configs),
                    std::move(conns)
                );
        }
    }
}



