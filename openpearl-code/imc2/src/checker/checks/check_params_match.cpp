#include <algorithm>
#include <vector>
#include <functional>
#include <experimental/optional>

#include "checker/error.hpp"
#include "checker/checks/check_params_match.hpp"

#include "types/platform.hpp"
#include "types/system.hpp"
#include "logger.hpp"
#include "util.hpp"

template <typename T>
using optional          = ::std::experimental::optional<T>;
using Parameter         = imc::types::platform::Parameter;
using ParameterInstance = imc::types::system::ParameterInstance;
using Platform          = imc::types::platform::Platform;
using SystemName        = imc::types::system::SystemName;
using error             = imc::checker::error;

optional<error> check_element_data(
        std::string e_name,
        const optional<std::vector<Parameter>>& e_parameters,
        std::string s_name,
        const std::vector<ParameterInstance>& s_parameters,
        const char* element_type_name
    ) noexcept
{
    if (!e_parameters) {
        if (!s_parameters.empty()) {
            // No parameters in platform spec, but system name has params
            error e;
            e
                << element_type_name << " '"
                << e_name
                << "' has no parameters"
                << std::endl;
            return e;
        } else {
            ::imc::logger::log::debug()
                << element_type_name << " '"
                << e_name
                << "' has no parameters, but also none in system name."
                << std::endl;
        }
    }

    if (e_parameters->size() != s_parameters.size()) {
        error e;
        e
            << "Numbers of parameters  in '"
            << e_name
            << "' do not match to number of parameters in '"
            << s_name
            << "': "
            << e_parameters->size()
            << " != "
            << s_parameters.size()
            << std::endl;
        return e;
    }

    // Yes, that's exactly _not_ how one should do it, but C++ makes it way to
    // hard to zip two iterators, so screw it!
    for(unsigned i = 0; i < e_parameters->size(); ++i) {
        if (e_parameters->at(i).get_type() != s_parameters[i].get_type()) {
            error e;
            e
                << "Types do not match: "
                << e_parameters->at(i).get_type()
                << " != " << s_parameters[i].get_type()
                << std::endl;
            return e;
        }

        // Check parameter length vs allowed parameter length
        // Switch by parameter type, because Fixed gets compared bitwise, while char
        // gets compared bytewise, for example.
        auto allowed_parameter_length = e_parameters->at(i).get_len();
        auto actual_length = s_parameters[i].get_computed_length();
        if (actual_length > allowed_parameter_length) {
            error e;
            e
                << "Parameter of "
                << s_parameters[i]
                << " has size " << actual_length
                << " but max size is " << allowed_parameter_length
                << std::endl;
            return e;
        }

        {
            auto rest = e_parameters->at(i).get_restriction();
            ::imc::logger::log::debug()
                << "CHECK whether " << s_parameters[i].get_value() << " matches "
                << *rest
                << std::endl;

            if (rest && !rest->matches(s_parameters[i].get_value())) {
                error e;
                e
                    << "Value '" << s_parameters[i].get_value() << "'"
                    << " does not match restriction: "
                    << *rest
                    << std::endl;
                return e;
            }
            // If no rule exists, we don't care
        }
    }

    return {};
}


/** Template helper to keep the uglyness down to a minimum (the code repetition, tbh).
 *
 * Checks for all system names whether an element exists with
 *  * The same name
 *  * which has the same parameters (length of parameter lists)
 *  * which have the same type (parameter types must match)
 */
template<typename T>
optional<error> check_parameters_match_for_system_name_element(
        const std::vector<T>& elements,
        const char* element_type_name,
        const std::vector<SystemName>& sysnames)
    noexcept
{
    for (auto& system_name : sysnames) {
        ::imc::logger::log::debug()
            << "Check system name: " << system_name
            << std::endl;

        bool name_present = false;

        for (const T& element : elements) {
            if (name_present) { // shorten the looping
                continue; // the inner iteration
            }

            auto e_name = element.get_name();
            auto s_name = system_name.get_name();

            if (e_name == s_name) {
                name_present = true;
                auto err = check_element_data(
                        e_name,
                        element.get_parameters(),
                        s_name,
                        system_name.get_parameter_instances(),
                        element_type_name);

                if (err) {
                    return *err;
                }
            }

        }
        if (!name_present) {
            error e;
            e << "Name '" << system_name.get_name() << "' missing." << std::endl;
            return e;
        }
    }

    return {};
}

namespace imc {
    namespace checker {
        namespace checks {

            /** Check whether the parameter numbers of the elements in the system parts of the
             * modules match the parameter numbers of the names in the platform specification
             *
             * The function uses the check_parameters_match_for_system_name_element() helper
             * function.  The `Signal` type does not feature parameters, but the `get_parameters()`
             * function still exists, which makes it possible to use the template helper.
             */
            optional<error> check_params_match::run(void)
                noexcept
            {
                // The following is ugly as hell, but still the prettiest and easiest way to write
                // this...

                ::imc::logger::log::debug()
                    << "Check whether all parameters match the platform requirement."
                    << std::endl;

                for(auto& module : this->modules) {
                    ::imc::logger::log::debug()
                        << "Check module..."
                        << std::endl;

                    for(auto& system_part : module.system_parts) {
                        ::imc::logger::log::debug()
                            << "Check system part..."
                            << std::endl;

#define TRY(e) do { auto res = (e); if (res) { return *res; } } while (0)
                        TRY(check_parameters_match_for_system_name_element(
                                    this->platform.get_dations(),
                                    "Dation",
                                    system_part.sysnames));

                        TRY(check_parameters_match_for_system_name_element(
                                this->platform.get_connections(),
                                "Connection",
                                system_part.sysnames));

                        TRY(check_parameters_match_for_system_name_element(
                                this->platform.get_configurations(),
                                "Configuration",
                                system_part.sysnames));

                        TRY(check_parameters_match_for_system_name_element(
                                this->platform.get_interrupts(),
                                "Interrupt",
                                system_part.sysnames));

                        TRY(check_parameters_match_for_system_name_element(
                                this->platform.get_signals(),
                                "Signal",
                                system_part.sysnames));
#undef TRY
                    }
                }

                ::imc::logger::log::debug()
                    << "All parameters seem to match" << std::endl;

                return {};
            }
        }
    }
}

