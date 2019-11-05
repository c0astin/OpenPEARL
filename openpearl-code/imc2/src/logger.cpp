#include <iostream>
#include "logger.hpp"

std::string node_type_to_str(const pugi::xml_node_type& ty) noexcept;

extern imc::logger::loglevel GLOBAL_LOG_LEVEL;

namespace imc {
    namespace logger {

        std::ostream& operator<<(std::ostream& os, imc::logger::loglevel& ll) {
            switch (ll) {
                case loglevel::DEBUG:
                    os << "[DEBUG]: ";
                    break;

                case loglevel::INFO:
                    os << "[INFO]:  ";
                    break;

                case loglevel::WARN:
                    os << "[WARN]:  ";
                    break;

                case loglevel::ERROR:
                    os << "[ERROR]: ";
                    break;
            }
            return os;
        }

        log::~log() {
            if (GLOBAL_LOG_LEVEL <= this->level) {
                std::cerr << this->level << this->out.str();
            }
        }

        imc::logger::log& log::operator<<(const std::string& s) noexcept {
            this->out << s;
            return *this;
        }

        imc::logger::log& log::operator<<(bool b)             noexcept {
            this->out << b;
            return *this;
        }

        imc::logger::log& log::operator<<(int i)             noexcept {
            this->out << i;
            return *this;
        }

        imc::logger::log& log::operator<<(char c)             noexcept {
            this->out << c;
            return *this;
        }

        imc::logger::log& log::operator<<(unsigned int u)    noexcept {
            this->out << u;
            return *this;
        }

        imc::logger::log& log::operator<<(size_t s)          noexcept {
            this->out << s;
            return *this;
        }

        imc::logger::log& log::operator<<(const char* s)     noexcept {
            this->out << s;
            return *this;
        }

        imc::logger::log& log::operator<<(std::ostream& (*os)(std::ostream&)) noexcept {
            this->out << os;
            return *this;
        }

        imc::logger::log& log::operator<<(const Configuration& c) noexcept {
            this->out
                << "Configuration("
                << "name = '" << c.get_name() << "'"
                << ")";
            return *this;
        }

        imc::logger::log& log::operator<<(const Parameter& p) noexcept {
            std::string nickname;
            auto nick = p.get_nick();
            if (!nick) {
                nickname = "<none>";
            } else {
                nickname = *nick;
            }

            this->out
                << "Parameter("
                << "len = " << p.get_len()
                << ", nick = '" << nickname << "'"
                << ")";
            return *this;
        }

        imc::logger::log& log::operator<<(const ParameterType& pt) noexcept {
            switch (pt) {
                case ParameterType::Fixed:
                    this->out << "ParameterType(Fixed)";
                    break;
                case ParameterType::Char:
                    this->out << "ParameterType(Char)";
                    break;
                case ParameterType::Bit:
                    this->out << "ParameterType(Bit)";
                    break;
                default:
                    this->out << "ParameterType(ILLEGAL)";
                    break;
            }
            return *this;
        }

        imc::logger::log& log::operator<<(const ParameterInstance& p) noexcept {
            *this
                << "ParameterInstance("
                << "value = '" << p.get_value() << "'"
                << ", type = " << p.get_type()
                << "')";
            return *this;
        }

        imc::logger::log& log::operator<<(const Restriction& r) noexcept {
            this->out << r.explain();
            return *this;
        }

        imc::logger::log& log::operator<<(const FixedRangeRestriction& r) noexcept {
            this->out << r.explain();
            return *this;
        }

        imc::logger::log& log::operator<<(const FixedGtRestriction& r) noexcept {
            this->out << r.explain();
            return *this;
        }

        imc::logger::log& log::operator<<(const NotEmptyRestriction& r) noexcept {
            this->out << r.explain();
            return *this;
        }

        imc::logger::log& log::operator<<(const AllRestriction& r) noexcept {
            this->out << r.explain();
            return *this;
        }

        imc::logger::log& log::operator<<(const Dation& d) noexcept {
            auto opt_instances = d.get_instances();
            std::string instances = "<none>";
            if (opt_instances) {
                instances = *opt_instances ;
            }

            auto opt_association_provider = d.get_require_association_provider();
            std::string association_provider = "<none>";
            if (opt_association_provider) {
                association_provider = *opt_association_provider;
            }

            auto opt_require_association_provider = d.get_require_association_provider();
            std::string require_association_provider = "<none>";
            if (opt_require_association_provider) {
                require_association_provider = *opt_require_association_provider ;
            }

            this->out
                << "Dation("
                << "name = '" << d.get_name() << "'"
                << ", instances = " << instances
                << ", attributes = [";

            for (auto& a : d.get_attributes()) {
                this->out << a << ", ";
            }

            this->out << "]"
                << ", dataspec = '" << d.get_dataspec() << "'"
                << ", association_provider = " << association_provider
                << ", require_association_provider = " << require_association_provider
                << ")";

            return *this;
        }

        imc::logger::log& log::operator<<(const Signal& s) noexcept {
            this->out
                << "Signal("
                << s.get_name()
                << ")";
            return *this;
        }

        imc::logger::log& log::operator<<(const Interrupt& i) noexcept {
            this->out
                << "Interrupt("
                << "name = '" << i.get_name() << "'"
                << ")";
            return *this;
        }

        imc::logger::log& log::operator<<(const Connection& c) noexcept {
            this->out
                << "Connection("
                << "name = '" << c.get_name() << "'"
                << ")";
            return *this;
        }

        imc::logger::log& log::operator<<(const UserName& un) noexcept {
            *this
                << "UserName("
                << "name = '" << un.get_name() << "'"
                << " => sysname = '" << un.get_system_name() << "',"
                << " parameters = " << un.get_parameter_instances()
                << ") at "
                << un.get_filelocation();
            return *this;
        }

        imc::logger::log& log::operator<<(const SystemName& sn) noexcept {
            this->out
                << "UserName("
                << "name = '" << sn.get_name() << "'"
                << ")";
            return *this;
        }

        imc::logger::log& log::operator<<(const Specification& spec) noexcept {
            this->out
                << "Specification("
                << "name = '" << spec.get_name() << "'"
                << ", type = '" << spec.get_type() << "'"
                << ", attributes = [";

            for (auto& a : spec.get_attributes()) {
                this->out << a << ", ";
            }

            this->out
                << "]"
                << ", dataspec = " << spec.get_dataspec()
                << ")";

            return *this;
        }

        imc::logger::log& log::operator<<(const Declaration& spec) noexcept {
            this->out
                << "Declaration("
                << "name = '" << spec.get_name() << "'"
                << ", type = '" << spec.get_type() << "'"
                << ", attributes = [";

            for (auto& a : spec.get_attributes()) {
                this->out << a << ", ";
            }

            this->out
                << "]"
                << ", dataspec = " << spec.get_dataspec()
                << ")";

            return *this;
        }

        imc::logger::log& log::operator<<(const FileLocation& floc) noexcept {
            this->out
                << "FileLocation(file = " << floc.get_file()
                << ", line = " << floc.get_line()
                << ", column = " << floc.get_column();

            auto token = floc.get_token();
            if (token) {
                this->out << ", token = " << *token;
            }

            this->out << ")";

            return *this;
        }

        imc::logger::log& log::operator<<(const Layout& layout) noexcept {
            *this
                << "Layout(device_id = " << layout.getDeviceId()
                << ", address = " << layout.getAddress()
                << ", bits = " << layout.getBits()
                << ")";

            return *this;
        }

        imc::logger::log& log::operator<<(const Module& module) noexcept {
            *this
                << "Module: {{{" << std::endl
                << "\tProblem parts: " << std::endl;

            for (const auto& problem_part: module.problem_parts) {
                *this << "\t * " << problem_part << std::endl;
            }

            *this << "\tSystem parts: " << std::endl;

            for (const auto& system_part: module.system_parts) {
                *this << "\t * " << system_part << std::endl;
            }

            *this << "}}}" << std::endl;
            return *this;
        }

        imc::logger::log& log::operator<<(const Problem& p) noexcept {
            *this
                << "Problem(spcs = " << p.get_spcs() << ", decls = " << p.get_decls() << ")"
                << std::endl;
            return *this;
        }

        imc::logger::log& log::operator<<(const System& s) noexcept {
            *this
                << "System(usernames = " << s.usernames << ", sysnames = " << s.sysnames << ")"
                << std::endl;
            return *this;
        }

        imc::logger::log& log::operator<<(const Type& ty) noexcept {
            switch (ty) {
                case Type::Interrupt:
                    this->out << "Type(Interrupt)";
                    break;
                case Type::Dation:
                    this->out << "Type(Dation)";
                    break;
                case Type::Signal:
                    this->out << "Type(Signal)";
                    break;
                default:
                    this->out << "Type(<unknown>)";
                    break;
            }
            return *this;
        }


        imc::logger::log& log::operator<<(const pugi::xml_node& node) noexcept {
            this->out
                << "pugi::xml_node("
                << "name = "           << node.name()
                << ", type = "         << node_type_to_str(node.type())
                << ", empty = "        << node.empty()
                << ") = '" << node.text().as_string() << "'";
            return *this;
        }

    } // logger
} // imc



std::string node_type_to_str(const pugi::xml_node_type& ty) noexcept {
    switch (ty) {
        case pugi::xml_node_type::node_null:
            return "null";
            break;

        case pugi::xml_node_type::node_document:
            return "document";
            break;

        case pugi::xml_node_type::node_element:
            return "element";
            break;

        case pugi::xml_node_type::node_pcdata:
            return "pcdata";
            break;

        case pugi::xml_node_type::node_cdata:
            return "cdata";
            break;

        case pugi::xml_node_type::node_comment:
            return "comment";
            break;

        case pugi::xml_node_type::node_pi:
            return "pi";
            break;

        case pugi::xml_node_type::node_declaration:
            return "declaration";
            break;

        case pugi::xml_node_type::node_doctype:
            return "doctype";
            break;
    }
    return "unknown";
}

