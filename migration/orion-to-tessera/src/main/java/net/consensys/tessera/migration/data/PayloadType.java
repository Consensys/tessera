package net.consensys.tessera.migration.data;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Predicate;
import net.consensys.orion.enclave.EncryptedPayload;
import net.consensys.orion.enclave.PrivacyGroupPayload;
import net.consensys.orion.enclave.QueryPrivacyGroupPayload;

public enum PayloadType {
  ENCRYPTED_PAYLOAD(EncryptedPayload.class, j -> j.contains("sender")),
  PRIVACY_GROUP_PAYLOAD(
      PrivacyGroupPayload.class, j -> j.contains("addresses") && j.contains("type")),
  QUERY_PRIVACY_GROUP_PAYLOAD(
      QueryPrivacyGroupPayload.class, j -> j.contains("addresses") && j.contains("toDelete"));

  private final Class type;

  private Predicate<Collection<String>> predicate;

  PayloadType(Class type, Predicate<Collection<String>> predicate) {
    this.predicate = predicate;
    this.type = type;
  }

  static PayloadType get(Collection<String> fieldNames) {
    return Arrays.stream(PayloadType.values())
        .filter(p -> p.predicate.test(fieldNames))
        .findFirst()
        .get();
  }

  public Class getType() {
    return type;
  }

  public String getValue() {
    return type.getSimpleName();
  }

  static PayloadType findByType(Class clazz) {
    return Arrays.stream(PayloadType.values())
        .filter(p -> Objects.equals(p.type, clazz))
        .findFirst()
        .get();
  }

  static PayloadType parsePayloadType(byte[] payloadData) {

    final JsonFactory jacksonJsonFactory = JacksonObjectMapperFactory.createFactory();

    Collection<String> jsonObjectBuilder = new ArrayList<>();
    try (JsonParser jacksonJsonParser = jacksonJsonFactory.createParser(payloadData)) {

      while (!jacksonJsonParser.isClosed()) {

        JsonToken jsonToken = jacksonJsonParser.nextToken();
        if (JsonToken.FIELD_NAME.equals(jsonToken)) {
          String fieldname = jacksonJsonParser.getCurrentName();
          jsonObjectBuilder.add(fieldname);
        }
      }
      return PayloadType.get(jsonObjectBuilder);

    } catch (IOException ex) {
      throw new UncheckedIOException(ex);
    }
  }
}
