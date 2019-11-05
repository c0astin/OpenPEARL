#ifndef IMC_TYPES_PROBLEM_HPP
#define IMC_TYPES_PROBLEM_HPP

#include <vector>
#include <string>
#include <experimental/optional>

#include "types/common.hpp"

namespace imc {
    namespace types {
        namespace problem {
            template <typename T>
            using optional  = ::std::experimental::optional<T>;

            using FileLocation  = types::common::FileLocation;
            using Attribute     = types::common::Attribute;
            using DataSpec      = types::common::DataSpec;

            enum Type {
                Interrupt,
                Dation,
                Signal
            };

            class Specification {
                private:

                    Type                    type;
                    std::string             name;
                    std::vector<Attribute>  attributes;
                    DataSpec                data;
                    FileLocation            fl;

                public:

                    Specification(
                            Type ty,
                            std::string n,
                            std::vector<Attribute> attrs,
                            DataSpec ds,
                            FileLocation fl = FileLocation()
                        )
                        : type(ty)
                        , name(n)
                        , attributes(attrs)
                        , data(ds)
                        , fl(fl)
                    {
                        // empty
                    }

                    ~Specification() = default;

                    Type                 get_type(void)       const noexcept;
                    std::string          get_name(void)       const noexcept;
                    std::vector<Attribute> get_attributes(void) const noexcept;
                    DataSpec             get_dataspec(void)   const noexcept;

            };

            class Declaration {
                private:

                    Type                    type;
                    std::string             name;
                    std::vector<Attribute>  attributes;
                    DataSpec                data;
                    FileLocation            fl;

                public:

                    Declaration(
                            Type ty,
                            std::string n,
                            std::vector<Attribute> attrs,
                            DataSpec ds,
                            FileLocation fl = FileLocation()
                        )
                        : type(ty)
                        , name(n)
                        , attributes(attrs)
                        , data(ds)
                        , fl(fl)
                    {
                        // empty
                    }

                    ~Declaration() = default;

                    Type                 get_type(void)       const noexcept;
                    std::string          get_name(void)       const noexcept;
                    std::vector<Attribute> get_attributes(void) const noexcept;
                    DataSpec             get_dataspec(void)   const noexcept;
            };

            class Problem {
                private:

                    std::vector<Specification>    spec;
                    std::vector<Declaration>      decl;

                public:

                    Problem(std::vector<Specification> spcs,
                            std::vector<Declaration> dcls)
                        : spec(spcs)
                        , decl(dcls)
                    {
                        // Nothing
                    }

                    const std::vector<Declaration>& get_decls(void) const noexcept;
                    const std::vector<Specification>& get_spcs(void) const noexcept;
            };

        } // problem
    } // types
} // imc

#endif // IMC_TYPES_PROBLEM_HPP
