module tessera.encryption.encryption.jnacl.main {
    requires jnacl;
    requires org.slf4j;
    requires tessera.encryption.encryption.api.main;

    uses com.quorum.tessera.encryption.EncryptorFactory;

    provides com.quorum.tessera.encryption.EncryptorFactory with
        com.quorum.tessera.nacl.jnacl.JnaclFactory;
}
