package net.consensys.tessera.migration.data;

import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventTranslator;
import javax.json.JsonObject;

public class OrionEvent implements EventTranslator<OrionEvent> {

    private Long eventNumber;

    private Long totalEventCount;

    private PayloadType payloadType;

    private JsonObject jsonObject;

    private byte[] key;

    private byte[] value;


    static final EventFactory<OrionEvent> FACTORY = () -> new OrionEvent();

    private OrionEvent() {
    }

    public OrionEvent(PayloadType payloadType,JsonObject jsonObject,byte[] key, byte[] value,Long totalEventCount,Long eventNumber) {
        this.key = key;
        this.value = value;
        this.jsonObject = jsonObject;
        this.payloadType = payloadType;
        this.totalEventCount = totalEventCount;
        this.eventNumber = eventNumber;
    }

    @Override
    public void translateTo(OrionEvent orionEvent, long sequence) {
        orionEvent.key = key;
        orionEvent.value = value;
        orionEvent.jsonObject = jsonObject;
        orionEvent.payloadType = payloadType;
        orionEvent.totalEventCount = totalEventCount;
        orionEvent.eventNumber = eventNumber;
    }

    public void reset() {
        this.key = null;
        this.value = null;
        this.jsonObject = null;
        this.payloadType = null;
        this.totalEventCount = null;
        this.eventNumber = null;
    }

    public byte[] getKey() {
        return key;
    }

    public byte[] getValue() {
        return value;
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



    @Override
    public String toString() {
        return "OrionEvent{" +
            "eventNumber=" + eventNumber +
            ", totalEventCount=" + totalEventCount +
            ", payloadType=" + payloadType +
            ", jsonObject=" + jsonObject +
            '}';
    }
}
