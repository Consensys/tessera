package com.quorum.tessera.ssl.util;

import java.net.InetAddress;
import java.net.UnknownHostException;

public interface HostnameUtil {

  default String getHostName() throws UnknownHostException {
    return InetAddress.getLocalHost().getHostName();
  }

  default String getHostIpAddress() throws UnknownHostException {
    return InetAddress.getLocalHost().getHostAddress();
  }

  static HostnameUtil create() {
    return new HostnameUtil() {};
  }
}
