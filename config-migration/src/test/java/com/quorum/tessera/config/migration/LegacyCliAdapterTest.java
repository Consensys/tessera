package com.quorum.tessera.config.migration;

import com.quorum.tessera.cli.CliResult;
import com.quorum.tessera.cli.CliType;
import com.quorum.tessera.config.*;
import com.quorum.tessera.config.builder.ConfigBuilder;
import com.quorum.tessera.config.builder.KeyDataBuilder;
import com.quorum.tessera.config.migration.test.FixtureUtil;
import com.quorum.tessera.io.SystemAdapter;
import com.quorum.tessera.test.util.ElUtil;
import org.apache.commons.cli.CommandLine;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemErrRule;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.*;

public class LegacyCliAdapterTest {

    @Rule public SystemErrRule systemErrRule = new SystemErrRule().enableLog();

    private final ConfigBuilder builderWithValidValues = FixtureUtil.builderWithValidValues();

    private final LegacyCliAdapter instance = new LegacyCliAdapter();

    private Path dataDirectory;

    @Before
    public void onSetUp() throws IOException {
        dataDirectory = Files.createTempDirectory("data");

        Files.createFile(dataDirectory.resolve("foo.pub"));
        Files.createFile(dataDirectory.resolve("foo.key"));
        Files.createFile(dataDirectory.resolve("foo2.pub"));
        Files.createFile(dataDirectory.resolve("foo2.key"));
    }

    @After
    public void onTearDown() throws IOException {
        Files.deleteIfExists(Paths.get("tessera-config.json"));

        Files.walk(dataDirectory).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
    }

    @Test
    public void getType() {
        assertThat(instance.getType()).isEqualTo(CliType.CONFIG_MIGRATION);
    }

    @Test
    public void help() throws Exception {
        final CliResult result = instance.execute("help");

        assertThat(result).isNotNull();
        assertThat(result.getConfig()).isNotPresent();
        assertThat(result.getStatus()).isEqualTo(0);
    }

    @Test
    public void noArgs() throws Exception {
        final CliResult result = instance.execute();

        assertThat(result).isNotNull();
        assertThat(result.getConfig()).isNotPresent();
        assertThat(result.getStatus()).isEqualTo(1);
    }

    @Test
    public void withoutCliArgsAllConfigIsSetFromTomlFile() throws Exception {
        dataDirectory = Paths.get("data");
        Files.createDirectory(dataDirectory);

        final Path alwaysSendTo1 = Files.createFile(dataDirectory.resolve("alwayssendto1"));
        final String keys1 =
                "/+UuD63zItL1EbjxkKUljMgG8Z1w0AJ8pNOR4iq2yQc=\njWKqelS4XjJ67JBbuKE7x9CVGFJ706wRYy/ev/OCOzk=";
        Files.write(alwaysSendTo1, keys1.getBytes());

        Path alwaysSendTo2 = Files.createFile(dataDirectory.resolve("alwayssendto2"));
        Files.write(alwaysSendTo2, "yGcjkFyZklTTXrn8+WIkYwicA2EGBn9wZFkctAad4X0=".getBytes());

        Path sampleFile = Paths.get(getClass().getResource("/sample-toml-no-nulls.conf").toURI());
        Map<String, Object> params = new HashMap<>();
        params.put("alwaysSendToPath1", "alwayssendto1");
        params.put("alwaysSendToPath2", "alwayssendto2");

        String data = ElUtil.process(new String(Files.readAllBytes(sampleFile)), params);

        Path configFile = Files.createTempFile("noOptions", ".txt");
        Files.write(configFile, data.getBytes());

        CliResult result = instance.execute("--tomlfile=" + configFile.toString());

        assertThat(result).isNotNull();
        assertThat(result.getConfig()).isPresent();
        final Config config = result.getConfig().get();

        final ServerConfig q2tConfig = this.getQ2tServer(config.getServerConfigs());
        assertThat(q2tConfig.getServerAddress()).isEqualTo("unix:data/constellation.ipc");

        final ServerConfig p2pConfig = this.getP2pServer(config.getServerConfigs());
        assertThat(p2pConfig.getServerAddress()).isEqualTo("http://127.0.0.1:9001");
        assertThat(p2pConfig.getBindingAddress()).isEqualTo("http://127.0.0.1:9001");
        assertThat(p2pConfig.getSslConfig().getTls()).isEqualByComparingTo(SslAuthenticationMode.STRICT);
        assertThat(p2pConfig.getSslConfig().getServerTlsCertificatePath().toString())
                .isEqualTo("data/tls-server-cert.pem");
        assertThat(p2pConfig.getSslConfig().getServerTrustCertificates().size()).isEqualTo(2);
        assertThat(p2pConfig.getSslConfig().getServerTrustCertificates().get(0).toString()).isEqualTo("data/chain1");
        assertThat(p2pConfig.getSslConfig().getServerTrustCertificates().get(1).toString()).isEqualTo("data/chain2");
        assertThat(p2pConfig.getSslConfig().getServerTlsKeyPath().toString()).isEqualTo("data/tls-server-key.pem");
        assertThat(p2pConfig.getSslConfig().getServerTrustMode()).isEqualByComparingTo(SslTrustMode.TOFU);
        assertThat(p2pConfig.getSslConfig().getKnownClientsFile().toString()).isEqualTo("data/tls-known-clients");
        assertThat(p2pConfig.getSslConfig().getClientTlsCertificatePath().toString())
                .isEqualTo("data/tls-client-cert.pem");
        assertThat(p2pConfig.getSslConfig().getClientTrustCertificates().size()).isEqualTo(2);
        assertThat(p2pConfig.getSslConfig().getClientTrustCertificates().get(0).toString())
                .isEqualTo("data/clientchain1");
        assertThat(p2pConfig.getSslConfig().getClientTrustCertificates().get(1).toString())
                .isEqualTo("data/clientchain2");
        assertThat(p2pConfig.getSslConfig().getClientTlsKeyPath().toString()).isEqualTo("data/tls-client-key.pem");
        assertThat(p2pConfig.getSslConfig().getClientTrustMode()).isEqualByComparingTo(SslTrustMode.CA_OR_TOFU);
        assertThat(p2pConfig.getSslConfig().getKnownServersFile().toString()).isEqualTo("data/tls-known-servers");

        assertThat(config.getPeers().size()).isEqualTo(2);
        assertThat(config.getPeers().get(0).getUrl()).isEqualTo("http://127.0.0.1:9001/");
        assertThat(config.getPeers().get(1).getUrl()).isEqualTo("http://127.0.0.1:9002/");
        assertThat(config.getKeys().getKeyData().size()).isEqualTo(2);
        assertThat(config.getKeys().getKeyData().get(0))
                .extracting("publicKeyPath")
                .containsExactly(Paths.get("data/foo.pub"));
        assertThat(config.getKeys().getKeyData().get(0))
                .extracting("privateKeyPath")
                .containsExactly(Paths.get("data/foo.key"));
        assertThat(config.getKeys().getKeyData().get(1))
                .extracting("publicKeyPath")
                .containsExactly(Paths.get("data/foo2.pub"));
        assertThat(config.getKeys().getKeyData().get(1))
                .extracting("privateKeyPath")
                .containsExactly(Paths.get("data/foo2.key"));
        assertThat(config.getAlwaysSendTo().size()).isEqualTo(3);
        assertThat(config.getAlwaysSendTo().get(0)).isEqualTo("/+UuD63zItL1EbjxkKUljMgG8Z1w0AJ8pNOR4iq2yQc=");
        assertThat(config.getAlwaysSendTo().get(1)).isEqualTo("jWKqelS4XjJ67JBbuKE7x9CVGFJ706wRYy/ev/OCOzk=");
        assertThat(config.getAlwaysSendTo().get(2)).isEqualTo("yGcjkFyZklTTXrn8+WIkYwicA2EGBn9wZFkctAad4X0=");
        assertThat(config.getKeys().getPasswordFile().toString()).isEqualTo("data/passwords");
        assertThat(config.getJdbcConfig().getUrl()).isEqualTo("jdbc:h2:mem:tessera");
        assertThat(config.isUseWhiteList()).isTrue();
    }

    @Test
    public void providingCliArgsOverridesTomlFileConfig() throws Exception {

        Path sampleFile = Paths.get(getClass().getResource("/sample.conf").toURI());

        String data = new String(Files.readAllBytes(sampleFile));

        Path configFile = Files.createTempFile("noOptions", ".txt");
        Files.write(configFile, data.getBytes());

        Path workdir = Paths.get("override");

        if (Files.exists(workdir)) {
            Files.walk(workdir).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
        }
        Files.createDirectory(workdir);
        Files.createFile(workdir.resolve("new.pub"));
        Files.createFile(workdir.resolve("new.key"));
        Path alwaysSendToFile = Files.createFile(workdir.resolve("alwayssendto"));
        Files.write(
                alwaysSendToFile,
                ("/+UuD63zItL1EbjxkKUljMgG8Z1w0AJ8pNOR4iq2yQc=\n" + "jWKqelS4XjJ67JBbuKE7x9CVGFJ706wRYy/ev/OCOzk=")
                        .getBytes());

        String[] args = {
            "--tomlfile=" + configFile.toString(),
            "--url=http://override",
            "--port=1111",
            "--workdir=override",
            "--socket=cli.ipc",
            "--othernodes=http://others",
            "--publickeys=new.pub",
            "--privatekeys=new.key",
            "--alwayssendto=alwayssendto",
            "--passwords=pw.txt",
            "--storage=jdbc:test",
            "--ipwhitelist",
            "--socket=cli.ipc",
            "--tls=off",
            "--tlsservercert=over-server-cert.pem",
            "--tlsserverchain=serverchain.file",
            "--tlsserverkey=over-server-key.pem",
            "--tlsservertrust=whitelist",
            "--tlsknownclients=over-known-clients",
            "--tlsclientcert=over-client-cert.pem",
            "--tlsclientchain=clientchain.file",
            "--tlsclientkey=over-client-key.pem",
            "--tlsclienttrust=tofu",
            "--tlsknownservers=over-known-servers"
        };

        CliResult result = instance.execute(args);

        assertThat(result).isNotNull();
        assertThat(result.getConfig()).isPresent();

        final Config config = result.getConfig().get();

        final ServerConfig q2tConfig = this.getQ2tServer(config.getServerConfigs());
        assertThat(q2tConfig.getServerAddress()).isEqualTo("unix:override/cli.ipc");

        final ServerConfig p2pConfig = this.getP2pServer(config.getServerConfigs());
        assertThat(p2pConfig.getBindingAddress()).isEqualTo("http://override:1111");
        assertThat(p2pConfig.getServerAddress()).isEqualTo("http://override:1111");
        assertThat(p2pConfig.getSslConfig().getTls()).isEqualByComparingTo(SslAuthenticationMode.OFF);
        assertThat(p2pConfig.getSslConfig().getServerTlsCertificatePath().toString())
                .isEqualTo("override/over-server-cert.pem");
        assertThat(p2pConfig.getSslConfig().getServerTrustCertificates().size()).isEqualTo(1);
        assertThat(p2pConfig.getSslConfig().getServerTrustCertificates().get(0).toString())
                .isEqualTo("override/serverchain.file");
        assertThat(p2pConfig.getSslConfig().getServerTlsKeyPath().toString()).isEqualTo("override/over-server-key.pem");
        assertThat(p2pConfig.getSslConfig().getServerTrustMode()).isEqualByComparingTo(SslTrustMode.WHITELIST);
        assertThat(p2pConfig.getSslConfig().getKnownClientsFile().toString()).isEqualTo("override/over-known-clients");
        assertThat(p2pConfig.getSslConfig().getClientTlsCertificatePath().toString())
                .isEqualTo("override/over-client-cert.pem");
        assertThat(p2pConfig.getSslConfig().getClientTrustCertificates().size()).isEqualTo(1);
        assertThat(p2pConfig.getSslConfig().getClientTrustCertificates().get(0).toString())
                .isEqualTo("override/clientchain.file");
        assertThat(p2pConfig.getSslConfig().getClientTlsKeyPath().toString()).isEqualTo("override/over-client-key.pem");
        assertThat(p2pConfig.getSslConfig().getClientTrustMode()).isEqualByComparingTo(SslTrustMode.TOFU);
        assertThat(p2pConfig.getSslConfig().getKnownServersFile().toString()).isEqualTo("override/over-known-servers");

        assertThat(config.getPeers().size()).isEqualTo(1);
        assertThat(config.getPeers().get(0).getUrl()).isEqualTo("http://others");
        assertThat(config.getKeys().getKeyData().size()).isEqualTo(1);
        assertThat(config.getKeys().getKeyData().get(0))
                .extracting("publicKeyPath")
                .containsExactly(Paths.get("override/new.pub"));
        assertThat(config.getKeys().getKeyData().get(0))
                .extracting("privateKeyPath")
                .containsExactly(Paths.get("override/new.key"));
        assertThat(config.getAlwaysSendTo().size()).isEqualTo(2);
        assertThat(config.getAlwaysSendTo().get(0)).isEqualTo("/+UuD63zItL1EbjxkKUljMgG8Z1w0AJ8pNOR4iq2yQc=");
        assertThat(config.getAlwaysSendTo().get(1)).isEqualTo("jWKqelS4XjJ67JBbuKE7x9CVGFJ706wRYy/ev/OCOzk=");
        assertThat(config.getKeys().getPasswordFile().toString()).isEqualTo("override/pw.txt");
        assertThat(config.getJdbcConfig().getUrl()).isEqualTo("jdbc:test");
        assertThat(config.isUseWhiteList()).isTrue();

        Files.walk(workdir).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
    }

    @Test
    public void ifConfigParameterIsNotSetInTomlOrCliThenDefaultIsUsed() throws Exception {

        Path configFile = Files.createTempFile("emptyConfig", ".txt");
        Path keysFile = Files.createTempFile("key", ".tmp").toAbsolutePath();
        Files.write(keysFile, Arrays.asList("SOMEDATA"));

        String[] requiredParams = {
            "--tomlfile=" + configFile.toString(),
            "--url=http://127.0.0.1",
            "--port=9001",
            "--othernodes=localhost:1111",
            "--socket=myipcfile.ipc",
            "--publickeys=" + keysFile.toString(),
            "--privatekeys=" + keysFile.toString()
        };

        CliResult result = instance.execute(requiredParams);

        assertThat(result).isNotNull();
        assertThat(result.getConfig()).isPresent();
        assertThat(result.getStatus()).isEqualTo(0);

        final Config config = result.getConfig().get();

        final ServerConfig q2tConfig = this.getQ2tServer(config.getServerConfigs());
        assertThat(q2tConfig.getServerAddress()).isEqualTo("unix:myipcfile.ipc");

        final ServerConfig p2pConfig = this.getP2pServer(config.getServerConfigs());
        assertThat(p2pConfig.getSslConfig().getTls()).isEqualByComparingTo(SslAuthenticationMode.OFF);
        assertThat(p2pConfig.getSslConfig().getServerTlsCertificatePath()).isNull();
        assertThat(p2pConfig.getSslConfig().getServerTrustCertificates().size()).isEqualTo(0);
        assertThat(p2pConfig.getSslConfig().getServerTlsKeyPath()).isNull();
        assertThat(p2pConfig.getSslConfig().getServerTrustMode()).isEqualByComparingTo(SslTrustMode.TOFU);
        assertThat(p2pConfig.getSslConfig().getKnownClientsFile()).isNull();
        assertThat(p2pConfig.getSslConfig().getClientTlsCertificatePath()).isNull();
        assertThat(p2pConfig.getSslConfig().getClientTrustCertificates().size()).isEqualTo(0);
        assertThat(p2pConfig.getSslConfig().getClientTlsKeyPath()).isNull();
        assertThat(p2pConfig.getSslConfig().getClientTrustMode()).isEqualByComparingTo(SslTrustMode.TOFU);
        assertThat(p2pConfig.getSslConfig().getKnownServersFile()).isNull();

        // Empty List
        assertThat(Optional.ofNullable(result.getConfig().get().getKeys().getKeyData()).isPresent()).isEqualTo(true);
        assertThat(result.getConfig().get().getAlwaysSendTo().size()).isEqualTo(0);
        assertThat(result.getConfig().get().getKeys().getPasswordFile()).isNull();
        assertThat(result.getConfig().get().getJdbcConfig().getUrl()).isEqualTo("jdbc:h2:mem:tessera");
        assertThat(result.getConfig().get().isUseWhiteList()).isFalse();
    }

    @Test
    public void ifWorkDirCliOverrideIsProvidedThenItIsAppliedToBothTomlAndCliSetParameters() throws Exception {

        Path workdir = Paths.get("override");

        if (Files.exists(workdir)) {
            Files.walk(workdir).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
        }
        Files.createDirectory(workdir);
        Files.createFile(workdir.resolve("new.pub"));
        Files.createFile(workdir.resolve("new.key"));
        Path alwaysSendToFile = Files.createFile(workdir.resolve("alwayssendto"));
        Files.write(
                alwaysSendToFile,
                ("/+UuD63zItL1EbjxkKUljMgG8Z1w0AJ8pNOR4iq2yQc=\n" + "jWKqelS4XjJ67JBbuKE7x9CVGFJ706wRYy/ev/OCOzk=")
                        .getBytes());

        Path sampleFile = Paths.get(getClass().getResource("/sample-toml-no-nulls-tls-off.conf").toURI());
        Map<String, Object> params = new HashMap<>();
        params.put("alwaysSendToPath1", "alwayssendto");
        params.put("alwaysSendToPath2", "alwayssendto");

        String data = ElUtil.process(new String(Files.readAllBytes(sampleFile)), params);

        Path configFile = Files.createTempFile("workdiroverride", ".txt");
        Files.write(configFile, data.getBytes());

        String[] args = {
            "--tomlfile=" + configFile.toString(),
            "--workdir=override",
            "--publickeys=" + "new.pub",
            "--privatekeys=" + "new.key"
        };

        CliResult result = instance.execute(args);

        assertThat(result).isNotNull();
        assertThat(result.getConfig()).isPresent();

        final Config config = result.getConfig().get();

        final ServerConfig q2tConfig = this.getQ2tServer(config.getServerConfigs());
        assertThat(q2tConfig.getServerAddress()).isEqualTo("unix:override/constellation.ipc");

        final ServerConfig p2pConfig = this.getP2pServer(config.getServerConfigs());
        assertThat(p2pConfig.getServerAddress()).isEqualTo("http://127.0.0.1:9001");
        assertThat(p2pConfig.getSslConfig().getTls()).isEqualByComparingTo(SslAuthenticationMode.OFF);
        assertThat(p2pConfig.getSslConfig().getServerTlsCertificatePath().toString())
                .isEqualTo("override/tls-server-cert.pem");
        assertThat(p2pConfig.getSslConfig().getServerTrustCertificates().size()).isEqualTo(2);
        assertThat(p2pConfig.getSslConfig().getServerTrustCertificates().get(0).toString())
                .isEqualTo("override/chain1");
        assertThat(p2pConfig.getSslConfig().getServerTrustCertificates().get(1).toString())
                .isEqualTo("override/chain2");
        assertThat(p2pConfig.getSslConfig().getServerTlsKeyPath().toString()).isEqualTo("override/tls-server-key.pem");
        assertThat(p2pConfig.getSslConfig().getServerTrustMode()).isEqualByComparingTo(SslTrustMode.TOFU);
        assertThat(p2pConfig.getSslConfig().getKnownClientsFile().toString()).isEqualTo("override/tls-known-clients");
        assertThat(p2pConfig.getSslConfig().getClientTlsCertificatePath().toString())
                .isEqualTo("override/tls-client-cert.pem");
        assertThat(p2pConfig.getSslConfig().getClientTrustCertificates().size()).isEqualTo(2);
        assertThat(p2pConfig.getSslConfig().getClientTrustCertificates().get(0).toString())
                .isEqualTo("override/clientchain1");
        assertThat(p2pConfig.getSslConfig().getClientTrustCertificates().get(1).toString())
                .isEqualTo("override/clientchain2");
        assertThat(p2pConfig.getSslConfig().getClientTlsKeyPath().toString()).isEqualTo("override/tls-client-key.pem");
        assertThat(p2pConfig.getSslConfig().getClientTrustMode()).isEqualByComparingTo(SslTrustMode.CA_OR_TOFU);
        assertThat(p2pConfig.getSslConfig().getKnownServersFile().toString()).isEqualTo("override/tls-known-servers");

        assertThat(config.getPeers().size()).isEqualTo(2);
        assertThat(config.getPeers().get(0).getUrl()).isEqualTo("http://127.0.0.1:9001/");
        assertThat(config.getPeers().get(1).getUrl()).isEqualTo("http://127.0.0.1:9002/");
        assertThat(config.getKeys().getKeyData().size()).isEqualTo(1);
        assertThat(config.getKeys().getKeyData().get(0))
                .extracting("publicKeyPath")
                .containsExactly(Paths.get("override/new.pub"));
        assertThat(config.getKeys().getKeyData().get(0))
                .extracting("privateKeyPath")
                .containsExactly(Paths.get("override/new.key"));
        assertThat(config.getAlwaysSendTo().size()).isEqualTo(4);
        assertThat(config.getAlwaysSendTo().get(0)).isEqualTo("/+UuD63zItL1EbjxkKUljMgG8Z1w0AJ8pNOR4iq2yQc=");
        assertThat(config.getAlwaysSendTo().get(1)).isEqualTo("jWKqelS4XjJ67JBbuKE7x9CVGFJ706wRYy/ev/OCOzk=");
        assertThat(config.getAlwaysSendTo().get(2)).isEqualTo("/+UuD63zItL1EbjxkKUljMgG8Z1w0AJ8pNOR4iq2yQc=");
        assertThat(config.getAlwaysSendTo().get(3)).isEqualTo("jWKqelS4XjJ67JBbuKE7x9CVGFJ706wRYy/ev/OCOzk=");
        assertThat(config.getKeys().getPasswordFile().toString()).isEqualTo("override/passwords");
        assertThat(config.getJdbcConfig().getUrl()).isEqualTo("jdbc:h2:mem:tessera");
        assertThat(config.isUseWhiteList()).isTrue();
    }

    @Test
    public void urlNotSetGivesNullHostname() throws Exception {

        Path configFile = Files.createTempFile("emptyConfig", ".txt");

        String[] requiredParams = {
            "--tomlfile=" + configFile.toString(), "--port=9001", "--othernodes=localhost:1111",
        };

        CliResult result = instance.execute(requiredParams);

        assertThat(result).isNotNull();
        assertThat(result.getConfig()).isPresent();

        final ServerConfig q2tConfig = this.getP2pServer(result.getConfig().get().getServerConfigs());
        assertThat(q2tConfig.getServerAddress()).isNull();
    }

    @Test
    public void urlWithPortSet() throws Exception {

        Path configFile = Files.createTempFile("emptyConfig", ".txt");

        String[] requiredParams = {
            "--tomlfile=" + configFile.toString(), "--url=http://127.0.0.1:9001", "--port=9001",
        };

        CliResult result = instance.execute(requiredParams);

        assertThat(result).isNotNull();
        assertThat(result.getConfig()).isPresent();

        final ServerConfig q2tConfig = this.getP2pServer(result.getConfig().get().getServerConfigs());
        assertThat(q2tConfig.getServerAddress()).isEqualTo("http://127.0.0.1:9001");
    }

    @Test
    public void invalidUrlProvided() throws Exception {

        Path configFile = Files.createTempFile("emptyConfig", ".txt");

        String[] requiredParams = {
            "--tomlfile=" + configFile.toString(), "--url=htt://invalidHost", "--port=9001",
        };

        final Throwable throwable = catchThrowable(() -> instance.execute(requiredParams));

        assertThat(throwable)
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Bad server url given: unknown protocol: htt");
    }

    @Test
    public void sampleTomlFileOnly() throws Exception {

        Path serverKeyStorePath = Files.createTempFile("serverKeyStorePath", ".bog");
        Path passwordFile = Files.createTempFile("passwords", ".txt");
        Files.write(passwordFile, Arrays.asList("PASWORD1"));

        Path sampleFile = Paths.get(getClass().getResource("/sample.conf").toURI());
        Map<String, Object> params = new HashMap<>();
        params.put("passwordFile", passwordFile);
        params.put("serverKeyStorePath", serverKeyStorePath);

        String data = Files.readAllLines(sampleFile).stream().collect(Collectors.joining(System.lineSeparator()));

        Path configFile = Files.createTempFile("noOptions", ".txt");
        Files.write(configFile, data.getBytes());

        CliResult result = instance.execute("--tomlfile=" + configFile.toString());

        assertThat(result).isNotNull();
        assertThat(result.getConfig()).isPresent();
        // TODO assert that value of config is as expected from sample config

        //        assertThat(result.getStatus()).isEqualTo(0);
    }

    @Test
    public void noOptionsWithTomlFile() throws Exception {

        Path serverKeyStorePath = Files.createTempFile("serverKeyStorePath", ".bog");
        Path passwordFile = Files.createTempFile("passwords", ".txt");
        Files.write(passwordFile, Arrays.asList("PASWORD1"));

        Path sampleFile = Paths.get(getClass().getResource("/sample-all-values.conf").toURI());
        Map<String, Object> params = new HashMap<>();
        params.put("passwordFile", passwordFile);
        params.put("serverKeyStorePath", serverKeyStorePath);

        String data = ElUtil.process(new String(Files.readAllBytes(sampleFile)), params);

        Path configFile = Files.createTempFile("noOptions", ".txt");
        Files.write(configFile, data.getBytes());

        CliResult result = instance.execute("--tomlfile=" + configFile.toString());

        assertThat(result).isNotNull();
        assertThat(result.getConfig()).isPresent();
    }

    @Test
    public void passwordOverrideProvidedButNoKeyDataOverrideProvidedThenPrintMessageToConsole() {
        final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        final PrintStream errStream = new PrintStream(errContent);

        MockSystemAdapter systemAdapter = (MockSystemAdapter) SystemAdapter.INSTANCE;
        systemAdapter.setErrPrintStream(errStream);

        CommandLine line = mock(CommandLine.class);
        when(line.getOptionValue("passwords")).thenReturn("override/path");

        ConfigBuilder configBuilder = ConfigBuilder.create();

        LegacyCliAdapter.applyOverrides(line, configBuilder, KeyDataBuilder.create());

        assertThat(errContent.toString())
                .isEqualTo(
                        "Info: Public/Private key data not provided in overrides.  Overriden password file has not been added to config.\n");
    }

    @Test
    public void noPasswordOrKeyDataOverrideProvidedThenNoMessagePrintedToConsole() {
        CommandLine line = mock(CommandLine.class);

        ConfigBuilder configBuilder = ConfigBuilder.create();

        LegacyCliAdapter.applyOverrides(line, configBuilder, KeyDataBuilder.create());

        assertThat(systemErrRule.getLog()).isEmpty();
    }

    @Test
    public void keyDataProvidedButNoPasswordProvidedThenNoMessagePrintedToConsole() {
        CommandLine line = mock(CommandLine.class);
        when(line.getOptionValue("passwords")).thenReturn("override/path");

        ConfigBuilder configBuilder = ConfigBuilder.create();

        List<String> publicKeys = new ArrayList<>();
        publicKeys.add("pub");
        List<String> privateKeys = new ArrayList<>();
        privateKeys.add("priv");

        KeyDataBuilder keyDataBuilder = KeyDataBuilder.create().withPublicKeys(publicKeys).withPrivateKeys(privateKeys);

        LegacyCliAdapter.applyOverrides(line, configBuilder, keyDataBuilder);

        assertThat(systemErrRule.getLog()).isEmpty();
    }

    @Test
    public void passwordAndKeyDataProvidedAsOverrideThenNoMessagePrintedToConsole() {
        CommandLine line = mock(CommandLine.class);

        ConfigBuilder configBuilder = ConfigBuilder.create();

        List<String> publicKeys = new ArrayList<>();
        publicKeys.add("pub");
        List<String> privateKeys = new ArrayList<>();
        privateKeys.add("priv");

        KeyDataBuilder keyDataBuilder = KeyDataBuilder.create().withPublicKeys(publicKeys).withPrivateKeys(privateKeys);

        LegacyCliAdapter.applyOverrides(line, configBuilder, keyDataBuilder);

        assertThat(systemErrRule.getLog()).isEmpty();
    }

    @Test
    public void ifTomlWorkDirProvidedWithoutOverrideWorkDirThenTomlWorkDirUsedOnOverridenValues() {
        CommandLine line = mock(CommandLine.class);
        when(line.getOptionValue("workdir")).thenReturn(null);
        String socketFilepath = "path/to/socket.ipc";
        when(line.getOptionValue("socket")).thenReturn(socketFilepath);

        String tomlWorkDir = "toml";
        ConfigBuilder configBuilder = ConfigBuilder.create().workdir(tomlWorkDir);

        ConfigBuilder result = LegacyCliAdapter.applyOverrides(line, configBuilder, KeyDataBuilder.create());

        Path expected = Paths.get(tomlWorkDir, socketFilepath);

        final ServerConfig q2tConfig = this.getQ2tServer(result.build().getServerConfigs());
        assertThat(q2tConfig.getServerAddress()).isEqualTo("unix:" + expected.toString());
    }

    @Test
    public void ifTomlWorkDirProvidedWithOverrideWorkDirThenOverrideWorkDirUsedOnOverridenValues() {
        CommandLine line = mock(CommandLine.class);
        String overrideWorkDir = "override";
        when(line.getOptionValue("workdir")).thenReturn(overrideWorkDir);
        when(line.getOptionValue("workdir", ".")).thenReturn(overrideWorkDir);
        String socketFilepath = "path/to/socket.ipc";
        when(line.getOptionValue("socket")).thenReturn(socketFilepath);

        ConfigBuilder configBuilder = ConfigBuilder.create();

        ConfigBuilder result = LegacyCliAdapter.applyOverrides(line, configBuilder, KeyDataBuilder.create());

        Path expected = Paths.get(overrideWorkDir, socketFilepath);

        final ServerConfig q2tConfig = this.getQ2tServer(result.build().getServerConfigs());
        assertThat(q2tConfig.getServerAddress()).isEqualTo("unix:" + expected.toString());
    }

    @Test
    public void ifTomlWorkDirNotProvidedWithoutOverrideWorkDirThenDefaultWorkDirUsedOnOverridenValues() {
        CommandLine line = mock(CommandLine.class);
        when(line.getOptionValue("workdir")).thenReturn(null);
        String socketFilepath = "path/to/socket.ipc";
        when(line.getOptionValue("socket")).thenReturn(socketFilepath);

        ConfigBuilder configBuilder = ConfigBuilder.create();

        ConfigBuilder result = LegacyCliAdapter.applyOverrides(line, configBuilder, KeyDataBuilder.create());

        final ServerConfig q2tConfig = this.getQ2tServer(result.build().getServerConfigs());
        assertThat(q2tConfig.getServerAddress()).isEqualTo("unix:" + socketFilepath);
    }

    @Test
    public void ifTomlWorkDirNotProvidedButOverrideWorkDirIsThenOverrideWorkDirUsedOnOverridenValues() {
        CommandLine line = mock(CommandLine.class);
        String overrideWorkDir = "override";
        when(line.getOptionValue("workdir")).thenReturn(overrideWorkDir);
        when(line.getOptionValue("workdir", ".")).thenReturn(overrideWorkDir);
        String socketFilepath = "path/to/socket.ipc";
        when(line.getOptionValue("socket")).thenReturn(socketFilepath);

        ConfigBuilder configBuilder = ConfigBuilder.create();

        ConfigBuilder result = LegacyCliAdapter.applyOverrides(line, configBuilder, KeyDataBuilder.create());

        Path expected = Paths.get(overrideWorkDir, socketFilepath);

        final ServerConfig q2tConfig = this.getQ2tServer(result.build().getServerConfigs());
        assertThat(q2tConfig.getServerAddress()).isEqualTo("unix:" + expected.toString());
    }

    @Test
    public void applyOverrides() throws Exception {

        String urlOverride = "http://junit.com:8989";
        int portOverride = 9999;
        String unixSocketFileOverride = "unixSocketFileOverride.ipc";
        String workdirOverride = "workdirOverride";
        List<Peer> overridePeers =
                Arrays.asList(new Peer("http://otherone.com:9188/other"), new Peer("http://yetanother.com:8829/other"));

        CommandLine commandLine = mock(CommandLine.class);

        // TODO check all CLI options have assertions here
        when(commandLine.getOptionValue("url")).thenReturn(urlOverride);
        when(commandLine.getOptionValue("port")).thenReturn(String.valueOf(portOverride));
        when(commandLine.getOptionValue("workdir")).thenReturn(workdirOverride);
        when(commandLine.getOptionValue("socket")).thenReturn(unixSocketFileOverride);
        when(commandLine.getOptionValues("othernodes"))
                .thenReturn(overridePeers.stream().map(Peer::getUrl).toArray(String[]::new));
        when(commandLine.getOptionValues("publickeys")).thenReturn(new String[] {"ONE", "TWO"});
        when(commandLine.getOptionValue("storage")).thenReturn("sqlite:somepath");
        when(commandLine.getOptionValue("tlsservertrust")).thenReturn("whitelist");
        when(commandLine.getOptionValue("tlsclienttrust")).thenReturn("ca");
        when(commandLine.getOptionValue("tlsservercert")).thenReturn("tlsservercert.cert");
        when(commandLine.getOptionValue("tlsclientcert")).thenReturn("tlsclientcert.cert");
        when(commandLine.getOptionValues("tlsserverchain"))
                .thenReturn(new String[] {"server1.crt", "server2.crt", "server3.crt"});
        when(commandLine.getOptionValues("tlsclientchain"))
                .thenReturn(new String[] {"client1.crt", "client2.crt", "client3.crt"});
        when(commandLine.getOptionValue("tlsserverkey")).thenReturn("tlsserverkey.key");
        when(commandLine.getOptionValue("tlsclientkey")).thenReturn("tlsclientkey.key");

        doReturn(new String[] {}).when(commandLine).getOptionValues("alwayssendto");

        List<Path> privateKeyPaths =
                Arrays.asList(
                        Files.createTempFile("applyOverrides1", ".txt"),
                        Files.createTempFile("applyOverrides2", ".txt"));

        when(commandLine.getOptionValue("tlsknownservers")).thenReturn("tlsknownservers.file");

        when(commandLine.getOptionValue("tlsknownclients")).thenReturn("tlsknownclients.file");

        final byte[] privateKeyData = FixtureUtil.createLockedPrivateKey().toString().getBytes();
        for (Path p : privateKeyPaths) {
            Files.write(p, privateKeyData);
        }

        final String[] privateKeyPathStrings = privateKeyPaths.stream().map(Path::toString).toArray(String[]::new);

        when(commandLine.getOptionValues("privatekeys")).thenReturn(privateKeyPathStrings);

        final List<String> privateKeyPasswords = Arrays.asList("SECRET1", "SECRET2");

        final Path privateKeyPasswordFile = Files.createTempFile("applyOverridesPasswords", ".txt");
        Files.write(privateKeyPasswordFile, privateKeyPasswords);

        when(commandLine.getOptionValue("passwords")).thenReturn(privateKeyPasswordFile.toString());

        Config result =
                LegacyCliAdapter.applyOverrides(commandLine, builderWithValidValues, KeyDataBuilder.create()).build();

        assertThat(result).isNotNull();

        final ServerConfig q2tConfig = this.getQ2tServer(result.getServerConfigs());
        assertThat(q2tConfig.getServerAddress())
                .isEqualTo("unix:" + Paths.get(workdirOverride, unixSocketFileOverride).toString());

        final ServerConfig p2pConfig = this.getP2pServer(result.getServerConfigs());
        assertThat(p2pConfig.getBindingAddress()).isEqualTo("http://junit.com:" + portOverride);
        assertThat(p2pConfig.getServerAddress()).isEqualTo("http://junit.com:" + portOverride);
        assertThat(p2pConfig.getSslConfig().getServerTrustMode()).isEqualTo(SslTrustMode.WHITELIST);
        assertThat(p2pConfig.getSslConfig().getClientTrustMode()).isEqualTo(SslTrustMode.CA);
        assertThat(p2pConfig.getSslConfig().getClientTlsCertificatePath())
                .isEqualTo(Paths.get("workdirOverride/tlsclientcert.cert"));
        assertThat(p2pConfig.getSslConfig().getServerTlsCertificatePath())
                .isEqualTo(Paths.get("workdirOverride/tlsservercert.cert"));
        assertThat(p2pConfig.getSslConfig().getServerTrustCertificates())
                .containsExactly(
                        Paths.get(workdirOverride, "server1.crt"),
                        Paths.get(workdirOverride, "server2.crt"),
                        Paths.get(workdirOverride, "server3.crt"));
        assertThat(p2pConfig.getSslConfig().getClientTrustCertificates())
                .containsExactly(
                        Paths.get(workdirOverride, "client1.crt"),
                        Paths.get(workdirOverride, "client2.crt"),
                        Paths.get(workdirOverride, "client3.crt"));
        assertThat(p2pConfig.getSslConfig().getServerKeyStore())
                .isEqualTo(Paths.get("workdirOverride/sslServerKeyStorePath"));
        assertThat(p2pConfig.getSslConfig().getClientKeyStore())
                .isEqualTo(Paths.get("workdirOverride/sslClientKeyStorePath"));
        assertThat(p2pConfig.getSslConfig().getKnownServersFile())
                .isEqualTo(Paths.get("workdirOverride/tlsknownservers.file"));
        assertThat(p2pConfig.getSslConfig().getKnownClientsFile())
                .isEqualTo(Paths.get(workdirOverride, "tlsknownclients.file"));

        assertThat(result.getPeers()).containsExactly(overridePeers.toArray(new Peer[0]));
        assertThat(result.getKeys().getKeyData()).hasSize(2);
        assertThat(result.getJdbcConfig()).isNotNull();
        assertThat(result.getJdbcConfig().getUrl()).isEqualTo("jdbc:sqlite:somepath");
    }

    //    @Test
    //    public void applyOverridesNullValues() {
    //        Config expectedValues = builderWithValidValues.build();
    //
    //        CommandLine commandLine = mock(CommandLine.class);
    //
    //        Config result = LegacyCliAdapter.applyOverrides(commandLine, builderWithValidValues,
    // KeyDataBuilder.create()).build();
    //
    //        assertThat(result).isNotNull();
    //
    //        final DeprecatedServerConfig expectedServerConfig = expectedValues.getServer();
    //        final DeprecatedServerConfig realServerConfig = result.getServer();
    //        final SslConfig sslConfig = realServerConfig.getSslConfig();
    //
    //        assertThat(realServerConfig.getHostName()).isEqualTo(expectedServerConfig.getHostName());
    //        assertThat(realServerConfig.getPort()).isEqualTo(expectedServerConfig.getPort());
    //        assertThat(realServerConfig.getBindingAddress()).isEqualTo(expectedServerConfig.getBindingAddress());
    //
    //        assertThat(result.getUnixSocketFile()).isEqualTo(expectedValues.getUnixSocketFile());
    //
    //        assertThat(result.getPeers()).containsOnlyElementsOf(expectedValues.getPeers());
    //
    //        assertThat(result.getJdbcConfig().getUrl()).isEqualTo("jdbc:bogus");
    //
    //        assertThat(sslConfig.getServerTrustMode()).isEqualTo(SslTrustMode.TOFU);
    //        assertThat(sslConfig.getClientTrustMode()).isEqualTo(SslTrustMode.CA_OR_TOFU);
    //        assertThat(sslConfig.getClientKeyStore()).isEqualTo(Paths.get("sslClientKeyStorePath"));
    //        assertThat(sslConfig.getServerKeyStore()).isEqualTo(Paths.get("sslServerKeyStorePath"));
    //
    // assertThat(sslConfig.getServerTrustCertificates()).containsExactly(Paths.get("sslServerTrustCertificates"));
    //
    // assertThat(sslConfig.getClientTrustCertificates()).containsExactly(Paths.get("sslClientTrustCertificates"));
    //        assertThat(sslConfig.getKnownServersFile()).isEqualTo(Paths.get("knownServersFile"));
    //        assertThat(sslConfig.getKnownClientsFile()).isEqualTo(Paths.get("knownClientsFile"));
    //    }

    @Test
    public void writeToOutputFileValidationError() throws Exception {
        Config config = mock(Config.class);

        Path outputPath = Files.createTempFile("writeToOutputFileValidationError", ".txt");

        CliResult result = LegacyCliAdapter.writeToOutputFile(config, outputPath);

        assertThat(result.getStatus()).isEqualTo(2);
    }

    private ServerConfig getQ2tServer(final List<ServerConfig> serverConfigs) {
        return serverConfigs.stream().filter(config -> AppType.Q2T.equals(config.getApp())).findFirst().orElse(null);
    }

    private ServerConfig getP2pServer(final List<ServerConfig> serverConfigs) {
        return serverConfigs.stream().filter(config -> AppType.P2P.equals(config.getApp())).findFirst().orElse(null);
    }
}
