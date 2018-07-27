package com.quorum.tessera.data.migration;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
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
                Option.builder("exporttype")
                        .desc("Export DB type i.e. h2, sqlite")
                        .hasArg(true)
                        .optionalArg(false)
                        .numberOfArgs(1)
                        .argName("TYPE")
                        .required()
                        .build());

        options.addOption(
                Option.builder("outputfile")
                        .desc("Path to output file")
                        .hasArg(true)
                        .optionalArg(false)
                        .numberOfArgs(1)
                        .argName("PATH")
                        .required()
                        .build());

        if (Arrays.asList(args).contains("help")) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("tessera-data-migration",options);
            return;
        }

        final CommandLineParser parser = new DefaultParser();

        final CommandLine line = parser.parse(options, args);

        final StoreType storeType = StoreType.valueOf(line.getOptionValue("storetype").toUpperCase());

        StoreLoader storeLoader = StoreLoader.create(storeType);

        Path inputpath = Paths.get(line.getOptionValue("inputpath"));

        Map<byte[], byte[]> data = storeLoader.load(inputpath);
        
        String exportTypeStr = line.getOptionValue("exporttype");
        
        ExportType exportType = ExportType.valueOf(exportTypeStr.toUpperCase());
        Path outputFile = Paths.get(line.getOptionValue("outputfile")).toAbsolutePath();
        DataExporter dataExporter = DataExporterFactory.create(exportType);
        dataExporter.export(data,outputFile);

        System.out.printf("Exported data to %s",Objects.toString(outputFile));
      
    }

}
