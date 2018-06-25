package com.github.nexus;

import com.github.nexus.configuration.Configuration;
import com.github.nexus.configuration.model.KeyData;

import javax.json.JsonObject;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;

public class TestConfiguration implements Configuration {

    @Override
    public List<String> publicKeys() {
        return emptyList();
    }

    @Override
    public List<JsonObject> privateKeys() {
        return emptyList();
    }

    @Override
    public List<String> passwords() {
        return emptyList();
    }

    @Override
    public List<KeyData> keyData() {
        return emptyList();
    }

    @Override
    public String url() {
        return "http://localhost";
    }

    @Override
    public int port() {
        return 8080;
    }

    @Override
    public URI uri() {
        return UriBuilder.fromUri(url()).port(port()).build();
    }

    @Override
    public List<String> othernodes() {
        return emptyList();
    }

    @Override
    public Path keygenBasePath() {
        return Paths.get("./").toAbsolutePath();
    }

    @Override
    public List<String> generatekeys() {
        return emptyList();
    }

    @Override
    public Set<String> whitelist() {
        return emptySet();
    }
    @Override
    public String workdir() { return "qdata"; };

    @Override
    public String socket() { return "/tmp/tst1.ipc"; }

    @Override
    public String databaseURL() {
        return "jdbc:h2:./target/h2/nexus;MODE=Oracle;TRACE_LEVEL_SYSTEM_OUT=0";
    }

    @Override
    public String tls() {
        return "off";
    }

    @Override
    public String serverKeyStore() {
        return null;
    }

    @Override
    public String serverKeyStorePassword() {
        return null;
    }

    @Override
    public String serverTrustStore() {
        return null;
    }

    @Override
    public String serverTrustStorePassword() {
        return null;
    }

    @Override
    public String knownClients() {
        return null;
    }

    @Override
    public String serverTrustMode() {
        return null;
    }

    @Override
    public String clientTrustMode() {
        return null;
    }

    @Override
    public String knownServers() {
        return null;
    }

    @Override
    public String clientKeyStore() {
        return null;
    }

    @Override
    public String clientKeyStorePassword() {
        return null;
    }

    @Override
    public String clientTrustStore() {
        return null;
    }

    @Override
    public String clientTrustStorePassword() {
        return null;
    }
}
