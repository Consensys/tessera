package com.quorum.tessera.ssl.util;

import static org.assertj.core.api.Java6Assertions.assertThat;

import java.net.InetAddress;
import java.net.UnknownHostException;
import org.junit.Test;

public class HostnameUtilTest {

  @Test
  public void testGetHostName() throws UnknownHostException {
    String hostName = HostnameUtil.create().getHostName();
    assertThat(hostName).isNotEmpty();
    assertThat(hostName).isEqualTo(InetAddress.getLocalHost().getHostName());
  }

  @Test
  public void testGetHostIpAddress() throws UnknownHostException {
    String ipAddress = HostnameUtil.create().getHostIpAddress();
    assertThat(ipAddress).isNotEmpty();
    assertThat(ipAddress).isEqualTo(InetAddress.getLocalHost().getHostAddress());
  }
}
