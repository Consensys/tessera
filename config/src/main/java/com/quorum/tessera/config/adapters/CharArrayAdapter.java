package com.quorum.tessera.config.adapters;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;

public class CharArrayAdapter extends XmlAdapter<String, char[]> {
  @Override
  public char[] unmarshal(String s) {
    return s == null ? null : s.toCharArray();
  }

  @Override
  public String marshal(char[] chars) {
    return chars == null ? null : String.valueOf(chars);
  }
}
