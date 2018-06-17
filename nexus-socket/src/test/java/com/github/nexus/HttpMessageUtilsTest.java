package com.github.nexus;

import com.github.nexus.socket.HttpMessageUtils;

import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.BufferedReader;
import java.io.IOException;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.mock;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class HttpMessageUtilsTest {

    @Test
    public void testContentLengthParser() {
        final String headerLine = "Content-Length: 42";

        assertEquals(42, HttpMessageUtils.getContentLength(headerLine));
    }

    @Test
    public void testGetHttpMessage() {
        final String headerLine1 = "HTTP/1.1 200 OK";
        final String headerLine2 = "Content-Type: text/plain";
        final String headerLine3 = "Content-Length: 9";
        final String headerLine4 = "";
        final String dataLine1 = "some text";

        BufferedReader mockReader = mock(BufferedReader.class);
        try {
            when(mockReader.readLine()).thenReturn(headerLine1, headerLine2, headerLine3, headerLine4);

            char[] data = new char[9];
            Mockito.doAnswer(new Answer<Object>() {
                @Override
                public Object answer(InvocationOnMock invocation) throws Throwable {
                    Object[] args = invocation.getArguments();
                    char[] cbuf = (char[]) args[0];
                    int off = (int) args[1];
                    int len = (int) args[2];
                    dataLine1.getChars(0, len, cbuf, off);
                    return 9;
                }
            }).when(mockReader).read(data, 0, 9);

            String message = HttpMessageUtils.getHttpMessage(mockReader);

            verify(mockReader).read(any(char[].class), any(int.class), any(int.class));
            assertThat(message).isEqualTo(headerLine1 + "\n" + headerLine2 + "\n" + headerLine3 + "\n" + headerLine4 + "\n" + dataLine1);
            verify(mockReader, times(4)).readLine();

        } catch (IOException ex) {
            fail("Unexpected exception thrown");
        }
    }

    @Test
    public void testGetHttpMessageException() {

        try {
            BufferedReader mockReader = mock(BufferedReader.class);

            when(mockReader.readLine()).thenThrow(new IOException());

            String message = HttpMessageUtils.getHttpMessage(mockReader);

            failBecauseExceptionWasNotThrown(IOException.class);

        } catch (Exception ex) {
        }
    }
}
