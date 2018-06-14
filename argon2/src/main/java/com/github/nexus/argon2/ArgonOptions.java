package com.github.nexus.argon2;

public class ArgonOptions {

    private final String algorithm;

    private final int iterations;

    private final int memory;

    private final int parallelism;

    public ArgonOptions(final String algorithm, final int iterations, final int memory, final int parallelism) {
        this.algorithm = algorithm;
        this.iterations = iterations;
        this.memory = memory;
        this.parallelism = parallelism;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public int getIterations() {
        return iterations;
    }

    public int getMemory() {
        return memory;
    }

    public int getParallelism() {
        return parallelism;
    }
}
