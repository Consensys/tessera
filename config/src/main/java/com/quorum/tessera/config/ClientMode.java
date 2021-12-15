package com.quorum.tessera.config;

import jakarta.xml.bind.annotation.XmlEnumValue;

public enum ClientMode {
  @XmlEnumValue("tessera")
  TESSERA,
  @XmlEnumValue("orion")
  ORION
}
