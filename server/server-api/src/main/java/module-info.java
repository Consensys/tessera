module tessera.server.server.api.main {
  requires org.slf4j;
  requires tessera.config.main;
  requires tessera.shared.main;

  exports com.quorum.tessera.server;

  uses com.quorum.tessera.server.TesseraServerFactory;
}
