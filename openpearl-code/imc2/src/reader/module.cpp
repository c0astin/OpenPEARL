/*
 * changes:
 * pass source file to system part and problem part readers instead of xml file path
 */

#include <iostream>
#include <vector>
#include <string>
#include <stdexcept>
#include <experimental/filesystem>
#include <experimental/optional>

#include <pugixml.hpp>

#include "logger.hpp"
#include "reader/module.hpp"
#include "reader/reader.hpp"
#include "types/problem.hpp"
#include "types/system.hpp"
#include "util.hpp"

template<typename T>
using optional                = ::std::experimental::optional<T>;
using Problem                 = imc::types::problem::Problem;
using System                  = imc::types::system::System;

namespace imc {
    namespace reader {
        template <typename T>
        using optional  = ::std::experimental::optional<T>;
        using path      = ::std::experimental::filesystem::path;

        optional<Module>
            ModuleXMLReader::operator()(void)
        {
            pugi::xml_document document;
            pugi::xml_parse_result res = document.load_file(std::string(this->filepath).c_str());

            ::imc::logger::log::debug() << "Document loaded" << std::endl;

            if (!res) {
                ::imc::logger::log::error() << filepath << ": internal compiler error: error parsing  " << res.description() << std::endl;
                return {};
            }

            optional<System> system;
            optional<Problem> problem;

            auto module_child = document.child("module");
            if (!module_child) {
                ::imc::logger::log::error() << filepath <<
                        ": internal compiler error: error while parsing 'module' in " << this->filepath << std::endl;
                return {};
            }
            std::string mod_name = module_child.attribute("name").value();
            std::string file_name = module_child.attribute("file").value();;
            ::imc::logger::log::debug() << "Loaded 'module' node " << std::endl;


            for (auto child : module_child.children()) {
                std::string child_name = child.name();
                if (child_name == "module") {
                    ::imc::logger::log::warn() << "TODO: Aggregating module metadata" << std::endl;
                } else if (child_name == "system") {
                    ::imc::logger::log::debug() << "Having 'system' node" << std::endl;
                    system = imc::reader::module::fetch_system(child, file_name); //this->filepath);
                    if (!system) {
                        ::imc::logger::log::error() << filepath <<
                                ": internal compiler error: error while parsing 'module.system'." << std::endl;
                        return {};
                    }


                } else if (child_name == "problem") {
                    ::imc::logger::log::debug() << "Having 'problem' node" << std::endl;
                    problem = imc::reader::module::fetch_problem(child, file_name); //this->filepath);
                    if (!problem) {
                        ::imc::logger::log::error() << filepath <<
                                ": internal compiler error: error while parsing 'module.problem'." << std::endl;
                        return {};
                    }

                    //problem_parts.push_back(std::move(*problem));
                } else {
                    ::imc::logger::log::error() << filepath << ": internal compiler error: unknown node: '" << child_name << "'" << std::endl;
                    return {};
                }
            }

            return Module(system, problem, mod_name, file_name);
        }

        ModuleXMLReader
            module_reader_from_str(std::string& pth)
            noexcept
        {
            return ModuleXMLReader(path(std::string(pth)));
        }
    }
}

