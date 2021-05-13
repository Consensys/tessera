package com.quorum.tessera.openapi;

public class ThirdPartyOperationsFilter extends TagOperationsFilter {

  @Override
  public String requiredTag() {
    return "third-party";
  }
}
