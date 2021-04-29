package net.consensys.tessera.migration.config;

import com.moandjiezana.toml.Toml;
import com.quorum.tessera.config.AppType;
import com.quorum.tessera.config.ClientMode;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.EncryptorConfig;
import com.quorum.tessera.config.EncryptorType;
import com.quorum.tessera.config.JdbcConfig;
import com.quorum.tessera.config.KeyConfiguration;
import com.quorum.tessera.config.Peer;
import com.quorum.tessera.config.ServerConfig;
import net.consensys.tessera.migration.data.TesseraJdbcOptions;

import java.io.IOException;
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


    private TesseraJdbcOptions tesseraJdbcOptions;

    public MigrateConfigCommand(
        Path orionConfigFile,
        Path outputFile,
        TesseraJdbcOptions tesseraJdbcOptions) {
        this.orionConfigFile = orionConfigFile;
        this.outputFile = outputFile;
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

        return config;
    }

    private Config createConfig() {
        Toml toml = new Toml().read(orionConfigFile.toAbsolutePath().toFile());

        String knownnodesstorage = toml.getString("knownnodesstorage");

        final Path currentDir = Paths.get("").toAbsolutePath();
        final Path workdir = currentDir.resolve(toml.getString("workdir", "."));

        long p2pPort = toml.getLong("nodeport",8080L); // p2p
        String p2pUrl = toml.getString("nodeurl", "http://127.0.0.1:8080"); // p2p
        URI p2pUri = URI.create(p2pUrl);
        if(p2pUri.getPort() != p2pPort) {
            p2pUri = URI.create(String.format("%s://%s:%s",p2pUri.getScheme(),p2pUri.getHost(),p2pPort));
        }

        final String p2pBindingAddress = toml.getString("nodenetworkinterface", "127.0.0.1");
        final URI p2pBindingUri = URI.create(String.format("%s://%s:%s",p2pUri.getScheme(),p2pBindingAddress,p2pUri.getPort()));

        String q2tUrl = toml.getString("clienturl", "http://127.0.0.1:8888");
        long q2tPort = toml.getLong("clientport",8888L);
        URI qt2Uri = URI.create(q2tUrl);
        if(q2tPort != qt2Uri.getPort()) {
            qt2Uri = URI.create(String.format("%s://%s:%s",qt2Uri.getScheme(),qt2Uri.getHost(),q2tPort));
        }

        final String qt2BindingAddress = toml.getString("clientnetworkinterface", "127.0.0.1");

        final URI qt2BindingUri = URI.create(String.format("%s://%s:%s", qt2Uri.getScheme(), qt2BindingAddress, qt2Uri.getPort()));


        String authMode = toml.getString("tls"); // STRICT or OFF

        String tlsServerTrust = toml.getString("tlsservertrust", "tofu"); //Server trust mode
        String tlsServerKey = toml.getString("tlsserverkey", "tls-server-key.pem");
        String tlsServerCert = toml.getString("tlsservercert", "tls-server-cert.pem");
        List<String> tlsServerChain = toml.getList("tlsserverchain", List.of());
        String tlsKnownClients = toml.getString("tlsknownclients", "tls-known-clients");

        String tlsClientTrust = toml.getString("tlsclienttrust", "ca-or-tofu"); // Client trust mode
        String tlsClientKey = toml.getString("tlsclientkey", "tls-client-key.pem");
        String tlsClientCert = toml.getString("tlsclientcert", "tls-client-cert.pem");
        List<String> tlsClientChain = toml.getList("tlsclientchain", List.of());
        String tlsKnownServers = toml.getString("tlsknownservers", "tls-known-servers");


        // This will be migrated into Q2T server
        String clientConnectionTls = toml.getString("clientconnectiontls");
        String clientConnectionTlsServerKey = toml.getString("clientconnectiontlsserverkey");
        String clientConnectionTlsServerCert = toml.getString("clientconnectiontlsservercert");
        List<String> clientConnectionTlsServerChain = toml.getList("clientconnectiontlsserverchain");
        String clientConnectionTlsServerTrust = toml.getString("clientconnectiontlsservertrust"); // Trust mode
        String clientConnectionTlsKnownClients = toml.getString("clientconnectiontlsknownclients");


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
                .withServerAddress(qt2Uri.toString())
                .withBindingAddress(qt2BindingUri.toString())
                .withSslConfig(
                    SslConfigBuilder.create().withSslConfigType("SERVER_ONLY")
                        .withSslAuthenticationMode(clientConnectionTls)
                        .withServerTlsKey(clientConnectionTlsServerKey)
                        .withServerTlsCertificatePath(clientConnectionTlsServerCert)
                        .withServerTlsTrustCertChain(clientConnectionTlsServerChain)
                        .withTlsServerTrustMode(clientConnectionTlsServerTrust)
                        .withKnownClientFilePath(clientConnectionTlsKnownClients)
                        .build()
                )
                .build();

        ServerConfig p2pServer =
            ServerConfigBuilder.create()
                .withAppType(AppType.P2P)
                .withServerAddress(p2pUri.toString())
                .withBindingAddress(p2pBindingUri.toString())
                .withSslConfig(
                    SslConfigBuilder.create()
                        .withSslAuthenticationMode(authMode)
                        .withTlsServerTrustMode(tlsServerTrust)
                        .withServerTlsKey(tlsServerKey)
                        .withServerTlsCertificatePath(tlsServerCert)
                        .withServerTlsTrustCertChain(tlsServerChain)
                        .withKnownClientFilePath(tlsKnownClients)
                        .withTlsClientTrustMode(tlsClientTrust)
                        .withClientTlsKey(tlsClientKey)
                        .withClientTlsCertificatePath(tlsClientCert)
                        .withClientTlsTrustCertChain(tlsClientChain)
                        .withKnownServersFilePath(tlsKnownServers)
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

        KeyConfiguration keyConfiguration = KeyConfigBuilder.create()
            .withWorkDir(workdir)
            .withPrivateKeys(privateKeys)
            .withPublicKeys(publicKeys)
            .withPasswordsFile(passwordsFile)
            .build();

        config.setKeys(keyConfiguration);

        return config;
    }
}
