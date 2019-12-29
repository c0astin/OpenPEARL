/*
 * changes: nov-2019 (rm)
 *   command line parameters reworked (-o is now really optional)
 *   platform object now accessible by getInstance() (required for direct associations)
 *   no output file creation if errors were detected
 */

#include <algorithm>
#include <iostream>
#include <ostream>
#include <sstream>
#include <string>
#include <vector>

#include <pugixml.hpp>
#include "docopt/docopt.h"

#include "imc.hpp"

#include "logger.hpp"
#include "reader/reader.hpp"
#include "reader/module.hpp"
#include "reader/platform.hpp"
#include "checker/checker.hpp"
#include "expr/computer.hpp"
#include "codegen/codegen.hpp"
#include "types/platform.hpp"

using path = ::std::experimental::filesystem::path;

imc::logger::loglevel GLOBAL_LOG_LEVEL = imc::logger::loglevel::WARN;

static const char USAGE[] =
R"(imc.

    Usage:
      imc [-d] [-v] -S SEARCHPATH [-o OUTPUT] -b PLATFORM INPUT ...
      imc (-h | --help)
      imc --version

    Options:
      -h --help     Show this screen.
      --version     Show version.
      -v            Be verbose.
      -d            Print debugging information while running.
      -b PLATFORM   Set platform specification file
      -o OUTPUT     Set output file (defaults to './system.cc')
      -S SEARCHPATH Searchpath for scripts to load into the platform.xml function execution scope
)";

std::string version(void) {
    std::ostringstream oss;

    oss << "IMC "
        << VERSION_MAJOR
        << "."
        << VERSION_MINOR;

    return oss.str();
}

int main(int argc, const char** argv) {
    auto args = docopt::docopt(USAGE, { argv + 1, argv + argc }, true, version());

    bool verbose = ((bool) args["-v"]) && args["-v"].asBool();
    bool debug   = ((bool) args["-d"]) && args["-d"].asBool();

    {
        if (verbose) {
            GLOBAL_LOG_LEVEL = imc::logger::loglevel::INFO;
        }

        if (debug) {
            GLOBAL_LOG_LEVEL = imc::logger::loglevel::DEBUG;
        }
    }


    if (!args["-b"]) {
    	  	std::cerr<< "-b missing" << std::endl;
    	  	return -1;
    }
    if (!args["-S"]) {
    	  	std::cerr<< "-o missing" << std::endl;
    	  	return -1;
    }

    // docopt verifies that we have these:

    std::string output_name;
    if (!args["-o"]) {
    	  	output_name = "./system.cc";
    } else {
            output_name = args["-o"].asString();
    }

    std::string platform_name      = args["-b"].asString();
    std::string searchpath         = args["-S"].asString();
    std::vector<std::string> input = args["INPUT"].asStringList();

    {
        auto log = ::imc::logger::log::info();
        log << std::endl
            << "  verbose       = " << verbose << std::endl
            << "  debug         = " << debug << std::endl
            << "  platform_name = " << platform_name << std::endl
            << "  searchpath    = " << searchpath    << std::endl
            << "  output_name   = " << output_name << std::endl;

        for (auto& elem : input) {
            log << "  input         = " << elem << std::endl;
        }
    }

    imc::reader::PlatformXMLReader platform_reader(
            std::move(path(platform_name)),
            std::move(path(searchpath)));

    auto platform = platform_reader();
    if (!platform) {
        ::imc::logger::log::error() << "Error while constructing platform object ("<<platform_name <<")" << std::endl;
        return 1;
    }
    ::imc::types::platform::Platform::setInstance(platform.value());

    std::vector<imc::reader::ModuleXMLReader>   module_readers;
    std::vector<imc::types::module::Module>     modules;

    std::transform(input.begin(), input.end(),
            std::back_inserter(module_readers),
            imc::reader::module_reader_from_str);

    for (auto& reader : module_readers) {
        auto mod = reader();
        if (!mod) {
            ::imc::logger::log::error() << "Error while reading module" << std::endl;
            return 1;
        }
        modules.push_back(std::move(*mod));
    }

    { // some debugging output
        ::imc::logger::log::debug() << "Modules: [" << std::endl;
        for (const auto& module: modules) {
            ::imc::logger::log::debug() << "Module: " << module << std::endl;
        }
        ::imc::logger::log::debug() << "]" << std::endl;
    }

    //
    // TODO: Expression computing
    //
    {
        auto result = imc::expr::compute(*platform);
        if (!result) {
            ::imc::logger::log::error() << "Error while computing expressions" << std::endl;
            return 1;
        }
    }

    { // some debugging output
        ::imc::logger::log::debug() << "Modules: [" << std::endl;
        for (const auto& module: modules) {
            ::imc::logger::log::debug() << "Module: " << module << std::endl;
        }
        ::imc::logger::log::debug() << "]" << std::endl;
    }

    auto result = imc::checker::inter_module_check(*platform, modules);
    if (!result) {
        ::imc::logger::log::error() << "Error ..." << std::endl;
        return 1;
    }

    { // some debugging output
        ::imc::logger::log::debug() << "Modules: [" << std::endl;
        for (const auto& module: modules) {
            ::imc::logger::log::debug() << "Module: " << module << std::endl;
        }
        ::imc::logger::log::debug() << "]" << std::endl;
    }

    if (::imc::logger::log::get_error_count() > 0) {
        std::cerr << "imc aborted with " << ::imc::logger::log::get_error_count() << " errors" << std::endl;
        return(-1);
    }

    imc::codegen::Codegen generator(std::move(modules), std::move(*platform), output_name);
    if (!generator.open()) {
        ::imc::logger::log::error() << "Error opening output file." << std::endl;
        return 1;
    }
    generator.write_out();

    ::imc::logger::log::info() << "Ready" << std::endl;
    return 0;
}
