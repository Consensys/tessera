package com.github.tessera.config;

import java.util.Objects;
import java.util.Optional;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlType(factoryMethod = "create")
@XmlAccessorType(XmlAccessType.FIELD)
public class ArgonOptions {

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

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 83 * hash + Objects.hashCode(this.algorithm);
        hash = 83 * hash + Objects.hashCode(this.iterations);
        hash = 83 * hash + Objects.hashCode(this.memory);
        hash = 83 * hash + Objects.hashCode(this.parallelism);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ArgonOptions other = (ArgonOptions) obj;
        if (!Objects.equals(this.algorithm, other.algorithm)) {
            return false;
        }
        if (!Objects.equals(this.iterations, other.iterations)) {
            return false;
        }
        if (!Objects.equals(this.memory, other.memory)) {
            return false;
        }
        if (!Objects.equals(this.parallelism, other.parallelism)) {
            return false;
        }
        return true;
    }

}
