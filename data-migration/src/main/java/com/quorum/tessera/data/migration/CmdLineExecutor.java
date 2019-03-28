package com.quorum.tessera.data.migration;

import org.apache.commons.cli.*;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static java.util.Collections.singletonList;

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

        final ExportType exportType = Optional.ofNullable(exportTypeStr)
                .map(String::toUpperCase)
                .map(ExportType::valueOf).get();

        final Path outputFile = Paths.get(line.getOptionValue("outputfile")).toAbsolutePath();

        final DataExporter dataExporter;
        if (exportType == ExportType.JDBC) {
            if (!line.hasOption("dbconfig")) {
                throw new MissingOptionException("dbconfig file path is required when no export type is defined.");
            }

            String dbconfig = line.getOptionValue("dbconfig");

            Properties properties = new Properties();
            try (InputStream inStream = Files.newInputStream(Paths.get(dbconfig))) {
                properties.load(inStream);
            }

            String insertRow = Objects.requireNonNull(properties.getProperty("insertRow",null),
                    "No insertRow value defined in config file. ");

            String createTable = Objects.requireNonNull(properties.getProperty("createTable",null),
                    "No createTable value defined in config file. ");

            String jdbcUrl = Objects.requireNonNull(properties.getProperty("jdbcUrl",null),
                    "No jdbcUrl value defined in config file. ");

            Path sqlFile = Files.createTempFile(UUID.randomUUID().toString(), ".txt");

            Files.write(sqlFile, singletonList(createTable));

            dataExporter = new JdbcDataExporter(jdbcUrl, insertRow, sqlFile.toUri().toURL());

        } else {
            dataExporter = DataExporterFactory.create(exportType);
        }

        dataExporter.export(data, outputFile, username, password);

        System.out.printf("Exported data to %s", Objects.toString(outputFile));
        System.out.println();

        return 0;
    }
}
