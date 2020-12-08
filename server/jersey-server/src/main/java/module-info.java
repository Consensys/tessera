module tessera.server.jersey.server.main {

    requires java.management;
    requires java.ws.rs;
    requires java.validation;
    requires org.eclipse.jetty.server;
    requires org.eclipse.jetty.servlet;
    requires org.slf4j;
    requires tessera.config.main;
    requires tessera.security.main;
    requires tessera.server.server.utils.main;
    requires tessera.server.server.api.main;
    requires java.servlet;
    requires tessera.shared.main;
    requires hk2.api;
    requires jakarta.inject;
    requires jersey.server;
    requires jersey.common;
    requires jersey.container.servlet.core;
    requires jul.to.slf4j;
    requires hk2.utils;
    requires java.annotation;
    requires jersey.bean.validation;
    requires jersey.hk2;

    requires java.net.http;

    exports com.quorum.tessera.server.jersey;
    exports com.quorum.tessera.server.http;

    exports com.quorum.tessera.server.jaxrs to hk2.locator;
    opens com.quorum.tessera.server.jaxrs to hk2.utils;

    provides com.quorum.tessera.server.TesseraServerFactory
        with com.quorum.tessera.server.jersey.JerseyServerFactory;

}

