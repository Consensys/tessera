package com.quorum.tessera.test.vault.azure;

import static com.quorum.tessera.config.util.EnvironmentVariables.AZURE_CLIENT_ID;
import static com.quorum.tessera.config.util.EnvironmentVariables.AZURE_CLIENT_SECRET;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.util.JaxbUtil;
import com.quorum.tessera.ssl.context.SSLContextBuilder;
import com.quorum.tessera.test.util.ElUtil;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsServer;
import config.PortUtil;
import exec.ExecArgsBuilder;
import exec.NodeExecManager;
import io.cucumber.java8.En;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.ws.rs.core.UriBuilder;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
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
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;
import javax.net.ssl.SSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AzureStepDefs implements En {

  private final int azureKeyVaultPort = new PortUtil(8081).nextPort();

  private final String azureKeyVaultUrl = String.format("https://localhost:%d", azureKeyVaultPort);

  private static final Logger LOGGER = LoggerFactory.getLogger(AzureStepDefs.class);

  private final ExecutorService executorService = Executors.newCachedThreadPool();
  private final AtomicReference<Process> tesseraProcess = new AtomicReference<>();
  private final AtomicReference<HttpServer> azureKeyVaultServerHolder = new AtomicReference<>();

  private final String publicKey = "BULeR8JyUWhiuuCMU/HLA0Q5pzkYT+cHII3ZKBey3Bo=";
  private final String privateKey = "Wl+xSyXVuuqzpvznOS7dOobhcn4C5auxkFRi7yLtgtA=";

  private final String authUrl = "/auth/oauth2/token";

  // /secrets/{name}/{version}
  // note: '/' must be provided at start of version arg, e.g. "/1". this is to allow urls with no
  // version by setting
  // version arg to "".
  private final String urlFormat = "/secrets/%s%s";

  private final String publicKeyUrl =
      String.format(urlFormat, "Pub", "/bvfw05z4cbu11ra2g94e43v9xxewqdq7");
  private final String privateKeyUrl =
      String.format(urlFormat, "Key", "/0my1ora2dciijx5jq9gv07sauzs5wjo2");

  private final String nodeAPubUrl = String.format(urlFormat, "nodeAPub", "");
  private final String nodeAKeyUrl = String.format(urlFormat, "nodeAKey", "");
  private final String nodeBPubUrl = String.format(urlFormat, "nodeBPub", "");
  private final String nodeBKeyUrl = String.format(urlFormat, "nodeBKey", "");

  SSLContext sslContext() throws Exception {
    final URL keystore = getClass().getResource("/certificates/server-localhost-with-san.jks");

    final Path truststore =
        Paths.get(getClass().getResource("/certificates/truststore.jks").toURI());

    /*
    “sslConfig”: {                              // Config required if InfluxDB server is using TLS
            “tls”: “STRICT”,
            “sslConfigType”: “CLIENT_ONLY”,
            “clientTrustMode”: “CA”,
            “clientTrustStore”: “/path/to/truststore.jks”,
            “clientTrustStorePassword”: “password”,
            “clientKeyStore”: “path/to/truststore.jks”,
            “clientKeyStorePassword”: “password”
        }
     */

    return SSLContextBuilder.createBuilder(
            "localhost",
            Paths.get(keystore.toURI()),
            "testtest".toCharArray(),
            truststore,
            "testtest".toCharArray())
        .forAllCertificates()
        .build();
  }

  public AzureStepDefs() throws Exception {
    final SSLContext sslContext = sslContext();
    After(
        () -> {
          azureKeyVaultServerHolder.get().stop(0);

          if (tesseraProcess.get() != null && tesseraProcess.get().isAlive()) {
            tesseraProcess.get().destroy();
            System.out.println("Stopped Tessera node...");
          }
        });

    Given(
        "^the mock AKV server has been started$",
        () -> {
          final InetSocketAddress inetAddress = new InetSocketAddress(azureKeyVaultPort);
          final HttpsServer httpServer = HttpsServer.create(inetAddress, 0);

          final HttpsConfigurator httpsConfigurator = new HttpsConfigurator(sslContext);
          httpServer.setHttpsConfigurator(httpsConfigurator);

          httpServer.createContext(
              "/",
              exchange -> {
                LOGGER.info("Handle path : {}", exchange.getRequestURI());

                byte[] greeting = "SALUTATIONS".getBytes();

                exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, greeting.length);
                exchange.getResponseBody().write(greeting);
                exchange.close();
              });

          azureKeyVaultServerHolder.set(httpServer);
          httpServer.start();

          final HttpClient httpClient = HttpClient.newBuilder().sslContext(sslContext).build();

          final HttpRequest request =
              HttpRequest.newBuilder().uri(URI.create(azureKeyVaultUrl)).GET().build();

          final HttpResponse<String> response =
              httpClient.send(request, HttpResponse.BodyHandlers.ofString());

          assertThat(response.statusCode()).isEqualTo(200);
          assertThat(response.body()).isEqualTo("SALUTATIONS");

          final HttpRequest request2 =
              HttpRequest.newBuilder().uri(URI.create(azureKeyVaultUrl + "/foo")).GET().build();

          final HttpResponse<String> response2 =
              httpClient.send(request2, HttpResponse.BodyHandlers.ofString());
          assertThat(response2.statusCode()).isEqualTo(200);
          assertThat(response2.body()).isEqualTo("SALUTATIONS");
        });

    Given(
        "^the mock AKV server has stubs for the endpoints used to get secrets$",
        () -> {
          // --- Authentication flow for the AKV client ---
          // The AKV Client makes a request to GET a secret from the AKV.  If the response is 200
          // Success then
          // no authentication is required and the secret is returned to the caller.  If the
          // response is 401
          // Unauthorized then the client proceeds to authenticate itself.  Any other response code
          // results in
          // error being returned and termination of the GET request.
          // See https://docs.microsoft.com/en-us/rest/api/keyvault/getsecret/getsecret for the AKV
          // HTTP
          // GetSecret API docs
          //
          // The 401 Unauthorized response will contain a WWW-Authenticate header which the AKV
          // Client uses to
          // authenticate itself.
          // The 'authorization' component of the header provides the url of the authorization
          // server to be
          // used.
          // The 'resource' component provides the Azure resource (i.e. key vault) to request
          // authentication
          // for.
          //
          // To authenticate, the client POSTs to the {authorization}/oauth2/token endpoint, where
          // {authorization}
          // is the authorization server url provided in the WWW-Authenticate header.
          // See
          // https://docs.microsoft.com/en-us/azure/active-directory/develop/v1-protocols-oauth-code#use-the-authorization-code-to-request-an-access-token
          // for more info.
          //
          // The WWW-Authenticate header is used in subsequent GETs to the same Azure resource to
          // make
          // authentication requests immediately instead of having to first make 401 GETs.

          final String respFormat = "{ \"value\": \"%s\" }";

          final String authScenario = "AUTH";
          final String received401 = "RECEIVED_401";

          final String authenticateHeader =
              String.format(
                  "Bearer authorization=%s, resource=%s",
                  azureKeyVaultUrl + "/auth", azureKeyVaultUrl);

          final HttpClient httpClient = HttpClient.newBuilder().sslContext(sslContext).build();

          azureKeyVaultServerHolder
              .get()
              .createContext(
                  publicKeyUrl,
                  exchange -> {
                    LOGGER.info("handle publicKeyUrl {}", publicKeyUrl);
                    JsonObject jsonObject =
                        Json.createObjectBuilder().add("value", publicKey).build();

                    byte[] response = jsonObject.toString().getBytes();
                    exchange.getResponseHeaders().add("Content-type", "application/json");
                    exchange.sendResponseHeaders(200, 0);
                    exchange.getResponseBody().write(response);

                    LOGGER.info("response send  {}", new String(response));

                    exchange.close();
                  });

          azureKeyVaultServerHolder
              .get()
              .createContext(
                  privateKeyUrl,
                  exchange -> {
                    LOGGER.info("handle privateKeyUrl {}", privateKeyUrl);

                    byte[] privateKeyResponse =
                        Json.createObjectBuilder()
                            .add("value", privateKey)
                            .build()
                            .toString()
                            .getBytes();

                    exchange.getResponseHeaders().add("Content-type", "application/json");
                    exchange.sendResponseHeaders(200, 0);
                    exchange.getResponseBody().write(privateKeyResponse);
                    LOGGER.info("response send  {}", new String(privateKeyResponse));
                    exchange.close();
                  });

          //
          // azureKeyVaultServerHolder.get().createContext(authUrl).setAuthenticator(new
          // Authenticator() {
          //                    @Override
          //                    public Result authenticate(HttpExchange exchange) {
          //                        LOGGER.info("authenticate ");
          //                        final String response = Json.createObjectBuilder()
          //                            .add("access_token", "my-token")
          //                            .add("token_type", "Bearer")
          //                            .add("expires_in", "3600")
          //                            .add("expires_on", "1388444763")
          //                            .add("resource", "https://resource/")
          //                            .add("refresh_token", "some-val")
          //                            .add("id_token", "some-val")
          //                            .build()
          //                            .toString();
          //
          //                        LOGGER.info("responseData {} ", response);
          //
          //                        byte[] responseData = response.getBytes();
          //                        return IOCallback.execute(() -> {
          //
          //                            exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK,
          // responseData.length);
          //
          //                            exchange.getRequestHeaders().add("client-request-id",
          // "{{request.headers.client-request-id}}");
          //                            exchange.getResponseBody().write(responseData);
          //                            exchange.close();
          //
          //                            HttpPrincipal httpPrincipal = new HttpPrincipal("skinner",
          // "/");
          //
          //                            return new Success(httpPrincipal);
          //                        });
          //
          //                    }
          //                });

          //
          //                wireMockServer
          //                    .get()
          //                    .stubFor(
          //                        get(urlPathEqualTo(publicKeyUrl))
          //                            .inScenario(authScenario)
          //                            .whenScenarioStateIs(Scenario.STARTED)
          //                            .willSetStateTo(received401)
          //                            .willReturn(
          //                                unauthorized().withHeader("WWW-Authenticate",
          // authenticateHeader)));
          //
          //                wireMockServer
          //                    .get()
          //                    .stubFor(
          //                        post(urlPathEqualTo(authUrl))
          //                            .willReturn(
          //                                okJson(
          //                                    "{ \"access_token\": \"my-token\", \"token_type\":
          // \"Bearer\", \"expires_in\": \"3600\", \"expires_on\": \"1388444763\", \"resource\":
          // \"https://resource/\", \"refresh_token\": \"some-val\", \"scope\": \"some-val\",
          // \"id_token\": \"some-val\"}")
          //                                    .withHeader(
          //                                        "client-request-id",
          //                                        "{{request.headers.client-request-id}}")
          //                                    .withTransformers("response-template")));
          //
          //                wireMockServer
          //                    .get()
          //                    .stubFor(
          //                        get(urlPathEqualTo(publicKeyUrl))
          //                            .inScenario(authScenario)
          //                            .whenScenarioStateIs(received401)
          //                            .willReturn(okJson(String.format(respFormat, publicKey))));
          //
          //                wireMockServer
          //                    .get()
          //                    .stubFor(
          //                        get(urlPathEqualTo(privateKeyUrl))
          //                            .willReturn(okJson(String.format(respFormat, privateKey))));
        });

    When(
        "^Tessera is started with the correct AKV environment variables$",
        () -> {
          Map<String, Object> params = new HashMap<>();
          params.put("azureKeyVaultUrl", azureKeyVaultUrl);

          Path tempTesseraConfig =
              ElUtil.createTempFileFromTemplate(
                  getClass().getResource("/vault/tessera-azure-config.json"), params);
          // tempTesseraConfig.toFile().deleteOnExit();

          // final String jarfile = System.getProperty("application.jar");

          final URL logbackConfigFile = NodeExecManager.class.getResource("/logback-node.xml");
          Path pid = Paths.get(System.getProperty("java.io.tmpdir"), "pidA.pid");

          final URL truststore = getClass().getResource("/certificates/truststore.jks");

          Path startScript =
              Optional.of("keyvault.azure.dist").map(System::getProperty).map(Paths::get).get();

          ExecArgsBuilder execArgsBuilder =
              new ExecArgsBuilder()
                  .withStartScript(startScript)
                  .withArg("-configfile", tempTesseraConfig.toString())
                  .withArg("-pidfile", pid.toAbsolutePath().toString())
                  .withArg("-jdbc.autoCreateTables", "true")
                  // .withClassPathItem(distDirectory.resolve("*"))
                  .withArg("--debug");

          final List<String> args = execArgsBuilder.build();

          final List<String> jvmArgs = new ArrayList<>();
          jvmArgs.add("-Djavax.net.ssl.trustStore=" + truststore.getFile());
          jvmArgs.add("-Djavax.net.ssl.trustStorePassword=testtest");
          jvmArgs.add("-Dspring.profiles.active=disable-unixsocket");
          jvmArgs.add("-Dlogback.configurationFile=" + logbackConfigFile.getFile());
          jvmArgs.add("-Ddebug=true");

          startTessera(args, jvmArgs, tempTesseraConfig);
        });

    Then(
        "^Tessera will retrieve the key pair from AKV$",
        () -> {
          //                wireMockServer.get().verify(2, postRequestedFor(urlEqualTo(authUrl)));
          //                wireMockServer.get().verify(3,
          // getRequestedFor(urlPathEqualTo(publicKeyUrl)));
          //                wireMockServer.get().verify(2,
          // getRequestedFor(urlPathEqualTo(privateKeyUrl)));

          final URL partyInfoUrl =
              UriBuilder.fromUri("http://localhost").port(8080).path("partyinfo").build().toURL();

          HttpURLConnection partyInfoUrlConnection =
              (HttpURLConnection) partyInfoUrl.openConnection();
          partyInfoUrlConnection.connect();

          int partyInfoResponseCode = partyInfoUrlConnection.getResponseCode();
          assertThat(partyInfoResponseCode).isEqualTo(HttpURLConnection.HTTP_OK);

          JsonReader jsonReader = Json.createReader(partyInfoUrlConnection.getInputStream());

          JsonObject partyInfoObject = jsonReader.readObject();

          assertThat(partyInfoObject).isNotNull();
          assertThat(partyInfoObject.getJsonArray("keys")).hasSize(1);
          assertThat(partyInfoObject.getJsonArray("keys").getJsonObject(0).getString("key"))
              .isEqualTo(publicKey);
        });

    Given(
        "^the mock AKV server has stubs for the endpoints used to store secrets$",
        () -> {
          final String authScenario = "AUTH";
          final String received401 = "RECEIVED_401";

          final String authenticateHeader =
              String.format(
                  "Bearer authorization=%s, resource=%s",
                  azureKeyVaultServerHolder.get().getAddress().getHostName() + "/auth",
                  azureKeyVaultServerHolder.get().getAddress().getHostName());

          //                wireMockServer
          //                    .get()
          //                    .stubFor(
          //                        put(urlPathEqualTo(nodeAPubUrl))
          //                            .inScenario(authScenario)
          //                            .whenScenarioStateIs(Scenario.STARTED)
          //                            .willSetStateTo(received401)
          //                            .willReturn(
          //                                unauthorized().withHeader("WWW-Authenticate",
          // authenticateHeader)));
          //
          //                wireMockServer
          //                    .get()
          //                    .stubFor(
          //                        post(urlPathEqualTo(authUrl))
          //                            .willReturn(
          //                                okJson(
          //                                    "{ \"access_token\": \"my-token\", \"token_type\":
          // \"Bearer\", \"expires_in\": \"3600\", \"expires_on\": \"1388444763\", \"resource\":
          // \"https://resource/\", \"refresh_token\": \"some-val\", \"scope\": \"some-val\",
          // \"id_token\": \"some-val\"}")
          //                                    .withHeader(
          //                                        "client-request-id",
          //                                        "{{request.headers.client-request-id}}")
          //                                    .withTransformers("response-template")));
          //
          //                wireMockServer
          //                    .get()
          //                    .stubFor(
          //                        put(urlPathEqualTo(nodeAPubUrl))
          //                            .inScenario(authScenario)
          //                            .whenScenarioStateIs(received401)
          //                            .willReturn(ok()));
          //
          //
          // wireMockServer.get().stubFor(put(urlPathEqualTo(nodeAKeyUrl)).willReturn(ok()));
          //
          // wireMockServer.get().stubFor(put(urlPathEqualTo(nodeBPubUrl)).willReturn(ok()));
          //
          // wireMockServer.get().stubFor(put(urlPathEqualTo(nodeBKeyUrl)).willReturn(ok()));
        });

    When(
        "^Tessera keygen is run with the following CLI args and AKV environment variables$",
        (String cliArgs) -> {
          Map<String, Object> params = Map.of("azureKeyVaultUrl", azureKeyVaultUrl);

          Path tempTesseraConfig =
              ElUtil.createTempFileFromTemplate(
                  getClass().getResource("/vault/tessera-azure-config.json"), params);
          // tempTesseraConfig.toFile().deleteOnExit();

          final String jarfile = System.getProperty("application.jar");

          final URL logbackConfigFile = NodeExecManager.class.getResource("/logback-test.xml");

          final URL truststore = getClass().getResource("/certificates/truststore.jks");

          String formattedArgs = String.format(cliArgs, azureKeyVaultUrl);

          Path startScript = Paths.get(System.getProperty("keyvault.azure.dist"));

          final List<String> args =
              new ExecArgsBuilder().withStartScript(startScript).withArg("--debug").build();

          args.addAll(Arrays.asList(formattedArgs.split(" ")));

          List<String> jvmArgs = new ArrayList<>();
          jvmArgs.add("-Djavax.net.ssl.trustStore=" + truststore.getFile());
          jvmArgs.add("-Djavax.net.ssl.trustStorePassword=testtest");
          jvmArgs.add("-Dspring.profiles.active=disable-unixsocket");
          jvmArgs.add("-Dlogback.configurationFile=" + logbackConfigFile.getFile());
          jvmArgs.add("-Ddebug=true");

          startTessera(
              args, jvmArgs, null); // node is not started during keygen so do not want to verify
        });

    Then(
        "^key pairs nodeA and nodeB will have been added to the AKV$",
        () -> {
          // nodeAPub is the first key to be PUT so request 1 returns 401 and request 2 is done
          // after auth
          //     wireMockServer.get().verify(2, putRequestedFor(urlPathEqualTo(nodeAPubUrl)));

          // the nodeAPub 401 response is cached by the client so auth is automatically attempted
          // before the
          // other PUTs
          //                wireMockServer.get().verify(1,
          // putRequestedFor(urlPathEqualTo(nodeAKeyUrl)));
          //                wireMockServer.get().verify(1,
          // putRequestedFor(urlPathEqualTo(nodeBPubUrl)));
          //                wireMockServer.get().verify(1,
          // putRequestedFor(urlPathEqualTo(nodeBKeyUrl)));

          // each PUT url (nodeAPub, nodeAKey, nodeBPub, nodeBKey) is authenticated

          // wireMockServer.get().verify(4, postRequestedFor(urlEqualTo(authUrl)));
          fail("test not implemented");
        });
  }

  // TODO(cjh) abstract out so can be shared by all vault ITs
  private void startTessera(List<String> args, List<String> jvmArgs, Path verifyConfig)
      throws Exception {
    LOGGER.info("Starting: {}", String.join(" ", args));
    String jvmArgsStr = String.join(" ", jvmArgs);
    LOGGER.info("JVM Args: {}", jvmArgsStr);

    ProcessBuilder tesseraProcessBuilder = new ProcessBuilder(args);

    Map<String, String> tesseraEnvironment = tesseraProcessBuilder.environment();
    tesseraEnvironment.put(AZURE_CLIENT_ID, "my-client-id");
    tesseraEnvironment.put(AZURE_CLIENT_SECRET, "my-client-secret");
    tesseraEnvironment.put("AZURE_TENANT_ID", "my-tenant-id");
    tesseraEnvironment.put(
        "JAVA_OPTS",
        jvmArgsStr); // JAVA_OPTS is read by start script and is used to provide jvm args

    try {
      tesseraProcess.set(tesseraProcessBuilder.redirectErrorStream(true).start());
    } catch (NullPointerException ex) {
      ex.printStackTrace();
      throw new NullPointerException("Check that application.jar property has been set");
    }

    executorService.submit(
        () -> {
          try (BufferedReader reader =
              Stream.of(tesseraProcess.get().getInputStream())
                  .map(InputStreamReader::new)
                  .map(BufferedReader::new)
                  .findAny()
                  .get()) {

            String line;
            while ((line = reader.readLine()) != null) {
              System.out.println(line);
            }

          } catch (IOException ex) {
            throw new UncheckedIOException(ex);
          }
        });

    CountDownLatch startUpLatch = new CountDownLatch(1);

    if (Objects.nonNull(verifyConfig)) {
      final Config config = JaxbUtil.unmarshal(Files.newInputStream(verifyConfig), Config.class);

      final URL bindingUrl =
          UriBuilder.fromUri(config.getP2PServerConfig().getBindingUri())
              .path("upcheck")
              .build()
              .toURL();

      executorService.submit(
          () -> {
            while (true) {
              try {
                HttpURLConnection conn = (HttpURLConnection) bindingUrl.openConnection();
                conn.connect();

                System.out.println(bindingUrl + " started." + conn.getResponseCode());

                startUpLatch.countDown();
                return;
              } catch (IOException ex) {
                try {
                  TimeUnit.MILLISECONDS.sleep(200L);
                } catch (InterruptedException ex1) {
                }
              }
            }
          });

      boolean started = startUpLatch.await(30, TimeUnit.SECONDS);

      if (!started) {
        System.err.println(bindingUrl + " Not started. ");
      }
    }

    executorService.submit(
        () -> {
          try {
            int exitCode = tesseraProcess.get().waitFor();
            startUpLatch.countDown();
            if (0 != exitCode) {
              System.err.println("Tessera node exited with code " + exitCode);
            }
          } catch (InterruptedException ex) {
            ex.printStackTrace();
          }
        });

    startUpLatch.await(30, TimeUnit.SECONDS);
  }
}
