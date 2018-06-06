package com.github.nexus.transaction;

public interface PayloadEncoder {

    /**
     * Encodes the payload to a byte array
     *
     * @param encodedPayload the payload to encode
     * @return the byte array representing the encoded payload
     */
    byte[] encode(EncodedPayload encodedPayload);

    /**
     * Decodes a byte array back into an encrypted payload
     *
     * @param input The byte array to decode into an EncodedPayload
     * @return the decoded payload
     */
    EncodedPayload decode(byte[] input);

}
