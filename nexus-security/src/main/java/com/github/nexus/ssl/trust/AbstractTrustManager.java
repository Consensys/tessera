package com.github.nexus.ssl.trust;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.X509TrustManager;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractTrustManager implements X509TrustManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractTrustManager.class);

    private File knownHostsFile;
    private List<String> certificates;

    public AbstractTrustManager(final File knownHostsFile) throws IOException {
        this.knownHostsFile = knownHostsFile;
        certificates = new ArrayList<>();
        getWhiteListedCertificateForServerAddress();
    }

    public AbstractTrustManager(){
    }

    private void getWhiteListedCertificateForServerAddress() throws IOException {

        if (knownHostsFile.exists()){
            try (BufferedReader reader = new BufferedReader(new FileReader(knownHostsFile))){
                String line;
                while ((line = reader.readLine()) != null) {
                    this.certificates.add(line);
                }
            }
        }
    }

    protected void generateWhiteListedFileIfNotExisted() throws IOException {

        if (!knownHostsFile.exists()) {

            File parentDirectory = knownHostsFile.getParentFile();

            if (parentDirectory != null && !parentDirectory.exists())
                if (!parentDirectory.mkdirs()) {
                    throw new IOException("Fail to create directory");
                }

            if (!knownHostsFile.createNewFile()) {
                throw new IOException("Fail to create file");
            }
        }
    }

    protected void addServerToKnownHostsList(String thumbPrint) throws IOException {
        LOGGER.info("Add entry to known host file");

        generateWhiteListedFileIfNotExisted();

        this.certificates.add(thumbPrint);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(knownHostsFile, true)))
        {
            writer.write(thumbPrint);
            writer.newLine();
        }
    }

    protected boolean certificateExistsInKnownHosts(String thumbPrint) {
        return this.certificates.stream().anyMatch(cert -> thumbPrint.equals(cert));
    }

}
