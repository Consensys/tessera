package com.quorum.tessera.sync;

import java.io.IOException;
import java.io.UncheckedIOException;
import javax.websocket.EncodeException;
import static org.assertj.core.api.Assertions.*;
import org.junit.Test;

public class WebSocketCallbackTest {

    @Test
    public void execute() {

        String expectedOutput = "Expected Output";
        String result =
                WebSocketSessionCallback.execute(
                        () -> {
                            return expectedOutput;
                        });

        assertThat(result).isEqualTo(expectedOutput);
    }

    @Test(expected = UncheckedWebSocketException.class)
    public void exeuteThrowsEncodeException() {

        WebSocketSessionCallback.execute(
                () -> {
                    throw new EncodeException(this, "Ouch that's gotta smart!!");
                });
    }

    @Test(expected = UncheckedIOException.class)
    public void exeuteThrowsIOException() {

        WebSocketSessionCallback.execute(
                () -> {
                    throw new IOException("Ouch that's gotta smart!!");
                });
    }
}
