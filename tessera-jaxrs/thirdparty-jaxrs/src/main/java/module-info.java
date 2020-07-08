module tessera.tessera.jaxrs.thirdparty.jaxrs.main {
    requires java.json;
    requires java.ws.rs;
    requires swagger.annotations;
    requires tessera.config.main;
    requires tessera.encryption.encryption.api.main;
    requires tessera.tessera.context.main;
    requires tessera.tessera.core.main;
    requires tessera.tessera.jaxrs.common.jaxrs.main;
    requires tessera.tessera.partyinfo.main;
}
