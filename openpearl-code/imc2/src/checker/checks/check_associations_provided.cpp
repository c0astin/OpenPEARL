/*
 * changes: Nov-2019 (rm)
 *   error handling extended - continue checking after error found
 *   search system name in signals and interrupts
 */
#include <vector>
#include <experimental/optional>

#include "checker/error.hpp"
#include "checker/checks/check_associations_provided.hpp"

#include "types/platform.hpp"
#include "logger.hpp"
#include "util.hpp"

template <typename T>
using optional      = ::std::experimental::optional<T>;
using Platform      = imc::types::platform::Platform;
using Dation		= imc::types::platform::Dation;
using Configuration = imc::types::platform::Configuration;
using Connection    = imc::types::platform::Connection;
using Signal        = imc::types::platform::Signal;
using Interrupt     = imc::types::platform::Interrupt;
using error         = imc::checker::error;
using UserName      = imc::types::system::UserName;
using System        = imc::types::system::System;
using SystemElement = imc::types::system::SystemElement;


namespace imc {
    namespace checker {
        namespace checks {

            void  match_provider_type(const Platform& platform, const std::vector<UserName> usernames,
                    const SystemElement se_requester) {

                // we must deal with:
                //     association
                //  given     required
                //   yes        yes     check type
                //   no         no      ok
                //   yes        no      error
                //   no         yes     error

                std::string providerSystemName="";
                std::string requester = se_requester.get_system_name();
                optional<std::string> requestedType = "";
                std::vector<std::string> providedTypes;
                bool associationGiven = se_requester.get_provider_id() != "";
                bool associationRequired;

                // search provider of the association
                // we have the name  of the provider --> let's search all usernames for this name

                if (associationGiven) {
                    for (UserName pn: usernames) {
                        if (se_requester.get_provider_id().compare(pn.get_name()) == 0) {
                            providerSystemName = pn.get_system_name();
                            break;
                        }
                    }

                    if (providerSystemName == "") {
                        // no provider found --> error
                        ::imc::logger::log::error()
                        << se_requester.get_location() << ": " << se_requester.get_system_name()
                        << ": required association '" << se_requester.get_provider_id()
                        << "' not found"
                        << std::endl;
                        return;
                    }
                }

                // provider entry found -> let's get the provided type(s) of connection by the provider
                // and the required type required by the requester
                // search in the platform definitions in dation, connections
                bool isInPlatformDefinition = false;

                for ( Dation d : platform.get_dations()) {
                    if (d.get_name().compare(requester) == 0) {
                        requestedType = d.get_require_association_provider();
                        isInPlatformDefinition = true;
                    }
                    if (associationGiven) {
                        if (d.get_name().compare(providerSystemName) == 0) {
                            providedTypes = d.get_provided_associations();
                        }
                    }
                }

                for ( Connection c : platform.get_connections()) {
                    if (c.get_name().compare(requester) == 0) {
                        requestedType = c.get_require_association_provider();
                        isInPlatformDefinition = true;
                    }
                    if (associationGiven) {
                        if (c.get_name().compare(providerSystemName) == 0) {
                            providedTypes = c.get_provided_associations();
                        }
                    }
                }

                for ( Configuration cnf : platform.get_configurations()) {
                    if (cnf.get_name().compare(requester) == 0) {
                        requestedType = cnf.get_require_association_provider();
                        isInPlatformDefinition = true;
                    }
                    // a configuration does never export a provider interface
                }

                for ( Interrupt i : platform.get_interrupts()) {
                    if (i.get_name().compare(requester) == 0) {
                        requestedType = i.get_require_association_provider();
                        isInPlatformDefinition = true;
                    }
                    // a configuration does never export a provider interface
                }

                for ( Signal sig : platform.get_signals()) {
                    if (sig.get_name().compare(requester) == 0) {
                        requestedType = sig.get_require_association_provider();
                        isInPlatformDefinition = true;
                    }
                    // a configuration does never export a provider interface
                }


                if (! isInPlatformDefinition) {
                    ::imc::logger::log::error()
                    << se_requester.get_location() << ": system name '" << requester
                    << "' : is not available for this platform" << std::endl;
                    return;
                }
                associationRequired = (bool)requestedType;


                if (associationGiven == false && associationRequired) {
                    ::imc::logger::log::error()
                    << se_requester.get_location() << ": '" << requester
                    << "' : connection of type '" << requestedType.value()
                    << "' required, but given" << std::endl;
                    return;
                }
                if (associationGiven && associationRequired == false) {
                    ::imc::logger::log::error()
                    << se_requester.get_location() << ": '" << requester
                    << "' : no connection expected" << std::endl;
                    return;
                }

                if (associationGiven == false && associationRequired == false) {
                    return;
                }

                // connection given and required --> check type
                bool found = false;
                error e;
                e << se_requester.get_location() << ": '" << requester
                        << "' : connection type mismatch: required: '" << requestedType.value()
                        << "' -- provided types: ";

                for (std::string s : providedTypes) {
                    e << "/" << s ;
                    if (s.compare(requestedType.value()) == 0) {
                        found = true;
                    }
                }
                if (!providedTypes.empty()) {
                    e << "/" << std::endl;
                } else {
                    e << "--nothing--" << std::endl;
                }

                if (! found) {
                    ::imc::logger::log::error() << e.as_string();
                    return;
                }

                // match succeeded
                return;
            }

            optional<error> check_associations_provided::run(void)
            noexcept {

                ::imc::logger::log::debug() << "check_association_provided not complete -- need to check all dations, connections and configuration" << std::endl;
                // 2019-11-28 seems to be complete (rm)

                for (Module m: this->modules) {
                    ::imc::logger::log::debug() << "Module name="<<m.module_name << "  fname:" << m.file_name << std::endl;

                    if (m.system_part) {

                        ::imc::logger::log::debug() << "usernames are:" << std::endl;
                        for (UserName un : m.system_part->usernames) {
                            ::imc::logger::log::debug() << "treat username "<< un.get_name() <<
                                                "  with system name: " << un.get_system_name() <<
                                                " needs assoc name " << un.get_provider_id() << std::endl;
                            match_provider_type(this->platform, m.system_part->usernames, un );
                        }

                        ::imc::logger::log::debug() << "configurations are:" << std::endl;
                        for (auto c : m.system_part->configurations) {
                            ::imc::logger::log::debug() << "treat configuration "
                                    << c.get_system_name() << " needs assoc name '" << c.get_provider_id() <<"'"
                                    << std::endl;
                            match_provider_type(this->platform, m.system_part->usernames, c );
                        }
                    }
                }



                return {};
            }

        }
    }
}


