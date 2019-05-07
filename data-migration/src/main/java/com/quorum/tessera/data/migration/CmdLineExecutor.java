package com.quorum.tessera.data.migration;

import org.apache.commons.cli.*;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;

import static java.util.Collections.singletonList;

public class CmdLineExecutor {

    public int execute(String... args) throws Exception {

        final Options options = this.createOptions();

        if (Arrays.asList(args).contains("help")) {
            new HelpFormatter().printHelp("tessera-data-migration", options);
            return 0;
        }

        final CommandLine line = new DefaultParser().parse(options, args);

        final StoreLoader storeLoader = StoreType.valueOf(line.getOptionValue("storetype").toUpperCase()).getLoader();

        final Path inputpath = Paths.get(line.getOptionValue("inputpath"));
        storeLoader.load(inputpath);

        final String username = line.getOptionValue("dbuser");
        final String password = line.getOptionValue("dbpass");

        final ExportType exportType = Optional
            .ofNullable(line.getOptionValue("exporttype"))
            .map(String::toUpperCase)
            .map(ExportType::valueOf)
            .get();

        final Path outputFile = Paths.get(line.getOptionValue("outputfile")).toAbsolutePath();

        final DataExporter dataExporter;
        if (exportType == ExportType.JDBC) {
            if (!line.hasOption("dbconfig")) {
                throw new MissingOptionException("dbconfig file path is required when no export type is defined.");
            }

            final String dbconfig = line.getOptionValue("dbconfig");

            final Properties properties = new Properties();
            try (InputStream inStream = Files.newInputStream(Paths.get(dbconfig))) {
                properties.load(inStream);
            }

            final String insertRow = Objects.requireNonNull(properties.getProperty("insertRow"), "No insertRow value defined in config file. ");
            final String createTable = Objects.requireNonNull(properties.getProperty("createTable"), "No createTable value defined in config file. ");
            final String jdbcUrl = Objects.requireNonNull(properties.getProperty("jdbcUrl"), "No jdbcUrl value defined in config file. ");

            dataExporter = new JdbcDataExporter(jdbcUrl, insertRow, singletonList(createTable));

        } else {
            dataExporter = DataExporterFactory.create(exportType);
        }

        dataExporter.export(storeLoader, outputFile, username, password);

        System.out.printf("Exported data to %s", Objects.toString(outputFile));
        System.out.println();

        return 0;
    }

    private Options createOptions() {
        final Options options = new Options();

        options.addOption(
            Option.builder()
                .longOpt("storetype")
                .desc("Store type i.e. bdb, dir, sqlite")
                .hasArg(true)
                .optionalArg(false)
                .numberOfArgs(1)
                .argName("TYPE")
                .valueSeparator('=')
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
                .longOpt("dbconfig")
                .desc("Properties file with create table, insert row and jdbc url")
                .hasArg(true)
                .optionalArg(false)
                .numberOfArgs(1)
                .argName("PATH")
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
                .argName("USER")
                .required()
                .build());

        options.addOption(
            Option.builder()
                .longOpt("dbpass")
                .desc("Database password to use")
                .hasArg(true)
                .optionalArg(true)
                .numberOfArgs(1)
                .argName("PASS")
                .required()
                .build());

        return options;
    }

}
