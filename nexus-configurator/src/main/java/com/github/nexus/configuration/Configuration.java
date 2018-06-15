package com.github.nexus.configuration;

import com.github.nexus.configuration.model.KeyData;

import javax.json.JsonObject;
import java.nio.file.Path;
import java.util.List;

public interface Configuration {

    Path keygenBasePath();

    List<String> publicKeys();

    List<JsonObject> privateKeys();

    List<String> passwords();

    List<KeyData> keyData();

    String url();

    int port();

    List<String> othernodes();

    List<String> generatekeys();

}
