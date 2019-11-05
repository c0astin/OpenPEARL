#ifndef IMC_LOGGER_HPP
#define IMC_LOGGER_HPP

#include <experimental/filesystem>
#include <experimental/optional>

#include <pugixml.hpp>

#include "types/common.hpp"
#include "types/platform.hpp"
#include "types/system.hpp"
#include "types/problem.hpp"
#include "types/module.hpp"

namespace imc {
    namespace logger {
        template<typename T>
        using Value                 = imc::types::platform::Value<T>;
        using FileLocation          = imc::types::common::FileLocation;
        using Configuration         = imc::types::platform::Configuration;
        using Parameter             = imc::types::platform::Parameter;
        using ParameterType         = imc::types::platform::ParameterType;
        using ParameterInstance     = imc::types::system::ParameterInstance;
        using Restriction           = imc::types::platform::Restriction;
        template<typename T>
        using ValueRestriction      = imc::types::platform::ValueRestriction<T>;
        using FixedRangeRestriction = imc::types::platform::FixedRangeRestriction;
        using FixedGtRestriction    = imc::types::platform::FixedGtRestriction;
        using NotEmptyRestriction   = imc::types::platform::NotEmptyRestriction;
        using AllRestriction        = imc::types::platform::AllRestriction;
        using Dation                = imc::types::platform::Dation;
        using Signal                = imc::types::platform::Signal;
        using Interrupt             = imc::types::platform::Interrupt;
        using Connection            = imc::types::platform::Connection;
        using Layout                = imc::types::platform::Layout;
        using UserName              = imc::types::system::UserName;
        using SystemName            = imc::types::system::SystemName;
        using Specification         = imc::types::problem::Specification;
        using Declaration           = imc::types::problem::Declaration;
        using Type                  = imc::types::problem::Type;
        using Module                = imc::types::module::Module;
        using Problem               = imc::types::problem::Problem;
        using System                = imc::types::system::System;

        enum loglevel {
            DEBUG,
            INFO,
            WARN,
            ERROR,
        };

        std::ostream& operator<<(std::ostream& os, loglevel& ll);

        /** log object class
         *
         * The log object class is used for logging with a streaming interface.
         *
         * Because the IMC log output should be in gcc-compatible format, we have to implement our
         * own logging functionality here. There would be libgcc or another library from the clang
         * project, but these things tend to be heavy-weight and I really do not want to pull information
         * such a huge dependency for simple logging.
         *
         * After all, we do not have any special needs here. We do not need to be that fast, we do
         * not need to be able to push logging information to files or something, just simple
         * logging to stderr.
         *
         * The logger implements a nice streaming interface, though, so we have at least a bit of a
         * nice logger here.
         *
         * And, of course, the logger is implemented as stateless logger, because statefulness is
         * the way to hell.
         */

        typedef std::ostream& (*ostream_manipulator)(std::ostream&);
        class log {
            private:
                loglevel     level;
                std::ostringstream out;

                log(loglevel l)
                    : level(l)
                {
                }

            public:
                log(const log& other)
                    : level(other.level)
                    , out(other.out.str())
                {
                }

                ~log() noexcept;

                static log debug(void) noexcept {
                    return log(loglevel::DEBUG);
                }

                static log info(void)  noexcept {
                    return log(loglevel::INFO);
                }

                static log warn(void)  noexcept {
                    return log(loglevel::WARN);
                }

                static log error(void) noexcept {
                    return log(loglevel::ERROR);
                }

                log& operator<<(const std::string& s)               noexcept;
                log& operator<<(bool b)                             noexcept;
                log& operator<<(int i)                              noexcept;
                log& operator<<(char c)                             noexcept;
                log& operator<<(unsigned int u)                     noexcept;
                log& operator<<(size_t s)                           noexcept;
                log& operator<<(const char* s)                      noexcept;
                log& operator<<(std::ostream& (*pf)(std::ostream&)) noexcept; // for std::endl

                // Overloads for platform types

                log& operator<<(const Configuration&)               noexcept;
                log& operator<<(const Parameter&)                   noexcept;
                log& operator<<(const ParameterType&)               noexcept;
                log& operator<<(const ParameterInstance&)           noexcept;
                log& operator<<(const Restriction&)                 noexcept;
                log& operator<<(const FixedRangeRestriction&)       noexcept;
                log& operator<<(const FixedGtRestriction&)          noexcept;
                log& operator<<(const NotEmptyRestriction&)         noexcept;
                log& operator<<(const AllRestriction&)              noexcept;

                log& operator<<(const Dation&)                      noexcept;
                log& operator<<(const Signal&)                      noexcept;
                log& operator<<(const Interrupt&)                   noexcept;
                log& operator<<(const Connection&)                  noexcept;
                log& operator<<(const UserName&)                    noexcept;
                log& operator<<(const SystemName&)                  noexcept;
                log& operator<<(const Specification&)               noexcept;
                log& operator<<(const Declaration&)               noexcept;
                log& operator<<(const Type&)                        noexcept;

                log& operator<<(const FileLocation&)                noexcept;
                log& operator<<(const Layout&)                noexcept;

                log& operator<<(const Module&)                      noexcept;
                log& operator<<(const Problem& p)                   noexcept;
                log& operator<<(const System& s)                    noexcept;

                // Overloads for external types

                log& operator<<(const pugi::xml_node&)              noexcept;

                template<typename T>
                log& operator<<(std::basic_ostream<T>&& os) noexcept {
                    this->out << os;
                    return *this;
                }

                template<typename T>
                log& operator<<(const std::vector<T>& lst) noexcept {
                    this->out << "[ ";
                    for (const T& elem : lst) {
                        *this << elem << ", ";
                    }
                    this->out << " ]";
                    return *this;
                }

                template<typename T>
                log& operator<<(const Value<T>& v) noexcept {
                    if (v.value && v.expression) {
                        this->out << "Value(" << *v.value << ", " << *v.expression << ")";
                    } else {
                        if (v.value) {
                            this->out << "Value(" << *v.value << ")";
                        }
                        if (v.expression) {
                            this->out << "Value(" << *v.expression << ")";
                        }
                    }

                    return *this;
                }

                template<typename T>
                log& operator<<(const ValueRestriction<T>& r) noexcept {
                    this->out << r.explain();
                    return *this;
                }

        };

    } // logger
} // imc

#endif // IMC_LOGGER_HPP
