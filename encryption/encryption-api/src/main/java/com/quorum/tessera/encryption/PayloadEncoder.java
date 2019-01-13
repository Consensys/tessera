package com.quorum.tessera.encryption;

/**
 * Encodes and decodes a {@link EncodedPayload} to and from its binary
 * representation
 */
public interface PayloadEncoder {

    /**
     * Encodes the payload to a byte array
     *
     * @param encodedPayloadWithRecipients the payload to encode
     * @return the byte array representing the encoded payload
     */
    byte[] encode(EncodedPayloadWithRecipients encodedPayloadWithRecipients);

    /**
     * Decodes a byte array back into an encrypted payload
     *
     * @param input The byte array to decode into an EncodedPayload
     * @return the decoded payload
     */
    EncodedPayloadWithRecipients decodePayloadWithRecipients(byte[] input);
    
    
    //TODO: 
    EncodedPayloadWithRecipients decodePayloadWithRecipients(byte[] input, PublicKey recipient);

    static PayloadEncoder create() {
        return new PayloadEncoderImpl();
    }
    
}
