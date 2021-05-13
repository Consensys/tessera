package com.quorum.tessera.enclave.rest;

public class EnclaveUnencryptPayload {

  private byte[] data;

  private byte[] providedKey;

  public byte[] getData() {
    return data;
  }

  public void setData(byte[] data) {
    this.data = data;
  }

  public byte[] getProvidedKey() {
    return providedKey;
  }

  public void setProvidedKey(byte[] providedKey) {
    this.providedKey = providedKey;
  }
}
