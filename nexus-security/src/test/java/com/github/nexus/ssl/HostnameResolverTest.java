package com.github.nexus.ssl;

import com.github.nexus.ssl.util.HostnameResolver;
import org.junit.Test;

import java.net.UnknownHostException;

import static org.assertj.core.api.Java6Assertions.assertThat;

public class HostnameResolverTest {

    @Test
    public void testGetHostName() throws UnknownHostException {
        String hostName = HostnameResolver.create().getHostName();
        assertThat(hostName).isNotEmpty();
    }

    @Test
    public void testThrowException(){
    }
}
