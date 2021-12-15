module tessera.shared {
  // requires java.compiler;

  requires jakarta.annotation;
  requires org.slf4j;

  exports com.quorum.tessera.base64;
  exports com.quorum.tessera.exception;
  exports com.quorum.tessera.io;
  exports com.quorum.tessera.passwords;
  exports com.quorum.tessera.reflect;
  exports com.quorum.tessera.service;
  exports com.quorum.tessera.version;
  exports com.quorum.tessera.threading;
  exports com.quorum.tessera.shared;
  exports com.quorum.tessera.serviceloader;

  uses com.quorum.tessera.io.FilesDelegate;
  uses com.quorum.tessera.version.ApiVersion;

  provides java.nio.file.spi.FileSystemProvider with
      com.quorum.tessera.nio.unix.UnixSocketFileSystemProvider;
  provides com.quorum.tessera.version.ApiVersion with
      com.quorum.tessera.version.BaseVersion,
      com.quorum.tessera.version.EnhancedPrivacyVersion,
      com.quorum.tessera.version.MultiTenancyVersion,
      com.quorum.tessera.version.PrivacyGroupVersion,
      com.quorum.tessera.version.MandatoryRecipientsVersion,
      com.quorum.tessera.version.CBORSupportVersion;
}
