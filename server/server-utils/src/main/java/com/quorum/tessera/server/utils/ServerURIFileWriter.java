package com.quorum.tessera.server.utils;

import com.quorum.tessera.server.TesseraServer;
import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerURIFileWriter {
  private static final Logger LOGGER = LoggerFactory.getLogger(ServerURIFileWriter.class);

  public static void writeServerURIsToFile(
      final Path outputServerURIPath, final List<TesseraServer> servers) {
    final List<TesseraServer> serverList =
        servers.stream()
            .filter(tesseraServer -> tesseraServer.getAppType() != null)
            .collect(Collectors.toList());

    writeURIFile(outputServerURIPath, serverList);
  }

  private static void writeURIFile(final Path dirPath, final List<TesseraServer> serverList) {
    final String fileName = "tessera.uris";
    final String fileHeader = "This file contains the URIs used by the running servers in Tessera";

    final List<String> uriList = new LinkedList<>();

    serverList.forEach(
        tesseraServer -> {
          uriList.add(
              String.format(
                  "%s=%s", tesseraServer.getAppType().toString(), getURIOrEmpty(tesseraServer)));
        });

    final File file = new File(dirPath.toFile(), fileName);
    file.deleteOnExit();

    try {
      uriList.add(
          0,
          String.format(
              "#%s. This file will be deleted after the enclave is shutdown.", fileHeader));
      Files.write(
          Path.of(dirPath.toAbsolutePath() + "/" + fileName), uriList, Charset.defaultCharset());
    } catch (final Exception e) {
      LOGGER.debug(String.format("Error writing %s file", fileName), e);
    }
  }

  private static String getURIOrEmpty(final TesseraServer tesseraServer) {
    try {
      return tesseraServer.getUri().toString();
    } catch (final NullPointerException e) {
      return "";
    }
  }
}
