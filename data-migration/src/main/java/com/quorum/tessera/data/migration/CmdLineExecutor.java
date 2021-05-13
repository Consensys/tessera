package com.quorum.tessera.data.migration;

import static java.util.Collections.singletonList;

import com.quorum.tessera.cli.CliAdapter;
import com.quorum.tessera.cli.CliResult;
import com.quorum.tessera.cli.CliType;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(
    headerHeading = "Usage:%n%n",
    synopsisHeading = "%n",
    parameterListHeading = "%nParameters:%n",
    optionListHeading = "%nOptions:%n",
    header = "Database migration tool from older systems to Tessera")
public class CmdLineExecutor implements CliAdapter, Callable<CliResult> {

  @Option(names = "help", usageHelp = true, description = "display this help message")
  private boolean isHelpRequested;

  @Option(names = "-storetype", required = true, description = "Store type i.e. bdb, dir, sqlite")
  private StoreType storeType;

  @Option(names = "-inputpath", required = true, description = "Path to input file or directory")
  private Path inputpath;

  @Option(names = "-exporttype", required = true, description = "Export DB type i.e. h2, sqlite")
  private ExportType exportType;

  @Option(
      names = "-dbconfig",
      description = "Properties file with create table, insert row and jdbc url")
  private Path dbconfig;

  @Option(names = "-outputfile", required = true, description = "Path to output file")
  private Path outputFile;

  @Option(names = "-dbuser", description = "Database username to use")
  private String username;

  @Option(names = "-dbpass", description = "Database password to use")
  private String password;

  @Override
  public CliResult call() throws Exception {
    return this.execute();
  }

  @Override
  public CliType getType() {
    return CliType.DATA_MIGRATION;
  }

  @Override
  public CliResult execute(String... args) throws Exception {
    final StoreLoader storeLoader = storeType.getLoader();

    storeLoader.load(inputpath);

    final DataExporter dataExporter;
    if (exportType == ExportType.JDBC) {
      if (dbconfig == null) {
        throw new IllegalArgumentException(
            "dbconfig file path is required when no export type is defined.");
      }

      final Properties properties = new Properties();
      try (InputStream inStream = Files.newInputStream(dbconfig)) {
        properties.load(inStream);
      }

      final String insertRow =
          Objects.requireNonNull(
              properties.getProperty("insertRow"), "No insertRow value defined in config file. ");
      final String createTable =
          Objects.requireNonNull(
              properties.getProperty("createTable"),
              "No createTable value defined in config file. ");
      final String jdbcUrl =
          Objects.requireNonNull(
              properties.getProperty("jdbcUrl"), "No jdbcUrl value defined in config file. ");

      dataExporter = new JdbcDataExporter(jdbcUrl, insertRow, singletonList(createTable));

    } else {
      dataExporter = DataExporterFactory.create(exportType);
    }

    dataExporter.export(storeLoader, outputFile, username, password);

    System.out.printf("Exported data to %s", Objects.toString(outputFile));
    System.out.println();

    return new CliResult(0, true, null);
  }
}
