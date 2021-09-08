package com.quorum.tessera.config.util;

import com.quorum.tessera.config.ConfigException;
import jakarta.xml.bind.JAXBException;
import java.io.IOException;
import javax.xml.transform.TransformerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@FunctionalInterface
public interface XmlProcessingCallback<T> {

  Logger LOGGER = LoggerFactory.getLogger(XmlProcessingCallback.class);

  T doExecute() throws IOException, JAXBException, TransformerException;

  static <T> T execute(XmlProcessingCallback<T> callback) {
    try {
      return callback.doExecute();
    } catch (IOException | JAXBException | TransformerException ex) {
      LOGGER.error(null, ex);
      throw new ConfigException(ex);
    }
  }
}
