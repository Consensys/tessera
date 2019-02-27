package com.quorum.tessera.enclave.websockets;

import com.quorum.tessera.enclave.RawTransaction;
import com.quorum.tessera.enclave.RawTransactionBuilder;
import com.quorum.tessera.encryption.PublicKey;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;
import java.util.Optional;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

public class RawTransactionCodec extends JsonCodec<RawTransaction> {

    private Encoder base64Encoder = Base64.getEncoder();

    @Override
    protected JsonObjectBuilder doEncode(RawTransaction txn) throws Exception {

        String encodedEncyptedKey = base64Encoder.encodeToString(txn.getEncryptedKey());
        String encodedEncyptedPayload = base64Encoder.encodeToString(txn.getEncryptedPayload());

        String encodedFromPublicKey = base64Encoder.encodeToString(txn.getFrom().getKeyBytes());

        String encodedNonce = base64Encoder.encodeToString(txn.getNonce().getNonceBytes());

        return Json.createObjectBuilder()
                .add("encryptedKey", encodedEncyptedKey)
                .add("encryptedPayload", encodedEncyptedPayload)
                .add("from", encodedFromPublicKey)
                .add("nonce", encodedNonce);

    }

    private Decoder base64Decoder = Base64.getDecoder();

    @Override
    protected RawTransaction doDecode(JsonObject json) throws Exception {

        byte[] encryptedKey = Optional.of("encryptedKey")
                .map(json::getString)
                .map(base64Decoder::decode)
                .get();

        byte[] encryptedPayload = Optional.of("encryptedPayload")
                .map(json::getString)
                .map(base64Decoder::decode)
                .get();

        PublicKey from = Optional.of("from")
                .map(json::getString)
                .map(base64Decoder::decode)
                .map(PublicKey::from)
                .get();

        byte[] nonce = Optional.of("nonce")
                .map(json::getString)
                .map(base64Decoder::decode)
                .get();

        return RawTransactionBuilder.create()
                .withEncryptedKey(encryptedKey)
                .withEncryptedPayload(encryptedPayload)
                .withFrom(from)
                .withNonce(nonce)
                .build();
    }

}
