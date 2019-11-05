#ifndef IMC_CODEGEN_HPP
#define IMC_CODEGEN_HPP

#include <experimental/filesystem>
#include <experimental/optional>
#include <iostream>
#include <fstream>

#include "types/common.hpp"
#include "types/module.hpp"
#include "types/platform.hpp"
#include "types/problem.hpp"
#include "types/system.hpp"

#ifndef CODE_INDENT_WIDTH
#define CODE_INDENT_WIDTH (4)
#endif

namespace imc {
    namespace codegen {

            template<typename T>
            using optional                = ::std::experimental::optional<T>;
            using Attribute               = imc::types::common::Attribute;
            using ParameterType           = imc::types::platform::ParameterType;
            using DataSpec                = imc::types::common::DataSpec;
            using log                     = imc::logger::log;
            using Module                  = imc::types::module::Module;
            using UserName                = imc::types::system::UserName;
            using ParameterInstance       = imc::types::system::ParameterInstance;
            using Platform                = imc::types::platform::Platform;

            enum Type {
                Configuration,
                Connection,
                Dation,
                Interrupt,
                Signal,
            };

            struct ForwardDeclaration {
                std::string sysname;
                std::string name;
                Type        type;

                ForwardDeclaration(const UserName& username, Type type)
                    : sysname(std::move(username.get_system_name()))
                    , name(std::move(username.get_name()))
                    , type(type)
                {
                    // empty
                }

                static optional<ForwardDeclaration> mk(
                        const UserName& username,
                        const Platform& platform)
                    noexcept;
            };


            struct InitializationCodeSnippet {
                std::string                     sysname;
                std::string                     name;
                Type                            type;
                std::vector<ParameterInstance>  params;

                // See the documentation of `generate_item_number()`
                unsigned int                    item_number;

                InitializationCodeSnippet(const UserName& username, Type type)
                    : sysname(std::move(username.get_system_name()))
                    , name(std::move(username.get_name()))
                    , type(type)
                    , params(username.get_parameter_instances())
                {
                    // empty
                }

                static optional<InitializationCodeSnippet> mk(
                        const UserName& username,
                        const Platform& platform)
                    noexcept;

                /**
                 * We need the item number because the old IMC generated config elements with the
                 * variable name `config<number>`.
                 * The old IMC used a abomination of state for generating this, which I'm not
                 * willing to implement.
                 *
                 * So, the shortest way to implement this "properly" (resembling the functionality
                 * of the old IMC) is to have a "item_number" variable for each
                 * InitializationCodeSnippet object.
                 *
                 * This variable is set by the caller using the helper function
                 * `generate_item_number`, which depends on the `type` variable and returns the
                 * item number for the next InitializationCodeSnippet.
                 */
                unsigned int generate_item_number(unsigned int number) noexcept {
                    if (this->type == Type::Configuration) {
                            this->item_number = number;
                            return ++number;
                    }
                    return number;
                }
            };

            class Indentation {
                unsigned int level;

                public:
                    explicit Indentation(unsigned int i) : level(i) {}

                    std::string to_str(void) const noexcept {
                        return std::string(this->level * CODE_INDENT_WIDTH, ' ');
                    }
            };


            class Codegen {
                private:
                    std::vector<Module> modules;
                    Platform            platform;
                    std::string         filepath;
                    std::ofstream       out;

                public:

                    Codegen(std::vector<Module>&& mods, Platform&& pltfm, std::string filepath)
                        : modules(std::move(mods))
                        , platform(std::move(pltfm))
                        , filepath(std::move(filepath))
                    {
                        // nothing
                    }

                    bool open(void) noexcept {
                        try {
                            this->out.open(this->filepath, std::ios::out);
                        } catch (void* e) {
                            // I don't even know (or can find in the docs) which exceptions might
                            // be thrown. So catch all and forget.
                            return false;
                        }

                        return true;
                    }

                    bool write_out(void) noexcept;

                private:

                    template<typename T>
                    Codegen& operator<<(std::basic_ostream<T>&& os) noexcept {
                        this->out << os;
                        return *this;
                    }

                    Codegen& operator<<(const std::string& s)               noexcept;
                    Codegen& operator<<(char c)                             noexcept;
                    Codegen& operator<<(int i)                              noexcept;
                    Codegen& operator<<(unsigned int u)                     noexcept;
                    Codegen& operator<<(const char* s)                      noexcept;
                    Codegen& operator<<(std::ostream& (*pf)(std::ostream&)) noexcept; // for std::endl
                    Codegen& operator<<(const Indentation& ind)             noexcept;

                    Codegen& operator<<(const ForwardDeclaration& fd) noexcept;
                    Codegen& operator<<(const InitializationCodeSnippet& ics) noexcept;
                    Codegen& operator<<(const std::vector<ParameterInstance>& vec) noexcept;
                    Codegen& operator<<(const ParameterInstance& pi) noexcept;
            };


    } // codegen
} // imc

#endif // IMC_CODEGEN_HPP


