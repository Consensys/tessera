package com.quorum.tessera.test;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.util.JaxbUtil;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.ws.rs.core.UriBuilder;

public enum Party {

    ONE("/+UuD63zItL1EbjxkKUljMgG8Z1w0AJ8pNOR4iq2yQc=", Party.class.getResource("/rest/config1.json")),
    TWO("yGcjkFyZklTTXrn8+WIkYwicA2EGBn9wZFkctAad4X0=", Party.class.getResource("/rest/config2.json")),
    THREE("giizjhZQM6peq52O7icVFxdTmTYinQSUsvyhXzgZqkE=", Party.class.getResource("/rest/config3.json")),
    FOUR("Tj8xg/HpsYmh7Te3UerzlLx1HgpWVOGq25ZgbwaPNVM=", Party.class.getResource("/rest/config4.json"));

    private final String publicKey;

    private final URI uri;

    private final Config config;

    Party(String publicKey, URL configUrl) {
        this.publicKey = publicKey;
        try (InputStream inputStream = configUrl.openStream()) {
            this.config = JaxbUtil.unmarshal(inputStream, Config.class);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
        this.uri = UriBuilder.fromUri(config.getServerConfig().getHostName()).port(config.getServerConfig().getPort()).build();

    }

    public String getPublicKey() {
        return publicKey;
    }

    public URI getUri() {
        return uri;
    }
    
    public List<Party> getAlwaysSendTo() {
        return config.getAlwaysSendTo()
            .stream()
            .map(Party::findByKey).collect(Collectors.toList());
    }
    
    static Party findByKey(String key) {
        return Stream.of(values()).filter(p -> p.getPublicKey().equals(key)).findFirst().get();
    }

    public Connection getDatabaseConnection() {

        String url = config.getJdbcConfig().getUrl();
        String username = config.getJdbcConfig().getUsername();
        String password = config.getJdbcConfig().getPassword();
        try {
            return DriverManager.getConnection(url, username, password);
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }
    

}
