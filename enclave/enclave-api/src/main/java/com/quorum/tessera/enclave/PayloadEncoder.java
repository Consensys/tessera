package com.quorum.tessera.enclave;

import com.quorum.tessera.ServiceLoaderUtil;
import com.quorum.tessera.encryption.PublicKey;

/** Encodes and decodes a {@link EncodedPayload} to and from its binary representation */
public interface PayloadEncoder {

    /**
     * Encodes the payload to a byte array
     *
     * @param payload the payload to encode
     * @return the byte array representing the encoded payload
     */
    byte[] encode(EncodedPayload payload);

    /**
     * Decodes a byte array back into an encrypted payload
     *
     * @param input The byte array to decode into an EncodedPayload
     * @return the decoded payload
     */
    EncodedPayload decode(byte[] input);

    /**
     * Strips a payload of any data that isn't relevant to the given recipient Used to format a payload before it is
     * sent to the target node
     *
     * @param input the full payload from which data needs to be stripped
     * @param recipient the recipient to retain information about
     * @return a payload which contains a subset of data from the input, which is relevant to the recipient
     */
    EncodedPayload forRecipient(EncodedPayload input, PublicKey recipient);

    /**
     * Checks whether recipientKeys is empty. If it is, it tries to add the specified recipient. If the recipientKeys
     * list is immutable then it creates a new EncodedPayload with the recipientKeys containing the specified recipient
     * and the rest of the fields copied from the input EncodedPayload.
     *
     * @param input the payload
     * @param recipient the recipient to add to recipientKeys
     * @return a payload which contains the added recipient key
     */
    EncodedPayload withRecipient(EncodedPayload input, PublicKey recipient);

    static PayloadEncoder create() {
        return ServiceLoaderUtil.load(PayloadEncoder.class).orElse(new PayloadEncoderImpl() {});
    }
}
