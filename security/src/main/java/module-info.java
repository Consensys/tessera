module tessera.security.main {
    requires java.base;
    requires java.xml.bind;
    requires cryptacular;
    requires org.slf4j;
    requires tessera.config.main;
    requires tessera.shared.main;
    requires org.bouncycastle.pkix;
    requires org.bouncycastle.provider;

    exports com.quorum.tessera.ssl.context;
}
