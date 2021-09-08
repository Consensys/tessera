package com.quorum.tessera.test.vault.azure;

import static com.quorum.tessera.config.util.EnvironmentVariables.AZURE_CLIENT_ID;
import static com.quorum.tessera.config.util.EnvironmentVariables.AZURE_CLIENT_SECRET;
import static org.assertj.core.api.Assertions.assertThat;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.util.JaxbUtil;
import com.quorum.tessera.ssl.context.SSLContextBuilder;
import com.quorum.tessera.test.util.ElUtil;
import com.sun.net.httpserver.*;
import config.PortUtil;
import exec.ExecArgsBuilder;
import exec.ExecUtils;
import exec.StreamConsumer;
import jakarta.json.Json;
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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class AzureKeyVaultIT {
  private final int keyVaultPort = new PortUtil(8081).nextPort();

  private final String keyVaultUrl = String.format("https://localhost:%d", keyVaultPort);

  private HttpsServer httpsServer;

  private SSLContext sslContext;

  private ExecutorService executorService = Executors.newCachedThreadPool();

  private URL keystore;

  private Path truststore;

  private URL logbackConfigFile;

  private Path startScript =
      Optional.of("keyvault.azure.dist").map(System::getProperty).map(Paths::get).get();

  private final Path distDirectory =
      Optional.of("keyvault.azure.dist")
          .map(System::getProperty)
          .map(Paths::get)
          .get()
          .resolve("*");

  private Path pid;

  private AzureKeyVaultHttpHandler httpHandler;

  @Before
  public void beforeTest() throws Exception {

    pid = Paths.get(System.getProperty("java.io.tmpdir"), "pidA.pid");

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
    sslContext.getDefaultSSLParameters().setNeedClientAuth(false);
    sslContext.getDefaultSSLParameters().setWantClientAuth(false);
    httpsServer = HttpsServer.create(new InetSocketAddress(keyVaultPort), 0);
    httpsServer.setHttpsConfigurator(new HttpsConfigurator(sslContext));

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

    httpHandler = new AzureKeyVaultHttpHandler(keyVaultUrl);
    httpsServer
        .createContext("/", httpHandler)
        .setAuthenticator(
            new Authenticator() {
              @Override
              public Result authenticate(HttpExchange exch) {

                HttpPrincipal httpPrincipal = new HttpPrincipal("my-client-id", "my-client-secret");

                return new Success(httpPrincipal);
              }
            });
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
  public void doStuff() throws Exception {

    Map<String, Object> params = Map.of("azureKeyVaultUrl", keyVaultUrl);

    Path tempTesseraConfig =
        ElUtil.createTempFileFromTemplate(
            getClass().getResource("/vault/tessera-azure-config.json"), params);

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
    executorService.submit(new StreamConsumer(process.getInputStream()));

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
        UriBuilder.fromUri(config.getP2PServerConfig().getBindingUri()).path("upcheck").build();

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

    assertThat(startUpLatch.await(30, TimeUnit.SECONDS)).isTrue();

    assertThat(httpHandler.getCounter()).isEqualTo(2);
  }

  private Map<String, String> env() {
    return Map.of(
        AZURE_CLIENT_ID,
        "my-client-id",
        AZURE_CLIENT_SECRET,
        "my-client-secret",
        "AZURE_TENANT_ID",
        "my-tenant-id",
        "JAVA_OPTS",
        String.join(" ", jvmArgs()));
  }

  private List<String> jvmArgs() {

    return List.of(
        "-Djavax.net.ssl.trustStore=" + truststore.toAbsolutePath().toString(),
        "-Djavax.net.ssl.trustStorePassword=testtest",
        "-Dlogback.configurationFile=" + logbackConfigFile.getFile(),
        "-Dnode.number=azure");
  }
}
