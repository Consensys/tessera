package com.github.tessera.config.cli;

import com.github.tessera.config.Config;
import com.github.tessera.config.ConfigFactory;
import com.github.tessera.config.SslAuthenticationMode;
import com.github.tessera.config.SslTrustMode;
import com.github.tessera.io.FilesDelegate;
import com.moandjiezana.toml.Toml;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TomlConfigFactory implements ConfigFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(TomlConfigFactory.class);

    private final FilesDelegate filesDelegate;

    public TomlConfigFactory() {
        this(FilesDelegate.create());
    }    
    
    public TomlConfigFactory(FilesDelegate filesDelegate) {
        this.filesDelegate = Objects.requireNonNull(filesDelegate);
    }
    
    
    
    @Override
    public Config create(InputStream configData, InputStream... keyConfigData) {
        if (keyConfigData.length != 0) {
            throw new UnsupportedOperationException("keyConfigData arg is not implemented for TomlConfigFactory");
        }

        Toml toml = new Toml().read(configData);

        if (LOGGER.isDebugEnabled()) {
            toml.toMap().entrySet().stream().forEach(entry -> {
                LOGGER.debug("Found entry in toml file : {} {}", entry.getKey(), entry.getValue());
            });
        }

        String url = toml.getString("url");

        String socket = toml.getString("socket");

        String tls = toml.getString("tls", "strict").toUpperCase();

        //??
        String workdir = toml.getString("workdir", ".");

        List<String> othernodes = toml.getList("othernodes", Collections.EMPTY_LIST);

        List<String> publicKeyList = toml.getList("publickeys", Collections.EMPTY_LIST);

        List<String> privateKeyList = toml.getList("privatekeys", Collections.EMPTY_LIST);

        //String privateKeyPasswordFile = toml.getString("passwords")
        final List<String> privateKeyPasswords;
        if (toml.contains("passwords")) {
            String privateKeyPasswordFile = toml.getString("passwords");
            
            Path privateKeyPasswordFilePath = Paths.get(privateKeyPasswordFile);
            privateKeyPasswords = filesDelegate
                    .lines(privateKeyPasswordFilePath)
                    .collect(Collectors.toList());

        } else {
            privateKeyPasswords = Collections.unmodifiableList(Collections.EMPTY_LIST);
        }
       
        
        List<String> alwayssendtoList = toml.getList("alwayssendto", Collections.EMPTY_LIST);

        String tlsserverkey = toml.getString("tlsserverkey", "tls-server-key.pem");

        List<String> tlsserverchain = toml.getList("tlsserverchain", Collections.EMPTY_LIST);

        String storage = toml.getString("storage", "dir:storage");

        //verbosity
        String tlsservercert = toml.getString("tlsservercert", "tls-server-cert.pem");

        String tlsservertrust = toml.getString("tlsservertrust", "tofu");

        String tlsclienttrust = toml.getString("tlsclienttrust", "ca-or-tofu");

        ConfigBuilder configBuilder = ConfigBuilder.create()
                .serverUri(url)
                .unixSocketFile(socket)
                .sslAuthenticationMode(SslAuthenticationMode.valueOf(tls))
                .sslServerKeyStorePath(tlsserverkey)
                .sslServerTrustMode(resolve(tlsservertrust))
                .sslServerTrustStorePath(tlsservertrust)
                .sslServerCertificate(tlsservercert)
                .sslClientTrustMode(resolve(tlsclienttrust))
                .sslClientKeyStorePath("FIXME")
                .sslClientKeyStorePassword("FIXME")
                .sslClientTrustStorePath("FIXME")
                .knownClientsFile("FIXME")
                .knownServersFile("FIXME")
                .peers(othernodes);

        return configBuilder.build();
    }

    static SslTrustMode resolve(String value) {
        return Stream.of(SslTrustMode.values())
                .filter(s -> Objects.equals(s.name(), Objects.toString(value).toUpperCase()))
                .findFirst()
                .orElse(
                        Stream.of(SslTrustMode.CA, SslTrustMode.TOFU)
                                .filter(o -> Objects.equals("ca-or-tofu", value))
                                .findAny()
                                .orElse(SslTrustMode.NONE));
    }

}
