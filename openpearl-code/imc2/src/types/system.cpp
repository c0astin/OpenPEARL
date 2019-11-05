#include <experimental/optional>
#include <vector>
#include <bitset>

#include "types/common.hpp"
#include "types/system.hpp"
#include "util.hpp"
#include "logger.hpp"

template<typename T>
using optional  = ::std::experimental::optional<T>;

namespace imc {
    namespace types {
        namespace system {

            ParameterInstance::ParameterInstance(ParameterType pt, std::string v)
                : paramtype(pt)
                , value(v)
            {
                ::imc::logger::log::debug()
                    << "Construct ParameterInstance: "
                    << this->paramtype
                    << " - "
                    << this->value
                    << std::endl;
            }

            // TODO: Add real implementation
            size_t ParameterInstance::get_computed_length(void) const noexcept {
                auto no_bits = 0;

                switch (this->get_type()) {
                    case ParameterType::Fixed:
                        {
                            try {
                                no_bits = std::stoi(this->get_value());
                            } catch (std::invalid_argument&) {
                                // TODO Care about this
                            } catch (std::out_of_range&) {
                                // TODO Care about this
                            }

                            break;
                        }

                    case ParameterType::Char:
                        no_bits = this->get_value().size() - 2;
                        break;

                    case ParameterType::Bit:
                        {
                            auto val = util::postfixed_to_int(this->get_value());
                            if (val) {
                                std::bitset<8> bs(*val);
                                no_bits = bs.size();
                            } else {
                                // TODO
                            }

                            break;
                        }

                    default:
                        // Error out;
                        break;
                }

                return no_bits;
            }

            const std::vector<ParameterInstance>& SystemName::get_parameter_instances(void)
                const noexcept
            {
                return this->params;
            }

            std::string SystemName::get_name(void) const noexcept {
                return this->name;
            }

            const std::vector<ParameterInstance>& UserName::get_parameter_instances(void)
                const noexcept
            {
                return this->parameters;
            }

            std::string UserName::get_name(void) const noexcept {
                return this->name;
            }

            std::string UserName::get_system_name(void) const noexcept {
                return this->systemname;
            }

            const FileLocation& UserName::get_filelocation(void) const noexcept {
                return this->file_location;
            }

        }
    }
}
