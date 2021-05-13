package net.consensys.tessera.migration.data;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;
import javax.json.Json;
import javax.json.JsonObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class PayloadTypeTest {

  private JsonObject jsonObject;

  private PayloadType payloadType;

  public PayloadTypeTest(Map.Entry<PayloadType, JsonObject> params) {
    this.payloadType = params.getKey();
    this.jsonObject = params.getValue();
  }

  @Test
  public void parsePayloadType() throws Exception {
    try (ByteArrayOutputStream cborEndcoded = new ByteArrayOutputStream()) {
      JacksonObjectMapperFactory.create().writeValue(cborEndcoded, jsonObject);

      PayloadType result = PayloadType.parsePayloadType(cborEndcoded.toByteArray());
      assertThat(result).isEqualTo(payloadType);
      assertThat(result.getValue()).isEqualTo(payloadType.getValue());
    }
  }

  @Parameterized.Parameters(name = "{0}")
  public static List<Map.Entry<PayloadType, JsonObject>> params() {

    return List.of(
        Map.entry(
            PayloadType.ENCRYPTED_PAYLOAD,
            Json.createObjectBuilder().add("sender", "SomeSender").build()),
        Map.entry(
            PayloadType.QUERY_PRIVACY_GROUP_PAYLOAD,
            Json.createObjectBuilder()
                .add("addresses", Json.createArrayBuilder())
                .add("toDelete", true)
                .build()),
        Map.entry(
            PayloadType.PRIVACY_GROUP_PAYLOAD,
            Json.createObjectBuilder()
                .add("addresses", Json.createArrayBuilder())
                .add("type", "SomeType")
                .build()));
  }
}
