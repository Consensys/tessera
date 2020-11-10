module tessera.key.generation.main {
    requires org.slf4j;
    requires tessera.config.main;
    requires tessera.encryption.encryption.api.main;
    requires tessera.key.vault.key.vault.api.main;
    requires tessera.shared.main;

    exports com.quorum.tessera.key.generation;

    uses com.quorum.tessera.key.generation.KeyGeneratorFactory;

    uses com.quorum.tessera.encryption.EncryptorFactory;

    provides com.quorum.tessera.key.generation.KeyGeneratorFactory
        with com.quorum.tessera.key.generation.DefaultKeyGeneratorFactory;

}
