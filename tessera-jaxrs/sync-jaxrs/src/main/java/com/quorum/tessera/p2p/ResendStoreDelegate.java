package com.quorum.tessera.sync;

import com.quorum.tessera.data.MessageHash;

/**
 * Use polymorphic inheritance to allow transaction manager impl to persist transactions during replay mode.
 *
 * <p>//TODO: We should create and implement ResendStoreDelegate and compose object rather than using polymorphism
 */
public interface ResendStoreDelegate {

    MessageHash storePayloadBypassResendMode(byte[] payload);
}
