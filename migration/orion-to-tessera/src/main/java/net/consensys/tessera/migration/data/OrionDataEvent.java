package net.consensys.tessera.migration.data;

import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventTranslator;
import com.quorum.tessera.enclave.RecipientBox;
import com.quorum.tessera.encryption.PublicKey;
import net.consensys.orion.enclave.EncryptedPayload;

import java.util.Map;
import java.util.Optional;

public class OrionDataEvent implements EventTranslator<OrionDataEvent> {

    private Long eventNumber;

    private Long totalEventCount;

    private byte[] key = new byte[44];

    private byte[] payloadData = new byte[20000];

    private byte[] privacyGroupData = new byte[20000];

    private PayloadType payloadType;

    private Map<PublicKey, RecipientBox> recipientBoxMap;

    private EncryptedPayload encryptedPayload;

    private OrionDataEvent() {
    }

    static final EventFactory<OrionDataEvent> FACTORY = () -> new OrionDataEvent();

    private OrionDataEvent(byte[] key,byte[] payloadData,byte[] privacyGroupData,PayloadType payloadType,Long eventNumber,Long totalEventCount,Map<PublicKey, RecipientBox> recipientBoxMap) {
        this.key = key;
        this.eventNumber = eventNumber;
        this.totalEventCount = totalEventCount;
        this.payloadType = payloadType;
        this.recipientBoxMap = recipientBoxMap;
        this.payloadData = payloadData;
        this.privacyGroupData = privacyGroupData;
    }

    @Override
    public void translateTo(OrionDataEvent orionDataEvent, long seq) {
        orionDataEvent.key = key;
        orionDataEvent.eventNumber = eventNumber;
        orionDataEvent.totalEventCount = totalEventCount;
        orionDataEvent.payloadType = payloadType;
        orionDataEvent.recipientBoxMap = recipientBoxMap;
        orionDataEvent.payloadData = payloadData;
        orionDataEvent.privacyGroupData = privacyGroupData;
        orionDataEvent.encryptedPayload = encryptedPayload;
    }

    public void reset() {
        this.key = null;
        this.payloadData = null;
        this.totalEventCount = null;
        this.eventNumber = null;
        this.payloadType = null;
        this.recipientBoxMap = null;
        this.privacyGroupData = null;
        this.encryptedPayload = null;
    }

    public Long getEventNumber() {
        return eventNumber;
    }

    public Long getTotalEventCount() {
        return totalEventCount;
    }

    public byte[] getKey() {
        return key;
    }

    public byte[] getPayloadData() {
        return payloadData;
    }

    public PayloadType getPayloadType() {
        return payloadType;
    }

    public Optional<Map<PublicKey, RecipientBox>> getRecipientBoxMap() {
        return Optional.ofNullable(recipientBoxMap);
    }

    public void setRecipientBoxMap(Map<PublicKey, RecipientBox> recipientBoxMap) {
        this.recipientBoxMap = recipientBoxMap;
    }

    public Optional<EncryptedPayload> getEncryptedPayload() {
        return Optional.ofNullable(encryptedPayload);
    }

    public void setEncryptedPayload(EncryptedPayload encryptedPayload) {
        this.encryptedPayload = encryptedPayload;
    }

    public byte[] getPrivacyGroupData() {
        return privacyGroupData;
    }

    public static class Builder {

        private Long eventNumber;

        private Long totalEventCount;

        private byte[] key;

        private byte[] payloadData;

        private byte[] privacyGroupData;

        private PayloadType payloadType;

        private Map<PublicKey, RecipientBox> recipientBoxMap;

        public Builder withEventNumber(Long eventNumber) {
            this.eventNumber = eventNumber;
            return this;
        }

        public Builder withTotalEventCount(Long totalEventCount) {
            this.totalEventCount = totalEventCount;
            return this;
        }

        public Builder withKey(byte[] key) {
            this.key = key;
            return this;
        }

        public Builder withPayloadData(byte[] payloadData) {
            this.payloadData = payloadData;
            return this;
        }

        public Builder withPrivacyGroupData(byte[] privacyGroupData) {
            this.privacyGroupData = privacyGroupData;
            return this;
        }

        public Builder withPayloadType(PayloadType payloadType) {
            this.payloadType = payloadType;
            return this;
        }

        public Builder withRecipientBoxMap(Map<PublicKey, RecipientBox> recipientBoxMap) {
            this.recipientBoxMap = recipientBoxMap;
            return this;
        }

        private Builder() {
        }
        public static Builder create() {
            return new Builder();
        }

        public OrionDataEvent build() {
            return new OrionDataEvent(key,payloadData,privacyGroupData,payloadType,eventNumber,totalEventCount,recipientBoxMap);
        }
    }

    @Override
    public String toString() {
        return "OrionDataEvent{" +
            "eventNumber=" + eventNumber +
            ", totalEventCount=" + totalEventCount +
           ", key=" + key.length +" bytes " +
            ", payloadData=" + payloadData.length + " bytes " +
            ", privacyGroupData=" + Optional.ofNullable(privacyGroupData).map(b -> b.length).orElse(0) + " bytes " +
            ", payloadType=" + payloadType +
            ", recipientBoxMap=" + recipientBoxMap +
            '}';
    }
}
