package com.quorum.tessera.openapi;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

public class FullyQualifiedNameResolverTest {

  @Test
  public void defaultConstructor() {
    final FullyQualifiedNameResolver fqnr = new FullyQualifiedNameResolver();
    assertThat(fqnr).isNotNull();
  }

  @Test
  public void constructor() {
    final FullyQualifiedNameResolver fqnr = new FullyQualifiedNameResolver(new ObjectMapper());
    assertThat(fqnr).isNotNull();
  }
}
