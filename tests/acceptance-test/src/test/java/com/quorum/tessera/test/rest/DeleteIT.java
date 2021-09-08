package com.quorum.tessera.test.rest;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

import com.quorum.tessera.api.SendResponse;
import com.quorum.tessera.test.*;
import jakarta.ws.rs.core.Response;
import java.net.URLEncoder;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Base64;
import java.util.Collections;
import org.junit.Test;

public class DeleteIT {

  private static final String COUNT_ALL =
      "select count(*) from ENCRYPTED_TRANSACTION where hash = ?";

  private final PartyHelper partyHelper = PartyHelper.create();

  @Test
  public void deleteTransactionThatExists() throws Exception {
    // setup (sending in a tx)

    Party sender = partyHelper.getParties().findAny().get();

    Party recipient =
        partyHelper
            .getParties()
            .filter(p -> !p.getPublicKey().equals(sender.getPublicKey()))
            .findAny()
            .get();

    RestUtils utils = new RestUtils();
    byte[] txnData = utils.createTransactionData();

    Response response = utils.send(sender, txnData, Collections.singleton(recipient));
    assertThat(response.getStatus()).isEqualTo(201);

    final SendResponse sendResponse = response.readEntity(SendResponse.class);

    final String encodedHash = URLEncoder.encode(sendResponse.getKey(), UTF_8.toString());

    try (PreparedStatement statement = sender.getDatabaseConnection().prepareStatement(COUNT_ALL)) {
      statement.setBytes(1, Base64.getDecoder().decode(sendResponse.getKey()));
      try (ResultSet rs = statement.executeQuery()) {
        assertThat(rs.next()).isTrue();
        assertThat(rs.getLong(1)).isEqualTo(1);
      }
    }

    // delete it
    final Response resp =
        sender.getRestClientWebTarget().path("transaction").path(encodedHash).request().delete();

    // validate result
    assertThat(resp).isNotNull();
    assertThat(resp.getStatus()).isEqualTo(204);
  }

  @Test
  public void deleteTransactionThatDoesntExist() {

    final String madeupHash = Base64.getUrlEncoder().encodeToString("madeup".getBytes());

    Party party = partyHelper.getParties().findAny().get();

    final Response response =
        party
            .getRestClientWebTarget()
            .path("transaction")
            .path(madeupHash)
            .request()
            .buildDelete()
            .invoke();

    // validate result
    assertThat(response).isNotNull();
    assertThat(response.getStatus()).isEqualTo(404);
  }
}
