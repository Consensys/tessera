package com.quorum.tessera.openapi;

public class P2POperationsFilter extends TagOperationsFilter {

  @Override
  public String requiredTag() {
    return "peer-to-peer";
  }
}
