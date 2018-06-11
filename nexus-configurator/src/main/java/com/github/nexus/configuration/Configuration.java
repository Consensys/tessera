package com.github.nexus.configuration;

import java.util.List;

public interface Configuration {

    List<String> publicKeys();

    List<String> privateKeys();

    String url();

    int port();

    List<String> othernodes();

}
