package com.quorum.tessera.context.internal;

import com.quorum.tessera.context.RuntimeContext;
import java.util.Optional;

/*
RuntimeContextFactory and RuntimeContext instance
 */
enum RuntimeContextHolder {
  INSTANCE;

  private RuntimeContext runtimeContext;

  Optional<RuntimeContext> getContext() {
    return Optional.ofNullable(runtimeContext);
  }

  void setContext(RuntimeContext runtimeContext) {
    if (this.runtimeContext != null && runtimeContext != null) {
      throw new IllegalStateException("RuntimeContext has already been stored");
    }
    this.runtimeContext = runtimeContext;
  }
}
