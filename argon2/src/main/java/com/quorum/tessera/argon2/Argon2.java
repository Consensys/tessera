package com.quorum.tessera.argon2;

import java.util.ServiceLoader;

/** Provides hashing functions using the Argon2 class of algorithms. */
public interface Argon2 {

  /**
   * Hash the password using the provided options
   *
   * @param options the custom options to call Argon2 with
   * @param password the password to hash
   * @param salt the salt to apply when hashing
   * @return the result which contains the output, as well as the input parameters
   */
  ArgonResult hash(ArgonOptions options, char[] password, byte[] salt);

  /**
   * Hash the password using the given salt and some default options
   *
   * @param password the password to hash
   * @param salt the salt to apply when hashing
   * @return the result which contains the output, as well as the input parameters
   */
  ArgonResult hash(char[] password, byte[] salt);

  // TODO: move into factory and return the stream itself
  static Argon2 create() {
    return ServiceLoader.load(Argon2.class).findFirst().get();
  }
}
