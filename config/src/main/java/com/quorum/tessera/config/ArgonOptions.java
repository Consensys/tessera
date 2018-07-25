package com.quorum.tessera.config;

import java.util.Optional;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlType(factoryMethod = "create")
@XmlAccessorType(XmlAccessType.FIELD)
public class ArgonOptions extends ConfigItem {

    @Pattern(regexp = "^(id|i|d)$")
    @XmlAttribute(name = "variant")
    private final String algorithm;

    @NotNull
    @XmlAttribute
    private final Integer iterations;

    @NotNull
    @XmlAttribute
    private final Integer memory;

    @NotNull
    @XmlAttribute
    private Integer parallelism;

    private static ArgonOptions create() {
        return new ArgonOptions();
    }

    public ArgonOptions(String algorithm, Integer iterations, Integer memory, Integer parallelism) {
        this.algorithm = Optional.ofNullable(algorithm).orElse("id");
        this.iterations = iterations;
        this.memory = memory;
        this.parallelism = parallelism;
    }

    private ArgonOptions() {
        this(null, null, null, null);
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public Integer getIterations() {
        return iterations;
    }

    public Integer getMemory() {
        return memory;
    }

    public Integer getParallelism() {
        return parallelism;
    }

}
