package com.quorum.tessera.config.util;

import static org.assertj.core.api.Assertions.assertThat;

import com.quorum.tessera.config.ConfigException;
import jakarta.xml.bind.JAXBException;
import java.io.IOException;
import javax.xml.transform.TransformerException;
import org.junit.Test;

public class XmlProcessingCallbackTest {

  @Test
  public void execute() {
    XmlProcessingCallback<String> callback = () -> "HELLO";
    assertThat(XmlProcessingCallback.execute(callback)).isEqualTo("HELLO");
  }

  @Test(expected = ConfigException.class)
  public void executeThrowsIOException() {
    XmlProcessingCallback callback =
        () -> {
          throw new IOException();
        };
    XmlProcessingCallback.execute(callback);
  }

  @Test(expected = ConfigException.class)
  public void executeThrowsJAXBException() {
    XmlProcessingCallback callback =
        () -> {
          throw new JAXBException("");
        };
    XmlProcessingCallback.execute(callback);
  }

  @Test(expected = ConfigException.class)
  public void executeThrowsTransformerException() {
    XmlProcessingCallback callback =
        () -> {
          throw new TransformerException("");
        };
    XmlProcessingCallback.execute(callback);
  }
}
