package com.github.tessera.data.migration;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

public class Main {

    private Main() {
        throw new UnsupportedOperationException("");
    }

    public static void main(String... args) throws Exception {

        Options options = new Options();
        options.addOption(
                Option.builder("inputfile")
                        .desc("Path to input file")
                        .hasArg(true)
                        .optionalArg(false)
                        .numberOfArgs(1)
                        .argName("PATH")
                        .required()
                        .build());

        options.addOption(
                Option.builder("outputfile")
                        .desc("Path to output file")
                        .hasArg(true)
                        .optionalArg(false)
                        .numberOfArgs(1)
                        .argName("PATH")
                        .build());

        CommandLineParser parser = new DefaultParser();

        CommandLine line = parser.parse(options, args);

        Path inputFile = Paths.get(line.getOptionValue("inputfile"));
        final OutputStream output;
        if (line.hasOption("outputfile")) {
            Path outputFile = Paths.get(line.getOptionValue("outputfile"));
            output = Files.newOutputStream(outputFile);
        } else {
            output = System.out;
        }

        BdbDumpFile bdbDumpFile = new BdbDumpFile(inputFile);

        bdbDumpFile.execute(output);

    }

}
