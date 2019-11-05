#ifndef IMC_CHECKER_CHECK_HPP
#define IMC_CHECKER_CHECK_HPP

#include <vector>
#include <experimental/optional>

#include "types/platform.hpp"
#include "types/module.hpp"
#include "checker/error.hpp"

namespace imc {
    namespace checker {

        template <typename T>
        using optional  = ::std::experimental::optional<T>;

        using Platform  = imc::types::platform::Platform;
        using Module    = imc::types::module::Module;

        class check {
            protected:
                const Platform& platform;
                const std::vector<Module>& modules;

            public:

                check(const Platform& pl, const std::vector<Module>& mods)
                    : platform(pl)
                    , modules(mods)
                {
                    // empty
                }

                // To make the type abstract
                virtual ~check() = 0;

                virtual optional<error> run(void) = 0;
        };

        /**
         * Helper to get all checks as a list of checks
         */
        std::vector<std::unique_ptr<check>> all_checks(
                const Platform& pl,
                const std::vector<Module>& mods)
            noexcept;

    } // checker
} // imc

#endif // IMC_CHECKER_CHECK_HPP

