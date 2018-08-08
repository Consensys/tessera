
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


public class CmdLineExecutor {

    private CmdLineExecutor() {
        throw new UnsupportedOperationException("");
    }


        protected static int execute(String... args) throws Exception {

        Options options = new Options();

        options.addOption(
                Option.builder()
                        .longOpt("storetype")
                        .desc("Store type i.e. bdb, dir, sqlite")
                        .hasArg(true)
                        .optionalArg(false)
                        .numberOfArgs(1)
                        .argName("TYPE").valueSeparator('=')
                        .required()
                        .build());

        options.addOption(
                Option.builder()
                        .longOpt("inputpath")
                        .desc("Path to input file or directory")
                        .hasArg(true)
                        .optionalArg(false)
                        .numberOfArgs(1)
                        .argName("PATH")
                        .required()
                        .build());

        options.addOption(
                Option.builder()
                        .longOpt("exporttype")
                        .desc("Export DB type i.e. h2, sqlite")
                        .hasArg(true)
                        .optionalArg(false)
                        .numberOfArgs(1)
                        .argName("TYPE")
                        .required()
                        .build());

        options.addOption(
                Option.builder()
                        .longOpt("outputfile")
                        .desc("Path to output file")
                        .hasArg(true)
                        .optionalArg(false)
                        .numberOfArgs(1)
                        .argName("PATH")
                        .required()
                        .build());

        options.addOption(
                Option.builder()
                    .longOpt("dbuser")
                    .desc("Database username to use")
                    .hasArg(true)
                    .optionalArg(true)
                    .numberOfArgs(1)
                    .argName("PATH")
                    .required()
                    .build());

        options.addOption(
                Option.builder()
                    .longOpt("dbpass")
                    .desc("Database password to use")
                    .hasArg(true)
                    .optionalArg(true)
                    .numberOfArgs(1)
                    .argName("PATH")
                    .required()
                    .build());

        if (Arrays.asList(args).contains("help")) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("tessera-data-migration", options);
            return 0;
        }

        final CommandLineParser parser = new DefaultParser();
        final CommandLine line = parser.parse(options, args);

        final StoreType storeType = StoreType.valueOf(line.getOptionValue("storetype").toUpperCase());
        final StoreLoader storeLoader = StoreLoader.create(storeType);

        final Path inputpath = Paths.get(line.getOptionValue("inputpath"));
        final Map<byte[], byte[]> data = storeLoader.load(inputpath);

        final String username = line.getOptionValue("dbuser");
        final String password = line.getOptionValue("dbpass");

        final String exportTypeStr = line.getOptionValue("exporttype");
        final ExportType exportType = ExportType.valueOf(exportTypeStr.toUpperCase());

        final Path outputFile = Paths.get(line.getOptionValue("outputfile")).toAbsolutePath();
        final DataExporter dataExporter = DataExporterFactory.create(exportType);
        dataExporter.export(data, outputFile, username, password);

        System.out.printf("Exported data to %s", Objects.toString(outputFile));
        System.out.println();

        return 0;
    }
}
