package com.github.nexus;

import com.github.nexus.configuration.Configuration;

import java.nio.file.Path;
import java.nio.file.Paths;
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
    public Path keygenBasePath() {
        return Paths.get("./").toAbsolutePath();
    }

    @Override
    public String workdir() { return "qdata"; };

    @Override
    public String socket() { return "/tmp/tst1.ipc"; };

}
