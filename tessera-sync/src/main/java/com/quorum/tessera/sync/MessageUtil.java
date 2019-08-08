package com.quorum.tessera.sync;

import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.enclave.PayloadEncoder;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.partyinfo.PartyInfoParser;
import com.quorum.tessera.partyinfo.model.PartyInfo;
import java.util.Base64;
import java.util.Optional;

public class MessageUtil {

    private static final PartyInfoParser PARTYINFO_PARSER = PartyInfoParser.create();

    private static final PayloadEncoder PAYLOAD_ENCODER = PayloadEncoder.create();

    private static final Base64.Decoder BASE64_DECODER = Base64.getDecoder();

    private static final Base64.Encoder BASE64_ENCODER = Base64.getEncoder();

    public MessageUtil() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }

    public static PublicKey decodePublicKeyFromBase64(String publicKeyData) {
        return Optional.of(publicKeyData).map(BASE64_DECODER::decode).map(PublicKey::from).get();
    }

    public static EncodedPayload decodeTransactionsFromBase64(String transactionData) {
        return Optional.of(transactionData).map(BASE64_DECODER::decode).map(PAYLOAD_ENCODER::decode).get();
    }

    public static PartyInfo decodePartyInfoFromBase64(String partyInfoData) {
        PartyInfo partyInfo = Optional.of(partyInfoData).map(BASE64_DECODER::decode).map(PARTYINFO_PARSER::from).get();
        return partyInfo;
    }

    public static String encodeToBase64(PartyInfo partyInfo) {
        return Optional.of(partyInfo).map(PARTYINFO_PARSER::to).map(BASE64_ENCODER::encodeToString).get();
    }

    public static String encodeToBase64(EncodedPayload transactions) {
        return Optional.of(transactions).map(PAYLOAD_ENCODER::encode).map(BASE64_ENCODER::encodeToString).get();
    }

    public static String encodeToBase64(PublicKey publicKey) {
        return publicKey.encodeToBase64();
    }
}
