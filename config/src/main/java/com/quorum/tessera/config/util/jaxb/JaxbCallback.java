package com.quorum.tessera.config.util.jaxb;

import jakarta.xml.bind.DataBindingException;
import jakarta.xml.bind.JAXBException;

@FunctionalInterface
public interface JaxbCallback<T> {

  T doExecute() throws JAXBException;

  static <T> T execute(JaxbCallback<T> callback) {
    try {
      return callback.doExecute();
    } catch (JAXBException ex) {
      throw new DataBindingException(ex);
    }
  }
}
