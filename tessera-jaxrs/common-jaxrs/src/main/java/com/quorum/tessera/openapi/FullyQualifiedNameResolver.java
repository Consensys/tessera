package com.quorum.tessera.openapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.core.jackson.ModelResolver;
import io.swagger.v3.core.jackson.TypeNameResolver;

public class FullyQualifiedNameResolver extends ModelResolver {

  public FullyQualifiedNameResolver() {
    this(new ObjectMapper());
  }

  public FullyQualifiedNameResolver(ObjectMapper mapper) {
    super(mapper, TypeNameResolver.std);
    TypeNameResolver.std.setUseFqn(true);
  }
}
