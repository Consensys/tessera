
package com.github.nexus.socket;

import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;

import static org.assertj.core.api.Assertions.assertThat;

public class HttpProxyFactoryTest {

    @Test
    public void create() throws URISyntaxException {

        final URI uri = new URI("http://bogus.com");

        final HttpProxyFactory httpProxyFactory = new HttpProxyFactory(uri);

        final HttpProxy httpProxy = httpProxyFactory.create();

        assertThat(httpProxy).isNotNull();

    }

}
