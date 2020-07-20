module tessera.shared.main {
    requires java.compiler;
    requires java.xml.bind;
    requires org.slf4j;

    exports com.quorum.tessera.base64;
    exports com.quorum.tessera.exception;
    exports com.quorum.tessera.io;
    exports com.quorum.tessera.jaxb;
    exports com.quorum.tessera.loader;
    exports com.quorum.tessera.passwords;
    exports com.quorum.tessera.reflect;
    exports com.quorum.tessera.service;
    exports com.quorum.tessera.version;
}
