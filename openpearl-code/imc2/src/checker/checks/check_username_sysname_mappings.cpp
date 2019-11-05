#include <algorithm>
#include <vector>
#include <functional>
#include <experimental/optional>

#include "checker/error.hpp"
#include "checker/checks/check_username_sysname_mappings.hpp"

#include "types/platform.hpp"
#include "types/system.hpp"
#include "logger.hpp"
#include "util.hpp"

template <typename T>
using optional      = ::std::experimental::optional<T>;
using Platform      = imc::types::platform::Platform;
using System        = imc::types::system::System;
using UserName      = imc::types::system::UserName;
using error         = imc::checker::error;

/**
 * A helper type for checking an element from the system part (usernames)
 *
 * This type is a functor.
 *
 * The checker gets the environment:
 *
 *  - The currently checked username
 *  - The mapped system name for that username
 *  - the platform
 *
 * When the operator() is called, it gets passed an element from the Platform, which it then can use
 * to match against the environment it stores.
 *
 * This means it:
 *
 *  1. checks whether the element has the system_name, if yes
 *  2. checks whether the number of parameters of the stored username match the element
 *  3. checks whether the parameter types of the parameters of the element match the username
 *     parameter types
 *
 *  In case of error, the checker prints the right error message and returns false.
 *  Also, if the element name does not match the required system_name, it returns false.
 *  This is needed because we do not know which type (Dation, Interrupt,...) has the user name. So
 *  we try all (see `check_username_systemname_mappings()`) and if none of them matches, we can
 *  abbort the whole checking process.
 */
class checker_helper {
    const UserName& username;
    const std::string& system_name;

    public:
        checker_helper(const UserName& username, const std::string& system_name)
            : username(username), system_name(system_name)
        {
            // nothing
        }

        template<typename T>
        optional<error> do_check(const T& element) {
            if (element.get_name() != system_name) {
                // name does not match, no error here
                ::imc::logger::log::debug()
                    << "'" << element.get_name() << "' != '" << system_name << "'"
                    << std::endl;
                return {}; // early return
            }

            auto instances = username.get_parameter_instances();
            auto params    = element.get_parameters();
            if (!params) {
                // no parameters for element required
                if (instances.size() != 0) {
                    // error: no parameters for element required, but username provides some
                    ::imc::logger::log::error()
                        << "'" << system_name << "' requires no parameters, but there are some provided"
                        << std::endl;
                    return {}; // early return
                }

                // no params required, no there, names match. Nice.
                return {};
            }

            if (params->size() != instances.size()) {
                // number of parameters wrong error output
                error e;
                e
                    << "Numbers of parameters do not match platform requirement: '"
                    << system_name << "'"
                    << std::endl;
                return e;
            }
            ::imc::logger::log::debug()
                << "'" << system_name << "': Numbers of parameters do match platform requirement!"
                << std::endl;

            // length match
            for (unsigned int i = 0; i < params->size(); ++i) {
                if (params->at(i).get_type() != instances[i].get_type()) {
                    // Type mismatch error output
                    error e;
                     e
                        << "Type error: '" << system_name << "': Expected"
                        << params->at(i).get_type()
                        << " in platform, got " << instances[i].get_type()
                        << std::endl;
                    return e;
                }
                ::imc::logger::log::debug()
                    << "'" << system_name << "': Type of parameter does match platform requirement!"
                    << std::endl;

                {
                    auto param_len    = params->at(i).get_len();
                    auto instance_len = instances[i].get_computed_length();

                    if (param_len < instance_len) {
                        // Parameter length does not match requirement error output
                        error e;
                        e
                            << "Parameter error: '" << system_name << "': Expected "
                            << param_len
                            << " in platform, got " << instance_len
                            << std::endl;
                        return e;
                    }
                    ::imc::logger::log::debug()
                        << "'" << system_name << "': Parameter length does match platform requirement!"
                        << " (" << param_len << " < " << instance_len << ")"
                        << " -> Param length is not greater than allowed length"
                        << std::endl;
                }


                {
                    if (!params->at(i).get_restriction()->matches(instances[i].get_value())) {
                        error e;
                        e
                            << "Parameter error: '"
                            << instances[i].get_value() << "' does not match restriction: "
                            << *params->at(i).get_restriction()
                            << " in " << instances[i]
                            << std::endl;
                        return e;
                    }
                    error e;
                    e
                        << "Parameter '"
                        << instances[i].get_value()
                        << "' does match restriction: '"
                        << *params->at(i).get_restriction()
                        << std::endl;
                }


                // all good
            }

            return {};
        }
};

namespace imc {
    namespace checker {
        namespace checks {

            /**
             * Performs checks on the user names which map to system names
             *
             * * Check whether for all user names, the mapped system name exists in the platform specification
             *
             * * Check whether for all user names, the mapped system name has the right parameters
             *   compared to the platform specification.
             */
            optional<error> check_username_sysname_mappings::run(void)
                noexcept
            {
                ::imc::logger::log::debug()
                    << "Checking whether all usernames->system names are correct for the platform" << std::endl;
                for (auto& mod : modules) {
                    ::imc::logger::log::debug()
                        << "Checking module..." << std::endl;

                    for (const System& system_part: mod.system_parts) {
                        ::imc::logger::log::debug()
                            << "Checking system part..." << std::endl;

                        for (const UserName& username: system_part.usernames) {
                            auto system_name = username.get_system_name();
                            ::imc::logger::log::debug()
                                << "Checking: '" << system_name << "'" << std::endl;

                            // Perform checks with the checker:
                            //
                            // Build the checker, which tries to match the available username, its
                            // mapped system name against elements of the platform
                            checker_helper check_helper(username, system_name);

#define TRY_ALL(elems) do {                              \
    for (auto elem: (elems)) {                           \
        optional<error> e = check_helper.do_check(elem); \
        if (e) {                                         \
            return *e;                                   \
        }                                                \
    }                                                    \
} while (0)
                            TRY_ALL(platform.get_dations());
                            TRY_ALL(platform.get_signals());
                            TRY_ALL(platform.get_interrupts());
                            TRY_ALL(platform.get_configurations());
                            TRY_ALL(platform.get_connections());
#undef TRY_ALL

                            ::imc::logger::log::debug()
                                << "-> '" << system_name << "' exists" << std::endl;
                        }
                    }
                }

                return {};
            }
        }
    }
}

