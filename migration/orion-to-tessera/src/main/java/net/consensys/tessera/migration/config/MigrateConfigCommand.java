package net.consensys.tessera.migration.config;

import com.moandjiezana.toml.Toml;
import com.quorum.tessera.config.AppType;
import com.quorum.tessera.config.ClientMode;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.EncryptorConfig;
import com.quorum.tessera.config.EncryptorType;
import com.quorum.tessera.config.JdbcConfig;
import com.quorum.tessera.config.Peer;
import com.quorum.tessera.config.ServerConfig;
import com.quorum.tessera.config.util.JaxbUtil;
import net.consensys.tessera.migration.data.TesseraJdbcOptions;

import javax.validation.ConstraintViolationException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
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

    private TesseraJdbcOptions tesseraJdbcOptions;

    public MigrateConfigCommand(
            Path orionConfigFile,
            Path outputFile,
            boolean skipValidation,
            TesseraJdbcOptions tesseraJdbcOptions) {
        this.orionConfigFile = orionConfigFile;
        this.outputFile = outputFile;
        this.skipValidation = skipValidation;
        this.tesseraJdbcOptions = tesseraJdbcOptions;
    }

    @Override
    public Config call() throws IOException {
        Config config = createConfig();
        config.getJdbcConfig().setUsername(tesseraJdbcOptions.getUsername());
        config.getJdbcConfig().setPassword(tesseraJdbcOptions.getPassword());
        config.getJdbcConfig().setUrl(tesseraJdbcOptions.getUrl());
        config.getJdbcConfig().setAutoCreateTables(true);
        config.setClientMode(ClientMode.ORION);

        try (OutputStream outputStream = Files.newOutputStream(outputFile)) {
            if (skipValidation) {
                JaxbUtil.marshalWithNoValidation(config, outputStream);
            } else {
                try {
                    JaxbUtil.marshal(config, outputStream);
                } catch(ConstraintViolationException ex) {
                    ex.printStackTrace();
                    JaxbUtil.marshalWithNoValidation(config, System.out);
                }
            }
        }

        return config;
    }

    private Config createConfig() {
        Toml toml = new Toml().read(orionConfigFile.toAbsolutePath().toFile());

        String knownnodesstorage = toml.getString("knownnodesstorage");

        final Path currentDir = Paths.get("").toAbsolutePath();
        final Path workdir = currentDir.resolve(toml.getString("workdir","."));

        Long p2pPort = toml.getLong("nodeport"); // p2p
        String p2pUrl = toml.getString("nodeurl","http://127.0.0.1"); // p2p
        URI p2pUri = URI.create(String.format("%s:%s",p2pUrl,String.valueOf(p2pPort)));

        List<String> tlsserverchain = toml.getList("tlsserverchain", List.of());
        String p2pBindingAddress = String.format("%s://%s:%s",
            p2pUri.getScheme(),toml.getString("nodenetworkinterface","0.0.0.0"),
            String.valueOf(p2pPort));

        String tlsservercert = toml.getString("tlsservercert");

        String q2tUrl = toml.getString("clienturl","http://127.0.0.1");
        Integer q2tPort = toml.contains("clientport") ? Math.toIntExact(toml.getLong("clientport")) : null;


        URI qt2Uri = URI.create(String.format("%s:%s",q2tUrl,String.valueOf(q2tPort)));
        String qt2BindingAddress = String.format("%s://%s:%s",qt2Uri.getScheme(),
            toml.getString("clientnetworkinterface","0.0.0.0"),String.valueOf(q2tPort));

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
        String socketfile = toml.getString("socket");

        String tlsserverkey = toml.getString("tlsserverkey");
        String tlsservertrust = toml.getString("tlsservertrust");

        final List<String> privateKeys = toml.getList("privatekeys", List.of());
        final String passwordsFile = toml.getString("passwords");
        final List<String> publicKeys = toml.getList("publickeys", List.of());

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

        config.setJdbcConfig(new JdbcConfig());

        ServerConfig q2tServer =
                ServerConfigBuilder.create()
                        .withAppType(AppType.Q2T)
                        .withSocketFile(socketfile)
                        .withServerAddress(qt2Uri.toString())
                        .withBindingAddress(qt2BindingAddress)
                    .withServerPort(q2tPort)

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
                        .withServerAddress(p2pUri.toString())
                        .withBindingAddress(p2pBindingAddress)
                        .withServerPort(Math.toIntExact(p2pPort))
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
                        .withWorkDir(workdir)
                        .withPrivateKeys(privateKeys)
                        .withPublicKeys(publicKeys)
                        .withPasswordsFile(passwordsFile)
                        .build());

        return config;
    }
}
