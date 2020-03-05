package com.quorum.tessera.test.vault.azure;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.util.JaxbUtil;
import com.quorum.tessera.test.util.ElUtil;
import cucumber.api.java8.En;
import exec.NodeExecManager;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.ws.rs.core.UriBuilder;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.net.HttpURLConnection;
import java.net.URL;
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

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static com.quorum.tessera.config.util.EnvironmentVariables.AZURE_CLIENT_ID;
import static com.quorum.tessera.config.util.EnvironmentVariables.AZURE_CLIENT_SECRET;
import static org.assertj.core.api.Assertions.assertThat;

public class AzureStepDefs implements En {

    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private final AtomicReference<Process> tesseraProcess = new AtomicReference<>();
    private final AtomicReference<WireMockServer> wireMockServer = new AtomicReference<>();

    private final String publicKey = "BULeR8JyUWhiuuCMU/HLA0Q5pzkYT+cHII3ZKBey3Bo=";
    private final String privateKey = "Wl+xSyXVuuqzpvznOS7dOobhcn4C5auxkFRi7yLtgtA=";

    private final String authUrl = "/auth/oauth2/token";

    // /secrets/{name}/{version}
    // note: '/' must be provided at start of version arg, e.g. "/1". this is to allow urls with no version by setting
    // version arg to "".
    private final String urlFormat = "/secrets/%s%s";

    private final String publicKeyUrl = String.format(urlFormat, "Pub", "/bvfw05z4cbu11ra2g94e43v9xxewqdq7");
    private final String privateKeyUrl = String.format(urlFormat, "Key", "/0my1ora2dciijx5jq9gv07sauzs5wjo2");

    private final String nodeAPubUrl = String.format(urlFormat, "nodeAPub", "");
    private final String nodeAKeyUrl = String.format(urlFormat, "nodeAKey", "");
    private final String nodeBPubUrl = String.format(urlFormat, "nodeBPub", "");
    private final String nodeBKeyUrl = String.format(urlFormat, "nodeBKey", "");

    public AzureStepDefs() {

        //                Before(
        //                    () -> {
        //                        // only needed when running outside of maven build process
        //                        System.setProperty(
        //                            "application.jar",
        //
        // "/Users/chrishounsom/jpmc-tessera/tessera-dist/tessera-app/target/tessera-app-0.11-SNAPSHOT-app.jar");
        //                    });

        After(
                () -> {
                    if (wireMockServer.get() != null && wireMockServer.get().isRunning()) {
                        wireMockServer.get().stop();
                        System.out.println("Stopped WireMockServer...");
                    }

                    if (tesseraProcess.get() != null && tesseraProcess.get().isAlive()) {
                        tesseraProcess.get().destroy();
                        System.out.println("Stopped Tessera node...");
                    }
                });

        Given(
                "^the mock AKV server has been started$",
                () -> {
                    final URL keystore = getClass().getResource("/certificates/localhost-with-san-keystore.jks");

                    // wiremock configures an HTTP server by default.  Even though we'll only use the HTTPS server we
                    // dynamically assign the HTTP port to ensure the default of 8080 is not used
                    wireMockServer.set(
                            new WireMockServer(
                                    options()
                                            .dynamicPort()
                                            .dynamicHttpsPort()
                                            .keystoreType("JKS")
                                            .keystorePath(keystore.getFile())
                                            .keystorePassword("testtest")
                                            .extensions(new ResponseTemplateTransformer(false))
                                    //                            .notifier(new ConsoleNotifier(true)) //enable to turn
                                    // on verbose debug msgs
                                    ));

                    wireMockServer.get().start();
                });

        Given(
                "^the mock AKV server has stubs for the endpoints used to get secrets$",
                () -> {
                    // --- Authentication flow for the AKV client ---
                    // The AKV Client makes a request to GET a secret from the AKV.  If the response is 200 Success then
                    // no authentication is required and the secret is returned to the caller.  If the response is 401
                    // Unauthorized then the client proceeds to authenticate itself.  Any other response code results in
                    // error being returned and termination of the GET request.
                    // See https://docs.microsoft.com/en-us/rest/api/keyvault/getsecret/getsecret for the AKV HTTP
                    // GetSecret API docs
                    //
                    // The 401 Unauthorized response will contain a WWW-Authenticate header which the AKV Client uses to
                    // authenticate itself.
                    // The 'authorization' component of the header provides the url of the authorization server to be
                    // used.
                    // The 'resource' component provides the Azure resource (i.e. key vault) to request authentication
                    // for.
                    //
                    // To authenticate, the client POSTs to the {authorization}/oauth2/token endpoint, where
                    // {authorization}
                    // is the authorization server url provided in the WWW-Authenticate header.
                    // See
                    // https://docs.microsoft.com/en-us/azure/active-directory/develop/v1-protocols-oauth-code#use-the-authorization-code-to-request-an-access-token
                    // for more info.
                    //
                    // The WWW-Authenticate header is used in subsequent GETs to the same Azure resource to make
                    // authentication requests immediately instead of having to first make 401 GETs.

                    final String respFormat = "{ \"value\": \"%s\" }";

                    final String authScenario = "AUTH";
                    final String received401 = "RECEIVED_401";

                    final String authenticateHeader =
                            String.format(
                                    "Bearer authorization=%s, resource=%s",
                                    wireMockServer.get().baseUrl() + "/auth", wireMockServer.get().baseUrl());

                    wireMockServer
                            .get()
                            .stubFor(
                                    get(urlPathEqualTo(publicKeyUrl))
                                            .inScenario(authScenario)
                                            .whenScenarioStateIs(Scenario.STARTED)
                                            .willSetStateTo(received401)
                                            .willReturn(
                                                    unauthorized().withHeader("WWW-Authenticate", authenticateHeader)));

                    wireMockServer
                            .get()
                            .stubFor(
                                    post(urlPathEqualTo(authUrl))
                                            .willReturn(
                                                    okJson(
                                                                    "{ \"access_token\": \"my-token\", \"token_type\": \"Bearer\", \"expires_in\": \"3600\", \"expires_on\": \"1388444763\", \"resource\": \"https://resource/\", \"refresh_token\": \"some-val\", \"scope\": \"some-val\", \"id_token\": \"some-val\"}")
                                                            .withHeader(
                                                                    "client-request-id",
                                                                    "{{request.headers.client-request-id}}")
                                                            .withTransformers("response-template")));

                    wireMockServer
                            .get()
                            .stubFor(
                                    get(urlPathEqualTo(publicKeyUrl))
                                            .inScenario(authScenario)
                                            .whenScenarioStateIs(received401)
                                            .willReturn(okJson(String.format(respFormat, publicKey))));

                    wireMockServer
                            .get()
                            .stubFor(
                                    get(urlPathEqualTo(privateKeyUrl))
                                            .willReturn(okJson(String.format(respFormat, privateKey))));
                });

        When(
                "^Tessera is started with the correct AKV environment variables$",
                () -> {
                    Map<String, Object> params = new HashMap<>();
                    params.put("azureKeyVaultUrl", wireMockServer.get().baseUrl());

                    Path tempTesseraConfig =
                            ElUtil.createTempFileFromTemplate(
                                    getClass().getResource("/vault/tessera-azure-config.json"), params);
                    tempTesseraConfig.toFile().deleteOnExit();

                    final String jarfile = System.getProperty("application.jar");

                    final URL logbackConfigFile = NodeExecManager.class.getResource("/logback-node.xml");
                    Path pid = Paths.get(System.getProperty("java.io.tmpdir"), "pidA.pid");

                    final URL truststore = getClass().getResource("/certificates/truststore.jks");

                    List<String> args =
                            new ArrayList<>(
                                    Arrays.asList(
                                            "java",
                                            // we set the truststore so that Tessera can trust the wiremock server
                                            "-Djavax.net.ssl.trustStore=" + truststore.getFile(),
                                            "-Djavax.net.ssl.trustStorePassword=testtest",
                                            "-Dspring.profiles.active=disable-unixsocket",
                                            "-Dlogback.configurationFile=" + logbackConfigFile.getFile(),
                                            "-Ddebug=true",
                                            "-jar",
                                            jarfile,
                                            "-configfile",
                                            tempTesseraConfig.toString(),
                                            "-pidfile",
                                            pid.toAbsolutePath().toString(),
                                            "-jdbc.autoCreateTables",
                                            "true"));

                    startTessera(args, tempTesseraConfig);
                });

        Then(
                "^Tessera will retrieve the key pair from AKV$",
                () -> {
                    wireMockServer.get().verify(2, postRequestedFor(urlEqualTo(authUrl)));
                    wireMockServer.get().verify(3, getRequestedFor(urlPathEqualTo(publicKeyUrl)));
                    wireMockServer.get().verify(2, getRequestedFor(urlPathEqualTo(privateKeyUrl)));

                    final URL partyInfoUrl =
                            UriBuilder.fromUri("http://localhost").port(8080).path("partyinfo").build().toURL();

                    HttpURLConnection partyInfoUrlConnection = (HttpURLConnection) partyInfoUrl.openConnection();
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
                                    wireMockServer.get().baseUrl() + "/auth", wireMockServer.get().baseUrl());

                    wireMockServer
                            .get()
                            .stubFor(
                                    put(urlPathEqualTo(nodeAPubUrl))
                                            .inScenario(authScenario)
                                            .whenScenarioStateIs(Scenario.STARTED)
                                            .willSetStateTo(received401)
                                            .willReturn(
                                                    unauthorized().withHeader("WWW-Authenticate", authenticateHeader)));

                    wireMockServer
                            .get()
                            .stubFor(
                                    post(urlPathEqualTo(authUrl))
                                            .willReturn(
                                                    okJson(
                                                                    "{ \"access_token\": \"my-token\", \"token_type\": \"Bearer\", \"expires_in\": \"3600\", \"expires_on\": \"1388444763\", \"resource\": \"https://resource/\", \"refresh_token\": \"some-val\", \"scope\": \"some-val\", \"id_token\": \"some-val\"}")
                                                            .withHeader(
                                                                    "client-request-id",
                                                                    "{{request.headers.client-request-id}}")
                                                            .withTransformers("response-template")));

                    wireMockServer
                            .get()
                            .stubFor(
                                    put(urlPathEqualTo(nodeAPubUrl))
                                            .inScenario(authScenario)
                                            .whenScenarioStateIs(received401)
                                            .willReturn(ok()));

                    wireMockServer.get().stubFor(put(urlPathEqualTo(nodeAKeyUrl)).willReturn(ok()));
                    wireMockServer.get().stubFor(put(urlPathEqualTo(nodeBPubUrl)).willReturn(ok()));
                    wireMockServer.get().stubFor(put(urlPathEqualTo(nodeBKeyUrl)).willReturn(ok()));
                });

        When(
                "^Tessera keygen is run with the following CLI args and AKV environment variables$",
                (String cliArgs) -> {
                    Map<String, Object> params = new HashMap<>();
                    params.put("azureKeyVaultUrl", wireMockServer.get().baseUrl());

                    Path tempTesseraConfig =
                            ElUtil.createTempFileFromTemplate(
                                    getClass().getResource("/vault/tessera-azure-config.json"), params);
                    tempTesseraConfig.toFile().deleteOnExit();

                    final String jarfile = System.getProperty("application.jar");

                    final URL logbackConfigFile = NodeExecManager.class.getResource("/logback-test.xml");

                    final URL truststore = getClass().getResource("/certificates/truststore.jks");

                    String formattedArgs = String.format(cliArgs, wireMockServer.get().baseUrl());

                    List<String> args = new ArrayList<>();
                    args.addAll(
                            Arrays.asList(
                                    "java",
                                    // we set the truststore so that Tessera can trust the wiremock server
                                    "-Djavax.net.ssl.trustStore=" + truststore.getFile(),
                                    "-Djavax.net.ssl.trustStorePassword=testtest",
                                    "-Dspring.profiles.active=disable-unixsocket",
                                    "-Dlogback.configurationFile=" + logbackConfigFile.getFile(),
                                    "-Ddebug=true",
                                    "-jar",
                                    jarfile));
                    args.addAll(Arrays.asList(formattedArgs.split(" ")));

                    startTessera(args, null); // node is not started during keygen so do not want to verify
                });

        Then(
                "^key pairs nodeA and nodeB will have been added to the AKV$",
                () -> {
                    // nodeAPub is the first key to be PUT so request 1 returns 401 and request 2 is done after auth
                    wireMockServer.get().verify(2, putRequestedFor(urlPathEqualTo(nodeAPubUrl)));
                    // the nodeAPub 401 response is cached by the client so auth is automatically attempted before the
                    // other PUTs
                    wireMockServer.get().verify(1, putRequestedFor(urlPathEqualTo(nodeAKeyUrl)));
                    wireMockServer.get().verify(1, putRequestedFor(urlPathEqualTo(nodeBPubUrl)));
                    wireMockServer.get().verify(1, putRequestedFor(urlPathEqualTo(nodeBKeyUrl)));
                    // each PUT url (nodeAPub, nodeAKey, nodeBPub, nodeBKey) is authenticated
                    wireMockServer.get().verify(4, postRequestedFor(urlEqualTo(authUrl)));
                });
    }

    private void startTessera(List<String> args, Path verifyConfig) throws Exception {
        System.out.println(String.join(" ", args));

        ProcessBuilder tesseraProcessBuilder = new ProcessBuilder(args);

        Map<String, String> tesseraEnvironment = tesseraProcessBuilder.environment();
        tesseraEnvironment.put(AZURE_CLIENT_ID, "my-client-id");
        tesseraEnvironment.put(AZURE_CLIENT_SECRET, "my-client-secret");

        try {
            tesseraProcess.set(tesseraProcessBuilder.redirectErrorStream(true).start());
        } catch (NullPointerException ex) {
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
                    UriBuilder.fromUri(config.getP2PServerConfig().getBindingUri()).path("upcheck").build().toURL();

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
