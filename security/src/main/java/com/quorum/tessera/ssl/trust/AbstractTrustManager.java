package com.quorum.tessera.ssl.trust;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.net.ssl.X509TrustManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class AbstractTrustManager implements X509TrustManager {

  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractTrustManager.class);

  private Path knownHostsFile;

  private Map<String, String> certificates;

  AbstractTrustManager(final Path knownHostsFile) throws IOException {
    this.knownHostsFile = knownHostsFile;
    certificates = new HashMap<>();
    getWhiteListedCertificateForServerAddress();
  }

  AbstractTrustManager() {}

  private void getWhiteListedCertificateForServerAddress() throws IOException {

    if (knownHostsFile.toFile().exists()) {
      try (BufferedReader reader = Files.newBufferedReader(knownHostsFile)) {
        String line;
        while ((line = reader.readLine()) != null) {
          String[] items = line.split(" ");
          this.certificates.put(items[0], items[1]);
        }
      }
    }
  }

  private void generateWhiteListedFileIfNotExisted() throws IOException {

    if (!knownHostsFile.toFile().exists()) {

      final Path parentDirectory = knownHostsFile.getParent();

      if (Objects.nonNull(parentDirectory) && !parentDirectory.toFile().exists()) {
        Files.createDirectory(parentDirectory);
      }

      Files.createFile(knownHostsFile);
    }
  }

  void addServerToKnownHostsList(String address, String thumbPrint) throws IOException {
    LOGGER.info("Add entry to known host file");

    generateWhiteListedFileIfNotExisted();

    this.certificates.put(address, thumbPrint);

    try (BufferedWriter writer =
        Files.newBufferedWriter(knownHostsFile, StandardOpenOption.APPEND)) {
      writer.write(address + " " + thumbPrint);
      writer.newLine();
    }
  }

  boolean certificateExistsInKnownHosts(String address) {
    return this.certificates.containsKey(address);
  }

  boolean certificateValidForKnownHost(String address, String thumbPrint) {
    return this.certificates.get(address).equals(thumbPrint);
  }
}
