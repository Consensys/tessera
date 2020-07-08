module tessera.config.main {
    requires java.validation;

    requires java.xml;
    requires java.xml.bind;
    requires jasypt;
    requires org.apache.commons.lang3;
    requires org.slf4j;
    requires tessera.argon2.main;
    requires tessera.encryption.encryption.api.main;
    requires tessera.shared.main;

    exports com.quorum.tessera.config;
    exports com.quorum.tessera.config.apps;
    exports com.quorum.tessera.config.keypairs;
    exports com.quorum.tessera.config.keys;
    exports com.quorum.tessera.config.util;
    exports com.quorum.tessera.config.vault.data;
}
