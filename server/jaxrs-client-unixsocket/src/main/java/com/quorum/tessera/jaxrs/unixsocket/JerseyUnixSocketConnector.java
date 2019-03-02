package com.quorum.tessera.jaxrs.unixsocket;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import org.glassfish.jersey.client.ClientRequest;
import org.glassfish.jersey.client.ClientResponse;
import org.glassfish.jersey.client.spi.AsyncConnectorCallback;
import org.glassfish.jersey.client.spi.Connector;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentProvider;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.BytesContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.unixsocket.client.HttpClientTransportOverUnixSockets;
import org.glassfish.jersey.message.internal.Statuses;

public class JerseyUnixSocketConnector implements Connector {

    private HttpClient httpClient;

    public JerseyUnixSocketConnector(Path unixfile) {
        httpClient = new HttpClient(new HttpClientTransportOverUnixSockets(unixfile.toString()), null);
        try{
            httpClient.start();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public ClientResponse apply(ClientRequest request) {

        try{
            return doApply(request);
        } catch (Exception ex) {
            throw new ProcessingException(ex);
        }

    }

    private ClientResponse doApply(ClientRequest request) throws Exception {

        HttpMethod httpMethod = HttpMethod.valueOf(request.getMethod());

        URI uri = request.getUri();
        Request clientRequest = httpClient.newRequest(uri)
                .method(httpMethod);
        
        MultivaluedMap<String,Object> headers = request.getHeaders();
        
        headers.keySet().stream().forEach(name -> {
           headers.get(name).forEach(value -> {
               clientRequest.header(name, Objects.toString(value));
           });
           
        });

        if (request.hasEntity()) {
            final long length = request.getLengthLong();

            ByteArrayOutputStream bout = new ByteArrayOutputStream();

            request.setStreamProvider((int contentLength) -> bout);
            request.writeEntity();
            
            ContentProvider content = new BytesContentProvider(bout.toByteArray());
            clientRequest.content(content);

        }
        final ContentResponse contentResponse = clientRequest.send();

        int statusCode = contentResponse.getStatus();
        String reason = contentResponse.getReason();

        final Response.StatusType status = Statuses.from(statusCode, reason);

        ClientResponse response = new ClientResponse(status, request);
        contentResponse.getHeaders().stream().forEach(header -> {
            response.headers(header.getName(), header.getValues());
        });

        response.setEntityStream(new ByteArrayInputStream(contentResponse.getContent()));
        return response;

    }

    @Override
    public Future<?> apply(final ClientRequest request, final AsyncConnectorCallback callback) {

        try{
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
        try{
            httpClient.stop();
        } catch (Exception ex) {

        }
    }

}
