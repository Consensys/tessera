package com.quorum.tessera.config.cli.parsers;

import org.apache.commons.cli.CommandLine;

/**
 * A parser that checks for CLI options and takes actions based upon them
 * <p>
 * The actions may have side-effects, and may choose to return a value
 *
 * @param <T> The return type from parsing the CLI options
 */
public interface Parser<T> {

    /**
     * Parses the CLI arguments and performs actions based upon whether the
     * arguments relevant to it are present or not
     *
     * @param commandLine the command line object that has parsed the configuration
     * @return the output of the parser, if any
     * @throws Exception if there is a problem with the supplied configuration,
     *                   any exception could be thrown
     */
    T parse(CommandLine commandLine) throws Exception;

}
