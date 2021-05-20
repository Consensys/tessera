module tessera.argontwo {
  requires de.mkammerer.argon2;
  requires org.slf4j;
  requires tessera.shared.main;

  exports com.quorum.tessera.argon2;

  uses com.quorum.tessera.argon2.Argon2;

  provides com.quorum.tessera.argon2.Argon2 with
      com.quorum.tessera.argon2.Argon2Impl;
}
