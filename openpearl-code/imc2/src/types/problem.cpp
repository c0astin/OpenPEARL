#include <experimental/optional>
#include <vector>

#include "types/common.hpp"
#include "types/problem.hpp"

template<typename T>
using optional  = ::std::experimental::optional<T>;

namespace imc {
    namespace types {
        namespace problem {
            using Attribute = types::common::Attribute;

            Type Specification::get_type(void) const noexcept {
                return this->type;
            }

            std::string Specification::get_name(void) const noexcept {
                return this->name;
            }

            std::vector<Attribute> Specification::get_attributes(void) const noexcept {
                return this->attributes;
            }

            DataSpec Specification::get_dataspec(void) const noexcept {
                return this->data;
            }

            Type Declaration::get_type(void) const noexcept {
                return this->type;
            }

            std::string Declaration::get_name(void) const noexcept {
                return this->name;
            }

            std::vector<Attribute> Declaration::get_attributes(void) const noexcept {
                return this->attributes;
            }

            DataSpec Declaration::get_dataspec(void) const noexcept {
                return this->data;
            }

            const std::vector<Declaration>& Problem::get_decls(void) const noexcept {
                return this->decl;
            }

            const std::vector<Specification>& Problem::get_spcs(void) const noexcept {
                return this->spec;
            }

        }
    }
}
