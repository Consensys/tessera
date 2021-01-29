package net.consensys.tessera.migration.data;

import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventTranslator;
import com.quorum.tessera.enclave.RecipientBox;
import com.quorum.tessera.encryption.PublicKey;

import javax.json.JsonObject;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class OrionEvent implements EventTranslator<OrionEvent> {

    private Long eventNumber;

    private Long totalEventCount;

    private PayloadType payloadType;

    private JsonObject jsonObject;

    private byte[] key;

    private Map<PublicKey, RecipientBox> recipientBoxMap;

    static final EventFactory<OrionEvent> FACTORY = () -> new OrionEvent();

    private OrionEvent() {
    }

    public OrionEvent(PayloadType payloadType,JsonObject jsonObject,byte[] key, Long totalEventCount,Long eventNumber,Map<PublicKey, RecipientBox> recipientBoxMap) {
        this.key = key;
        this.jsonObject = jsonObject;
        this.payloadType = payloadType;
        this.totalEventCount = totalEventCount;
        this.eventNumber = eventNumber;
        this.recipientBoxMap = recipientBoxMap;
    }

    @Override
    public void translateTo(OrionEvent orionEvent, long sequence) {
        orionEvent.key = key;
        orionEvent.jsonObject = jsonObject;
        orionEvent.payloadType = payloadType;
        orionEvent.totalEventCount = totalEventCount;
        orionEvent.eventNumber = eventNumber;
    }

    public void reset() {
        this.key = null;
        this.jsonObject = null;
        this.payloadType = null;
        this.totalEventCount = null;
        this.eventNumber = null;
    }

    public byte[] getKey() {
        return key;
    }

    public JsonObject getJsonObject() {
        return jsonObject;
    }

    public PayloadType getPayloadType() {
        return payloadType;
    }

    public Long getTotalEventCount() {
        return totalEventCount;
    }

    public Long getEventNumber() {
        return eventNumber;
    }


    public Optional<Map<PublicKey, RecipientBox>> getRecipientBoxMap() {
        return Optional.ofNullable(recipientBoxMap);
    }

    @Override
    public String toString() {
        return "OrionEvent{" +
            "eventNumber=" + eventNumber +
            ", totalEventCount=" + totalEventCount +
            ", payloadType=" + payloadType +
            ", jsonObject=" + jsonObject +
            '}';
    }

    public static class Builder {

        public static Builder create() {
            return new Builder();
        }

        private Long totalEventCount;

        private Long eventNumber;

        private PayloadType payloadType;

        private JsonObject jsonObject;

        private byte[] key;

        private Map<PublicKey,RecipientBox> recipientBoxMap;

        public Builder withTotalEventCount(Long totalEventCount) {
            this.totalEventCount = totalEventCount;
            return this;
        }

        public Builder withEventNumber(Long eventNumber) {
            this.eventNumber = eventNumber;
            return this;
        }

        public Builder withRecipientBoxMap(Map<PublicKey,RecipientBox> recipientBoxMap) {
            this.recipientBoxMap = recipientBoxMap;
            return this;
        }

        public Builder withPayloadType(PayloadType payloadType) {
            this.payloadType = payloadType;
            return this;
        }

        public Builder withJsonObject(JsonObject jsonObject) {
            this.jsonObject = jsonObject;
            return this;
        }

        public Builder withKey(byte[] key) {
            this.key = key;
            return this;
        }

        public OrionEvent build() {

            Objects.requireNonNull(totalEventCount);
            Objects.requireNonNull(payloadType);
            Objects.requireNonNull(jsonObject);
            Objects.requireNonNull(key);
            Objects.requireNonNull(eventNumber);

            if (payloadType == PayloadType.ENCRYPTED_PAYLOAD) {
                Objects.requireNonNull(recipientBoxMap);
                assert !recipientBoxMap.isEmpty();
            }

            if (totalEventCount < eventNumber) {
                throw new IllegalArgumentException("Event number is greater than the total");
            }

            return new OrionEvent(payloadType,jsonObject,key,totalEventCount,eventNumber,recipientBoxMap);
        }

    }

}
