#include <algorithm>
#include <vector>
#include <functional>
#include <experimental/optional>

#include "checker/error.hpp"
#include "checker/checks/check_each_spc_has_one_dcl.hpp"

#include "types/problem.hpp"
#include "logger.hpp"
#include "util.hpp"

template <typename T>
using optional      = ::std::experimental::optional<T>;
using Declaration   = imc::types::problem::Declaration;
using Specification = imc::types::problem::Specification;
using Problem       = imc::types::problem::Problem;
using error         = imc::checker::error;

namespace imc {
    namespace checker {
        namespace checks {

            /**
             * Checks whether all specifications match their respective declarations in name and
             * type
             */
            optional<error> check_each_spc_has_one_dcl::run(void)
                noexcept
            {
                std::vector<std::reference_wrapper<const Declaration>> declarations;
                for (auto& mod : this->modules) {
                    for (const Problem& probl_part : mod.problem_parts) {
                        for (const Declaration& decl : probl_part.get_decls()) {
                            declarations.push_back(std::ref(decl));
                        }
                    }
                }

                for (auto& mod : this->modules) {
                    for (const Problem& probl_part : mod.problem_parts) {
                        for (const Specification& spc : probl_part.get_spcs()) {
                            unsigned int count = 0;
                            auto name = spc.get_name();
                            for (const Declaration& decl : declarations) {
                                auto decl_name = decl.get_name();
                                if (decl.get_name() == name) {
                                    count++;
                                }

                                auto decl_type = decl.get_type();
                                auto spc_type = spc.get_type();

                                if (decl_type != spc_type) {
                                    error e;
                                    e
                                        << "SPC '" << name
                                        << "' has other type than DCL '" << decl_name << "': "
                                        << "'" << decl_type << "' != '" << spc_type << "'!"
                                        << std::endl;

                                    return e;
                                }
                            }

                            if (count > 1) {
                                error e;
                                e
                                    << "SPC '" << name << "' has more than one DCL: Has " << count
                                    << std::endl;

                                return e;
                            }
                        }
                    }
                }

                ::imc::logger::log::debug()
                    << "Each SPC seems to have one DCL" << std::endl;

                return {};
            }
        }
    }
}

