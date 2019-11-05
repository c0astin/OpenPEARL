#ifndef IMC_CHECKER_CHECK_PARAM_MATCH_SYSNAME_HPP
#define IMC_CHECKER_CHECK_PARAM_MATCH_SYSNAME_HPP

#include "checker/error.hpp"
#include "checker/check.hpp"

namespace imc {
    namespace checker {
        namespace checks {

            class check_username_sysname_mappings : public ::imc::checker::check {

                public:

                    check_username_sysname_mappings(
                            const Platform& pl,
                            const std::vector<Module>& mods
                        ) : ::imc::checker::check(pl, mods)
                    {
                        // empty
                    }

                    optional<error> run(void) noexcept override;
            };

        } // checks
    } // checker
} // imc

#endif // IMC_CHECKER_CHECK_PARAM_MATCH_SYSNAME_HPP

