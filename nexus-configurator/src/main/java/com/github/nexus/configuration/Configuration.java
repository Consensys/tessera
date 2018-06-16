package com.github.nexus.configuration;

import java.nio.file.Path;
import java.util.List;

public interface Configuration {

    Path keygenBasePath();

    List<String> publicKeys();

    String privateKeys();

    String url();

    int port();

    List<String> othernodes();

    String workdir();

    String socket();

}
