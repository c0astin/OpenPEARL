#ifndef IMC_CHECKER_ERROR_HPP
#define IMC_CHECKER_ERROR_HPP

#include <ostream>
#include <sstream>

#include "types/common.hpp"
#include "types/platform.hpp"
#include "types/system.hpp"

namespace imc {
    namespace checker {

        using ParameterType         = imc::types::platform::ParameterType;
        using ParameterInstance     = imc::types::system::ParameterInstance;
        using Restriction           = imc::types::platform::Restriction;
        template<typename T>
        using ValueRestriction      = imc::types::platform::ValueRestriction<T>;
        using FixedRangeRestriction = imc::types::platform::FixedRangeRestriction;
        using FixedGtRestriction    = imc::types::platform::FixedGtRestriction;
        using NotEmptyRestriction   = imc::types::platform::NotEmptyRestriction;
        using AllRestriction        = imc::types::platform::AllRestriction;
        using Layout                = imc::types::platform::Layout;

        class error {
            private:
                std::ostringstream msg;

            public:
                 error()  = default;
                ~error() = default;

                error(const error& other)
                    : msg(other.msg.str())
                {
                }

                std::string as_string(void) const noexcept;

                error& operator<<(const std::string& s)               noexcept;
                error& operator<<(bool b)                             noexcept;
                error& operator<<(int i)                              noexcept;
                error& operator<<(char c)                             noexcept;
                error& operator<<(unsigned int u)                     noexcept;
                error& operator<<(size_t s)                           noexcept;
                error& operator<<(const char* s)                      noexcept;
                error& operator<<(std::ostream& (*pf)(std::ostream&)) noexcept; // for std::endl

                error& operator<<(const ParameterType&)               noexcept;
                error& operator<<(const ParameterInstance&)           noexcept;
                error& operator<<(const Restriction&)                 noexcept;
                error& operator<<(const FixedRangeRestriction&)       noexcept;
                error& operator<<(const FixedGtRestriction&)          noexcept;
                error& operator<<(const NotEmptyRestriction&)         noexcept;
                error& operator<<(const AllRestriction&)              noexcept;

                error& operator<<(const Layout&)                      noexcept;
        };

    } // checker
} // imc

#endif // IMC_CHECKER_ERROR_HPP

