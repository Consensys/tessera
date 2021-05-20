open module tessera.data {
  requires java.instrument;
  requires java.persistence;
  requires org.bouncycastle.provider;
  requires org.slf4j;
  requires tessera.config;
  requires tessera.enclave.enclave.api.main;
  requires tessera.encryption.encryption.api.main;
  requires tessera.shared.main;
  requires java.sql;
  requires com.zaxxer.hikari;
  requires java.validation;
  requires tessera.eclipselink.utils.main;

  //    opens com.quorum.tessera.data to org.eclipse.persistence.core;
  //    opens com.quorum.tessera.data.staging to org.eclipse.persistence.core;

  exports com.quorum.tessera.data;
  exports com.quorum.tessera.data.staging;

  uses com.quorum.tessera.enclave.PayloadDigest;
  uses com.quorum.tessera.data.EncryptedTransactionDAO;
  uses com.quorum.tessera.data.EncryptedRawTransactionDAO;
  uses com.quorum.tessera.data.staging.StagingEntityDAO;
  uses com.quorum.tessera.data.DataSourceFactory;
  uses com.quorum.tessera.data.PrivacyGroupDAO;

  provides com.quorum.tessera.data.EncryptedTransactionDAO with
      com.quorum.tessera.data.EncryptedTransactionDAOProvider;
  provides com.quorum.tessera.data.EncryptedRawTransactionDAO with
      com.quorum.tessera.data.EncryptedRawTransactionDAOProvider;
  provides com.quorum.tessera.data.staging.StagingEntityDAO with
      com.quorum.tessera.data.staging.StagingEntityDAOProvider;
  provides com.quorum.tessera.data.PrivacyGroupDAO with
      com.quorum.tessera.data.PrivacyGroupDAOProvider;
  provides com.quorum.tessera.data.DataSourceFactory with
      com.quorum.tessera.data.DataSourceFactoryProvider;
}
