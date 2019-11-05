#ifndef IMC_READER_HPP
#define IMC_READER_HPP

#include <experimental/filesystem>
#include <experimental/optional>

#include <pugixml.hpp>

namespace imc {
    namespace reader {

        template <typename T>
        using optional  = ::std::experimental::optional<T>;
        using path      = ::std::experimental::filesystem::path;

        /*! @brief Call operator
         *
         * The XMLReader types are implemented as functors. Because recent GCC lacks
         * `<variant>`, we return `optional<T>` with certain implications which are described in the
         * documentation of the call operator.
         */
        template <typename T>
        class Reader {
            protected:
                path filepath;

            public:
                Reader(path fpath) : filepath(fpath) { };
                Reader(Reader&&) = default;

                /** @brief Call operator required function overload
                 *
                 * First of all, an `optional<T>` _containing_ a value means nothing more than a
                 * successful operation, whereas an empty `optional<T>` marks a failed operation.
                 *
                 * The call operator prints error messages in the error case, so that aborting the
                 * program after a call to the operator can be done without further printing of
                 * error text.
                 *
                 * This is not that nice, but better than constructing some ugly `<variant>` like
                 * thing to return here.
                 *
                 * @todo Port call operator to return `variant<T, E>`.
                 */
                virtual optional<T> operator()(void) = 0;
        };

        pugi::xml_parse_result load_xml(const path& p) noexcept;

    } // reader
} // imc

#endif // IMC_READER_HPP
