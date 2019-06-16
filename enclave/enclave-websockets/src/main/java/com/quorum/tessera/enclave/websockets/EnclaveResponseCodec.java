package com.quorum.tessera.enclave.websockets;

import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

public class EnclaveResponseCodec extends JsonCodec<EnclaveResponse> {

    private static final Base64.Encoder BASE64_ENCODER = Base64.getEncoder();

    private static final Base64.Decoder BASE64_DECODER = Base64.getDecoder();

    private Map<EnclaveResponseType, JsonCodec> lookup = new HashMap<>();

    public EnclaveResponseCodec() {
        lookup.put(EnclaveResponseType.PUBLIC_KEY, new PublicKeyCodec());
        lookup.put(EnclaveResponseType.PUBLIC_KEYS, new PublicKeySetCodec());
        lookup.put(EnclaveResponseType.STATUS, new StatusCodec());
        lookup.put(EnclaveResponseType.RAW_TXN, new RawTransactionCodec());
        lookup.put(EnclaveResponseType.ENCODED_PAYLOAD, new EncodedPayloadCodec());
    }

    @Override
    protected JsonObjectBuilder doEncode(EnclaveResponse response) throws Exception {

        EnclaveResponseType enclaveResponseType = response.getRequestType().getResponseType();

        final String payload;
        if (enclaveResponseType == EnclaveResponseType.BYTES) {
            ByteBuffer payloadData = (ByteBuffer) response.getPayload();
            payload = BASE64_ENCODER.encodeToString(payloadData.array());
        } else {
            payload = lookup.get(enclaveResponseType).encode(response.getPayload());
        }

        return Json.createObjectBuilder()
                .add("requestType", response.getRequestType().name())
                .add("payload", payload);

    }

    @Override
    protected EnclaveResponse doDecode(JsonObject json) throws Exception {

        EnclaveRequestType enclaveRequestType = EnclaveRequestType.valueOf(json.getString("requestType"));

        EnclaveResponseType enclaveResponseType = enclaveRequestType.getResponseType();

        String encodedPayload = json.getString("payload");

        if (enclaveResponseType == EnclaveResponseType.BYTES) {
            byte[] data = BASE64_DECODER.decode(encodedPayload);
            return new EnclaveResponse(enclaveRequestType, ByteBuffer.wrap(data));
        } else {
            Object o = lookup.get(enclaveResponseType).decode(encodedPayload);
            return new EnclaveResponse(enclaveRequestType, o);
        }
    }

}
