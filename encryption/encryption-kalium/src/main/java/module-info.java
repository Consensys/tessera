module tessera.encryption.encryption.kalium.main {
    requires kalium;
    requires org.slf4j;
    requires tessera.encryption.encryption.api.main;

    provides com.quorum.tessera.encryption.EncryptorFactory with
        com.quorum.tessera.nacl.kalium.KaliumFactory;
}
