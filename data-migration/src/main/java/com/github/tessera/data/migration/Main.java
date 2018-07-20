package com.github.tessera.data.migration;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
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
                Option.builder("storetype")
                        .desc("Store type i.e. bdb, dir")
                        .hasArg(true)
                        .optionalArg(false)
                        .numberOfArgs(1)
                        .argName("TYPE")
                        .required()
                        .build());
        
        
        options.addOption(
                Option.builder("inputpath")
                        .desc("Path to input file or directory")
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

        final CommandLineParser parser = new DefaultParser();

        final CommandLine line = parser.parse(options, args);
        
        final StoreType storeType = StoreType.valueOf(line.getOptionValue("storetype").toUpperCase());
        
        StoreLoader storeLoader = StoreLoader.create(storeType);
        
        Path inputpath = Paths.get(line.getOptionValue("inputpath"));

        final OutputStream output;
        if (line.hasOption("outputfile")) {
            Path outputFile = Paths.get(line.getOptionValue("outputfile"));
            output = Files.newOutputStream(outputFile);
        } else {
            output = System.out;
        }
        
        Map<byte[],byte[]> data = storeLoader.load(inputpath);
        
        DataExporter dataExporter = DataExporter.create();
        dataExporter.export(data);

    }

}
