package com.github.nexus.ssl.trust;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.X509TrustManager;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class AbstractTrustManager implements X509TrustManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractTrustManager.class);

    private Path knownHostsFile;
    private List<String> certificates;

    public AbstractTrustManager(final Path knownHostsFile) throws IOException {
        this.knownHostsFile = knownHostsFile;
        certificates = new ArrayList<>();
        getWhiteListedCertificateForServerAddress();
    }

    public AbstractTrustManager(){
    }

    private void getWhiteListedCertificateForServerAddress() throws IOException {

        if (Files.exists(knownHostsFile)){
            try (BufferedReader reader = Files.newBufferedReader(knownHostsFile)) {
                String line;
                while ((line = reader.readLine()) != null) {
                    this.certificates.add(line);
                }
            }
        }
    }

    protected void generateWhiteListedFileIfNotExisted() throws IOException {

        if (Files.notExists(knownHostsFile)) {

            final Path parentDirectory = knownHostsFile.getParent();

            if (Objects.nonNull(parentDirectory) && Files.notExists(parentDirectory))
                Files.createDirectory(parentDirectory);

            Files.createFile(knownHostsFile);
        }
    }

    protected void addServerToKnownHostsList(String thumbPrint) throws IOException {
        LOGGER.info("Add entry to known host file");

        generateWhiteListedFileIfNotExisted();

        this.certificates.add(thumbPrint);

        try (BufferedWriter writer = Files.newBufferedWriter(knownHostsFile, StandardOpenOption.APPEND))
        {
            writer.write(thumbPrint);
            writer.newLine();
        }
    }

    protected boolean certificateExistsInKnownHosts(String thumbPrint) {
        return this.certificates.stream().anyMatch(cert -> thumbPrint.equals(cert));
    }

}
