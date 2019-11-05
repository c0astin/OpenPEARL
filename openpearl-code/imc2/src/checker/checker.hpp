#ifndef IMC_CHECKER_HPP
#define IMC_CHECKER_HPP

#include <vector>
#include <experimental/optional>

#include "types/platform.hpp"
#include "types/module.hpp"

namespace imc {
    namespace checker {

        template <typename T>
        using optional  = ::std::experimental::optional<T>;

        using Platform  = imc::types::platform::Platform;
        using Module    = imc::types::module::Module;

        bool inter_module_check(
                    const Platform& platform,
                    const std::vector<Module>& module
            ) noexcept;

    } // checker
} // imc

#endif // IMC_CHECKER_HPP
