#include "checker/error.hpp"

namespace imc {
    namespace checker {
        std::string error::as_string(void) const noexcept {
            return this->msg.str();
        }

        imc::checker::error& error::operator<<(const std::string& s) noexcept {
            this->msg << s;
            return *this;
        }

        imc::checker::error& error::operator<<(bool b)             noexcept {
            this->msg << b;
            return *this;
        }

        imc::checker::error& error::operator<<(int i)             noexcept {
            this->msg << i;
            return *this;
        }

        imc::checker::error& error::operator<<(char c)             noexcept {
            this->msg << c;
            return *this;
        }

        imc::checker::error& error::operator<<(unsigned int u)    noexcept {
            this->msg << u;
            return *this;
        }

        imc::checker::error& error::operator<<(size_t s)          noexcept {
            this->msg << s;
            return *this;
        }

        imc::checker::error& error::operator<<(const char* s)     noexcept {
            this->msg << s;
            return *this;
        }

        imc::checker::error& error::operator<<(std::ostream& (*os)(std::ostream&)) noexcept {
            this->msg << os;
            return *this;
        }

        imc::checker::error& error::operator<<(const ParameterType& pt) noexcept {
            switch (pt) {
                case ParameterType::Fixed:
                    this->msg << "ParameterType(Fixed)";
                    break;
                case ParameterType::Char:
                    this->msg << "ParameterType(Char)";
                    break;
                case ParameterType::Bit:
                    this->msg << "ParameterType(Bit)";
                    break;
            }
            return *this;
        }

        imc::checker::error& error::operator<<(const ParameterInstance& p) noexcept {
            this->msg
                << "ParameterInstance("
                << "value = '" << p.get_value() << "'"
                << ", type = " << p.get_type()
                << "')";
            return *this;
        }

        imc::checker::error& error::operator<<(const Restriction& r) noexcept {
            this->msg << r.explain();
            return *this;
        }

        imc::checker::error& error::operator<<(const FixedRangeRestriction& r) noexcept {
            this->msg << r.explain();
            return *this;
        }

        imc::checker::error& error::operator<<(const FixedGtRestriction& r) noexcept {
            this->msg << r.explain();
            return *this;
        }

        imc::checker::error& error::operator<<(const NotEmptyRestriction& r) noexcept {
            this->msg << r.explain();
            return *this;
        }

        imc::checker::error& error::operator<<(const AllRestriction& r) noexcept {
            this->msg << r.explain();
            return *this;
        }

        imc::checker::error& error::operator<<(const Layout& l) noexcept {
            this->msg
                << "Layout(name = " << l.getDeviceId()
                << ", addr = " << l.getAddress()
                << ", bits = [";
            for (auto bit : l.getBits()) {
                this->msg << (bit ? "1" : "0") << ", ";
            }
            this->msg << "])";
            return *this;
        }

    }
}

