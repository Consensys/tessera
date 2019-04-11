package com.quorum.tessera.enclave.websockets;

import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.enclave.EncodedPayloadBuilder;
import com.quorum.tessera.encryption.PublicKey;

import javax.json.*;
import javax.websocket.DecodeException;
import javax.websocket.EncodeException;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;
import java.util.List;
import java.util.stream.Collectors;

public class EncodedPayloadCodec extends JsonCodec<EncodedPayload> {

    @Override
    public JsonObjectBuilder doEncode(EncodedPayload payload) throws EncodeException {

        final Encoder base64Encoder = Base64.getEncoder();

        final JsonObjectBuilder jsonBuilder = Json.createObjectBuilder();

        final String cipherText = base64Encoder.encodeToString(payload.getCipherText());
        jsonBuilder.add("cipherText", cipherText);

        final String cipherTextNonce = base64Encoder.encodeToString(payload.getCipherTextNonce().getNonceBytes());

        jsonBuilder.add("cipherTextNonce", cipherTextNonce);

        final JsonArrayBuilder recipientBoxes = Json.createArrayBuilder();

        payload.getRecipientBoxes().stream()
            .map(base64Encoder::encodeToString)
            .forEach(recipientBoxes::add);

        jsonBuilder.add("recipientBoxes", recipientBoxes);

        final JsonArrayBuilder recipientKeys = Json.createArrayBuilder();

        payload.getRecipientKeys().stream()
            .map(PublicKey::getKeyBytes)
            .map(base64Encoder::encodeToString)
            .forEach(recipientKeys::add);

        jsonBuilder.add("recipientKeys", recipientKeys);

        final String recipientNonce = base64Encoder.encodeToString(payload.getRecipientNonce().getNonceBytes());

        final String senderKey = base64Encoder.encodeToString(payload.getSenderKey().getKeyBytes());

        return jsonBuilder.add("recipientNonce", recipientNonce)
            .add("senderKey", senderKey);

    }

    @Override
    public EncodedPayload doDecode(JsonObject jsonObject) throws DecodeException {

        Decoder base64Decoder = Base64.getDecoder();

        byte[] cipherText = base64Decoder.decode(jsonObject.getString("cipherText"));

        byte[] cipherTextNonce = base64Decoder.decode(jsonObject.getString("cipherTextNonce"));

        List<byte[]> recipientBoxes = jsonObject.getJsonArray("recipientBoxes")
            .stream()
            .map(JsonString.class::cast)
            .map(JsonString::getString)
            .map(base64Decoder::decode)
            .collect(Collectors.toList());

        List<PublicKey> recipientKeys = jsonObject.getJsonArray("recipientKeys")
            .stream()
            .map(JsonString.class::cast)
            .map(JsonString::getString)
            .map(base64Decoder::decode)
            .map(PublicKey::from)
            .collect(Collectors.toList());

        byte[] recipientNonce = base64Decoder.decode(jsonObject.getString("recipientNonce"));

        PublicKey senderKey = PublicKey.from(base64Decoder.decode(jsonObject.getString("senderKey")));

        return EncodedPayloadBuilder.create()
            .withCipherText(cipherText)
            .withCipherTextNonce(cipherTextNonce)
            .withRecipientBoxes(recipientBoxes)
            .withRecipientKeys(recipientKeys.toArray(new PublicKey[0]))
            .withSenderKey(senderKey)
            .withRecipientNonce(recipientNonce)
            .build();

    }


}
