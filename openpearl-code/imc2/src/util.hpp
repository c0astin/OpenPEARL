#ifndef IMC_UTIL_HPP
#define IMC_UTIL_HPP

#include <vector>
#include <string>
#include <sstream>
#include <iomanip>

#include <experimental/optional>

namespace imc {

    namespace util {
        template <typename T>
        using optional  = ::std::experimental::optional<T>;

        std::vector<std::string> strsplit(const std::string& s, const char tok) noexcept;

        bool strEndsWith(std::string const& fullString, std::string const& ending) noexcept;

        // Prints the argument to a hex string
        // Does NOT prepend "0x"!
        template<typename T>
        std::string toHexString(T t) noexcept {
            std::stringstream ss;
            ss << std::hex << t;
            return ss.str();
        }

        std::string trim(std::string const& s) noexcept;

        optional<int> postfixed_to_int(const std::string& str) noexcept;
        optional<int> postfixed_to_uint(const std::string& str) noexcept;

        bool is_blank(const std::string& s) noexcept;

    }

}

#endif // IMC_UTIL_HPP
