package com.quorum.tessera.argon2;

import java.util.Arrays;

/** The result of a hash call to Argon2 contains the input options and the output hash */
public class ArgonResult {

  private final ArgonOptions options;

  private final byte[] salt;

  private final byte[] hash;

  public ArgonResult(final ArgonOptions options, final byte[] salt, final byte[] hash) {
    this.options = options;
    this.salt = Arrays.copyOf(salt, salt.length);
    this.hash = Arrays.copyOf(hash, hash.length);
  }

  public ArgonOptions getOptions() {
    return options;
  }

  public byte[] getSalt() {
    return salt;
  }

  public byte[] getHash() {
    return hash;
  }
}
