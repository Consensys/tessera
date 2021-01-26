package net.consensys.tessera.migration.data;

import com.quorum.tessera.enclave.RecipientBox;
import com.quorum.tessera.encryption.PublicKey;

import javax.json.JsonObject;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

public class OrionEventFactory {

    private Long totalEventCount;

    private final AtomicLong eventNumber = new AtomicLong(0);

    public OrionEventFactory(Long totalEventCount) {
        this.totalEventCount = totalEventCount;
    }


    public OrionEvent create(PayloadType payloadType, byte[] key, byte[] value, JsonObject jsonObject, Map<PublicKey, RecipientBox> recipientBoxMap) {
        if (this.totalEventCount == null) {
            throw new IllegalStateException("Total event count is required");
        }

        Objects.requireNonNull(jsonObject);

        if (payloadType == PayloadType.ENCRYPTED_PAYLOAD) {
            Objects.requireNonNull(recipientBoxMap);
            assert !recipientBoxMap.isEmpty();
        }

        if (totalEventCount < eventNumber.incrementAndGet()) {
            throw new IllegalArgumentException("Event number is greater than the total");
        }

        return new OrionEvent(payloadType, jsonObject, key, value, totalEventCount, eventNumber.get(), recipientBoxMap);
    }


}
