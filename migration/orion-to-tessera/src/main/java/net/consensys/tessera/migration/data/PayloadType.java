package net.consensys.tessera.migration.data;

import javax.json.JsonObject;
import java.util.Arrays;
import java.util.function.Predicate;

public enum PayloadType {
    EncryptedPayload(j -> j.containsKey("sender")),
    PrivacyGroupPayload(j -> j.containsKey("addresses") && j.containsKey("type")),
    QueryPrivacyGroupPayload(j -> j.containsKey("addresses") && j.containsKey("toDelete"));

    private Predicate<JsonObject> predicate;

    PayloadType(Predicate<JsonObject> predicate) {
        this.predicate = predicate;
    }

    static PayloadType get(JsonObject jsonObject) {
        return Arrays.stream(PayloadType.values()).filter(p -> p.predicate.test(jsonObject)).findFirst().get();
    }
}
