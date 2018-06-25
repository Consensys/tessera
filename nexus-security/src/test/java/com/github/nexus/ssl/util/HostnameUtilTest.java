package com.github.nexus.ssl.util;

import org.junit.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;

import static org.assertj.core.api.Java6Assertions.assertThat;

public class HostnameUtilTest {

    @Test
    public void testGetHostName() throws UnknownHostException {
        String hostName = HostnameUtil.create().getHostName();
        assertThat(hostName).isNotEmpty();
        assertThat(hostName).isEqualTo(InetAddress.getLocalHost().getHostName());
    }

}
