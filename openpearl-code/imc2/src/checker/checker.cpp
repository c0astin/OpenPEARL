#include <vector>
#include <experimental/optional>

#include "checker/error.hpp"
#include "checker/check.hpp"
#include "logger.hpp"

namespace imc {
    namespace checker {
        bool inter_module_check(const Platform& platform, const std::vector<Module>& modules)
            noexcept
        {
            bool res = true;

            ::imc::logger::log::debug()
                << "Starting checks..." << std::endl;

            std::vector<error> errors;
            for (auto& check : all_checks(platform, modules)) {
                auto oerr = check->run();
                if (oerr) {
                    errors.push_back(*oerr);
                }
            }

            for (auto error: errors) {
                ::imc::logger::log::error() << error.as_string() << std::endl;
                res = false;
            }

            ::imc::logger::log::debug()
                << "... Finished checking" << std::endl;

            return res;
        }
    }
}

