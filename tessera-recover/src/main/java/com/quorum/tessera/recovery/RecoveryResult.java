package com.quorum.tessera.recovery;

public enum RecoveryResult {
  SUCCESS(0),
  PARTIAL_SUCCESS(1),
  FAILURE(2);

  private final int code;

  RecoveryResult(int code) {
    this.code = code;
  }

  public int getCode() {
    return code;
  }
}
