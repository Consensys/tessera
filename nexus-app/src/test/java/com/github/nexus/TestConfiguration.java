package com.github.nexus;

import com.github.nexus.configuration.Configuration;

import java.util.Collections;
import java.util.List;

public class TestConfiguration implements Configuration {

    @Override
    public List<String> publicKeys() {
        return Collections.emptyList();
    }

    @Override
    public String privateKeys() {
        return "";
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
    public List<String> othernodes() {
        return Collections.emptyList();
    }

    @Override
    public String keygenBasePath() {
        return "./";
    }

}
