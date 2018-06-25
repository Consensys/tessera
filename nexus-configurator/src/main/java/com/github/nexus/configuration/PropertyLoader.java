package com.github.nexus.configuration;

import org.apache.commons.cli.Options;

import java.nio.file.Path;
import java.util.List;
import java.util.Properties;
import java.util.ServiceLoader;

public interface PropertyLoader {

    Options options = new Options() {
        {
            addOption("publicKeys", "publicKeys", true, "public keys");
            addOption("privateKeys", "privateKeys", true, "private keys");
            addOption("passwords", "passwords", true, "passwords for encrypted private keys");
            addOption("configfile", "configfile", true, "config file location");
            addOption("url", "url", true, "base url to use");
            addOption("port", "port", true, "port to listen for http requests on");
            addOption("othernodes", "othernodes", true, "initial set of other nodes");
            addOption("keygenBasePath", "keygenBasePath", true, "base path that generated keys should be placed");
            addOption("generatekeys", "generatekeys", true, "key names to generate a public/private keypair for");
            addOption("whitelist", "whitelist", true, "a whitelist of allowed hosts that can connect to this node");
            addOption("workdir", "workdir", true, "directory where work files are placed (must match value specified to Quorum)");
            addOption("socket", "socket", true, "IPC socket to create for access to the Private API");
            addOption("databaseURL", "databaseURL", true, "database url specify db driver");
            addOption("tls", "tls", true, "enable or disable ssl");
            addOption("serverKeyStore", "serverKeyStore", true, "server keystore file");
            addOption("serverKeyStorePassword", "serverKeyStorePassword", true, "server keystore password");
            addOption("serverTrustStore", "serverTrustStore", true, "server truststore file");
            addOption("serverTrustStorePassword", "serverTrustStorePassword", true, "server truststore password");
            addOption("serverTrustMode", "serverTrustMode", true, "server trust mode");
            addOption("knownClients", "knownClients", true, "server known clients");
            addOption("clientKeyStore", "clientKeyStore", true, "client keystore file");
            addOption("clientKeyStorePassword", "clientKeyStorePassword", true, "client keystore password");
            addOption("clientTrustStore", "clientTrustStore", true, "client truststore file");
            addOption("clientTrustStorePassword", "clientTrustStorePassword", true, "client truststore password");
            addOption("clientTrustMode", "clientTrustMode", true, "client trust mode");
            addOption("knownServers", "knownServers", true, "known servers list");
        }
    };

    /**
     * Attempts to extract the "configfile" property from the arguments and
     * return a List containing the path to that file
     *
     * @param args the arguments to search
     * @return a list potentially containing a path to a configuration file
     */
    List<Path> getConfigFilePath(String... args);

    /**
     * Turns CLI arguments into properties that can be used in the application.
     * The set of accepted properties is the same as ones are can be passed in
     * via a configuration file
     *
     * @param args the commandline arguments to parse
     * @return populated properties parsed from cli
     */
    Properties getCliProperties(String... args);

    /**
     * Returns a {@link Properties} that contains all the properties that are
     * known to the system by merging various sources and filtering out any
     * unknown properties
     *
     * @param cliParameters CLI parameters that should be parsed for extra properties
     * @return all known properties that were provided to the application
     */
    Properties getAllProperties(String... cliParameters);

    static PropertyLoader create() {
        final ServiceLoader<PropertyLoader> configFactory = ServiceLoader.load(PropertyLoader.class);
        return configFactory.iterator().next();
    }

}
