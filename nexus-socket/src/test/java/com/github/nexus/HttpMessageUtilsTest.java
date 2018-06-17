package com.github.nexus;

import com.github.nexus.socket.HttpMessageUtils;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class HttpProxyTest {

    @Test
    public void testContentLengthParser() {
        final String headerLine = "Content-Length: 42";

        assertEquals(42, HttpMessageUtils.getContentLength(headerLine));
    }


    @Ignore
    public void testSomething() {
        final String headerLine = "HTTP/1.1 200 OK\n" +
            "Content-Type: text/plain\n" +
            "Content-Length: 9\n" +
            "\n" +
            "some text";

    }
}
