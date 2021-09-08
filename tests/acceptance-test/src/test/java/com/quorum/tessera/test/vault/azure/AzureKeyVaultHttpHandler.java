package com.quorum.tessera.test.vault.azure;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AzureKeyVaultHttpHandler implements HttpHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(AzureKeyVaultHttpHandler.class);

  private AtomicInteger counter = new AtomicInteger(0);

  private final String publicKey = "BULeR8JyUWhiuuCMU/HLA0Q5pzkYT+cHII3ZKBey3Bo=";

  private final String keyVaultUrl;

  public AzureKeyVaultHttpHandler(String keyVaultUrl) {
    this.keyVaultUrl = keyVaultUrl;
  }

  @Override
  public void handle(HttpExchange exchange) throws IOException {
    LOGGER.info("HttpExchange getRequestMethod {}", exchange.getRequestMethod());
    LOGGER.info("HttpExchange getRequestURI {}", exchange.getRequestURI());
    LOGGER.info("HttpExchange content type {}", exchange.getRequestHeaders().get("Content-type"));
    counter.incrementAndGet();

    exchange
        .getRequestHeaders()
        .entrySet()
        .forEach(
            e -> {
              LOGGER.info("HttpExchange Header: {} = {}", e.getKey(), e.getValue());
              exchange.getResponseHeaders().add(e.getKey(), String.join(",", e.getValue()));
            });

    //        exchange.getResponseHeaders().add("WWW-Authenticate",
    //            String.format("Bearer authorization=%s/auth, resource=%s",keyVaultUrl));

    if (exchange.getRequestURI().toString().startsWith("/secrets/Pub/")) {
      JsonObject jsonObject = Json.createObjectBuilder().add("value", publicKey).build();

      byte[] response = jsonObject.toString().getBytes();
      exchange.sendResponseHeaders(200, response.length);
      exchange.getResponseBody().write(response);

      LOGGER.info("response send  {}", new String(response));

      exchange.close();
    } else {

      exchange.sendResponseHeaders(200, 0);
      exchange.close();
    }
  }

  public int getCounter() {
    return counter.intValue();
  }
}
