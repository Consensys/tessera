package net.consensys.tessera.migration.config;

import com.moandjiezana.toml.Toml;
import com.quorum.tessera.config.*;
import com.quorum.tessera.config.util.JaxbUtil;
import net.consensys.tessera.migration.TeeOutputStream;
import net.consensys.tessera.migration.data.TesseraJdbcOptions;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

public class MigrateConfigCommand implements Callable<Config> {

    private Path orionConfigFile;

    private Path outputFile;

    private boolean skipValidation;

    private boolean verbose;

    private TesseraJdbcOptions tesseraJdbcOptions;

    public MigrateConfigCommand(
            Path orionConfigFile,
            Path outputFile,
            boolean skipValidation,
            boolean verbose,
            TesseraJdbcOptions tesseraJdbcOptions) {
        this.orionConfigFile = orionConfigFile;
        this.outputFile = outputFile;
        this.skipValidation = skipValidation;
        this.verbose = verbose;
        this.tesseraJdbcOptions = tesseraJdbcOptions;
    }

    @Override
    public Config call() throws IOException {
        Config config = createConfig();
        config.getJdbcConfig().setUsername(tesseraJdbcOptions.getUsername());
        config.getJdbcConfig().setPassword(tesseraJdbcOptions.getPassword());
        config.getJdbcConfig().setUrl(tesseraJdbcOptions.getUrl());

        try (OutputStream outputStream = new TeeOutputStream(Files.newOutputStream(outputFile), System.out)) {
            if (skipValidation) {
                JaxbUtil.marshalWithNoValidation(config, outputStream);
            } else {
                JaxbUtil.marshal(config, outputStream);
            }
        }

        return config;
    }

    private Config createConfig() {
        Toml toml = new Toml().read(orionConfigFile.toAbsolutePath().toFile());

        String knownnodesstorage = toml.getString("knownnodesstorage");

        Long nodeport = toml.getLong("nodeport"); // p2p
        String nodeurl = toml.getString("nodeurl"); // p2p
        List<String> tlsserverchain = toml.getList("tlsserverchain", List.of());

        String tlsservercert = toml.getString("tlsservercert");

        String clienturl = toml.getString("clienturl");
        Integer clientport = toml.contains("clientport") ? Math.toIntExact(toml.getLong("clientport")) : null;

        String tlsclientkey = toml.getString("tlsclientkey");
        String tlsclienttrust = toml.getString("tlsclienttrust");
        String clientconnectiontls = toml.getString("clientconnectiontls");
        String clientconnectiontlsservercert = toml.getString("clientconnectiontlsservercert");
        List<String> clientconnectiontlsserverchain = toml.getList("clientconnectiontlsserverchain");
        String clientconnectiontlsserverkey = toml.getString("clientconnectiontlsserverkey");
        String clientconnectionTlsServerTrust = toml.getString("clientconnectionTlsServerTrust");
        String tlsknownclients = toml.getString("tlsknownclients");
        String tlsknownservers = toml.getString("tlsknownservers");

        String tlsclientcert = toml.getString("tlsclientcert");

        String serverAuthTls = toml.getString("tls");
        String socketfile = toml.getString("socket", "[No IPC socket file specfied]");

        String tlsserverkey = toml.getString("tlsserverkey");
        String tlsservertrust = toml.getString("tlsservertrust");

        List<String> privateKeys = toml.getList("privatekeys", List.of());
        String passwordsFile = toml.getString("passwords");
        List<String> publicKeys = toml.getList("publickeys", List.of());

        List<String> otherNodes = toml.getList("othernodes", List.of());

        List<String> alwaysSendTo = toml.getList("alwayssendto", List.of());

        Config config = new Config();
        config.setBootstrapNode(false);
        config.setUseWhiteList(false);
        config.setRecoveryMode(false);
        config.setEncryptor(
                new EncryptorConfig() {
                    {
                        setType(EncryptorType.NACL);
                    }
                });
        config.setPeers(otherNodes.stream().map(Peer::new).collect(Collectors.toList()));

        config.setJdbcConfig(JdbcConfigBuilder.create().buildDefault());

        ServerConfig q2tServer =
                ServerConfigBuilder.create()
                        .withAppType(AppType.Q2T)
                        .withSocketFile(socketfile)
                        //     .withServerPort(clientport)
                        //     .withServerAddress(clienturl)
                        //                .withSslConfig(SslConfigBuilder.create()
                        //                        .withClientTrustMode(clientconnectiontls)
                        //                        .withClientKeyStore(tlsclientkey)
                        //                        .build())
                        .build();

        ServerConfig p2pServer =
                ServerConfigBuilder.create()
                        .withAppType(AppType.P2P)
                        .withServerAddress(nodeurl)
                        .withServerPort(Math.toIntExact(nodeport))
                        .withSslConfig(
                                SslConfigBuilder.create()
                                        .withSslAuthenticationMode(serverAuthTls)
                                        .withServerKeyStore(tlsserverkey)
                                        .withTlsServerTrust(tlsservertrust)
                                        .withKnownClientFilePath(tlsknownclients)
                                        .withKnownServersFilePath(tlsknownservers)
                                        .withClientTrustMode(tlsclienttrust)
                                        .withClientKeyStore(tlsclientkey)
                                        .withServerTlsCertificatePath(tlsservercert)
                                        .withClientTlsCertificatePath(tlsclientcert)
                                        .withClientTrustMode(clientconnectionTlsServerTrust)
                                        .build())
                        .build();

        config.setServerConfigs(List.of(q2tServer, p2pServer));

        List<String> encodeKeyValues =
                alwaysSendTo.stream()
                        .map(Paths::get)
                        .map(
                                p -> {
                                    try {
                                        return Files.lines(p)
                                                .findFirst()
                                                .orElse(
                                                        String.format(
                                                                "[Error: No lines found in file %s",
                                                                p.toAbsolutePath()));
                                    } catch (IOException e) {
                                        return String.format("[Error: Unable to read key file %s]", p.toAbsolutePath());
                                    }
                                })
                        .collect(Collectors.toList());

        config.getAlwaysSendTo().addAll(encodeKeyValues);

        config.setKeys(
                KeyConfigBuilder.create()
                        .withPrivateKeys(privateKeys)
                        .withPublicKeys(publicKeys)
                        .withPasswordsFile(passwordsFile)
                        .build());

        return config;
    }
}
