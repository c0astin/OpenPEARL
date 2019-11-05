#ifndef IMC_CHECKER_CHECK_SPC_HAS_DCL_HPP
#define IMC_CHECKER_CHECK_SPC_HAS_DCL_HPP

#include "checker/error.hpp"
#include "checker/check.hpp"

namespace imc {
    namespace checker {
        namespace checks {

            class check_each_spc_has_one_dcl : public ::imc::checker::check {

                public:

                    check_each_spc_has_one_dcl(
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

#endif // IMC_CHECKER_CHECK_SPC_HAS_DCL_HPP
