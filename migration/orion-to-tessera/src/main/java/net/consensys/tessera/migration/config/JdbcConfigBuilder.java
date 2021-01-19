package net.consensys.tessera.migration.config;

import com.quorum.tessera.config.JdbcConfig;

import java.util.Objects;

public class JdbcConfigBuilder {

    public static JdbcConfigBuilder create() {
        return new JdbcConfigBuilder();
    }

    private String username, password, url;

    public JdbcConfigBuilder withUser(String username) {
        this.username = username;
        return this;
    }

    public JdbcConfigBuilder withPassword(String password) {
        this.password = password;
        return this;
    }

    public JdbcConfigBuilder withUrl(String url) {
        this.url = url;
        return this;
    }

    public JdbcConfig build() {
        Objects.requireNonNull(url);
        Objects.requireNonNull(username);
        Objects.requireNonNull(password);

        JdbcConfig jdbcConfig = new JdbcConfig();
        jdbcConfig.setUrl(url);
        jdbcConfig.setUsername(username);
        jdbcConfig.setPassword(password);
        return jdbcConfig;
    }

    public JdbcConfig buildDefault() {
        return withPassword("[JDBC password]").withUrl("[JDBC URL]").withUser("[JDBC user]").build();
    }
}
