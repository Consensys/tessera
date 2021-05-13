package com.quorum.tessera.argon2;

import de.mkammerer.argon2.Argon2Advanced;
import de.mkammerer.argon2.Argon2Factory;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Argon2Impl implements Argon2 {

  private static final Logger LOGGER = LoggerFactory.getLogger(Argon2Impl.class);

  private static final ArgonOptions DEFAULT_OPTIONS = new ArgonOptions("i", 10, 1048576, 4);

  @Override
  public ArgonResult hash(final ArgonOptions options, final char[] password, final byte[] salt) {
    final Argon2Advanced argon2 = this.getArgon2Instance(options.getAlgorithm());

    final byte[] hash =
        argon2.rawHash(
            options.getIterations(), options.getMemory(), options.getParallelism(), password, salt);

    LOGGER.debug("Argon2 hash produced the array {}", Arrays.toString(hash));

    return new ArgonResult(options, salt, hash);
  }

  @Override
  public ArgonResult hash(final char[] password, final byte[] salt) {
    return this.hash(DEFAULT_OPTIONS, password, salt);
  }

  /**
   * The string form of the algorithm to use. If an invalid algorithm is chosen, a default of
   * Argon2i is chosen.
   *
   * @param algorithm the algorithm to use
   * @return an instance of the chosen algorithm
   */
  private Argon2Advanced getArgon2Instance(final String algorithm) {
    LOGGER.debug("Searching for the Argon2 algorithm {}", algorithm);

    switch (algorithm) {
      case "d":
        return Argon2Factory.createAdvanced(Argon2Factory.Argon2Types.ARGON2d);
      case "id":
        return Argon2Factory.createAdvanced(Argon2Factory.Argon2Types.ARGON2id);
      case "i":
        return Argon2Factory.createAdvanced(Argon2Factory.Argon2Types.ARGON2i);
      default:
        throw new IllegalArgumentException("Invalid Argon2 algorithm " + algorithm);
    }
  }
}
