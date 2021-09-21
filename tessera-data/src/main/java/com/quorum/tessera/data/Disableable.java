package com.quorum.tessera.data;

/** Inelegant work around */
public interface Disableable {

  default boolean isDisabled() {
    String s = System.getProperty("disable.jpa.listeners", "false");
    return Boolean.valueOf(System.getProperty("disable.jpa.listeners", "false"));
  }
}
