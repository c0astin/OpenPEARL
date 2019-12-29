#include <experimental/optional>
#include <vector>
#include <memory>
#include <algorithm>

#include "types/common.hpp"
#include "types/platform.hpp"
#include "logger.hpp"
#include "util.hpp"

template<typename T>
using optional        = ::std::experimental::optional<T>;
using Attribute       = imc::types::common::Attribute;
using DataSpec        = imc::types::common::DataSpec;
using Parameter       = imc::types::platform::Parameter;
using LayoutGenerator = imc::types::platform::LayoutGenerator;

namespace imc {
    namespace types {
        namespace platform {

            template<>
            bool ValueRestriction<unsigned int>::matches(const std::string& value) const noexcept {
                return std::any_of(
                        this->allowed_values.begin(),
                        this->allowed_values.end(),
                        [&](auto& allowed_value) {
                            try {
                                return allowed_value == std::stoul(value);
                            } catch (const std::invalid_argument& e) {
                                optional<unsigned int> o_uint = imc::util::postfixed_to_uint(value);
                                if (o_uint) {
                                    return allowed_value == *o_uint;
                                } else {
                                    ::imc::logger::log::error()
                                        << "Cannot convert to integer: "
                                        << value
                                        << std::endl;
                                    return false;
                                }
                            } catch (const std::out_of_range& e) {
                                ::imc::logger::log::error() << "Out of range: " << value << std::endl;
                                return false;
                            }
                        });
            }

            template<>
            const std::string ValueRestriction<unsigned int>::explain(void) const noexcept {
                std::stringstream ss;
                ss << "ValueRestriction(";
                for (auto& val: this->allowed_values) {
                	std::string s = ::imc::util::uint_to_postfixed((unsigned int)val,showAs);
                    ss << s << ", ";
                }
                ss << ")";
                return ss.str();
            }

            template<>
            void ValueRestriction<unsigned int>::add_to_engine(chaiscript::ChaiScript&, std::string) noexcept {
                // Nothing here.
                return;
            }

            template<>
            void ValueRestriction<unsigned int>::compute_if_required_with_engine(chaiscript::ChaiScript&, std::string) noexcept {
                // Nothing here.
                return;
            }

            template<>
            bool ValueRestriction<std::string>::matches(const std::string& value) const noexcept {
                return std::any_of(
                        this->allowed_values.begin(),
                        this->allowed_values.end(),
                        [&](auto& allowed_value) {
                            return allowed_value == value;
                        });
            }

            template<>
            const std::string ValueRestriction<std::string>::explain(void) const noexcept {
                std::stringstream ss;
                ss << "ValueRestriction(";
                for (auto& val: this->allowed_values) {
                    ss << val << ", ";
                }
                ss << ")";
                return ss.str();
            }

            template<>
            void ValueRestriction<std::string>::add_to_engine(chaiscript::ChaiScript&, std::string) noexcept {
                // Nothing here.
                return;
            }

            template<>
            void ValueRestriction<std::string>::compute_if_required_with_engine(chaiscript::ChaiScript&, std::string) noexcept {
                // Nothing here.
                return;
            }

            bool FixedRangeRestriction::matches(const std::string& value) const noexcept {
                unsigned int val;
                try {
                    val = std::stoul(value);
                } catch (const std::invalid_argument& e) {
                    optional<unsigned int> o_uint = imc::util::postfixed_to_uint(value);
                    if (o_uint) {
                        val = *o_uint;
                    } else {
                        ::imc::logger::log::error()
                            << "Cannot convert to integer: "
                            << value
                            << std::endl;
                        return false;
                    }
                } catch (const std::out_of_range& e) {
                    ::imc::logger::log::error() << "Out of range: " << value << std::endl;
                    return false;
                }

                if (!this->lowerbound.value) {
                    ::imc::logger::log::error() << "Expression seems to be not resolved: " << *this << std::endl;
                    return false;
                }

                if (!this->upperbound.value) {
                    ::imc::logger::log::error() << "Expression seems to be not resolved: " << *this << std::endl;
                    return false;
                }

                return *this->lowerbound.value <= val && val <= *this->upperbound.value;
            }

            const std::string FixedRangeRestriction::explain(void) const noexcept {
                std::stringstream ss;
                ss << "FixedRangeRestriction(";

                if (this->lowerbound.value && this->lowerbound.expression) {
                    ss << "lowerbound = ("
                        << *this->lowerbound.value << ", "
                        << *this->lowerbound.expression << ")";
                } else {
                    if (this->lowerbound.value) {
                        ss << "lowerbound = " << *this->lowerbound.value;
                    } else if (this->lowerbound.expression) {
                        ss << "lowerbound = " << *this->lowerbound.expression;
                    } else {
                        ss << "lowerbound = <none>";
                    }
                }

                ss << ", ";

                if (this->upperbound.value && this->upperbound.expression) {
                    ss << "upperbound = ("
                        << *this->upperbound.value << ", "
                        << *this->upperbound.expression << ")";
                } else {
                    if (this->upperbound.value) {
                        ss << "upperbound = " << *this->upperbound.value;
                    } else if (this->upperbound.expression) {
                        ss << "upperbound = " << *this->upperbound.expression;
                    } else {
                        ss << "upperbound = <none>";
                    }
                }

                ss << ")";

                return ss.str();
            }

            void FixedRangeRestriction::add_to_engine(chaiscript::ChaiScript& engine, std::string name) noexcept {
                ::imc::logger::log::debug() << "Computing " << *this << std::endl;
                if (this->lowerbound.value) {
                    std::string lower_name(name + "_lower");

                    ::imc::logger::log::debug()
                        << "Adding to engine: '"
                        << lower_name << "' = " << *this->lowerbound.value
                        << std::endl;

                    engine.add(chaiscript::var(*this->lowerbound.value), lower_name);
                }

                if (this->upperbound.value) {
                    std::string upper_name(name + "_upper");

                    ::imc::logger::log::debug()
                        << "Adding to engine: '"
                        << upper_name << "' = " << *this->upperbound.value
                        << std::endl;

                    engine.add(chaiscript::var(*this->upperbound.value), upper_name);
                }
            }

            void FixedRangeRestriction::compute_if_required_with_engine(chaiscript::ChaiScript& e, std::string name) noexcept {
                if (this->lowerbound.expression) {
                    std::string lower_name(name + "_lower");
                    imc::logger::log::log::debug()
                        << "Computed expression: " << *this->lowerbound.expression << std::endl;

                    unsigned int result = e.eval<unsigned int>(*this->lowerbound.expression);

                    imc::logger::log::log::debug()
                        << "Computed restriction: " << *this << " to have value = " << result << std::endl;

                    e.add(chaiscript::var(result), lower_name);
                    imc::logger::log::log::debug()
                        << "Re-registered '" << lower_name << " = " << result << std::endl;

                    this->lowerbound.value = result;
                }

                if (this->upperbound.expression) {
                    std::string upper_name(name + "_upper");
                    imc::logger::log::log::debug()
                        << "Computed expression: " << *this->upperbound.expression << std::endl;

                    unsigned int result = e.eval<unsigned int>(*this->upperbound.expression);

                    imc::logger::log::log::debug()
                        << "Computed restriction: " << *this << " to have value = " << result << std::endl;

                    e.add(chaiscript::var(result), upper_name);
                    imc::logger::log::log::debug()
                        << "Re-registered '" << upper_name << " = " << result << std::endl;

                    this->upperbound.value = result;
                }
            }

            bool FixedGtRestriction::matches(const std::string& value) const noexcept {
                unsigned int val;
                try {
                    val = std::stoul(value);
                } catch (const std::invalid_argument& e) {
                    optional<unsigned int> o_uint = imc::util::postfixed_to_uint(value);
                    if (o_uint) {
                        val = *o_uint;
                    } else {
                        ::imc::logger::log::error()
                            << "Cannot convert to integer: "
                            << value
                            << std::endl;
                        return false;
                    }
                } catch (const std::out_of_range& e) {
                    ::imc::logger::log::error() << "Out of range: " << value << std::endl;
                    return false;
                }

                if (!this->lowerbound.value) {
                    ::imc::logger::log::error() << "Expression seems to be not resolved: " << *this << std::endl;
                    return false;
                }


                return *this->lowerbound.value <= val;
            }

            void FixedGtRestriction::add_to_engine(chaiscript::ChaiScript& engine, std::string name) noexcept {
                if (this->lowerbound.value) {

                    ::imc::logger::log::debug()
                        << "Adding to engine: '"
                        << name << "' = " << *this->lowerbound.value
                        << std::endl;

                    engine.add(chaiscript::var(*this->lowerbound.value), name);
                }
            }

            void FixedGtRestriction::compute_if_required_with_engine(chaiscript::ChaiScript& e, std::string name) noexcept {
                if (this->lowerbound.expression) {
                    imc::logger::log::log::debug()
                        << "Computed expression: " << *this->lowerbound.expression << std::endl;

                    unsigned int result = e.eval<unsigned int>(*this->lowerbound.expression);

                    imc::logger::log::log::debug()
                        << "Computed restriction: " << *this << " to have value = " << result << std::endl;

                    e.add(chaiscript::var(result), name);
                    imc::logger::log::log::debug()
                        << "Re-registered '" << name << " = " << result << std::endl;

                    this->lowerbound.value = result;
                }
            }

            const std::string FixedGtRestriction::explain(void) const noexcept {
                std::stringstream ss;

                ss << "FixedGtRestriction(";
                if (this->lowerbound.value) {
                    ss << "lowerbound = " << *this->lowerbound.value;
                } else {
                    ss << "lowerbound = <none>";
                }
                ss << ")";

                return ss.str();
            }

            bool NotEmptyRestriction::matches(const std::string& value) const noexcept {
                return !value.empty();
            }

            const std::string NotEmptyRestriction::explain(void) const noexcept {
                std::stringstream ss;
                ss << "NotEmptyRestriction()";
                return ss.str();
            }

            void NotEmptyRestriction::add_to_engine(chaiscript::ChaiScript&, std::string) noexcept {
                return;
            }

            void NotEmptyRestriction::compute_if_required_with_engine(chaiscript::ChaiScript&, std::string) noexcept {
                // Nothing here.
                return;
            }

            bool AllRestriction::matches(const std::string&) const noexcept {
                return true;
            }

            const std::string AllRestriction::explain(void) const noexcept {
                std::stringstream ss;
                ss << "AllRestriction()";
                return ss.str();
            }

            void AllRestriction::add_to_engine(chaiscript::ChaiScript&, std::string) noexcept {
                return;
            }

            void AllRestriction::compute_if_required_with_engine(chaiscript::ChaiScript&, std::string) noexcept {
                // Nothing here.
                return;
            }

            void Parameter::set_restriction(std::shared_ptr<Restriction> r) noexcept {
                this->restriction = r;
            }

            void Parameter::set_len(unsigned int i) noexcept {
                this->len = i;
            }


            void Parameter::set_name(std::string name) noexcept {
                this->name = name;
            }

            unsigned int Parameter::get_len(void) const noexcept {
                return this->len;
            }

            std::string Parameter::get_name(void) const noexcept {
                return this->name;
            }

            const ParameterType& Parameter::get_type(void) const noexcept {
                return this->paramtype;
            }

            const std::shared_ptr<Restriction> Parameter::get_restriction(void) const noexcept {
                return this->restriction;
            }

            // Destructor for PlatformType, which is virtual, so the class itself is virtual
            PlatformType::~PlatformType() {}

            const std::string& PlatformType::get_name(void) const noexcept {
                return this->name;
            }

            const optional<std::vector<Parameter>>& PlatformType::get_parameters(void) const noexcept {
                return this->parameters;
            }

            void PlatformType::push_back_param(Parameter&& p) noexcept {
                if (!this->parameters) {
                    this->parameters = std::vector<Parameter>();
                }

                this->parameters->push_back(std::move(p));
            }

            void PlatformType::set_layout_generator(
                    std::shared_ptr<chaiscript::ChaiScript> engine,
                    std::string generator_fn_name
                ) noexcept
            {
                this->chai_engine = engine;
                this->generator_fn_name = generator_fn_name;
            }

            optional<LayoutGenerator> PlatformType::get_layout_generator(void) noexcept {
                if (!this->generator_fn_name) {
                    ::imc::logger::log::debug()
                        << "Not having a Name for a layout generator, so no generator"
                        << std::endl;
                    return {};
                }

                ::imc::logger::log::debug()
                    << "Found generator: " << *this->generator_fn_name
                    << std::endl;

                try {
                    return this->chai_engine->eval<LayoutGenerator>(*this->generator_fn_name);
                } catch (chaiscript::exception::eval_error& ee) {
                    ::imc::logger::log::error()
                        << "ChaiScript engine error: "
                        << ee.reason << " at "
                        << ee.start_position.line << ":" << ee.start_position.column
                        << std::endl
                        << ee.detail << std::endl;
                } catch (chaiscript::exception::bad_boxed_cast& bbc) {
                    ::imc::logger::log::error()
                        << "ChaiScript engine error: "
                        << bbc.what()
                        << std::endl;
                }

                return {};
            }


            void Dation::set_dataspec(DataSpec&& ds) noexcept {
                this->dataspec = ds;
            }

            void Dation::push_back_attribute(Attribute&& attr) noexcept {
                this->attributes.push_back(std::move(attr));
            }

            optional<unsigned int> Dation::get_instances(void) const noexcept {
                return this->instances;
            }

            std::vector<Attribute> Dation::get_attributes(void) const noexcept {
                return this->attributes;
            }

            DataSpec Dation::get_dataspec(void) const noexcept {
                return this->dataspec;
            }

            void Configuration::set_instances(unsigned int i) noexcept {
                this->instances = i;
            }

            Platform * Platform::instance = 0;

            const std::vector<Dation>& Platform::get_dations(void) const noexcept {
                return this->dations;
            }

            const std::vector<Signal>& Platform::get_signals(void) const noexcept {
                return this->signals;
            }

            const std::vector<Interrupt>& Platform::get_interrupts(void) const noexcept {
                return this->interrupts;
            }

            const std::vector<Configuration>& Platform::get_configurations(void) const noexcept {
                return this->configurations;
            }

            const std::vector<Connection>& Platform::get_connections(void) const noexcept {
                return this->connections;
            }

            // Destructor for AssociationRequireableType, which is virtual, so the class itself is virtual
            AssociationRequireableType::~AssociationRequireableType() { }

            // Destructor for AssociationProviderType , which is virtual, so the class itself is virtual
            AssociationProviderType::~AssociationProviderType() { }
        }
    }
}
