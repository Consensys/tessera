module tessera.encryption.jnacl {
  requires jnacl;
  requires org.slf4j;
  requires tessera.encryption.api;

  uses com.quorum.tessera.encryption.EncryptorFactory;

  provides com.quorum.tessera.encryption.EncryptorFactory with
      com.quorum.tessera.encryption.nacl.jnacl.JnaclFactory;
}
