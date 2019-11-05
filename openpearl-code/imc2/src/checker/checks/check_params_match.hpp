#ifndef IMC_CHECKER_CHECK_PARAMS_MATCH_HPP
#define IMC_CHECKER_CHECK_PARAMS_MATCH_HPP

#include "checker/error.hpp"
#include "checker/check.hpp"

namespace imc {
    namespace checker {
        namespace checks {

            class check_params_match : public ::imc::checker::check {

                public:

                    check_params_match(
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

#endif // IMC_CHECKER_CHECK_PARAMS_MATCH_HPP
