package com.quorum.tessera.test.vault.aws;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AwsKeyVaultHttpHandler implements HttpHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(AwsKeyVaultHttpHandler.class);

  private Map<String, List<JsonObject>> requests = new TreeMap<>();

  private AtomicInteger counter = new AtomicInteger(0);

  private final String publicKey = "BULeR8JyUWhiuuCMU/HLA0Q5pzkYT+cHII3ZKBey3Bo=";

  @Override
  public void handle(HttpExchange exchange) throws IOException {

    String method = exchange.getRequestMethod();
    LOGGER.debug("method {} ", method);
    exchange.getRequestHeaders().entrySet().stream()
        .forEach(
            e -> {
              LOGGER.debug("{} = {}", e.getKey(), e.getValue());
              // exchange.getRequestHeaders().add(e.getKey(),String.join(",",e.getValue()));
            });

    RequestHandler<JsonObject> requestHandler =
        new RequestHandler<>() {
          @Override
          public JsonObject handle(HttpExchange exchange) throws IOException {
            return Json.createReader(exchange.getRequestBody()).readObject();
          }
        };

    JsonObject jsonObject = requestHandler.handle(exchange);

    LOGGER.debug("Body : {}", jsonObject);

    counter.incrementAndGet();

    String requestTarget = exchange.getRequestHeaders().getFirst("X-amz-target");
    requests.putIfAbsent(requestTarget, new ArrayList<>());
    requests.get(requestTarget).add(jsonObject);

    java.util.function.Predicate<HttpExchange> filterByTargerName =
        e -> e.getRequestHeaders().getFirst("X-amz-target").equals("secretsmanager.GetSecretValue");

    final ResponseHander<JsonObject> r;
    if ("secretsmanager.GetSecretValue".equals(requestTarget)) {

      r =
          (exch, o) -> {
            JsonObject json =
                Json.createObjectBuilder()
                    .add("ARN", "arn")
                    .add("CreatedDate", 121211444L)
                    .add("Name", "publicKey")
                    .addNull("SecretBinary")
                    .add("SecretString", publicKey)
                    .add("VersionId", "123")
                    .add("VersionStages", Json.createArrayBuilder().add("stage1"))
                    .build();

            byte[] data = json.toString().getBytes();

            exch.sendResponseHeaders(200, data.length);
            exch.getResponseBody().write(data);
          };

    } else if ("secretsmanager.CreateSecret".equals(requestTarget)) {

      r =
          (exch, o) -> {
            JsonObject json =
                Json.createObjectBuilder()
                    .add("ARN", "Some String Value")
                    .add("Name", jsonObject.getString("Name"))
                    .add("VersionId", jsonObject.getString("ClientRequestToken"))
                    .build();

            byte[] data = json.toString().getBytes();

            exch.sendResponseHeaders(200, data.length);
            exch.getResponseBody().write(data);
          };
    } else {
      throw new UnsupportedOperationException(requestTarget + " what you talkin about willis?");
    }

    r.handle(exchange, jsonObject);

    exchange.close();
  }

  public int getCounter() {
    return counter.intValue();
  }

  public Map<String, List<JsonObject>> getRequests() {
    return Map.copyOf(requests);
  }

  @FunctionalInterface
  interface RequestHandler<T> {
    T handle(HttpExchange exchange) throws IOException;
  }

  @FunctionalInterface
  interface ResponseHander<T> {

    void handle(HttpExchange exchange, T request) throws IOException;
  }
}
