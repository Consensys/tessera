module tessera.server.jaxrs.client.unixsocket.main {
    requires java.ws.rs;
    requires jersey.client;
    requires jersey.common;
    requires org.eclipse.jetty.client;
    requires org.eclipse.jetty.http;
    requires org.eclipse.jetty.unixsocket;
    requires org.eclipse.jetty.util;
    requires org.slf4j;

    exports com.quorum.tessera.jaxrs.unixsocket;
}
