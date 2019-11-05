#include <vector>
#include <experimental/optional>
#include <memory>

#include "chaiscript/chaiscript.hpp"

#include "types/platform.hpp"
#include "expr/computer.hpp"
#include "logger.hpp"

template <typename T>
using optional              = ::std::experimental::optional<T>;
using Restriction           = imc::types::platform::Restriction;
using FixedRangeRestriction = imc::types::platform::FixedRangeRestriction;
using FixedGtRestriction    = imc::types::platform::FixedGtRestriction;
using Parameter             = imc::types::platform::Parameter;
using Platform              = imc::types::platform::Platform;
using PlatformType          = imc::types::platform::PlatformType;
using LayoutGenerator       = imc::types::platform::LayoutGenerator;
using Layout                = imc::types::platform::Layout;

struct Nick {
    std::string name;
    std::shared_ptr<Restriction> restriction;
    unsigned int len;

    Nick(std::string name, std::shared_ptr<Restriction> r, unsigned int len)
        : name(name)
        , restriction(r)
        , len(len)
    {
        // nothing
    }

};

std::vector<Nick> fetch_nicks(const Platform& pl) noexcept {
    std::vector<Nick> nicks;
    auto accumulate_nicks = [&](const PlatformType& pl) {
        auto params = pl.get_parameters();
        if (params) {
            for (auto& param : *params) {
                auto nick = param.get_nick();
                if (nick) {
                    imc::logger::log::log::debug() << "Having " << *nick << std::endl;
                    Nick n(*nick, param.get_restriction(), param.get_len());
                    nicks.push_back(n);
                }
            }
        }
    };

    imc::logger::log::log::debug() << "Accumulating nicknames: Dations" << std::endl;
    std::for_each(pl.get_dations().begin(), pl.get_dations().end(), accumulate_nicks);

    imc::logger::log::log::debug() << "Accumulating nicknames: Signals" << std::endl;
    std::for_each(pl.get_signals().begin(), pl.get_signals().end(), accumulate_nicks);

    imc::logger::log::log::debug() << "Accumulating nicknames: Interrupts" << std::endl;
    std::for_each(pl.get_interrupts().begin(), pl.get_interrupts().end(), accumulate_nicks);

    imc::logger::log::log::debug() << "Accumulating nicknames: Configurations" << std::endl;
    std::for_each(pl.get_configurations().begin(), pl.get_configurations().end(), accumulate_nicks);

    imc::logger::log::log::debug() << "Accumulating nicknames: Connections" << std::endl;
    std::for_each(pl.get_connections().begin(), pl.get_connections().end(), accumulate_nicks);

    return nicks;
}

std::vector<int> to_vector_int(const std::vector<chaiscript::Boxed_Value> &vs) {
    std::vector<int> vi;
    std::transform(vs.begin(),
            vs.end(),
            std::back_inserter(vi),
            [](const chaiscript::Boxed_Value &bv) {
                return chaiscript::Boxed_Number(bv).get_as<int>();
            });
    return vi;
}

namespace imc {
    namespace expr {
        bool compute(Platform& platform) noexcept {
            chaiscript::ChaiScript scriptengine;

            imc::logger::log::log::debug() << "Accumulating nicknames..." << std::endl;

            // Collect all "nick" names from platform, enter into engine as variables
            std::vector<Nick> nicks = fetch_nicks(platform);

            // Collect all Values which are expressions, pass expression to engine, fill out
            // Value.value member
            imc::logger::log::log::debug() << "Registering nicknames in scripting engine" << std::endl;
            for (auto& nick: nicks) {
                imc::logger::log::log::debug() << "Registering '" << nick.name << "'" << std::endl;
                nick.restriction->add_to_engine(scriptengine, nick.name);
            }

            imc::logger::log::log::debug() << "Evaluating expressions" << std::endl;

            for (auto& nick: nicks) {
                imc::logger::log::log::debug()
                    << "Computing restriction: " << *nick.restriction << std::endl;

                nick.restriction->compute_if_required_with_engine(scriptengine, nick.name);

                imc::logger::log::log::debug()
                    << "Computed restriction: " << *nick.restriction << std::endl;
            }

            // In case of Error, print and return
            return true;
        }

        bool create_layout_generator(
                std::shared_ptr<chaiscript::ChaiScript> engine,
                const std::string code,
                const std::string generator_name
            ) noexcept
        {
            // registering of the code
            try {
                engine->eval(code);
            } catch (chaiscript::exception::eval_error& ee) {
                ::imc::logger::log::error()
                    << "ChaiScript engine error: "
                    << ee.reason << " at "
                    << ee.start_position.line << ":" << ee.start_position.column
                    << std::endl
                    << ee.detail << std::endl;

                return false;
            } catch (chaiscript::exception::bad_boxed_cast& bbc) {
                ::imc::logger::log::error()
                    << "ChaiScript engine error: "
                    << bbc.what()
                    << std::endl;

                return false;
            }

            // Checking whether the `generator_name` actually refers to a LayoutGenerator.
            try {
                auto gen = engine->eval<LayoutGenerator>(generator_name);
            } catch (chaiscript::exception::eval_error& ee) {
                ::imc::logger::log::error()
                    << "ChaiScript engine error: "
                    << ee.reason << " at "
                    << ee.start_position.line << ":" << ee.start_position.column
                    << std::endl
                    << ee.detail << std::endl;

                return false;
            } catch (chaiscript::exception::bad_boxed_cast& bbc) {
                ::imc::logger::log::error()
                    << "ChaiScript engine error: "
                    << bbc.what()
                    << std::endl;

                return false;
            }

            return true;
        }

        void register_layout_type(std::shared_ptr<chaiscript::ChaiScript> engine)
            noexcept
        {
            //engine->add(chaiscript::constructor<Layout (std::string)>(), "Layout");
            //engine->add(chaiscript::user_type<Layout>(), "Layout");

            chaiscript::ModulePtr m = chaiscript::ModulePtr(new chaiscript::Module());
            chaiscript::utility::add_class<Layout>(*m,
                    "Layout",
                    {
                        chaiscript::constructor<Layout()>(),
                        chaiscript::constructor<Layout(std::string)>(),
                        chaiscript::constructor<Layout (const Layout &)>(),
                    },

                    {
                        { chaiscript::fun(&Layout::setDeviceId),     "setDeviceId" },
                        { chaiscript::fun(&Layout::getDeviceId),     "getDeviceId" },
                        { chaiscript::fun(&Layout::setAddress),      "setAddress" },
                        { chaiscript::fun(&Layout::getAddress),      "getAddress" },
                        { chaiscript::fun(&Layout::getBits),         "getBits" },
                        { chaiscript::fun(&Layout::setBits),         "setBits" },
                    });


            engine->add(m);

            engine->add(chaiscript::vector_conversion<std::vector<int>>());
            engine->add(chaiscript::type_conversion<std::vector<chaiscript::Boxed_Value>,
                    std::vector<int>>(
                        [](const std::vector<chaiscript::Boxed_Value> &t_bvs) {
                            return to_vector_int(t_bvs);
                        }
                    )
                    );
            engine->add(chaiscript::bootstrap::standard_library::vector_type<std::vector<std::string>>("VectorString"));
            engine->add(chaiscript::type_conversion<const char *, std::string>());
            engine->add(chaiscript::type_conversion<std::vector<chaiscript::Boxed_Value>,
                    std::vector<std::string>>(
                        [](const std::vector<chaiscript::Boxed_Value> &vs) {
                            std::vector<std::string> vstr;

                            std::transform(vs.begin(),
                                    vs.end(),
                                    std::back_inserter(vstr),
                                    [](const chaiscript::Boxed_Value &bv) {
                                        return bv.get().cast<std::string>();
                                    });

                            return vstr;
                        }
                    )
                    );
        }

    } // ::imc::logger::log
} // imc

