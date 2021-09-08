module tessera.server.utils {
  requires org.eclipse.jetty.server;
  requires org.eclipse.jetty.util;
  requires tessera.config;
  requires tessera.security;
  requires org.eclipse.jetty.unixsocket.server;

  exports com.quorum.tessera.server.utils;
}
