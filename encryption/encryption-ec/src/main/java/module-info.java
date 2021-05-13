module tessera.encryption.encryption.ec.main {
  requires org.bouncycastle.provider;
  requires org.slf4j;
  requires tessera.encryption.encryption.api.main;

  provides com.quorum.tessera.encryption.EncryptorFactory with
      com.jpmorgan.quorum.encryption.ec.EllipticalCurveEncryptorFactory;
}
