package com.quorum.tessera.config;

import javax.xml.bind.annotation.XmlEnumValue;

public enum ClientMode {
  @XmlEnumValue("tessera")
  TESSERA,
  @XmlEnumValue("orion")
  ORION
}
