/*
 * changes: 2019-11-29 (rm)
 * setInstance, getInstance added as class methods for easy access to the platform data
 *   this is not nice but efficient to avoid tramp parameters for the checks
 *   The reason for this change is the ModuleReader which must check nested associations
 *   to be ether usernames or system names to avoid lot of irritation errors if an
 *   association provider ok defined.
 *   This is the minimal change in the softwar architecture.
 *   signals and interrupts may have associations
 */

#ifndef IMC_TYPES_PLATFORM_HPP
#define IMC_TYPES_PLATFORM_HPP

    #include <string>
    #include <vector>
#include <memory>

#include <chaiscript/chaiscript.hpp>

#include "types/common.hpp"

namespace imc {
    namespace types {
        namespace platform {
            template <typename T>
            using optional  = ::std::experimental::optional<T>;
            using path      = ::std::experimental::filesystem::path;

            using FileLocation  = types::common::FileLocation;
            using Attribute     = types::common::Attribute;
            using DataSpec      = types::common::DataSpec;
            using ParameterType = types::common::ParameterType;

            template<typename T>
            struct Value {
                optional<T>             value;
                optional<std::string>   expression;

                Value(void)             = default;
                Value(T t)              : value(t) {}
                Value(std::string expr) : expression(expr) {}
            };

            struct Restriction {
                virtual bool matches(const std::string& value) const noexcept = 0;
                virtual const std::string explain(void) const noexcept = 0;
                virtual void add_to_engine(chaiscript::ChaiScript&, std::string) noexcept = 0;
                virtual void compute_if_required_with_engine(chaiscript::ChaiScript&, std::string) noexcept = 0;
            };

            template<typename T>
            struct ValueRestriction : public Restriction {
                std::vector<T> allowed_values;
                std::string showAs;
                bool matches(const std::string& value) const noexcept override;
                const std::string explain(void) const noexcept override;
                void add_to_engine(chaiscript::ChaiScript&, std::string) noexcept override;
                void compute_if_required_with_engine(chaiscript::ChaiScript&, std::string) noexcept override;
            };

            struct FixedRangeRestriction : public Restriction {
                Value<unsigned int> lowerbound;
                Value<unsigned int> upperbound;

                bool matches(const std::string& value) const noexcept override;
                const std::string explain(void) const noexcept override;
                void add_to_engine(chaiscript::ChaiScript& engine, std::string name) noexcept override;
                void compute_if_required_with_engine(chaiscript::ChaiScript& e, std::string name) noexcept override;

                FixedRangeRestriction(Value<unsigned int> lower, Value<unsigned int> upper)
                    : lowerbound(lower)
                    , upperbound(upper)
                {
                    // Nothing
                }
            };

            struct FixedGtRestriction : public Restriction {
                Value<unsigned int> lowerbound;

                bool matches(const std::string& value) const noexcept override;
                const std::string explain(void) const noexcept override;
                void add_to_engine(chaiscript::ChaiScript& engine, std::string name) noexcept override;
                void compute_if_required_with_engine(chaiscript::ChaiScript& e, std::string) noexcept override;

                FixedGtRestriction(unsigned int lower) : lowerbound(Value<unsigned int>(lower)) {
                    // Nothing
                }

                FixedGtRestriction(std::string expr) : lowerbound(Value<unsigned int>(expr)) {
                    // Nothing
                }
            };

            struct NotEmptyRestriction : public Restriction {
                bool matches(const std::string& value) const noexcept override;
                const std::string explain(void) const noexcept override;
                void add_to_engine(chaiscript::ChaiScript&, std::string) noexcept override;
                void compute_if_required_with_engine(chaiscript::ChaiScript&, std::string) noexcept override;
            };

            struct AllRestriction : public Restriction {
                bool matches(const std::string& value) const noexcept override;
                const std::string explain(void) const noexcept override;
                void add_to_engine(chaiscript::ChaiScript&, std::string) noexcept override;
                void compute_if_required_with_engine(chaiscript::ChaiScript&, std::string) noexcept override;
            };

            class Parameter {
                private:

                    //
                    // members
                    //

                    ParameterType                paramtype;
                    std::shared_ptr<Restriction> restriction;
                    unsigned int                 len;
                    std::string                  name;

                public:

                    Parameter(ParameterType&& pt)
                        : paramtype(std::move(pt))
                    {
                        // empty
                    }

                    void set_restriction(std::shared_ptr<Restriction> r) noexcept;
                    void set_len(unsigned int i) noexcept;
                    void set_name(std::string name) noexcept;

                    unsigned int get_len(void) const noexcept;
                    std::string get_name(void) const noexcept;
                    const ParameterType& get_type(void) const noexcept;
                    const std::shared_ptr<Restriction> get_restriction(void) const noexcept;
            };

            class Layout {
            	std::string			provider_id;
                std::string         device_id;
                std::string         address;
                std::vector<bool>   bits;

                public:
                    Layout() = default;
                    Layout(std::string id)
                        : device_id(id)
                    {
                        // Intentionally left blank
                    	address = "none";
                    	provider_id = "none";
                    }

                    Layout(const Layout& other)
                        : provider_id(other.provider_id)
                    	, device_id(other.device_id)
                        , address(other.address)
                        , bits(other.bits)
                    {
                        // Intentionally left blank
                    }

                    void setProviderId(std::string id) noexcept {
                        this->provider_id = id;
                    }

                    std::string getProviderId(void) const noexcept {
                        return this->provider_id;
                    }

                    void setDeviceId(std::string id) noexcept {
                        this->device_id = id;
                    }

                    std::string getDeviceId(void) const noexcept {
                        return this->device_id;
                    }

                    void setAddress(std::string addr) noexcept {
                        this->address = addr;
                    }

                    std::string getAddress(void) const noexcept {
                        return this->address;
                    }

                    void setBits(std::vector<int> v) noexcept {
                        this->bits.clear();
                        for (auto b : v) {
                            this->bits.push_back(b != 0); // if is zero, set false, else true
                        }
                    }

                    std::vector<bool> getBits(void) const noexcept {
                        return this->bits;
                    }
            };

            typedef std::function<Layout (std::string, std::string, std::vector<std::string>)> LayoutGenerator;

            class PlatformType {
                private:
                    std::string name;
                    optional<std::vector<Parameter>>        parameters;
                    std::shared_ptr<chaiscript::ChaiScript> chai_engine;
                    optional<std::string>                   generator_fn_name;

                public:
                    PlatformType(std::string name) : name(name) {}

                    // To make the type abstract
                    virtual ~PlatformType() = 0;

                    const std::string& get_name(void) const noexcept;

                    const optional<std::vector<Parameter>>& get_parameters(void) const noexcept;

                    void push_back_param(Parameter&& p) noexcept;

                    void set_layout_generator(
                            std::shared_ptr<chaiscript::ChaiScript> engine,
                            std::string generator_fn_name)
                        noexcept;

                    optional<LayoutGenerator> get_layout_generator(void) noexcept;
            };


            class AssociationRequireableType : public PlatformType {
                private:
                    optional<std::string> required_association;

                public:
                    AssociationRequireableType(std::string name) : PlatformType(name) {}

                    // To make the type abstract
                    virtual ~AssociationRequireableType() = 0;

                    void set_required_association_provider(std::string&& prov) noexcept {
                        this->required_association = prov;
                    }

                    const optional<std::string>& get_require_association_provider(void) const noexcept {
                        return this->required_association;
                    }
            };

            class AssociationProviderType : public AssociationRequireableType {
                private:
                    std::vector<std::string> provided_associations;

                public:
                    AssociationProviderType(std::string name) : AssociationRequireableType(name) {}

                    // To make the type abstract
                    virtual ~AssociationProviderType() = 0;

                    void add_provided_association(std::string&& prov) noexcept {
                        this->provided_associations.push_back(std::move(prov));
                    }

                    const std::vector<std::string>& get_provided_associations(void) const noexcept {
                        return this->provided_associations;
                    }
            };

            /**
             * Represents a signal
             *
             * The `get_parameters()` function and the parameters member variable _must_ exist, even
             * if it is never used.
             *
             * This makes the code in the matcher way simpler.
             *
             * I know this is ugly, but it is C++... so live with it.
             */
            class Signal : public AssociationRequireableType {
                private:
                public:
                    Signal(std::string&& name) : AssociationRequireableType(name) {}
            };

            class Interrupt : public AssociationRequireableType {
                public:
                    Interrupt(std::string&& name) : AssociationRequireableType(name) {}
            };

            class Configuration : public AssociationRequireableType {
                private:

                    //
                    // members
                    //

                    optional<unsigned int>          instances;

                public:
                    Configuration(std::string&& name) : AssociationRequireableType(name) {}

                    void set_instances(unsigned int) noexcept;
            };

            class Dation : public AssociationProviderType {
                private:

                    //
                    // members
                    //

                    optional<unsigned int>           instances;
                    std::vector<Attribute>           attributes;
                    DataSpec                         dataspec;

                public:
                    Dation(std::string&& name) : AssociationProviderType(name) {}

                    Dation(std::string&& name, DataSpec&& ds)
                        : AssociationProviderType(std::move(name))
                        , dataspec(ds)
                    {
                        // empty
                    }

                    void set_dataspec(DataSpec&& ds) noexcept;
                    void push_back_attribute(Attribute&& param) noexcept;

                    // Getters

                    optional<unsigned int>                get_instances(void) const noexcept;
                    std::vector<Attribute>                get_attributes(void) const noexcept;
                    DataSpec                              get_dataspec(void) const noexcept;
            };

            class Connection : public AssociationProviderType {
                private:

                    //
                    // associations
                    //

                    optional<std::shared_ptr<Connection>> next;

                public:

                    Connection(std::string&& name) : AssociationProviderType(name) {}
            };

            class Platform {
                private:
                    path location;
                    std::vector<Dation> dations;
                    std::vector<Signal> signals;
                    std::vector<Interrupt> interrupts;
                    std::vector<Configuration> configurations;
                    std::vector<Connection> connections;
                    static Platform *instance;
                public:
                    static Platform & getInstance(void) {
                        return *instance;
                    }
                    static void setInstance(Platform & pf) {
                        instance = & pf;
                    }

                    Platform(
                        path filelocation,
                        std::vector<Dation>&& d,
                        std::vector<Signal>&& s,
                        std::vector<Interrupt>&& i,
                        std::vector<Configuration>&& configs,
                        std::vector<Connection>&& conns
                    ) :
                        location(filelocation),
                        dations(d),
                        signals(s),
                        interrupts(i),
                        configurations(configs),
                        connections(conns)
                    {
                        // Nothing
                    }

                    const std::vector<Dation>& get_dations(void)                 const noexcept;
                    const std::vector<Signal>& get_signals(void)                 const noexcept;
                    const std::vector<Interrupt>& get_interrupts(void)           const noexcept;
                    const std::vector<Configuration>& get_configurations(void)   const noexcept;
                    const std::vector<Connection>& get_connections(void)         const noexcept;

            };

        } // platform
    } // types
} // imc

#endif // IMC_TYPES_PLATFORM_HPP
