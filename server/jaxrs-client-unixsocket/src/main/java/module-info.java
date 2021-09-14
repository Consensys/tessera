module tessera.server.jersey.unixsocket {
  requires jakarta.ws.rs;
  requires jersey.client;
  requires jersey.common;
  requires org.eclipse.jetty.client;
  requires org.eclipse.jetty.http;
  requires org.eclipse.jetty.util;
  requires org.slf4j;
  requires org.eclipse.jetty.unixsocket.client;

  exports com.quorum.tessera.jaxrs.unixsocket;
}
