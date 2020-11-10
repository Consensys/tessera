module tessera.key.vault.azure.key.vault.main {
    requires org.slf4j;
    requires tessera.config.main;
    requires tessera.key.vault.key.vault.api.main;
    requires java.annotation;
    requires com.azure.identity;
    requires com.azure.security.keyvault.secrets;
  //  requires lombok;
 //   requires transitive org.mapstruct.processor;
    provides com.quorum.tessera.key.vault.KeyVaultServiceFactory with
        com.quorum.tessera.key.vault.azure.AzureKeyVaultServiceFactory;


}
