module tessera.argontwo {
  requires de.mkammerer.argon2;
  requires tessera.shared;
  requires org.slf4j;

  exports com.quorum.tessera.argon2;

  uses com.quorum.tessera.argon2.Argon2;

  provides com.quorum.tessera.argon2.Argon2 with
      com.quorum.tessera.argon2.Argon2Impl;
}
