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
                ::imc::logger::log::error() << "Parsing error: " << res.description() << std::endl;
                return {};
            }

            std::vector<System> system_parts;
            std::vector<Problem> problem_parts;

            auto module_child = document.child("module");
            if (!module_child) {
                ::imc::logger::log::error() << "Error while parsing 'module' in " << this->filepath << std::endl;
                return {};
            }
            ::imc::logger::log::debug() << "Loaded 'module' node " << std::endl;

            for (auto child : module_child.children()) {
                std::string child_name = child.name();
                if (child_name == "module") {
                    ::imc::logger::log::warn() << "TODO: Aggregating module metadata" << std::endl;
                } else if (child_name == "system") {
                    ::imc::logger::log::debug() << "Having 'system' node" << std::endl;
                    optional<System> system = imc::reader::module::fetch_system(child, this->filepath);
                    if (!system) {
                        ::imc::logger::log::error() << "Error while parsing 'module.system'." << std::endl;
                        return {};
                    }

                    system_parts.push_back(std::move(*system));
                } else if (child_name == "problem") {
                    ::imc::logger::log::debug() << "Having 'problem' node" << std::endl;
                    optional<Problem> problem = imc::reader::module::fetch_problem(child, this->filepath);
                    if (!problem) {
                        ::imc::logger::log::error() << "Error while parsing 'module.problem'." << std::endl;
                        return {};
                    }

                    problem_parts.push_back(std::move(*problem));
                } else {
                    ::imc::logger::log::error() << "Unknown node: '" << child_name << "'" << std::endl;
                    return {};
                }
            }

            return Module(std::move(system_parts), std::move(problem_parts));
        }

        ModuleXMLReader
            module_reader_from_str(std::string& pth)
            noexcept
        {
            return ModuleXMLReader(path(std::string(pth)));
        }
    }
}

