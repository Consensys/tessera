package com.quorum.tessera.encryption;

public class EncryptorFactoryNotFoundException extends RuntimeException {

  public EncryptorFactoryNotFoundException(String encryptorType) {
    super(encryptorType + " implementation of EncryptorFactory was not found on the classpath");
  }
}
