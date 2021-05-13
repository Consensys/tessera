package com.quorum.tessera.openapi;

public class Q2TOperationsFilter extends TagOperationsFilter {

  @Override
  public String requiredTag() {
    return "quorum-to-tessera";
  }
}
