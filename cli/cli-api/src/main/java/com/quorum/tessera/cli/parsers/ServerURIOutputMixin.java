package com.quorum.tessera.cli.parsers;

import com.quorum.tessera.config.AppType;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.server.TesseraServer;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

public class ServerURIOutputMixin {

  private static final Logger LOGGER = LoggerFactory.getLogger(ServerURIOutputMixin.class);

  @CommandLine.Option(
      names = {"--outputServerURIs"},
      description = "Output the server URI(s) to a specified path")
  private Path outputServerURIPath = null;

  public void updateConfig(final Config config) {
    if (outputServerURIPath != null) {
      config.setOutputServerURIPath(outputServerURIPath);
    }
  }

  public static void writeServerURIsToFile(final Config config, final List<TesseraServer> servers) {
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

      final Path path = config.getOutputServerURIPath();

      final List<String> uriPaths = new LinkedList<>();
      uriPaths.add(writeURIFile(path, q2tServer, "q2tServer.uri"));
      uriPaths.add(writeURIFile(path, p2pServer, "p2pServer.uri"));
      uriPaths.add(writeURIFile(path, thirdPartyServer, "thirdPartyServer.uri"));

      // Add a shutdown hook to clean them up
      Runtime.getRuntime()
          .addShutdownHook(
              new Thread(
                  () -> {
                    try {
                      for (final String uriFilePath : uriPaths) {
                        Files.delete(Path.of(uriFilePath));
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

  private static String writeURIFile(
      final Path dirPath, final TesseraServer tesseraServer, final String outputFile)
      throws IOException {
    final Path path =
        Files.writeString(
            Path.of(dirPath.toAbsolutePath() + "/" + outputFile),
            tesseraServer.getUri().toString());
    return path.toString();
  }
}
