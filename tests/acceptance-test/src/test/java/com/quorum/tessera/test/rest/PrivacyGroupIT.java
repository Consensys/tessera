package com.quorum.tessera.test.rest;

import static org.assertj.core.api.Assertions.*;

import com.quorum.tessera.test.Party;
import com.quorum.tessera.test.PartyHelper;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.StringReader;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Test;

public class PrivacyGroupIT {

  private final PartyHelper partyHelper = PartyHelper.create();

  private final PrivacyGroupTestUtil privacyGroupTestUtil = new PrivacyGroupTestUtil();

  @Test
  public void testCreate() {

    final String output = privacyGroupTestUtil.create("A", "B");
    final JsonObject jsonObj = Json.createReader(new StringReader(output)).readObject();

    assertThat(jsonObj.getString("privacyGroupId").length()).isEqualTo(44);
    assertThat(jsonObj.getString("name")).isEqualTo("Organisation [A, B]");
    assertThat(jsonObj.getString("description"))
        .isEqualTo("Contains members of Organisation [A, B]");
    assertThat(jsonObj.getString("type")).isEqualTo("PANTHEON");

    final List<String> members =
        jsonObj.getJsonArray("members").getValuesAs(JsonString.class).stream()
            .map(JsonString::getString)
            .collect(Collectors.toList());

    assertThat(members)
        .containsExactlyInAnyOrder(
            partyHelper.findByAlias("A").getPublicKey(),
            partyHelper.findByAlias("B").getPublicKey());
  }

  @Test
  public void testRetrieve() {

    final String output = privacyGroupTestUtil.create("C", "D");
    final JsonObject jsonObj = Json.createReader(new StringReader(output)).readObject();
    final String privacyGroupId = jsonObj.getString("privacyGroupId");

    final String nodeCResult = privacyGroupTestUtil.retrieve("C", privacyGroupId);
    final JsonObject nodeCJson = Json.createReader(new StringReader(nodeCResult)).readObject();

    assertThat(nodeCJson.getString("privacyGroupId")).isEqualTo(privacyGroupId);
    assertThat(nodeCJson.getString("name")).isEqualTo("Organisation [C, D]");
    assertThat(nodeCJson.getString("description"))
        .isEqualTo("Contains members of Organisation [C, D]");
    assertThat(nodeCJson.getString("type")).isEqualTo("PANTHEON");

    final List<String> members =
        nodeCJson.getJsonArray("members").getValuesAs(JsonString.class).stream()
            .map(JsonString::getString)
            .collect(Collectors.toList());
    assertThat(members)
        .containsExactlyInAnyOrder(
            partyHelper.findByAlias("C").getPublicKey(),
            partyHelper.findByAlias("D").getPublicKey());

    final String nodeDResult = privacyGroupTestUtil.retrieve("D", privacyGroupId);
    final JsonObject nodeDJson = Json.createReader(new StringReader(nodeDResult)).readObject();
    assertThat(nodeDJson).isEqualTo(nodeCJson);

    // Retrieve will fail on node A as it's not a member
    assertThatThrownBy(() -> privacyGroupTestUtil.retrieve("A", privacyGroupId))
        .isInstanceOf(AssertionError.class)
        .hasMessageContaining("404");
  }

  @Test
  public void testFind() {
    String pg1 = privacyGroupTestUtil.create("A", "B", "D");
    final JsonObject output1 = Json.createReader(new StringReader(pg1)).readObject();
    String pg2 = privacyGroupTestUtil.create("D", "B", "A");
    final JsonObject output2 = Json.createReader(new StringReader(pg2)).readObject();

    String resultA = privacyGroupTestUtil.find("A", "A", "B", "D");
    final JsonArray nodeAJson = Json.createReader(new StringReader(resultA)).readArray();
    assertThat(nodeAJson.size()).isEqualTo(2);
    assertThat(nodeAJson.getValuesAs(JsonObject.class)).containsExactlyInAnyOrder(output1, output2);

    String resultB = privacyGroupTestUtil.find("B", "A", "B", "D");
    final JsonArray nodeBJson = Json.createReader(new StringReader(resultB)).readArray();
    assertThat(nodeBJson.size()).isEqualTo(2);
    assertThat(nodeBJson.getValuesAs(JsonObject.class)).containsExactlyInAnyOrder(output1, output2);

    String resultC = privacyGroupTestUtil.find("C", "A", "B", "D");
    final JsonArray nodeCJson = Json.createReader(new StringReader(resultC)).readArray();
    assertThat(nodeCJson.size()).isEqualTo(0);

    String resultD = privacyGroupTestUtil.find("D", "A", "B", "D");
    final JsonArray nodeDJson = Json.createReader(new StringReader(resultD)).readArray();
    assertThat(nodeDJson.size()).isEqualTo(2);
    assertThat(nodeDJson.getValuesAs(JsonObject.class)).containsExactlyInAnyOrder(output1, output2);
  }

  @Test
  public void testDelete() {
    final String output = privacyGroupTestUtil.create("C", "A", "B", "D");
    final JsonObject jsonObj = Json.createReader(new StringReader(output)).readObject();
    final String privacyGroupId = jsonObj.getString("privacyGroupId");
    final Party sender = partyHelper.findByAlias("C");

    privacyGroupTestUtil.retrieve("A", privacyGroupId);
    privacyGroupTestUtil.retrieve("B", privacyGroupId);
    privacyGroupTestUtil.retrieve("C", privacyGroupId);
    privacyGroupTestUtil.retrieve("D", privacyGroupId);

    JsonObject json =
        Json.createObjectBuilder()
            .add("from", sender.getPublicKey())
            .add("privacyGroupId", privacyGroupId)
            .build();

    final Response response =
        sender
            .getRestClient()
            .target(sender.getQ2TUri())
            .path("/deletePrivacyGroup")
            .request()
            .post(Entity.entity(json, MediaType.APPLICATION_JSON));

    assertThat(response.getStatus()).isEqualTo(200);

    assertThatThrownBy(() -> privacyGroupTestUtil.retrieve("A", privacyGroupId))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(() -> privacyGroupTestUtil.retrieve("B", privacyGroupId))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(() -> privacyGroupTestUtil.retrieve("C", privacyGroupId))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(() -> privacyGroupTestUtil.retrieve("D", privacyGroupId))
        .isInstanceOf(AssertionError.class);
  }
}
