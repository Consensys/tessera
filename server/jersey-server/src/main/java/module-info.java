module tessera.server.jersey.server.main {
    requires java.management;
    requires java.ws.rs;
  //  requires javax.servlet.api;
   // requires jersey.bean.validation;
    requires java.validation;
    requires jersey.server;
    requires jersey.container.servlet.core;
    requires jul.to.slf4j;
    requires org.eclipse.jetty.server;
    requires org.eclipse.jetty.servlet;
    requires org.slf4j;
    requires tessera.config.main;
    requires tessera.security.main;
    requires tessera.server.server.utils.main;
    requires tessera.server.server.api.main;
    requires java.servlet;
    requires tessera.shared.main;

    exports com.quorum.tessera.server.jersey;
    exports com.quorum.tessera.server.http;
}

