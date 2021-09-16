package com.quorum.tessera.jaxrs.unixsocket;

import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentProvider;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.BytesContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.unixsocket.client.HttpClientTransportOverUnixSockets;
import org.glassfish.jersey.client.ClientRequest;
import org.glassfish.jersey.client.ClientResponse;
import org.glassfish.jersey.client.spi.AsyncConnectorCallback;
import org.glassfish.jersey.client.spi.Connector;
import org.glassfish.jersey.message.internal.Statuses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JerseyUnixSocketConnector implements Connector {

  private static final Logger LOGGER = LoggerFactory.getLogger(JerseyUnixSocketConnector.class);

  private HttpClient httpClient;

  private URI unixfile;

  public JerseyUnixSocketConnector(URI unixfile) {
    this.unixfile = unixfile;
    String unixFilePath = Paths.get(unixfile).toFile().getAbsolutePath();

    httpClient = new HttpClient(new HttpClientTransportOverUnixSockets(unixFilePath));
    try {
      httpClient.start();
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  @Override
  public ClientResponse apply(ClientRequest request) {

    try {
      return doApply(request);
    } catch (Exception ex) {
      throw new ProcessingException(ex);
    }
  }

  private ClientResponse doApply(ClientRequest request) throws Exception {

    HttpMethod httpMethod = HttpMethod.valueOf(request.getMethod());
    final URI originalUri = request.getUri();
    final URI uri;
    Path basePath = Paths.get(unixfile);

    if (originalUri.getScheme().startsWith("unix")) {

      String path = originalUri.getRawPath().replaceFirst(basePath.toString(), "");

      LOGGER.trace("Extracted path {} from {}", path, originalUri.getRawPath());

      uri =
          UriBuilder.fromUri(originalUri)
              .replacePath(path)
              .scheme("http")
              .port(99)
              .host("localhost")
              .build();

      LOGGER.trace("Created psuedo uri {} for originalUri {}", uri, originalUri);
    } else {
      uri = originalUri;
    }

    Request clientRequest = httpClient.newRequest(uri).method(httpMethod);

    MultivaluedMap<String, Object> headers = request.getHeaders();

    headers.keySet().stream()
        .forEach(
            name -> {
              headers
                  .get(name)
                  .forEach(
                      value -> {
                        clientRequest.header(name, Objects.toString(value));
                      });
            });

    if (request.hasEntity()) {
      final long length = request.getLengthLong();

      try (ByteArrayOutputStream bout = new ByteArrayOutputStream()) {

        request.setStreamProvider((int contentLength) -> bout);
        request.writeEntity();

        ContentProvider content = new BytesContentProvider(bout.toByteArray());
        clientRequest.content(content);
      }
    }
    final ContentResponse contentResponse = clientRequest.send();

    int statusCode = contentResponse.getStatus();
    String reason = contentResponse.getReason();

    LOGGER.trace(
        "uri {}, method: {},statusCode:{},reason: {} ", uri, httpMethod, statusCode, reason);

    final Response.StatusType status = Statuses.from(statusCode, reason);

    ClientResponse response = new ClientResponse(status, request);
    contentResponse.getHeaders().stream()
        .forEach(
            header -> {
              response.headers(header.getName(), (Object[]) header.getValues());
            });

    response.setEntityStream(new ByteArrayInputStream(contentResponse.getContent()));
    return response;
  }

  @Override
  public Future<?> apply(final ClientRequest request, final AsyncConnectorCallback callback) {

    try {
      callback.response(doApply(request));
    } catch (IOException ex) {
      callback.failure(new ProcessingException(ex));
    } catch (Throwable t) {
      callback.failure(t);
    }

    return CompletableFuture.completedFuture(null);
  }

  @Override
  public String getName() {
    return getClass().getSimpleName();
  }

  @Override
  public void close() {
    try {
      httpClient.stop();
    } catch (Exception ex) {

    }
  }
}
