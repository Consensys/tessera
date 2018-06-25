package com.github.nexus.config;

public interface ArgonOptions {

    String getAlgorithm();

    int getIterations();

    int getMemory();

    int getParallelism();
}
