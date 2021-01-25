package net.consensys.tessera.migration.data;

import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventTranslator;
import com.quorum.tessera.encryption.PublicKey;
import net.consensys.orion.enclave.EncryptedKey;
import net.consensys.orion.enclave.EncryptedPayload;
import net.consensys.orion.enclave.PrivacyGroupPayload;

import java.util.*;
import java.util.stream.Collectors;

public class OrionRecordEvent implements EventTranslator<OrionRecordEvent> {

    private PayloadType payloadType;

    private String key;

    private byte[] value;

    private EncryptedPayload encryptedPayload;

    private Map<String, EncryptedKey> recipientKeyToBoxes;

    private PrivacyGroupPayload privacyGroupPayload;

    private InputType inputType;

    static final EventFactory<OrionRecordEvent> FACTORY = () -> new OrionRecordEvent();

    private boolean ok = true;

    private OrionRecordEvent() {
        this.key = null;
        this.encryptedPayload = null;
        this.recipientKeyToBoxes = null;
        this.value = null;
        this.inputType = null;
        this.payloadType = null;
    }

    public OrionRecordEvent(PayloadType payloadType,InputType inputType, String key, byte[] value) {
        this.key = Objects.requireNonNull(key);
        this.value = Objects.requireNonNull(value);
        this.inputType = Objects.requireNonNull(inputType);
        this.payloadType = Objects.requireNonNull(payloadType);
    }

    public Map<PublicKey, EncryptedKey> getRecipientKeyToBoxes() {
        return recipientKeyToBoxes.entrySet().stream()
            .sorted(Map.Entry.<String, EncryptedKey>comparingByKey())
            .collect(
                Collectors.toMap(
                    e ->
                        Optional.of(e.getKey())
                            .map(Base64.getDecoder()::decode)
                            .map(PublicKey::from)
                            .get(),
                    e ->
                        Optional.of(e.getValue())
                            //  .map(Base64.getDecoder()::decode)
                            .get(),
                    (l, r) -> l,
                    LinkedHashMap::new));
    }

    public EncryptedPayload getEncryptedPayload() {
        return encryptedPayload;
    }

    public String getKey() {
        return key;
    }

    public byte[] getValue() {
        return value;
    }

    public void setRecipientKeyToBoxes(Map<String, EncryptedKey> recipientKeyToBoxes) {
        this.recipientKeyToBoxes = recipientKeyToBoxes;
    }

    @Override
    public void translateTo(OrionRecordEvent event, long sequence) {
        event.recipientKeyToBoxes = recipientKeyToBoxes;
        event.encryptedPayload = encryptedPayload;
        event.key = key;
        event.value = value;
        event.inputType = inputType;
    }

    public void reset() {
        this.recipientKeyToBoxes = null;
        this.key = null;
        this.encryptedPayload = null;
        this.value = null;
        this.privacyGroupPayload = null;
        this.inputType = null;
        this.payloadType = null;
    }

    @Override
    public String toString() {
        return "OrionRecordEvent{"
                + "key='"
                + key
                + '\''
                + ", value="
                + Arrays.toString(value)
                + ", encryptedPayload="
                + encryptedPayload
                + ", recipientKeyToBoxes="
                + recipientKeyToBoxes
                + ", privacyGroupPayload="
                + privacyGroupPayload
                + ", inputType="
                + inputType
                + '}';
    }

    public void setEncryptedPayload(EncryptedPayload encryptedPayload) {
        this.encryptedPayload = encryptedPayload;
    }

    public void setPrivacyGroupPayload(PrivacyGroupPayload privacyGroupPayload) {
        this.privacyGroupPayload = privacyGroupPayload;
    }

    public Optional<PrivacyGroupPayload> getPrivacyGroupPayload() {
        return Optional.ofNullable(privacyGroupPayload);
    }

    public boolean isError() {
        return !ok;
    }

    public void error() {
        this.ok = false;
    }
}
