package com.github.nexus.configuration;

import com.github.nexus.configuration.model.KeyData;

import javax.json.JsonObject;
import java.net.URI;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

public interface Configuration {

    Path keygenBasePath();

    List<String> publicKeys();

    List<JsonObject> privateKeys();

    List<String> passwords();

    //transient
    List<KeyData> keyData();

    String url();

    int port();

    //transient
    URI uri();

    List<String> othernodes();

    List<String> generatekeys();

    Set<String> whitelist();

    String workdir();

    String socket();

}
