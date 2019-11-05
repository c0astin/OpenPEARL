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
using error         = imc::checker::error;

std::vector<std::string> collect_required_association_providers(const Platform& platform) noexcept {
    std::vector<std::string> req;

    for (auto& elem: platform.get_configurations()) {
        auto e = elem.get_require_association_provider();
        if (e) {
            req.push_back(*e);
        }
    }
    for (auto& elem: platform.get_dations()) {
        auto e = elem.get_require_association_provider();
        if (e) {
            req.push_back(*e);
        }
    }
    for (auto& elem: platform.get_connections()) {
        auto e = elem.get_require_association_provider();
        if (e) {
            req.push_back(*e);
        }
    }
    return req;
}

namespace imc {
    namespace checker {
        namespace checks {

            optional<error> check_associations_provided::run(void)
                noexcept
            {
                const std::vector<std::string> required_association_providers =
                    collect_required_association_providers(this->platform);

                std::vector<std::string> provided_associations;
                for (auto& connection : this->platform.get_connections()) {
                    auto con_assoc = connection.get_provided_associations();
                    std::copy(con_assoc.begin(),
                            con_assoc.end(),
                            std::back_inserter(provided_associations));
                }

                for (auto& required_provider : required_association_providers) {
                    auto provided = std::any_of(
                            provided_associations.begin(),
                            provided_associations.end(),
                            [&](auto& prov) {
                                return prov == required_provider;
                            });

                    if (!provided) {
                        error e;
                        e
                            << "Required provider '" << required_provider << "' not provided"
                            << std::endl;
                        return e;
                    }
                }

                ::imc::logger::log::debug()
                    << "All required associations are provided"
                    << std::endl;

                return {};
            }
        }
    }
}


