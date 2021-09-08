package com.quorum.tessera.config;

import jakarta.xml.bind.annotation.XmlEnumValue;

public enum AppType {
  P2P,
  Q2T,
  @XmlEnumValue("ThirdParty")
  THIRD_PARTY,
  ENCLAVE,
  ADMIN
}
