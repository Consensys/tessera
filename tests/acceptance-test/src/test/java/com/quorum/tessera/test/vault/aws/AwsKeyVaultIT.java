package com.quorum.tessera.test.vault.aws;

import static org.assertj.core.api.Assertions.assertThat;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.ServerConfig;
import com.quorum.tessera.config.util.JaxbUtil;
import com.quorum.tessera.ssl.context.SSLContextBuilder;
import com.quorum.tessera.test.util.ElUtil;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.HttpsServer;
import config.PortUtil;
import exec.ExecArgsBuilder;
import exec.ExecUtils;
import exec.StreamConsumer;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.net.ssl.SSLContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AwsKeyVaultIT {

  private static final Logger LOGGER = LoggerFactory.getLogger(AwsKeyVaultIT.class);

  private HttpsServer httpsServer;

  private final int keyVaultPort = new PortUtil(8081).nextPort();

  private URL keystore;

  private Path truststore;

  private final String keyVaultUrl = String.format("https://localhost:%d", keyVaultPort);

  private URL logbackConfigFile;

  private SSLContext sslContext;

  private AwsKeyVaultHttpHandler httpHandler;

  private ExecutorService executorService = Executors.newCachedThreadPool();

  private Path startScript =
      Optional.of("keyvault.aws.dist").map(System::getProperty).map(Paths::get).get();

  private final Path distDirectory =
      Optional.of("keyvault.aws.dist").map(System::getProperty).map(Paths::get).get().resolve("*");

  private Path pid;

  @Before
  public void beforeTest() throws Exception {
    pid =
        Paths.get(
            System.getProperty("java.io.tmpdir"),
            String.format("%s.pid", UUID.randomUUID().toString().replaceAll("-", "")));

    logbackConfigFile = getClass().getResource("/logback-node.xml");

    keystore = getClass().getResource("/certificates/server-localhost-with-san.jks");
    truststore = Paths.get(getClass().getResource("/certificates/truststore.jks").toURI());
    sslContext =
        SSLContextBuilder.createBuilder(
                "localhost",
                Paths.get(keystore.toURI()),
                "testtest".toCharArray(),
                truststore,
                "testtest".toCharArray())
            .forAllCertificates()
            .build();

    httpsServer = HttpsServer.create(new InetSocketAddress(keyVaultPort), 0);
    httpsServer.setHttpsConfigurator(
        new HttpsConfigurator(sslContext) {

          @Override
          public void configure(HttpsParameters params) {
            params.setWantClientAuth(false);
            params.setNeedClientAuth(false);
          }
        });

    httpsServer.createContext(
        "/ping",
        exchange -> {
          byte[] greeting =
              Json.createObjectBuilder()
                  .add("salutation", "SALUTATIONS")
                  .build()
                  .toString()
                  .getBytes();

          exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, greeting.length);
          exchange.getResponseBody().write(greeting);
          exchange.close();
        });

    httpHandler = new AwsKeyVaultHttpHandler();
    httpsServer.createContext("/", httpHandler);
    httpsServer.start();

    final HttpClient httpClient = HttpClient.newBuilder().sslContext(sslContext).build();

    final HttpRequest request =
        HttpRequest.newBuilder().uri(URI.create(keyVaultUrl.concat("/ping"))).GET().build();

    final HttpResponse<String> response =
        httpClient.send(request, HttpResponse.BodyHandlers.ofString());

    assertThat(response.statusCode()).isEqualTo(200);
    assertThat(response.body()).contains("SALUTATIONS");

    assertThat(httpHandler.getCounter()).isZero();
  }

  @After
  public void afterTest() throws Exception {
    ExecUtils.kill(pid);

    executorService.shutdown();
    httpsServer.stop(0);
  }

  @Test
  public void tesseraStartupRequestsKeysWhosIdsAreConfigured() throws Exception {

    Map<String, Object> params = Map.of("awsSecretsManagerEndpoint", keyVaultUrl);
    Path tempTesseraConfig =
        ElUtil.createTempFileFromTemplate(
            getClass().getResource("/vault/tessera-aws-config.json"), params);

    List<String> args =
        new ExecArgsBuilder()
            .withStartScript(startScript)
            .withClassPathItem(distDirectory)
            .withArg("-configfile", tempTesseraConfig.toString())
            .withArg("-pidfile", pid.toAbsolutePath().toString())
            .withArg("-jdbc.autoCreateTables", "true")
            .build();

    ProcessBuilder processBuilder = new ProcessBuilder(args);
    processBuilder.environment().putAll(env());
    processBuilder.redirectErrorStream(true);
    Process process = processBuilder.start();
    executorService.submit(new StreamConsumer(process.getInputStream(), LOGGER::info));

    executorService.submit(
        () -> {
          int exitCode = process.waitFor();
          assertThat(exitCode)
              .describedAs("Tessera node exited with code %d", exitCode)
              .isEqualTo(0);
          return null;
        });

    final Config config = JaxbUtil.unmarshal(Files.newInputStream(tempTesseraConfig), Config.class);
    final URI bindingUrl =
        Optional.of(config)
            .map(Config::getP2PServerConfig)
            .map(ServerConfig::getBindingUri)
            .map(UriBuilder::fromUri)
            .map(u -> u.path("upcheck"))
            .map(UriBuilder::build)
            .get();

    HttpClient httpClient = HttpClient.newHttpClient();
    final HttpRequest request = HttpRequest.newBuilder().uri(bindingUrl).GET().build();

    CountDownLatch startUpLatch = new CountDownLatch(1);

    executorService.submit(
        () -> {
          while (true) {
            try {

              HttpResponse<String> response =
                  httpClient.send(request, HttpResponse.BodyHandlers.ofString());
              if (response.statusCode() == 200) {
                startUpLatch.countDown();
              }

            } catch (InterruptedException | IOException e) {
            }
          }
        });

    assertThat(startUpLatch.await(2, TimeUnit.MINUTES)).isTrue();

    assertThat(httpHandler.getCounter()).isEqualTo(2);

    List<JsonObject> requests = httpHandler.getRequests().get("secretsmanager.GetSecretValue");
    assertThat(requests).hasSize(2);

    List<String> secretIds =
        requests.stream().map(j -> j.getString("SecretId")).collect(Collectors.toList());

    assertThat(secretIds).containsExactlyInAnyOrder("secretIdPub", "secretIdKey");
  }

  @Test
  public void keyGenerationRequestCreateSecretCallToAws() throws Exception {

    List<String> nodesToGenerateKeysFor = List.of("nodeA", "nodeB", "nodeC");

    final List<String> args =
        new ExecArgsBuilder()
            .withStartScript(startScript)
            .withClassPathItem(distDirectory)
            .withArg("-keygen")
            .withArg("-keygenvaulttype", "AWS")
            .withArg("-filename", String.join(",", nodesToGenerateKeysFor))
            .withArg("-keygenvaulturl", keyVaultUrl)
            .build();

    ProcessBuilder processBuilder = new ProcessBuilder(args);
    processBuilder.environment().putAll(env());
    processBuilder.redirectErrorStream(false);
    Process process = processBuilder.start();
    executorService.submit(new StreamConsumer(process.getInputStream(), LOGGER::info));
    executorService.submit(new StreamConsumer(process.getErrorStream(), LOGGER::error));

    process.waitFor();
    assertThat(process.exitValue()).isZero();

    final String apiTarget = "secretsmanager.CreateSecret";

    assertThat(httpHandler.getRequests()).containsOnlyKeys(apiTarget);
    assertThat(httpHandler.getRequests().get(apiTarget)).hasSize(6);

    List<JsonObject> requests = httpHandler.getRequests().get(apiTarget);
    List<String> expectedNames =
        nodesToGenerateKeysFor.stream()
            .flatMap(n -> Stream.of(n.concat("Pub"), n.concat("Key")))
            .collect(Collectors.toList());

    assertThat(requests.stream().map(j -> j.getString("Name")).collect(Collectors.toList()))
        .containsExactlyInAnyOrderElementsOf(expectedNames);
  }

  private Map<String, String> env() {
    return Map.of("AWS_REGION", "us-east-1", "JAVA_OPTS", String.join(" ", jvmArgs()));
  }

  private List<String> jvmArgs() {

    return List.of(
        "-Djavax.net.ssl.trustStore=" + truststore.toAbsolutePath().toString(),
        "-Djavax.net.ssl.trustStorePassword=testtest",
        "-Dlogback.configurationFile=" + logbackConfigFile.getFile(),
        "-Daws.region=a-region",
        "-Daws.accessKeyId=an-id",
        "-Daws.secretAccessKey=a-key",
        "-Dnode.number=aws");
  }
}
