#include <vector>
#include <experimental/optional>

#include "checker/error.hpp"
#include "checker/checks/check_pin_layout.hpp"

#include "types/system.hpp"
#include "types/common.hpp"
#include "types/platform.hpp"
#include "logger.hpp"
#include "util.hpp"

template <typename T>
using optional          = ::std::experimental::optional<T>;
using Connection        = imc::types::platform::Connection;
using Dation            = imc::types::platform::Dation;
using FileLocation      = imc::types::common::FileLocation;
using Interrupt         = imc::types::platform::Interrupt;
using Layout            = imc::types::platform::Layout;
using LayoutGenerator   = imc::types::platform::LayoutGenerator;
using Module            = imc::types::module::Module;
using ParameterInstance = imc::types::system::ParameterInstance;
using Platform          = imc::types::platform::Platform;
using Signal            = imc::types::platform::Signal;
using System            = imc::types::system::System;
using SystemName        = imc::types::system::SystemName;
using UserName          = imc::types::system::UserName;
using error             = imc::checker::error;

typedef std::tuple<std::string, Layout>           NamedLayout;
typedef std::vector<NamedLayout>                  LayoutMap;
typedef std::reference_wrapper<NamedLayout>       NamedLayoutRef;
typedef std::pair<NamedLayoutRef, NamedLayoutRef> NamedLayoutPair;
typedef std::vector<NamedLayoutPair>              LayoutPairs;

// Sort the layout_map by the bits in the Layout objects
void sort_layout_map_by_bits(LayoutMap& layout_map) noexcept {
    std::sort(layout_map.begin(), layout_map.end(),
            [](auto& tpl_a, auto& tpl_b) {
                return std::get<1>(tpl_a).getBits() < std::get<1>(tpl_b).getBits();
            });
}

// Check if a pair of layouts collide
bool does_collide(NamedLayoutPair& pr) noexcept {
    bool collision = false;

    // Happy unboxing...
    const Layout first_layout  = std::get<1>(std::get<0>(pr).get());
    const Layout second_layout = std::get<1>(std::get<1>(pr).get());

    ::imc::logger::log::debug() << "Performing layout check on "
        << first_layout << " vs " << second_layout << std::endl
        << "(Comparing "
        << (size_t) &first_layout << " <-> " << (size_t) &second_layout
        << ")" << std::endl;

    if (first_layout.getDeviceId() == second_layout.getDeviceId()) {
        if (first_layout.getAddress() == second_layout.getAddress()) {
            // collision if bit arrays are equal
            auto a = first_layout.getBits();
            auto b = second_layout.getBits();

            if (a.size() != b.size()) {
                return false;
            }

            for (unsigned int i = 0; !collision && i < a.size() && i < b.size(); ++i) {
                collision = (a[i] & b[i]);
            }
        }
    } else {
        // collision if addresses for different devices are equal
        collision = first_layout.getAddress() == second_layout.getAddress();
    }

    if (!collision) {
        ::imc::logger::log::debug()
            << "Does not collide: "
            << first_layout  << std::endl
            << second_layout << std::endl;
    }

    return collision;
}

// For all pairs, check if they collide and if they do, return an error
optional<error> check_layout_pairs_for_collisions(const LayoutPairs& pairs) noexcept {
    ::imc::logger::log::debug()
        << "Performing layout checks on "
        << pairs.size()
        << " pairs now" << std::endl;

    for (auto pr: pairs) {
        if (does_collide(pr)) {
            error e;
            e
                << "Colliding parameters found: "
                << std::get<1>(std::get<0>(pr).get()) << ")"
                << " <=> "
                << std::get<1>(std::get<1>(pr).get()) << ")"
                << std::endl;
            return e;
        }
    }

    return {};
}

optional<error> check_layouts_for_name_with_gen(
        const std::vector<Module>& modules,
        const std::string name,
        LayoutGenerator& layout_generator
    ) noexcept
{
    { // some debugging output
        ::imc::logger::log::debug() << "Modules: [" << std::endl;
        for (const auto& module: modules) {
            ::imc::logger::log::debug() << "Module: " << module << std::endl;
        }
        ::imc::logger::log::debug() << "]" << std::endl;
    }

    // Iterate through all modules the IMC checks
    for (const auto& module : modules) {
        ::imc::logger::log::debug()
            << "Performing layout on UserName objects with SystemName: "
            << name
            << std::endl;

        // For all relevant usernames:
        // - get the system name
        // - get the parameter instances
        // - collect the string value of each parameter instance
        // - run the layout generator on the name and the parameter instance strings
        // - store the result, with the name, in the layout_map
        LayoutMap       layout_map;

        for (const System& system_part : module.system_parts) {
            ::imc::logger::log::debug() << "Checking System part: " << system_part << std::endl;

            for (const auto& username: system_part.usernames) {
                const std::string username_system_name = username.get_system_name();

                if (username_system_name  == name) {
                    const std::vector<ParameterInstance>& params = username.get_parameter_instances();
                    std::vector<std::string> params_strs;

                    std::transform(params.begin(), params.end(),
                            std::back_inserter(params_strs),
                            [](const ParameterInstance& param) {
                                ::imc::logger::log::debug()
                                    << "Getting from parameter instance: Type = '"
                                    << param.get_type() << "'" << std::endl;

                                ::imc::logger::log::debug()
                                    << "Getting from parameter instance: Value = '"
                                    << param.get_value() << "'" << std::endl;

                                std::string s(param.get_value());
                                return s;
                            });

                    ::imc::logger::log::debug()
                        << "Generating Layout: " << username_system_name
                        << std::endl;

                    try {
                        Layout layout = layout_generator(username_system_name, params_strs);
                        layout_map.push_back(std::make_tuple(username_system_name, layout));
                    } catch (const chaiscript::exception::eval_error& ee) {
                        error e;
                        e
                            << "ChaiScript engine error: "
                            << ee.reason << " at "
                            << ee.start_position.line << ":" << ee.start_position.column
                            << std::endl
                            << ee.detail << std::endl;

                        return e;
                    } catch (const chaiscript::exception::bad_boxed_cast &bbc) {
                        error e;
                        e
                            << "ChaiScript engine error: "
                            << bbc.what()
                            << std::endl;
                        return e;
                    } catch (const std::exception &ex) {
                        error e;
                        e
                            << "ChaiScript engine error: Script threw: "
                            << ex.what()
                            << std::endl;
                        return e;
                    }
                }
            }
        }

        ::imc::logger::log::debug()
            << "Still having " << layout_map.size() << " usernames" << std::endl;

        sort_layout_map_by_bits(layout_map);

        LayoutPairs pairs;
        {
            ::imc::logger::log::debug()
                << "Converting layout map to list of pairs"
                << std::endl;

            // I don't know how to do better in this hell of a language
            for (unsigned int i = 0; i < layout_map.size(); ++i) {
                for (unsigned int j = 0; j < layout_map.size(); ++j) {
                    if (i != j) {
                        pairs.push_back(std::make_pair(std::ref(layout_map[i]), std::ref(layout_map[j])));
                    }
                }
            }
        }

        auto error = check_layout_pairs_for_collisions(pairs);
        if (error) {
            return *error;
        }
    }

    return {};
}

namespace imc {
    namespace checker {
        namespace checks {

            optional<error> check_pin_layout::run(void)
                noexcept
            {
                // Iterator through all dations
#define PERFORM_CHECKS(elems) do {                                                         \
    auto elements = (elems);                                                               \
    for (auto& element : elements) {                                                       \
        ::imc::logger::log::debug() << "Performing pin check on: "                         \
            << element << std::endl;                                                       \
        auto gen = element.get_layout_generator();                                         \
        if (!gen) {                                                                        \
            ::imc::logger::log::debug() << "No layout generator" << std::endl;             \
            continue;                                                                      \
        }                                                                                  \
        auto e = check_layouts_for_name_with_gen(this->modules, element.get_name(), *gen); \
        if (e) {                                                                           \
            ::imc::logger::log::debug() << "Check error..." << std::endl;                  \
            return *e;                                                                     \
        }                                                                                  \
    }                                                                                      \
} while (0)

                ::imc::logger::log::debug() << "Performing pin check on dations" << std::endl;
                PERFORM_CHECKS(this->platform.get_dations());

                ::imc::logger::log::debug() << "Performing pin check on signals" << std::endl;
                PERFORM_CHECKS(this->platform.get_signals());

                ::imc::logger::log::debug() << "Performing pin check on interrupts" << std::endl;
                PERFORM_CHECKS(this->platform.get_interrupts());

                ::imc::logger::log::debug() << "Performing pin check on configurations" << std::endl;
                PERFORM_CHECKS(this->platform.get_configurations());

                ::imc::logger::log::debug() << "Performing pin check on connections" << std::endl;
                PERFORM_CHECKS(this->platform.get_connections());
#undef PERFORM_CHECKS

                return {};
            }
        }
    }
}

