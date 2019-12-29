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

           // enum SystemNameResourceType {
           //     Configuration
           // };

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

            /**
             * SystemElement is ether UserName or Configuration
             */
            class SystemElement {
                private:
                    std::string systemname;
                    //SystemNameResourceType resource_type;
                    std::string  required_association;
                    FileLocation file_location;
                    std::vector<ParameterInstance> params;

                public:

                    SystemElement(std::string&& n, /*SystemNameResourceType rty,*/ std::string&& assoc,
                   		 FileLocation&& floc, std::vector<ParameterInstance>&& params)
                        : systemname(std::move(n))
                      //  , resource_type(rty)
        				, required_association(std::move(assoc))
                		, file_location(std::move(floc))
                        , params(std::move(params))
                    {
                        // Nothing
                    }

                    std::string get_system_name(void) const noexcept;

                    const FileLocation& get_filelocation(void) const noexcept;

                    std::string get_location(void) const noexcept;

                    std::string get_provider_id(void) const noexcept;

                    const std::vector<ParameterInstance>& get_parameter_instances(void) const noexcept;
            };

            class Configuration : public SystemElement {
            	public:
                    Configuration(std::string&& sysname, std::string&& assoc,
                   		 FileLocation floc, std::vector<ParameterInstance>&& params)
					  	: SystemElement(std::move(sysname)
                                      , std::move(assoc)
									  , std::move(floc)
									  , std::move(params))
                    {
                        // Nothing
                    }

             };

            class UserName : public SystemElement{
                private:
                    std::string  name;

                public:

                    UserName(std::string&& n, std::string&& sysname, std::string&& assoc,
                    		 FileLocation floc, std::vector<ParameterInstance>&& params)
                        :  SystemElement(std::move(sysname)
                                       ,std::move(assoc)
                                       ,std::move(floc)
                                       ,std::move(params))
                			, name(std::move(n))
                    {
                        // Nothing
                    }

                    std::string get_name(void) const noexcept;
            };

            struct System {
                std::vector<UserName>     usernames;
                std::vector<Configuration>   configurations;

                System(std::vector<UserName>&& un, std::vector<Configuration>&& sn)
                    : usernames(std::move(un))
                    , configurations(std::move(sn))
                {
                    // empty
                }
            };

        } // system
    } // types
} // imc

#endif // IMC_TYPES_SYSTEM_HPP
