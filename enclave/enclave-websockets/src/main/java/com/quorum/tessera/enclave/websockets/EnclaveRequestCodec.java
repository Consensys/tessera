package com.quorum.tessera.enclave.websockets;

import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.enclave.RawTransaction;
import com.quorum.tessera.encryption.PublicKey;

import javax.json.*;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;
import java.util.List;
import java.util.stream.Collectors;

public class EnclaveRequestCodec extends JsonCodec<EnclaveRequest> {

    private static final Encoder BASE64_ENCODER = Base64.getEncoder();

    private static final Decoder BASE64_DECODER = Base64.getDecoder();

    @Override
    public JsonObjectBuilder doEncode(EnclaveRequest request) throws Exception {
        EnclaveRequestType enclaveRequestType = request.getType();

        JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();

        for (int i = 0; i < enclaveRequestType.getParamTypes().size(); i++) {
            Object value = request.getArgs().get(i);

            ArgType type = enclaveRequestType.getParamTypes().get(i);

            switch (type) {
                case PUBLIC_KEY_LIST:
                    JsonArrayBuilder nestedBuilder = Json.createArrayBuilder();
                    List<PublicKey> publicKeys = List.class.cast(value);

                    publicKeys.forEach(k -> nestedBuilder.add(BASE64_ENCODER.encodeToString(k.getKeyBytes())));
                    jsonArrayBuilder.add(nestedBuilder);
                    break;

                case BYTE_ARRAY:
                    String encodedValue = BASE64_ENCODER.encodeToString((byte[]) value);
                    jsonArrayBuilder.add(encodedValue);
                    break;

                case PUBLIC_KEY:
                    PublicKey publicKey = PublicKey.class.cast(value);
                    String encodedKey = BASE64_ENCODER.encodeToString(publicKey.getKeyBytes());
                    jsonArrayBuilder.add(encodedKey);
                    break;

                case RAW_TRANSACTION:
                    RawTransaction txn = RawTransaction.class.cast(value);
                    JsonObjectBuilder encoded = new RawTransactionCodec().doEncode(txn);
                    jsonArrayBuilder.add(encoded);
                    break;

                case ENCODED_PAYLOAD:
                    EncodedPayload encodedPayload = EncodedPayload.class.cast(value);
                    JsonObjectBuilder encodedObject = new EncodedPayloadCodec().doEncode(encodedPayload);
                    jsonArrayBuilder.add(encodedObject);
                    break;
            }

        }

        return Json.createObjectBuilder()
                .add("type", request.getType().name())
                .add("args", jsonArrayBuilder);
    }

    @Override
    public EnclaveRequest doDecode(JsonObject json) throws Exception {

        EnclaveRequestType enclaveRequestType = EnclaveRequestType.valueOf(json.getString("type"));

        JsonArray args = json.getJsonArray("args");

        EnclaveRequest.Builder requestBuilder = EnclaveRequest.Builder.create()
                .withType(enclaveRequestType);

        for (int i = 0; i < args.size(); i++) {

            final ArgType type = enclaveRequestType.getParamTypes().get(i);

            switch(type) {
                case BYTE_ARRAY:
                    String encodedValue = args.getString(i);
                    byte[] decodedValue = BASE64_DECODER.decode(encodedValue);
                    requestBuilder.withArg(decodedValue);
                    break;

                case PUBLIC_KEY:
                    String encodedKey = args.getString(i);
                    byte[] decodedKey = BASE64_DECODER.decode(encodedKey);
                    requestBuilder.withArg(PublicKey.from(decodedKey));
                    break;

                case PUBLIC_KEY_LIST:
                    List<PublicKey> publicKeys = args.getJsonArray(i).stream()
                        .map(JsonString.class::cast)
                        .map(JsonString::getString)
                        .map(BASE64_DECODER::decode)
                        .map(PublicKey::from)
                        .collect(Collectors.toList());

                    requestBuilder.withArg(publicKeys);
                    break;

                case RAW_TRANSACTION:
                    requestBuilder.withArg(new RawTransactionCodec().doDecode(args.getJsonObject(i)));
                    break;

                case ENCODED_PAYLOAD:
                    requestBuilder.withArg(new EncodedPayloadCodec().doDecode(args.getJsonObject(i)));
                    break;
            }
        }

        return requestBuilder.build();

    }


    
    
}
