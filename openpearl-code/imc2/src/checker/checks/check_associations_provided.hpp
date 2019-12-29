#ifndef IMC_CHECKER_CHECK_ASSOCIATIONS_PROVIDED_HPP
#define IMC_CHECKER_CHECK_ASSOCIATIONS_PROVIDED_HPP

#include "checker/error.hpp"
#include "checker/check.hpp"

namespace imc {
    namespace checker {
        namespace checks {

            class check_associations_provided : public ::imc::checker::check {
            private:

                public:

                    check_associations_provided(
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

#endif // IMC_CHECKER_CHECK_ASSOCIATIONS_PROVIDED_HPP

