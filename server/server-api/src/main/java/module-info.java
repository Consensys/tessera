module tessera.server.api {
  requires org.slf4j;
  requires tessera.config;
  requires tessera.shared.main;

  exports com.quorum.tessera.server;

  uses com.quorum.tessera.server.TesseraServerFactory;
}
