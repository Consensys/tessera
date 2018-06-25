package com.github.nexus.config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlType(factoryMethod = "create")
@XmlAccessorType(XmlAccessType.FIELD)
public class ArgonOptions {

    @XmlAttribute
    private final String algorithm;

    @XmlAttribute
    private final int iterations;

    @XmlAttribute
    private final int memory;

    @XmlAttribute
    private int parallelism;

    private static ArgonOptions create() {
        return new ArgonOptions();
    }
    
    public ArgonOptions(String algorithm, int iterations, int memory, int parallelism) {
        this.algorithm = algorithm;
        this.iterations = iterations;
        this.memory = memory;
        this.parallelism = parallelism;
    }

    private ArgonOptions() {
        this(null, -1, -1, -1);
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
