package transaction.whitelist;

import static org.assertj.core.api.Assertions.assertThat;

import com.quorum.tessera.config.*;
import com.quorum.tessera.config.util.JaxbUtil;
import com.quorum.tessera.test.DBType;
import com.quorum.tessera.test.PartyHelper;
import config.ConfigBuilder;
import exec.ExecArgsBuilder;
import exec.ExecUtils;
import io.cucumber.java8.En;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.core.Response;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import suite.*;

public class WhitelistSteps implements En {

  private static final Logger LOGGER = LoggerFactory.getLogger(WhitelistSteps.class);

  private final URL logbackConfigFile = WhitelistSteps.class.getResource("/logback-node.xml");

  private static final int P2P_PORT = 7070;

  private static final int Q2T_PORT = 7001;

  public WhitelistSteps() {

    try {
      final Path pid = Files.createTempFile("whitelist", ".pid");

      ExecutorService executorService = Executors.newCachedThreadPool();

      Given(
          "Node at port {int}",
          (Integer port) -> {
            ExecutionContext executionContext =
                ExecutionContext.Builder.create()
                    .with(CommunicationType.REST)
                    .with(DBType.H2)
                    .with(EnclaveType.LOCAL)
                    .with(SocketType.HTTP)
                    .with(EncryptorType.NACL)
                    .build();

            ConfigBuilder whiteListConfigBuilder =
                new ConfigBuilder()
                    .withNodeId("whitelist")
                    .withNodeNumber(5)
                    .withQ2TSocketType(SocketType.HTTP)
                    .withQt2Port(Q2T_PORT)
                    .withExecutionContext(executionContext)
                    .withP2pPort(port)
                    .withEncryptorConfig(
                        new EncryptorConfig() {
                          {
                            setType(EncryptorType.NACL);
                          }
                        })
                    .withKeys(
                        "WxsJ4souK0mptNx1UGw6hb1WNNIbPhLPvW9GoaXau3Q=",
                        "YbOOFA4mwSSdGH6aFfGl2M7N1aiPOj5nHpD7GzJKSiA=");

            Config whiteListConfig = whiteListConfigBuilder.build();
            whiteListConfig.setUseWhiteList(true);

            Path configFile =
                Paths.get(System.getProperty("java.io.tmpdir")).resolve("white-list-config.json");

            try (OutputStream out = Files.newOutputStream(configFile)) {
              JaxbUtil.marshalWithNoValidation(whiteListConfig, out);
            } catch (IOException ex) {
              throw new UncheckedIOException(ex);
            }

            final String appPath = System.getProperty("application.jar");

            if (Objects.equals("", appPath)) {
              throw new IllegalStateException("No application.jar system property defined");
            }

            final Path startScript = Paths.get(appPath);
            List<String> cmd =
                new ExecArgsBuilder()
                    .withStartScript(startScript)
                    .withConfigFile(configFile)
                    .withJvmArg("-Dlogback.configurationFile=" + logbackConfigFile)
                    .withJvmArg("-Dnode.number=whitelist")
                    .withPidFile(pid)
                    .build();

            ProcessBuilder processBuilder = new ProcessBuilder(cmd);
            processBuilder.redirectErrorStream(true);

            Process process = processBuilder.start();

            executorService.submit(
                () -> {
                  try (BufferedReader reader =
                      Stream.of(process.getInputStream())
                          .map(InputStreamReader::new)
                          .map(BufferedReader::new)
                          .findAny()
                          .get()) {

                    String line;
                    while ((line = reader.readLine()) != null) {
                      LOGGER.info("Exec line Whitelist : {}", line);
                    }

                  } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                  }
                });

            executorService.submit(
                () -> {
                  try {
                    process.waitFor();
                  } catch (InterruptedException ex) {

                  }
                });

            ServerStatusCheck serverStatusCheck =
                ServerStatusCheck.create(
                    whiteListConfig.getServerConfigs().stream()
                        .filter(s -> s.getApp() == AppType.P2P)
                        .findAny()
                        .get());

            Boolean started =
                executorService
                    .submit(new ServerStatusCheckExecutor(serverStatusCheck))
                    .get(60, TimeUnit.SECONDS);

            assertThat(started).isTrue();
          });

      List<Response> responseHolder = new ArrayList<>();
      When(
          "a request is made against the node",
          () -> {
            Client client =
                PartyHelper.create()
                    .getParties()
                    .filter(p -> p.getP2PUri().getPort() != P2P_PORT)
                    .findAny()
                    .get()
                    .getRestClient();

            Response response =
                client.target("http://localhost:" + P2P_PORT).path("upcheck").request().get();

            responseHolder.add(response);
          });

      Then(
          "the response code is UNAUTHORIZED",
          () -> assertThat(responseHolder.get(0).getStatus()).isEqualTo(401));

      Then(
          "the node is stopped",
          () -> {
            ExecUtils.kill(pid);
          });

    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }
}
