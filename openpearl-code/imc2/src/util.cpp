#include <vector>
#include <string>
#include <sstream>

#include <experimental/optional>

#include "logger.hpp"

template <typename T>
using optional  = ::std::experimental::optional<T>;

std::string ltrim(std::string str) {
    auto it2 = std::find_if(str.begin(), str.end(),
            [](char ch) {
                return !std::isspace<char>(ch, std::locale::classic());
            });
    str.erase(str.begin(), it2);
    return str;
}

std::string rtrim(std::string str) {
    auto it1 = std::find_if(str.rbegin(), str.rend(),
            [](char ch) {
                return !std::isspace<char>(ch, std::locale::classic());
            });

    str.erase(it1.base(), str.end());
    return str;
}


namespace imc {

    namespace util {

        std::vector<std::string> strsplit(const std::string& s, const char tok) noexcept {
            std::stringstream ss(s);
            std::string item;
            std::vector<std::string> tokens;

            while(getline(ss, item, tok)) {
                tokens.push_back(item);
            }

            return tokens;
        }

        bool strEndsWith(std::string const& fullString, std::string const& ending) noexcept {
            if (fullString.length() >= ending.length()) {
                return (0 == fullString.compare(fullString.length() - ending.length(), ending.length(), ending));
            } else {
                return false;
            }
        }

        std::string trim(std::string str) noexcept {
            return ltrim(rtrim(str));
        }

        optional<int> postfixed_to_int(const std::string& str) noexcept {
            auto val = 0;
            size_t pos;

            auto text = trim(str);
            ::imc::logger::log::debug() << "Trimmed: '" << str << "' to '" << text << "'" << std::endl;

            try {
                if (strEndsWith(text, "'B")) {
                    val = std::stoi(text.substr(1, text.size() - 2), &pos, 2);
                } else if (strEndsWith(text, "'B1")) {
                    val = std::stoi(text.substr(1, text.size() - 3), &pos, 2);
                } else if (strEndsWith(text, "'B2")) {
                    val = std::stoi(text.substr(1, text.size() - 3), &pos, 4);
                } else if (strEndsWith(text, "'B3")) {
                    val = std::stoi(text.substr(1, text.size() - 3), &pos, 8);
                } else if (strEndsWith(text, "'B4")) {
                    val = std::stoi(text.substr(1, text.size() - 3), &pos, 16);
                } else {
                    ::imc::logger::log::error()
                        << "Illegal value: '" << text << "'" << std::endl;
                    return {};
                }
            } catch (std::invalid_argument&) {
                ::imc::logger::log::error()
                    << "Invalid value: <<" << text << ">>"
                    << std::endl;
                return {};
            } catch (std::out_of_range&) {
                ::imc::logger::log::error()
                    << "Value Out of Range: " << text
                    << std::endl;

                return {};
            }

            return val;
        }

        optional<unsigned int> postfixed_to_uint(const std::string& str) noexcept {
            auto val = 0;
            size_t pos;

            auto text = trim(str);
            ::imc::logger::log::debug() << "Trimmed: '" << str << "' to '" << text << "'" << std::endl;

            try {
                if (strEndsWith(text, "'B")) {
                    val = std::stoul(text.substr(1, text.size() - 2), &pos, 2);
                } else if (strEndsWith(text, "'B1")) {
                    val = std::stoul(text.substr(1, text.size() - 3), &pos, 2);
                } else if (strEndsWith(text, "'B2")) {
                    val = std::stoul(text.substr(1, text.size() - 3), &pos, 4);
                } else if (strEndsWith(text, "'B3")) {
                    val = std::stoul(text.substr(1, text.size() - 3), &pos, 8);
                } else if (strEndsWith(text, "'B4")) {
                    val = std::stoul(text.substr(1, text.size() - 3), &pos, 16);
                } else {
                    ::imc::logger::log::error()
                        << "Illegal value: '" << text << "'" << std::endl;
                    return {};
                }
            } catch (std::invalid_argument&) {
                ::imc::logger::log::error()
                    << "Invalid value: <<" << text << ">>"
                    << std::endl;
                return {};
            } catch (std::out_of_range&) {
                ::imc::logger::log::error()
                    << "Value Out of Range: " << text
                    << std::endl;

                return {};
            }

            return val;
        }

        bool is_blank(const std::string& s) noexcept {
            return std::all_of(s.cbegin(), s.cend(), [](char c) { return std::isspace(c); });
        }

    }

}

