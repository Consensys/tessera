package com.quorum.tessera.test.rest;

import com.quorum.tessera.test.Party;
import static com.quorum.tessera.test.rest.RawHeaderName.RECIPIENTS;
import static com.quorum.tessera.test.rest.RawHeaderName.SENDER;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class RestUtils {

    private Client client = ClientBuilder.newClient();

    public Response sendRaw(Party sender, byte[] transactionData, Party... recipients) {

        Objects.requireNonNull(sender);
        
        String recipientString = Stream.of(recipients)
            .map(Party::getPublicKey)
            .collect(Collectors.joining(","));

        Invocation.Builder invocationBuilder = client.target(sender.getUri())
            .path("sendraw").request().header(SENDER, sender.getPublicKey());

        Optional.of(recipientString)
            .filter(s -> !Objects.equals("", s))
            .ifPresent(s -> invocationBuilder.header(RECIPIENTS, s));
        
        return invocationBuilder
            .post(Entity.entity(transactionData, MediaType.APPLICATION_OCTET_STREAM));
    }


    public Stream<Response> findTransaction(String transactionId, Party... party) {

        String encodedId = urlEncode(transactionId);

        return Stream.of(party)
            .map(p -> client.target(p.getUri()))
            .map(target -> target.path("transaction"))
            .map(target -> target.path(encodedId))
            .map(target -> target.request().get());

    }

    static String urlEncode(String data) {
        try {
            return URLEncoder.encode(data, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        }
    }

    public byte[] createTransactionData() {
        Random random = new Random();
        byte[] bytes = new byte[random.nextInt(500)];
        random.nextBytes(bytes);
        return bytes;
    }

}
