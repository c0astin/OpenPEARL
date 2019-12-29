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
using Configuration     = imc::types::system::Configuration;
using SystemElement     = imc::types::system::SystemElement;
using UserName     		= imc::types::system::UserName;
using error             = imc::checker::error;

static void check_element_data(
        std::string e_name,
        const optional<std::vector<Parameter>>& e_parameters,
        SystemElement system_element,
        const char* element_type_name
    ) noexcept
{
    std::string s_name = system_element.get_system_name();
    const std::vector<ParameterInstance> s_parameters = system_element.get_parameter_instances();

	std::cout << "platform name : " << e_name<<" : module system name: " << s_name << ": should be of type " << element_type_name<< std::endl;



	if (!e_parameters) {
        if (!s_parameters.empty()) {
            // No parameters in platform spec, but system name has params
            ::imc::logger::log::error()
                    << system_element.get_location()<<": "
                << element_type_name << " '"
                << e_name
                << "' has no parameters"
                << std::endl;
            return;
        } else {
            ::imc::logger::log::debug()
                << element_type_name << " '"
                << e_name
                << "' has no parameters, but also none in system name."
                << std::endl;
        }
    }

	if ((bool)e_parameters) {
	    if (e_parameters->size() != s_parameters.size()) {
	        ::imc::logger::log::error()
	           << system_element.get_location()<<": "
	           << "numbers of parameters for '"
	           << e_name
	           << "' do not match! required "
	           << e_parameters->size()
	           << ": supplied  "
	           << s_parameters.size()
	           << std::endl;

	        return;
	    }


	    // Yes, that's exactly _not_ how one should do it, but C++ makes it way to
	    // hard to zip two iterators, so screw it!
	    for(unsigned i = 0; i < e_parameters->size(); ++i) {
	        if (e_parameters->at(i).get_type() != s_parameters[i].get_type()) {
	            ::imc::logger::log::error()
	            << system_element.get_location()<<": "
	            << "types do not match for : '" << e_name << "' : expected "
	            << e_parameters->at(i).get_type()
	            << ": supplied " << s_parameters[i].get_type()
	            << std::endl;
	            return;
	        }

	        // Check parameter length vs allowed parameter length
	        // Switch by parameter type, because Fixed gets compared bitwise, while char
	        // gets compared bytewise, for example.
	        auto allowed_parameter_length = e_parameters->at(i).get_len();
	        auto actual_length = s_parameters[i].get_computed_length();
	        if (actual_length > allowed_parameter_length) {
                ::imc::logger::log::error()
                << system_element.get_location()<<": "
	            << "Parameter #"<<i+1<< " of type "
	            << s_parameters[i]
	                            << " has size " << actual_length
	                            << " but max size is " << allowed_parameter_length
	                            << std::endl;
	            return;
	        }

	        {
	            auto rest = e_parameters->at(i).get_restriction();
	            ::imc::logger::log::debug()
	            << "CHECK whether " << s_parameters[i].get_value() << " matches "
	            << *rest
	            << std::endl;

	            if (rest && !rest->matches(s_parameters[i].get_value())) {
	                ::imc::logger::log::error()
	                << system_element.get_location()<<": parameter #" << i+1
	                << ": value '" << s_parameters[i].get_value() << "'"
	                << " does not match restriction: "
	                << *rest
	                << std::endl;
	                return;
	            }
	            // If no rule exists, we don't care
	        }
	    }
	}

    return;
}

/** Template helper to keep the uglyness down to a minimum (the code repetition, tbh).
 *
 * Checks for all system names whether an element exists with
 *  * The same name
 *  * which has the same parameters (length of parameter lists)
 *  * which have the same type (parameter types must match)
 */
template<typename T>
void check_parameters_match_for_one_system_name_element(
                const std::vector<T>& elements,
                const char* element_type_name,
                const SystemElement& sys_element)
            noexcept
 {
        bool name_present = false;

        for (const T& element : elements) {
            if (name_present) { // shorten the looping
                continue; // the inner iteration
            }

            auto e_name = element.get_name();
            auto s_name = sys_element.get_system_name();

            if (e_name == s_name) {
                name_present = true;
                check_element_data(
                        e_name,
                        element.get_parameters(),
                        sys_element,
                        element_type_name);
            }

        }


    return;
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
        const std::vector<UserName>& user_names,
		const std::vector<Configuration>& configurations)
    noexcept
{

    if (strcmp(element_type_name,"Configuration") != 0) {
        for (auto& username : user_names) {
            ::imc::logger::log::debug()
            << "Check system name: " << username
            << std::endl;

            check_parameters_match_for_one_system_name_element(
                    elements,
                    element_type_name,
                    username);
        }
    } else {
        for (auto& cnf : configurations) {
            ::imc::logger::log::debug()
            << "Check configuration: " << cnf
            << std::endl;

            check_parameters_match_for_one_system_name_element(
                    elements,
                    element_type_name,
                    cnf);
        }
    }
    return{};
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
                std::cout << "check_params_match" << std::endl;
                // The following is ugly as hell, but still the prettiest and easiest way to write
                // this...

                ::imc::logger::log::debug()
                    << "Check whether all parameters match the platform requirement."
                    << std::endl;

                for(auto& module : this->modules) {
                    ::imc::logger::log::debug()
                        << "Check module..."
                        << std::endl;

                   if (module.system_part) {
                        ::imc::logger::log::debug()
                            << "Check system part..."
                            << std::endl;


#define TRY(e) do { auto res = (e); if (res) { return *res; } } while (0)


                        TRY(check_parameters_match_for_system_name_element(
                                    this->platform.get_dations(),
                                    "Dation",
                                    module.system_part.value().usernames,
                                    module.system_part.value().configurations));


                        TRY(check_parameters_match_for_system_name_element(
                                this->platform.get_connections(),
                                "Connection",
                                module.system_part.value().usernames,
                                module.system_part.value().configurations));

                        TRY(check_parameters_match_for_system_name_element(
                                this->platform.get_configurations(),
                                "Configuration",
                                module.system_part.value().usernames,
								module.system_part.value().configurations));

                        TRY(check_parameters_match_for_system_name_element(
                                this->platform.get_interrupts(),
                                "Interrupt",
                                module.system_part.value().usernames,
                                module.system_part.value().configurations));

                        TRY(check_parameters_match_for_system_name_element(
                                this->platform.get_signals(),
                                "Signal",
                                module.system_part.value().usernames,
                                module.system_part.value().configurations));


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

