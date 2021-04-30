module tessera.encryption.encryption.api.main {
    requires org.slf4j;
    requires tessera.shared.main;
    requires org.bouncycastle.provider;

    uses com.quorum.tessera.encryption.EncryptorFactory;

    exports com.quorum.tessera.encryption;

}
