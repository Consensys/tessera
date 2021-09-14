package com.quorum.tessera.launcher;

import com.quorum.tessera.cli.CliException;
import com.quorum.tessera.cli.CliResult;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.ConfigException;
import com.quorum.tessera.config.ConfigFactory;
import com.quorum.tessera.config.cli.PicoCliDelegate;
import com.quorum.tessera.context.RuntimeContext;
import com.quorum.tessera.discovery.Discovery;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.privacygroup.ResidentGroupHandler;
import com.quorum.tessera.recovery.workflow.BatchResendManager;
import com.quorum.tessera.transaction.EncodedPayloadManager;
import com.quorum.tessera.transaction.TransactionManager;
import jakarta.json.JsonException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.security.Security;
import java.util.*;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The main entry point for the application. This just starts up the application in the embedded
 * container.
 */
public class Main {

  private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

  public static void main(final String... args) throws Exception {
    Security.addProvider(new BouncyCastleProvider());
    LOGGER.debug("args [{}]", String.join(",", args));
    try {

      PicoCliDelegate picoCliDelegate = new PicoCliDelegate();
      LOGGER.debug("Execute PicoCliDelegate with args [{}]", String.join(",", args));
      final CliResult cliResult = picoCliDelegate.execute(args);
      LOGGER.debug("Executed PicoCliDelegate with args [{}].", String.join(",", args));

      if (cliResult.isSuppressStartup()) {
        System.exit(0);
      }

      if (cliResult.getStatus() != 0) {
        System.exit(cliResult.getStatus());
      }

      final Config config =
          cliResult
              .getConfig()
              .orElseThrow(
                  () -> new NoSuchElementException("No config found. Tessera will not run."));

      // Start legacy spring profile stuff
      final String springProfileWarning =
          "Warn: Spring profiles will not be supported in future. To start in recover mode use 'tessera recover'";
      if (System.getProperties().containsKey("spring.profiles.active")) {
        System.out.println(springProfileWarning);
        config.setRecoveryMode(
            System.getProperty("spring.profiles.active").contains("enable-sync-poller"));
      } else if (System.getenv().containsKey("SPRING_PROFILES_ACTIVE")) {
        System.out.println(springProfileWarning);
        config.setRecoveryMode(
            System.getenv("SPRING_PROFILES_ACTIVE").contains("enable-sync-poller"));
      }
      // end spring profile stuff
      LOGGER.debug("Storing config {}", config);
      ConfigFactory.create().store(config);
      LOGGER.debug("Stored config {}", config);

      LOGGER.debug("Creating enclave");
      final Enclave enclave = Enclave.create();
      LOGGER.debug("Created enclave {}", enclave);

      LOGGER.debug("Creating RuntimeContext");
      final RuntimeContext runtimeContext = RuntimeContext.getInstance();
      LOGGER.debug("Created RuntimeContext {}", runtimeContext);

      LOGGER.debug("Creating Discovery");
      Discovery discovery = Discovery.create();
      discovery.onCreate();
      LOGGER.debug("Created Discovery {}", discovery);

      if (runtimeContext.isMultiplePrivateStates()) {
        LOGGER.debug("Creating ResidentGroupHandler");
        ResidentGroupHandler residentGroupHandler = ResidentGroupHandler.create();
        residentGroupHandler.onCreate(config);
        LOGGER.debug("Created ResidentGroupHandler {}", residentGroupHandler);
      }

      LOGGER.debug("Creating EncodedPayloadManager");
      EncodedPayloadManager.create();
      LOGGER.debug("Created EncodedPayloadManager");

      LOGGER.debug("Creating BatchResendManager");
      BatchResendManager.create();
      LOGGER.debug("Created BatchResendManager");

      LOGGER.debug("Creating txn manager");
      TransactionManager transactionManager = TransactionManager.create();
      LOGGER.debug("Created txn manager");

      LOGGER.debug("Validating if transaction table exists");
      if (!transactionManager.upcheck()) {
        throw new RuntimeException(
            "The database has not been setup correctly. Please ensure transaction tables "
                + "are present and correct");
      }

      LOGGER.debug("Creating ScheduledServiceFactory");
      ScheduledServiceFactory scheduledServiceFactory = ScheduledServiceFactory.fromConfig(config);
      scheduledServiceFactory.build();
      LOGGER.debug("Created ScheduledServiceFactory");

      LOGGER.debug("Creating Launcher");
      Launcher.create(runtimeContext.isRecoveryMode()).launchServer(config);
      LOGGER.debug("Created Launcher");
    } catch (final ConstraintViolationException ex) {
      for (final ConstraintViolation<?> violation : ex.getConstraintViolations()) {
        System.err.println(
            "ERROR: Config validation issue: "
                + violation.getPropertyPath()
                + " "
                + violation.getMessage());
      }
      System.exit(1);
    } catch (final ConfigException ex) {
      LOGGER.debug("", ex);
      final Throwable cause = ExceptionUtils.getRootCause(ex);

      if (JsonException.class.isInstance(cause)) {
        System.err.println("ERROR: Invalid json, cause is " + cause.getMessage());
      } else {
        System.err.println("ERROR: Configuration exception, cause is " + Objects.toString(cause));
      }
      System.exit(3);
    } catch (final CliException ex) {
      LOGGER.debug("", ex);
      System.err.println("ERROR: CLI exception, cause is " + ex.getMessage());
      System.exit(4);
    } catch (final ServiceConfigurationError ex) {
      LOGGER.debug("", ex);
      Optional<Throwable> e = Optional.of(ex);

      e.map(Throwable::getMessage).ifPresent(System.err::println);

      // get root cause
      while (e.map(Throwable::getCause).isPresent()) {
        e = e.map(Throwable::getCause);
      }

      e.map(Throwable::toString).ifPresent(System.err::println);

      System.exit(5);
    } catch (final Throwable ex) {
      LOGGER.debug(null, ex);
      if (Arrays.asList(args).contains("--debug")) {
        ex.printStackTrace();
      } else {
        if (Optional.ofNullable(ex.getMessage()).isPresent()) {
          System.err.println("ERROR: Cause is " + ex.getMessage());
        } else {
          System.err.println("ERROR: In class " + ex.getClass().getSimpleName());
        }
      }

      System.exit(2);
    }
  }
}
