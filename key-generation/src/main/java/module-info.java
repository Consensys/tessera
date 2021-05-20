module tessera.key.generation.main {
  requires org.slf4j;
  requires tessera.config;
  requires tessera.encryption.encryption.api.main;
  requires tessera.keyvault.api;
  requires tessera.shared.main;
  requires org.bouncycastle.provider;

  exports com.quorum.tessera.key.generation;

  uses com.quorum.tessera.key.generation.KeyGeneratorFactory;
  uses com.quorum.tessera.encryption.EncryptorFactory;

  provides com.quorum.tessera.key.generation.KeyGeneratorFactory with
      com.quorum.tessera.key.generation.DefaultKeyGeneratorFactory;
}
