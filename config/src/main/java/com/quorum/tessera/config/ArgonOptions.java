package com.quorum.tessera.config;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import java.util.Optional;

@XmlAccessorType(XmlAccessType.FIELD)
public class ArgonOptions extends ConfigItem {

  @Pattern(regexp = "^(id|i|d)$")
  @XmlAttribute(name = "variant")
  private String algorithm;

  @NotNull @XmlAttribute private Integer iterations;

  @NotNull @XmlAttribute private Integer memory;

  @NotNull @XmlAttribute private Integer parallelism;

  public ArgonOptions(String algorithm, Integer iterations, Integer memory, Integer parallelism) {
    this.algorithm = Optional.ofNullable(algorithm).orElse("id");
    this.iterations = iterations;
    this.memory = memory;
    this.parallelism = parallelism;
  }

  public ArgonOptions() {}

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

  public void setAlgorithm(String algorithm) {
    this.algorithm = algorithm;
  }

  public void setIterations(Integer iterations) {
    this.iterations = iterations;
  }

  public void setMemory(Integer memory) {
    this.memory = memory;
  }

  public void setParallelism(Integer parallelism) {
    this.parallelism = parallelism;
  }
}
