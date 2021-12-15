module tessera.security {
  requires jakarta.xml.bind;
  requires cryptacular;
  requires org.slf4j;
  requires tessera.config;
  requires tessera.shared;
  requires org.bouncycastle.pkix;
  requires org.bouncycastle.provider;

  exports com.quorum.tessera.ssl.context;

  uses com.quorum.tessera.ssl.context.ClientSSLContextFactory;
  uses com.quorum.tessera.ssl.context.ServerSSLContextFactory;

  provides com.quorum.tessera.ssl.context.ClientSSLContextFactory with
      com.quorum.tessera.ssl.context.ClientSSLContextFactoryImpl;
  provides com.quorum.tessera.ssl.context.ServerSSLContextFactory with
      com.quorum.tessera.ssl.context.ServerSSLContextFactoryImpl;
}
