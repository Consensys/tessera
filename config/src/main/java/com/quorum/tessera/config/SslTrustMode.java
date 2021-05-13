package com.quorum.tessera.config;

public enum SslTrustMode {
  CA,
  TOFU,
  WHITELIST,
  CA_OR_TOFU,
  NONE
}
