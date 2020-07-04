package com.quorum.tessera.launcher;

import com.quorum.tessera.cli.CliDelegate;
import com.quorum.tessera.cli.CliException;
import com.quorum.tessera.cli.CliResult;
import com.quorum.tessera.cli.CliType;
import com.quorum.tessera.config.AppType;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.ConfigException;
import com.quorum.tessera.config.apps.TesseraAppFactory;
import com.quorum.tessera.config.cli.PicoCliDelegate;
import com.quorum.tessera.context.RuntimeContext;
import com.quorum.tessera.context.RuntimeContextFactory;
import com.quorum.tessera.partyinfo.PartyInfoService;
import com.quorum.tessera.partyinfo.PartyInfoServiceFactory;
import com.quorum.tessera.server.TesseraServer;
import com.quorum.tessera.server.TesseraServerFactory;
import com.quorum.tessera.enclave.EnclaveFactory;
import com.quorum.tessera.transaction.TransactionManagerFactory;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.JsonException;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
/** The main entry point for the application. This just starts up the application in the embedded container. */
public class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main(final String... args) throws Exception {
        System.setProperty(CliType.CLI_TYPE_KEY, CliType.CONFIG.name());
        System.setProperty("javax.xml.bind.JAXBContextFactory", "org.eclipse.persistence.jaxb.JAXBContextFactory");
        System.setProperty("javax.xml.bind.context.factory", "org.eclipse.persistence.jaxb.JAXBContextFactory");

        try {

            PicoCliDelegate picoCliDelegate = new PicoCliDelegate();
            LOGGER.debug("Execute PicoCliDelegate with args [{}]", String.join(",", args));
            final CliResult cliResult = picoCliDelegate.execute(args);
            LOGGER.debug("Executed PicoCliDelegate with args [{}].", String.join(",", args));
            CliDelegate.instance().setConfig(cliResult.getConfig().orElse(null));

            if (cliResult.isSuppressStartup()) {
                System.exit(0);
            }

            if (cliResult.getStatus() != 0) {
                System.exit(cliResult.getStatus());
            }

            final Config config =
                    cliResult
                            .getConfig()
                            .orElseThrow(() -> new NoSuchElementException("No config found. Tessera will not run."));

            RuntimeContext runtimeContext = RuntimeContextFactory.newFactory().create(config);

            EnclaveFactory enclaveFactory = EnclaveFactory.create();
            enclaveFactory.create(config);

            PartyInfoService partyInfoService = PartyInfoServiceFactory.create().create(config);
            partyInfoService.populateStore();


            TransactionManagerFactory.create().create(config);

            ApplicationContext springContext = new ClassPathXmlApplicationContext("tessera-spring.xml");


            runWebServer(config);

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
            final Throwable cause = ExceptionUtils.getRootCause(ex);

            if (JsonException.class.isInstance(cause)) {
                System.err.println("ERROR: Invalid json, cause is " + cause.getMessage());
            } else {
                System.err.println("ERROR: Configuration exception, cause is " + Objects.toString(cause));
            }
            System.exit(3);
        } catch (final CliException ex) {
            System.err.println("ERROR: CLI exception, cause is " + ex.getMessage());
            System.exit(4);
        } catch (final ServiceConfigurationError ex) {
            Optional<Throwable> e = Optional.of(ex);

            e.map(Throwable::getMessage).ifPresent(System.err::println);

            // get root cause
            while (e.map(Throwable::getCause).isPresent()) {
                e = e.map(Throwable::getCause);
            }

            e.map(Throwable::toString).ifPresent(System.err::println);

            System.exit(5);
        } catch (final Throwable ex) {
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

    private static void runWebServer(final Config config) throws Exception {

        final List<TesseraServer> servers =
                config.getServerConfigs().stream()
                        .filter(server -> !AppType.ENCLAVE.equals(server.getApp()))
                        .map(
                                conf -> {
                                    Object app =
                                            TesseraAppFactory.create(conf.getCommunicationType(), conf.getApp())
                                                    .orElseThrow(
                                                            () ->
                                                                    new IllegalStateException(
                                                                            "Cant create app for " + conf.getApp()));

                                    return TesseraServerFactory.create(conf.getCommunicationType())
                                            .createServer(conf, Collections.singleton(app));
                                })
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());

        Runtime.getRuntime()
                .addShutdownHook(
                        new Thread(
                                () -> {
                                    try {
                                        for (TesseraServer ts : servers) {
                                            ts.stop();
                                        }
                                    } catch (Exception ex) {
                                        LOGGER.error(null, ex);
                                    }
                                }));

        for (TesseraServer ts : servers) {
            ts.start();
        }
    }
}
