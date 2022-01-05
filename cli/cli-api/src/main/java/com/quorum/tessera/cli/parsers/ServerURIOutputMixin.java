package com.quorum.tessera.cli.parsers;

import com.quorum.tessera.config.AppType;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.server.TesseraServer;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

public class ServerURIOutputMixin {

  private static final Logger LOGGER = LoggerFactory.getLogger(ServerURIOutputMixin.class);

  @CommandLine.Option(
      names = {"--XoutputServerURIs"},
      description = "Output the server URI(s) to a specified path",
      hidden = true)
  private Path outputServerURIPath = null;

  public void updateConfig(final Config config) {
    updateConfig(this.outputServerURIPath, config);
  }

  public void updateConfig(final Path outputServerURIPath, final Config config) {
    if (outputServerURIPath != null) {
      config.setOutputServerURIPath(outputServerURIPath);
    }
  }

  public static void writeServerURIsToFile(
      final Path outputServerURIPath, final List<TesseraServer> servers) {
    try {
      final TesseraServer q2tServer =
          servers.stream()
              .filter(server -> server.getAppType() != null && server.getAppType() == AppType.Q2T)
              .findFirst()
              .orElse(null);

      final TesseraServer p2pServer =
          servers.stream()
              .filter(server -> server.getAppType() != null && server.getAppType() == AppType.P2P)
              .findFirst()
              .orElse(null);

      final TesseraServer thirdPartyServer =
          servers.stream()
              .filter(
                  server ->
                      server.getAppType() != null && server.getAppType() == AppType.THIRD_PARTY)
              .findFirst()
              .orElse(null);

      final List<TesseraServer> serverList =
          Stream.of(q2tServer, p2pServer, thirdPartyServer)
              .filter(Objects::nonNull)
              .collect(Collectors.toList());
      final List<Path> uriPaths = writeURIFile(outputServerURIPath, serverList);

      // Add a shutdown hook to clean them up
      Runtime.getRuntime()
          .addShutdownHook(
              new Thread(
                  () -> {
                    try {
                      for (final Path uriFilePath : uriPaths) {
                        Files.delete(uriFilePath);
                      }
                    } catch (final Exception ex) {
                      LOGGER.error(null, ex);
                    }
                  }));
    } catch (final Exception e) {
      LOGGER.error("Unable to write server URI paths to file", e);
      e.printStackTrace();
    }
  }

  private static List<Path> writeURIFile(final Path dirPath, final List<TesseraServer> serverList)
      throws IOException {
    final List<Path> filePaths = new LinkedList<>();
    for (final TesseraServer tesseraServer : serverList) {
      filePaths.add(
          Files.writeString(
              Path.of(
                  dirPath.toAbsolutePath() + "/" + fileNameFromAppType(tesseraServer.getAppType())),
              tesseraServer.getUri().toString()));
    }
    return filePaths;
  }

  private static String fileNameFromAppType(final AppType appType) {
    switch (appType) {
      case P2P:
        return "p2pServer.uri";
      case Q2T:
        return "q2tServer.uri";
      case THIRD_PARTY:
        return "thirdPartyServer.uri";
      default:
        return null;
    }
  }
}
