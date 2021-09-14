package com.quorum.tessera.q2t;

import static java.util.stream.Collectors.toSet;

import com.quorum.tessera.api.common.RawTransactionResource;
import com.quorum.tessera.api.common.UpCheckResource;
import com.quorum.tessera.app.TesseraRestApplication;
import com.quorum.tessera.config.AppType;
import com.quorum.tessera.config.ClientMode;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.ConfigFactory;
import com.quorum.tessera.privacygroup.PrivacyGroupManager;
import com.quorum.tessera.transaction.EncodedPayloadManager;
import com.quorum.tessera.transaction.TransactionManager;
import jakarta.ws.rs.ApplicationPath;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

/**
 * The main application that is submitted to the HTTP server Contains all the service classes
 * created by the service locator
 */
@ApplicationPath("/")
public class Q2TRestApp extends TesseraRestApplication
    implements com.quorum.tessera.config.apps.TesseraApp {

  private final TransactionManager transactionManager;

  private final EncodedPayloadManager encodedPayloadManager;

  private final PrivacyGroupManager privacyGroupManager;

  protected Q2TRestApp(
      TransactionManager transactionManager,
      EncodedPayloadManager encodedPayloadManager,
      PrivacyGroupManager privacyGroupManager) {
    this.transactionManager = Objects.requireNonNull(transactionManager);
    this.encodedPayloadManager = Objects.requireNonNull(encodedPayloadManager);
    this.privacyGroupManager = Objects.requireNonNull(privacyGroupManager);
  }

  public Q2TRestApp() {
    this(TransactionManager.create(), EncodedPayloadManager.create(), PrivacyGroupManager.create());
  }

  @Override
  public Set<Object> getSingletons() {
    TransactionResource transactionResource =
        new TransactionResource(transactionManager, privacyGroupManager);
    TransactionResource3 transactionResource3 =
        new TransactionResource3(transactionManager, privacyGroupManager);
    TransactionResource4 transactionResource4 =
        new TransactionResource4(transactionManager, privacyGroupManager);

    RawTransactionResource rawTransactionResource = new RawTransactionResource(transactionManager);
    EncodedPayloadResource encodedPayloadResource =
        new EncodedPayloadResource(encodedPayloadManager, transactionManager);
    final UpCheckResource upCheckResource = new UpCheckResource();

    final PrivacyGroupResource privacyGroupResource = new PrivacyGroupResource(privacyGroupManager);

    final Config config = ConfigFactory.create().getConfig();
    if (config.getClientMode() == ClientMode.ORION) {
      final BesuTransactionResource besuResource =
          new BesuTransactionResource(transactionManager, privacyGroupManager);
      return Set.of(besuResource, rawTransactionResource, privacyGroupResource, upCheckResource);
    }

    return Set.of(
        transactionResource,
        rawTransactionResource,
        encodedPayloadResource,
        privacyGroupResource,
        upCheckResource,
        transactionResource3,
        transactionResource4);
  }

  @Override
  public Set<Class<?>> getClasses() {
    return Stream.concat(super.getClasses().stream(), Stream.of(Q2TApiResource.class))
        .collect(toSet());
  }

  @Override
  public AppType getAppType() {
    return AppType.Q2T;
  }
}
