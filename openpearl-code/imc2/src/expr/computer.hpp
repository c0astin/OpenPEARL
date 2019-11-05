#ifndef IMC_EXPR_COMPUTER_HPP
#define IMC_EXPR_COMPUTER_HPP

#include <memory>
#include <experimental/optional>

#include "chaiscript/chaiscript.hpp"

#include "types/platform.hpp"

template <typename T>
using optional  = ::std::experimental::optional<T>;

namespace imc {
    namespace expr {

        /**
         * Function to compute the expressions inside the restrictions of the platform definition.
         *
         * WARNING!
         *
         * This function mutates state, so the platform object will be mutated. It is not suited
         * for calling in multithreaded environments therefore.
         */
        bool compute(imc::types::platform::Platform& platform) noexcept;

        /** Puts the `code` into the `engine` and verifies that after that, a function named
         * `generator_name` exists in the engine which returns a `Layout` and takes a string and a
         * vector<string> as arguments.
         */
        bool create_layout_generator(
                std::shared_ptr<chaiscript::ChaiScript> engine,
                const std::string code,
                const std::string generator_name
            ) noexcept;

        void register_layout_type(std::shared_ptr<chaiscript::ChaiScript> engine)
            noexcept;

    } // logger
} // imc

#endif // IMC_EXPR_COMPUTER_HPP
