package com.jpmorgan.quorum.enclave.websockets;

import com.quorum.tessera.encryption.PublicKey;
import java.io.StringReader;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;
import java.util.List;
import java.util.stream.Collectors;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.websocket.DecodeException;
import javax.websocket.EncodeException;

public class EnclaveRequestCodec extends CodecAdapter<EnclaveRequest> {

    private static final Encoder BASE64_ENCODER = Base64.getEncoder();
    
    private static final Decoder BASE64_DECODER = Base64.getDecoder();
    
    @Override
    public String doEncode(EnclaveRequest request) throws EncodeException {

        EnclaveRequestType enclaveRequestType = request.getType();

        JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();

        if(enclaveRequestType.getParamTypes().size() != request.getArgs().size()) {
            throw new IllegalStateException("Arg desciptors and and args differ in length");
        }
        
        for (int i = 0; i < enclaveRequestType.getParamTypes().size(); i++) {
            Object value = request.getArgs().get(i);

            ArgType type = enclaveRequestType.getParamTypes().get(i);

            if (type == ArgType.PUBLIC_KEY_LIST) {
                JsonArrayBuilder nestedBuilder = Json.createArrayBuilder();
                List<PublicKey> publicKeys = List.class.cast(value);

                publicKeys.forEach((k) -> {
                    nestedBuilder.add(BASE64_ENCODER.encodeToString(k.getKeyBytes()));
                });
                jsonArrayBuilder.add(nestedBuilder);
                continue;
            } 
            
            if(type == ArgType.BYTE_ARRAY) {
                String encodedValue = BASE64_ENCODER.encodeToString((byte[]) value);
                jsonArrayBuilder.add(encodedValue);
                continue;
            }
            
            if(type == ArgType.PUBLIC_KEY) {
                PublicKey publicKey = PublicKey.class.cast(value);
                String encodedKey = BASE64_ENCODER.encodeToString(publicKey.getKeyBytes());
                jsonArrayBuilder.add(encodedKey);
                continue;
            }

        }

        return Json.createObjectBuilder()
                .add("type", request.getType().name())
                .add("args", jsonArrayBuilder)
                .build()
                .toString();
    }

    @Override
    public EnclaveRequest doDecode(String s) throws DecodeException {

        try (JsonReader jsonReader = Json.createReader(new StringReader(s))){

            JsonObject json = jsonReader.readObject();
            EnclaveRequestType enclaveRequestType = EnclaveRequestType.valueOf(json.getString("type"));

            JsonArray args = json.getJsonArray("args");
            
            EnclaveRequest.Builder requestBuilder = EnclaveRequest.Builder.create()
                    .withType(enclaveRequestType);
            
            for(int i = 0; i < args.size();i++) {
                ArgType type = enclaveRequestType.getParamTypes().get(i);
                if(type == ArgType.BYTE_ARRAY) {
                    String encodedValue = args.getString(i);
                    byte[] decodedValue = Base64.getDecoder().decode(encodedValue);
                    requestBuilder.withArg(decodedValue);
                }
                
                if(type == ArgType.PUBLIC_KEY) {
                    String encodedValue = args.getString(i);
                    byte[] decodedValue = Base64.getDecoder().decode(encodedValue);
                    requestBuilder.withArg(PublicKey.from(decodedValue));
                }
                
                if(type == ArgType.PUBLIC_KEY_LIST) {
                    
                    List<PublicKey> publicKeys = args.getJsonArray(i).stream()
                            .map(JsonString.class::cast)
                            .map(JsonString::getString)
                            .map(BASE64_DECODER::decode)
                            .map(PublicKey::from)
                            .collect(Collectors.toList());
                    
                    requestBuilder.withArg(publicKeys);
                }
                
                
            }
            
            
            return requestBuilder.build();

        }
    }


}
