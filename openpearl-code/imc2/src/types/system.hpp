#ifndef IMC_TYPES_SYSTEM_HPP
#define IMC_TYPES_SYSTEM_HPP

#include <string>
#include <vector>

#include "types/common.hpp"

namespace imc {
    namespace types {
        namespace system {
            using FileLocation  = imc::types::common::FileLocation;
            using ParameterType = imc::types::common::ParameterType;

            enum SystemNameResourceType {
                Configuration
            };

            class ParameterInstance {
                private:

                    //
                    // members
                    //

                    ParameterType           paramtype;
                    std::string             value;

                public:

                    ParameterInstance(ParameterType pt, std::string v);

                    ParameterInstance(const ParameterInstance& other)
                        : paramtype(other.paramtype)
                        , value(other.value)
                    {
                        // empty
                    }

                    ParameterType get_type(void) const noexcept {
                        return this->paramtype;
                    }

                    const std::string& get_value(void) const noexcept {
                        return this->value;
                    }

                    /**
                     * Compute the parameter length from its string value.
                     *
                     * @todo Current implementation returns raw string length
                     */
                    size_t get_computed_length(void) const noexcept;

            };

            class SystemName {
                private:
                    std::string name;
                    SystemNameResourceType resource_type;
                    std::vector<ParameterInstance> params;

                public:

                    SystemName(std::string&& n, SystemNameResourceType rty, std::vector<ParameterInstance>&& params)
                        : name(std::move(n))
                        , resource_type(rty)
                        , params(std::move(params))
                    {
                        // Nothing
                    }

                    std::string get_name(void) const noexcept;

                    const std::vector<ParameterInstance>& get_parameter_instances(void) const noexcept;
            };

            class UserName {
                private:
                    std::string  name;
                    std::string  systemname;
                    FileLocation file_location;
                    std::vector<ParameterInstance> parameters;

                public:

                    UserName(std::string&& n, std::string&& sysname, FileLocation&& floc, std::vector<ParameterInstance>&& params)
                        : name(std::move(n))
                        , systemname(std::move(sysname))
                        , file_location(std::move(floc))
                        , parameters(std::move(params))
                    {
                        // Nothing
                    }

                    std::string get_system_name(void) const noexcept;

                    const FileLocation& get_filelocation(void) const noexcept;

                    std::string get_name(void) const noexcept;
                    const std::vector<ParameterInstance>& get_parameter_instances(void) const noexcept;
            };

            struct System {
                std::vector<UserName>     usernames;
                std::vector<SystemName>   sysnames;

                System(std::vector<UserName>&& un, std::vector<SystemName>&& sn)
                    : usernames(std::move(un))
                    , sysnames(std::move(sn))
                {
                    // empty
                }
            };

        } // system
    } // types
} // imc

#endif // IMC_TYPES_SYSTEM_HPP
