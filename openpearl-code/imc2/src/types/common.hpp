#ifndef IMC_TYPES_COMMON_HPP
#define IMC_TYPES_COMMON_HPP

#include <experimental/filesystem>
#include <experimental/optional>

#include <string>

namespace imc {
    namespace types {
        namespace common {

            template <typename T>
            using optional  = ::std::experimental::optional<T>;
            using path      = ::std::experimental::filesystem::path;

            enum ParameterType {
                Fixed,
                Char,
                Bit
            };

            /**
             * @brief Trait for fetching metadata of source
             */
            class FileLocation {
                private:

                    path                  filepath;
                    unsigned int          line;
                    unsigned int          column;
                    optional<std::string> token;

                public:

                    FileLocation() = default;
                    ~FileLocation() = default;

                    void set_file(path p) {
                        this->filepath = p;
                    }

                    void set_line(unsigned int i) {
                        this->line = i;
                    }

                    void set_column(unsigned int i) {
                        this->column = i;
                    }

                    void get_token(std::string s) {
                        this->token = s;
                    }

                    path const & get_file() const {
                        return this->filepath;
                    }

                    unsigned int get_line() const {
                        return this->line;
                    }

                    unsigned int get_column() const {
                        return this->column;
                    }

                    optional<std::string> get_token() const {
                        return this->token;
                    }
            };

            using TypeRepresentator = std::string;
            using Attribute         = TypeRepresentator;
            using DataSpec          = TypeRepresentator;

        } // common
    } // types
} // imc

#endif // IMC_TYPES_COMMON_HPP
