package net.consensys.tessera.migration.data;

import javax.json.JsonObject;
import java.util.Arrays;
import java.util.function.Predicate;

public enum PayloadType {
    ENCRYPTED_PAYLOAD("EncryptedPayload", j -> j.containsKey("sender")),
    PRIVACY_GROUP_PAYLOAD("PrivacyGroupPayload", j -> j.containsKey("addresses") && j.containsKey("type")),
    QUERY_PRIVACY_GROUP_PAYLOAD(
            "QueryPrivacyGroupPayload", j -> j.containsKey("addresses") && j.containsKey("toDelete"));

    private final String value;

    private Predicate<JsonObject> predicate;

    PayloadType(String value, Predicate<JsonObject> predicate) {
        this.predicate = predicate;
        this.value = value;
    }

    static PayloadType get(JsonObject jsonObject) {
        return Arrays.stream(PayloadType.values()).filter(p -> p.predicate.test(jsonObject)).findFirst().get();
    }
}
